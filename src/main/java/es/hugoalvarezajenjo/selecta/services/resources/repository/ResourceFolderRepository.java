package es.hugoalvarezajenjo.selecta.services.resources.repository;

import es.hugoalvarezajenjo.selecta.services.resources.ResourceFolder;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceFolderRepository extends JpaRepository<ResourceFolder, Long> {

    List<ResourceFolder> findBySubjectAndParentIsNullOrderByDisplayOrderAsc(Subject subject);

    List<ResourceFolder> findBySubjectOrderByDisplayOrderAsc(Subject subject);

    Optional<ResourceFolder> findBySubjectAndNameIgnoreCaseAndParent(Subject subject, String name, ResourceFolder parent);

    List<ResourceFolder> findByParentOrderByDisplayOrderAsc(ResourceFolder parent);

    boolean existsBySubjectAndNameIgnoreCaseAndParent(Subject subject, String name, ResourceFolder parent);
}
