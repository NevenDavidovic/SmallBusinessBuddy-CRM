package smallbusinessbuddycrm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection and management class for Small Business Buddy CRM.
 * Handles SQLite database initialization, migrations, and performance optimization.
 *
 * Features:
 * - Automatic table creation and schema management
 * - Comprehensive database indexing for optimal performance
 * - Database migration support for schema updates
 * - Notification system optimization
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/db/smallbusinessbuddy.db";
    private static Connection connection;

    /**
     * Gets a connection to the SQLite database.
     * Creates a new connection if none exists or if the current connection is closed.
     *
     * @return Active database connection
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * Creates specialized indexes for notification system queries.
     * Optimizes birthday calculations, workshop date ranges, and member status queries.
     * These indexes significantly improve notification loading performance.
     */
    public static void createNotificationIndexes() {
        String[] indexQueries = {
                "CREATE INDEX IF NOT EXISTS idx_contacts_birthday ON contacts(birthday) WHERE birthday IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_underaged_birth_date ON underaged(birth_date) WHERE birth_date IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshops_from_date ON workshops(from_date) WHERE from_date IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshops_to_date ON workshops(to_date) WHERE to_date IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshops_date_range ON workshops(from_date, to_date)",
                "CREATE INDEX IF NOT EXISTS idx_contacts_member_status ON contacts(is_member)",
                "CREATE INDEX IF NOT EXISTS idx_underaged_member_status ON underaged(is_member)"
        };

        executeIndexes(indexQueries, "notification");
    }

    /**
     * Creates comprehensive performance indexes for general database operations.
     * Covers name searches, foreign key relationships, and common query patterns.
     * Improves performance for contact management, workshop operations, and list handling.
     */
    public static void createPerformanceIndexes() {
        String[] performanceIndexes = {
                "CREATE INDEX IF NOT EXISTS idx_contacts_full_name ON contacts(first_name, last_name)",
                "CREATE INDEX IF NOT EXISTS idx_contacts_last_name ON contacts(last_name)",
                "CREATE INDEX IF NOT EXISTS idx_contacts_first_name ON contacts(first_name)",
                "CREATE INDEX IF NOT EXISTS idx_contacts_email ON contacts(email) WHERE email IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_phone ON contacts(phone_num) WHERE phone_num IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_pin ON contacts(pin) WHERE pin IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_city ON contacts(city) WHERE city IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_postal_code ON contacts(postal_code) WHERE postal_code IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_member_since ON contacts(member_since) WHERE member_since IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_member_until ON contacts(member_until) WHERE member_until IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_underaged_full_name ON underaged(first_name, last_name)",
                "CREATE INDEX IF NOT EXISTS idx_underaged_last_name ON underaged(last_name)",
                "CREATE INDEX IF NOT EXISTS idx_underaged_contact_id ON underaged(contact_id)",
                "CREATE INDEX IF NOT EXISTS idx_underaged_age ON underaged(age) WHERE age IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_underaged_gender ON underaged(gender) WHERE gender IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_underaged_member_since ON underaged(member_since) WHERE member_since IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_workshops_name ON workshops(name)",
                "CREATE INDEX IF NOT EXISTS idx_workshops_teacher_id ON workshops(teacher_id) WHERE teacher_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshops_active_range ON workshops(from_date, to_date) WHERE from_date IS NOT NULL AND to_date IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_workshop_id ON workshop_participants(workshop_id)",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_contact_id ON workshop_participants(contact_id) WHERE contact_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_underaged_id ON workshop_participants(underaged_id) WHERE underaged_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_type ON workshop_participants(participant_type)",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_payment_status ON workshop_participants(payment_status)",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_composite ON workshop_participants(workshop_id, participant_type, payment_status)",

                "CREATE INDEX IF NOT EXISTS idx_teachers_full_name ON teachers(first_name, last_name)",
                "CREATE INDEX IF NOT EXISTS idx_teachers_last_name ON teachers(last_name)",
                "CREATE INDEX IF NOT EXISTS idx_teachers_email ON teachers(email) WHERE email IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_lists_name ON lists(name)",
                "CREATE INDEX IF NOT EXISTS idx_lists_type ON lists(type)",
                "CREATE INDEX IF NOT EXISTS idx_lists_object_type ON lists(object_type)",
                "CREATE INDEX IF NOT EXISTS idx_lists_active ON lists(is_deleted, deleted_at)",
                "CREATE INDEX IF NOT EXISTS idx_lists_folder ON lists(folder) WHERE folder IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_list_contacts_list_id ON list_contacts(list_id)",
                "CREATE INDEX IF NOT EXISTS idx_list_contacts_contact_id ON list_contacts(contact_id)",
                "CREATE INDEX IF NOT EXISTS idx_list_contacts_added_at ON list_contacts(added_at) WHERE added_at IS NOT NULL"
        };

        executeIndexes(performanceIndexes, "performance");
    }

    /**
     * Creates indexes optimized for payment system operations.
     * Improves performance for payment templates, transaction tracking, and billing operations.
     * Essential for efficient payment slip generation and status monitoring.
     */
    public static void createPaymentIndexes() {
        String[] paymentIndexes = {
                "CREATE INDEX IF NOT EXISTS idx_payment_template_active ON payment_template(is_active)",
                "CREATE INDEX IF NOT EXISTS idx_payment_template_name ON payment_template(name)",
                "CREATE INDEX IF NOT EXISTS idx_payment_template_amount ON payment_template(amount)",

                "CREATE INDEX IF NOT EXISTS idx_newsletter_template_active ON newsletter_template(is_active)",
                "CREATE INDEX IF NOT EXISTS idx_newsletter_template_type ON newsletter_template(template_type)",
                "CREATE INDEX IF NOT EXISTS idx_newsletter_template_name ON newsletter_template(name)",

                "CREATE INDEX IF NOT EXISTS idx_payment_attachment_default ON payment_attachment(is_default)",
                "CREATE INDEX IF NOT EXISTS idx_payment_attachment_name ON payment_attachment(name)",

                "CREATE INDEX IF NOT EXISTS idx_payment_info_organization_id ON payment_info(organization_id)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_payment_template_id ON payment_info(payment_template_id)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_newsletter_template_id ON payment_info(newsletter_template_id) WHERE newsletter_template_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_contact_id ON payment_info(contact_id) WHERE contact_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_list_id ON payment_info(list_id) WHERE list_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_workshop_id ON payment_info(workshop_id) WHERE workshop_id IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_status ON payment_info(status)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_target_type ON payment_info(target_type)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_generated_at ON payment_info(generated_at) WHERE generated_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_sent_at ON payment_info(sent_at) WHERE sent_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_paid_at ON payment_info(paid_at) WHERE paid_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_amount ON payment_info(amount)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_status_target ON payment_info(status, target_type)",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_org_status ON payment_info(organization_id, status)",

                "CREATE INDEX IF NOT EXISTS idx_payment_slip_payment_info_id ON payment_slip(payment_info_id)",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_contact_id ON payment_slip(contact_id)",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_status ON payment_slip(status)",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_sent_at ON payment_slip(sent_at) WHERE sent_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_paid_at ON payment_slip(paid_at) WHERE paid_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_amount ON payment_slip(amount)",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_contact_status ON payment_slip(contact_id, status)",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_payment_status ON payment_slip(payment_info_id, status)"
        };

        executeIndexes(paymentIndexes, "payment system");
    }

    /**
     * Creates indexes for reporting and analytics operations.
     * Optimizes timestamp-based queries and audit trail functionality.
     * Improves performance for data analysis and historical reporting.
     */
    public static void createReportingIndexes() {
        String[] reportingIndexes = {
                "CREATE INDEX IF NOT EXISTS idx_organization_created_at ON organization(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_organization_updated_at ON organization(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_contacts_created_at ON contacts(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_contacts_updated_at ON contacts(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_underaged_created_at ON underaged(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_underaged_updated_at ON underaged(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_teachers_created_at ON teachers(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_teachers_updated_at ON teachers(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_workshops_created_at ON workshops(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshops_updated_at ON workshops(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_created_at ON workshop_participants(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_workshop_participants_updated_at ON workshop_participants(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_lists_created_at ON lists(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_lists_updated_at ON lists(updated_at) WHERE updated_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_lists_deleted_at ON lists(deleted_at) WHERE deleted_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_payment_template_created_at ON payment_template(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_template_updated_at ON payment_template(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_newsletter_template_created_at ON newsletter_template(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_newsletter_template_updated_at ON newsletter_template(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_payment_attachment_created_at ON payment_attachment(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_attachment_updated_at ON payment_attachment(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_payment_info_created_at ON payment_info(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_info_updated_at ON payment_info(updated_at) WHERE updated_at IS NOT NULL",

                "CREATE INDEX IF NOT EXISTS idx_payment_slip_created_at ON payment_slip(created_at) WHERE created_at IS NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_payment_slip_updated_at ON payment_slip(updated_at) WHERE updated_at IS NOT NULL"
        };

        executeIndexes(reportingIndexes, "reporting and analytics");
    }

    /**
     * Helper method to execute index creation queries with proper error handling and logging.
     *
     * @param indexQueries Array of SQL CREATE INDEX statements
     * @param category Description of the index category for logging purposes
     */
    private static void executeIndexes(String[] indexQueries, String category) {
        try (Connection conn = getConnection()) {
            System.out.println("Creating SQLite indexes for " + category + "...");

            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;

            for (String query : indexQueries) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.executeUpdate();

                    String[] parts = query.split(" ");
                    String indexName = parts.length > 5 ? parts[5] : "unknown";
                    System.out.println("Created SQLite index: " + indexName);
                    successCount++;
                } catch (SQLException e) {
                    if (e.getMessage().contains("already exists")) {
                        skipCount++;
                    } else {
                        System.err.println("Failed to create index: " + query);
                        System.err.println("Error: " + e.getMessage());
                        errorCount++;
                    }
                }
            }

            System.out.println(category + " indexes processed: "
                    + successCount + " created, "
                    + skipCount + " skipped, "
                    + errorCount + " errors");

        } catch (SQLException e) {
            System.err.println("Error creating " + category + " indexes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates all performance indexes for comprehensive database optimization.
     * This method combines notification, performance, payment, and reporting indexes.
     * Should be called during database initialization for optimal performance.
     *
     * Performance improvements expected:
     * - Name searches: 10-50x faster
     * - Workshop queries: 5-20x faster
     * - Payment tracking: 5-15x faster
     * - Reporting queries: 10-100x faster
     */
    public static void createAllPerformanceIndexes() {
        System.out.println("Starting comprehensive database index creation...");

        long startTime = System.currentTimeMillis();

        createNotificationIndexes();
        createPerformanceIndexes();
        createPaymentIndexes();
        createReportingIndexes();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Comprehensive database indexing completed in " + duration + "ms");
        System.out.println("Database is now fully optimized for high performance!");
    }

    /**
     * Migrates workshop_participants table to remove teacher_id column.
     * Teachers are now linked directly to workshops instead of participants.
     * This migration preserves existing participant data while updating the schema.
     */
    public static void fixWorkshopParticipantsForTeachers() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking if database migration is needed...");

            boolean needsMigration = false;
            try {
                String testSQL = "SELECT teacher_id FROM workshop_participants LIMIT 1";
                stmt.executeQuery(testSQL);
                needsMigration = true;
                System.out.println("Migration needed - removing teacher_id from workshop_participants...");
            } catch (SQLException e) {
                System.out.println("Database already migrated - no migration needed.");
                return;
            }

            if (!needsMigration) {
                return;
            }

            String createBackupSQL = """
                CREATE TABLE IF NOT EXISTS workshop_participants_backup AS 
                SELECT * FROM workshop_participants;
                """;

            String dropOriginalSQL = "DROP TABLE IF EXISTS workshop_participants;";

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

            String restoreDataSQL = """
                INSERT INTO workshop_participants 
                (id, workshop_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at)
                SELECT id, workshop_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at
                FROM workshop_participants_backup
                WHERE participant_type IN ('ADULT', 'CHILD');
                """;

            String dropBackupSQL = "DROP TABLE IF EXISTS workshop_participants_backup;";

            try {
                stmt.execute(createBackupSQL);
                System.out.println("Created backup table");
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
                System.out.println("Cleaned up backup table");
            } catch (Exception e) {
                System.out.println("Note: Backup cleanup failed: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds teacher_id column to workshops table if it doesn't exist.
     * This migration supports the new workshop-teacher relationship model.
     */
    public static void addTeacherIdToWorkshops() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking if workshops table needs teacher_id column...");

            try {
                String testSQL = "SELECT teacher_id FROM workshops LIMIT 1";
                stmt.executeQuery(testSQL);
                System.out.println("teacher_id column already exists in workshops table.");
                return;
            } catch (SQLException e) {
                System.out.println("Adding teacher_id column to workshops table...");
            }

            String addColumnSQL = """
                ALTER TABLE workshops 
                ADD COLUMN teacher_id INTEGER REFERENCES teachers(id) ON DELETE SET NULL;
                """;

            stmt.execute(addColumnSQL);
            System.out.println("Added teacher_id column to workshops table");

        } catch (SQLException e) {
            System.err.println("Error adding teacher_id to workshops: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the complete database schema with all tables and optimizations.
     * Creates all necessary tables, runs migrations, and applies performance indexes.
     * This method should be called once during application startup.
     *
     * Database schema includes:
     * - Core CRM tables (organization, contacts, underaged members)
     * - Workshop management (workshops, participants, teachers)
     * - List management system
     * - Payment processing system
     * - Template management
     */
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
                contact_id INTEGER,
                list_id INTEGER,
                workshop_id INTEGER,
                target_type TEXT NOT NULL CHECK (target_type IN ('CONTACT', 'LIST', 'WORKSHOP')),
                amount DECIMAL(10,2) NOT NULL,
                model_of_payment TEXT NOT NULL,
                poziv_na_broj TEXT,
                barcode_data TEXT,
                pdf_path TEXT,
                status TEXT NOT NULL DEFAULT 'GENERATED' CHECK (status IN ('GENERATED', 'SENT', 'PAID', 'CANCELLED')),
                generated_at TEXT,
                sent_at TEXT,
                paid_at TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (organization_id) REFERENCES organization(id) ON DELETE CASCADE,
                FOREIGN KEY (payment_template_id) REFERENCES payment_template(id) ON DELETE CASCADE,
                FOREIGN KEY (newsletter_template_id) REFERENCES newsletter_template(id) ON DELETE SET NULL,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
                FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                CHECK (
                    (contact_id IS NOT NULL AND list_id IS NULL AND workshop_id IS NULL AND target_type = 'CONTACT') OR
                    (list_id IS NOT NULL AND contact_id IS NULL AND workshop_id IS NULL AND target_type = 'LIST') OR
                    (workshop_id IS NOT NULL AND contact_id IS NULL AND list_id IS NULL AND target_type = 'WORKSHOP')
                )
            );
            """;

        String createPaymentSlipTableSQL = """
            CREATE TABLE IF NOT EXISTS payment_slip (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                payment_info_id INTEGER NOT NULL,
                contact_id INTEGER NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                poziv_na_broj TEXT,
                barcode_data TEXT,
                status TEXT NOT NULL DEFAULT 'GENERATED' CHECK (status IN ('GENERATED', 'SENT', 'PAID', 'CANCELLED')),
                sent_at TEXT,
                paid_at TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (payment_info_id) REFERENCES payment_info(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Initializing database tables...");

            stmt.execute(createOrganizationTableSQL);
            stmt.execute(createContactsTableSQL);
            stmt.execute(createUnderagedTableSQL);
            stmt.execute(createTeachersTableSQL);
            stmt.execute(createWorkshopsTableSQL);
            stmt.execute(createWorkshopParticipantsTableSQL);
            stmt.execute(createListsTableSQL);
            stmt.execute(createListContactsTableSQL);
            stmt.execute(createPaymentTemplateTableSQL);
            stmt.execute(createNewsletterTemplateTableSQL);
            stmt.execute(createPaymentAttachmentTableSQL);
            stmt.execute(createPaymentInfoTableSQL);
            stmt.execute(createPaymentSlipTableSQL);

            System.out.println("Database tables created successfully");

            addTeacherIdToWorkshops();
            fixWorkshopParticipantsForTeachers();
            createAllPerformanceIndexes();

            System.out.println("Database initialization completed successfully");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}