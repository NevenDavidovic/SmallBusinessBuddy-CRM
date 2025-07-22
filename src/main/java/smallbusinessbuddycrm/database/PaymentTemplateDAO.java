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

public class PaymentTemplateDAO {

    /**
     * Gets all payment templates (active and inactive)
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
     * Gets all active payment templates
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
     * Gets a payment template by ID
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
     * Saves a payment template (insert or update)
     */
    public boolean save(PaymentTemplate template) throws SQLException {
        if (template.getId() == 0) {
            return insert(template);
        } else {
            return update(template);
        }
    }

    /**
     * Inserts a new payment template
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
                // Get the generated ID
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
     * Updates an existing payment template
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
     * Deletes payment templates by IDs
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

            // Check if all deletions were successful
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Toggles the active status of a payment template
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
     * Checks if a template name already exists (for validation)
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
     * Maps a ResultSet row to a PaymentTemplate object
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