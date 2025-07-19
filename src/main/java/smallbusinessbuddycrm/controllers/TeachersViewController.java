package smallbusinessbuddycrm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.model.Teacher;

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

    // UI Controls
    @FXML private Button createTeacherButton;
    @FXML private Button deleteSelectedButton;
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

        System.out.println("TeachersViewController initialized successfully");
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

        // Set up edit button column
        editColumn.setCellFactory(tc -> new TableCell<Teacher, Void>() {
            private final Button editButton = new Button("Edit");

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
        refreshButton.setOnAction(e -> handleRefresh());

        System.out.println("Event handlers setup completed");
    }

    private void handleCreateTeacher() {
        try {
            // Create a dialog for adding a new teacher
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle("Add New Teacher");
            dialog.setHeaderText("Enter teacher information");

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("First Name");

            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Last Name");

            TextField emailField = new TextField();
            emailField.setPromptText("Email");

            TextField phoneField = new TextField();
            phoneField.setPromptText("Phone Number");

            content.getChildren().addAll(
                    new Label("First Name:"), firstNameField,
                    new Label("Last Name:"), lastNameField,
                    new Label("Email:"), emailField,
                    new Label("Phone:"), phoneField
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Enable/disable OK button based on input
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
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
                if (dialogButton == ButtonType.OK) {
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
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Teacher Added");
                    successAlert.setContentText("Teacher " + newTeacher.getFullName() + " has been added successfully!");
                    successAlert.showAndWait();

                    System.out.println("New teacher added: " + newTeacher.getFullName());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to Add Teacher");
                    errorAlert.setContentText("Could not save the teacher to the database.");
                    errorAlert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating teacher: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Create Teacher Failed");
            errorAlert.setContentText("An error occurred while creating the teacher: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void handleEditTeacher(Teacher teacher) {
        try {
            // Create a dialog for editing the teacher
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle("Edit Teacher");
            dialog.setHeaderText("Edit teacher: " + teacher.getFullName());

            // Create the dialog content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField firstNameField = new TextField(teacher.getFirstName());
            TextField lastNameField = new TextField(teacher.getLastName());
            TextField emailField = new TextField(teacher.getEmail() != null ? teacher.getEmail() : "");
            TextField phoneField = new TextField(teacher.getPhoneNum() != null ? teacher.getPhoneNum() : "");

            content.getChildren().addAll(
                    new Label("First Name:"), firstNameField,
                    new Label("Last Name:"), lastNameField,
                    new Label("Email:"), emailField,
                    new Label("Phone:"), phoneField
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Enable/disable OK button based on input
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

            // Validation
            firstNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || lastNameField.getText().trim().isEmpty());
            });
            lastNameField.textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty() || firstNameField.getText().trim().isEmpty());
            });

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
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
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Teacher Updated");
                    successAlert.setContentText("Teacher " + teacher.getFullName() + " has been updated successfully!");
                    successAlert.showAndWait();

                    System.out.println("Teacher updated: " + teacher.getFullName());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to Update Teacher");
                    errorAlert.setContentText("Could not save the changes to the database.");
                    errorAlert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("Error editing teacher: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Edit Teacher Failed");
            errorAlert.setContentText("An error occurred while editing the teacher: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void handleDeleteSelected() {
        System.out.println("Delete selected teachers clicked"); // Debug

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
            alert.setTitle("No Selection");
            alert.setHeaderText("No teachers selected");
            alert.setContentText("Please select one or more teachers to delete using the checkboxes.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete selected teachers?");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedTeachers.size() +
                " teacher" + (selectedTeachers.size() > 1 ? "s" : "") + "? This action cannot be undone.");

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
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Teachers deleted");
                    successAlert.setContentText("Successfully deleted " + selectedTeachers.size() + " teacher(s).");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Delete Failed");
                    errorAlert.setContentText("Failed to delete the selected teachers from the database.");
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting teachers: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Delete Failed");
                errorAlert.setContentText("An error occurred while deleting teachers: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleRefresh() {
        loadTeachers();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText("Data Refreshed");
        successAlert.setContentText("Teacher data has been refreshed successfully!");
        successAlert.showAndWait();
    }
}