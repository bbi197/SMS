package com.school.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Date; // Needed for java.sql.Date conversion
import java.text.SimpleDateFormat; // For formatting date - uncomment if used

// Import the main application class to access the logout method
import com.school.SchoolManagementApp; // This import should be here
import com.school.DatabaseUtility; // Ensure this import is present
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List
import java.util.Collections; // Import Collections for sorting
import com.formdev.flatlaf.FlatClientProperties; // Import for FlatLaf specific properties
import java.math.BigDecimal; // Use BigDecimal for score

/**
 * TeacherPanel provides the interface for teachers to manage student grades
 * and view reports for the classes and subjects they are assigned to.
 * It interacts with the database using DatabaseUtility and performs operations
 * in the background using SwingWorker for a responsive UI.
 */
public class TeacherPanel extends JPanel {
    // private final String dbUrl = "jdbc:mysql://localhost:3306/school_db"; // Removed: Use DatabaseUtility
    // private final String dbUser = "your_username"; // Removed: Use DatabaseUtility
    // private final String dbPass = "your_password"; // Removed: Use DatabaseUtility
    private final int loggedInTeacherId; // Store the logged-in teacher's ID
    private SchoolManagementApp parentFrame; // Reference to the main application frame

    private JTabbedPane teacherTabbedPane;

    // Panels for different sections
    private JPanel gradeManagementPanel;
    private JPanel teacherReportsPanel; // Panel for teacher-specific reports


    // --- Grade Management Components ---
    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;
    private JComboBox<String> gradeSubjectComboBox, gradeStudentComboBox, gradeTermComboBox; // Combo boxes for filtering/adding grades
    private JTextField gradeIdField, scoreField, commentField; // Fields for grade details
    private JButton addGradeButton, updateGradeButton, deleteGradeButton, clearGradeFieldsButton; // Buttons for CRUD operations
    private Map<String, Integer> subjectNameToIdMap = new HashMap<>(); // Map subject name to ID
    private Map<String, Integer> studentNameToIdMap = new HashMap<>(); // Map student name to ID
    private Map<String, Integer> enrollmentIdMap = new HashMap<>(); // Map studentName + className + subjectName + term to enrollment_id

    // --- Teacher Reports Components ---
    private JComboBox<String> reportSubjectComboBox, reportTermComboBox, reportClassComboBox; // Combo boxes for reports
    private JTextArea teacherReportDisplayArea; // Area to display reports
    private JButton generateTeacherReportButton, printTeacherReportButton; // Buttons for reports
    private Map<String, Integer> classNameToIdMap = new HashMap<>(); // Map class name to ID (reused)


    // --- Loading Indicator ---
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private JPanel loadingPanel; // Panel to hold loading indicator


