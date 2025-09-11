package smallbusinessbuddycrm.controllers.teacher;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A comprehensive teacher management dialog that provides full CRUD (Create, Read, Update, Delete)
 * operations for teacher records in the Small Business Buddy CRM system.
 *
 * <p>This dialog features:</p>
 * <ul>
 *   <li><strong>Data Table View:</strong> Displays all teachers in a sortable, filterable table</li>
 *   <li><strong>Search Functionality:</strong> Real-time filtering by name, email, or phone number</li>
 *   <li><strong>CRUD Operations:</strong> Add new teachers, edit existing records, delete multiple teachers</li>
 *   <li><strong>Multi-Selection:</strong> Checkbox-based selection for batch operations</li>
 *   <li><strong>Internationalization:</strong> Full support for language switching with dynamic text updates</li>
 *   <li><strong>Data Persistence:</strong> Direct integration with TeacherDAO for database operations</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error messaging and user feedback</li>
 * </ul>
 *
 * <p>The dialog supports real-time language switching through the LanguageManager, automatically
 * updating all UI text, error messages, and table headers when the application language changes.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * TeacherManagementDialog dialog = new TeacherManagementDialog(parentStage);
 * boolean result = dialog.showAndWait();
 * // Dialog result indicates if any changes were made
 * }</pre>
 *
 * <p><strong>Database Integration:</strong> This dialog directly modifies teacher data in the database.
 * All changes (add, edit, delete) are immediately persisted through the TeacherDAO.</p>
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 * @since 1.0
 * @see CreateEditTeacherDialog
 * @see Teacher
 * @see TeacherDAO
 * @see LanguageManager
 */
public class TeacherManagementDialog {

    /** The main dialog stage window */
    private Stage dialog;

    /** Flag indicating whether any operations were performed (always true for this dialog) */
    private boolean result = false;

    /** Data Access Object for teacher database operations */
    private TeacherDAO teacherDAO;

    /** Observable list containing all teachers loaded from the database */
    private ObservableList<Teacher> allTeachers;

    /** Filtered view of teachers based on search criteria */
    private FilteredList<Teacher> filteredTeachers;

    /** Main table view displaying teacher data */
    private TableView<Teacher> teachersTable;

    /** Text field for search/filter functionality */
    private TextField searchField;

    /** Label displaying the current record count */
    private Label recordCountLabel;

    /** Language manager for internationalization support */
    private LanguageManager languageManager;

    /**
     * Set tracking selected teacher IDs for batch operations.
     * Uses teacher IDs rather than object references to maintain selection
     * across table refreshes and data updates.
     */
    private Set<Integer> selectedTeacherIds = new HashSet<>();

    // UI Components that require language updates
    /** Main title label at the top of the dialog */
    private Label titleLabel;

    /** Button for deleting all selected teachers */
    private Button deleteSelectedButton;

    /** Button for adding a new teacher */
    private Button addTeacherButton;

    /** Button for closing the dialog */
    private Button closeButton;

    /** Table column containing edit buttons for each teacher */
    private TableColumn<Teacher, Void> editColumn;

    /** Table column displaying teacher first names */
    private TableColumn<Teacher, String> firstNameColumn;

    /** Table column displaying teacher last names */
    private TableColumn<Teacher, String> lastNameColumn;

    /** Table column displaying teacher email addresses */
    private TableColumn<Teacher, String> emailColumn;

    /** Table column displaying teacher phone numbers */
    private TableColumn<Teacher, String> phoneColumn;

    /** Table column displaying teacher creation timestamps */
    private TableColumn<Teacher, String> createdColumn;

