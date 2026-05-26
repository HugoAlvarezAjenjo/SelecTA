package es.hugoalvarezajenjo.selecta.ui.auth;

import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserRole;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
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
        // Only STUDENT and TEACHER can self-register
        if (registrationDto.getRole() != UserRole.STUDENT && registrationDto.getRole() != UserRole.TEACHER) {
            return "redirect:/register?error=role";
        }

        // Check if email already exists
        if (this.userService.existsByEmail(registrationDto.getEmail())) {
            return "redirect:/register?error=email";
        }

        User user;
        switch (registrationDto.getRole()) {
            case STUDENT:
                Student student = new Student();
                student.setTitulation(registrationDto.getTitulation());
                student.setApproved(true); // Students are approved immediately
                user = student;
                break;
            case TEACHER:
                Teacher teacher = new Teacher();
                teacher.setApproved(false); // Teachers need admin approval
                user = teacher;
                break;
            default:
                throw new IllegalArgumentException("Invalid user role");
        }

        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());
        user.setUsername(registrationDto.getName());
        user.setRole(registrationDto.getRole());

        this.userService.registerUser(user);
        log.info("New user registered: email={}, role={}, approved={}", user.getEmail(), user.getRole(), user.isApproved());

        // Teachers get redirected to a pending approval page
        if (registrationDto.getRole() == UserRole.TEACHER) {
            return "authentication/pending-approval";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String showLoginForm(final Model model) {
        return "authentication/login";
    }
}
