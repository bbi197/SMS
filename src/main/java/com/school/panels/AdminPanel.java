package com.school.panels;

import com.school.DatabaseUtility; // Ensure this import is present
import com.school.panels.TeacherPanel; // Keep import if TeacherPanel is referenced
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Date; // Import Date for date_recorded - still needed for Grades, but not Fees date retrieval
import java.util.Collections; // Import for sorting
import java.util.Comparator; // Import for sorting
import java.util.List; // Import List
import java.util.ArrayList; // Import ArrayList
import java.util.Set; // Import Set
import java.util.HashSet; // Import HashSet
import java.util.stream.Collectors; // Import for stream operations

// Import the main application class to access the logout method
import com.school.SchoolManagementApp;
import com.formdev.flatlaf.FlatClientProperties; // Import for FlatLaf specific properties
import java.text.SimpleDateFormat; // For formatting dates in reports
import java.math.BigDecimal; // Use BigDecimal for currency

/**
 * AdminPanel provides the administrative interface for the school management system.
 * It allows managing students, teachers, classes, subjects, assignments, enrollments,
 * generating reports, managing student promotion, and fees.
 * It uses DatabaseUtility for all database interactions.
 */
public class AdminPanel extends JPanel {
    // private String dbUrl; // Removed: Use DatabaseUtility.getConnection()
    private int loggedInUserId; // Store the logged-in Admin's user ID
    private SchoolManagementApp parentFrame; // Reference to the main application frame

    private JTabbedPane adminTabbedPane;

    // Panels for different management sections
    private JPanel studentManagementPanel;
    private JPanel teacherManagementPanel;
    private JPanel classManagementPanel;
    private JPanel subjectManagementPanel; // Added Subject Management Panel
    private JPanel assignmentManagementPanel;
    private JPanel enrollmentManagementPanel;
    private JPanel performanceReportsPanel;
    private JPanel studentPromotionPanel; // Added Student Promotion Panel
    private JPanel feeManagementPanel; // Added Fee Management Panel


    // --- Student Management Components ---
    private JTextField studentNameField, studentGradeField, studentIdField;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JButton prevStudentPageButton, nextStudentPageButton; // Pagination buttons
    private JLabel studentPageInfoLabel; // Label to show current page info
    private int studentCurrentPage = 1;
    private int studentRowsPerPage = 20; // Number of rows per page
    private JComboBox<String> studentClassComboBox; // To assign initial class to new student
    private Map<String, Integer> classNameToIdMap = new HashMap<>(); // Map class name to ID (reused)


    // --- Teacher Management Components ---
    private JTextField teacherNameField, teacherSubjectField, teacherIdField;
    private JTable teacherTable;
    private DefaultTableModel teacherTableModel;
    private JButton prevTeacherPageButton, nextTeacherPageButton; // Pagination buttons
    private JLabel teacherPageInfoLabel; // Label to show current page info
    private int teacherCurrentPage = 1;
    private int teacherRowsPerPage = 20;


    // --- Class Management Components ---
    private JTextField classNameField, classGradeField, classIdField, classFeeField; // Added classFeeField
    private JTable classTable;
    private DefaultTableModel classTableModel;
    private JButton prevClassPageButton, nextClassPageButton; // Pagination buttons
    private JLabel classPageInfoLabel; // Label to show current page info
    private int classCurrentPage = 1;
    private int classRowsPerPage = 20;


    // --- Subject Management Components ---
    private JTextField subjectNameField, subjectIdField; // Added Subject fields
    private JTable subjectTable; // Added Subject table
    private DefaultTableModel subjectTableModel; // Added Subject table model
    private JButton prevSubjectPageButton, nextSubjectPageButton; // Subject pagination
    private JLabel subjectPageInfoLabel; // Subject pagination label
    private int subjectCurrentPage = 1;
    private int subjectRowsPerPage = 20;


    // --- Assignment Management Components ---
    private JComboBox<String> assignClassComboBox, assignTeacherComboBox, assignSubjectComboBox; // Assignment combo boxes
    private JTable assignmentTable; // Assignment table
    private DefaultTableModel assignmentTableModel; // Assignment table model
    private JButton addAssignmentButton, deleteAssignmentButton; // Assignment buttons
    private Map<String, Integer> teacherNameToIdMap = new HashMap<>(); // Map teacher name to ID
    private Map<String, Integer> subjectNameToIdMap = new HashMap<>(); // Map subject name to ID


    // --- Enrollment Management Components ---
    private JComboBox<String> enrollStudentComboBox, enrollClassComboBox; // Enrollment combo boxes
    private JTable enrollmentTable; // Enrollment table
    private DefaultTableModel enrollmentTableModel; // Enrollment table model
    private JButton addEnrollmentButton, deleteEnrollmentButton; // Enrollment buttons
    private Map<String, Integer> studentNameToIdMap = new HashMap<>(); // Map student name to ID


    // --- Performance Reports Components ---
    private JComboBox<String> reportClassComboBox, reportSubjectComboBox, reportTermComboBox; // Report combo boxes
    private JTextArea reportDisplayArea; // Area to display reports
    private JButton generateReportButton, printReportButton; // Report buttons


    // --- Student Promotion Components ---
    private JComboBox<String> promoteFromClassComboBox, promoteToClassComboBox; // Promotion combo boxes
    private JButton promoteStudentsButton; // Promotion button


    // --- Fee Management Components ---
    private JComboBox<String> feeStudentComboBox, feeClassComboBox, feeTermComboBox; // Fee combo boxes
    private JTextField feeAmountDueField, feeAmountPaidField; // Fee amount fields
    private JTable feeTable; // Fee table
    private DefaultTableModel feeTableModel; // Fee table model
    private JButton addFeeButton, updateFeeButton, deleteFeeButton, clearFeeFields; // Fee buttons
    private JTextField feeIdField; // Fee ID field


    // --- Loading Indicator ---
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private JPanel loadingPanel; // Panel to hold loading indicator


    // Constructor - Added loggedInUserId parameter
    public AdminPanel(SchoolManagementApp parentFrame, int loggedInUserId) {
        this.parentFrame = parentFrame; // Store reference to the parent frame
        this.loggedInUserId = loggedInUserId; // Store the logged-in user ID

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Light grey background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold font for tabs


        setupLoadingIndicator(); // Setup the loading bar and label panel

        // Initialize panels
        studentManagementPanel = createStudentManagementPanel();
        teacherManagementPanel = createTeacherManagementPanel();
        classManagementPanel = createClassManagementPanel();
        subjectManagementPanel = createSubjectManagementPanel(); // Create Subject Panel
        assignmentManagementPanel = createAssignmentManagementPanel(); // Create Assignment Panel
        enrollmentManagementPanel = createEnrollmentManagementPanel(); // Create Enrollment Panel
        performanceReportsPanel = createPerformanceReportsPanel(); // Create Reports Panel
        studentPromotionPanel = createStudentPromotionPanel(); // Create Promotion Panel
        feeManagementPanel = createFeeManagementPanel(); // Create Fee Panel


        // Add panels to the tabbed pane
        adminTabbedPane.addTab("Students", studentManagementPanel);
        adminTabbedPane.addTab("Teachers", teacherManagementPanel);
        adminTabbedPane.addTab("Classes", classManagementPanel);
        adminTabbedPane.addTab("Subjects", subjectManagementPanel); // Add Subject Tab
        adminTabbedPane.addTab("Assignments", assignmentManagementPanel); // Add Assignment Tab
        adminTabbedPane.addTab("Enrollments", enrollmentManagementPanel); // Add Enrollment Tab
        adminTabbedPane.addTab("Reports", performanceReportsPanel); // Add Reports Tab
        adminTabbedPane.addTab("Promotion", studentPromotionPanel); // Add Promotion Tab
        adminTabbedPane.addTab("Fees", feeManagementPanel); // Add Fee Tab


        add(adminTabbedPane, BorderLayout.CENTER);

        // Add a ChangeListener to the tabbed pane to load data when a tab is selected
        adminTabbedPane.addChangeListener(e -> {
            int selectedIndex = adminTabbedPane.getSelectedIndex();
            // Load data for the selected tab
            switch (selectedIndex) {
                case 0: // Students
                    loadStudentData();
                    loadClassesForStudent(); // Load classes into the combo box
                    break;
                case 1: // Teachers
                    loadTeacherData();
                    break;
                case 2: // Classes
                    loadClassData();
                    break;
                case 3: // Subjects
                    loadSubjectData();
                    break;
                case 4: // Assignments
                    loadAssignmentData();
                    loadAssignmentComboBoxes(); // Load data for combo boxes
                    break;
                case 5: // Enrollments
                    loadEnrollmentData();
                    loadEnrollmentComboBoxes(); // Load data for combo boxes
                    break;
                case 6: // Reports
                    loadReportComboBoxes(); // Load data for combo boxes
                    break;
                case 7: // Promotion
                    loadPromotionComboBoxes(); // Load data for combo boxes
                    break;
                case 8: // Fees
                    loadFeeData();
                    loadFeeComboBoxes(); // Load data for combo boxes
                    break;
            }
        });

        // Load data for the initially selected tab (Students, index 0)
        loadStudentData();
        loadClassesForStudent();

        // Add Logout button to a panel at the top
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align to the right
        topControlPanel.setBackground(new Color(245, 245, 245)); // Match background
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Smaller font for logout
        logoutButton.setBackground(new Color(220, 53, 69)); // Red background
        logoutButton.setForeground(Color.WHITE); // White text
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        topControlPanel.add(logoutButton);
        add(topControlPanel, BorderLayout.NORTH); // Add to the top of the main panel

        // --- Action Listener for Logout Button ---
        logoutButton.addActionListener(e -> {
            // Call the logout method in the parent frame (SchoolManagementApp)
            if (parentFrame != null) {
                parentFrame.logout();
            }
        });
    }


    // --- Helper Methods ---

