package smallbusinessbuddycrm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateContactDialog {

    private Stage dialogStage;
    private Contact result = null;
    private boolean okClicked = false;

    // Contact form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker birthdayPicker;
    private TextField pinField; // Added PIN field
    private TextField emailField;
    private TextField phoneField;
    private TextField streetNameField;
    private TextField streetNumField;
    private TextField postalCodeField;
    private TextField cityField;
    private CheckBox memberCheckBox;
    private DatePicker memberSincePicker;
    private DatePicker memberUntilPicker;

    // Underaged members section
    private TableView<UnderagedMember> underagedTableView;
    private ObservableList<UnderagedMember> underagedMembersList = FXCollections.observableArrayList();

    // Underaged member form fields
    private TextField childFirstNameField;
    private TextField childLastNameField;
    private DatePicker childBirthDatePicker;
    private TextField childAgeField;
    private TextField childPinField; // Added PIN field for children
    private ComboBox<String> childGenderComboBox;
    private CheckBox childMemberCheckBox;
    private DatePicker childMemberSincePicker;
    private DatePicker childMemberUntilPicker;
    private TextArea childNoteTextArea;

    public CreateContactDialog(Stage parentStage) {
        createDialogStage();
        dialogStage.initOwner(parentStage);
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Create New Contact");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(true);

        // Create the main scroll pane for better handling of large content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Create the layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Create New Contact");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Contact form
        VBox contactSection = createContactSection();

        // Underaged members section
        VBox underagedSection = createUnderagedMembersSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, contactSection, underagedSection, buttonBox);
        scrollPane.setContent(mainLayout);

        Scene scene = new Scene(scrollPane, 700, 850);
        dialogStage.setScene(scene);
    }

    private VBox createContactSection() {
        VBox section = new VBox(10);

        // Section title
        Label sectionTitle = new Label("Contact Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Form grid
        GridPane formGrid = createContactFormGrid();

        section.getChildren().addAll(sectionTitle, formGrid);
        return section;
    }

    private GridPane createContactFormGrid() {
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

        // Birthday (Added)
        grid.add(new Label("Birthday:"), 0, row);
        birthdayPicker = new DatePicker();
        birthdayPicker.setPrefWidth(250);
        grid.add(birthdayPicker, 1, row++);

        // PIN (Added)
        grid.add(new Label("PIN:"), 0, row);
        pinField = new TextField();
        pinField.setPrefWidth(250);
        pinField.setPromptText("Personal Identification Number");
        grid.add(pinField, 1, row++);

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

    private VBox createUnderagedMembersSection() {
        VBox section = new VBox(10);

        // Section title
        HBox titleBox = new HBox(10);
        Label sectionTitle = new Label("Underaged Members / Children");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label optionalLabel = new Label("(Optional)");
        optionalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
        titleBox.getChildren().addAll(sectionTitle, optionalLabel);

        // Create table for underaged members
        underagedTableView = createUnderagedMembersTable();

        // Create form for adding underaged members
        VBox addChildForm = createAddChildForm();

        section.getChildren().addAll(titleBox, underagedTableView, addChildForm);
        return section;
    }

    private TableView<UnderagedMember> createUnderagedMembersTable() {
        TableView<UnderagedMember> table = new TableView<>();
        table.setPrefHeight(150);
        table.setItems(underagedMembersList);

        // First Name column
        TableColumn<UnderagedMember, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(100);

        // Last Name column
        TableColumn<UnderagedMember, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(100);

        // Age column
        TableColumn<UnderagedMember, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(50);

        // PIN column
        TableColumn<UnderagedMember, String> pinCol = new TableColumn<>("PIN");
        pinCol.setCellValueFactory(new PropertyValueFactory<>("pin"));
        pinCol.setPrefWidth(80);

        // Gender column
        TableColumn<UnderagedMember, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(70);

        // Member column
        TableColumn<UnderagedMember, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(cellData -> {
            boolean isMember = cellData.getValue().isMember();
            return new javafx.beans.property.SimpleStringProperty(isMember ? "Yes" : "No");
        });
        memberCol.setPrefWidth(70);

        // Actions column
        TableColumn<UnderagedMember, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<UnderagedMember, Void>() {
            private final Button deleteBtn = new Button("Remove");

            {
                deleteBtn.setOnAction(event -> {
                    UnderagedMember member = getTableView().getItems().get(getIndex());
                    underagedMembersList.remove(member);
                });
                deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 10px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        actionsCol.setPrefWidth(80);

        table.getColumns().addAll(firstNameCol, lastNameCol, ageCol, pinCol, genderCol, memberCol, actionsCol);
        return table;
    }

    private VBox createAddChildForm() {
        VBox formSection = new VBox(10);

        Label formTitle = new Label("Add Underaged Member:");
        formTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        GridPane childGrid = new GridPane();
        childGrid.setHgap(10);
        childGrid.setVgap(10);

        int row = 0;

        // Child First Name
        childGrid.add(new Label("First Name:"), 0, row);
        childFirstNameField = new TextField();
        childFirstNameField.setPrefWidth(150);
        childGrid.add(childFirstNameField, 1, row);

        // Child Last Name
        childGrid.add(new Label("Last Name:"), 2, row);
        childLastNameField = new TextField();
        childLastNameField.setPrefWidth(150);
        childGrid.add(childLastNameField, 3, row++);

        // Birth Date and Age
        childGrid.add(new Label("Birth Date:"), 0, row);
        childBirthDatePicker = new DatePicker();
        childBirthDatePicker.setPrefWidth(150);
        childGrid.add(childBirthDatePicker, 1, row);

        childGrid.add(new Label("Age:"), 2, row);
        childAgeField = new TextField();
        childAgeField.setPrefWidth(50);
        childAgeField.setDisable(true); // Auto-calculated
        childGrid.add(childAgeField, 3, row++);

        // PIN
        childGrid.add(new Label("PIN:"), 0, row);
        childPinField = new TextField();
        childPinField.setPrefWidth(150);
        childPinField.setPromptText("Personal ID Number");
        childGrid.add(childPinField, 1, row);

        // Gender
        childGrid.add(new Label("Gender:"), 2, row);
        childGenderComboBox = new ComboBox<>();
        childGenderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        childGenderComboBox.setPrefWidth(150);
        childGrid.add(childGenderComboBox, 3, row++);

        // Member checkbox
        childMemberCheckBox = new CheckBox("Is Member");
        childGrid.add(childMemberCheckBox, 0, row, 2, 1);
        row++;

        // Member dates
        childGrid.add(new Label("Member Since:"), 0, row);
        childMemberSincePicker = new DatePicker();
        childMemberSincePicker.setPrefWidth(150);
        childMemberSincePicker.setDisable(true);
        childGrid.add(childMemberSincePicker, 1, row);

        childGrid.add(new Label("Member Until:"), 2, row);
        childMemberUntilPicker = new DatePicker();
        childMemberUntilPicker.setPrefWidth(150);
        childMemberUntilPicker.setDisable(true);
        childGrid.add(childMemberUntilPicker, 3, row++);

        // Note
        childGrid.add(new Label("Note:"), 0, row);
        childNoteTextArea = new TextArea();
        childNoteTextArea.setPrefRowCount(2);
        childNoteTextArea.setPrefWidth(320);
        childGrid.add(childNoteTextArea, 1, row, 3, 1);

        // Auto-calculate age when birth date changes
        childBirthDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                int age = LocalDate.now().getYear() - newDate.getYear();
                if (LocalDate.now().getDayOfYear() < newDate.getDayOfYear()) {
                    age--;
                }
                childAgeField.setText(String.valueOf(age));
            }
        });

        // Enable/disable member date pickers
        childMemberCheckBox.setOnAction(e -> {
            boolean isMember = childMemberCheckBox.isSelected();
            childMemberSincePicker.setDisable(!isMember);
            childMemberUntilPicker.setDisable(!isMember);
            if (!isMember) {
                childMemberSincePicker.setValue(null);
                childMemberUntilPicker.setValue(null);
            }
        });

        // Add button
        HBox buttonBox = new HBox(10);
        Button addChildButton = new Button("Add Child");
        addChildButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addChildButton.setOnAction(e -> handleAddChild());

        Button clearFormButton = new Button("Clear Form");
        clearFormButton.setOnAction(e -> clearChildForm());

        buttonBox.getChildren().addAll(addChildButton, clearFormButton);

        formSection.getChildren().addAll(formTitle, childGrid, buttonBox);
        return formSection;
    }

    private void handleAddChild() {
        if (validateChildInput()) {
            UnderagedMember child = createUnderagedMemberFromInput();
            underagedMembersList.add(child);
            clearChildForm();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Child added successfully!");
            alert.showAndWait();
        }
    }

    private boolean validateChildInput() {
        StringBuilder errors = new StringBuilder();

        if (childFirstNameField.getText().trim().isEmpty()) {
            errors.append("- Child first name is required\n");
        }

        if (childLastNameField.getText().trim().isEmpty()) {
            errors.append("- Child last name is required\n");
        }

        if (childBirthDatePicker.getValue() == null) {
            errors.append("- Child birth date is required\n");
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

    private UnderagedMember createUnderagedMemberFromInput() {
        UnderagedMember child = new UnderagedMember();

        child.setFirstName(childFirstNameField.getText().trim());
        child.setLastName(childLastNameField.getText().trim());
        child.setBirthDate(childBirthDatePicker.getValue());
        child.setPin(childPinField.getText().trim()); // Added PIN

        try {
            child.setAge(Integer.parseInt(childAgeField.getText().trim()));
        } catch (NumberFormatException e) {
            child.setAge(0);
        }

        child.setGender(childGenderComboBox.getValue());
        child.setMember(childMemberCheckBox.isSelected());
        child.setMemberSince(childMemberSincePicker.getValue());
        child.setMemberUntil(childMemberUntilPicker.getValue());
        child.setNote(childNoteTextArea.getText().trim());

        String now = java.time.LocalDateTime.now().toString();
        child.setCreatedAt(now);
        child.setUpdatedAt(now);

        return child;
    }

    private void clearChildForm() {
        childFirstNameField.clear();
        childLastNameField.clear();
        childBirthDatePicker.setValue(null);
        childAgeField.clear();
        childPinField.clear(); // Added PIN field clearing
        childGenderComboBox.setValue(null);
        childMemberCheckBox.setSelected(false);
        childMemberSincePicker.setValue(null);
        childMemberUntilPicker.setValue(null);
        childNoteTextArea.clear();

        // Disable member date pickers
        childMemberSincePicker.setDisable(true);
        childMemberUntilPicker.setDisable(true);
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
                ContactDAO contactDAO = new ContactDAO();

                boolean success = contactDAO.createContact(newContact);

                if (success) {
                    // Save underaged members if any
                    if (!underagedMembersList.isEmpty()) {
                        UnderagedDAO underagedDAO = new UnderagedDAO();

                        for (UnderagedMember child : underagedMembersList) {
                            child.setContactId(newContact.getId()); // Set the parent contact ID
                            boolean childSuccess = underagedDAO.createUnderagedMember(child);

                            if (!childSuccess) {
                                System.err.println("Failed to save child: " + child.getFullName());
                            }
                        }
                    }

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
        contact.setBirthday(birthdayPicker.getValue()); // Added birthday handling
        contact.setPin(pinField.getText().trim()); // Added PIN handling
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

    // Method to get the created underaged members (if needed for further processing)
    public ObservableList<UnderagedMember> getUnderagedMembers() {
        return underagedMembersList;
    }
}