package es.hugoalvarezajenjo.selecta.ui.admin;

import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final SubjectService subjectService;

    @GetMapping
    public String dashboard(final Model model) {
        final long pendingCount = userService.getPendingUsers().size();
        final long totalUsers = userService.getAllUsers().size();
        final long totalSubjects = subjectService.getAllSubjects().size();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSubjects", totalSubjects);
        model.addAttribute("adminName", userService.getCurrentUser().getUsername());

        return "admin/dashboard";
    }
}
