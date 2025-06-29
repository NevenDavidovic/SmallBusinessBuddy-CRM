package smallbusinessbuddycrm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
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

        System.out.println("TeachersViewController initialized successfully");
    }

    private void setupTable() {
        // Set up checkbox column
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setEditable(true);

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
            }

            return matchesSearch;
        });

        updateRecordCount();
    }

    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();
            System.out.println("DAO returned " + teachers.size() + " teachers");

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
            Stage currentStage = (Stage) createTeacherButton.getScene().getWindow();
            CreateTeacherDialog dialog = new CreateTeacherDialog(currentStage);

            if (dialog.showAndWait()) {
                Teacher newTeacher = dialog.getResult();
                if (newTeacher != null) {
                    allTeachersList.add(newTeacher);
                    updateRecordCount();
                    teachersTable.getSelectionModel().select(newTeacher);
                    teachersTable.scrollTo(newTeacher);
                    System.out.println("New teacher added: " + newTeacher.getFullName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening create teacher dialog: " + e.getMessage());
            e.printStackTrace();

            // Show error to user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Create Teacher Failed");
            errorAlert.setContentText("An error occurred while creating the teacher dialog.");
            errorAlert.showAndWait();
        }
    }

    private void handleEditTeacher(Teacher teacher) {
        try {
            Stage currentStage = (Stage) createTeacherButton.getScene().getWindow();
            EditTeacherDialog dialog = new EditTeacherDialog(currentStage, teacher);

            if (dialog.showAndWait()) {
                // Refresh the table to show updated data
                teachersTable.refresh();
                System.out.println("Teacher updated: " + teacher.getFullName());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Teacher Updated");
                successAlert.setContentText("Teacher has been successfully updated.");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error opening edit teacher dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        // Get all selected teachers from the filtered list
        List<Teacher> selectedTeachers = filteredTeachersList.stream()
                .filter(Teacher::isSelected)
                .collect(Collectors.toList());

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
            }
        }
    }

    private void handleExportTeachers() {
        try {
            // Get currently visible teachers (filtered/searched)
            List<Teacher> teachersToExport = new ArrayList<>(filteredTeachersList);

            if (teachersToExport.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Data");
                alert.setHeaderText("No teachers to export");
                alert.setContentText("There are no teachers visible to export. Please check your filters or add some teachers first.");
                alert.showAndWait();
                return;
            }

            // For now, show export info. Later implement actual CSV export
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export Teachers");
            info.setHeaderText("Export functionality");
            info.setContentText("Will export " + teachersToExport.size() + " teachers to CSV.\nExport functionality coming soon!");
            info.showAndWait();

        } catch (Exception e) {
            System.err.println("Error exporting teachers: " + e.getMessage());
            e.printStackTrace();
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

    // Placeholder classes for dialogs - these need to be created
    private static class CreateTeacherDialog {
        private Stage parentStage;

        public CreateTeacherDialog(Stage parentStage) {
            this.parentStage = parentStage;
        }

        public boolean showAndWait() {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Create Teacher");
            info.setHeaderText("Create Teacher Dialog");
            info.setContentText("Create teacher dialog coming soon!");
            info.showAndWait();
            return false; // For now, always return false
        }

        public Teacher getResult() {
            return null;
        }
    }

    private static class EditTeacherDialog {
        private Stage parentStage;
        private Teacher teacher;

        public EditTeacherDialog(Stage parentStage, Teacher teacher) {
            this.parentStage = parentStage;
            this.teacher = teacher;
        }

        public boolean showAndWait() {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Edit Teacher");
            info.setHeaderText("Edit Teacher: " + teacher.getFullName());
            info.setContentText("Edit teacher dialog coming soon!");
            info.showAndWait();
            return false; // For now, always return false
        }
    }
}