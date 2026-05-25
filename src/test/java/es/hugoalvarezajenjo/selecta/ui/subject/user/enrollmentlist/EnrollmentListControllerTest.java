package es.hugoalvarezajenjo.selecta.ui.subject.user.enrollmentlist;

import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListItem;
import es.hugoalvarezajenjo.selecta.services.enrollment.EnrollmentListService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentListControllerTest {

    @Mock
    private EnrollmentListService enrollmentListService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private EnrollmentListController controller;

    private Student createStudent() {
        Student s = new Student();
        s.setId(1L);
        s.setUsername("testStudent");
        return s;
    }

    private EnrollmentListItem createItem(Long subjectId, String name, int credits, int position) {
        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setName(name);
        subject.setCredits(credits);

        EnrollmentListItem item = new EnrollmentListItem();
        item.setSubject(subject);
        item.setPosition(position);
        return item;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Page rendering
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /enrollment-list (page)")
    class ShowPageTests {

        @Test
        @DisplayName("Redirects to login when user is not authenticated")
        void redirectsWhenNotAuthenticated() {
            when(userService.getCurrentUser()).thenReturn(null);

            String result = controller.showEnrollmentList(model);

            assertThat(result).isEqualTo("redirect:/login");
            verifyNoInteractions(enrollmentListService);
        }

        @Test
        @DisplayName("Returns enrollment list view with model attributes")
        void returnsViewWithModelAttributes() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.getMainList(1L)).thenReturn(List.of(
                    createItem(10L, "AI", 6, 0)));
            when(enrollmentListService.getReserveList(1L)).thenReturn(List.of());
            when(enrollmentListService.getMainCredits(1L)).thenReturn(6);
            when(enrollmentListService.getReserveCredits(1L)).thenReturn(0);

            String result = controller.showEnrollmentList(model);

            assertThat(result).isEqualTo("subject/user/enrollment-list");
            verify(model).addAttribute("mainItems", enrollmentListService.getMainList(1L));
            verify(model).addAttribute("mainCount", 1);
            verify(model).addAttribute("reserveCount", 0);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Add subject API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /enrollment-list/api/add")
    class AddSubjectApiTests {

        @Test
        @DisplayName("Returns 401 when not logged in")
        void returns401WhenNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<?> response = controller.addSubject(10L, false);

            assertThat(response.getStatusCode().value()).isEqualTo(401);
        }

        @Test
        @DisplayName("Returns success when subject is added to main list")
        void returnsSuccessWhenAdded() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.addSubject(student, 10L, false)).thenReturn(true);

            ResponseEntity<?> response = controller.addSubject(10L, false);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("success", true);
        }

        @Test
        @DisplayName("Returns 400 when subject already in list")
        void returnsBadRequestWhenDuplicate() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.addSubject(student, 10L, false)).thenReturn(false);

            ResponseEntity<?> response = controller.addSubject(10L, false);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("Adds to reserve list when asReserve is true")
        void addsToReserveWhenFlagSet() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.addSubject(student, 10L, true)).thenReturn(true);

            ResponseEntity<?> response = controller.addSubject(10L, true);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(enrollmentListService).addSubject(student, 10L, true);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Remove subject API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /enrollment-list/api/remove/{id}")
    class RemoveSubjectApiTests {

        @Test
        @DisplayName("Removes subject and returns updated credits")
        void removesAndReturnsCredits() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.getMainCredits(1L)).thenReturn(6);
            when(enrollmentListService.getReserveCredits(1L)).thenReturn(4);

            ResponseEntity<?> response = controller.removeSubject(10L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(enrollmentListService).removeSubject(1L, 10L);
        }

        @Test
        @DisplayName("Returns 401 when not logged in")
        void returns401WhenNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<?> response = controller.removeSubject(10L);

            assertThat(response.getStatusCode().value()).isEqualTo(401);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Toggle reserve API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /enrollment-list/api/toggle-reserve/{id}")
    class ToggleReserveApiTests {

        @Test
        @DisplayName("Toggles reserve status")
        void togglesReserve() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);

            ResponseEntity<?> response = controller.toggleReserve(10L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(enrollmentListService).toggleReserve(1L, 10L);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Reorder API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /enrollment-list/api/reorder")
    class ReorderApiTests {

        @Test
        @DisplayName("Reorders main list")
        void reordersMainList() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);

            ReorderRequest request = new ReorderRequest();
            request.setOrderedSubjectIds(List.of(3L, 1L, 2L));
            request.setReserve(false);

            ResponseEntity<?> response = controller.reorderList(request);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(enrollmentListService).reorder(1L, List.of(3L, 1L, 2L), false);
        }

        @Test
        @DisplayName("Reorders reserve list")
        void reordersReserveList() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);

            ReorderRequest request = new ReorderRequest();
            request.setOrderedSubjectIds(List.of(5L, 4L));
            request.setReserve(true);

            ResponseEntity<?> response = controller.reorderList(request);

            verify(enrollmentListService).reorder(1L, List.of(5L, 4L), true);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Note API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /enrollment-list/api/note/{id}")
    class NoteApiTests {

        @Test
        @DisplayName("Updates note for subject")
        void updatesNote() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);

            NoteRequest request = new NoteRequest();
            request.setNote("grupo mañana");

            ResponseEntity<?> response = controller.updateNote(10L, request);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(enrollmentListService).updateNote(1L, 10L, "grupo mañana");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Check API
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /enrollment-list/api/check/{id}")
    class CheckApiTests {

        @Test
        @DisplayName("Returns inList true when subject is in user's list")
        void returnsTrueWhenInList() {
            Student student = createStudent();
            when(userService.getCurrentUser()).thenReturn(student);
            when(enrollmentListService.isInList(1L, 10L)).thenReturn(true);

            ResponseEntity<?> response = controller.checkInList(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("inList", true);
        }

        @Test
        @DisplayName("Returns inList false when not logged in")
        void returnsFalseWhenAnonymous() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<?> response = controller.checkInList(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("inList", false);
        }
    }
}
