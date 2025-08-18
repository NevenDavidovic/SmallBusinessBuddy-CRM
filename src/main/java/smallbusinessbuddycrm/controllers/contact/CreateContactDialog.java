package smallbusinessbuddycrm.controllers.contact;

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
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.time.LocalDate;

public class CreateContactDialog {

    private Stage dialogStage;
    private Contact result = null;
    private boolean okClicked = false;

    // Contact form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker birthdayPicker;
    private TextField pinField;
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
    private TextField childPinField;
    private ComboBox<String> childGenderComboBox;
    private CheckBox childMemberCheckBox;
    private DatePicker childMemberSincePicker;
    private DatePicker childMemberUntilPicker;
    private TextArea childNoteTextArea;

    // UI Labels for translation
    private Label titleLabel;
    private Label contactInfoLabel;
    private Label underagedSectionLabel;
    private Label optionalLabel;
    private Label childFormTitleLabel;

    // Form labels
    private Label firstNameLabel;
    private Label lastNameLabel;
    private Label birthdayLabel;
    private Label pinLabel;
    private Label emailLabel;
    private Label phoneLabel;
    private Label streetNameLabel;
    private Label streetNumLabel;
    private Label postalCodeLabel;
    private Label cityLabel;
    private Label memberLabel;
    private Label memberSinceLabel;
    private Label memberUntilLabel;

    // Child form labels
    private Label childFirstNameLabel;
    private Label childLastNameLabel;
    private Label childBirthDateLabel;
    private Label childAgeLabel;
    private Label childPinLabel;
    private Label childGenderLabel;
    private Label childMemberSinceLabel;
    private Label childMemberUntilLabel;
    private Label childNoteLabel;

    // Buttons
    private Button addChildButton;
    private Button clearFormButton;
    private Button cancelButton;
    private Button saveButton;

    public CreateContactDialog(Stage parentStage) {
        createDialogStage();
        dialogStage.initOwner(parentStage);
        updateTexts(); // Initial translation
    }

    private void createDialogStage() {
        dialogStage = new Stage();
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
        titleLabel = new Label();
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
        contactInfoLabel = new Label();
        contactInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Form grid
        GridPane formGrid = createContactFormGrid();

        section.getChildren().addAll(contactInfoLabel, formGrid);
        return section;
    }

    private GridPane createContactFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // First Name
        firstNameLabel = new Label();
        grid.add(firstNameLabel, 0, row);
        firstNameField = new TextField();
        firstNameField.setPrefWidth(250);
        grid.add(firstNameField, 1, row++);

        // Last Name
        lastNameLabel = new Label();
        grid.add(lastNameLabel, 0, row);
        lastNameField = new TextField();
        grid.add(lastNameField, 1, row++);

        // Birthday
        birthdayLabel = new Label();
        grid.add(birthdayLabel, 0, row);
        birthdayPicker = new DatePicker();
        birthdayPicker.setPrefWidth(250);
        grid.add(birthdayPicker, 1, row++);

        // PIN
        pinLabel = new Label();
        grid.add(pinLabel, 0, row);
        pinField = new TextField();
        pinField.setPrefWidth(250);
        grid.add(pinField, 1, row++);

        // Email
        emailLabel = new Label();
        grid.add(emailLabel, 0, row);
        emailField = new TextField();
        grid.add(emailField, 1, row++);

        // Phone
        phoneLabel = new Label();
        grid.add(phoneLabel, 0, row);
        phoneField = new TextField();
        grid.add(phoneField, 1, row++);

        // Street Name
        streetNameLabel = new Label();
        grid.add(streetNameLabel, 0, row);
        streetNameField = new TextField();
        grid.add(streetNameField, 1, row++);

        // Street Number
        streetNumLabel = new Label();
        grid.add(streetNumLabel, 0, row);
        streetNumField = new TextField();
        grid.add(streetNumField, 1, row++);

        // Postal Code
        postalCodeLabel = new Label();
        grid.add(postalCodeLabel, 0, row);
        postalCodeField = new TextField();
        grid.add(postalCodeField, 1, row++);

        // City
        cityLabel = new Label();
        grid.add(cityLabel, 0, row);
        cityField = new TextField();
        grid.add(cityField, 1, row++);

        // Member Status
        memberLabel = new Label();
        grid.add(memberLabel, 0, row);
        memberCheckBox = new CheckBox();
        grid.add(memberCheckBox, 1, row++);

