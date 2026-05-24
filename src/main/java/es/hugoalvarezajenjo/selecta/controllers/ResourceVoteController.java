package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceVoteService;
import es.hugoalvarezajenjo.selecta.services.resources.VoteType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceVoteController {

    private final ResourceVoteService resourceVoteService;

    @PostMapping("/{resourceId}/vote")
    public ResponseEntity<Map<String, Object>> vote(
            @PathVariable final Long resourceId,
            @RequestParam final VoteType type) {
        try {
            final Map<String, Object> result = resourceVoteService.toggleVote(resourceId, type);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Must be logged in to vote"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
