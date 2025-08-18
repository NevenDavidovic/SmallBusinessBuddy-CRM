package smallbusinessbuddycrm.controllers.contact;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.util.HashMap;
import java.util.Map;

public class EditColumnsDialog {

    private Stage dialogStage;
    private boolean okClicked = false;
    private Map<String, CheckBox> columnCheckBoxes = new HashMap<>();
    private Map<String, Boolean> columnVisibility = new HashMap<>();

    // UI Labels for translation
    private Label titleLabel;
    private Label instructionLabel;
    private Label noteLabel;

    // Buttons
    private Button selectAllButton;
    private Button deselectAllButton;
    private Button resetButton;
    private Button cancelButton;
    private Button applyButton;

    // ⭐ NEW: Language change listener for automatic updates
    private Runnable languageChangeListener;
    private LanguageManager languageManager;

    public EditColumnsDialog(Stage parentStage, Map<String, Boolean> currentVisibility) {
        System.out.println("EditColumnsDialog created with visibility: " + currentVisibility);

        // ⭐ NEW: Initialize language manager and listener
        this.languageManager = LanguageManager.getInstance();
        this.languageChangeListener = this::updateTexts;

        // Initialize our map with current values
        this.columnVisibility.putAll(currentVisibility);

        createDialogStage();
        dialogStage.initOwner(parentStage);

        // ⭐ NEW: Register for language change notifications
        languageManager.addLanguageChangeListener(languageChangeListener);

        updateTexts(); // Initial translation
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(true); // Allow resizing for better scroll experience

        // ⭐ NEW: Enhanced close handler - cleanup language listener
        dialogStage.setOnCloseRequest(e -> {
            System.out.println("Dialog closed via X button - applying changes and cleaning up");
            e.consume(); // Prevent default close
            handleApply(); // Apply changes before closing
            cleanup(); // Clean up language listener
        });

        // Create main content layout
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));

        // Title
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Instructions
        instructionLabel = new Label();
        instructionLabel.setStyle("-fx-text-fill: #666666;");

        // Note about always visible columns
        noteLabel = new Label();
        noteLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px; -fx-font-style: italic;");

        // Column checkboxes
        VBox checkboxContainer = createColumnCheckboxes();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainContent.getChildren().addAll(titleLabel, instructionLabel, noteLabel, checkboxContainer, buttonBox);

        // Create scroll pane and wrap the main content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scroll
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll as needed
        scrollPane.setPannable(true); // Allow panning with mouse drag

        // Set scroll speed (optional - makes scrolling smoother)
        scrollPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 2; // Multiply for faster scrolling
            double scrollSpeed = scrollPane.getVvalue() - (deltaY / scrollPane.getContent().getBoundsInLocal().getHeight());
            scrollPane.setVvalue(scrollSpeed);
        });

        // Create scene with scroll pane as root
        Scene scene = new Scene(scrollPane, 400, 500); // Smaller height to demonstrate scrolling

        // Add keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    System.out.println("ESC pressed - cancelling dialog");
                    okClicked = false;
                    cleanup(); // ⭐ NEW: Clean up on cancel too
                    dialogStage.close();
                    break;
                case ENTER:
                    System.out.println("ENTER pressed - applying changes");
                    handleApply();
                    cleanup(); // ⭐ NEW: Clean up on apply
                    break;
            }
        });

        dialogStage.setScene(scene);

        // Set minimum size for better user experience
        dialogStage.setMinWidth(380);
        dialogStage.setMinHeight(400);
    }

    private VBox createColumnCheckboxes() {
        VBox container = new VBox(10);
        container.setStyle("-fx-border-color: #dfe3eb; -fx-border-radius: 5; -fx-padding: 15;");

        // Define all available columns - MUST MATCH EXACTLY with controller
        String[] columns = {
                "First Name", "Last Name", "Birthday", "Age", "PIN",
                "Email", "Phone Number", "Street Name", "Street Number",
                "Postal Code", "City", "Member Status", "Member Since",
                "Member Until", "Created", "Updated"
        };

        System.out.println("Creating checkboxes for columns:");
        for (String columnName : columns) {
            boolean isVisible = columnVisibility.getOrDefault(columnName, true);
            System.out.println("  " + columnName + " -> " + isVisible);

            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(isVisible);
            checkBox.setStyle("-fx-font-size: 13px;");
            columnCheckBoxes.put(columnName, checkBox);
            container.getChildren().add(checkBox);
        }

        // Add separator and utility buttons
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0 10 0;");
        container.getChildren().add(separator);

        HBox utilityButtons = new HBox(10);
        selectAllButton = new Button();
        deselectAllButton = new Button();
        resetButton = new Button();

        selectAllButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
        deselectAllButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px;");
        resetButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 11px;");

        selectAllButton.setOnAction(e -> {
            System.out.println("Select All clicked");
            for (CheckBox cb : columnCheckBoxes.values()) {
                cb.setSelected(true);
            }
        });

        deselectAllButton.setOnAction(e -> {
            System.out.println("Deselect All clicked");
            for (CheckBox cb : columnCheckBoxes.values()) {
                cb.setSelected(false);
            }
        });

        resetButton.setOnAction(e -> {
            System.out.println("Reset to Default clicked");
            // Reset to default visibility (show essential columns, hide some optional ones)
            for (Map.Entry<String, CheckBox> entry : columnCheckBoxes.entrySet()) {
                String columnName = entry.getKey();
                CheckBox checkBox = entry.getValue();

                // Default visible columns (PIN is hidden by default for privacy)
                boolean defaultVisible = !columnName.equals("PIN") &&
                        !columnName.equals("Street Number") &&
                        !columnName.equals("Member Since") &&
                        !columnName.equals("Member Until") &&
                        !columnName.equals("Created") &&
                        !columnName.equals("Updated");

                checkBox.setSelected(defaultVisible);
                System.out.println("  Reset " + columnName + " to: " + defaultVisible);
            }
        });

        utilityButtons.getChildren().addAll(selectAllButton, deselectAllButton, resetButton);
        container.getChildren().add(utilityButtons);

        return container;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        cancelButton = new Button();
        cancelButton.setPrefWidth(80);
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> {
            System.out.println("Cancel button clicked - no changes applied");
            okClicked = false;
            cleanup(); // ⭐ NEW: Clean up on cancel
            dialogStage.close();
        });

        applyButton = new Button();
        applyButton.setPrefWidth(100);
        applyButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        applyButton.setOnAction(e -> {
            handleApply();
            cleanup(); // ⭐ NEW: Clean up on apply
        });

        // Make Apply button default (responds to Enter key)
        applyButton.setDefaultButton(true);
        // Make Cancel button cancel (responds to Escape key)
        cancelButton.setCancelButton(true);

        buttonBox.getChildren().addAll(cancelButton, applyButton);
        return buttonBox;
    }

    // ⭐ NEW: Enhanced updateTexts method with better error handling
    private void updateTexts() {
        try {
            System.out.println("Updating EditColumnsDialog texts for language: " +
                    (languageManager.isEnglish() ? "English" : "Croatian"));

            // Update dialog title
            if (dialogStage != null) {
                dialogStage.setTitle(languageManager.getText("edit.columns.dialog.title"));
            }

            // Update main labels
            if (titleLabel != null) titleLabel.setText(languageManager.getText("edit.columns.title"));
            if (instructionLabel != null) instructionLabel.setText(languageManager.getText("edit.columns.instruction"));
            if (noteLabel != null) noteLabel.setText(languageManager.getText("edit.columns.note"));

            // Update buttons
            if (selectAllButton != null) selectAllButton.setText(languageManager.getText("edit.columns.select.all"));
            if (deselectAllButton != null) deselectAllButton.setText(languageManager.getText("edit.columns.deselect.all"));
            if (resetButton != null) resetButton.setText(languageManager.getText("edit.columns.reset.default"));
            if (cancelButton != null) cancelButton.setText(languageManager.getText("button.cancel"));
            if (applyButton != null) applyButton.setText(languageManager.getText("edit.columns.apply.close"));

            // Update column checkboxes
            updateColumnCheckboxTexts();

            System.out.println("EditColumnsDialog text update completed successfully");

        } catch (Exception e) {
            System.err.println("Error updating EditColumnsDialog texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateColumnCheckboxTexts() {
        // Map of internal column names to translation keys
        Map<String, String> columnTranslationKeys = new HashMap<>();
        columnTranslationKeys.put("First Name", "edit.columns.column.first.name");
        columnTranslationKeys.put("Last Name", "edit.columns.column.last.name");
        columnTranslationKeys.put("Birthday", "edit.columns.column.birthday");
        columnTranslationKeys.put("Age", "edit.columns.column.age");
        columnTranslationKeys.put("PIN", "edit.columns.column.pin");
        columnTranslationKeys.put("Email", "edit.columns.column.email");
        columnTranslationKeys.put("Phone Number", "edit.columns.column.phone");
        columnTranslationKeys.put("Street Name", "edit.columns.column.street.name");
        columnTranslationKeys.put("Street Number", "edit.columns.column.street.number");
        columnTranslationKeys.put("Postal Code", "edit.columns.column.postal.code");
        columnTranslationKeys.put("City", "edit.columns.column.city");
        columnTranslationKeys.put("Member Status", "edit.columns.column.member.status");
        columnTranslationKeys.put("Member Since", "edit.columns.column.member.since");
        columnTranslationKeys.put("Member Until", "edit.columns.column.member.until");
        columnTranslationKeys.put("Created", "edit.columns.column.created");
        columnTranslationKeys.put("Updated", "edit.columns.column.updated");

        // Update checkbox labels
        for (Map.Entry<String, CheckBox> entry : columnCheckBoxes.entrySet()) {
            String columnName = entry.getKey();
            CheckBox checkBox = entry.getValue();
            String translationKey = columnTranslationKeys.get(columnName);

            if (translationKey != null) {
                checkBox.setText(languageManager.getText(translationKey));
            } else {
                // Fallback to original name if translation key not found
                checkBox.setText(columnName);
                System.err.println("No translation key found for column: " + columnName);
            }
        }
    }

    private void handleApply() {
        System.out.println("Apply button clicked - updating column visibility");

        // Clear and rebuild the visibility map
        columnVisibility.clear();

        // Update column visibility based on checkboxes
        for (Map.Entry<String, CheckBox> entry : columnCheckBoxes.entrySet()) {
            String columnName = entry.getKey();
            boolean isSelected = entry.getValue().isSelected();
            columnVisibility.put(columnName, isSelected);
            System.out.println("Column '" + columnName + "' set to: " + isSelected);
        }

        okClicked = true;
        System.out.println("Dialog closing with OK=true, final visibility: " + columnVisibility);
        dialogStage.close();
    }

    // ⭐ NEW: Cleanup method to prevent memory leaks
    private void cleanup() {
        if (languageManager != null && languageChangeListener != null) {
            languageManager.removeLanguageChangeListener(languageChangeListener);
            System.out.println("EditColumnsDialog: Language change listener removed");
        }
    }

    public boolean showAndWait() {
        updateTexts(); // Update translations before showing
        System.out.println("Showing dialog...");
        dialogStage.showAndWait();
        System.out.println("Dialog closed, okClicked=" + okClicked);

        // ⭐ NEW: Ensure cleanup happens even if dialog is closed unexpectedly
        cleanup();

        return okClicked;
    }

    public Map<String, Boolean> getColumnVisibility() {
        System.out.println("Returning column visibility: " + columnVisibility);
        return new HashMap<>(columnVisibility); // Return a copy to avoid reference issues
    }

    // ⭐ NEW: Optional manual cleanup method for external use
    public void dispose() {
        cleanup();
    }
}