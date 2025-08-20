package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.UnderagedMember;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for UnderagedMember entity operations.
 * Handles database interactions for underaged member management including CRUD operations,
 * contact relationships, age tracking, and membership management.
 *
 * Features:
 * - Complete underaged member lifecycle management
 * - Contact relationship tracking
 * - Age and birth date management
 * - Membership status and period tracking
 * - Gender and personal information storage
 * - Bulk operations support
 * - Guardian contact association
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class UnderagedDAO {

    /**
     * Retrieves all underaged members from the database.
     * Includes comprehensive logging for debugging purposes.
     * Contains complete member information including contact relationships.
     *
     * @return List of all underaged members with full details
     */
    public List<UnderagedMember> getAllUnderagedMembers() {
        List<UnderagedMember> underagedMembers = new ArrayList<>();
        String query = "SELECT * FROM underaged";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== UnderagedDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            String countQuery = "SELECT COUNT(*) as count FROM underaged";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in underaged table: " + count);
                }
            }

            String tableInfoQuery = "PRAGMA table_info(underaged)";
            try (Statement infoStmt = conn.createStatement();
                 ResultSet infoRs = infoStmt.executeQuery(tableInfoQuery)) {

                System.out.println("Table structure:");
                while (infoRs.next()) {
                    System.out.println("  " + infoRs.getString("name") + " - " + infoRs.getString("type"));
                }
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("Executing query: " + query);

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    UnderagedMember underagedMember = createUnderagedMemberFromResultSet(rs);
                    underagedMembers.add(underagedMember);

                    if (rowCount == 1) {
                        System.out.println("First underaged member loaded: " + underagedMember.getFirstName() + " " + underagedMember.getLastName() + " (Age: " + underagedMember.getAge() + ")");
                        System.out.println("  Birth Date: " + underagedMember.getBirthDate());
                        System.out.println("  PIN: " + underagedMember.getPin());
                        System.out.println("  Member Since: " + underagedMember.getMemberSince());
                        System.out.println("  Contact ID: " + underagedMember.getContactId());
                    }
                }

                System.out.println("Query returned " + rowCount + " rows");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllUnderagedMembers: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + underagedMembers.size() + " underaged members from DAO");
        System.out.println("===============================");

        return underagedMembers;
    }

    /**
     * Retrieves all underaged members associated with a specific contact (guardian).
     * Used to display children/dependents for a particular contact.
     * Results are ordered alphabetically by name.
     *
     * @param contactId The ID of the guardian contact
     * @return List of underaged members associated with the contact, ordered by name
     */
    public List<UnderagedMember> getUnderagedMembersByContactId(int contactId) {
        List<UnderagedMember> underagedMembers = new ArrayList<>();
        String query = "SELECT * FROM underaged WHERE contact_id = ? ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, contactId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UnderagedMember underagedMember = createUnderagedMemberFromResultSet(rs);
                underagedMembers.add(underagedMember);
            }

            System.out.println("Loaded " + underagedMembers.size() + " underaged members for contact ID: " + contactId);
        } catch (SQLException e) {
            System.err.println("Error loading underaged members for contact: " + e.getMessage());
            e.printStackTrace();
        }

        return underagedMembers;
    }

    /**
     * Creates a new underaged member in the database.
     * Automatically generates ID and handles date conversions.
     * Establishes guardian relationship through contact_id.
     *
     * @param underagedMember The underaged member object to create
     * @return true if member was created successfully, false otherwise
     */
    public boolean createUnderagedMember(UnderagedMember underagedMember) {
        String query = """
        INSERT INTO underaged (
            first_name, last_name, birth_date, age, pin, gender, 
            is_member, member_since, member_until, note, 
            created_at, updated_at, contact_id
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, underagedMember.getFirstName());
            stmt.setString(2, underagedMember.getLastName());
            stmt.setString(3, underagedMember.getBirthDate() != null ? underagedMember.getBirthDate().toString() : null);
            stmt.setInt(4, underagedMember.getAge());
            stmt.setString(5, underagedMember.getPin());
            stmt.setString(6, underagedMember.getGender());
            stmt.setInt(7, underagedMember.isMember() ? 1 : 0);
            stmt.setString(8, underagedMember.getMemberSince() != null ? underagedMember.getMemberSince().toString() : null);
            stmt.setString(9, underagedMember.getMemberUntil() != null ? underagedMember.getMemberUntil().toString() : null);
            stmt.setString(10, underagedMember.getNote());
            stmt.setString(11, underagedMember.getCreatedAt());
            stmt.setString(12, underagedMember.getUpdatedAt());
            stmt.setInt(13, underagedMember.getContactId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        underagedMember.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Underaged member created successfully with ID: " + underagedMember.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating underaged member: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing underaged member in the database.
     * Automatically updates the updated_at timestamp.
     * Handles date conversions and guardian relationship updates.
     *
     * @param underagedMember The underaged member object with updated information
     * @return true if member was updated successfully, false otherwise
     */
    public boolean updateUnderagedMember(UnderagedMember underagedMember) {
        String query = """
        UPDATE underaged SET 
            first_name = ?, last_name = ?, birth_date = ?, age = ?, pin = ?, 
            gender = ?, is_member = ?, member_since = ?, member_until = ?, 
            note = ?, updated_at = ?, contact_id = ?
        WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, underagedMember.getFirstName());
            stmt.setString(2, underagedMember.getLastName());
            stmt.setString(3, underagedMember.getBirthDate() != null ? underagedMember.getBirthDate().toString() : null);
            stmt.setInt(4, underagedMember.getAge());
            stmt.setString(5, underagedMember.getPin());
            stmt.setString(6, underagedMember.getGender());
            stmt.setInt(7, underagedMember.isMember() ? 1 : 0);
            stmt.setString(8, underagedMember.getMemberSince() != null ? underagedMember.getMemberSince().toString() : null);
            stmt.setString(9, underagedMember.getMemberUntil() != null ? underagedMember.getMemberUntil().toString() : null);
            stmt.setString(10, underagedMember.getNote());
            stmt.setString(11, java.time.LocalDateTime.now().toString());
            stmt.setInt(12, underagedMember.getContactId());
            stmt.setInt(13, underagedMember.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Underaged member updated successfully with ID: " + underagedMember.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating underaged member: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a single underaged member from the database.
     * This operation will remove all associated records including workshop participations.
     *
     * @param underagedMemberId The ID of the underaged member to delete
     * @return true if member was deleted successfully, false otherwise
     */
    public boolean deleteUnderagedMember(int underagedMemberId) {
        String query = "DELETE FROM underaged WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, underagedMemberId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Underaged member deleted successfully with ID: " + underagedMemberId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting underaged member: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes multiple underaged members in a single database operation.
     * Uses optimized IN clause for better performance than individual deletes.
     * This operation will cascade to workshop participations.
     *
     * @param underagedMemberIds List of underaged member IDs to delete
     * @return true if all members were deleted successfully, false otherwise
     */
    public boolean deleteUnderagedMembers(List<Integer> underagedMemberIds) {
        if (underagedMemberIds.isEmpty()) {
            return false;
        }

        String placeholders = String.join(",", Collections.nCopies(underagedMemberIds.size(), "?"));
        String query = "DELETE FROM underaged WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < underagedMemberIds.size(); i++) {
                stmt.setInt(i + 1, underagedMemberIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted " + rowsAffected + " underaged members successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting underaged members: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves a specific underaged member by ID.
     *
     * @param id The ID of the underaged member to retrieve
     * @return UnderagedMember object if found, null otherwise
     */
    public UnderagedMember getUnderagedMemberById(int id) {
        String query = "SELECT * FROM underaged WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createUnderagedMemberFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting underaged member by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates an UnderagedMember object from a database ResultSet.
     * Handles proper type conversion, date parsing, and null value management.
     * Includes comprehensive error handling for date parsing operations.
     *
     * @param rs ResultSet containing underaged member data
     * @return Populated UnderagedMember object
     * @throws SQLException if database access error occurs
     */
    private UnderagedMember createUnderagedMemberFromResultSet(ResultSet rs) throws SQLException {
        UnderagedMember underagedMember = new UnderagedMember();

        underagedMember.setId(rs.getInt("id"));
        underagedMember.setFirstName(rs.getString("first_name"));
        underagedMember.setLastName(rs.getString("last_name"));

        String birthDateStr = rs.getString("birth_date");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            try {
                underagedMember.setBirthDate(LocalDate.parse(birthDateStr));
            } catch (Exception e) {
                System.err.println("Error parsing birth_date: " + birthDateStr);
            }
        }

        underagedMember.setAge(rs.getInt("age"));
        underagedMember.setPin(rs.getString("pin"));
        underagedMember.setGender(rs.getString("gender"));
        underagedMember.setMember(rs.getInt("is_member") == 1);

        String memberSinceStr = rs.getString("member_since");
        if (memberSinceStr != null && !memberSinceStr.trim().isEmpty()) {
            try {
                underagedMember.setMemberSince(LocalDate.parse(memberSinceStr));
            } catch (Exception e) {
                System.err.println("Error parsing member_since date: " + memberSinceStr);
            }
        }

        String memberUntilStr = rs.getString("member_until");
        if (memberUntilStr != null && !memberUntilStr.trim().isEmpty()) {
            try {
                underagedMember.setMemberUntil(LocalDate.parse(memberUntilStr));
            } catch (Exception e) {
                System.err.println("Error parsing member_until date: " + memberUntilStr);
            }
        }

        underagedMember.setNote(rs.getString("note"));
        underagedMember.setCreatedAt(rs.getString("created_at"));
        underagedMember.setUpdatedAt(rs.getString("updated_at"));
        underagedMember.setContactId(rs.getInt("contact_id"));

        return underagedMember;
    }
}