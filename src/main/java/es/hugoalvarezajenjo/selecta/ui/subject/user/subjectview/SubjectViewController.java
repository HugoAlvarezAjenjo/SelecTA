package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.markdown.MarkdownService;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceVoteService;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/subject")
@RequiredArgsConstructor
@Slf4j
public class SubjectViewController {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
    private final ResourceVoteService resourceVoteService;
    private final MarkdownService markdownService;
    private final UserService userService;

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
                SubjectResourceDTO.createFromDomain(
                        this.subjectResourceService.getPublicResourcesFromSubject(id),
                        resourceVoteService::getUpvoteCount,
                        resourceVoteService::getDownvoteCount,
                        resourceVoteService::getUserVote));
        
        java.util.List<SubjectInfoDTO> relatedSubjects = this.subjectService.getRelatedSubjects(id, 3).stream()
                .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                .toList();
        model.addAttribute("relatedSubjects", relatedSubjects);

        boolean isFavourite = false;
        final User user = this.userService.getCurrentUser();
        log.info("SelecTA Log: SubjectView check - User retrieved: {}, isStudent: {}", 
            user != null ? user.getEmail() : "null", user instanceof Student);
        
        if (user instanceof Student student) {
            isFavourite = student.getFavouriteSubjects().stream()
                    .anyMatch(s -> s.getId().equals(id));
            log.info("SelecTA Log: Subject {} isFavourite for student: {}", id, isFavourite);
        }
        model.addAttribute("isFavourite", isFavourite);
        
        return "subject/user/subject-view";
    }

    @GetMapping("/{id}/favourite")
    public String toggleFavourite(@PathVariable final Long id) {
        this.userService.toggleFavouriteSubject(id);
        return "redirect:/subject/" + id;
    }
}
