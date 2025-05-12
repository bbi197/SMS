package com.school;

import com.formdev.flatlaf.FlatLightLaf;
import com.school.panels.AdminPanel;
import com.school.panels.LoginPanel;
import com.school.panels.TeacherPanel;
import com.school.panels.StudentPanel; // Import StudentPanel
import com.school.DatabaseUtility; // Ensure this import is present

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.io.File; // Keep for potential future use, though less critical for MySQL init
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream; // Import for reading config file

// Implement LoginListener to receive login success notifications
public class SchoolManagementApp extends JFrame implements LoginPanel.LoginListener {

    private JPanel mainPanel;
    private CardLayout cardLayout;

    // --- Database Configuration ---\
    // Assumed to be in the application's working directory or classpath
    private static final String CONFIG_FILE = "config.properties";
    // These will be populated from config.properties
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    // private boolean useMemory = false; // Less relevant for MySQL, but can be kept for flexibility

    // Panels - Keep references to the main panels
    private LoginPanel loginPanel;
    private AdminPanel adminPanel; // Keep reference for potential future use (e.g., state)
    private TeacherPanel teacherPanel; // Keep reference
    private StudentPanel studentPanel; // Keep reference


    // Constructor
    public SchoolManagementApp() {
        // Load database configuration from file FIRST
        loadDatabaseConfig();

        // Set the look and feel
        try {
            FlatLightLaf.setup(); // Use FlatLaf light theme
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
            // Fallback to default Swing L&F if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Failed to set default L&F: " + ex.getMessage());
            }
        }


        setTitle("School Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Set a reasonable default size
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize and add the Login Panel
        loginPanel = new LoginPanel(this); // Pass 'this' as the LoginListener
        mainPanel.add(loginPanel, "Login");

        add(mainPanel); // Add the main panel to the frame

        // Initially show the login panel
        cardLayout.show(mainPanel, "Login");
    }

    /**
     * Loads database configuration from the config.properties file.
     * Sets the loaded properties in the DatabaseUtility class.
     */
    private void loadDatabaseConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            dbUrl = prop.getProperty("db.url");
            dbUser = prop.getProperty("db.user");
            dbPassword = prop.getProperty("db.password");

            // Set the configuration in the DatabaseUtility
            DatabaseUtility.setDatabaseUrl(dbUrl);
            DatabaseUtility.setDatabaseCredentials(dbUser, dbPassword);

