package smallbusinessbuddycrm.utilities;

import smallbusinessbuddycrm.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to set up initial payment templates and organization data
 */
public class PaymentTemplateSetup {

    /**
     * Creates sample payment templates for testing
     */
    public static void createSamplePaymentTemplates() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Sample payment templates
            String[] templates = {
                    "INSERT INTO payment_template (name, description, amount, model_of_payment, poziv_na_broj, is_active, created_at, updated_at) VALUES " +
                            "('Membership Fee', 'Annual membership fee', 50.00, '00', null, 1, ?, ?)",

                    "INSERT INTO payment_template (name, description, amount, model_of_payment, poziv_na_broj, is_active, created_at, updated_at) VALUES " +
                            "('Workshop Fee', 'Standard workshop participation fee', 25.00, '00', null, 1, ?, ?)",

                    "INSERT INTO payment_template (name, description, amount, model_of_payment, poziv_na_broj, is_active, created_at, updated_at) VALUES " +
                            "('Donation', 'General donation', 10.00, '00', null, 1, ?, ?)",

                    "INSERT INTO payment_template (name, description, amount, model_of_payment, poziv_na_broj, is_active, created_at, updated_at) VALUES " +
                            "('Late Fee', 'Late payment penalty', 5.00, '00', null, 1, ?, ?)"
            };

            for (String templateSQL : templates) {
                try (PreparedStatement stmt = conn.prepareStatement(templateSQL)) {
                    stmt.setString(1, timestamp);
                    stmt.setString(2, timestamp);
                    stmt.executeUpdate();
                }
            }

            System.out.println("✓ Sample payment templates created successfully!");

        } catch (SQLException e) {
            System.err.println("Error creating sample payment templates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates sample organization data if none exists
     */
    public static void createSampleOrganization() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check if organization already exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM organization")) {
                var rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Organization already exists, skipping creation.");
                    return;
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String organizationSQL = """
                INSERT INTO organization 
                (name, IBAN, street_name, street_num, postal_code, city, email, phone_num, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(organizationSQL)) {
                stmt.setString(1, "Sample Business Organization");
                stmt.setString(2, "HR1234567890123456789"); // Sample Croatian IBAN
                stmt.setString(3, "Ilica");
                stmt.setString(4, "10");
                stmt.setString(5, "10000");
                stmt.setString(6, "Zagreb");
                stmt.setString(7, "info@sampleorg.hr");
                stmt.setString(8, "+385 1 234 5678");
                stmt.setString(9, timestamp);
                stmt.setString(10, timestamp);

                stmt.executeUpdate();
            }

            System.out.println("✓ Sample organization created successfully!");

        } catch (SQLException e) {
            System.err.println("Error creating sample organization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets up all sample data
     */
    public static void setupSampleData() {
        System.out.println("Setting up sample payment data...");
        createSampleOrganization();
        createSamplePaymentTemplates();
        System.out.println("Sample data setup completed!");
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        // Initialize database first
        DatabaseConnection.initializeDatabase();

        // Set up sample data
        setupSampleData();
    }
}