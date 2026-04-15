package es.hugoalvarezajenjo.selecta.ui.subject.teacher;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherDashboardController {

    private final UserService userService;

    @GetMapping
    public String dashboard(final Model model) {
        final User currentUser = this.userService.getCurrentUser();

        Set<Subject> subjects = Set.of();
        String teacherName = "Profesor";

        if (currentUser instanceof Teacher teacher) {
            subjects = teacher.getSubjects();
            teacherName = teacher.getUsername();
        }

        model.addAttribute("teacherName", teacherName);
        model.addAttribute("subjects", subjects);

        return "subject/teacher/dashboard";
    }
}
