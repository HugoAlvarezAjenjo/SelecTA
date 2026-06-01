-- =========================
-- SUBJECTS (UPM Ingeniería del Software)
-- =========================

INSERT INTO subject (id, name, description, credits, discontinued) VALUES
(1, 'Álgebra', 'Fundamentos de álgebra lineal para ingeniería', 6, false),
(2, 'Análisis Matemático', 'Cálculo diferencial e integral', 6, false),
(3, 'Matemática Discreta', 'Lógica y estructuras discretas', 6, false),
(4, 'Fundamentos de la Programación', 'Introducción a la programación', 6, false),
(5, 'Programación Orientada a Objetos', 'Programación con objetos en Java', 6, false),
(6, 'Estructuras de Datos', 'Listas, árboles, grafos y estructuras avanzadas', 6, false),
(7, 'Estructura de Computadores', 'Arquitectura de computadores', 6, false),
(8, 'Fundamentos Físicos y Tecnológicos de la Informática', 'Bases físicas de la informática', 6, false),
(9, 'Fundamentos de Economía y Empresa', 'Introducción a la empresa', 6, false),
(10, 'Aspectos Jurídicos, Éticos y Profesionales', 'Ética profesional en informática', 6, false),

(11, 'Algorítmica y Complejidad', 'Análisis de algoritmos', 6, false),
(12, 'Estadística', 'Probabilidad y estadística', 6, false),
(13, 'Ingeniería de Requisitos', 'Captura de requisitos software', 6, false),
(14, 'Sistemas Operativos', 'Gestión de procesos y memoria', 6, false),
(15, 'Bases de Datos', 'Modelado relacional y SQL', 6, false),
(16, 'Bases de Datos Avanzadas', 'Optimización y diseño avanzado', 6, false),
(17, 'Seguridad de la Información', 'Principios de ciberseguridad', 6, false),
(18, 'Interacción Persona-Máquina', 'UX/UI y diseño centrado en usuario', 6, false),
(19, 'POO Avanzada', 'Patrones de diseño y arquitectura', 6, false),
(20, 'Lenguajes de Programación', 'Paradigmas de programación', 6, false),

(21, 'Arquitectura de Software', 'Diseño de sistemas software', 6, false),
(22, 'Construcción de Software', 'Desarrollo de aplicaciones', 6, false),
(23, 'Integración de Sistemas', 'Integración de servicios', 6, false),
(24, 'Redes de Computadores', 'Protocolos y redes', 6, false),
(25, 'Testing y Validación', 'Pruebas de software', 6, false),
(26, 'Calidad del Software', 'Métricas y calidad', 6, false),
(27, 'Gestión de Proyectos', 'PMBOK aplicado a software', 6, false),
(28, 'Seguridad Avanzada', 'Seguridad en sistemas complejos', 6, false),
(29, 'Inglés Técnico', 'Comunicación profesional en inglés', 6, false),

(30, 'Trabajo Fin de Grado', 'Proyecto final del grado', 12, false),

-- Optativas
(31, 'Sistemas Inteligentes', 'IA básica y agentes', 6, false),
(32, 'Data Mining', 'Minería de datos', 6, false),
(33, 'Big Data', 'Procesamiento de datos masivos', 6, false),
(34, 'Web Semántica', 'Ontologías y datos estructurados', 6, false),
(35, 'Programación Concurrente', 'Threads y paralelismo', 6, false),
(36, 'Compiladores', 'Procesadores de lenguajes', 6, false),
(37, 'Sistemas Distribuidos', 'Arquitecturas distribuidas', 6, false),
(38, 'Cloud Computing', 'Computación en la nube', 6, false),
(39, 'Algoritmos Avanzados', 'Optimización algorítmica', 6, false),
(40, 'Teoría de la Información', 'Codificación y entropía', 6, false),
(41, 'Simulación', 'Modelado de sistemas', 6, false),
(42, 'Gobierno TI', 'IT governance', 6, false),
(43, 'GPS y Sistemas de Posicionamiento', 'Tecnologías de localización', 4, false),
(44, 'Emprendimiento', 'Creación de startups', 4, false),
(45, 'Modelos de Negocio', 'Business design', 4, false),
(46, 'Liderazgo', 'Gestión de equipos', 4, false),
(47, 'Gestión de Proyectos Avanzada', 'Gestión de portfolios', 6, false),
(48, 'Comunicación Técnica', 'Presentaciones técnicas', 3, false),
(49, 'Traducción Técnica', 'Documentación técnica', 4, false),
(50, 'Accesibilidad', 'Diseño inclusivo', 4, false),
(51, 'Multimedia', 'Tecnologías multimedia', 4, false),
(52, 'Prácticas Externas', 'Prácticas en empresa', 18, false);

