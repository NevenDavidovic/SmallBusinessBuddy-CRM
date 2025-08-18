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

public class TeachersViewController implements Initializable {

    @FXML private TableView<Teacher> teachersTable;
    @FXML private TableColumn<Teacher, Boolean> selectColumn;
    @FXML private TableColumn<Teacher, Void> editColumn;
    @FXML private TableColumn<Teacher, String> firstNameColumn;
    @FXML private TableColumn<Teacher, String> lastNameColumn;
    @FXML private TableColumn<Teacher, String> emailColumn;
    @FXML private TableColumn<Teacher, String> phoneColumn;
    @FXML private TableColumn<Teacher, String> createdAtColumn;
    @FXML private TableColumn<Teacher, String> updatedAtColumn;
    @FXML private Label teachersPageTitle;

    // UI Controls
    @FXML private Button createTeacherButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;

    // Data lists
    private ObservableList<Teacher> allTeachersList = FXCollections.observableArrayList();
    private FilteredList<Teacher> filteredTeachersList;

    // DAO
    private TeacherDAO teacherDAO = new TeacherDAO();

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

    private void updateRecordCount() {
        int count = filteredTeachersList.size();
        recordCountLabel.setText(count + " record" + (count != 1 ? "s" : ""));
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createTeacherButton.setOnAction(e -> handleCreateTeacher());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        exportButton.setOnAction(e -> handleExportTeachers());
        refreshButton.setOnAction(e -> handleRefresh());

        System.out.println("Event handlers setup completed");
    }

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