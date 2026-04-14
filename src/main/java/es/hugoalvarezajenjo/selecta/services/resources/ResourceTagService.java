package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;

import java.util.List;
import java.util.Optional;

public interface ResourceTagService {

    /**
     * Creates a new tag for a subject. Normalizes the name and prevents duplicates.
     * If a tag with the same normalized name already exists, returns the existing one.
     */
    ResourceTag getOrCreateTag(String rawName, Subject subject, ResourceTag parent);

    /**
     * Returns the full tag tree (root tags with children loaded) for a subject.
     */
    List<ResourceTag> getTagTree(Subject subject);

    /**
     * Returns all tags (flat list) for a subject.
     */
    List<ResourceTag> getAllTags(Subject subject);

    /**
     * Searches tags by name (case-insensitive, partial match) within a subject.
     */
    List<ResourceTag> searchTags(Subject subject, String query);

    /**
     * Finds a tag by its ID.
     */
    Optional<ResourceTag> findById(Long tagId);

    /**
     * Renames a tag. Prevents collisions with existing names.
     */
    ResourceTag renameTag(Long tagId, String newName);

    /**
     * Moves a tag to a new parent (or to root if parent is null).
     */
    ResourceTag moveTag(Long tagId, Long newParentId);

    /**
     * Deletes a tag. Resources are NOT deleted — they just lose the tag association.
     * Children tags are promoted to the parent of the deleted tag (or to root).
     */
    void deleteTag(Long tagId);

    /**
     * Assigns a tag to a resource.
     */
    void tagResource(Long resourceId, Long tagId);

    /**
     * Removes a tag from a resource.
     */
    void untagResource(Long resourceId, Long tagId);

    /**
     * Returns all resources that have a given tag.
     */
    List<SubjectResource> getResourcesByTag(Long tagId);

    /**
     * Searches resources by name or tag name within a subject (Obsidian-style quick search).
     */
    List<SubjectResource> searchResources(Long subjectId, String query);
}
