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
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.*;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.model.WorkshopParticipant.ParticipantType;
import smallbusinessbuddycrm.model.WorkshopParticipant.PaymentStatus;

import java.net.URL;
import java.util.*;
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
            workshopNameLabel.setText("Workshop: " + workshop.getName() + " (" + workshop.getDateRange() + ")");
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
                    checkBox.setOnAction(event -> participant.put("selected", checkBox.isSelected()));
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
                return new SimpleStringProperty(parentName + " (" + parentPhone + ")");
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
        selectAdultColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectAdultColumn));
        selectAdultColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectAdultColumn.setEditable(true);

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
        selectChildColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectChildColumn));
        selectChildColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectChildColumn.setEditable(true);

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
    }

    private void loadWorkshopParticipants() {
        if (currentWorkshop == null) return;

        try {
            java.util.List<Map<String, Object>> participants = participantDAO.getWorkshopParticipantsWithDetails(currentWorkshop.getId());
            participantsList.setAll(participants);
            System.out.println("Loaded " + participants.size() + " participants for workshop: " + currentWorkshop.getName());
        } catch (Exception e) {
            System.err.println("Error loading workshop participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAvailableParticipants() {
        try {
            // Load all contacts
            java.util.List<Contact> allContacts = contactDAO.getAllContacts();
            availableAdultsList.setAll(allContacts);
            availableAdultsCountLabel.setText("(" + allContacts.size() + " available)");

            // Load all underaged members
            java.util.List<UnderagedMember> allChildren = underagedDAO.getAllUnderagedMembers();
            availableChildrenList.setAll(allChildren);
            availableChildrenCountLabel.setText("(" + allChildren.size() + " available)");

            System.out.println("Loaded " + allContacts.size() + " adults and " + allChildren.size() + " children");
        } catch (Exception e) {
            System.err.println("Error loading available participants: " + e.getMessage());
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

    // Event handlers (placeholder implementations)
    private void handleEditParticipant(Map<String, Object> participant) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Edit Participant");
        info.setHeaderText("Edit: " + participant.get("participant_name"));
        info.setContentText("Edit participant functionality coming soon!");
        info.showAndWait();
    }

    private void handleRemoveParticipant(Map<String, Object> participant) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Participant");
        confirmation.setHeaderText("Remove " + participant.get("participant_name") + "?");
        confirmation.setContentText("Are you sure you want to remove this participant from the workshop?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // TODO: Implement actual removal
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Remove Participant");
            info.setContentText("Remove participant functionality coming soon!");
            info.showAndWait();
        }
    }

    private void handleRemoveSelected() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Remove Selected");
        info.setContentText("Remove selected participants functionality coming soon!");
        info.showAndWait();
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
        filteredAdultsList.forEach(contact -> contact.setSelected(true));
        availableAdultsTable.refresh();
    }

    private void handleClearAdultsSelection() {
        availableAdultsList.forEach(contact -> contact.setSelected(false));
        availableAdultsTable.refresh();
    }

    private void handleSelectAllChildren() {
        filteredChildrenList.forEach(child -> child.setSelected(true));
        availableChildrenTable.refresh();
    }

    private void handleClearChildrenSelection() {
        availableChildrenList.forEach(child -> child.setSelected(false));
        availableChildrenTable.refresh();
    }

    private void handleAddSelectedAdults() {
        java.util.List<Contact> selectedAdults = availableAdultsList.stream()
                .filter(Contact::isSelected)
                .collect(Collectors.toList());

        if (selectedAdults.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setContentText("Please select at least one adult to add.");
            warning.showAndWait();
            return;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Add Adults");
        info.setContentText("Will add " + selectedAdults.size() + " adults with payment status: " +
                adultsPaymentStatusCombo.getValue() + "\nFunctionality coming soon!");
        info.showAndWait();
    }

    private void handleAddSelectedChildren() {
        java.util.List<UnderagedMember> selectedChildren = availableChildrenList.stream()
                .filter(UnderagedMember::isSelected)
                .collect(Collectors.toList());

        if (selectedChildren.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("No Selection");
            warning.setContentText("Please select at least one child to add.");
            warning.showAndWait();
            return;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Add Children");
        info.setContentText("Will add " + selectedChildren.size() + " children with payment status: " +
                childrenPaymentStatusCombo.getValue() + "\nFunctionality coming soon!");
        info.showAndWait();
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