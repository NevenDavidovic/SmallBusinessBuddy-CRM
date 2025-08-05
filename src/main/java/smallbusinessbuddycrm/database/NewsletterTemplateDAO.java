package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.NewsletterTemplate;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NewsletterTemplateDAO {
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NewsletterTemplateDAO(Connection connection) {
        this.connection = connection;

    }

public NewsletterTemplate save(NewsletterTemplate template) throws SQLException {
        if (template.getId() == 0) {
            return insert(template);
        } else {
            return update(template);
        }
    }

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

    public List<NewsletterTemplate> findAll() throws SQLException {
        return findAll(true);
    }

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

    public boolean delete(int id) throws SQLException {
        String deleteSQL = "DELETE FROM newsletter_template WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean softDelete(int id) throws SQLException {
        String updateSQL = "UPDATE newsletter_template SET is_active = 0, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, LocalDateTime.now().format(DATE_FORMATTER));
            pstmt.setInt(2, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

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
}