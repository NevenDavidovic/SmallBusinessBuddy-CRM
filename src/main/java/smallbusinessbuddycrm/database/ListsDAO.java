package smallbusinessbuddycrm.dao;

import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.model.List;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ListsDAO {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Create a new list
    public boolean createList(List list) {
        String sql = """
            INSERT INTO lists (name, description, type, object_type, creator, folder, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, list.getName());
            pstmt.setString(2, list.getDescription());
            pstmt.setString(3, list.getType());
            pstmt.setString(4, list.getObjectType());
            pstmt.setString(5, list.getCreator());
            pstmt.setString(6, list.getFolder());
            pstmt.setString(7, list.getCreatedAt().format(DATETIME_FORMATTER));
            pstmt.setString(8, list.getUpdatedAt().format(DATETIME_FORMATTER));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    list.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all active lists (not deleted)
    public ArrayList<List> getAllActiveLists() {
        ArrayList<List> lists = new ArrayList<>();
        String sql = """
            SELECT l.*, COUNT(lc.contact_id) as list_size
            FROM lists l
            LEFT JOIN list_contacts lc ON l.id = lc.list_id
            WHERE l.is_deleted = 0
            GROUP BY l.id
            ORDER BY l.updated_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                List list = mapResultSetToList(rs);
                list.setListSize(rs.getInt("list_size"));
                lists.add(list);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lists: " + e.getMessage());
            e.printStackTrace();
        }
        return lists;
    }

    // Get unused lists (lists with no contacts)
    public ArrayList<List> getUnusedLists() {
        ArrayList<List> lists = new ArrayList<>();
        String sql = """
            SELECT l.*, COUNT(lc.contact_id) as list_size
            FROM lists l
            LEFT JOIN list_contacts lc ON l.id = lc.list_id
            WHERE l.is_deleted = 0
            GROUP BY l.id
            HAVING COUNT(lc.contact_id) = 0
            ORDER BY l.updated_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                List list = mapResultSetToList(rs);
                list.setListSize(rs.getInt("list_size"));
                lists.add(list);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving unused lists: " + e.getMessage());
            e.printStackTrace();
        }
        return lists;
    }

    // Get recently deleted lists
    public ArrayList<List> getRecentlyDeletedLists() {
        ArrayList<List> lists = new ArrayList<>();
        String sql = """
            SELECT l.*, COUNT(lc.contact_id) as list_size
            FROM lists l
            LEFT JOIN list_contacts lc ON l.id = lc.list_id
            WHERE l.is_deleted = 1
            GROUP BY l.id
            ORDER BY l.deleted_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                List list = mapResultSetToList(rs);
                list.setListSize(rs.getInt("list_size"));
                lists.add(list);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving deleted lists: " + e.getMessage());
            e.printStackTrace();
        }
        return lists;
    }

    // Get list by ID
    public List getListById(int id) {
        String sql = """
            SELECT l.*, COUNT(lc.contact_id) as list_size
            FROM lists l
            LEFT JOIN list_contacts lc ON l.id = lc.list_id
            WHERE l.id = ?
            GROUP BY l.id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                List list = mapResultSetToList(rs);
                list.setListSize(rs.getInt("list_size"));
                return list;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving list by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Update list
    public boolean updateList(List list) {
        String sql = """
            UPDATE lists SET name = ?, description = ?, type = ?, object_type = ?, 
                           creator = ?, folder = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, list.getName());
            pstmt.setString(2, list.getDescription());
            pstmt.setString(3, list.getType());
            pstmt.setString(4, list.getObjectType());
            pstmt.setString(5, list.getCreator());
            pstmt.setString(6, list.getFolder());
            pstmt.setString(7, LocalDateTime.now().format(DATETIME_FORMATTER));
            pstmt.setInt(8, list.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Soft delete list
    public boolean deleteList(int listId) {
        String sql = """
            UPDATE lists SET is_deleted = 1, deleted_at = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String now = LocalDateTime.now().format(DATETIME_FORMATTER);
            pstmt.setString(1, now);
            pstmt.setString(2, now);
            pstmt.setInt(3, listId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Restore deleted list
    public boolean restoreList(int listId) {
        String sql = """
            UPDATE lists SET is_deleted = 0, deleted_at = NULL, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDateTime.now().format(DATETIME_FORMATTER));
            pstmt.setInt(2, listId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error restoring list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Permanently delete list
    public boolean permanentlyDeleteList(int listId) {
        String deleteContactsSQL = "DELETE FROM list_contacts WHERE list_id = ?";
        String deleteListSQL = "DELETE FROM lists WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Delete contacts from list first
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteContactsSQL)) {
                pstmt1.setInt(1, listId);
                pstmt1.executeUpdate();
            }

            // Then delete the list
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteListSQL)) {
                pstmt2.setInt(1, listId);
                int rowsAffected = pstmt2.executeUpdate();

                conn.commit();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error permanently deleting list: " + e.getMessage());
            e.printStackTrace();
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }

    // Add contact to list
    public boolean addContactToList(int listId, int contactId) {
        String sql = """
            INSERT INTO list_contacts (list_id, contact_id, added_at)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, contactId);
            pstmt.setString(3, LocalDateTime.now().format(DATETIME_FORMATTER));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding contact to list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Remove contact from list
    public boolean removeContactFromList(int listId, int contactId) {
        String sql = "DELETE FROM list_contacts WHERE list_id = ? AND contact_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, contactId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing contact from list: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Search lists by name
    public ArrayList<List> searchListsByName(String searchTerm) {
        ArrayList<List> lists = new ArrayList<>();
        String sql = """
            SELECT l.*, COUNT(lc.contact_id) as list_size
            FROM lists l
            LEFT JOIN list_contacts lc ON l.id = lc.list_id
            WHERE l.is_deleted = 0 AND l.name LIKE ?
            GROUP BY l.id
            ORDER BY l.updated_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                List list = mapResultSetToList(rs);
                list.setListSize(rs.getInt("list_size"));
                lists.add(list);
            }
        } catch (SQLException e) {
            System.err.println("Error searching lists: " + e.getMessage());
            e.printStackTrace();
        }
        return lists;
    }

    // Helper method to map ResultSet to List object
    private List mapResultSetToList(ResultSet rs) throws SQLException {
        List list = new List();
        list.setId(rs.getInt("id"));
        list.setName(rs.getString("name"));
        list.setDescription(rs.getString("description"));
        list.setType(rs.getString("type"));
        list.setObjectType(rs.getString("object_type"));
        list.setCreator(rs.getString("creator"));
        list.setFolder(rs.getString("folder"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            list.setCreatedAt(LocalDateTime.parse(createdAtStr, DATETIME_FORMATTER));
        }

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            list.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DATETIME_FORMATTER));
        }

        list.setDeleted(rs.getBoolean("is_deleted"));

        String deletedAtStr = rs.getString("deleted_at");
        if (deletedAtStr != null) {
            list.setDeletedAt(LocalDateTime.parse(deletedAtStr, DATETIME_FORMATTER));
        }

        return list;
    }
}