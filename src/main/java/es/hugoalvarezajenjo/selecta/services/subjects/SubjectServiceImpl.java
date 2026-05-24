package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectSpecifications;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.hibernate.Hibernate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectServiceImpl(@Autowired final SubjectRepository subjectRepository,
                              @Autowired final UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Subject> getAllSubjects() {
        return this.subjectRepository.findAll();
    }

    @Override
    public void saveSubject(final Subject subject) {
        this.subjectRepository.save(subject);
        log.info("Subject saved: id={}, name='{}'", subject.getId(), subject.getName());
    }

    @Override
    public Optional<Subject> getSubjectById(final Long id) {
        return this.subjectRepository.findById(id);
    }

    @Override
    public void deleteSubjectById(final Long id) {
        log.info("Deleting subject id={}", id);
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

    @Override
    public List<Subject> recommendSubjects(final SubjectRecommendationCriteria criteria) {
        if (criteria == null) {
            return getActiveSubjects();
        }

        Specification<Subject> spec = SubjectSpecifications.isNotDiscontinued();

        if (criteria.getMaxCredits() != null) {
            spec = spec.and(SubjectSpecifications.hasCreditsLessThanEqual(criteria.getMaxCredits()));
        }

        if (criteria.getSemesterTypes() != null && !criteria.getSemesterTypes().isEmpty()) {
            java.util.List<es.hugoalvarezajenjo.selecta.services.types.Semester> targetSemesters = new java.util.ArrayList<>();
            if (criteria.getSemesterTypes().contains("ODD")) {
                targetSemesters.addAll(java.util.Arrays.asList(
                    es.hugoalvarezajenjo.selecta.services.types.Semester.FIRST, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.THIRD, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.FIFTH, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.SEVENTH));
            }
            if (criteria.getSemesterTypes().contains("EVEN")) {
                targetSemesters.addAll(java.util.Arrays.asList(
                    es.hugoalvarezajenjo.selecta.services.types.Semester.SECOND, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.FOURTH, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.SIXTH, 
                    es.hugoalvarezajenjo.selecta.services.types.Semester.EIGHTH));
            }
            spec = spec.and(SubjectSpecifications.hasSemesterIn(targetSemesters));
        }

        if (criteria.getLanguage() != null) {
            spec = spec.and(SubjectSpecifications.withLanguage(criteria.getLanguage().name()));
        }

        List<Subject> filteredSubjects = this.subjectRepository.findAll(spec);

        String searchKeywords = criteria.getSearchKeywords();
        if (searchKeywords != null && !searchKeywords.trim().isEmpty()) {
            String[] words = searchKeywords.trim().toLowerCase().split("[\\s,]+"); // Split by comma or space
            
            // Re-order by relevance match
            filteredSubjects.sort((s1, s2) -> {
                long s1Score = calculateRelevanceScore(s1, words);
                long s2Score = calculateRelevanceScore(s2, words);
                return Long.compare(s2Score, s1Score); // Descending
            });
            
            // Only return items that had at least 1 match with keywords if keywords are provided
            filteredSubjects = filteredSubjects.stream()
                .filter(s -> calculateRelevanceScore(s, words) > 0)
                .toList();
        }

        return filteredSubjects;
    }

    private long calculateRelevanceScore(Subject subject, String[] words) {
        long score = 0;
        String name = subject.getName() != null ? subject.getName().toLowerCase() : "";
        String desc = subject.getDescription() != null ? subject.getDescription().toLowerCase() : "";
        java.util.Set<String> tags = subject.getTags();

        for (String word : words) {
            if (word.length() > 2) { // Skip tiny words
                if (name.contains(word)) score += 3; // Name match gets highest weight
                if (desc.contains(word)) score += 1; // Description match
                if (tags != null && tags.stream().map(String::toLowerCase).anyMatch(t -> t.contains(word))) {
                    score += 2; // Tag match
                }
            }
        }
        return score;
    }

    @Override
    @Transactional
    public void addTeacherToSubject(final Long subjectId, final Long teacherId) {
        final Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        final User user = this.userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        if (!(user instanceof Teacher teacher)) {
            throw new IllegalArgumentException("User " + teacherId + " is not a teacher");
        }
        subject.getTeachers().add(teacher);
        this.subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void removeTeacherFromSubject(final Long subjectId, final Long teacherId) {
        final Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        final User user = this.userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        if (!(user instanceof Teacher teacher)) {
            throw new IllegalArgumentException("User " + teacherId + " is not a teacher");
        }
        subject.getTeachers().remove(teacher);
        this.subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void addContributor(final Long subjectId, final Long studentId) {
        final Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        final User rawUser = this.userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Unproxy to get the real entity class (fixes Hibernate proxy instanceof issue)
        final User user = (User) Hibernate.unproxy(rawUser);

        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException("User " + studentId + " is not a student (class=" + user.getClass().getSimpleName() + ", role=" + user.getRole() + ")");
        }
        subject.getContributors().add(student);
        this.subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void removeContributor(final Long subjectId, final Long studentId) {
        final Subject subject = this.subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        final User rawUser = this.userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        final User user = (User) Hibernate.unproxy(rawUser);
        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException("User " + studentId + " is not a student");
        }
        subject.getContributors().remove(student);
        this.subjectRepository.save(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isContributor(final Long subjectId, final Long userId) {
        final Optional<Subject> subjectOpt = this.subjectRepository.findById(subjectId);
        if (subjectOpt.isEmpty()) return false;
        return subjectOpt.get().getContributors().stream()
                .anyMatch(c -> c.getId().equals(userId));
    }
}
