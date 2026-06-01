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
    public SubjectResource saveResource(final SubjectResource subjectResource) {
        return this.subjectResourceRepository.save(subjectResource);
    }

    @Override
    public List<SubjectResource> getResourcesFromSubject(final Long subjectId) {
        return this.subjectResourceRepository.findSubjectResourceBySubjectId(subjectId);
    }

    @Override
    public List<SubjectResource> getPublicResourcesFromSubject(final Long subjectId) {
        return this.subjectResourceRepository.findSubjectResourceBySubjectIdAndIsPrivate(subjectId, false);
    }

    @Override
    public SubjectResource findById(final Long resourceId) {
        return this.subjectResourceRepository.findById(resourceId).orElse(null);
    }

    @Override
    public void togglePrivacy(final Long resourceId) {
        final SubjectResource resource = this.subjectResourceRepository.findById(resourceId).orElse(null);
        if (resource != null) {
            resource.setPrivate(!resource.isPrivate());
            this.subjectResourceRepository.save(resource);
        }
    }

    @Override
    public void deleteResource(final Long resourceId) {
        this.subjectResourceRepository.deleteById(resourceId);
    }

    @Override
    public List<SubjectResource> getResourcesUploadedByUser(final Long userId) {
        return this.subjectResourceRepository.findByUploadedById(userId);
    }
}
