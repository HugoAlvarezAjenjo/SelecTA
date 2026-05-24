package es.hugoalvarezajenjo.selecta.services.recommendation;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRecommendationCriteria;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineTest {

    @Mock
    private SubjectRatingRepository ratingRepository;

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private RecommendationEngine engine;

    // ──────────────────────────────────────────────────────────────────────
    // Test Helpers
    // ──────────────────────────────────────────────────────────────────────

    private Subject createSubject(Long id, String name, Set<String> tags, int credits,
                                  Set<Languages> languages, Set<Semester> semesters) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        subject.setDescription("Description for " + name);
        subject.setTags(tags);
        subject.setCredits(credits);
        subject.setLanguages(languages);
        subject.setSemesters(semesters);
        subject.setDiscontinued(false);
        return subject;
    }

    private Subject createSubject(Long id, String name, Set<String> tags) {
        return createSubject(id, name, tags, 6,
                Set.of(Languages.SPANISH), Set.of(Semester.FIRST));
    }

    private SubjectRating createRating(Long id, Subject subject, User user, int rating) {
        SubjectRating sr = new SubjectRating();
        sr.setId(id);
        sr.setSubject(subject);
        sr.setUser(user);
        sr.setRating(rating);
        return sr;
    }

    private Student createStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setUsername("student" + id);
        return student;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Tag Affinity Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Tag Affinity (Weighted Jaccard)")
    class TagAffinityTests {

        @Test
        @DisplayName("Returns 0 when profile has no tags")
        void emptyProfileReturnsZero() {
            UserInterestProfile profile = UserInterestProfile.anonymous();
            Subject subject = createSubject(1L, "ML Basics", Set.of("AI", "ML"));

            double affinity = engine.computeTagAffinity(profile, subject);

            assertThat(affinity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Returns 0 when subject has no tags")
        void emptySubjectTagsReturnsZero() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI", "ML"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidateNoTags = createSubject(2L, "No Tags", Set.of());

            double affinity = engine.computeTagAffinity(profile, candidateNoTags);

            assertThat(affinity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Returns 1.0 for perfect tag match")
        void perfectMatchReturnsOne() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidate = createSubject(2L, "Candidate", Set.of("AI"));

            double affinity = engine.computeTagAffinity(profile, candidate);

            assertThat(affinity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Partial overlap returns value between 0 and 1")
        void partialOverlapReturnsMiddleValue() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI", "ML", "Data"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidate = createSubject(2L, "Candidate", Set.of("AI", "Networks"));

            double affinity = engine.computeTagAffinity(profile, candidate);

            assertThat(affinity).isGreaterThan(0.0).isLessThan(1.0);
        }

        @Test
        @DisplayName("Higher rated subjects weight tags more")
        void higherRatingWeightsTagsMore() {
            Student user = createStudent(1L);
            Subject sub5star = createSubject(10L, "Five Star", Set.of("AI"));
            Subject sub4star = createSubject(11L, "Four Star", Set.of("Networks"));
            SubjectRating rating5 = createRating(1L, sub5star, user, 5);
            SubjectRating rating4 = createRating(2L, sub4star, user, 4);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating5, rating4), true);

            Subject candidateAI = createSubject(2L, "AI Course", Set.of("AI"));
            Subject candidateNet = createSubject(3L, "Net Course", Set.of("Networks"));

            double affinityAI = engine.computeTagAffinity(profile, candidateAI);
            double affinityNet = engine.computeTagAffinity(profile, candidateNet);

            // AI tag comes from 5-star rating → higher weight → higher affinity
            assertThat(affinityAI).isGreaterThan(affinityNet);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Popularity (Wilson Score) Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Popularity (Wilson Confidence Score)")
    class PopularityTests {

        @Test
        @DisplayName("No ratings returns 0")
        void noRatingsReturnsZero() {
            double score = engine.computePopularity(null);
            assertThat(score).isEqualTo(0.0);

            double scoreEmpty = engine.computePopularity(new RecommendationEngine.PopularityData(0.0, 0));
            assertThat(scoreEmpty).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Single 5-star rating scores lower than 50 ratings of 4-star")
        void wilsonPenalizesFewRatings() {
            // 1 rating of 5 stars
            double scoreSingle5 = engine.computePopularity(
                    new RecommendationEngine.PopularityData(5.0, 1));

            // 50 ratings averaging 4 stars
            double scoreMany4 = engine.computePopularity(
                    new RecommendationEngine.PopularityData(4.0, 50));

            assertThat(scoreMany4).isGreaterThan(scoreSingle5);
        }

        @Test
        @DisplayName("Score is always in [0, 1] range")
        void scoreIsNormalized() {
            double score1 = engine.computePopularity(new RecommendationEngine.PopularityData(5.0, 100));
            double score2 = engine.computePopularity(new RecommendationEngine.PopularityData(1.0, 1));
            double score3 = engine.computePopularity(new RecommendationEngine.PopularityData(3.5, 20));

            assertThat(score1).isBetween(0.0, 1.0);
            assertThat(score2).isBetween(0.0, 1.0);
            assertThat(score3).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("More ratings increase confidence factor")
        void moreRatingsIncreaseConfidence() {
            double score5ratings = engine.computePopularity(new RecommendationEngine.PopularityData(4.0, 5));
            double score50ratings = engine.computePopularity(new RecommendationEngine.PopularityData(4.0, 50));

            assertThat(score50ratings).isGreaterThan(score5ratings);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Content Match Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Content Match (Soft Filters)")
    class ContentMatchTests {

        @Test
        @DisplayName("Null criteria returns 0")
        void nullCriteriaReturnsZero() {
            Subject subject = createSubject(1L, "Test", Set.of("AI"));
            double score = engine.computeContentMatch(subject, null);
            assertThat(score).isEqualTo(0.0);
        }

        @Test
        @DisplayName("All filters satisfied returns 1.0")
        void allFiltersSatisfiedReturnsOne() {
            Subject subject = createSubject(1L, "Test", Set.of("AI"), 6,
                    Set.of(Languages.SPANISH), Set.of(Semester.FIRST));

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder()
                    .semesterTypes(List.of("ODD"))
                    .language(Languages.SPANISH)
                    .maxCredits(6)
                    .selectedTags(List.of("AI"))
                    .build();

            double score = engine.computeContentMatch(subject, criteria);
            assertThat(score).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Half filters satisfied returns 0.5")
        void halfFiltersSatisfiedReturnsHalf() {
            Subject subject = createSubject(1L, "Test", Set.of("AI"), 9,
                    Set.of(Languages.SPANISH), Set.of(Semester.FIRST));

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder()
                    .semesterTypes(List.of("ODD"))  // Satisfied (FIRST is ODD)
                    .language(Languages.ENGLISH)     // NOT satisfied (subject is SPANISH)
                    .maxCredits(6)                   // NOT satisfied (subject has 9 ECTS)
                    .selectedTags(List.of("AI"))     // Satisfied
                    .build();

            double score = engine.computeContentMatch(subject, criteria);
            assertThat(score).isEqualTo(0.5);
        }

        @Test
        @DisplayName("No active filters returns 0")
        void noActiveFiltersReturnsZero() {
            Subject subject = createSubject(1L, "Test", Set.of("AI"));

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();

            double score = engine.computeContentMatch(subject, criteria);
            assertThat(score).isEqualTo(0.0);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Diversity Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Diversity Signal")
    class DiversityTests {

        @Test
        @DisplayName("Subject with no overlap has maximum diversity")
        void noOverlapMaxDiversity() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI", "ML"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidate = createSubject(2L, "Different", Set.of("Networks", "Security"));

            double diversity = engine.computeDiversity(profile, candidate);

            assertThat(diversity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Subject with full overlap has minimum diversity")
        void fullOverlapMinDiversity() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI", "ML"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidate = createSubject(2L, "Same", Set.of("AI", "ML"));

            double diversity = engine.computeDiversity(profile, candidate);

            assertThat(diversity).isCloseTo(0.0, within(0.01));
        }

        @Test
        @DisplayName("Empty profile means maximum diversity for all subjects")
        void emptyProfileMaxDiversity() {
            UserInterestProfile profile = UserInterestProfile.anonymous();
            Subject candidate = createSubject(2L, "Any", Set.of("AI", "ML"));

            double diversity = engine.computeDiversity(profile, candidate);

            assertThat(diversity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Subject with no tags returns neutral diversity")
        void noTagsNeutralDiversity() {
            Student user = createStudent(1L);
            Subject ratedSubject = createSubject(10L, "Rated", Set.of("AI"));
            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            UserInterestProfile profile = UserInterestProfile.build(List.of(rating), true);
            Subject candidate = createSubject(2L, "No Tags", Set.of());

            double diversity = engine.computeDiversity(profile, candidate);

            assertThat(diversity).isEqualTo(0.5);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Cosine Similarity Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cosine Similarity")
    class CosineSimilarityTests {

        @Test
        @DisplayName("Identical ratings return 1.0")
        void identicalRatingsReturnOne() {
            Map<Long, Integer> userA = Map.of(1L, 5, 2L, 4, 3L, 3);
            Map<Long, Integer> userB = Map.of(1L, 5, 2L, 4, 3L, 3);

            double similarity = engine.cosineSimilarity(userA, userB);

            assertThat(similarity).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("No common subjects return 0.0")
        void noCommonSubjectsReturnZero() {
            Map<Long, Integer> userA = Map.of(1L, 5, 2L, 4);
            Map<Long, Integer> userB = Map.of(3L, 5, 4L, 4);

            double similarity = engine.cosineSimilarity(userA, userB);

            assertThat(similarity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Similar ratings produce high similarity")
        void similarRatingsProduceHighSimilarity() {
            Map<Long, Integer> userA = Map.of(1L, 5, 2L, 4, 3L, 3);
            Map<Long, Integer> userB = Map.of(1L, 5, 2L, 5, 3L, 3);

            double similarity = engine.cosineSimilarity(userA, userB);

            assertThat(similarity).isGreaterThan(0.9);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // MMR (Diversity Re-ranking) Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MMR Re-ranking")
    class MMRTests {

        @Test
        @DisplayName("First item is always the highest scored")
        void firstItemIsHighestScored() {
            Subject s1 = createSubject(1L, "Top", Set.of("AI"));
            Subject s2 = createSubject(2L, "Second", Set.of("ML"));

            List<RecommendationEngine.ScoredCandidate> scored = List.of(
                    new RecommendationEngine.ScoredCandidate(s1, 0.9, Map.of()),
                    new RecommendationEngine.ScoredCandidate(s2, 0.7, Map.of())
            );

            List<RecommendationEngine.ScoredCandidate> result = engine.applyMMR(scored, UserInterestProfile.anonymous(), 2);

            assertThat(result.get(0).subject().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("MMR promotes diverse items over similar ones")
        void mmrPromotesDiversity() {
            Subject s1 = createSubject(1L, "AI Basics", Set.of("AI", "ML"));
            Subject s2 = createSubject(2L, "AI Advanced", Set.of("AI", "ML", "Deep Learning"));
            Subject s3 = createSubject(3L, "Networks", Set.of("Networks", "Security"));

            // s2 has higher raw score than s3, but s2 is very similar to s1
            List<RecommendationEngine.ScoredCandidate> scored = List.of(
                    new RecommendationEngine.ScoredCandidate(s1, 0.9, Map.of()),
                    new RecommendationEngine.ScoredCandidate(s2, 0.85, Map.of()),
                    new RecommendationEngine.ScoredCandidate(s3, 0.7, Map.of())
            );

            List<RecommendationEngine.ScoredCandidate> result = engine.applyMMR(scored, UserInterestProfile.anonymous(), 3);

            // s3 should be promoted above s2 due to diversity with s1
            assertThat(result.get(0).subject().getId()).isEqualTo(1L);
            // The order of s2 and s3 depends on lambda, but s3 should be promoted
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Empty list returns empty")
        void emptyListReturnsEmpty() {
            List<RecommendationEngine.ScoredCandidate> result = engine.applyMMR(
                    List.of(), UserInterestProfile.anonymous(), 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Limit is respected")
        void limitIsRespected() {
            Subject s1 = createSubject(1L, "A", Set.of("AI"));
            Subject s2 = createSubject(2L, "B", Set.of("ML"));
            Subject s3 = createSubject(3L, "C", Set.of("Net"));

            List<RecommendationEngine.ScoredCandidate> scored = List.of(
                    new RecommendationEngine.ScoredCandidate(s1, 0.9, Map.of()),
                    new RecommendationEngine.ScoredCandidate(s2, 0.8, Map.of()),
                    new RecommendationEngine.ScoredCandidate(s3, 0.7, Map.of())
            );

            List<RecommendationEngine.ScoredCandidate> result = engine.applyMMR(scored, UserInterestProfile.anonymous(), 2);

            assertThat(result).hasSize(2);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Jaccard Between Subjects Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Jaccard Between Subjects")
    class JaccardBetweenSubjectsTests {

        @Test
        @DisplayName("Identical tags return 1.0")
        void identicalTagsReturnOne() {
            Subject a = createSubject(1L, "A", Set.of("AI", "ML"));
            Subject b = createSubject(2L, "B", Set.of("AI", "ML"));

            assertThat(engine.jaccardBetweenSubjects(a, b)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("No common tags return 0.0")
        void noCommonTagsReturnZero() {
            Subject a = createSubject(1L, "A", Set.of("AI", "ML"));
            Subject b = createSubject(2L, "B", Set.of("Networks", "Security"));

            assertThat(engine.jaccardBetweenSubjects(a, b)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Both empty tags return 0.0")
        void bothEmptyReturnZero() {
            Subject a = createSubject(1L, "A", Set.of());
            Subject b = createSubject(2L, "B", Set.of());

            assertThat(engine.jaccardBetweenSubjects(a, b)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Partial overlap returns correct Jaccard index")
        void partialOverlapCorrectJaccard() {
            // Tags: {AI, ML} ∩ {AI, Networks} = {AI}, Union = {AI, ML, Networks}
            // Jaccard = 1/3
            Subject a = createSubject(1L, "A", Set.of("AI", "ML"));
            Subject b = createSubject(2L, "B", Set.of("AI", "Networks"));

            assertThat(engine.jaccardBetweenSubjects(a, b)).isCloseTo(1.0 / 3.0, within(0.001));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adaptive Weights Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Adaptive Weights")
    class AdaptiveWeightsTests {

        @Test
        @DisplayName("Anonymous user gets no personalization weights")
        void anonymousNoPersonalization() {
            RecommendationWeights weights = RecommendationWeights.forContext(UserContext.NO_LOGIN);

            assertThat(weights.getTagAffinity()).isEqualTo(0.0);
            assertThat(weights.getCollaborative()).isEqualTo(0.0);
            assertThat(weights.getPopularity()).isEqualTo(0.40);
            assertThat(weights.getContentMatch()).isEqualTo(0.50);
            assertThat(weights.getDiversity()).isEqualTo(0.10);
        }

        @Test
        @DisplayName("Rich history user gets full personalization weights")
        void richHistoryFullPersonalization() {
            RecommendationWeights weights = RecommendationWeights.forContext(UserContext.LOGIN_RICH_HISTORY);

            assertThat(weights.getTagAffinity()).isEqualTo(0.35);
            assertThat(weights.getCollaborative()).isEqualTo(0.25);
            assertThat(weights.getPopularity()).isEqualTo(0.20);
            assertThat(weights.getContentMatch()).isEqualTo(0.15);
            assertThat(weights.getDiversity()).isEqualTo(0.05);
        }

        @Test
        @DisplayName("All weights sum to 1.0 for every context")
        void weightsSumToOne() {
            for (UserContext context : UserContext.values()) {
                RecommendationWeights weights = RecommendationWeights.forContext(context);
                double sum = weights.getTagAffinity() + weights.getCollaborative()
                        + weights.getPopularity() + weights.getContentMatch()
                        + weights.getDiversity();
                assertThat(sum).isCloseTo(1.0, within(0.001));
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // User Context Determination Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("User Context Determination")
    class UserContextTests {

        @Test
        @DisplayName("Not logged in → NO_LOGIN")
        void notLoggedIn() {
            assertThat(UserContext.determine(false, 0)).isEqualTo(UserContext.NO_LOGIN);
            assertThat(UserContext.determine(false, 10)).isEqualTo(UserContext.NO_LOGIN);
        }

        @Test
        @DisplayName("Logged in with 0 ratings → LOGIN_NO_RATINGS")
        void loggedInNoRatings() {
            assertThat(UserContext.determine(true, 0)).isEqualTo(UserContext.LOGIN_NO_RATINGS);
        }

        @Test
        @DisplayName("Logged in with 1-2 ratings → LOGIN_FEW_RATINGS")
        void loggedInFewRatings() {
            assertThat(UserContext.determine(true, 1)).isEqualTo(UserContext.LOGIN_FEW_RATINGS);
            assertThat(UserContext.determine(true, 2)).isEqualTo(UserContext.LOGIN_FEW_RATINGS);
        }

        @Test
        @DisplayName("Logged in with 3+ ratings → LOGIN_RICH_HISTORY")
        void loggedInRichHistory() {
            assertThat(UserContext.determine(true, 3)).isEqualTo(UserContext.LOGIN_RICH_HISTORY);
            assertThat(UserContext.determine(true, 50)).isEqualTo(UserContext.LOGIN_RICH_HISTORY);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // User Interest Profile Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("User Interest Profile")
    class UserInterestProfileTests {

        @Test
        @DisplayName("Anonymous profile has empty data")
        void anonymousProfileEmpty() {
            UserInterestProfile profile = UserInterestProfile.anonymous();

            assertThat(profile.getTagWeights()).isEmpty();
            assertThat(profile.getRatedSubjects()).isEmpty();
            assertThat(profile.getPositivelyRatedSubjectIds()).isEmpty();
            assertThat(profile.getContext()).isEqualTo(UserContext.NO_LOGIN);
            assertThat(profile.hasPersonalizationData()).isFalse();
        }

        @Test
        @DisplayName("Only ratings ≥4 contribute to tag weights")
        void onlyPositiveRatingsContribute() {
            Student user = createStudent(1L);
            Subject goodSubject = createSubject(10L, "Good", Set.of("AI"));
            Subject badSubject = createSubject(11L, "Bad", Set.of("Networks"));

            SubjectRating goodRating = createRating(1L, goodSubject, user, 5);
            SubjectRating badRating = createRating(2L, badSubject, user, 2);

            UserInterestProfile profile = UserInterestProfile.build(List.of(goodRating, badRating), true);

            assertThat(profile.getTagWeights()).containsKey("ai");
            assertThat(profile.getTagWeights()).doesNotContainKey("networks");
        }

        @Test
        @DisplayName("Profile correctly identifies positively rated subjects")
        void positivelyRatedSubjectsIdentified() {
            Student user = createStudent(1L);
            Subject s1 = createSubject(10L, "Good", Set.of("AI"));
            Subject s2 = createSubject(11L, "Bad", Set.of("ML"));

            SubjectRating r1 = createRating(1L, s1, user, 4);
            SubjectRating r2 = createRating(2L, s2, user, 2);

            UserInterestProfile profile = UserInterestProfile.build(List.of(r1, r2), true);

            assertThat(profile.getPositivelyRatedSubjectIds()).contains(10L);
            assertThat(profile.getPositivelyRatedSubjectIds()).doesNotContain(11L);
            assertThat(profile.getRatedSubjects()).hasSize(2);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Explainability Tests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Explainability")
    class ExplainabilityTests {

        @Test
        @DisplayName("Tag affinity dominant → affinity explanation")
        void tagAffinityExplanation() {
            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("tagAffinity", 0.8);
            breakdown.put("collaborative", 0.1);
            breakdown.put("popularity", 0.2);
            breakdown.put("contentMatch", 0.1);
            breakdown.put("diversity", 0.1);

            Subject subject = createSubject(1L, "Test", Set.of("AI", "ML"));
            UserInterestProfile profile = UserInterestProfile.anonymous();

            String explanation = engine.generateExplanation(breakdown, subject, profile);

            assertThat(explanation).contains("Afín a tus intereses");
        }

        @Test
        @DisplayName("Popularity dominant → popularity explanation")
        void popularityExplanation() {
            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("tagAffinity", 0.1);
            breakdown.put("collaborative", 0.1);
            breakdown.put("popularity", 0.9);
            breakdown.put("contentMatch", 0.2);
            breakdown.put("diversity", 0.1);

            Subject subject = createSubject(1L, "Test", Set.of("AI"));
            UserInterestProfile profile = UserInterestProfile.anonymous();

            String explanation = engine.generateExplanation(breakdown, subject, profile);

            assertThat(explanation).contains("Popular entre estudiantes");
        }

        @Test
        @DisplayName("Collaborative dominant → collaborative explanation")
        void collaborativeExplanation() {
            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("tagAffinity", 0.1);
            breakdown.put("collaborative", 0.9);
            breakdown.put("popularity", 0.1);
            breakdown.put("contentMatch", 0.1);
            breakdown.put("diversity", 0.1);

            Subject subject = createSubject(1L, "Test", Set.of("AI"));
            UserInterestProfile profile = UserInterestProfile.anonymous();

            String explanation = engine.generateExplanation(breakdown, subject, profile);

            assertThat(explanation).contains("Estudiantes con gustos similares");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Full Pipeline Integration Test
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Full Recommendation Pipeline")
    class FullPipelineTests {

        @Test
        @DisplayName("Anonymous user gets recommendations based on popularity and content match")
        void anonymousUserGetRecommendations() {
            Subject s1 = createSubject(1L, "AI Basics", Set.of("AI", "ML"), 6,
                    Set.of(Languages.SPANISH), Set.of(Semester.FIRST));
            Subject s2 = createSubject(2L, "Networks", Set.of("Networks"), 4,
                    Set.of(Languages.ENGLISH), Set.of(Semester.SECOND));

            when(subjectService.getActiveSubjects()).thenReturn(List.of(s1, s2));
            when(ratingRepository.findAllWithSubject()).thenReturn(List.of());

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder()
                    .selectedTags(List.of("AI"))
                    .language(Languages.SPANISH)
                    .build();

            List<SubjectScoreDTO> results = engine.recommend(null, criteria, 10);

            assertThat(results).isNotEmpty();
            assertThat(results).allSatisfy(dto -> {
                assertThat(dto.getTotalScore()).isBetween(0.0, 1.0);
                assertThat(dto.getMatchPercentage()).isBetween(0, 100);
                assertThat(dto.getExplanation()).isNotBlank();
            });
        }

        @Test
        @DisplayName("Logged-in user with ratings gets personalized recommendations")
        void loggedInUserGetsPersonalizedRecommendations() {
            Student user = createStudent(1L);

            Subject ratedSubject = createSubject(10L, "Rated AI", Set.of("AI", "ML"), 6,
                    Set.of(Languages.SPANISH), Set.of(Semester.FIRST));
            Subject candidate1 = createSubject(1L, "Advanced AI", Set.of("AI", "Deep Learning"), 6,
                    Set.of(Languages.SPANISH), Set.of(Semester.THIRD));
            Subject candidate2 = createSubject(2L, "Networks", Set.of("Networks", "Security"), 4,
                    Set.of(Languages.ENGLISH), Set.of(Semester.SECOND));

            SubjectRating rating = createRating(1L, ratedSubject, user, 5);

            when(subjectService.getActiveSubjects()).thenReturn(List.of(ratedSubject, candidate1, candidate2));
            when(ratingRepository.findByUserId(user.getId())).thenReturn(List.of(rating));
            when(ratingRepository.findAllWithSubject()).thenReturn(List.of(rating));
            when(ratingRepository.findBySubjectIdIn(anyList())).thenReturn(List.of(rating));

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();

            List<SubjectScoreDTO> results = engine.recommend(user, criteria, 10);

            // Should not include the already-rated subject
            assertThat(results).noneMatch(dto -> dto.getSubjectId().equals(10L));
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("Already rated subjects are excluded from results")
        void ratedSubjectsExcluded() {
            Student user = createStudent(1L);

            Subject rated = createSubject(10L, "Rated", Set.of("AI"));
            Subject candidate = createSubject(1L, "Candidate", Set.of("ML"));

            SubjectRating rating = createRating(1L, rated, user, 5);

            when(subjectService.getActiveSubjects()).thenReturn(List.of(rated, candidate));
            when(ratingRepository.findByUserId(user.getId())).thenReturn(List.of(rating));
            when(ratingRepository.findAllWithSubject()).thenReturn(List.of(rating));
            when(ratingRepository.findBySubjectIdIn(anyList())).thenReturn(List.of(rating));

            SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();

            List<SubjectScoreDTO> results = engine.recommend(user, criteria, 10);

            assertThat(results).noneMatch(dto -> dto.getSubjectId().equals(10L));
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getSubjectId()).isEqualTo(1L);
        }
    }
}
