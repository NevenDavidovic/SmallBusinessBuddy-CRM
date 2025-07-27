package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.model.Workshop;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EditWorkshopDialog {

    private Stage dialogStage;
    private Workshop workshop;
    private boolean okClicked = false;

    // Workshop form fields - matching your current Workshop model
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox; // NEW: Teacher selection

    // Error labels for validation
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    // DAOs
    private TeacherDAO teacherDAO = new TeacherDAO();

    public EditWorkshopDialog(Stage parentStage, Workshop workshop) {
        this.workshop = workshop;
        createDialogStage();
        dialogStage.initOwner(parentStage);
        loadTeachers();
        populateFields();
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle("Edit Workshop");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Edit Workshop");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form
        VBox workshopSection = createWorkshopSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 500); // Made bigger for teacher field
        dialogStage.setScene(scene);
    }

    private VBox createWorkshopSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Workshop Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Workshop Name
        grid.add(new Label("Workshop Name *:"), 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText("Enter workshop name...");
        grid.add(nameField, 1, row++);

        // Name error label
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

        // From Date
        grid.add(new Label("From Date *:"), 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

        // From date error label
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

        // To Date
        grid.add(new Label("To Date *:"), 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

        // To date error label
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

        // NEW: Teacher Selection
        grid.add(new Label("Teacher:"), 0, row);
        teacherComboBox = new ComboBox<>();
        teacherComboBox.setPrefWidth(280);
        teacherComboBox.setPromptText("Select a teacher (optional)");

        // Custom string converter for teacher display
        teacherComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                if (teacher == null) {
                    return "No Teacher";
                }
                if (teacher.getId() == -1) {
                    return "No Teacher";
                }
                return teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        grid.add(teacherComboBox, 1, row++);

        // Add a note about teacher assignment
        Label teacherNote = new Label("You can also manage teachers from the main workshops view");
        teacherNote.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        grid.add(teacherNote, 1, row++);

        // Add date validation listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
            validateForm();
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Real-time validation for name
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    // NEW: Load teachers for selection
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();

            // Add "No Teacher" option
            Teacher noTeacher = new Teacher();
            noTeacher.setId(-1);
            noTeacher.setFirstName("No");
            noTeacher.setLastName("Teacher");

            teacherComboBox.getItems().clear();
            teacherComboBox.getItems().add(noTeacher);
            teacherComboBox.getItems().addAll(teachers);

            System.out.println("Loaded " + teachers.size() + " teachers for selection");
        } catch (Exception e) {
            System.err.println("Error loading teachers: " + e.getMessage());
            e.printStackTrace();

            // Add just the "No Teacher" option if loading fails
            Teacher noTeacher = new Teacher();
            noTeacher.setId(-1);
            noTeacher.setFirstName("No");
            noTeacher.setLastName("Teacher");
            teacherComboBox.getItems().add(noTeacher);
        }
    }

    private void populateFields() {
        if (workshop != null) {
            nameField.setText(workshop.getName() != null ? workshop.getName() : "");
            fromDatePicker.setValue(workshop.getFromDate());
            toDatePicker.setValue(workshop.getToDate());

            // NEW: Set current teacher selection
            if (workshop.hasTeacher()) {
                try {
                    Teacher currentTeacher = teacherDAO.getTeacherById(workshop.getTeacherId());
                    if (currentTeacher != null) {
                        // Find and select the current teacher in the combo box
                        for (Teacher teacher : teacherComboBox.getItems()) {
                            if (teacher.getId() == currentTeacher.getId()) {
                                teacherComboBox.setValue(teacher);
                                break;
                            }
                        }
                    } else {
                        // Teacher not found, select "No Teacher"
                        teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher" is first
                    }
                } catch (Exception e) {
                    System.err.println("Error loading current teacher: " + e.getMessage());
                    teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher"
                }
            } else {
                // No teacher assigned, select "No Teacher"
                teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher" is first
            }
        }
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        Button updateButton = new Button("Update");
        updateButton.setPrefWidth(80);
        updateButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        updateButton.setOnAction(e -> handleUpdate());

        buttonBox.getChildren().addAll(cancelButton, updateButton);
        return buttonBox;
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Clear all error messages
        nameErrorLabel.setVisible(false);
        fromDateErrorLabel.setVisible(false);
        toDateErrorLabel.setVisible(false);

        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Workshop name is required");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate dates
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            fromDateErrorLabel.setText("From date is required");
            fromDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (toDate == null) {
            toDateErrorLabel.setText("To date is required");
            toDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            toDateErrorLabel.setText("To date must be after from date");
            toDateErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    private void handleUpdate() {
        if (validateInput()) {
            try {
                // Store original teacher for comparison
                Integer originalTeacherId = workshop.getTeacherId();

                updateWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.updateWorkshop(workshop);

                if (success) {
                    okClicked = true;
                    dialogStage.close();

                    // Show success message with teacher change info
                    String successMessage = "Workshop '" + workshop.getName() + "' has been updated successfully!";

                    // Check if teacher assignment changed
                    Teacher selectedTeacher = teacherComboBox.getValue();
                    if (selectedTeacher != null) {
                        if (selectedTeacher.getId() == -1) {
                            // No teacher selected
                            if (originalTeacherId != null) {
                                successMessage += "\n\nTeacher assignment removed.";
                            }
                        } else {
                            // Teacher selected
                            if (originalTeacherId == null) {
                                successMessage += "\n\nTeacher assigned: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName();
                            } else if (!originalTeacherId.equals(selectedTeacher.getId())) {
                                successMessage += "\n\nTeacher changed to: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName();
                            }
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Workshop Updated");
                    successAlert.setContentText(successMessage);
                    successAlert.showAndWait();
                } else {
                    showErrorAlert("Failed to update workshop in database.");
                }

            } catch (Exception e) {
                showErrorAlert("Error updating workshop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Workshop name is required\n");
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            errors.append("- From date is required\n");
        }

        if (toDate == null) {
            errors.append("- To date is required\n");
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            errors.append("- To date must be after from date\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void updateWorkshopFromInput() {
        // Update the existing workshop object with form data
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

        // NEW: Update teacher assignment
        Teacher selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            workshop.setTeacherId(selectedTeacher.getId());
            System.out.println("Teacher updated: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
        } else {
            workshop.setTeacherId(null);
            System.out.println("Teacher assignment removed");
        }

        // Update the timestamp
        workshop.setUpdatedAt(LocalDateTime.now().toString());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialogStage.showAndWait();
        return okClicked;
    }
}