package es.hugoalvarezajenjo.selecta.services.recommendation;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRecommendationCriteria;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid Recommendation Engine that combines multiple signals:
 * 1. Tag Affinity (Weighted Jaccard)
 * 2. Collaborative Filtering (User-User CF with Cosine Similarity)
 * 3. Popularity (Wilson Confidence Score)
 * 4. Content Match (Soft filter scoring)
 * 5. Diversity (MMR — Maximal Marginal Relevance)
 *
 * Weights are adaptive based on user context (cold-start handling).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationEngine {

    private static final int MAX_NEIGHBORS = 5;
    private static final int MIN_COMMON_RATINGS = 2;
    private static final double MMR_LAMBDA = 0.7;

    private final SubjectRatingRepository ratingRepository;
    private final SubjectService subjectService;

    /**
     * Builds a user interest profile from their rating history.
     */
    @Transactional(readOnly = true)
    public UserInterestProfile buildUserProfile(User user) {
        if (user == null) {
            return UserInterestProfile.anonymous();
        }
        List<SubjectRating> ratings = ratingRepository.findByUserId(user.getId());
        return UserInterestProfile.build(ratings, true);
    }

    /**
     * Main recommendation method. Scores all active subjects and returns the top results
     * re-ranked with MMR for diversity.
     *
     * @param user     the current user (nullable for anonymous)
     * @param criteria filter criteria from the UI form
     * @param limit    maximum number of results to return
     * @return scored and ranked list of subject recommendations
     */
    @Transactional(readOnly = true)
    public List<SubjectScoreDTO> recommend(User user, SubjectRecommendationCriteria criteria, int limit) {
        log.info("Recommendation request — user: {}, context will be determined from profile",
                user != null ? user.getUsername() : "anonymous");

        UserInterestProfile profile = buildUserProfile(user);
        RecommendationWeights weights = RecommendationWeights.forContext(profile.getContext());

        log.info("User context: {}, profile tags: {}, rated subjects: {}",
                profile.getContext(), profile.getTagWeights().size(), profile.getRatedSubjects().size());
        log.debug("Weights applied — tagAffinity: {}, collaborative: {}, popularity: {}, contentMatch: {}, diversity: {}",
                weights.getTagAffinity(), weights.getCollaborative(), weights.getPopularity(),
                weights.getContentMatch(), weights.getDiversity());

        List<Subject> candidates = subjectService.getActiveSubjects();

        // Exclude already-rated subjects from recommendations
        candidates = candidates.stream()
                .filter(s -> !profile.getRatedSubjects().containsKey(s.getId()))
                .toList();

        log.debug("Candidates after exclusion of rated subjects: {}", candidates.size());

        // Pre-compute popularity data for all subjects
        Map<Long, PopularityData> popularityMap = computePopularityMap();

        // Pre-compute collaborative signal
        Map<Long, Double> collaborativeScores = computeCollaborativeScores(profile, candidates);

        log.debug("Collaborative signal computed for {} subjects", collaborativeScores.size());

        // Score each candidate
        List<ScoredCandidate> scored = candidates.stream()
                .map(subject -> scoreSubject(subject, profile, weights, criteria, popularityMap, collaborativeScores))
                .sorted(Comparator.comparingDouble(ScoredCandidate::totalScore).reversed())
                .toList();

        // Apply MMR for diversity re-ranking
        List<ScoredCandidate> diversified = applyMMR(scored, profile, limit);

        log.info("Recommendation complete — {} results returned (from {} candidates, limit {})",
                diversified.size(), scored.size(), limit);

        if (!diversified.isEmpty()) {
            log.debug("Top result: '{}' with score {}", diversified.get(0).subject().getName(),
                    String.format("%.3f", diversified.get(0).totalScore()));
        }

        // Convert to DTOs
        return diversified.stream()
                .map(sc -> toDTO(sc, popularityMap, profile))
                .toList();
    }

    // ──────────────────────────────────────────────────────────────────────
    // SIGNAL 1: Tag Affinity (Weighted Jaccard)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Computes weighted Jaccard similarity between user profile tags and subject tags.
     * Formula: Σ min(w_profile(t), w_subject(t)) / Σ max(w_profile(t), w_subject(t))
     */
    double computeTagAffinity(UserInterestProfile profile, Subject subject) {
        Map<String, Double> profileWeights = profile.getTagWeights();
        Set<String> subjectTags = subject.getTags();

        if (profileWeights.isEmpty() || subjectTags == null || subjectTags.isEmpty()) {
            return 0.0;
        }

        // Subject tags get uniform weight of 1.0
        Map<String, Double> subjectWeights = subjectTags.stream()
                .collect(Collectors.toMap(String::toLowerCase, t -> 1.0, (a, b) -> a));

        Set<String> allTags = new HashSet<>(profileWeights.keySet());
        allTags.addAll(subjectWeights.keySet());

        double sumMin = 0.0;
        double sumMax = 0.0;

        for (String tag : allTags) {
            double pWeight = profileWeights.getOrDefault(tag, 0.0);
            double sWeight = subjectWeights.getOrDefault(tag, 0.0);
            sumMin += Math.min(pWeight, sWeight);
            sumMax += Math.max(pWeight, sWeight);
        }

        return sumMax > 0 ? sumMin / sumMax : 0.0;
    }

    // ──────────────────────────────────────────────────────────────────────
    // SIGNAL 2: Collaborative Filtering (User-User CF)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Computes collaborative filtering scores for all candidates.
     * Finds similar users (neighbors) and aggregates their ratings.
     */
    Map<Long, Double> computeCollaborativeScores(UserInterestProfile profile, List<Subject> candidates) {
        if (profile.getRatedSubjects().isEmpty()) {
            return Map.of();
        }

        List<Long> ratedSubjectIds = new ArrayList<>(profile.getRatedSubjects().keySet());
        List<SubjectRating> relatedRatings = ratingRepository.findBySubjectIdIn(ratedSubjectIds);

        // Group ratings by user (excluding current user's own ratings)
        Map<Long, Map<Long, Integer>> otherUsersRatings = relatedRatings.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getId(),
                        Collectors.toMap(r -> r.getSubject().getId(), SubjectRating::getRating, (a, b) -> b)
                ));

        // Find top-N neighbors by cosine similarity
        List<Neighbor> neighbors = otherUsersRatings.entrySet().stream()
                .filter(entry -> countCommonRatings(profile.getRatedSubjects(), entry.getValue()) >= MIN_COMMON_RATINGS)
                .map(entry -> new Neighbor(entry.getKey(), cosineSimilarity(profile.getRatedSubjects(), entry.getValue()), entry.getValue()))
                .filter(n -> n.similarity() > 0)
                .sorted(Comparator.comparingDouble(Neighbor::similarity).reversed())
                .limit(MAX_NEIGHBORS)
                .toList();

        if (neighbors.isEmpty()) {
            return Map.of();
        }

        // Get all ratings from neighbors for candidate subjects
        Set<Long> candidateIds = candidates.stream().map(Subject::getId).collect(Collectors.toSet());
        Set<Long> neighborUserIds = neighbors.stream().map(Neighbor::userId).collect(Collectors.toSet());

        List<SubjectRating> neighborRatings = ratingRepository.findAll().stream()
                .filter(r -> neighborUserIds.contains(r.getUser().getId()))
                .filter(r -> candidateIds.contains(r.getSubject().getId()))
                .toList();

        // Aggregate: weighted average of neighbor ratings for each candidate
        Map<Long, List<WeightedRating>> aggregated = new HashMap<>();
        Map<Long, Double> neighborSimilarities = neighbors.stream()
                .collect(Collectors.toMap(Neighbor::userId, Neighbor::similarity));

        for (SubjectRating rating : neighborRatings) {
            Long subjectId = rating.getSubject().getId();
            Double similarity = neighborSimilarities.get(rating.getUser().getId());
            if (similarity != null) {
                aggregated.computeIfAbsent(subjectId, k -> new ArrayList<>())
                        .add(new WeightedRating(rating.getRating(), similarity));
            }
        }

        // Compute final score per subject: weighted average normalized to [0, 1]
        Map<Long, Double> scores = new HashMap<>();
        for (Map.Entry<Long, List<WeightedRating>> entry : aggregated.entrySet()) {
            double weightedSum = entry.getValue().stream()
                    .mapToDouble(wr -> wr.rating() * wr.weight())
                    .sum();
            double totalWeight = entry.getValue().stream()
                    .mapToDouble(WeightedRating::weight)
                    .sum();
            if (totalWeight > 0) {
                scores.put(entry.getKey(), (weightedSum / totalWeight) / 5.0);
            }
        }

        return scores;
    }

    /**
     * Cosine similarity between two users' rating vectors.
     */
    double cosineSimilarity(Map<Long, Integer> userA, Map<Long, Integer> userB) {
        Set<Long> common = new HashSet<>(userA.keySet());
        common.retainAll(userB.keySet());

        if (common.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Long subjectId : common) {
            double a = userA.get(subjectId);
            double b = userB.get(subjectId);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator > 0 ? dotProduct / denominator : 0.0;
    }

    private int countCommonRatings(Map<Long, Integer> userA, Map<Long, Integer> userB) {
        Set<Long> common = new HashSet<>(userA.keySet());
        common.retainAll(userB.keySet());
        return common.size();
    }

    // ──────────────────────────────────────────────────────────────────────
    // SIGNAL 3: Popularity (Wilson Confidence Score)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Wilson-like popularity score:
     * score = (avgRating / 5.0) * (1 - 1 / (1 + log(numRatings + 1)))
     *
     * This prevents a single 5★ review from dominating over 50 reviews averaging 4★.
     */
    double computePopularity(PopularityData data) {
        if (data == null || data.count() == 0) {
            return 0.0;
        }
        double normalizedRating = data.average() / 5.0;
        double confidenceFactor = 1.0 - (1.0 / (1.0 + Math.log(data.count() + 1)));
        return normalizedRating * confidenceFactor;
    }

    /**
     * Pre-computes popularity data for all subjects from all ratings.
     */
    Map<Long, PopularityData> computePopularityMap() {
        List<SubjectRating> allRatings = ratingRepository.findAllWithSubject();

        return allRatings.stream()
                .collect(Collectors.groupingBy(r -> r.getSubject().getId()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<SubjectRating> ratings = entry.getValue();
                            double avg = ratings.stream().mapToInt(SubjectRating::getRating).average().orElse(0.0);
                            return new PopularityData(avg, ratings.size());
                        }
                ));
    }

    // ──────────────────────────────────────────────────────────────────────
    // SIGNAL 4: Content Match (Soft Filters)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Computes how well a subject matches explicit user criteria (soft filters).
     * Each active filter contributes equally; score = satisfied / total active.
     */
    double computeContentMatch(Subject subject, SubjectRecommendationCriteria criteria) {
        if (criteria == null) {
            return 0.0;
        }

        int activeFilters = 0;
        int satisfied = 0;

        // Semester filter
        if (criteria.getSemesterTypes() != null && !criteria.getSemesterTypes().isEmpty()) {
            activeFilters++;
            if (matchesSemester(subject, criteria.getSemesterTypes())) {
                satisfied++;
            }
        }

        // Language filter
        if (criteria.getLanguage() != null) {
            activeFilters++;
            if (subject.getLanguages() != null && subject.getLanguages().contains(criteria.getLanguage())) {
                satisfied++;
            }
        }

        // Credits filter
        if (criteria.getMaxCredits() != null && criteria.getMaxCredits() > 0) {
            activeFilters++;
            if (subject.getCredits() <= criteria.getMaxCredits()) {
                satisfied++;
            }
        }

        // Tag filter (selected tags from chips)
        if (criteria.getSelectedTags() != null && !criteria.getSelectedTags().isEmpty()) {
            activeFilters++;
            if (subject.getTags() != null) {
                Set<String> subjectTagsLower = subject.getTags().stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
                boolean anyMatch = criteria.getSelectedTags().stream()
                        .map(String::toLowerCase)
                        .anyMatch(subjectTagsLower::contains);
                if (anyMatch) {
                    satisfied++;
                }
            }
        }

        return activeFilters > 0 ? (double) satisfied / activeFilters : 0.0;
    }

    private boolean matchesSemester(Subject subject, List<String> semesterTypes) {
        if (subject.getSemesters() == null || subject.getSemesters().isEmpty()) {
            return false;
        }
        Set<Semester> targetSemesters = new HashSet<>();
        if (semesterTypes.contains("ODD")) {
            targetSemesters.addAll(List.of(Semester.FIRST, Semester.THIRD, Semester.FIFTH, Semester.SEVENTH));
        }
        if (semesterTypes.contains("EVEN")) {
            targetSemesters.addAll(List.of(Semester.SECOND, Semester.FOURTH, Semester.SIXTH, Semester.EIGHTH));
        }
        return subject.getSemesters().stream().anyMatch(targetSemesters::contains);
    }

    // ──────────────────────────────────────────────────────────────────────
    // SIGNAL 5: Diversity (pre-MMR individual diversity signal)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Diversity signal: rewards subjects that introduce new tags not heavily covered
     * in the user's profile.
     * score = 1 - (overlapping tag weight / total subject tags)
     */
    double computeDiversity(UserInterestProfile profile, Subject subject) {
        if (subject.getTags() == null || subject.getTags().isEmpty()) {
            return 0.5; // Neutral for subjects without tags
        }
        if (profile.getTagWeights().isEmpty()) {
            return 1.0; // Maximum diversity for users without profile
        }

        Set<String> subjectTags = subject.getTags().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        double overlapWeight = subjectTags.stream()
                .mapToDouble(tag -> profile.getTagWeights().getOrDefault(tag, 0.0))
                .sum();

        double maxPossibleWeight = subjectTags.size(); // Each tag can contribute max 1.0

        return 1.0 - (overlapWeight / maxPossibleWeight);
    }

    // ──────────────────────────────────────────────────────────────────────
    // SCORING ORCHESTRATION
    // ──────────────────────────────────────────────────────────────────────

    private ScoredCandidate scoreSubject(Subject subject,
                                         UserInterestProfile profile,
                                         RecommendationWeights weights,
                                         SubjectRecommendationCriteria criteria,
                                         Map<Long, PopularityData> popularityMap,
                                         Map<Long, Double> collaborativeScores) {

        double tagAffinity = computeTagAffinity(profile, subject);
        double collaborative = collaborativeScores.getOrDefault(subject.getId(), 0.0);
        double popularity = computePopularity(popularityMap.get(subject.getId()));
        double contentMatch = computeContentMatch(subject, criteria);
        double diversity = computeDiversity(profile, subject);

        double totalScore = weights.getTagAffinity() * tagAffinity
                + weights.getCollaborative() * collaborative
                + weights.getPopularity() * popularity
                + weights.getContentMatch() * contentMatch
                + weights.getDiversity() * diversity;

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("tagAffinity", tagAffinity);
        breakdown.put("collaborative", collaborative);
        breakdown.put("popularity", popularity);
        breakdown.put("contentMatch", contentMatch);
        breakdown.put("diversity", diversity);

        return new ScoredCandidate(subject, totalScore, breakdown);
    }

    // ──────────────────────────────────────────────────────────────────────
    // MMR (Maximal Marginal Relevance) for diversity re-ranking
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Applies MMR re-ranking to balance relevance and diversity.
     * λ * relevance(s) - (1-λ) * max_similarity(s, already_selected)
     */
    List<ScoredCandidate> applyMMR(List<ScoredCandidate> scored, UserInterestProfile profile, int limit) {
        if (scored.isEmpty()) {
            return List.of();
        }

        List<ScoredCandidate> available = new ArrayList<>(scored);
        List<ScoredCandidate> selected = new ArrayList<>();

        // First item is always the highest scored
        selected.add(available.remove(0));

        while (selected.size() < limit && !available.isEmpty()) {
            ScoredCandidate best = null;
            double bestMMRScore = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;

            for (int i = 0; i < available.size(); i++) {
                ScoredCandidate candidate = available.get(i);
                double maxSim = selected.stream()
                        .mapToDouble(sel -> jaccardBetweenSubjects(candidate.subject(), sel.subject()))
                        .max()
                        .orElse(0.0);

                double mmrScore = MMR_LAMBDA * candidate.totalScore() - (1 - MMR_LAMBDA) * maxSim;

                if (mmrScore > bestMMRScore) {
                    bestMMRScore = mmrScore;
                    best = candidate;
                    bestIndex = i;
                }
            }

            if (best != null) {
                selected.add(best);
                available.remove(bestIndex);
            }
        }

        return selected;
    }

    /**
     * Jaccard similarity between two subjects based on their tags.
     * Used in MMR to measure inter-item similarity.
     */
    double jaccardBetweenSubjects(Subject a, Subject b) {
        Set<String> tagsA = a.getTags() != null
                ? a.getTags().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Set.of();
        Set<String> tagsB = b.getTags() != null
                ? b.getTags().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Set.of();

        if (tagsA.isEmpty() && tagsB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(tagsA);
        intersection.retainAll(tagsB);

        Set<String> union = new HashSet<>(tagsA);
        union.addAll(tagsB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // ──────────────────────────────────────────────────────────────────────
    // EXPLAINABILITY
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Generates a human-readable explanation based on the dominant signal.
     */
    String generateExplanation(Map<String, Double> breakdown, Subject subject, UserInterestProfile profile) {
        String dominantSignal = breakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("popularity");

        return switch (dominantSignal) {
            case "tagAffinity" -> generateTagAffinityExplanation(subject, profile);
            case "collaborative" -> "Estudiantes con gustos similares valoran bien esta asignatura";
            case "popularity" -> generatePopularityExplanation(subject);
            case "contentMatch" -> "Encaja con tus preferencias de búsqueda";
            case "diversity" -> "Explora un área diferente a tu perfil habitual";
            default -> "Recomendada para ti";
        };
    }

    private String generateTagAffinityExplanation(Subject subject, UserInterestProfile profile) {
        if (subject.getTags() == null || subject.getTags().isEmpty()) {
            return "Afín a tus intereses";
        }
        Set<String> profileTags = profile.getProfileTags();
        List<String> matchingTags = subject.getTags().stream()
                .filter(t -> profileTags.contains(t.toLowerCase()))
                .limit(3)
                .toList();

        if (matchingTags.isEmpty()) {
            return "Afín a tus intereses";
        }
        return "Afín a tus intereses en " + String.join(", ", matchingTags);
    }

    private String generatePopularityExplanation(Subject subject) {
        return "Popular entre estudiantes";
    }

    // ──────────────────────────────────────────────────────────────────────
    // DTO CONVERSION
    // ──────────────────────────────────────────────────────────────────────

    private SubjectScoreDTO toDTO(ScoredCandidate sc, Map<Long, PopularityData> popularityMap, UserInterestProfile profile) {
        Subject subject = sc.subject();
        PopularityData pop = popularityMap.get(subject.getId());

        List<String> attributes = new ArrayList<>();
        attributes.add(subject.getCredits() + " ECTS");
        if (subject.getSemesters() != null) {
            for (Semester sem : subject.getSemesters()) {
                attributes.add("Semestre " + sem);
            }
        }
        if (subject.getLanguages() != null) {
            for (Languages lang : subject.getLanguages()) {
                attributes.add(lang.toString());
            }
        }

        String explanation = generateExplanation(sc.breakdown(), subject, profile);

        return SubjectScoreDTO.builder()
                .subjectId(subject.getId())
                .name(subject.getName())
                .description(subject.getDescription())
                .totalScore(sc.totalScore())
                .matchPercentage((int) Math.round(sc.totalScore() * 100))
                .signalBreakdown(sc.breakdown())
                .explanation(explanation)
                .averageRating(pop != null ? Math.round(pop.average() * 10.0) / 10.0 : null)
                .ratingCount(pop != null ? (long) pop.count() : 0L)
                .tags(subject.getTags())
                .attributes(attributes)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // INTERNAL RECORDS
    // ──────────────────────────────────────────────────────────────────────

    record ScoredCandidate(Subject subject, double totalScore, Map<String, Double> breakdown) {}
    record PopularityData(double average, int count) {}
    record Neighbor(Long userId, double similarity, Map<Long, Integer> ratings) {}
    record WeightedRating(int rating, double weight) {}
}
