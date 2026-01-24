package es.hugoalvarezajenjo.selecta.services.subjects.repository;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectSpecificationsTest {

    @Mock
    private Root<Subject> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> namePath;

    @Mock
    private Path<String> descriptionPath;

    @Mock
    private Expression<String> lowerName;

    @Mock
    private Expression<String> lowerDescription;

    @Mock
    private Predicate predicate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Use lenient stubbing for setup as some tests don't use all of these
        lenient().when(root.<String>get(anyString())).thenReturn((Path) namePath);
        lenient().when(cb.lower(any())).thenReturn(lowerName);
        lenient().when(cb.conjunction()).thenReturn(predicate);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.or(any(), any())).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void containsWordsInNameOrDescription_shouldReturnConjunction_whenQueryIsEmpty() {
        Specification<Subject> spec = SubjectSpecifications.containsWordsInNameOrDescription("");
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void containsWordsInNameOrDescription_shouldReturnConjunction_whenQueryIsNull() {
        Specification<Subject> spec = SubjectSpecifications.containsWordsInNameOrDescription(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void containsWordsInNameOrDescription_shouldFilterByWordsLongerThanTwo() {
        Specification<Subject> spec = SubjectSpecifications.containsWordsInNameOrDescription("ab cdef");
        spec.toPredicate(root, query, cb);

        // "ab" is ignored, "cdef" is used twice (name and description)
        verify(cb, times(2)).like(any(), eq("%cdef%"));
    }

    @Test
    void containsWordsInNameOrDescription_shouldHandleMultipleWords() {
        Specification<Subject> spec = SubjectSpecifications.containsWordsInNameOrDescription("java spring");
        spec.toPredicate(root, query, cb);

        // Each word is searched in name and description
        verify(cb, times(2)).like(any(), eq("%java%"));
        verify(cb, times(2)).like(any(), eq("%spring%"));
        verify(cb, times(2)).or(any(Predicate.class), any(Predicate.class));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void withLanguage_shouldFilterByLanguage() {
        Path<Object> languagesPath = mock(Path.class);
        lenient().when(root.get("languages")).thenReturn(languagesPath);

        Specification<Subject> spec = SubjectSpecifications.withLanguage("Spanish");
        spec.toPredicate(root, query, cb);

        verify(cb).isMember(eq("Spanish"), any(Expression.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void withSemester_shouldFilterBySemester() {
        Path<Object> semestersPath = mock(Path.class);
        lenient().when(root.get("semesters")).thenReturn(semestersPath);

        Specification<Subject> spec = SubjectSpecifications.withSemester("First");
        spec.toPredicate(root, query, cb);

        verify(cb).isMember(eq("First"), any(Expression.class));
    }
}
