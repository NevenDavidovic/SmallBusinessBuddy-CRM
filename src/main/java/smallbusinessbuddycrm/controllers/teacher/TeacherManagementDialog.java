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

public class TeacherManagementDialog {

    private Stage dialog;
    private boolean result = false;
    private TeacherDAO teacherDAO;
    private ObservableList<Teacher> allTeachers;
    private FilteredList<Teacher> filteredTeachers;
    private TableView<Teacher> teachersTable;
    private TextField searchField;
    private Label recordCountLabel;
    private LanguageManager languageManager;

    // Selection tracking (since Teacher model might not have isSelected field)
    private Set<Integer> selectedTeacherIds = new HashSet<>();

    // UI Labels that need to be updated on language change
    private Label titleLabel;
    private Button deleteSelectedButton;
    private Button addTeacherButton;
    private Button closeButton;
    private TableColumn<Teacher, Void> editColumn;
    private TableColumn<Teacher, String> firstNameColumn;
    private TableColumn<Teacher, String> lastNameColumn;
    private TableColumn<Teacher, String> emailColumn;
    private TableColumn<Teacher, String> phoneColumn;
    private TableColumn<Teacher, String> createdColumn;

    public TeacherManagementDialog(Stage parent) {
        this.teacherDAO = new TeacherDAO();
        this.languageManager = LanguageManager.getInstance();

        createDialog(parent);
        loadTeachers();

        // Add language change listener
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(languageManager.getText("teacher.management.alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(languageManager.getText("teacher.management.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(languageManager.getText("teacher.management.alert.warning.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }
}