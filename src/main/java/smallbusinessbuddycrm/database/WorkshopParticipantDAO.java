package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.WorkshopParticipant;
import smallbusinessbuddycrm.model.WorkshopParticipant.ParticipantType;
import smallbusinessbuddycrm.model.WorkshopParticipant.PaymentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class WorkshopParticipantDAO {

    public List<WorkshopParticipant> getAllWorkshopParticipants() {
        List<WorkshopParticipant> participants = new ArrayList<>();
        String query = "SELECT * FROM workshop_participants ORDER BY workshop_id, participant_type";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Debug: Print connection info
            System.out.println("=== WorkshopParticipantDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            // Debug: Check if table exists and has data
            String countQuery = "SELECT COUNT(*) as count FROM workshop_participants";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in workshop_participants table: " + count);
                }
            }

            // Main query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("Executing query: " + query);

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    WorkshopParticipant participant = createWorkshopParticipantFromResultSet(rs);
                    participants.add(participant);

                    // Debug: Print first participant details
                    if (rowCount == 1) {
                        System.out.println("First participant loaded: Workshop ID " + participant.getWorkshopId() +
                                ", Type: " + participant.getParticipantType() +
                                ", Payment: " + participant.getPaymentStatus());
                    }
                }

                System.out.println("Query returned " + rowCount + " rows");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllWorkshopParticipants: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + participants.size() + " participants from DAO");
        System.out.println("===============================");

        return participants;
    }

    // UPDATED: Get all participants for a specific workshop with complete information including teacher info
    public List<Map<String, Object>> getWorkshopParticipantsWithDetails(int workshopId) {
        List<Map<String, Object>> participants = new ArrayList<>();
        String query = """
            SELECT 
                wp.id as participant_id,
                wp.workshop_id,
                wp.participant_type,
                wp.payment_status,
                wp.notes,
                wp.created_at,
                wp.updated_at,
                w.name as workshop_name,
                w.from_date as workshop_from_date,
                w.to_date as workshop_to_date,
                w.teacher_id as workshop_teacher_id,
                t.first_name as teacher_first_name,
                t.last_name as teacher_last_name,
                t.email as teacher_email,
                t.phone_num as teacher_phone,
                c.id as contact_id,
                c.first_name as contact_first_name,
                c.last_name as contact_last_name,
                c.email as contact_email,
                c.phone_num as contact_phone,
                c.birthday as contact_birthday,
                u.id as underaged_id,
                u.first_name as underaged_first_name,
                u.last_name as underaged_last_name,
                u.birth_date as underaged_birth_date,
                u.age as underaged_age,
                u.gender as underaged_gender,
                u.contact_id as parent_contact_id,
                parent.first_name as parent_first_name,
                parent.last_name as parent_last_name,
                parent.email as parent_email,
                parent.phone_num as parent_phone
            FROM workshop_participants wp
            JOIN workshops w ON wp.workshop_id = w.id
            LEFT JOIN teachers t ON w.teacher_id = t.id
            LEFT JOIN contacts c ON wp.contact_id = c.id
            LEFT JOIN underaged u ON wp.underaged_id = u.id
            LEFT JOIN contacts parent ON u.contact_id = parent.id
            WHERE wp.workshop_id = ?
            ORDER BY wp.participant_type, c.last_name, c.first_name, u.last_name, u.first_name
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            System.out.println("Fetching participants for workshop ID: " + workshopId);
            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> participantData = new HashMap<>();

                // Participant info
                participantData.put("participant_id", rs.getInt("participant_id"));
                participantData.put("workshop_id", rs.getInt("workshop_id"));
                participantData.put("participant_type", rs.getString("participant_type"));
                participantData.put("payment_status", rs.getString("payment_status"));
                participantData.put("notes", rs.getString("notes"));
                participantData.put("created_at", rs.getString("created_at"));
                participantData.put("updated_at", rs.getString("updated_at"));
                participantData.put("selected", false); // For UI selection

                // Workshop info
                participantData.put("workshop_name", rs.getString("workshop_name"));
                participantData.put("workshop_from_date", rs.getString("workshop_from_date"));
                participantData.put("workshop_to_date", rs.getString("workshop_to_date"));

                // Teacher info (now from workshops table)
                participantData.put("workshop_teacher_id", rs.getObject("workshop_teacher_id"));
                String teacherFirstName = rs.getString("teacher_first_name");
                String teacherLastName = rs.getString("teacher_last_name");
                if (teacherFirstName != null || teacherLastName != null) {
                    participantData.put("teacher_name", (teacherFirstName != null ? teacherFirstName : "") + " " + (teacherLastName != null ? teacherLastName : ""));
                    participantData.put("teacher_email", rs.getString("teacher_email"));
                    participantData.put("teacher_phone", rs.getString("teacher_phone"));
                }

                if ("ADULT".equals(rs.getString("participant_type"))) {
                    // Adult participant (contact)
                    participantData.put("contact_id", rs.getInt("contact_id"));
                    String firstName = rs.getString("contact_first_name");
                    String lastName = rs.getString("contact_last_name");
                    participantData.put("participant_name", (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    participantData.put("participant_email", rs.getString("contact_email"));
                    participantData.put("participant_phone", rs.getString("contact_phone"));
                    participantData.put("participant_birthday", rs.getString("contact_birthday"));
                    participantData.put("participant_age", calculateAge(rs.getString("contact_birthday")));
                    participantData.put("participant_gender", null); // Adults don't have gender in contacts table
                } else if ("CHILD".equals(rs.getString("participant_type"))) {
                    // Child participant (underaged)
                    participantData.put("underaged_id", rs.getInt("underaged_id"));
                    String firstName = rs.getString("underaged_first_name");
                    String lastName = rs.getString("underaged_last_name");
                    participantData.put("participant_name", (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    participantData.put("participant_age", rs.getInt("underaged_age"));
                    participantData.put("participant_gender", rs.getString("underaged_gender"));
                    participantData.put("participant_birthday", rs.getString("underaged_birth_date"));
                    participantData.put("participant_email", null); // Children don't have direct email

                    // Parent/Guardian info
                    participantData.put("parent_contact_id", rs.getInt("parent_contact_id"));
                    String parentFirstName = rs.getString("parent_first_name");
                    String parentLastName = rs.getString("parent_last_name");
                    participantData.put("parent_name", (parentFirstName != null ? parentFirstName : "") + " " + (parentLastName != null ? parentLastName : ""));
                    participantData.put("parent_email", rs.getString("parent_email"));
                    participantData.put("parent_phone", rs.getString("parent_phone"));
                }

                participants.add(participantData);
            }

            System.out.println("Loaded " + participants.size() + " participants with details for workshop ID: " + workshopId);
        } catch (SQLException e) {
            System.err.println("Error loading workshop participants with details: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    // Get all workshops for a specific participant
    public List<Map<String, Object>> getParticipantWorkshops(int participantId, ParticipantType participantType) {
        List<Map<String, Object>> workshops = new ArrayList<>();
        String query = """
        SELECT 
            wp.id as participant_record_id,
            wp.payment_status,
            wp.notes,
            wp.created_at as enrollment_date,
            w.id as workshop_id,
            w.name as workshop_name,
            w.from_date,
            w.to_date,
            w.teacher_id,
            t.first_name as teacher_first_name,
            t.last_name as teacher_last_name
        FROM workshop_participants wp
        JOIN workshops w ON wp.workshop_id = w.id
        LEFT JOIN teachers t ON w.teacher_id = t.id
        WHERE """ + (participantType == ParticipantType.ADULT ? "wp.contact_id = ?" : "wp.underaged_id = ?") + """
        ORDER BY w.from_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, participantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> workshopData = new HashMap<>();

                workshopData.put("participant_record_id", rs.getInt("participant_record_id"));
                workshopData.put("workshop_id", rs.getInt("workshop_id"));
                workshopData.put("workshop_name", rs.getString("workshop_name"));
                workshopData.put("from_date", rs.getString("from_date"));
                workshopData.put("to_date", rs.getString("to_date"));
                workshopData.put("payment_status", rs.getString("payment_status"));
                workshopData.put("notes", rs.getString("notes"));
                workshopData.put("enrollment_date", rs.getString("enrollment_date"));

                // Teacher info
                workshopData.put("teacher_id", rs.getObject("teacher_id"));
                String teacherFirstName = rs.getString("teacher_first_name");
                String teacherLastName = rs.getString("teacher_last_name");
                if (teacherFirstName != null || teacherLastName != null) {
                    workshopData.put("teacher_name", (teacherFirstName != null ? teacherFirstName : "") + " " + (teacherLastName != null ? teacherLastName : ""));
                }

                workshops.add(workshopData);
            }

            System.out.println("Found " + workshops.size() + " workshops for " + participantType + " participant ID: " + participantId);
        } catch (SQLException e) {
            System.err.println("Error getting participant workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    public boolean createWorkshopParticipant(WorkshopParticipant participant) {
        if (!participant.isValid()) {
            System.err.println("Cannot create invalid workshop participant: " + participant);
            return false;
        }

        String query = """
        INSERT INTO workshop_participants (
            workshop_id, underaged_id, contact_id, participant_type, 
            payment_status, notes, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, participant.getWorkshopId());

            // Set either underaged_id or contact_id based on participant type
            if (participant.getUnderagedId() != null) {
                stmt.setInt(2, participant.getUnderagedId());
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setInt(3, participant.getContactId());
            }

            stmt.setString(4, participant.getParticipantType().toString());
            stmt.setString(5, participant.getPaymentStatus().toString());
            stmt.setString(6, participant.getNotes());
            stmt.setString(7, participant.getCreatedAt());
            stmt.setString(8, participant.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        participant.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Workshop participant created successfully with ID: " + participant.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating workshop participant: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateWorkshopParticipant(WorkshopParticipant participant) {
        if (!participant.isValid()) {
            System.err.println("Cannot update invalid workshop participant: " + participant);
            return false;
        }

        String query = """
        UPDATE workshop_participants SET 
            workshop_id = ?, underaged_id = ?, contact_id = ?, participant_type = ?, 
            payment_status = ?, notes = ?, updated_at = ?
        WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, participant.getWorkshopId());

            // Set either underaged_id or contact_id based on participant type
            if (participant.getUnderagedId() != null) {
                stmt.setInt(2, participant.getUnderagedId());
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setInt(3, participant.getContactId());
            }

            stmt.setString(4, participant.getParticipantType().toString());
            stmt.setString(5, participant.getPaymentStatus().toString());
            stmt.setString(6, participant.getNotes());
            stmt.setString(7, java.time.LocalDateTime.now().toString());
            stmt.setInt(8, participant.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Workshop participant updated successfully with ID: " + participant.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating workshop participant: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteWorkshopParticipant(int participantId) {
        String query = "DELETE FROM workshop_participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, participantId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Workshop participant deleted successfully with ID: " + participantId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting workshop participant: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteWorkshopParticipants(List<Integer> participantIds) {
        if (participantIds.isEmpty()) {
            return false;
        }

        String placeholders = String.join(",", Collections.nCopies(participantIds.size(), "?"));
        String query = "DELETE FROM workshop_participants WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < participantIds.size(); i++) {
                stmt.setInt(i + 1, participantIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted " + rowsAffected + " workshop participants successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting workshop participants: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get participants by workshop and type
    public List<WorkshopParticipant> getParticipantsByWorkshop(int workshopId) {
        List<WorkshopParticipant> participants = new ArrayList<>();
        String query = "SELECT * FROM workshop_participants WHERE workshop_id = ? ORDER BY participant_type";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WorkshopParticipant participant = createWorkshopParticipantFromResultSet(rs);
                participants.add(participant);
            }

            System.out.println("Found " + participants.size() + " participants for workshop ID: " + workshopId);
        } catch (SQLException e) {
            System.err.println("Error getting participants by workshop: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    // Get adult participants for a workshop
    public List<WorkshopParticipant> getAdultParticipants(int workshopId) {
        List<WorkshopParticipant> participants = new ArrayList<>();
        String query = "SELECT * FROM workshop_participants WHERE workshop_id = ? AND participant_type = 'ADULT'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WorkshopParticipant participant = createWorkshopParticipantFromResultSet(rs);
                participants.add(participant);
            }

            System.out.println("Found " + participants.size() + " adult participants for workshop ID: " + workshopId);
        } catch (SQLException e) {
            System.err.println("Error getting adult participants: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    // Get child participants for a workshop
    public List<WorkshopParticipant> getChildParticipants(int workshopId) {
        List<WorkshopParticipant> participants = new ArrayList<>();
        String query = "SELECT * FROM workshop_participants WHERE workshop_id = ? AND participant_type = 'CHILD'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WorkshopParticipant participant = createWorkshopParticipantFromResultSet(rs);
                participants.add(participant);
            }

            System.out.println("Found " + participants.size() + " child participants for workshop ID: " + workshopId);
        } catch (SQLException e) {
            System.err.println("Error getting child participants: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    // Get participants by payment status
    public List<WorkshopParticipant> getParticipantsByPaymentStatus(PaymentStatus paymentStatus) {
        List<WorkshopParticipant> participants = new ArrayList<>();
        String query = "SELECT * FROM workshop_participants WHERE payment_status = ? ORDER BY workshop_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, paymentStatus.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WorkshopParticipant participant = createWorkshopParticipantFromResultSet(rs);
                participants.add(participant);
            }

            System.out.println("Found " + participants.size() + " participants with payment status: " + paymentStatus);
        } catch (SQLException e) {
            System.err.println("Error getting participants by payment status: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    // Get workshop statistics
    public Map<String, Integer> getWorkshopStatistics(int workshopId) {
        Map<String, Integer> stats = new HashMap<>();
        String query = """
        SELECT 
            COUNT(*) as total_participants,
            SUM(CASE WHEN participant_type = 'ADULT' THEN 1 ELSE 0 END) as adult_count,
            SUM(CASE WHEN participant_type = 'CHILD' THEN 1 ELSE 0 END) as child_count,
            SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) as paid_count,
            SUM(CASE WHEN payment_status = 'PENDING' THEN 1 ELSE 0 END) as pending_count
        FROM workshop_participants 
        WHERE workshop_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, workshopId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("total_participants", rs.getInt("total_participants"));
                stats.put("adult_count", rs.getInt("adult_count"));
                stats.put("child_count", rs.getInt("child_count"));
                stats.put("paid_count", rs.getInt("paid_count"));
                stats.put("pending_count", rs.getInt("pending_count"));
            }

            System.out.println("Workshop " + workshopId + " statistics: " + stats);
        } catch (SQLException e) {
            System.err.println("Error getting workshop statistics: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    // *** UPDATED METHODS FOR CONTROLLER - ALL RETURN BOOLEAN ***

    /**
     * Wrapper method for controller - calls existing createWorkshopParticipant
     */
    public boolean addParticipant(WorkshopParticipant participant) {
        return createWorkshopParticipant(participant);
    }

    /**
     * Remove a single participant from a workshop
     */
    public boolean removeParticipant(int participantId) {
        String sql = "DELETE FROM workshop_participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, participantId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error removing participant: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove multiple participants from a workshop
     */
    public boolean removeParticipants(List<Integer> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return false;
        }

        // Create SQL with proper number of placeholders
        String placeholders = participantIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = "DELETE FROM workshop_participants WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters
            for (int i = 0; i < participantIds.size(); i++) {
                pstmt.setInt(i + 1, participantIds.get(i));
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected == participantIds.size(); // All participants should be deleted

        } catch (SQLException e) {
            System.err.println("Error removing participants: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update a participant's details (payment status and notes)
     */
    public boolean updateParticipant(int participantId, PaymentStatus paymentStatus, String notes) {
        String sql = "UPDATE workshop_participants SET payment_status = ?, notes = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentStatus.toString());
            pstmt.setString(2, notes);
            pstmt.setString(3, java.time.LocalDateTime.now().toString());
            pstmt.setInt(4, participantId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating participant: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a participant exists in the workshop
     */
    public boolean participantExists(int workshopId, int contactId, int underagedId) {
        String sql = "SELECT COUNT(*) FROM workshop_participants WHERE workshop_id = ? AND " +
                "(contact_id = ? OR underaged_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, workshopId);
            pstmt.setInt(2, contactId);
            pstmt.setInt(3, underagedId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking participant existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a specific participant by ID
     */
    public Map<String, Object> getParticipantById(int participantId) {
        String sql = """
            SELECT 
                wp.id as participant_id,
                wp.workshop_id,
                wp.contact_id,
                wp.underaged_id,
                wp.participant_type,
                wp.payment_status,
                wp.notes,
                wp.created_at,
                wp.updated_at,
                CASE 
                    WHEN wp.participant_type = 'ADULT' THEN 
                        CONCAT(c.first_name, ' ', c.last_name)
                    ELSE 
                        CONCAT(u.first_name, ' ', u.last_name)
                END as participant_name,
                CASE 
                    WHEN wp.participant_type = 'ADULT' THEN c.email
                    ELSE parent_c.email
                END as participant_email,
                CASE 
                    WHEN wp.participant_type = 'ADULT' THEN c.phone_num
                    ELSE parent_c.phone_num
                END as participant_phone,
                CASE 
                    WHEN wp.participant_type = 'CHILD' THEN u.age
                    ELSE NULL
                END as participant_age,
                CASE 
                    WHEN wp.participant_type = 'CHILD' THEN 
                        CONCAT(parent_c.first_name, ' ', parent_c.last_name)
                    ELSE NULL
                END as parent_name,
                CASE 
                    WHEN wp.participant_type = 'CHILD' THEN parent_c.phone_num
                    ELSE NULL
                END as parent_phone
            FROM workshop_participants wp
            LEFT JOIN contacts c ON wp.contact_id = c.id
            LEFT JOIN underaged u ON wp.underaged_id = u.id
            LEFT JOIN contacts parent_c ON u.contact_id = parent_c.id
            WHERE wp.id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, participantId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> participant = new HashMap<>();
                participant.put("participant_id", rs.getInt("participant_id"));
                participant.put("workshop_id", rs.getInt("workshop_id"));
                participant.put("contact_id", rs.getObject("contact_id"));
                participant.put("underaged_id", rs.getObject("underaged_id"));
                participant.put("participant_type", rs.getString("participant_type"));
                participant.put("payment_status", rs.getString("payment_status"));
                participant.put("notes", rs.getString("notes"));
                participant.put("created_at", rs.getString("created_at"));
                participant.put("updated_at", rs.getString("updated_at"));
                participant.put("participant_name", rs.getString("participant_name"));
                participant.put("participant_email", rs.getString("participant_email"));
                participant.put("participant_phone", rs.getString("participant_phone"));
                participant.put("participant_age", rs.getObject("participant_age"));
                participant.put("parent_name", rs.getString("parent_name"));
                participant.put("parent_phone", rs.getString("parent_phone"));
                participant.put("selected", false); // Initialize selection state

                return participant;
            }

        } catch (SQLException e) {
            System.err.println("Error getting participant by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Helper method to calculate age from birthday string
    private Integer calculateAge(String birthdayStr) {
        if (birthdayStr == null || birthdayStr.trim().isEmpty()) {
            return null;
        }

        try {
            java.time.LocalDate birthday = java.time.LocalDate.parse(birthdayStr);
            java.time.LocalDate now = java.time.LocalDate.now();
            int age = now.getYear() - birthday.getYear();
            if (now.getDayOfYear() < birthday.getDayOfYear()) {
                age--;
            }
            return age;
        } catch (Exception e) {
            System.err.println("Error calculating age from birthday: " + birthdayStr);
            return null;
        }
    }

    // Helper method: Extract workshop participant creation logic (UPDATED - removed teacher_id)
    private WorkshopParticipant createWorkshopParticipantFromResultSet(ResultSet rs) throws SQLException {
        WorkshopParticipant participant = new WorkshopParticipant();

        participant.setId(rs.getInt("id"));
        participant.setWorkshopId(rs.getInt("workshop_id"));

        // Handle nullable foreign keys
        int underagedId = rs.getInt("underaged_id");
        if (!rs.wasNull()) {
            participant.setUnderagedId(underagedId);
        }

        int contactId = rs.getInt("contact_id");
        if (!rs.wasNull()) {
            participant.setContactId(contactId);
        }

        // Handle enums
        try {
            participant.setParticipantType(ParticipantType.valueOf(rs.getString("participant_type")));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid participant type: " + rs.getString("participant_type"));
            participant.setParticipantType(ParticipantType.ADULT); // Default fallback
        }

        try {
            participant.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment status: " + rs.getString("payment_status"));
            participant.setPaymentStatus(PaymentStatus.PENDING); // Default fallback
        }

        participant.setNotes(rs.getString("notes"));
        participant.setCreatedAt(rs.getString("created_at"));
        participant.setUpdatedAt(rs.getString("updated_at"));

        return participant;
    }
}