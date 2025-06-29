package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UnderagedMemberController implements Initializable {

    @FXML private TableView<UnderagedMember> underagedTableView;
    @FXML private TableColumn<UnderagedMember, Boolean> selectColumn;
    @FXML private TableColumn<UnderagedMember, String> firstNameColumn;
    @FXML private TableColumn<UnderagedMember, String> lastNameColumn;
    @FXML private TableColumn<UnderagedMember, String> birthDateColumn;
    @FXML private TableColumn<UnderagedMember, Integer> ageColumn;
    @FXML private TableColumn<UnderagedMember, String> pinColumn; // Added PIN column
    @FXML private TableColumn<UnderagedMember, String> genderColumn;
    @FXML private TableColumn<UnderagedMember, String> memberStatusColumn;
    @FXML private TableColumn<UnderagedMember, String> memberSinceColumn;
    @FXML private TableColumn<UnderagedMember, String> memberUntilColumn;
    @FXML private TableColumn<UnderagedMember, String> contactColumn;
    @FXML private TableColumn<UnderagedMember, String> noteColumn;

    // Form fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField ageField;
    @FXML private TextField pinField; // Added PIN field
    @FXML private ComboBox<String> genderComboBox;
    @FXML private CheckBox isMemberCheckBox;
    @FXML private DatePicker memberSincePicker;
    @FXML private DatePicker memberUntilPicker;
    @FXML private ComboBox<Contact> contactComboBox;
    @FXML private TextArea noteTextArea;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button refreshButton;

    // Search and filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;

    private ObservableList<UnderagedMember> underagedMembersList = FXCollections.observableArrayList();
    private ObservableList<Contact> contactsList = FXCollections.observableArrayList();

    private UnderagedDAO underagedDAO = new UnderagedDAO();
    private ContactDAO contactDAO = new ContactDAO();

    private UnderagedMember currentUnderagedMember;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFormFields();
        loadData();
        setupEventHandlers();
        toggleFormMode(false);
    }

    private void setupTableColumns() {
        // Select column with checkboxes
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        // Basic info columns
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));

        // PIN column
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));

        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        // Date columns with formatting
        birthDateColumn.setCellValueFactory(cellData -> {
            LocalDate birthDate = cellData.getValue().getBirthDate();
            String formattedDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
            return new SimpleStringProperty(formattedDate);
        });

        memberSinceColumn.setCellValueFactory(cellData -> {
            LocalDate memberSince = cellData.getValue().getMemberSince();
            String formattedDate = memberSince != null ? memberSince.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
            return new SimpleStringProperty(formattedDate);
        });

        memberUntilColumn.setCellValueFactory(cellData -> {
            LocalDate memberUntil = cellData.getValue().getMemberUntil();
            String formattedDate = memberUntil != null ? memberUntil.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
            return new SimpleStringProperty(formattedDate);
        });

        // Member status column
        memberStatusColumn.setCellValueFactory(cellData -> {
            boolean isMember = cellData.getValue().isMember();
            return new SimpleStringProperty(isMember ? "Yes" : "No");
        });

        // Contact column - show parent/guardian name
        contactColumn.setCellValueFactory(cellData -> {
            int contactId = cellData.getValue().getContactId();
            Contact contact = contactsList.stream()
                    .filter(c -> c.getId() == contactId)
                    .findFirst()
                    .orElse(null);
            String contactName = contact != null ? contact.getFullName() : "Unknown";
            return new SimpleStringProperty(contactName);
        });

        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Make table editable
        underagedTableView.setEditable(true);
        underagedTableView.setItems(underagedMembersList);
    }

    private void setupFormFields() {
        // Setup gender combo box
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        // Setup filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Members Only", "Non-Members", "Under 12", "12-17"
        ));
        filterComboBox.setValue("All");

        // Auto-calculate age when birth date changes
        birthDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                int age = LocalDate.now().getYear() - newDate.getYear();
                if (LocalDate.now().getDayOfYear() < newDate.getDayOfYear()) {
                    age--;
                }
                ageField.setText(String.valueOf(age));
            }
        });

        // Setup contact combo box
        contactComboBox.setConverter(new javafx.util.StringConverter<Contact>() {
            @Override
            public String toString(Contact contact) {
                return contact != null ? contact.getFullName() : "";
            }

            @Override
            public Contact fromString(String string) {
                return contactsList.stream()
                        .filter(contact -> contact.getFullName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void loadData() {
        // Load contacts for combo box
        contactsList.clear();
        contactsList.addAll(contactDAO.getAllContacts());
        contactComboBox.setItems(contactsList);

        // Load underaged members
        refreshUnderagedMembersList();
    }

    private void refreshUnderagedMembersList() {
        underagedMembersList.clear();
        List<UnderagedMember> allMembers = underagedDAO.getAllUnderagedMembers();
        underagedMembersList.addAll(allMembers);

        // Apply current filter
        applyFilter();

        System.out.println("Loaded " + allMembers.size() + " underaged members");
    }

    private void setupEventHandlers() {
        // Table selection handler
        underagedTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null && !isEditMode) {
                        populateForm(newSelection);
                    }
                    updateButtonStates();
                }
        );

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilter());

        // Filter functionality
        filterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
    }

    @FXML
    private void handleAdd() {
        clearForm();
        toggleFormMode(true);
        isEditMode = false;
        currentUnderagedMember = null;
    }

    @FXML
    private void handleEdit() {
        UnderagedMember selected = underagedTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentUnderagedMember = selected;
            populateForm(selected);
            toggleFormMode(true);
            isEditMode = true;
        }
    }

    @FXML
    private void handleDelete() {
        UnderagedMember selected = underagedTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Confirmation");
            confirmation.setHeaderText("Delete Underaged Member");
            confirmation.setContentText("Are you sure you want to delete " + selected.getFullName() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (underagedDAO.deleteUnderagedMember(selected.getId())) {
                    refreshUnderagedMembersList();
                    clearForm();
                    showSuccessMessage("Underaged member deleted successfully!");
                } else {
                    showErrorMessage("Failed to delete underaged member.");
                }
            }
        }
    }

    @FXML
    private void handleDeleteSelected() {
        List<UnderagedMember> selectedMembers = underagedMembersList.stream()
                .filter(UnderagedMember::isSelected)
                .toList();

        if (selectedMembers.isEmpty()) {
            showWarningMessage("No underaged members selected for deletion.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Delete Selected Underaged Members");
        confirmation.setContentText("Are you sure you want to delete " + selectedMembers.size() + " selected underaged members?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<Integer> idsToDelete = selectedMembers.stream()
                    .map(UnderagedMember::getId)
                    .toList();

            if (underagedDAO.deleteUnderagedMembers(idsToDelete)) {
                refreshUnderagedMembersList();
                clearForm();
                showSuccessMessage("Selected underaged members deleted successfully!");
            } else {
                showErrorMessage("Failed to delete selected underaged members.");
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        UnderagedMember underagedMember = createUnderagedMemberFromForm();
        boolean success;

        if (isEditMode && currentUnderagedMember != null) {
            underagedMember.setId(currentUnderagedMember.getId());
            underagedMember.setCreatedAt(currentUnderagedMember.getCreatedAt());
            success = underagedDAO.updateUnderagedMember(underagedMember);
        } else {
            underagedMember.setCreatedAt(LocalDate.now().toString());
            success = underagedDAO.createUnderagedMember(underagedMember);
        }

        if (success) {
            refreshUnderagedMembersList();
            toggleFormMode(false);
            clearForm();
            showSuccessMessage("Underaged member saved successfully!");
        } else {
            showErrorMessage("Failed to save underaged member.");
        }
    }

    @FXML
    private void handleCancel() {
        toggleFormMode(false);
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        refreshUnderagedMembersList();
        showSuccessMessage("Data refreshed successfully!");
    }

    private void populateForm(UnderagedMember underagedMember) {
        firstNameField.setText(underagedMember.getFirstName());
        lastNameField.setText(underagedMember.getLastName());
        birthDatePicker.setValue(underagedMember.getBirthDate());
        ageField.setText(String.valueOf(underagedMember.getAge()));
        pinField.setText(underagedMember.getPin()); // Added PIN field population
        genderComboBox.setValue(underagedMember.getGender());
        isMemberCheckBox.setSelected(underagedMember.isMember());
        memberSincePicker.setValue(underagedMember.getMemberSince());
        memberUntilPicker.setValue(underagedMember.getMemberUntil());
        noteTextArea.setText(underagedMember.getNote());

        // Set contact
        Contact contact = contactsList.stream()
                .filter(c -> c.getId() == underagedMember.getContactId())
                .findFirst()
                .orElse(null);
        contactComboBox.setValue(contact);
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        birthDatePicker.setValue(null);
        ageField.clear();
        pinField.clear(); // Added PIN field clearing
        genderComboBox.setValue(null);
        isMemberCheckBox.setSelected(false);
        memberSincePicker.setValue(null);
        memberUntilPicker.setValue(null);
        contactComboBox.setValue(null);
        noteTextArea.clear();
    }

    private UnderagedMember createUnderagedMemberFromForm() {
        UnderagedMember underagedMember = new UnderagedMember();

        underagedMember.setFirstName(firstNameField.getText().trim());
        underagedMember.setLastName(lastNameField.getText().trim());
        underagedMember.setBirthDate(birthDatePicker.getValue());
        underagedMember.setPin(pinField.getText().trim()); // Added PIN field handling

        try {
            underagedMember.setAge(Integer.parseInt(ageField.getText().trim()));
        } catch (NumberFormatException e) {
            underagedMember.setAge(0);
        }

        underagedMember.setGender(genderComboBox.getValue());
        underagedMember.setMember(isMemberCheckBox.isSelected());
        underagedMember.setMemberSince(memberSincePicker.getValue());
        underagedMember.setMemberUntil(memberUntilPicker.getValue());
        underagedMember.setNote(noteTextArea.getText().trim());
        underagedMember.setUpdatedAt(LocalDate.now().toString());

        Contact selectedContact = contactComboBox.getValue();
        if (selectedContact != null) {
            underagedMember.setContactId(selectedContact.getId());
        }

        return underagedMember;
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.add("First name is required.");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errors.add("Last name is required.");
        }
        if (birthDatePicker.getValue() == null) {
            errors.add("Birth date is required.");
        }
        if (contactComboBox.getValue() == null) {
            errors.add("Parent/Guardian contact is required.");
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterValue = filterComboBox.getValue();

        List<UnderagedMember> allMembers = underagedDAO.getAllUnderagedMembers();
        List<UnderagedMember> filteredMembers = allMembers.stream()
                .filter(member -> {
                    // Search filter - now includes PIN search
                    boolean matchesSearch = searchText.isEmpty() ||
                            member.getFirstName().toLowerCase().contains(searchText) ||
                            member.getLastName().toLowerCase().contains(searchText) ||
                            (member.getPin() != null && member.getPin().toLowerCase().contains(searchText)) ||
                            (member.getNote() != null && member.getNote().toLowerCase().contains(searchText));

                    // Category filter
                    boolean matchesFilter = switch (filterValue) {
                        case "Members Only" -> member.isMember();
                        case "Non-Members" -> !member.isMember();
                        case "Under 12" -> member.getAge() < 12;
                        case "12-17" -> member.getAge() >= 12 && member.getAge() <= 17;
                        default -> true; // "All"
                    };

                    return matchesSearch && matchesFilter;
                })
                .toList();

        underagedMembersList.clear();
        underagedMembersList.addAll(filteredMembers);
    }

    private void toggleFormMode(boolean editMode) {
        // Enable/disable form fields
        firstNameField.setDisable(!editMode);
        lastNameField.setDisable(!editMode);
        birthDatePicker.setDisable(!editMode);
        ageField.setDisable(true); // Always disabled as it's calculated
        pinField.setDisable(!editMode); // Added PIN field toggle
        genderComboBox.setDisable(!editMode);
        isMemberCheckBox.setDisable(!editMode);
        memberSincePicker.setDisable(!editMode);
        memberUntilPicker.setDisable(!editMode);
        contactComboBox.setDisable(!editMode);
        noteTextArea.setDisable(!editMode);

        // Show/hide form buttons
        saveButton.setVisible(editMode);
        cancelButton.setVisible(editMode);

        // Enable/disable table action buttons
        addButton.setDisable(editMode);
        editButton.setDisable(editMode);
        deleteButton.setDisable(editMode);
        deleteSelectedButton.setDisable(editMode);
        refreshButton.setDisable(editMode);
    }

    private void updateButtonStates() {
        UnderagedMember selected = underagedTableView.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        boolean hasSelectedItems = underagedMembersList.stream().anyMatch(UnderagedMember::isSelected);

        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        deleteSelectedButton.setDisable(!hasSelectedItems);
    }

    // Message helper methods
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }
}