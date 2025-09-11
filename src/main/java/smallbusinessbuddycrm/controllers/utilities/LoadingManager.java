package smallbusinessbuddycrm.utilities;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.concurrent.CompletableFuture;

/**
 * Singleton utility class for managing loading overlays in JavaFX applications.
 *
 * This class provides a centralized solution for displaying loading indicators with customizable
 * messages during long-running operations. It creates a semi-transparent overlay with a progress
 * indicator and text that can be shown/hidden as needed throughout the application.
 *
 * Key Features:
 * - Singleton Pattern: Ensures single instance across the application for consistent loading state
 * - Thread-Safe Operations: All UI updates use Platform.runLater for JavaFX thread safety
 * - Customizable Messages: Support for custom loading text or default "Loading..." message
 * - Overlay Management: Semi-transparent background that blocks user interaction during loading
 * - Async Integration: Built-in support for CompletableFuture operations with automatic cleanup
 * - Professional UI: Styled progress indicator and text with modern visual design
 *
 * Usage Examples:
 *
 * Basic usage:
 * <pre>
 * LoadingManager.getInstance().initialize(rootStackPane);
 * LoadingManager.getInstance().showLoading("Saving data...");
 * // ... perform operation ...
 * LoadingManager.getInstance().hideLoading();
 * </pre>
 *
 * With CompletableFuture:
 * <pre>
 * CompletableFuture&lt;Void&gt; saveOperation = CompletableFuture.runAsync(() -> {
 *     // Long running operation
 * });
 * LoadingManager.getInstance().withLoading("Saving...", saveOperation);
 * </pre>
 *
 * Architecture:
 * - Uses StackPane overlay for full-screen coverage
 * - VBox layout for centered progress indicator and text
 * - Platform.runLater ensures all UI updates are thread-safe
 * - CompletableFuture integration provides clean async operation management
 *
 * The LoadingManager must be initialized with a root StackPane container before use.
 * The overlay will be added to this container and managed automatically.
 *
 * Thread Safety:
 * All public methods are thread-safe and can be called from any thread. UI updates
 * are automatically dispatched to the JavaFX Application Thread using Platform.runLater.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class LoadingManager {

    // Singleton instance
    private static LoadingManager instance;

    // UI Components
    private StackPane loadingOverlay;
    private Label loadingText;
    private StackPane rootContainer;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the loading overlay components and sets up the UI structure.
     */
    private LoadingManager() {
        setupLoadingOverlay();
    }

    /**
     * Returns the singleton instance of LoadingManager.
     * Creates a new instance if one doesn't exist, ensuring thread-safe singleton access.
     *
     * @return The single LoadingManager instance
     */
    public static LoadingManager getInstance() {
        if (instance == null) {
            instance = new LoadingManager();
        }
        return instance;
    }

    /**
     * Initializes the LoadingManager with the root container for overlay display.
     * Sets up the loading overlay within the provided root container and ensures
     * it's properly added to the scene graph for display management.
     *
     * @param rootContainer The main StackPane container where loading overlay will be displayed
     */
    public void initialize(StackPane rootContainer) {
        this.rootContainer = rootContainer;

        if (!rootContainer.getChildren().contains(loadingOverlay)) {
            rootContainer.getChildren().add(loadingOverlay);
        }

        System.out.println("✅ LoadingManager initialized");
    }

    /**
     * Creates and configures the loading overlay UI components.
     * Sets up a semi-transparent background with progress indicator and text label,
     * arranges components in a centered vertical layout with appropriate styling.
     */
    private void setupLoadingOverlay() {
        // Create semi-transparent overlay background
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        loadingOverlay.setVisible(false);

        // Create and style progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(80, 80);
        progressIndicator.setStyle("-fx-progress-color: #2196F3;");

        // Create and style loading text label
        loadingText = new Label("Loading...");
        loadingText.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold;"
        );

        // Arrange components in vertical layout
        VBox loadingContent = new VBox(20);
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.getChildren().addAll(progressIndicator, loadingText);

        loadingOverlay.getChildren().add(loadingContent);
    }

    /**
     * Shows the loading overlay with default "Loading..." message.
     * Convenience method that calls showLoading(String) with default text.
     * Ensures thread-safe UI updates using Platform.runLater.
     */
    public void showLoading() {
        showLoading("Loading...");
    }

    /**
     * Shows the loading overlay with a custom loading message.
     * Updates the loading text and makes the overlay visible, bringing it to front.
     * Uses Platform.runLater to ensure thread-safe JavaFX UI updates.
     *
     * @param message The custom message to display while loading
     */
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

    /**
     * Hides the loading overlay and returns control to the application.
     * Makes the loading overlay invisible, allowing user interaction with the main UI.
     * Uses Platform.runLater to ensure thread-safe JavaFX UI updates.
     */
    public void hideLoading() {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(false);
            }
        });
    }

    /**
     * Checks if the loading overlay is currently visible.
     * Provides a way to query the current loading state without affecting the UI.
     *
     * @return true if loading overlay is currently visible, false otherwise
     */
    public boolean isLoading() {
        return loadingOverlay != null && loadingOverlay.isVisible();
    }

    /**
     * Convenience method for managing loading state during asynchronous operations.
     * Shows loading overlay with specified message, then automatically hides it
     * when the CompletableFuture completes (either successfully or with exception).
     *
     * @param <T> The type of result returned by the CompletableFuture
     * @param message The loading message to display during the operation
     * @param future The CompletableFuture representing the asynchronous operation
     */
    public <T> void withLoading(String message, CompletableFuture<T> future) {
        showLoading(message);

        future.whenComplete((result, throwable) -> {
            hideLoading();
        });
    }
}