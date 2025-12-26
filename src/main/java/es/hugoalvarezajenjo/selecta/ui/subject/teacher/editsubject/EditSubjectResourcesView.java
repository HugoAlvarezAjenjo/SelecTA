package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/edit/subject/{subjectId}/resources")
@RequiredArgsConstructor
public class EditSubjectResourcesView {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;

    private static final String UPLOAD_DIR = "file-storage/";

    @GetMapping
    public String editSubjectResourcesView(@PathVariable final Long subjectId, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return "subject/user/no-subject";
        }

        model.addAttribute("subject", EditSubjectDescriptionDTO.createFromDomain(subject.get()));
        model.addAttribute("subjectResources",
                SubjectResourceDTO.createFromDomain(this.subjectResourceService.getResourcesFromSubject(subjectId)));
        model.addAttribute("resourceTypes", ResourceType.values());

        return "subject/teacher/edit-subject-resources";
    }

    @PostMapping("/upload")
    public String uploadResource(
            @PathVariable final Long subjectId,
            @RequestParam("file") final MultipartFile file,
            @RequestParam final String name,
            @RequestParam(required = false) final String description,
            @RequestParam final ResourceType type,
            @RequestParam(required = false) final String language,
            final RedirectAttributes redirectAttributes) {

        final Optional<Subject> subjectOpt = this.subjectService.getSubjectById(subjectId);
        if (subjectOpt.isEmpty()) {
            return "subject/user/no-subject";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, selecciona un archivo");
            return "redirect:/edit/subject/" + subjectId + "/resources";
        }

        try {
            // Create upload directory if it doesn't exist
            final Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            final String originalFilename = file.getOriginalFilename();
            final String filename = System.currentTimeMillis() + "_" + originalFilename;
            final Path filePath = uploadPath.resolve(filename);

            // Save file
            file.transferTo(filePath.toFile());

            // Create and save resource entity
            final SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setUrl("/" + UPLOAD_DIR + filename);
            resource.setCreationDate(LocalDate.now());

            this.subjectResourceService.saveResource(resource);

            redirectAttributes.addFlashAttribute("success", "Recurso subido correctamente");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
        }

        return "redirect:/edit/subject/" + subjectId + "/resources";
    }

    @PostMapping("/{resourceId}/delete")
    public String deleteResource(
            @PathVariable final Long subjectId,
            @PathVariable final Long resourceId,
            final RedirectAttributes redirectAttributes) {

        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null) {
            redirectAttributes.addFlashAttribute("error", "Recurso no encontrado");
            return "redirect:/edit/subject/" + subjectId + "/resources";
        }

        // Delete file from storage
        if (resource.getUrl() != null && !resource.getUrl().isEmpty()) {
            try {
                final Path filePath = Paths.get(resource.getUrl().substring(1)); // Remove leading "/"
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but continue with database deletion
                System.err.println("Error deleting file: " + e.getMessage());
            }
        }

        // Delete from database
        this.subjectResourceService.deleteResource(resourceId);

        redirectAttributes.addFlashAttribute("success", "Recurso eliminado correctamente");
        return "redirect:/edit/subject/" + subjectId + "/resources";
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable final Long resourceId) {
        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null || resource.getUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            final Path filePath = Paths.get(resource.getUrl().substring(1)); // Remove leading "/"
            final File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            final Resource fileResource = new FileSystemResource(file);
            final String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getName() + "\"")
                    .body(fileResource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
