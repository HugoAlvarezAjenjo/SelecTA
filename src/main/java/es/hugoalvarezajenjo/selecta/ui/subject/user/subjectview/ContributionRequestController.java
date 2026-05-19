package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequestService;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
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
@RequestMapping("/subject/{subjectId}")
@RequiredArgsConstructor
public class ContributionRequestController {

    private final ContributionRequestService requestService;
    private final SubjectResourceService resourceService;
    private final StorageService storageService;
    private final UserService userService;

    /**
     * Student requests to become a contributor (access request).
     */
    @PostMapping("/request-access")
    public String requestAccess(@PathVariable final Long subjectId,
                                 @RequestParam(required = false) final String message,
                                 final RedirectAttributes redirectAttributes) {
        final User user = this.userService.getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/subject/" + subjectId;
        }
        try {
            this.requestService.requestAccess(subjectId, user, message);
            redirectAttributes.addFlashAttribute("success", "Solicitud de acceso enviada. El profesor la revisará.");
        } catch (final Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/subject/" + subjectId;
    }

    /**
     * Student proposes a specific resource for approval.
     */
    @PostMapping("/propose-resource")
    public String proposeResource(@PathVariable final Long subjectId,
                                   @RequestParam("file") final MultipartFile file,
                                   @RequestParam final String name,
                                   @RequestParam(required = false) final String description,
                                   @RequestParam final ResourceType type,
                                   @RequestParam(required = false) final String language,
                                   @RequestParam(required = false) final String message,
                                   final RedirectAttributes redirectAttributes) {
        final User user = this.userService.getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/subject/" + subjectId;
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un archivo");
            return "redirect:/subject/" + subjectId;
        }

        try {
            // Create resource as private (pending approval)
            SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setOriginalName(file.getOriginalFilename());
            resource.setCreationDate(LocalDate.now());
            resource.setPrivate(true);       // Hidden until approved
            resource.setOfficial(false);
            resource.setUploadedBy(user);

            resource = this.resourceService.saveResource(resource);

            // Save file
            this.storageService.uploadFile(resource.getId().toString(), file.getInputStream(), file.getSize(), file.getContentType());

            // Create the request
            this.requestService.proposeResource(subjectId, user, resource, message);

            redirectAttributes.addFlashAttribute("success", "Recurso propuesto. El profesor lo revisará antes de publicarlo.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
        }

        return "redirect:/subject/" + subjectId;
    }
}
