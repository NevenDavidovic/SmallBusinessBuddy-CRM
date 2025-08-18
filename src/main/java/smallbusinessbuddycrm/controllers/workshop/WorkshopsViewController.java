package smallbusinessbuddycrm.controllers.workshop;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.database.WorkshopParticipantDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.model.Workshop;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import smallbusinessbuddycrm.utilities.LanguageManager;

public class WorkshopsViewController implements Initializable {

    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableColumn<Workshop, Boolean> selectColumn;
    @FXML private TableColumn<Workshop, Void> editColumn;
    @FXML private TableColumn<Workshop, String> nameColumn;
    @FXML private TableColumn<Workshop, String> fromDateColumn;
    @FXML private TableColumn<Workshop, String> toDateColumn;
    @FXML private TableColumn<Workshop, String> durationColumn;
    @FXML private TableColumn<Workshop, String> statusColumn;
    @FXML private TableColumn<Workshop, String> teacherColumn; // NEW: Teacher column
    @FXML private TableColumn<Workshop, String> participantCountColumn;
    @FXML private TableColumn<Workshop, Void> manageParticipantsColumn;
    @FXML private TableColumn<Workshop, Void> manageTeacherColumn; // NEW: Manage teacher column
    @FXML private TableColumn<Workshop, String> createdAtColumn;

    // UI Controls
    @FXML private Button createWorkshopButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button allWorkshopsButton;
    @FXML private Button activeWorkshopsButton;
    @FXML private Button upcomingWorkshopsButton;
    @FXML private Button pastWorkshopsButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;
    @FXML private Label workshopsPageTitle;

    // Data lists
    private ObservableList<Workshop> allWorkshopsList = FXCollections.observableArrayList();
    private FilteredList<Workshop> filteredWorkshopsList;

    // DAOs
    private WorkshopDAO workshopDAO = new WorkshopDAO();
    private WorkshopParticipantDAO participantDAO = new WorkshopParticipantDAO();
    private TeacherDAO teacherDAO = new TeacherDAO(); // NEW: Teacher DAO

    // Cache for teacher names
    private Map<Integer, String> teacherNamesCache;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadTeacherCache();
        setupTable();
        setupSearchAndFilters();
        loadWorkshops();
        setupEventHandlers();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();

    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Page title
        if (workshopsPageTitle != null) {
            workshopsPageTitle.setText(languageManager.getText("workshops.page.title"));
        }