        // Member Since
        memberSinceLabel = new Label();
        grid.add(memberSinceLabel, 0, row);
        memberSincePicker = new DatePicker();
        memberSincePicker.setDisable(true);
        grid.add(memberSincePicker, 1, row++);

        // Member Until
        memberUntilLabel = new Label();
        grid.add(memberUntilLabel, 0, row);
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
        underagedSectionLabel = new Label();
        underagedSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        optionalLabel = new Label();
        optionalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
        titleBox.getChildren().addAll(underagedSectionLabel, optionalLabel);

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
        TableColumn<UnderagedMember, String> firstNameCol = new TableColumn<>();
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(100);

        // Last Name column
        TableColumn<UnderagedMember, String> lastNameCol = new TableColumn<>();
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(100);

        // Age column
        TableColumn<UnderagedMember, Integer> ageCol = new TableColumn<>();
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(50);

        // PIN column
        TableColumn<UnderagedMember, String> pinCol = new TableColumn<>();
        pinCol.setCellValueFactory(new PropertyValueFactory<>("pin"));
        pinCol.setPrefWidth(80);

        // Gender column
        TableColumn<UnderagedMember, String> genderCol = new TableColumn<>();
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(70);

        // Member column
        TableColumn<UnderagedMember, String> memberCol = new TableColumn<>();
        memberCol.setCellValueFactory(cellData -> {
            boolean isMember = cellData.getValue().isMember();
            LanguageManager lm = LanguageManager.getInstance();
            String memberText = isMember ? lm.getText("underaged.table.member.yes") : lm.getText("underaged.table.member.no");
            return new javafx.beans.property.SimpleStringProperty(memberText);
        });
        memberCol.setPrefWidth(70);

