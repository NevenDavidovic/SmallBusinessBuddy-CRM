package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.model.Organization;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Organization entity operations.
 * Handles database interactions for organization management including CRUD operations,
 * search functionality, and existence validation.
 *
 * Features:
 * - Complete organization lifecycle management
 * - Name-based search capabilities
 * - Image/logo storage and retrieval
 * - Single organization setup support
 * - Existence validation
 * - Statistics and counting
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class OrganizationDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates a new organization in the database.
     * Automatically generates ID and sets creation timestamp.
     * Handles binary image data storage.
     *
     * @param organization The organization object to save
     * @return true if organization was created successfully, false otherwise
     */
    public boolean save(Organization organization) {
        String sql = """
            INSERT INTO organization (name, IBAN, street_name, street_num, postal_code, 
                                    city, email, image, phone_num, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, organization.getName());
            pstmt.setString(2, organization.getIban());
            pstmt.setString(3, organization.getStreetName());
            pstmt.setString(4, organization.getStreetNum());
            pstmt.setString(5, organization.getPostalCode());
            pstmt.setString(6, organization.getCity());
            pstmt.setString(7, organization.getEmail());
            if (organization.getImage() != null) {
                pstmt.setBytes(8, organization.getImage());
            } else {
                pstmt.setNull(8, java.sql.Types.BLOB);
            }
            pstmt.setString(9, organization.getPhoneNum());
            pstmt.setString(10, organization.getCreatedAt().format(DATE_FORMATTER));
            pstmt.setString(11, organization.getUpdatedAt().format(DATE_FORMATTER));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        organization.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error saving organization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing organization in the database.
     * Automatically updates the updated_at timestamp.
     * Handles binary image data updates.
     *
     * @param organization The organization object with updated information
     * @return true if organization was updated successfully, false otherwise
     */
    public boolean update(Organization organization) {
        String sql = """
            UPDATE organization 
            SET name = ?, IBAN = ?, street_name = ?, street_num = ?, postal_code = ?, 
                city = ?, email = ?, image = ?, phone_num = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            organization.setUpdatedAt(LocalDateTime.now());

            pstmt.setString(1, organization.getName());
            pstmt.setString(2, organization.getIban());
            pstmt.setString(3, organization.getStreetName());
            pstmt.setString(4, organization.getStreetNum());
            pstmt.setString(5, organization.getPostalCode());
            pstmt.setString(6, organization.getCity());
            pstmt.setString(7, organization.getEmail());
            if (organization.getImage() != null) {
                pstmt.setBytes(8, organization.getImage());
            } else {
                pstmt.setNull(8, java.sql.Types.BLOB);
            }
            pstmt.setString(9, organization.getPhoneNum());
            pstmt.setString(10, organization.getUpdatedAt().format(DATE_FORMATTER));
            pstmt.setInt(11, organization.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating organization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves an organization by its ID.
     *
     * @param id The ID of the organization to retrieve
     * @return Optional containing the organization if found, empty Optional otherwise
     */
    public Optional<Organization> findById(int id) {
        String sql = "SELECT * FROM organization WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToOrganization(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Error retrieving organization: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Retrieves all organizations from the database.
     * Results are ordered alphabetically by name.
     *
     * @return List of all organizations, ordered by name
     */
    public List<Organization> findAll() {
        String sql = "SELECT * FROM organization ORDER BY name";
        List<Organization> organizations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                organizations.add(mapResultSetToOrganization(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving organizations: " + e.getMessage());
            e.printStackTrace();
        }

        return organizations;
    }

    /**
     * Searches for organizations by name using partial matching.
     * Uses LIKE query for flexible name searching.
     *
     * @param name The name or partial name to search for
     * @return List of organizations with names containing the search term, ordered by name
     */
    public List<Organization> findByName(String name) {
        String sql = "SELECT * FROM organization WHERE name LIKE ? ORDER BY name";
        List<Organization> organizations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                organizations.add(mapResultSetToOrganization(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching organizations: " + e.getMessage());
            e.printStackTrace();
        }

        return organizations;
    }

    /**
     * Permanently deletes an organization from the database.
     * This operation cannot be undone and will cascade to related data.
     *
     * @param id The ID of the organization to delete
     * @return true if organization was deleted successfully, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM organization WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting organization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if an organization exists with the specified ID.
     * Efficient existence check without retrieving full organization data.
     *
     * @param id The ID to check for existence
     * @return true if organization exists, false otherwise
     */
    public boolean exists(int id) {
        String sql = "SELECT 1 FROM organization WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking organization existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the total count of organizations in the database.
     * Useful for dashboard statistics and pagination.
     *
     * @return Number of organizations in the database, 0 if error occurs
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM organization";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting organizations: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Retrieves the first organization from the database.
     * Useful for single organization setups where only one organization exists.
     * Commonly used for loading the primary organization configuration.
     *
     * @return Optional containing the first organization if found, empty Optional otherwise
     */
    public Optional<Organization> getFirst() {
        String sql = "SELECT * FROM organization ORDER BY id LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapResultSetToOrganization(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Error retrieving first organization: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Maps a database ResultSet to an Organization object.
     * Handles proper type conversion, date parsing, and binary data retrieval.
     *
     * @param rs ResultSet containing organization data
     * @return Populated Organization object
     * @throws SQLException if database access error occurs or date parsing fails
     */
    private Organization mapResultSetToOrganization(ResultSet rs) throws SQLException {
        Organization org = new Organization();
        org.setId(rs.getInt("id"));
        org.setName(rs.getString("name"));
        org.setIban(rs.getString("IBAN"));
        org.setStreetName(rs.getString("street_name"));
        org.setStreetNum(rs.getString("street_num"));
        org.setPostalCode(rs.getString("postal_code"));
        org.setCity(rs.getString("city"));
        org.setEmail(rs.getString("email"));
        org.setImage(rs.getBytes("image"));
        org.setPhoneNum(rs.getString("phone_num"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            org.setCreatedAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER));
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            org.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DATE_FORMATTER));
        }

        return org;
    }
}