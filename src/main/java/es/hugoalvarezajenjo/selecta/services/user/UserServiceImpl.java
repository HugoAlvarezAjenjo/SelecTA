package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService, UserAuthentication, UserDetailsService {
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(final UserRepository userRepository, final SubjectRepository subjectRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(final User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(final Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    @Transactional
    public void toggleFavouriteSubject(final Long subjectId) {
        final User currentUser = this.getCurrentUser();
        if (!(currentUser instanceof Student student)) {
            return;
        }

        final Optional<Subject> subject = this.subjectRepository.findById(subjectId);
        if (subject.isEmpty()) {
            return;
        }

        if (student.getFavouriteSubjects().contains(subject.get())) {
            student.getFavouriteSubjects().remove(subject.get());
        } else {
            student.getFavouriteSubjects().add(subject.get());
        }

        this.userRepository.save(student);
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        log.info("SelecTA Log: Loading user by email: {}", email);
        final User user = this.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        log.info("SelecTA Log: Found user in DB: {}, role stored: {}", user.getEmail(), user.getRole());
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("SelecTA Log: No authentication found or not authenticated");
            return null;
        }

        Object principal = authentication.getPrincipal();
        log.info("SelecTA Log: Current principal type: {}, value: {}", principal.getClass().getName(), principal);
        
        if (!(principal instanceof UserDetails)) {
            log.warn("SelecTA Log: Principal is not UserDetails instance");
            return null;
        }

        UserDetails userDetails = (UserDetails) principal;
        log.info("SelecTA Log: Current user email from principal: {}", userDetails.getUsername());
        User user = this.getUserByEmail(userDetails.getUsername()).orElse(null);
        if (user != null) {
            log.info("SelecTA Log: User found in context: {}, actual class: {}, role in domain: {}", 
                user.getEmail(), user.getClass().getSimpleName(), user.getRole());
        }
        return user;
    }

    private Optional<User> getUserByEmail(final String email) {
        return this.userRepository.findByEmail(email);
    }
}
