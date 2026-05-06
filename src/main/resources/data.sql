-- Insert Subjects
INSERT INTO subject (id, name, description, credits, discontinued) VALUES
                                                         (1, 'Mathematics', 'Advanced mathematics course covering algebra and calculus', 6, false),
                                                         (2, 'Physics', 'Fundamentals of physics including mechanics and thermodynamics', 6, false),
                                                         (3, 'Programming', 'Java programming basics and object-oriented design', 4, false),
                                                         (4, 'Database Systems', 'Introduction to database design and SQL', 5, false),
                                                         (5, 'Web Development', 'Front-end and back-end web development technologies', 5, false);

-- Insert Subject languages (for the subject_languages join table)
INSERT INTO subject_languages (subject_id, languages) VALUES
                                                          (1, 'ENGLISH'),
                                                          (1, 'SPANISH'),
                                                          (2, 'ENGLISH'),
                                                          (3, 'SPANISH'),
                                                          (4, 'ENGLISH'),
                                                          (5, 'SPANISH');

-- Insert Subject semesters (for the subject_semesters join table)
INSERT INTO subject_semesters (subject_id, semesters) VALUES
                                                          (1, 'FIRST'),
                                                          (1, 'SECOND'),
                                                          (2, 'FIRST'),
                                                          (3, 'SECOND'),
                                                          (3, 'THIRD'),
                                                          (4, 'FOURTH'),
                                                          (5, 'FIFTH');

-- Insert Subject tags
INSERT INTO subject_tags (subject_id, tags) VALUES
                                                (1, 'Math'), (1, 'Science'), (1, 'Engineering'),
                                                (2, 'Physics'), (2, 'Science'), (2, 'Engineering'),
                                                (3, 'Computer Science'), (3, 'Software Engineering'), (3, 'Programming'),
                                                (4, 'Computer Science'), (4, 'Software Engineering'), (4, 'Data'),
                                                (5, 'Computer Science'), (5, 'Software Engineering'), (5, 'Web');

-- Insert Subject Resources
INSERT INTO subject_resource (subject_id, name, description, creation_date, type, language, original_name, is_private, official) VALUES
                                                                                                         (1, 'Math Syllabus', 'Complete course syllabus for Mathematics', '2024-01-15', 'EXERCISE', 'ENGLISH', 'math_syllabus.pdf', false, true),
                                                                                                         (1, 'Algebra Exercises', 'Practice exercises for algebra with solutions', '2024-01-20', 'EXERCISE', 'SPANISH', 'algebra_exercises.zip', false, true),
                                                                                                         (2, 'Physics Lab Guide', 'Step-by-step laboratory guide for physics experiments', '2024-02-01', 'NOTES', 'ENGLISH', 'lab_guide.pdf', false, true),
                                                                                                         (3, 'Java Tutorial PDF', 'Complete introduction to Java programming language', '2024-02-10', 'NOTES', 'SPANISH', 'java_tutorial.pdf', false, true),
                                                                                                         (3, 'OOP Presentation', 'Object-Oriented Programming concepts slides', '2024-02-15', 'PRESENTATION', 'SPANISH', 'oop_slides.pptx', false, true),
                                                                                                         (4, 'SQL Video Tutorial', 'Video tutorial on SQL queries and database design', '2024-02-20', 'VIDEO', 'ENGLISH', 'sql_tutorial.mp4', false, true),
                                                                                                         (5, 'React Official Docs', 'Link to React official documentation', '2024-02-25', 'EXTERNAL_RESOURCE', 'ENGLISH', 'react_docs.url', false, true),
                                                                                                         (5, 'Spring Boot Guide', 'Comprehensive Spring Boot development guide', '2024-03-01', 'NOTES', 'SPANISH', 'spring_guide.pdf', false, true),
                                                                                                         (2, 'Mechanics Video Lectures', 'Video lectures on classical mechanics', '2024-03-05', 'VIDEO', 'ENGLISH', 'mechanics.mp4', false, true),
                                                                                                         (1, 'Calculus Presentation', 'Slides for calculus concepts and formulas', '2024-03-10', 'PRESENTATION', 'ENGLISH', 'calculus.pptx', false, true);

-- Reset identity counters to avoid primary key violations with prepopulated data
ALTER TABLE subject ALTER COLUMN id RESTART WITH 6;

-- Insert Admin (using BCrypt for 'password')
INSERT INTO accounts (username, password, email, user_type, role, approved) VALUES
('admin', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'admin@selecta.com', 'ADMIN', 'ADMIN', true);

-- Insert Teachers (using BCrypt for 'password')
INSERT INTO accounts (username, password, email, user_type, role, approved) VALUES
('hugo', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'hugo@example.com', 'TEACHER', 'TEACHER', true),
('alicia', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'alicia@example.com', 'TEACHER', 'TEACHER', true);

-- Link Teachers to Subjects (admin=1, hugo=2, alicia=3)
INSERT INTO subject_teachers (subject_id, teacher_id) VALUES
(1, 2),
(3, 2),
(3, 3),
(5, 3);

-- Insert a pending teacher (awaiting admin approval)
INSERT INTO accounts (username, password, email, user_type, role, approved) VALUES
('pedro', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'pedro@example.com', 'TEACHER', 'TEACHER', false),
('laura', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'laura@example.com', 'TEACHER', 'TEACHER', false);

-- Insert Students (using BCrypt for 'password')
INSERT INTO accounts (username, password, email, user_type, role, approved, titulation) VALUES
('carlos', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'carlos@example.com', 'STUDENT', 'STUDENT', true, 'Grado en Ingeniería Informática'),
('maria', '$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS', 'maria@example.com', 'STUDENT', 'STUDENT', true, 'Grado en Ingeniería Informática');

-- Insert Resource Votes (student carlos=6, maria=7 voting on resources)
-- IDs: admin=1, hugo=2, alicia=3, pedro=4, laura=5, carlos=6, maria=7
INSERT INTO resource_vote (resource_id, user_id, vote_type) VALUES
(1, 6, 'UPVOTE'),
(2, 6, 'UPVOTE'),
(3, 6, 'DOWNVOTE'),
(4, 6, 'UPVOTE'),
(5, 6, 'UPVOTE'),
(1, 7, 'UPVOTE'),
(2, 7, 'DOWNVOTE'),
(4, 7, 'UPVOTE'),
(6, 7, 'UPVOTE');
