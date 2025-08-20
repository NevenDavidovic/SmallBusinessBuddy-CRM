package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.PaymentAttachment;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for PaymentAttachment entity operations.
 * Handles database interactions for payment attachment template management including
 * CRUD operations, default template management, duplication, and validation.
 *
 * Features:
 * - Complete template lifecycle management
 * - Default template designation and management
 * - HTML content storage and retrieval
 * - Template duplication functionality
 * - Name-based search and validation
 * - Transaction-safe default switching
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class PaymentAttachmentDAO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Retrieves all payment attachment templates from the database.
     * Results are ordered with default template first, then alphabetically by name.
     *
     * @return List of all payment attachment templates, ordered by default status and name
     */
    public List<PaymentAttachment> findAll() {
        List<PaymentAttachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM payment_attachment ORDER BY is_default DESC, name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all payment attachments: " + e.getMessage());
            e.printStackTrace();
        }

        return attachments;
    }

    /**
     * Retrieves a payment attachment template by its ID.
     *
     * @param id The ID of the payment attachment to retrieve
     * @return Optional containing the payment attachment if found, empty Optional otherwise
     */
    public Optional<PaymentAttachment> findById(Long id) {
        String sql = "SELECT * FROM payment_attachment WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttachment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding payment attachment by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Retrieves the default payment attachment template.
     * Only one template can be marked as default at any time.
     *
     * @return Optional containing the default payment attachment if found, empty Optional otherwise
     */
    public Optional<PaymentAttachment> findDefault() {
        String sql = "SELECT * FROM payment_attachment WHERE is_default = 1 LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapResultSetToAttachment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding default payment attachment: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Searches for payment attachment templates by name using case-insensitive partial matching.
     * Results are ordered with default template first, then alphabetically by name.
     *
     * @param searchTerm The search term to match against template names
     * @return List of payment attachments with names containing the search term
     */
    public List<PaymentAttachment> findByNameContaining(String searchTerm) {
        List<PaymentAttachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM payment_attachment WHERE LOWER(name) LIKE LOWER(?) ORDER BY is_default DESC, name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapResultSetToAttachment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching payment attachments: " + e.getMessage());
            e.printStackTrace();
        }

        return attachments;
    }

    /**
     * Saves a payment attachment template to the database.
     * Automatically determines whether to insert or update based on ID presence.
     * Handles default template management to ensure only one default exists.
     *
     * @param attachment The payment attachment to save
     * @return true if attachment was saved successfully, false otherwise
     */
    public boolean save(PaymentAttachment attachment) {
        if (attachment.getId() == null) {
            return insert(attachment);
        } else {
            return update(attachment);
        }
    }

    /**
     * Inserts a new payment attachment template into the database.
     * Automatically generates ID and manages default template exclusivity.
     *
     * @param attachment The attachment to insert
     * @return true if insertion was successful, false otherwise
     */
    private boolean insert(PaymentAttachment attachment) {
        String sql = """
            INSERT INTO payment_attachment 
            (name, description, html_content, is_default, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (attachment.isDefault()) {
                unsetAllDefaults();
            }

            attachment.setUpdatedAt(LocalDateTime.now());
            if (attachment.getCreatedAt() == null) {
                attachment.setCreatedAt(LocalDateTime.now());
            }

            pstmt.setString(1, attachment.getName());
            pstmt.setString(2, attachment.getDescription());
            pstmt.setString(3, attachment.getHtmlContent());
            pstmt.setBoolean(4, attachment.isDefault());
            pstmt.setString(5, attachment.getCreatedAt().format(FORMATTER));
            pstmt.setString(6, attachment.getUpdatedAt().format(FORMATTER));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attachment.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting payment attachment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing payment attachment template in the database.
     * Automatically updates timestamp and manages default template exclusivity.
     *
     * @param attachment The attachment to update
     * @return true if update was successful, false otherwise
     */
    private boolean update(PaymentAttachment attachment) {
        String sql = """
            UPDATE payment_attachment SET 
            name = ?, description = ?, html_content = ?, is_default = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (attachment.isDefault()) {
                unsetAllDefaults();
            }

            attachment.setUpdatedAt(LocalDateTime.now());

            pstmt.setString(1, attachment.getName());
            pstmt.setString(2, attachment.getDescription());
            pstmt.setString(3, attachment.getHtmlContent());
            pstmt.setBoolean(4, attachment.isDefault());
            pstmt.setString(5, attachment.getUpdatedAt().format(FORMATTER));
            pstmt.setLong(6, attachment.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment attachment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a payment attachment template from the database.
     * Prevents deletion of the default template to maintain system integrity.
     *
     * @param id The ID of the payment attachment to delete
     * @return true if deletion was successful, false if template is default or error occurs
     */
    public boolean delete(Long id) {
        Optional<PaymentAttachment> attachment = findById(id);
        if (attachment.isPresent() && attachment.get().isDefault()) {
            System.err.println("Cannot delete default payment attachment template");
            return false;
        }

        String sql = "DELETE FROM payment_attachment WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting payment attachment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Sets a payment attachment template as the default template.
     * Uses transaction to ensure atomicity when switching default status.
     * Unsets all other templates as default before setting the new one.
     *
     * @param id The ID of the template to set as default
     * @return true if operation was successful, false otherwise
     */
    public boolean setAsDefault(Long id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                unsetAllDefaults();

                String sql = "UPDATE payment_attachment SET is_default = 1, updated_at = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, LocalDateTime.now().format(FORMATTER));
                    pstmt.setLong(2, id);
                    int updated = pstmt.executeUpdate();

                    if (updated > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error setting payment attachment as default: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Creates a duplicate of an existing payment attachment template with a new name.
     * The duplicate is never set as default and gets a modified description.
     * Useful for creating template variations.
     *
     * @param id The ID of the template to duplicate
     * @param newName The name for the new duplicate template
     * @return The newly created duplicate template, or null if duplication fails
     */
    public PaymentAttachment duplicate(Long id, String newName) {
        Optional<PaymentAttachment> original = findById(id);
        if (original.isPresent()) {
            PaymentAttachment copy = new PaymentAttachment();
            PaymentAttachment orig = original.get();

            copy.setName(newName);
            copy.setDescription("Copy of " + orig.getDescription());
            copy.setHtmlContent(orig.getHtmlContent());
            copy.setDefault(false);

            if (save(copy)) {
                return copy;
            }
        }
        return null;
    }

    /**
     * Checks if a template name already exists in the database.
     * Supports case-insensitive checking and can exclude a specific ID for update validation.
     *
     * @param name The name to check for existence
     * @param excludeId Optional ID to exclude from the check (for updates)
     * @return true if name exists, false otherwise
     */
    public boolean nameExists(String name, Long excludeId) {
        String sql = excludeId != null ?
                "SELECT COUNT(*) FROM payment_attachment WHERE LOWER(name) = LOWER(?) AND id != ?" :
                "SELECT COUNT(*) FROM payment_attachment WHERE LOWER(name) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            if (excludeId != null) {
                pstmt.setLong(2, excludeId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if name exists: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the total count of payment attachment templates in the database.
     * Useful for dashboard statistics and pagination.
     *
     * @return Number of payment attachment templates, 0 if error occurs
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM payment_attachment";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment attachment count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Unsets the default flag for all payment attachment templates.
     * Helper method used internally to maintain default template exclusivity.
     *
     * @throws SQLException if database operation fails
     */
    private void unsetAllDefaults() throws SQLException {
        String sql = "UPDATE payment_attachment SET is_default = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    /**
     * Maps a database ResultSet to a PaymentAttachment object.
     * Handles proper type conversion, date parsing, and boolean conversion.
     *
     * @param rs ResultSet containing payment attachment data
     * @return Populated PaymentAttachment object
     * @throws SQLException if database access error occurs or date parsing fails
     */
    private PaymentAttachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        PaymentAttachment attachment = new PaymentAttachment();

        attachment.setId(rs.getLong("id"));
        attachment.setName(rs.getString("name"));
        attachment.setDescription(rs.getString("description"));
        attachment.setHtmlContent(rs.getString("html_content"));
        attachment.setDefault(rs.getBoolean("is_default"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            attachment.setCreatedAt(LocalDateTime.parse(createdAtStr, FORMATTER));
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            attachment.setUpdatedAt(LocalDateTime.parse(updatedAtStr, FORMATTER));
        }

        return attachment;
    }
}