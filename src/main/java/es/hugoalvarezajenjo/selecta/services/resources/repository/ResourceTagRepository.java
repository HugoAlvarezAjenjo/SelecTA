package es.hugoalvarezajenjo.selecta.services.resources.repository;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceTag;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceTagRepository extends JpaRepository<ResourceTag, Long> {

    Optional<ResourceTag> findBySubjectAndNameIgnoreCase(Subject subject, String name);

    List<ResourceTag> findBySubjectAndParentIsNullOrderByDisplayOrderAsc(Subject subject);

    List<ResourceTag> findBySubjectOrderByDisplayOrderAsc(Subject subject);

    @Query("SELECT t FROM ResourceTag t WHERE t.subject = :subject AND LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY t.name")
    List<ResourceTag> searchByName(@Param("subject") Subject subject, @Param("query") String query);

    List<ResourceTag> findByParentOrderByDisplayOrderAsc(ResourceTag parent);

    boolean existsBySubjectAndNameIgnoreCase(Subject subject, String name);
}
