package es.hugoalvarezajenjo.selecta.ui.subject.user.subjectview;

import es.hugoalvarezajenjo.selecta.services.markdown.MarkdownService;
import es.hugoalvarezajenjo.selecta.services.resources.ResourceType;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.types.Languages;
import es.hugoalvarezajenjo.selecta.services.types.Semester;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectViewControllerTest {

    @Mock
    private SubjectService subjectService;

    @Mock
    private SubjectResourceService subjectResourceService;

    @Mock
    private MarkdownService markdownService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private SubjectViewController subjectViewController;

    private Subject testSubject;
    private SubjectResource testResource;
    private final Long EXISTING_SUBJECT_ID = 1L;
    private final Long NON_EXISTING_SUBJECT_ID = 999L;

    @BeforeEach
    void setUp() {
        // Setup test subject with all attributes
        testSubject = new Subject();
        testSubject.setId(EXISTING_SUBJECT_ID);
        testSubject.setName("Mathematics");
        testSubject.setDescription("Advanced Mathematics Course");
        testSubject.setCredits(6);
        testSubject.setSemesters(Set.of(Semester.FIRST, Semester.SECOND));
        testSubject.setLanguages(Set.of(Languages.ENGLISH, Languages.SPANISH));

        // Setup test resource
        testResource = new SubjectResource();
        testResource.setId(1L);
        testResource.setName("Course Syllabus");
        testResource.setDescription("Complete course syllabus document");
        testResource.setType(ResourceType.PRESENTATION);
        testResource.setLanguage("English");
        testResource.setCreationDate(LocalDate.of(2024, 1, 15));
        testResource.setOriginalName("syllabus.pdf");
        testResource.setSubjectId(EXISTING_SUBJECT_ID);
    }

    @Test
    @DisplayName("Should display subject page with correct subject details and resources")
    void shouldDisplaySubjectPageWithCorrectDetailsAndResources() {
        // Arrange
        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID))
                .thenReturn(List.of(testResource));
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        // Act
        String viewName = subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        assertEquals("subject/user/subject-view", viewName,
                "User should see the complete subject details page");
    }

    @Test
    @DisplayName("Should provide correct subject information converted to DTO with all attributes")
    void shouldProvideCorrectSubjectInformationAsDTO() {
        // Arrange
        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID)).thenReturn(List.of());
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        ArgumentCaptor<SubjectInfoDTO> subjectCaptor = ArgumentCaptor.forClass(SubjectInfoDTO.class);

        // Act
        subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        verify(model).addAttribute(eq("subject"), subjectCaptor.capture());

        SubjectInfoDTO subjectDTO = subjectCaptor.getValue();
        assertNotNull(subjectDTO, "Subject DTO should not be null");
        assertEquals(testSubject.getId(), subjectDTO.getId());
        assertEquals(testSubject.getName(), subjectDTO.getName());
        assertEquals(testSubject.getDescription(), subjectDTO.getDescription());

        // Verify attributes are correctly constructed
        assertNotNull(subjectDTO.getAttributes(), "Attributes should not be null");
        List<String> attributes = (List<String>) subjectDTO.getAttributes();
        assertTrue(attributes.contains("6 ects"), "Should include credits");
        assertTrue(attributes.contains("1 semester"), "Should include first semester");
        assertTrue(attributes.contains("2 semester"), "Should include second semester");
        assertTrue(attributes.contains("Ingles"), "Should include English language");
        assertTrue(attributes.contains("Español"), "Should include Spanish language");
        assertEquals(5, attributes.size(), "Should have all attributes");

        // Verify teachers are handled (even if empty)
        assertNotNull(subjectDTO.getTeachers(), "Teachers should not be null");
        assertFalse(subjectDTO.getTeachers().iterator().hasNext(), "Teachers should be empty");
    }

    @Test
    @DisplayName("Should provide correct subject resources converted to DTO list")
    void shouldProvideCorrectSubjectResourcesAsDTO() {
        // Arrange
        List<SubjectResource> testResources = List.of(testResource);
        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID))
                .thenReturn(testResources);
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        ArgumentCaptor<List<SubjectResourceDTO>> resourcesCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        verify(model).addAttribute(eq("resources"), resourcesCaptor.capture());

        List<SubjectResourceDTO> resourcesDTO = resourcesCaptor.getValue();
        assertNotNull(resourcesDTO, "Resources DTO list should not be null");
        assertEquals(1, resourcesDTO.size(), "Should have one resource");

        SubjectResourceDTO resourceDTO = resourcesDTO.getFirst();
        assertEquals(testResource.getName(), resourceDTO.getName());
        assertEquals(testResource.getDescription(), resourceDTO.getDescription());
        assertEquals(testResource.getType().toString(), resourceDTO.getType());
        assertEquals(testResource.getLanguage(), resourceDTO.getLanguage());
        assertEquals(testResource.getCreationDate().toString(), resourceDTO.getUploadDate());
    }

    @Test
    @DisplayName("Should handle subject with minimal attributes correctly")
    void shouldHandleSubjectWithMinimalAttributes() {
        // Arrange
        Subject minimalSubject = new Subject();
        minimalSubject.setId(2L);
        minimalSubject.setName("Physics");
        minimalSubject.setDescription("Physics Basics");
        minimalSubject.setCredits(4);
        // No semesters, no languages

        when(subjectService.getSubjectById(2L)).thenReturn(Optional.of(minimalSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(2L)).thenReturn(List.of());
        when(subjectService.getRelatedSubjects(2L, 3)).thenReturn(List.of());

        ArgumentCaptor<SubjectInfoDTO> subjectCaptor = ArgumentCaptor.forClass(SubjectInfoDTO.class);

        // Act
        subjectViewController.subjectView(2L, model);

        // Assert
        verify(model).addAttribute(eq("subject"), subjectCaptor.capture());

        SubjectInfoDTO subjectDTO = subjectCaptor.getValue();
        assertEquals(minimalSubject.getId(), subjectDTO.getId());
        assertEquals(minimalSubject.getName(), subjectDTO.getName());
        assertEquals(minimalSubject.getDescription(), subjectDTO.getDescription());

        List<String> attributes = (List<String>) subjectDTO.getAttributes();
        assertTrue(attributes.contains("4 ects"), "Should include credits");
        assertEquals(1, attributes.size(), "Should only have credits attribute");
    }

    @Test
    @DisplayName("Should display subject not found page for non-existent subjects")
    void shouldDisplayNotFoundPageForNonExistentSubjects() {
        // Arrange
        when(subjectService.getSubjectById(NON_EXISTING_SUBJECT_ID)).thenReturn(Optional.empty());

        // Act
        String viewName = subjectViewController.subjectView(NON_EXISTING_SUBJECT_ID, model);

        // Assert
        assertEquals("subject/user/no-subject", viewName,
                "User should see error page when requesting non-existent subjects");

        // Verify no subject data is added to model for non-existent subjects
        verify(model, never()).addAttribute(eq("subject"), any());
        verify(model, never()).addAttribute(eq("resources"), any());
    }

    @Test
    @DisplayName("Should handle empty resources list correctly")
    void shouldHandleEmptyResourcesList() {
        // Arrange
        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID))
                .thenReturn(List.of()); // Empty list
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        ArgumentCaptor<List<SubjectResourceDTO>> resourcesCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        verify(model).addAttribute(eq("resources"), resourcesCaptor.capture());

        List<SubjectResourceDTO> resourcesDTO = resourcesCaptor.getValue();
        assertNotNull(resourcesDTO, "Resources DTO should exist even for empty lists");
        assertTrue(resourcesDTO.isEmpty(), "Resources list should be empty");
    }

    @Test
    @DisplayName("Should handle multiple resources correctly")
    void shouldHandleMultipleResources() {
        // Arrange
        SubjectResource secondResource = new SubjectResource();
        secondResource.setId(2L);
        secondResource.setName("Exercise Sheet");
        secondResource.setDescription("Weekly exercises");
        secondResource.setType(ResourceType.PRESENTATION);
        secondResource.setLanguage("Spanish");
        secondResource.setCreationDate(LocalDate.of(2024, 1, 20));
        secondResource.setOriginalName("exercises.pdf");
        secondResource.setSubjectId(EXISTING_SUBJECT_ID);

        List<SubjectResource> testResources = List.of(testResource, secondResource);
        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID))
                .thenReturn(testResources);
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        ArgumentCaptor<List<SubjectResourceDTO>> resourcesCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        verify(model).addAttribute(eq("resources"), resourcesCaptor.capture());

        List<SubjectResourceDTO> resourcesDTO = resourcesCaptor.getValue();
        assertEquals(2, resourcesDTO.size(), "Should have two resources");
        assertEquals("Course Syllabus", resourcesDTO.get(0).getName());
        assertEquals("Exercise Sheet", resourcesDTO.get(1).getName());
    }

    @Test
    @DisplayName("Should provide teacher names and emails in the DTO")
    void shouldProvideTeacherNamesAndEmailsInDTO() {
        // Arrange
        es.hugoalvarezajenjo.selecta.services.user.Teacher teacher = new es.hugoalvarezajenjo.selecta.services.user.Teacher();
        teacher.setUsername("hugo");
        teacher.setEmail("hugo@example.com");
        testSubject.setTeachers(Set.of(teacher));

        when(subjectService.getSubjectById(EXISTING_SUBJECT_ID)).thenReturn(Optional.of(testSubject));
        when(subjectResourceService.getPublicResourcesFromSubject(EXISTING_SUBJECT_ID)).thenReturn(List.of());
        when(subjectService.getRelatedSubjects(EXISTING_SUBJECT_ID, 3)).thenReturn(List.of());

        ArgumentCaptor<SubjectInfoDTO> subjectCaptor = ArgumentCaptor.forClass(SubjectInfoDTO.class);

        // Act
        subjectViewController.subjectView(EXISTING_SUBJECT_ID, model);

        // Assert
        verify(model).addAttribute(eq("subject"), subjectCaptor.capture());
        SubjectInfoDTO subjectDTO = subjectCaptor.getValue();

        List<SubjectInfoDTO.TeacherInfo> teachers = (List<SubjectInfoDTO.TeacherInfo>) subjectDTO.getTeachers();
        assertEquals(1, teachers.size());
        assertEquals("hugo", teachers.get(0).getName());
        assertEquals("hugo@example.com", teachers.get(0).getEmail());
    }

    @Test
    @DisplayName("Should not fetch resources when subject does not exist")
    void shouldNotFetchResourcesForNonExistentSubjects() {
        // Arrange
        when(subjectService.getSubjectById(NON_EXISTING_SUBJECT_ID)).thenReturn(Optional.empty());

        // Act
        subjectViewController.subjectView(NON_EXISTING_SUBJECT_ID, model);

        // Assert - BEHAVIOR: We optimize by not fetching resources for non-existent
        // subjects
        verifyNoInteractions(subjectResourceService);
    }
}