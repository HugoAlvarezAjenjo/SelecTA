package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.resources.*;
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
@RequestMapping("/teacher/subject/{subjectId}/resources")
@RequiredArgsConstructor
public class EditSubjectResourcesView {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final ResourceTagService resourceTagService;
    private final ResourceFolderService resourceFolderService;
    private final StorageService storageService;

    @GetMapping
    public String editSubjectResourcesView(@PathVariable final Long subjectId, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) return "subject/user/no-subject";

        model.addAttribute("subjectDTO", EditSubjectDescriptionDTO.createFromDomain(subject.get()));
        model.addAttribute("subjectResources",
                SubjectResourceDTO.createFromDomain(this.subjectResourceService.getResourcesFromSubject(subjectId)));
        model.addAttribute("resourceTypes", ResourceType.values());

        // Tags (flat list)
        model.addAttribute("allTags", ResourceTagDTO.createListFromDomain(
                this.resourceTagService.getAllTags(subject.get())));

        // Folders (tree)
        model.addAttribute("folderTree", this.resourceFolderService.getFolderTree(subject.get()).stream()
                .map(ResourceFolderDTO::createFromDomain).toList());
        model.addAttribute("allFolders", this.resourceFolderService.getAllFolders(subject.get()).stream()
                .map(ResourceFolderDTO::createFlat).toList());

        return "subject/teacher/edit-subject-resources";
    }

    @PostMapping("/upload")
    public String uploadResource(
            @PathVariable final Long subjectId,
            @RequestParam(value = "file", required = false) final MultipartFile file,
            @RequestParam final String name,
            @RequestParam(required = false) final String description,
            @RequestParam final ResourceType type,
            @RequestParam(required = false) final String language,
            @RequestParam(required = false) final String url,
            @RequestParam(defaultValue = "false") final boolean isPrivate,
            @RequestParam(required = false) final List<Long> tagIds,
            @RequestParam(required = false) final Long folderId,
            final RedirectAttributes redirectAttributes) {

        final Optional<Subject> subjectOpt = this.subjectService.getSubjectById(subjectId);
        if (subjectOpt.isEmpty()) return "subject/user/no-subject";

        final boolean isExternalLink = type == ResourceType.EXTERNAL_RESOURCE;

        // Validate: external links need URL, other types need file
        if (isExternalLink && (url == null || url.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "Por favor, introduce una URL válida");
            return "redirect:/teacher/subject/" + subjectId + "/resources";
        }
        if (!isExternalLink && (file == null || file.isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "Por favor, selecciona un archivo");
            return "redirect:/teacher/subject/" + subjectId + "/resources";
        }

        try {
            SubjectResource resource = new SubjectResource();
            resource.setSubjectId(subjectId);
            resource.setName(name);
            resource.setDescription(description != null ? description : "");
            resource.setType(type);
            resource.setLanguage(language != null ? language : "");
            resource.setCreationDate(LocalDate.now());
            resource.setPrivate(isPrivate);

            if (isExternalLink) {
                resource.setUrl(url);
                resource.setOriginalName(url);
            } else {
                resource.setOriginalName(file.getOriginalFilename());
            }

            // Set folder if provided
            if (folderId != null) {
                this.resourceFolderService.findById(folderId).ifPresent(resource::setFolder);
            }

            resource = this.subjectResourceService.saveResource(resource);

            // Assign tags
            if (tagIds != null) {
                for (final Long tagId : tagIds) {
                    this.resourceTagService.tagResource(resource.getId(), tagId);
                }
            }

            // Only upload to storage for file-based resources
            if (!isExternalLink) {
                final String filename = resource.getId().toString();
                this.storageService.uploadFile(filename, file.getInputStream(), file.getSize(), file.getContentType());
            }

            redirectAttributes.addFlashAttribute("success",
                    isExternalLink ? "Enlace externo añadido correctamente" : "Recurso subido correctamente");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
        }
        return "redirect:/teacher/subject/" + subjectId + "/resources";
    }

    @PostMapping("/{resourceId}/delete")
    public String deleteResource(@PathVariable final Long subjectId, @PathVariable final Long resourceId,
                                  final RedirectAttributes redirectAttributes) {
        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null) {
            redirectAttributes.addFlashAttribute("error", "Recurso no encontrado");
            return "redirect:/teacher/subject/" + subjectId + "/resources";
        }
        try { this.storageService.deleteFile(resourceId.toString()); }
        catch (Exception e) { System.err.println("Error deleting file: " + e.getMessage()); }
        this.subjectResourceService.deleteResource(resourceId);
        redirectAttributes.addFlashAttribute("success", "Recurso eliminado correctamente");
        return "redirect:/teacher/subject/" + subjectId + "/resources";
    }

    @PostMapping("/{resourceId}/toggle-privacy")
    public String toggleResourcePrivacy(@PathVariable final Long subjectId, @PathVariable final Long resourceId,
                                         final RedirectAttributes redirectAttributes) {
        final SubjectResource resource = this.subjectResourceService.findById(resourceId);
        if (resource == null) {
            redirectAttributes.addFlashAttribute("error", "Recurso no encontrado");
            return "redirect:/teacher/subject/" + subjectId + "/resources";
        }
        this.subjectResourceService.togglePrivacy(resourceId);
        final String status = !resource.isPrivate() ? "privado" : "público";
        redirectAttributes.addFlashAttribute("success", "El recurso ahora es " + status);
        return "redirect:/teacher/subject/" + subjectId + "/resources";
    }
}
