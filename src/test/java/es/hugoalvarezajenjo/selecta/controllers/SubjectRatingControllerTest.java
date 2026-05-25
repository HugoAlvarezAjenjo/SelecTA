package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectRatingControllerTest {

    @Mock
    private SubjectRatingRepository ratingRepository;

    @Mock
    private SubjectRepository subjectRepository;

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
            when(ratingRepository.getAverageRating(10L)).thenReturn(4.2);
            when(ratingRepository.countBySubjectId(10L)).thenReturn(15L);

            SubjectRating existing = new SubjectRating();
            existing.setRating(5);
            when(ratingRepository.findBySubjectIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

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
            when(ratingRepository.getAverageRating(10L)).thenReturn(3.5);
            when(ratingRepository.countBySubjectId(10L)).thenReturn(8L);

            ResponseEntity<?> response = controller.getRating(10L);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("userRating", 0);
        }

        @Test
        @DisplayName("Returns 0 average when no ratings exist")
        void returnsZeroWhenNoRatings() {
            when(userService.getCurrentUser()).thenReturn(null);
            when(ratingRepository.getAverageRating(10L)).thenReturn(null);
            when(ratingRepository.countBySubjectId(10L)).thenReturn(0L);

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
            Subject subject = new Subject();
            subject.setId(10L);

            when(userService.getCurrentUser()).thenReturn(user);
            when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
            when(ratingRepository.findBySubjectIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
            when(ratingRepository.getAverageRating(10L)).thenReturn(4.0);
            when(ratingRepository.countBySubjectId(10L)).thenReturn(1L);

            ResponseEntity<?> response = controller.setRating(10L, 4);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(ratingRepository).save(any(SubjectRating.class));
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("userRating", 4);
        }

        @Test
        @DisplayName("Updates existing rating instead of creating new one")
        void updatesExistingRating() {
            Student user = createUser();
            Subject subject = new Subject();
            subject.setId(10L);

            SubjectRating existing = new SubjectRating();
            existing.setRating(3);
            existing.setSubject(subject);
            existing.setUser(user);

            when(userService.getCurrentUser()).thenReturn(user);
            when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
            when(ratingRepository.findBySubjectIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
            when(ratingRepository.getAverageRating(10L)).thenReturn(5.0);
            when(ratingRepository.countBySubjectId(10L)).thenReturn(1L);

            controller.setRating(10L, 5);

            verify(ratingRepository).save(existing);
            assertThat(existing.getRating()).isEqualTo(5);
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

        @Test
        @DisplayName("Returns 404 when subject not found")
        void returns404WhenSubjectNotFound() {
            Student user = createUser();
            when(userService.getCurrentUser()).thenReturn(user);
            when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.setRating(99L, 4);

            assertThat(response.getStatusCode().value()).isEqualTo(404);
        }
    }
}
