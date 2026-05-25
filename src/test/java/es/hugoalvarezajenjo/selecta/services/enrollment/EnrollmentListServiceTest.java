package es.hugoalvarezajenjo.selecta.services.enrollment;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentListServiceTest {

    @Mock
    private EnrollmentListRepository repository;

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private EnrollmentListService service;

    private Student createUser(Long id) {
        Student s = new Student();
        s.setId(id);
        s.setUsername("student" + id);
        return s;
    }

    private Subject createSubject(Long id, String name, int credits) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        subject.setCredits(credits);
        return subject;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adding subjects
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Adding subjects to enrollment list")
    class AddSubjectTests {

        @Test
        @DisplayName("A student can add a subject to their main list")
        void canAddSubjectToMainList() {
            Student user = createUser(1L);
            Subject subject = createSubject(10L, "AI", 6);

            when(repository.existsByUserIdAndSubjectId(1L, 10L)).thenReturn(false);
            when(subjectService.getSubjectById(10L)).thenReturn(Optional.of(subject));
            when(repository.countByUserIdAndReserveFalse(1L)).thenReturn(0);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.addSubject(user, 10L, false);

            assertThat(result).isTrue();
            verify(repository).save(argThat(item ->
                    item.getSubject().getId().equals(10L) &&
                    !item.isReserve() &&
                    item.getPosition() == 0));
        }

        @Test
        @DisplayName("A student can add a subject to their reserve list")
        void canAddSubjectToReserveList() {
            Student user = createUser(1L);
            Subject subject = createSubject(10L, "Networks", 4);

            when(repository.existsByUserIdAndSubjectId(1L, 10L)).thenReturn(false);
            when(subjectService.getSubjectById(10L)).thenReturn(Optional.of(subject));
            when(repository.countByUserIdAndReserveTrue(1L)).thenReturn(2);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.addSubject(user, 10L, true);

            assertThat(result).isTrue();
            verify(repository).save(argThat(item ->
                    item.isReserve() &&
                    item.getPosition() == 2));
        }

        @Test
        @DisplayName("Cannot add the same subject twice")
        void cannotAddDuplicate() {
            Student user = createUser(1L);

            when(repository.existsByUserIdAndSubjectId(1L, 10L)).thenReturn(true);

            boolean result = service.addSubject(user, 10L, false);

            assertThat(result).isFalse();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Adding a non-existent subject throws exception")
        void addingNonExistentSubjectThrows() {
            Student user = createUser(1L);

            when(repository.existsByUserIdAndSubjectId(1L, 99L)).thenReturn(false);
            when(subjectService.getSubjectById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addSubject(user, 99L, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Subject not found");
        }

        @Test
        @DisplayName("New items are appended at the end of the list")
        void newItemsAppendedAtEnd() {
            Student user = createUser(1L);
            Subject subject = createSubject(10L, "DB", 6);

            when(repository.existsByUserIdAndSubjectId(1L, 10L)).thenReturn(false);
            when(subjectService.getSubjectById(10L)).thenReturn(Optional.of(subject));
            when(repository.countByUserIdAndReserveFalse(1L)).thenReturn(5);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.addSubject(user, 10L, false);

            verify(repository).save(argThat(item -> item.getPosition() == 5));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Removing subjects
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Removing subjects from enrollment list")
    class RemoveSubjectTests {

        @Test
        @DisplayName("Removing a subject normalizes positions in the source list")
        void removingNormalizesPositions() {
            EnrollmentListItem item = new EnrollmentListItem();
            item.setReserve(false);

            when(repository.findByUserIdAndSubjectId(1L, 10L)).thenReturn(Optional.of(item));
            when(repository.findByUserIdAndReserveFalseOrderByPositionAsc(1L)).thenReturn(List.of());

            service.removeSubject(1L, 10L);

            verify(repository).deleteByUserIdAndSubjectId(1L, 10L);
        }

        @Test
        @DisplayName("Removing a non-existent item does nothing")
        void removingNonExistentDoesNothing() {
            when(repository.findByUserIdAndSubjectId(1L, 99L)).thenReturn(Optional.empty());

            service.removeSubject(1L, 99L);

            verify(repository, never()).deleteByUserIdAndSubjectId(any(), any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Toggling reserve
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Toggling between main and reserve")
    class ToggleReserveTests {

        @Test
        @DisplayName("Toggling moves a subject from main to reserve")
        void toggleMainToReserve() {
            Subject subject = createSubject(10L, "AI", 6);
            EnrollmentListItem item = new EnrollmentListItem();
            item.setSubject(subject);
            item.setReserve(false);
            item.setPosition(2);

            when(repository.findByUserIdAndSubjectId(1L, 10L)).thenReturn(Optional.of(item));
            when(repository.countByUserIdAndReserveTrue(1L)).thenReturn(1);
            when(repository.findByUserIdAndReserveFalseOrderByPositionAsc(1L)).thenReturn(List.of());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.toggleReserve(1L, 10L);

            assertThat(item.isReserve()).isTrue();
            assertThat(item.getPosition()).isEqualTo(1); // end of reserve list
        }

        @Test
        @DisplayName("Toggling moves a subject from reserve to main")
        void toggleReserveToMain() {
            Subject subject = createSubject(10L, "Networks", 4);
            EnrollmentListItem item = new EnrollmentListItem();
            item.setSubject(subject);
            item.setReserve(true);
            item.setPosition(0);

            when(repository.findByUserIdAndSubjectId(1L, 10L)).thenReturn(Optional.of(item));
            when(repository.countByUserIdAndReserveFalse(1L)).thenReturn(3);
            when(repository.findByUserIdAndReserveTrueOrderByPositionAsc(1L)).thenReturn(List.of());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.toggleReserve(1L, 10L);

            assertThat(item.isReserve()).isFalse();
            assertThat(item.getPosition()).isEqualTo(3); // end of main list
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Reordering
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Reordering the list")
    class ReorderTests {

        @Test
        @DisplayName("Reordering updates positions correctly")
        void reorderingUpdatesPositions() {
            Subject s1 = createSubject(1L, "A", 6);
            Subject s2 = createSubject(2L, "B", 6);
            Subject s3 = createSubject(3L, "C", 6);

            EnrollmentListItem item1 = new EnrollmentListItem();
            item1.setSubject(s1); item1.setPosition(0);
            EnrollmentListItem item2 = new EnrollmentListItem();
            item2.setSubject(s2); item2.setPosition(1);
            EnrollmentListItem item3 = new EnrollmentListItem();
            item3.setSubject(s3); item3.setPosition(2);

            when(repository.findByUserIdAndReserveFalseOrderByPositionAsc(1L))
                    .thenReturn(List.of(item1, item2, item3));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Reorder: C, A, B
            service.reorder(1L, List.of(3L, 1L, 2L), false);

            assertThat(item3.getPosition()).isEqualTo(0);
            assertThat(item1.getPosition()).isEqualTo(1);
            assertThat(item2.getPosition()).isEqualTo(2);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Credits calculation
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Credits calculation")
    class CreditsTests {

        @Test
        @DisplayName("Main credits sums only non-reserve items")
        void mainCreditsOnlyNonReserve() {
            Subject s1 = createSubject(1L, "A", 6);
            Subject s2 = createSubject(2L, "B", 4);

            EnrollmentListItem item1 = new EnrollmentListItem();
            item1.setSubject(s1);
            EnrollmentListItem item2 = new EnrollmentListItem();
            item2.setSubject(s2);

            when(repository.findByUserIdAndReserveFalseOrderByPositionAsc(1L))
                    .thenReturn(List.of(item1, item2));

            int credits = service.getMainCredits(1L);

            assertThat(credits).isEqualTo(10);
        }

        @Test
        @DisplayName("Empty list returns 0 credits")
        void emptyListZeroCredits() {
            when(repository.findByUserIdAndReserveFalseOrderByPositionAsc(1L))
                    .thenReturn(List.of());

            assertThat(service.getMainCredits(1L)).isEqualTo(0);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Notes
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Notes management")
    class NotesTests {

        @Test
        @DisplayName("Updating a note trims whitespace and saves")
        void updatingNoteTrimsSaves() {
            EnrollmentListItem item = new EnrollmentListItem();
            item.setNote(null);

            when(repository.findByUserIdAndSubjectId(1L, 10L)).thenReturn(Optional.of(item));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.updateNote(1L, 10L, "  grupo mañana  ");

            assertThat(item.getNote()).isEqualTo("grupo mañana");
            verify(repository).save(item);
        }

        @Test
        @DisplayName("Setting null note clears it")
        void nullNoteClearsIt() {
            EnrollmentListItem item = new EnrollmentListItem();
            item.setNote("old note");

            when(repository.findByUserIdAndSubjectId(1L, 10L)).thenReturn(Optional.of(item));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.updateNote(1L, 10L, null);

            assertThat(item.getNote()).isNull();
        }
    }
}
