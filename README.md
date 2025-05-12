# School Management System (SMS)

A simple desktop application developed in Java Swing for managing school data, including students, teachers, classes, subjects, grades, and fees. The application uses a MySQL database for data storage.

## Features

* **User Authentication:** Login screen with different roles (Admin, Teacher, Student).
* **Admin Panel:**
    * Manage Students (Add, Update, Delete, View with pagination).
    * Manage Teachers (Add, Update, Delete, View with pagination).
    * Manage Classes (Add, Update, Delete, View with pagination).
    * Manage Subjects (Add, Update, Delete, View with pagination).
    * Manage Class Assignments (Assign teachers to classes and subjects).
    * Manage Enrollments (Enroll students in classes).
    * Generate Performance Reports (Filtered by Class, Subject, Term).
    * Student Promotion (Promote students from one class to another).
    * Manage Fees (Add, Update, Delete, View fee records).
* **Teacher Panel:**
    * Manage Grades (Add, Update, Delete, View grades for assigned classes/subjects).
    * View Reports (Generate performance reports for their assigned classes/subjects).
* **Student Panel:**
    * View Personal Information.
    * View Enrolled Classes.
    * View Grades (with filtering options).
    * View Fee Records (including balance).
* **Database Integration:** Uses MySQL to persist data.
* **Modern UI:** Utilizes the FlatLaf library for a clean, modern look and feel.
* **PDF Reports:** Capability to generate simple PDF reports (using iTextPDF).

## Prerequisites

Before you begin, ensure you have the following installed:

* **Java Development Kit (JDK):** Version 8 or higher.
* **Apache Maven:** For project build and dependency management.
* **MySQL Server:** A running instance of MySQL database.
* **MySQL Connector/J:** The JDBC driver for MySQL (handled by Maven).

## Setup Instructions

1.  **Clone or Download the Project:** Obtain the project files and navigate to the root directory (`SchoolManagementSystem/`).

2.  **Set up the Database:**
    * Open your MySQL client (e.g., MySQL Workbench, command line).
    * Execute the SQL script located at `database/schema.sql`. This will create the `school_db` database, tables, and insert some initial data.
    ```bash
    -- Example command in MySQL client:
    SOURCE path/to/your/SchoolManagementSystem/database/schema.sql;
    ```
    *Replace `path/to/your/SchoolManagementSystem/` with the actual path to your project directory.*

3.  **Configure Database Connection:**
    * Navigate to `src/main/resources/`.
    * Open the `config.properties` file.
    * Update the `db.url`, `db.user`, and `db.password` properties with your MySQL database connection details.

    ```properties
    # Database Configuration
    db.url=jdbc:mysql://localhost:3306/school_db # Update if your MySQL server is elsewhere or uses a different port/database name
    db.user=your_mysql_username # Replace with your MySQL username
    db.password=your_mysql_password # Replace with your MySQL password
    ```

## Building and Running

The project uses Maven for building.

1.  **Open a Terminal or Command Prompt:** Navigate to the root directory of the project (`SchoolManagementSystem/`).

2.  **Build the Project:** Run the Maven `install` goal. This will download dependencies, compile the code, and package the application.

    ```bash
    mvn clean install
    ```

3.  **Run the Application:**

    * If you configured the `maven-shade-plugin` in `pom.xml` (currently commented out) to create an executable JAR:
        ```bash
        java -jar target/SchoolManagementSystem-1.0-SNAPSHOT.jar
        ```
        *(Replace `SchoolManagementSystem-1.0-SNAPSHOT.jar` with the actual generated JAR filename).*

    * Alternatively, you can run directly using the Maven Exec plugin:
        ```bash
        mvn exec:java -Dexec.mainClass="com.school.Main"
        ```

4.  **Running from an IDE:**
    * Import the project into your favorite Java IDE (IntelliJ IDEA, Eclipse, NetBeans) as a Maven project.
    * Ensure the IDE is configured to use the correct JDK.
    * Make sure `config.properties` is in `src/main/resources/`.
    * Run the `com.school.Main` class directly from the IDE.

## Default Login Credentials

After setting up the database with `schema.sql`, you can use the following default credentials:

* **Admin:**
    * Username: `admin`
    * Password: `adminpass`
    * Role: `Admin`
* **Teacher:**
    * Username: `teacher`
    * Password: `teacherpass`
    * Role: `Teacher`
* **Student:**
    * Username: `student`
    * Password: `studentpass`
    * Role: `Student`

*(Note: You can modify these credentials or add more users by editing and re-running parts of the `schema.sql` script or by implementing user management features in the Admin panel).*

## Project Structure

SchoolManagementSystem/├── pom.xml├── README.md├── src/│   └── main/│       ├── java/│       │   └── com/│       │       └── school/│       │           ├── Main.java│       │           ├── SchoolManagementApp.java│       │           ├── DatabaseUtility.java│       │           └── panels/│       │               ├── AdminPanel.java│       │               ├── LoginPanel.java│       │               ├── StudentPanel.java│       │               └── TeacherPanel.java│       └── resources/│           └── config.properties└── database/└── schema.sql*(If you included `PdfReportGenerator.java` in a `utils` package, it would be under `src/main/java/com/school/utils/`)*

## Dependencies

Key dependencies managed by Maven (`pom.xml`):

* **MySQL Connector/J:** JDBC driver for MySQL database connectivity.
* **FlatLaf:** Provides a modern and customizable look and feel for the Swing GUI.
* **iTextPDF:** Library for generating PDF documents.

---
