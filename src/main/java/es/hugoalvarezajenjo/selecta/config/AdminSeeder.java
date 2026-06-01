package es.hugoalvarezajenjo.selecta.config;

import es.hugoalvarezajenjo.selecta.services.user.Admin;
import es.hugoalvarezajenjo.selecta.services.user.UserRole;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a default admin user on first startup in production.
 * Idempotent — skips if an admin with the configured email already exists.
 *
 * Configure via environment variables:
 *   ADMIN_USERNAME (default: admin)
 *   ADMIN_PASSWORD (required)
 *   ADMIN_EMAIL (default: admin@selecta.upm.es)
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL:admin@selecta.upm.es}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD not set — skipping admin seeding. Set the ADMIN_PASSWORD environment variable to create the default admin.");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists ({}), skipping seeding.", adminEmail);
            return;
        }

        Admin admin = new Admin();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail(adminEmail);
        admin.setRole(UserRole.ADMIN);
        admin.setApproved(true);
        userRepository.save(admin);

        log.info("Default admin created: {} ({})", adminUsername, adminEmail);
    }
}
