package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeacherDAO {

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Debug: Print connection info
            System.out.println("=== TeacherDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            // Debug: Check if table exists and has data
            String countQuery = "SELECT COUNT(*) as count FROM teachers";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in teachers table: " + count);
                }
            }

            // Debug: Show table structure
            String tableInfoQuery = "PRAGMA table_info(teachers)";
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
                    Teacher teacher = createTeacherFromResultSet(rs);
                    teachers.add(teacher);

                    // Debug: Print first teacher details
                    if (rowCount == 1) {
                        System.out.println("First teacher loaded: " + teacher.getFirstName() + " " + teacher.getLastName() + " (" + teacher.getEmail() + ")");
                    }
                }

                System.out.println("Query returned " + rowCount + " rows");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllTeachers: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + teachers.size() + " teachers from DAO");
        System.out.println("===============================");

        return teachers;
    }

    public boolean createTeacher(Teacher teacher) {
        String query = """
        INSERT INTO teachers (
            first_name, last_name, email, phone_num, 
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, teacher.getFirstName());
            stmt.setString(2, teacher.getLastName());
            stmt.setString(3, teacher.getEmail());
            stmt.setString(4, teacher.getPhoneNum());
            stmt.setString(5, teacher.getCreatedAt());
            stmt.setString(6, teacher.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        teacher.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Teacher created successfully with ID: " + teacher.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating teacher: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTeacher(Teacher teacher) {
        String query = """
        UPDATE teachers SET 
            first_name = ?, last_name = ?, email = ?, phone_num = ?, 
            updated_at = ?
        WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teacher.getFirstName());
            stmt.setString(2, teacher.getLastName());
            stmt.setString(3, teacher.getEmail());
            stmt.setString(4, teacher.getPhoneNum());
            stmt.setString(5, java.time.LocalDateTime.now().toString());
            stmt.setInt(6, teacher.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher updated successfully with ID: " + teacher.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating teacher: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteTeacher(int teacherId) {
        String query = "DELETE FROM teachers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, teacherId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher deleted successfully with ID: " + teacherId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting teacher: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteTeachers(List<Integer> teacherIds) {
        if (teacherIds.isEmpty()) {
            return false;
        }

        // Create placeholders for IN clause (?, ?, ?, ...)
        String placeholders = String.join(",", Collections.nCopies(teacherIds.size(), "?"));
        String query = "DELETE FROM teachers WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the teacher IDs as parameters
            for (int i = 0; i < teacherIds.size(); i++) {
                stmt.setInt(i + 1, teacherIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted " + rowsAffected + " teachers successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting teachers: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Teacher getTeacherById(int teacherId) {
        String query = "SELECT * FROM teachers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createTeacherFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting teacher by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Teacher> searchTeachers(String searchTerm) {
        List<Teacher> teachers = new ArrayList<>();
        String query = """
        SELECT * FROM teachers 
        WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? 
        ORDER BY first_name, last_name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Teacher teacher = createTeacherFromResultSet(rs);
                teachers.add(teacher);
            }

            System.out.println("Found " + teachers.size() + " teachers matching: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("Error searching teachers: " + e.getMessage());
            e.printStackTrace();
        }

        return teachers;
    }

    // Helper method: Extract teacher creation logic
    private Teacher createTeacherFromResultSet(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();

        teacher.setId(rs.getInt("id"));
        teacher.setFirstName(rs.getString("first_name"));
        teacher.setLastName(rs.getString("last_name"));
        teacher.setEmail(rs.getString("email"));
        teacher.setPhoneNum(rs.getString("phone_num"));
        teacher.setCreatedAt(rs.getString("created_at"));
        teacher.setUpdatedAt(rs.getString("updated_at"));

        return teacher;
    }

    /**
     * Get teachers assigned to a specific workshop
     */
    public List<Teacher> getTeachersForWorkshop(int workshopId) {
        String sql = """
        SELECT t.id, t.first_name, t.last_name, t.email, t.phone_num, 
               t.created_at, t.updated_at
        FROM teachers t
        INNER JOIN workshop_participants wp ON t.id = wp.teacher_id
        WHERE wp.workshop_id = ? AND wp.teacher_id IS NOT NULL
        """;

        List<Teacher> teachers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, workshopId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setId(rs.getInt("id"));
                    teacher.setFirstName(rs.getString("first_name"));
                    teacher.setLastName(rs.getString("last_name"));
                    teacher.setEmail(rs.getString("email"));
                    teacher.setPhoneNum(rs.getString("phone_num"));
                    teacher.setCreatedAt(rs.getString("created_at"));
                    teacher.setUpdatedAt(rs.getString("updated_at"));
                    teachers.add(teacher);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting teachers for workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return teachers;
    }

    /**
     * Check if a teacher is assigned to any workshop on conflicting dates
     */
    public boolean isTeacherAvailable(int teacherId, String fromDate, String toDate) {
        String sql = """
        SELECT COUNT(*) as conflict_count
        FROM workshop_participants wp
        INNER JOIN workshops w ON wp.workshop_id = w.id
        WHERE wp.teacher_id = ? 
        AND ((w.from_date BETWEEN ? AND ?) OR (w.to_date BETWEEN ? AND ?)
             OR (w.from_date <= ? AND w.to_date >= ?))
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            stmt.setString(4, fromDate);
            stmt.setString(5, toDate);
            stmt.setString(6, fromDate);
            stmt.setString(7, toDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("conflict_count") == 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking teacher availability: " + e.getMessage());
            e.printStackTrace();
        }

        return false; // Assume not available if error occurs
    }

}