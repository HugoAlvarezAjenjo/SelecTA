package es.hugoalvarezajenjo.selecta.ui.admin.users;

import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.Value;

import java.util.List;

@Value
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean approved;

    public static AdminUserDTO fromDomain(final User user) {
        return new AdminUserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isApproved()
        );
    }

    public static List<AdminUserDTO> fromDomain(final List<User> users) {
        return users.stream()
                .map(AdminUserDTO::fromDomain)
                .toList();
    }
}
