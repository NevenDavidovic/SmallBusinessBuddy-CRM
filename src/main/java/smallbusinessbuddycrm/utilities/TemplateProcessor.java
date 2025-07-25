package smallbusinessbuddycrm.utilities;

import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.database.UnderagedDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized utility for processing payment template placeholders and underage logic
 */
public class TemplateProcessor {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Check if a contact is underage (under 18)
     * @param contact The contact to check
     * @return true if contact is under 18, false otherwise
     */
    public static boolean isContactUnderage(Contact contact) {
        if (contact.getBirthday() == null) {
            return false; // Cannot determine age without birthday
        }

        LocalDate birthDate = contact.getBirthday();
        LocalDate currentDate = LocalDate.now();
        LocalDate eighteenYearsAgo = currentDate.minusYears(18);

        return birthDate.isAfter(eighteenYearsAgo);
    }

    /**
     * Get the first active underage member for a contact
     * @param contactId The contact ID
     * @return UnderagedMember or null if none found
     */
    public static UnderagedMember getActiveUnderagedMember(int contactId) {
        try {
            UnderagedDAO underagedDAO = new UnderagedDAO();
            List<UnderagedMember> underagedList = underagedDAO.getUnderagedMembersByContactId(contactId);

            // Filter to only members and get first one
            return underagedList.stream()
                    .filter(UnderagedMember::isMember)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error loading underage member for contact " + contactId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all active underage members for a contact (for selection dialog)
     * @param contactId The contact ID
     * @return List of UnderagedMembers that are active members
     */
    public static List<UnderagedMember> getAllActiveUnderagedMembers(int contactId) {
        try {
            UnderagedDAO underagedDAO = new UnderagedDAO();
            List<UnderagedMember> allUnderagedList = underagedDAO.getUnderagedMembersByContactId(contactId);

            // Filter to only show members
            return allUnderagedList.stream()
                    .filter(UnderagedMember::isMember)
                    .toList();
        } catch (Exception e) {
            System.err.println("Error loading underage members for contact " + contactId + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Process a template description with placeholders
     * @param template The template string with placeholders
     * @param contact The contact data
     * @param underagedMember The underage member data (can be null)
     * @return Processed template with placeholders replaced
     */
    public static String processTemplate(String template, Contact contact, UnderagedMember underagedMember) {
        if (template == null || template.trim().isEmpty()) {
            return "Payment";
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());
            String placeholder = matcher.group(1);
            String replacement = processPlaceholder(placeholder, contact, underagedMember);
            result.append(replacement);
            lastEnd = matcher.end();
        }

        result.append(template.substring(lastEnd));
        return result.toString().trim();
    }

    /**
     * Process a template description with automatic underage member loading
     * @param template The template string with placeholders
     * @param contact The contact data
     * @return Processed template with placeholders replaced
     */
    public static String processTemplate(String template, Contact contact) {
        UnderagedMember underagedMember = null;

        if (isContactUnderage(contact)) {
            underagedMember = getActiveUnderagedMember(contact.getId());
        }

        return processTemplate(template, contact, underagedMember);
    }

    /**
     * Process individual placeholders
     * @param placeholder The placeholder name (without {{}})
     * @param contact The contact data
     * @param underagedMember The underage member data (can be null)
     * @return The replacement value
     */
    public static String processPlaceholder(String placeholder, Contact contact, UnderagedMember underagedMember) {
        try {
            if (placeholder.startsWith("contact_attributes.")) {
                String attribute = placeholder.substring("contact_attributes.".length());
                return getContactAttribute(attribute, contact);
            } else if (placeholder.startsWith("underaged_attributes.")) {
                String attribute = placeholder.substring("underaged_attributes.".length());
                return getUnderagedAttribute(attribute, underagedMember);
            } else if (placeholder.startsWith("custom_text.")) {
                String text = placeholder.substring("custom_text.".length());
                return text;
            }
        } catch (Exception e) {
            System.err.println("Error processing placeholder '" + placeholder + "': " + e.getMessage());
        }
        return "";
    }

    /**
     * Get contact attribute value
     * @param attribute The attribute name
     * @param contact The contact
     * @return The attribute value or empty string
     */
    public static String getContactAttribute(String attribute, Contact contact) {
        if (contact == null) return "";

        switch (attribute) {
            case "first_name": return contact.getFirstName() != null ? contact.getFirstName() : "";
            case "last_name": return contact.getLastName() != null ? contact.getLastName() : "";
            case "email": return contact.getEmail() != null ? contact.getEmail() : "";
            case "phone_num": return contact.getPhoneNum() != null ? contact.getPhoneNum() : "";
            case "birthday": return contact.getBirthday() != null ? contact.getBirthday().toString() : "";
            case "pin": return contact.getPin() != null ? contact.getPin() : "";
            case "street_name": return contact.getStreetName() != null ? contact.getStreetName() : "";
            case "street_num": return contact.getStreetNum() != null ? contact.getStreetNum() : "";
            case "postal_code": return contact.getPostalCode() != null ? contact.getPostalCode() : "";
            case "city": return contact.getCity() != null ? contact.getCity() : "";
            case "member_since": return contact.getMemberSince() != null ? contact.getMemberSince().toString() : "";
            case "member_until": return contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : "";
            default: return "";
        }
    }

    /**
     * Get underage member attribute value
     * @param attribute The attribute name
     * @param underaged The underage member
     * @return The attribute value or empty string
     */
    public static String getUnderagedAttribute(String attribute, UnderagedMember underaged) {
        if (underaged == null) return "";

        switch (attribute) {
            case "first_name": return underaged.getFirstName() != null ? underaged.getFirstName() : "";
            case "last_name": return underaged.getLastName() != null ? underaged.getLastName() : "";
            case "birth_date": return underaged.getBirthDate() != null ? underaged.getBirthDate().toString() : "";
            case "age": return String.valueOf(underaged.getAge());
            case "pin": return underaged.getPin() != null ? underaged.getPin() : "";
            case "gender": return underaged.getGender() != null ? underaged.getGender() : "";
            case "is_member": return underaged.isMember() ? "Yes" : "No";
            case "member_since": return underaged.getMemberSince() != null ? underaged.getMemberSince().toString() : "";
            case "member_until": return underaged.getMemberUntil() != null ? underaged.getMemberUntil().toString() : "";
            case "note": return underaged.getNote() != null ? underaged.getNote() : "";
            default: return "";
        }
    }
}