    /**
     * Sets up the loading indicator components (progress bar and label).
     */
    private void setupLoadingIndicator() {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Make it an indeterminate progress bar
        progressBar.putClientProperty("JProgressBar.arc", 999); // A large arc value makes it fully rounded

        loadingLabel = new JLabel("Loading data...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a panel to hold the progress bar and label
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(245, 245, 245)); // Match panel background
        loadingPanel.add(loadingLabel, BorderLayout.NORTH);
        loadingPanel.add(progressBar, BorderLayout.CENTER);

        // Initially, don't add it to the main panel. Add it when loading starts.
        progressBar.setVisible(false); // Hide initially
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
            if (loading) {
                // Add the loading panel to the main panel if it's not already there
                if (loadingPanel.getParent() == null) {
                    // Add to the bottom of the main panel or another suitable location
                    add(loadingPanel, BorderLayout.SOUTH); // Added to SOUTH
                }
                progressBar.setVisible(true);
                loadingLabel.setVisible(true);
                // Optional: Disable UI elements while loading
                adminTabbedPane.setEnabled(!loading);
            } else {
                progressBar.setVisible(false);
                loadingLabel.setVisible(false);
                // Remove the loading panel after the loading is complete
                if (loadingPanel.getParent() == this) { // Check if it's currently added to this panel
                    remove(loadingPanel);
                }
                // Optional: Re-enable UI elements
                adminTabbedPane.setEnabled(!loading);
            }
            revalidate(); // Important: Tell the layout manager to re-layout
            repaint();
        });
    }


    /**
     * Helper method to display error messages consistently using a JOptionPane.
     * Ensures the dialog is shown on the Event Dispatch Thread (EDT).
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Helper method to apply consistent styling to JButtons.
     *
     * @param button The JButton to style.
     * @param bgColor The background color for the button.
     */
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
    }


    // --- Student Management Panel ---
    private JPanel createStudentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding/updating student details
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Details")); // Changed title
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Student ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        studentIdField = new JTextField(15);
        studentIdField.setEditable(false); // ID is auto-generated and for display when updating
        studentIdField.setBackground(new Color(235, 235, 235)); // Light grey background for non-editable field
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(studentIdField, gbc);

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        studentNameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(studentNameField, gbc);

        JLabel gradeLabel = new JLabel("Grade Level:"); // Changed label text
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(gradeLabel, gbc);
        studentGradeField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(studentGradeField, gbc);

        JLabel classLabel = new JLabel("Assigned Class:"); // Changed label text
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(classLabel, gbc);
        studentClassComboBox = new JComboBox<>();
        studentClassComboBox.setPreferredSize(new Dimension(150, studentClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(studentClassComboBox, gbc);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        JButton addStudentButton = new JButton("Add Student");
        styleButton(addStudentButton, new Color(40, 167, 69)); // Green
        JButton updateStudentButton = new JButton("Update Student");
        styleButton(updateStudentButton, new Color(0, 123, 255)); // Blue
        JButton deleteStudentButton = new JButton("Delete Student");
        styleButton(deleteStudentButton, new Color(220, 53, 69)); // Red
        JButton clearStudentButton = new JButton("Clear Fields"); // Added Clear button
        styleButton(clearStudentButton, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addStudentButton);
        buttonPanel.add(updateStudentButton);
        buttonPanel.add(deleteStudentButton);
        buttonPanel.add(clearStudentButton); // Add Clear button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Table Panel to display students
        studentTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Grade Level", "Class", "Status"}, 0) { // Added "Status"
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        studentTable = new JTable(studentTableModel);
        studentTable.setFillsViewportHeight(true);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane studentScrollPane = new JScrollPane(studentTable);
        studentScrollPane.setBorder(BorderFactory.createTitledBorder("Students List"));

        // Pagination Panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(new Color(250, 250, 250));
        prevStudentPageButton = new JButton("Previous");
        styleButton(prevStudentPageButton, new Color(108, 117, 125)); // Grey
        studentPageInfoLabel = new JLabel("Page 1");
        nextStudentPageButton = new JButton("Next");
        styleButton(nextStudentPageButton, new Color(108, 117, 125)); // Grey
        paginationPanel.add(prevStudentPageButton);
        paginationPanel.add(studentPageInfoLabel);
        paginationPanel.add(nextStudentPageButton);

        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(studentScrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addStudentButton.addActionListener(e -> addStudent());
        updateStudentButton.addActionListener(e -> updateStudent());
        deleteStudentButton.addActionListener(e -> deleteStudent());
        clearStudentButton.addActionListener(e -> clearStudentFields()); // Listener for Clear button

        prevStudentPageButton.addActionListener(e -> {
            if (studentCurrentPage > 1) {
                studentCurrentPage--;
                loadStudentData();
            }
        });
        nextStudentPageButton.addActionListener(e -> {
            // Determine the total number of pages
            int totalStudents = getStudentCount(); // Implement this method
            int totalPages = (int) Math.ceil((double) totalStudents / studentRowsPerPage);
            if (studentCurrentPage < totalPages) {
                studentCurrentPage++;
                loadStudentData();
            }
        });

        // Add ListSelectionListener to the table to populate fields when a row is selected
        studentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                int selectedRow = studentTable.getSelectedRow();
                // Populate the text fields from the selected row
                studentIdField.setText(studentTable.getValueAt(selectedRow, 0).toString());
                studentNameField.setText(studentTable.getValueAt(selectedRow, 1).toString());
                studentGradeField.setText(studentTable.getValueAt(selectedRow, 2).toString());
                // Select the correct class in the combo box
                String className = studentTable.getValueAt(selectedRow, 3).toString();
                studentClassComboBox.setSelectedItem(className);
            }
        });


        return panel;
    }

    /**
     * Adds a new student record to the database.
     * Uses a SwingWorker to perform the database operation in the background.
     */
    private void addStudent() {
        String name = studentNameField.getText().trim();
        String grade = studentGradeField.getText().trim();
        String className = (String) studentClassComboBox.getSelectedItem(); // Get selected class name

        if (name.isEmpty() || grade.isEmpty() || className == null || className.isEmpty()) {
            showError("Please fill in Name, Grade Level, and select a Class.");
            return;
        }

        Integer classId = classNameToIdMap.get(className); // Get class ID from the map
        if (classId == null) {
            showError("Selected class is invalid. Please select a valid class.");
            return;
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Use try-with-resources for automatic resource management
                try (Connection conn = DatabaseUtility.getConnection();
                     // Use PreparedStatement to prevent SQL injection
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO students (name, grade_level, class_id) VALUES (?, ?, ?)", // Insert into students table directly
                             Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, grade);
                    pstmt.setInt(3, classId); // Set the class_id
                    pstmt.executeUpdate();

                    // Get the generated student ID (if needed for further operations, e.g., initial enrollment)
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    int newStudentId = -1;
                    if (generatedKeys.next()) {
                        newStudentId = generatedKeys.getInt(1);
                    }
                    generatedKeys.close(); // Close the ResultSet

                    if (newStudentId == -1) {
                        // This might indicate an issue with the database or driver not returning keys
                        System.err.println("Warning: Failed to retrieve generated student ID after insert.");
                    }

                    // Note: Enrollment into the class is handled by setting the class_id in the students table directly,
                    // based on the simplified schema. If a separate enrollments table is used for history,
                    // an insert into 'enrollments' would be needed here as well.
                    // The current schema links students directly to one class via class_id in the students table.

                } catch (SQLException e) {
                    // Wrap the SQLException in a generic Exception for SwingWorker's done() method
                    throw new Exception("Database error adding student: " + e.getMessage(), e);
                } catch (Exception e) {
                     // Catch other potential exceptions during the process
                     throw new Exception("An unexpected error occurred while adding student: " + e.getMessage(), e);
                }
                return null; // Indicate successful completion
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // This will re-throw any exception that occurred in doInBackground()
                    JOptionPane.showMessageDialog(AdminPanel.this, "Student added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearStudentFields(); // Clear input fields
                    loadStudentData(); // Refresh the student table to show the new student
                } catch (InterruptedException | ExecutionException e) {
                    // Handle exceptions from the SwingWorker
                    Throwable cause = e.getCause(); // Get the actual exception (SQLException or other)
                    String errorMessage = "Error adding student: " + (cause != null ? cause.getMessage() : e.getMessage());
                    // Display the error message to the user
                    showError(errorMessage);
                    e.printStackTrace(); // Print the full stack trace for detailed debugging
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Updates an existing student record in the database based on the ID in the studentIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void updateStudent() {
        String idStr = studentIdField.getText().trim();
        String name = studentNameField.getText().trim();
        String grade = studentGradeField.getText().trim();
        String className = (String) studentClassComboBox.getSelectedItem(); // Get selected class name

        if (idStr.isEmpty() || name.isEmpty() || grade.isEmpty() || className == null || className.isEmpty()) {
            showError("Please select a student from the table and fill in all fields.");
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Student ID format. Please select a student from the table.");
            return;
        }

        Integer classId = classNameToIdMap.get(className); // Get class ID from the map
         if (classId == null) {
            showError("Selected class is invalid. Please select a valid class.");
            return;
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE students SET name=?, grade_level=?, class_id=? WHERE student_id=?")) { // Updated column names and WHERE clause
                    pstmt.setString(1, name);
                    pstmt.setString(2, grade);
                    pstmt.setInt(3, classId); // Set the class_id
                    pstmt.setInt(4, studentId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        // No rows were updated, likely because the student ID wasn't found
                        throw new Exception("Student with ID " + studentId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating student: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while updating student: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Student updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearStudentFields(); // Clear input fields
                    loadStudentData(); // Refresh the student table
                } catch (InterruptedException | ExecutionException e) {
                     Throwable cause = e.getCause();
                    String errorMessage = "Error updating student: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Deletes a student record from the database based on the ID in the studentIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void deleteStudent() {
        String idStr = studentIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a student from the table to delete.");
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Student ID format. Please select a student from the table.");
            return;
        }

        // Show a confirmation dialog before deleting
        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete student with ID " + studentId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled the deletion
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM students WHERE student_id=?")) { // Updated column name
                    pstmt.setInt(1, studentId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        // No rows were deleted, likely because the student ID wasn't found
                        throw new Exception("Student with ID " + studentId + " not found.");
                    }
                } catch (SQLException e) {
                   throw new Exception("Database error deleting student: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while deleting student: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearStudentFields(); // Clear input fields
                    loadStudentData(); // Refresh the student table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting student: " + (cause != null ? cause.getMessage() : e.getMessage());
                     // Check for specific foreign key constraint violation error if necessary
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("foreign key constraint fails")) {
                         errorMessage += "\nThis student may be linked to other records (e.g., enrollments, grades). Delete those first.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Loads student data from the database and populates the student table.
     * Implements basic pagination. Uses a SwingWorker for background database operation.
     */
    private void loadStudentData() {
        setLoading(true); // Show loading indicator
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                int offset = (studentCurrentPage - 1) * studentRowsPerPage;
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT s.student_id, s.name, s.grade_level, c.class_name, s.status " + // Added s.status
                             "FROM students s " +
                             "JOIN classes c ON s.class_id = c.class_id " + // Join to get class name (using class_id)
                             "ORDER BY s.student_id LIMIT ? OFFSET ?")) { // Updated column name
                    pstmt.setInt(1, studentRowsPerPage);
                    pstmt.setInt(2, offset);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("student_id"), // Updated column name
                                rs.getString("name"),
                                rs.getString("grade_level"), // Updated column name
                                rs.getString("class_name"), // Get class name
                                rs.getString("status") // Get status
                        };
                        data.add(row);
                    }
                    rs.close(); // Close the ResultSet
                } catch (SQLException e) {
                   throw new Exception("Database error loading student data: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new Exception("An unexpected error occurred while loading student data: " + e.getMessage(), e);
                }
                return data; // Return the list of data rows
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    List<Object[]> data = get(); // Get the result from doInBackground()
                    studentTableModel.setRowCount(0); // Clear the table
                    for (Object[] row : data) {
                        studentTableModel.addRow(row); // Add rows to the table model
                    }
                    // Update pagination label and button states
                    int totalStudents = getStudentCount(); // Get total count for pagination
                    int totalPages = (int) Math.ceil((double) totalStudents / studentRowsPerPage);
                    studentPageInfoLabel.setText("Page " + studentCurrentPage + " of " + totalPages);
                    prevStudentPageButton.setEnabled(studentCurrentPage > 1);
                    nextStudentPageButton.setEnabled(studentCurrentPage < totalPages);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading student data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Gets the total count of students in the database for pagination purposes.
     * Uses a direct database query (not in SwingWorker as it's a quick count).
     *
     * @return The total number of students.
     */
    private int getStudentCount() {
        int count = 0;
        try (Connection conn = DatabaseUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            // Log the error but don't necessarily show a dialog as it might happen frequently during pagination updates
            System.err.println("Error getting student count: " + e.getMessage());
            e.printStackTrace();
            // Return 0 to avoid breaking pagination logic, although the count is inaccurate
        }
        return count;
    }


    /**
     * Clears the input fields in the Student Management panel.
     */
    private void clearStudentFields() {
        studentIdField.setText("");
        studentNameField.setText("");
        studentGradeField.setText("");
        // Reset combo box to the first item or a default "Select Class" item if available
        if (studentClassComboBox.getItemCount() > 0) {
             studentClassComboBox.setSelectedIndex(0);
        }
         studentTable.clearSelection(); // Clear table selection
    }

    /**
     * Loads class data from the database to populate the student class combo box.
     * Uses a SwingWorker for background database operation.
     */
    private void loadClassesForStudent() {
         setLoading(true); // Show loading indicator
         SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name")) { // Updated column names
                    classNameToIdMap.clear(); // Clear the map before re-populating
                    Vector<String> classNames = new Vector<>(); // Use Vector for JComboBox model
                    while (rs.next()) {
                        int id = rs.getInt("class_id"); // Updated column name
                        String name = rs.getString("class_name"); // Updated column name
                        classNames.add(name);
                        classNameToIdMap.put(name, id); // Populate the map
                    }
                    rs.close(); // Close the ResultSet
                    // Update the combo box model on the EDT
                    SwingUtilities.invokeLater(() -> {
                        studentClassComboBox.setModel(new DefaultComboBoxModel<>(classNames));
                    });
                } catch (SQLException e) {
                    throw new Exception("Database error loading classes: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new Exception("An unexpected error occurred while loading classes: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try{
                    get(); // Check for exceptions
                    // UI update (setting model) is already done in doInBackground via invokeLater
                } catch(InterruptedException | ExecutionException e){
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading classes for student panel: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear the combo box if loading fails
                     SwingUtilities.invokeLater(() -> {
                         studentClassComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
         }.execute(); // Execute the SwingWorker
    }


    // --- Teacher Management Panel ---
    private JPanel createTeacherManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding/updating teacher details
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Teacher Details")); // Changed title
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Teacher ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        teacherIdField = new JTextField(15);
        teacherIdField.setEditable(false); // ID is auto-generated and for display when updating
        teacherIdField.setBackground(new Color(235, 235, 235)); // Light grey background for non-editable field
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(teacherIdField, gbc);

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        teacherNameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(teacherNameField, gbc);

        JLabel subjectLabel = new JLabel("Subject:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(subjectLabel, gbc);
        teacherSubjectField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(teacherSubjectField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        JButton addTeacherButton = new JButton("Add Teacher");
        styleButton(addTeacherButton, new Color(40, 167, 69)); // Green
        JButton updateTeacherButton = new JButton("Update Teacher");
        styleButton(updateTeacherButton, new Color(0, 123, 255)); // Blue
        JButton deleteTeacherButton = new JButton("Delete Teacher");
        styleButton(deleteTeacherButton, new Color(220, 53, 69)); // Red
        JButton clearTeacherButton = new JButton("Clear Fields"); // Added Clear button
        styleButton(clearTeacherButton, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addTeacherButton);
        buttonPanel.add(updateTeacherButton);
        buttonPanel.add(deleteTeacherButton);
        buttonPanel.add(clearTeacherButton); // Add Clear button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Table Panel to display teachers
        teacherTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Subject"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        teacherTable = new JTable(teacherTableModel);
        teacherTable.setFillsViewportHeight(true);
        teacherTable.setRowHeight(25);
        teacherTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane teacherScrollPane = new JScrollPane(teacherTable);
        teacherScrollPane.setBorder(BorderFactory.createTitledBorder("Teachers List"));

        // Pagination Panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(new Color(250, 250, 250));
        prevTeacherPageButton = new JButton("Previous");
        styleButton(prevTeacherPageButton, new Color(108, 117, 125)); // Grey
        teacherPageInfoLabel = new JLabel("Page 1");
        nextTeacherPageButton = new JButton("Next");
        styleButton(nextTeacherPageButton, new Color(108, 117, 125)); // Grey
        paginationPanel.add(prevTeacherPageButton);
        paginationPanel.add(teacherPageInfoLabel);
        paginationPanel.add(nextTeacherPageButton);

        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(teacherScrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addTeacherButton.addActionListener(e -> addTeacher());
        updateTeacherButton.addActionListener(e -> updateTeacher());
        deleteTeacherButton.addActionListener(e -> deleteTeacher());
        clearTeacherButton.addActionListener(e -> clearTeacherFields()); // Listener for Clear button

        prevTeacherPageButton.addActionListener(e -> {
            if (teacherCurrentPage > 1) {
                teacherCurrentPage--;
                loadTeacherData();
            }
        });
        nextTeacherPageButton.addActionListener(e -> {
            int totalTeachers = getTeacherCount();
            int totalPages = (int) Math.ceil((double) totalTeachers / teacherRowsPerPage);
            if (teacherCurrentPage < totalPages) {
                teacherCurrentPage++;
                loadTeacherData();
            }
        });

        // Add ListSelectionListener to the table to populate fields when a row is selected
        teacherTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && teacherTable.getSelectedRow() != -1) {
                int selectedRow = teacherTable.getSelectedRow();
                // Populate the text fields from the selected row
                teacherIdField.setText(teacherTable.getValueAt(selectedRow, 0).toString());
                teacherNameField.setText(teacherTable.getValueAt(selectedRow, 1).toString());
                teacherSubjectField.setText(teacherTable.getValueAt(selectedRow, 2).toString());
            }
        });

        return panel;
    }

    /**
     * Adds a new teacher record to the database.
     * Uses a SwingWorker for background database operation.
     */
    private void addTeacher() {
        String name = teacherNameField.getText().trim();
        String subject = teacherSubjectField.getText().trim();
        if (name.isEmpty() || subject.isEmpty()) {
            showError("Please fill in Name and Subject.");
            return;
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO teachers (name, subject) VALUES (?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, subject);
                    pstmt.executeUpdate();

                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newTeacherId = generatedKeys.getInt(1);
                        // Optionally, do something with the newTeacherId, like creating a user account for them.
                        // For now, we just add the teacher record.
                    }
                    generatedKeys.close();
                } catch (SQLException e) {
                    throw new Exception("Database error adding teacher: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while adding teacher: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Teacher added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearTeacherFields(); // Clear input fields
                    loadTeacherData(); // Refresh the teacher table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding teacher: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Updates an existing teacher record in the database based on the ID in the teacherIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void updateTeacher() {
        String idStr = teacherIdField.getText().trim();
        String name = teacherNameField.getText().trim();
        String subject = teacherSubjectField.getText().trim();
        if (idStr.isEmpty() || name.isEmpty() || subject.isEmpty()) {
            showError("Please select a teacher from the table and fill in all fields.");
            return;
        }
        int teacherId;
        try {
            teacherId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Teacher ID format. Please select a teacher from the table.");
            return;
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE teachers SET name=?, subject=? WHERE teacher_id=?")) { // Updated column name
                    pstmt.setString(1, name);
                    pstmt.setString(2, subject);
                    pstmt.setInt(3, teacherId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new Exception("Teacher with ID " + teacherId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating teacher: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while updating teacher: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Teacher updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearTeacherFields(); // Clear input fields
                    loadTeacherData(); // Refresh the teacher table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error updating teacher: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Deletes a teacher record from the database based on the ID in the teacherIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void deleteTeacher() {
        String idStr = teacherIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a teacher from the table to delete.");
            return;
        }
        int teacherId;
        try {
            teacherId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Teacher ID format. Please select a teacher from the table.");
            return;
        }

        // Show a confirmation dialog before deleting
        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete teacher with ID " + teacherId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled the deletion
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM teachers WHERE teacher_id=?")) { // Updated column name
                    pstmt.setInt(1, teacherId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Teacher with ID " + teacherId + " not found.");
                    }
                } catch (SQLException e) {
                   throw new Exception("Database error deleting teacher: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while deleting teacher: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Teacher deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearTeacherFields(); // Clear input fields
                    loadTeacherData(); // Refresh the teacher table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting teacher: " + (cause != null ? cause.getMessage() : e.getMessage());
                     // Check for specific foreign key constraint violation error if necessary
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("foreign key constraint fails")) {
                         errorMessage += "\nThis teacher may be linked to user accounts or class assignments. Delete those first.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Loads teacher data from the database and populates the teacher table.
     * Implements basic pagination. Uses a SwingWorker for background database operation.
     */
    private void loadTeacherData() {
        setLoading(true); // Show loading indicator
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                int offset = (teacherCurrentPage - 1) * teacherRowsPerPage;
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT teacher_id, name, subject FROM teachers ORDER BY teacher_id LIMIT ? OFFSET ?")) { // Updated column name
                    pstmt.setInt(1, teacherRowsPerPage);
                    pstmt.setInt(2, offset);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("teacher_id"), // Updated column name
                                rs.getString("name"),
                                rs.getString("subject")
                        };
                        data.add(row);
                    }
                    rs.close(); // Close the ResultSet
                } catch (SQLException e) {
                    throw new Exception("Database error loading teacher data: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new Exception("An unexpected error occurred while loading teacher data: " + e.getMessage(), e);
                }
                return data; // Return the list of data rows
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    List<Object[]> data = get(); // Get the result from doInBackground()
                    teacherTableModel.setRowCount(0); // Clear the table
                    for (Object[] row : data) {
                        teacherTableModel.addRow(row); // Add rows to the table model
                    }
                    // Update pagination label and button states
                    int totalTeachers = getTeacherCount(); // Get total count for pagination
                    int totalPages = (int) Math.ceil((double) totalTeachers / teacherRowsPerPage);
                    teacherPageInfoLabel.setText("Page " + teacherCurrentPage + " of " + totalPages);
                    prevTeacherPageButton.setEnabled(teacherCurrentPage > 1);
                    nextTeacherPageButton.setEnabled(teacherCurrentPage < totalPages);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading teacher data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Gets the total count of teachers in the database for pagination purposes.
     * Uses a direct database query (not in SwingWorker as it's a quick count).
     *
     * @return The total number of teachers.
     */
    private int getTeacherCount() {
        int count = 0;
        try (Connection conn = DatabaseUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teachers")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            // Log the error but don't necessarily show a dialog
            System.err.println("Error getting teacher count: " + e.getMessage());
            e.printStackTrace();
            // Return 0 to avoid breaking pagination logic
        }
        return count;
    }

    /**
     * Clears the input fields in the Teacher Management panel.
     */
    private void clearTeacherFields() {
        teacherIdField.setText("");
        teacherNameField.setText("");
        teacherSubjectField.setText("");
         teacherTable.clearSelection(); // Clear table selection
    }


    // --- Class Management Panel ---
    private JPanel createClassManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding/updating class details
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Class Details")); // Changed title
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Class ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        classIdField = new JTextField(15);
        classIdField.setEditable(false); // ID is auto-generated and for display when updating
        classIdField.setBackground(new Color(235, 235, 235)); // Light grey background for non-editable field
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(classIdField, gbc);

        JLabel nameLabel = new JLabel("Class Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        classNameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(classNameField, gbc);

        JLabel gradeLabel = new JLabel("Grade Level:"); // Changed label text
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(gradeLabel, gbc);
        classGradeField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(classGradeField, gbc);

        JLabel feeLabel = new JLabel("Fee:"); // Added Fee Label
        gbc.gridx = 0;
        gbc.gridy = 3; // Added gridy for Fee
        formPanel.add(feeLabel, gbc);
        classFeeField = new JTextField(15); // Added Fee Field
        gbc.gridx = 1;
        gbc.gridy = 3; // Added gridy for Fee
        formPanel.add(classFeeField, gbc);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        JButton addClassButton = new JButton("Add Class");
        styleButton(addClassButton, new Color(40, 167, 69)); // Green
        JButton updateClassButton = new JButton("Update Class");
        styleButton(updateClassButton, new Color(0, 123, 255)); // Blue
        JButton deleteClassButton = new JButton("Delete Class");
        styleButton(deleteClassButton, new Color(220, 53, 69)); // Red
        JButton clearClassButton = new JButton("Clear Fields"); // Added Clear button
        styleButton(clearClassButton, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addClassButton);
        buttonPanel.add(updateClassButton);
        buttonPanel.add(deleteClassButton);
        buttonPanel.add(clearClassButton); // Add Clear button
        gbc.gridx = 0;
        gbc.gridy = 4; // Adjusted gridy
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Table Panel to display classes
        classTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Grade Level", "Fee"}, 0) { // Added "Fee" column
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        classTable = new JTable(classTableModel);
        classTable.setFillsViewportHeight(true);
        classTable.setRowHeight(25);
        classTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane classScrollPane = new JScrollPane(classTable);
        classScrollPane.setBorder(BorderFactory.createTitledBorder("Classes List"));

        // Pagination Panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(new Color(250, 250, 250));
        prevClassPageButton = new JButton("Previous");
        styleButton(prevClassPageButton, new Color(108, 117, 125)); // Grey
        classPageInfoLabel = new JLabel("Page 1");
        nextClassPageButton = new JButton("Next");
        styleButton(nextClassPageButton, new Color(108, 117, 125)); // Grey
        paginationPanel.add(prevClassPageButton);
        paginationPanel.add(classPageInfoLabel);
        paginationPanel.add(nextClassPageButton);

        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(classScrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addClassButton.addActionListener(e -> addClass());
        updateClassButton.addActionListener(e -> updateClass());
        deleteClassButton.addActionListener(e -> deleteClass());
        clearClassButton.addActionListener(e -> clearClassFields()); // Listener for Clear button

        prevClassPageButton.addActionListener(e -> {
            if (classCurrentPage > 1) {
                classCurrentPage--;
                loadClassData();
            }
        });
        nextClassPageButton.addActionListener(e -> {
            int totalClasses = getClassCount();
            int totalPages = (int) Math.ceil((double) totalClasses / classRowsPerPage);
            if (classCurrentPage < totalPages) {
                classCurrentPage++;
                loadClassData();
            }
        });

        // Add ListSelectionListener to the table to populate fields when a row is selected
        classTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && classTable.getSelectedRow() != -1) {
                int selectedRow = classTable.getSelectedRow();
                // Populate the text fields from the selected row
                classIdField.setText(classTable.getValueAt(selectedRow, 0).toString());
                classNameField.setText(classTable.getValueAt(selectedRow, 1).toString());
                classGradeField.setText(classTable.getValueAt(selectedRow, 2).toString());
                 // Handle potential null or non-numeric fee values gracefully
                Object feeValue = classTable.getValueAt(selectedRow, 3);
                classFeeField.setText(feeValue != null ? feeValue.toString() : "");
            }
        });

        return panel;
    }

    /**
     * Adds a new class record to the database.
     * Uses a SwingWorker for background database operation.
     */
    private void addClass() {
        String name = classNameField.getText().trim();
        String grade = classGradeField.getText().trim();
        String feeStr = classFeeField.getText().trim(); // Get fee as string

        if (name.isEmpty() || grade.isEmpty() || feeStr.isEmpty()) {
            showError("Please fill in Class Name, Grade Level, and Fee.");
            return;
        }

        int fee;
        try {
            fee = Integer.parseInt(feeStr); // Parse fee to integer
             if (fee < 0) {
                 showError("Fee cannot be negative.");
                 return;
             }
        } catch (NumberFormatException e) {
            showError("Invalid Fee format. Please enter a valid number.");
            return;
        }


        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO classes (class_name, grade_level, fee) VALUES (?, ?, ?)",  // Updated column names
                             Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, grade);
                    pstmt.setInt(3, fee); // Set the fee
                    pstmt.executeUpdate();

                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newClassId = generatedKeys.getInt(1);
                        // Optionally, do something with the newClassId
                    }
                    generatedKeys.close();
                } catch (SQLException e) {
                    throw new Exception("Database error adding class: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while adding class: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Class added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearClassFields(); // Clear input fields
                    loadClassData(); // Refresh the class table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding class: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A class with this name already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Updates an existing class record in the database based on the ID in the classIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void updateClass() {
        String idStr = classIdField.getText().trim();
        String name = classNameField.getText().trim();
        String grade = classGradeField.getText().trim();
        String feeStr = classFeeField.getText().trim(); // Get fee as string

        if (idStr.isEmpty() || name.isEmpty() || grade.isEmpty() || feeStr.isEmpty()) {
            showError("Please select a class from the table and fill in all fields.");
            return;
        }

        int classId;
        try {
            classId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Class ID format. Please select a class from the table.");
            return;
        }

        int fee;
        try {
            fee = Integer.parseInt(feeStr); // Parse fee to integer
             if (fee < 0) {
                 showError("Fee cannot be negative.");
                 return;
             }
        } catch (NumberFormatException e) {
            showError("Invalid Fee format. Please enter a valid number.");
            return;
        }


        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE classes SET class_name=?, grade_level=?, fee=? WHERE class_id=?")) { // Updated column names and WHERE clause
                    pstmt.setString(1, name);
                    pstmt.setString(2, grade);
                    pstmt.setInt(3, fee); // Set the fee
                    pstmt.setInt(4, classId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new Exception("Class with ID " + classId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating class: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while updating class: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Class updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearClassFields(); // Clear input fields
                    loadClassData(); // Refresh the class table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error updating class: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A class with this name already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Deletes a class record from the database based on the ID in the classIdField.
     * Uses a SwingWorker for background database operation.
     */
    private void deleteClass() {
        String idStr = classIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a class from the table to delete.");
            return;
        }
        int classId;
        try {
            classId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Class ID format. Please select a class from the table.");
            return;
        }

        // Show a confirmation dialog before deleting
        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete class with ID " + classId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled the deletion
        }

        setLoading(true); // Show loading indicator
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM classes WHERE class_id=?")) { // Updated column name
                    pstmt.setInt(1, classId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Class with ID " + classId + " not found.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error deleting class: " + e.getMessage(), e);
                } catch (Exception e) {
                     throw new Exception("An unexpected error occurred while deleting class: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AdminPanel.this, "Class deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearClassFields(); // Clear input fields
                    loadClassData(); // Refresh the class table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting class: " + (cause != null ? cause.getMessage() : e.getMessage());
                     // Check for specific foreign key constraint violation error if necessary
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("foreign key constraint fails")) {
                         errorMessage += "\nThis class may be linked to students, assignments, enrollments, or fees. Delete those first.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Loads class data from the database and populates the class table.
     * Implements basic pagination. Uses a SwingWorker for background database operation.
     */
    private void loadClassData() {
        setLoading(true); // Show loading indicator
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                int offset = (classCurrentPage - 1) * classRowsPerPage;
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT class_id, class_name, grade_level, fee FROM classes ORDER BY class_id LIMIT ? OFFSET ?")) { // Updated column names
                    pstmt.setInt(1, classRowsPerPage);
                    pstmt.setInt(2, offset);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("class_id"), // Updated column name
                                rs.getString("class_name"), // Updated column name
                                rs.getString("grade_level"), // Updated column name
                                rs.getInt("fee") // Get fee
                        };
                        data.add(row);
                    }
                    rs.close(); // Close the ResultSet
                } catch (SQLException e) {
                    throw new Exception("Database error loading class data: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new Exception("An unexpected error occurred while loading class data: " + e.getMessage(), e);
                }
                return data; // Return the list of data rows
            }

            @Override
            protected void done() {
                setLoading(false); // Hide loading indicator
                try {
                    List<Object[]> data = get(); // Get the result from doInBackground()
                    classTableModel.setRowCount(0); // Clear the table
                    for (Object[] row : data) {
                        classTableModel.addRow(row); // Add rows to the table model
                    }
                    // Update pagination label and button states
                    int totalClasses = getClassCount(); // Get total count for pagination
                    int totalPages = (int) Math.ceil((double) totalClasses / classRowsPerPage);
                    classPageInfoLabel.setText("Page " + classCurrentPage + " of " + totalPages);
                    prevClassPageButton.setEnabled(classCurrentPage > 1);
                    nextClassPageButton.setEnabled(classCurrentPage < totalPages);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading class data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    /**
     * Gets the total count of classes in the database for pagination purposes.
     * Uses a direct database query (not in SwingWorker as it's a quick count).
     *
     * @return The total number of classes.
     */
    private int getClassCount() {
        int count = 0;
        try (Connection conn = DatabaseUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM classes")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            // Log the error but don't necessarily show a dialog
            System.err.println("Error getting class count: " + e.getMessage());
            e.printStackTrace();
            // Return 0 to avoid breaking pagination logic
        }
        return count;
    }


    /**
     * Clears the input fields in the Class Management panel.
     */
    private void clearClassFields() {
        classIdField.setText("");
        classNameField.setText("");
        classGradeField.setText("");
        classFeeField.setText("");
         classTable.clearSelection(); // Clear table selection
    }


    // --- Subject Management Panel ---
    private JPanel createSubjectManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding/updating subject details
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Subject Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Subject ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        subjectIdField = new JTextField(15);
        subjectIdField.setEditable(false);
        subjectIdField.setBackground(new Color(235, 235, 235));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(subjectIdField, gbc);

        JLabel nameLabel = new JLabel("Subject Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        subjectNameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(subjectNameField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        JButton addSubjectButton = new JButton("Add Subject");
        styleButton(addSubjectButton, new Color(40, 167, 69)); // Green
        JButton updateSubjectButton = new JButton("Update Subject");
        styleButton(updateSubjectButton, new Color(0, 123, 255)); // Blue
        JButton deleteSubjectButton = new JButton("Delete Subject");
        styleButton(deleteSubjectButton, new Color(220, 53, 69)); // Red
        JButton clearSubjectFieldsButton = new JButton("Clear Fields");
        styleButton(clearSubjectFieldsButton, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addSubjectButton);
        buttonPanel.add(updateSubjectButton);
        buttonPanel.add(deleteSubjectButton);
        buttonPanel.add(clearSubjectFieldsButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Table Panel to display subjects
        subjectTableModel = new DefaultTableModel(new Object[]{"ID", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        subjectTable = new JTable(subjectTableModel);
        subjectTable.setFillsViewportHeight(true);
        subjectTable.setRowHeight(25);
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane subjectScrollPane = new JScrollPane(subjectTable);
        subjectScrollPane.setBorder(BorderFactory.createTitledBorder("Subjects List"));

        // Pagination Panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(new Color(250, 250, 250));
        prevSubjectPageButton = new JButton("Previous");
        styleButton(prevSubjectPageButton, new Color(108, 117, 125)); // Grey
        subjectPageInfoLabel = new JLabel("Page 1");
        nextSubjectPageButton = new JButton("Next");
        styleButton(nextSubjectPageButton, new Color(108, 117, 125)); // Grey
        paginationPanel.add(prevSubjectPageButton);
        paginationPanel.add(subjectPageInfoLabel);
        paginationPanel.add(nextSubjectPageButton);


        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(subjectScrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addSubjectButton.addActionListener(e -> addSubject());
        updateSubjectButton.addActionListener(e -> updateSubject());
        deleteSubjectButton.addActionListener(e -> deleteSubject());
        clearSubjectFieldsButton.addActionListener(e -> clearSubjectFields());

        prevSubjectPageButton.addActionListener(e -> {
            if (subjectCurrentPage > 1) {
                subjectCurrentPage--;
                loadSubjectData();
            }
        });
        nextSubjectPageButton.addActionListener(e -> {
            int totalSubjects = getSubjectCount();
            int totalPages = (int) Math.ceil((double) totalSubjects / subjectRowsPerPage);
            if (subjectCurrentPage < totalPages) {
                subjectCurrentPage++;
                loadSubjectData();
            }
        });

        subjectTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && subjectTable.getSelectedRow() != -1) {
                int selectedRow = subjectTable.getSelectedRow();
                subjectIdField.setText(subjectTable.getValueAt(selectedRow, 0).toString());
                subjectNameField.setText(subjectTable.getValueAt(selectedRow, 1).toString());
            }
        });

        return panel;
    }

    /**
     * Adds a new subject record to the database.
     * Uses SwingWorker for background database operation.
     */
    private void addSubject() {
        String name = subjectNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please fill in Subject Name.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO subjects (subject_name) VALUES (?)",
                             Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        // int newSubjectId = generatedKeys.getInt(1); // Not used currently
                    }
                    generatedKeys.close();
                } catch (SQLException e) {
                    throw new Exception("Database error adding subject: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Subject added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearSubjectFields();
                    loadSubjectData();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding subject: " + (cause != null ? cause.getMessage() : e.getMessage());
                    if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A subject with this name already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Updates an existing subject record in the database.
     * Uses SwingWorker for background database operation.
     */
    private void updateSubject() {
        String idStr = subjectIdField.getText().trim();
        String name = subjectNameField.getText().trim();
        if (idStr.isEmpty() || name.isEmpty()) {
            showError("Please select a subject from the table and fill in the name.");
            return;
        }
        int subjectId;
        try {
            subjectId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Subject ID format. Please select a subject from the table.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE subjects SET subject_name=? WHERE subject_id=?")) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, subjectId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new Exception("Subject with ID " + subjectId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating subject: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Subject updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearSubjectFields();
                    loadSubjectData();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error updating subject: " + (cause != null ? cause.getMessage() : e.getMessage());
                    if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A subject with this name already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Deletes a subject record from the database.
     * Uses SwingWorker for background database operation.
     */
    private void deleteSubject() {
        String idStr = subjectIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a subject from the table to delete.");
            return;
        }
        int subjectId;
        try {
            subjectId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Subject ID format. Please select a subject from the table.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete subject with ID " + subjectId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM subjects WHERE subject_id=?")) {
                    pstmt.setInt(1, subjectId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Subject with ID " + subjectId + " not found.");
                    }
                } catch (SQLException e) {
                   throw new Exception("Database error deleting subject: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Subject deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearSubjectFields();
                    loadSubjectData();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting subject: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("foreign key constraint fails")) {
                         errorMessage += "\nThis subject may be linked to class assignments or grades. Delete those first.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Loads subject data from the database and populates the subject table.
     * Implements basic pagination. Uses SwingWorker for background database operation.
     */
    private void loadSubjectData() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                int offset = (subjectCurrentPage - 1) * subjectRowsPerPage;
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT subject_id, subject_name FROM subjects ORDER BY subject_id LIMIT ? OFFSET ?")) {
                    pstmt.setInt(1, subjectRowsPerPage);
                    pstmt.setInt(2, offset);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("subject_id"),
                                rs.getString("subject_name")
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading subject data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    subjectTableModel.setRowCount(0);
                    for (Object[] row : data) {
                        subjectTableModel.addRow(row);
                    }
                    int totalSubjects = getSubjectCount();
                    int totalPages = (int) Math.ceil((double) totalSubjects / subjectRowsPerPage);
                    subjectPageInfoLabel.setText("Page " + subjectCurrentPage + " of " + totalPages);
                    prevSubjectPageButton.setEnabled(subjectCurrentPage > 1);
                    nextSubjectPageButton.setEnabled(subjectCurrentPage < totalPages);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading subject data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Gets the total count of subjects for pagination.
     */
    private int getSubjectCount() {
        int count = 0;
        try (Connection conn = DatabaseUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting subject count: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Clears the input fields in the Subject Management panel.
     */
    private void clearSubjectFields() {
        subjectIdField.setText("");
        subjectNameField.setText("");
        subjectTable.clearSelection();
    }


    // --- Assignment Management Panel ---
    private JPanel createAssignmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding assignments
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Assign Teacher to Class and Subject"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel classLabel = new JLabel("Class:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(classLabel, gbc);
        assignClassComboBox = new JComboBox<>();
        assignClassComboBox.setPreferredSize(new Dimension(200, assignClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(assignClassComboBox, gbc);

        JLabel teacherLabel = new JLabel("Teacher:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(teacherLabel, gbc);
        assignTeacherComboBox = new JComboBox<>();
        assignTeacherComboBox.setPreferredSize(new Dimension(200, assignTeacherComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(assignTeacherComboBox, gbc);

        JLabel subjectLabel = new JLabel("Subject:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(subjectLabel, gbc);
        assignSubjectComboBox = new JComboBox<>();
        assignSubjectComboBox.setPreferredSize(new Dimension(200, assignSubjectComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(assignSubjectComboBox, gbc);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        addAssignmentButton = new JButton("Add Assignment");
        styleButton(addAssignmentButton, new Color(40, 167, 69)); // Green
        JButton deleteAssignmentButton = new JButton("Delete Assignment");
        styleButton(deleteAssignmentButton, new Color(220, 53, 69)); // Red
        buttonPanel.add(addAssignmentButton);
        buttonPanel.add(deleteAssignmentButton);
        gbc.gridx = 0;
        gbc.gridy = 3; // Adjusted gridy
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Table Panel
        assignmentTableModel = new DefaultTableModel(new Object[]{"ID", "Class", "Teacher", "Subject"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        assignmentTable = new JTable(assignmentTableModel);
        assignmentTable.setFillsViewportHeight(true);
        assignmentTable.setRowHeight(25);
        assignmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
        assignmentScrollPane.setBorder(BorderFactory.createTitledBorder("Assignments List"));


        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(assignmentScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addAssignmentButton.addActionListener(e -> addAssignment());
        deleteAssignmentButton.addActionListener(e -> deleteAssignment());

        // Add ListSelectionListener to the table to populate fields when a row is selected
        assignmentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && assignmentTable.getSelectedRow() != -1) {
                int selectedRow = assignmentTable.getSelectedRow();
                // Populate the combo boxes from the selected row
                assignClassComboBox.setSelectedItem(assignmentTable.getValueAt(selectedRow, 1).toString());
                assignTeacherComboBox.setSelectedItem(assignmentTable.getValueAt(selectedRow, 2).toString());
                assignSubjectComboBox.setSelectedItem(assignmentTable.getValueAt(selectedRow, 3).toString());
            }
        });

        return panel;
    }

    /**
     * Loads data for assignment combo boxes (classes, teachers, subjects).
     * Uses SwingWorker for background database operation.
     */
    private void loadAssignmentComboBoxes() {
        setLoading(true);
        SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> classNames = new Vector<>();
                Vector<String> teacherNames = new Vector<>();
                Vector<String> subjectNames = new Vector<>();

                classNameToIdMap.clear(); // Clear maps before re-populating
                teacherNameToIdMap.clear();
                subjectNameToIdMap.clear();

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Classes
                    ResultSet rsClasses = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name");
                    while (rsClasses.next()) {
                        int id = rsClasses.getInt("class_id");
                        String name = rsClasses.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rsClasses.close();
                    data.put("classes", classNames);

                    // Load Teachers
                    ResultSet rsTeachers = stmt.executeQuery("SELECT teacher_id, name FROM teachers ORDER BY name");
                    while (rsTeachers.next()) {
                        int id = rsTeachers.getInt("teacher_id");
                        String name = rsTeachers.getString("name");
                        teacherNames.add(name);
                        teacherNameToIdMap.put(name, id);
                    }
                    rsTeachers.close();
                    data.put("teachers", teacherNames);

                    // Load Subjects
                    ResultSet rsSubjects = stmt.executeQuery("SELECT subject_id, subject_name FROM subjects ORDER BY subject_name");
                    while (rsSubjects.next()) {
                        int id = rsSubjects.getInt("subject_id");
                        String name = rsSubjects.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rsSubjects.close();
                    data.put("subjects", subjectNames);

                } catch (SQLException e) {
                    throw new Exception("Database error loading combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    // Update combo box models on the EDT
                    SwingUtilities.invokeLater(() -> {
                        assignClassComboBox.setModel(new DefaultComboBoxModel<>(data.get("classes")));
                        assignTeacherComboBox.setModel(new DefaultComboBoxModel<>(data.get("teachers")));
                        assignSubjectComboBox.setModel(new DefaultComboBoxModel<>(data.get("subjects")));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading assignment combo box data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         assignClassComboBox.setModel(new DefaultComboBoxModel<>());
                         assignTeacherComboBox.setModel(new DefaultComboBoxModel<>());
                         assignSubjectComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }


    /**
     * Adds a new class assignment record to the database.
     * Uses SwingWorker for background database operation.
     */
    private void addAssignment() {
        String className = (String) assignClassComboBox.getSelectedItem();
        String teacherName = (String) assignTeacherComboBox.getSelectedItem();
        String subjectName = (String) assignSubjectComboBox.getSelectedItem();

        if (className == null || className.isEmpty() || teacherName == null || teacherName.isEmpty() || subjectName == null || subjectName.isEmpty()) {
            showError("Please select a Class, Teacher, and Subject.");
            return;
        }

        Integer classId = classNameToIdMap.get(className);
        Integer teacherId = teacherNameToIdMap.get(teacherName);
        Integer subjectId = subjectNameToIdMap.get(subjectName);

        if (classId == null || teacherId == null || subjectId == null) {
            showError("Invalid selections. Please select valid Class, Teacher, and Subject.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO class_assignments (class_id, teacher_id, subject_id) VALUES (?, ?, ?)")) {
                    pstmt.setInt(1, classId);
                    pstmt.setInt(2, teacherId);
                    pstmt.setInt(3, subjectId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new Exception("Database error adding assignment: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Assignment added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAssignmentData(); // Refresh the assignment table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding assignment: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: This assignment (Class, Teacher, Subject combination) already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Deletes a class assignment record from the database.
     * Deletes the selected row from the table and uses its ID.
     * Uses SwingWorker for background database operation.
     */
    private void deleteAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an assignment from the table to delete.");
            return;
        }

        int assignmentId = (int) assignmentTableModel.getValueAt(selectedRow, 0); // Get ID from the table model

        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete this assignment (ID " + assignmentId + ")? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM class_assignments WHERE assignment_id=?")) {
                    pstmt.setInt(1, assignmentId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Assignment with ID " + assignmentId + " not found.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error deleting assignment: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Assignment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAssignmentData(); // Refresh the assignment table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting assignment: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Loads class assignment data from the database and populates the assignment table.
     * Uses SwingWorker for background database operation.
     */
    private void loadAssignmentData() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT ca.assignment_id, c.class_name, t.name AS teacher_name, s.subject_name " +
                             "FROM class_assignments ca " +
                             "JOIN classes c ON ca.class_id = c.class_id " +
                             "JOIN teachers t ON ca.teacher_id = t.teacher_id " +
                             "JOIN subjects s ON ca.subject_id = s.subject_id " +
                             "ORDER BY ca.assignment_id")) {
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("assignment_id"),
                                rs.getString("class_name"),
                                rs.getString("teacher_name"),
                                rs.getString("subject_name")
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading assignment data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    assignmentTableModel.setRowCount(0);
                    for (Object[] row : data) {
                        assignmentTableModel.addRow(row);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading assignment data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    // --- Enrollment Management Panel ---
    private JPanel createEnrollmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding enrollments
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Enroll Student in Class"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel studentLabel = new JLabel("Student:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(studentLabel, gbc);
        enrollStudentComboBox = new JComboBox<>();
        enrollStudentComboBox.setPreferredSize(new Dimension(200, enrollStudentComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(enrollStudentComboBox, gbc);

        JLabel classLabel = new JLabel("Class:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(classLabel, gbc);
        enrollClassComboBox = new JComboBox<>();
        enrollClassComboBox.setPreferredSize(new Dimension(200, enrollClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(enrollClassComboBox, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        addEnrollmentButton = new JButton("Add Enrollment");
        styleButton(addEnrollmentButton, new Color(40, 167, 69)); // Green
        JButton deleteEnrollmentButton = new JButton("Delete Enrollment");
        styleButton(deleteEnrollmentButton, new Color(220, 53, 69)); // Red
        buttonPanel.add(addEnrollmentButton);
        buttonPanel.add(deleteEnrollmentButton);
        gbc.gridx = 0;
        gbc.gridy = 2; // Adjusted gridy
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // Table Panel
        enrollmentTableModel = new DefaultTableModel(new Object[]{"ID", "Student", "Class"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        enrollmentTable = new JTable(enrollmentTableModel);
        enrollmentTable.setFillsViewportHeight(true);
        enrollmentTable.setRowHeight(25);
        enrollmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane enrollmentScrollPane = new JScrollPane(enrollmentTable);
        enrollmentScrollPane.setBorder(BorderFactory.createTitledBorder("Enrollments List"));


        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(enrollmentScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addEnrollmentButton.addActionListener(e -> addEnrollment());
        deleteEnrollmentButton.addActionListener(e -> deleteEnrollment());

        // Add ListSelectionListener to the table to populate fields when a row is selected
        enrollmentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && enrollmentTable.getSelectedRow() != -1) {
                int selectedRow = enrollmentTable.getSelectedRow();
                // Populate the combo boxes from the selected row
                enrollStudentComboBox.setSelectedItem(enrollmentTable.getValueAt(selectedRow, 1).toString());
                enrollClassComboBox.setSelectedItem(enrollmentTable.getValueAt(selectedRow, 2).toString());
            }
        });

        return panel;
    }

    /**
     * Loads data for enrollment combo boxes (students, classes).
     * Uses SwingWorker for background database operation.
     */
    private void loadEnrollmentComboBoxes() {
        setLoading(true);
        SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> studentNames = new Vector<>();
                Vector<String> classNames = new Vector<>();

                studentNameToIdMap.clear(); // Clear maps before re-populating
                classNameToIdMap.clear(); // Use the same map as for student panel

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Students
                    ResultSet rsStudents = stmt.executeQuery("SELECT student_id, name FROM students ORDER BY name");
                    while (rsStudents.next()) {
                        int id = rsStudents.getInt("student_id");
                        String name = rsStudents.getString("name");
                        studentNames.add(name);
                        studentNameToIdMap.put(name, id);
                    }
                    rsStudents.close();
                    data.put("students", studentNames);

                    // Load Classes
                    ResultSet rsClasses = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name");
                    while (rsClasses.next()) {
                        int id = rsClasses.getInt("class_id");
                        String name = rsClasses.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rsClasses.close();
                    data.put("classes", classNames);

                } catch (SQLException e) {
                    throw new Exception("Database error loading combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    // Update combo box models on the EDT
                    SwingUtilities.invokeLater(() -> {
                        enrollStudentComboBox.setModel(new DefaultComboBoxModel<>(data.get("students")));
                        enrollClassComboBox.setModel(new DefaultComboBoxModel<>(data.get("classes")));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading enrollment combo box data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         enrollStudentComboBox.setModel(new DefaultComboBoxModel<>());
                         enrollClassComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }

    /**
     * Adds a new enrollment record to the database.
     * Uses SwingWorker for background database operation.
     */
    private void addEnrollment() {
        String studentName = (String) enrollStudentComboBox.getSelectedItem();
        String className = (String) enrollClassComboBox.getSelectedItem();

        if (studentName == null || studentName.isEmpty() || className == null || className.isEmpty()) {
            showError("Please select a Student and a Class.");
            return;
        }

        Integer studentId = studentNameToIdMap.get(studentName);
        Integer classId = classNameToIdMap.get(className);

        if (studentId == null || classId == null) {
            showError("Invalid selections. Please select a valid Student and Class.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO enrollments (student_id, class_id) VALUES (?, ?)")) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, classId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new Exception("Database error adding enrollment: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Student enrolled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEnrollmentData(); // Refresh the enrollment table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding enrollment: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: This student is already enrolled in this class.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Deletes an enrollment record from the database.
     * Deletes the selected row from the table and uses its ID.
     * Uses SwingWorker for background database operation.
     */
    private void deleteEnrollment() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an enrollment from the table to delete.");
            return;
        }

        int enrollmentId = (int) enrollmentTableModel.getValueAt(selectedRow, 0); // Get ID from the table model

        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete this enrollment (ID " + enrollmentId + ")? This will also delete associated grades. This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM enrollments WHERE enrollment_id=?")) {
                    pstmt.setInt(1, enrollmentId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Enrollment with ID " + enrollmentId + " not found.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error deleting enrollment: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Enrollment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEnrollmentData(); // Refresh the enrollment table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting enrollment: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Loads enrollment data from the database and populates the enrollment table.
     * Uses SwingWorker for background database operation.
     */
    private void loadEnrollmentData() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT e.enrollment_id, s.name AS student_name, c.class_name " +
                             "FROM enrollments e " +
                             "JOIN students s ON e.student_id = s.student_id " +
                             "JOIN classes c ON e.class_id = c.class_id " +
                             "ORDER BY e.enrollment_id")) {
                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("enrollment_id"),
                                rs.getString("student_name"),
                                rs.getString("class_name")
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading enrollment data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    enrollmentTableModel.setRowCount(0);
                    for (Object[] row : data) {
                        enrollmentTableModel.addRow(row);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading enrollment data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    // --- Performance Reports Panel ---
    private JPanel createPerformanceReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Control Panel for selecting report criteria
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(new Color(250, 250, 250));

        controlPanel.add(new JLabel("Class:"));
        reportClassComboBox = new JComboBox<>();
        reportClassComboBox.setPreferredSize(new Dimension(150, reportClassComboBox.getPreferredSize().height));
        controlPanel.add(reportClassComboBox);

        controlPanel.add(new JLabel("Subject:"));
        reportSubjectComboBox = new JComboBox<>();
        reportSubjectComboBox.setPreferredSize(new Dimension(150, reportSubjectComboBox.getPreferredSize().height));
        controlPanel.add(reportSubjectComboBox);

        controlPanel.add(new JLabel("Term:"));
        reportTermComboBox = new JComboBox<>();
        reportTermComboBox.setPreferredSize(new Dimension(100, reportTermComboBox.getPreferredSize().height));
        controlPanel.add(reportTermComboBox);

        generateReportButton = new JButton("Generate Report");
        styleButton(generateReportButton, new Color(0, 123, 255)); // Blue
        controlPanel.add(generateReportButton);

        printReportButton = new JButton("Print Report");
        styleButton(printReportButton, new Color(108, 117, 125)); // Grey
        controlPanel.add(printReportButton);


        // Report Display Area
        reportDisplayArea = new JTextArea();
        reportDisplayArea.setEditable(false);
        reportDisplayArea.setWrapStyleWord(true);
        reportDisplayArea.setLineWrap(true);
        reportDisplayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Monospaced font for alignment
        JScrollPane reportScrollPane = new JScrollPane(reportDisplayArea);
        reportScrollPane.setBorder(BorderFactory.createTitledBorder("Performance Report"));


        // Add components to the main panel
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(reportScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        generateReportButton.addActionListener(e -> generatePerformanceReport());
        printReportButton.addActionListener(e -> printPerformanceReport());

        // Add listeners to combo boxes to update dependent combo boxes
        reportClassComboBox.addActionListener(e -> populateReportSubjectComboBox());
        reportSubjectComboBox.addActionListener(e -> populateReportTermComboBox());


        return panel;
    }

    /**
     * Loads data for report combo boxes (classes, subjects, terms).
     * Uses SwingWorker for background database operation.
     */
    private void loadReportComboBoxes() {
        setLoading(true);
        SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> classNames = new Vector<>();
                Vector<String> subjectNames = new Vector<>();
                Vector<String> terms = new Vector<>();

                classNameToIdMap.clear(); // Clear maps before re-populating
                subjectNameToIdMap.clear();

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Classes
                    ResultSet rsClasses = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name");
                    while (rsClasses.next()) {
                        int id = rsClasses.getInt("class_id");
                        String name = rsClasses.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rsClasses.close();
                    data.put("classes", classNames);

                    // Load Subjects
                    ResultSet rsSubjects = stmt.executeQuery("SELECT subject_id, subject_name FROM subjects ORDER BY subject_name");
                    while (rsSubjects.next()) {
                        int id = rsSubjects.getInt("subject_id");
                        String name = rsSubjects.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rsSubjects.close();
                    data.put("subjects", subjectNames);

                    // Load Terms from Grades table
                    ResultSet rsTerms = stmt.executeQuery("SELECT DISTINCT term FROM grades ORDER BY term");
                    while (rsTerms.next()) {
                        terms.add(rsTerms.getString("term"));
                    }
                    rsTerms.close();
                    data.put("terms", terms);

                } catch (SQLException e) {
                    throw new Exception("Database error loading report combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    SwingUtilities.invokeLater(() -> {
                        reportClassComboBox.setModel(new DefaultComboBoxModel<>(data.get("classes")));
                        reportSubjectComboBox.setModel(new DefaultComboBoxModel<>(data.get("subjects")));
                        reportTermComboBox.setModel(new DefaultComboBoxModel<>(data.get("terms")));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading report combo box data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         reportClassComboBox.setModel(new DefaultComboBoxModel<>());
                         reportSubjectComboBox.setModel(new DefaultComboBoxModel<>());
                         reportTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }

    /**
     * Populates the report subject combo box based on the selected class.
     * This helps filter subjects relevant to the selected class.
     * Uses SwingWorker for background database operation.
     */
    private void populateReportSubjectComboBox() {
        String selectedClass = (String) reportClassComboBox.getSelectedItem();
        if (selectedClass == null || selectedClass.isEmpty()) {
            reportSubjectComboBox.setModel(new DefaultComboBoxModel<>()); // Clear subjects if no class is selected
            reportTermComboBox.setModel(new DefaultComboBoxModel<>()); // Clear terms as well
            return;
        }

        Integer classId = classNameToIdMap.get(selectedClass);
        if (classId == null) {
            reportSubjectComboBox.setModel(new DefaultComboBoxModel<>());
            reportTermComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        setLoading(true);
        SwingWorker<Vector<String>, Void> worker = new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() throws Exception {
                Vector<String> subjectNames = new Vector<>();
                subjectNameToIdMap.clear(); // Clear map

                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT DISTINCT s.subject_id, s.subject_name FROM subjects s JOIN class_assignments ca ON s.subject_id = ca.subject_id WHERE ca.class_id = ? ORDER BY s.subject_name")) {
                    pstmt.setInt(1, classId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("subject_id");
                        String name = rs.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading subjects for report: " + e.getMessage(), e);
                }
                return subjectNames;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Vector<String> subjectNames = get();
                    SwingUtilities.invokeLater(() -> {
                        reportSubjectComboBox.setModel(new DefaultComboBoxModel<>(subjectNames));
                        // After populating subjects, trigger populating terms based on the new subject selection
                        populateReportTermComboBox(); // Call the next method
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading subjects for report: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                         reportSubjectComboBox.setModel(new DefaultComboBoxModel<>());
                         reportTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }

    /**
     * Populates the report term combo box based on the selected class and subject.
     * This helps filter terms relevant to the selected class and subject.
     * Uses SwingWorker for background database operation.
     */
    private void populateReportTermComboBox() {
        String selectedClass = (String) reportClassComboBox.getSelectedItem();
        String selectedSubject = (String) reportSubjectComboBox.getSelectedItem();

        if (selectedClass == null || selectedClass.isEmpty() || selectedSubject == null || selectedSubject.isEmpty()) {
            reportTermComboBox.setModel(new DefaultComboBoxModel<>()); // Clear terms if criteria are missing
            return;
        }

        Integer classId = classNameToIdMap.get(selectedClass);
        Integer subjectId = subjectNameToIdMap.get(selectedSubject);

        if (classId == null || subjectId == null) {
            reportTermComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        setLoading(true);
        SwingWorker<Vector<String>, Void> worker = new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() throws Exception {
                Vector<String> terms = new Vector<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT DISTINCT g.term FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id WHERE e.class_id = ? AND g.subject_id = ? ORDER BY g.term")) {
                    pstmt.setInt(1, classId);
                    pstmt.setInt(2, subjectId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        terms.add(rs.getString("term"));
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading terms for report: " + e.getMessage(), e);
                }
                return terms;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Vector<String> terms = get();
                    SwingUtilities.invokeLater(() -> {
                        reportTermComboBox.setModel(new DefaultComboBoxModel<>(terms));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading terms for report: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     SwingUtilities.invokeLater(() -> {
                         reportTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }


    /**
     * Generates a performance report for a selected class, subject, and term.
     * Displays the report in the reportDisplayArea.
     * Uses SwingWorker for background database operation.
     */
    private void generatePerformanceReport() {
        String selectedClass = (String) reportClassComboBox.getSelectedItem();
        String selectedSubject = (String) reportSubjectComboBox.getSelectedItem();
        String selectedTerm = (String) reportTermComboBox.getSelectedItem();

        if (selectedClass == null || selectedClass.isEmpty() || selectedSubject == null || selectedSubject.isEmpty() || selectedTerm == null || selectedTerm.isEmpty()) {
            showError("Please select a Class, Subject, and Term to generate a report.");
            return;
        }

        Integer classId = classNameToIdMap.get(selectedClass);
        Integer subjectId = subjectNameToIdMap.get(selectedSubject);

        if (classId == null || subjectId == null) {
            showError("Invalid selections. Please select valid Class, Subject, and Term.");
            return;
        }

        setLoading(true);
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder reportContent = new StringBuilder();
                reportContent.append(String.format("--- Performance Report for %s - %s (%s) ---\n\n", selectedClass, selectedSubject, selectedTerm));
                reportContent.append(String.format("%-5s %-20s %-10s %s\n", "ID", "Student Name", "Score", "Comments"));
                reportContent.append("--------------------------------------------------------\n");

                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT s.student_id, s.name AS student_name, g.score, g.comments " +
                         "FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id JOIN students s ON e.student_id = s.student_id " +
                         "WHERE e.class_id = ? AND g.subject_id = ? AND g.term = ? ORDER BY s.name")) {
                    pstmt.setInt(1, classId);
                    pstmt.setInt(2, subjectId);
                    pstmt.setString(3, selectedTerm);
                    ResultSet rs = pstmt.executeQuery();

                    List<Double> scores = new ArrayList<>();
                    while (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String studentName = rs.getString("student_name");
                        Double score = rs.getDouble("score");
                        String comments = rs.getString("comments");
                        reportContent.append(String.format("%-5d %-20s %-10.2f %s\n", studentId, studentName, score, comments != null ? comments : ""));
                        scores.add(score);
                    }
                    rs.close();

                    // Calculate average score
                    if (!scores.isEmpty()) {
                        double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        reportContent.append("--------------------------------------------------------\n");
                        reportContent.append(String.format("Average Score: %.2f\n", averageScore));
                    } else {
                        reportContent.append("No grades recorded for this criteria.\n");
                    }

                } catch (SQLException e) {
                    throw new Exception("Database error generating report: " + e.getMessage(), e);
                }
                return reportContent.toString();
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    String report = get();
                    SwingUtilities.invokeLater(() -> reportDisplayArea.setText(report));
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error generating report: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> reportDisplayArea.setText("Error generating report: " + errorMessage));
                }
            }
        }.execute();
    }

    /**
     * Prints the content of the reportDisplayArea.
     * Uses a simple JOptionPane for preview.
     */
    private void printPerformanceReport() {
        String reportContent = reportDisplayArea.getText();

        if (reportContent == null || reportContent.trim().isEmpty() || reportContent.contains("Error generating report")) {
            JOptionPane.showMessageDialog(this, "No valid report to print.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea printArea = new JTextArea(reportContent);
        printArea.setEditable(false);
        printArea.setWrapStyleWord(true);
        printArea.setLineWrap(true);
        printArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Maintain monospaced font for printing
        JScrollPane printScrollPane = new JScrollPane(printArea);
        printScrollPane.setPreferredSize(new Dimension(600, 400));

        // Simple print preview using JOptionPane
        JOptionPane.showMessageDialog(this, printScrollPane, "Print Preview", JOptionPane.PLAIN_MESSAGE);

        // For actual printing, you would use Java's Printing API (java.awt.print)
        // Example (basic):
        // try {
        //     boolean complete = printArea.print();
        //     if (complete) {
        //         System.out.println("Printing complete.");
        //     } else {
        //         System.out.println("Printing cancelled.");
        //     }
        // } catch (java.awt.print.PrinterException ex) {
        //     System.err.println("Error during printing: " + ex.getMessage());
        //     ex.printStackTrace();
        // }
    }


    // --- Student Promotion Panel ---
    private JPanel createStudentPromotionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel fromLabel = new JLabel("Promote Students From Class:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(fromLabel, gbc);

        promoteFromClassComboBox = new JComboBox<>();
        promoteFromClassComboBox.setPreferredSize(new Dimension(200, promoteFromClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(promoteFromClassComboBox, gbc);

        JLabel toLabel = new JLabel("Promote Students To Class:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(toLabel, gbc);

        promoteToClassComboBox = new JComboBox<>();
        promoteToClassComboBox.setPreferredSize(new Dimension(200, promoteToClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(promoteToClassComboBox, gbc);

        promoteStudentsButton = new JButton("Promote Students");
        styleButton(promoteStudentsButton, new Color(40, 167, 69)); // Green
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(promoteStudentsButton, gbc);

        // --- Action Listener ---
        promoteStudentsButton.addActionListener(e -> promoteStudents());

        return panel;
    }

    /**
     * Loads class data for the promotion combo boxes.
     * Uses SwingWorker for background database operation.
     */
    private void loadPromotionComboBoxes() {
        setLoading(true);
        SwingWorker<Vector<String>, Void> worker = new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() throws Exception {
                Vector<String> classNames = new Vector<>();
                classNameToIdMap.clear(); // Clear map

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name")) {
                    while (rs.next()) {
                        int id = rs.getInt("class_id");
                        String name = rs.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading classes for promotion: " + e.getMessage(), e);
                }
                return classNames;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Vector<String> classNames = get();
                    SwingUtilities.invokeLater(() -> {
                        promoteFromClassComboBox.setModel(new DefaultComboBoxModel<>(classNames));
                        promoteToClassComboBox.setModel(new DefaultComboBoxModel<>(classNames));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading classes for promotion: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     SwingUtilities.invokeLater(() -> {
                         promoteFromClassComboBox.setModel(new DefaultComboBoxModel<>());
                         promoteToClassComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }


    /**
     * Promotes students from one class to another by updating their class_id in the database.
     * Uses SwingWorker for background database operation.
     */
    private void promoteStudents() {
        String fromClassName = (String) promoteFromClassComboBox.getSelectedItem();
        String toClassName = (String) promoteToClassComboBox.getSelectedItem();

        if (fromClassName == null || fromClassName.isEmpty() || toClassName == null || toClassName.isEmpty()) {
            showError("Please select both a 'From' class and a 'To' class.");
            return;
        }

        if (fromClassName.equals(toClassName)) {
            showError("'From' class and 'To' class cannot be the same.");
            return;
        }

        Integer fromClassId = classNameToIdMap.get(fromClassName);
        Integer toClassId = classNameToIdMap.get(toClassName);

        if (fromClassId == null || toClassId == null) {
            showError("Invalid class selections. Please select valid classes.");
            return;
        }

        // Show a confirmation dialog
        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to promote all students from '" + fromClassName + "' to '" + toClassName + "'?",
                "Confirm Promotion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        setLoading(true);
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int promotedCount = 0;
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE students SET class_id = ? WHERE class_id = ?")) {
                    pstmt.setInt(1, toClassId);
                    pstmt.setInt(2, fromClassId);
                    promotedCount = pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new Exception("Database error during student promotion: " + e.getMessage(), e);
                }
                return promotedCount; // Return the number of students promoted
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    int promotedCount = get();
                    JOptionPane.showMessageDialog(AdminPanel.this,
                            promotedCount + " student(s) promoted successfully from " + fromClassName + " to " + toClassName + ".",
                            "Promotion Complete", JOptionPane.INFORMATION_MESSAGE);
                    // Optionally refresh student data if the student tab is active
                    if (adminTabbedPane.getSelectedIndex() == 0) { // Check if Student tab is active
                         loadStudentData();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error promoting students: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    // --- Fee Management Panel ---
    private JPanel createFeeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for managing fee payments
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Fee Payment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Fee ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        feeIdField = new JTextField(15);
        feeIdField.setEditable(false);
        feeIdField.setBackground(new Color(235, 235, 235));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(feeIdField, gbc);

        JLabel studentLabel = new JLabel("Student:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(studentLabel, gbc);
        feeStudentComboBox = new JComboBox<>();
        feeStudentComboBox.setPreferredSize(new Dimension(200, feeStudentComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(feeStudentComboBox, gbc);

        JLabel classLabel = new JLabel("Class:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(classLabel, gbc);
        feeClassComboBox = new JComboBox<>();
        feeClassComboBox.setPreferredSize(new Dimension(200, feeClassComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(feeClassComboBox, gbc);

        JLabel termLabel = new JLabel("Term:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(termLabel, gbc);
        feeTermComboBox = new JComboBox<>();
        feeTermComboBox.setPreferredSize(new Dimension(150, feeTermComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(feeTermComboBox, gbc);

        JLabel amountDueLabel = new JLabel("Amount Due:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(amountDueLabel, gbc);
        feeAmountDueField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(feeAmountDueField, gbc);

        JLabel amountPaidLabel = new JLabel("Amount Paid:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(amountPaidLabel, gbc);
        feeAmountPaidField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(feeAmountPaidField, gbc);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        addFeeButton = new JButton("Add Fee Record");
        styleButton(addFeeButton, new Color(40, 167, 69)); // Green
        updateFeeButton = new JButton("Update Fee Record");
        styleButton(updateFeeButton, new Color(0, 123, 255)); // Blue
        deleteFeeButton = new JButton("Delete Fee Record");
        styleButton(deleteFeeButton, new Color(220, 53, 69)); // Red
        clearFeeFields = new JButton("Clear Fields");
        styleButton(clearFeeFields, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addFeeButton);
        buttonPanel.add(updateFeeButton);
        buttonPanel.add(deleteFeeButton);
        buttonPanel.add(clearFeeFields);
        gbc.gridx = 0;
        gbc.gridy = 6; // Adjusted gridy
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // Table Panel to display fee records
        feeTableModel = new DefaultTableModel(new Object[]{"ID", "Student", "Class", "Term", "Amount Due", "Amount Paid", "Date Last Paid"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        feeTable = new JTable(feeTableModel);
        feeTable.setFillsViewportHeight(true);
        feeTable.setRowHeight(25);
        feeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane feeScrollPane = new JScrollPane(feeTable);
        feeScrollPane.setBorder(BorderFactory.createTitledBorder("Fee Records List"));


        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(feeScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addFeeButton.addActionListener(e -> addFee());
        updateFeeButton.addActionListener(e -> updateFee());
        deleteFeeButton.addActionListener(e -> deleteFee());
        clearFeeFields.addActionListener(e -> clearFeeFields());

        // Add ListSelectionListener to the table to populate fields when a row is selected
        feeTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && feeTable.getSelectedRow() != -1) {
                int selectedRow = feeTable.getSelectedRow();
                // Populate the fields from the selected row
                feeIdField.setText(feeTable.getValueAt(selectedRow, 0).toString());
                feeStudentComboBox.setSelectedItem(feeTable.getValueAt(selectedRow, 1).toString());
                feeClassComboBox.setSelectedItem(feeTable.getValueAt(selectedRow, 2).toString());
                feeTermComboBox.setSelectedItem(feeTable.getValueAt(selectedRow, 3).toString());
                feeAmountDueField.setText(feeTable.getValueAt(selectedRow, 4).toString());
                feeAmountPaidField.setText(feeTable.getValueAt(selectedRow, 5).toString());
                // Date Last Paid is for display only, or could be handled with a DatePicker if needed for input
            }
        });

        return panel;
    }

    /**
     * Loads data for fee combo boxes (students, classes, terms).
     * Uses SwingWorker for background database operation.
     */
    private void loadFeeComboBoxes() {
         setLoading(true);
         SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> studentNames = new Vector<>();
                Vector<String> classNames = new Vector<>();
                Vector<String> terms = new Vector<>();

                studentNameToIdMap.clear(); // Clear maps before re-populating
                classNameToIdMap.clear();

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Students
                    ResultSet rsStudents = stmt.executeQuery("SELECT student_id, name FROM students ORDER BY name");
                    while (rsStudents.next()) {
                        int id = rsStudents.getInt("student_id");
                        String name = rsStudents.getString("name");
                        studentNames.add(name);
                        studentNameToIdMap.put(name, id);
                    }
                    rsStudents.close();
                    data.put("students", studentNames);

                    // Load Classes
                    ResultSet rsClasses = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name");
                    while (rsClasses.next()) {
                        int id = rsClasses.getInt("class_id");
                        String name = rsClasses.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rsClasses.close();
                    data.put("classes", classNames);

                    // Load Terms from Grades table (or define a standard set of terms)
                    ResultSet rsTerms = stmt.executeQuery("SELECT DISTINCT term FROM grades ORDER BY term"); // Using terms from grades for consistency
                    while (rsTerms.next()) {
                        terms.add(rsTerms.getString("term"));
                    }
                    rsTerms.close();
                    data.put("terms", terms);

                } catch (SQLException e) {
                    throw new Exception("Database error loading fee combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    SwingUtilities.invokeLater(() -> {
                        feeStudentComboBox.setModel(new DefaultComboBoxModel<>(data.get("students")));
                        feeClassComboBox.setModel(new DefaultComboBoxModel<>(data.get("classes")));
                        feeTermComboBox.setModel(new DefaultComboBoxModel<>(data.get("terms")));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading fee combo box data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         feeStudentComboBox.setModel(new DefaultComboBoxModel<>());
                         feeClassComboBox.setModel(new DefaultComboBoxModel<>());
                         feeTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
         }.execute();
    }


    /**
     * Adds a new fee record to the database.
     * Uses SwingWorker for background database operation.
     */
    private void addFee() {
        String studentName = (String) feeStudentComboBox.getSelectedItem();
        String className = (String) feeClassComboBox.getSelectedItem();
        String term = (String) feeTermComboBox.getSelectedItem();
        String amountDueStr = feeAmountDueField.getText().trim();
        String amountPaidStr = feeAmountPaidField.getText().trim();

        if (studentName == null || studentName.isEmpty() || className == null || className.isEmpty() || term == null || term.isEmpty() || amountDueStr.isEmpty() || amountPaidStr.isEmpty()) {
            showError("Please select a Student, Class, Term, and enter Amount Due and Amount Paid.");
            return;
        }

        Integer studentId = studentNameToIdMap.get(studentName);
        Integer classId = classNameToIdMap.get(className);

        if (studentId == null || classId == null) {
            showError("Invalid selections. Please select a valid Student and Class.");
            return;
        }

        BigDecimal amountDue, amountPaid;
        try {
            amountDue = new BigDecimal(amountDueStr);
            amountPaid = new BigDecimal(amountPaidStr);
             if (amountDue.compareTo(BigDecimal.ZERO) < 0 || amountPaid.compareTo(BigDecimal.ZERO) < 0) {
                 showError("Amounts cannot be negative.");
                 return;
             }
             if (amountPaid.compareTo(amountDue) > 0) {
                 showError("Amount Paid cannot exceed Amount Due.");
                 return;
             }
        } catch (NumberFormatException e) {
            showError("Invalid amount format. Please enter valid numbers for Amount Due and Amount Paid.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO fees (student_id, class_id, term, amount_due, amount_paid, date_last_paid) VALUES (?, ?, ?, ?, ?, ?)")) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, classId);
                    pstmt.setString(3, term);
                    pstmt.setBigDecimal(4, amountDue); // Use setBigDecimal
                    pstmt.setBigDecimal(5, amountPaid); // Use setBigDecimal
                    // Set date_last_paid to current date if amount paid is greater than 0
                    if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                         pstmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                    } else {
                         pstmt.setNull(6, java.sql.Types.DATE);
                    }

                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new Exception("Database error adding fee record: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Fee record added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFeeFields();
                    loadFeeData(); // Refresh the fee table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding fee record: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A fee record for this student, class, and term already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Updates an existing fee record in the database based on the ID in the feeIdField.
     * Uses SwingWorker for background database operation.
     */
    private void updateFee() {
        String idStr = feeIdField.getText().trim();
        String studentName = (String) feeStudentComboBox.getSelectedItem();
        String className = (String) feeClassComboBox.getSelectedItem();
        String term = (String) feeTermComboBox.getSelectedItem();
        String amountDueStr = feeAmountDueField.getText().trim();
        String amountPaidStr = feeAmountPaidField.getText().trim();

        if (idStr.isEmpty() || studentName == null || studentName.isEmpty() || className == null || className.isEmpty() || term == null || term.isEmpty() || amountDueStr.isEmpty() || amountPaidStr.isEmpty()) {
            showError("Please select a fee record from the table and fill in all fields.");
            return;
        }

        int feeId;
        try {
            feeId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Fee ID format. Please select a fee record from the table.");
            return;
        }

        Integer studentId = studentNameToIdMap.get(studentName);
        Integer classId = classNameToIdMap.get(className);

        if (studentId == null || classId == null) {
            showError("Invalid selections. Please select a valid Student and Class.");
            return;
        }

        BigDecimal amountDue, amountPaid;
        try {
            amountDue = new BigDecimal(amountDueStr);
            amountPaid = new BigDecimal(amountPaidStr);
             if (amountDue.compareTo(BigDecimal.ZERO) < 0 || amountPaid.compareTo(BigDecimal.ZERO) < 0) {
                 showError("Amounts cannot be negative.");
                 return;
             }
             if (amountPaid.compareTo(amountDue) > 0) {
                 showError("Amount Paid cannot exceed Amount Due.");
                 return;
             }
        } catch (NumberFormatException e) {
            showError("Invalid amount format. Please enter valid numbers for Amount Due and Amount Paid.");
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE fees SET student_id=?, class_id=?, term=?, amount_due=?, amount_paid=?, date_last_paid=? WHERE fee_id=?")) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, classId);
                    pstmt.setString(3, term);
                    pstmt.setBigDecimal(4, amountDue); // Use setBigDecimal
                    pstmt.setBigDecimal(5, amountPaid); // Use setBigDecimal
                     // Update date_last_paid if amount paid is greater than 0
                    if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                         pstmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                    } else {
                         pstmt.setNull(6, java.sql.Types.DATE);
                    }
                    pstmt.setInt(7, feeId);

                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new Exception("Fee record with ID " + feeId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating fee record: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Fee record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFeeFields();
                    loadFeeData(); // Refresh the fee table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error updating fee record: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A fee record for this student, class, and term already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Deletes a fee record from the database based on the ID in the feeIdField.
     * Uses SwingWorker for background database operation.
     */
    private void deleteFee() {
        String idStr = feeIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a fee record from the table to delete.");
            return;
        }
        int feeId;
        try {
            feeId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Fee ID format. Please select a fee record from the table.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(AdminPanel.this,
                "Are you sure you want to delete fee record with ID " + feeId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM fees WHERE fee_id=?")) {
                    pstmt.setInt(1, feeId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Fee record with ID " + feeId + " not found.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error deleting fee record: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Fee record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFeeFields();
                    loadFeeData(); // Refresh the fee table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting fee record: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Loads fee data from the database and populates the fee table.
     * Uses SwingWorker for background database operation.
     */
    private void loadFeeData() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT f.fee_id, s.name AS student_name, c.class_name, f.term, f.amount_due, f.amount_paid, f.date_last_paid " +
                             "FROM fees f " +
                             "JOIN students s ON f.student_id = s.student_id " +
                             "JOIN classes c ON f.class_id = c.class_id " +
                             "ORDER BY f.fee_id")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // For formatting date
                    while (rs.next()) {
                        Date dateLastPaid = rs.getDate("date_last_paid");
                        Object[] row = {
                                rs.getInt("fee_id"),
                                rs.getString("student_name"),
                                rs.getString("class_name"),
                                rs.getString("term"),
                                rs.getBigDecimal("amount_due"), // Use getBigDecimal
                                rs.getBigDecimal("amount_paid"), // Use getBigDecimal
                                dateLastPaid != null ? dateFormat.format(dateLastPaid) : "N/A" // Format date or show N/A
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading fee data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    feeTableModel.setRowCount(0);
                    for (Object[] row : data) {
                        feeTableModel.addRow(row);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading fee data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Clears the input fields in the Fee Management panel.
     */
    private void clearFeeFields() {
        feeIdField.setText("");
        // Reset combo boxes to the first item or a default if available
        if (feeStudentComboBox.getItemCount() > 0) feeStudentComboBox.setSelectedIndex(0);
        if (feeClassComboBox.getItemCount() > 0) feeClassComboBox.setSelectedIndex(0);
        if (feeTermComboBox.getItemCount() > 0) feeTermComboBox.setSelectedIndex(0);
        feeAmountDueField.setText("");
        feeAmountPaidField.setText("");
        feeTable.clearSelection(); // Clear table selection
    }

} // End of AdminPanel class
