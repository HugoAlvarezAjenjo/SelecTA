package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceVoteService;
import es.hugoalvarezajenjo.selecta.services.resources.VoteResult;
import es.hugoalvarezajenjo.selecta.services.resources.VoteType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Votos", description = "Sistema de upvote/downvote para recursos compartidos")
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceVoteController {

    private final ResourceVoteService resourceVoteService;

    @Operation(summary = "Votar un recurso",
            description = "Toggle de voto (UPVOTE/DOWNVOTE). Si ya existe el mismo voto, se elimina. Si existe otro tipo, se cambia. Requiere autenticación.")
    @PostMapping("/{resourceId}/vote")
    public ResponseEntity<?> vote(
            @Parameter(description = "ID del recurso") @PathVariable final Long resourceId,
            @Parameter(description = "Tipo de voto: UPVOTE o DOWNVOTE") @RequestParam final VoteType type) {
        try {
            final VoteResult result = resourceVoteService.toggleVote(resourceId, type);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Must be logged in to vote"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
