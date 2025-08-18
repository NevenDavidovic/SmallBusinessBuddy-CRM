package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.utilities.LanguageManager;
import smallbusinessbuddycrm.MainController;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    // Header elements
    @FXML private Label welcomeTitle;
    @FXML private Label welcomeSubtitle;

    // Feature cards
    @FXML private Label crmCardTitle;
    @FXML private Label crmCardDescription;
    @FXML private Button crmButton;

    @FXML private Label marketingCardTitle;
    @FXML private Label marketingCardDescription;
    @FXML private Button marketingButton;

    @FXML private Label reportingCardTitle;
    @FXML private Label reportingCardDescription;
    @FXML private Button reportingButton;

    // Quick actions
    @FXML private Label quickActionsTitle;
    @FXML private Button addContactButton;
    @FXML private Button createWorkshopButton;
    @FXML private Button viewSettingsButton;

    // Organization info
    @FXML private Label organizationLabel;
    @FXML private Label organizationName;

    private LanguageManager languageManager;
    private Runnable languageChangeListener;
    private OrganizationDAO organizationDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageManager = LanguageManager.getInstance();
        organizationDAO = new OrganizationDAO();

        // Setup language change listener
        languageChangeListener = this::updateTexts;
        languageManager.addLanguageChangeListener(languageChangeListener);

        // Load organization data
        loadOrganizationInfo();

        // Initialize translations
        updateTexts();

        System.out.println("Welcome screen initialized");
    }

    private void updateTexts() {
        try {
            // Update header
            if (welcomeTitle != null) {
                welcomeTitle.setText(languageManager.getText("welcome.title"));
            }
            if (welcomeSubtitle != null) {
                welcomeSubtitle.setText(languageManager.getText("welcome.subtitle"));
            }

            // Update CRM card
            if (crmCardTitle != null) {
                crmCardTitle.setText(languageManager.getText("welcome.crm.title"));
            }
            if (crmCardDescription != null) {
                crmCardDescription.setText(languageManager.getText("welcome.crm.description"));
            }
            if (crmButton != null) {
                crmButton.setText(languageManager.getText("welcome.crm.button"));
            }

            // Update Marketing card
            if (marketingCardTitle != null) {
                marketingCardTitle.setText(languageManager.getText("welcome.marketing.title"));
            }
            if (marketingCardDescription != null) {
                marketingCardDescription.setText(languageManager.getText("welcome.marketing.description"));
            }
            if (marketingButton != null) {
                marketingButton.setText(languageManager.getText("welcome.marketing.button"));
            }

            // Update Reporting card
            if (reportingCardTitle != null) {
                reportingCardTitle.setText(languageManager.getText("welcome.reporting.title"));
            }
            if (reportingCardDescription != null) {
                reportingCardDescription.setText(languageManager.getText("welcome.reporting.description"));
            }
            if (reportingButton != null) {
                reportingButton.setText(languageManager.getText("welcome.reporting.button"));
            }

            // Update Quick Actions
            if (quickActionsTitle != null) {
                quickActionsTitle.setText(languageManager.getText("welcome.quick.actions"));
            }
            if (addContactButton != null) {
                addContactButton.setText(languageManager.getText("welcome.add.contact"));
            }
            if (createWorkshopButton != null) {
                createWorkshopButton.setText(languageManager.getText("welcome.create.workshop"));
            }
            if (viewSettingsButton != null) {
                viewSettingsButton.setText(languageManager.getText("welcome.settings"));
            }

            // Update Organization section
            if (organizationLabel != null) {
                organizationLabel.setText(languageManager.getText("welcome.organization.label"));
            }

            System.out.println("Welcome screen texts updated for: " +
                    (languageManager.isEnglish() ? "English" : "Croatian"));

        } catch (Exception e) {
            System.err.println("Error updating welcome screen texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadOrganizationInfo() {
        try {
            Optional<Organization> organization = organizationDAO.getFirst();
            if (organization.isPresent()) {
                Organization org = organization.get();
                if (organizationName != null) {
                    organizationName.setText(org.getName());
                }
            } else {
                if (organizationName != null) {
                    organizationName.setText(languageManager.getText("welcome.organization.none"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading organization info: " + e.getMessage());
            if (organizationName != null) {
                organizationName.setText(languageManager.getText("welcome.organization.error"));
            }
        }
    }

    // Navigation methods - these will be called by the main controller
    @FXML
    private void handleCRMAction() {
        navigateToView("/views/crm/contacts-view.fxml");
    }

    @FXML
    private void handleMarketingAction() {
        navigateToView("/views/marketing/email-builder.fxml");
    }

    @FXML
    private void handleReportingAction() {
        navigateToView("/views/reporting/reporting-nav-dashboard-view.fxml");
    }

    @FXML
    private void handleAddContactAction() {
        navigateToView("/views/crm/contacts-view.fxml");
    }

    @FXML
    private void handleCreateWorkshopAction() {
        navigateToView("/views/crm/workshops-view.fxml");
    }

    @FXML
    private void handleSettingsAction() {
        navigateToView("/views/settings-view.fxml");
    }

    private void navigateToView(String fxmlPath) {
        try {
            // Get the main controller and navigate
            MainController mainController = getMainController();
            if (mainController != null) {
                mainController.navigateTo(fxmlPath);
            }
        } catch (Exception e) {
            System.err.println("Error navigating to view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private MainController getMainController() {
        // This is a simple way to get the main controller
        // In a more complex app, you might use dependency injection or event bus
        try {
            return (MainController) welcomeTitle.getScene().getWindow().getUserData();
        } catch (Exception e) {
            System.err.println("Could not get main controller");
            return null;
        }
    }

    public void cleanup() {
        if (languageManager != null && languageChangeListener != null) {
            languageManager.removeLanguageChangeListener(languageChangeListener);
            System.out.println("WelcomeController: Language change listener removed");
        }
    }
}