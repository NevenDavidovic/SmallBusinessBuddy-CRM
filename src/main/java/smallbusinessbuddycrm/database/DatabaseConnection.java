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

            stmt.execute(createOrganizationTableSQL);
            stmt.execute(createContactsTableSQL);
            stmt.execute(createListsTableSQL);
            stmt.execute(createListContactsTableSQL);
            System.out.println("Baza i tablice su inicijalizirane.");

        } catch (SQLException e) {
            System.err.println("Greška pri inicijalizaciji baze: " + e.getMessage());
            e.printStackTrace();
        }
    }
}