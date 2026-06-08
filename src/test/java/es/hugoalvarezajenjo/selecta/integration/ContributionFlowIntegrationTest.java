package es.hugoalvarezajenjo.selecta.integration;

import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequest;
import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequestService;
import es.hugoalvarezajenjo.selecta.services.contributions.RequestStatus;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the Contribution Request flow.
 * Tests the full lifecycle: request → approve/reject → effects.
 */
@SpringBootTest
@Transactional
@DisplayName("Contribution Flow Integration Tests")
class ContributionFlowIntegrationTest {

    @Autowired
    private ContributionRequestService contributionRequestService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserRepository userRepository;

    private User student;
    private final Long SUBJECT_ID = 1L; // Mathematics

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("carlos@demo.com").orElseThrow();
    }

    @Test
    @DisplayName("Student can request contributor access")
    void studentCanRequestAccess() {
        ContributionRequest request = contributionRequestService.requestAccess(
                SUBJECT_ID, student, "Quiero contribuir con ejercicios resueltos");

        assertThat(request.getId()).isNotNull();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(request.getRequester().getId()).isEqualTo(student.getId());
    }

    @Test
    @DisplayName("Approving access request makes student a contributor")
    void approvingAccess_makesStudentContributor() {
        ContributionRequest request = contributionRequestService.requestAccess(
                SUBJECT_ID, student, "Quiero contribuir");

        contributionRequestService.approveRequest(request.getId());

        assertThat(subjectService.isContributor(SUBJECT_ID, student.getId())).isTrue();
    }

    @Test
    @DisplayName("Rejecting access request does NOT make student a contributor")
    void rejectingAccess_doesNotMakeContributor() {
        ContributionRequest request = contributionRequestService.requestAccess(
                SUBJECT_ID, student, "Quiero contribuir");

        contributionRequestService.rejectRequest(request.getId());

        assertThat(subjectService.isContributor(SUBJECT_ID, student.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot create duplicate pending access request")
    void cannotCreateDuplicatePendingRequest() {
        contributionRequestService.requestAccess(SUBJECT_ID, student, "First request");

        assertThatThrownBy(() ->
                contributionRequestService.requestAccess(SUBJECT_ID, student, "Second request"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Already contributor cannot request access again")
    void alreadyContributor_cannotRequestAccess() {
        // First: request and approve
        ContributionRequest request = contributionRequestService.requestAccess(
                SUBJECT_ID, student, "Request");
        contributionRequestService.approveRequest(request.getId());

        // Then: try to request again
        assertThatThrownBy(() ->
                contributionRequestService.requestAccess(SUBJECT_ID, student, "Again"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pending requests are listed correctly")
    void pendingRequests_listedCorrectly() {
        contributionRequestService.requestAccess(SUBJECT_ID, student, "My request");

        List<ContributionRequest> pending = contributionRequestService.getPendingRequests(SUBJECT_ID);
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getRequester().getId()).isEqualTo(student.getId());
    }
}
