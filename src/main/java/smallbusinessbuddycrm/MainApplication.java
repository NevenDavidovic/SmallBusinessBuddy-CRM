package smallbusinessbuddycrm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/smallbusinessbuddycrm/main-view.fxml"));
        Parent root = loader.load();

        // Get the controller
        MainController controller = loader.getController();

        // Create and set the scene
        Scene scene = new Scene(root, 1300, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/main-style.css").toExternalForm());

        // Configure and show the stage
        primaryStage.setTitle("Small Business Buddy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}