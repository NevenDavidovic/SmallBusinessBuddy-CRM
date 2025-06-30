package smallbusinessbuddycrm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/db/smallbusinessbuddy.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    // Metoda za inicijalizaciju baze i tablice
    public static void initializeDatabase() {
        String createOrganizationTableSQL = """
            CREATE TABLE IF NOT EXISTS organization (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                IBAN TEXT NOT NULL,
                street_name TEXT,
                street_num TEXT,
                postal_code TEXT,
                city TEXT,
                email TEXT,
                image BLOB,
                phone_num TEXT,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createContactsTableSQL = """
            CREATE TABLE IF NOT EXISTS contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                birthday TEXT,
                pin TEXT,
                street_name TEXT,
                street_num TEXT,
                postal_code TEXT,
                city TEXT,
                email TEXT,
                phone_num TEXT,
                is_member INTEGER DEFAULT 0,
                member_since TEXT,
                member_until TEXT,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createUnderagedTableSQL = """
            CREATE TABLE IF NOT EXISTS underaged (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                birth_date TEXT,
                age INTEGER,
                pin TEXT,
                gender TEXT,
                is_member INTEGER DEFAULT 0,
                member_since TEXT,
                member_until TEXT,
                note TEXT,
                created_at TEXT,
                updated_at TEXT,
                contact_id INTEGER,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
            );
            """;

        String createTeachersTableSQL = """
            CREATE TABLE IF NOT EXISTS teachers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                email TEXT,
                phone_num TEXT,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createWorkshopsTableSQL = """
            CREATE TABLE IF NOT EXISTS workshops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                from_date TEXT,
                to_date TEXT,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createWorkshopParticipantsTableSQL = """
            CREATE TABLE IF NOT EXISTS workshop_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workshop_id INTEGER NOT NULL,
                teacher_id INTEGER,
                underaged_id INTEGER,
                contact_id INTEGER,
                participant_type TEXT NOT NULL CHECK (participant_type IN ('ADULT', 'CHILD')),
                payment_status TEXT NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')),
                notes TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
                FOREIGN KEY (underaged_id) REFERENCES underaged(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                CHECK (
                    (underaged_id IS NOT NULL AND contact_id IS NULL AND participant_type = 'CHILD') OR
                    (contact_id IS NOT NULL AND underaged_id IS NULL AND participant_type = 'ADULT')
                )
            );
            """;

        String createListsTableSQL = """
            CREATE TABLE IF NOT EXISTS lists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                type TEXT NOT NULL DEFAULT 'CUSTOM',
                object_type TEXT NOT NULL DEFAULT 'CONTACT',
                creator TEXT,
                folder TEXT,
                created_at TEXT,
                updated_at TEXT,
                is_deleted INTEGER DEFAULT 0,
                deleted_at TEXT
            );
            """;

        String createListContactsTableSQL = """
            CREATE TABLE IF NOT EXISTS list_contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                list_id INTEGER NOT NULL,
                contact_id INTEGER NOT NULL,
                added_at TEXT,
                FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                UNIQUE(list_id, contact_id)
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create all tables
            stmt.execute(createOrganizationTableSQL);
            stmt.execute(createContactsTableSQL);
            stmt.execute(createUnderagedTableSQL);
            stmt.execute(createTeachersTableSQL);
            stmt.execute(createWorkshopsTableSQL);
            stmt.execute(createWorkshopParticipantsTableSQL);
            stmt.execute(createListsTableSQL);
            stmt.execute(createListContactsTableSQL);

            System.out.println("Baza i tablice su inicijalizirane.");
            System.out.println("Workshop management tables created successfully.");

        } catch (SQLException e) {
            System.err.println("Greška pri inicijalizaciji baze: " + e.getMessage());
            e.printStackTrace();
        }
    }


}