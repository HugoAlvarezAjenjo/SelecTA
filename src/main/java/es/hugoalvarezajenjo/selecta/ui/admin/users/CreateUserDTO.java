package es.hugoalvarezajenjo.selecta.ui.admin.users;

import es.hugoalvarezajenjo.selecta.services.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDTO {
    private String username;
    private String email;
    private String password;
    private UserRole role;
}