    /**
     * Constructs a new TeacherManagementDialog with full teacher management capabilities.
     * Initializes the dialog UI, loads teacher data from the database, and sets up
     * language change listeners for internationalization support.
     *
     * @param parent the parent stage that owns this modal dialog, used for proper window management
     *               and ensuring the dialog appears centered relative to the parent
     */
    public TeacherManagementDialog(Stage parent) {
        this.teacherDAO = new TeacherDAO();
        this.languageManager = LanguageManager.getInstance();

        createDialog(parent);
        loadTeachers();

        // Add language change listener
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements when the application language changes.
     * This method is called automatically when a language change event is fired
     * by the LanguageManager, ensuring that the entire dialog interface is
     * immediately updated to reflect the new language.
     *
     * <p>Updates include:</p>
     * <ul>
     *   <li>Dialog title and main heading</li>
     *   <li>All button labels</li>
     *   <li>Table column headers</li>
     *   <li>Placeholder text for search field and empty table</li>
     *   <li>Record count labels</li>
     * </ul>
     */
    private void updateTexts() {
        // Update dialog title
        if (dialog != null) {
            dialog.setTitle(languageManager.getText("teacher.management.dialog.title"));
        }

        // Update main title label
        if (titleLabel != null) {
            titleLabel.setText(languageManager.getText("teacher.management.title"));
        }

        // Update buttons
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setText(languageManager.getText("teacher.management.button.delete.selected"));
        }
        if (addTeacherButton != null) {
            addTeacherButton.setText(languageManager.getText("teacher.management.button.add"));
        }
        if (closeButton != null) {
            closeButton.setText(languageManager.getText("teacher.management.button.close"));
        }

        // Update search field prompt
        if (searchField != null) {
            searchField.setPromptText(languageManager.getText("teacher.management.search.prompt"));
        }

        // Update table columns
        if (editColumn != null) {
            editColumn.setText(languageManager.getText("teacher.management.column.edit"));
        }
        if (firstNameColumn != null) {
            firstNameColumn.setText(languageManager.getText("teacher.management.column.first.name"));
        }
        if (lastNameColumn != null) {
            lastNameColumn.setText(languageManager.getText("teacher.management.column.last.name"));
        }
        if (emailColumn != null) {
            emailColumn.setText(languageManager.getText("teacher.management.column.email"));
        }
        if (phoneColumn != null) {
            phoneColumn.setText(languageManager.getText("teacher.management.column.phone"));
        }
        if (createdColumn != null) {
            createdColumn.setText(languageManager.getText("teacher.management.column.created"));
        }

        // Update table placeholder
        if (teachersTable != null) {
            teachersTable.setPlaceholder(new Label(languageManager.getText("teacher.management.table.placeholder")));
        }

        // Update record count
        updateRecordCount();
    }

    /**
     * Creates and configures the main dialog window with all UI components.
     * Sets up the modal dialog with proper styling, layout, and event handlers.
     * The dialog includes a header section, search functionality, data table,
     * and action buttons arranged in a vertical layout.
     *
     * @param parent the parent stage for this modal dialog, used for ownership and positioning
     */
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(languageManager.getText("teacher.management.dialog.title"));
        dialog.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header section
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label(languageManager.getText("teacher.management.title"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        recordCountLabel = new Label(languageManager.getText("teacher.management.count.zero"));
        recordCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #71757a;");

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, javafx.scene.layout.Priority.ALWAYS);

        deleteSelectedButton = new Button(languageManager.getText("teacher.management.button.delete.selected"));
        deleteSelectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());

        addTeacherButton = new Button(languageManager.getText("teacher.management.button.add"));
        addTeacherButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4;");
        addTeacherButton.setOnAction(e -> handleAddTeacher());

        buttonBox.getChildren().addAll(deleteSelectedButton, addTeacherButton);
        headerBox.getChildren().addAll(titleLabel, recordCountLabel, buttonBox);

        // Search section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText(languageManager.getText("teacher.management.search.prompt"));
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-border-color: #dfe3eb;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());

        searchBox.getChildren().add(searchField);

        // Teachers table
        createTeachersTable();

        // Close button
        HBox closeButtonBox = new HBox();
        closeButtonBox.setAlignment(Pos.CENTER_RIGHT);

        closeButton = new Button(languageManager.getText("teacher.management.button.close"));
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4; -fx-padding: 8 16;");
        closeButton.setOnAction(e -> {
            result = true;
            dialog.close();
        });

        closeButtonBox.getChildren().add(closeButton);

