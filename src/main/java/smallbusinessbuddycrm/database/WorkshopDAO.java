package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.Workshop;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkshopDAO {

    public List<Workshop> getAllWorkshops() {
        List<Workshop> workshops = new ArrayList<>();
        String query = "SELECT * FROM workshops ORDER BY from_date DESC, name";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Debug: Print connection info
            System.out.println("=== WorkshopDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            // Debug: Check if table exists and has data
            String countQuery = "SELECT COUNT(*) as count FROM workshops";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in workshops table: " + count);
                }
            }

            // Debug: Show table structure
            String tableInfoQuery = "PRAGMA table_info(workshops)";
            try (Statement infoStmt = conn.createStatement();
                 ResultSet infoRs = infoStmt.executeQuery(tableInfoQuery)) {

                System.out.println("Table structure:");
                while (infoRs.next()) {
                    System.out.println("  " + infoRs.getString("name") + " - " + infoRs.getString("type"));
                }
            }

            // Main query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("Executing query: " + query);

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    Workshop workshop = createWorkshopFromResultSet(rs);
                    workshops.add(workshop);

                    // Debug: Print first workshop details
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

            // Handle dates - convert LocalDate to String
            stmt.setString(2, workshop.getFromDate() != null ? workshop.getFromDate().toString() : null);
            stmt.setString(3, workshop.getToDate() != null ? workshop.getToDate().toString() : null);

            // Handle teacher_id
            if (workshop.getTeacherId() != null) {
                stmt.setInt(4, workshop.getTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, workshop.getCreatedAt());
            stmt.setString(6, workshop.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated ID
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

            // Handle dates - convert LocalDate to String
            stmt.setString(2, workshop.getFromDate() != null ? workshop.getFromDate().toString() : null);
            stmt.setString(3, workshop.getToDate() != null ? workshop.getToDate().toString() : null);

            // Handle teacher_id
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

    public boolean deleteWorkshops(List<Integer> workshopIds) {
        if (workshopIds.isEmpty()) {
            return false;
        }

        // Create placeholders for IN clause (?, ?, ?, ...)
        String placeholders = String.join(",", Collections.nCopies(workshopIds.size(), "?"));
        String query = "DELETE FROM workshops WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the workshop IDs as parameters
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

    // NEW: Get workshops by teacher
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

    // NEW: Assign teacher to workshop
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

    // NEW: Remove teacher from workshop
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

    // NEW: Get workshops with teacher information
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
                // You could add teacher name to workshop model or handle it separately
                workshops.add(workshop);
            }

            System.out.println("Found " + workshops.size() + " workshops with teacher information");
        } catch (SQLException e) {
            System.err.println("Error getting workshops with teachers: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    // Helper method: Extract workshop creation logic
    private Workshop createWorkshopFromResultSet(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();

        workshop.setId(rs.getInt("id"));
        workshop.setName(rs.getString("name"));

        // Handle teacher_id
        int teacherId = rs.getInt("teacher_id");
        if (!rs.wasNull()) {
            workshop.setTeacherId(teacherId);
        }

        // Handle dates
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