package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ResourcePreviewController {

    private final SubjectResourceService subjectResourceService;
    private final SubjectService subjectService;

    private static final Set<String> PDF_EXTENSIONS = Set.of("pdf");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "ogg");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "svg", "webp");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "flac");

    @GetMapping("/subject/{subjectId}/resources/{resourceId}/preview")
    public String previewResource(@PathVariable final Long subjectId,
                                  @PathVariable final Long resourceId,
                                  final Model model) {
        final SubjectResource resource = subjectResourceService.findById(resourceId);
        if (resource == null || !resource.getSubjectId().equals(subjectId)) {
            return "error/404";
        }

        // For external resources, redirect to the URL directly
        if (resource.getType() == ResourceType.EXTERNAL_RESOURCE && resource.getUrl() != null) {
            return "redirect:" + resource.getUrl();
        }

        final Optional<Subject> subject = subjectService.getSubjectById(subjectId);
        if (subject.isEmpty()) {
            return "error/404";
        }

        final String previewType = resolvePreviewType(resource.getOriginalName());
        final String viewUrl = "/api/resources/" + resourceId + "/view";
        final String downloadUrl = "/api/resources/" + resourceId + "/download";

        model.addAttribute("resource", resource);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("subjectName", subject.get().getName());
        model.addAttribute("previewType", previewType);
        model.addAttribute("viewUrl", viewUrl);
        model.addAttribute("downloadUrl", downloadUrl);

        return "subject/user/resource-preview";
    }

    private String resolvePreviewType(final String originalName) {
        if (originalName == null) {
            return "OTHER";
        }
        final String extension = getFileExtension(originalName).toLowerCase();
        if (PDF_EXTENSIONS.contains(extension)) {
            return "PDF";
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return "VIDEO";
        }
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return "IMAGE";
        }
        if (AUDIO_EXTENSIONS.contains(extension)) {
            return "AUDIO";
        }
        return "OTHER";
    }

    private String getFileExtension(final String filename) {
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Determines whether a resource can be previewed in the browser.
     */
    public static boolean isPreviewable(final String originalName, final ResourceType type) {
        if (type == ResourceType.EXTERNAL_RESOURCE) {
            return false;
        }
        if (originalName == null) {
            return false;
        }
        final String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        return PDF_EXTENSIONS.contains(ext)
                || VIDEO_EXTENSIONS.contains(ext)
                || IMAGE_EXTENSIONS.contains(ext)
                || AUDIO_EXTENSIONS.contains(ext);
    }
}
