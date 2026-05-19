package es.hugoalvarezajenjo.selecta.services.contributions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRequestRepository extends JpaRepository<ContributionRequest, Long> {

    List<ContributionRequest> findBySubjectIdAndStatus(Long subjectId, RequestStatus status);

    List<ContributionRequest> findBySubjectIdAndStatusOrderByCreatedAtDesc(Long subjectId, RequestStatus status);

    Optional<ContributionRequest> findBySubjectIdAndRequesterIdAndTypeAndStatus(
            Long subjectId, Long requesterId, RequestType type, RequestStatus status);

    boolean existsBySubjectIdAndRequesterIdAndTypeAndStatus(
            Long subjectId, Long requesterId, RequestType type, RequestStatus status);

    List<ContributionRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
}
