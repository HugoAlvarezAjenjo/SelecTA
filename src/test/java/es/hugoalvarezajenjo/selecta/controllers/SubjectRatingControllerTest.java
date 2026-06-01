package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRatingService;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectRatingControllerTest {

    @Mock
    private SubjectRatingService ratingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubjectRatingController controller;

    private Student createUser() {
        Student s = new Student();
        s.setId(1L);
        s.setUsername("student1");
        return s;
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET rating
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/subject/{id}/rating")
    class GetRatingTests {

        @Test
        @DisplayName("Returns average, count, and user rating when logged in")
        void returnsAllDataForLoggedInUser() {
            Student user = createUser();
            when(userService.getCurrentUser()).thenReturn(user);
            when(ratingService.getAverageRating(10L)).thenReturn(4.2);
            when(ratingService.getRatingCount(10L)).thenReturn(15L);

            SubjectRating existing = new SubjectRating();
            existing.setRating(5);
            when(ratingService.getUserRating(10L, 1L)).thenReturn(Optional.of(existing));

            ResponseEntity<?> response = controller.getRating(10L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("average", 4.2);
            assertThat(body).containsEntry("count", 15L);
            assertThat(body).containsEntry("userRating", 5);
        }

        @Test
        @DisplayName("Returns 0 userRating when not logged in")
        void returnsZeroUserRatingWhenAnonymous() {
            when(userService.getCurrentUser()).thenReturn(null);
            when(ratingService.getAverageRating(10L)).thenReturn(3.5);
            when(ratingService.getRatingCount(10L)).thenReturn(8L);

            ResponseEntity<?> response = controller.getRating(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("userRating", 0);
        }

        @Test
        @DisplayName("Returns 0 average when no ratings exist")
        void returnsZeroWhenNoRatings() {
            when(userService.getCurrentUser()).thenReturn(null);
            when(ratingService.getAverageRating(10L)).thenReturn(null);
            when(ratingService.getRatingCount(10L)).thenReturn(0L);

            ResponseEntity<?> response = controller.getRating(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("average", 0.0);
            assertThat(body).containsEntry("count", 0L);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // POST rating
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/subject/{id}/rating")
    class SetRatingTests {

        @Test
        @DisplayName("Sets rating and returns updated average")
        void setsRatingSuccessfully() {
            Student user = createUser();

            when(userService.getCurrentUser()).thenReturn(user);
            when(ratingService.getAverageRating(10L)).thenReturn(4.0);
            when(ratingService.getRatingCount(10L)).thenReturn(1L);

            ResponseEntity<?> response = controller.setRating(10L, 4);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(ratingService).setRating(10L, user, 4);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("userRating", 4);
        }

        @Test
        @DisplayName("Returns 400 for invalid rating value")
        void returnsBadRequestForInvalidRating() {
            ResponseEntity<?> response = controller.setRating(10L, 6);
            assertThat(response.getStatusCode().value()).isEqualTo(400);

            ResponseEntity<?> response2 = controller.setRating(10L, 0);
            assertThat(response2.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("Returns 401 when not logged in")
        void returns401WhenNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<?> response = controller.setRating(10L, 4);

            assertThat(response.getStatusCode().value()).isEqualTo(401);
        }
    }
}
