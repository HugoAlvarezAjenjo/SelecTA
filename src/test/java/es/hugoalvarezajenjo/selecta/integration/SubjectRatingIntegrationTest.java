package es.hugoalvarezajenjo.selecta.integration;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Subject Ratings.
 * Verifies persistence, constraints, and aggregation queries.
 */
@SpringBootTest
@Transactional
@DisplayName("Subject Rating Integration Tests")
class SubjectRatingIntegrationTest {

    @Autowired
    private SubjectRatingRepository ratingRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserRepository userRepository;

    private User carlos;
    private User maria;
    private Subject math;

    @BeforeEach
    void setUp() {
        carlos = userRepository.findByEmail("carlos@example.com").orElseThrow();
        maria = userRepository.findByEmail("maria@example.com").orElseThrow();
        math = subjectService.getSubjectById(1L).orElseThrow();
    }

    @Test
    @DisplayName("Rating is persisted with correct data")
    void rating_isPersisted() {
        SubjectRating rating = new SubjectRating();
        rating.setSubject(math);
        rating.setUser(carlos);
        rating.setRating(5);
        ratingRepository.save(rating);

        Optional<SubjectRating> found = ratingRepository.findBySubjectIdAndUserId(1L, carlos.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Updating rating overrides the existing value")
    void updatingRating_overridesValue() {
        SubjectRating rating = new SubjectRating();
        rating.setSubject(math);
        rating.setUser(carlos);
        rating.setRating(3);
        ratingRepository.save(rating);

        // Update
        SubjectRating existing = ratingRepository.findBySubjectIdAndUserId(1L, carlos.getId()).orElseThrow();
        existing.setRating(5);
        ratingRepository.save(existing);

        assertThat(ratingRepository.findBySubjectIdAndUserId(1L, carlos.getId()).get().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Average rating is calculated correctly")
    void averageRating_calculatedCorrectly() {
        // Carlos rates 4, Maria rates 2 → average = 3.0
        saveRating(carlos, math, 4);
        saveRating(maria, math, 2);

        Double avg = ratingRepository.getAverageRating(1L);
        assertThat(avg).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Count by subject returns correct count")
    void countBySubject_returnsCorrectCount() {
        saveRating(carlos, math, 5);
        saveRating(maria, math, 4);

        long count = ratingRepository.countBySubjectId(1L);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("One user's rating does not affect another user's rating")
    void ratings_areIsolatedPerUser() {
        saveRating(carlos, math, 5);
        saveRating(maria, math, 2);

        assertThat(ratingRepository.findBySubjectIdAndUserId(1L, carlos.getId()).get().getRating()).isEqualTo(5);
        assertThat(ratingRepository.findBySubjectIdAndUserId(1L, maria.getId()).get().getRating()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllWithSubject loads subjects eagerly for popularity calculation")
    void findAllWithSubject_loadsSubjects() {
        saveRating(carlos, math, 5);

        var all = ratingRepository.findAllWithSubject();
        assertThat(all).isNotEmpty();
        assertThat(all.get(0).getSubject()).isNotNull();
        assertThat(all.get(0).getSubject().getName()).isNotNull();
    }

    // ──────────────────────────────────────────────────────────────────────
    private void saveRating(User user, Subject subject, int rating) {
        SubjectRating r = new SubjectRating();
        r.setSubject(subject);
        r.setUser(user);
        r.setRating(rating);
        ratingRepository.save(r);
    }
}
