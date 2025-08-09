package smallbusinessbuddycrm.database;

import smallbusinessbuddycrm.model.Contact;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactDAO {

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contacts";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Debug: Print connection info
            System.out.println("=== ContactDAO Debug Info ===");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            System.out.println("Connection valid: " + conn.isValid(5));

            // Debug: Check if table exists and has data
            String countQuery = "SELECT COUNT(*) as count FROM contacts";
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {

                if (countRs.next()) {
                    int count = countRs.getInt("count");
                    System.out.println("Total records in contacts table: " + count);
                }
            }

            // Debug: Show table structure
            String tableInfoQuery = "PRAGMA table_info(contacts)";
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
                    Contact contact = createContactFromResultSet(rs);
                    contacts.add(contact);

                    // Debug: Print first contact details
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

    // *** NEW METHOD: Get contacts that are in a specific list ***
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

            // Handle birthday - convert LocalDate to String
            stmt.setString(3, contact.getBirthday() != null ? contact.getBirthday().toString() : null);

            // Handle PIN
            stmt.setString(4, contact.getPin());

            stmt.setString(5, contact.getStreetName());
            stmt.setString(6, contact.getStreetNum());
            stmt.setString(7, contact.getPostalCode());
            stmt.setString(8, contact.getCity());
            stmt.setString(9, contact.getEmail());
            stmt.setString(10, contact.getPhoneNum());
            stmt.setInt(11, contact.isMember() ? 1 : 0);

            // Handle member dates - convert LocalDate to String
            stmt.setString(12, contact.getMemberSince() != null ? contact.getMemberSince().toString() : null);
            stmt.setString(13, contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : null);

            stmt.setString(14, contact.getCreatedAt());
            stmt.setString(15, contact.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated ID
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

    public boolean deleteContacts(List<Integer> contactIds) {
        if (contactIds.isEmpty()) {
            return false;
        }

        // Create placeholders for IN clause (?, ?, ?, ...)
        String placeholders = String.join(",", Collections.nCopies(contactIds.size(), "?"));
        String query = "DELETE FROM contacts WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the contact IDs as parameters
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

            // Handle birthday - convert LocalDate to String
            stmt.setString(3, contact.getBirthday() != null ? contact.getBirthday().toString() : null);

            // Handle PIN
            stmt.setString(4, contact.getPin());

            stmt.setString(5, contact.getStreetName());
            stmt.setString(6, contact.getStreetNum());
            stmt.setString(7, contact.getPostalCode());
            stmt.setString(8, contact.getCity());
            stmt.setString(9, contact.getEmail());
            stmt.setString(10, contact.getPhoneNum());
            stmt.setInt(11, contact.isMember() ? 1 : 0);

            // Handle member dates - convert LocalDate to String
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

    // *** NEW HELPER METHOD: Extract contact creation logic ***
    private Contact createContactFromResultSet(ResultSet rs) throws SQLException {
        Contact contact = new Contact();

        contact.setId(rs.getInt("id"));
        contact.setFirstName(rs.getString("first_name"));
        contact.setLastName(rs.getString("last_name"));

        // Handle birthday
        String birthdayStr = rs.getString("birthday");
        if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
            try {
                contact.setBirthday(LocalDate.parse(birthdayStr));
            } catch (Exception e) {
                System.err.println("Error parsing birthday: " + birthdayStr);
            }
        }

        // Handle PIN
        contact.setPin(rs.getString("pin"));

        contact.setStreetName(rs.getString("street_name"));
        contact.setStreetNum(rs.getString("street_num"));
        contact.setPostalCode(rs.getString("postal_code"));
        contact.setCity(rs.getString("city"));
        contact.setEmail(rs.getString("email"));
        contact.setPhoneNum(rs.getString("phone_num"));
        contact.setMember(rs.getInt("is_member") == 1);

        // Handle member_since and member_until dates
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

    // *** NEW METHOD: Get contacts with upcoming birthdays ***
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
                    // Calculate this year's birthday
                    LocalDate thisYearBirthday = contact.getBirthday().withYear(today.getYear());

                    // If birthday already passed this year, check next year's birthday
                    if (thisYearBirthday.isBefore(today)) {
                        thisYearBirthday = thisYearBirthday.plusYears(1);
                    }

                    // Check if birthday falls within the specified range
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

    // *** NEW METHOD: Get contacts by age range ***
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
}