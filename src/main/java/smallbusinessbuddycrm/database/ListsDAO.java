package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.List;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ListsDAO {

    public ArrayList<List> getAllActiveLists() {
        ArrayList<List> lists = new ArrayList<>();
        String query = "SELECT * FROM lists WHERE is_deleted = 0 ORDER BY updated_at DESC";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== ListsDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());

            // Debug: Check counts
            String countQuery = "SELECT COUNT(*) as total FROM lists";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {
                if (countRs.next()) {
                    System.out.println("📊 Total records in lists table: " + countRs.getInt("total"));
                }
            }

            String activeQuery = "SELECT COUNT(*) as active FROM lists WHERE is_deleted = 0";
            try (Statement activeStmt = conn.createStatement();
                 ResultSet activeRs = activeStmt.executeQuery(activeQuery)) {
                if (activeRs.next()) {
                    System.out.println("✅ Active records (is_deleted = 0): " + activeRs.getInt("active"));
                }
            }

            // ✅ FIX: First, collect all list data WITHOUT calling getContactCountForList
            ArrayList<List> tempLists = new ArrayList<>();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("🔍 Executing main query: " + query);
                int rowCount = 0;

                while (rs.next()) {
                    rowCount++;
                    System.out.println("⚙️ Processing row " + rowCount + ":");

                    try {
                        List list = createListFromResultSetWithoutCount(rs);
                        tempLists.add(list);
                        System.out.println("  ✅ Successfully created list: " + list.getName() + " (ID: " + list.getId() + ")");

                    } catch (Exception e) {
                        System.err.println("  ❌ ERROR creating list from row " + rowCount + ":");
                        System.err.println("      Exception: " + e.getClass().getSimpleName());
                        System.err.println("      Message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                System.out.println("📊 Main query processed " + rowCount + " rows");
            }

            // ✅ FIX: Now get contact counts for each list using separate connections
            for (List list : tempLists) {
                try {
                    int contactCount = getContactCountForList(list.getId());
                    list.setListSize(contactCount);
                    lists.add(list);
                    System.out.println("📊 Set contact count for '" + list.getName() + "': " + contactCount);
                } catch (Exception e) {
                    System.err.println("❌ Error getting contact count for list " + list.getId() + ": " + e.getMessage());
                    // Still add the list even if contact count fails
                    list.setListSize(0);
                    lists.add(list);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getAllActiveLists: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🎯 FINAL RESULT: Returning " + lists.size() + " lists from DAO");
        System.out.println("==========================================");
        return lists;
    }

    public ArrayList<List> searchListsByName(String searchTerm) {
        ArrayList<List> lists = new ArrayList<>();
        String query = "SELECT * FROM lists WHERE name LIKE ? AND is_deleted = 0 ORDER BY updated_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            // Collect lists first, then get contact counts
            ArrayList<List> tempLists = new ArrayList<>();
            while (rs.next()) {
                List list = createListFromResultSetWithoutCount(rs);
                tempLists.add(list);
            }

            // Get contact counts separately
            for (List list : tempLists) {
                int contactCount = getContactCountForList(list.getId());
                list.setListSize(contactCount);
                lists.add(list);
            }

            System.out.println("Search found " + lists.size() + " lists for: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("Error searching lists: " + e.getMessage());
            e.printStackTrace();
        }

        return lists;
    }

    public boolean createList(List list) {
        String query = """
            INSERT INTO lists (name, description, type, object_type, creator, folder, created_at, updated_at, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String currentTime = LocalDateTime.now().toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, list.getName());
            stmt.setString(2, list.getDescription());
            stmt.setString(3, list.getType() != null ? list.getType() : "CUSTOM");
            stmt.setString(4, list.getObjectType() != null ? list.getObjectType() : "CONTACT");
            stmt.setString(5, list.getCreator());
            stmt.setString(6, list.getFolder() != null ? list.getFolder() : "");
            stmt.setString(7, currentTime);
            stmt.setString(8, currentTime);
            stmt.setInt(9, 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        list.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("List created successfully with ID: " + list.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating list: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateList(List list) {
        String query = "UPDATE lists SET name = ?, description = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, list.getName());
            stmt.setString(2, list.getDescription());
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setInt(4, list.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("List updated successfully: " + list.getName());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating list: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteList(int listId) {
        String query = "UPDATE lists SET is_deleted = 1, deleted_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, LocalDateTime.now().toString());
            stmt.setInt(2, listId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("List marked as deleted: " + listId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting list: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean addContactToList(int listId, int contactId) {
        String checkQuery = "SELECT COUNT(*) FROM list_contacts WHERE list_id = ? AND contact_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, listId);
            checkStmt.setInt(2, contactId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Contact " + contactId + " is already in list " + listId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if contact exists in list: " + e.getMessage());
            return false;
        }

        String insertQuery = "INSERT INTO list_contacts (list_id, contact_id, added_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, contactId);
            stmt.setString(3, LocalDateTime.now().toString());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Contact " + contactId + " added to list " + listId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error adding contact to list: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean removeContactFromList(int listId, int contactId) {
        String query = "DELETE FROM list_contacts WHERE list_id = ? AND contact_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, contactId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Contact " + contactId + " removed from list " + listId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error removing contact from list: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public int getContactCountForList(int listId) {
        String query = "SELECT COUNT(*) FROM list_contacts WHERE list_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("Error getting contact count for list: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // ✅ NEW: Create list without getting contact count (to avoid statement closure)
    private List createListFromResultSetWithoutCount(ResultSet rs) throws SQLException {
        List list = new List();
        list.setId(rs.getInt("id"));
        list.setName(rs.getString("name"));
        list.setDescription(rs.getString("description"));
        list.setType(rs.getString("type"));
        list.setObjectType(rs.getString("object_type"));
        list.setCreator(rs.getString("creator"));
        list.setFolder(rs.getString("folder"));

        String createdAt = rs.getString("created_at");
        String updatedAt = rs.getString("updated_at");

        list.setCreatedAt(createdAt);
        list.setUpdatedAt(updatedAt);
        // Note: listSize will be set separately after this method returns

        return list;
    }
}