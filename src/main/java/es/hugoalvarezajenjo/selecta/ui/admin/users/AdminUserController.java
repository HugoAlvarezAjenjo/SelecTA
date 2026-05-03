package es.hugoalvarezajenjo.selecta.ui.admin.users;

import es.hugoalvarezajenjo.selecta.services.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @GetMapping("/new")
    public String showCreateUserForm(final Model model) {
        model.addAttribute("createUser", new CreateUserDTO());
        return "admin/users/form";
    }

    @PostMapping("/new")
    public String createUser(@ModelAttribute("createUser") final CreateUserDTO dto, final Model model) {
        // Only TEACHER and ADMIN can be created by admin
        if (dto.getRole() != UserRole.TEACHER && dto.getRole() != UserRole.ADMIN) {
            model.addAttribute("createUser", dto);
            model.addAttribute("error", "Solo se pueden crear cuentas de Profesor o Administrador.");
            return "admin/users/form";
        }

        // Check for duplicate email
        if (userService.existsByEmail(dto.getEmail())) {
            model.addAttribute("createUser", dto);
            model.addAttribute("error", "Ya existe un usuario con el email: " + dto.getEmail());
            return "admin/users/form";
        }

        User user;
        if (dto.getRole() == UserRole.ADMIN) {
            user = new Admin();
        } else {
            user = new Teacher();
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setApproved(true); // Admin-created users are approved immediately

        userService.registerUser(user);

        return "redirect:/admin/users?created";
    }
}
