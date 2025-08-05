package smallbusinessbuddycrm.services.newsletter;

/**
 * Handles creation of table-based newsletter components for better email client compatibility
 */
public class NewsletterComponentBuilder {

    public String createTextComponent(String text) {
        return "\n<!-- Text Component -->\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 20px 0;\">\n" +
                "    <tr>\n" +
                "        <td style=\"padding: 15px; border-left: 3px solid #007bff; background-color: #f8f9fa;\">\n" +
                "            <p style=\"margin: 0; font-family: Arial, sans-serif; font-size: 16px; line-height: 24px; color: #333;\">" + text + "</p>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>";
    }

    public String createHeadingComponent(String heading) {
        return "\n<!-- Heading Component -->\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 30px 0 20px 0;\">\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <h2 style=\"margin: 0; font-family: Arial, sans-serif; font-size: 24px; font-weight: bold; color: #333; line-height: 32px;\">" + heading + "</h2>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>";
    }

    public String createImageComponent(String imageUrl) {
        return "\n<!-- Image Component -->\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 25px 0;\">\n" +
                "    <tr>\n" +
                "        <td align=\"center\">\n" +
                "            <img src=\"" + imageUrl + "\" alt=\"Newsletter Image\" style=\"max-width: 100%; height: auto; border-radius: 8px; display: block;\" />\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>";
    }

    public String createButtonComponent(String buttonText, String buttonUrl, String colorStyle) {
        String backgroundColor = extractBackgroundColor(colorStyle);
        String textColor = extractTextColor(colorStyle);

        return "\n<!-- Button Component -->\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 30px 0;\">\n" +
                "    <tr>\n" +
                "        <td align=\"center\">\n" +
                "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                    <td style=\"background-color: " + backgroundColor + "; border-radius: 6px; padding: 12px 24px;\">\n" +
                "                        <a href=\"" + buttonUrl + "\" style=\"font-family: Arial, sans-serif; font-size: 16px; font-weight: bold; color: " + textColor + "; text-decoration: none; display: block;\">" + buttonText + "</a>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>";
    }

    public String createDividerComponent() {
        return "\n<!-- Divider Component -->\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 30px 0;\">\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                    <td style=\"height: 2px; background: linear-gradient(to right, #007bff, #6c757d);\"></td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>";
    }

    private String extractBackgroundColor(String colorStyle) {
        if (colorStyle.contains("background-color: #28a745")) return "#28a745";
        if (colorStyle.contains("background-color: #fd7e14")) return "#fd7e14";
        if (colorStyle.contains("background-color: #dc3545")) return "#dc3545";
        return "#007bff"; // Default blue
    }

    private String extractTextColor(String colorStyle) {
        if (colorStyle.contains("color: white")) return "#ffffff";
        return "#333333"; // Default dark text
    }

    public String getButtonColorStyle(String color) {
        switch (color) {
            case "Success Green": return "background-color: #28a745; color: white;";
            case "Warning Orange": return "background-color: #fd7e14; color: white;";
            case "Danger Red": return "background-color: #dc3545; color: white;";
            default: return "background-color: #007bff; color: white;";
        }
    }

    /**
     * Helper class for button dialog results
     */
    public static class ButtonResult {
        private final String text;
        private final String url;
        private final String color;

        public ButtonResult(String text, String url, String color) {
            this.text = text;
            this.url = url;
            this.color = color;
        }

        public String getText() { return text; }
        public String getUrl() { return url; }
        public String getColor() { return color; }
    }
}