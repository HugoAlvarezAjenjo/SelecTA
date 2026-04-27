package es.hugoalvarezajenjo.selecta.services.resources;

import java.util.Map;

public interface ResourceVoteService {

    /**
     * Toggles a vote for a resource. If the user already has the same vote type, it removes it.
     * If the user has a different vote type, it changes it. If no vote exists, it creates one.
     *
     * @param resourceId the resource ID
     * @param voteType   the vote type (UPVOTE or DOWNVOTE)
     * @return a map with "upvotes", "downvotes", and "userVote" (null if removed)
     */
    Map<String, Object> toggleVote(Long resourceId, VoteType voteType);

    long getUpvoteCount(Long resourceId);

    long getDownvoteCount(Long resourceId);

    /**
     * Returns the current user's vote type for a resource, or null if no vote exists.
     */
    VoteType getUserVote(Long resourceId);
}
