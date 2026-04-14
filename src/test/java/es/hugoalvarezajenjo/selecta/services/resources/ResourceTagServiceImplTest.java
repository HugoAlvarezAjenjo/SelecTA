package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceTagRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceTagServiceImplTest {

    @Mock
    private ResourceTagRepository tagRepository;

    @Mock
    private SubjectResourceRepository resourceRepository;

    @InjectMocks
    private ResourceTagServiceImpl service;

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setId(1L);
        subject.setName("Inteligencia Artificial");
    }

    @Nested
    class GetOrCreateTag {

        @Test
        void shouldReturnExistingTagWhenNameMatchesCaseInsensitive() {
            final ResourceTag existing = createTag(1L, "Tema 1", null);
            when(tagRepository.findBySubjectAndNameIgnoreCase(subject, "Tema 1")).thenReturn(Optional.of(existing));

            final ResourceTag result = service.getOrCreateTag("  tema 1  ", subject, null);

            assertThat(result).isEqualTo(existing);
            verify(tagRepository, never()).save(any());
        }

        @Test
        void shouldCreateNewTagWhenNotExists() {
            when(tagRepository.findBySubjectAndNameIgnoreCase(eq(subject), any())).thenReturn(Optional.empty());
            when(tagRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject)).thenReturn(List.of());
            when(tagRepository.save(any(ResourceTag.class))).thenAnswer(inv -> {
                final ResourceTag tag = inv.getArgument(0);
                tag.setId(10L);
                return tag;
            });

            final ResourceTag result = service.getOrCreateTag("microorganismos", subject, null);

            assertThat(result.getName()).isEqualTo("Microorganismos"); // normalized
            assertThat(result.getSubject()).isEqualTo(subject);
            assertThat(result.getParent()).isNull();
            assertThat(result.getDisplayOrder()).isEqualTo(0);
        }

        @Test
        void shouldCreateTagUnderParent() {
            final ResourceTag parent = createTag(1L, "Tema 1", null);
            when(tagRepository.findBySubjectAndNameIgnoreCase(eq(subject), any())).thenReturn(Optional.empty());
            when(tagRepository.findByParentOrderByDisplayOrderAsc(parent)).thenReturn(List.of());
            when(tagRepository.save(any(ResourceTag.class))).thenAnswer(inv -> {
                final ResourceTag tag = inv.getArgument(0);
                tag.setId(20L);
                return tag;
            });

            final ResourceTag result = service.getOrCreateTag("Virus", subject, parent);

            assertThat(result.getParent()).isEqualTo(parent);
        }

        @Test
        void shouldRejectBlankName() {
            assertThatThrownBy(() -> service.getOrCreateTag("   ", subject, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        void shouldRejectNullName() {
            assertThatThrownBy(() -> service.getOrCreateTag(null, subject, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class RenameTag {

        @Test
        void shouldRenameTagSuccessfully() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            tag.setSubject(subject);

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "Tema 2")).thenReturn(false);
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.renameTag(1L, "Tema 2");

            assertThat(result.getName()).isEqualTo("Tema 2");
        }

        @Test
        void shouldRejectRenameToExistingName() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            tag.setSubject(subject);

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "Tema 2")).thenReturn(true);

            assertThatThrownBy(() -> service.renameTag(1L, "Tema 2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        void shouldAllowRenamingToSameNameDifferentCase() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            tag.setSubject(subject);

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "TEMA 1")).thenReturn(true);
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.renameTag(1L, "TEMA 1");

            assertThat(result.getName()).isEqualTo("TEMA 1");
        }

        @Test
        void shouldThrowForNonExistentTag() {
            when(tagRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.renameTag(999L, "New Name"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tag not found");
        }
    }

    @Nested
    class MoveTag {

        @Test
        void shouldMoveTagToRoot() {
            final ResourceTag parent = createTag(1L, "Tema 1", null);
            final ResourceTag child = createTag(2L, "Virus", parent);
            child.setSubject(subject);

            when(tagRepository.findById(2L)).thenReturn(Optional.of(child));
            when(tagRepository.findBySubjectAndParentIsNullOrderByDisplayOrderAsc(subject)).thenReturn(List.of(parent));
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.moveTag(2L, null);

            assertThat(result.getParent()).isNull();
        }

        @Test
        void shouldMoveTagUnderNewParent() {
            final ResourceTag parent1 = createTag(1L, "Tema 1", null);
            final ResourceTag parent2 = createTag(2L, "Tema 2", null);
            parent2.setSubject(subject);
            final ResourceTag child = createTag(3L, "Virus", parent1);
            child.setSubject(subject);

            when(tagRepository.findById(3L)).thenReturn(Optional.of(child));
            when(tagRepository.findById(2L)).thenReturn(Optional.of(parent2));
            when(tagRepository.findByParentOrderByDisplayOrderAsc(parent2)).thenReturn(List.of());
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.moveTag(3L, 2L);

            assertThat(result.getParent()).isEqualTo(parent2);
        }

        @Test
        void shouldRejectSelfParenting() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            tag.setSubject(subject);
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

            assertThatThrownBy(() -> service.moveTag(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("its own parent");
        }

        @Test
        void shouldRejectCircularReference() {
            final ResourceTag grandparent = createTag(1L, "Tema 1", null);
            final ResourceTag parent = createTag(2L, "Sub A", grandparent);
            final ResourceTag child = createTag(3L, "Sub B", parent);
            child.setSubject(subject);

            when(tagRepository.findById(3L)).thenReturn(Optional.of(child)); // child to move
            when(tagRepository.findById(1L)).thenReturn(Optional.of(grandparent)); // trying to move under grandparent - wait, that's not circular

            // Actually circular: move grandparent under child
            grandparent.setSubject(subject);
            when(tagRepository.findById(1L)).thenReturn(Optional.of(grandparent));
            when(tagRepository.findById(3L)).thenReturn(Optional.of(child));

            assertThatThrownBy(() -> service.moveTag(1L, 3L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("descendant");
        }
    }

    @Nested
    class DeleteTag {

        @Test
        void shouldDeleteTagAndPromoteChildren() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            final ResourceTag child1 = createTag(2L, "Sub A", tag);
            final ResourceTag child2 = createTag(3L, "Sub B", tag);

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.findByParentOrderByDisplayOrderAsc(tag)).thenReturn(List.of(child1, child2));
            when(resourceRepository.findByTagId(1L)).thenReturn(List.of());

            service.deleteTag(1L);

            // Children promoted to root (parent's parent = null)
            assertThat(child1.getParent()).isNull();
            assertThat(child2.getParent()).isNull();
            verify(tagRepository).delete(tag);
        }

        @Test
        void shouldRemoveTagFromResourcesOnDelete() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            final SubjectResource resource = new SubjectResource();
            resource.setId(10L);
            resource.setTags(new HashSet<>(Set.of(tag)));

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.findByParentOrderByDisplayOrderAsc(tag)).thenReturn(List.of());
            when(resourceRepository.findByTagId(1L)).thenReturn(List.of(resource));

            service.deleteTag(1L);

            assertThat(resource.getTags()).doesNotContain(tag);
            verify(resourceRepository).save(resource);
            verify(tagRepository).delete(tag);
        }
    }

    @Nested
    class TagAndUntagResource {

        @Test
        void shouldTagResource() {
            final SubjectResource resource = new SubjectResource();
            resource.setId(10L);
            resource.setTags(new HashSet<>());
            final ResourceTag tag = createTag(1L, "Tema 1", null);

            when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

            service.tagResource(10L, 1L);

            assertThat(resource.getTags()).contains(tag);
            verify(resourceRepository).save(resource);
        }

        @Test
        void shouldUntagResource() {
            final ResourceTag tag = createTag(1L, "Tema 1", null);
            final SubjectResource resource = new SubjectResource();
            resource.setId(10L);
            resource.setTags(new HashSet<>(Set.of(tag)));

            when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

            service.untagResource(10L, 1L);

            assertThat(resource.getTags()).doesNotContain(tag);
            verify(resourceRepository).save(resource);
        }

        @Test
        void shouldThrowWhenTaggingNonExistentResource() {
            when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.tagResource(999L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Resource not found");
        }
    }

    @Nested
    class SearchResources {

        @Test
        void shouldReturnAllResourcesWhenQueryIsBlank() {
            final List<SubjectResource> resources = List.of(new SubjectResource());
            when(resourceRepository.findSubjectResourceBySubjectId(1L)).thenReturn(resources);

            final List<SubjectResource> result = service.searchResources(1L, "   ");

            assertThat(result).hasSize(1);
            verify(resourceRepository).findSubjectResourceBySubjectId(1L);
        }

        @Test
        void shouldSearchByNameOrTag() {
            final List<SubjectResource> resources = List.of(new SubjectResource());
            when(resourceRepository.searchByNameOrTag(1L, "micro")).thenReturn(resources);

            final List<SubjectResource> result = service.searchResources(1L, "  micro  ");

            assertThat(result).hasSize(1);
            verify(resourceRepository).searchByNameOrTag(1L, "micro");
        }

        @Test
        void shouldReturnAllResourcesWhenQueryIsNull() {
            when(resourceRepository.findSubjectResourceBySubjectId(1L)).thenReturn(List.of());

            final List<SubjectResource> result = service.searchResources(1L, null);

            verify(resourceRepository).findSubjectResourceBySubjectId(1L);
        }
    }

    @Nested
    class NormalizeName {

        @Test
        void shouldCapitalizeFirstLetter() {
            assertThat(ResourceTag.normalizeName("microorganismos")).isEqualTo("Microorganismos");
        }

        @Test
        void shouldTrimWhitespace() {
            assertThat(ResourceTag.normalizeName("  tema 1  ")).isEqualTo("Tema 1");
        }

        @Test
        void shouldPreserveExistingCapitalization() {
            assertThat(ResourceTag.normalizeName("DNA")).isEqualTo("DNA");
        }

        @Test
        void shouldRejectBlank() {
            assertThatThrownBy(() -> ResourceTag.normalizeName("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectNull() {
            assertThatThrownBy(() -> ResourceTag.normalizeName(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // --- Helpers ---

    private ResourceTag createTag(final Long id, final String name, final ResourceTag parent) {
        final ResourceTag tag = new ResourceTag();
        tag.setId(id);
        tag.setName(name);
        tag.setParent(parent);
        tag.setSubject(subject);
        tag.setChildren(new ArrayList<>());
        tag.setDisplayOrder(0);
        return tag;
    }
}
