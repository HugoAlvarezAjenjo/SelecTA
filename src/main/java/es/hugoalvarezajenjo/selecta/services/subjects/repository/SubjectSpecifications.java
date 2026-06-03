package es.hugoalvarezajenjo.selecta.services.subjects.repository;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
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
                    String stripped = stripAccents(word);
                    List<Predicate> wordPredicates = new ArrayList<>();

                    // Search with original word
                    wordPredicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), "%" + word + "%"));
                    wordPredicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")), "%" + word + "%"));
                    wordPredicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.join("tags")), "%" + word + "%"));

                    // If stripping accents produces a different string, also search with that
                    if (!stripped.equals(word)) {
                        wordPredicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")), "%" + stripped + "%"));
                        wordPredicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")), "%" + stripped + "%"));
                        wordPredicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.join("tags")), "%" + stripped + "%"));
                    }

                    predicates.add(criteriaBuilder.or(wordPredicates.toArray(new Predicate[0])));
                }
            }

            if (!predicates.isEmpty()) {
                query.distinct(true); // Avoid duplicates from join
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Accent-insensitive search using SQL TRANSLATE function.
     * Works on both H2 and PostgreSQL by replacing accented chars at DB level.
     */
    public static Specification<Subject> accentInsensitiveSearch(String searchQuery) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return cb.conjunction();
            }

            String[] words = searchQuery.trim().toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>();

            for (String word : words) {
                if (word.length() > 2) {
                    String normalizedWord = stripAccents(word);

                    // Use TRANSLATE SQL function to normalize accents in DB columns
                    Expression<String> normalizedName = cb.function("TRANSLATE", String.class,
                            cb.lower(root.get("name")),
                            cb.literal(ACCENTED_CHARS), cb.literal(PLAIN_CHARS));
                    Expression<String> normalizedDesc = cb.function("TRANSLATE", String.class,
                            cb.lower(root.get("description")),
                            cb.literal(ACCENTED_CHARS), cb.literal(PLAIN_CHARS));

                    Predicate namePred = cb.like(normalizedName, "%" + normalizedWord + "%");
                    Predicate descPred = cb.like(normalizedDesc, "%" + normalizedWord + "%");

                    // Tags join with TRANSLATE
                    Expression<String> normalizedTag = cb.function("TRANSLATE", String.class,
                            cb.lower(root.join("tags")),
                            cb.literal(ACCENTED_CHARS), cb.literal(PLAIN_CHARS));
                    Predicate tagPred = cb.like(normalizedTag, "%" + normalizedWord + "%");

                    predicates.add(cb.or(namePred, descPred, tagPred));
                }
            }

            if (!predicates.isEmpty()) {
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static final String ACCENTED_CHARS = "áéíóúàèìòùäëïöüâêîôûñÁÉÍÓÚÀÈÌÒÙÄËÏÖÜÂÊÎÔÛÑ";
    private static final String PLAIN_CHARS    = "aeiouaeiouaeiouaeiounAEIOUAEIOUAEIOUAEIOUN";

    /**
     * Removes diacritical marks (accents) from a string in Java.
     * E.g., "programación" → "programacion"
     */
    private static String stripAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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

    public static Specification<Subject> isNotDiscontinued() {
        return (root, query, cb) -> cb.equal(root.get("discontinued"), false);
    }

    public static Specification<Subject> hasCreditsLessThanEqual(Integer maxCredits) {
        return (root, query, cb) -> {
            if (maxCredits == null || maxCredits <= 0) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("credits"), maxCredits);
        };
    }

    public static Specification<Subject> hasSemesterIn(List<es.hugoalvarezajenjo.selecta.services.types.Semester> semesters) {
        return (root, query, cb) -> {
            if (semesters == null || semesters.isEmpty()) {
                return cb.conjunction();
            }
            return root.join("semesters").in(semesters);
        };
    }
}