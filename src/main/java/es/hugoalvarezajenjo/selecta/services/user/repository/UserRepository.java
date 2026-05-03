package es.hugoalvarezajenjo.selecta.services.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByApprovedFalse();

    List<User> findByRole(UserRole role);

    List<User> findAllByOrderByIdAsc();

    boolean existsByEmail(String email);
}
