package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceTag;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceTagService;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject.ResourceTagDTO;
import es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject.SubjectResourceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Etiquetado de recursos para facilitar su búsqueda y filtrado")
@RestController
@RequestMapping("/api/subjects/{subjectId}/tags")
@RequiredArgsConstructor
public class ResourceTagController {

    private final ResourceTagService tagService;
    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<ResourceTagDTO>> getAllTags(@PathVariable final Long subjectId) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ResourceTagDTO.createListFromDomain(this.tagService.getAllTags(subject.get())));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceTagDTO>> searchTags(@PathVariable final Long subjectId, @RequestParam("q") final String query) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ResourceTagDTO.createListFromDomain(this.tagService.searchTags(subject.get(), query)));
    }

    @PostMapping
    public ResponseEntity<?> createTag(@PathVariable final Long subjectId, @RequestBody final Map<String, String> body) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        final String name = body.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        try {
            return ResponseEntity.ok(ResourceTagDTO.createFromDomain(this.tagService.getOrCreateTag(name, subject.get())));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{tagId}/rename")
    public ResponseEntity<?> renameTag(@PathVariable final Long subjectId, @PathVariable final Long tagId, @RequestBody final Map<String, String> body) {
        try {
            return ResponseEntity.ok(ResourceTagDTO.createFromDomain(this.tagService.renameTag(tagId, body.get("name"))));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(@PathVariable final Long subjectId, @PathVariable final Long tagId) {
        try {
            this.tagService.deleteTag(tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{tagId}/resources/{resourceId}")
    public ResponseEntity<?> tagResource(@PathVariable final Long subjectId, @PathVariable final Long tagId, @PathVariable final Long resourceId) {
        try {
            this.tagService.tagResource(resourceId, tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{tagId}/resources/{resourceId}")
    public ResponseEntity<?> untagResource(@PathVariable final Long subjectId, @PathVariable final Long tagId, @PathVariable final Long resourceId) {
        try {
            this.tagService.untagResource(resourceId, tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/resources/search")
    public ResponseEntity<List<SubjectResourceDTO>> searchResources(@PathVariable final Long subjectId, @RequestParam("q") final String query) {
        return ResponseEntity.ok(SubjectResourceDTO.createFromDomain(this.tagService.searchResources(subjectId, query)));
    }
}
