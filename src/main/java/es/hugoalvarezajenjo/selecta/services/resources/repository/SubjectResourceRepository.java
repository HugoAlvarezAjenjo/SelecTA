package es.hugoalvarezajenjo.selecta.services.resources.repository;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectResourceRepository extends JpaRepository<SubjectResource, Long> {
    List<SubjectResource> findSubjectResourceBySubjectId(Long subjectId);

    List<SubjectResource> findSubjectResourceBySubjectIdAndIsPrivate(Long subjectId, boolean isPrivate);

    @Query("SELECT DISTINCT r FROM SubjectResource r LEFT JOIN r.tags t " +
           "WHERE r.subjectId = :subjectId " +
           "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<SubjectResource> searchByNameOrTag(@Param("subjectId") Long subjectId, @Param("query") String query);

    @Query("SELECT r FROM SubjectResource r JOIN r.tags t WHERE t.id = :tagId")
    List<SubjectResource> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT r FROM SubjectResource r WHERE r.subjectId = :subjectId AND r.folder IS NULL")
    List<SubjectResource> findUnfolderedBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT r FROM SubjectResource r WHERE r.folder.id = :folderId")
    List<SubjectResource> findByFolderId(@Param("folderId") Long folderId);

    @Query("SELECT r FROM SubjectResource r WHERE r.uploadedBy.id = :userId")
    List<SubjectResource> findByUploadedById(@Param("userId") Long userId);
}
