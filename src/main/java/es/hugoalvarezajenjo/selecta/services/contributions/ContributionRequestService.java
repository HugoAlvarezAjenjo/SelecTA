package es.hugoalvarezajenjo.selecta.services.contributions;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContributionRequestService {

    private final ContributionRequestRepository requestRepository;
    private final SubjectService subjectService;
    private final SubjectResourceService resourceService;
    private final StorageService storageService;

    /**
     * Student requests access to become a contributor.
     */
    public ContributionRequest requestAccess(final Long subjectId, final User requester, final String message) {
        // Check if already has a pending request
        if (this.requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                subjectId, requester.getId(), RequestType.ACCESS, RequestStatus.PENDING)) {
            throw new IllegalStateException("Ya tienes una solicitud de acceso pendiente");
        }

        final Subject subject = this.subjectService.getSubjectById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        // Check if already a contributor
        if (this.subjectService.isContributor(subjectId, requester.getId())) {
            throw new IllegalStateException("Ya eres contribuidor de esta asignatura");
        }

        final ContributionRequest request = new ContributionRequest();
        request.setSubject(subject);
        request.setRequester(requester);
        request.setType(RequestType.ACCESS);
        request.setMessage(message);
        return this.requestRepository.save(request);
    }

    /**
     * Student proposes a resource (creates it as private/pending).
     */
    public ContributionRequest proposeResource(final Long subjectId, final User requester,
                                                final SubjectResource resource, final String message) {
        final Subject subject = this.subjectService.getSubjectById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        final ContributionRequest request = new ContributionRequest();
        request.setSubject(subject);
        request.setRequester(requester);
        request.setType(RequestType.RESOURCE);
        request.setMessage(message);
        request.setPendingResource(resource);
        return this.requestRepository.save(request);
    }

    /**
     * Get all pending requests for a subject.
     */
    @Transactional(readOnly = true)
    public List<ContributionRequest> getPendingRequests(final Long subjectId) {
        return this.requestRepository.findBySubjectIdAndStatusOrderByCreatedAtDesc(subjectId, RequestStatus.PENDING);
    }

    /**
     * Professor approves a request.
     */
    public void approveRequest(final Long requestId) {
        final ContributionRequest request = this.requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(RequestStatus.APPROVED);

        if (request.getType() == RequestType.ACCESS) {
            // Grant contributor access
            this.subjectService.addContributor(request.getSubject().getId(), request.getRequester().getId());
        } else if (request.getType() == RequestType.RESOURCE) {
            // Make the resource public
            final SubjectResource resource = request.getPendingResource();
            if (resource != null) {
                resource.setPrivate(false);
                this.resourceService.saveResource(resource);
            }
        }

        this.requestRepository.save(request);
    }

    /**
     * Professor rejects a request.
     */
    public void rejectRequest(final Long requestId) {
        final ContributionRequest request = this.requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(RequestStatus.REJECTED);

        if (request.getType() == RequestType.RESOURCE && request.getPendingResource() != null) {
            // Delete the pending resource file and entity
            try {
                this.storageService.deleteFile(request.getPendingResource().getId().toString());
            } catch (Exception ignored) {}
            this.resourceService.deleteResource(request.getPendingResource().getId());
        }

        this.requestRepository.save(request);
    }

    /**
     * Check if student has a pending access request for a subject.
     */
    @Transactional(readOnly = true)
    public boolean hasPendingAccessRequest(final Long subjectId, final Long userId) {
        return this.requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                subjectId, userId, RequestType.ACCESS, RequestStatus.PENDING);
    }
}
