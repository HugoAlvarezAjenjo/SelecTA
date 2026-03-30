package es.hugoalvarezajenjo.selecta.services.user;

import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserInheritanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testSaveAndRetrieveStudent() {
        Student student = new Student();
        student.setEmail("student@example.com");
        student.setPassword("password");
        student.setUsername("Test Student"); // Using username for name
        student.setTitulation("Software Engineering");

        userService.registerUser(student);

        Optional<User> retrieved = userRepository.findByEmail("student@example.com");
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof Student);
        Student retrievedStudent = (Student) retrieved.get();
        assertEquals("Software Engineering", retrievedStudent.getTitulation());
        assertEquals(UserRole.STUDENT, retrievedStudent.getRole());
        assertEquals("Test Student", retrievedStudent.getUsername());
        assertNotNull(retrievedStudent.getId()); // id is the internal id
        assertTrue(passwordEncoder.matches("password", retrievedStudent.getPassword()));
    }

    @Test
    void testSaveAndRetrieveTeacher() {
        Teacher teacher = new Teacher();
        teacher.setEmail("teacher@example.com");
        teacher.setPassword("password");
        teacher.setUsername("Test Teacher");

        userService.registerUser(teacher);

        Optional<User> retrieved = userRepository.findByEmail("teacher@example.com");
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof Teacher);
        Teacher retrievedTeacher = (Teacher) retrieved.get();
        assertEquals(UserRole.TEACHER, retrievedTeacher.getRole());
        assertEquals("Test Teacher", retrievedTeacher.getUsername());
    }

    @Test
    void testSaveAndRetrieveAdmin() {
        Admin admin = new Admin();
        admin.setEmail("admin@example.com");
        admin.setPassword("password");
        admin.setUsername("Test Admin");

        userService.registerUser(admin);

        Optional<User> retrieved = userRepository.findByEmail("admin@example.com");
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof Admin);
        Admin retrievedAdmin = (Admin) retrieved.get();
        assertEquals(UserRole.ADMIN, retrievedAdmin.getRole());
        assertEquals("Test Admin", retrievedAdmin.getUsername());
    }
}
