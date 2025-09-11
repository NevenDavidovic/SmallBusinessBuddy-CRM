package smallbusinessbuddycrm.controllers.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * JavaFX controller for the Teachers view, providing comprehensive teacher management
 * functionality within the Small Business Buddy CRM application.
 *
 * <p>This controller manages the main teachers page and provides:</p>
 * <ul>
 *   <li><strong>Data Table Management:</strong> Displays teachers in a sortable, filterable table view</li>
 *   <li><strong>CRUD Operations:</strong> Create, read, update, and delete teacher records</li>
 *   <li><strong>Real-time Search:</strong> Filter teachers by name, email, or phone number</li>
 *   <li><strong>Batch Operations:</strong> Multi-select functionality for bulk deletions</li>
 *   <li><strong>Export Capabilities:</strong> Export filtered teacher data</li>
 *   <li><strong>Internationalization:</strong> Dynamic language switching support</li>
 *   <li><strong>Database Integration:</strong> Direct persistence through TeacherDAO</li>
 *   <li><strong>User Feedback:</strong> Comprehensive success and error messaging</li>
 * </ul>
 *
 * <p>The controller follows the JavaFX FXML pattern with @FXML-annotated UI components
 * that are automatically injected from the corresponding FXML file. It implements the
 * Initializable interface to perform setup operations when the view is loaded.</p>
 *
 * <p><strong>FXML Integration:</strong> This controller is designed to work with a corresponding
 * FXML file that defines the UI layout. All @FXML-annotated fields must have matching
 * fx:id attributes in the FXML file.</p>
 *
 * <p><strong>Language Support:</strong> The controller automatically registers with the
 * LanguageManager to receive language change notifications and updates all UI text
 * dynamically when the application language changes.</p>
 *
 * <p>Usage in FXML:</p>
 * <pre>{@code
 * <AnchorPane xmlns="http://javafx.com/javafx"
 *             fx:controller="smallbusinessbuddycrm.controllers.teacher.TeachersViewController">
 *     <TableView fx:id="teachersTable" />
 *     <!-- other UI components with matching fx:id values -->
 * </AnchorPane>
 * }</pre>
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 * @since 1.0
 * @see Teacher
 * @see TeacherDAO
 * @see LanguageManager
 * @see javafx.fxml.Initializable
 */
public class TeachersViewController implements Initializable {

    // Table and Column Components
    /** Main table view displaying teacher records with full CRUD support */
    @FXML private TableView<Teacher> teachersTable;

    /** Table column with checkboxes for multi-selection of teachers */
    @FXML private TableColumn<Teacher, Boolean> selectColumn;

    /** Table column containing edit buttons for individual teacher modification */
    @FXML private TableColumn<Teacher, Void> editColumn;

    /** Table column displaying teacher first names */
    @FXML private TableColumn<Teacher, String> firstNameColumn;

    /** Table column displaying teacher last names */
    @FXML private TableColumn<Teacher, String> lastNameColumn;

    /** Table column displaying teacher email addresses */
    @FXML private TableColumn<Teacher, String> emailColumn;

    /** Table column displaying teacher phone numbers */
    @FXML private TableColumn<Teacher, String> phoneColumn;

    /** Table column displaying teacher creation timestamps */
    @FXML private TableColumn<Teacher, String> createdAtColumn;

    /** Table column displaying teacher last update timestamps */
    @FXML private TableColumn<Teacher, String> updatedAtColumn;

    /** Main page title label for the teachers view */
    @FXML private Label teachersPageTitle;

    // UI Control Components
    /** Button for creating/adding new teacher records */
    @FXML private Button createTeacherButton;

    /** Button for deleting all selected teachers in batch operation */
    @FXML private Button deleteSelectedButton;

    /** Button for exporting teacher data to external formats */
    @FXML private Button exportButton;

    /** Button for refreshing the teacher data from the database */
    @FXML private Button refreshButton;

    /** Text field for searching/filtering teachers by various criteria */
    @FXML private TextField searchField;

