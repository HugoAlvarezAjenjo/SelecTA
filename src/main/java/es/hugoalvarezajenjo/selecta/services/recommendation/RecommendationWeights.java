package es.hugoalvarezajenjo.selecta.services.recommendation;

import lombok.Value;

/**
 * Immutable set of weights for the hybrid recommendation scoring formula.
 *
 * Score(subject) = tagAffinity*w1 + collaborative*w2 + popularity*w3 + contentMatch*w4 + diversity*w5
 *
 * Weights are adaptive: they change based on the user's context to handle
 * the cold-start problem gracefully.
 */
@Value
public class RecommendationWeights {
    double tagAffinity;
    double collaborative;
    double popularity;
    double contentMatch;
    double diversity;

    /**
     * Factory method that returns the appropriate weights for a given user context.
     */
    public static RecommendationWeights forContext(UserContext context) {
        return switch (context) {
            case NO_LOGIN -> new RecommendationWeights(0.0, 0.0, 0.40, 0.50, 0.10);
            case LOGIN_NO_RATINGS -> new RecommendationWeights(0.0, 0.0, 0.35, 0.50, 0.15);
            case LOGIN_FEW_RATINGS -> new RecommendationWeights(0.25, 0.10, 0.30, 0.25, 0.10);
            case LOGIN_RICH_HISTORY -> new RecommendationWeights(0.35, 0.25, 0.20, 0.15, 0.05);
        };
    }
}
