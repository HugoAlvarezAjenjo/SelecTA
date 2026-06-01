package es.hugoalvarezajenjo.selecta.services.resources;

import java.util.List;

public interface SubjectResourceService {
    SubjectResource saveResource(SubjectResource subjectResource);

    List<SubjectResource> getResourcesFromSubject(Long subjectId);

    List<SubjectResource> getPublicResourcesFromSubject(Long subjectId);

    SubjectResource findById(Long resourceId);

    void togglePrivacy(Long resourceId);

    void deleteResource(Long resourceId);

    List<SubjectResource> getResourcesUploadedByUser(Long userId);
}
