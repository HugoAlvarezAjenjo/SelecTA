package es.hugoalvarezajenjo.selecta.service;

import es.hugoalvarezajenjo.selecta.entity.User;
import es.hugoalvarezajenjo.selecta.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(final User user) {
        //TODO: Encode Password
        return userRepository.save(user);
    }
}
