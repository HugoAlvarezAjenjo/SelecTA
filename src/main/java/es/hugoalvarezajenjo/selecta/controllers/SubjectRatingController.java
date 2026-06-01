package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRatingService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/subject/{subjectId}/rating")
@RequiredArgsConstructor
public class SubjectRatingController {

    private final SubjectRatingService ratingService;
    private final UserService userService;

    /**
     * GET — returns average rating + user's own rating
     */
    @GetMapping
    public ResponseEntity<?> getRating(@PathVariable final Long subjectId) {
        final Double avg = this.ratingService.getAverageRating(subjectId);
        final long count = this.ratingService.getRatingCount(subjectId);

        Integer userRating = null;
        final User user = this.userService.getCurrentUser();
        if (user != null) {
            userRating = this.ratingService.getUserRating(subjectId, user.getId())
                    .map(SubjectRating::getRating)
                    .orElse(null);
        }

        return ResponseEntity.ok(Map.of(
                "average", avg != null ? Math.round(avg * 10.0) / 10.0 : 0,
                "count", count,
                "userRating", userRating != null ? userRating : 0
        ));
    }

    /**
     * POST — set/update user's rating (1-5)
     */
    @PostMapping
    public ResponseEntity<?> setRating(@PathVariable final Long subjectId, @RequestParam final int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }

        final User user = this.userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Must be logged in"));
        }

        this.ratingService.setRating(subjectId, user, rating);

        // Return updated average
        final Double avg = this.ratingService.getAverageRating(subjectId);
        final long count = this.ratingService.getRatingCount(subjectId);

        return ResponseEntity.ok(Map.of(
                "average", avg != null ? Math.round(avg * 10.0) / 10.0 : 0,
                "count", count,
                "userRating", rating
        ));
    }
}
