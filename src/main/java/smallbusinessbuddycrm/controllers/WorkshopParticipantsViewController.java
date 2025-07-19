package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.*;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.model.WorkshopParticipant.ParticipantType;
import smallbusinessbuddycrm.model.WorkshopParticipant.PaymentStatus;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.model.Teacher;



import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WorkshopParticipantsViewController implements Initializable {

    // Current Participants Tab
    @FXML private TabPane participantsTabPane;
    @FXML private TableView<Map<String, Object>> participantsTable;
    @FXML private TableColumn<Map<String, Object>, Boolean> selectParticipantColumn;
    @FXML private TableColumn<Map<String, Object>, String> participantNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> participantTypeColumn;
    @FXML private TableColumn<Map<String, Object>, String> participantAgeColumn;
    @FXML private TableColumn<Map<String, Object>, String> participantEmailColumn;
    @FXML private TableColumn<Map<String, Object>, String> participantPhoneColumn;
    @FXML private TableColumn<Map<String, Object>, String> parentInfoColumn;
    @FXML private TableColumn<Map<String, Object>, String> paymentStatusColumn;
    @FXML private TableColumn<Map<String, Object>, String> notesColumn;
    @FXML private TableColumn<Map<String, Object>, String> enrollmentDateColumn;
    @FXML private TableColumn<Map<String, Object>, Void> actionsColumn;
    @FXML private Button manageTeachersButton;
    @FXML private Label teacherLabel;

    // Add Adults Tab
    @FXML private TableView<Contact> availableAdultsTable;
    @FXML private TableColumn<Contact, Boolean> selectAdultColumn;
    @FXML private TableColumn<Contact, String> adultNameColumn;
    @FXML private TableColumn<Contact, String> adultEmailColumn;
    @FXML private TableColumn<Contact, String> adultPhoneColumn;
    @FXML private TableColumn<Contact, String> adultMemberStatusColumn;
    @FXML private TableColumn<Contact, String> adultAgeColumn;

    // Add Children Tab
    @FXML private TableView<UnderagedMember> availableChildrenTable;
    @FXML private TableColumn<UnderagedMember, Boolean> selectChildColumn;
    @FXML private TableColumn<UnderagedMember, String> childNameColumn;
    @FXML private TableColumn<UnderagedMember, String> childAgeColumn;
    @FXML private TableColumn<UnderagedMember, String> childGenderColumn;
    @FXML private TableColumn<UnderagedMember, String> childMemberStatusColumn;
    @FXML private TableColumn<UnderagedMember, String> parentNameColumn;
    @FXML private TableColumn<UnderagedMember, String> parentEmailColumn;
    @FXML private TableColumn<UnderagedMember, String> parentPhoneColumn;

    // UI Controls
    @FXML private Label workshopNameLabel;
    @FXML private Label totalParticipantsLabel;
    @FXML private Label adultsCountLabel;
    @FXML private Label childrenCountLabel;
    @FXML private Label paidCountLabel;
    @FXML private Label pendingCountLabel;

    // Search and filter controls
    @FXML private TextField searchParticipantsField;
    @FXML private ComboBox<String> participantTypeFilter;
    @FXML private ComboBox<String> paymentStatusFilter;
    @FXML private TextField searchAdultsField;
    @FXML private TextField searchChildrenField;
    @FXML private ComboBox<String> ageRangeFilter;
    @FXML private CheckBox membersOnlyAdultsFilter;
    @FXML private CheckBox membersOnlyChildrenFilter;

    // Action buttons
    @FXML private Button removeSelectedButton;
    @FXML private Button exportParticipantsButton;
    @FXML private Button refreshButton;
    @FXML private Button selectAllAdultsButton;
    @FXML private Button clearAdultsSelectionButton;
    @FXML private Button selectAllChildrenButton;
    @FXML private Button clearChildrenSelectionButton;
    @FXML private Button addSelectedAdultsButton;
    @FXML private Button addSelectedChildrenButton;
    @FXML private Button addFamilyButton;

    // Form controls for adding participants
    @FXML private ComboBox<PaymentStatus> adultsPaymentStatusCombo;
    @FXML private ComboBox<PaymentStatus> childrenPaymentStatusCombo;
    @FXML private TextField adultsNotesField;
    @FXML private TextField childrenNotesField;

    // Labels for counts
    @FXML private Label availableAdultsCountLabel;
    @FXML private Label availableChildrenCountLabel;

    // Data lists
    private ObservableList<Map<String, Object>> participantsList = FXCollections.observableArrayList();
    private FilteredList<Map<String, Object>> filteredParticipantsList;
    private ObservableList<Contact> availableAdultsList = FXCollections.observableArrayList();
    private FilteredList<Contact> filteredAdultsList;
    private ObservableList<UnderagedMember> availableChildrenList = FXCollections.observableArrayList();
    private FilteredList<UnderagedMember> filteredChildrenList;

    // DAOs
    private WorkshopParticipantDAO participantDAO = new WorkshopParticipantDAO();
    private ContactDAO contactDAO = new ContactDAO();
    private UnderagedDAO underagedDAO = new UnderagedDAO();
    private TeacherDAO teacherDAO = new TeacherDAO();

    // Current workshop
    private Workshop currentWorkshop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("WorkshopParticipantsViewController.initialize() called");

        // Initialize database first
        DatabaseConnection.initializeDatabase();

        setupTables();
        setupSearchAndFilters();
        setupEventHandlers();
        setupComboBoxes();
        loadAvailableParticipants();

        System.out.println("WorkshopParticipantsViewController initialized successfully");
    }

    public void setWorkshop(Workshop workshop) {
        this.currentWorkshop = workshop;
        if (workshop != null) {
            workshopNameLabel.setText("Workshop: " + workshop.getName() + " (" + workshop.getFormattedFromDate() + " - " + workshop.getFormattedToDate() + ")");
            loadWorkshopParticipants();
            updateStatistics();
        } else {
            workshopNameLabel.setText("No workshop selected");
            participantsList.clear();
            clearStatistics();
        }
    }

    private void setupTables() {
        setupParticipantsTable();
        setupAvailableAdultsTable();
        setupAvailableChildrenTable();
    }

    private void setupParticipantsTable() {
        // Participants table setup
        selectParticipantColumn.setCellFactory(tc -> new TableCell<Map<String, Object>, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getIndex() >= participantsTable.getItems().size()) {
                    setGraphic(null);
                } else {
                    Map<String, Object> participant = participantsTable.getItems().get(getIndex());
                    boolean isSelected = (Boolean) participant.getOrDefault("selected", false);
                    checkBox.setSelected(isSelected);
                    checkBox.setOnAction(event -> {
                        participant.put("selected", checkBox.isSelected());
                        System.out.println("Participant " + participant.get("participant_name") + " selected: " + checkBox.isSelected());
                    });
                    setGraphic(checkBox);
                }
            }
        });

        participantNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("participant_name")));

        participantTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("participant_type")));

        participantAgeColumn.setCellValueFactory(cellData -> {
            Object age = cellData.getValue().get("participant_age");
            return new SimpleStringProperty(age != null ? age.toString() : "");
        });

        participantEmailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("participant_email")));

        participantPhoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("participant_phone")));

        parentInfoColumn.setCellValueFactory(cellData -> {
            String type = (String) cellData.getValue().get("participant_type");
            if ("CHILD".equals(type)) {
                String parentName = (String) cellData.getValue().get("parent_name");
                String parentPhone = (String) cellData.getValue().get("parent_phone");
                if (parentName != null && parentPhone != null) {
                    return new SimpleStringProperty(parentName + " (" + parentPhone + ")");
                } else if (parentName != null) {
                    return new SimpleStringProperty(parentName);
                }
            }
            return new SimpleStringProperty("");
        });

        paymentStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("payment_status")));

        notesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("notes")));

        enrollmentDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("created_at")));

        // Actions column
        actionsColumn.setCellFactory(tc -> new TableCell<Map<String, Object>, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button removeButton = new Button("Remove");
            private final javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(5);

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");

                editButton.setOnAction(event -> {
                    Map<String, Object> participant = getTableView().getItems().get(getIndex());
                    handleEditParticipant(participant);
                });

                removeButton.setOnAction(event -> {
                    Map<String, Object> participant = getTableView().getItems().get(getIndex());
                    handleRemoveParticipant(participant);
                });

                actionBox.getChildren().addAll(editButton, removeButton);
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

        participantsTable.setItems(participantsList);
    }

    private void setupAvailableAdultsTable() {
        // FIXED: Custom checkbox setup for adults
        selectAdultColumn.setCellFactory(tc -> {
            CheckBox checkBox = new CheckBox();
            TableCell<Contact, Boolean> cell = new TableCell<Contact, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Contact contact = getTableView().getItems().get(getIndex());
                        if (contact != null) {
                            checkBox.setSelected(contact.isSelected());
                            checkBox.setOnAction(e -> {
                                contact.setSelected(checkBox.isSelected());
                                System.out.println("Adult " + contact.getFullName() + " selected: " + checkBox.isSelected());
                            });
                            setGraphic(checkBox);
                        }
                    }
                }
            };
            return cell;
        });

        adultNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));
        adultEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        adultPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        adultMemberStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ? "Member" : "Non-member"));
        adultAgeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getAge())));

        availableAdultsTable.setEditable(true);
        availableAdultsTable.setItems(filteredAdultsList);
    }

    private void setupAvailableChildrenTable() {
        // FIXED: Custom checkbox setup for children
        selectChildColumn.setCellFactory(tc -> {
            CheckBox checkBox = new CheckBox();
            TableCell<UnderagedMember, Boolean> cell = new TableCell<UnderagedMember, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        UnderagedMember child = getTableView().getItems().get(getIndex());
                        if (child != null) {
                            checkBox.setSelected(child.isSelected());
                            checkBox.setOnAction(e -> {
                                child.setSelected(checkBox.isSelected());
                                System.out.println("Child " + child.getFullName() + " selected: " + checkBox.isSelected());
                            });
                            setGraphic(checkBox);
                        }
                    }
                }
            };
            return cell;
        });

        childNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));
        childAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        childGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        childMemberStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ? "Member" : "Non-member"));

        // Parent info columns - cache contacts for performance
        java.util.List<Contact> contacts = contactDAO.getAllContacts();
        Map<Integer, Contact> contactMap = new HashMap<>();
        for (Contact contact : contacts) {
            contactMap.put(contact.getId(), contact);
        }

        parentNameColumn.setCellValueFactory(cellData -> {
            int contactId = cellData.getValue().getContactId();
            Contact parent = contactMap.get(contactId);
            return new SimpleStringProperty(parent != null ? parent.getFullName() : "Unknown");
        });

        parentEmailColumn.setCellValueFactory(cellData -> {
            int contactId = cellData.getValue().getContactId();
            Contact parent = contactMap.get(contactId);
            return new SimpleStringProperty(parent != null ? parent.getEmail() : "");
        });

        parentPhoneColumn.setCellValueFactory(cellData -> {
            int contactId = cellData.getValue().getContactId();
            Contact parent = contactMap.get(contactId);
            return new SimpleStringProperty(parent != null ? parent.getPhoneNum() : "");
        });

        availableChildrenTable.setEditable(true);
        availableChildrenTable.setItems(filteredChildrenList);
    }

    private void setupSearchAndFilters() {
        // Participants search and filter
        filteredParticipantsList = new FilteredList<>(participantsList, p -> true);
        participantsTable.setItems(filteredParticipantsList);

        // Available adults filter
        filteredAdultsList = new FilteredList<>(availableAdultsList, p -> true);
        availableAdultsTable.setItems(filteredAdultsList);

        // Available children filter
        filteredChildrenList = new FilteredList<>(availableChildrenList, p -> true);
        availableChildrenTable.setItems(filteredChildrenList);

        // Set up search listeners
        searchParticipantsField.textProperty().addListener((obs, old, newValue) -> updateParticipantsFilter());
        searchAdultsField.textProperty().addListener((obs, old, newValue) -> updateAdultsFilter());
        searchChildrenField.textProperty().addListener((obs, old, newValue) -> updateChildrenFilter());
    }

    private void setupComboBoxes() {
        // Payment status combos
        adultsPaymentStatusCombo.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        adultsPaymentStatusCombo.setValue(PaymentStatus.PENDING);

        childrenPaymentStatusCombo.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        childrenPaymentStatusCombo.setValue(PaymentStatus.PENDING);

        // Filter combos
        participantTypeFilter.setItems(FXCollections.observableArrayList("All Types", "Adults", "Children"));
        participantTypeFilter.setValue("All Types");
        participantTypeFilter.valueProperty().addListener((obs, old, newValue) -> updateParticipantsFilter());

        paymentStatusFilter.setItems(FXCollections.observableArrayList("All Payments", "PENDING", "PAID", "REFUNDED", "CANCELLED"));
        paymentStatusFilter.setValue("All Payments");
        paymentStatusFilter.valueProperty().addListener((obs, old, newValue) -> updateParticipantsFilter());

        ageRangeFilter.setItems(FXCollections.observableArrayList("All Ages", "Under 6", "6-12", "13-17", "18+"));
        ageRangeFilter.setValue("All Ages");
        ageRangeFilter.valueProperty().addListener((obs, old, newValue) -> updateChildrenFilter());
    }

    private void setupEventHandlers() {
        removeSelectedButton.setOnAction(e -> handleRemoveSelected());
        exportParticipantsButton.setOnAction(e -> handleExportParticipants());
        refreshButton.setOnAction(e -> handleRefresh());

        selectAllAdultsButton.setOnAction(e -> handleSelectAllAdults());
        clearAdultsSelectionButton.setOnAction(e -> handleClearAdultsSelection());
        selectAllChildrenButton.setOnAction(e -> handleSelectAllChildren());
        clearChildrenSelectionButton.setOnAction(e -> handleClearChildrenSelection());

        addSelectedAdultsButton.setOnAction(e -> handleAddSelectedAdults());
        addSelectedChildrenButton.setOnAction(e -> handleAddSelectedChildren());
        addFamilyButton.setOnAction(e -> handleAddFamily());

        membersOnlyAdultsFilter.setOnAction(e -> updateAdultsFilter());
        membersOnlyChildrenFilter.setOnAction(e -> updateChildrenFilter());

        manageTeachersButton.setOnAction(e -> handleManageTeachers());
    }

    private void handleManageTeachers() {
        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Workshop Selected");
            warning.setHeaderText("Please select a workshop first");
            warning.setContentText("You need to select a workshop before you can assign a teacher.");
            warning.showAndWait();
            return;
        }

        // Check if there are participants first
        try {
            List<Map<String, Object>> participants = participantDAO.getWorkshopParticipantsWithDetails(currentWorkshop.getId());

            if (participants.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Add Participants First");
                info.setHeaderText("This workshop needs participants before you can assign a teacher");
                info.setContentText("Please add some participants to this workshop first using the 'Add Participants' tab, " +
                        "then come back to assign a teacher.\n\n" +
                        "Teachers are assigned to participants, so participants must be added first.");

                ButtonType addParticipantsButton = new ButtonType("Go to Add Participants");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                info.getButtonTypes().setAll(addParticipantsButton, cancelButton);

                Optional<ButtonType> result = info.showAndWait();
                if (result.isPresent() && result.get() == addParticipantsButton) {
                    // Switch to the Add Participants tab
                    participantsTabPane.getSelectionModel().select(1); // Index 1 = Add Participants tab
                }
                return;
            }

            // Proceed with teacher assignment if participants exist
            try {
                Stage currentStage = (Stage) manageTeachersButton.getScene().getWindow();
                WorkshopTeacherAssignmentDialog dialog = new WorkshopTeacherAssignmentDialog(currentStage, currentWorkshop);

                if (dialog.showAndWait()) {
                    // Refresh teacher display and statistics after assignment
                    updateTeacherDisplay();
                    updateStatistics();

                    // Show success message with teacher info
                    List<Teacher> assignedTeachers = teacherDAO.getTeachersForWorkshop(currentWorkshop.getId());
                    String message;

                    if (assignedTeachers.isEmpty()) {
                        message = "Teacher assignment updated. No teacher is currently assigned to this workshop.";
                    } else {
                        Teacher teacher = assignedTeachers.get(0);
                        message = "Teacher assignment updated successfully!\n\n" +
                                "Assigned Teacher: " + teacher.getFullName() +
                                (teacher.getEmail() != null ? "\nEmail: " + teacher.getEmail() : "") +
                                (teacher.getPhoneNum() != null ? "\nPhone: " + teacher.getPhoneNum() : "") +
                                "\n\nAssigned to " + participants.size() + " participant" + (participants.size() == 1 ? "" : "s") + ".";
                    }

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Teacher Assignment Updated");
                    success.setHeaderText("Teacher assignment updated");
                    success.setContentText(message);
                    success.showAndWait();
                }

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to open teacher assignment");
                error.setContentText("An error occurred: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Failed to check participants");
            error.setContentText("Could not check if workshop has participants: " + e.getMessage());
            error.showAndWait();
            e.printStackTrace();
        }
    }

    private void loadWorkshopParticipants() {
        if (currentWorkshop == null) return;

        try {
            java.util.List<Map<String, Object>> participants = participantDAO.getWorkshopParticipantsWithDetails(currentWorkshop.getId());

            // Initialize selection property for each participant
            for (Map<String, Object> participant : participants) {
                participant.put("selected", false);
            }

            participantsList.setAll(participants);
            System.out.println("Loaded " + participants.size() + " participants for workshop: " + currentWorkshop.getName());
        } catch (Exception e) {
            System.err.println("Error loading workshop participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAvailableParticipants() {
        try {
            // Load all contacts and initialize selection
            java.util.List<Contact> allContacts = contactDAO.getAllContacts();
            for (Contact contact : allContacts) {
                contact.setSelected(false);
            }
            availableAdultsList.setAll(allContacts);
            availableAdultsCountLabel.setText("(" + allContacts.size() + " available)");

            // Load all underaged members and initialize selection
            java.util.List<UnderagedMember> allChildren = underagedDAO.getAllUnderagedMembers();
            for (UnderagedMember child : allChildren) {
                child.setSelected(false);
            }
            availableChildrenList.setAll(allChildren);
            availableChildrenCountLabel.setText("(" + allChildren.size() + " available)");

            System.out.println("Loaded " + allContacts.size() + " adults and " + allChildren.size() + " children");
        } catch (Exception e) {
            System.err.println("Error loading available participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTeacherDisplay() {
        try {
            List<Teacher> assignedTeachers = teacherDAO.getTeachersForWorkshop(currentWorkshop.getId());

            if (assignedTeachers.isEmpty()) {
                teacherLabel.setText("Teacher: Not assigned");
                teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;"); // Red color
            } else {
                Teacher teacher = assignedTeachers.get(0); // Get the first teacher
                teacherLabel.setText("Teacher: " + teacher.getFullName());
                teacherLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 12px;"); // Green color
            }
        } catch (Exception e) {
            teacherLabel.setText("Teacher: Error loading");
            teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
            e.printStackTrace();
        }
    }



    private void updateStatistics() {
        if (currentWorkshop == null) {
            clearStatistics();
            return;
        }

        try {
            Map<String, Integer> stats = participantDAO.getWorkshopStatistics(currentWorkshop.getId());

            totalParticipantsLabel.setText("Total: " + stats.getOrDefault("total_participants", 0));
            adultsCountLabel.setText("Adults: " + stats.getOrDefault("adult_count", 0));
            childrenCountLabel.setText("Children: " + stats.getOrDefault("child_count", 0));
            paidCountLabel.setText("Paid: " + stats.getOrDefault("paid_count", 0));
            pendingCountLabel.setText("Pending: " + stats.getOrDefault("pending_count", 0));
            updateTeacherDisplay();

        } catch (Exception e) {


            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
            clearStatistics();
        }
    }


    private void clearStatistics() {
        totalParticipantsLabel.setText("Total: 0");
        adultsCountLabel.setText("Adults: 0");
        childrenCountLabel.setText("Children: 0");
        paidCountLabel.setText("Paid: 0");
        pendingCountLabel.setText("Pending: 0");
        teacherLabel.setText("Teacher: Not assigned");
        teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
    }

    // Filter methods
    private void updateParticipantsFilter() {
        String searchText = searchParticipantsField.getText().toLowerCase().trim();
        String typeFilter = participantTypeFilter.getValue();
        String paymentFilter = paymentStatusFilter.getValue();

        filteredParticipantsList.setPredicate(participant -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    ((String) participant.get("participant_name")).toLowerCase().contains(searchText);

            // Type filter
            boolean matchesType = "All Types".equals(typeFilter) ||
                    ("Adults".equals(typeFilter) && "ADULT".equals(participant.get("participant_type"))) ||
                    ("Children".equals(typeFilter) && "CHILD".equals(participant.get("participant_type")));

            // Payment filter
            boolean matchesPayment = "All Payments".equals(paymentFilter) ||
                    paymentFilter.equals(participant.get("payment_status"));

            return matchesSearch && matchesType && matchesPayment;
        });
    }

    private void updateAdultsFilter() {
        String searchText = searchAdultsField.getText().toLowerCase().trim();
        boolean membersOnly = membersOnlyAdultsFilter.isSelected();

        filteredAdultsList.setPredicate(contact -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    contact.getFullName().toLowerCase().contains(searchText) ||
                    (contact.getEmail() != null && contact.getEmail().toLowerCase().contains(searchText));

            boolean matchesMember = !membersOnly || contact.isMember();

            return matchesSearch && matchesMember;
        });
    }

    private void updateChildrenFilter() {
        String searchText = searchChildrenField.getText().toLowerCase().trim();
        String ageRange = ageRangeFilter.getValue();
        boolean membersOnly = membersOnlyChildrenFilter.isSelected();

        filteredChildrenList.setPredicate(child -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    child.getFullName().toLowerCase().contains(searchText);

            boolean matchesAge = "All Ages".equals(ageRange) ||
                    ("Under 6".equals(ageRange) && child.getAge() < 6) ||
                    ("6-12".equals(ageRange) && child.getAge() >= 6 && child.getAge() <= 12) ||
                    ("13-17".equals(ageRange) && child.getAge() >= 13 && child.getAge() <= 17) ||
                    ("18+".equals(ageRange) && child.getAge() >= 18);

            boolean matchesMember = !membersOnly || child.isMember();

            return matchesSearch && matchesAge && matchesMember;
        });
    }

    // COMPLETE EVENT HANDLERS WITH FULL FUNCTIONALITY

    private void handleEditParticipant(Map<String, Object> participant) {
        try {
            // Create a dialog for editing participant details
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Participant");
            dialog.setHeaderText("Edit participant: " + participant.get("participant_name"));

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            // Payment Status
            Label paymentLabel = new Label("Payment Status:");
            ComboBox<PaymentStatus> paymentCombo = new ComboBox<>();
            paymentCombo.getItems().setAll(PaymentStatus.values());

            // Set current payment status
            String currentPaymentStatus = (String) participant.get("payment_status");
            try {
                PaymentStatus currentStatus = PaymentStatus.valueOf(currentPaymentStatus);
                paymentCombo.setValue(currentStatus);
            } catch (Exception e) {
                paymentCombo.setValue(PaymentStatus.PENDING);
            }

            // Notes
            Label notesLabel = new Label("Notes:");
            TextArea notesArea = new TextArea((String) participant.get("notes"));
            notesArea.setPrefRowCount(3);
            notesArea.setWrapText(true);

            content.getChildren().addAll(paymentLabel, paymentCombo, notesLabel, notesArea);
            dialog.getDialogPane().setContent(content);

            // Add buttons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Show dialog and handle result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update the participant
                PaymentStatus newPaymentStatus = paymentCombo.getValue();
                String newNotes = notesArea.getText().trim();

                try {
                    // Get participant ID and type
                    int participantId = (Integer) participant.get("participant_id");

                    // Update in database
                    boolean success = participantDAO.updateParticipant(participantId, newPaymentStatus,
                            newNotes.isEmpty() ? null : newNotes);

                    if (success) {
                        // Update the local data
                        participant.put("payment_status", newPaymentStatus.toString());
                        participant.put("notes", newNotes);
                        participant.put("updated_at", java.time.LocalDateTime.now().toString());

                        // Refresh the table
                        participantsTable.refresh();
                        updateStatistics();

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Participant Updated");
                        successAlert.setContentText("Participant details updated successfully!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Update Failed");
                        errorAlert.setHeaderText("Database Error");
                        errorAlert.setContentText("Failed to update participant in the database.");
                        errorAlert.showAndWait();
                    }

                } catch (Exception e) {
                    System.err.println("Error updating participant: " + e.getMessage());
                    e.printStackTrace();

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Update Failed");
                    errorAlert.setHeaderText("Error");
                    errorAlert.setContentText("An error occurred while updating the participant: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            }

        } catch (Exception e) {
            System.err.println("Error opening edit dialog: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Dialog Error");
            errorAlert.setHeaderText("Failed to Open Edit Dialog");
            errorAlert.setContentText("An error occurred while opening the edit dialog: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void handleRemoveParticipant(Map<String, Object> participant) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Participant");
        confirmation.setHeaderText("Remove " + participant.get("participant_name") + "?");
        confirmation.setContentText("Are you sure you want to remove this participant from the workshop? This action cannot be undone.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                int participantId = (Integer) participant.get("participant_id");

                boolean success = participantDAO.removeParticipant(participantId);

                if (success) {
                    // Remove from the list
                    participantsList.remove(participant);
                    updateStatistics();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Participant Removed");
                    successAlert.setContentText("Participant has been removed from the workshop successfully!");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Removal Failed");
                    errorAlert.setHeaderText("Database Error");
                    errorAlert.setContentText("Failed to remove participant from the database.");
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error removing participant: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Removal Failed");
                errorAlert.setHeaderText("Error");
                errorAlert.setContentText("An error occurred while removing the participant: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleRemoveSelected() {
        System.out.println("Remove selected participants clicked"); // Debug

        // Get selected participants
        java.util.List<Map<String, Object>> selectedParticipants = participantsList.stream()
                .filter(p -> (Boolean) p.getOrDefault("selected", false))
                .collect(Collectors.toList());

        System.out.println("Selected participants count: " + selectedParticipants.size()); // Debug

        if (selectedParticipants.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setHeaderText("No participants selected");
            warning.setContentText("Please select one or more participants to remove using the checkboxes.");
            warning.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Selected Participants");
        confirmation.setHeaderText("Remove " + selectedParticipants.size() + " participants?");
        confirmation.setContentText("Are you sure you want to remove the selected participants from this workshop? This action cannot be undone.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Collect participant IDs
                java.util.List<Integer> participantIds = selectedParticipants.stream()
                        .map(p -> (Integer) p.get("participant_id"))
                        .collect(Collectors.toList());

                System.out.println("Attempting to remove participant IDs: " + participantIds); // Debug

                boolean success = participantDAO.removeParticipants(participantIds);

                if (success) {
                    // Remove from the list
                    participantsList.removeAll(selectedParticipants);
                    updateStatistics();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Participants Removed");
                    successAlert.setContentText("Successfully removed " + selectedParticipants.size() + " participants from the workshop!");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Removal Failed");
                    errorAlert.setHeaderText("Database Error");
                    errorAlert.setContentText("Failed to remove some or all participants from the database.");
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error removing selected participants: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Removal Failed");
                errorAlert.setHeaderText("Error");
                errorAlert.setContentText("An error occurred while removing participants: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleExportParticipants() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Export Participants");
        info.setContentText("Export participants functionality coming soon!");
        info.showAndWait();
    }

    private void handleRefresh() {
        loadWorkshopParticipants();
        loadAvailableParticipants();
        updateStatistics();

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Refreshed");
        success.setContentText("Participant data refreshed successfully!");
        success.showAndWait();
    }

    private void handleSelectAllAdults() {
        System.out.println("Select all adults clicked"); // Debug
        filteredAdultsList.forEach(contact -> contact.setSelected(true));
        availableAdultsTable.refresh();

        // Debug: Print selection count
        long selectedCount = availableAdultsList.stream().filter(Contact::isSelected).count();
        System.out.println("Selected adults: " + selectedCount);
    }

    private void handleClearAdultsSelection() {
        System.out.println("Clear adults selection clicked"); // Debug
        availableAdultsList.forEach(contact -> contact.setSelected(false));
        availableAdultsTable.refresh();
    }

    private void handleSelectAllChildren() {
        System.out.println("Select all children clicked"); // Debug
        filteredChildrenList.forEach(child -> child.setSelected(true));
        availableChildrenTable.refresh();

        // Debug: Print selection count
        long selectedCount = availableChildrenList.stream().filter(UnderagedMember::isSelected).count();
        System.out.println("Selected children: " + selectedCount);
    }

    private void handleClearChildrenSelection() {
        System.out.println("Clear children selection clicked"); // Debug
        availableChildrenList.forEach(child -> child.setSelected(false));
        availableChildrenTable.refresh();
    }

    private void handleAddSelectedAdults() {
        System.out.println("Add selected adults clicked"); // Debug

        java.util.List<Contact> selectedAdults = availableAdultsList.stream()
                .filter(Contact::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected adults count: " + selectedAdults.size()); // Debug

        if (selectedAdults.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setContentText("Please select at least one adult to add.");
            warning.showAndWait();
            return;
        }

        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Workshop");
            warning.setContentText("Please select a workshop first.");
            warning.showAndWait();
            return;
        }

        try {
            PaymentStatus paymentStatus = adultsPaymentStatusCombo.getValue();
            String notes = adultsNotesField.getText().trim();
            String timestamp = java.time.LocalDateTime.now().toString();

            java.util.List<Contact> addedContacts = new ArrayList<>();
            java.util.List<String> skippedContacts = new ArrayList<>();

            for (Contact contact : selectedAdults) {
                // Check if participant already exists
                if (participantDAO.participantExists(currentWorkshop.getId(), contact.getId(), 0)) {
                    skippedContacts.add(contact.getFullName());
                    contact.setSelected(false); // Clear selection
                    continue;
                }

                WorkshopParticipant participant = new WorkshopParticipant();
                participant.setWorkshopId(currentWorkshop.getId());
                participant.setContactId(contact.getId());
                participant.setParticipantType(ParticipantType.ADULT);
                participant.setPaymentStatus(paymentStatus);
                participant.setNotes(notes.isEmpty() ? null : notes);
                participant.setCreatedAt(timestamp);
                participant.setUpdatedAt(timestamp);

                if (participantDAO.addParticipant(participant)) {
                    addedContacts.add(contact);
                }
                contact.setSelected(false); // Clear selection
            }

            // Clear form
            adultsNotesField.clear();

            // Refresh data
            loadWorkshopParticipants();
            updateStatistics();

            // Show results
            StringBuilder message = new StringBuilder();
            if (!addedContacts.isEmpty()) {
                message.append("Successfully added ").append(addedContacts.size()).append(" adults to the workshop!");
            }
            if (!skippedContacts.isEmpty()) {
                if (message.length() > 0) message.append("\n\n");
                message.append("Skipped ").append(skippedContacts.size()).append(" adults who were already in the workshop:\n");
                message.append(String.join(", ", skippedContacts));
            }

            Alert result = new Alert(addedContacts.isEmpty() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
            result.setTitle("Add Adults Result");
            result.setHeaderText(addedContacts.isEmpty() ? "No Adults Added" : "Adults Added");
            result.setContentText(message.toString());
            result.showAndWait();

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Addition Failed");
            error.setContentText("Failed to add adults: " + e.getMessage());
            error.showAndWait();
            e.printStackTrace();
        }
    }

    private void handleAddSelectedChildren() {
        System.out.println("Add selected children clicked"); // Debug

        java.util.List<UnderagedMember> selectedChildren = availableChildrenList.stream()
                .filter(UnderagedMember::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected children count: " + selectedChildren.size()); // Debug

        if (selectedChildren.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setContentText("Please select at least one child to add.");
            warning.showAndWait();
            return;
        }

        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Workshop");
            warning.setContentText("Please select a workshop first.");
            warning.showAndWait();
            return;
        }

        try {
            PaymentStatus paymentStatus = childrenPaymentStatusCombo.getValue();
            String notes = childrenNotesField.getText().trim();
            String timestamp = java.time.LocalDateTime.now().toString();

            java.util.List<UnderagedMember> addedChildren = new ArrayList<>();
            java.util.List<String> skippedChildren = new ArrayList<>();

            for (UnderagedMember child : selectedChildren) {
                // Check if participant already exists
                if (participantDAO.participantExists(currentWorkshop.getId(), 0, child.getId())) {
                    skippedChildren.add(child.getFullName());
                    child.setSelected(false); // Clear selection
                    continue;
                }

                WorkshopParticipant participant = new WorkshopParticipant();
                participant.setWorkshopId(currentWorkshop.getId());
                participant.setUnderagedId(child.getId());
                participant.setParticipantType(ParticipantType.CHILD);
                participant.setPaymentStatus(paymentStatus);
                participant.setNotes(notes.isEmpty() ? null : notes);
                participant.setCreatedAt(timestamp);
                participant.setUpdatedAt(timestamp);

                if (participantDAO.addParticipant(participant)) {
                    addedChildren.add(child);
                }
                child.setSelected(false); // Clear selection
            }

            // Clear form
            childrenNotesField.clear();

            // Refresh data
            loadWorkshopParticipants();
            updateStatistics();

            // Show results
            StringBuilder message = new StringBuilder();
            if (!addedChildren.isEmpty()) {
                message.append("Successfully added ").append(addedChildren.size()).append(" children to the workshop!");
            }
            if (!skippedChildren.isEmpty()) {
                if (message.length() > 0) message.append("\n\n");
                message.append("Skipped ").append(skippedChildren.size()).append(" children who were already in the workshop:\n");
                message.append(String.join(", ", skippedChildren));
            }

            Alert result = new Alert(addedChildren.isEmpty() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
            result.setTitle("Add Children Result");
            result.setHeaderText(addedChildren.isEmpty() ? "No Children Added" : "Children Added");
            result.setContentText(message.toString());
            result.showAndWait();

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Addition Failed");
            error.setContentText("Failed to add children: " + e.getMessage());
            error.showAndWait();
            e.printStackTrace();
        }
    }

    private void handleAddFamily() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Add Family");
        info.setHeaderText("Add Entire Family");
        info.setContentText("This feature will allow you to select a contact and automatically add them plus all their children to the workshop.\n\nFunctionality coming soon!");
        info.showAndWait();
    }

    // Public methods for external navigation
    public void refreshData() {
        loadWorkshopParticipants();
        loadAvailableParticipants();
        updateStatistics();
    }

    public Workshop getCurrentWorkshop() {
        return currentWorkshop;
    }
}