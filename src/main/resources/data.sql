-- Insert Subjects
INSERT INTO subject (id, name, description, credits) VALUES
                                                         (1, 'Mathematics', 'Advanced mathematics course covering algebra and calculus', 6),
                                                         (2, 'Physics', 'Fundamentals of physics including mechanics and thermodynamics', 6),
                                                         (3, 'Programming', 'Java programming basics and object-oriented design', 4),
                                                         (4, 'Database Systems', 'Introduction to database design and SQL', 5),
                                                         (5, 'Web Development', 'Front-end and back-end web development technologies', 5);

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

-- Insert Subject Resources
INSERT INTO subject_resource (id, subject_id, name, description, creation_date, type, language, url) VALUES
                                                                                                         (1, 1, 'Math Syllabus', 'Complete course syllabus for Mathematics', '2024-01-15', 'EXERCISE', 'ENGLISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (2, 1, 'Algebra Exercises', 'Practice exercises for algebra with solutions', '2024-01-20', 'EXERCISE', 'SPANISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (3, 2, 'Physics Lab Guide', 'Step-by-step laboratory guide for physics experiments', '2024-02-01', 'NOTES', 'ENGLISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (4, 3, 'Java Tutorial PDF', 'Complete introduction to Java programming language', '2024-02-10', 'NOTES', 'SPANISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (5, 3, 'OOP Presentation', 'Object-Oriented Programming concepts slides', '2024-02-15', 'PRESENTATION', 'SPANISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (6, 4, 'SQL Video Tutorial', 'Video tutorial on SQL queries and database design', '2024-02-20', 'VIDEO', 'ENGLISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (7, 5, 'React Official Docs', 'Link to React official documentation', '2024-02-25', 'EXTERNAL_RESOURCE', 'ENGLISH', 'https://reactjs.org/docs'),
                                                                                                         (8, 5, 'Spring Boot Guide', 'Comprehensive Spring Boot development guide', '2024-03-01', 'NOTES', 'SPANISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (9, 2, 'Mechanics Video Lectures', 'Video lectures on classical mechanics', '2024-03-05', 'VIDEO', 'ENGLISH', '/subject_resources/subject_resource.txt'),
                                                                                                         (10, 1, 'Calculus Presentation', 'Slides for calculus concepts and formulas', '2024-03-10', 'PRESENTATION', 'ENGLISH', '/subject_resources/subject_resource.txt');