        root.getChildren().addAll(headerBox, searchBox, teachersTable, closeButtonBox);

        Scene scene = new Scene(root, 800, 600);
        dialog.setScene(scene);
    }

    /**
     * Creates and configures the main data table with all columns and cell factories.
     * Sets up a comprehensive table view including:
     * - Selection checkboxes for batch operations
     * - Edit buttons for individual teacher modification
     * - Data columns for all teacher properties
     * - Proper cell value factories and formatting
     *
     * <p>The table supports multi-selection through checkboxes and provides
     * inline edit functionality through dedicated edit buttons in each row.</p>
     */
    private void createTeachersTable() {
        teachersTable = new TableView<>();
        teachersTable.setPrefHeight(400);
        teachersTable.setStyle("-fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-width: 1px;");

        // Selection checkbox column
        TableColumn<Teacher, Boolean> selectColumn = new TableColumn<>("");
        selectColumn.setPrefWidth(40);
        selectColumn.setCellFactory(tc -> new TableCell<Teacher, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getIndex() >= teachersTable.getItems().size()) {
                    setGraphic(null);
                } else {
                    Teacher teacher = teachersTable.getItems().get(getIndex());
                    checkBox.setSelected(selectedTeacherIds.contains(teacher.getId()));
                    checkBox.setOnAction(event -> {
                        if (checkBox.isSelected()) {
                            selectedTeacherIds.add(teacher.getId());
                        } else {
                            selectedTeacherIds.remove(teacher.getId());
                        }
                    });
                    setGraphic(checkBox);
                }
            }
        });

        // Edit button column
        editColumn = new TableColumn<>(languageManager.getText("teacher.management.column.edit"));
        editColumn.setPrefWidth(60);
        editColumn.setCellFactory(tc -> new TableCell<Teacher, Void>() {
            private final Button editButton = new Button(languageManager.getText("teacher.management.button.edit"));

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
                    // Update button text when language changes
                    editButton.setText(languageManager.getText("teacher.management.button.edit"));
                    setGraphic(editButton);
                }
            }
        });

        // Data columns
        firstNameColumn = new TableColumn<>(languageManager.getText("teacher.management.column.first.name"));
        firstNameColumn.setPrefWidth(120);
        firstNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName()));

        lastNameColumn = new TableColumn<>(languageManager.getText("teacher.management.column.last.name"));
        lastNameColumn.setPrefWidth(120);
        lastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        emailColumn = new TableColumn<>(languageManager.getText("teacher.management.column.email"));
        emailColumn.setPrefWidth(200);
        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        phoneColumn = new TableColumn<>(languageManager.getText("teacher.management.column.phone"));
        phoneColumn.setPrefWidth(140);
        phoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhoneNum()));

        createdColumn = new TableColumn<>(languageManager.getText("teacher.management.column.created"));
        createdColumn.setPrefWidth(140);
        createdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));

        teachersTable.getColumns().addAll(selectColumn, editColumn, firstNameColumn,
                lastNameColumn, emailColumn, phoneColumn, createdColumn);

        // Set placeholder
        teachersTable.setPlaceholder(new Label(languageManager.getText("teacher.management.table.placeholder")));
    }

    /**
     * Loads all teacher records from the database and populates the table view.
     * Creates observable collections for data binding and sets up filtering support.
     * Handles database errors gracefully by displaying error messages to the user.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Retrieves all teachers from the database via TeacherDAO</li>
     *   <li>Creates observable and filtered lists for data binding</li>
     *   <li>Updates the record count display</li>
     *   <li>Clears any existing selections</li>
     *   <li>Handles and displays database errors</li>
     * </ul>
     */
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();
            allTeachers = FXCollections.observableArrayList(teachers);
            filteredTeachers = new FilteredList<>(allTeachers, p -> true);
            teachersTable.setItems(filteredTeachers);
            updateRecordCount();

            // Clear selection when loading
            selectedTeacherIds.clear();

        } catch (Exception e) {
            showError(languageManager.getText("teacher.management.error.load.title"), e.getMessage());
        }
    }

    /**
     * Updates the table filter based on the current search field content.
     * Performs case-insensitive searching across multiple teacher fields including
     * first name, last name, email, and phone number. Updates the record count
     * display to reflect the filtered results.
     *
     * <p>The filter searches the following fields:</p>
     * <ul>
     *   <li>First Name</li>
     *   <li>Last Name</li>
     *   <li>Email Address</li>
     *   <li>Phone Number</li>
     * </ul>
     *
     * <p>Search is performed using case-insensitive substring matching,
     * so partial matches will be displayed in the results.</p>
     */
    private void updateFilter() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredTeachers.setPredicate(teacher -> {
            if (searchText.isEmpty()) {
                return true;
            }

            return (teacher.getFirstName() != null && teacher.getFirstName().toLowerCase().contains(searchText)) ||
                    (teacher.getLastName() != null && teacher.getLastName().toLowerCase().contains(searchText)) ||
                    (teacher.getEmail() != null && teacher.getEmail().toLowerCase().contains(searchText)) ||
                    (teacher.getPhoneNum() != null && teacher.getPhoneNum().toLowerCase().contains(searchText));
        });

        updateRecordCount();
    }

    /**
     * Updates the record count label with the current number of displayed teachers.
     * Provides internationalized text that changes based on the number of records:
     * different messages for zero, one, or multiple teachers. Supports language
     * switching by using the LanguageManager for all text retrieval.
     *
     * <p>The count reflects the filtered results, not the total number of teachers
     * in the database, so it updates dynamically as the user types in the search field.</p>
     */
    private void updateRecordCount() {
        int count = filteredTeachers.size();
        String countText;
        if (count == 0) {
            countText = languageManager.getText("teacher.management.count.zero");
        } else if (count == 1) {
            countText = languageManager.getText("teacher.management.count.one");
        } else {
            countText = languageManager.getText("teacher.management.count.many").replace("{0}", String.valueOf(count));
        }
        recordCountLabel.setText(countText);
    }

    /**
     * Handles the "Add Teacher" button action by opening the CreateEditTeacherDialog.
     * Creates a new teacher through the dialog interface and, if successful,
     * adds the teacher to the database and updates the table view.
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Opens CreateEditTeacherDialog in create mode (no existing teacher)</li>
     *   <li>If user confirms, attempts to save the new teacher to database</li>
     *   <li>On success, adds teacher to the observable list and shows success message</li>
     *   <li>On failure, displays an error message</li>
     *   <li>Updates record count to reflect changes</li>
     * </ol>
     */
    private void handleAddTeacher() {
        CreateEditTeacherDialog dialog = new CreateEditTeacherDialog((Stage) this.dialog.getOwner(), null);

        if (dialog.showAndWait()) {
            Teacher newTeacher = dialog.getTeacher();
            if (teacherDAO.createTeacher(newTeacher)) {
                allTeachers.add(newTeacher);
                updateRecordCount();
                showSuccess(languageManager.getText("teacher.management.success.added")
                        .replace("{0}", newTeacher.getFullName()));
            } else {
                showError(languageManager.getText("teacher.management.error.add.title"),
                        languageManager.getText("teacher.management.error.add.message"));
            }
        }
    }

    /**
     * Handles editing an existing teacher by opening the CreateEditTeacherDialog.
     * Opens the dialog in edit mode with the teacher's current information pre-populated.
     * If the user confirms changes, updates the teacher in the database and refreshes the display.
     *
     * @param teacher the teacher record to edit, must not be null
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Opens CreateEditTeacherDialog in edit mode with existing teacher data</li>
     *   <li>If user confirms changes, attempts to update teacher in database</li>
     *   <li>On success, refreshes table view and shows success message</li>
     *   <li>On failure, displays an error message and reverts any UI changes</li>
     * </ol>
     */
    private void handleEditTeacher(Teacher teacher) {
        CreateEditTeacherDialog dialog = new CreateEditTeacherDialog((Stage) this.dialog.getOwner(), teacher);

        if (dialog.showAndWait()) {
            if (teacherDAO.updateTeacher(teacher)) {
                teachersTable.refresh();
                showSuccess(languageManager.getText("teacher.management.success.updated")
                        .replace("{0}", teacher.getFullName()));
            } else {
                showError(languageManager.getText("teacher.management.error.update.title"),
                        languageManager.getText("teacher.management.error.update.message"));
            }
        }
    }

    /**
     * Handles the deletion of selected teachers with confirmation dialog.
     * Performs batch deletion of all teachers selected via checkboxes in the table.
     * Provides appropriate confirmation messages and handles both single and multiple deletions.
     *
     * <p>Process flow:</p>
     * <ol>
     *   <li>Validates that at least one teacher is selected</li>
     *   <li>Shows confirmation dialog with details about the deletion</li>
     *   <li>If confirmed, attempts to delete teachers from database</li>
     *   <li>On success, removes teachers from UI and shows success message</li>
     *   <li>On failure, displays error message and maintains current state</li>
     *   <li>Updates record count and clears selections</li>
     * </ol>
     *
     * <p><strong>Warning:</strong> This operation cannot be undone. Teachers are permanently
     * removed from the database upon confirmation.</p>
     */
    private void handleDeleteSelected() {
        List<Teacher> selectedTeachers = filteredTeachers.stream()
                .filter(teacher -> selectedTeacherIds.contains(teacher.getId()))
                .collect(Collectors.toList());

        if (selectedTeachers.isEmpty()) {
            showWarning(languageManager.getText("teacher.management.warning.no.selection.title"),
                    languageManager.getText("teacher.management.warning.no.selection.message"));
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager.getText("teacher.management.confirm.delete.title"));
        confirmAlert.setHeaderText(languageManager.getText("teacher.management.confirm.delete.header"));

        String countText = selectedTeachers.size() == 1 ?
                languageManager.getText("teacher.management.confirm.delete.single") :
                languageManager.getText("teacher.management.confirm.delete.multiple")
                        .replace("{0}", String.valueOf(selectedTeachers.size()));

        confirmAlert.setContentText(languageManager.getText("teacher.management.confirm.delete.content")
                .replace("{0}", countText));

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            List<Integer> teacherIds = selectedTeachers.stream()
                    .map(Teacher::getId)
                    .collect(Collectors.toList());

            if (teacherDAO.deleteTeachers(teacherIds)) {
                allTeachers.removeAll(selectedTeachers);
                selectedTeacherIds.removeAll(teacherIds);
                updateRecordCount();
                teachersTable.refresh();

                String successText = selectedTeachers.size() == 1 ?
                        languageManager.getText("teacher.management.success.deleted.single") :
                        languageManager.getText("teacher.management.success.deleted.multiple")
                                .replace("{0}", String.valueOf(selectedTeachers.size()));

                showSuccess(successText);
            } else {
                showError(languageManager.getText("teacher.management.error.delete.title"),
                        languageManager.getText("teacher.management.error.delete.message"));
            }
        }
    }

    /**
     * Displays a success message dialog to the user.
     * Uses internationalized text for the dialog title and displays the provided message.
     *
     * @param message the success message to display, may contain placeholders that should
     *                be resolved before calling this method
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(languageManager.getText("teacher.management.alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error message dialog to the user.
     * Uses internationalized text for the dialog elements and provides consistent
     * error reporting throughout the application.
     *
     * @param title the title for the error dialog
     * @param message the specific error message to display to the user
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(languageManager.getText("teacher.management.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a warning message dialog to the user.
     * Used for non-critical issues that require user attention, such as
     * attempting operations without proper selections.
     *
     * @param title the title for the warning dialog
     * @param message the warning message to display to the user
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(languageManager.getText("teacher.management.alert.warning.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays the dialog modally and waits for user interaction.
     * Blocks execution until the user closes the dialog through any means
     * (close button, window close, or escape key).
     *
     * @return true indicating that the dialog was displayed (always returns true for this dialog)
     *         Note: Unlike CreateEditTeacherDialog, this dialog doesn't track whether changes
     *         were made, as it's a management interface rather than a single-operation dialog
     */
    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }
}