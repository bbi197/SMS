package com.school.utils; // Recommended package for utility classes

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet; // Import for handling database results
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Utility class for generating PDF reports using iTextPDF.
 * This class can be used by other panels (like AdminPanel or TeacherPanel)
 * to create printable reports from data.
 */
public class PdfReportGenerator {

    /**
     * Generates a simple PDF report from a ResultSet.
     * This method is a basic example and can be extended to handle
     * different report formats and data structures.
     *
     * @param resultSet The ResultSet containing the data for the report.
     * @param filePath  The path where the PDF file will be saved.
     * @param reportTitle The title of the report.
     * @throws DocumentException If an error occurs while creating the PDF document.
     * @throws IOException If an I/O error occurs while writing the file.
     * @throws SQLException If a database error occurs while reading the ResultSet.
     */
    public static void generateSimpleReport(ResultSet resultSet, String filePath, String reportTitle)
            throws DocumentException, IOException, SQLException {

        // Create a new Document object
        Document document = new Document(PageSize.A4);

        try {
            // Create a PdfWriter instance to write the document to the specified file path
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Open the document
            document.open();

            // Add a title to the document
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph(reportTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20); // Add space after the title
            document.add(title);

            // Get metadata from the ResultSet to determine column names and count
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create a PdfPTable with the same number of columns as the ResultSet
            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100); // Set table width to 100% of the page width
            table.setSpacingBefore(10f); // Add space before the table
            table.setSpacingAfter(10f); // Add space after the table

            // Set column widths (optional, can be adjusted based on content)
            // float[] columnWidths = new float[columnCount];
            // for (int i = 0; i < columnCount; i++) {
            //     columnWidths[i] = 1f; // Default width
            // }
            // table.setWidths(columnWidths);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            for (int i = 1; i <= columnCount; i++) {
                PdfPCell headerCell = new PdfPCell(new Phrase(metaData.getColumnLabel(i), headerFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY); // Add background color
                headerCell.setPadding(5); // Add padding
                table.addCell(headerCell);
            }

            // Add table rows from the ResultSet data
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    // Get data as String to handle various data types
                    String cellData = resultSet.getString(i);
                    PdfPCell dataCell = new PdfPCell(new Phrase(cellData != null ? cellData : "", dataFont));
                    dataCell.setPadding(5); // Add padding
                    table.addCell(dataCell);
                }
            }

            // Add the table to the document
            document.add(table);

        } finally {
            // Close the document in a finally block to ensure it's always closed
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    // You can add more methods here for different report types,
    // like reports with charts, images, specific formatting, etc.
    // For example:
    // public static void generateStudentTranscript(Student student, List<Grade> grades, String filePath) { ... }
    // public static void generateFeeStatement(Student student, List<FeeRecord> fees, String filePath) { ... }

}
