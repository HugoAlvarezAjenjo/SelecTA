package es.hugoalvarezajenjo.selecta.ui.profile;

import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview.SubjectInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final UserService userService;

    @GetMapping
    public String showProfile(final Model model) {
        final User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            log.warn("SelecTA Log: Profile access attempted without authentication");
            return "redirect:/login";
        }

        log.info("SelecTA Log: Loading profile for user: {}, role: {}", currentUser.getEmail(), currentUser.getRole());
        
        model.addAttribute("user", currentUser);
        
        List<SubjectInfoDTO> subjects = new ArrayList<>();
        if (currentUser instanceof Student student) {
            subjects = student.getFavouriteSubjects().stream()
                    .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                    .toList();
            model.addAttribute("isStudent", true);
            model.addAttribute("titulation", student.getTitulation());
            model.addAttribute("subjectsTitle", "Mis Asignaturas Favoritas");
        } else if (currentUser instanceof Teacher teacher) {
            subjects = teacher.getSubjects().stream()
                    .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                    .toList();
            model.addAttribute("isTeacher", true);
            model.addAttribute("subjectsTitle", "Asignaturas que imparto");
        }

        model.addAttribute("subjects", subjects);
        return "profile";
    }
}