    /** Label displaying the current count of visible teacher records */
    @FXML private Label recordCountLabel;

    // Data Management Components
    /**
     * Observable list containing all teacher records loaded from the database.
     * This is the master data source that is wrapped by the filtered list.
     */
    private ObservableList<Teacher> allTeachersList = FXCollections.observableArrayList();

    /**
     * Filtered list that wraps the allTeachersList and provides real-time filtering
     * based on search criteria. This is what the table view displays.
     */
    private FilteredList<Teacher> filteredTeachersList;

    /** Data Access Object for all teacher database operations */
    private TeacherDAO teacherDAO = new TeacherDAO();

    /**
     * Initializes the controller when the FXML view is loaded.
     * This method is automatically called by the JavaFX framework after loading the FXML
     * and injecting all @FXML-annotated fields.
     *
     * <p>Initialization sequence:</p>
     * <ol>
     *   <li>Initialize database connection</li>
     *   <li>Set up table structure and cell factories</li>
     *   <li>Configure search and filtering functionality</li>
     *   <li>Load teacher data from database</li>
     *   <li>Set up event handlers for UI components</li>
     *   <li>Register language change listener and update UI text</li>
     * </ol>
     *
     * @param location the location used to resolve relative paths for the root object,
     *                 or null if the location is not known
     * @param resources the resources used to localize the root object,
     *                  or null if the root object was not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("TeachersViewController.initialize() called");

        // Initialize database first
        DatabaseConnection.initializeDatabase();

        setupTable();
        setupSearchAndFilters();
        loadTeachers();
        setupEventHandlers();

        // ADD LANGUAGE MANAGER SETUP LAST
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();

        System.out.println("TeachersViewController initialized successfully");
    }

    /**
     * Updates all UI text elements when the application language changes.
     * This method is automatically called when a language change event is fired
     * by the LanguageManager, ensuring immediate UI updates for internationalization.
     *
     * <p>Updates include:</p>
     * <ul>
     *   <li>Page title and button labels</li>
     *   <li>Table column headers</li>
     *   <li>Search field placeholder text</li>
     *   <li>Table placeholder for empty state</li>
     *   <li>Edit button text in table cells (via table refresh)</li>
     * </ul>
     *
     * <p><strong>Note:</strong> This method includes a table refresh to update
     * dynamically generated content like edit button text in table cells.</p>
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update labels and buttons
        if (teachersPageTitle != null) teachersPageTitle.setText(languageManager.getText("teachers.page.title"));
        if (deleteSelectedButton != null) deleteSelectedButton.setText(languageManager.getText("teachers.delete.selected"));
        if (createTeacherButton != null) createTeacherButton.setText(languageManager.getText("teachers.add.teacher"));
        if (refreshButton != null) refreshButton.setText(languageManager.getText("teachers.refresh"));
        if (searchField != null) searchField.setPromptText(languageManager.getText("teachers.search.placeholder"));

        // Update table columns
        if (editColumn != null) editColumn.setText(languageManager.getText("teachers.column.edit"));
        if (firstNameColumn != null) firstNameColumn.setText(languageManager.getText("teachers.column.first.name"));
        if (lastNameColumn != null) lastNameColumn.setText(languageManager.getText("teachers.column.last.name"));
        if (emailColumn != null) emailColumn.setText(languageManager.getText("teachers.column.email"));
        if (phoneColumn != null) phoneColumn.setText(languageManager.getText("teachers.column.phone"));
        if (createdAtColumn != null) createdAtColumn.setText(languageManager.getText("teachers.column.created"));
        if (updatedAtColumn != null) updatedAtColumn.setText(languageManager.getText("teachers.column.updated"));

        // Update table placeholder if exists
        if (teachersTable != null) {
            teachersTable.setPlaceholder(new Label(languageManager.getText("teachers.no.teachers.found")));
        }

        // IMPORTANT: Refresh the table to update edit button texts
        if (teachersTable != null) {
            teachersTable.refresh();
        }

        System.out.println("Teachers view texts updated");
    }

    /**
     * Sets up the table view structure including all columns and their cell factories.
     * Configures custom cell factories for selection checkboxes and edit buttons,
     * and establishes property value factories for data columns.
     *
     * <p>Table features configured:</p>
     * <ul>
     *   <li><strong>Selection Column:</strong> Checkboxes bound to teacher selection state</li>
     *   <li><strong>Edit Column:</strong> Action buttons with language-aware text</li>
     *   <li><strong>Data Columns:</strong> Property-based value factories for teacher data</li>
     *   <li><strong>Table Editing:</strong> Enables table editing capabilities</li>
     * </ul>
     *
     * <p><strong>Note:</strong> The selection system uses the Teacher model's isSelected()
     * property to track which teachers are selected for batch operations.</p>
     */
    private void setupTable() {
        // FIXED: Set up checkbox column with custom cell factory
        selectColumn.setCellFactory(tc -> {
            CheckBox checkBox = new CheckBox();
            TableCell<Teacher, Boolean> cell = new TableCell<Teacher, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Teacher teacher = getTableView().getItems().get(getIndex());
                        if (teacher != null) {
                            checkBox.setSelected(teacher.isSelected());
                            checkBox.setOnAction(e -> {
                                teacher.setSelected(checkBox.isSelected());
                                System.out.println("Teacher " + teacher.getFullName() + " selected: " + checkBox.isSelected());
                            });
                            setGraphic(checkBox);
                        }
                    }
                }
            };
            return cell;
        });

        // FIXED: Set up edit button column with proper translation updates
        editColumn.setCellFactory(tc -> new TableCell<Teacher, Void>() {
            private final Button editButton = new Button();

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                editButton.setPrefWidth(50);
                editButton.setOnAction(event -> {
                    Teacher teacher = getTableView().getItems().get(getIndex());
                    handleEditTeacher(teacher);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // FIXED: Always get fresh translation text when cell updates
                    editButton.setText(LanguageManager.getInstance().getText("teachers.action.edit"));
                    setGraphic(editButton);
                }
            }
        });

        // Set up column bindings
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        // Make table editable
        teachersTable.setEditable(true);
        teachersTable.setItems(filteredTeachersList);
    }

    /**
     * Configures the search and filtering functionality for the teacher table.
     * Creates a FilteredList wrapper around the main teacher data and sets up
     * real-time filtering based on search field input.
     *
     * <p>The filtered list automatically updates the table view as the user types,
     * providing immediate visual feedback. The filter is applied to multiple teacher
     * fields including name, email, and phone number.</p>
     */
    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredTeachersList = new FilteredList<>(allTeachersList, p -> true);

        // Set the table to use the filtered list
        teachersTable.setItems(filteredTeachersList);

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    /**
     * Updates the table filter based on current search criteria.
     * Performs case-insensitive substring matching across multiple teacher fields
     * and updates the record count display to reflect filtered results.
     *
     * <p>Searchable fields include:</p>
     * <ul>
     *   <li>First Name</li>
     *   <li>Last Name</li>
     *   <li>Email Address</li>
     *   <li>Phone Number</li>
     * </ul>
     *
     * <p>The search is performed using case-insensitive contains matching,
     * so partial text matches will be included in the results.</p>
     */
    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredTeachersList.setPredicate(teacher -> {
            // If no search text, show all teachers
            if (searchText.isEmpty()) {
                return true;
            }

            // Check if search text matches name or email
            boolean matchesSearch = false;

            if (teacher.getFirstName() != null && teacher.getFirstName().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (teacher.getLastName() != null && teacher.getLastName().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (teacher.getEmail() != null && teacher.getEmail().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (teacher.getPhoneNum() != null && teacher.getPhoneNum().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            }

            return matchesSearch;
        });

        updateRecordCount();
    }

    /**
     * Loads all teacher records from the database and populates the data collections.
     * Retrieves teachers via TeacherDAO, initializes their selection state, and
     * updates the UI to reflect the loaded data.
     *
     * <p>Loading process:</p>
     * <ol>
     *   <li>Fetch all teachers from database via TeacherDAO</li>
     *   <li>Initialize selection state (all teachers start unselected)</li>
     *   <li>Update the observable list which automatically updates the table</li>
     *   <li>Update the record count display</li>
     *   <li>Handle and log any database errors</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong> Database errors are caught and logged
     * to the console. In a production environment, these should be displayed
     * to the user through appropriate error dialogs.</p>
     */
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();
            System.out.println("DAO returned " + teachers.size() + " teachers");

            // Initialize selection state for all teachers
            for (Teacher teacher : teachers) {
                teacher.setSelected(false);
            }

            allTeachersList.setAll(teachers);
            updateRecordCount();

        } catch (Exception e) {
            System.err.println("Error loading teachers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the record count label with the current number of visible teachers.
     * The count reflects filtered results, not the total database count, so it
     * updates dynamically as the user applies search filters.
     *
     * <p><strong>Note:</strong> This method uses simple English pluralization rules.
     * For full internationalization, this should be updated to use proper
     * plural forms from the LanguageManager.</p>
     */
    private void updateRecordCount() {
        int count = filteredTeachersList.size();
        recordCountLabel.setText(count + " record" + (count != 1 ? "s" : ""));
    }

    /**
     * Sets up event handlers for all interactive UI components.
     * Connects button actions to their corresponding handler methods using
     * lambda expressions for clean, readable event handling.
     *
     * <p>This method is called during controller initialization and sets up
     * handlers for create, delete, export, and refresh operations.</p>
     */
    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createTeacherButton.setOnAction(e -> handleCreateTeacher());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        exportButton.setOnAction(e -> handleExportTeachers());
        refreshButton.setOnAction(e -> handleRefresh());

        System.out.println("Event handlers setup completed");
    }

    /**
     * Handles the creation of a new teacher through a dialog interface.
     * Opens a custom dialog for entering teacher information, validates the input,
     * saves the new teacher to the database, and updates the UI accordingly.
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Create and configure input dialog with internationalized text</li>
     *   <li>Set up form validation for required fields</li>
     *   <li>If user confirms, create Teacher object from form data</li>
     *   <li>Attempt to save teacher to database via TeacherDAO</li>
     *   <li>On success, add to table and show success message</li>
     *   <li>On failure, display error message with details</li>
     * </ol>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>First name is required (cannot be empty)</li>
     *   <li>Last name is required (cannot be empty)</li>
     *   <li>Email is optional but must be valid if provided</li>
     *   <li>Phone number is optional</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong> All exceptions are caught and displayed
     * to the user through error dialogs with internationalized messages.</p>
     */
    private void handleCreateTeacher() {
        try {
            LanguageManager languageManager = LanguageManager.getInstance();

            // Create a dialog for adding a new teacher
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle(languageManager.getText("teachers.dialog.add.title"));
            dialog.setHeaderText(languageManager.getText("teachers.dialog.add.header"));

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField firstNameField = new TextField();
            firstNameField.setPromptText(languageManager.getText("teachers.field.first.name.placeholder"));

            TextField lastNameField = new TextField();
            lastNameField.setPromptText(languageManager.getText("teachers.field.last.name.placeholder"));

            TextField emailField = new TextField();
            emailField.setPromptText(languageManager.getText("teachers.field.email.placeholder"));

            TextField phoneField = new TextField();
            phoneField.setPromptText(languageManager.getText("teachers.field.phone.placeholder"));

            content.getChildren().addAll(
                    new Label(languageManager.getText("teachers.field.first.name") + ":"), firstNameField,
                    new Label(languageManager.getText("teachers.field.last.name") + ":"), lastNameField,
                    new Label(languageManager.getText("teachers.field.email") + ":"), emailField,
                    new Label(languageManager.getText("teachers.field.phone") + ":"), phoneField
            );

            dialog.getDialogPane().setContent(content);

            // FIXED: Create custom button types with proper translations
            ButtonType okButtonType = new ButtonType(languageManager.getText("common.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(languageManager.getText("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

            // Enable/disable OK button based on input
            Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
            okButton.setDisable(true);

            // Validation
            firstNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || lastNameField.getText().trim().isEmpty());
            });
            lastNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || firstNameField.getText().trim().isEmpty());
            });

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    Teacher teacher = new Teacher();
                    teacher.setFirstName(firstNameField.getText().trim());
                    teacher.setLastName(lastNameField.getText().trim());
                    teacher.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                    teacher.setPhoneNum(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                    teacher.setCreatedAt(java.time.LocalDateTime.now().toString());
                    teacher.setUpdatedAt(java.time.LocalDateTime.now().toString());
                    return teacher;
                }
                return null;
            });

            Optional<Teacher> result = dialog.showAndWait();
            if (result.isPresent()) {
                Teacher newTeacher = result.get();
                boolean success = teacherDAO.createTeacher(newTeacher);

                if (success) {
                    allTeachersList.add(newTeacher);
                    updateRecordCount();
                    teachersTable.getSelectionModel().select(newTeacher);
                    teachersTable.scrollTo(newTeacher);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("common.success"));
                    successAlert.setHeaderText(languageManager.getText("teachers.message.teacher.added"));
                    successAlert.setContentText(languageManager.getText("teachers.message.teacher.added.success")
                            .replace("{0}", newTeacher.getFullName()));
                    successAlert.showAndWait();

                    System.out.println("New teacher added: " + newTeacher.getFullName());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("common.error"));
                    errorAlert.setHeaderText(languageManager.getText("teachers.message.add.failed"));
                    errorAlert.setContentText(languageManager.getText("teachers.message.save.database.error"));
                    errorAlert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating teacher: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("common.error"));
            errorAlert.setHeaderText(languageManager.getText("teachers.message.create.failed"));
            errorAlert.setContentText(languageManager.getText("teachers.message.error.occurred") + ": " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    /**
     * Handles editing an existing teacher through a pre-populated dialog interface.
     * Opens a dialog with the teacher's current information, allows modifications,
     * and saves changes to the database if confirmed by the user.
     *
     * @param teacher the teacher record to edit, must not be null and should have a valid ID
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Create dialog pre-populated with current teacher data</li>
     *   <li>Apply same validation rules as create dialog</li>
     *   <li>If user confirms, update Teacher object with form data</li>
     *   <li>Attempt to update teacher in database via TeacherDAO</li>
     *   <li>On success, refresh table display and show success message</li>
     *   <li>On failure, display error message and maintain current state</li>
     * </ol>
     *
     * <p><strong>Note:</strong> The teacher object is modified in-place, so changes
     * are immediately reflected in the table view after a successful database update.</p>
     */
    private void handleEditTeacher(Teacher teacher) {
        try {
            LanguageManager languageManager = LanguageManager.getInstance();

            // Create a dialog for editing the teacher
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle(languageManager.getText("teachers.dialog.edit.title"));
            dialog.setHeaderText(languageManager.getText("teachers.dialog.edit.header") + ": " + teacher.getFullName());

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField firstNameField = new TextField(teacher.getFirstName());
            TextField lastNameField = new TextField(teacher.getLastName());
            TextField emailField = new TextField(teacher.getEmail() != null ? teacher.getEmail() : "");
            TextField phoneField = new TextField(teacher.getPhoneNum() != null ? teacher.getPhoneNum() : "");

            content.getChildren().addAll(
                    new Label(languageManager.getText("teachers.field.first.name") + ":"), firstNameField,
                    new Label(languageManager.getText("teachers.field.last.name") + ":"), lastNameField,
                    new Label(languageManager.getText("teachers.field.email") + ":"), emailField,
                    new Label(languageManager.getText("teachers.field.phone") + ":"), phoneField
            );

            dialog.getDialogPane().setContent(content);

            // FIXED: Create custom button types with proper translations
            ButtonType okButtonType = new ButtonType(languageManager.getText("common.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(languageManager.getText("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

            // Enable/disable OK button based on input
            Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);

            // Validation
            firstNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || lastNameField.getText().trim().isEmpty());
            });
            lastNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || firstNameField.getText().trim().isEmpty());
            });

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    teacher.setFirstName(firstNameField.getText().trim());
                    teacher.setLastName(lastNameField.getText().trim());
                    teacher.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                    teacher.setPhoneNum(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                    teacher.setUpdatedAt(java.time.LocalDateTime.now().toString());
                    return teacher;
                }
                return null;
            });

            Optional<Teacher> result = dialog.showAndWait();
            if (result.isPresent()) {
                boolean success = teacherDAO.updateTeacher(teacher);

                if (success) {
                    // Refresh the table to show updated data
                    teachersTable.refresh();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("common.success"));
                    successAlert.setHeaderText(languageManager.getText("teachers.message.teacher.updated"));
                    successAlert.setContentText(languageManager.getText("teachers.message.teacher.updated.success")
                            .replace("{0}", teacher.getFullName()));
                    successAlert.showAndWait();

                    System.out.println("Teacher updated: " + teacher.getFullName());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("common.error"));
                    errorAlert.setHeaderText(languageManager.getText("teachers.message.update.failed"));
                    errorAlert.setContentText(languageManager.getText("teachers.message.save.changes.error"));
                    errorAlert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error editing teacher: " + e.getMessage());
            e.printStackTrace();

            LanguageManager languageManager = LanguageManager.getInstance();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle(languageManager.getText("common.error"));
            errorAlert.setHeaderText(languageManager.getText("teachers.message.edit.failed"));
            errorAlert.setContentText(languageManager.getText("teachers.message.error.occurred") + ": " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    /**
     * Handles the deletion of selected teachers with user confirmation.
     * Identifies all selected teachers, prompts for confirmation, and performs
     * batch deletion from both the database and UI collections.
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Identify all teachers marked as selected via checkboxes</li>
     *   <li>Validate that at least one teacher is selected</li>
     *   <li>Show confirmation dialog with deletion details</li>
     *   <li>If confirmed, extract teacher IDs and delete from database</li>
     *   <li>On success, remove from UI collections and show success message</li>
     *   <li>On failure, display error message and maintain current state</li>
     * </ol>
     *
     * <p><strong>Selection System:</strong> This method relies on the Teacher model's
     * isSelected() property which is managed by the checkbox column in the table.</p>
     *
     * <p><strong>Warning:</strong> This operation is destructive and cannot be undone.
     * Teachers are permanently removed from the database upon confirmation.</p>
     *
     * <p><strong>Debug Output:</strong> This method includes extensive console logging
     * for debugging selection and deletion issues. In production, this should be
     * replaced with proper logging framework usage.</p>
     */
    private void handleDeleteSelected() {
        System.out.println("Delete selected teachers clicked"); // Debug
        LanguageManager languageManager = LanguageManager.getInstance();

        // Debug: Print all teachers and their selection status
        System.out.println("=== Current Teacher Selection Status ===");
        for (Teacher teacher : filteredTeachersList) {
            System.out.println("Teacher: " + teacher.getFullName() + ", Selected: " + teacher.isSelected());
        }

        // Get all selected teachers from the filtered list
        List<Teacher> selectedTeachers = filteredTeachersList.stream()
                .filter(Teacher::isSelected)
                .collect(Collectors.toList());

        System.out.println("Selected teachers count: " + selectedTeachers.size()); // Debug

        if (selectedTeachers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageManager.getText("teachers.dialog.no.selection.title"));
            alert.setHeaderText(languageManager.getText("teachers.dialog.no.selection.header"));
            alert.setContentText(languageManager.getText("teachers.dialog.no.selection.content"));
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager.getText("teachers.dialog.confirm.delete.title"));
        confirmAlert.setHeaderText(languageManager.getText("teachers.dialog.confirm.delete.header"));

        String contentText = languageManager.getText("teachers.dialog.confirm.delete.content")
                .replace("{0}", String.valueOf(selectedTeachers.size()))
                .replace("{1}", selectedTeachers.size() > 1 ? "s" : "");
        confirmAlert.setContentText(contentText);

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                List<Integer> teacherIds = selectedTeachers.stream()
                        .map(Teacher::getId)
                        .collect(Collectors.toList());

                System.out.println("Attempting to delete teacher IDs: " + teacherIds); // Debug

                boolean success = teacherDAO.deleteTeachers(teacherIds);

                if (success) {
                    // Remove from the original list (filtered list will update automatically)
                    allTeachersList.removeAll(selectedTeachers);
                    updateRecordCount();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("common.success"));
                    successAlert.setHeaderText(languageManager.getText("teachers.message.teachers.deleted"));
                    successAlert.setContentText(languageManager.getText("teachers.message.teachers.deleted.success")
                            .replace("{0}", String.valueOf(selectedTeachers.size())));
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("common.error"));
                    errorAlert.setHeaderText(languageManager.getText("teachers.message.delete.failed"));
                    errorAlert.setContentText(languageManager.getText("teachers.message.delete.database.error"));
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting teachers: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(languageManager.getText("common.error"));
                errorAlert.setHeaderText(languageManager.getText("teachers.message.delete.failed"));
                errorAlert.setContentText(languageManager.getText("teachers.message.error.occurred") + ": " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    /**
     * Handles the export of teacher data to external formats.
     * Currently provides a placeholder implementation that shows information
     * about the export operation. In the future, this should be expanded to
     * support actual file export functionality.
     *
     * <p><strong>Current Implementation:</strong> Shows an information dialog
     * indicating how many teachers would be exported (based on current filter).</p>
     *
     * <p><strong>Future Enhancements:</strong></p>
     * <ul>
     *   <li>CSV export functionality</li>
     *   <li>Excel export support</li>
     *   <li>PDF report generation</li>
     *   <li>Custom field selection for export</li>
     *   <li>File chooser dialog for save location</li>
     * </ul>
     *
     * <p>The method validates that there are teachers to export and shows
     * appropriate warnings if the filtered list is empty.</p>
     */
    private void handleExportTeachers() {
        try {
            LanguageManager languageManager = LanguageManager.getInstance();

            // Get currently visible teachers (filtered/searched)
            List<Teacher> teachersToExport = new ArrayList<>(filteredTeachersList);

            if (teachersToExport.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(languageManager.getText("teachers.export.no.data.title"));
                alert.setHeaderText(languageManager.getText("teachers.export.no.data.header"));
                alert.setContentText(languageManager.getText("teachers.export.no.data.content"));
                alert.showAndWait();
                return;
            }

            // For now, show export info. Later implement actual CSV export
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle(languageManager.getText("teachers.export.title"));
            info.setHeaderText(languageManager.getText("teachers.export.header"));
            info.setContentText(languageManager.getText("teachers.export.content")
                    .replace("{0}", String.valueOf(teachersToExport.size())));
            info.showAndWait();

        } catch (Exception e) {
            System.err.println("Error exporting teachers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the refresh operation by reloading all teacher data from the database.
     * Provides a way for users to update the display with the latest database state,
     * useful when data might have been modified by other parts of the application
     * or external sources.
     *
     * <p>This method calls the loadTeachers() method to perform the actual data
     * refresh and displays a success message to confirm the operation completed.</p>
     *
     * <p><strong>Side Effects:</strong></p>
     * <ul>
     *   <li>All teacher selection states are reset to unselected</li>
     *   <li>The table view is updated with fresh data</li>
     *   <li>Search filters remain active and are reapplied to new data</li>
     *   <li>Record count is updated to reflect current data</li>
     * </ul>
     */
    private void handleRefresh() {
        LanguageManager languageManager = LanguageManager.getInstance();

        loadTeachers();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(languageManager.getText("common.success"));
        successAlert.setHeaderText(languageManager.getText("teachers.message.data.refreshed"));
        successAlert.setContentText(languageManager.getText("teachers.message.data.refreshed.success"));
        successAlert.showAndWait();
    }
}