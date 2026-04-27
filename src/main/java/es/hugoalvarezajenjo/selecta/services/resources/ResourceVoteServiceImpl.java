package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceVoteRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceVoteServiceImpl implements ResourceVoteService {

    private final ResourceVoteRepository resourceVoteRepository;
    private final SubjectResourceRepository subjectResourceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Map<String, Object> toggleVote(final Long resourceId, final VoteType voteType) {
        final User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to vote");
        }

        final SubjectResource resource = subjectResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        final Optional<ResourceVote> existingVote = resourceVoteRepository
                .findByResourceIdAndUserId(resourceId, currentUser.getId());

        String userVote = null;

        if (existingVote.isPresent()) {
            final ResourceVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type → remove (toggle off)
                resourceVoteRepository.delete(vote);
            } else {
                // Different vote type → switch
                vote.setVoteType(voteType);
                resourceVoteRepository.save(vote);
                userVote = voteType.name();
            }
        } else {
            // No vote exists → create new
            final ResourceVote newVote = new ResourceVote();
            newVote.setResource(resource);
            newVote.setUser(currentUser);
            newVote.setVoteType(voteType);
            resourceVoteRepository.save(newVote);
            userVote = voteType.name();
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("upvotes", getUpvoteCount(resourceId));
        result.put("downvotes", getDownvoteCount(resourceId));
        result.put("userVote", userVote);
        return result;
    }

    @Override
    public long getUpvoteCount(final Long resourceId) {
        return resourceVoteRepository.countByResourceIdAndVoteType(resourceId, VoteType.UPVOTE);
    }

    @Override
    public long getDownvoteCount(final Long resourceId) {
        return resourceVoteRepository.countByResourceIdAndVoteType(resourceId, VoteType.DOWNVOTE);
    }

    @Override
    public VoteType getUserVote(final Long resourceId) {
        final User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return resourceVoteRepository.findByResourceIdAndUserId(resourceId, currentUser.getId())
                .map(ResourceVote::getVoteType)
                .orElse(null);
    }
}
