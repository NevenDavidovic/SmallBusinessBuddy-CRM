package smallbusinessbuddycrm.utilities;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.concurrent.CompletableFuture;

public class LoadingManager {

    private static LoadingManager instance;
    private StackPane loadingOverlay;
    private Label loadingText;
    private StackPane rootContainer;

    private LoadingManager() {
        setupLoadingOverlay();
    }

    public static LoadingManager getInstance() {
        if (instance == null) {
            instance = new LoadingManager();
        }
        return instance;
    }

    public void initialize(StackPane rootContainer) {
        this.rootContainer = rootContainer;

        if (!rootContainer.getChildren().contains(loadingOverlay)) {
            rootContainer.getChildren().add(loadingOverlay);
        }

        System.out.println("✅ LoadingManager initialized");
    }

    private void setupLoadingOverlay() {
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        loadingOverlay.setVisible(false);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(80, 80);
        progressIndicator.setStyle("-fx-progress-color: #2196F3;");

        loadingText = new Label("Loading...");
        loadingText.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold;"
        );

        VBox loadingContent = new VBox(20);
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.getChildren().addAll(progressIndicator, loadingText);

        loadingOverlay.getChildren().add(loadingContent);
    }

    public void showLoading() {
        showLoading("Loading...");
    }

    public void showLoading(String message) {
        Platform.runLater(() -> {
            if (loadingText != null) {
                loadingText.setText(message);
            }

            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
                loadingOverlay.toFront();
            }
        });
    }

    public void hideLoading() {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(false);
            }
        });
    }

    public boolean isLoading() {
        return loadingOverlay != null && loadingOverlay.isVisible();
    }

    public <T> void withLoading(String message, CompletableFuture<T> future) {
        showLoading(message);

        future.whenComplete((result, throwable) -> {
            hideLoading();
        });
    }
}