-- =========================
-- LANGUAGES
-- =========================
INSERT INTO subject_languages (subject_id, languages) VALUES
(1,'SPANISH'),(2,'SPANISH'),(3,'SPANISH'),(4,'SPANISH'),(5,'SPANISH'),
(6,'SPANISH'),(7,'SPANISH'),(8,'SPANISH'),(9,'SPANISH'),(10,'SPANISH'),
(11,'SPANISH'),(12,'SPANISH'),(13,'SPANISH'),(14,'SPANISH'),(15,'SPANISH'),
(16,'SPANISH'),(17,'SPANISH'),(18,'SPANISH'),(19,'SPANISH'),(20,'SPANISH'),
(21,'SPANISH'),(22,'SPANISH'),(23,'SPANISH'),(24,'SPANISH'),(25,'SPANISH'),
(26,'SPANISH'),(27,'SPANISH'),(28,'SPANISH'),(29,'ENGLISH'),
(30,'SPANISH'),
(31,'SPANISH'),(32,'SPANISH'),(33,'SPANISH'),(34,'SPANISH'),(35,'SPANISH'),
(36,'SPANISH'),(37,'SPANISH'),(38,'SPANISH'),(39,'SPANISH'),(40,'SPANISH'),
(41,'SPANISH'),(42,'SPANISH'),(43,'SPANISH'),(44,'SPANISH'),(45,'SPANISH'),
(46,'SPANISH'),(47,'SPANISH'),(48,'SPANISH'),(49,'SPANISH'),(50,'SPANISH'),
(51,'SPANISH'),(52,'SPANISH');

-- =========================
-- SEMESTERS
-- =========================
INSERT INTO subject_semesters (subject_id, semesters) VALUES
(1,'FIRST'),(2,'FIRST'),(3,'FIRST'),(4,'FIRST'),(5,'FIRST'),
(6,'SECOND'),(7,'SECOND'),(8,'SECOND'),(9,'SECOND'),(10,'SECOND'),
(11,'THIRD'),(12,'THIRD'),(13,'THIRD'),(14,'THIRD'),(15,'THIRD'),
(16,'FOURTH'),(17,'FOURTH'),(18,'FOURTH'),(19,'FOURTH'),(20,'FOURTH'),
(21,'FIFTH'),(22,'FIFTH'),(23,'FIFTH'),(24,'FIFTH'),(25,'FIFTH'),
(26,'SIXTH'),(27,'SIXTH'),(28,'SIXTH'),(29,'SIXTH'),
(30,'EIGHTH'),
(31,'SEVENTH'),(32,'SEVENTH'),(33,'SEVENTH'),(34,'SEVENTH'),
(35,'SEVENTH'),(36,'SEVENTH'),(37,'SEVENTH'),(38,'SEVENTH'),
(39,'SEVENTH'),(40,'SEVENTH'),(41,'SEVENTH'),(42,'SEVENTH'),
(43,'SEVENTH'),(44,'SEVENTH'),(45,'SEVENTH'),(46,'SEVENTH'),
(47,'SEVENTH'),(48,'SEVENTH'),(49,'SEVENTH'),(50,'SEVENTH'),
(51,'SEVENTH'),(52,'EIGHTH');

-- =========================
-- TAGS LIMPIOS Y REUTILIZABLES
-- =========================

INSERT INTO subject_tags (subject_id, tags) VALUES

-- 1º CURSO
(1,'Matemáticas'),(1,'Álgebra'),
(2,'Matemáticas'),(2,'Cálculo'),
(3,'Matemáticas'),(3,'Lógica'),
(4,'Programación'),
(5,'Programación'),(5,'POO'),
(6,'Programación'),(6,'EstructurasDatos'),
(7,'Hardware'),(7,'ArquitecturaComputadores'),
(8,'Física'),
(9,'Empresa'),
(10,'Ética'),

-- 2º CURSO
(11,'Algoritmos'),(11,'Programación'),
(12,'Matemáticas'),(12,'Estadística'),
(13,'SoftwareEngineering'),(13,'Modelado'),
(14,'SistemasOperativos'),
(15,'BasesDeDatos'),
(16,'BasesDeDatos'),
(17,'Seguridad'),
(18,'UX'),
(19,'Programación'),(19,'POO'),
(20,'Lenguajes'),

