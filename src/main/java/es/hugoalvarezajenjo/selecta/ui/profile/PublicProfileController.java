package es.hugoalvarezajenjo.selecta.ui.profile;

import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview.SubjectInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PublicProfileController {

    private final UserService userService;

    @GetMapping("/user/{id}")
    public String viewPublicProfile(@PathVariable final Long id, final Model model) {
        final Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return "error/404";
        }

        final User user = userOpt.get();
        model.addAttribute("user", user);

        List<SubjectInfoDTO> subjects = new ArrayList<>();
        if (user instanceof Teacher teacher) {
            subjects = teacher.getSubjects().stream()
                    .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                    .toList();
            model.addAttribute("isTeacher", true);
            model.addAttribute("subjectsTitle", "Asignaturas que imparte");
        } else if (user instanceof Student student) {
            model.addAttribute("isStudent", true);
            model.addAttribute("titulation", student.getTitulation());
            model.addAttribute("subjectsTitle", "Asignaturas favoritas");
        }

        model.addAttribute("subjects", subjects);
        return "public/user/profile";
    }
}
