package es.hugoalvarezajenjo.selecta.ui.auth;

import es.hugoalvarezajenjo.selecta.services.user.Admin;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(final Model model) {
        model.addAttribute("userRegistration", new UserRegistrationDto());
        return "authentication/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegistration") final UserRegistrationDto registrationDto) {
        User user;
        switch (registrationDto.getRole()) {
            case STUDENT:
                Student student = new Student();
                student.setTitulation(registrationDto.getTitulation());
                user = student;
                break;
            case TEACHER:
                user = new Teacher();
                break;
            case ADMIN:
                user = new Admin();
                break;
            default:
                throw new IllegalArgumentException("Invalid user role");
        }

        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());
        user.setUsername(registrationDto.getName()); // Map name to username
        user.setRole(registrationDto.getRole());

        this.userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(final Model model) {
        return "authentication/login";
    }
}
