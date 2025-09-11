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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditContactDialog {

    private Stage dialogStage;
    private Contact contact;
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
    private List<Integer> deletedUnderagedIds = new ArrayList<>();

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

    // Button references for cleaner toggle functionality
    private Button addChildButton;
    private Button updateChildButton;
    private Button cancelEditButton;

    // For editing existing children
    private UnderagedMember currentEditingChild = null;
    private boolean isEditingChild = false;

    // UI Labels for translation
    private Label titleLabel;
    private Label contactInfoLabel;
    private Label underagedSectionLabel;
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
    private Button clearFormButton;
    private Button cancelButton;
    private Button saveButton;

    /**
     * Creates a new EditContactDialog for modifying existing contacts and their underaged members.
     * Initializes the dialog stage, loads existing data, populates form fields, and applies translations.
     *
     * @param parentStage The parent stage that owns this modal dialog
     * @param contact The existing contact to edit
     */
    public EditContactDialog(Stage parentStage, Contact contact) {
        this.contact = contact;
        createDialogStage();
        dialogStage.initOwner(parentStage);
        loadData();
        populateFields();
        updateTexts();
    }

    /**
     * Creates and configures the main dialog stage with all UI components.
     * Sets up modal behavior, scroll pane, layout sections, and scene configuration.
     * Initializes a resizable dialog with 700x850 dimensions.
     */
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

    /**
     * Loads existing data for the contact being edited.
     * Retrieves associated underaged members from database and populates the members list
     * for display and modification in the dialog.
     */
    private void loadData() {
        // Load existing underaged members for this contact
        UnderagedDAO underagedDAO = new UnderagedDAO();
        List<UnderagedMember> existingChildren = underagedDAO.getUnderagedMembersByContactId(contact.getId());
        underagedMembersList.clear();
        underagedMembersList.addAll(existingChildren);
    }

    /**
     * Creates the contact information section containing personal and address fields.
     * Includes contact form grid with all required and optional contact fields pre-populated.
     *
     * @return VBox containing the complete contact information section
     */
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

    /**
     * Creates the main contact form grid with all input fields and labels.
     * Sets up form fields for personal info, address, and membership details.
     * Configures field bindings and member checkbox behavior for date pickers.
     *
     * @return GridPane containing all contact form fields with proper layout
     */
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

    /**
     * Creates the underaged members section with table and edit form.
     * Includes section title, data table for existing children, and form for adding/editing.
     *
     * @return VBox containing the complete underaged members management section
     */
    private VBox createUnderagedMembersSection() {
        VBox section = new VBox(10);

        // Section title
        HBox titleBox = new HBox(10);
        underagedSectionLabel = new Label();
        underagedSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        titleBox.getChildren().add(underagedSectionLabel);

        // Create table for underaged members
        underagedTableView = createUnderagedMembersTable();

        // Create form for adding/editing underaged members
        VBox childForm = createChildForm();

        section.getChildren().addAll(titleBox, underagedTableView, childForm);
        return section;
    }

    /**
     * Creates and configures the table for displaying underaged members.
     * Sets up columns for name, age, PIN, gender, member status, and edit/delete actions.
     * Includes edit and delete functionality with confirmation dialogs.
     *
     * @return TableView configured for displaying and managing underaged members
     */
    private TableView<UnderagedMember> createUnderagedMembersTable() {
        TableView<UnderagedMember> table = new TableView<>();
        table.setPrefHeight(150);
        table.setItems(underagedMembersList);

        // First Name column
        TableColumn<UnderagedMember, String> firstNameCol = new TableColumn<>();
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(90);

        // Last Name column
        TableColumn<UnderagedMember, String> lastNameCol = new TableColumn<>();
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(90);

        // Age column
        TableColumn<UnderagedMember, Integer> ageCol = new TableColumn<>();
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(50);

        // PIN column
        TableColumn<UnderagedMember, String> pinCol = new TableColumn<>();
        pinCol.setCellValueFactory(new PropertyValueFactory<>("pin"));
        pinCol.setPrefWidth(70);

        // Gender column
        TableColumn<UnderagedMember, String> genderCol = new TableColumn<>();
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(60);

        // Member column
        TableColumn<UnderagedMember, String> memberCol = new TableColumn<>();
        memberCol.setCellValueFactory(cellData -> {
            boolean isMember = cellData.getValue().isMember();
            return new javafx.beans.property.SimpleStringProperty(isMember ? "Yes" : "No");
        });
        memberCol.setPrefWidth(60);

        // Actions column
        TableColumn<UnderagedMember, Void> actionsCol = new TableColumn<>();
        actionsCol.setCellFactory(param -> new TableCell<UnderagedMember, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setOnAction(event -> {
                    UnderagedMember member = getTableView().getItems().get(getIndex());
                    editChild(member);
                });
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");

                deleteBtn.setOnAction(event -> {
                    UnderagedMember member = getTableView().getItems().get(getIndex());
                    deleteChild(member);
                });
                deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 10px;");

                actionBox.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
        actionsCol.setPrefWidth(120);

        table.getColumns().addAll(firstNameCol, lastNameCol, ageCol, pinCol, genderCol, memberCol, actionsCol);
        return table;
    }

    /**
     * Creates the form for adding and editing underaged members.
     * Includes input fields for personal info, membership details, and notes.
     * Sets up auto-calculation of age and dynamic button visibility for edit mode.
     *
     * @return VBox containing the complete child addition/editing form with action buttons
     */
    private VBox createChildForm() {
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
        childGenderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
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

        HBox buttonBox = new HBox(10);

        addChildButton = new Button();
        addChildButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addChildButton.setOnAction(e -> handleAddChild());

        updateChildButton = new Button();
        updateChildButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateChildButton.setOnAction(e -> handleUpdateChild());
        updateChildButton.setVisible(false);

        cancelEditButton = new Button();
        cancelEditButton.setOnAction(e -> cancelChildEdit());
        cancelEditButton.setVisible(false);

        clearFormButton = new Button();
        clearFormButton.setOnAction(e -> clearChildForm());

        buttonBox.getChildren().addAll(addChildButton, updateChildButton, cancelEditButton, clearFormButton);

        formSection.getChildren().addAll(childFormTitleLabel, childGrid, buttonBox);
        return formSection;
    }

    /**
     * Updates all UI text elements based on current language settings.
     * Refreshes dialog title, labels, placeholders, table headers, button text,
     * and combo box options when language changes between English and Croatian.
     */
    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update dialog title
        if (dialogStage != null) {
            dialogStage.setTitle(lm.getText("contact.edit.dialog.title"));
        }

        // Update main labels
        if (titleLabel != null) titleLabel.setText(lm.getText("contact.edit.dialog.title"));
        if (contactInfoLabel != null) contactInfoLabel.setText(lm.getText("contact.dialog.contact.info"));
        if (underagedSectionLabel != null) underagedSectionLabel.setText(lm.getText("contact.dialog.underaged.section"));

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
        if (childFormTitleLabel != null) childFormTitleLabel.setText(lm.getText("child.edit.form.title"));
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

        // Update gender combo box - keep original items
        if (childGenderComboBox != null) {
            String selectedValue = childGenderComboBox.getValue();
            ObservableList<String> genderOptions = FXCollections.observableArrayList(
                    lm.getText("gender.male"),
                    lm.getText("gender.female"),
                    lm.getText("gender.other")
            );
            childGenderComboBox.setItems(genderOptions);

            if (selectedValue != null) {
                childGenderComboBox.getSelectionModel().select(selectedValue);
            }
        }

        // Update buttons
        if (addChildButton != null) addChildButton.setText(lm.getText("child.form.add.button"));
        if (updateChildButton != null) updateChildButton.setText(lm.getText("child.form.update.button"));
        if (cancelEditButton != null) cancelEditButton.setText(lm.getText("child.form.cancel.edit.button"));
        if (clearFormButton != null) clearFormButton.setText(lm.getText("child.form.clear.button"));
        if (cancelButton != null) cancelButton.setText(lm.getText("button.cancel"));
        if (saveButton != null) saveButton.setText(lm.getText("button.save.changes"));
    }

    /**
     * Initiates editing mode for an existing underaged member.
     * Populates child form with selected member's data, enables edit mode,
     * and toggles button visibility to show Update and Cancel options.
     *
     * @param child The underaged member to edit
     */
    private void editChild(UnderagedMember child) {
        currentEditingChild = child;
        isEditingChild = true;

        // Populate form with child data
        childFirstNameField.setText(child.getFirstName());
        childLastNameField.setText(child.getLastName());
        childBirthDatePicker.setValue(child.getBirthDate());
        childAgeField.setText(String.valueOf(child.getAge()));
        childPinField.setText(child.getPin() != null ? child.getPin() : "");
        childGenderComboBox.setValue(child.getGender());
        childMemberCheckBox.setSelected(child.isMember());
        childMemberSincePicker.setValue(child.getMemberSince());
        childMemberUntilPicker.setValue(child.getMemberUntil());
        childNoteTextArea.setText(child.getNote() != null ? child.getNote() : "");

        // Update member date pickers state
        boolean isMember = child.isMember();
        childMemberSincePicker.setDisable(!isMember);
        childMemberUntilPicker.setDisable(!isMember);

        // Toggle button visibility
        toggleChildFormButtons(true);
    }

    /**
     * Handles deletion of an underaged member with confirmation.
     * Shows confirmation dialog, removes from list if confirmed, and tracks
     * deletion for database update if the member has an existing ID.
     *
     * @param child The underaged member to delete
     */
    private void deleteChild(UnderagedMember child) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Delete Underaged Member");
        confirmation.setContentText("Are you sure you want to delete " + child.getFullName() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            underagedMembersList.remove(child);

            // If this is an existing child (has ID), track it for deletion
            if (child.getId() > 0) {
                deletedUnderagedIds.add(child.getId());
            }

            showSuccessMessage("Child removed successfully!");
        }
    }

    /**
     * Handles adding a new underaged member to the contact.
     * Validates child input, creates UnderagedMember object, adds to list,
     * clears form, and shows success confirmation message.
     */
    private void handleAddChild() {
        if (validateChildInput()) {
            UnderagedMember child = createUnderagedMemberFromInput();
            underagedMembersList.add(child);
            clearChildForm();
            showSuccessMessage("Child added successfully!");
        }
    }

    /**
     * Handles updating an existing underaged member being edited.
     * Validates input, updates the existing member object, refreshes table display,
     * exits edit mode, and shows success confirmation message.
     */
    private void handleUpdateChild() {
        if (validateChildInput() && currentEditingChild != null) {
            // Update the existing child object
            updateUnderagedMemberFromInput(currentEditingChild);

            // Refresh the table
            underagedTableView.refresh();

            cancelChildEdit();
            showSuccessMessage("Child updated successfully!");
        }
    }

    /**
     * Cancels child editing mode and returns to add mode.
     * Clears editing state, resets form, and toggles button visibility
     * back to normal add mode configuration.
     */
    private void cancelChildEdit() {
        currentEditingChild = null;
        isEditingChild = false;
        clearChildForm();
        toggleChildFormButtons(false);
    }

    /**
     * Toggles child form button visibility based on edit mode state.
     * Shows Add button in normal mode, shows Update/Cancel buttons in edit mode.
     *
     * @param editMode true for edit mode, false for add mode
     */
    private void toggleChildFormButtons(boolean editMode) {
        addChildButton.setVisible(!editMode);
        updateChildButton.setVisible(editMode);
        cancelEditButton.setVisible(editMode);
    }

    /**
     * Validates child form input before adding or updating underaged member.
     * Checks required fields (first name, last name, birth date) and shows
     * validation error dialog with specific field requirements if validation fails.
     *
     * @return true if all required child fields are valid, false otherwise
     */
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

    /**
     * Creates new UnderagedMember object from current child form input fields.
     * Populates all fields including calculated age, membership details, and timestamps.
     * Sets creation and update timestamps for new member record.
     *
     * @return UnderagedMember object populated with current form data
     */
    private UnderagedMember createUnderagedMemberFromInput() {
        UnderagedMember child = new UnderagedMember();
        populateUnderagedMemberFromInput(child);

        String now = java.time.LocalDateTime.now().toString();
        child.setCreatedAt(now);
        child.setUpdatedAt(now);

        return child;
    }

    /**
     * Updates existing UnderagedMember object from current child form input fields.
     * Populates all fields with current form data and updates the modification timestamp.
     *
     * @param child The existing UnderagedMember object to update
     */
    private void updateUnderagedMemberFromInput(UnderagedMember child) {
        populateUnderagedMemberFromInput(child);
        child.setUpdatedAt(java.time.LocalDateTime.now().toString());
    }

    /**
     * Populates UnderagedMember object with data from child form input fields.
     * Common method used by both create and update operations to set member data
     * including personal info, membership details, and parent contact association.
     *
     * @param child The UnderagedMember object to populate with form data
     */
    private void populateUnderagedMemberFromInput(UnderagedMember child) {
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
        child.setContactId(contact.getId());
    }

    /**
     * Clears all child form input fields and resets to default state.
     * Resets text fields, date pickers, combo boxes, checkboxes, and text areas.
     * Disables member date pickers when clearing member checkbox.
     */
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

    /**
     * Populates form fields with existing contact data for editing.
     * Fills all contact form fields with current contact information and
     * sets proper member date picker states based on membership status.
     */
    private void populateFields() {
        // Fill contact fields with existing data
        firstNameField.setText(contact.getFirstName() != null ? contact.getFirstName() : "");
        lastNameField.setText(contact.getLastName() != null ? contact.getLastName() : "");
        birthdayPicker.setValue(contact.getBirthday());
        pinField.setText(contact.getPin() != null ? contact.getPin() : "");
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

    /**
     * Creates the dialog button box with Cancel and Save Changes actions.
     * Configures button styling, sizing, and event handlers for dialog completion.
     *
     * @return HBox containing Cancel and Save Changes buttons with proper styling
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        cancelButton = new Button();
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        saveButton = new Button();
        saveButton.setPrefWidth(120);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    /**
     * Handles saving all contact and underaged member changes to database.
     * Validates input, updates contact record, processes deleted children,
     * creates/updates remaining children, and closes dialog on success.
     */
    private void handleSave() {
        if (validateInput()) {
            try {
                updateContactFromInput();
                ContactDAO contactDAO = new ContactDAO();
                UnderagedDAO underagedDAO = new UnderagedDAO();

                boolean success = contactDAO.updateContact(contact);

                if (success) {
                    // Handle underaged members changes

                    // 1. Delete removed children
                    if (!deletedUnderagedIds.isEmpty()) {
                        underagedDAO.deleteUnderagedMembers(deletedUnderagedIds);
                    }

                    // 2. Update/create children
                    for (UnderagedMember child : underagedMembersList) {
                        if (child.getId() > 0) {
                            // Existing child - update
                            underagedDAO.updateUnderagedMember(child);
                        } else {
                            // New child - create
                            underagedDAO.createUnderagedMember(child);
                        }
                    }

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

    /**
     * Validates main contact form input before saving to database.
     * Checks required fields (first name, last name, email) and email format.
     * Shows validation error dialog with specific field requirements if validation fails.
     *
     * @return true if all required contact fields are valid, false otherwise
     */
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

    /**
     * Performs basic email validation using simple pattern matching.
     * Checks for presence of @ symbol and at least one dot for domain validation.
     *
     * @param email The email address to validate
     * @return true if email contains @ and . characters, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    /**
     * Updates the contact object with data from main form input fields.
     * Populates all contact fields including address, membership details, and timestamps.
     * Sets or clears membership dates based on member checkbox state.
     */
    private void updateContactFromInput() {
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
        } else {
            contact.setMemberSince(null);
            contact.setMemberUntil(null);
        }

        // Update timestamp
        contact.setUpdatedAt(java.time.LocalDateTime.now().toString());
    }

    /**
     * Displays error alert dialog with specified message.
     * Shows modal error dialog with standard title and header for operation failures.
     *
     * @param message The error message to display to the user
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays success message dialog with specified content.
     * Shows modal information dialog with success styling for completed operations.
     *
     * @param message The success message to display to the user
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows the dialog and waits for user interaction.
     * Updates translations before display and returns whether user saved changes.
     *
     * @return true if user saved changes, false if cancelled
     */
    public boolean showAndWait() {
        updateTexts(); // Update translations before showing
        dialogStage.showAndWait();
        return okClicked;
    }

    /**
     * Gets the updated contact object after successful dialog completion.
     * Returns the Contact object with all modifications applied.
     *
     * @return The updated Contact object, or original if cancelled
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Gets the list of underaged members associated with this contact.
     * Provides access to children that were added or modified during editing.
     *
     * @return ObservableList of UnderagedMember objects for this contact
     */
    public ObservableList<UnderagedMember> getUnderagedMembers() {
        return underagedMembersList;
    }
}