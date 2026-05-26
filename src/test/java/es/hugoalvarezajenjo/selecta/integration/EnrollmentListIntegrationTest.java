package es.hugoalvarezajenjo.selecta.integration;

import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListItem;
import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListService;
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

/**
 * Integration tests for the Enrollment List subsystem.
 * Verifies CRUD operations, position normalization, and constraints with real H2 DB.
 */
@SpringBootTest
@Transactional
@DisplayName("Enrollment List Integration Tests")
class EnrollmentListIntegrationTest {

    @Autowired
    private EnrollmentListService enrollmentListService;

    @Autowired
    private UserRepository userRepository;

    private User student;

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("carlos@example.com").orElseThrow();
    }

    @Test
    @DisplayName("Add subject to main list assigns correct position")
    void addSubject_assignsCorrectPosition() {
        enrollmentListService.addSubject(student, 1L);
        enrollmentListService.addSubject(student, 2L);
        enrollmentListService.addSubject(student, 3L);

        List<EnrollmentListItem> mainList = enrollmentListService.getMainList(student.getId());
        assertThat(mainList).hasSize(3);
        assertThat(mainList.get(0).getSubject().getId()).isEqualTo(1L);
        assertThat(mainList.get(0).getPosition()).isEqualTo(0);
        assertThat(mainList.get(1).getPosition()).isEqualTo(1);
        assertThat(mainList.get(2).getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("Duplicate subject is rejected")
    void addDuplicate_isRejected() {
        boolean first = enrollmentListService.addSubject(student, 1L);
        boolean second = enrollmentListService.addSubject(student, 1L);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(enrollmentListService.getMainList(student.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Remove subject renormalizes positions")
    void removeSubject_renormalizesPositions() {
        enrollmentListService.addSubject(student, 1L);
        enrollmentListService.addSubject(student, 2L);
        enrollmentListService.addSubject(student, 3L);

        enrollmentListService.removeSubject(student.getId(), 2L); // Remove middle

        List<EnrollmentListItem> mainList = enrollmentListService.getMainList(student.getId());
        assertThat(mainList).hasSize(2);
        assertThat(mainList.get(0).getSubject().getId()).isEqualTo(1L);
        assertThat(mainList.get(0).getPosition()).isEqualTo(0);
        assertThat(mainList.get(1).getSubject().getId()).isEqualTo(3L);
        assertThat(mainList.get(1).getPosition()).isEqualTo(1); // Renormalized
    }

    @Test
    @DisplayName("Reorder changes positions correctly")
    void reorder_changesPositions() {
        enrollmentListService.addSubject(student, 1L);
        enrollmentListService.addSubject(student, 2L);
        enrollmentListService.addSubject(student, 3L);

        // Reverse the order
        enrollmentListService.reorder(student.getId(), List.of(3L, 2L, 1L), false);

        List<EnrollmentListItem> mainList = enrollmentListService.getMainList(student.getId());
        assertThat(mainList.get(0).getSubject().getId()).isEqualTo(3L);
        assertThat(mainList.get(1).getSubject().getId()).isEqualTo(2L);
        assertThat(mainList.get(2).getSubject().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Toggle reserve moves item between lists")
    void toggleReserve_movesBetweenLists() {
        enrollmentListService.addSubject(student, 1L, false); // main
        enrollmentListService.addSubject(student, 2L, false); // main

        enrollmentListService.toggleReserve(student.getId(), 1L);

        assertThat(enrollmentListService.getMainList(student.getId())).hasSize(1);
        assertThat(enrollmentListService.getReserveList(student.getId())).hasSize(1);
        assertThat(enrollmentListService.getReserveList(student.getId()).get(0).getSubject().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Credits calculation is correct")
    void credits_calculatedCorrectly() {
        enrollmentListService.addSubject(student, 1L); // Math: 6 credits
        enrollmentListService.addSubject(student, 3L); // Programming: 4 credits

        int credits = enrollmentListService.getMainCredits(student.getId());
        assertThat(credits).isEqualTo(10); // 6 + 4
    }

    @Test
    @DisplayName("Note can be updated")
    void note_canBeUpdated() {
        enrollmentListService.addSubject(student, 1L);
        enrollmentListService.updateNote(student.getId(), 1L, "Grupo de mañana");

        List<EnrollmentListItem> list = enrollmentListService.getMainList(student.getId());
        assertThat(list.get(0).getNote()).isEqualTo("Grupo de mañana");
    }
}
