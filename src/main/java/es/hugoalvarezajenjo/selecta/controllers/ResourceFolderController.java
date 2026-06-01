package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceFolder;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceFolderService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject.ResourceFolderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Carpetas", description = "Organización de recursos en carpetas jerárquicas")
@RestController
@RequestMapping("/api/subjects/{subjectId}/folders")
@RequiredArgsConstructor
public class ResourceFolderController {

    private final ResourceFolderService folderService;
    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<ResourceFolderDTO>> getFolderTree(@PathVariable final Long subjectId) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(this.folderService.getFolderTree(subject.get()).stream()
                .map(ResourceFolderDTO::createFromDomain)
                .toList());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ResourceFolderDTO>> getAllFolders(@PathVariable final Long subjectId) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(this.folderService.getAllFolders(subject.get()).stream()
                .map(ResourceFolderDTO::createFlat)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@PathVariable final Long subjectId, @RequestBody final Map<String, Object> body) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return ResponseEntity.notFound().build();
        final String name = (String) body.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        ResourceFolder parent = null;
        if (body.get("parentId") != null) {
            parent = this.folderService.findById(((Number) body.get("parentId")).longValue()).orElse(null);
        }
        try {
            return ResponseEntity.ok(ResourceFolderDTO.createFlat(this.folderService.createFolder(name, subject.get(), parent)));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{folderId}/rename")
    public ResponseEntity<?> renameFolder(@PathVariable final Long subjectId, @PathVariable final Long folderId, @RequestBody final Map<String, String> body) {
        try {
            return ResponseEntity.ok(ResourceFolderDTO.createFlat(this.folderService.renameFolder(folderId, body.get("name"))));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{folderId}/move")
    public ResponseEntity<?> moveFolder(@PathVariable final Long subjectId, @PathVariable final Long folderId, @RequestBody final Map<String, Object> body) {
        try {
            final Long parentId = body.get("parentId") != null ? ((Number) body.get("parentId")).longValue() : null;
            return ResponseEntity.ok(ResourceFolderDTO.createFlat(this.folderService.moveFolder(folderId, parentId)));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable final Long subjectId, @PathVariable final Long folderId) {
        try {
            this.folderService.deleteFolder(folderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{folderId}/resources/{resourceId}")
    public ResponseEntity<?> moveResourceToFolder(@PathVariable final Long subjectId, @PathVariable final Long folderId, @PathVariable final Long resourceId) {
        try {
            this.folderService.moveResourceToFolder(resourceId, folderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<?> removeResourceFromFolder(@PathVariable final Long subjectId, @PathVariable final Long resourceId) {
        try {
            this.folderService.removeResourceFromFolder(resourceId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
