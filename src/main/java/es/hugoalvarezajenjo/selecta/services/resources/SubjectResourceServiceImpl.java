package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectResourceServiceImpl implements SubjectResourceService {
    private final SubjectResourceRepository subjectResourceRepository;

    @Override
    public void saveResource(final SubjectResource subjectResource) {
        this.subjectResourceRepository.save(subjectResource);
    }

    @Override
    public List<SubjectResource> getResourcesFromSubject(final Long subjectId) {
        return this.subjectResourceRepository.findSubjectResourceBySubjectId(subjectId);
    }

    @Override
    public SubjectResource findById(final Long resourceId) {
        return this.subjectResourceRepository.findById(resourceId).orElse(null);
    }

    @Override
    public void deleteResource(final Long resourceId) {
        this.subjectResourceRepository.deleteById(resourceId);
    }
}
