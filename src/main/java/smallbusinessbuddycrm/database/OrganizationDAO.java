package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.model.Organization;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizationDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Create a new organization
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
            System.err.println("Greška pri spremanju organizacije: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update existing organization
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
            System.err.println("Greška pri ažuriranju organizacije: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Find organization by ID
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
            System.err.println("Greška pri dohvaćanju organizacije: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Get all organizations
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
            System.err.println("Greška pri dohvaćanju organizacija: " + e.getMessage());
            e.printStackTrace();
        }

        return organizations;
    }

    // Search organizations by name
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
            System.err.println("Greška pri pretraživanju organizacija: " + e.getMessage());
            e.printStackTrace();
        }

        return organizations;
    }

    // Delete organization
    public boolean delete(int id) {
        String sql = "DELETE FROM organization WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Greška pri brisanju organizacije: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if organization exists
    public boolean exists(int id) {
        String sql = "SELECT 1 FROM organization WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Greška pri provjeri postojanja organizacije: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get organization count
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM organization";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Greška pri brojanju organizacija: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // Get the first organization (useful for single organization setups)
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
            System.err.println("Greška pri dohvaćanju prve organizacije: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Helper method to map ResultSet to Organization object
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