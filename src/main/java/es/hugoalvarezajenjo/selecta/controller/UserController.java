package es.hugoalvarezajenjo.selecta.controller;

import es.hugoalvarezajenjo.selecta.entity.User;
import es.hugoalvarezajenjo.selecta.mapper.UserMapper;
import es.hugoalvarezajenjo.selecta.service.UserAuthentication;
import es.hugoalvarezajenjo.selecta.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserAuthentication userAuthentication;

    public UserController(final UserService userService, final UserMapper userMapper, final UserAuthentication userAuthentication) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userAuthentication = userAuthentication;
    }

    @GetMapping
    public String profile(final Model model) {
        model.addAttribute("user", this.userMapper.userToUserProfileDto(this.userAuthentication.getCurrentUser()));
        return "public/user/profile";
    }

    @GetMapping("/{id}")
    public String viewUserProfile(final Model model, @PathVariable final Long id) {
        final Optional<User> user = this.userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "public/user/profile";
        }
        // TODO: Manejo de errores si el usuario no se encuentra
        model.addAttribute("error", "User not found");
        return "public/user/profile";
    }
}
