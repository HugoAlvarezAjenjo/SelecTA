package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceFolderRepository;
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
public class ResourceFolderServiceImpl implements ResourceFolderService {

    private final ResourceFolderRepository folderRepository;
    private final SubjectResourceRepository resourceRepository;

    @Override
    public ResourceFolder createFolder(final String name, final Subject subject, final ResourceFolder parent) {
        final String normalized = ResourceFolder.normalizeName(name);
        if (this.folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(subject, normalized, parent)) {
            throw new IllegalArgumentException("A folder with name '" + normalized + "' already exists at this level");
        }
        final ResourceFolder folder = new ResourceFolder();
        folder.setName(normalized);
        folder.setSubject(subject);
        folder.setParent(parent);
        folder.setDisplayOrder(this.calculateNextOrder(subject, parent));
        return this.folderRepository.save(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceFolder> getFolderTree(final Subject subject) {
        return this.folderRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceFolder> getAllFolders(final Subject subject) {
        return this.folderRepository.findBySubjectOrderByDisplayOrderAsc(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResourceFolder> findById(final Long folderId) {
        return this.folderRepository.findById(folderId);
    }

    @Override
    public ResourceFolder renameFolder(final Long folderId, final String newName) {
        final ResourceFolder folder = this.folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderId));
        final String normalized = ResourceFolder.normalizeName(newName);
        if (this.folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(folder.getSubject(), normalized, folder.getParent())
                && !folder.getName().equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("A folder with name '" + normalized + "' already exists at this level");
        }
        folder.setName(normalized);
        return this.folderRepository.save(folder);
    }

    @Override
    public ResourceFolder moveFolder(final Long folderId, final Long newParentId) {
        final ResourceFolder folder = this.folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderId));
        if (newParentId == null) {
            folder.setParent(null);
        } else {
            if (newParentId.equals(folderId)) {
                throw new IllegalArgumentException("A folder cannot be its own parent");
            }
            final ResourceFolder newParent = this.folderRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent folder not found: " + newParentId));
            if (this.isDescendantOf(newParent, folder)) {
                throw new IllegalArgumentException("Cannot move a folder under its own descendant");
            }
            folder.setParent(newParent);
        }
        folder.setDisplayOrder(this.calculateNextOrder(folder.getSubject(), folder.getParent()));
        return this.folderRepository.save(folder);
    }

    @Override
    public void deleteFolder(final Long folderId) {
        final ResourceFolder folder = this.folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderId));
        // Promote children to parent
        for (final ResourceFolder child : this.folderRepository.findByParentOrderByDisplayOrderAsc(folder)) {
            child.setParent(folder.getParent());
            this.folderRepository.save(child);
        }
        // Unassign resources from this folder
        for (final SubjectResource resource : this.resourceRepository.findByFolderId(folderId)) {
            resource.setFolder(folder.getParent());
            this.resourceRepository.save(resource);
        }
        this.folderRepository.delete(folder);
    }

    @Override
    public void moveResourceToFolder(final Long resourceId, final Long folderId) {
        final SubjectResource resource = this.resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        final ResourceFolder folder = this.folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderId));
        resource.setFolder(folder);
        this.resourceRepository.save(resource);
    }

    @Override
    public void removeResourceFromFolder(final Long resourceId) {
        final SubjectResource resource = this.resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        resource.setFolder(null);
        this.resourceRepository.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResource> getResourcesInFolder(final Long folderId) {
        return this.resourceRepository.findByFolderId(folderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResource> getUnfolderedResources(final Long subjectId) {
        return this.resourceRepository.findUnfolderedBySubjectId(subjectId);
    }

    private int calculateNextOrder(final Subject subject, final ResourceFolder parent) {
        final List<ResourceFolder> siblings = parent == null
                ? this.folderRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject)
                : this.folderRepository.findByParentOrderByDisplayOrderAsc(parent);
        return siblings.isEmpty() ? 0 : siblings.get(siblings.size() - 1).getDisplayOrder() + 1;
    }

    private boolean isDescendantOf(final ResourceFolder potentialDescendant, final ResourceFolder ancestor) {
        ResourceFolder current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) return true;
            current = current.getParent();
        }
        return false;
    }
}
