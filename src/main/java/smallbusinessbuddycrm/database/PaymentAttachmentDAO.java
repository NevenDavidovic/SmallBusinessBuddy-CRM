// PaymentAttachmentDAO.java - Data Access Object
package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.PaymentAttachment;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentAttachmentDAO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Find all payment attachment templates
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
     * Find payment attachment by ID
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
     * Find the default payment attachment template
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
     * Find templates by name (case-insensitive search)
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
     * Save (insert or update) payment attachment
     */
    public boolean save(PaymentAttachment attachment) {
        if (attachment.getId() == null) {
            return insert(attachment);
        } else {
            return update(attachment);
        }
    }

    /**
     * Insert new payment attachment
     */
    private boolean insert(PaymentAttachment attachment) {
        String sql = """
            INSERT INTO payment_attachment 
            (name, description, html_content, is_default, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // If this is being set as default, unset other defaults first
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
     * Update existing payment attachment
     */
    private boolean update(PaymentAttachment attachment) {
        String sql = """
            UPDATE payment_attachment SET 
            name = ?, description = ?, html_content = ?, is_default = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // If this is being set as default, unset other defaults first
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
     * Delete payment attachment by ID
     */
    public boolean delete(Long id) {
        // Check if this is the default template
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
     * Set a template as the default (and unset others)
     */
    public boolean setAsDefault(Long id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // First unset all defaults
                unsetAllDefaults();

                // Then set the specified one as default
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
     * Duplicate an existing template with a new name
     */
    public PaymentAttachment duplicate(Long id, String newName) {
        Optional<PaymentAttachment> original = findById(id);
        if (original.isPresent()) {
            PaymentAttachment copy = new PaymentAttachment();
            PaymentAttachment orig = original.get();

            copy.setName(newName);
            copy.setDescription("Copy of " + orig.getDescription());
            copy.setHtmlContent(orig.getHtmlContent());
            copy.setDefault(false); // Copies are never default

            if (save(copy)) {
                return copy;
            }
        }
        return null;
    }

    /**
     * Check if a name already exists (for validation)
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
     * Get count of all templates
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
     * Unset all default flags (helper method)
     */
    private void unsetAllDefaults() throws SQLException {
        String sql = "UPDATE payment_attachment SET is_default = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet to PaymentAttachment object
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