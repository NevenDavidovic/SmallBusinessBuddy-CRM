package smallbusinessbuddycrm.utilities;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for JSON parsing operations
 */
public class JsonUtils {

    /**
     * Extracts a value from JSON string using multiple extraction methods
     * @param json JSON string
     * @param key Key to extract
     * @return Extracted value or null if not found
     */
    public static String extractValue(String json, String key) {
        try {
            System.out.println("🔍 Extracting '" + key + "' from JSON...");

            // Try multiple regex patterns for different JSON formats
            String[] patterns = {
                    "\"" + key + "\":\\s*\"([^\"]*)",         // "key": "value"
                    "\"" + key + "\":\"([^\"]*)",             // "key":"value"
                    "'" + key + "':\\s*'([^']*)",             // 'key': 'value'
                    "\"" + key + "\":\\s*([^,}\\]]+)",        // "key": value (without quotes)
            };

            for (int i = 0; i < patterns.length; i++) {
                try {
                    Pattern regexPattern = Pattern.compile(patterns[i]);
                    Matcher matcher = regexPattern.matcher(json);

                    if (matcher.find()) {
                        String value = matcher.group(1).trim();
                        // Remove surrounding quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        System.out.println("✅ Extracted '" + key + "' using pattern " + (i+1) + ": '" + value + "'");
                        return value;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Pattern " + (i+1) + " failed: " + e.getMessage());
                }
            }

            // Manual search as final fallback
            String manualResult = extractValueManually(json, key);
            if (manualResult != null) {
                return manualResult;
            }

            System.out.println("❌ Could not extract '" + key + "' using any method");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error extracting " + key + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Manual JSON value extraction as fallback method
     * @param json JSON string
     * @param key Key to extract
     * @return Extracted value or null if not found
     */
    private static String extractValueManually(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex != -1) {
                int colonIndex = json.indexOf(":", keyIndex);
                if (colonIndex != -1) {
                    int startQuote = json.indexOf("\"", colonIndex);
                    if (startQuote != -1) {
                        int endQuote = json.indexOf("\"", startQuote + 1);
                        if (endQuote != -1) {
                            String value = json.substring(startQuote + 1, endQuote);
                            System.out.println("✅ Manual extraction of '" + key + "': '" + value + "'");
                            return value;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Manual extraction failed for " + key + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Simple regex-based extraction for backwards compatibility
     * @param json JSON string
     * @param key Key to extract
     * @return Extracted value or null if not found
     */
    public static String extractWithRegex(String json, String key) {
        try {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                String value = matcher.group(1);
                System.out.println("✅ REGEX extracted " + key + ": " + value.substring(0, Math.min(20, value.length())) + "...");
                return value;
            } else {
                System.out.println("❌ REGEX could not find: " + key);
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Regex extraction failed for " + key + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates if a string is valid JSON format
     * @param jsonString String to validate
     * @return true if valid JSON
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        String trimmed = jsonString.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * Escapes special characters for JSON
     * @param value String to escape
     * @return Escaped string
     */
    public static String escapeJsonString(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Creates a simple JSON object with key-value pairs
     * @param keyValuePairs Alternating keys and values
     * @return JSON string
     */
    public static String createSimpleJson(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }

        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(escapeJsonString(keyValuePairs[i])).append("\":");
            json.append("\"").append(escapeJsonString(keyValuePairs[i + 1])).append("\"");
        }
        json.append("}");

        return json.toString();
    }
}