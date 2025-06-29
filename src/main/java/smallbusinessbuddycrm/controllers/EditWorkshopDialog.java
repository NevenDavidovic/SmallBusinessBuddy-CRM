package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.model.Workshop;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EditWorkshopDialog {

    private Stage dialogStage;
    private Workshop workshop;
    private boolean okClicked = false;

    // Workshop form fields - matching your current Workshop model
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;

    // Error labels for validation
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    public EditWorkshopDialog(Stage parentStage, Workshop workshop) {
        this.workshop = workshop;
        createDialogStage();
        dialogStage.initOwner(parentStage);
        populateFields();
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Edit Workshop");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Edit Workshop");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form
        VBox workshopSection = createWorkshopSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

        Scene scene = new Scene(mainLayout, 450, 300);
        dialogStage.setScene(scene);
    }

    private VBox createWorkshopSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Workshop Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Workshop Name
        grid.add(new Label("Workshop Name *:"), 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText("Enter workshop name...");
        grid.add(nameField, 1, row++);

        // Name error label
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

        // From Date
        grid.add(new Label("From Date *:"), 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

        // From date error label
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

        // To Date
        grid.add(new Label("To Date *:"), 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

        // To date error label
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

        // Add date validation listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
            validateForm();
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Real-time validation for name
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private void populateFields() {
        if (workshop != null) {
            nameField.setText(workshop.getName() != null ? workshop.getName() : "");
            fromDatePicker.setValue(workshop.getFromDate());
            toDatePicker.setValue(workshop.getToDate());
        }
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        Button updateButton = new Button("Update");
        updateButton.setPrefWidth(80);
        updateButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        updateButton.setOnAction(e -> handleUpdate());

        buttonBox.getChildren().addAll(cancelButton, updateButton);
        return buttonBox;
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Clear all error messages
        nameErrorLabel.setVisible(false);
        fromDateErrorLabel.setVisible(false);
        toDateErrorLabel.setVisible(false);

        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Workshop name is required");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate dates
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            fromDateErrorLabel.setText("From date is required");
            fromDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (toDate == null) {
            toDateErrorLabel.setText("To date is required");
            toDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            toDateErrorLabel.setText("To date must be after from date");
            toDateErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    private void handleUpdate() {
        if (validateInput()) {
            try {
                updateWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.updateWorkshop(workshop);

                if (success) {
                    okClicked = true;
                    dialogStage.close();

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Workshop Updated");
                    successAlert.setContentText("Workshop '" + workshop.getName() + "' has been updated successfully!");
                    successAlert.showAndWait();
                } else {
                    showErrorAlert("Failed to update workshop in database.");
                }

            } catch (Exception e) {
                showErrorAlert("Error updating workshop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Workshop name is required\n");
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            errors.append("- From date is required\n");
        }

        if (toDate == null) {
            errors.append("- To date is required\n");
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            errors.append("- To date must be after from date\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void updateWorkshopFromInput() {
        // Update the existing workshop object with form data
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

        // Update the timestamp
        workshop.setUpdatedAt(LocalDateTime.now().toString());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialogStage.showAndWait();
        return okClicked;
    }
}