package com.school.panels;

import com.school.DatabaseUtility; // Ensure this import is present
import com.school.SchoolManagementApp.LoginListener; // Import the LoginListener interface

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.concurrent.ExecutionException; // Import for SwingWorker

/**
 * LoginPanel provides the user interface for logging into the school management system.
 * It handles user authentication against the database and notifies a listener
 * upon successful login.
 */
public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox; // Added role selection
    private JLabel messageLabel; // Label to display messages (errors, loading)
    private LoginListener loginListener; // Listener for login success

    // --- Loading Indicator ---
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private JPanel loadingPanel; // Panel to hold loading indicator


    /**
     * Constructor for the LoginPanel.
     *
     * @param listener The LoginListener to notify upon successful login.
     */
    public LoginPanel(LoginListener listener) {
        this.loginListener = listener; // Store the listener
        setLayout(new GridBagLayout());
        setBackground(new Color(250, 250, 250)); // Light background
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Stretch horizontally

        // --- Title Label ---
        JLabel titleLabel = new JLabel("School Management Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span two columns
        add(titleLabel, gbc);

        // --- Username Field ---
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Reset gridwidth
        add(userLabel, gbc);

        usernameField = new JTextField(20); // Increased preferred size
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(usernameField, gbc);

        // --- Password Field ---
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passLabel, gbc);

        passwordField = new JPasswordField(20); // Increased preferred size
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        // --- Role Selection ---
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(roleLabel, gbc);

        String[] roles = {"Admin", "Teacher", "Student"}; // Define roles
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(roleComboBox, gbc);

        // --- Login Button ---
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 123, 255)); // Blue color
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        loginButton.putClientProperty("FlatLaf.styleClass", "accent"); // Use FlatLaf accent style

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Span two columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        add(loginButton, gbc);

        // --- Message Label (for errors/loading) ---
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED); // Default to red for error messages
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(messageLabel, gbc);

        // --- Loading Indicator ---
        setupLoadingIndicator(); // Setup the loading bar and label panel
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Do not stretch
        add(loadingPanel, gbc); // Add the loading panel to the layout


        // --- Action Listener for Login Button ---
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
    }

    /**
     * Sets up the loading indicator components (progress bar and label).
     */
    private void setupLoadingIndicator() {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Make it an indeterminate progress bar
        progressBar.putClientProperty("JProgressBar.arc", 999); // A large arc value makes it fully rounded

        loadingLabel = new JLabel("Logging in...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a panel to hold the progress bar and label
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(250, 250, 250)); // Match panel background
        loadingPanel.add(loadingLabel, BorderLayout.NORTH);
        loadingPanel.add(progressBar, BorderLayout.CENTER);

        // Initially hide the loading indicator
        progressBar.setVisible(false);
        loadingLabel.setVisible(false);
    }

    /**
     * Shows or hides the loading indicator.
     * Ensures UI updates are done on the Event Dispatch Thread (EDT).
     *
     * @param loading true to show the indicator, false to hide it.
     */
    private void setLoading(final boolean loading) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(loading);
            loadingLabel.setVisible(loading);
            messageLabel.setText(loading ? "Logging in..." : ""); // Update message label
            messageLabel.setForeground(loading ? Color.BLACK : Color.RED); // Change message color
            // Optional: Disable login components while loading
            usernameField.setEnabled(!loading);
            passwordField.setEnabled(!loading);
            roleComboBox.setEnabled(!loading);
            // Find the login button and disable it
            for (Component comp : getComponents()) {
                if (comp instanceof JButton && ((JButton) comp).getText().equals("Login")) {
                    comp.setEnabled(!loading);
                    break;
                }
            }
        });
    }


    /**
     * Attempts to authenticate the user against the database.
     * Uses a SwingWorker to perform the database query in the background.
     */
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars); // Get password as String
        String selectedRole = (String) roleComboBox.getSelectedItem();

        // Clear password array immediately after use
        java.util.Arrays.fill(passwordChars, ' ');

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            showMessage("Please enter username, password, and select a role.", Color.RED);
            return;
        }

        setLoading(true); // Show loading indicator

        // Use SwingWorker for background database operation
        SwingWorker<User, String> worker = new SwingWorker<User, String>() {
            @Override
            protected User doInBackground() throws Exception {
                User user = null;
                // Use try-with-resources for automatic resource management
                try (Connection conn = DatabaseUtility.getConnection(); // Get connection from DatabaseUtility
                     // Use PreparedStatement to prevent SQL injection
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT user_id, username, role, teacher_id FROM users WHERE username = ? AND password = ? AND role = ?")) {

                    pstmt.setString(1, username);
                    pstmt.setString(2, password); // In a real application, hash and salt passwords!
                    pstmt.setString(3, selectedRole);

                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        // User found and credentials match
                        int userId = rs.getInt("user_id");
                        String dbUsername = rs.getString("username");
                        String dbRole = rs.getString("role");
                        // Get teacher_id, handle NULL case
                        int teacherId = rs.getInt("teacher_id");
                        if (rs.wasNull()) {
                            teacherId = -1; // Use -1 to indicate no teacher ID
                        }

                        user = new User(userId, dbUsername, dbRole, teacherId);
                    }
                    rs.close(); // Close the ResultSet

                } catch (SQLException e) {
                    // Wrap the SQLException in a generic Exception for SwingWorker's done() method
                    throw new Exception("Database error during login: " + e.getMessage(), e);
                } catch (Exception e) {
                     // Catch other potential exceptions during the process
                     throw new Exception("An unexpected error occurred during login: " + e.getMessage(), e);
                }
                return user; // Return the User object (null if login failed)
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    User user = get(); // This will re-throw any exception that occurred in doInBackground()

                    if (user != null) {
                        // Login successful
                        showMessage("Login successful!", Color.GREEN);
                        // Notify the listener (SchoolManagementApp)
                        if (loginListener != null) {
                             // Pass user ID and teacher ID to the listener
                            loginListener.onLoginSuccess(user.getRole(), user.getUserId(), user.getTeacherId());
                        }
                    } else {
                        // Login failed (user not found or credentials incorrect)
                        showMessage("Invalid username, password, or role.", Color.RED);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // Handle exceptions from the SwingWorker
                    Throwable cause = e.getCause(); // Get the actual exception (SQLException or other)
                    String errorMessage = "Login error: " + (cause != null ? cause.getMessage() : e.getMessage());
                    // Display the error message to the user
                    showMessage(errorMessage, Color.RED);
                    e.printStackTrace(); // Print the full stack trace for detailed debugging
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Displays a message in the messageLabel.
     * Ensures the message is displayed on the Event Dispatch Thread (EDT).
     *
     * @param message The message to display.
     * @param color   The color of the message text.
     */
    private void showMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setForeground(color);
            messageLabel.setText(message);
        });
    }

    /**
     * Clears the username and password fields and the message label.
     * Useful after logout.
     */
    public void clearFields() {
        SwingUtilities.invokeLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
            messageLabel.setText(""); // Clear any previous messages
            messageLabel.setForeground(Color.RED); // Reset message color to red
            roleComboBox.setSelectedIndex(0); // Reset role selection to the first item (Admin)
        });
    }

    /**
     * Simple private static class to hold login details retrieved from the database.
     * This is used to pass data from the SwingWorker's doInBackground() to done().
     */
    private static class User {
        private int userId;
        private String username;
        private String role;
        private int teacherId; // Use int, -1 if no teacher ID associated

        public User(int userId, String username, String role, int teacherId) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.teacherId = teacherId;
        }

        // Getter methods
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public int getTeacherId() { return teacherId; } // Return int
    }
}
