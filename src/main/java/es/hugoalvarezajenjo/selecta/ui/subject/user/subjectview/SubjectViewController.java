package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.markdown.MarkdownService;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectViewController {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final MarkdownService markdownService;

    @GetMapping("/{id}")
    public String subjectView(@PathVariable final Long id, final Model model) {
        final Optional<Subject> subject = this.subjectService.getSubjectById(id);
        if (subject.isEmpty()) {
            return "subject/user/no-subject";
        }

        // Convert markdown to HTML
        String longDescriptionHtml = markdownService.toHtml(subject.get().getLongDescription());

        model.addAttribute("subject", SubjectInfoDTO.createFromDomain(subject.get(), longDescriptionHtml));
        model.addAttribute("resources",
                SubjectResourceDTO.createFromDomain(this.subjectResourceService.getPublicResourcesFromSubject(id)));
        return "subject/user/subject-view";
    }
}
