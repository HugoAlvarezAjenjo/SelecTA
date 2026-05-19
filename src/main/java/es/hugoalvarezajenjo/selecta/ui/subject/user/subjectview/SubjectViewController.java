package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.markdown.MarkdownService;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceVoteService;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequestService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectRating;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRatingRepository;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private final SubjectRatingRepository ratingRepository;
    private final ContributionRequestService contributionRequestService;

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

        // Contributor check — can the current user upload resources?
        boolean isContributor = false;
        if (user != null) {
            isContributor = this.subjectService.isContributor(id, user.getId());
        }
        model.addAttribute("isContributor", isContributor);
        model.addAttribute("resourceTypes", ResourceType.values());

        // Pending contribution request check
        boolean hasPendingRequest = false;
        if (user != null && !isContributor) {
            hasPendingRequest = this.contributionRequestService.hasPendingAccessRequest(id, user.getId());
        }
        model.addAttribute("hasPendingRequest", hasPendingRequest);

        // Rating data (server-side)
        final Double avgRating = this.ratingRepository.getAverageRating(id);
        final long ratingCount = this.ratingRepository.countBySubjectId(id);
        int userRating = 0;
        if (user != null) {
            userRating = this.ratingRepository.findBySubjectIdAndUserId(id, user.getId())
                    .map(r -> r.getRating()).orElse(0);
        }
        model.addAttribute("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        model.addAttribute("ratingCount", ratingCount);
        model.addAttribute("userRating", userRating);
        
        return "subject/user/subject-view";
    }

    @PostMapping("/{id}/rate")
    public String rateSubject(@PathVariable final Long id, @RequestParam final int rating) {
        final User user = this.userService.getCurrentUser();
        if (user != null && rating >= 1 && rating <= 5) {
            final Subject subject = this.subjectService.getSubjectById(id).orElse(null);
            if (subject != null) {
                final SubjectRating entity = this.ratingRepository.findBySubjectIdAndUserId(id, user.getId())
                        .orElseGet(() -> {
                            final SubjectRating r = new SubjectRating();
                            r.setSubject(subject);
                            r.setUser(user);
                            return r;
                        });
                entity.setRating(rating);
                this.ratingRepository.save(entity);
            }
        }
        return "redirect:/subject/" + id;
    }

    @GetMapping("/{id}/favourite")
    public String toggleFavourite(@PathVariable final Long id) {
        this.userService.toggleFavouriteSubject(id);
        return "redirect:/subject/" + id;
    }
}
