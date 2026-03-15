package es.hugoalvarezajenjo.selecta.services.subjects;

import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private Subject subject1;
    private Subject subject2;
    private final Long EXISTING_ID = 1L;
    private final Long NON_EXISTING_ID = 999L;

    @BeforeEach
    void setUp() {
        subject1 = new Subject();
        subject1.setId(EXISTING_ID);
        subject1.setName("Mathematics");
        subject1.setDescription("Advanced Mathematics Course");
        subject1.setCredits(6);

        subject2 = new Subject();
        subject2.setId(2L);
        subject2.setName("Physics");
        subject2.setDescription("Physics Fundamentals");
        subject2.setCredits(4);
    }

    @Test
    @DisplayName("Should return all subjects when repository has data")
    void getAllSubjects_WhenSubjectsExist_ReturnsAllSubjects() {
        // Arrange
        List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);
        when(subjectRepository.findAll()).thenReturn(expectedSubjects);

        // Act
        List<Subject> actualSubjects = subjectService.getAllSubjects();

        // Assert
        assertNotNull(actualSubjects);
        assertEquals(2, actualSubjects.size());
        assertEquals(expectedSubjects, actualSubjects);
        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no subjects exist")
    void getAllSubjects_WhenNoSubjects_ReturnsEmptyList() {
        // Arrange
        when(subjectRepository.findAll()).thenReturn(List.of());

        // Act
        List<Subject> actualSubjects = subjectService.getAllSubjects();

        // Assert
        assertNotNull(actualSubjects);
        assertTrue(actualSubjects.isEmpty());
        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save subject successfully")
    void saveSubject_WithValidSubject_SavesSuccessfully() {
        // Arrange
        Subject newSubject = new Subject();
        newSubject.setName("Chemistry");
        newSubject.setDescription("Chemistry Basics");
        newSubject.setCredits(5);

        // Act
        subjectService.saveSubject(newSubject);

        // Assert
        verify(subjectRepository, times(1)).save(newSubject);
    }

    @Test
    @DisplayName("Should return subject when valid ID is provided")
    void getSubjectById_WithExistingId_ReturnsSubject() {
        // Arrange
        when(subjectRepository.findById(EXISTING_ID)).thenReturn(Optional.of(subject1));

        // Act
        Optional<Subject> result = subjectService.getSubjectById(EXISTING_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(subject1, result.get());
        assertEquals(EXISTING_ID, result.get().getId());
        verify(subjectRepository, times(1)).findById(EXISTING_ID);
    }

    @Test
    @DisplayName("Should return empty when non-existing ID is provided")
    void getSubjectById_WithNonExistingId_ReturnsEmpty() {
        // Arrange
        when(subjectRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Subject> result = subjectService.getSubjectById(NON_EXISTING_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(subjectRepository, times(1)).findById(NON_EXISTING_ID);
    }

    @Test
    @DisplayName("Should delete subject when valid ID is provided")
    void deleteSubjectById_WithExistingId_DeletesSuccessfully() {
        // Arrange
        doNothing().when(subjectRepository).deleteById(EXISTING_ID);

        // Act
        subjectService.deleteSubjectById(EXISTING_ID);

        // Assert
        verify(subjectRepository, times(1)).deleteById(EXISTING_ID);
    }

    @Test
    @DisplayName("Should delete subject even when ID doesn't exist")
    void deleteSubjectById_WithNonExistingId_ExecutesWithoutError() {
        // Arrange
        doNothing().when(subjectRepository).deleteById(NON_EXISTING_ID);

        // Act & Assert
        assertDoesNotThrow(() -> subjectService.deleteSubjectById(NON_EXISTING_ID));
        verify(subjectRepository, times(1)).deleteById(NON_EXISTING_ID);
    }

    @Test
    @DisplayName("Should handle null ID in delete operation")
    void deleteSubjectById_WithNullId_ExecutesWithoutError() {
        // Arrange
        doNothing().when(subjectRepository).deleteById(null);

        // Act & Assert
        assertDoesNotThrow(() -> subjectService.deleteSubjectById(null));
        verify(subjectRepository, times(1)).deleteById(null);
    }

    @Test
    @DisplayName("Should handle null ID in get operation")
    void getSubjectById_WithNullId_ReturnsEmpty() {
        // Arrange
        when(subjectRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Subject> result = subjectService.getSubjectById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(subjectRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should return all subjects when search query is null")
    void findBySearchQuery_WithNullQuery_ReturnsAllSubjects() {
        // Arrange
        List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);
        when(subjectRepository.findAll()).thenReturn(expectedSubjects);

        // Act
        List<Subject> result = subjectService.findBySearchQuery(null);

        // Assert
        assertEquals(expectedSubjects, result);
        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return all subjects when search query is empty")
    void findBySearchQuery_WithEmptyQuery_ReturnsAllSubjects() {
        // Arrange
        List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);
        when(subjectRepository.findAll()).thenReturn(expectedSubjects);

        // Act
        List<Subject> result = subjectService.findBySearchQuery("   ");

        // Assert
        assertEquals(expectedSubjects, result);
        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return filtered subjects when search query is provided")
    @SuppressWarnings("unchecked")
    void findBySearchQuery_WithValidQuery_ReturnsFilteredSubjects() {
        // Arrange
        List<Subject> filteredSubjects = List.of(subject1);
        when(subjectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(filteredSubjects);

        // Act
        List<Subject> result = subjectService.findBySearchQuery("Math");

        // Assert
        assertEquals(filteredSubjects, result);
        verify(subjectRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }

    @Test
    @DisplayName("Should return related subjects sorted by matching tags count")
    @SuppressWarnings("unchecked")
    void getRelatedSubjects_WithMatchingTags_ReturnsRelatedSubjects() {
        // Arrange
        Subject targetSubject = new Subject();
        targetSubject.setId(1L);
        targetSubject.setTags(new java.util.HashSet<>(Arrays.asList("Math", "Science")));

        Subject related1 = new Subject();
        related1.setId(2L);
        related1.setTags(new java.util.HashSet<>(Arrays.asList("Math", "Science", "Physics"))); // 2 matches

        Subject related2 = new Subject();
        related2.setId(3L);
        related2.setTags(new java.util.HashSet<>(Arrays.asList("Science", "Biology"))); // 1 match

        Subject unrelated = new Subject();
        unrelated.setId(4L);
        unrelated.setTags(new java.util.HashSet<>(Arrays.asList("History"))); // 0 matches

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(targetSubject));
        when(subjectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(Arrays.asList(targetSubject, related1, related2, unrelated));

        // Act
        List<Subject> result = subjectService.getRelatedSubjects(1L, 3);

        // Assert
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId()); // Most matches first
        assertEquals(3L, result.get(1).getId());
    }

    @Test
    @DisplayName("Should return empty list when target subject has no tags")
    void getRelatedSubjects_TargetHasNoTags_ReturnsEmptyList() {
        // Arrange
        Subject targetSubject = new Subject();
        targetSubject.setId(1L);
        targetSubject.setTags(new java.util.HashSet<>());

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(targetSubject));

        // Act
        List<Subject> result = subjectService.getRelatedSubjects(1L, 3);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should respect the limit parameter for related subjects")
    @SuppressWarnings("unchecked")
    void getRelatedSubjects_WithLimit_ReturnsLimitedResults() {
        // Arrange
        Subject targetSubject = new Subject();
        targetSubject.setId(1L);
        targetSubject.setTags(new java.util.HashSet<>(Arrays.asList("Math")));

        Subject related1 = new Subject();
        related1.setId(2L);
        related1.setTags(new java.util.HashSet<>(Arrays.asList("Math")));

        Subject related2 = new Subject();
        related2.setId(3L);
        related2.setTags(new java.util.HashSet<>(Arrays.asList("Math")));

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(targetSubject));
        when(subjectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(Arrays.asList(targetSubject, related1, related2));

        // Act
        List<Subject> result = subjectService.getRelatedSubjects(1L, 1);

        // Assert
        assertEquals(1, result.size());
    }
}