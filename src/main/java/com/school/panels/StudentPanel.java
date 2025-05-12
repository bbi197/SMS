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
import java.text.SimpleDateFormat; // For formatting date

// Import the main application class to access the logout method
import com.school.SchoolManagementApp; // This import should be here
import com.school.DatabaseUtility; // Ensure this import is present
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List
import java.util.Collections; // Import Collections for sorting
import com.formdev.flatlaf.FlatClientProperties; // Import for FlatLaf specific properties
import java.math.BigDecimal; // Use BigDecimal for fee amounts

/**
 * StudentPanel provides the interface for students to view their personal information,
 * enrolled classes, grades, and fee records.
 * It interacts with the database using DatabaseUtility and performs operations
 * in the background using SwingWorker for a responsive UI.
 */
public class StudentPanel extends JPanel {
    private final int loggedInStudentId; // Store the logged-in student's ID
    private SchoolManagementApp parentFrame; // Reference to the main application frame

    private JTabbedPane studentTabbedPane;

    // Panels for different sections
    private JPanel myInfoPanel;
    private JPanel myClassesPanel;
    private JPanel myGradesPanel;
    private JPanel myFeesPanel;


    // --- My Info Components ---
    private JLabel infoNameLabel, infoGradeLabel, infoClassLabel, infoStatusLabel;


    // --- My Classes Components ---
    private JTable classesTable;
    private DefaultTableModel classesTableModel;


    // --- My Grades Components ---
    private JTable gradesTable;
    private DefaultTableModel gradesTableModel;
    private JComboBox<String> gradesSubjectFilterComboBox, gradesTermFilterComboBox; // Filters for grades
    private Map<String, Integer> subjectNameToIdMap = new HashMap<>(); // Map subject name to ID (reused)


    // --- My Fees Components ---
    private JTable feesTable;
    private DefaultTableModel feesTableModel;


    // --- Loading Indicator ---
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private JPanel loadingPanel; // Panel to hold loading indicator


