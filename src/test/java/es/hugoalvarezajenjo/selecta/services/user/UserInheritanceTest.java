package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({UserServiceImpl.class, UserInheritanceTest.TestConfig.class})
@Transactional
@DisplayName("User JPA Inheritance Integration Tests")
class UserInheritanceTest {

    /**
     * Provides only the PasswordEncoder bean needed for this test,
     * without loading the full SecurityConfig (which requires web context).
     */
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should persist and retrieve Student with correct type and titulation")
    void testSaveAndRetrieveStudent() {
        Student student = new Student();
        student.setEmail("student@example.com");
        student.setPassword("password");
        student.setUsername("Test Student");
        student.setTitulation("Software Engineering");

        userService.registerUser(student);

        Optional<User> retrieved = userRepository.findByEmail("student@example.com");
        assertTrue(retrieved.isPresent());
        assertInstanceOf(Student.class, retrieved.get());
        Student retrievedStudent = (Student) retrieved.get();
        assertEquals("Software Engineering", retrievedStudent.getTitulation());
        assertEquals(UserRole.STUDENT, retrievedStudent.getRole());
        assertEquals("Test Student", retrievedStudent.getUsername());
        assertNotNull(retrievedStudent.getId());
        assertTrue(passwordEncoder.matches("password", retrievedStudent.getPassword()));
    }

    @Test
    @DisplayName("Should persist and retrieve Teacher with correct type")
    void testSaveAndRetrieveTeacher() {
        Teacher teacher = new Teacher();
        teacher.setEmail("teacher@example.com");
        teacher.setPassword("password");
        teacher.setUsername("Test Teacher");

        userService.registerUser(teacher);

        Optional<User> retrieved = userRepository.findByEmail("teacher@example.com");
        assertTrue(retrieved.isPresent());
        assertInstanceOf(Teacher.class, retrieved.get());
        Teacher retrievedTeacher = (Teacher) retrieved.get();
        assertEquals(UserRole.TEACHER, retrievedTeacher.getRole());
        assertEquals("Test Teacher", retrievedTeacher.getUsername());
    }

    @Test
    @DisplayName("Should persist and retrieve Admin with correct type")
    void testSaveAndRetrieveAdmin() {
        Admin admin = new Admin();
        admin.setEmail("admin@example.com");
        admin.setPassword("password");
        admin.setUsername("Test Admin");

        userService.registerUser(admin);

        Optional<User> retrieved = userRepository.findByEmail("admin@example.com");
        assertTrue(retrieved.isPresent());
        assertInstanceOf(Admin.class, retrieved.get());
        Admin retrievedAdmin = (Admin) retrieved.get();
        assertEquals(UserRole.ADMIN, retrievedAdmin.getRole());
        assertEquals("Test Admin", retrievedAdmin.getUsername());
    }
}
