package es.hugoalvarezajenjo.selecta.services.subjects;

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
}
