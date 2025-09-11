package smallbusinessbuddycrm.controllers.workshop;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import smallbusinessbuddycrm.database.*;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.model.WorkshopParticipant.ParticipantType;
import smallbusinessbuddycrm.model.WorkshopParticipant.PaymentStatus;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private Label teacherLabel; // Read-only teacher display
    @FXML private Label workshopParticipantsPageTitle;
    @FXML private Tab currentParticipantsTab;
    @FXML private Tab addParticipantsTab;
    @FXML private Label addAdultParticipantsLabel;
    @FXML private Label addChildParticipantsLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label notesLabel;
    @FXML private Label paymentStatusLabel2;
    @FXML private Label notesLabel2;
    @FXML private Label quickActionsLabel;

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
        updateTexts();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);



        setupTables();
        setupSearchAndFilters();
        setupEventHandlers();
        setupComboBoxes();
        loadAvailableParticipants();


    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Page title and main labels
        if (workshopParticipantsPageTitle != null) {
            workshopParticipantsPageTitle.setText(languageManager.getText("workshop.participants.page.title"));
        }

        // Buttons
        if (exportParticipantsButton != null) {
            exportParticipantsButton.setText(languageManager.getText("workshop.participants.export"));
        }
        if (refreshButton != null) {
            refreshButton.setText(languageManager.getText("workshop.participants.refresh"));
        }
        if (removeSelectedButton != null) {
            removeSelectedButton.setText(languageManager.getText("workshop.participants.remove.selected"));
        }

        // Tabs
        if (currentParticipantsTab != null) {
            currentParticipantsTab.setText(languageManager.getText("workshop.participants.current.tab"));
        }
        if (addParticipantsTab != null) {
            addParticipantsTab.setText(languageManager.getText("workshop.participants.add.tab"));
        }

        // Search fields
        if (searchParticipantsField != null) {
            searchParticipantsField.setPromptText(languageManager.getText("workshop.participants.search.placeholder"));
        }
        if (searchAdultsField != null) {
            searchAdultsField.setPromptText(languageManager.getText("workshop.participants.search.adults.placeholder"));
        }
        if (searchChildrenField != null) {
            searchChildrenField.setPromptText(languageManager.getText("workshop.participants.search.children.placeholder"));
        }
        if (adultsNotesField != null) {
            adultsNotesField.setPromptText(languageManager.getText("workshop.participants.notes.placeholder"));
        }
        if (childrenNotesField != null) {
            childrenNotesField.setPromptText(languageManager.getText("workshop.participants.notes.placeholder"));
        }

        // Table columns - Current Participants
        if (participantNameColumn != null) {
            participantNameColumn.setText(languageManager.getText("workshop.participants.column.name"));
        }
        if (participantTypeColumn != null) {
            participantTypeColumn.setText(languageManager.getText("workshop.participants.column.type"));
        }
        if (participantAgeColumn != null) {
            participantAgeColumn.setText(languageManager.getText("workshop.participants.column.age"));
        }
        if (participantEmailColumn != null) {
            participantEmailColumn.setText(languageManager.getText("workshop.participants.column.email"));
        }
        if (participantPhoneColumn != null) {
            participantPhoneColumn.setText(languageManager.getText("workshop.participants.column.phone"));
        }
        if (parentInfoColumn != null) {
            parentInfoColumn.setText(languageManager.getText("workshop.participants.column.parent"));
        }
        if (paymentStatusColumn != null) {
            paymentStatusColumn.setText(languageManager.getText("workshop.participants.column.payment"));
        }
        if (notesColumn != null) {
            notesColumn.setText(languageManager.getText("workshop.participants.column.notes"));
        }
        if (enrollmentDateColumn != null) {
            enrollmentDateColumn.setText(languageManager.getText("workshop.participants.column.enrolled"));
        }
        if (actionsColumn != null) {
            actionsColumn.setText(languageManager.getText("workshop.participants.column.actions"));
        }

        // Table columns - Adults
        if (adultNameColumn != null) {
            adultNameColumn.setText(languageManager.getText("workshop.participants.adults.column.name"));
        }
        if (adultEmailColumn != null) {
            adultEmailColumn.setText(languageManager.getText("workshop.participants.adults.column.email"));
        }
        if (adultPhoneColumn != null) {
            adultPhoneColumn.setText(languageManager.getText("workshop.participants.adults.column.phone"));
        }
        if (adultMemberStatusColumn != null) {
            adultMemberStatusColumn.setText(languageManager.getText("workshop.participants.adults.column.member"));
        }
        if (adultAgeColumn != null) {
            adultAgeColumn.setText(languageManager.getText("workshop.participants.adults.column.age"));
        }

        // Table columns - Children
        if (childNameColumn != null) {
            childNameColumn.setText(languageManager.getText("workshop.participants.children.column.name"));
        }
        if (childAgeColumn != null) {
            childAgeColumn.setText(languageManager.getText("workshop.participants.children.column.age"));
        }
        if (childGenderColumn != null) {
            childGenderColumn.setText(languageManager.getText("workshop.participants.children.column.gender"));
        }
        if (childMemberStatusColumn != null) {
            childMemberStatusColumn.setText(languageManager.getText("workshop.participants.children.column.member"));
        }
        if (parentNameColumn != null) {
            parentNameColumn.setText(languageManager.getText("workshop.participants.children.column.parent.name"));
        }
        if (parentEmailColumn != null) {
            parentEmailColumn.setText(languageManager.getText("workshop.participants.children.column.parent.email"));
        }
        if (parentPhoneColumn != null) {
            parentPhoneColumn.setText(languageManager.getText("workshop.participants.children.column.parent.phone"));
        }

        // Section labels
        if (addAdultParticipantsLabel != null) {
            addAdultParticipantsLabel.setText(languageManager.getText("workshop.participants.add.adults"));
        }
        if (addChildParticipantsLabel != null) {
            addChildParticipantsLabel.setText(languageManager.getText("workshop.participants.add.children"));
        }
        if (quickActionsLabel != null) {
            quickActionsLabel.setText(languageManager.getText("workshop.participants.quick.actions"));
        }

        // Form labels
        if (paymentStatusLabel != null) {
            paymentStatusLabel.setText(languageManager.getText("workshop.participants.payment.status"));
        }
        if (notesLabel != null) {
            notesLabel.setText(languageManager.getText("workshop.participants.notes"));
        }
        if (paymentStatusLabel2 != null) {
            paymentStatusLabel2.setText(languageManager.getText("workshop.participants.payment.status"));
        }
        if (notesLabel2 != null) {
            notesLabel2.setText(languageManager.getText("workshop.participants.notes"));
        }

        // Checkboxes
        if (membersOnlyAdultsFilter != null) {
            membersOnlyAdultsFilter.setText(languageManager.getText("workshop.participants.members.only"));
        }
        if (membersOnlyChildrenFilter != null) {
            membersOnlyChildrenFilter.setText(languageManager.getText("workshop.participants.members.only"));
        }

        // Action buttons
        if (selectAllAdultsButton != null) {
            selectAllAdultsButton.setText(languageManager.getText("workshop.participants.select.all"));
        }
        if (clearAdultsSelectionButton != null) {
            clearAdultsSelectionButton.setText(languageManager.getText("workshop.participants.clear.selection"));
        }
        if (selectAllChildrenButton != null) {
            selectAllChildrenButton.setText(languageManager.getText("workshop.participants.select.all"));
        }
        if (clearChildrenSelectionButton != null) {
            clearChildrenSelectionButton.setText(languageManager.getText("workshop.participants.clear.selection"));
        }
        if (addSelectedAdultsButton != null) {
            addSelectedAdultsButton.setText(languageManager.getText("workshop.participants.add.selected.adults"));
        }
        if (addSelectedChildrenButton != null) {
            addSelectedChildrenButton.setText(languageManager.getText("workshop.participants.add.selected.children"));
        }

        // Update ComboBox prompt texts
        updateComboBoxPromptTexts();

        // Update table placeholder if exists
        if (participantsTable != null) {
            participantsTable.setPlaceholder(new Label(languageManager.getText("workshop.participants.no.participants")));
        }

        // Update statistics labels if needed (these are dynamic, but we can update the base text)
        updateStatistics(); // This will refresh the statistics with new language

        System.out.println("Workshop participants view texts updated");
    }

    private void updateComboBoxPromptTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update filter combo boxes
        if (participantTypeFilter != null) {
            String currentValue = participantTypeFilter.getValue();
            participantTypeFilter.setItems(FXCollections.observableArrayList(
                    languageManager.getText("workshop.participants.type.filter.all"),
                    languageManager.getText("workshop.participants.type.filter.adults"),
                    languageManager.getText("workshop.participants.type.filter.children")
            ));
            // Try to maintain selection, default to first item
            if (currentValue != null) {
                // Map old values to new values
                switch (currentValue) {
                    case "All Types":
                        participantTypeFilter.setValue(languageManager.getText("workshop.participants.type.filter.all"));
                        break;
                    case "Adults":
                        participantTypeFilter.setValue(languageManager.getText("workshop.participants.type.filter.adults"));
                        break;
                    case "Children":
                        participantTypeFilter.setValue(languageManager.getText("workshop.participants.type.filter.children"));
                        break;
                    default:
                        participantTypeFilter.setValue(languageManager.getText("workshop.participants.type.filter.all"));
                }
            } else {
                participantTypeFilter.setValue(languageManager.getText("workshop.participants.type.filter.all"));
            }
        }

        if (paymentStatusFilter != null) {
            String currentValue = paymentStatusFilter.getValue();
            paymentStatusFilter.setItems(FXCollections.observableArrayList(
                    languageManager.getText("workshop.participants.payment.filter.all"),
                    "PENDING", "PAID", "REFUNDED", "CANCELLED"
            ));
            if (currentValue != null && !currentValue.equals("All Payments")) {
                paymentStatusFilter.setValue(currentValue);
            } else {
                paymentStatusFilter.setValue(languageManager.getText("workshop.participants.payment.filter.all"));
            }
        }

        if (ageRangeFilter != null) {
            String currentValue = ageRangeFilter.getValue();
            ageRangeFilter.setItems(FXCollections.observableArrayList(
                    languageManager.getText("workshop.participants.age.filter.all"),
                    languageManager.getText("workshop.participants.age.filter.under6"),
                    languageManager.getText("workshop.participants.age.filter.6to12"),
                    languageManager.getText("workshop.participants.age.filter.13to17"),
                    languageManager.getText("workshop.participants.age.filter.18plus")
            ));
            // Map old values to new values
            if (currentValue != null) {
                switch (currentValue) {
                    case "All Ages":
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.all"));
                        break;
                    case "Under 6":
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.under6"));
                        break;
                    case "6-12":
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.6to12"));
                        break;
                    case "13-17":
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.13to17"));
                        break;
                    case "18+":
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.18plus"));
                        break;
                    default:
                        ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.all"));
                }
            } else {
                ageRangeFilter.setValue(languageManager.getText("workshop.participants.age.filter.all"));
            }
        }
    }


    public void setWorkshop(Workshop workshop) {
        this.currentWorkshop = workshop;
        if (workshop != null) {
            // Keep this in English/original format since it shows actual workshop data
            workshopNameLabel.setText("Workshop: " + workshop.getName() + " (" + workshop.getFormattedFromDate() + " - " + workshop.getFormattedToDate() + ")");
            loadWorkshopParticipants();
            updateStatistics();
        } else {
            LanguageManager languageManager = LanguageManager.getInstance();
            workshopNameLabel.setText(languageManager.getText("workshop.participants.no.workshop.selected"));
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
        LanguageManager languageManager = LanguageManager.getInstance();

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

        // Participant type column with translation
        participantTypeColumn.setCellValueFactory(cellData -> {
            String type = (String) cellData.getValue().get("participant_type");
            String translatedType = type;
            if ("ADULT".equals(type)) {
                translatedType = languageManager.getText("workshop.participants.type.adult");
            } else if ("CHILD".equals(type)) {
                translatedType = languageManager.getText("workshop.participants.type.child");
            }
            return new SimpleStringProperty(translatedType);
        });

        participantAgeColumn.setCellValueFactory(cellData -> {
            Object age = cellData.getValue().get("participant_age");
            return new SimpleStringProperty(age != null ? age.toString() : "");
        });

        participantEmailColumn.setCellValueFactory(cellData -> {
            String email = (String) cellData.getValue().get("participant_email");
            String parentEmail = (String) cellData.getValue().get("parent_email");
            // For children, show parent email if participant email is null
            String displayEmail = email != null ? email : (parentEmail != null ? parentEmail : "");
            return new SimpleStringProperty(displayEmail);
        });

        participantPhoneColumn.setCellValueFactory(cellData -> {
            String phone = (String) cellData.getValue().get("participant_phone");
            String parentPhone = (String) cellData.getValue().get("parent_phone");
            // For children, show parent phone if participant phone is null
            String displayPhone = phone != null ? phone : (parentPhone != null ? parentPhone : "");
            return new SimpleStringProperty(displayPhone);
        });

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

        // Payment status column with translation
        paymentStatusColumn.setCellValueFactory(cellData -> {
            String status = (String) cellData.getValue().get("payment_status");
            String translatedStatus = status;
            if (status != null) {
                switch (status) {
                    case "PENDING":
                        translatedStatus = languageManager.getText("workshop.participants.payment.status.pending");
                        break;
                    case "PAID":
                        translatedStatus = languageManager.getText("workshop.participants.payment.status.paid");
                        break;
                    case "REFUNDED":
                        translatedStatus = languageManager.getText("workshop.participants.payment.status.refunded");
                        break;
                    case "CANCELLED":
                        translatedStatus = languageManager.getText("workshop.participants.payment.status.cancelled");
                        break;
                    default:
                        translatedStatus = status;
                }
            }
            return new SimpleStringProperty(translatedStatus);
        });

        notesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("notes")));

        enrollmentDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty((String) cellData.getValue().get("created_at")));

        // Actions column with translated buttons
        actionsColumn.setCellFactory(tc -> new TableCell<Map<String, Object>, Void>() {
            private final Button editButton = new Button();
            private final Button removeButton = new Button();
            private final javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(5);

            {
                // Initialize button styles
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");

                // Set initial button text
                updateButtonTexts();

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

            private void updateButtonTexts() {
                LanguageManager lm = LanguageManager.getInstance();
                editButton.setText(lm.getText("workshop.participants.button.edit"));
                removeButton.setText(lm.getText("workshop.participants.button.remove"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    // Update button text when cell is updated (for language changes)
                    updateButtonTexts();
                    setGraphic(actionBox);
                }
            }
        });

        participantsTable.setItems(participantsList);

        // Set table placeholder
        participantsTable.setPlaceholder(new Label(languageManager.getText("workshop.participants.no.participants")));
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
        adultMemberStatusColumn.setCellValueFactory(cellData -> {
            LanguageManager languageManager = LanguageManager.getInstance();
            return new SimpleStringProperty(cellData.getValue().isMember() ?
                    languageManager.getText("workshop.participants.member") :
                    languageManager.getText("workshop.participants.non.member"));
        });
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
        childMemberStatusColumn.setCellValueFactory(cellData -> {
            LanguageManager languageManager = LanguageManager.getInstance();
            return new SimpleStringProperty(cellData.getValue().isMember() ?
                    languageManager.getText("workshop.participants.member") :
                    languageManager.getText("workshop.participants.non.member"));
        });

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
        participantTypeFilter.setItems(FXCollections.observableArrayList(
                LanguageManager.getInstance().getText("workshop.participants.type.filter.all"),
                LanguageManager.getInstance().getText("workshop.participants.type.filter.adults"),
                LanguageManager.getInstance().getText("workshop.participants.type.filter.children")
        ));
        participantTypeFilter.setValue(LanguageManager.getInstance().getText("workshop.participants.type.filter.all"));
        participantTypeFilter.valueProperty().addListener((obs, old, newValue) -> updateParticipantsFilter());

        paymentStatusFilter.setItems(FXCollections.observableArrayList(
                LanguageManager.getInstance().getText("workshop.participants.payment.filter.all"),
                "PENDING", "PAID", "REFUNDED", "CANCELLED"
        ));
        paymentStatusFilter.setValue(LanguageManager.getInstance().getText("workshop.participants.payment.filter.all"));
        paymentStatusFilter.valueProperty().addListener((obs, old, newValue) -> updateParticipantsFilter());


        ageRangeFilter.setItems(FXCollections.observableArrayList(
                LanguageManager.getInstance().getText("workshop.participants.age.filter.all"),
                LanguageManager.getInstance().getText("workshop.participants.age.filter.under6"),
                LanguageManager.getInstance().getText("workshop.participants.age.filter.6to12"),
                LanguageManager.getInstance().getText("workshop.participants.age.filter.13to17"),
                LanguageManager.getInstance().getText("workshop.participants.age.filter.18plus")
        ));
        ageRangeFilter.setValue(LanguageManager.getInstance().getText("workshop.participants.age.filter.all"));
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

        membersOnlyAdultsFilter.setOnAction(e -> updateAdultsFilter());
        membersOnlyChildrenFilter.setOnAction(e -> updateChildrenFilter());

        // REMOVED: manageTeachersButton event handler
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
            LanguageManager languageManager = LanguageManager.getInstance();

            if (currentWorkshop != null && currentWorkshop.hasTeacher()) {
                Teacher teacher = teacherDAO.getTeacherById(currentWorkshop.getTeacherId());
                if (teacher != null) {
                    String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
                    teacherLabel.setText(languageManager.getText("workshop.participants.teacher").replace("{0}", teacherName));
                    teacherLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 12px;"); // Green color
                } else {
                    teacherLabel.setText(languageManager.getText("workshop.participants.teacher.unknown"));
                    teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;"); // Red color
                }
            } else {
                teacherLabel.setText(languageManager.getText("workshop.participants.teacher.not.assigned"));
                teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;"); // Red color
            }
        } catch (Exception e) {
            LanguageManager languageManager = LanguageManager.getInstance();
            teacherLabel.setText(languageManager.getText("workshop.participants.teacher.error"));
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
            LanguageManager languageManager = LanguageManager.getInstance();
            Map<String, Integer> stats = participantDAO.getWorkshopStatistics(currentWorkshop.getId());

            // Use MessageFormat for parameterized strings if you want, or simple concatenation
            totalParticipantsLabel.setText(languageManager.getText("workshop.participants.total").replace("{0}",
                    String.valueOf(stats.getOrDefault("total_participants", 0))));
            adultsCountLabel.setText(languageManager.getText("workshop.participants.adults").replace("{0}",
                    String.valueOf(stats.getOrDefault("adult_count", 0))));
            childrenCountLabel.setText(languageManager.getText("workshop.participants.children").replace("{0}",
                    String.valueOf(stats.getOrDefault("child_count", 0))));
            paidCountLabel.setText(languageManager.getText("workshop.participants.paid").replace("{0}",
                    String.valueOf(stats.getOrDefault("paid_count", 0))));
            pendingCountLabel.setText(languageManager.getText("workshop.participants.pending").replace("{0}",
                    String.valueOf(stats.getOrDefault("pending_count", 0))));

            updateTeacherDisplay();

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
            clearStatistics();
        }
    }

    private void clearStatistics() {
        LanguageManager languageManager = LanguageManager.getInstance();

        totalParticipantsLabel.setText(languageManager.getText("workshop.participants.total").replace("{0}", "0"));
        adultsCountLabel.setText(languageManager.getText("workshop.participants.adults").replace("{0}", "0"));
        childrenCountLabel.setText(languageManager.getText("workshop.participants.children").replace("{0}", "0"));
        paidCountLabel.setText(languageManager.getText("workshop.participants.paid").replace("{0}", "0"));
        pendingCountLabel.setText(languageManager.getText("workshop.participants.pending").replace("{0}", "0"));
        teacherLabel.setText(languageManager.getText("workshop.participants.teacher.not.assigned"));
        teacherLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
    }

    // Filter methods
    private void updateParticipantsFilter() {
        String searchText = searchParticipantsField.getText().toLowerCase().trim();
        String typeFilter = participantTypeFilter.getValue();
        String paymentFilter = paymentStatusFilter.getValue();

        LanguageManager languageManager = LanguageManager.getInstance();

        filteredParticipantsList.setPredicate(participant -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    ((String) participant.get("participant_name")).toLowerCase().contains(searchText);

            // Type filter - check against translated values
            boolean matchesType = typeFilter == null ||
                    typeFilter.equals(languageManager.getText("workshop.participants.type.filter.all")) ||
                    (typeFilter.equals(languageManager.getText("workshop.participants.type.filter.adults")) &&
                            "ADULT".equals(participant.get("participant_type"))) ||
                    (typeFilter.equals(languageManager.getText("workshop.participants.type.filter.children")) &&
                            "CHILD".equals(participant.get("participant_type")));

            // Payment filter - check against translated values
            boolean matchesPayment = paymentFilter == null ||
                    paymentFilter.equals(languageManager.getText("workshop.participants.payment.filter.all")) ||
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

        LanguageManager languageManager = LanguageManager.getInstance();

        filteredChildrenList.setPredicate(child -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    child.getFullName().toLowerCase().contains(searchText);

            boolean matchesAge = ageRange == null ||
                    ageRange.equals(languageManager.getText("workshop.participants.age.filter.all")) ||
                    (ageRange.equals(languageManager.getText("workshop.participants.age.filter.under6")) && child.getAge() < 6) ||
                    (ageRange.equals(languageManager.getText("workshop.participants.age.filter.6to12")) && child.getAge() >= 6 && child.getAge() <= 12) ||
                    (ageRange.equals(languageManager.getText("workshop.participants.age.filter.13to17")) && child.getAge() >= 13 && child.getAge() <= 17) ||
                    (ageRange.equals(languageManager.getText("workshop.participants.age.filter.18plus")) && child.getAge() >= 18);

            boolean matchesMember = !membersOnly || child.isMember();

            return matchesSearch && matchesAge && matchesMember;
        });
    }



    private void handleEditParticipant(Map<String, Object> participant) {
        try {
            LanguageManager languageManager = LanguageManager.getInstance();

            // Create a dialog for editing participant details
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(languageManager.getText("workshop.participants.edit.dialog.title"));
            dialog.setHeaderText(languageManager.getText("workshop.participants.edit.dialog.header")
                    .replace("{0}", (String) participant.get("participant_name")));

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            // Payment Status
            Label paymentLabel = new Label(languageManager.getText("workshop.participants.edit.payment.label"));
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

            // Notes - FIX: Handle null notes safely
            Label notesLabel = new Label(languageManager.getText("workshop.participants.edit.notes.label"));
            String existingNotes = (String) participant.get("notes");
            // Ensure we never pass null to TextArea constructor
            TextArea notesArea = new TextArea(existingNotes != null ? existingNotes : "");
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

                // FIX: Safe handling of TextArea.getText()
                String rawNotes = notesArea.getText();
                String newNotes = (rawNotes != null) ? rawNotes.trim() : "";

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
                        successAlert.setTitle(languageManager.getText("workshop.participants.edit.success.title"));
                        successAlert.setHeaderText(languageManager.getText("workshop.participants.edit.success.header"));
                        successAlert.setContentText(languageManager.getText("workshop.participants.edit.success.content"));
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle(languageManager.getText("workshop.participants.edit.error.title"));
                        errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.database.error.header"));
                        errorAlert.setContentText(languageManager.getText("workshop.participants.edit.database.error.content"));
                        errorAlert.showAndWait();
                    }

                } catch (Exception e) {
                    System.err.println("Error updating participant: " + e.getMessage());
                    e.printStackTrace();

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("workshop.participants.edit.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.general.error.header"));
                    errorAlert.setContentText(languageManager.getText("workshop.participants.edit.general.error.content")
                            .replace("{0}", e.getMessage()));
                    errorAlert.showAndWait();
                }
            }

        } catch (Exception e) {
            System.err.println("Error opening edit dialog: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshop.participants.dialog.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshop.participants.dialog.error.header"));
            errorAlert.setContentText(languageManager.getText("workshop.participants.dialog.error.content")
                    .replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    private void handleRemoveParticipant(Map<String, Object> participant) {
        LanguageManager languageManager = LanguageManager.getInstance();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(languageManager.getText("workshop.participants.remove.dialog.title"));
        confirmation.setHeaderText(languageManager.getText("workshop.participants.remove.dialog.header")
                .replace("{0}", (String) participant.get("participant_name")));
        confirmation.setContentText(languageManager.getText("workshop.participants.remove.dialog.content"));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                int participantId = (Integer) participant.get("participant_id");

                boolean success = participantDAO.removeParticipant(participantId);

                if (success) {
                    // Remove from the list
                    participantsList.remove(participant);
                    updateStatistics();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("workshop.participants.remove.success.title"));
                    successAlert.setHeaderText(languageManager.getText("workshop.participants.remove.success.header"));
                    successAlert.setContentText(languageManager.getText("workshop.participants.remove.success.content"));
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("workshop.participants.remove.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.database.error.header"));
                    errorAlert.setContentText(languageManager.getText("workshop.participants.remove.database.error.content"));
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error removing participant: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(languageManager.getText("workshop.participants.remove.error.title"));
                errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.general.error.header"));
                errorAlert.setContentText(languageManager.getText("workshop.participants.remove.general.error.content")
                        .replace("{0}", e.getMessage()));
                errorAlert.showAndWait();
            }
        }
    }

    private void handleRemoveSelected() {
        LanguageManager languageManager = LanguageManager.getInstance();
        System.out.println("Remove selected participants clicked");

        // Get selected participants
        java.util.List<Map<String, Object>> selectedParticipants = participantsList.stream()
                .filter(p -> (Boolean) p.getOrDefault("selected", false))
                .collect(Collectors.toList());

        System.out.println("Selected participants count: " + selectedParticipants.size());

        if (selectedParticipants.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.no.selection.warning.title"));
            warning.setHeaderText(languageManager.getText("workshop.participants.no.selection.warning.title"));
            warning.setContentText(languageManager.getText("workshop.participants.no.selection.remove.content"));
            warning.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(languageManager.getText("workshop.participants.remove.selected.dialog.title"));
        confirmation.setHeaderText(languageManager.getText("workshop.participants.remove.selected.dialog.header")
                .replace("{0}", String.valueOf(selectedParticipants.size())));
        confirmation.setContentText(languageManager.getText("workshop.participants.remove.selected.dialog.content"));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Collect participant IDs
                java.util.List<Integer> participantIds = selectedParticipants.stream()
                        .map(p -> (Integer) p.get("participant_id"))
                        .collect(Collectors.toList());

                System.out.println("Attempting to remove participant IDs: " + participantIds);

                boolean success = participantDAO.removeParticipants(participantIds);

                if (success) {
                    // Remove from the list
                    participantsList.removeAll(selectedParticipants);
                    updateStatistics();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("workshop.participants.remove.selected.success.title"));
                    successAlert.setHeaderText(languageManager.getText("workshop.participants.remove.selected.success.header"));
                    successAlert.setContentText(languageManager.getText("workshop.participants.remove.selected.success.content")
                            .replace("{0}", String.valueOf(selectedParticipants.size())));
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("workshop.participants.remove.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.database.error.header"));
                    errorAlert.setContentText(languageManager.getText("workshop.participants.remove.selected.error.content"));
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error removing selected participants: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(languageManager.getText("workshop.participants.remove.error.title"));
                errorAlert.setHeaderText(languageManager.getText("workshop.participants.edit.general.error.header"));
                errorAlert.setContentText(languageManager.getText("workshop.participants.remove.selected.general.error.content")
                        .replace("{0}", e.getMessage()));
                errorAlert.showAndWait();
            }
        }
    }

    private void handleExportParticipants() {
        LanguageManager languageManager = LanguageManager.getInstance();

        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.export.no.workshop.title"));
            warning.setHeaderText(languageManager.getText("workshop.participants.export.no.workshop.header"));
            warning.setContentText(languageManager.getText("workshop.participants.export.no.workshop.content"));
            warning.showAndWait();
            return;
        }

        if (participantsList.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.export.no.participants.title"));
            warning.setHeaderText(languageManager.getText("workshop.participants.export.no.participants.header"));
            warning.setContentText(languageManager.getText("workshop.participants.export.no.participants.content"));
            warning.showAndWait();
            return;
        }

        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(languageManager.getText("workshop.participants.export.dialog.title"));

            // Set extension filter
            FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter(
                    languageManager.getText("workshop.participants.export.csv.files") + " (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(csvFilter);

            // Set initial filename with workshop name and date
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String workshopName = currentWorkshop.getName().replaceAll("[^a-zA-Z0-9]", "_");
            String initialFileName = String.format("Workshop_%s_Participants_%s.csv", workshopName, timestamp);
            fileChooser.setInitialFileName(initialFileName);

            // Show save dialog
            File file = fileChooser.showSaveDialog(exportParticipantsButton.getScene().getWindow());

            if (file != null) {
                exportParticipantsToCSV(file);
            }

        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshop.participants.export.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshop.participants.export.error.header"));
            errorAlert.setContentText(languageManager.getText("workshop.participants.export.error.content")
                    .replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    private void exportParticipantsToCSV(File file) throws IOException {
        LanguageManager languageManager = LanguageManager.getInstance();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {

            // Write BOM for UTF-8 (helps with Excel compatibility)
            writer.write('\ufeff');

            // Write workshop header information
            writer.write("# " + languageManager.getText("workshop.participants.export.workshop.info"));
            writer.newLine();
            writer.write("# " + languageManager.getText("workshop.participants.export.workshop.name") + ": " + currentWorkshop.getName());
            writer.newLine();
            writer.write("# " + languageManager.getText("workshop.participants.export.workshop.dates") + ": " +
                    currentWorkshop.getFormattedFromDate() + " - " + currentWorkshop.getFormattedToDate());
            writer.newLine();
            writer.write("# " + languageManager.getText("workshop.participants.export.export.date") + ": " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("# " + languageManager.getText("workshop.participants.export.total.participants") + ": " + participantsList.size());
            writer.newLine();
            writer.newLine();

            // Write CSV header
            writeCSVHeader(writer, languageManager);

            // Write participant data
            for (Map<String, Object> participant : participantsList) {
                writeParticipantRow(writer, participant, languageManager);
            }

            writer.flush();

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle(languageManager.getText("workshop.participants.export.success.title"));
            successAlert.setHeaderText(languageManager.getText("workshop.participants.export.success.header"));
            successAlert.setContentText(languageManager.getText("workshop.participants.export.success.content")
                    .replace("{0}", file.getAbsolutePath())
                    .replace("{1}", String.valueOf(participantsList.size())));
            successAlert.showAndWait();

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void writeCSVHeader(BufferedWriter writer, LanguageManager languageManager) throws IOException {
        String[] headers = {
                languageManager.getText("workshop.participants.export.header.name"),
                languageManager.getText("workshop.participants.export.header.type"),
                languageManager.getText("workshop.participants.export.header.age"),
                languageManager.getText("workshop.participants.export.header.email"),
                languageManager.getText("workshop.participants.export.header.phone"),
                languageManager.getText("workshop.participants.export.header.parent.info"),
                languageManager.getText("workshop.participants.export.header.payment.status"),
                languageManager.getText("workshop.participants.export.header.notes"),
                languageManager.getText("workshop.participants.export.header.enrollment.date"),
                languageManager.getText("workshop.participants.export.header.last.updated")
        };

        writer.write(String.join(",", escapeCSVFields(headers)));
        writer.newLine();
    }

    private void writeParticipantRow(BufferedWriter writer, Map<String, Object> participant, LanguageManager languageManager) throws IOException {
        // Extract and format participant data
        String name = getString(participant, "participant_name");
        String type = translateParticipantType(getString(participant, "participant_type"), languageManager);
        String age = getString(participant, "participant_age");
        String email = getEmailForDisplay(participant);
        String phone = getPhoneForDisplay(participant);
        String parentInfo = getParentInfo(participant);
        String paymentStatus = translatePaymentStatus(getString(participant, "payment_status"), languageManager);
        String notes = getString(participant, "notes");
        String enrollmentDate = formatDate(getString(participant, "created_at"));
        String lastUpdated = formatDate(getString(participant, "updated_at"));

        String[] fields = {name, type, age, email, phone, parentInfo, paymentStatus, notes, enrollmentDate, lastUpdated};

        writer.write(String.join(",", escapeCSVFields(fields)));
        writer.newLine();
    }

    private String[] escapeCSVFields(String[] fields) {
        String[] escapedFields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            escapedFields[i] = escapeCSVField(fields[i]);
        }
        return escapedFields;
    }

    private String escapeCSVField(String field) {
        if (field == null) {
            return "\"\"";
        }

        // If field contains comma, quote, or newline, wrap in quotes and escape quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private String getEmailForDisplay(Map<String, Object> participant) {
        String email = getString(participant, "participant_email");
        String parentEmail = getString(participant, "parent_email");
        return !email.isEmpty() ? email : parentEmail;
    }

    private String getPhoneForDisplay(Map<String, Object> participant) {
        String phone = getString(participant, "participant_phone");
        String parentPhone = getString(participant, "parent_phone");
        return !phone.isEmpty() ? phone : parentPhone;
    }

    private String getParentInfo(Map<String, Object> participant) {
        String type = getString(participant, "participant_type");
        if ("CHILD".equals(type)) {
            String parentName = getString(participant, "parent_name");
            String parentPhone = getString(participant, "parent_phone");
            if (!parentName.isEmpty() && !parentPhone.isEmpty()) {
                return parentName + " (" + parentPhone + ")";
            } else if (!parentName.isEmpty()) {
                return parentName;
            }
        }
        return "";
    }

    private String translateParticipantType(String type, LanguageManager languageManager) {
        if (type == null) return "";

        switch (type) {
            case "ADULT":
                return languageManager.getText("workshop.participants.type.adult");
            case "CHILD":
                return languageManager.getText("workshop.participants.type.child");
            default:
                return type;
        }
    }

    private String translatePaymentStatus(String status, LanguageManager languageManager) {
        if (status == null) return "";

        switch (status) {
            case "PENDING":
                return languageManager.getText("workshop.participants.payment.status.pending");
            case "PAID":
                return languageManager.getText("workshop.participants.payment.status.paid");
            case "REFUNDED":
                return languageManager.getText("workshop.participants.payment.status.refunded");
            case "CANCELLED":
                return languageManager.getText("workshop.participants.payment.status.cancelled");
            default:
                return status;
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            // Try to parse and format the date for better readability
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            // If parsing fails, return the original string
            return dateString;
        }
    }

    private void handleRefresh() {
        LanguageManager languageManager = LanguageManager.getInstance();

        loadWorkshopParticipants();
        loadAvailableParticipants();
        updateStatistics();

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle(languageManager.getText("workshop.participants.refresh.success.title"));
        success.setContentText(languageManager.getText("workshop.participants.refresh.success.content"));
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
        LanguageManager languageManager = LanguageManager.getInstance();
        System.out.println("Add selected adults clicked");

        java.util.List<Contact> selectedAdults = availableAdultsList.stream()
                .filter(Contact::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected adults count: " + selectedAdults.size());

        if (selectedAdults.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.no.selection.warning.title"));
            warning.setContentText(languageManager.getText("workshop.participants.no.selection.adults.content"));
            warning.showAndWait();
            return;
        }

        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.no.workshop.warning.title"));
            warning.setContentText(languageManager.getText("workshop.participants.no.workshop.warning.content"));
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
                message.append(languageManager.getText("workshop.participants.add.adults.success.content")
                        .replace("{0}", String.valueOf(addedContacts.size())));
            }
            if (!skippedContacts.isEmpty()) {
                if (message.length() > 0) message.append("\n\n");
                message.append(languageManager.getText("workshop.participants.skipped.message")
                        .replace("{0}", String.valueOf(skippedContacts.size()))
                        .replace("{1}", languageManager.getText("workshop.participants.skipped.adults"))
                        .replace("{2}", String.join(", ", skippedContacts)));
            }

            Alert result = new Alert(addedContacts.isEmpty() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
            result.setTitle(languageManager.getText("workshop.participants.add.adults.success.title"));
            result.setHeaderText(addedContacts.isEmpty() ?
                    languageManager.getText("workshop.participants.partial.add.warning.header") :
                    languageManager.getText("workshop.participants.add.adults.success.header"));
            result.setContentText(message.toString());
            result.showAndWait();

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle(languageManager.getText("workshop.participants.add.error.title"));
            error.setContentText(languageManager.getText("workshop.participants.add.adults.error.content")
                    .replace("{0}", e.getMessage()));
            error.showAndWait();
            e.printStackTrace();
        }
    }


    private void handleAddSelectedChildren() {
        LanguageManager languageManager = LanguageManager.getInstance();
        System.out.println("Add selected children clicked");

        java.util.List<UnderagedMember> selectedChildren = availableChildrenList.stream()
                .filter(UnderagedMember::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected children count: " + selectedChildren.size());

        if (selectedChildren.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.no.selection.warning.title"));
            warning.setContentText(languageManager.getText("workshop.participants.no.selection.children.content"));
            warning.showAndWait();
            return;
        }

        if (currentWorkshop == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle(languageManager.getText("workshop.participants.no.workshop.warning.title"));
            warning.setContentText(languageManager.getText("workshop.participants.no.workshop.warning.content"));
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
                message.append(languageManager.getText("workshop.participants.add.children.success.content")
                        .replace("{0}", String.valueOf(addedChildren.size())));
            }
            if (!skippedChildren.isEmpty()) {
                if (message.length() > 0) message.append("\n\n");
                message.append(languageManager.getText("workshop.participants.skipped.message")
                        .replace("{0}", String.valueOf(skippedChildren.size()))
                        .replace("{1}", languageManager.getText("workshop.participants.skipped.children"))
                        .replace("{2}", String.join(", ", skippedChildren)));
            }

            Alert result = new Alert(addedChildren.isEmpty() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
            result.setTitle(languageManager.getText("workshop.participants.add.children.success.title"));
            result.setHeaderText(addedChildren.isEmpty() ?
                    languageManager.getText("workshop.participants.partial.add.children.warning.header") :
                    languageManager.getText("workshop.participants.add.children.success.header"));
            result.setContentText(message.toString());
            result.showAndWait();

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle(languageManager.getText("workshop.participants.add.error.title"));
            error.setContentText(languageManager.getText("workshop.participants.add.children.error.content")
                    .replace("{0}", e.getMessage()));
            error.showAndWait();
            e.printStackTrace();
        }
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