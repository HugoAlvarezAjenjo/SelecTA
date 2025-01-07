package es.hugoalvarezajenjo.selecta.service;

import es.hugoalvarezajenjo.selecta.entity.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectService {
    List<Subject> getAllSubjects();

    void saveSubject(Subject subject);

    Optional<Subject> getSubjectById(Long id);

    void deleteSubjectById(Long id);
}
