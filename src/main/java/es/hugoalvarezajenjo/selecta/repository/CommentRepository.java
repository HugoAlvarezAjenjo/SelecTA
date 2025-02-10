package es.hugoalvarezajenjo.selecta.repository;

import es.hugoalvarezajenjo.selecta.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
