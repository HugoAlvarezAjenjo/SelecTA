package es.hugoalvarezajenjo.selecta.services.user;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> getUserById(Long id);

    void toggleFavouriteSubject(Long subjectId);

    User getCurrentUser();

    List<User> getPendingUsers();

    List<User> getAllUsers();

    void approveUser(Long userId);

    void rejectUser(Long userId);
}
