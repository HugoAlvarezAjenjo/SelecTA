package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRatingService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Ratings", description = "Valoraciones de asignaturas (1-5 estrellas)")
@RestController
@RequestMapping("/api/subjects/{subjectId}/rating")
@RequiredArgsConstructor
public class SubjectRatingController {

    private final SubjectRatingService ratingService;
    private final UserService userService;

    @Operation(summary = "Obtener rating de una asignatura",
            description = "Devuelve la media, número de valoraciones y la valoración del usuario actual (si está autenticado)")
    @GetMapping
    public ResponseEntity<?> getRating(
            @Parameter(description = "ID de la asignatura") @PathVariable final Long subjectId) {
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

    @Operation(summary = "Valorar una asignatura",
            description = "Establece o actualiza la valoración del usuario actual. Requiere autenticación.")
    @PostMapping
    public ResponseEntity<?> setRating(
            @Parameter(description = "ID de la asignatura") @PathVariable final Long subjectId,
            @Parameter(description = "Valoración (1-5)") @RequestParam final int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }

        final User user = this.userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Must be logged in"));
        }

        this.ratingService.setRating(subjectId, user, rating);

        final Double avg = this.ratingService.getAverageRating(subjectId);
        final long count = this.ratingService.getRatingCount(subjectId);

        return ResponseEntity.ok(Map.of(
                "average", avg != null ? Math.round(avg * 10.0) / 10.0 : 0,
                "count", count,
                "userRating", rating
        ));
    }
}
