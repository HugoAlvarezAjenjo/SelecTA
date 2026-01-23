package es.hugoalvarezajenjo.selecta.services.resources;

import java.util.List;

public interface SubjectResourceService {
    SubjectResource saveResource(SubjectResource subjectResource);

    List<SubjectResource> getResourcesFromSubject(Long subjectId);

    SubjectResource findById(Long resourceId);

    void deleteResource(Long resourceId);
}
