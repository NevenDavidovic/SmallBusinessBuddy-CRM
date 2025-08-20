package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.Contact;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for Contact entity operations.
 * Handles all database interactions for contact management including CRUD operations,
 * birthday tracking, age-based queries, and list membership management.
 *
 * Features:
 * - Complete contact lifecycle management
 * - Birthday and age-based filtering
 * - List membership queries
 * - Bulk operations support
 * - Automatic date handling and validation
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class ContactDAO {

    /**
     * Retrieves all contacts from the database.
     * Includes comprehensive logging for debugging purposes.
     *
     * @return List of all contacts, ordered by name
     */
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contacts";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== ContactDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            String countQuery = "SELECT COUNT(*) as count FROM contacts";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in contacts table: " + count);
                }
            }

            String tableInfoQuery = "PRAGMA table_info(contacts)";
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
                    Contact contact = createContactFromResultSet(rs);
                    contacts.add(contact);

                    if (rowCount == 1) {
                        System.out.println("First contact loaded: " + contact.getFirstName() + " " + contact.getLastName() + " (" + contact.getEmail() + ")");
                        System.out.println("  Birthday: " + contact.getBirthday());
                        System.out.println("  PIN: " + contact.getPin());
                        System.out.println("  Age: " + contact.getAge());
                        System.out.println("  Member Since: " + contact.getMemberSince());
                        System.out.println("  Member Until: " + contact.getMemberUntil());
                    }
                }

                System.out.println("Query returned " + rowCount + " rows");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllContacts: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + contacts.size() + " contacts from DAO");
        System.out.println("===============================");

        return contacts;
    }

    /**
     * Retrieves all contacts that belong to a specific list.
     * Uses JOIN query for optimal performance with proper indexing.
     *
     * @param listId The ID of the list to query
     * @return List of contacts belonging to the specified list, ordered by name
     */
    public List<Contact> getContactsInList(int listId) {
        List<Contact> contacts = new ArrayList<>();
        String query = """
            SELECT c.* FROM contacts c 
            INNER JOIN list_contacts lc ON c.id = lc.contact_id 
            WHERE lc.list_id = ? 
            ORDER BY c.first_name, c.last_name
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Contact contact = createContactFromResultSet(rs);
                contacts.add(contact);
            }

            System.out.println("Loaded " + contacts.size() + " contacts for list ID: " + listId);
        } catch (SQLException e) {
            System.err.println("Error loading contacts for list: " + e.getMessage());
            e.printStackTrace();
        }

        return contacts;
    }

    /**
     * Creates a new contact in the database.
     * Handles automatic ID generation and proper date formatting.
     *
     * @param contact The contact object to create
     * @return true if contact was created successfully, false otherwise
     */
    public boolean createContact(Contact contact) {
        String query = """
        INSERT INTO contacts (
            first_name, last_name, birthday, pin, street_name, street_num, postal_code, 
            city, email, phone_num, is_member, member_since, member_until, 
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, contact.getFirstName());
            stmt.setString(2, contact.getLastName());
            stmt.setString(3, contact.getBirthday() != null ? contact.getBirthday().toString() : null);
            stmt.setString(4, contact.getPin());
            stmt.setString(5, contact.getStreetName());
            stmt.setString(6, contact.getStreetNum());
            stmt.setString(7, contact.getPostalCode());
            stmt.setString(8, contact.getCity());
            stmt.setString(9, contact.getEmail());
            stmt.setString(10, contact.getPhoneNum());
            stmt.setInt(11, contact.isMember() ? 1 : 0);
            stmt.setString(12, contact.getMemberSince() != null ? contact.getMemberSince().toString() : null);
            stmt.setString(13, contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : null);
            stmt.setString(14, contact.getCreatedAt());
            stmt.setString(15, contact.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        contact.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Contact created successfully with ID: " + contact.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a single contact from the database.
     *
     * @param contactId The ID of the contact to delete
     * @return true if contact was deleted successfully, false otherwise
     */
    public boolean deleteContact(int contactId) {
        String query = "DELETE FROM contacts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, contactId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Contact deleted successfully with ID: " + contactId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes multiple contacts in a single database operation.
     * Uses optimized IN clause for better performance than individual deletes.
     *
     * @param contactIds List of contact IDs to delete
     * @return true if all contacts were deleted successfully, false otherwise
     */
    public boolean deleteContacts(List<Integer> contactIds) {
        if (contactIds.isEmpty()) {
            return false;
        }

        String placeholders = String.join(",", Collections.nCopies(contactIds.size(), "?"));
        String query = "DELETE FROM contacts WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < contactIds.size(); i++) {
                stmt.setInt(i + 1, contactIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted " + rowsAffected + " contacts successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing contact in the database.
     * Automatically sets the updated_at timestamp.
     *
     * @param contact The contact object with updated information
     * @return true if contact was updated successfully, false otherwise
     */
    public boolean updateContact(Contact contact) {
        String query = """
        UPDATE contacts SET 
            first_name = ?, last_name = ?, birthday = ?, pin = ?, street_name = ?, street_num = ?, 
            postal_code = ?, city = ?, email = ?, phone_num = ?, 
            is_member = ?, member_since = ?, member_until = ?, updated_at = ?
        WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, contact.getFirstName());
            stmt.setString(2, contact.getLastName());
            stmt.setString(3, contact.getBirthday() != null ? contact.getBirthday().toString() : null);
            stmt.setString(4, contact.getPin());
            stmt.setString(5, contact.getStreetName());
            stmt.setString(6, contact.getStreetNum());
            stmt.setString(7, contact.getPostalCode());
            stmt.setString(8, contact.getCity());
            stmt.setString(9, contact.getEmail());
            stmt.setString(10, contact.getPhoneNum());
            stmt.setInt(11, contact.isMember() ? 1 : 0);
            stmt.setString(12, contact.getMemberSince() != null ? contact.getMemberSince().toString() : null);
            stmt.setString(13, contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : null);
            stmt.setString(14, java.time.LocalDateTime.now().toString());
            stmt.setInt(15, contact.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Contact updated successfully with ID: " + contact.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves a specific contact by ID.
     *
     * @param contactId The ID of the contact to retrieve
     * @return Contact object if found, null otherwise
     */
    public Contact getContactById(int contactId) {
        String query = "SELECT * FROM contacts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, contactId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Contact contact = createContactFromResultSet(rs);
                System.out.println("Loaded contact with ID: " + contactId);
                return contact;
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact by ID: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("No contact found with ID: " + contactId);
        return null;
    }

    /**
     * Retrieves contacts with birthdays occurring within the specified number of days.
     * Handles year rollover for birthdays that have already passed this year.
     * Optimized for notification system usage.
     *
     * @param daysAhead Number of days in the future to check for birthdays
     * @return List of contacts with upcoming birthdays, ordered by name
     */
    public List<Contact> getContactsWithUpcomingBirthdays(int daysAhead) {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contacts WHERE birthday IS NOT NULL ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(daysAhead);

            while (rs.next()) {
                Contact contact = createContactFromResultSet(rs);

                if (contact.getBirthday() != null) {
                    LocalDate thisYearBirthday = contact.getBirthday().withYear(today.getYear());

                    if (thisYearBirthday.isBefore(today)) {
                        thisYearBirthday = thisYearBirthday.plusYears(1);
                    }

                    if (!thisYearBirthday.isBefore(today) && !thisYearBirthday.isAfter(futureDate)) {
                        contacts.add(contact);
                    }
                }
            }

            System.out.println("Found " + contacts.size() + " contacts with birthdays in the next " + daysAhead + " days");
        } catch (SQLException e) {
            System.err.println("Error getting contacts with upcoming birthdays: " + e.getMessage());
            e.printStackTrace();
        }

        return contacts;
    }

    /**
     * Retrieves contacts within a specific age range.
     * Calculates age based on current date and birthday.
     *
     * @param minAge Minimum age (inclusive)
     * @param maxAge Maximum age (inclusive)
     * @return List of contacts within the specified age range, ordered by name
     */
    public List<Contact> getContactsByAgeRange(int minAge, int maxAge) {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contacts WHERE birthday IS NOT NULL ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Contact contact = createContactFromResultSet(rs);

                if (contact.getBirthday() != null) {
                    int age = contact.getAge();
                    if (age >= minAge && age <= maxAge) {
                        contacts.add(contact);
                    }
                }
            }

            System.out.println("Found " + contacts.size() + " contacts in age range " + minAge + "-" + maxAge);
        } catch (SQLException e) {
            System.err.println("Error getting contacts by age range: " + e.getMessage());
            e.printStackTrace();
        }

        return contacts;
    }

    /**
     * Creates a Contact object from a database ResultSet.
     * Handles proper type conversion and date parsing with error handling.
     *
     * @param rs ResultSet containing contact data
     * @return Populated Contact object
     * @throws SQLException if database access error occurs
     */
    private Contact createContactFromResultSet(ResultSet rs) throws SQLException {
        Contact contact = new Contact();

        contact.setId(rs.getInt("id"));
        contact.setFirstName(rs.getString("first_name"));
        contact.setLastName(rs.getString("last_name"));

        String birthdayStr = rs.getString("birthday");
        if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
            try {
                contact.setBirthday(LocalDate.parse(birthdayStr));
            } catch (Exception e) {
                System.err.println("Error parsing birthday: " + birthdayStr);
            }
        }

        contact.setPin(rs.getString("pin"));
        contact.setStreetName(rs.getString("street_name"));
        contact.setStreetNum(rs.getString("street_num"));
        contact.setPostalCode(rs.getString("postal_code"));
        contact.setCity(rs.getString("city"));
        contact.setEmail(rs.getString("email"));
        contact.setPhoneNum(rs.getString("phone_num"));
        contact.setMember(rs.getInt("is_member") == 1);

        String memberSinceStr = rs.getString("member_since");
        if (memberSinceStr != null && !memberSinceStr.trim().isEmpty()) {
            try {
                contact.setMemberSince(LocalDate.parse(memberSinceStr));
            } catch (Exception e) {
                System.err.println("Error parsing member_since date: " + memberSinceStr);
            }
        }

        String memberUntilStr = rs.getString("member_until");
        if (memberUntilStr != null && !memberUntilStr.trim().isEmpty()) {
            try {
                contact.setMemberUntil(LocalDate.parse(memberUntilStr));
            } catch (Exception e) {
                System.err.println("Error parsing member_until date: " + memberUntilStr);
            }
        }

        contact.setCreatedAt(rs.getString("created_at"));
        contact.setUpdatedAt(rs.getString("updated_at"));

        return contact;
    }
}