package com.school; // or com.school.utils; - ensure this matches your package structure

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 * It provides static methods to set connection parameters and get a database connection.
 */
public class DatabaseUtility {

    // Static variables to hold the database URL, username, and password
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    /**
     * Sets the database URL. This method should be called once, typically during
     * application startup in the main application class (e.g., SchoolManagementApp),
     * after loading the configuration from a file.
     *
     * @param url The JDBC database URL (e.g., "jdbc:mysql://localhost:3306/school_db").
     */
    public static void setDatabaseUrl(String url) {
        dbUrl = url;
    }

    /**
     * Sets the database credentials. This method should be called once, typically during
     * application startup, after loading them from a configuration file.
     *
     * @param user The database username.
     * @param password The database password.
     */
    public static void setDatabaseCredentials(String user, String password) {
        dbUser = user;
        dbPassword = password;
    }

    /**
     * Gets a connection to the database using the URL and credentials set by
     * setDatabaseUrl() and setDatabaseCredentials().
     *
     * @return A valid database Connection object.
     * @throws SQLException If a database access error occurs, or if the database URL or credentials have not been set.
     */
    public static Connection getConnection() throws SQLException {
        // Check if database configuration has been set
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new SQLException("Database URL or credentials not set. Please load configuration first.");
        }

        // Attempt to load the JDBC driver (optional for modern JDBC drivers, but good practice)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Use the correct driver class name for MySQL Connector/J 8.0+
        } catch (ClassNotFoundException e) {
            // If the driver is not found, it means the JAR is not in the classpath
            throw new SQLException("MySQL JDBC Driver not found. Ensure 'mysql-connector-java.jar' is in the classpath.", e);
        }

        // Establish and return the database connection using URL, user, and password
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    // Optional: Helper methods to close resources (Connection, Statement, ResultSet)
    // These are generally not needed with try-with-resources, but can be included if preferred.

    /**
     * Closes a database connection, handling potential SQLExceptions.
     *
     * @param conn The Connection to close.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                // e.printStackTrace(); // Optionally print stack trace
            }
        }
    }

    /**
     * Closes a Statement, handling potential SQLExceptions.
     *
     * @param stmt The Statement to close.
     */
    public static void closeStatement(java.sql.Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement: " + e.getMessage());
                // e.printStackTrace();
            }
        }
    }

    /**
     * Closes a ResultSet, handling potential SQLExceptions.
     *
     * @param rs The ResultSet to close.
     */
    public static void closeResultSet(java.sql.ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
                // e.printStackTrace();
            }
        }
    }
}
