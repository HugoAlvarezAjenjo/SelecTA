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
    public ResourceTag getOrCreateTag(final String rawName, final Subject subject, final ResourceTag parent) {
        final String normalized = ResourceTag.normalizeName(rawName);

        final Optional<ResourceTag> existing = this.tagRepository.findBySubjectAndNameIgnoreCase(subject, normalized);
        if (existing.isPresent()) {
            return existing.get();
        }

        final ResourceTag tag = new ResourceTag();
        tag.setName(normalized);
        tag.setSubject(subject);
        tag.setParent(parent);
        tag.setDisplayOrder(this.calculateNextDisplayOrder(subject, parent));
        return this.tagRepository.save(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTag> getTagTree(final Subject subject) {
        return this.tagRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTag> getAllTags(final Subject subject) {
        return this.tagRepository.findBySubjectOrderByDisplayOrderAsc(subject);
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
            throw new IllegalArgumentException("A tag with name '" + normalized + "' already exists in this subject");
        }

        tag.setName(normalized);
        return this.tagRepository.save(tag);
    }

    @Override
    public ResourceTag moveTag(final Long tagId, final Long newParentId) {
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        if (newParentId == null) {
            tag.setParent(null);
        } else {
            if (newParentId.equals(tagId)) {
                throw new IllegalArgumentException("A tag cannot be its own parent");
            }
            final ResourceTag newParent = this.tagRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found: " + newParentId));

            // Prevent circular references
            if (this.isDescendantOf(newParent, tag)) {
                throw new IllegalArgumentException("Cannot move a tag under its own descendant");
            }
            tag.setParent(newParent);
        }

        tag.setDisplayOrder(this.calculateNextDisplayOrder(tag.getSubject(), tag.getParent()));
        return this.tagRepository.save(tag);
    }

    @Override
    public void deleteTag(final Long tagId) {
        final ResourceTag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        // Promote children to the deleted tag's parent
        final List<ResourceTag> children = this.tagRepository.findByParentOrderByDisplayOrderAsc(tag);
        for (final ResourceTag child : children) {
            child.setParent(tag.getParent());
            this.tagRepository.save(child);
        }

        // Remove this tag from all resources (M2M cleanup)
        final List<SubjectResource> taggedResources = this.resourceRepository.findByTagId(tagId);
        for (final SubjectResource resource : taggedResources) {
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

    // --- Private helpers ---

    private int calculateNextDisplayOrder(final Subject subject, final ResourceTag parent) {
        final List<ResourceTag> siblings;
        if (parent == null) {
            siblings = this.tagRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject);
        } else {
            siblings = this.tagRepository.findByParentOrderByDisplayOrderAsc(parent);
        }
        return siblings.isEmpty() ? 0 : siblings.get(siblings.size() - 1).getDisplayOrder() + 1;
    }

    private boolean isDescendantOf(final ResourceTag potentialDescendant, final ResourceTag ancestor) {
        ResourceTag current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
