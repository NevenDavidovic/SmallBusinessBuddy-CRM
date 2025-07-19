package smallbusinessbuddycrm.controllers;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeacherManagementDialog {

    private Stage dialog;
    private boolean result = false;
    private TeacherDAO teacherDAO;
    private ObservableList<Teacher> allTeachers;
    private FilteredList<Teacher> filteredTeachers;
    private TableView<Teacher> teachersTable;
    private TextField searchField;
    private Label recordCountLabel;

    // Selection tracking (since Teacher model might not have isSelected field)
    private Set<Integer> selectedTeacherIds = new HashSet<>();

    public TeacherManagementDialog(Stage parent) {
        this.teacherDAO = new TeacherDAO();
        createDialog(parent);
        loadTeachers();
    }

    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Teacher Management");
        dialog.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header section
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Teachers");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        recordCountLabel = new Label("0 teachers");
        recordCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #71757a;");

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, javafx.scene.layout.Priority.ALWAYS);

        Button deleteSelectedButton = new Button("Delete Selected");
        deleteSelectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());

        Button addTeacherButton = new Button("Add Teacher");
        addTeacherButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4;");
        addTeacherButton.setOnAction(e -> handleAddTeacher());

        buttonBox.getChildren().addAll(deleteSelectedButton, addTeacherButton);
        headerBox.getChildren().addAll(titleLabel, recordCountLabel, buttonBox);

        // Search section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search teachers by name, email, or phone...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-border-color: #dfe3eb;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());

        searchBox.getChildren().add(searchField);

        // Teachers table
        createTeachersTable();

        // Close button
        HBox closeButtonBox = new HBox();
        closeButtonBox.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("Close");
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
        TableColumn<Teacher, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setPrefWidth(60);
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

        // Data columns
        TableColumn<Teacher, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setPrefWidth(120);
        firstNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName()));

        TableColumn<Teacher, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setPrefWidth(120);
        lastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        TableColumn<Teacher, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setPrefWidth(200);
        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<Teacher, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setPrefWidth(140);
        phoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhoneNum()));

        TableColumn<Teacher, String> createdColumn = new TableColumn<>("Created");
        createdColumn.setPrefWidth(140);
        createdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));

        teachersTable.getColumns().addAll(selectColumn, editColumn, firstNameColumn,
                lastNameColumn, emailColumn, phoneColumn, createdColumn);

        // Set placeholder
        teachersTable.setPlaceholder(new Label("No teachers found. Click 'Add Teacher' to get started!"));
    }

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
            showError("Failed to load teachers", e.getMessage());
        }
    }

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

    private void updateRecordCount() {
        int count = filteredTeachers.size();
        recordCountLabel.setText(count + " teacher" + (count != 1 ? "s" : ""));
    }

    private void handleAddTeacher() {
        CreateEditTeacherDialog dialog = new CreateEditTeacherDialog((Stage) this.dialog.getOwner(), null);

        if (dialog.showAndWait()) {
            Teacher newTeacher = dialog.getTeacher();
            if (teacherDAO.createTeacher(newTeacher)) {
                allTeachers.add(newTeacher);
                updateRecordCount();
                showSuccess("Teacher '" + newTeacher.getFullName() + "' added successfully!");
            } else {
                showError("Failed to add teacher", "Please try again.");
            }
        }
    }

    private void handleEditTeacher(Teacher teacher) {
        CreateEditTeacherDialog dialog = new CreateEditTeacherDialog((Stage) this.dialog.getOwner(), teacher);

        if (dialog.showAndWait()) {
            if (teacherDAO.updateTeacher(teacher)) {
                teachersTable.refresh();
                showSuccess("Teacher '" + teacher.getFullName() + "' updated successfully!");
            } else {
                showError("Failed to update teacher", "Please try again.");
            }
        }
    }

    private void handleDeleteSelected() {
        List<Teacher> selectedTeachers = filteredTeachers.stream()
                .filter(teacher -> selectedTeacherIds.contains(teacher.getId()))
                .collect(Collectors.toList());

        if (selectedTeachers.isEmpty()) {
            showWarning("No teachers selected", "Please select one or more teachers to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete selected teachers?");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedTeachers.size() +
                " teacher" + (selectedTeachers.size() > 1 ? "s" : "") + "? This action cannot be undone.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            List<Integer> teacherIds = selectedTeachers.stream()
                    .map(Teacher::getId)
                    .collect(Collectors.toList());

            if (teacherDAO.deleteTeachers(teacherIds)) {
                allTeachers.removeAll(selectedTeachers);
                selectedTeacherIds.removeAll(teacherIds);
                updateRecordCount();
                teachersTable.refresh();
                showSuccess("Successfully deleted " + selectedTeachers.size() + " teacher(s).");
            } else {
                showError("Delete Failed", "Failed to delete the selected teachers.");
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }
}