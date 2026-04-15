package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;

import java.util.List;
import java.util.Optional;

public interface ResourceTagService {

    ResourceTag getOrCreateTag(String rawName, Subject subject);

    List<ResourceTag> getAllTags(Subject subject);

    List<ResourceTag> searchTags(Subject subject, String query);

    Optional<ResourceTag> findById(Long tagId);

    ResourceTag renameTag(Long tagId, String newName);

    void deleteTag(Long tagId);

    void tagResource(Long resourceId, Long tagId);

    void untagResource(Long resourceId, Long tagId);

    List<SubjectResource> getResourcesByTag(Long tagId);

    List<SubjectResource> searchResources(Long subjectId, String query);
}
