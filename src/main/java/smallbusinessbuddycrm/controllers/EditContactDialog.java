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

public class EditContactDialog {

    private Stage dialogStage;
    private Contact contact;
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

    public EditContactDialog(Stage parentStage, Contact contact) {
        this.contact = contact;
        createDialogStage();
        dialogStage.initOwner(parentStage);
        populateFields();
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Edit Contact");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Edit Contact");
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

    private void populateFields() {
        // Fill fields with existing contact data
        firstNameField.setText(contact.getFirstName() != null ? contact.getFirstName() : "");
        lastNameField.setText(contact.getLastName() != null ? contact.getLastName() : "");
        emailField.setText(contact.getEmail() != null ? contact.getEmail() : "");
        phoneField.setText(contact.getPhoneNum() != null ? contact.getPhoneNum() : "");
        streetNameField.setText(contact.getStreetName() != null ? contact.getStreetName() : "");
        streetNumField.setText(contact.getStreetNum() != null ? contact.getStreetNum() : "");
        postalCodeField.setText(contact.getPostalCode() != null ? contact.getPostalCode() : "");
        cityField.setText(contact.getCity() != null ? contact.getCity() : "");

        memberCheckBox.setSelected(contact.isMember());

        if (contact.isMember()) {
            memberSincePicker.setDisable(false);
            memberUntilPicker.setDisable(false);
            memberSincePicker.setValue(contact.getMemberSince());
            memberUntilPicker.setValue(contact.getMemberUntil());
        }
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        Button saveButton = new Button("Save Changes");
        saveButton.setPrefWidth(120);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    private void handleSave() {
        if (validateInput()) {
            try {
                updateContactFromInput();
                ContactDAO dao = new ContactDAO();

                boolean success = dao.updateContact(contact);

                if (success) {
                    okClicked = true;
                    dialogStage.close();
                } else {
                    showErrorAlert("Failed to update contact in database.");
                }

            } catch (Exception e) {
                showErrorAlert("Error updating contact: " + e.getMessage());
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

        if (phoneField.getText().trim().isEmpty()) {
            errors.append("- Phone number is required\n");
        }

        if (streetNameField.getText().trim().isEmpty()) {
            errors.append("- Street name is required\n");
        }

        if (streetNumField.getText().trim().isEmpty()) {
            errors.append("- Street number is required\n");
        }

        if (postalCodeField.getText().trim().isEmpty()) {
            errors.append("- Postal code is required\n");
        }

        if (cityField.getText().trim().isEmpty()) {
            errors.append("- City is required\n");
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

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void updateContactFromInput() {
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
        } else {
            contact.setMemberSince(null);
            contact.setMemberUntil(null);
        }

        // Update timestamp
        contact.setUpdatedAt(java.time.LocalDateTime.now().toString());
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

    public Contact getContact() {
        return contact;
    }
}