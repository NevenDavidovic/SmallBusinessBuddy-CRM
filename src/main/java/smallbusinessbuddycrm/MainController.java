package smallbusinessbuddycrm;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import smallbusinessbuddycrm.database.*;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private TitledPane bookmarks;
    @FXML private MenuButton userProfileButton;
    @FXML private Circle userAvatar;

    // 🔔 NOTIFICATION SYSTEM COMPONENTS
    @FXML private MenuButton notificationMenuButton;
    @FXML private StackPane notificationIconContainer;

    // Language chooser buttons
    @FXML private Button englishButton;
    @FXML private Button croatianButton;

    // Navigation sections
    @FXML private TitledPane crmPane;
    @FXML private TitledPane marketingPane;
    @FXML private TitledPane commercePane;
    @FXML private TitledPane designManagerPane;
    @FXML private TitledPane reportingPane;
    @FXML private TitledPane dataManagementPane;
    @FXML private TitledPane libraryPane;

    // Menu items
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;

    // Language label
    @FXML private Label languageLabel;

    // CRM section buttons
    @FXML private Button contactsButton;
    @FXML private Button listsButton;
    @FXML private Button workshopsButton;
    @FXML private Button teachersButton;

    // Marketing section buttons
    @FXML private Button createEmailButton;
    @FXML private Button emailStatsButton;

    // Commerce section buttons
    @FXML private Button barcodeButton;
    @FXML private Button paymentSlipsButton;
    @FXML private Button paymentHistoryButton;
    @FXML private Button bulkGenerationButton;

    // Design Manager section buttons
    @FXML private Button paymentTemplateButton;
    @FXML private Button newsletterTemplateButton;
    @FXML private Button paymentAttachmentButton;

    // Reporting section buttons
    @FXML private Button overviewButton;
    @FXML private Button underagedStatsButton;
    @FXML private Button contactStatsButton;
    @FXML private Button workshopStatsButton;

    // Data Management section buttons
    @FXML private Button propertiesButton;
    @FXML private Button importsButton;
    @FXML private Button exportButton;

    // Library section buttons
    @FXML private Button documentsButton;
    @FXML private Button tasksButton;
    @FXML private Button resourcesButton;

    // Core system components
    private OrganizationDAO organizationDAO = new OrganizationDAO();
    private LanguageManager languageManager;
    private Runnable languageChangeListener;

    // 🔔 NOTIFICATION SYSTEM COMPONENTS - Real-time only
    private ContactDAO contactDAO;
    private UnderagedDAO underagedDAO;
    private WorkshopDAO workshopDAO;
    private Circle notificationBadge;
    private Label badgeLabel;
    private Timeline notificationUpdateTimeline;

    @FXML
    public void initialize() {
        languageManager = LanguageManager.getInstance();

        // Create and register language change listener
        languageChangeListener = this::updateAllTexts;
        languageManager.addLanguageChangeListener(languageChangeListener);

        loadOrganizationName();
        updateLanguageButtons();
        updateAllTexts();

        // Load welcome screen or show default message
        loadInitialContent();

        // 🔔 Initialize real-time notification system
        initializeNotificationSystem();

        System.out.println("MainController initialized");
    }

    // 🔔 REAL-TIME NOTIFICATION SYSTEM INITIALIZATION
    private void initializeNotificationSystem() {
        try {
            // Initialize only existing DAOs
            contactDAO = new ContactDAO();
            underagedDAO = new UnderagedDAO();
            workshopDAO = new WorkshopDAO();

            // Setup notification badge
            setupNotificationBadge();

            // Update badge immediately
            updateNotificationBadge();

            // Setup periodic updates (every 5 minutes for more responsiveness)
            setupNotificationUpdates();

            System.out.println("Real-time notification system initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing notification system: " + e.getMessage());
        }
    }

    // 🔔 SETUP NOTIFICATION BADGE
    private void setupNotificationBadge() {
        if (notificationMenuButton != null && notificationIconContainer != null) {
            // Create notification badge
            notificationBadge = new Circle(10); // Increased to match 20x20 dimensions
            notificationBadge.setFill(Color.web("#f44336"));
            notificationBadge.setVisible(false);

            // Create count label
            badgeLabel = new Label();
            badgeLabel.setStyle(
                    "-fx-background-color: #f44336;" +
                            "-fx-background-radius: 10;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: center;" +
                            "-fx-text-alignment: center;" +
                            "-fx-min-width: 20;" +
                            "-fx-min-height: 20;" +
                            "-fx-max-width: 24;" +
                            "-fx-max-height: 24;" +
                            "-fx-translate-x: -3;" // Simulates margin-right: 3px
            );
            badgeLabel.setAlignment(Pos.CENTER);
            badgeLabel.setVisible(false);

            // Position badge at top-right
            StackPane.setAlignment(notificationBadge, Pos.TOP_RIGHT);
            StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
            StackPane.setMargin(notificationBadge, new Insets(-5, -5, 0, 0));
            StackPane.setMargin(badgeLabel, new Insets(-5, -5, 0, 0));

            notificationIconContainer.getChildren().addAll(notificationBadge, badgeLabel);

            // Setup dropdown behavior
            notificationMenuButton.setOnShowing(e -> loadNotificationDropdown());

            System.out.println("Notification badge setup completed");
        } else {
            System.err.println("NotificationMenuButton or IconContainer not found - check FXML fx:id");
        }
    }

    // 🔔 GET CURRENT NOTIFICATIONS (only today/upcoming events)
    private List<NotificationItem> getNotifications(int limit) {
        List<NotificationItem> notifications = new java.util.ArrayList<>();

        // Generate notifications in real-time
        generateCurrentNotifications(notifications);

        // Sort by priority (today events first, then tomorrow, etc.)
        notifications.sort((a, b) -> {
            // Priority: TODAY events first, then TOMORROW, then others
            if (a.message.contains(getTranslation("notification.today")) && !b.message.contains(getTranslation("notification.today"))) return -1;
            if (!a.message.contains(getTranslation("notification.today")) && b.message.contains(getTranslation("notification.today"))) return 1;
            if (a.message.contains(getTranslation("notification.tomorrow")) && !b.message.contains(getTranslation("notification.tomorrow"))) return -1;
            if (!a.message.contains(getTranslation("notification.tomorrow")) && b.message.contains(getTranslation("notification.tomorrow"))) return 1;
            return a.createdAt.compareTo(b.createdAt);
        });

        // Limit results
        return notifications.size() > limit ?
                notifications.subList(0, limit) : notifications;
    }

    // 🔔 GENERATE CURRENT NOTIFICATIONS (real-time, no database storage)
    private void generateCurrentNotifications(List<NotificationItem> notifications) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        try {
            // 🎂 TODAY'S AND UPCOMING BIRTHDAYS
            generateCurrentBirthdayNotifications(notifications, today, formatter);

            // 🚀 TODAY'S AND UPCOMING WORKSHOPS
            generateCurrentWorkshopNotifications(notifications, today, formatter);

        } catch (Exception e) {
            System.err.println("❌ Error generating current notifications: " + e.getMessage());
        }
    }

    // 🔔 GENERATE CURRENT BIRTHDAY NOTIFICATIONS
    private void generateCurrentBirthdayNotifications(List<NotificationItem> notifications, LocalDate today, DateTimeFormatter formatter) {
        try {
            // Contact birthdays (next 3 days only)
            List<Contact> contactsWithBirthdays = contactDAO.getContactsWithUpcomingBirthdays(3);
            for (Contact contact : contactsWithBirthdays) {
                if (contact.getBirthday() != null) {
                    LocalDate thisYearBirthday = contact.getBirthday().withYear(today.getYear());
                    if (thisYearBirthday.isBefore(today)) {
                        thisYearBirthday = thisYearBirthday.plusYears(1);
                    }

                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, thisYearBirthday);

                    // Only show if within next 3 days
                    if (daysUntil >= 0 && daysUntil <= 3) {
                        String title = daysUntil == 0 ? "🎉 " + getTranslation("notification.birthday.today") :
                                daysUntil == 1 ? "🎂 " + getTranslation("notification.birthday.tomorrow") :
                                        "🎈 " + getTranslation("notification.birthday.upcoming");

                        String timeText = daysUntil == 0 ? getTranslation("notification.today") :
                                daysUntil == 1 ? getTranslation("notification.tomorrow") :
                                        getTranslation("notification.in") + " " + daysUntil + " " + getTranslation("notification.days");

                        String message = String.format("%s %s %s %s (%s)",
                                contact.getFirstName(),
                                contact.getLastName(),
                                getTranslation("notification.birthday.is"),
                                timeText,
                                thisYearBirthday.format(formatter)
                        );

                        NotificationItem notification = createNotificationItem(
                                title, message, "BIRTHDAY_CONTACT", contact.getId()
                        );
                        notifications.add(notification);
                    }
                }
            }

            // Underaged member birthdays (next 3 days only)
            List<UnderagedMember> allUnderagedMembers = underagedDAO.getAllUnderagedMembers();
            for (UnderagedMember underaged : allUnderagedMembers) {
                if (underaged.getBirthDate() != null) {
                    LocalDate thisYearBirthday = underaged.getBirthDate().withYear(today.getYear());
                    if (thisYearBirthday.isBefore(today)) {
                        thisYearBirthday = thisYearBirthday.plusYears(1);
                    }

                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, thisYearBirthday);

                    // Only show if within next 3 days
                    if (daysUntil >= 0 && daysUntil <= 3) {
                        String title = daysUntil == 0 ? "🎉 " + getTranslation("notification.birthday.today") :
                                daysUntil == 1 ? "🎂 " + getTranslation("notification.birthday.tomorrow") :
                                        "🎈 " + getTranslation("notification.birthday.upcoming");

                        String timeText = daysUntil == 0 ? getTranslation("notification.today") :
                                daysUntil == 1 ? getTranslation("notification.tomorrow") :
                                        getTranslation("notification.in") + " " + daysUntil + " " + getTranslation("notification.days");

                        String message = String.format("%s %s %s %s (%s)",
                                underaged.getFirstName(),
                                underaged.getLastName(),
                                getTranslation("notification.birthday.is"),
                                timeText,
                                thisYearBirthday.format(formatter)
                        );

                        NotificationItem notification = createNotificationItem(
                                title, message, "BIRTHDAY_UNDERAGED", underaged.getId()
                        );
                        notifications.add(notification);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error generating birthday notifications: " + e.getMessage());
        }
    }

    // 🔔 GENERATE CURRENT WORKSHOP NOTIFICATIONS
    private void generateCurrentWorkshopNotifications(List<NotificationItem> notifications, LocalDate today, DateTimeFormatter formatter) {
        try {
            // Upcoming workshops (next 3 days only)
            List<Workshop> upcomingWorkshops = workshopDAO.getUpcomingWorkshops(3);
            for (Workshop workshop : upcomingWorkshops) {
                if (workshop.getFromDate() != null) {
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, workshop.getFromDate());

                    // Only show if within next 3 days
                    if (daysUntil >= 0 && daysUntil <= 3) {
                        String title = daysUntil == 0 ? "🚀 " + getTranslation("notification.workshop.starting.today") :
                                daysUntil == 1 ? "📅 " + getTranslation("notification.workshop.tomorrow") :
                                        "🔔 " + getTranslation("notification.workshop.upcoming");

                        String timeText = daysUntil == 0 ? getTranslation("notification.starts.today") :
                                daysUntil == 1 ? getTranslation("notification.starts.tomorrow") :
                                        getTranslation("notification.starts.in") + " " + daysUntil + " " + getTranslation("notification.days");

                        String message = String.format("%s '%s' %s (%s)",
                                getTranslation("notification.workshop"),
                                workshop.getName(),
                                timeText,
                                workshop.getFromDate().format(formatter)
                        );

                        NotificationItem notification = createNotificationItem(
                                title, message, "WORKSHOP_UPCOMING", workshop.getId()
                        );
                        notifications.add(notification);
                    }
                }
            }

            // Active workshops ending today
            List<Workshop> activeWorkshops = workshopDAO.getActiveWorkshops();
            for (Workshop workshop : activeWorkshops) {
                if (workshop.getToDate() != null && workshop.getToDate().equals(today)) {
                    String title = "🏁 " + getTranslation("notification.workshop.ending.today");
                    String message = String.format("%s '%s' %s",
                            getTranslation("notification.workshop"),
                            workshop.getName(),
                            getTranslation("notification.ends.today")
                    );

                    NotificationItem notification = createNotificationItem(
                            title, message, "WORKSHOP_ENDING_TODAY", workshop.getId()
                    );
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error generating workshop notifications: " + e.getMessage());
        }
    }

    // 🔔 CREATE NOTIFICATION ITEM (helper method)
    private NotificationItem createNotificationItem(String title, String message, String type, int relatedId) {
        NotificationItem notification = new NotificationItem();
        notification.id = 0; // Not stored in database
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.relatedId = relatedId;
        notification.isRead = false; // Always unread since they're current
        notification.createdAt = java.time.LocalDateTime.now();
        return notification;
    }

    // 🔔 GET UNREAD COUNT (real-time count)
    private int getUnreadCount() {
        List<NotificationItem> currentNotifications = new java.util.ArrayList<>();
        generateCurrentNotifications(currentNotifications);
        return currentNotifications.size(); // All current notifications are "unread"
    }

    // 🔔 SETUP PERIODIC NOTIFICATION UPDATES (more frequent)
    private void setupNotificationUpdates() {
        // Update notifications every 5 minutes for real-time feel
        notificationUpdateTimeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> {
            updateNotificationBadge();
        }));
        notificationUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationUpdateTimeline.play();

        System.out.println("Real-time notification update timeline started (5 min intervals)");
    }

    // 🔔 LOAD NOTIFICATION DROPDOWN
    private void loadNotificationDropdown() {
        Platform.runLater(() -> {
            try {
                // Clear existing menu items
                notificationMenuButton.getItems().clear();

                List<NotificationItem> notifications = getNotifications(8);

                if (notifications.isEmpty()) {
                    createEmptyStateMenuItem();
                } else {
                    createHeaderMenuItem();
                    notificationMenuButton.getItems().add(new SeparatorMenuItem());

                    for (NotificationItem notification : notifications) {
                        CustomMenuItem notificationItem = createNotificationMenuItem(notification);
                        notificationMenuButton.getItems().add(notificationItem);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Error loading notification dropdown: " + e.getMessage());
                e.printStackTrace();
                createErrorMenuItem();
            }
        });
    }

    // 🔔 SIMPLIFIED DROPDOWN MENU ITEMS
    private void createHeaderMenuItem() {
        int currentCount = getUnreadCount();
        String headerText = currentCount > 0 ?
                "🔔 " + getTranslation("notification.current.events") + " (" + currentCount + ")" :
                "🔔 " + getTranslation("notification.no.current.events");

        Label headerLabel = new Label(headerText);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 12 8 12;");

        CustomMenuItem headerItem = new CustomMenuItem(headerLabel);
        headerItem.setDisable(true);
        headerItem.setHideOnClick(false);

        notificationMenuButton.getItems().add(headerItem);
    }

    // 🔔 SIMPLIFIED MENU ITEM CREATION (no read/unread state)
    private CustomMenuItem createNotificationMenuItem(NotificationItem notification) {
        VBox notificationContent = new VBox(3);
        notificationContent.setPadding(new Insets(12, 16, 12, 16));
        notificationContent.setMaxWidth(350);
        notificationContent.setPrefWidth(350);

        // Title (always fresh/current)
        Label titleLabel = new Label(notification.title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        // Message
        Label messageLabel = new Label(notification.message);
        messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(330);

        // Current time indicator
        Label timeLabel = new Label(getTranslation("notification.now"));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        notificationContent.getChildren().addAll(titleLabel, messageLabel, timeLabel);

        // Always fresh appearance
        notificationContent.setStyle("-fx-background-color: #f0f8ff;");

        CustomMenuItem menuItem = new CustomMenuItem(notificationContent);
        menuItem.setHideOnClick(true);

        // Add hover effect
        notificationContent.setOnMouseEntered(e -> {
            notificationContent.setStyle("-fx-background-color: #e3f2fd;");
        });

        notificationContent.setOnMouseExited(e -> {
            notificationContent.setStyle("-fx-background-color: #f0f8ff;");
        });

        return menuItem;
    }

    // 🔔 CREATE EMPTY STATE MENU ITEM
    private void createEmptyStateMenuItem() {
        Label emptyLabel = new Label("📭 " + getTranslation("notification.no.events"));
        emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic; -fx-padding: 20 12 20 12;");

        CustomMenuItem emptyItem = new CustomMenuItem(emptyLabel);
        emptyItem.setDisable(true);
        emptyItem.setHideOnClick(false);

        notificationMenuButton.getItems().add(emptyItem);
    }

    // 🔔 CREATE ERROR MENU ITEM
    private void createErrorMenuItem() {
        Label errorLabel = new Label("❌ " + getTranslation("notification.error.loading"));
        errorLabel.setStyle("-fx-text-fill: #f44336; -fx-padding: 12;");

        CustomMenuItem errorItem = new CustomMenuItem(errorLabel);
        errorItem.setDisable(true);
        errorItem.setHideOnClick(false);

        notificationMenuButton.getItems().add(errorItem);
    }

    // 🔔 UPDATE NOTIFICATION BADGE
    private void updateNotificationBadge() {
        Platform.runLater(() -> {
            int unreadCount = getUnreadCount();

            if (unreadCount > 0) {
                notificationBadge.setVisible(true);
                badgeLabel.setVisible(true);
                badgeLabel.setText(String.valueOf(Math.min(unreadCount, 99))); // Cap at 99
            } else {
                notificationBadge.setVisible(false);
                badgeLabel.setVisible(false);
            }
        });
    }

    // 🔔 NOTIFICATION ITEM CLASS
    private static class NotificationItem {
        int id;
        String title;
        String message;
        String type;
        int relatedId;
        boolean isRead;
        java.time.LocalDateTime createdAt;
    }

    // 🔔 TRANSLATION HELPER METHOD
    private String getTranslation(String key) {
        try {
            return languageManager.getText(key);
        } catch (Exception e) {
            // Fallback to English if translation missing
            return getEnglishFallback(key);
        }
    }

    // 🔔 ENGLISH FALLBACKS FOR NOTIFICATIONS
    private String getEnglishFallback(String key) {
        return switch (key) {
            case "notification.birthday.today" -> "Birthday Today!";
            case "notification.birthday.tomorrow" -> "Birthday Tomorrow";
            case "notification.birthday.upcoming" -> "Upcoming Birthday";
            case "notification.birthday.is" -> "'s birthday is";
            case "notification.workshop.starting.today" -> "Workshop Starting Today!";
            case "notification.workshop.tomorrow" -> "Workshop Tomorrow";
            case "notification.workshop.upcoming" -> "Upcoming Workshop";
            case "notification.workshop.ending.today" -> "Workshop Ending Today";
            case "notification.workshop" -> "Workshop";
            case "notification.today" -> "today";
            case "notification.tomorrow" -> "tomorrow";
            case "notification.in" -> "in";
            case "notification.days" -> "days";
            case "notification.starts.today" -> "starts today";
            case "notification.starts.tomorrow" -> "starts tomorrow";
            case "notification.starts.in" -> "starts in";
            case "notification.ends.today" -> "ends today";
            case "notification.current.events" -> "Current Events";
            case "notification.no.current.events" -> "No Current Events";
            case "notification.now" -> "Now";
            case "notification.no.events" -> "No notifications";
            case "notification.error.loading" -> "Error loading notifications";
            default -> key;
        };
    }

    // ================================
    // EXISTING METHODS (unchanged)
    // ================================

    private void loadInitialContent() {
        try {
            // Try to load welcome screen first
            navigateTo("/views/welcome-view.fxml");
        } catch (Exception e) {
            // If welcome screen doesn't exist, show a simple welcome message
            showSimpleWelcome();
        }
    }

    private void showSimpleWelcome() {
        try {
            Label welcomeLabel = new Label();
            welcomeLabel.setText(languageManager.getText("welcome.simple.message"));
            welcomeLabel.setStyle(
                    "-fx-font-size: 24px; " +
                            "-fx-text-fill: #1a1d29; " +
                            "-fx-padding: 50; " +
                            "-fx-text-alignment: center;"
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(welcomeLabel);

            System.out.println("Showing simple welcome message");
        } catch (Exception ex) {
            // Final fallback - just show "Hello World"
            Label fallbackLabel = new Label("Hello World - Small Business Buddy CRM");
            fallbackLabel.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-text-fill: #333; " +
                            "-fx-padding: 50;"
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(fallbackLabel);

            System.out.println("Showing fallback hello world message");
        }
    }

    @FXML
    private void switchToEnglish() {
        languageManager.setLanguage("en");
        updateLanguageButtons();
        System.out.println("Main: Switched to English");
    }

    @FXML
    private void switchToCroatian() {
        languageManager.setLanguage("hr");
        updateLanguageButtons();
        System.out.println("Main: Prebačeno na hrvatski");
    }

    private void updateLanguageButtons() {
        if (englishButton == null || croatianButton == null) {
            System.err.println("Language buttons are null - check FXML fx:id");
            return;
        }

        englishButton.getStyleClass().removeAll("language-active");
        croatianButton.getStyleClass().removeAll("language-active");

        if (languageManager.isEnglish()) {
            englishButton.getStyleClass().add("language-active");
        } else {
            croatianButton.getStyleClass().add("language-active");
        }
    }

    private void updateAllTexts() {
        updateNavigationTexts();
        updateCurrentViewTexts();
        updateLanguageButtons();
    }

    private void updateNavigationTexts() {
        try {
            // Update main navigation sections
            if (crmPane != null) crmPane.setText(languageManager.getText("nav.crm"));
            if (marketingPane != null) marketingPane.setText(languageManager.getText("nav.marketing"));
            if (commercePane != null) commercePane.setText(languageManager.getText("nav.commerce"));
            if (designManagerPane != null) designManagerPane.setText(languageManager.getText("nav.design.manager"));
            if (reportingPane != null) reportingPane.setText(languageManager.getText("nav.reporting"));
            if (dataManagementPane != null) dataManagementPane.setText(languageManager.getText("nav.data.management"));
            if (libraryPane != null) libraryPane.setText(languageManager.getText("nav.library"));

            // Update menu items
            if (profileMenuItem != null) profileMenuItem.setText(languageManager.getText("menu.profile"));
            if (settingsMenuItem != null) settingsMenuItem.setText(languageManager.getText("menu.settings"));

            // Update language label
            if (languageLabel != null) languageLabel.setText(languageManager.getText("language.selector"));

            // Update CRM section buttons
            if (contactsButton != null) contactsButton.setText(languageManager.getText("crm.contacts"));
            if (listsButton != null) listsButton.setText(languageManager.getText("crm.lists"));
            if (workshopsButton != null) workshopsButton.setText(languageManager.getText("crm.workshops"));
            if (teachersButton != null) teachersButton.setText(languageManager.getText("crm.teachers"));

            // Update Marketing section buttons
            if (createEmailButton != null) createEmailButton.setText(languageManager.getText("marketing.create.campaign"));
            if (emailStatsButton != null) emailStatsButton.setText(languageManager.getText("marketing.statistics"));

            // Update Commerce section buttons
            if (barcodeButton != null) barcodeButton.setText(languageManager.getText("commerce.barcode"));
            if (paymentSlipsButton != null) paymentSlipsButton.setText(languageManager.getText("commerce.payment.slips"));
            if (paymentHistoryButton != null) paymentHistoryButton.setText(languageManager.getText("commerce.payment.history"));
            if (bulkGenerationButton != null) bulkGenerationButton.setText(languageManager.getText("commerce.bulk.generation"));

            // Update Design Manager section buttons
            if (paymentTemplateButton != null) paymentTemplateButton.setText(languageManager.getText("design.payment.template"));
            if (newsletterTemplateButton != null) newsletterTemplateButton.setText(languageManager.getText("design.newsletter.template"));
            if (paymentAttachmentButton != null) paymentAttachmentButton.setText(languageManager.getText("design.payment.attachment"));

            // Update Reporting section buttons
            if (overviewButton != null) overviewButton.setText(languageManager.getText("reporting.overview"));
            if (underagedStatsButton != null) underagedStatsButton.setText(languageManager.getText("reporting.underaged.stats"));
            if (contactStatsButton != null) contactStatsButton.setText(languageManager.getText("reporting.contact.stats"));
            if (workshopStatsButton != null) workshopStatsButton.setText(languageManager.getText("reporting.workshop.stats"));

            // Update Data Management section buttons
            if (propertiesButton != null) propertiesButton.setText(languageManager.getText("data.properties"));
            if (importsButton != null) importsButton.setText(languageManager.getText("data.imports"));
            if (exportButton != null) exportButton.setText(languageManager.getText("data.export"));

            // Update Library section buttons
            if (documentsButton != null) documentsButton.setText(languageManager.getText("library.documents"));
            if (tasksButton != null) tasksButton.setText(languageManager.getText("library.tasks"));
            if (resourcesButton != null) resourcesButton.setText(languageManager.getText("library.resources"));

            String currentLang = languageManager.isEnglish() ? "English" : "Croatian";
            System.out.println("Main navigation updated to: " + currentLang);

        } catch (Exception e) {
            System.err.println("Error updating navigation texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCurrentViewTexts() {
        // Update the simple welcome message if it's currently shown
        Node currentView = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);
        if (currentView instanceof Label) {
            Label label = (Label) currentView;
            if (label.getText().contains("Hello World") ||
                    label.getText().contains("Small Business Buddy") ||
                    label.getText().contains("Dobrodošli")) {
                // This is our simple welcome message, update it
                try {
                    label.setText(languageManager.getText("welcome.simple.message"));
                } catch (Exception e) {
                    // Fallback text
                    String fallbackText = languageManager.isEnglish() ?
                            "Welcome to Small Business Buddy CRM" :
                            "Dobrodošli u Small Business Buddy CRM";
                    label.setText(fallbackText);
                }
            }
        }
    }

    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("Successfully navigated to: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("Could not load view: " + fxmlPath);
            e.printStackTrace();

            // Show error message in content area
            showErrorMessage("Could not load: " + fxmlPath);
        }
    }

    private void showErrorMessage(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #dc3545; " +
                        "-fx-padding: 50; " +
                        "-fx-text-alignment: center;"
        );

        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorLabel);
    }

    public void cleanup() {
        if (languageManager != null && languageChangeListener != null) {
            languageManager.removeLanguageChangeListener(languageChangeListener);
            System.out.println("MainController: Language change listener removed");
        }

        // 🔔 Stop notification timeline
        if (notificationUpdateTimeline != null) {
            notificationUpdateTimeline.stop();
            System.out.println("MainController: Notification update timeline stopped");
        }
    }

    // 🔔 PUBLIC METHOD TO REFRESH NOTIFICATIONS
    public void refreshNotifications() {
        updateNotificationBadge();
    }

    // Navigation action methods
    @FXML private void handleContactsAction() { navigateTo("/views/crm/contacts-view.fxml"); }
    @FXML private void handleOrganizationAction() { navigateTo("/views/general/organization-view.fxml"); }
    @FXML private void handleListsAction() { navigateTo("/views/crm/lists-view.fxml"); }
    @FXML private void handleWorkshopsAction() { navigateTo("/views/crm/workshops-view.fxml"); }
    @FXML private void handleEmailAction() { navigateTo("/views/marketing/email-builder.fxml"); }
    @FXML private void handleBarcodeAppAction() { navigateTo("/views/commerce/barcode-generator-view.fxml"); }
    @FXML private void handlePaymentSlipsAction() { navigateTo("/views/commerce/payment-slips-view.fxml"); }
    @FXML private void handlePaymentHistoryAction() { navigateTo("/views/commerce/payment-history-view.fxml"); }
    @FXML private void handleTeachersAction() { navigateTo("/views/crm/teacher-view.fxml"); }
    @FXML private void handleEmailTemplateAction() { navigateTo("/views/marketing/email-builder.fxml"); }
    @FXML private void handlePaymentTemplateAction() { navigateTo("/views/commerce/payment-template-view.fxml"); }
    @FXML private void handleHelpTemplateAction() { navigateTo("/views/general/help-view.fxml"); }
    @FXML private void handleBulkGenerationAction() { navigateTo("/views/commerce/bulk-generation.fxml"); }
    @FXML private void handlePaymentAttachmentAction() { navigateTo("/views/commerce/payment-attachment-view.fxml"); }
    @FXML private void handleSettingsAction() { navigateTo("/views/settings-view.fxml"); }
    @FXML private void handleHomeReportingScreen() { navigateTo("/views/reporting/reporting-nav-dashboard-view.fxml"); }
    @FXML private void handleContactReportingScreen() { navigateTo("/views/reporting/contacts-report.fxml"); }
    @FXML private void handleUnderagedReportingScreen() { navigateTo("/views/reporting/underaged-report.fxml"); }
    @FXML private void handleWorkshopReportingScreen() { navigateTo("/views/reporting/workshops-report.fxml"); }

    private void loadOrganizationName() {
        try {
            Optional<Organization> organization = organizationDAO.getFirst();
            if (organization.isPresent()) {
                Organization org = organization.get();
                String orgName = org.getName();
                System.out.println("Organization found: " + orgName);
                if (userProfileButton != null) {
                    userProfileButton.setText(orgName);
                }
                loadOrganizationImage(org);
            } else {
                if (userProfileButton != null) {
                    userProfileButton.setText("No Organization");
                }
                setDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Error loading organization data: " + e.getMessage());
            if (userProfileButton != null) {
                userProfileButton.setText("Error Loading");
            }
            setDefaultAvatar();
        }
    }

    private void loadOrganizationImage(Organization organization) {
        if (userAvatar == null) return;

        if (organization.getImage() != null && organization.getImage().length > 0) {
            try {
                javafx.scene.image.Image orgImage = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(organization.getImage())
                );
                if (!orgImage.isError()) {
                    javafx.scene.paint.ImagePattern imagePattern = new javafx.scene.paint.ImagePattern(orgImage);
                    userAvatar.setFill(imagePattern);
                } else {
                    setDefaultAvatar();
                }
            } catch (Exception e) {
                setDefaultAvatar();
            }
        } else {
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        if (userAvatar != null) {
            userAvatar.setFill(javafx.scene.paint.Color.web("#0099cc"));
        }
    }

    public void refreshOrganizationData() {
        loadOrganizationName();
    }
}