package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Teacher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateEditTeacherDialog {

    private Stage dialog;
    private boolean result = false;
    private Teacher teacher;
    private boolean isEdit;

    // Form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;

    public CreateEditTeacherDialog(Stage parent, Teacher existingTeacher) {
        this.isEdit = existingTeacher != null;
        this.teacher = isEdit ? existingTeacher : new Teacher();
        createDialog(parent);

        if (isEdit) {
            populateFields();
        }
    }

    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEdit ? "Edit Teacher" : "Add New Teacher");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Label titleLabel = new Label(isEdit ? "Edit Teacher" : "Add New Teacher");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Form grid
        GridPane formGrid = createFormGrid();

        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(titleLabel, formGrid, buttonBox);

        Scene scene = new Scene(root, 420, 350);
        dialog.setScene(scene);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        // First Name
        Label firstNameLabel = new Label("First Name *");
        firstNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        firstNameField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        firstNameField.setPrefWidth(250);

        // Last Name
        Label lastNameLabel = new Label("Last Name *");
        lastNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        lastNameField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        lastNameField.setPrefWidth(250);

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        emailField.setPrefWidth(250);

        // Phone
        Label phoneLabel = new Label("Phone Number");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        phoneField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        phoneField.setPrefWidth(250);

        // Add to grid
        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(phoneLabel, 0, 3);
        grid.add(phoneField, 1, 3);

        return grid;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4; -fx-padding: 8 16;");
        cancelButton.setOnAction(e -> {
            result = false;
            dialog.close();
        });

        Button saveButton = new Button(isEdit ? "Update Teacher" : "Add Teacher");
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4; -fx-padding: 8 16;");
        saveButton.setOnAction(e -> handleSave());

        // Make save button default
        saveButton.setDefaultButton(true);

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    private void populateFields() {
        if (teacher != null) {
            firstNameField.setText(teacher.getFirstName() != null ? teacher.getFirstName() : "");
            lastNameField.setText(teacher.getLastName() != null ? teacher.getLastName() : "");
            emailField.setText(teacher.getEmail() != null ? teacher.getEmail() : "");
            phoneField.setText(teacher.getPhoneNum() != null ? teacher.getPhoneNum() : "");
        }
    }

    private void handleSave() {
        // Validate required fields
        if (!validateFields()) {
            return;
        }

        // Update teacher object
        teacher.setFirstName(firstNameField.getText().trim());
        teacher.setLastName(lastNameField.getText().trim());
        teacher.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
        teacher.setPhoneNum(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());

        // Set timestamps
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        if (!isEdit) {
            teacher.setCreatedAt(timestamp);
        }
        teacher.setUpdatedAt(timestamp);

        result = true;
        dialog.close();
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("• First name is required\n");
            highlightField(firstNameField, true);
        } else {
            highlightField(firstNameField, false);
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("• Last name is required\n");
            highlightField(lastNameField, true);
        } else {
            highlightField(lastNameField, false);
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            errors.append("• Please enter a valid email address\n");
            highlightField(emailField, true);
        } else {
            highlightField(emailField, false);
        }

        // Show errors if any
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void highlightField(TextField field, boolean isError) {
        if (isError) {
            field.setStyle("-fx-border-color: #dc3545; -fx-border-radius: 4; -fx-padding: 8; -fx-border-width: 2;");
        } else {
            field.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }

    public Teacher getTeacher() {
        return teacher;
    }
}