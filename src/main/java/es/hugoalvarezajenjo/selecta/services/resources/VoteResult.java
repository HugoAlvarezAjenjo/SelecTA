package es.hugoalvarezajenjo.selecta.services.resources;

/**
 * Typed result of a vote toggle operation.
 */
public record VoteResult(
        long upvotes,
        long downvotes,
        VoteType userVote
) {
}
