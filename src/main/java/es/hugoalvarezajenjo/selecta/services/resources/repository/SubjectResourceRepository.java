package es.hugoalvarezajenjo.selecta.services.resources.repository;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectResourceRepository extends JpaRepository<SubjectResource, Long> {
    List<SubjectResource> findSubjectResourceBySubjectId(Long subjectId);
}