        // Actions column
        TableColumn<UnderagedMember, Void> actionsCol = new TableColumn<>();
        actionsCol.setCellFactory(param -> new TableCell<UnderagedMember, Void>() {
            private final Button deleteBtn = new Button();

            {
                deleteBtn.setOnAction(event -> {
                    UnderagedMember member = getTableView().getItems().get(getIndex());
                    underagedMembersList.remove(member);
                });
                deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 10px;");

                // Update button text when language changes
                LanguageManager.getInstance().addLanguageChangeListener(() -> {
                    deleteBtn.setText(LanguageManager.getInstance().getText("underaged.table.remove"));
                });
                deleteBtn.setText(LanguageManager.getInstance().getText("underaged.table.remove"));
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

        childFormTitleLabel = new Label();
        childFormTitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        GridPane childGrid = new GridPane();
        childGrid.setHgap(10);
        childGrid.setVgap(10);

        int row = 0;

        // Child First Name
        childFirstNameLabel = new Label();
        childGrid.add(childFirstNameLabel, 0, row);
        childFirstNameField = new TextField();
        childFirstNameField.setPrefWidth(150);
        childGrid.add(childFirstNameField, 1, row);

        // Child Last Name
        childLastNameLabel = new Label();
        childGrid.add(childLastNameLabel, 2, row);
        childLastNameField = new TextField();
        childLastNameField.setPrefWidth(150);
        childGrid.add(childLastNameField, 3, row++);

        // Birth Date and Age
        childBirthDateLabel = new Label();
        childGrid.add(childBirthDateLabel, 0, row);
        childBirthDatePicker = new DatePicker();
        childBirthDatePicker.setPrefWidth(150);
        childGrid.add(childBirthDatePicker, 1, row);

        childAgeLabel = new Label();
        childGrid.add(childAgeLabel, 2, row);
        childAgeField = new TextField();
        childAgeField.setPrefWidth(50);
        childAgeField.setDisable(true); // Auto-calculated
        childGrid.add(childAgeField, 3, row++);

        // PIN
        childPinLabel = new Label();
        childGrid.add(childPinLabel, 0, row);
        childPinField = new TextField();
        childPinField.setPrefWidth(150);
        childGrid.add(childPinField, 1, row);

        // Gender
        childGenderLabel = new Label();
        childGrid.add(childGenderLabel, 2, row);
        childGenderComboBox = new ComboBox<>();
        childGenderComboBox.setPrefWidth(150);
        childGrid.add(childGenderComboBox, 3, row++);

        // Member checkbox
        childMemberCheckBox = new CheckBox();
        childGrid.add(childMemberCheckBox, 0, row, 2, 1);
        row++;

        // Member dates
        childMemberSinceLabel = new Label();
        childGrid.add(childMemberSinceLabel, 0, row);
        childMemberSincePicker = new DatePicker();
        childMemberSincePicker.setPrefWidth(150);
        childMemberSincePicker.setDisable(true);
        childGrid.add(childMemberSincePicker, 1, row);

        childMemberUntilLabel = new Label();
        childGrid.add(childMemberUntilLabel, 2, row);
        childMemberUntilPicker = new DatePicker();
        childMemberUntilPicker.setPrefWidth(150);
        childMemberUntilPicker.setDisable(true);
        childGrid.add(childMemberUntilPicker, 3, row++);

        // Note
        childNoteLabel = new Label();
        childGrid.add(childNoteLabel, 0, row);
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
        addChildButton = new Button();
        addChildButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addChildButton.setOnAction(e -> handleAddChild());

        clearFormButton = new Button();
        clearFormButton.setOnAction(e -> clearChildForm());

        buttonBox.getChildren().addAll(addChildButton, clearFormButton);

        formSection.getChildren().addAll(childFormTitleLabel, childGrid, buttonBox);
        return formSection;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        cancelButton = new Button();
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        saveButton = new Button();
        saveButton.setPrefWidth(80);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update dialog title
        if (dialogStage != null) {
            dialogStage.setTitle(lm.getText("contact.dialog.title"));
        }

        // Update main labels
        if (titleLabel != null) titleLabel.setText(lm.getText("contact.dialog.title"));
        if (contactInfoLabel != null) contactInfoLabel.setText(lm.getText("contact.dialog.contact.info"));
        if (underagedSectionLabel != null) underagedSectionLabel.setText(lm.getText("contact.dialog.underaged.section"));
        if (optionalLabel != null) optionalLabel.setText(lm.getText("contact.dialog.optional"));

        // Update form labels
        if (firstNameLabel != null) firstNameLabel.setText(lm.getText("contact.form.first.name"));
        if (lastNameLabel != null) lastNameLabel.setText(lm.getText("contact.form.last.name"));
        if (birthdayLabel != null) birthdayLabel.setText(lm.getText("contact.form.birthday"));
        if (pinLabel != null) pinLabel.setText(lm.getText("contact.form.pin"));
        if (emailLabel != null) emailLabel.setText(lm.getText("contact.form.email"));
        if (phoneLabel != null) phoneLabel.setText(lm.getText("contact.form.phone"));
        if (streetNameLabel != null) streetNameLabel.setText(lm.getText("contact.form.street.name"));
        if (streetNumLabel != null) streetNumLabel.setText(lm.getText("contact.form.street.number"));
        if (postalCodeLabel != null) postalCodeLabel.setText(lm.getText("contact.form.postal.code"));
        if (cityLabel != null) cityLabel.setText(lm.getText("contact.form.city"));
        if (memberLabel != null) memberLabel.setText(lm.getText("contact.form.member"));
        if (memberSinceLabel != null) memberSinceLabel.setText(lm.getText("contact.form.member.since"));
        if (memberUntilLabel != null) memberUntilLabel.setText(lm.getText("contact.form.member.until"));

        // Update placeholders
        if (pinField != null) pinField.setPromptText(lm.getText("contact.form.pin.placeholder"));
        if (memberCheckBox != null) memberCheckBox.setText(lm.getText("contact.form.is.member"));

        // Update table column headers
        if (underagedTableView != null && underagedTableView.getColumns().size() >= 7) {
            underagedTableView.getColumns().get(0).setText(lm.getText("underaged.table.first.name"));
            underagedTableView.getColumns().get(1).setText(lm.getText("underaged.table.last.name"));
            underagedTableView.getColumns().get(2).setText(lm.getText("underaged.table.age"));
            underagedTableView.getColumns().get(3).setText(lm.getText("underaged.table.pin"));
            underagedTableView.getColumns().get(4).setText(lm.getText("underaged.table.gender"));
            underagedTableView.getColumns().get(5).setText(lm.getText("underaged.table.member"));
            underagedTableView.getColumns().get(6).setText(lm.getText("underaged.table.actions"));
        }

        // Update child form labels
        if (childFormTitleLabel != null) childFormTitleLabel.setText(lm.getText("child.form.title"));
        if (childFirstNameLabel != null) childFirstNameLabel.setText(lm.getText("child.form.first.name"));
        if (childLastNameLabel != null) childLastNameLabel.setText(lm.getText("child.form.last.name"));
        if (childBirthDateLabel != null) childBirthDateLabel.setText(lm.getText("child.form.birth.date"));
        if (childAgeLabel != null) childAgeLabel.setText(lm.getText("child.form.age"));
        if (childPinLabel != null) childPinLabel.setText(lm.getText("child.form.pin"));
        if (childGenderLabel != null) childGenderLabel.setText(lm.getText("child.form.gender"));
        if (childMemberSinceLabel != null) childMemberSinceLabel.setText(lm.getText("child.form.member.since"));
        if (childMemberUntilLabel != null) childMemberUntilLabel.setText(lm.getText("child.form.member.until"));
        if (childNoteLabel != null) childNoteLabel.setText(lm.getText("child.form.note"));

        // Update child form fields
        if (childPinField != null) childPinField.setPromptText(lm.getText("child.form.pin.placeholder"));
        if (childMemberCheckBox != null) childMemberCheckBox.setText(lm.getText("child.form.is.member"));

        // Update gender combo box
        if (childGenderComboBox != null) {
            String selectedValue = childGenderComboBox.getValue();
            ObservableList<String> genderOptions = FXCollections.observableArrayList(
                    lm.getText("gender.male"),
                    lm.getText("gender.female"),
                    lm.getText("gender.other")
            );
            childGenderComboBox.setItems(genderOptions);

            // Try to maintain selection
            if (selectedValue != null) {
                childGenderComboBox.getSelectionModel().select(selectedValue);
            }
        }

        // Update buttons
        if (addChildButton != null) addChildButton.setText(lm.getText("child.form.add.button"));
        if (clearFormButton != null) clearFormButton.setText(lm.getText("child.form.clear.button"));
        if (cancelButton != null) cancelButton.setText(lm.getText("button.cancel"));
        if (saveButton != null) saveButton.setText(lm.getText("button.save"));

        // Refresh table to update member status column
        if (underagedTableView != null) {
            underagedTableView.refresh();
        }
    }

    private void handleAddChild() {
        if (validateChildInput()) {
            UnderagedMember child = createUnderagedMemberFromInput();
            underagedMembersList.add(child);
            clearChildForm();

            // Show success message
            LanguageManager lm = LanguageManager.getInstance();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lm.getText("dialog.success"));
            alert.setHeaderText(null);
            alert.setContentText(lm.getText("dialog.child.added"));
            alert.showAndWait();
        }
    }

    private boolean validateChildInput() {
        StringBuilder errors = new StringBuilder();
        LanguageManager lm = LanguageManager.getInstance();

        if (childFirstNameField.getText().trim().isEmpty()) {
            errors.append(lm.getText("validation.child.first.name.required")).append("\n");
        }

        if (childLastNameField.getText().trim().isEmpty()) {
            errors.append(lm.getText("validation.child.last.name.required")).append("\n");
        }

        if (childBirthDatePicker.getValue() == null) {
            errors.append(lm.getText("validation.child.birth.date.required")).append("\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lm.getText("dialog.validation.error"));
            alert.setHeaderText(lm.getText("dialog.validation.fix.errors"));
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
        child.setPin(childPinField.getText().trim());

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
        childPinField.clear();
        childGenderComboBox.setValue(null);
        childMemberCheckBox.setSelected(false);
        childMemberSincePicker.setValue(null);
        childMemberUntilPicker.setValue(null);
        childNoteTextArea.clear();

        // Disable member date pickers
        childMemberSincePicker.setDisable(true);
        childMemberUntilPicker.setDisable(true);
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
        LanguageManager lm = LanguageManager.getInstance();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.append(lm.getText("validation.first.name.required")).append("\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errors.append(lm.getText("validation.last.name.required")).append("\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append(lm.getText("validation.email.required")).append("\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append(lm.getText("validation.email.invalid")).append("\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lm.getText("dialog.validation.error"));
            alert.setHeaderText(lm.getText("dialog.validation.fix.errors"));
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
        contact.setBirthday(birthdayPicker.getValue());
        contact.setPin(pinField.getText().trim());
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
        updateTexts(); // Update translations before showing
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