    /**
     * Constructor for the TeacherPanel.
     *
     * @param parentFrame The main application frame.
     * @param teacherId   The ID of the logged-in teacher.
     */
    public TeacherPanel(SchoolManagementApp parentFrame, int teacherId) {
        this.parentFrame = parentFrame; // Store reference to the parent frame
        this.loggedInTeacherId = teacherId; // Store the logged-in teacher's ID

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Light grey background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        teacherTabbedPane = new JTabbedPane();
        teacherTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold font for tabs

        setupLoadingIndicator(); // Setup the loading bar and label panel

        // Initialize panels
        gradeManagementPanel = createGradeManagementPanel();
        teacherReportsPanel = createTeacherReportsPanel(); // Create Reports Panel

        // Add panels to the tabbed pane
        teacherTabbedPane.addTab("Manage Grades", gradeManagementPanel);
        teacherTabbedPane.addTab("Reports", teacherReportsPanel); // Add Reports Tab

        add(teacherTabbedPane, BorderLayout.CENTER);

        // Add a ChangeListener to the tabbed pane to load data when a tab is selected
        teacherTabbedPane.addChangeListener(e -> {
            int selectedIndex = teacherTabbedPane.getSelectedIndex();
            // Load data for the selected tab
            switch (selectedIndex) {
                case 0: // Manage Grades
                    loadGradeData(); // Load data for the table
                    loadGradeComboBoxes(); // Load data for combo boxes
                    break;
                case 1: // Reports
                    loadReportComboBoxes(); // Load data for combo boxes
                    break;
            }
        });

        // Load data for the initially selected tab (Manage Grades, index 0)
        loadGradeData();
        loadGradeComboBoxes();


        // Add Logout button to a panel at the top
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align to the right
        topControlPanel.setBackground(new Color(245, 245, 245)); // Match background
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Smaller font for logout
        styleButton(logoutButton, new Color(220, 53, 69)); // Red background

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
                teacherTabbedPane.setEnabled(!loading);
            } else {
                progressBar.setVisible(false);
                loadingLabel.setVisible(false);
                // Remove the loading panel after the loading is complete
                if (loadingPanel.getParent() == this) { // Check if it's currently added to this panel
                    remove(loadingPanel);
                }
                 // Optional: Re-enable UI elements
                teacherTabbedPane.setEnabled(!loading);
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


    // --- Grade Management Panel ---
    private JPanel createGradeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Form Panel for adding/updating grade details
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));
        formPanel.setBorder(BorderFactory.createTitledBorder("Grade Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel idLabel = new JLabel("Grade ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        gradeIdField = new JTextField(15);
        gradeIdField.setEditable(false); // ID is auto-generated and for display when updating
        gradeIdField.setBackground(new Color(235, 235, 235)); // Light grey background for non-editable field
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(gradeIdField, gbc);

        JLabel subjectLabel = new JLabel("Subject:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(subjectLabel, gbc);
        gradeSubjectComboBox = new JComboBox<>();
        gradeSubjectComboBox.setPreferredSize(new Dimension(200, gradeSubjectComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(gradeSubjectComboBox, gbc);

        JLabel studentLabel = new JLabel("Student:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(studentLabel, gbc);
        gradeStudentComboBox = new JComboBox<>();
        gradeStudentComboBox.setPreferredSize(new Dimension(200, gradeStudentComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(gradeStudentComboBox, gbc);

        JLabel termLabel = new JLabel("Term:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(termLabel, gbc);
        gradeTermComboBox = new JComboBox<>();
        gradeTermComboBox.setPreferredSize(new Dimension(150, gradeTermComboBox.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(gradeTermComboBox, gbc);


        JLabel scoreLabel = new JLabel("Score:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(scoreLabel, gbc);
        scoreField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(scoreField, gbc);

        JLabel commentLabel = new JLabel("Comments:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(commentLabel, gbc);
        commentField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(commentField, gbc);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        addGradeButton = new JButton("Add Grade");
        styleButton(addGradeButton, new Color(40, 167, 69)); // Green
        updateGradeButton = new JButton("Update Grade");
        styleButton(updateGradeButton, new Color(0, 123, 255)); // Blue
        deleteGradeButton = new JButton("Delete Grade");
        styleButton(deleteGradeButton, new Color(220, 53, 69)); // Red
        clearGradeFieldsButton = new JButton("Clear Fields");
        styleButton(clearGradeFieldsButton, new Color(108, 117, 125)); // Grey

        buttonPanel.add(addGradeButton);
        buttonPanel.add(updateGradeButton);
        buttonPanel.add(deleteGradeButton);
        buttonPanel.add(clearGradeFieldsButton);
        gbc.gridx = 0;
        gbc.gridy = 6; // Adjusted gridy
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // Table Panel to display grades
        gradeTableModel = new DefaultTableModel(new Object[]{"ID", "Student", "Class", "Subject", "Term", "Score", "Comments", "Date Recorded"}, 0) { // Added Class and Date Recorded
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Double.class; // Score column
                return super.getColumnClass(columnIndex);
            }
        };
        gradeTable = new JTable(gradeTableModel);
        gradeTable.setFillsViewportHeight(true);
        gradeTable.setRowHeight(25);
        gradeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane gradeScrollPane = new JScrollPane(gradeTable);
        gradeScrollPane.setBorder(BorderFactory.createTitledBorder("Grades List"));


        // Add components to the main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(gradeScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addGradeButton.addActionListener(e -> addGrade());
        updateGradeButton.addActionListener(e -> updateGrade());
        deleteGradeButton.addActionListener(e -> deleteGrade());
        clearGradeFieldsButton.addActionListener(e -> clearGradeFields());

        // Add listeners to combo boxes to update dependent combo boxes
        gradeSubjectComboBox.addActionListener(e -> populateGradeStudentComboBox());
        gradeStudentComboBox.addActionListener(e -> populateGradeTermComboBox()); // Populate terms based on student and subject


        // Add ListSelectionListener to the table to populate fields when a row is selected
        gradeTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && gradeTable.getSelectedRow() != -1) {
                int selectedRow = gradeTable.getSelectedRow();
                // Populate the fields and combo boxes from the selected row
                gradeIdField.setText(gradeTable.getValueAt(selectedRow, 0).toString());
                // Select the correct items in the combo boxes
                gradeSubjectComboBox.setSelectedItem(gradeTable.getValueAt(selectedRow, 3).toString());
                gradeStudentComboBox.setSelectedItem(gradeTable.getValueAt(selectedRow, 1).toString());
                gradeTermComboBox.setSelectedItem(gradeTable.getValueAt(selectedRow, 4).toString());
                scoreField.setText(gradeTable.getValueAt(selectedRow, 5).toString());
                // Handle potential null comments gracefully
                Object commentsValue = gradeTable.getValueAt(selectedRow, 6);
                commentField.setText(commentsValue != null ? commentsValue.toString() : "");
            }
        });


        return panel;
    }

    /**
     * Loads data for grade management combo boxes (subjects, students, terms)
     * relevant to the logged-in teacher's assignments.
     * Uses SwingWorker for background database operation.
     */
    private void loadGradeComboBoxes() {
        setLoading(true);
        SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> subjectNames = new Vector<>();
                Vector<String> studentNames = new Vector<>();
                Vector<String> terms = new Vector<>();

                subjectNameToIdMap.clear(); // Clear maps before re-populating
                studentNameToIdMap.clear();
                classNameToIdMap.clear(); // Also need class names for enrollment ID mapping
                enrollmentIdMap.clear(); // Clear enrollment map

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Subjects assigned to this teacher
                    ResultSet rsSubjects = stmt.executeQuery(
                        "SELECT DISTINCT s.subject_id, s.subject_name FROM subjects s " +
                        "JOIN class_assignments ca ON s.subject_id = ca.subject_id " +
                        "WHERE ca.teacher_id = " + loggedInTeacherId + " ORDER BY s.subject_name");
                    while (rsSubjects.next()) {
                        int id = rsSubjects.getInt("subject_id");
                        String name = rsSubjects.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rsSubjects.close();
                    data.put("subjects", subjectNames);

                    // Load Students enrolled in classes assigned to this teacher
                    // This is a bit complex as a teacher might teach multiple subjects in multiple classes.
                    // We need students from all classes the teacher is assigned to, for any subject.
                    ResultSet rsStudents = stmt.executeQuery(
                         "SELECT DISTINCT s.student_id, s.name FROM students s " +
                         "JOIN enrollments e ON s.student_id = e.student_id " +
                         "JOIN class_assignments ca ON e.class_id = ca.class_id " +
                         "WHERE ca.teacher_id = " + loggedInTeacherId + " ORDER BY s.name");
                    while (rsStudents.next()) {
                        int id = rsStudents.getInt("student_id");
                        String name = rsStudents.getString("name");
                        studentNames.add(name);
                        studentNameToIdMap.put(name, id);
                    }
                    rsStudents.close();
                    data.put("students", studentNames);

                    // Load Terms from Grades table (or define a standard set of terms)
                    // We can load all terms that exist in the grades table for simplicity,
                    // or filter by terms relevant to the teacher's classes/subjects if needed.
                    ResultSet rsTerms = stmt.executeQuery("SELECT DISTINCT term FROM grades ORDER BY term");
                    while (rsTerms.next()) {
                        terms.add(rsTerms.getString("term"));
                    }
                    rsTerms.close();
                    data.put("terms", terms);

                    // Load Class names for enrollment ID mapping
                     ResultSet rsClasses = stmt.executeQuery("SELECT class_id, class_name FROM classes ORDER BY class_name");
                     while (rsClasses.next()) {
                         int id = rsClasses.getInt("class_id");
                         String name = rsClasses.getString("class_name");
                         classNameToIdMap.put(name, id);
                     }
                     rsClasses.close();


                } catch (SQLException e) {
                    throw new Exception("Database error loading grade combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    SwingUtilities.invokeLater(() -> {
                        gradeSubjectComboBox.setModel(new DefaultComboBoxModel<>(data.get("subjects")));
                        gradeStudentComboBox.setModel(new DefaultComboBoxModel<>(data.get("students")));
                        gradeTermComboBox.setModel(new DefaultComboBoxModel<>(data.get("terms")));
                        // Trigger population of dependent combo boxes after initial load
                         populateGradeStudentComboBox(); // Populate students based on initial subject selection
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading grade combo box data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         gradeSubjectComboBox.setModel(new DefaultComboBoxModel<>());
                         gradeStudentComboBox.setModel(new DefaultComboBoxModel<>());
                         gradeTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }

    /**
     * Populates the grade student combo box based on the selected subject.
     * Filters students who are in classes where the teacher teaches the selected subject.
     * Uses SwingWorker for background database operation.
     */
    private void populateGradeStudentComboBox() {
        String selectedSubject = (String) gradeSubjectComboBox.getSelectedItem();
        if (selectedSubject == null || selectedSubject.isEmpty()) {
            gradeStudentComboBox.setModel(new DefaultComboBoxModel<>()); // Clear students if no subject is selected
            gradeTermComboBox.setModel(new DefaultComboBoxModel<>()); // Clear terms as well
            return;
        }

        Integer subjectId = subjectNameToIdMap.get(selectedSubject);
        if (subjectId == null) {
             gradeStudentComboBox.setModel(new DefaultComboBoxModel<>());
             gradeTermComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        setLoading(true);
        SwingWorker<Vector<String>, Void> worker = new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() throws Exception {
                Vector<String> studentNames = new Vector<>();
                studentNameToIdMap.clear(); // Clear map
                enrollmentIdMap.clear(); // Clear enrollment map

                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT DISTINCT s.student_id, s.name, e.enrollment_id, c.class_name FROM students s " +
                         "JOIN enrollments e ON s.student_id = e.student_id " +
                         "JOIN class_assignments ca ON e.class_id = ca.class_id " +
                         "JOIN classes c ON e.class_id = c.class_id " + // Join to get class name
                         "WHERE ca.teacher_id = ? AND ca.subject_id = ? ORDER BY s.name")) {
                    pstmt.setInt(1, loggedInTeacherId);
                    pstmt.setInt(2, subjectId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String studentName = rs.getString("name");
                        int enrollmentId = rs.getInt("enrollment_id");
                        String className = rs.getString("class_name");

                        studentNames.add(studentName);
                        studentNameToIdMap.put(studentName, studentId);
                        // Store enrollment ID using a composite key (studentName + className + subjectName)
                        // This is a simplified approach; a more robust way might involve a custom object or a different map structure
                        enrollmentIdMap.put(studentName + "_" + className + "_" + selectedSubject, enrollmentId);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading students for grade: " + e.getMessage(), e);
                }
                return studentNames;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Vector<String> studentNames = get();
                    SwingUtilities.invokeLater(() -> {
                        gradeStudentComboBox.setModel(new DefaultComboBoxModel<>(studentNames));
                        // After populating students, trigger populating terms based on the new selections
                        populateGradeTermComboBox(); // Call the next method
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading students for grade: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     SwingUtilities.invokeLater(() -> {
                         gradeStudentComboBox.setModel(new DefaultComboBoxModel<>());
                         gradeTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }

    /**
     * Populates the grade term combo box based on the selected subject and student.
     * Filters terms for which grades already exist for this student and subject.
     * Uses SwingWorker for background database operation.
     */
    private void populateGradeTermComboBox() {
        String selectedSubject = (String) gradeSubjectComboBox.getSelectedItem();
        String selectedStudent = (String) gradeStudentComboBox.getSelectedItem();

        if (selectedSubject == null || selectedSubject.isEmpty() || selectedStudent == null || selectedStudent.isEmpty()) {
            gradeTermComboBox.setModel(new DefaultComboBoxModel<>()); // Clear terms if criteria are missing
            return;
        }

        Integer studentId = studentNameToIdMap.get(selectedStudent);
        Integer subjectId = subjectNameToIdMap.get(selectedSubject);

        if (studentId == null || subjectId == null) {
            gradeTermComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        setLoading(true);
        SwingWorker<Vector<String>, Void> worker = new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() throws Exception {
                Vector<String> terms = new Vector<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT DISTINCT g.term FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id WHERE e.student_id = ? AND g.subject_id = ? ORDER BY g.term")) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, subjectId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        terms.add(rs.getString("term"));
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading terms for grade: " + e.getMessage(), e);
                }
                return terms;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Vector<String> terms = get();
                    SwingUtilities.invokeLater(() -> {
                        gradeTermComboBox.setModel(new DefaultComboBoxModel<>(terms));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading terms for grade: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     SwingUtilities.invokeLater(() -> {
                         gradeTermComboBox.setModel(new DefaultComboBoxModel<>());
                     });
                }
            }
        }.execute();
    }


    /**
     * Adds a new grade record to the database.
     * Uses SwingWorker for background database operation.
     */
    private void addGrade() {
        String selectedSubject = (String) gradeSubjectComboBox.getSelectedItem();
        String selectedStudent = (String) gradeStudentComboBox.getSelectedItem();
        String selectedTerm = (String) gradeTermComboBox.getSelectedItem();
        String scoreStr = scoreField.getText().trim();
        String comments = commentField.getText().trim();

        if (selectedSubject == null || selectedSubject.isEmpty() || selectedStudent == null || selectedStudent.isEmpty() || selectedTerm == null || selectedTerm.isEmpty() || scoreStr.isEmpty()) {
            showError("Please select Subject, Student, Term and enter a Score.");
            return;
        }

        Integer subjectId = subjectNameToIdMap.get(selectedSubject);
        Integer studentId = studentNameToIdMap.get(selectedStudent);

        if (subjectId == null || studentId == null) {
            showError("Invalid selections. Please select valid Subject, Student, and Term.");
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 100) {
                showError("Score must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid score format. Please enter a valid number.");
            return;
        }

        // Find the enrollment_id based on student and class.
        // A teacher might teach the same subject in different classes.
        // We need to find the correct enrollment for the selected student in the class
        // where the teacher teaches the selected subject.
        Integer enrollmentId = getEnrollmentId(studentId, subjectId, loggedInTeacherId);

        if (enrollmentId == null) {
            showError("Could not find a valid enrollment for this student and subject combination taught by you.");
            return;
        }


        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO grades (enrollment_id, subject_id, score, comments, term, date_recorded) VALUES (?, ?, ?, ?, ?, ?)")) {
                    pstmt.setInt(1, enrollmentId); // Use the found enrollment ID
                    pstmt.setInt(2, subjectId);
                    pstmt.setDouble(3, score);
                    pstmt.setString(4, comments.isEmpty() ? null : comments); // Store empty comments as NULL
                    pstmt.setString(5, selectedTerm);
                    pstmt.setDate(6, new java.sql.Date(System.currentTimeMillis())); // Record current date

                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    throw new Exception("Database error adding grade: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(TeacherPanel.this, "Grade added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearGradeFields();
                    loadGradeData(); // Refresh the grade table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error adding grade: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A grade for this student, subject, and term already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

     /**
      * Helper method to get the enrollment_id for a given student, subject, and teacher.
      * This is needed because a teacher might teach the same subject in multiple classes,
      * and we need the specific enrollment of the student in the class where this teacher
      * teaches this subject.
      *
      * @param studentId The student's ID.
      * @param subjectId The subject's ID.
      * @param teacherId The teacher's ID.
      * @return The enrollment_id, or null if not found.
      */
     private Integer getEnrollmentId(int studentId, int subjectId, int teacherId) {
         Integer enrollmentId = null;
         try (Connection conn = DatabaseUtility.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(
                  "SELECT e.enrollment_id FROM enrollments e " +
                  "JOIN class_assignments ca ON e.class_id = ca.class_id " +
                  "WHERE e.student_id = ? AND ca.subject_id = ? AND ca.teacher_id = ?")) {
             pstmt.setInt(1, studentId);
             pstmt.setInt(2, subjectId);
             pstmt.setInt(3, teacherId);
             ResultSet rs = pstmt.executeQuery();
             if (rs.next()) {
                 enrollmentId = rs.getInt("enrollment_id");
             }
             rs.close();
         } catch (SQLException e) {
             System.err.println("Database error fetching enrollment ID: " + e.getMessage());
             e.printStackTrace();
         }
         return enrollmentId;
     }


    /**
     * Updates an existing grade record in the database based on the ID in the gradeIdField.
     * Uses SwingWorker for background database operation.
     */
    private void updateGrade() {
        String idStr = gradeIdField.getText().trim();
        String selectedSubject = (String) gradeSubjectComboBox.getSelectedItem();
        String selectedStudent = (String) gradeStudentComboBox.getSelectedItem();
        String selectedTerm = (String) gradeTermComboBox.getSelectedItem();
        String scoreStr = scoreField.getText().trim();
        String comments = commentField.getText().trim();

        if (idStr.isEmpty() || selectedSubject == null || selectedSubject.isEmpty() || selectedStudent == null || selectedStudent.isEmpty() || selectedTerm == null || selectedTerm.isEmpty() || scoreStr.isEmpty()) {
            showError("Please select a grade from the table and fill in all fields.");
            return;
        }

        int gradeId;
        try {
            gradeId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Grade ID format. Please select a grade from the table.");
            return;
        }

        Integer subjectId = subjectNameToIdMap.get(selectedSubject);
        Integer studentId = studentNameToIdMap.get(selectedStudent);

        if (subjectId == null || studentId == null) {
            showError("Invalid selections. Please select valid Subject, Student, and Term.");
            return;
        }

         double score;
        try {
            score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 100) {
                showError("Score must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid score format. Please enter a valid number.");
            return;
        }

        // Find the enrollment_id based on student and class.
        Integer enrollmentId = getEnrollmentId(studentId, subjectId, loggedInTeacherId);

        if (enrollmentId == null) {
            showError("Could not find a valid enrollment for this student and subject combination taught by you.");
            return;
        }


        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE grades SET enrollment_id=?, subject_id=?, score=?, comments=?, term=?, date_recorded=? WHERE grade_id=?")) {
                    pstmt.setInt(1, enrollmentId); // Use the found enrollment ID
                    pstmt.setInt(2, subjectId);
                    pstmt.setDouble(3, score);
                    pstmt.setString(4, comments.isEmpty() ? null : comments); // Store empty comments as NULL
                    pstmt.setString(5, selectedTerm);
                    pstmt.setDate(6, new java.sql.Date(System.currentTimeMillis())); // Update date to current date
                    pstmt.setInt(7, gradeId);

                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new Exception("Grade with ID " + gradeId + " not found or no changes were made.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error updating grade: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(TeacherPanel.this, "Grade updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearGradeFields();
                    loadGradeData(); // Refresh the grade table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error updating grade: " + (cause != null ? cause.getMessage() : e.getMessage());
                     if (cause instanceof SQLException && cause.getMessage().toLowerCase().contains("duplicate entry")) {
                         errorMessage = "Error: A grade for this student, subject, and term already exists.";
                     }
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Deletes a grade record from the database based on the ID in the gradeIdField.
     * Uses SwingWorker for background database operation.
     */
    private void deleteGrade() {
        String idStr = gradeIdField.getText().trim();
        if (idStr.isEmpty()) {
            showError("Please select a grade from the table to delete.");
            return;
        }
        int gradeId;
        try {
            gradeId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("Invalid Grade ID format. Please select a grade from the table.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(TeacherPanel.this,
                "Are you sure you want to delete grade with ID " + gradeId + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM grades WHERE grade_id=?")) {
                    pstmt.setInt(1, gradeId);
                    int rowsDeleted = pstmt.executeUpdate();
                    if (rowsDeleted == 0) {
                        throw new Exception("Grade with ID " + gradeId + " not found.");
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error deleting grade: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(TeacherPanel.this, "Grade deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearGradeFields();
                    loadGradeData(); // Refresh the grade table
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error deleting grade: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Loads grade data from the database and populates the grade table.
     * Filters grades to show only those for subjects and classes assigned to this teacher.
     * Uses SwingWorker for background database operation.
     */
    private void loadGradeData() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT g.grade_id, s.name AS student_name, c.class_name, sub.subject_name, g.term, g.score, g.comments, g.date_recorded " +
                             "FROM grades g " +
                             "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                             "JOIN students s ON e.student_id = s.student_id " +
                             "JOIN classes c ON e.class_id = c.class_id " +
                             "JOIN subjects sub ON g.subject_id = sub.subject_id " +
                             "JOIN class_assignments ca ON e.class_id = ca.class_id AND g.subject_id = ca.subject_id " + // Join to filter by teacher's assignments
                             "WHERE ca.teacher_id = ? ORDER BY c.class_name, sub.subject_name, s.name, g.term")) {
                    pstmt.setInt(1, loggedInTeacherId);
                    ResultSet rs = pstmt.executeQuery();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // For formatting date

                    while (rs.next()) {
                         Date dateRecorded = rs.getDate("date_recorded");
                        Object[] row = {
                                rs.getInt("grade_id"),
                                rs.getString("student_name"),
                                rs.getString("class_name"),
                                rs.getString("subject_name"),
                                rs.getString("term"),
                                rs.getDouble("score"),
                                rs.getString("comments"),
                                dateRecorded != null ? dateFormat.format(dateRecorded) : "N/A"
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading grade data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    gradeTableModel.setRowCount(0);
                    for (Object[] row : data) {
                        gradeTableModel.addRow(row);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading grade data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Clears the input fields in the Grade Management panel.
     */
    private void clearGradeFields() {
        gradeIdField.setText("");
        scoreField.setText("");
        commentField.setText("");
        // Reset combo boxes to the first item or a default if available
        if (gradeSubjectComboBox.getItemCount() > 0) gradeSubjectComboBox.setSelectedIndex(0);
        if (gradeStudentComboBox.getItemCount() > 0) gradeStudentComboBox.setSelectedIndex(0);
        if (gradeTermComboBox.getItemCount() > 0) gradeTermComboBox.setSelectedIndex(0);
         gradeTable.clearSelection(); // Clear table selection
    }


    // --- Teacher Reports Panel ---
    private JPanel createTeacherReportsPanel() {
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

        generateTeacherReportButton = new JButton("Generate Report");
        styleButton(generateTeacherReportButton, new Color(0, 123, 255)); // Blue
        controlPanel.add(generateTeacherReportButton);

        printTeacherReportButton = new JButton("Print Report");
        styleButton(printTeacherReportButton, new Color(108, 117, 125)); // Grey
        controlPanel.add(printTeacherReportButton);


        // Report Display Area
        teacherReportDisplayArea = new JTextArea();
        teacherReportDisplayArea.setEditable(false);
        teacherReportDisplayArea.setWrapStyleWord(true);
        teacherReportDisplayArea.setLineWrap(true);
        teacherReportDisplayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Monospaced font for alignment
        JScrollPane reportScrollPane = new JScrollPane(teacherReportDisplayArea);
        reportScrollPane.setBorder(BorderFactory.createTitledBorder("Performance Report"));


        // Add components to the main panel
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(reportScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        generateTeacherReportButton.addActionListener(e -> generateTeacherReport());
        printTeacherReportButton.addActionListener(e -> printTeacherReport());

        // Add listeners to combo boxes to update dependent combo boxes
        reportClassComboBox.addActionListener(e -> populateReportSubjectComboBox());
        reportSubjectComboBox.addActionListener(e -> populateReportTermComboBox());


        return panel;
    }

    /**
     * Loads data for report combo boxes (classes, subjects, terms)
     * relevant to the logged-in teacher's assignments.
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

                    // Load Classes assigned to this teacher
                    ResultSet rsClasses = stmt.executeQuery(
                        "SELECT DISTINCT c.class_id, c.class_name FROM classes c " +
                        "JOIN class_assignments ca ON c.class_id = ca.class_id " +
                        "WHERE ca.teacher_id = " + loggedInTeacherId + " ORDER BY c.class_name");
                    while (rsClasses.next()) {
                        int id = rsClasses.getInt("class_id");
                        String name = rsClasses.getString("class_name");
                        classNames.add(name);
                        classNameToIdMap.put(name, id);
                    }
                    rsClasses.close();
                    data.put("classes", classNames);

                    // Load Subjects assigned to this teacher
                    ResultSet rsSubjects = stmt.executeQuery(
                        "SELECT DISTINCT s.subject_id, s.subject_name FROM subjects s " +
                        "JOIN class_assignments ca ON s.subject_id = ca.subject_id " +
                        "WHERE ca.teacher_id = " + loggedInTeacherId + " ORDER BY s.subject_name");
                    while (rsSubjects.next()) {
                        int id = rsSubjects.getInt("subject_id");
                        String name = rsSubjects.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rsSubjects.close();
                    data.put("subjects", subjectNames);

                    // Load Terms from Grades table relevant to this teacher's assignments
                    ResultSet rsTerms = stmt.executeQuery(
                        "SELECT DISTINCT g.term FROM grades g " +
                        "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                        "JOIN class_assignments ca ON e.class_id = ca.class_id AND g.subject_id = ca.subject_id " +
                        "WHERE ca.teacher_id = " + loggedInTeacherId + " ORDER BY g.term");
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
                        // Trigger population of dependent combo boxes after initial load
                        populateReportSubjectComboBox(); // Populate subjects based on initial class selection
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
     * Populates the report subject combo box based on the selected class
     * and the logged-in teacher's assignments.
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
                         "SELECT DISTINCT s.subject_id, s.subject_name FROM subjects s " +
                         "JOIN class_assignments ca ON s.subject_id = ca.subject_id " +
                         "WHERE ca.teacher_id = ? AND ca.class_id = ? ORDER BY s.subject_name")) {
                    pstmt.setInt(1, loggedInTeacherId);
                    pstmt.setInt(2, classId);
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
     * Populates the report term combo box based on the selected class and subject
     * and the logged-in teacher's assignments.
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
                         "SELECT DISTINCT g.term FROM grades g " +
                         "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                         "JOIN class_assignments ca ON e.class_id = ca.class_id AND g.subject_id = ca.subject_id " +
                         "WHERE ca.teacher_id = ? AND e.class_id = ? AND g.subject_id = ? ORDER BY g.term")) {
                    pstmt.setInt(1, loggedInTeacherId);
                    pstmt.setInt(2, classId);
                    pstmt.setInt(3, subjectId);
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
     * Generates a performance report for a selected class, subject, and term
     * for the logged-in teacher's assigned students.
     * Displays the report in the teacherReportDisplayArea.
     * Uses SwingWorker for background database operation.
     */
    private void generateTeacherReport() {
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
                         "FROM grades g " +
                         "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN class_assignments ca ON e.class_id = ca.class_id AND g.subject_id = ca.subject_id " + // Join to filter by teacher's assignments
                         "WHERE ca.teacher_id = ? AND e.class_id = ? AND g.subject_id = ? AND g.term = ? ORDER BY s.name")) {
                    pstmt.setInt(1, loggedInTeacherId);
                    pstmt.setInt(2, classId);
                    pstmt.setInt(3, subjectId);
                    pstmt.setString(4, selectedTerm);
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
                    SwingUtilities.invokeLater(() -> teacherReportDisplayArea.setText(report));
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error generating report: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> teacherReportDisplayArea.setText("Error generating report: " + errorMessage));
                }
            }
        }.execute();
    }

    /**
     * Prints the content of the teacherReportDisplayArea.
     * Uses a simple JOptionPane for preview.
     */
     private void printTeacherReport() {
         String reportContent = teacherReportDisplayArea.getText();

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
} // End of TeacherPanel class
