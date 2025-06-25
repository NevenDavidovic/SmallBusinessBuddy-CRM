package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.database.ContactDAO;

import java.time.LocalDate;

public class CreateContactDialog {

    private Stage dialogStage;
    private Contact result = null;
    private boolean okClicked = false;

    // Form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField streetNameField;
    private TextField streetNumField;
    private TextField postalCodeField;
    private TextField cityField;
    private CheckBox memberCheckBox;
    private DatePicker memberSincePicker;
    private DatePicker memberUntilPicker;

    public CreateContactDialog(Stage parentStage) {
        createDialogStage();
        dialogStage.initOwner(parentStage);  // This makes it a proper modal dialog
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Create New Contact");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Create New Contact");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Form fields
        GridPane formGrid = createFormGrid();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, formGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 600);
        dialogStage.setScene(scene);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // First Name
        grid.add(new Label("First Name:"), 0, row);
        firstNameField = new TextField();
        firstNameField.setPrefWidth(250);
        grid.add(firstNameField, 1, row++);

        // Last Name
        grid.add(new Label("Last Name:"), 0, row);
        lastNameField = new TextField();
        grid.add(lastNameField, 1, row++);

        // Email
        grid.add(new Label("Email:"), 0, row);
        emailField = new TextField();
        grid.add(emailField, 1, row++);

        // Phone
        grid.add(new Label("Phone Number:"), 0, row);
        phoneField = new TextField();
        grid.add(phoneField, 1, row++);

        // Street Name
        grid.add(new Label("Street Name:"), 0, row);
        streetNameField = new TextField();
        grid.add(streetNameField, 1, row++);

        // Street Number
        grid.add(new Label("Street Number:"), 0, row);
        streetNumField = new TextField();
        grid.add(streetNumField, 1, row++);

        // Postal Code
        grid.add(new Label("Postal Code:"), 0, row);
        postalCodeField = new TextField();
        grid.add(postalCodeField, 1, row++);

        // City
        grid.add(new Label("City:"), 0, row);
        cityField = new TextField();
        grid.add(cityField, 1, row++);

        // Member Status
        grid.add(new Label("Member:"), 0, row);
        memberCheckBox = new CheckBox("Is Member");
        grid.add(memberCheckBox, 1, row++);

        // Member Since
        grid.add(new Label("Member Since:"), 0, row);
        memberSincePicker = new DatePicker();
        memberSincePicker.setDisable(true);
        grid.add(memberSincePicker, 1, row++);

        // Member Until
        grid.add(new Label("Member Until:"), 0, row);
        memberUntilPicker = new DatePicker();
        memberUntilPicker.setDisable(true);
        grid.add(memberUntilPicker, 1, row++);

        // Enable/disable date pickers based on member checkbox
        memberCheckBox.setOnAction(e -> {
            boolean isMember = memberCheckBox.isSelected();
            memberSincePicker.setDisable(!isMember);
            memberUntilPicker.setDisable(!isMember);
            if (!isMember) {
                memberSincePicker.setValue(null);
                memberUntilPicker.setValue(null);
            }
        });

        return grid;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(80);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    private void handleSave() {
        if (validateInput()) {
            try {
                Contact newContact = createContactFromInput();
                ContactDAO dao = new ContactDAO();

                boolean success = dao.createContact(newContact);

                if (success) {
                    result = newContact;
                    okClicked = true;
                    dialogStage.close();
                } else {
                    showErrorAlert("Failed to save contact to database.");
                }

            } catch (Exception e) {
                showErrorAlert("Error saving contact: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("- First name is required\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("- Last name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- Please enter a valid email address\n");
        }

        // Member since/until are NOT mandatory - no validation needed
        // isMember is a checkbox - no validation needed

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

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private Contact createContactFromInput() {
        Contact contact = new Contact();

        contact.setFirstName(firstNameField.getText().trim());
        contact.setLastName(lastNameField.getText().trim());
        contact.setEmail(emailField.getText().trim());
        contact.setPhoneNum(phoneField.getText().trim());
        contact.setStreetName(streetNameField.getText().trim());
        contact.setStreetNum(streetNumField.getText().trim());
        contact.setPostalCode(postalCodeField.getText().trim());
        contact.setCity(cityField.getText().trim());
        contact.setMember(memberCheckBox.isSelected());

        if (memberCheckBox.isSelected()) {
            contact.setMemberSince(memberSincePicker.getValue());
            contact.setMemberUntil(memberUntilPicker.getValue());
        }

        // Set timestamps
        String now = java.time.LocalDateTime.now().toString();
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        return contact;
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

    public Contact getResult() {
        return result;
    }
}