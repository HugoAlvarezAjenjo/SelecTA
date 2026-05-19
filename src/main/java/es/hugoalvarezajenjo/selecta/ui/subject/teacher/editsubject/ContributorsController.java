package es.hugoalvarezajenjo.selecta.ui.subject.teacher.editsubject;

import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequest;
import es.hugoalvarezajenjo.selecta.services.contributions.ContributionRequestService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher/subject/{subjectId}/contributors")
@RequiredArgsConstructor
public class ContributorsController {

    private final SubjectService subjectService;
    private final UserRepository userRepository;
    private final ContributionRequestService requestService;

    @GetMapping
    public String contributorsView(@PathVariable final Long subjectId, final Model model) {
        final Optional<Subject> subjectOpt = this.subjectService.getSubjectById(subjectId);
        if (subjectOpt.isEmpty()) return "subject/user/no-subject";

        final Subject subject = subjectOpt.get();
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("subjectName", subject.getName());
        model.addAttribute("contributors", subject.getContributors());

        // Get all students for the add dropdown
        final List<User> allStudents = this.userRepository.findAll().stream()
                .filter(u -> u instanceof Student)
                .filter(u -> !subject.getContributors().contains(u))
                .toList();
        model.addAttribute("availableStudents", allStudents);

        // Pending requests
        final List<ContributionRequest> pendingRequests = this.requestService.getPendingRequests(subjectId);
        model.addAttribute("pendingRequests", pendingRequests);

        return "subject/teacher/contributors";
    }

    @PostMapping("/add")
    public String addContributor(@PathVariable final Long subjectId,
                                  @RequestParam final Long studentId,
                                  final RedirectAttributes redirectAttributes) {
        try {
            this.subjectService.addContributor(subjectId, studentId);
            redirectAttributes.addFlashAttribute("success", "Alumno autorizado como contribuidor");
        } catch (final IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subject/" + subjectId + "/contributors";
    }

    @PostMapping("/{studentId}/remove")
    public String removeContributor(@PathVariable final Long subjectId,
                                     @PathVariable final Long studentId,
                                     final RedirectAttributes redirectAttributes) {
        try {
            this.subjectService.removeContributor(subjectId, studentId);
            redirectAttributes.addFlashAttribute("success", "Alumno desautorizado como contribuidor");
        } catch (final IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subject/" + subjectId + "/contributors";
    }

    @PostMapping("/requests/{requestId}/approve")
    public String approveRequest(@PathVariable final Long subjectId,
                                  @PathVariable final Long requestId,
                                  final RedirectAttributes redirectAttributes) {
        try {
            this.requestService.approveRequest(requestId);
            redirectAttributes.addFlashAttribute("success", "Solicitud aprobada");
        } catch (final Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subject/" + subjectId + "/contributors";
    }

    @PostMapping("/requests/{requestId}/reject")
    public String rejectRequest(@PathVariable final Long subjectId,
                                 @PathVariable final Long requestId,
                                 final RedirectAttributes redirectAttributes) {
        try {
            this.requestService.rejectRequest(requestId);
            redirectAttributes.addFlashAttribute("success", "Solicitud rechazada");
        } catch (final Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subject/" + subjectId + "/contributors";
    }
}
