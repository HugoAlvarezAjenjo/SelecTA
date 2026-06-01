package es.hugoalvarezajenjo.selecta.ui.home;

import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview.SubjectInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final UserService userService;

    @GetMapping
    public String home(final Model model) {
        final User currentUser = userService.getCurrentUser();
        log.debug("Home check - User retrieved: {}, isStudent: {}", 
            currentUser != null ? currentUser.getEmail() : "null", currentUser instanceof Student);

        if (currentUser instanceof Student student) {
            List<SubjectInfoDTO> favourites = student.getFavouriteSubjects().stream()
                    .map(s -> SubjectInfoDTO.createFromDomain(s, ""))
                    .toList();
            log.debug("Student {} has {} favourites", student.getEmail(), favourites.size());
            model.addAttribute("favouriteSubjects", favourites);
        }
        return "index";
    }
}
