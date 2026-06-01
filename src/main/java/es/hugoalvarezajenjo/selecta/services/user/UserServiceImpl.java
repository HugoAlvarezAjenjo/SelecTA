package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.repository.SubjectRepository;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserAuthentication {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

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
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No authentication found or not authenticated");
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            log.debug("Principal is not UserDetails instance");
            return null;
        }

        return this.userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }

    @Override
    public List<User> getPendingUsers() {
        return this.userRepository.findByApprovedFalse();
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public void approveUser(final Long userId) {
        final User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setApproved(true);
        this.userRepository.save(user);
        log.info("User {} approved by admin", user.getEmail());
    }

    @Override
    @Transactional
    public void rejectUser(final Long userId) {
        final User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        log.info("User {} rejected and deleted by admin", user.getEmail());
        this.userRepository.delete(user);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public List<User> getApprovedTeachers() {
        return this.userRepository.findByRoleAndApprovedTrue(UserRole.TEACHER);
    }
}
