package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceTagRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        subject.setName("Test Subject");
    }

    @Nested
    class GetOrCreateTag {
        @Test
        void shouldReturnExistingTag() {
            final ResourceTag existing = createTag(1L, "Exámenes");
            when(tagRepository.findBySubjectAndNameIgnoreCase(subject, "Exámenes")).thenReturn(Optional.of(existing));

            final ResourceTag result = service.getOrCreateTag("  exámenes  ", subject);

            assertThat(result).isEqualTo(existing);
            verify(tagRepository, never()).save(any());
        }

        @Test
        void shouldCreateNewTag() {
            when(tagRepository.findBySubjectAndNameIgnoreCase(eq(subject), any())).thenReturn(Optional.empty());
            when(tagRepository.save(any(ResourceTag.class))).thenAnswer(inv -> {
                final ResourceTag t = inv.getArgument(0); t.setId(10L); return t;
            });

            final ResourceTag result = service.getOrCreateTag("microorganismos", subject);

            assertThat(result.getName()).isEqualTo("Microorganismos");
            assertThat(result.getSubject()).isEqualTo(subject);
        }

        @Test
        void shouldRejectBlankName() {
            assertThatThrownBy(() -> service.getOrCreateTag("   ", subject))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectNullName() {
            assertThatThrownBy(() -> service.getOrCreateTag(null, subject))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class RenameTag {
        @Test
        void shouldRenameSuccessfully() {
            final ResourceTag tag = createTag(1L, "Old");
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "New")).thenReturn(false);
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.renameTag(1L, "New");
            assertThat(result.getName()).isEqualTo("New");
        }

        @Test
        void shouldRejectDuplicateName() {
            final ResourceTag tag = createTag(1L, "Old");
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "Existing")).thenReturn(true);

            assertThatThrownBy(() -> service.renameTag(1L, "Existing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        void shouldAllowSameNameDifferentCase() {
            final ResourceTag tag = createTag(1L, "Test");
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            when(tagRepository.existsBySubjectAndNameIgnoreCase(subject, "TEST")).thenReturn(true);
            when(tagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ResourceTag result = service.renameTag(1L, "TEST");
            assertThat(result.getName()).isEqualTo("TEST");
        }
    }

    @Nested
    class DeleteTag {
        @Test
        void shouldDeleteAndRemoveFromResources() {
            final ResourceTag tag = createTag(1L, "Tag");
            final SubjectResource resource = new SubjectResource();
            resource.setId(10L);
            resource.setTags(new HashSet<>(Set.of(tag)));

            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
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
            final ResourceTag tag = createTag(1L, "Tag");

            when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

            service.tagResource(10L, 1L);
            assertThat(resource.getTags()).contains(tag);
        }

        @Test
        void shouldUntagResource() {
            final ResourceTag tag = createTag(1L, "Tag");
            final SubjectResource resource = new SubjectResource();
            resource.setId(10L);
            resource.setTags(new HashSet<>(Set.of(tag)));

            when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

            service.untagResource(10L, 1L);
            assertThat(resource.getTags()).doesNotContain(tag);
        }

        @Test
        void shouldThrowForNonExistentResource() {
            when(resourceRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.tagResource(999L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class SearchResources {
        @Test
        void shouldReturnAllWhenBlank() {
            when(resourceRepository.findSubjectResourceBySubjectId(1L)).thenReturn(List.of(new SubjectResource()));
            assertThat(service.searchResources(1L, "   ")).hasSize(1);
        }

        @Test
        void shouldSearchByQuery() {
            when(resourceRepository.searchByNameOrTag(1L, "micro")).thenReturn(List.of(new SubjectResource()));
            assertThat(service.searchResources(1L, "  micro  ")).hasSize(1);
        }
    }

    @Nested
    class NormalizeName {
        @Test
        void shouldCapitalize() { assertThat(ResourceTag.normalizeName("test")).isEqualTo("Test"); }
        @Test
        void shouldTrim() { assertThat(ResourceTag.normalizeName("  abc  ")).isEqualTo("Abc"); }
        @Test
        void shouldRejectBlank() { assertThatThrownBy(() -> ResourceTag.normalizeName("  ")).isInstanceOf(IllegalArgumentException.class); }
        @Test
        void shouldRejectNull() { assertThatThrownBy(() -> ResourceTag.normalizeName(null)).isInstanceOf(IllegalArgumentException.class); }
    }

    private ResourceTag createTag(final Long id, final String name) {
        final ResourceTag tag = new ResourceTag();
        tag.setId(id);
        tag.setName(name);
        tag.setSubject(subject);
        return tag;
    }
}
