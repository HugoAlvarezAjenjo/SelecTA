package es.hugoalvarezajenjo.selecta.services.resources;

import java.util.List;

public interface SubjectResourceService {
    List<SubjectResource> getResourcesFromSubject(Long subjectId);
}
