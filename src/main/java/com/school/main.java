package com.school;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the School Management System application.
 * This class contains the main method to start the GUI.
 */
public class Main {

    /**
     * The main method that launches the application.
     * It creates and shows the SchoolManagementApp frame on the Event Dispatch Thread (EDT).
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure the GUI is created and updated on the Event Dispatch Thread (EDT).
        // This is crucial for Swing applications to maintain thread safety.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create an instance of the main application frame
                SchoolManagementApp app = new SchoolManagementApp();
                // Make the frame visible
                app.setVisible(true);
            }
        });
    }
}
package com.school;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the School Management System application.
 * This class contains the main method to start the GUI.
 */
public class Main {

    /**
     * The main method that launches the application.
     * It creates and shows the SchoolManagementApp frame on the Event Dispatch Thread (EDT).
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure the GUI is created and updated on the Event Dispatch Thread (EDT).
        // This is crucial for Swing applications to maintain thread safety.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create an instance of the main application frame
                SchoolManagementApp app = new SchoolManagementApp();
                // Make the frame visible
                app.setVisible(true);
            }
        });
    }
}