-- 3º CURSO
(21,'SoftwareEngineering'),(21,'ArquitecturaSoftware'),
(22,'SoftwareEngineering'),
(23,'IntegraciónSistemas'),
(24,'Redes'),
(25,'Testing'),(25,'SoftwareEngineering'),
(26,'CalidadSoftware'),
(27,'GestiónProyectos'),
(28,'Seguridad'),
(29,'Inglés'),

-- 4º CURSO
(30,'TFG'),

-- OPTATIVAS
(31,'IA'),
(32,'DataScience'),
(33,'BigData'),
(34,'WebSemántica'),
(35,'Programación'),
(36,'Compiladores'),
(37,'SistemasDistribuidos'),
(38,'Cloud'),
(39,'Algoritmos'),
(40,'TeoríaInformación'),
(41,'Simulación'),
(42,'IT'),
(43,'SistemasEmbebidos'),
(44,'Emprendimiento'),
(45,'Negocio'),
(46,'Gestión'),
(47,'GestiónProyectos'),
(48,'Comunicación'),
(49,'Comunicación'),
(50,'UX'),
(51,'Multimedia'),
(52,'Prácticas');
-- =========================
-- RESOURCES (1 por asignatura)
-- =========================
INSERT INTO subject_resource
(subject_id,name,description,creation_date,type,language,original_name,is_private,official)
VALUES
(1,'Guía Álgebra','Material base Álgebra','2025-01-01','NOTES','SPANISH','algebra.pdf',false,true),
(2,'Guía Análisis','Material base','2025-01-01','NOTES','SPANISH','analisis.pdf',false,true),
(3,'Guía Discreta','Material base','2025-01-01','NOTES','SPANISH','discreta.pdf',false,true),
(4,'Intro Programación','Java básico','2025-01-01','VIDEO','SPANISH','fp.mp4',false,true),
(5,'POO Java','Clases y objetos','2025-01-01','NOTES','SPANISH','poo.pdf',false,true),
(6,'Estructuras','Listas y árboles','2025-01-01','NOTES','SPANISH','ed.pdf',false,true),
(7,'Arquitectura','CPU y memoria','2025-01-01','NOTES','SPANISH','cpu.pdf',false,true),
(8,'Física','Bases físicas','2025-01-01','NOTES','SPANISH','fisica.pdf',false,true),
(9,'Empresa','Economía básica','2025-01-01','NOTES','SPANISH','empresa.pdf',false,true),
(10,'Ética','Profesión','2025-01-01','NOTES','SPANISH','etica.pdf',false,true),

(11,'Algoritmos','Complejidad','2025-01-01','NOTES','SPANISH','algo.pdf',false,true),
(12,'Estadística','Probabilidad','2025-01-01','NOTES','SPANISH','stats.pdf',false,true),
(13,'Requisitos','Ingeniería software','2025-01-01','NOTES','SPANISH','req.pdf',false,true),
(14,'SO','Sistemas operativos','2025-01-01','VIDEO','SPANISH','so.mp4',false,true),
(15,'SQL','Bases de datos','2025-01-01','VIDEO','SPANISH','sql.mp4',false,true),
(16,'BD Avanzadas','Optimización','2025-01-01','NOTES','SPANISH','bda.pdf',false,true),
(17,'Seguridad','Criptografía','2025-01-01','NOTES','SPANISH','sec.pdf',false,true),
(18,'UX','Diseño interfaces','2025-01-01','NOTES','SPANISH','ux.pdf',false,true),
(19,'POO Avanzada','Patrones','2025-01-01','NOTES','SPANISH','patrones.pdf',false,true),
(20,'Lenguajes','Paradigmas','2025-01-01','NOTES','SPANISH','lang.pdf',false,true),

(21,'Arquitectura SW','Diseño sistemas','2025-01-01','NOTES','SPANISH','arch.pdf',false,true),
(22,'Construcción SW','Dev software','2025-01-01','NOTES','SPANISH','build.pdf',false,true),
(23,'Integración','APIs','2025-01-01','NOTES','SPANISH','api.pdf',false,true),
(24,'Redes','TCP/IP','2025-01-01','VIDEO','SPANISH','net.mp4',false,true),
(25,'Testing','QA','2025-01-01','NOTES','SPANISH','test.pdf',false,true),
(26,'Calidad','Métricas','2025-01-01','NOTES','SPANISH','quality.pdf',false,true),
(27,'Proyectos','PM','2025-01-01','NOTES','SPANISH','pm.pdf',false,true),
(28,'Seguridad SW','Security','2025-01-01','NOTES','SPANISH','sec2.pdf',false,true),
(29,'English','Communication','2025-01-01','NOTES','ENGLISH','eng.pdf',false,true),

