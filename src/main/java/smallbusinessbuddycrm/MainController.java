package smallbusinessbuddycrm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private TitledPane bookmarks;
    @FXML private MenuButton userProfileButton;
    @FXML private Circle userAvatar;

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

    // Menu items (NEW)
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

    // Reporting section buttons (NEW)
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

    // Remove this - not needed anymore
    @FXML private Button seeMoreButton;

    private OrganizationDAO organizationDAO = new OrganizationDAO();
    private LanguageManager languageManager;

    @FXML
    public void initialize() {
        languageManager = LanguageManager.getInstance();

        loadOrganizationName();
        updateLanguageButtons();
        updateAllTexts(); // Add this to translate on startup
    }

    @FXML
    private void switchToEnglish() {
        languageManager.setLanguage("en");
        updateLanguageButtons();
        updateAllTexts();
        System.out.println("Switched to English");
    }

    @FXML
    private void switchToCroatian() {
        languageManager.setLanguage("hr");
        updateLanguageButtons();
        updateAllTexts();
        System.out.println("Prebačeno na hrvatski");
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
            System.out.println("Navigation texts updated for language: " + currentLang);
        } catch (Exception e) {
            System.err.println("Error updating navigation texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCurrentViewTexts() {
        System.out.println("Current view texts update requested");
    }

    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // Keep all your existing organization methods exactly as they are
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