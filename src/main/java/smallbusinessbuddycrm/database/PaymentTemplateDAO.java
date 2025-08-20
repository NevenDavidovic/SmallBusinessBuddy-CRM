package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.PaymentTemplate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PaymentTemplate entity operations.
 * Handles database interactions for payment template management including CRUD operations,
 * status management, bulk operations, and validation.
 *
 * Features:
 * - Complete template lifecycle management
 * - Active/inactive status filtering and toggling
 * - Bulk deletion operations
 * - Name uniqueness validation
 * - Croatian payment system integration (poziv na broj)
 * - Amount and payment model management
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class PaymentTemplateDAO {

    /**
     * Retrieves all payment templates from the database, both active and inactive.
     * Results are ordered alphabetically by name for consistent display.
     *
     * @return List of all payment templates, ordered by name
     * @throws SQLException if database access error occurs
     */
    public List<PaymentTemplate> getAllPaymentTemplates() throws SQLException {
        List<PaymentTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM payment_template ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PaymentTemplate template = mapResultSetToTemplate(rs);
                templates.add(template);
            }
        }

        return templates;
    }

    /**
     * Retrieves only active payment templates from the database.
     * Used for normal operations where inactive templates should be hidden.
     * Results are ordered alphabetically by name.
     *
     * @return List of active payment templates, ordered by name
     * @throws SQLException if database access error occurs
     */
    public List<PaymentTemplate> getActivePaymentTemplates() throws SQLException {
        List<PaymentTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM payment_template WHERE is_active = 1 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PaymentTemplate template = mapResultSetToTemplate(rs);
                templates.add(template);
            }
        }

        return templates;
    }

    /**
     * Retrieves a payment template by its ID.
     *
     * @param id The ID of the payment template to retrieve
     * @return The payment template if found, null otherwise
     * @throws SQLException if database access error occurs
     */
    public PaymentTemplate getPaymentTemplateById(int id) throws SQLException {
        String sql = "SELECT * FROM payment_template WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTemplate(rs);
                }
            }
        }

        return null;
    }

    /**
     * Saves a payment template to the database.
     * Automatically determines whether to insert or update based on template ID.
     * Sets creation and update timestamps appropriately.
     *
     * @param template The payment template to save
     * @return true if template was saved successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean save(PaymentTemplate template) throws SQLException {
        if (template.getId() == 0) {
            return insert(template);
        } else {
            return update(template);
        }
    }

    /**
     * Inserts a new payment template into the database.
     * Automatically generates ID and sets creation/update timestamps.
     *
     * @param template The template to insert
     * @return true if insertion was successful, false otherwise
     * @throws SQLException if insertion fails
     */
    private boolean insert(PaymentTemplate template) throws SQLException {
        String sql = """
            INSERT INTO payment_template 
            (name, description, amount, model_of_payment, poziv_na_broj, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, template.getName());
            stmt.setString(2, template.getDescription());
            stmt.setBigDecimal(3, template.getAmount());
            stmt.setString(4, template.getModelOfPayment());
            stmt.setString(5, template.getPozivNaBroj());
            stmt.setBoolean(6, template.isActive());
            stmt.setString(7, timestamp);
            stmt.setString(8, timestamp);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        template.setId(generatedKeys.getInt(1));
                        template.setCreatedAt(timestamp);
                        template.setUpdatedAt(timestamp);
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Updates an existing payment template in the database.
     * Automatically updates the updated_at timestamp.
     *
     * @param template The template to update
     * @return true if update was successful, false otherwise
     * @throws SQLException if update fails
     */
    private boolean update(PaymentTemplate template) throws SQLException {
        String sql = """
            UPDATE payment_template SET 
            name = ?, description = ?, amount = ?, model_of_payment = ?, 
            poziv_na_broj = ?, is_active = ?, updated_at = ?
            WHERE id = ?
            """;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, template.getName());
            stmt.setString(2, template.getDescription());
            stmt.setBigDecimal(3, template.getAmount());
            stmt.setString(4, template.getModelOfPayment());
            stmt.setString(5, template.getPozivNaBroj());
            stmt.setBoolean(6, template.isActive());
            stmt.setString(7, timestamp);
            stmt.setInt(8, template.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                template.setUpdatedAt(timestamp);
                return true;
            }
        }

        return false;
    }

    /**
     * Deletes multiple payment templates in a single batch operation.
     * Uses batch processing for optimal performance when deleting multiple templates.
     * This operation is permanent and cannot be undone.
     *
     * @param templateIds List of template IDs to delete
     * @return true if all deletions were successful, false if any failed or list is empty
     * @throws SQLException if database operation fails
     */
    public boolean deleteTemplates(List<Integer> templateIds) throws SQLException {
        if (templateIds == null || templateIds.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM payment_template WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Integer id : templateIds) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();

            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Toggles the active status of a payment template.
     * If template is active, it becomes inactive and vice versa.
     * Automatically updates the updated_at timestamp.
     *
     * @param templateId The ID of the template to toggle
     * @return true if status was toggled successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean toggleActiveStatus(int templateId) throws SQLException {
        String sql = """
            UPDATE payment_template SET 
            is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END,
            updated_at = ?
            WHERE id = ?
            """;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, timestamp);
            stmt.setInt(2, templateId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Checks if a template name already exists in the database.
     * Performs case-insensitive name checking and can exclude a specific ID for update validation.
     * Used to prevent duplicate template names.
     *
     * @param name The name to check for existence
     * @param excludeId The ID to exclude from the check (for updates)
     * @return true if name exists, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean nameExists(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payment_template WHERE LOWER(name) = LOWER(?) AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Maps a database ResultSet to a PaymentTemplate object.
     * Handles proper type conversion for all payment template fields including
     * BigDecimal for amounts and Croatian payment references.
     *
     * @param rs ResultSet containing payment template data
     * @return Populated PaymentTemplate object
     * @throws SQLException if database access error occurs
     */
    private PaymentTemplate mapResultSetToTemplate(ResultSet rs) throws SQLException {
        PaymentTemplate template = new PaymentTemplate();
        template.setId(rs.getInt("id"));
        template.setName(rs.getString("name"));
        template.setDescription(rs.getString("description"));
        template.setAmount(rs.getBigDecimal("amount"));
        template.setModelOfPayment(rs.getString("model_of_payment"));
        template.setPozivNaBroj(rs.getString("poziv_na_broj"));
        template.setActive(rs.getBoolean("is_active"));
        template.setCreatedAt(rs.getString("created_at"));
        template.setUpdatedAt(rs.getString("updated_at"));
        return template;
    }
}