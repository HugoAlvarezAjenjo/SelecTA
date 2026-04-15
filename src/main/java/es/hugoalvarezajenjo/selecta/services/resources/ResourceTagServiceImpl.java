package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceTagRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceTagServiceImpl implements ResourceTagService {

    private final ResourceTagRepository tagRepository;
    private final SubjectResourceRepository resourceRepository;

    @Override
    public ResourceTag getOrCreateTag(final String rawName, final Subject subject) {
        final String normalized = ResourceTag.normalizeName(rawName);
        return this.tagRepository.findBySubjectAndNameIgnoreCase(subject, normalized)
                .orElseGet(() -> {
                    final ResourceTag tag = new ResourceTag();
                    tag.setName(normalized);
                    tag.setSubject(subject);
                    return this.tagRepository.save(tag);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTag> getAllTags(final Subject subject) {
        return this.tagRepository.findBySubjectOrderByNameAsc(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTag> searchTags(final Subject subject, final String query) {
        if (query == null || query.isBlank()) {
            return this.getAllTags(subject);
        }
        return this.tagRepository.searchByName(subject, query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResourceTag> findById(final Long tagId) {
        return this.tagRepository.findById(tagId);
    }

    @Override
    public ResourceTag renameTag(final Long tagId, final String newName) {
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        final String normalized = ResourceTag.normalizeName(newName);
        final boolean collision = this.tagRepository.existsBySubjectAndNameIgnoreCase(tag.getSubject(), normalized);
        if (collision && !tag.getName().equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("A tag with name '" + normalized + "' already exists");
        }
        tag.setName(normalized);
        return this.tagRepository.save(tag);
    }

    @Override
    public void deleteTag(final Long tagId) {
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        final List<SubjectResource> tagged = this.resourceRepository.findByTagId(tagId);
        for (final SubjectResource resource : tagged) {
            resource.getTags().remove(tag);
            this.resourceRepository.save(resource);
        }
        this.tagRepository.delete(tag);
    }

    @Override
    public void tagResource(final Long resourceId, final Long tagId) {
        final SubjectResource resource = this.resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        resource.getTags().add(tag);
        this.resourceRepository.save(resource);
    }

    @Override
    public void untagResource(final Long resourceId, final Long tagId) {
        final SubjectResource resource = this.resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        resource.getTags().remove(tag);
        this.resourceRepository.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResource> getResourcesByTag(final Long tagId) {
        return this.resourceRepository.findByTagId(tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResource> searchResources(final Long subjectId, final String query) {
        if (query == null || query.isBlank()) {
            return this.resourceRepository.findSubjectResourceBySubjectId(subjectId);
        }
        return this.resourceRepository.searchByNameOrTag(subjectId, query.trim());
    }
}
