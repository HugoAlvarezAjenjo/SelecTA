package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("User Registration")
    class RegistrationTests {
        @Test
        @DisplayName("Should encode password and save user")
        void shouldRegisterUser() {
            // Given
            User user = new Student();
            user.setPassword("rawPassword");
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            User registeredUser = userService.registerUser(user);

            // Then
            assertEquals("encodedPassword", user.getPassword());
            verify(passwordEncoder).encode("rawPassword");
            verify(userRepository).save(user);
            assertNotNull(registeredUser);
        }
    }

    @Nested
    @DisplayName("User Retrieval")
    class RetrievalTests {
        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // Given
            Long userId = 1L;
            User user = new Student();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            Optional<User> foundUser = userService.getUserById(userId);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(user, foundUser.get());
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void shouldReturnEmptyWhenUserNotFoundById() {
            // Given
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<User> foundUser = userService.getUserById(1L);

            // Then
            assertFalse(foundUser.isPresent());
        }
    }

    @Nested
    @DisplayName("Spring Security UserDetails Loading")
    class UserDetailsLoadingTests {
        @Test
        @DisplayName("Should load UserDetails by email")
        void shouldLoadUserByUsername() {
            // Given
            String email = "test@example.com";
            User user = new Student();
            user.setEmail(email);
            user.setPassword("hashedPassword");
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When
            UserDetails userDetails = userService.loadUserByUsername(email);

            // Then
            assertEquals(email, userDetails.getUsername());
            assertEquals("hashedPassword", userDetails.getPassword());
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void shouldThrowExceptionWhenEmailNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent@example.com"));
        }

        @Test
        @DisplayName("Should throw exception when user is not approved")
        void shouldThrowExceptionWhenUserNotApproved() {
            // Given
            String email = "pending@example.com";
            User user = new Teacher();
            user.setEmail(email);
            user.setPassword("hashedPassword");
            user.setApproved(false);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));
        }
    }

    @Nested
    @DisplayName("Current User Context")
    class CurrentUserContextTests {
        @Mock
        private SecurityContext securityContext;
        @Mock
        private Authentication authentication;
        @Mock
        private UserDetails userDetails;

        @BeforeEach
        void setUp() {
            SecurityContextHolder.setContext(securityContext);
        }

        @Test
        @DisplayName("Should retrieve current logged-in user")
        void shouldGetCurrentUser() {
            // Given
            String email = "current@example.com";
            User user = new Student();
            user.setEmail(email);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When
            User currentUser = userService.getCurrentUser();

            // Then
            assertNotNull(currentUser);
            assertEquals(email, currentUser.getEmail());
        }

        @Test
        @DisplayName("Should return null if no user is logged in")
        void shouldReturnNullWhenNoUserLoggedIn() {
            // Given - isAuthenticated() defaults to false, so getCurrentUser returns null early
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When
            User currentUser = userService.getCurrentUser();

            // Then
            assertNull(currentUser);
        }
    }

    @Nested
    @DisplayName("User Approval Management")
    class ApprovalTests {

        @Test
        @DisplayName("Should return pending users")
        void shouldReturnPendingUsers() {
            // Given
            Teacher pending1 = new Teacher();
            pending1.setId(1L);
            pending1.setApproved(false);
            Teacher pending2 = new Teacher();
            pending2.setId(2L);
            pending2.setApproved(false);
            when(userRepository.findByApprovedFalse()).thenReturn(List.of(pending1, pending2));

            // When
            List<User> pendingUsers = userService.getPendingUsers();

            // Then
            assertEquals(2, pendingUsers.size());
            verify(userRepository).findByApprovedFalse();
        }

        @Test
        @DisplayName("Should return all users ordered by ID")
        void shouldReturnAllUsers() {
            // Given
            Student student = new Student();
            student.setId(1L);
            Teacher teacher = new Teacher();
            teacher.setId(2L);
            when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of(student, teacher));

            // When
            List<User> allUsers = userService.getAllUsers();

            // Then
            assertEquals(2, allUsers.size());
            verify(userRepository).findAllByOrderByIdAsc();
        }

        @Test
        @DisplayName("Should approve a user")
        void shouldApproveUser() {
            // Given
            Teacher teacher = new Teacher();
            teacher.setId(1L);
            teacher.setApproved(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(userRepository.save(any(User.class))).thenReturn(teacher);

            // When
            userService.approveUser(1L);

            // Then
            assertTrue(teacher.isApproved());
            verify(userRepository).save(teacher);
        }

        @Test
        @DisplayName("Should throw when approving non-existent user")
        void shouldThrowWhenApprovingNonExistentUser() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> userService.approveUser(999L));
        }

        @Test
        @DisplayName("Should reject and delete a user")
        void shouldRejectUser() {
            // Given
            Teacher teacher = new Teacher();
            teacher.setId(1L);
            teacher.setEmail("rejected@example.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));

            // When
            userService.rejectUser(1L);

            // Then
            verify(userRepository).delete(teacher);
        }

        @Test
        @DisplayName("Should throw when rejecting non-existent user")
        void shouldThrowWhenRejectingNonExistentUser() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> userService.rejectUser(999L));
        }

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertTrue(userService.existsByEmail("existing@example.com"));
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

            // When & Then
            assertFalse(userService.existsByEmail("new@example.com"));
        }
    }
}
