package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/subject/{subjectId}/contribute")
@RequiredArgsConstructor
public class StudentUploadController {

    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final StorageService storageService;
    private final UserService userService;

    @PostMapping
    public String uploadContribution(
            @PathVariable final Long subjectId,
            @RequestParam("file") final MultipartFile file,
            @RequestParam final String name,
            @RequestParam(required = false) final String description,
            @RequestParam final ResourceType type,
            @RequestParam(required = false) final String language,
            final RedirectAttributes redirectAttributes) {

        final User currentUser = this.userService.getCurrentUser();

        // Check if user is a contributor for this subject
        if (currentUser == null || !this.subjectService.isContributor(subjectId, currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para subir recursos a esta asignatura");
            return "redirect:/subject/" + subjectId;
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, selecciona un archivo");
            return "redirect:/subject/" + subjectId;
        }

        try {
            SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setOriginalName(file.getOriginalFilename());
            resource.setCreationDate(LocalDate.now());
            resource.setPrivate(false);      // Student contributions are always public
            resource.setOfficial(false);     // Marked as unofficial
            resource.setUploadedBy(currentUser);

            resource = this.subjectResourceService.saveResource(resource);

            final String filename = resource.getId().toString();
            this.storageService.uploadFile(filename, file.getInputStream(), file.getSize(), file.getContentType());

            redirectAttributes.addFlashAttribute("success", "¡Recurso subido correctamente! Se mostrará como contribución no oficial.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
        }

        return "redirect:/subject/" + subjectId;
    }

    /**
     * Students can delete their own uploaded resources.
     */
    @PostMapping("/{resourceId}/delete")
    public String deleteOwnResource(
            @PathVariable final Long subjectId,
            @PathVariable final Long resourceId,
            final RedirectAttributes redirectAttributes) {

        final User currentUser = this.userService.getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/subject/" + subjectId;
        }

        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null) {
            redirectAttributes.addFlashAttribute("error", "Recurso no encontrado");
            return "redirect:/profile";
        }

        // Only allow deletion of own resources
        if (resource.getUploadedBy() == null || !resource.getUploadedBy().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes eliminar tus propios recursos");
            return "redirect:/profile";
        }

        try { this.storageService.deleteFile(resourceId.toString()); }
        catch (Exception e) { /* file may not exist */ }
        this.subjectResourceService.deleteResource(resourceId);

        redirectAttributes.addFlashAttribute("success", "Recurso eliminado correctamente");
        return "redirect:/profile";
    }
}
