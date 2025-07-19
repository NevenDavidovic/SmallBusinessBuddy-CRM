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
import smallbusinessbuddycrm.database.WorkshopParticipantDAO;  // Changed from WorkshopDAO
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.model.Workshop;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkshopTeacherAssignmentDialog {

    private Stage dialog;
    private boolean result = false;
    private Workshop workshop;
    private TeacherDAO teacherDAO;
    private WorkshopParticipantDAO workshopParticipantDAO;  // Changed from workshopDAO

    private Teacher currentTeacher;
    private ObservableList<Teacher> allTeachers;
    private FilteredList<Teacher> filteredTeachers;
    private TableView<Teacher> teachersTable;
    private TextField searchField;
    private Label currentTeacherLabel;
    private Label availableCountLabel;

    public WorkshopTeacherAssignmentDialog(Stage parent, Workshop workshop) {
        this.workshop = workshop;
        this.teacherDAO = new TeacherDAO();
        this.workshopParticipantDAO = new WorkshopParticipantDAO();  // Changed from workshopDAO
        createDialog(parent);
        loadTeachers();
    }

    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Assign Teacher - " + workshop.getName());
        dialog.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        VBox headerBox = createHeader();

        // Current teacher section
        VBox currentTeacherSection = createCurrentTeacherSection();

        // Available teachers section
        VBox availableTeachersSection = createAvailableTeachersSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(headerBox, currentTeacherSection, availableTeachersSection, buttonBox);

        Scene scene = new Scene(root, 700, 600);
        dialog.setScene(scene);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(5);

        Label titleLabel = new Label("Teacher Assignment");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label workshopLabel = new Label("Workshop: " + workshop.getName());
        workshopLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        Label dateLabel = new Label("Date: " + workshop.getFormattedFromDate() + " - " + workshop.getFormattedToDate());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        headerBox.getChildren().addAll(titleLabel, workshopLabel, dateLabel);
        return headerBox;
    }

    private VBox createCurrentTeacherSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label titleLabel = new Label("Currently Assigned Teacher");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        currentTeacherLabel = new Label("No teacher assigned");
        currentTeacherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-padding: 10;");

        Button removeTeacherButton = new Button("Remove Current Teacher");
        removeTeacherButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        removeTeacherButton.setOnAction(e -> handleRemoveCurrentTeacher());
        removeTeacherButton.setDisable(true);

        // Store reference to button for enabling/disabling
        currentTeacherLabel.setUserData(removeTeacherButton);

        section.getChildren().addAll(titleLabel, currentTeacherLabel, removeTeacherButton);
        return section;
    }

    private VBox createAvailableTeachersSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Available Teachers");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        availableCountLabel = new Label("(0 available)");
        availableCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        headerBox.getChildren().addAll(titleLabel, availableCountLabel);

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search by name, email, or phone...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, newValue) -> updateFilter());

        Button addNewButton = new Button("Add New Teacher");
        addNewButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-border-radius: 4;");
        addNewButton.setOnAction(e -> handleAddNewTeacher());

        searchBox.getChildren().addAll(searchLabel, searchField, addNewButton);

        // Teachers table
        teachersTable = createTeachersTable();

        section.getChildren().addAll(headerBox, searchBox, teachersTable);
        return section;
    }

    private TableView<Teacher> createTeachersTable() {
        TableView<Teacher> table = new TableView<>();
        table.setPrefHeight(250);
        table.setStyle("-fx-background-color: white;");

        // Name column
        TableColumn<Teacher, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        // Email column
        TableColumn<Teacher, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setPrefWidth(180);
        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail() != null ?
                        cellData.getValue().getEmail() : ""));

        // Phone column
        TableColumn<Teacher, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setPrefWidth(120);
        phoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhoneNum() != null ?
                        cellData.getValue().getPhoneNum() : ""));

        // Assign action column
        TableColumn<Teacher, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(100);
        actionColumn.setCellFactory(tc -> new TableCell<Teacher, Void>() {
            private final Button assignButton = new Button("Assign");

            {
                assignButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
                assignButton.setOnAction(event -> {
                    Teacher teacher = getTableView().getItems().get(getIndex());
                    handleAssignTeacher(teacher);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(assignButton);
                }
            }
        });

        table.getColumns().addAll(nameColumn, emailColumn, phoneColumn, actionColumn);
        table.setPlaceholder(new Label("No teachers available"));

        return table;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button manageAllButton = new Button("Manage All Teachers");
        manageAllButton.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-border-radius: 4;");
        manageAllButton.setOnAction(e -> handleManageAllTeachers());

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4;");
        closeButton.setOnAction(e -> {
            result = true;
            dialog.close();
        });

        buttonBox.getChildren().addAll(manageAllButton, closeButton);
        return buttonBox;
    }

    private void loadTeachers() {
        try {
            // Load current teacher for this workshop
            List<Teacher> assignedTeachers = teacherDAO.getTeachersForWorkshop(workshop.getId());
            if (!assignedTeachers.isEmpty()) {
                currentTeacher = assignedTeachers.get(0); // Get the first (and only) teacher
                currentTeacherLabel.setText(currentTeacher.getFullName() +
                        (currentTeacher.getEmail() != null ? " (" + currentTeacher.getEmail() + ")" : ""));
                currentTeacherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-padding: 10; -fx-font-weight: bold;");

                // Enable remove button
                Button removeButton = (Button) currentTeacherLabel.getUserData();
                if (removeButton != null) {
                    removeButton.setDisable(false);
                }
            } else {
                currentTeacher = null;
                currentTeacherLabel.setText("No teacher assigned");
                currentTeacherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-padding: 10;");

                // Disable remove button
                Button removeButton = (Button) currentTeacherLabel.getUserData();
                if (removeButton != null) {
                    removeButton.setDisable(true);
                }
            }

            // Load all teachers and filter out the currently assigned one
            List<Teacher> allTeachersList = teacherDAO.getAllTeachers();
            if (currentTeacher != null) {
                allTeachersList.removeIf(teacher -> teacher.getId() == currentTeacher.getId());
            }

            allTeachers = FXCollections.observableArrayList(allTeachersList);
            filteredTeachers = new FilteredList<>(allTeachers, p -> true);
            teachersTable.setItems(filteredTeachers);
            updateAvailableCount();

        } catch (Exception e) {
            showError("Loading Error", "Failed to load teachers: " + e.getMessage());
            e.printStackTrace();
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

        updateAvailableCount();
    }

    private void updateAvailableCount() {
        availableCountLabel.setText("(" + filteredTeachers.size() + " available)");
    }

    private void handleAssignTeacher(Teacher teacher) {
        // First check if there are any participants
        try {
            List<Map<String, Object>> participants = workshopParticipantDAO.getWorkshopParticipantsWithDetails(workshop.getId());

            if (participants.isEmpty()) {
                Alert warning = new Alert(Alert.AlertType.INFORMATION);
                warning.setTitle("Add Participants First");
                warning.setHeaderText("This workshop needs participants before you can assign a teacher");
                warning.setContentText("Please add participants to this workshop first, then come back to assign a teacher.\n\n" +
                        "You can add participants using the 'Add Participants' tab in the workshop management view.");

                // Add buttons to help user
                ButtonType addParticipantsButton = new ButtonType("Add Participants First");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                warning.getButtonTypes().setAll(addParticipantsButton, cancelButton);

                Optional<ButtonType> result = warning.showAndWait();
                if (result.isPresent() && result.get() == addParticipantsButton) {
                    // Close this dialog and let user add participants
                    dialog.close();
                }
                return;
            }

            // Proceed with normal teacher assignment if participants exist
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Assign Teacher");
            confirmation.setHeaderText("Assign " + teacher.getFullName() + " to this workshop?");

            String message = "This will assign " + teacher.getFullName() + " as the teacher for " + participants.size() + " participant" + (participants.size() == 1 ? "" : "s") + ".";
            if (currentTeacher != null) {
                message += "\n\nNote: " + currentTeacher.getFullName() + " will be replaced as the current teacher.";
            }
            confirmation.setContentText(message);

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    // Assign teacher to all participants in this workshop
                    if (workshopParticipantDAO.addTeacherToWorkshop(workshop.getId(), teacher.getId())) {
                        showSuccess("Teacher " + teacher.getFullName() + " assigned to " + participants.size() +
                                " participant" + (participants.size() == 1 ? "" : "s") + " successfully!");
                        loadTeachers(); // Refresh the display
                    } else {
                        showError("Assignment Failed", "Failed to assign teacher to the workshop participants.");
                    }
                } catch (Exception e) {
                    showError("Error", "An error occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            showError("Error", "Could not check participants: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleRemoveCurrentTeacher() {
        if (currentTeacher == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Teacher");
        confirmation.setHeaderText("Remove " + currentTeacher.getFullName() + " from this workshop?");
        confirmation.setContentText("This will remove the current teacher assignment. The workshop will have no assigned teacher.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (workshopParticipantDAO.removeTeacherFromWorkshop(workshop.getId(), currentTeacher.getId())) {
                    showSuccess("Teacher removed from workshop successfully!");
                    loadTeachers(); // Refresh the display
                } else {
                    showError("Removal Failed", "Failed to remove teacher from the workshop.");
                }
            } catch (Exception e) {
                showError("Error", "An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleAddNewTeacher() {
        CreateEditTeacherDialog dialog = new CreateEditTeacherDialog((Stage) this.dialog.getOwner(), null);

        if (dialog.showAndWait()) {
            Teacher newTeacher = dialog.getTeacher();
            if (teacherDAO.createTeacher(newTeacher)) {
                allTeachers.add(newTeacher);
                updateAvailableCount();
                showSuccess("Teacher '" + newTeacher.getFullName() + "' added successfully!");
            } else {
                showError("Failed", "Failed to add teacher. Please try again.");
            }
        }
    }

    private void handleManageAllTeachers() {
        TeacherManagementDialog managementDialog = new TeacherManagementDialog((Stage) dialog.getOwner());

        if (managementDialog.showAndWait()) {
            // Refresh the teacher lists after management
            loadTeachers();
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

    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }
}