package smallbusinessbuddycrm.services.newsletter;

import javafx.scene.paint.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles HTML generation using table-based layouts for better email client compatibility
 */
public class NewsletterHtmlGenerator {

    public String generateNewsletterHtml(String title, String company, String content, Color headerColor) {
        String template = getTableBasedTemplate();

        return template
                .replace("{{TITLE}}", title != null ? title : "Newsletter")
                .replace("{{COMPANY}}", company != null ? company : "Your Company")
                .replace("{{CONTENT}}", content != null ? content : "")
                .replace("{{DATE}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                .replace("{{HEADER_COLOR}}", toHexString(headerColor));
    }

    private String getTableBasedTemplate() {
        return """
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>{{TITLE}}</title>
                <style type="text/css">
                    /* Email Client Reset */
                    body, table, td, p, a, li, blockquote { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
                    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
                    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }
                    
                    /* Global Styles */
                    body { margin: 0 !important; padding: 0 !important; background-color: #f4f4f4; }
                    table { border-collapse: collapse !important; }
                    .container { max-width: 600px; margin: 0 auto; }
                    
                    /* Typography */
                    .header-text { font-family: Arial, sans-serif; color: white; font-size: 28px; font-weight: bold; text-align: center; }
                    .subheader-text { font-family: Arial, sans-serif; color: white; font-size: 16px; text-align: center; }
                    .body-text { font-family: Arial, sans-serif; color: #333333; font-size: 16px; line-height: 24px; }
                    .footer-text { font-family: Arial, sans-serif; color: #666666; font-size: 14px; text-align: center; }
                    
                    /* Responsive */
                    @media screen and (max-width: 600px) {
                        .container { width: 100% !important; }
                        .header-text { font-size: 24px !important; }
                    }
                </style>
            </head>
            <body>
                <!-- Main Container Table -->
                <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f4f4f4;">
                    <tr>
                        <td align="center" style="padding: 20px 0;">
                            
                            <!-- Email Container -->
                            <table class="container" width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: white; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                                
                                <!-- Header Section -->
                                <tr>
                                    <td style="background-color: {{HEADER_COLOR}}; padding: 40px 20px;">
                                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td class="header-text">{{TITLE}}</td>
                                            </tr>
                                            <tr>
                                                <td class="subheader-text" style="padding-top: 10px;">{{COMPANY}} • {{DATE}}</td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <!-- Content Section -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td class="body-text">
                                                    {{CONTENT}}
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <!-- Footer Section -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 30px 20px;">
                                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td class="footer-text">
                                                    &copy; 2025 {{COMPANY}}. All rights reserved.
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="footer-text" style="padding-top: 10px;">
                                                    You're receiving this newsletter because you subscribed to our updates.
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                            </table>
                            
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;
    }

    public String extractContentFromHtml(String htmlContent) {
        if (htmlContent == null) return "";

        // Extract content from table-based structure
        String marker = "{{CONTENT}}";
        if (htmlContent.contains(marker)) {
            return ""; // Template placeholder
        }

        // Try to extract from rendered content
        String startMarker = "<td class=\"body-text\">";
        String endMarker = "</td>";

        int startIndex = htmlContent.indexOf(startMarker);
        if (startIndex != -1) {
            startIndex += startMarker.length();
            int endIndex = htmlContent.indexOf(endMarker, startIndex);
            if (endIndex != -1) {
                return htmlContent.substring(startIndex, endIndex).trim();
            }
        }

        return htmlContent; // Return original if extraction fails
    }

    public void extractDetailsFromContent(String htmlContent,
                                          java.util.function.Consumer<String> companyNameSetter) {
        if (htmlContent == null) return;

        // Extract company name from table structure
        String companyMarker = "{{COMPANY}}";
        if (htmlContent.contains(companyMarker)) {
            companyNameSetter.accept("{{COMPANY}}");
        } else {
            // Try to extract from rendered content
            String pattern = "([^•]+) •";
            java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regexPattern.matcher(htmlContent);
            if (matcher.find()) {
                companyNameSetter.accept(matcher.group(1).trim());
            }
        }
    }

    private String toHexString(Color color) {
        if (color == null) {
            color = Color.web("#007bff"); // Default blue
        }
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}