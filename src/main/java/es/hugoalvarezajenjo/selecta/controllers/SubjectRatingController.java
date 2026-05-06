package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subject/{subjectId}/rating")
@RequiredArgsConstructor
public class SubjectRatingController {

    private final SubjectRatingRepository ratingRepository;
    private final SubjectRepository subjectRepository;
    private final UserService userService;

    /**
     * GET — returns average rating + user's own rating
     */
    @GetMapping
    public ResponseEntity<?> getRating(@PathVariable final Long subjectId) {
        final Double avg = this.ratingRepository.getAverageRating(subjectId);
        final long count = this.ratingRepository.countBySubjectId(subjectId);

        Integer userRating = null;
        final User user = this.userService.getCurrentUser();
        if (user != null) {
            final Optional<SubjectRating> existing = this.ratingRepository.findBySubjectIdAndUserId(subjectId, user.getId());
            userRating = existing.map(SubjectRating::getRating).orElse(null);
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

        final Subject subject = this.subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }

        final SubjectRating entity = this.ratingRepository.findBySubjectIdAndUserId(subjectId, user.getId())
                .orElseGet(() -> {
                    final SubjectRating r = new SubjectRating();
                    r.setSubject(subject);
                    r.setUser(user);
                    return r;
                });
        entity.setRating(rating);
        this.ratingRepository.save(entity);

        // Return updated average
        final Double avg = this.ratingRepository.getAverageRating(subjectId);
        final long count = this.ratingRepository.countBySubjectId(subjectId);

        return ResponseEntity.ok(Map.of(
                "average", avg != null ? Math.round(avg * 10.0) / 10.0 : 0,
                "count", count,
                "userRating", rating
        ));
    }
}
