package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;

import java.util.List;
import java.util.Optional;

public interface ResourceFolderService {

    ResourceFolder createFolder(String name, Subject subject, ResourceFolder parent);

    List<ResourceFolder> getFolderTree(Subject subject);

    List<ResourceFolder> getAllFolders(Subject subject);

    Optional<ResourceFolder> findById(Long folderId);

    ResourceFolder renameFolder(Long folderId, String newName);

    ResourceFolder moveFolder(Long folderId, Long newParentId);

    void deleteFolder(Long folderId);

    void moveResourceToFolder(Long resourceId, Long folderId);

    void removeResourceFromFolder(Long resourceId);

    List<SubjectResource> getResourcesInFolder(Long folderId);

    List<SubjectResource> getUnfolderedResources(Long subjectId);
}
