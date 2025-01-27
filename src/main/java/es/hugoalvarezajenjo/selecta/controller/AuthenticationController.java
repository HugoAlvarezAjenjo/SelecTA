package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.entity.User;
import es.hugoalvarezajenjo.selecta.service.UserService;
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
        model.addAttribute("user", new User());
        return "authentication/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") final User user) {
        this.userService.registerUser(user);
        return "authentication/login";
    }

    @GetMapping("/login")
    public String showLoginForm(final Model model) {
        return "authentication/login";
    }
}
