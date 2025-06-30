package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class EditColumnsDialog {

    private Stage dialogStage;
    private boolean okClicked = false;
    private Map<String, CheckBox> columnCheckBoxes = new HashMap<>();
    private Map<String, Boolean> columnVisibility = new HashMap<>();

    public EditColumnsDialog(Stage parentStage, Map<String, Boolean> currentVisibility) {
        System.out.println("EditColumnsDialog created with visibility: " + currentVisibility);

        // Initialize our map with current values
        this.columnVisibility.putAll(currentVisibility);

        createDialogStage();
        dialogStage.initOwner(parentStage);
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Edit Columns");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Handle window close button (X) - apply changes instead of canceling
        dialogStage.setOnCloseRequest(e -> {
            System.out.println("Dialog closed via X button - applying changes");
            e.consume(); // Prevent default close
            handleApply(); // Apply changes before closing
        });

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Choose Columns to Display");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Instructions
        Label instructionLabel = new Label("Check the columns you want to show in the table:");
        instructionLabel.setStyle("-fx-text-fill: #666666;");

        // Note about always visible columns
        Label noteLabel = new Label("Note: Checkbox and Edit columns are always visible");
        noteLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px; -fx-font-style: italic;");

        // Column checkboxes
        VBox checkboxContainer = createColumnCheckboxes();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, instructionLabel, noteLabel, checkboxContainer, buttonBox);

        Scene scene = new Scene(mainLayout, 380, 580);

        // Add keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    System.out.println("ESC pressed - cancelling dialog");
                    okClicked = false;
                    dialogStage.close();
                    break;
                case ENTER:
                    System.out.println("ENTER pressed - applying changes");
                    handleApply();
                    break;
            }
        });

        dialogStage.setScene(scene);
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

            CheckBox checkBox = new CheckBox(columnName);
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
        Button selectAllButton = new Button("Select All");
        Button deselectAllButton = new Button("Deselect All");
        Button resetButton = new Button("Reset to Default");

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

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> {
            System.out.println("Cancel button clicked - no changes applied");
            okClicked = false;
            dialogStage.close();
        });

        Button applyButton = new Button("Apply & Close");
        applyButton.setPrefWidth(100);
        applyButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        applyButton.setOnAction(e -> handleApply());

        // Make Apply button default (responds to Enter key)
        applyButton.setDefaultButton(true);
        // Make Cancel button cancel (responds to Escape key)
        cancelButton.setCancelButton(true);

        buttonBox.getChildren().addAll(cancelButton, applyButton);
        return buttonBox;
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

    public boolean showAndWait() {
        System.out.println("Showing dialog...");
        dialogStage.showAndWait();
        System.out.println("Dialog closed, okClicked=" + okClicked);
        return okClicked;
    }

    public Map<String, Boolean> getColumnVisibility() {
        System.out.println("Returning column visibility: " + columnVisibility);
        return new HashMap<>(columnVisibility); // Return a copy to avoid reference issues
    }
}