    /**
     * Constructor for the StudentPanel.
     *
     * @param parentFrame The main application frame.
     * @param studentId   The ID of the logged-in student.
     */
    public StudentPanel(SchoolManagementApp parentFrame, int studentId) {
        this.parentFrame = parentFrame; // Store reference to the parent frame
        this.loggedInStudentId = studentId; // Store the logged-in student's ID

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Light grey background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        studentTabbedPane = new JTabbedPane();
        studentTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold font for tabs

        setupLoadingIndicator(); // Setup the loading bar and label panel

        // Initialize panels
        myInfoPanel = createMyInfoPanel();
        myClassesPanel = createMyClassesPanel();
        myGradesPanel = createMyGradesPanel();
        myFeesPanel = createMyFeesPanel();


        // Add panels to the tabbed pane
        studentTabbedPane.addTab("My Info", myInfoPanel);
        studentTabbedPane.addTab("My Classes", myClassesPanel);
        studentTabbedPane.addTab("My Grades", myGradesPanel);
        studentTabbedPane.addTab("My Fees", myFeesPanel);


        add(studentTabbedPane, BorderLayout.CENTER);

        // Add a ChangeListener to the tabbed pane to load data when a tab is selected
        studentTabbedPane.addChangeListener(e -> {
            int selectedIndex = studentTabbedPane.getSelectedIndex();
            // Load data for the selected tab
            switch (selectedIndex) {
                case 0: // My Info
                    loadMyInfo();
                    break;
                case 1: // My Classes
                    loadMyClasses();
                    break;
                case 2: // My Grades
                    loadMyGrades();
                    loadGradesFilterComboBoxes(); // Load data for filter combo boxes
                    break;
                case 3: // My Fees
                    loadMyFees();
                    break;
            }
        });

        // Load data for the initially selected tab (My Info, index 0)
        loadMyInfo();


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
                studentTabbedPane.setEnabled(!loading);
            } else {
                progressBar.setVisible(false);
                loadingLabel.setVisible(false);
                // Remove the loading panel after the loading is complete
                if (loadingPanel.getParent() == this) { // Check if it's currently added to this panel
                    remove(loadingPanel);
                }
                 // Optional: Re-enable UI elements
                studentTabbedPane.setEnabled(!loading);
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


    // --- My Info Panel ---
    private JPanel createMyInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("My Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1; // Reset gridwidth
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        JLabel nameStaticLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(nameStaticLabel, gbc);
        infoNameLabel = new JLabel("Loading..."); // Placeholder text
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(infoNameLabel, gbc);

        JLabel gradeStaticLabel = new JLabel("Grade Level:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(gradeStaticLabel, gbc);
        infoGradeLabel = new JLabel("Loading..."); // Placeholder text
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(infoGradeLabel, gbc);

        JLabel classStaticLabel = new JLabel("Current Class:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(classStaticLabel, gbc);
        infoClassLabel = new JLabel("Loading..."); // Placeholder text
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(infoClassLabel, gbc);

        JLabel statusStaticLabel = new JLabel("Status:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(statusStaticLabel, gbc);
        infoStatusLabel = new JLabel("Loading..."); // Placeholder text
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(infoStatusLabel, gbc);


        // Add a filler to push components to the top
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0; // This row will take up extra vertical space
        panel.add(Box.createVerticalGlue(), gbc);


        return panel;
    }

    /**
     * Loads the logged-in student's information from the database.
     * Uses SwingWorker for background database operation.
     */
    private void loadMyInfo() {
        setLoading(true);
        SwingWorker<Map<String, String>, Void> worker = new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                Map<String, String> studentInfo = new HashMap<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT s.name, s.grade_level, c.class_name, s.status " +
                             "FROM students s JOIN classes c ON s.class_id = c.class_id " +
                             "WHERE s.student_id = ?")) {
                    pstmt.setInt(1, loggedInStudentId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        studentInfo.put("name", rs.getString("name"));
                        studentInfo.put("grade_level", rs.getString("grade_level"));
                        studentInfo.put("class_name", rs.getString("class_name"));
                        studentInfo.put("status", rs.getString("status"));
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading student info: " + e.getMessage(), e);
                }
                return studentInfo;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, String> studentInfo = get();
                    SwingUtilities.invokeLater(() -> {
                        if (!studentInfo.isEmpty()) {
                            infoNameLabel.setText(studentInfo.get("name"));
                            infoGradeLabel.setText(studentInfo.get("grade_level"));
                            infoClassLabel.setText(studentInfo.get("class_name"));
                            infoStatusLabel.setText(studentInfo.get("status"));
                        } else {
                            infoNameLabel.setText("N/A");
                            infoGradeLabel.setText("N/A");
                            infoClassLabel.setText("N/A");
                            infoStatusLabel.setText("N/A");
                            showError("Student information not found.");
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading student info: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        infoNameLabel.setText("Error");
                        infoGradeLabel.setText("Error");
                        infoClassLabel.setText("Error");
                        infoStatusLabel.setText("Error");
                    });
                }
            }
        }.execute();
    }


    // --- My Classes Panel ---
    private JPanel createMyClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        classesTableModel = new DefaultTableModel(new Object[]{"Class Name", "Grade Level", "Teacher", "Subject"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        classesTable = new JTable(classesTableModel);
        classesTable.setFillsViewportHeight(true);
        classesTable.setRowHeight(25);
        classesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane classesScrollPane = new JScrollPane(classesTable);
        classesScrollPane.setBorder(BorderFactory.createTitledBorder("My Enrolled Classes"));

        panel.add(classesScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads the classes the logged-in student is enrolled in.
     * Uses SwingWorker for background database operation.
     */
    private void loadMyClasses() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT DISTINCT c.class_name, c.grade_level, t.name AS teacher_name, s.subject_name " +
                             "FROM enrollments e " +
                             "JOIN classes c ON e.class_id = c.class_id " +
                             "LEFT JOIN class_assignments ca ON c.class_id = ca.class_id " + // Use LEFT JOIN in case a class has no assignment yet
                             "LEFT JOIN teachers t ON ca.teacher_id = t.teacher_id " + // Use LEFT JOIN
                             "LEFT JOIN subjects s ON ca.subject_id = s.subject_id " + // Use LEFT JOIN
                             "WHERE e.student_id = ? ORDER BY c.class_name, s.subject_name")) {
                    pstmt.setInt(1, loggedInStudentId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Object[] row = {
                                rs.getString("class_name"),
                                rs.getString("grade_level"),
                                rs.getString("teacher_name") != null ? rs.getString("teacher_name") : "N/A", // Handle potential NULLs
                                rs.getString("subject_name") != null ? rs.getString("subject_name") : "N/A" // Handle potential NULLs
                        };
                        data.add(row);
                    }
                    rs.close();
                } catch (SQLException e) {
                    throw new Exception("Database error loading classes: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    SwingUtilities.invokeLater(() -> {
                        classesTableModel.setRowCount(0); // Clear table
                        for (Object[] row : data) {
                            classesTableModel.addRow(row); // Add rows
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading classes: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> classesTableModel.setRowCount(0)); // Clear table on error
                }
            }
        }.execute();
    }


    // --- My Grades Panel ---
    private JPanel createMyGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(new Color(250, 250, 250));

