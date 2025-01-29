package es.hugoalvarezajenjo.selecta.service;

import es.hugoalvarezajenjo.selecta.entity.User;

import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> getUserById(Long id);
}
