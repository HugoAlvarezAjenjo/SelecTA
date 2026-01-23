package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/edit/subject/{subjectId}/resources")
@RequiredArgsConstructor
public class EditSubjectResourcesView {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final StorageService storageService;

    @GetMapping
    public String editSubjectResourcesView(@PathVariable final Long subjectId, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return "subject/user/no-subject";
        }

        model.addAttribute("subjectDTO", EditSubjectDescriptionDTO.createFromDomain(subject.get()));
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
            // Create and save resource entity first to generate ID
            SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setOriginalName(file.getOriginalFilename());
            resource.setCreationDate(LocalDate.now());

            resource = this.subjectResourceService.saveResource(resource);

            // Use the generated ID as the filename
            final String filename = resource.getId().toString();

            // Save file using StorageService
            this.storageService.uploadFile(filename, file.getInputStream(), file.getSize(), file.getContentType());

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

        // Delete file from storage using the resourceId as filename
        try {
            this.storageService.deleteFile(resourceId.toString());
        } catch (Exception e) {
            // Log error but continue with database deletion
            System.err.println("Error deleting file: " + e.getMessage());
        }

        // Delete from database
        this.subjectResourceService.deleteResource(resourceId);

        redirectAttributes.addFlashAttribute("success", "Recurso eliminado correctamente");
        return "redirect:/edit/subject/" + subjectId + "/resources";
    }

    // Download handling has been moved to ResourceDownloadController for better
    // security and separation of concerns
}
