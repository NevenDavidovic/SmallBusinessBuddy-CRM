package smallbusinessbuddycrm.services;

import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

public class BarcodePaymentService {

    public static class PaymentSlipData {
        private String organizationName;
        private String organizationIban;
        private String organizationAddress;
        private String payerName;
        private String payerAddress;
        private BigDecimal amount;
        private String modelOfPayment;
        private String pozivNaBroj;
        private String description;
        private String barcodeData;

        // Getters and setters
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

        public String getOrganizationIban() { return organizationIban; }
        public void setOrganizationIban(String organizationIban) { this.organizationIban = organizationIban; }

        public String getOrganizationAddress() { return organizationAddress; }
        public void setOrganizationAddress(String organizationAddress) { this.organizationAddress = organizationAddress; }

        public String getPayerName() { return payerName; }
        public void setPayerName(String payerName) { this.payerName = payerName; }

        public String getPayerAddress() { return payerAddress; }
        public void setPayerAddress(String payerAddress) { this.payerAddress = payerAddress; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getModelOfPayment() { return modelOfPayment; }
        public void setModelOfPayment(String modelOfPayment) { this.modelOfPayment = modelOfPayment; }

        public String getPozivNaBroj() { return pozivNaBroj; }
        public void setPozivNaBroj(String pozivNaBroj) { this.pozivNaBroj = pozivNaBroj; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getBarcodeData() { return barcodeData; }
        public void setBarcodeData(String barcodeData) { this.barcodeData = barcodeData; }
    }

    /**
     * Generates payment slip data for a contact using specified payment template
     */
    public static PaymentSlipData generatePaymentSlip(Contact contact, int paymentTemplateId) throws SQLException {
        PaymentSlipData slipData = new PaymentSlipData();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get organization data (assuming single organization for now)
            Organization organization = getOrganization(conn);
            if (organization == null) {
                throw new SQLException("No organization found in database");
            }

            // Get payment template data
            PaymentTemplate paymentTemplate = getPaymentTemplate(conn, paymentTemplateId);
            if (paymentTemplate == null) {
                throw new SQLException("Payment template not found with ID: " + paymentTemplateId);
            }

            // Build payment slip data
            slipData.setOrganizationName(organization.getName());
            slipData.setOrganizationIban(organization.getIban());
            slipData.setOrganizationAddress(buildOrganizationAddress(organization));

            slipData.setPayerName(contact.getFirstName() + " " + contact.getLastName());
            slipData.setPayerAddress(buildContactAddress(contact));

            slipData.setAmount(paymentTemplate.getAmount());
            slipData.setModelOfPayment(paymentTemplate.getModelOfPayment());

            // Generate poziv na broj - use template or generate based on contact ID
            String pozivNaBroj = paymentTemplate.getPozivNaBroj();
            if (pozivNaBroj == null || pozivNaBroj.trim().isEmpty()) {
                pozivNaBroj = generatePozivNaBroj(contact.getId());
            } else {
                // Replace placeholders if any
                pozivNaBroj = pozivNaBroj.replace("{contact_id}", String.valueOf(contact.getId()));
            }
            slipData.setPozivNaBroj(pozivNaBroj);

            slipData.setDescription(paymentTemplate.getDescription());

            // Generate barcode data (Croatian HUB-3 format)
            String barcodeData = generateHUB3Barcode(slipData);
            slipData.setBarcodeData(barcodeData);

            // Save payment info to database
            savePaymentInfo(conn, organization.getId(), paymentTemplateId, contact, slipData);

            return slipData;
        }
    }

