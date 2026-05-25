package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceFolderRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceFolderServiceImplTest {

    @Mock
    private ResourceFolderRepository folderRepository;

    @Mock
    private SubjectResourceRepository resourceRepository;

    @InjectMocks
    private ResourceFolderServiceImpl service;

    private Subject createSubject() {
        Subject s = new Subject();
        s.setId(1L);
        s.setName("AI");
        return s;
    }

    private ResourceFolder createFolder(Long id, String name, Subject subject, ResourceFolder parent) {
        ResourceFolder f = new ResourceFolder();
        f.setId(id);
        f.setName(name);
        f.setSubject(subject);
        f.setParent(parent);
        f.setDisplayOrder(0);
        return f;
    }

    @Nested
    @DisplayName("Creating folders")
    class CreateFolderTests {

        @Test
        @DisplayName("Creates a root folder successfully")
        void createsRootFolder() {
            Subject subject = createSubject();
            when(folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(subject, "Tema 1", null)).thenReturn(false);
            when(folderRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject)).thenReturn(List.of());
            when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResourceFolder result = service.createFolder("Tema 1", subject, null);

            assertThat(result.getName()).isEqualTo("Tema 1");
            assertThat(result.getSubject()).isEqualTo(subject);
            assertThat(result.getParent()).isNull();
        }

        @Test
        @DisplayName("Throws when duplicate name exists at same level")
        void throwsOnDuplicateName() {
            Subject subject = createSubject();
            when(folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(subject, "Tema 1", null)).thenReturn(true);

            assertThatThrownBy(() -> service.createFolder("Tema 1", subject, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Renaming folders")
    class RenameFolderTests {

        @Test
        @DisplayName("Renames a folder successfully")
        void renamesFolder() {
            Subject subject = createSubject();
            ResourceFolder folder = createFolder(10L, "Old Name", subject, null);

            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));
            when(folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(subject, "New Name", null)).thenReturn(false);
            when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResourceFolder result = service.renameFolder(10L, "New Name");

            assertThat(result.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Allows renaming to same name (case change)")
        void allowsSameNameCaseChange() {
            Subject subject = createSubject();
            ResourceFolder folder = createFolder(10L, "tema 1", subject, null);

            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));
            when(folderRepository.existsBySubjectAndNameIgnoreCaseAndParent(subject, "Tema 1", null)).thenReturn(true);
            when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResourceFolder result = service.renameFolder(10L, "Tema 1");

            assertThat(result.getName()).isEqualTo("Tema 1");
        }

        @Test
        @DisplayName("Throws when folder not found")
        void throwsWhenNotFound() {
            when(folderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.renameFolder(99L, "New"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Moving folders")
    class MoveFolderTests {

        @Test
        @DisplayName("Moves folder to root (null parent)")
        void movesToRoot() {
            Subject subject = createSubject();
            ResourceFolder parent = createFolder(5L, "Parent", subject, null);
            ResourceFolder folder = createFolder(10L, "Child", subject, parent);

            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));
            when(folderRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject)).thenReturn(List.of());
            when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResourceFolder result = service.moveFolder(10L, null);

            assertThat(result.getParent()).isNull();
        }

        @Test
        @DisplayName("Throws when trying to make folder its own parent")
        void throwsOnSelfParent() {
            Subject subject = createSubject();
            ResourceFolder folder = createFolder(10L, "Folder", subject, null);

            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));

            assertThatThrownBy(() -> service.moveFolder(10L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("own parent");
        }

        @Test
        @DisplayName("Throws when trying to move under own descendant")
        void throwsOnCircularMove() {
            Subject subject = createSubject();
            ResourceFolder grandparent = createFolder(1L, "GP", subject, null);
            ResourceFolder parent = createFolder(2L, "P", subject, grandparent);
            ResourceFolder child = createFolder(3L, "C", subject, parent);

            when(folderRepository.findById(1L)).thenReturn(Optional.of(grandparent));
            when(folderRepository.findById(3L)).thenReturn(Optional.of(child));

            // Moving grandparent under child would create a cycle
            assertThatThrownBy(() -> service.moveFolder(1L, 3L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("descendant");
        }
    }

    @Nested
    @DisplayName("Deleting folders")
    class DeleteFolderTests {

        @Test
        @DisplayName("Deleting a folder promotes children and resources to parent")
        void deletingPromotesChildren() {
            Subject subject = createSubject();
            ResourceFolder parent = createFolder(1L, "Parent", subject, null);
            ResourceFolder folder = createFolder(10L, "ToDelete", subject, parent);
            ResourceFolder child = createFolder(20L, "Child", subject, folder);

            SubjectResource resource = new SubjectResource();
            resource.setId(50L);
            resource.setFolder(folder);

            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));
            when(folderRepository.findByParentOrderByDisplayOrderAsc(folder)).thenReturn(List.of(child));
            when(resourceRepository.findByFolderId(10L)).thenReturn(List.of(resource));

            service.deleteFolder(10L);

            assertThat(child.getParent()).isEqualTo(parent);
            assertThat(resource.getFolder()).isEqualTo(parent);
            verify(folderRepository).delete(folder);
        }

        @Test
        @DisplayName("Throws when folder not found")
        void throwsWhenNotFound() {
            when(folderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteFolder(99L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Resource-folder operations")
    class ResourceFolderOpsTests {

        @Test
        @DisplayName("Moves a resource to a folder")
        void movesResourceToFolder() {
            SubjectResource resource = new SubjectResource();
            resource.setId(50L);
            ResourceFolder folder = createFolder(10L, "Folder", createSubject(), null);

            when(resourceRepository.findById(50L)).thenReturn(Optional.of(resource));
            when(folderRepository.findById(10L)).thenReturn(Optional.of(folder));
            when(resourceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.moveResourceToFolder(50L, 10L);

            assertThat(resource.getFolder()).isEqualTo(folder);
        }

        @Test
        @DisplayName("Removes resource from folder (sets null)")
        void removesResourceFromFolder() {
            ResourceFolder folder = createFolder(10L, "Folder", createSubject(), null);
            SubjectResource resource = new SubjectResource();
            resource.setId(50L);
            resource.setFolder(folder);

            when(resourceRepository.findById(50L)).thenReturn(Optional.of(resource));
            when(resourceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.removeResourceFromFolder(50L);

            assertThat(resource.getFolder()).isNull();
        }
    }
}
