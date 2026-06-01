package es.hugoalvarezajenjo.selecta.services.recommendation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object representing a scored subject recommendation.
 *
 * Contains the final score, signal breakdown for explainability,
 * and display-ready metadata.
 */
@Getter
@Builder
public class SubjectScoreDTO {

    private static final double FOR_YOU_THRESHOLD = 0.7;

    private final Long subjectId;
    private final String name;
    private final String description;

    /** Final hybrid score in [0.0, 1.0] */
    private final double totalScore;

    /** Percentage match (0-100) for display */
    private final int matchPercentage;

    /** Breakdown of each signal's contribution to the total score */
    private final Map<String, Double> signalBreakdown;

    /** Human-readable explanation of the recommendation reason */
    private final String explanation;

    /** Average rating from all users */
    private final Double averageRating;

    /** Total number of ratings */
    private final Long ratingCount;

    /** Tags associated with the subject */
    private final Set<String> tags;

    /** Display attributes (credits, semesters, languages) */
    private final List<String> attributes;

    /**
     * Returns true if this subject qualifies for the "Para ti" badge.
     */
    public boolean isForYou() {
        return totalScore >= FOR_YOU_THRESHOLD;
    }
}
