package smallbusinessbuddycrm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private TitledPane bookmarks;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        // You can set up initial view or configuration here

        // If you want to expand a specific accordion pane by default
        // For example, to expand the CRM section:
        // crmPane.setExpanded(true);
    }

    /**
     * Navigates to a new view by loading it into the content area
     * while keeping the sidebar and top navigation intact.
     *
     * @param fxmlPath The path to the FXML file to load
     */
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Clear current content and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately in your application
        }
    }

    // Handler methods for sidebar buttons

    @FXML
    private void handleContactsAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/contacts_view.fxml");
    }

    @FXML
    private void handleCompaniesAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/companies_view.fxml");
    }

    @FXML
    private void handleDealsAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/deals_view.fxml");
    }

    // Add more handlers for other menu items

    @FXML
    private void handleSettingsAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/settings_view.fxml");
    }

    @FXML
    private void handleProfileAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/profile_view.fxml");
    }

    @FXML
    private void handleLogoutAction() {
        // Handle logout logic
        System.out.println("Logout requested");
    }
}