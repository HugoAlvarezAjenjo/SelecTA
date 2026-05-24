package es.hugoalvarezajenjo.selecta.services.recommendation;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a user's interest profile derived from their rating history.
 *
 * Only ratings ≥ 4 are considered as positive interest signals.
 * Tags are weighted by their frequency across positively-rated subjects.
 */
@Getter
public class UserInterestProfile {

    private static final int POSITIVE_RATING_THRESHOLD = 4;

    /** Tag → normalized weight [0.0, 1.0] based on frequency in liked subjects */
    private final Map<String, Double> tagWeights;

    /** Subject ID → rating for all rated subjects */
    private final Map<Long, Integer> ratedSubjects;

    /** IDs of subjects the user rated positively (≥4) */
    private final Set<Long> positivelyRatedSubjectIds;

    /** The user context determined from the profile */
    private final UserContext context;

    private UserInterestProfile(Map<String, Double> tagWeights,
                                Map<Long, Integer> ratedSubjects,
                                Set<Long> positivelyRatedSubjectIds,
                                UserContext context) {
        this.tagWeights = Collections.unmodifiableMap(tagWeights);
        this.ratedSubjects = Collections.unmodifiableMap(ratedSubjects);
        this.positivelyRatedSubjectIds = Collections.unmodifiableSet(positivelyRatedSubjectIds);
        this.context = context;
    }

    /**
     * Builds a user interest profile from their ratings.
     *
     * @param ratings all ratings by this user
     * @param isLoggedIn whether the user is authenticated
     * @return the constructed profile
     */
    public static UserInterestProfile build(List<SubjectRating> ratings, boolean isLoggedIn) {
        if (ratings == null) {
            ratings = List.of();
        }

        Map<Long, Integer> ratedSubjects = ratings.stream()
                .collect(Collectors.toMap(
                        r -> r.getSubject().getId(),
                        SubjectRating::getRating,
                        (a, b) -> b
                ));

        Set<Long> positiveIds = ratings.stream()
                .filter(r -> r.getRating() >= POSITIVE_RATING_THRESHOLD)
                .map(r -> r.getSubject().getId())
                .collect(Collectors.toSet());

        Map<String, Double> tagWeights = computeTagWeights(ratings);

        UserContext context = UserContext.determine(isLoggedIn, ratings.size());

        return new UserInterestProfile(tagWeights, ratedSubjects, positiveIds, context);
    }

    /**
     * Creates an empty profile for anonymous users.
     */
    public static UserInterestProfile anonymous() {
        return new UserInterestProfile(
                Map.of(),
                Map.of(),
                Set.of(),
                UserContext.NO_LOGIN
        );
    }

    /**
     * Returns the set of tags present in the user's profile.
     */
    public Set<String> getProfileTags() {
        return tagWeights.keySet();
    }

    /**
     * Returns whether this profile has enough data for personalized recommendations.
     */
    public boolean hasPersonalizationData() {
        return !tagWeights.isEmpty();
    }

    /**
     * Computes weighted tag frequencies from positive ratings.
     * Tags from higher-rated subjects get more weight.
     */
    private static Map<String, Double> computeTagWeights(List<SubjectRating> ratings) {
        Map<String, Double> rawWeights = new HashMap<>();

        List<SubjectRating> positiveRatings = ratings.stream()
                .filter(r -> r.getRating() >= POSITIVE_RATING_THRESHOLD)
                .toList();

        if (positiveRatings.isEmpty()) {
            return Map.of();
        }

        for (SubjectRating rating : positiveRatings) {
            Subject subject = rating.getSubject();
            if (subject.getTags() == null || subject.getTags().isEmpty()) {
                continue;
            }
            // Weight contribution: rating/5.0 gives more weight to 5★ vs 4★
            double weight = rating.getRating() / 5.0;
            for (String tag : subject.getTags()) {
                rawWeights.merge(tag.toLowerCase(), weight, Double::sum);
            }
        }

        if (rawWeights.isEmpty()) {
            return Map.of();
        }

        // Normalize to [0, 1]
        double maxWeight = rawWeights.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : rawWeights.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / maxWeight);
        }

        return normalized;
    }
}
