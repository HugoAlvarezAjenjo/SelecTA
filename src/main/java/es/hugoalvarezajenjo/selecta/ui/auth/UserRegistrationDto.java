package es.hugoalvarezajenjo.selecta.ui.auth;

import es.hugoalvarezajenjo.selecta.services.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    private String email;
    private String password;
    private String name;
    private UserRole role;
    private String titulation;
}
