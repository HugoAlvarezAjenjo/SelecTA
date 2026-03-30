package es.hugoalvarezajenjo.selecta.services.user;

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
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("unknown");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When
            User currentUser = userService.getCurrentUser();

            // Then
            assertNull(currentUser);
        }
    }
}
