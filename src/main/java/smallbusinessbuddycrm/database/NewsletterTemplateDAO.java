package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.NewsletterTemplate;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for NewsletterTemplate entity operations.
 * Handles database interactions for newsletter template management including CRUD operations,
 * template duplication, search functionality, and soft delete capabilities.
 *
 * Features:
 * - Complete template lifecycle management
 * - Type-based template filtering
 * - Template duplication functionality
 * - Search and filtering capabilities
 * - Soft delete and hard delete options
 * - Active/inactive template management
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class NewsletterTemplateDAO {
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Constructs a new NewsletterTemplateDAO with the specified database connection.
     *
     * @param connection Database connection to use for operations
     */
    public NewsletterTemplateDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Saves a newsletter template to the database.
     * Automatically determines whether to insert or update based on template ID.
     *
     * @param template The newsletter template to save
     * @return The saved template with updated ID and timestamps
     * @throws SQLException if database operation fails
     */
    public NewsletterTemplate save(NewsletterTemplate template) throws SQLException {
        if (template.getId() == 0) {
            return insert(template);
        } else {
            return update(template);
        }
    }

    /**
     * Inserts a new newsletter template into the database.
     * Automatically generates ID and sets creation timestamp.
     *
     * @param template The template to insert
     * @return The inserted template with generated ID
     * @throws SQLException if insertion fails
     */
    private NewsletterTemplate insert(NewsletterTemplate template) throws SQLException {
        String insertSQL = """
            INSERT INTO newsletter_template (name, subject, content, template_type, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, template.getName());
            pstmt.setString(2, template.getSubject());
            pstmt.setString(3, template.getContent());
            pstmt.setString(4, template.getTemplateType());
            pstmt.setInt(5, template.isActive() ? 1 : 0);
            pstmt.setString(6, template.getCreatedAt().format(DATE_FORMATTER));
            pstmt.setString(7, template.getUpdatedAt().format(DATE_FORMATTER));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating newsletter template failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    template.setId(generatedKeys.getInt(1));
                    return template;
                } else {
                    throw new SQLException("Creating newsletter template failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing newsletter template in the database.
     * Automatically updates the updated_at timestamp.
     *
     * @param template The template to update
     * @return The updated template
     * @throws SQLException if update fails
     */
    private NewsletterTemplate update(NewsletterTemplate template) throws SQLException {
        String updateSQL = """
            UPDATE newsletter_template 
            SET name = ?, subject = ?, content = ?, template_type = ?, is_active = ?, updated_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            template.setUpdatedAt(LocalDateTime.now());

            pstmt.setString(1, template.getName());
            pstmt.setString(2, template.getSubject());
            pstmt.setString(3, template.getContent());
            pstmt.setString(4, template.getTemplateType());
            pstmt.setInt(5, template.isActive() ? 1 : 0);
            pstmt.setString(6, template.getUpdatedAt().format(DATE_FORMATTER));
            pstmt.setInt(7, template.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating newsletter template failed, no rows affected.");
            }
            return template;
        }
    }

    /**
     * Retrieves a newsletter template by its ID.
     *
     * @param id The ID of the template to retrieve
     * @return The template if found, null otherwise
     * @throws SQLException if database access error occurs
     */
    public NewsletterTemplate findById(int id) throws SQLException {
        String selectSQL = "SELECT * FROM newsletter_template WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTemplate(rs);
                }
                return null;
            }
        }
    }

    /**
     * Retrieves all active newsletter templates.
     * Convenience method that calls findAll(true).
     *
     * @return List of all active templates, ordered by most recently updated
     * @throws SQLException if database access error occurs
     */
    public List<NewsletterTemplate> findAll() throws SQLException {
        return findAll(true);
    }

    /**
     * Retrieves all newsletter templates, optionally filtering by active status.
     *
     * @param activeOnly If true, returns only active templates; if false, returns all templates
     * @return List of templates ordered by most recently updated
     * @throws SQLException if database access error occurs
     */
    public List<NewsletterTemplate> findAll(boolean activeOnly) throws SQLException {
        String selectSQL = activeOnly ?
                "SELECT * FROM newsletter_template WHERE is_active = 1 ORDER BY updated_at DESC" :
                "SELECT * FROM newsletter_template ORDER BY updated_at DESC";

        List<NewsletterTemplate> templates = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }
        }

        return templates;
    }

    /**
     * Retrieves newsletter templates by template type.
     * Only returns active templates.
     *
     * @param templateType The type of templates to retrieve (e.g., "PAYMENT", "MARKETING")
     * @return List of templates matching the specified type, ordered by most recently updated
     * @throws SQLException if database access error occurs
     */
    public List<NewsletterTemplate> findByType(String templateType) throws SQLException {
        String selectSQL = "SELECT * FROM newsletter_template WHERE template_type = ? AND is_active = 1 ORDER BY updated_at DESC";

        List<NewsletterTemplate> templates = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, templateType);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapResultSetToTemplate(rs));
                }
            }
        }

        return templates;
    }

    /**
     * Searches for newsletter templates by name using partial matching.
     * Only returns active templates.
     *
     * @param searchTerm The search term to match against template names
     * @return List of templates with names containing the search term, ordered by most recently updated
     * @throws SQLException if database access error occurs
     */
    public List<NewsletterTemplate> searchByName(String searchTerm) throws SQLException {
        String selectSQL = "SELECT * FROM newsletter_template WHERE name LIKE ? AND is_active = 1 ORDER BY updated_at DESC";

        List<NewsletterTemplate> templates = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapResultSetToTemplate(rs));
                }
            }
        }

        return templates;
    }

    /**
     * Permanently deletes a newsletter template from the database.
     * This operation cannot be undone. Consider using softDelete() for data preservation.
     *
     * @param id The ID of the template to delete
     * @return true if template was deleted successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean delete(int id) throws SQLException {
        String deleteSQL = "DELETE FROM newsletter_template WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Soft deletes a newsletter template by marking it as inactive.
     * Preserves the template data while making it unavailable for normal operations.
     * Recommended over hard delete for data integrity.
     *
     * @param id The ID of the template to soft delete
     * @return true if template was soft deleted successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean softDelete(int id) throws SQLException {
        String updateSQL = "UPDATE newsletter_template SET is_active = 0, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, LocalDateTime.now().format(DATE_FORMATTER));
            pstmt.setInt(2, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Creates a duplicate of an existing newsletter template with a new name.
     * Useful for creating template variations or backups.
     *
     * @param templateId The ID of the template to duplicate
     * @param newName The name for the new duplicate template
     * @return The newly created duplicate template
     * @throws SQLException if template not found or duplication fails
     */
    public NewsletterTemplate duplicate(int templateId, String newName) throws SQLException {
        NewsletterTemplate original = findById(templateId);
        if (original == null) {
            throw new SQLException("Template with ID " + templateId + " not found");
        }

        NewsletterTemplate duplicate = new NewsletterTemplate(
                newName,
                "Copy of " + original.getSubject(),
                original.getContent()
        );
        duplicate.setTemplateType(original.getTemplateType());

        return insert(duplicate);
    }

    /**
     * Gets the total count of active newsletter templates.
     * Useful for dashboard statistics and pagination.
     *
     * @return Number of active templates in the database
     * @throws SQLException if database access error occurs
     */
    public int getTemplateCount() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM newsletter_template WHERE is_active = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Maps a database ResultSet to a NewsletterTemplate object.
     * Handles proper type conversion and date parsing.
     *
     * @param rs ResultSet containing template data
     * @return Populated NewsletterTemplate object
     * @throws SQLException if database access error occurs or date parsing fails
     */
    private NewsletterTemplate mapResultSetToTemplate(ResultSet rs) throws SQLException {
        return new NewsletterTemplate(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("subject"),
                rs.getString("content"),
                rs.getString("template_type"),
                rs.getInt("is_active") == 1,
                LocalDateTime.parse(rs.getString("created_at"), DATE_FORMATTER),
                LocalDateTime.parse(rs.getString("updated_at"), DATE_FORMATTER)
        );
    }
}