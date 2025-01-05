package es.hugoalvarezajenjo.selecta.repository;

import es.hugoalvarezajenjo.selecta.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
