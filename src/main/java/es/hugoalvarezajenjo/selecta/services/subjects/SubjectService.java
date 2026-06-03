package es.hugoalvarezajenjo.selecta.services.subjects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubjectService {
    List<Subject> getAllSubjects();

    void saveSubject(Subject subject);

    Optional<Subject> getSubjectById(Long id);

    void deleteSubjectById(Long id);

    List<Subject> findBySearchQuery(String searchQuery);

    List<Subject> getActiveSubjects();

    List<Subject> findActiveBySearchQuery(String searchQuery);

    Page<Subject> findActiveBySearchQuery(String searchQuery, Pageable pageable);

    Page<Subject> findActiveBySearchQuery(String searchQuery, Integer semester, String language, Pageable pageable);

    List<Subject> getRelatedSubjects(Long subjectId, int limit);

    void addTeacherToSubject(Long subjectId, Long teacherId);

    void removeTeacherFromSubject(Long subjectId, Long teacherId);

    void addContributor(Long subjectId, Long studentId);

    void removeContributor(Long subjectId, Long studentId);

    boolean isContributor(Long subjectId, Long userId);
}
