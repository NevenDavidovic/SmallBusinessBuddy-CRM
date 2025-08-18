package smallbusinessbuddycrm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    // Migration method to fix existing database - updated to remove teacher_id from workshop_participants
    public static void fixWorkshopParticipantsForTeachers() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking if database migration is needed...");


            boolean needsMigration = false;
            try {
                String testSQL = "SELECT teacher_id FROM workshop_participants LIMIT 1";
                stmt.executeQuery(testSQL);
                needsMigration = true; // If this succeeds, teacher_id exists and needs to be removed
                System.out.println("Migration needed - removing teacher_id from workshop_participants...");
            } catch (SQLException e) {
                // If this fails, teacher_id column doesn't exist, no migration needed
                System.out.println("Database already migrated - no migration needed.");
                return;
            }

            if (!needsMigration) {
                return;
            }

            // Step 1: Create a backup table with current data
            String createBackupSQL = """
                CREATE TABLE IF NOT EXISTS workshop_participants_backup AS 
                SELECT * FROM workshop_participants;
                """;

            // Step 2: Drop the original table
            String dropOriginalSQL = "DROP TABLE IF EXISTS workshop_participants;";

            // Step 3: Create new table without teacher_id (teachers are now linked to workshops directly)
            String createNewTableSQL = """
                CREATE TABLE workshop_participants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workshop_id INTEGER NOT NULL,
                    underaged_id INTEGER,
                    contact_id INTEGER,
                    participant_type TEXT NOT NULL CHECK (participant_type IN ('ADULT', 'CHILD')),
                    payment_status TEXT NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')),
                    notes TEXT,
                    created_at TEXT,
                    updated_at TEXT,
                    FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                    FOREIGN KEY (underaged_id) REFERENCES underaged(id) ON DELETE CASCADE,
                    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                    CHECK (
                        (underaged_id IS NOT NULL AND contact_id IS NULL AND participant_type = 'CHILD') OR
                        (contact_id IS NOT NULL AND underaged_id IS NULL AND participant_type = 'ADULT')
                    )
                );
                """;

            // Step 4: Restore data from backup (excluding teacher_id and TEACHER participant_type)
            String restoreDataSQL = """
                INSERT INTO workshop_participants 
                (id, workshop_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at)
                SELECT id, workshop_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at
                FROM workshop_participants_backup
                WHERE participant_type IN ('ADULT', 'CHILD');
                """;

            // Step 5: Drop backup table
            String dropBackupSQL = "DROP TABLE IF EXISTS workshop_participants_backup;";

            // Execute migration
            try {
                stmt.execute(createBackupSQL);
                System.out.println("✓ Created backup table");
            } catch (Exception e) {
                System.out.println("Note: Backup creation failed (table might be empty): " + e.getMessage());
            }

            stmt.execute(dropOriginalSQL);

            stmt.execute(createNewTableSQL);

            try {
                stmt.execute(restoreDataSQL);

            } catch (Exception e) {
                System.out.println("Note: Data restore failed (table might have been empty): " + e.getMessage());
            }

            try {
                stmt.execute(dropBackupSQL);
                System.out.println("✓ Cleaned up backup table");
            } catch (Exception e) {
                System.out.println("Note: Backup cleanup failed: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Migration method to add teacher_id to workshops table
    public static void addTeacherIdToWorkshops() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking if workshops table needs teacher_id column...");

            // Check if teacher_id column already exists
            try {
                String testSQL = "SELECT teacher_id FROM workshops LIMIT 1";
                stmt.executeQuery(testSQL);
                System.out.println("teacher_id column already exists in workshops table.");
                return;
            } catch (SQLException e) {
                // Column doesn't exist, need to add it
                System.out.println("Adding teacher_id column to workshops table...");
            }

            // Add teacher_id column to workshops table
            String addColumnSQL = """
                ALTER TABLE workshops 
                ADD COLUMN teacher_id INTEGER REFERENCES teachers(id) ON DELETE SET NULL;
                """;

            stmt.execute(addColumnSQL);
            System.out.println("✓ Added teacher_id column to workshops table");

        } catch (SQLException e) {
            System.err.println("❌ Error adding teacher_id to workshops: " + e.getMessage());
            e.printStackTrace();
        }
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
                teacher_id INTEGER,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL
            );
            """;


        String createWorkshopParticipantsTableSQL = """
            CREATE TABLE IF NOT EXISTS workshop_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workshop_id INTEGER NOT NULL,
                underaged_id INTEGER,
                contact_id INTEGER,
                participant_type TEXT NOT NULL CHECK (participant_type IN ('ADULT', 'CHILD')),
                payment_status TEXT NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')),
                notes TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
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

        // NEW PAYMENT SYSTEM TABLES
        String createPaymentTemplateTableSQL = """
            CREATE TABLE IF NOT EXISTS payment_template (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                amount DECIMAL(10,2) NOT NULL,
                model_of_payment TEXT NOT NULL,
                poziv_na_broj TEXT,
                is_active INTEGER DEFAULT 1,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createNewsletterTemplateTableSQL = """
            CREATE TABLE IF NOT EXISTS newsletter_template (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                subject TEXT NOT NULL,
                content TEXT NOT NULL,
                template_type TEXT DEFAULT 'PAYMENT',
                is_active INTEGER DEFAULT 1,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        // NEW: Simple Payment Attachment Template Table
        String createPaymentAttachmentTableSQL = """
            CREATE TABLE IF NOT EXISTS payment_attachment (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                html_content TEXT NOT NULL,
                is_default INTEGER DEFAULT 0,
                created_at TEXT,
                updated_at TEXT
            );
            """;

        String createPaymentInfoTableSQL = """
            CREATE TABLE IF NOT EXISTS payment_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organization_id INTEGER NOT NULL,
                payment_template_id INTEGER NOT NULL,
                newsletter_template_id INTEGER,
                
                -- Target references (only one should be filled)
                contact_id INTEGER,
                list_id INTEGER,
                workshop_id INTEGER,
                
                -- Payment details
                target_type TEXT NOT NULL CHECK (target_type IN ('CONTACT', 'LIST', 'WORKSHOP')),
                amount DECIMAL(10,2) NOT NULL,
                model_of_payment TEXT NOT NULL,
                poziv_na_broj TEXT,
                
                -- Generation info
                barcode_data TEXT,
                pdf_path TEXT,
                
                -- Status tracking
                status TEXT NOT NULL DEFAULT 'GENERATED' CHECK (status IN ('GENERATED', 'SENT', 'PAID', 'CANCELLED')),
                generated_at TEXT,
                sent_at TEXT,
                paid_at TEXT,
                
                -- Metadata
                created_at TEXT,
                updated_at TEXT,
                
                -- Foreign keys
                FOREIGN KEY (organization_id) REFERENCES organization(id) ON DELETE CASCADE,
                FOREIGN KEY (payment_template_id) REFERENCES payment_template(id) ON DELETE CASCADE,
                FOREIGN KEY (newsletter_template_id) REFERENCES newsletter_template(id) ON DELETE SET NULL,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
                FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                
                -- Ensure only one target is specified
                CHECK (
                    (contact_id IS NOT NULL AND list_id IS NULL AND workshop_id IS NULL AND target_type = 'CONTACT') OR
                    (list_id IS NOT NULL AND contact_id IS NULL AND workshop_id IS NULL AND target_type = 'LIST') OR
                    (workshop_id IS NOT NULL AND contact_id IS NULL AND list_id IS NULL AND target_type = 'WORKSHOP')
                )
            );
            """;

        // Table to track individual payment slips for bulk operations
        String createPaymentSlipTableSQL = """
            CREATE TABLE IF NOT EXISTS payment_slip (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                payment_info_id INTEGER NOT NULL,
                contact_id INTEGER NOT NULL,
                
                -- Individual slip details
                amount DECIMAL(10,2) NOT NULL,
                poziv_na_broj TEXT,
                barcode_data TEXT,
                
                -- Status tracking
                status TEXT NOT NULL DEFAULT 'GENERATED' CHECK (status IN ('GENERATED', 'SENT', 'PAID', 'CANCELLED')),
                sent_at TEXT,
                paid_at TEXT,
                
                -- Metadata
                created_at TEXT,
                updated_at TEXT,
                
                -- Foreign keys
                FOREIGN KEY (payment_info_id) REFERENCES payment_info(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create all existing tables
            stmt.execute(createOrganizationTableSQL);
            stmt.execute(createContactsTableSQL);
            stmt.execute(createUnderagedTableSQL);
            stmt.execute(createTeachersTableSQL);
            stmt.execute(createWorkshopsTableSQL);
            stmt.execute(createWorkshopParticipantsTableSQL);
            stmt.execute(createListsTableSQL);
            stmt.execute(createListContactsTableSQL);

            // Create payment system tables
            stmt.execute(createPaymentTemplateTableSQL);
            stmt.execute(createNewsletterTemplateTableSQL);
            stmt.execute(createPaymentAttachmentTableSQL); // NEW: Simple template table
            stmt.execute(createPaymentInfoTableSQL);
            stmt.execute(createPaymentSlipTableSQL);

                       // IMPORTANT: Run migrations after table creation
            addTeacherIdToWorkshops(); // Add teacher_id to workshops if not exists
            fixWorkshopParticipantsForTeachers(); // Remove teacher_id from workshop_participants



        } catch (SQLException e) {
            System.err.println("Greška pri inicijalizaciji baze: " + e.getMessage());
            e.printStackTrace();
        }
    }
}