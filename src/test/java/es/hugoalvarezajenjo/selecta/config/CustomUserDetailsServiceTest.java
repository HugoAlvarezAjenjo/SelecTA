package es.hugoalvarezajenjo.selecta.config;

import es.hugoalvarezajenjo.selecta.services.user.Student;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

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
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

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
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("nonexistent@example.com"));
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
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }
}
