package smallbusinessbuddycrm.services.google;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;

/**
 * Enhanced Gmail service with attachment support for payment slips
 */
public class GmailService {

    private final HttpClient httpClient;

    public GmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Sends an email with payment slip attachments via Gmail API
     * @param accessToken OAuth access token
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param attachments Array of email attachments
     * @return true if email was sent successfully
     */
    public boolean sendEmailWithAttachments(String accessToken, String to, String subject, String body, EmailAttachment... attachments) {
        try {
            System.out.println("📧 Sending email with attachments to: " + to);

            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ No access token");
                return false;
            }

            String email = buildEmailMessageWithAttachments(to, subject, body, attachments);
            String encodedEmail = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(email.getBytes(StandardCharsets.UTF_8));

            String jsonPayload = "{\"raw\":\"" + encodedEmail + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📧 Gmail API status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ Email with attachments sent successfully!");
                return true;
            } else {
                System.err.println("❌ Gmail error: " + response.statusCode());
                System.err.println("❌ Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Email sending error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a payment slip email with PDF and/or PNG attachments
     * @param accessToken OAuth access token
     * @param recipientEmail Recipient email
     * @param payerName Name of the person paying
     * @param organizationName Name of the organization receiving payment
     * @param amount Payment amount
     * @param description Payment description
     * @param pdfContent PDF content as byte array (can be null)
     * @param barcodeImage Barcode image (can be null)
     * @return true if email was sent successfully
     */
    public boolean sendPaymentSlip(String accessToken, String recipientEmail, String payerName,
                                   String organizationName, String amount, String description,
                                   byte[] pdfContent, BufferedImage barcodeImage) {
        try {
            // Create email content
            String subject = "Payment Slip - " + organizationName;
            String body = buildPaymentSlipEmailBody(payerName, organizationName, amount, description);

            // Create attachments list
            java.util.List<EmailAttachment> attachments = new java.util.ArrayList<>();

            // Add PDF attachment if provided
            if (pdfContent != null) {
                attachments.add(new EmailAttachment(
                        "payment_slip_" + System.currentTimeMillis() + ".pdf",
                        "application/pdf",
                        pdfContent
                ));
            }

            // Add barcode image attachment if provided
            if (barcodeImage != null) {
                byte[] imageBytes = convertImageToBytes(barcodeImage);
                attachments.add(new EmailAttachment(
                        "payment_barcode_" + System.currentTimeMillis() + ".png",
                        "image/png",
                        imageBytes
                ));
            }

            return sendEmailWithAttachments(accessToken, recipientEmail, subject, body,
                    attachments.toArray(new EmailAttachment[0]));

        } catch (Exception e) {
            System.err.println("❌ Error sending payment slip: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends an email via Gmail API (original method for backward compatibility)
     */
    public boolean sendEmail(String accessToken, String to, String subject, String body) {
        return sendEmailWithAttachments(accessToken, to, subject, body);
    }

    /**
     * Builds the raw email message with attachments in RFC 2822 format
     */
    private String buildEmailMessageWithAttachments(String to, String subject, String body, EmailAttachment... attachments) {
        StringBuilder email = new StringBuilder();
        String boundary = "boundary_" + System.currentTimeMillis();

        // Email headers
        email.append("To: ").append(to).append("\r\n");
        email.append("Subject: ").append(subject).append("\r\n");
        email.append("MIME-Version: 1.0\r\n");

        if (attachments != null && attachments.length > 0) {
            email.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n");
        } else {
            email.append("Content-Type: text/plain; charset=utf-8\r\n");
        }

        email.append("\r\n");

        if (attachments != null && attachments.length > 0) {
            // Email body part
            email.append("--").append(boundary).append("\r\n");
            email.append("Content-Type: text/plain; charset=utf-8\r\n");
            email.append("\r\n");
            email.append(body).append("\r\n");

            // Attachment parts
            for (EmailAttachment attachment : attachments) {
                email.append("--").append(boundary).append("\r\n");
                email.append("Content-Type: ").append(attachment.getMimeType()).append("\r\n");
                email.append("Content-Transfer-Encoding: base64\r\n");
                email.append("Content-Disposition: attachment; filename=\"").append(attachment.getFileName()).append("\"\r\n");
                email.append("\r\n");

                // Encode attachment content as base64
                String base64Content = Base64.getEncoder().encodeToString(attachment.getContent());
                // Split base64 content into lines of 76 characters (RFC requirement)
                for (int i = 0; i < base64Content.length(); i += 76) {
                    int end = Math.min(i + 76, base64Content.length());
                    email.append(base64Content.substring(i, end)).append("\r\n");
                }
            }

            email.append("--").append(boundary).append("--\r\n");
        } else {
            // Simple email without attachments
            email.append(body);
        }

        return email.toString();
    }

    /**
     * Builds the email body for payment slip notifications
     */
    private String buildPaymentSlipEmailBody(String payerName, String organizationName, String amount, String description) {
        return "Dear " + payerName + ",\n\n" +
                "Please find attached your payment slip for " + organizationName + ".\n\n" +
                "Payment Details:\n" +
                "Amount: " + amount + " EUR\n" +
                "Description: " + description + "\n" +
                "Generated: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n\n" +
                "Instructions:\n" +
                "1. You can print the attached PDF and use it at any bank in Croatia\n" +
                "2. The barcode contains all payment information for easy processing\n" +
                "3. Keep this email for your records\n\n" +
                "If you have any questions about this payment, please contact " + organizationName + ".\n\n" +
                "Best regards,\n" +
                organizationName + "\n\n" +
                "---\n" +
                "This is an automated message. Please do not reply to this email.";
    }

    /**
     * Converts BufferedImage to byte array
     */
    private byte[] convertImageToBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    /**
     * Sends a test email with comprehensive CRM information
     */
    public boolean sendTestEmail(String accessToken, String userEmail, String userName) {
        String testSubject = "🎉 CRM Test Email - Gmail Integration Success!";
        String testBody = buildTestEmailBody(userEmail, userName);
        return sendEmail(accessToken, userEmail, testSubject, testBody);
    }

    /**
     * Sends a quick test email
     */
    public boolean sendQuickTestEmail(String accessToken, String userEmail, String userName) {
        String quickSubject = "⚡ Quick Test - CRM Working!";
        String quickBody = buildQuickTestEmailBody(userEmail, userName);
        return sendEmail(accessToken, userEmail, quickSubject, quickBody);
    }

    /**
     * Builds the test email body with comprehensive information
     */
    private String buildTestEmailBody(String userEmail, String userName) {
        return "Hello " + userName + "!\n\n" +
                "This is a test email from your Small Business Buddy CRM.\n\n" +
                "✅ OAuth 2.0: Working perfectly\n" +
                "✅ Gmail API: Connected and functional\n" +
                "✅ Email Sending: Successfully tested\n" +
                "✅ Token Persistence: Saved locally\n" +
                "✅ User Info Extraction: Complete\n" +
                "✅ Security: Bank-level encryption\n" +
                "✅ Attachment Support: Ready for payment slips\n\n" +
                "🎯 Connection Details:\n" +
                "Connected as: " + userEmail + "\n" +
                "User Name: " + userName + "\n" +
                "Sent at: " + LocalDateTime.now() + "\n" +
                "App: Small Business Buddy CRM\n\n" +
                "🚀 Your CRM is now ready to manage customer relationships and send professional emails with attachments!\n\n" +
                "Best regards,\n" +
                "Your Small Business Buddy CRM Team";
    }

    /**
     * Builds the quick test email body
     */
    private String buildQuickTestEmailBody(String userEmail, String userName) {
        return "🎉 QUICK TEST SUCCESS!\n\n" +
                "Your Gmail integration is working perfectly!\n\n" +
                "✅ Connected as: " + (userEmail != null ? userEmail : "Unknown") + "\n" +
                "✅ User Name: " + (userName != null ? userName : "Unknown") + "\n" +
                "✅ Test time: " + LocalDateTime.now() + "\n" +
                "✅ Token persistence: Working\n" +
                "✅ Email functionality: Ready\n" +
                "✅ Attachment support: Available\n\n" +
                "Your Small Business Buddy CRM is fully operational!";
    }

    /**
     * Validates if Gmail API access is working
     */
    public boolean validateGmailAccess(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/profile"))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 Gmail API validation status: " + response.statusCode());
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("❌ Gmail API validation failed: " + e.getMessage());
            return false;
        }
    }

    public void cleanup() {
        // Cleanup any resources if needed
    }

    /**
     * Inner class to represent email attachments
     */
    public static class EmailAttachment {
        private final String fileName;
        private final String mimeType;
        private final byte[] content;

        public EmailAttachment(String fileName, String mimeType, byte[] content) {
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public byte[] getContent() {
            return content;
        }
    }

    /**
     * Builds HTML email message in RFC 2822 format
     * @param to Recipient email
     * @param subject Email subject
     * @param htmlBody HTML content
     * @return Formatted email message
     */
    private String buildHtmlEmailMessage(String to, String subject, String htmlBody) {
        StringBuilder email = new StringBuilder();

        // Email headers
        email.append("To: ").append(to).append("\r\n");
        email.append("Subject: ").append(subject).append("\r\n");
        email.append("MIME-Version: 1.0\r\n");

        // IMPORTANT: Set content type to HTML instead of plain text
        email.append("Content-Type: text/html; charset=utf-8\r\n");
        email.append("\r\n");

        // HTML body content
        email.append(htmlBody);

        return email.toString();
    }

    /**
     * Sends HTML newsletter with both HTML and plain text versions (better compatibility)
     * @param accessToken OAuth access token
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML content for the newsletter
     * @param plainTextBody Plain text version (optional, will be auto-generated if null)
     * @return true if email was sent successfully
     */
    public boolean sendMultipartNewsletter(String accessToken, String to, String subject,
                                           String htmlBody, String plainTextBody) {
        try {
            System.out.println("📧 Sending multipart newsletter to: " + to);

            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ No access token");
                return false;
            }

            // Auto-generate plain text version if not provided
            if (plainTextBody == null || plainTextBody.trim().isEmpty()) {
                plainTextBody = convertHtmlToPlainText(htmlBody);
            }

            String email = buildMultipartEmailMessage(to, subject, htmlBody, plainTextBody);
            String encodedEmail = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(email.getBytes(StandardCharsets.UTF_8));

            String jsonPayload = "{\"raw\":\"" + encodedEmail + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📧 Gmail API status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ Multipart newsletter sent successfully!");
                return true;
            } else {
                System.err.println("❌ Gmail error: " + response.statusCode());
                System.err.println("❌ Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Multipart newsletter sending error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Builds multipart email with both HTML and plain text versions
     * @param to Recipient email
     * @param subject Email subject
     * @param htmlBody HTML content
     * @param plainTextBody Plain text content
     * @return Formatted multipart email message
     */
    private String buildMultipartEmailMessage(String to, String subject, String htmlBody, String plainTextBody) {
        StringBuilder email = new StringBuilder();
        String boundary = "newsletter_boundary_" + System.currentTimeMillis();

        // Email headers
        email.append("To: ").append(to).append("\r\n");
        email.append("Subject: ").append(subject).append("\r\n");
        email.append("MIME-Version: 1.0\r\n");
        email.append("Content-Type: multipart/alternative; boundary=\"").append(boundary).append("\"\r\n");
        email.append("\r\n");

        // Plain text version
        email.append("--").append(boundary).append("\r\n");
        email.append("Content-Type: text/plain; charset=utf-8\r\n");
        email.append("\r\n");
        email.append(plainTextBody).append("\r\n");

        // HTML version
        email.append("--").append(boundary).append("\r\n");
        email.append("Content-Type: text/html; charset=utf-8\r\n");
        email.append("\r\n");
        email.append(htmlBody).append("\r\n");

        // End boundary
        email.append("--").append(boundary).append("--\r\n");

        return email.toString();
    }

    /**
     * Convert HTML to plain text for email clients that don't support HTML
     * @param htmlContent HTML content
     * @return Plain text version
     */
    private String convertHtmlToPlainText(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        // Simple HTML to text conversion
        String plainText = htmlContent
                // Replace common HTML tags with meaningful text
                .replaceAll("(?i)<br\\s*/?\\s*>", "\n")
                .replaceAll("(?i)<p\\s*[^>]*>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)<h[1-6]\\s*[^>]*>", "\n** ")
                .replaceAll("(?i)</h[1-6]>", " **\n")
                .replaceAll("(?i)<li\\s*[^>]*>", "\n• ")
                .replaceAll("(?i)</li>", "")
                .replaceAll("(?i)<a\\s+[^>]*href=\"([^\"]*)\"[^>]*>([^<]*)</a>", "$2 ($1)")
                // Remove all remaining HTML tags
                .replaceAll("(?i)<[^>]*>", "")
                // Clean up whitespace
                .replaceAll("\\n\\s*\\n", "\n\n")
                .replaceAll("^\\s+", "")
                .trim();

        return plainText;
    }

    /**
     * Sends an HTML newsletter via Gmail API
     * @param accessToken OAuth access token
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML content for the newsletter
     * @return true if email was sent successfully
     */
    public boolean sendHtmlNewsletter(String accessToken, String to, String subject, String htmlBody) {
        try {
            System.out.println("📧 Sending HTML newsletter to: " + to);

            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ No access token");
                return false;
            }

            String email = buildHtmlEmailMessage(to, subject, htmlBody);
            String encodedEmail = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(email.getBytes(StandardCharsets.UTF_8));

            String jsonPayload = "{\"raw\":\"" + encodedEmail + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📧 Gmail API status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ HTML newsletter sent successfully!");
                return true;
            } else {
                System.err.println("❌ Gmail error: " + response.statusCode());
                System.err.println("❌ Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Newsletter sending error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}