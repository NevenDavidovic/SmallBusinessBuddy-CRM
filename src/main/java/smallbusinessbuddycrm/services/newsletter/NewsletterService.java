package smallbusinessbuddycrm.services.newsletter;

import javafx.collections.ObservableList;
import smallbusinessbuddycrm.database.NewsletterTemplateDAO;
import smallbusinessbuddycrm.model.NewsletterTemplate;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

/**
 * Service class handling newsletter business logic with simplified status using existing isActive field
 */
public class NewsletterService {

    // Use template types as status indicators
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_SENT = "SENT";

    private NewsletterTemplateDAO templateDAO;
    private Connection dbConnection;

    public NewsletterService(Connection dbConnection) {
        this.dbConnection = dbConnection;
        if (dbConnection != null) {
            this.templateDAO = new NewsletterTemplateDAO(dbConnection);
        }
    }

    public List<NewsletterTemplate> getAllNewsletters() throws Exception {
        if (templateDAO != null) {
            return templateDAO.findAll();
        }
        return createDemoData();
    }

    public List<NewsletterTemplate> searchNewsletters(String searchTerm, String typeFilter) throws Exception {
        List<NewsletterTemplate> items;

        if (templateDAO == null) {
            return createDemoData();
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            items = templateDAO.findAll();
        } else {
            items = templateDAO.searchByName(searchTerm.trim());
        }

        // Apply type filter
        if (typeFilter != null && !typeFilter.equals("All Items")) {
            if (typeFilter.equals("Templates")) {
                items = items.stream()
                        .filter(t -> Arrays.asList("NEWSLETTER", "MARKETING", "ANNOUNCEMENT", "UPDATE", "PROMOTION")
                                .contains(t.getTemplateType()))
                        .toList();
            } else if (typeFilter.equals("Newsletters")) {
                items = items.stream()
                        .filter(t -> Arrays.asList(STATUS_DRAFT, STATUS_READY, STATUS_SENT)
                                .contains(t.getTemplateType()))
                        .toList();
            }
        }

        return items;
    }

    public NewsletterTemplate saveNewsletter(NewsletterTemplate newsletter) throws Exception {
        if (templateDAO != null) {
            // Ensure it's active when saving
            newsletter.setActive(true);
            return templateDAO.save(newsletter);
        } else {
            // Demo mode
            if (newsletter.getId() == 0) {
                newsletter.setId(System.currentTimeMillis() > Integer.MAX_VALUE ?
                        (int)(System.currentTimeMillis() % Integer.MAX_VALUE) :
                        (int)System.currentTimeMillis());
                newsletter.setCreatedAt(LocalDateTime.now());
            }
            newsletter.setUpdatedAt(LocalDateTime.now());
            newsletter.setActive(true);
            return newsletter;
        }
    }

    public boolean deleteNewsletter(int id) throws Exception {
        if (templateDAO != null) {
            return templateDAO.delete(id);
        }
        return true; // Demo mode always succeeds
    }

    public NewsletterTemplate duplicateNewsletter(int templateId, String newName) throws Exception {
        if (templateDAO != null) {
            return templateDAO.duplicate(templateId, newName);
        } else {
            // Demo mode - create a simple duplicate
            NewsletterTemplate duplicate = new NewsletterTemplate();
            duplicate.setId((int)(System.currentTimeMillis() % Integer.MAX_VALUE));
            duplicate.setName(newName);
            duplicate.setSubject("Copy of Newsletter");
            duplicate.setTemplateType("NEWSLETTER");
            duplicate.setActive(true);
            duplicate.setCreatedAt(LocalDateTime.now());
            return duplicate;
        }
    }

    // Helper method to get status based on template type and other factors
    public String getNewsletterStatus(NewsletterTemplate newsletter) {
        if (!newsletter.isActive()) {
            return "INACTIVE";
        }

        // Use template type as status indicator
        String type = newsletter.getTemplateType();
        if (Arrays.asList("NEWSLETTER", "MARKETING", "ANNOUNCEMENT", "UPDATE", "PROMOTION").contains(type)) {
            // These are templates
            return "TEMPLATE";
        } else if (Arrays.asList(STATUS_DRAFT, STATUS_READY, STATUS_SENT).contains(type)) {
            // These are actual newsletters with status
            return type;
        } else {
            return STATUS_DRAFT; // Default
        }
    }

    public List<String> getAvailableStatuses() {
        return Arrays.asList(STATUS_DRAFT, STATUS_READY, STATUS_SENT);
    }

    private List<NewsletterTemplate> createDemoData() {
        NewsletterTemplate demo1 = new NewsletterTemplate();
        demo1.setId(1);
        demo1.setName("Welcome Newsletter");
        demo1.setSubject("Welcome to our community!");
        demo1.setTemplateType(STATUS_DRAFT); // Use status as type
        demo1.setActive(true);
        demo1.setCreatedAt(LocalDateTime.now().minusDays(5));

        NewsletterTemplate demo2 = new NewsletterTemplate();
        demo2.setId(2);
        demo2.setName("Product Launch");
        demo2.setSubject("Exciting new product announcement");
        demo2.setTemplateType(STATUS_READY); // Use status as type
        demo2.setActive(true);
        demo2.setCreatedAt(LocalDateTime.now().minusDays(2));

        NewsletterTemplate demo3 = new NewsletterTemplate();
        demo3.setId(3);
        demo3.setName("Monthly Update");
        demo3.setSubject("Your monthly company update");
        demo3.setTemplateType(STATUS_SENT); // Use status as type
        demo3.setActive(true);
        demo3.setCreatedAt(LocalDateTime.now().minusDays(10));

        return Arrays.asList(demo1, demo2, demo3);
    }

    public boolean isDatabaseConnected() {
        return templateDAO != null;
    }
}