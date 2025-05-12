-- MySQL version of the school database schema
-- This script creates the necessary database and tables for the school management system.
-- It uses IF NOT EXISTS to avoid errors if the database or tables already exist.
-- This version also includes dummy data for testing purposes.

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS school_db;

-- Use the created or existing database
USE school_db;

-- Users table
-- Stores user login information and roles.
-- Links to the teachers table for teacher users.
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    -- In a real application, passwords should be hashed (e.g., using BCrypt) and stored securely.
    -- For this example, we are storing plain text passwords.
    password VARCHAR(100) NOT NULL,
    -- Role can be 'Admin' or 'Teacher'. More roles can be added as needed.
    role ENUM('Admin', 'Teacher') NOT NULL,
    -- Optional link to the teachers table for users with the 'Teacher' role.
    -- ON DELETE SET NULL means if a teacher record is deleted, the teacher_id in users will become NULL.
    teacher_id INT UNIQUE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

-- Teachers table
-- Stores information about teachers.
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(100) -- The primary subject the teacher teaches.
);

-- Students table
-- Stores information about students.
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    grade_level VARCHAR(20), -- e.g., '1st Grade', 'High School Senior'
    -- Added a status column for potential future use (e.g., 'Active', 'Graduated', 'Left')
    status VARCHAR(50) DEFAULT 'Active'
);

-- Classes table
-- Stores information about the classes offered.
CREATE TABLE IF NOT EXISTS classes (
    class_id INT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(50) UNIQUE NOT NULL,
    grade_level VARCHAR(20), -- The grade level associated with the class.
    -- Added a fee column for potential future fee management features
    fee INT DEFAULT 0
);

-- Subjects table
-- Stores information about the subjects taught.
CREATE TABLE IF NOT EXISTS subjects (
    subject_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(100) UNIQUE NOT NULL
);

