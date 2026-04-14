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

@RestController
@RequestMapping("/api/subject/{subjectId}/tags")
@RequiredArgsConstructor
public class ResourceTagController {

    private final ResourceTagService tagService;
    private final SubjectService subjectService;

    /**
     * GET /api/subject/{subjectId}/tags — Full tag tree for the subject.
     */
    @GetMapping
    public ResponseEntity<List<ResourceTagDTO>> getTagTree(@PathVariable final Long subjectId) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final List<ResourceTagDTO> tree = this.tagService.getTagTree(subject.get()).stream()
                .map(ResourceTagDTO::createFromDomain)
                .toList();
        return ResponseEntity.ok(tree);
    }

    /**
     * GET /api/subject/{subjectId}/tags/all — Flat list of all tags.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ResourceTagDTO>> getAllTags(@PathVariable final Long subjectId) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final List<ResourceTagDTO> tags = this.tagService.getAllTags(subject.get()).stream()
                .map(ResourceTagDTO::createFlat)
                .toList();
        return ResponseEntity.ok(tags);
    }

    /**
     * GET /api/subject/{subjectId}/tags/search?q=query — Search tags by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ResourceTagDTO>> searchTags(
            @PathVariable final Long subjectId,
            @RequestParam("q") final String query) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final List<ResourceTagDTO> results = this.tagService.searchTags(subject.get(), query).stream()
                .map(ResourceTagDTO::createFlat)
                .toList();
        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/subject/{subjectId}/tags — Create a new tag.
     * Body: { "name": "Tema 1", "parentId": null }
     */
    @PostMapping
    public ResponseEntity<?> createTag(
            @PathVariable final Long subjectId,
            @RequestBody final Map<String, Object> body) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tag name is required"));
        }

        ResourceTag parent = null;
        if (body.get("parentId") != null) {
            final Long parentId = ((Number) body.get("parentId")).longValue();
            parent = this.tagService.findById(parentId).orElse(null);
        }

        try {
            final ResourceTag tag = this.tagService.getOrCreateTag(name, subject.get(), parent);
            return ResponseEntity.ok(ResourceTagDTO.createFlat(tag));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/subject/{subjectId}/tags/{tagId}/rename — Rename a tag.
     * Body: { "name": "New Name" }
     */
    @PutMapping("/{tagId}/rename")
    public ResponseEntity<?> renameTag(
            @PathVariable final Long subjectId,
            @PathVariable final Long tagId,
            @RequestBody final Map<String, String> body) {
        try {
            final ResourceTag tag = this.tagService.renameTag(tagId, body.get("name"));
            return ResponseEntity.ok(ResourceTagDTO.createFlat(tag));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/subject/{subjectId}/tags/{tagId}/move — Move a tag to a new parent.
     * Body: { "parentId": 5 } or { "parentId": null } for root.
     */
    @PutMapping("/{tagId}/move")
    public ResponseEntity<?> moveTag(
            @PathVariable final Long subjectId,
            @PathVariable final Long tagId,
            @RequestBody final Map<String, Object> body) {
        try {
            final Long parentId = body.get("parentId") != null
                    ? ((Number) body.get("parentId")).longValue()
                    : null;
            final ResourceTag tag = this.tagService.moveTag(tagId, parentId);
            return ResponseEntity.ok(ResourceTagDTO.createFlat(tag));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/subject/{subjectId}/tags/{tagId} — Delete a tag (resources keep intact).
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(
            @PathVariable final Long subjectId,
            @PathVariable final Long tagId) {
        try {
            this.tagService.deleteTag(tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/subject/{subjectId}/tags/{tagId}/resources/{resourceId} — Tag a resource.
     */
    @PostMapping("/{tagId}/resources/{resourceId}")
    public ResponseEntity<?> tagResource(
            @PathVariable final Long subjectId,
            @PathVariable final Long tagId,
            @PathVariable final Long resourceId) {
        try {
            this.tagService.tagResource(resourceId, tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/subject/{subjectId}/tags/{tagId}/resources/{resourceId} — Untag a resource.
     */
    @DeleteMapping("/{tagId}/resources/{resourceId}")
    public ResponseEntity<?> untagResource(
            @PathVariable final Long subjectId,
            @PathVariable final Long tagId,
            @PathVariable final Long resourceId) {
        try {
            this.tagService.untagResource(resourceId, tagId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/subject/{subjectId}/resources/search?q=query — Obsidian-style resource search.
     */
    @GetMapping("/resources/search")
    public ResponseEntity<List<SubjectResourceDTO>> searchResources(
            @PathVariable final Long subjectId,
            @RequestParam("q") final String query) {
        final List<SubjectResource> results = this.tagService.searchResources(subjectId, query);
        return ResponseEntity.ok(SubjectResourceDTO.createFromDomain(results));
    }
}
