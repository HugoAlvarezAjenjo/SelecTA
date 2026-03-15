package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(@Autowired final SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public List<Subject> getAllSubjects() {
        return this.subjectRepository.findAll();
    }

    @Override
    public void saveSubject(final Subject subject) {
        this.subjectRepository.save(subject);
    }

    @Override
    public Optional<Subject> getSubjectById(final Long id) {
        return this.subjectRepository.findById(id);
    }

    @Override
    public void deleteSubjectById(final Long id) {
        this.subjectRepository.deleteById(id);
    }

    @Override
    public List<Subject> findBySearchQuery(final String query) {
        if (query == null || query.trim().isEmpty()) {
            return this.subjectRepository.findAll();
        }

        final Specification<Subject> spec = SubjectSpecifications.containsWordsInNameOrDescription(query);
        return this.subjectRepository.findAll(spec);
    }

    @Override
    public List<Subject> getActiveSubjects() {
        return this.subjectRepository.findAll(SubjectSpecifications.isNotDiscontinued());
    }

    @Override
    public List<Subject> findActiveBySearchQuery(final String query) {
        Specification<Subject> spec = SubjectSpecifications.isNotDiscontinued();

        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and(SubjectSpecifications.containsWordsInNameOrDescription(query));
        }

        return this.subjectRepository.findAll(spec);
    }

    @Override
    public List<Subject> getRelatedSubjects(final Long subjectId, final int limit) {
        final Optional<Subject> subjectOpt = this.subjectRepository.findById(subjectId);
        if (subjectOpt.isEmpty()) {
            return List.of();
        }

        final Subject targetSubject = subjectOpt.get();
        final java.util.Set<String> targetTags = targetSubject.getTags();

        if (targetTags == null || targetTags.isEmpty()) {
            return List.of();
        }

        return this.getActiveSubjects().stream()
                .filter(s -> !s.getId().equals(subjectId)) // Exclude the target subject itself
                .filter(s -> s.getTags() != null && s.getTags().stream().anyMatch(targetTags::contains)) // Must have at least one common tag
                .sorted((s1, s2) -> {
                    long s1Matches = s1.getTags().stream().filter(targetTags::contains).count();
                    long s2Matches = s2.getTags().stream().filter(targetTags::contains).count();
                    return Long.compare(s2Matches, s1Matches); // Descending order of matches
                })
                .limit(limit)
                .toList();
    }
}
