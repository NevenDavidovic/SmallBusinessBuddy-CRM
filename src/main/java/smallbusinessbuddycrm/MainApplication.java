package smallbusinessbuddycrm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.controllers.organization.OrganizationSetupDialog;

import java.util.Optional;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database and tables before loading GUI
        smallbusinessbuddycrm.database.DatabaseConnection.initializeDatabase();

        // Check if organization exists, if not show setup dialog
        checkAndSetupOrganization(primaryStage);

        // Load the main FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
        Parent root = loader.load();

        // Get the controller
        MainController controller = loader.getController();

        // Create and set the scene
        Scene scene = new Scene(root, 1300, 700);

        // Handle CSS file not found gracefully
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/main-style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styling");
        }

        // Configure and show the stage
        primaryStage.setTitle("Small Business Buddy - CRM");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private void checkAndSetupOrganization(Stage parentStage) {
        OrganizationDAO organizationDAO = new OrganizationDAO();
        Optional<Organization> existingOrg = organizationDAO.getFirst();

        if (existingOrg.isEmpty()) {
            // No organization exists, show setup dialog
            OrganizationSetupDialog setupDialog = new OrganizationSetupDialog();
            Optional<Organization> result = setupDialog.showAndWait(parentStage);

            if (result.isPresent()) {
                // Save the organization to database
                Organization newOrg = result.get();
                boolean saved = organizationDAO.save(newOrg);

                if (!saved) {
                    System.err.println("Failed to save organization to database!");
                    // If save fails, exit the app
                    javafx.application.Platform.exit();
                }
            } else {
                // User cancelled setup, exit the app
                System.out.println("No organization created, exiting application.");
                javafx.application.Platform.exit();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}