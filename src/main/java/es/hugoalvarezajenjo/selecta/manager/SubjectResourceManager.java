package es.hugoalvarezajenjo.selecta.manager;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectResourceManager {
    private final SubjectService subjectService;
    private final SubjectResourceService subjectResourceService;
}
