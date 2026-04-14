package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceTag;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceTagService;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/edit/subject/{subjectId}/resources")
@RequiredArgsConstructor
public class EditSubjectResourcesView {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final ResourceTagService resourceTagService;
    private final SubjectResourceRepository subjectResourceRepository;
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

        // Tag tree for the sidebar
        final List<ResourceTagDTO> tagTree = this.resourceTagService.getTagTree(subject.get()).stream()
                .map(ResourceTagDTO::createFromDomain)
                .toList();
        model.addAttribute("tagTree", tagTree);

        // All tags flat for the chip input autocomplete
        final List<ResourceTagDTO> allTags = this.resourceTagService.getAllTags(subject.get()).stream()
                .map(ResourceTagDTO::createFlat)
                .toList();
        model.addAttribute("allTags", allTags);

        // Uncategorized resources
        final List<SubjectResourceDTO> uncategorized = SubjectResourceDTO.createFromDomain(
                this.subjectResourceRepository.findUncategorizedBySubjectId(subjectId));
        model.addAttribute("uncategorizedResources", uncategorized);

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
            @RequestParam(defaultValue = "false") final boolean isPrivate,
            @RequestParam(required = false) final List<Long> tagIds,
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
            SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setOriginalName(file.getOriginalFilename());
            resource.setCreationDate(LocalDate.now());
            resource.setPrivate(isPrivate);

            resource = this.subjectResourceService.saveResource(resource);

            // Assign tags if provided
            if (tagIds != null && !tagIds.isEmpty()) {
                for (final Long tagId : tagIds) {
                    this.resourceTagService.tagResource(resource.getId(), tagId);
                }
            }

            // Use the generated ID as the filename
            final String filename = resource.getId().toString();
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

        try {
            this.storageService.deleteFile(resourceId.toString());
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }

        this.subjectResourceService.deleteResource(resourceId);

        redirectAttributes.addFlashAttribute("success", "Recurso eliminado correctamente");
        return "redirect:/edit/subject/" + subjectId + "/resources";
    }

    @PostMapping("/{resourceId}/toggle-privacy")
    public String toggleResourcePrivacy(
            @PathVariable final Long subjectId,
            @PathVariable final Long resourceId,
            final RedirectAttributes redirectAttributes) {

        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null) {
            redirectAttributes.addFlashAttribute("error", "Recurso no encontrado");
            return "redirect:/edit/subject/" + subjectId + "/resources";
        }

        this.subjectResourceService.togglePrivacy(resourceId);

        final String status = !resource.isPrivate() ? "privado" : "público";
        redirectAttributes.addFlashAttribute("success", "El recurso ahora es " + status);
        return "redirect:/edit/subject/" + subjectId + "/resources";
    }
}
