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

/**
 * Controller class for comprehensive workshop management with advanced table interface.
 *
 * This controller provides a complete workshop management system with sophisticated features:
 * - Advanced table view with multiple action columns and data visualization
 * - Teacher assignment management with real-time cache optimization
 * - Participant management integration with modal window support
 * - Multi-criteria search and filtering system (status-based and text search)
 * - Batch operations for efficient workshop management
 * - Complete internationalization with dynamic language switching
 * - Status tracking with visual indicators (active/upcoming/past/draft)
 * - Real-time data synchronization and cache management
 *
 * Key Features:
 * - Workshop CRUD Operations: Complete create, read, update, delete functionality
 * - Teacher Management: Assign, change, or remove teachers from workshops
 * - Participant Integration: Direct access to participant management for each workshop
 * - Advanced Filtering: Status-based filters (all/active/upcoming/past) plus text search
 * - Batch Operations: Multi-select deletion with confirmation dialogs
 * - Performance Optimization: Teacher name caching for efficient table rendering
 * - Status Visualization: Real-time status calculation and display
 * - Internationalization: Full language support with pluralization rules
 *
 * Table Management Features:
 * - Checkbox selection column for batch operations
 * - Inline edit buttons for quick workshop modification
 * - Teacher management buttons with assignment dialogs
 * - Participant management buttons opening dedicated windows
 * - Status indicators with color coding and localized text
 * - Duration calculation and formatting
 * - Creation date tracking and display
 * - Real-time participant count updates
 *
 * Search & Filter System:
 * - Text search across workshop names and teacher names
 * - Status-based filtering (active, upcoming, past workshops)
 * - Real-time filter application with instant results
 * - Visual filter button states with active/inactive styling
 * - Search highlighting and case-insensitive matching
 *
 * Teacher Assignment Management:
 * - Teacher selection dialogs with current assignment indication
 * - "No Teacher" option for workshops without assigned teachers
 * - Teacher cache optimization for fast name resolution
 * - Assignment change tracking with detailed user feedback
 * - Database integration for persistent teacher assignments
 *
 * Internationalization Support:
 * - Dynamic language switching for all UI elements
 * - Localized status text (active/upcoming/past/draft)
 * - Language-specific pluralization rules (Croatian vs English)
 * - Localized error messages and confirmation dialogs
 * - Date formatting according to locale preferences
 *
 * The controller integrates with multiple DAO classes (WorkshopDAO, TeacherDAO,
 * WorkshopParticipantDAO) and provides seamless navigation to related management
 * windows (participant management, workshop creation/editing dialogs).
 *
 * Performance Considerations:
 * - Teacher name caching reduces database queries during table rendering
 * - Filtered lists provide efficient search without full data reloading
 * - Lazy loading of participant counts for optimal performance
 * - Smart cache invalidation when data changes occur
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class WorkshopsViewController implements Initializable {

    // Table View and Columns
    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableColumn<Workshop, Boolean> selectColumn;
    @FXML private TableColumn<Workshop, Void> editColumn;
    @FXML private TableColumn<Workshop, String> nameColumn;
    @FXML private TableColumn<Workshop, String> fromDateColumn;
    @FXML private TableColumn<Workshop, String> toDateColumn;
    @FXML private TableColumn<Workshop, String> durationColumn;
    @FXML private TableColumn<Workshop, String> statusColumn;
    @FXML private TableColumn<Workshop, String> teacherColumn;
    @FXML private TableColumn<Workshop, String> participantCountColumn;
    @FXML private TableColumn<Workshop, Void> manageParticipantsColumn;
    @FXML private TableColumn<Workshop, Void> manageTeacherColumn;
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

    // Data Collections
    private ObservableList<Workshop> allWorkshopsList = FXCollections.observableArrayList();
    private FilteredList<Workshop> filteredWorkshopsList;

    // Database Access Objects
    private WorkshopDAO workshopDAO = new WorkshopDAO();
    private WorkshopParticipantDAO participantDAO = new WorkshopParticipantDAO();
    private TeacherDAO teacherDAO = new TeacherDAO();

    // Performance Cache
    private Map<Integer, String> teacherNamesCache;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up teacher cache, table columns, search/filter functionality, loads workshop data,
     * configures event handlers, and initializes internationalization support.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
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

    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh page title, buttons, table headers,
     * placeholders, and record count display with localized text. Refreshes table
     * to update cell values and status displays.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update page title
        if (workshopsPageTitle != null) {
            workshopsPageTitle.setText(languageManager.getText("workshops.page.title"));
        }

        // Update action buttons
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setText(languageManager.getText("workshops.delete.selected"));
        }
        if (createWorkshopButton != null) {
            createWorkshopButton.setText(languageManager.getText("workshops.create.workshop"));
        }
        if (refreshButton != null) {
            refreshButton.setText(languageManager.getText("workshops.refresh"));
        }

        // Update filter buttons
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

        // Update search field placeholder
        if (searchField != null) {
            searchField.setPromptText(languageManager.getText("workshops.search.placeholder"));
        }

        // Update table column headers
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

        // Update table placeholder
        if (workshopsTable != null) {
            workshopsTable.setPlaceholder(new Label(languageManager.getText("workshops.no.workshops.found")));
        }

        // Refresh table to update localized cell values
        if (workshopsTable != null) {
            workshopsTable.refresh();
        }

        // Update record count display
        updateRecordCount();

        System.out.println("Workshops view texts updated");
    }

    /**
     * Loads all teachers into a cache for quick name lookup during table display.
     * Creates a map of teacher IDs to full names for efficient teacher name resolution
     * in workshop table cells. Handles loading errors with empty map fallback.
     */
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

    /**
     * Configures all table columns with appropriate cell factories and value factories.
     * Sets up checkbox selection, action buttons (edit, manage participants, manage teacher),
     * data columns with formatting, and applies conditional styling. Includes teacher
     * name resolution and participant count calculation.
     */
    private void setupTable() {
        // Configure checkbox selection column
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

        // Configure edit button column
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

        // Configure manage participants column
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

        // Configure manage teacher column
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

        // Configure data column bindings
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

        // Configure teacher column with cache lookup
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

    /**
     * Initializes search functionality and filtering system.
     * Creates filtered list wrapper around workshop data and sets up real-time
     * search listener that triggers filter updates on text changes.
     */
    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredWorkshopsList = new FilteredList<>(allWorkshopsList, p -> true);

        // Set the table to use the filtered list
        workshopsTable.setItems(filteredWorkshopsList);

        // Set up real-time search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    /**
     * Updates the filtered list based on current search text and active filter.
     * Applies search criteria across workshop names and teacher names, combined
     * with current filter state (all/active/upcoming/past workshops).
     */
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

    /**
     * Determines if a workshop matches the currently active filter button.
     * Checks which filter button is active based on styling and returns whether
     * the workshop should be displayed according to that filter criteria.
     *
     * @param workshop The Workshop to check against current filter
     * @return true if workshop matches current filter criteria, false otherwise
     */
    private boolean matchesCurrentFilter(Workshop workshop) {
        // Check which filter button is active based on their styling
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

        return true; // Default: show all workshops
    }

    /**
     * Loads all workshops from database and populates the table.
     * Fetches workshops via WorkshopDAO, updates the observable list,
     * and refreshes record count display. Handles loading errors gracefully.
     */
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

    /**
     * Updates the record count label with current filtered workshop count.
     * Displays localized count text with appropriate singular/plural forms
     * and language-specific pluralization rules (Croatian vs English).
     */
    private void updateRecordCount() {
        int count = filteredWorkshopsList.size();
        LanguageManager languageManager = LanguageManager.getInstance();

        // Handle language-specific pluralization
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

    /**
     * Configures event handlers for all interactive UI components.
     * Sets up button click handlers for create, delete, refresh, and filter
     * buttons with appropriate behavior and styling management.
     */
    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        // Main action button handlers
        createWorkshopButton.setOnAction(e -> handleCreateWorkshop());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        refreshButton.setOnAction(e -> handleRefresh());

        // Filter button handlers
        allWorkshopsButton.setOnAction(e -> handleFilterButton(allWorkshopsButton));
        activeWorkshopsButton.setOnAction(e -> handleFilterButton(activeWorkshopsButton));
        upcomingWorkshopsButton.setOnAction(e -> handleFilterButton(upcomingWorkshopsButton));
        pastWorkshopsButton.setOnAction(e -> handleFilterButton(pastWorkshopsButton));

        System.out.println("Event handlers setup completed");
    }

    /**
     * Handles filter button clicks and updates active filter state.
     * Resets all filter button styles to inactive, sets clicked button to active,
     * and triggers filter update to refresh the displayed workshop list.
     *
     * @param clickedButton The filter button that was clicked
     */
    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive state
        allWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        activeWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        upcomingWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        pastWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter to reflect new selection
        updateFilters();
    }

    /**
     * Handles creating a new workshop via dialog.
     * Opens create workshop dialog, processes the result if successful,
     * adds new workshop to the list, updates displays, and refreshes teacher cache.
     */
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

    /**
     * Handles editing an existing workshop via dialog.
     * Opens edit workshop dialog with current workshop data, processes updates,
     * refreshes table display and caches, and provides user feedback.
     *
     * @param workshop The Workshop to edit
     */
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

    /**
     * Handles teacher assignment management for a workshop.
     * Shows teacher selection dialog, processes assignment changes, updates database
     * via DAO, refreshes displays, and provides detailed user feedback with
     * assignment change information.
     *
     * @param workshop The Workshop to manage teacher assignment for
     */
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

    /**
     * Handles deletion of selected workshops with confirmation.
     * Identifies selected workshops, shows confirmation dialog, performs batch
     * deletion via DAO, updates UI, and provides localized user feedback.
     */
    private void handleDeleteSelected() {
        LanguageManager languageManager = LanguageManager.getInstance();
        System.out.println("Delete button clicked");

        // Debug: Print workshop selection status
        System.out.println("=== Current Workshop Selection Status ===");
        for (Workshop workshop : filteredWorkshopsList) {
            System.out.println("Workshop: " + workshop.getName() + ", Selected: " + workshop.isSelected());
        }

        // Get all selected workshops from the filtered list
        List<Workshop> selectedWorkshops = filteredWorkshopsList.stream()
                .filter(Workshop::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected workshops count: " + selectedWorkshops.size());

        if (selectedWorkshops.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageManager.getText("workshops.delete.no.selection.title"));
            alert.setHeaderText(languageManager.getText("workshops.delete.no.selection.header"));
            alert.setContentText(languageManager.getText("workshops.delete.no.selection.content"));
            alert.showAndWait();
            return;
        }

        // Show confirmation dialog
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

                System.out.println("Attempting to delete workshop IDs: " + workshopIds);

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

    /**
     * Handles opening the workshop participants management window.
     * Loads participant management view in modal window, sets workshop context,
     * configures window properties, and refreshes table after window closes
     * to update participant counts.
     *
     * @param workshop The Workshop to manage participants for
     */
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

            // Set window properties
            participantsStage.setMinWidth(1000);
            participantsStage.setMinHeight(700);
            participantsStage.setMaximized(false);

            // Show the stage and wait for it to close
            participantsStage.showAndWait();

            // Refresh workshops table to update participant counts
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

    /**
     * Handles refreshing all workshop data from database.
     * Reloads workshops and teacher cache, updates displays, and shows
     * success confirmation to user with localized messaging.
     */
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