package es.hugoalvarezajenjo.selecta.services.resources;

import es.hugoalvarezajenjo.selecta.services.resources.repository.ResourceVoteRepository;
import es.hugoalvarezajenjo.selecta.services.resources.repository.SubjectResourceRepository;
import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceVoteServiceImplTest {

    @Mock
    private ResourceVoteRepository resourceVoteRepository;
    @Mock
    private SubjectResourceRepository subjectResourceRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ResourceVoteServiceImpl service;

    private Student student;
    private SubjectResource resource;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setEmail("carlos@example.com");

        resource = new SubjectResource();
        resource.setId(10L);
        resource.setName("Test Resource");
    }

    @Nested
    class ToggleVote {

        @Test
        void shouldCreateNewUpvoteWhenNoVoteExists() {
            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectResourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
            when(resourceVoteRepository.save(any(ResourceVote.class))).thenAnswer(inv -> inv.getArgument(0));
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(1L);
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(0L);

            final Map<String, Object> result = service.toggleVote(10L, VoteType.UPVOTE);

            assertThat(result.get("upvotes")).isEqualTo(1L);
            assertThat(result.get("downvotes")).isEqualTo(0L);
            assertThat(result.get("userVote")).isEqualTo("UPVOTE");

            final ArgumentCaptor<ResourceVote> captor = ArgumentCaptor.forClass(ResourceVote.class);
            verify(resourceVoteRepository).save(captor.capture());
            assertThat(captor.getValue().getVoteType()).isEqualTo(VoteType.UPVOTE);
            assertThat(captor.getValue().getUser()).isEqualTo(student);
            assertThat(captor.getValue().getResource()).isEqualTo(resource);
        }

        @Test
        void shouldCreateNewDownvoteWhenNoVoteExists() {
            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectResourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
            when(resourceVoteRepository.save(any(ResourceVote.class))).thenAnswer(inv -> inv.getArgument(0));
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(0L);
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(1L);

            final Map<String, Object> result = service.toggleVote(10L, VoteType.DOWNVOTE);

            assertThat(result.get("upvotes")).isEqualTo(0L);
            assertThat(result.get("downvotes")).isEqualTo(1L);
            assertThat(result.get("userVote")).isEqualTo("DOWNVOTE");
        }

        @Test
        void shouldRemoveVoteWhenSameTypeAlreadyExists() {
            final ResourceVote existingVote = new ResourceVote();
            existingVote.setId(5L);
            existingVote.setResource(resource);
            existingVote.setUser(student);
            existingVote.setVoteType(VoteType.UPVOTE);

            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectResourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingVote));
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(0L);
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(0L);

            final Map<String, Object> result = service.toggleVote(10L, VoteType.UPVOTE);

            assertThat(result.get("userVote")).isNull();
            verify(resourceVoteRepository).delete(existingVote);
            verify(resourceVoteRepository, never()).save(any());
        }

        @Test
        void shouldSwitchVoteWhenDifferentTypeExists() {
            final ResourceVote existingVote = new ResourceVote();
            existingVote.setId(5L);
            existingVote.setResource(resource);
            existingVote.setUser(student);
            existingVote.setVoteType(VoteType.UPVOTE);

            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectResourceRepository.findById(10L)).thenReturn(Optional.of(resource));
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingVote));
            when(resourceVoteRepository.save(any(ResourceVote.class))).thenAnswer(inv -> inv.getArgument(0));
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(0L);
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(1L);

            final Map<String, Object> result = service.toggleVote(10L, VoteType.DOWNVOTE);

            assertThat(result.get("userVote")).isEqualTo("DOWNVOTE");
            assertThat(existingVote.getVoteType()).isEqualTo(VoteType.DOWNVOTE);
            verify(resourceVoteRepository).save(existingVote);
            verify(resourceVoteRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWhenUserNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            assertThatThrownBy(() -> service.toggleVote(10L, VoteType.UPVOTE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("logged in");
        }

        @Test
        void shouldThrowWhenResourceNotFound() {
            when(userService.getCurrentUser()).thenReturn(student);
            when(subjectResourceRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.toggleVote(999L, VoteType.UPVOTE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Resource not found");
        }
    }

    @Nested
    class GetVoteCounts {

        @Test
        void shouldReturnUpvoteCount() {
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(5L);

            assertThat(service.getUpvoteCount(10L)).isEqualTo(5L);
        }

        @Test
        void shouldReturnDownvoteCount() {
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(3L);

            assertThat(service.getDownvoteCount(10L)).isEqualTo(3L);
        }

        @Test
        void shouldReturnZeroWhenNoVotes() {
            when(resourceVoteRepository.countByResourceIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(0L);

            assertThat(service.getUpvoteCount(10L)).isEqualTo(0L);
        }
    }

    @Nested
    class GetUserVote {

        @Test
        void shouldReturnUserVoteTypeWhenExists() {
            final ResourceVote vote = new ResourceVote();
            vote.setVoteType(VoteType.UPVOTE);

            when(userService.getCurrentUser()).thenReturn(student);
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.of(vote));

            assertThat(service.getUserVote(10L)).isEqualTo(VoteType.UPVOTE);
        }

        @Test
        void shouldReturnNullWhenNoVoteExists() {
            when(userService.getCurrentUser()).thenReturn(student);
            when(resourceVoteRepository.findByResourceIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

            assertThat(service.getUserVote(10L)).isNull();
        }

        @Test
        void shouldReturnNullWhenUserNotLoggedIn() {
            when(userService.getCurrentUser()).thenReturn(null);

            assertThat(service.getUserVote(10L)).isNull();
        }
    }
}
