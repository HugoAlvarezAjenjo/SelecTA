package es.hugoalvarezajenjo.selecta.repository;

import es.hugoalvarezajenjo.selecta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