-- Class Assignments table
-- Links classes, teachers, and subjects to define which teacher teaches which subject in which class.
CREATE TABLE IF NOT EXISTS class_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    teacher_id INT NOT NULL,
    subject_id INT NOT NULL,
    -- Ensures that a specific combination of class, teacher, and subject is unique.
    UNIQUE (class_id, teacher_id, subject_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

-- Enrollments table
-- Links students to classes to track which students are enrolled in which classes.
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    class_id INT NOT NULL,
    -- Ensures a student is enrolled in a specific class only once.
    UNIQUE (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
);

-- Grades table
-- Stores grades for students in specific subjects and terms.
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    -- Links to the enrollment table to identify the student and class.
    enrollment_id INT NOT NULL,
    -- Links to the subjects table to identify the subject.
    subject_id INT NOT NULL,
    -- Score is a decimal between 0 and 100.
    score DECIMAL(5,2) CHECK (score >= 0 AND score <= 100),
    comments TEXT,
    term VARCHAR(50) NOT NULL, -- e.g., 'Term 1', 'Semester 2', 'Final'
    date_recorded DATE, -- The date the grade was recorded.
    -- Ensures a unique grade entry for a student in a specific subject and term.
    UNIQUE (enrollment_id, subject_id, term),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

-- Fees table (Added for fee management)
-- Tracks fee payments for students per term.
CREATE TABLE IF NOT EXISTS fees (
    fee_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    class_id INT NOT NULL, -- Linking fee to the class the student is enrolled in for a specific term
    term VARCHAR(50) NOT NULL,
    amount_due DECIMAL(10,2) NOT NULL, -- Total fee amount due for the term
    amount_paid DECIMAL(10,2) DEFAULT 0.00, -- Total amount paid for the term
    date_last_paid DATE, -- Date of the last payment
    -- Ensures a unique fee record for a student in a specific class and term.
    UNIQUE (student_id, class_id, term),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
);

-- --------------------
-- DUMMY DATA INSERTS
-- --------------------

-- Insert default admin and teacher logins (using INSERT IGNORE to avoid duplicates on re-run)
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', 'adminpass', 'Admin'),
('teacher', 'teacherpass', 'Teacher');

-- Insert dummy teachers
INSERT IGNORE INTO teachers (teacher_id, name, subject) VALUES
(1, 'Mr. Smith', 'Math'),
(2, 'Ms. Johnson', 'Science'),
(3, 'Mrs. Davis', 'English');

-- Link the default teacher user to the actual teacher record for 'Mr. Smith'
-- This is important for the TeacherPanel to retrieve data specific to the logged-in teacher.
UPDATE users SET teacher_id = (SELECT teacher_id FROM teachers WHERE name = 'Mr. Smith') WHERE username = 'teacher';

-- Insert dummy classes
INSERT IGNORE INTO classes (class_id, class_name, grade_level, fee) VALUES
(1, 'Grade 1A', '1st Grade', 1500),
(2, 'Grade 2B', '2nd Grade', 1600),
(3, 'Grade 10 Science', '10th Grade', 2000),
(4, 'Grade 10 Math', '10th Grade', 2000),
(5, 'Grade 10 English', '10th Grade', 2000);


-- Insert dummy subjects
INSERT IGNORE INTO subjects (subject_id, subject_name) VALUES
(1, 'Mathematics'),
(2, 'Science'),
(3, 'English'),
(4, 'History'),
(5, 'Art');

-- Insert dummy students
INSERT IGNORE INTO students (student_id, name, grade_level, status) VALUES
(1, 'Alice Wonderland', '1st Grade', 'Active'),
(2, 'Bob The Builder', '1st Grade', 'Active'),
(3, 'Charlie Chaplin', '2nd Grade', 'Active'),
(4, 'Diana Prince', '10th Grade', 'Active'),
(5, 'Ethan Hunt', '10th Grade', 'Active');


-- Insert dummy class assignments
-- Mr. Smith (ID 1) teaches Math (ID 1) in Grade 1A (ID 1) and Grade 10 Math (ID 4)
-- Ms. Johnson (ID 2) teaches Science (ID 2) in Grade 2B (ID 2) and Grade 10 Science (ID 3)
-- Mrs. Davis (ID 3) teaches English (ID 3) in Grade 1A (ID 1) and Grade 10 English (ID 5)
INSERT IGNORE INTO class_assignments (class_id, teacher_id, subject_id) VALUES
(1, 1, 1), -- Grade 1A, Mr. Smith, Math
(4, 1, 1), -- Grade 10 Math, Mr. Smith, Math
(2, 2, 2), -- Grade 2B, Ms. Johnson, Science
(3, 2, 2), -- Grade 10 Science, Ms. Johnson, Science
(1, 3, 3), -- Grade 1A, Mrs. Davis, English
(5, 3, 3); -- Grade 10 English, Mrs. Davis, English


-- Insert dummy enrollments
-- Alice (ID 1) is in Grade 1A (ID 1)
-- Bob (ID 2) is in Grade 1A (ID 1)
-- Charlie (ID 3) is in Grade 2B (ID 2)
-- Diana (ID 4) is in Grade 10 Science (ID 3), Grade 10 Math (ID 4), Grade 10 English (ID 5)
-- Ethan (ID 5) is in Grade 10 Science (ID 3), Grade 10 Math (ID 4), Grade 10 English (ID 5)
INSERT IGNORE INTO enrollments (student_id, class_id) VALUES
(1, 1),
(2, 1),
(3, 2),
(4, 3), -- Diana in Grade 10 Science
(4, 4), -- Diana in Grade 10 Math
(4, 5), -- Diana in Grade 10 English
(5, 3), -- Ethan in Grade 10 Science
(5, 4), -- Ethan in Grade 10 Math
(5, 5); -- Ethan in Grade 10 English


-- Insert dummy grades
-- Grades for Alice (Enrollment ID for Alice in Grade 1A is 1)
INSERT IGNORE INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES
(1, 1, 85.5, 'Good progress', 'Term 1', '2023-11-15'), -- Alice, Math
(1, 3, 92.0, 'Excellent', 'Term 1', '2023-11-15'); -- Alice, English

-- Grades for Bob (Enrollment ID for Bob in Grade 1A is 2)
INSERT IGNORE INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES
(2, 1, 78.0, 'Needs practice', 'Term 1', '2023-11-16'), -- Bob, Math
(2, 3, 88.5, 'Well done', 'Term 1', '2023-11-16'); -- Bob, English

-- Grades for Charlie (Enrollment ID for Charlie in Grade 2B is 3)
INSERT IGNORE INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES
(3, 2, 95.0, 'Outstanding!', 'Term 1', '2023-11-17'); -- Charlie, Science

-- Grades for Diana (Enrollment IDs: Grade 10 Sci=4, Math=5, English=6)
INSERT IGNORE INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES
(4, 2, 88.0, 'Solid understanding', 'Semester 1', '2024-01-20'), -- Diana, Science
(5, 1, 75.0, 'Could improve on algebra', 'Semester 1', '2024-01-20'), -- Diana, Math
(6, 3, 91.0, 'Great essay', 'Semester 1', '2024-01-20'); -- Diana, English

-- Grades for Ethan (Enrollment IDs: Grade 10 Sci=7, Math=8, English=9)
INSERT IGNORE INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES
(7, 2, 82.0, 'Good effort', 'Semester 1', '2024-01-21'), -- Ethan, Science
(8, 1, 90.0, 'Excellent problem solver', 'Semester 1', '2024-01-21'), -- Ethan, Math
(9, 3, 85.0, 'Good reading comprehension', 'Semester 1', '2024-01-21'); -- Ethan, English


-- Insert dummy fees
-- Assuming fees are per class per term
-- Alice (ID 1) in Grade 1A (ID 1), Term 1
INSERT IGNORE INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES
(1, 1, 'Term 1', 1500.00, 1500.00, '2023-10-01'); -- Paid in full

-- Bob (ID 2) in Grade 1A (ID 1), Term 1
INSERT IGNORE INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES
(2, 1, 'Term 1', 1500.00, 1000.00, '2023-10-05'); -- Partially paid

-- Charlie (ID 3) in Grade 2B (ID 2), Term 1
INSERT IGNORE INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES
(3, 2, 'Term 1', 1600.00, 1600.00, '2023-10-10'); -- Paid in full

-- Diana (ID 4) in Grade 10 Science (ID 3), Semester 1
INSERT IGNORE INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES
(4, 3, 'Semester 1', 2000.00, 2000.00, '2024-01-01'); -- Paid in full

-- Ethan (ID 5) in Grade 10 Math (ID 4), Semester 1
INSERT IGNORE INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES
(5, 4, 'Semester 1', 2000.00, 0.00, NULL); -- Not paid yet


--Admin:
--Username: admin
--Password: adminpass

--Teacher:
--Username: teacher
--Password: teacherpass
