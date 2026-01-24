package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import org.junit.jupiter.api.BeforeEach;
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
class SubjectResourceServiceImplTest {

    @Mock
    private SubjectResourceRepository subjectResourceRepository;

    @InjectMocks
    private SubjectResourceServiceImpl subjectResourceService;

    private SubjectResource resource;

    @BeforeEach
    void setUp() {
        resource = new SubjectResource();
        resource.setId(1L);
        resource.setName("Test Resource");
    }

    @Test
    void saveResource_shouldReturnSavedResource() {
        when(subjectResourceRepository.save(resource)).thenReturn(resource);

        SubjectResource savedResource = subjectResourceService.saveResource(resource);

        assertNotNull(savedResource);
        assertEquals("Test Resource", savedResource.getName());
        verify(subjectResourceRepository).save(resource);
    }

    @Test
    void getResourcesFromSubject_shouldReturnList() {
        Long subjectId = 1L;
        List<SubjectResource> resources = Arrays.asList(resource);
        when(subjectResourceRepository.findSubjectResourceBySubjectId(subjectId)).thenReturn(resources);

        List<SubjectResource> result = subjectResourceService.getResourcesFromSubject(subjectId);

        assertEquals(1, result.size());
        assertEquals(resource, result.get(0));
        verify(subjectResourceRepository).findSubjectResourceBySubjectId(subjectId);
    }

    @Test
    void findById_shouldReturnResource_whenFound() {
        when(subjectResourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        SubjectResource result = subjectResourceService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(subjectResourceRepository).findById(1L);
    }

    @Test
    void findById_shouldReturnNull_whenNotFound() {
        when(subjectResourceRepository.findById(1L)).thenReturn(Optional.empty());

        SubjectResource result = subjectResourceService.findById(1L);

        assertNull(result);
        verify(subjectResourceRepository).findById(1L);
    }

    @Test
    void deleteResource_shouldCallRepositoryDelete() {
        Long resourceId = 1L;

        subjectResourceService.deleteResource(resourceId);

        verify(subjectResourceRepository).deleteById(resourceId);
    }
}
