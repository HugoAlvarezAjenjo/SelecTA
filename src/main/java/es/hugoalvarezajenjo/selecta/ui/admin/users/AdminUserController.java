package es.hugoalvarezajenjo.selecta.ui.admin.users;

import es.hugoalvarezajenjo.selecta.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(final Model model) {
        model.addAttribute("pendingUsers", AdminUserDTO.fromDomain(userService.getPendingUsers()));
        model.addAttribute("allUsers", AdminUserDTO.fromDomain(userService.getAllUsers()));
        return "admin/users/list";
    }

    @PostMapping("/{id}/approve")
    public String approveUser(@PathVariable final Long id) {
        userService.approveUser(id);
        return "redirect:/admin/users?approved";
    }

    @PostMapping("/{id}/reject")
    public String rejectUser(@PathVariable final Long id) {
        userService.rejectUser(id);
        return "redirect:/admin/users?rejected";
    }
}
