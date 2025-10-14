package es.hugoalvarezajenjo.selecta.services.subjects.repository;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SubjectSpecifications {

    public static Specification<Subject> containsWordsInNameOrDescription(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // return all if no query
            }

            String[] words = searchQuery.trim().toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>();

            for (String word : words) {
                if (word.length() > 2) { // Only consider words longer than 2 chars
                    Predicate namePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            "%" + word + "%"
                    );
                    Predicate descPredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            "%" + word + "%"
                    );
                    predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
                }
            }

            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Additional specifications for future filter enhancements
    public static Specification<Subject> withLanguage(String language) {
        return (root, query, cb) -> {
            if (language == null || language.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.isMember(language, root.get("languages"));
        };
    }

    public static Specification<Subject> withSemester(String semester) {
        return (root, query, cb) -> {
            if (semester == null || semester.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.isMember(semester, root.get("semesters"));
        };
    }
}