    /**
     * Gets the first organization from database
     */
    private static Organization getOrganization(Connection conn) throws SQLException {
        String sql = "SELECT * FROM organization LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                Organization org = new Organization();
                org.setId(rs.getInt("id"));
                org.setName(rs.getString("name"));
                org.setIban(rs.getString("IBAN"));
                org.setStreetName(rs.getString("street_name"));
                org.setStreetNum(rs.getString("street_num"));
                org.setPostalCode(rs.getString("postal_code"));
                org.setCity(rs.getString("city"));
                org.setEmail(rs.getString("email"));
                org.setPhoneNum(rs.getString("phone_num"));
                return org;
            }
        }
        return null;
    }

    /**
     * Gets payment template by ID
     */
    private static PaymentTemplate getPaymentTemplate(Connection conn, int templateId) throws SQLException {
        String sql = "SELECT * FROM payment_template WHERE id = ? AND is_active = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, templateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PaymentTemplate template = new PaymentTemplate();
                    template.setId(rs.getInt("id"));
                    template.setName(rs.getString("name"));
                    template.setDescription(rs.getString("description"));
                    template.setAmount(rs.getBigDecimal("amount"));
                    template.setModelOfPayment(rs.getString("model_of_payment"));
                    template.setPozivNaBroj(rs.getString("poziv_na_broj"));
                    template.setActive(rs.getBoolean("is_active"));
                    return template;
                }
            }
        }
        return null;
    }

    /**
     * Builds organization address string
     */
    private static String buildOrganizationAddress(Organization org) {
        StringBuilder address = new StringBuilder();
        if (org.getStreetName() != null && !org.getStreetName().trim().isEmpty()) {
            address.append(org.getStreetName());
            if (org.getStreetNum() != null && !org.getStreetNum().trim().isEmpty()) {
                address.append(" ").append(org.getStreetNum());
            }
        }
        if (org.getPostalCode() != null && !org.getPostalCode().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(org.getPostalCode());
        }
        if (org.getCity() != null && !org.getCity().trim().isEmpty()) {
            if (org.getPostalCode() != null && !org.getPostalCode().trim().isEmpty()) {
                address.append(" ");
            } else if (address.length() > 0) {
                address.append(", ");
            }
            address.append(org.getCity());
        }
        return address.toString();
    }

    /**
     * Builds contact address string
     */
    private static String buildContactAddress(Contact contact) {
        StringBuilder address = new StringBuilder();
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            address.append(contact.getStreetName());
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                address.append(" ").append(contact.getStreetNum());
            }
        }
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(contact.getPostalCode());
        }
        if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
                address.append(" ");
            } else if (address.length() > 0) {
                address.append(", ");
            }
            address.append(contact.getCity());
        }
        return address.toString();
    }

    /**
     * Generates poziv na broj based on contact ID
     */
    private static String generatePozivNaBroj(int contactId) {
        // Generate check digit using modulo 11 algorithm (Croatian standard)
        String baseNumber = String.format("%010d", contactId); // Pad to 10 digits
        int checkDigit = calculateMod11CheckDigit(baseNumber);
        return baseNumber + checkDigit;
    }

    /**
     * Calculates modulo 11 check digit
     */
    private static int calculateMod11CheckDigit(String number) {
        int sum = 0;
        int weight = 2;

        // Process from right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(number.charAt(i)) * weight;
            weight++;
            if (weight > 7) weight = 2; // Reset weight cycle
        }

        int remainder = sum % 11;
        if (remainder < 2) {
            return 0;
        } else {
            return 11 - remainder;
        }
    }

    /**
     * Generates HUB-3 barcode data (simplified version)
     */
    private static String generateHUB3Barcode(PaymentSlipData slipData) {
        // HUB-3 format: Currency + Amount + IBAN + Model + Reference + Name + Description
        StringBuilder barcode = new StringBuilder();

        // Currency (HRK = 191, EUR = 978)
        barcode.append("978"); // Assuming EUR, change to 191 for HRK

        // Amount (15 digits, right-aligned, zero-padded)
        String amountStr = slipData.getAmount().multiply(new BigDecimal("100")).toBigInteger().toString();
        barcode.append(String.format("%015d", Long.parseLong(amountStr)));

        // IBAN (21 characters, space-padded if needed)
        String iban = slipData.getOrganizationIban().replaceAll("\\s+", "");
        barcode.append(String.format("%-21s", iban));

        // Model (2 digits)
        barcode.append(String.format("%02d", Integer.parseInt(slipData.getModelOfPayment())));

        // Reference number (22 characters, space-padded)
        barcode.append(String.format("%-22s", slipData.getPozivNaBroj()));

        // Payer name (25 characters, truncated if needed)
        String payerName = slipData.getPayerName();
        if (payerName.length() > 25) payerName = payerName.substring(0, 25);
        barcode.append(String.format("%-25s", payerName));

        // Description (25 characters, truncated if needed)
        String description = slipData.getDescription() != null ? slipData.getDescription() : "";
        if (description.length() > 25) description = description.substring(0, 25);
        barcode.append(String.format("%-25s", description));

        return barcode.toString();
    }

    /**
     * Saves payment info to database
     */
    private static void savePaymentInfo(Connection conn, int organizationId, int paymentTemplateId,
                                        Contact contact, PaymentSlipData slipData) throws SQLException {
        String sql = """
            INSERT INTO payment_info 
            (organization_id, payment_template_id, contact_id, target_type, amount, 
             model_of_payment, poziv_na_broj, barcode_data, status, generated_at, created_at, updated_at)
            VALUES (?, ?, ?, 'CONTACT', ?, ?, ?, ?, 'GENERATED', ?, ?, ?)
            """;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, organizationId);
            stmt.setInt(2, paymentTemplateId);
            stmt.setInt(3, contact.getId());
            stmt.setBigDecimal(4, slipData.getAmount());
            stmt.setString(5, slipData.getModelOfPayment());
            stmt.setString(6, slipData.getPozivNaBroj());
            stmt.setString(7, slipData.getBarcodeData());
            stmt.setString(8, timestamp);
            stmt.setString(9, timestamp);
            stmt.setString(10, timestamp);

            stmt.executeUpdate();
        }
    }
}