        filterPanel.add(new JLabel("Filter by Subject:"));
        gradesSubjectFilterComboBox = new JComboBox<>();
        gradesSubjectFilterComboBox.setPreferredSize(new Dimension(150, gradesSubjectFilterComboBox.getPreferredSize().height));
        filterPanel.add(gradesSubjectFilterComboBox);

        filterPanel.add(new JLabel("Filter by Term:"));
        gradesTermFilterComboBox = new JComboBox<>();
        gradesTermFilterComboBox.setPreferredSize(new Dimension(100, gradesTermFilterComboBox.getPreferredSize().height));
        filterPanel.add(gradesTermFilterComboBox);

        JButton applyFilterButton = new JButton("Apply Filter");
        styleButton(applyFilterButton, new Color(0, 123, 255)); // Blue
        filterPanel.add(applyFilterButton);

        JButton clearFilterButton = new JButton("Clear Filter");
        styleButton(clearFilterButton, new Color(108, 117, 125)); // Grey
        filterPanel.add(clearFilterButton);


        gradesTableModel = new DefaultTableModel(new Object[]{"Subject", "Term", "Score", "Comments", "Date Recorded"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class; // Score column
                return super.getColumnClass(columnIndex);
            }
        };
        gradesTable = new JTable(gradesTableModel);
        gradesTable.setFillsViewportHeight(true);
        gradesTable.setRowHeight(25);
        gradesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane gradesScrollPane = new JScrollPane(gradesTable);
        gradesScrollPane.setBorder(BorderFactory.createTitledBorder("My Grades"));

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(gradesScrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        applyFilterButton.addActionListener(e -> loadMyGrades(
            (String) gradesSubjectFilterComboBox.getSelectedItem(),
            (String) gradesTermFilterComboBox.getSelectedItem()
        ));
        clearFilterButton.addActionListener(e -> {
            gradesSubjectFilterComboBox.setSelectedIndex(0); // Reset to first item (usually "All Subjects")
            gradesTermFilterComboBox.setSelectedIndex(0); // Reset to first item (usually "All Terms")
            loadMyGrades(); // Reload without filters
        });


        return panel;
    }