        // Buttons
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setText(languageManager.getText("workshops.delete.selected"));
        }
        if (createWorkshopButton != null) {
            createWorkshopButton.setText(languageManager.getText("workshops.create.workshop"));
        }
        if (refreshButton != null) {
            refreshButton.setText(languageManager.getText("workshops.refresh"));
        }

        // Filter buttons
        if (allWorkshopsButton != null) {
            allWorkshopsButton.setText(languageManager.getText("workshops.all.workshops"));
        }
        if (activeWorkshopsButton != null) {
            activeWorkshopsButton.setText(languageManager.getText("workshops.active"));
        }
        if (upcomingWorkshopsButton != null) {
            upcomingWorkshopsButton.setText(languageManager.getText("workshops.upcoming"));
        }
        if (pastWorkshopsButton != null) {
            pastWorkshopsButton.setText(languageManager.getText("workshops.past"));
        }

        // Search field
        if (searchField != null) {
            searchField.setPromptText(languageManager.getText("workshops.search.placeholder"));
        }

        // Table columns
        if (editColumn != null) {
            editColumn.setText(languageManager.getText("workshops.column.edit"));
        }
        if (nameColumn != null) {
            nameColumn.setText(languageManager.getText("workshops.column.name"));
        }
        if (fromDateColumn != null) {
            fromDateColumn.setText(languageManager.getText("workshops.column.from.date"));
        }
        if (toDateColumn != null) {
            toDateColumn.setText(languageManager.getText("workshops.column.to.date"));
        }
        if (durationColumn != null) {
            durationColumn.setText(languageManager.getText("workshops.column.duration"));
        }
        if (statusColumn != null) {
            statusColumn.setText(languageManager.getText("workshops.column.status"));
        }
        if (teacherColumn != null) {
            teacherColumn.setText(languageManager.getText("workshops.column.teacher"));
        }
        if (participantCountColumn != null) {
            participantCountColumn.setText(languageManager.getText("workshops.column.participant.count"));
        }
        if (manageParticipantsColumn != null) {
            manageParticipantsColumn.setText(languageManager.getText("workshops.column.manage.participants"));
        }
        if (manageTeacherColumn != null) {
            manageTeacherColumn.setText(languageManager.getText("workshops.column.manage.teacher"));
        }
        if (createdAtColumn != null) {
            createdAtColumn.setText(languageManager.getText("workshops.column.created"));
        }

        // Update table placeholder if exists
        if (workshopsTable != null) {
            workshopsTable.setPlaceholder(new Label(languageManager.getText("workshops.no.workshops.found")));
        }

        // Refresh table to update cell values with new language
        if (workshopsTable != null) {
            workshopsTable.refresh();
        }

        // Update record count
        updateRecordCount();

        System.out.println("Workshops view texts updated");
    }


    // NEW: Load teacher names into cache for quick lookup
    private void loadTeacherCache() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();
            teacherNamesCache = teachers.stream()
                    .collect(Collectors.toMap(
                            Teacher::getId,
                            teacher -> teacher.getFirstName() + " " + teacher.getLastName()
                    ));
            System.out.println("Loaded " + teacherNamesCache.size() + " teachers into cache");
        } catch (Exception e) {
            System.err.println("Error loading teacher cache: " + e.getMessage());
            teacherNamesCache = Map.of(); // Empty map as fallback
        }
    }

    private void setupTable() {
        // FIXED: Set up checkbox column properly
        selectColumn.setCellFactory(tc -> {
            CheckBox checkBox = new CheckBox();
            TableCell<Workshop, Boolean> cell = new TableCell<Workshop, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Workshop workshop = getTableView().getItems().get(getIndex());
                        if (workshop != null) {
                            checkBox.setSelected(workshop.isSelected());
                            checkBox.setOnAction(e -> {
                                workshop.setSelected(checkBox.isSelected());
                                System.out.println("Workshop " + workshop.getName() + " selected: " + checkBox.isSelected());
                            });
                            setGraphic(checkBox);
                        }
                    }
                }
            };
            return cell;
        });

        // Set up edit button column
        editColumn.setCellFactory(tc -> new TableCell<Workshop, Void>() {
            private final Button editButton = new Button();

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                editButton.setPrefWidth(50);
                editButton.setOnAction(event -> {
                    Workshop workshop = getTableView().getItems().get(getIndex());
                    handleEditWorkshop(workshop);
                });

                // Set initial text
                editButton.setText(LanguageManager.getInstance().getText("workshops.action.edit"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Update button text when table refreshes
                    editButton.setText(LanguageManager.getInstance().getText("workshops.action.edit"));
                    setGraphic(editButton);
                }
            }
        });
        // Set up manage participants column
        manageParticipantsColumn.setCellFactory(tc -> new TableCell<Workshop, Void>() {
            private final Button manageButton = new Button();

            {
                manageButton.setStyle("-fx-background-color: #0099cc; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                manageButton.setPrefWidth(80);
                manageButton.setOnAction(event -> {
                    Workshop workshop = getTableView().getItems().get(getIndex());
                    handleManageParticipants(workshop);
                });

                // Set initial text
                manageButton.setText(LanguageManager.getInstance().getText("workshops.action.participants"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Update button text when table refreshes
                    manageButton.setText(LanguageManager.getInstance().getText("workshops.action.participants"));
                    setGraphic(manageButton);
                }
            }
        });
        // NEW: Set up manage teacher column
        manageTeacherColumn.setCellFactory(tc -> new TableCell<Workshop, Void>() {
            private final Button manageButton = new Button();

            {
                manageButton.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                manageButton.setPrefWidth(70);
                manageButton.setOnAction(event -> {
                    Workshop workshop = getTableView().getItems().get(getIndex());
                    handleManageTeacher(workshop);
                });

                // Set initial text
                manageButton.setText(LanguageManager.getInstance().getText("workshops.action.teacher"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Update button text when table refreshes
                    manageButton.setText(LanguageManager.getInstance().getText("workshops.action.teacher"));
                    setGraphic(manageButton);
                }
            }
        });

        // Set up column bindings
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        fromDateColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            return new SimpleStringProperty(workshop.getFormattedFromDate());
        });

        toDateColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            return new SimpleStringProperty(workshop.getFormattedToDate());
        });

        durationColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            long days = workshop.getDurationInDays();
            if (days > 0) {
                LanguageManager languageManager = LanguageManager.getInstance();
                String durationText = languageManager.getText("workshops.duration.days").replace("{0}", String.valueOf(days));
                return new SimpleStringProperty(durationText);
            } else {
                return new SimpleStringProperty("");
            }
        });

        statusColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            LanguageManager languageManager = LanguageManager.getInstance();
            String status;

            if (workshop.isActive()) {
                status = languageManager.getText("workshops.status.active");
            } else if (workshop.isUpcoming()) {
                status = languageManager.getText("workshops.status.upcoming");
            } else if (workshop.isPast()) {
                status = languageManager.getText("workshops.status.past");
            } else {
                status = languageManager.getText("workshops.status.draft");
            }

            return new SimpleStringProperty(status);
        });

        // NEW: Set up teacher column
        teacherColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            LanguageManager languageManager = LanguageManager.getInstance();

            if (workshop.hasTeacher()) {
                String teacherName = teacherNamesCache.get(workshop.getTeacherId());
                return new SimpleStringProperty(teacherName != null ? teacherName :
                        languageManager.getText("workshops.teacher.unknown"));
            } else {
                return new SimpleStringProperty(languageManager.getText("workshops.teacher.no.teacher"));
            }
        });

        participantCountColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            // Get participant count from database
            Map<String, Integer> stats = participantDAO.getWorkshopStatistics(workshop.getId());
            int count = stats.getOrDefault("total_participants", 0);
            return new SimpleStringProperty(String.valueOf(count));
        });

        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Make table editable
        workshopsTable.setEditable(true);
    }

    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredWorkshopsList = new FilteredList<>(allWorkshopsList, p -> true);

        // Set the table to use the filtered list
        workshopsTable.setItems(filteredWorkshopsList);

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredWorkshopsList.setPredicate(workshop -> {
            // If no search text, show workshops based on current filter
            if (searchText.isEmpty()) {
                return matchesCurrentFilter(workshop);
            }

            // Check if search text matches workshop name or teacher name
            boolean matchesSearch = (workshop.getName() != null &&
                    workshop.getName().toLowerCase().contains(searchText)) ||
                    (workshop.hasTeacher() && teacherNamesCache.get(workshop.getTeacherId()) != null &&
                            teacherNamesCache.get(workshop.getTeacherId()).toLowerCase().contains(searchText));

            // Return true only if matches both search and current filter
            return matchesSearch && matchesCurrentFilter(workshop);
        });

        updateRecordCount();
    }

    private boolean matchesCurrentFilter(Workshop workshop) {
        // Check which filter button is active based on their style
        String allStyle = allWorkshopsButton.getStyle();
        String activeStyle = activeWorkshopsButton.getStyle();
        String upcomingStyle = upcomingWorkshopsButton.getStyle();
        String pastStyle = pastWorkshopsButton.getStyle();

        if (allStyle.contains("#f5f8fa")) {
            return true; // Show all workshops
        } else if (activeStyle.contains("#f5f8fa")) {
            return workshop.isActive();
        } else if (upcomingStyle.contains("#f5f8fa")) {
            return workshop.isUpcoming();
        } else if (pastStyle.contains("#f5f8fa")) {
            return workshop.isPast();
        }

        return true; // Default: show all
    }

    private void loadWorkshops() {
        try {
            List<Workshop> workshops = workshopDAO.getAllWorkshops();
            System.out.println("DAO returned " + workshops.size() + " workshops");

            allWorkshopsList.setAll(workshops);
            updateRecordCount();

        } catch (Exception e) {
            System.err.println("Error loading workshops: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRecordCount() {
        int count = filteredWorkshopsList.size();
        LanguageManager languageManager = LanguageManager.getInstance();

        // Handle plural form for Croatian
        String recordText;
        if (languageManager.isCroatian()) {
            String pluralSuffix = (count == 1) ? "" : "a";
            recordText = languageManager.getText("workshops.records")
                    .replace("{0}", String.valueOf(count))
                    .replace("{1}", pluralSuffix);
        } else {
            String pluralSuffix = (count == 1) ? "" : "s";
            recordText = languageManager.getText("workshops.records")
                    .replace("{0}", String.valueOf(count))
                    .replace("{1}", pluralSuffix);
        }

        recordCountLabel.setText(recordText);
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createWorkshopButton.setOnAction(e -> handleCreateWorkshop());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        refreshButton.setOnAction(e -> handleRefresh());

        // Filter buttons
        allWorkshopsButton.setOnAction(e -> handleFilterButton(allWorkshopsButton));
        activeWorkshopsButton.setOnAction(e -> handleFilterButton(activeWorkshopsButton));
        upcomingWorkshopsButton.setOnAction(e -> handleFilterButton(upcomingWorkshopsButton));
        pastWorkshopsButton.setOnAction(e -> handleFilterButton(pastWorkshopsButton));

        System.out.println("Event handlers setup completed");
    }

    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive
        allWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        activeWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        upcomingWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        pastWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter
        updateFilters();
    }

    private void handleCreateWorkshop() {
        try {
            Stage currentStage = (Stage) createWorkshopButton.getScene().getWindow();
            CreateWorkshopDialog dialog = new CreateWorkshopDialog(currentStage);

            if (dialog.showAndWait()) {
                Workshop newWorkshop = dialog.getResult();
                if (newWorkshop != null) {
                    allWorkshopsList.add(newWorkshop);
                    updateRecordCount();
                    workshopsTable.getSelectionModel().select(newWorkshop);
                    workshopsTable.scrollTo(newWorkshop);
                    System.out.println("New workshop added: " + newWorkshop.getName());

                    // Refresh teacher cache in case new teachers were added
                    loadTeacherCache();
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening create workshop dialog: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshops.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshops.create.failed"));
            errorAlert.setContentText(languageManager.getText("workshops.create.error.message").replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    private void handleEditWorkshop(Workshop workshop) {
        try {
            Stage currentStage = (Stage) createWorkshopButton.getScene().getWindow();
            EditWorkshopDialog dialog = new EditWorkshopDialog(currentStage, workshop);

            if (dialog.showAndWait()) {
                // Refresh the table to show updated data
                workshopsTable.refresh();
                updateFilters(); // Re-apply filters in case status changed
                loadTeacherCache(); // Refresh teacher cache in case teacher assignments changed
                System.out.println("Workshop updated: " + workshop.getName());
            }
        } catch (Exception e) {
            System.err.println("Error opening edit workshop dialog: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshops.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshops.edit.failed"));
            errorAlert.setContentText(languageManager.getText("workshops.edit.error.message").replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    // NEW: Handle teacher management for a workshop
    private void handleManageTeacher(Workshop workshop) {
        try {
            LanguageManager languageManager = LanguageManager.getInstance();
            List<Teacher> availableTeachers = teacherDAO.getAllTeachers();

            if (availableTeachers.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(languageManager.getText("workshops.teacher.no.teachers.title"));
                alert.setHeaderText(languageManager.getText("workshops.teacher.no.teachers.header"));
                alert.setContentText(languageManager.getText("workshops.teacher.no.teachers.content"));
                alert.showAndWait();
                return;
            }

            // Create choice dialog for teacher selection
            ChoiceDialog<Teacher> dialog = new ChoiceDialog<>();
            dialog.setTitle(languageManager.getText("workshops.teacher.assign.title"));
            dialog.setHeaderText(languageManager.getText("workshops.teacher.assign.header").replace("{0}", workshop.getName()));
            dialog.setContentText(languageManager.getText("workshops.teacher.assign.content"));

            // Add "No Teacher" option
            Teacher noTeacher = new Teacher();
            noTeacher.setId(-1);
            noTeacher.setFirstName(languageManager.getText("workshops.teacher.no"));
            noTeacher.setLastName(languageManager.getText("workshops.teacher.teacher"));

            dialog.getItems().add(noTeacher);
            dialog.getItems().addAll(availableTeachers);

            // Set current selection
            if (workshop.hasTeacher()) {
                Teacher currentTeacher = availableTeachers.stream()
                        .filter(t -> t.getId() == workshop.getTeacherId())
                        .findFirst()
                        .orElse(noTeacher);
                dialog.setSelectedItem(currentTeacher);
            } else {
                dialog.setSelectedItem(noTeacher);
            }

            Optional<Teacher> result = dialog.showAndWait();

            if (result.isPresent()) {
                Teacher selectedTeacher = result.get();

                boolean success;
                if (selectedTeacher.getId() == -1) {
                    // Remove teacher assignment
                    success = workshopDAO.removeTeacherFromWorkshop(workshop.getId());
                    workshop.setTeacherId(null);
                } else {
                    // Assign new teacher
                    success = workshopDAO.assignTeacherToWorkshop(workshop.getId(), selectedTeacher.getId());
                    workshop.setTeacherId(selectedTeacher.getId());
                }

                if (success) {
                    // Refresh the table and teacher cache
                    loadTeacherCache();
                    workshopsTable.refresh();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("workshops.teacher.success.title"));
                    successAlert.setHeaderText(languageManager.getText("workshops.teacher.success.header"));

                    if (selectedTeacher.getId() == -1) {
                        successAlert.setContentText(languageManager.getText("workshops.teacher.removed.success"));
                    } else {
                        successAlert.setContentText(languageManager.getText("workshops.teacher.assigned.success")
                                .replace("{0}", selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName()));
                    }
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("workshops.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("workshops.teacher.assignment.failed"));
                    errorAlert.setContentText(languageManager.getText("workshops.teacher.assignment.error"));
                    errorAlert.showAndWait();
                }
            }

        } catch (Exception e) {
            System.err.println("Error managing teacher: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshops.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshops.teacher.management.failed"));
            errorAlert.setContentText(languageManager.getText("workshops.teacher.management.error").replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    private void handleDeleteSelected() {
        LanguageManager languageManager = LanguageManager.getInstance();
        System.out.println("Delete button clicked"); // Debug

        // Debug: Print all workshops and their selection status
        System.out.println("=== Current Workshop Selection Status ===");
        for (Workshop workshop : filteredWorkshopsList) {
            System.out.println("Workshop: " + workshop.getName() + ", Selected: " + workshop.isSelected());
        }

        // Get all selected workshops from the filtered list
        List<Workshop> selectedWorkshops = filteredWorkshopsList.stream()
                .filter(Workshop::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected workshops count: " + selectedWorkshops.size()); // Debug

        if (selectedWorkshops.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageManager.getText("workshops.delete.no.selection.title"));
            alert.setHeaderText(languageManager.getText("workshops.delete.no.selection.header"));
            alert.setContentText(languageManager.getText("workshops.delete.no.selection.content"));
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager.getText("workshops.delete.confirm.title"));
        confirmAlert.setHeaderText(languageManager.getText("workshops.delete.confirm.header"));

        String contentText;
        if (selectedWorkshops.size() == 1) {
            contentText = languageManager.getText("workshops.delete.confirm.content.single");
        } else {
            contentText = languageManager.getText("workshops.delete.confirm.content.multiple")
                    .replace("{0}", String.valueOf(selectedWorkshops.size()));
        }
        confirmAlert.setContentText(contentText);

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                List<Integer> workshopIds = selectedWorkshops.stream()
                        .map(Workshop::getId)
                        .collect(Collectors.toList());

                System.out.println("Attempting to delete workshop IDs: " + workshopIds); // Debug

                boolean success = workshopDAO.deleteWorkshops(workshopIds);

                if (success) {
                    // Remove from the original list (filtered list will update automatically)
                    allWorkshopsList.removeAll(selectedWorkshops);
                    updateRecordCount();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("workshops.delete.success.title"));
                    successAlert.setHeaderText(languageManager.getText("workshops.delete.success.header"));

                    String successContent;
                    if (selectedWorkshops.size() == 1) {
                        successContent = languageManager.getText("workshops.delete.success.content.single");
                    } else {
                        successContent = languageManager.getText("workshops.delete.success.content.multiple")
                                .replace("{0}", String.valueOf(selectedWorkshops.size()));
                    }
                    successAlert.setContentText(successContent);
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("workshops.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("workshops.delete.database.failed"));
                    errorAlert.setContentText(languageManager.getText("workshops.delete.database.error"));
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting workshops: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(languageManager.getText("workshops.error.title"));
                errorAlert.setHeaderText(languageManager.getText("workshops.delete.failed"));
                errorAlert.setContentText(languageManager.getText("workshops.delete.error.message").replace("{0}", e.getMessage()));
                errorAlert.showAndWait();
            }
        }
    }

    private void handleManageParticipants(Workshop workshop) {
        try {
            // Load the workshop participants view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/crm/workshop-participants-view.fxml"));
            Parent root = loader.load();

            // Get the controller and set the workshop
            WorkshopParticipantsViewController controller = loader.getController();
            controller.setWorkshop(workshop);

            LanguageManager languageManager = LanguageManager.getInstance();

            // Create new stage for the participants view
            Stage participantsStage = new Stage();
            participantsStage.setTitle(languageManager.getText("workshops.participants.window.title") + " - " + workshop.getName());
            participantsStage.setScene(new Scene(root));
            participantsStage.initModality(Modality.APPLICATION_MODAL);
            participantsStage.initOwner(createWorkshopButton.getScene().getWindow());

            // Set minimum size and make it resizable
            participantsStage.setMinWidth(1000);
            participantsStage.setMinHeight(700);
            participantsStage.setMaximized(false);

            // Show the stage
            participantsStage.showAndWait();

            // After closing the participants window, refresh the workshops table
            // to update participant counts in case they changed
            workshopsTable.refresh();

        } catch (Exception e) {
            System.err.println("Error opening participant management: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("workshops.error.title"));
            errorAlert.setHeaderText(languageManager.getText("workshops.participants.open.failed"));
            errorAlert.setContentText(languageManager.getText("workshops.participants.open.error").replace("{0}", e.getMessage()));
            errorAlert.showAndWait();
        }
    }

    private void handleRefresh() {
        loadWorkshops();
        loadTeacherCache(); // Also refresh teacher cache

        LanguageManager languageManager = LanguageManager.getInstance();
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(languageManager.getText("workshops.refresh.success.title"));
        successAlert.setHeaderText(languageManager.getText("workshops.refresh.success.header"));
        successAlert.setContentText(languageManager.getText("workshops.refresh.success.content"));
        successAlert.showAndWait();
    }
}