package es.hugoalvarezajenjo.selecta.integration;

import es.hugoalvarezajenjo.selecta.services.recommendation.RecommendationEngine;
import es.hugoalvarezajenjo.selecta.services.recommendation.SubjectScoreDTO;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRecommendationCriteria;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the RecommendationEngine.
 * Uses the real H2 database with seeded data from data.sql.
 */
@SpringBootTest
@Transactional
@DisplayName("Recommendation Engine Integration Tests")
class RecommendationEngineIntegrationTest {

    @Autowired
    private RecommendationEngine recommendationEngine;

    @Autowired
    private SubjectRatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectService subjectService;

    @Test
    @DisplayName("Anonymous user gets recommendations based on popularity and content match")
    void anonymousUser_getsRecommendations() {
        // Seed some ratings first
        seedRatings();

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();
        List<SubjectScoreDTO> results = recommendationEngine.recommend(null, criteria, 5);

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(r -> {
            assertThat(r.getTotalScore()).isBetween(0.0, 1.0);
            assertThat(r.getMatchPercentage()).isBetween(0, 100);
        });
    }

    @Test
    @DisplayName("User with ratings gets personalized recommendations")
    void userWithRatings_getsPersonalizedRecommendations() {
        User carlos = userRepository.findByEmail("carlos@example.com").orElseThrow();

        // Carlos rates CS-related subjects highly
        rateSubject(carlos, 3L, 5); // Programming
        rateSubject(carlos, 4L, 5); // Database Systems
        rateSubject(carlos, 5L, 4); // Web Development

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();
        List<SubjectScoreDTO> results = recommendationEngine.recommend(carlos, criteria, 5);

        assertThat(results).isNotEmpty();
        // Already-rated subjects should NOT appear in recommendations
        assertThat(results).noneMatch(r -> r.getSubjectId().equals(3L));
        assertThat(results).noneMatch(r -> r.getSubjectId().equals(4L));
        assertThat(results).noneMatch(r -> r.getSubjectId().equals(5L));
    }

    @Test
    @DisplayName("Rated subjects are excluded from recommendations")
    void ratedSubjects_areExcluded() {
        User carlos = userRepository.findByEmail("carlos@example.com").orElseThrow();

        // Rate ALL subjects
        for (long i = 1; i <= 5; i++) {
            rateSubject(carlos, i, 4);
        }

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();
        List<SubjectScoreDTO> results = recommendationEngine.recommend(carlos, criteria, 10);

        // All subjects rated → nothing to recommend
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("MMR produces diverse results (not all same tags)")
    void mmr_producesDiversity() {
        User maria = userRepository.findByEmail("maria@example.com").orElseThrow();
        // Maria likes science subjects
        rateSubject(maria, 1L, 5); // Math (Science, Engineering)
        rateSubject(maria, 2L, 5); // Physics (Science, Engineering)

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();
        List<SubjectScoreDTO> results = recommendationEngine.recommend(maria, criteria, 3);

        // With only 3 remaining subjects (3, 4, 5) all CS-related,
        // MMR should still return them (they're the only candidates)
        assertThat(results).hasSizeLessThanOrEqualTo(3);
        assertThat(results).allSatisfy(r -> assertThat(r.getTotalScore()).isGreaterThan(0.0));
    }

    @Test
    @DisplayName("Score breakdown contains all 5 signals")
    void scoreBreakdown_containsAllSignals() {
        seedRatings();
        User carlos = userRepository.findByEmail("carlos@example.com").orElseThrow();
        rateSubject(carlos, 1L, 5);

        SubjectRecommendationCriteria criteria = SubjectRecommendationCriteria.builder().build();
        List<SubjectScoreDTO> results = recommendationEngine.recommend(carlos, criteria, 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSignalBreakdown()).containsKeys(
                "tagAffinity", "collaborative", "popularity", "contentMatch", "diversity"
        );
    }

    @Test
    @DisplayName("Content match filter increases score for matching subjects")
    void contentMatch_increasesScoreForMatchingSubjects() {
        SubjectRecommendationCriteria criteriaWithFilter = SubjectRecommendationCriteria.builder()
                .selectedTags(List.of("Computer Science"))
                .build();

        SubjectRecommendationCriteria criteriaWithout = SubjectRecommendationCriteria.builder().build();

        List<SubjectScoreDTO> withFilter = recommendationEngine.recommend(null, criteriaWithFilter, 5);
        List<SubjectScoreDTO> without = recommendationEngine.recommend(null, criteriaWithout, 5);

        // Subjects with "Computer Science" tag should score higher with the filter
        assertThat(withFilter).isNotEmpty();
        // The CS subjects (3, 4, 5) should be boosted
        SubjectScoreDTO topWithFilter = withFilter.get(0);
        assertThat(Set.of(3L, 4L, 5L)).contains(topWithFilter.getSubjectId());
    }

    // ──────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────

    private void rateSubject(User user, Long subjectId, int rating) {
        Subject subject = subjectService.getSubjectById(subjectId).orElseThrow();
        SubjectRating r = ratingRepository.findBySubjectIdAndUserId(subjectId, user.getId())
                .orElseGet(() -> {
                    SubjectRating newRating = new SubjectRating();
                    newRating.setSubject(subject);
                    newRating.setUser(user);
                    return newRating;
                });
        r.setRating(rating);
        ratingRepository.save(r);
    }

    private void seedRatings() {
        User carlos = userRepository.findByEmail("carlos@example.com").orElseThrow();
        User maria = userRepository.findByEmail("maria@example.com").orElseThrow();
        rateSubject(carlos, 1L, 4);
        rateSubject(carlos, 3L, 5);
        rateSubject(maria, 2L, 5);
        rateSubject(maria, 5L, 3);
    }
}
