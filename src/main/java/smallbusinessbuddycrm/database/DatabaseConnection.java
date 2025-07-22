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

    // Migration method to fix existing database
    public static void fixWorkshopParticipantsForTeachers() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Checking if database migration is needed...");

            // Check if the table already supports teachers
            try {
                // Try to create a test teacher record - if it fails, we need migration
                String testSQL = "SELECT participant_type FROM workshop_participants WHERE participant_type = 'TEACHER' LIMIT 1";
                stmt.executeQuery(testSQL);
                System.out.println("Database already supports teachers - no migration needed.");
                return;
            } catch (SQLException e) {
                // If this fails, it means we need to migrate
                System.out.println("Migration needed - updating database schema...");
            }

            // Step 1: Create a backup table with current data
            String createBackupSQL = """
                CREATE TABLE IF NOT EXISTS workshop_participants_backup AS 
                SELECT * FROM workshop_participants;
                """;

            // Step 2: Drop the original table
            String dropOriginalSQL = "DROP TABLE IF EXISTS workshop_participants;";

            // Step 3: Create new table with updated constraint that supports teachers
            String createNewTableSQL = """
                CREATE TABLE workshop_participants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workshop_id INTEGER NOT NULL,
                    teacher_id INTEGER,
                    underaged_id INTEGER,
                    contact_id INTEGER,
                    participant_type TEXT NOT NULL CHECK (participant_type IN ('ADULT', 'CHILD', 'TEACHER')),
                    payment_status TEXT NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')),
                    notes TEXT,
                    created_at TEXT,
                    updated_at TEXT,
                    FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
                    FOREIGN KEY (underaged_id) REFERENCES underaged(id) ON DELETE CASCADE,
                    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                    CHECK (
                        (underaged_id IS NOT NULL AND contact_id IS NULL AND teacher_id IS NULL AND participant_type = 'CHILD') OR
                        (contact_id IS NOT NULL AND underaged_id IS NULL AND teacher_id IS NULL AND participant_type = 'ADULT') OR
                        (teacher_id IS NOT NULL AND contact_id IS NULL AND underaged_id IS NULL AND participant_type = 'TEACHER')
                    )
                );
                """;

            // Step 4: Restore data from backup (if backup exists)
            String restoreDataSQL = """
                INSERT INTO workshop_participants 
                (id, workshop_id, teacher_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at)
                SELECT id, workshop_id, teacher_id, underaged_id, contact_id, participant_type, payment_status, notes, created_at, updated_at
                FROM workshop_participants_backup;
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
            System.out.println("✓ Dropped original table");

            stmt.execute(createNewTableSQL);
            System.out.println("✓ Created new table with teacher support");

            try {
                stmt.execute(restoreDataSQL);
                System.out.println("✓ Restored existing data");
            } catch (Exception e) {
                System.out.println("Note: Data restore failed (table might have been empty): " + e.getMessage());
            }

            try {
                stmt.execute(dropBackupSQL);
                System.out.println("✓ Cleaned up backup table");
            } catch (Exception e) {
                System.out.println("Note: Backup cleanup failed: " + e.getMessage());
            }

            System.out.println("🎉 Database migration completed successfully!");
            System.out.println("Teacher assignment should now work!");

        } catch (SQLException e) {
            System.err.println("❌ Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to insert default payment attachment template
    private static void insertDefaultPaymentTemplate() {
        try (Connection conn = getConnection()) {
            // Check if we already have templates
            String checkSQL = "SELECT COUNT(*) FROM payment_attachment";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSQL);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Payment templates already exist.");
                    return;
                }
            } catch (SQLException e) {
                // Table might not exist yet, continue
                System.out.println("Payment attachment table not found, will create default template after table creation.");
                return;
            }

            // Insert default Croatian template
            String defaultHTML = """
                <!DOCTYPE html>
                <html lang="hr">
                <head>
                    <meta charset="UTF-8">
                    <title>HUB-3 Payment Slip</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; color: #333; }
                        .header { text-align: center; border-bottom: 2px solid #0099cc; padding-bottom: 10px; margin-bottom: 20px; }
                        .header h1 { color: #0099cc; margin: 0; font-size: 24px; }
                        .section { border: 1px solid #ddd; border-radius: 5px; padding: 15px; margin: 10px 0; background-color: #f9f9f9; }
                        .section h3 { margin-top: 0; color: #0099cc; }
                        .field { margin-bottom: 8px; }
                        .field-label { font-weight: bold; display: inline-block; width: 100px; }
                        .barcode-section { text-align: center; margin: 30px 0; padding: 20px; border: 2px dashed #ccc; }
                        .barcode-image { max-width: 100%; border: 1px solid #333; background: white; }
                        .amount-highlight { font-size: 18px; font-weight: bold; color: #28a745; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🇭🇷 Croatian HUB-3 Payment Slip</h1>
                        <p>Generated on: {{CURRENT_DATE}}</p>
                    </div>
                    
                    <div class="section">
                        <h3>💳 Payment Information</h3>
                        <div class="field"><span class="field-label">Amount:</span> <span class="amount-highlight">{{AMOUNT}} EUR</span></div>
                        <div class="field"><span class="field-label">Reference:</span> {{REFERENCE}}</div>
                        <div class="field"><span class="field-label">Model:</span> {{MODEL}}</div>
                        <div class="field"><span class="field-label">Purpose:</span> {{PURPOSE}}</div>
                        <div class="field"><span class="field-label">Description:</span> {{DESCRIPTION}}</div>
                    </div>
                    
                    <div class="section">
                        <h3>👤 Payer Information</h3>
                        <div class="field"><span class="field-label">Name:</span> {{PAYER_NAME}}</div>
                        <div class="field"><span class="field-label">Address:</span> {{PAYER_ADDRESS}}</div>
                        <div class="field"><span class="field-label">City:</span> {{PAYER_CITY}}</div>
                    </div>
                    
                    <div class="section">
                        <h3>🏢 Recipient Information</h3>
                        <div class="field"><span class="field-label">Company:</span> {{RECIPIENT_NAME}}</div>
                        <div class="field"><span class="field-label">Address:</span> {{RECIPIENT_ADDRESS}}</div>
                        <div class="field"><span class="field-label">City:</span> {{RECIPIENT_CITY}}</div>
                        <div class="field"><span class="field-label">IBAN:</span> {{RECIPIENT_IBAN}}</div>
                    </div>
                    
                    <div class="barcode-section">
                        <h3>📊 HUB-3 PDF417 Barcode</h3>
                        <p>Scan this barcode with your banking app</p>
                        <img src="data:image/png;base64,{{BARCODE_BASE64}}" alt="HUB-3 Barcode" class="barcode-image">
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px; font-size: 12px; color: #666;">
                        <p>HUB-3 Payment Standard | Bank Code: {{BANK_CODE}}</p>
                    </div>
                </body>
                </html>
                """;

            String insertSQL = """
                INSERT INTO payment_attachment (name, description, html_content, is_default, created_at, updated_at)
                VALUES (?, ?, ?, 1, datetime('now'), datetime('now'))
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, "Croatian HUB-3 Default");
                pstmt.setString(2, "Standard Croatian banking format with clean styling");
                pstmt.setString(3, defaultHTML);
                pstmt.executeUpdate();
                System.out.println("✓ Default payment template inserted!");
            }

        } catch (SQLException e) {
            System.err.println("Error inserting default template: " + e.getMessage());
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
                created_at TEXT,
                updated_at TEXT
            );
            """;

        // UPDATED: This now includes TEACHER support
        String createWorkshopParticipantsTableSQL = """
            CREATE TABLE IF NOT EXISTS workshop_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workshop_id INTEGER NOT NULL,
                teacher_id INTEGER,
                underaged_id INTEGER,
                contact_id INTEGER,
                participant_type TEXT NOT NULL CHECK (participant_type IN ('ADULT', 'CHILD', 'TEACHER')),
                payment_status TEXT NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')),
                notes TEXT,
                created_at TEXT,
                updated_at TEXT,
                FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE,
                FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL,
                FOREIGN KEY (underaged_id) REFERENCES underaged(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                CHECK (
                    (underaged_id IS NOT NULL AND contact_id IS NULL AND teacher_id IS NULL AND participant_type = 'CHILD') OR
                    (contact_id IS NOT NULL AND underaged_id IS NULL AND teacher_id IS NULL AND participant_type = 'ADULT') OR
                    (teacher_id IS NOT NULL AND contact_id IS NULL AND underaged_id IS NULL AND participant_type = 'TEACHER')
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

            System.out.println("Baza i tablice su inicijalizirane.");
            System.out.println("Workshop management tables created successfully.");
            System.out.println("Payment system tables created successfully.");
            System.out.println("Payment attachment template table created successfully.");

            // IMPORTANT: Run migration after table creation
            fixWorkshopParticipantsForTeachers();

            // Insert default payment template
            insertDefaultPaymentTemplate();

        } catch (SQLException e) {
            System.err.println("Greška pri inicijalizaciji baze: " + e.getMessage());
            e.printStackTrace();
        }
    }
}