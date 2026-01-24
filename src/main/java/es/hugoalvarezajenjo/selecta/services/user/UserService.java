package es.hugoalvarezajenjo.selecta.services.user;

import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> getUserById(Long id);
}