(30,'TFG','Proyecto final','2025-01-01','NOTES','SPANISH','tfg.pdf',false,true),

(31,'IA','Sistemas inteligentes','2025-01-01','NOTES','SPANISH','ia.pdf',false,true),
(32,'Data Mining','Patrones','2025-01-01','NOTES','SPANISH','dm.pdf',false,true),
(33,'Big Data','Hadoop','2025-01-01','VIDEO','SPANISH','bd.mp4',false,true),
(34,'Web Semántica','Ontologías','2025-01-01','NOTES','SPANISH','sem.pdf',false,true),
(35,'Concurrente','Threads','2025-01-01','NOTES','SPANISH','conc.pdf',false,true),
(36,'Compiladores','Parsing','2025-01-01','NOTES','SPANISH','comp.pdf',false,true),
(37,'Distribuidos','Microservicios','2025-01-01','NOTES','SPANISH','dist.pdf',false,true),
(38,'Cloud','AWS','2025-01-01','VIDEO','SPANISH','cloud.mp4',false,true),
(39,'Algoritmos avanzados','Optimización','2025-01-01','NOTES','SPANISH','adv.pdf',false,true),
(40,'Info teoría','Entropía','2025-01-01','NOTES','SPANISH','info.pdf',false,true),
(41,'Simulación','Modelos','2025-01-01','NOTES','SPANISH','sim.pdf',false,true),
(42,'Gobierno TI','ITIL','2025-01-01','NOTES','SPANISH','itil.pdf',false,true),
(43,'GPS','Localización','2025-01-01','NOTES','SPANISH','gps.pdf',false,true),
(44,'Emprendimiento','Startups','2025-01-01','NOTES','SPANISH','start.pdf',false,true),
(45,'Business','Modelos','2025-01-01','NOTES','SPANISH','biz.pdf',false,true),
(46,'Liderazgo','Equipos','2025-01-01','NOTES','SPANISH','lead.pdf',false,true),
(47,'Gestión avanzada','Portfolios','2025-01-01','NOTES','SPANISH','port.pdf',false,true),
(48,'Comunicación','Presentaciones','2025-01-01','NOTES','SPANISH','com.pdf',false,true),
(49,'Traducción','Docs técnicos','2025-01-01','NOTES','SPANISH','trad.pdf',false,true),
(50,'Accesibilidad','UX inclusiva','2025-01-01','NOTES','SPANISH','acc.pdf',false,true),
(51,'Multimedia','Audio/video','2025-01-01','VIDEO','SPANISH','mm.mp4',false,true),
(52,'Prácticas','Empresa','2025-01-01','NOTES','SPANISH','prac.pdf',false,true);

-- =========================
-- USERS (password for all: 'password')
-- =========================

INSERT INTO accounts (username,password,email,user_type,role,approved) VALUES
('admin','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','admin@demo.com','ADMIN','ADMIN',true),
('hugo','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','hugo@demo.com','TEACHER','TEACHER',true),
('alicia','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','alicia@demo.com','TEACHER','TEACHER',true),
('pedro','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','pedro@demo.com','TEACHER','TEACHER',true),
('laura','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','laura@demo.com','TEACHER','TEACHER',false);

INSERT INTO accounts (username,password,email,user_type,role,approved,titulation) VALUES
('carlos','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','carlos@demo.com','STUDENT','STUDENT',true,'Grado en Ingeniería del Software'),
('maria','$2a$12$YdOWmWVPWm5rz5vIEPdwAeZUQ4VaYmJwVWwtKobtGFv5iB2qHX4aS','maria@demo.com','STUDENT','STUDENT',true,'Grado en Ingeniería del Software');

-- =========================
-- TEACHER RELATIONS
-- =========================

INSERT INTO subject_teachers (subject_id, teacher_id) VALUES
(1,2),(2,2),(3,3),(4,3),(5,4),(6,4),(7,2),(8,3),
(21,2),(22,3),(23,4),(24,2),(25,3),(26,4);

-- =========================
-- VOTES (demo random)
-- =========================

INSERT INTO resource_vote (resource_id,user_id,vote_type) VALUES
(1,6,'UPVOTE'),(2,6,'UPVOTE'),(3,6,'DOWNVOTE'),
(4,6,'UPVOTE'),(5,6,'UPVOTE'),
(1,7,'UPVOTE'),(2,7,'DOWNVOTE'),(4,7,'UPVOTE');
