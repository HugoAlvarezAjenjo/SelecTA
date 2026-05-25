package es.hugoalvarezajenjo.selecta.services.contributions;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContributionRequestServiceTest {

    @Mock
    private ContributionRequestRepository requestRepository;

    @Mock
    private SubjectService subjectService;

    @Mock
    private SubjectResourceService resourceService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ContributionRequestService service;

    private Student createStudent(Long id) {
        Student s = new Student();
        s.setId(id);
        s.setUsername("student" + id);
        return s;
    }

    private Subject createSubject(Long id, String name) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        return subject;
    }

    private ContributionRequest createPendingRequest(Long id, RequestType type, Subject subject, User requester) {
        ContributionRequest req = new ContributionRequest();
        req.setId(id);
        req.setType(type);
        req.setStatus(RequestStatus.PENDING);
        req.setSubject(subject);
        req.setRequester(requester);
        return req;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Requesting Access
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Requesting contributor access")
    class RequestAccessTests {

        @Test
        @DisplayName("A student can request access to become a contributor")
        void studentCanRequestAccess() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");

            when(requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                    10L, 1L, RequestType.ACCESS, RequestStatus.PENDING)).thenReturn(false);
            when(subjectService.getSubjectById(10L)).thenReturn(Optional.of(subject));
            when(subjectService.isContributor(10L, 1L)).thenReturn(false);
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ContributionRequest result = service.requestAccess(10L, student, "I want to help");

            assertThat(result.getType()).isEqualTo(RequestType.ACCESS);
            assertThat(result.getSubject()).isEqualTo(subject);
            assertThat(result.getRequester()).isEqualTo(student);
            assertThat(result.getMessage()).isEqualTo("I want to help");
        }

        @Test
        @DisplayName("Cannot request access if already has a pending request")
        void cannotRequestIfPending() {
            Student student = createStudent(1L);

            when(requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                    10L, 1L, RequestType.ACCESS, RequestStatus.PENDING)).thenReturn(true);

            assertThatThrownBy(() -> service.requestAccess(10L, student, "msg"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pendiente");
        }

        @Test
        @DisplayName("Cannot request access if already a contributor")
        void cannotRequestIfAlreadyContributor() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");

            when(requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                    10L, 1L, RequestType.ACCESS, RequestStatus.PENDING)).thenReturn(false);
            when(subjectService.getSubjectById(10L)).thenReturn(Optional.of(subject));
            when(subjectService.isContributor(10L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.requestAccess(10L, student, "msg"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("contribuidor");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Approving Requests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Approving requests")
    class ApproveTests {

        @Test
        @DisplayName("Approving an ACCESS request grants contributor access")
        void approvingAccessGrantsContributor() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");
            ContributionRequest request = createPendingRequest(100L, RequestType.ACCESS, subject, student);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            service.approveRequest(100L);

            assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
            verify(subjectService).addContributor(10L, 1L);
            verify(requestRepository).save(request);
        }

        @Test
        @DisplayName("Approving a RESOURCE request makes the resource public")
        void approvingResourceMakesPublic() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");
            ContributionRequest request = createPendingRequest(100L, RequestType.RESOURCE, subject, student);

            SubjectResource resource = new SubjectResource();
            resource.setId(50L);
            resource.setPrivate(true);
            request.setPendingResource(resource);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            service.approveRequest(100L);

            assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
            assertThat(resource.isPrivate()).isFalse();
            verify(resourceService).saveResource(resource);
        }

        @Test
        @DisplayName("Cannot approve a request that is not pending")
        void cannotApproveNonPending() {
            ContributionRequest request = new ContributionRequest();
            request.setId(100L);
            request.setStatus(RequestStatus.APPROVED);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.approveRequest(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        @Test
        @DisplayName("Cannot approve non-existent request")
        void cannotApproveNonExistent() {
            when(requestRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approveRequest(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Rejecting Requests
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rejecting requests")
    class RejectTests {

        @Test
        @DisplayName("Rejecting an ACCESS request just changes status")
        void rejectingAccessChangesStatus() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");
            ContributionRequest request = createPendingRequest(100L, RequestType.ACCESS, subject, student);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            service.rejectRequest(100L);

            assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
            verify(subjectService, never()).addContributor(any(), any());
            verify(requestRepository).save(request);
        }

        @Test
        @DisplayName("Rejecting a RESOURCE request deletes the pending resource")
        void rejectingResourceDeletesIt() {
            Student student = createStudent(1L);
            Subject subject = createSubject(10L, "AI");
            ContributionRequest request = createPendingRequest(100L, RequestType.RESOURCE, subject, student);

            SubjectResource resource = new SubjectResource();
            resource.setId(50L);
            request.setPendingResource(resource);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            service.rejectRequest(100L);

            assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
            verify(storageService).deleteFile("50");
            verify(resourceService).deleteResource(50L);
        }

        @Test
        @DisplayName("Cannot reject a request that is not pending")
        void cannotRejectNonPending() {
            ContributionRequest request = new ContributionRequest();
            request.setId(100L);
            request.setStatus(RequestStatus.REJECTED);

            when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.rejectRequest(100L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Querying pending status
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Checking pending request status")
    class PendingStatusTests {

        @Test
        @DisplayName("Returns true when user has a pending request")
        void returnsTrueWhenPending() {
            when(requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                    10L, 1L, RequestType.ACCESS, RequestStatus.PENDING)).thenReturn(true);

            assertThat(service.hasPendingAccessRequest(10L, 1L)).isTrue();
        }

        @Test
        @DisplayName("Returns false when user has no pending request")
        void returnsFalseWhenNoPending() {
            when(requestRepository.existsBySubjectIdAndRequesterIdAndTypeAndStatus(
                    10L, 1L, RequestType.ACCESS, RequestStatus.PENDING)).thenReturn(false);

            assertThat(service.hasPendingAccessRequest(10L, 1L)).isFalse();
        }
    }
}