            // Basic validation
            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new IOException("Database configuration incomplete in " + CONFIG_FILE);
            }

            System.out.println("Database configuration loaded successfully.");

        } catch (IOException ex) {
            System.err.println("Error loading database configuration file: " + CONFIG_FILE);
            System.err.println("Please ensure '" + CONFIG_FILE + "' exists in the application directory " +
                               "and contains 'db.url', 'db.user', and 'db.password'.");
            ex.printStackTrace();
            // Exit the application if database configuration cannot be loaded
            JOptionPane.showMessageDialog(this,
                                          "Failed to load database configuration.\n" +
                                          "Please ensure 'config.properties' is present and correct.",
                                          "Configuration Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit application
        }
    }


    /**
     * Implementation of the LoginListener interface.
     * Called by the LoginPanel upon successful authentication.
     * Switches the view to the appropriate panel based on the user's role.
     *
     * @param role      The role of the logged-in user ("Admin", "Teacher", "Student").
     * @param userId    The user_id from the users table.
     * @param teacherId The teacher_id from the users table (or -1 if not a teacher).
     */
    @Override
    public void onLoginSuccess(String role, int userId, int teacherId) {
        // Remove any previously displayed role panel
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof AdminPanel || comp instanceof TeacherPanel || comp instanceof StudentPanel) {
                mainPanel.remove(comp);
            }
        }

        // Create and add the new panel based on the role
        switch (role) {
            case "Admin":
                adminPanel = new AdminPanel(this, userId); // Pass parent frame and user ID
                mainPanel.add(adminPanel, "Admin");
                cardLayout.show(mainPanel, "Admin");
                setTitle("School Management System - Admin Panel");
                break;
            case "Teacher":
                 // Ensure teacherId is valid before creating TeacherPanel
                if (teacherId == -1) {
                    showError("Teacher account not linked to a teacher record.");
                    // Stay on login panel or show error panel
                    cardLayout.show(mainPanel, "Login"); // Stay on login for now
                    setTitle("School Management System");
                    return; // Stop here
                }
                teacherPanel = new TeacherPanel(this, teacherId); // Pass parent frame and teacher ID
                mainPanel.add(teacherPanel, "Teacher");
                cardLayout.show(mainPanel, "Teacher");
                setTitle("School Management System - Teacher Panel");
                break;
            case "Student":
                 // For student, we need the student_id, which is not directly in the users table in schema.sql.
                 // We need to fetch the student_id from the students table based on the user_id
                 // assuming a link exists (e.g., a user_id column in the students table, or a separate mapping table).
                 // Based on the provided schema, there isn't a direct link from users to students.
                 // A common approach is to have a user_id in the students table or a student_id in the users table (like teacher_id).
                 // Let's assume for now that student_id is stored in the users table or can be derived.
                 // If schema doesn't support this, this part needs adjustment.
                 // Assuming student_id is needed and can be retrieved based on user_id (requires schema change or mapping)
                 // For now, let's pass the userId and the StudentPanel can fetch its own student_id if needed.
                 // A better approach would be to fetch student_id during login if the role is 'Student'.
                 // Let's modify the LoginPanel's User class and query slightly if possible, or handle it here.
                 // Let's adjust the LoginPanel to fetch student_id if role is Student.

                 // --- Re-evaluating based on LoginPanel update ---
                 // The updated LoginPanel's User class now includes teacherId, but not studentId.
                 // The schema also doesn't link users directly to students.
                 // To proceed, we need a way to link a 'Student' role user to a 'students' table record.
                 // Option 1: Add a `student_id` column to the `users` table (similar to `teacher_id`).
                 // Option 2: Add a `user_id` column to the `students` table.
                 // Option 3: Use a separate mapping table (e.g., `student_users`).
                 // Let's go with Option 1 for simplicity and consistency with `teacher_id`.
                 // *Requires a schema update and LoginPanel update to fetch `student_id` if role is 'Student'.*

                 // **TEMPORARY WORKAROUND:** For demonstration purposes with the current schema,
                 // we'll assume the student_id is the same as the user_id for 'Student' role.
                 // This is NOT a robust solution for a real application.
                 int studentId = userId; // !!! TEMPORARY ASSUMPTION !!!

                studentPanel = new StudentPanel(this, studentId); // Pass parent frame and student ID
                mainPanel.add(studentPanel, "Student");
                cardLayout.show(mainPanel, "Student");
                setTitle("School Management System - Student Panel");
                break;
            default:
                showError("Unknown user role: " + role);
                // Stay on login panel
                cardLayout.show(mainPanel, "Login");
                setTitle("School Management System");
                break;
        }
        // Ensure the new panel is visible and the UI is updated
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Helper method to show errors consistently using a JOptionPane.
     * Ensures the dialog is shown on the Event Dispatch Thread (EDT).
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    // Main method to start the application
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
             // Attempt to load FlatLaf Look and Feel first
             try {
                 FlatLightLaf.setup();
             } catch (Exception e) {
                 System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
                 // Fallback to default Swing L&F if FlatLaf fails
                 try {
                     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                 } catch (Exception ex) {
                     System.err.println("Failed to set default L&F: " + ex.getMessage());
                 }
             }
            new SchoolManagementApp().setVisible(true);
        });
    }

    /**
     * Handles the logout process.
     * Removes the current role-specific panel and shows the LoginPanel.
     */
    public void logout() {
        // Remove the currently displayed role panel
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            // Check if the component is one of the role-specific panels
            if (comp instanceof AdminPanel || comp instanceof TeacherPanel || comp instanceof StudentPanel) {
                mainPanel.remove(comp);
                // Optional: Dereference the panel to allow garbage collection
                if (comp instanceof AdminPanel) adminPanel = null;
                else if (comp instanceof TeacherPanel) teacherPanel = null;
                else if (comp instanceof StudentPanel) studentPanel = null;
                break; // Assuming only one role panel is visible at a time
            }
        }

        // Clear fields in the login panel and show it
        if (loginPanel != null) {
            loginPanel.clearFields();
        } else {
             // This case should ideally not happen if loginPanel is initialized correctly,
             // but as a fallback, try to find it in the components.
             for (Component component : mainPanel.getComponents()) {
                 if (component instanceof LoginPanel) {
                     loginPanel = (LoginPanel) component; // Re-assign the reference
                     loginPanel.clearFields();
                     break;
                 }
             }
        }

        cardLayout.show(mainPanel, "Login");
        setTitle("School Management System"); // Reset window title
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Interface for LoginPanel to notify SchoolManagementApp
    // This is defined within SchoolManagementApp to keep it coupled
    // with the class that needs to listen for login events.
    public interface LoginListener {
        void onLoginSuccess(String role, int userId, int teacherId); // Added teacherId
    }
}
