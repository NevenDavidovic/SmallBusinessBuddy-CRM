package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.Workshop;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for Workshop entity operations.
 * Handles database interactions for workshop management including CRUD operations,
 * teacher assignments, date-based filtering, and participant management.
 *
 * Features:
 * - Complete workshop lifecycle management
 * - Teacher assignment and management
 * - Date-based workshop filtering (active, upcoming, past)
 * - Search and filtering capabilities
 * - Bulk operations support
 * - Workshop status tracking
 * - Teacher relationship management
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class WorkshopDAO {

    /**
     * Retrieves all workshops from the database.
     * Includes comprehensive logging for debugging purposes.
     * Results are ordered by start date (most recent first), then by name.
     *
     * @return List of all workshops with full details, ordered by date and name
     */
    public List<Workshop> getAllWorkshops() {
        List<Workshop> workshops = new ArrayList<>();
        String query = "SELECT * FROM workshops ORDER BY from_date DESC, name";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== WorkshopDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            String countQuery = "SELECT COUNT(*) as count FROM workshops";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in workshops table: " + count);
                }
            }

            String tableInfoQuery = "PRAGMA table_info(workshops)";
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
                    Workshop workshop = createWorkshopFromResultSet(rs);
                    workshops.add(workshop);

                    if (rowCount == 1) {
                        System.out.println("First workshop loaded: " + workshop.getName() + " (" + workshop.getDateRange() + ")");
                        System.out.println("  Duration: " + workshop.getDurationInDays() + " days");
                        System.out.println("  Status: " + (workshop.isActive() ? "Active" : workshop.isUpcoming() ? "Upcoming" : "Past"));
                        System.out.println("  Has Teacher: " + workshop.hasTeacher());
                    }
                }

                System.out.println("Query returned " + rowCount + " rows");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllWorkshops: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + workshops.size() + " workshops from DAO");
        System.out.println("===============================");

        return workshops;
    }

    /**
     * Creates a new workshop in the database.
     * Automatically generates ID and handles date conversions.
     * Teacher assignment is optional and can be set later.
     *
     * @param workshop The workshop object to create
     * @return true if workshop was created successfully, false otherwise
     */
    public boolean createWorkshop(Workshop workshop) {
        String query = """
        INSERT INTO workshops (
            name, from_date, to_date, teacher_id,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, workshop.getName());
            stmt.setString(2, workshop.getFromDate() != null ? workshop.getFromDate().toString() : null);
            stmt.setString(3, workshop.getToDate() != null ? workshop.getToDate().toString() : null);

            if (workshop.getTeacherId() != null) {
                stmt.setInt(4, workshop.getTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, workshop.getCreatedAt());
            stmt.setString(6, workshop.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        workshop.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Workshop created successfully with ID: " + workshop.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing workshop in the database.
     * Automatically updates the updated_at timestamp.
     * Handles date conversions and teacher assignment changes.
     *
     * @param workshop The workshop object with updated information
     * @return true if workshop was updated successfully, false otherwise
     */
    public boolean updateWorkshop(Workshop workshop) {
        String query = """
        UPDATE workshops SET 
            name = ?, from_date = ?, to_date = ?, teacher_id = ?,
            updated_at = ?
        WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, workshop.getName());
            stmt.setString(2, workshop.getFromDate() != null ? workshop.getFromDate().toString() : null);
            stmt.setString(3, workshop.getToDate() != null ? workshop.getToDate().toString() : null);

            if (workshop.getTeacherId() != null) {
                stmt.setInt(4, workshop.getTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, java.time.LocalDateTime.now().toString());
            stmt.setInt(6, workshop.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Workshop updated successfully with ID: " + workshop.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a single workshop from the database.
     * This operation will cascade to workshop participants and related data.
     *
     * @param workshopId The ID of the workshop to delete
     * @return true if workshop was deleted successfully, false otherwise
     */
    public boolean deleteWorkshop(int workshopId) {
        String query = "DELETE FROM workshops WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Workshop deleted successfully with ID: " + workshopId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes multiple workshops in a single database operation.
     * Uses optimized IN clause for better performance than individual deletes.
     * This operation will cascade to participants and related data.
     *
     * @param workshopIds List of workshop IDs to delete
     * @return true if all workshops were deleted successfully, false otherwise
     */
    public boolean deleteWorkshops(List<Integer> workshopIds) {
        if (workshopIds.isEmpty()) {
            return false;
        }

        String placeholders = String.join(",", Collections.nCopies(workshopIds.size(), "?"));
        String query = "DELETE FROM workshops WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < workshopIds.size(); i++) {
                stmt.setInt(i + 1, workshopIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted " + rowsAffected + " workshops successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves a specific workshop by ID.
     *
     * @param workshopId The ID of the workshop to retrieve
     * @return Workshop object if found, null otherwise
     */
    public Workshop getWorkshopById(int workshopId) {
        String query = "SELECT * FROM workshops WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createWorkshopFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting workshop by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves all workshops that are currently active (running today).
     * A workshop is active if today's date falls between start and end dates.
     * Results are ordered by start date.
     *
     * @return List of currently active workshops, ordered by start date
     */
    public List<Workshop> getActiveWorkshops() {
        List<Workshop> workshops = new ArrayList<>();
        String today = LocalDate.now().toString();
        String query = """
        SELECT * FROM workshops 
        WHERE from_date <= ? AND to_date >= ?
        ORDER BY from_date
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, today);
            stmt.setString(2, today);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Workshop workshop = createWorkshopFromResultSet(rs);
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " active workshops");
        } catch (SQLException e) {
            System.err.println("Error getting active workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Retrieves workshops that are starting within the specified number of days.
     * Used for upcoming workshop notifications and planning.
     * Results are ordered by start date.
     *
     * @param daysAhead Number of days in the future to check for workshops
     * @return List of upcoming workshops, ordered by start date
     */
    public List<Workshop> getUpcomingWorkshops(int daysAhead) {
        List<Workshop> workshops = new ArrayList<>();
        String today = LocalDate.now().toString();
        String futureDate = LocalDate.now().plusDays(daysAhead).toString();

        String query = """
        SELECT * FROM workshops 
        WHERE from_date > ? AND from_date <= ?
        ORDER BY from_date
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, today);
            stmt.setString(2, futureDate);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Workshop workshop = createWorkshopFromResultSet(rs);
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " upcoming workshops in the next " + daysAhead + " days");
        } catch (SQLException e) {
            System.err.println("Error getting upcoming workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Searches for workshops by name using partial matching.
     * Results are ordered by start date (most recent first), then by name.
     *
     * @param searchTerm The search term to match against workshop names
     * @return List of workshops with names containing the search term
     */
    public List<Workshop> searchWorkshops(String searchTerm) {
        List<Workshop> workshops = new ArrayList<>();
        String query = """
        SELECT * FROM workshops 
        WHERE name LIKE ? 
        ORDER BY from_date DESC, name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Workshop workshop = createWorkshopFromResultSet(rs);
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " workshops matching: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("Error searching workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Retrieves all workshops assigned to a specific teacher.
     * Results are ordered by start date (most recent first), then by name.
     *
     * @param teacherId The ID of the teacher
     * @return List of workshops assigned to the teacher
     */
    public List<Workshop> getWorkshopsByTeacher(int teacherId) {
        List<Workshop> workshops = new ArrayList<>();
        String query = """
        SELECT * FROM workshops 
        WHERE teacher_id = ?
        ORDER BY from_date DESC, name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Workshop workshop = createWorkshopFromResultSet(rs);
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " workshops for teacher ID: " + teacherId);
        } catch (SQLException e) {
            System.err.println("Error getting workshops by teacher: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Assigns a teacher to a specific workshop.
     * Updates the workshop's teacher assignment and timestamp.
     *
     * @param workshopId The ID of the workshop
     * @param teacherId The ID of the teacher to assign
     * @return true if teacher was assigned successfully, false otherwise
     */
    public boolean assignTeacherToWorkshop(int workshopId, int teacherId) {
        String query = "UPDATE workshops SET teacher_id = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, teacherId);
            stmt.setString(2, java.time.LocalDateTime.now().toString());
            stmt.setInt(3, workshopId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher " + teacherId + " assigned to workshop " + workshopId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error assigning teacher to workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Removes the teacher assignment from a workshop.
     * Sets the teacher_id to NULL and updates timestamp.
     *
     * @param workshopId The ID of the workshop to remove teacher from
     * @return true if teacher was removed successfully, false otherwise
     */
    public boolean removeTeacherFromWorkshop(int workshopId) {
        String query = "UPDATE workshops SET teacher_id = NULL, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, java.time.LocalDateTime.now().toString());
            stmt.setInt(2, workshopId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher removed from workshop " + workshopId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error removing teacher from workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves all workshops with their associated teacher information.
     * Uses LEFT JOIN to include workshops without assigned teachers.
     * Results are ordered by start date (most recent first), then by name.
     *
     * @return List of workshops with teacher information included
     */
    public List<Workshop> getWorkshopsWithTeachers() {
        List<Workshop> workshops = new ArrayList<>();
        String query = """
        SELECT w.*, t.first_name as teacher_first_name, t.last_name as teacher_last_name
        FROM workshops w
        LEFT JOIN teachers t ON w.teacher_id = t.id
        ORDER BY w.from_date DESC, w.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Workshop workshop = createWorkshopFromResultSet(rs);
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " workshops with teacher information");
        } catch (SQLException e) {
            System.err.println("Error getting workshops with teachers: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Creates a Workshop object from a database ResultSet.
     * Handles proper type conversion, date parsing, and null value management.
     * Includes comprehensive error handling for date parsing operations.
     *
     * @param rs ResultSet containing workshop data
     * @return Populated Workshop object
     * @throws SQLException if database access error occurs
     */
    private Workshop createWorkshopFromResultSet(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();

        workshop.setId(rs.getInt("id"));
        workshop.setName(rs.getString("name"));

        int teacherId = rs.getInt("teacher_id");
        if (!rs.wasNull()) {
            workshop.setTeacherId(teacherId);
        }

        String fromDateStr = rs.getString("from_date");
        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                workshop.setFromDate(LocalDate.parse(fromDateStr));
            } catch (Exception e) {
                System.err.println("Error parsing from_date: " + fromDateStr);
            }
        }

        String toDateStr = rs.getString("to_date");
        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                workshop.setToDate(LocalDate.parse(toDateStr));
            } catch (Exception e) {
                System.err.println("Error parsing to_date: " + toDateStr);
            }
        }

        workshop.setCreatedAt(rs.getString("created_at"));
        workshop.setUpdatedAt(rs.getString("updated_at"));

        return workshop;
    }
}