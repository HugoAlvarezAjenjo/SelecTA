package es.hugoalvarezajenjo.selecta.services.resources.repository;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceVote;
import es.hugoalvarezajenjo.selecta.services.resources.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceVoteRepository extends JpaRepository<ResourceVote, Long> {

    Optional<ResourceVote> findByResourceIdAndUserId(Long resourceId, Long userId);

    long countByResourceIdAndVoteType(Long resourceId, VoteType voteType);
}