    /**
     * Loads data for the grades filter combo boxes (subjects and terms)
     * relevant to the logged-in student's grades.
     * Uses SwingWorker for background database operation.
     */
    private void loadGradesFilterComboBoxes() {
        setLoading(true);
        SwingWorker<Map<String, Vector<String>>, Void> worker = new SwingWorker<Map<String, Vector<String>>, Void>() {
            @Override
            protected Map<String, Vector<String>> doInBackground() throws Exception {
                Map<String, Vector<String>> data = new HashMap<>();
                Vector<String> subjectNames = new Vector<>();
                Vector<String> terms = new Vector<>();

                // Add "All" option to filters
                subjectNames.add("All Subjects");
                terms.add("All Terms");

                subjectNameToIdMap.clear(); // Clear map

                try (Connection conn = DatabaseUtility.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Load Subjects the student has grades for
                    ResultSet rsSubjects = stmt.executeQuery(
                        "SELECT DISTINCT sub.subject_id, sub.subject_name FROM subjects sub " +
                        "JOIN grades g ON sub.subject_id = g.subject_id " +
                        "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                        "WHERE e.student_id = " + loggedInStudentId + " ORDER BY sub.subject_name");
                    while (rsSubjects.next()) {
                        int id = rsSubjects.getInt("subject_id");
                        String name = rsSubjects.getString("subject_name");
                        subjectNames.add(name);
                        subjectNameToIdMap.put(name, id);
                    }
                    rsSubjects.close();
                    data.put("subjects", subjectNames);

                    // Load Terms the student has grades for
                    ResultSet rsTerms = stmt.executeQuery(
                        "SELECT DISTINCT g.term FROM grades g " +
                        "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                        "WHERE e.student_id = " + loggedInStudentId + " ORDER BY g.term");
                    while (rsTerms.next()) {
                        terms.add(rsTerms.getString("term"));
                    }
                    rsTerms.close();
                    data.put("terms", terms);

                } catch (SQLException e) {
                    throw new Exception("Database error loading grades filter combo box data: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<String, Vector<String>> data = get();
                    SwingUtilities.invokeLater(() -> {
                        gradesSubjectFilterComboBox.setModel(new DefaultComboBoxModel<>(data.get("subjects")));
                        gradesTermFilterComboBox.setModel(new DefaultComboBoxModel<>(data.get("terms")));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading grades filter data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                     // Clear combo boxes if loading fails
                     SwingUtilities.invokeLater(() -> {
                         gradesSubjectFilterComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Error loading subjects"}));
                         gradesTermFilterComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Error loading terms"}));
                     });
                }
            }
        }.execute();
    }

    /**
     * Loads the logged-in student's grades from the database, with optional filtering.
     * Uses SwingWorker for background database operation.
     *
     * @param subjectFilter Optional subject name to filter by. Null or "All Subjects" for no subject filter.
     * @param termFilter    Optional term to filter by. Null or "All Terms" for no term filter.
     */
    private void loadMyGrades(String subjectFilter, String termFilter) {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                StringBuilder sql = new StringBuilder(
                    "SELECT sub.subject_name, g.term, g.score, g.comments, g.date_recorded " +
                    "FROM grades g " +
                    "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                    "JOIN subjects sub ON g.subject_id = sub.subject_id " +
                    "WHERE e.student_id = ?"
                );
                List<Object> params = new ArrayList<>();
                params.add(loggedInStudentId);

                if (subjectFilter != null && !subjectFilter.isEmpty() && !subjectFilter.equals("All Subjects")) {
                    sql.append(" AND sub.subject_name = ?");
                    params.add(subjectFilter);
                }
                if (termFilter != null && !termFilter.isEmpty() && !termFilter.equals("All Terms")) {
                    sql.append(" AND g.term = ?");
                    params.add(termFilter);
                }

                sql.append(" ORDER BY sub.subject_name, g.term");

                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // For formatting date

                    while (rs.next()) {
                        Date dateRecorded = rs.getDate("date_recorded");
                        Object[] row = {
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
                    throw new Exception("Database error loading grades: " + e.getMessage(), e);
                }
                return data;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Object[]> data = get();
                    SwingUtilities.invokeLater(() -> {
                        gradesTableModel.setRowCount(0); // Clear table
                        if (data.isEmpty()) {
                             // Optionally display a message in the table area or a label
                             // For simplicity, we'll just show an empty table.
                        } else {
                             for (Object[] row : data) {
                                 gradesTableModel.addRow(row); // Add rows
                             }
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading grades: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> gradesTableModel.setRowCount(0)); // Clear table on error
                }
            }
        }.execute();
    }

    /**
     * Loads the logged-in student's grades without any filters.
     */
    private void loadMyGrades() {
        loadMyGrades(null, null); // Call the filtered method with null filters
    }


    // --- My Fees Panel ---
    private JPanel createMyFeesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        feesTableModel = new DefaultTableModel(new Object[]{"Class", "Term", "Amount Due", "Amount Paid", "Balance", "Date Last Paid"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4) return BigDecimal.class; // Amount columns
                return super.getColumnClass(columnIndex);
            }
        };
        feesTable = new JTable(feesTableModel);
        feesTable.setFillsViewportHeight(true);
        feesTable.setRowHeight(25);
        feesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane feesScrollPane = new JScrollPane(feesTable);
        feesScrollPane.setBorder(BorderFactory.createTitledBorder("My Fee Records"));

        panel.add(feesScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads the logged-in student's fee records from the database.
     * Calculates the balance for each record.
     * Uses SwingWorker for background database operation.
     */
    private void loadMyFees() {
        setLoading(true);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> data = new ArrayList<>();
                try (Connection conn = DatabaseUtility.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT c.class_name, f.term, f.amount_due, f.amount_paid, f.date_last_paid " +
                             "FROM fees f JOIN classes c ON f.class_id = c.class_id " +
                             "WHERE f.student_id = ? ORDER BY c.class_name, f.term")) {
                    pstmt.setInt(1, loggedInStudentId);
                    ResultSet rs = pstmt.executeQuery();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // For formatting date

                    while (rs.next()) {
                        BigDecimal amountDue = rs.getBigDecimal("amount_due");
                        BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
                        BigDecimal balance = amountDue.subtract(amountPaid); // Calculate balance
                        Date dateLastPaid = rs.getDate("date_last_paid");

                        Object[] row = {
                                rs.getString("class_name"),
                                rs.getString("term"),
                                amountDue,
                                amountPaid,
                                balance,
                                dateLastPaid != null ? dateFormat.format(dateLastPaid) : "N/A"
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
                    SwingUtilities.invokeLater(() -> {
                        feesTableModel.setRowCount(0); // Clear table
                        if (data.isEmpty()) {
                             // Optionally display a message
                        } else {
                             for (Object[] row : data) {
                                 feesTableModel.addRow(row); // Add rows
                             }
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Error loading fee data: " + (cause != null ? cause.getMessage() : e.getMessage());
                    showError(errorMessage);
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> feesTableModel.setRowCount(0)); // Clear table on error
                }
            }
        }.execute();
    }
} // End of StudentPanel class
