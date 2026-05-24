package es.hugoalvarezajenjo.selecta.services.recommendation;

/**
 * Represents the context of a user for adaptive weight selection
 * in the recommendation engine.
 *
 * The context determines how much each signal contributes to the final score,
 * handling the cold-start problem gracefully.
 */
public enum UserContext {
    /** Anonymous user — no personalization possible */
    NO_LOGIN,

    /** Logged in but has never rated any subject */
    LOGIN_NO_RATINGS,

    /** Logged in with fewer than 3 ratings — limited personalization */
    LOGIN_FEW_RATINGS,

    /** Logged in with 3+ ratings — full personalization available */
    LOGIN_RICH_HISTORY;

    private static final int RICH_HISTORY_THRESHOLD = 3;

    /**
     * Determines user context based on login status and rating count.
     */
    public static UserContext determine(boolean isLoggedIn, int ratingCount) {
        if (!isLoggedIn) {
            return NO_LOGIN;
        }
        if (ratingCount == 0) {
            return LOGIN_NO_RATINGS;
        }
        if (ratingCount < RICH_HISTORY_THRESHOLD) {
            return LOGIN_FEW_RATINGS;
        }
        return LOGIN_RICH_HISTORY;
    }
}
