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

public class CreateWorkshopDialog {

    private Stage dialogStage;
    private Workshop result = null;
    private boolean okClicked = false;

    // Workshop form fields - matching your current Workshop model
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox; // NEW: Teacher selection
    private Button saveButton; // Make save button a class field so we can control it

    // Error labels for validation
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    // DAOs
    private TeacherDAO teacherDAO = new TeacherDAO();

    public CreateWorkshopDialog(Stage parentStage) {
        System.out.println("CreateWorkshopDialog constructor called"); // DEBUG
        createDialogStage();
        dialogStage.initOwner(parentStage);
    }

    private void createDialogStage() {
        System.out.println("Creating dialog stage..."); // DEBUG

        dialogStage = new Stage();
        dialogStage.setTitle("Create Workshop");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Create New Workshop");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form
        VBox workshopSection = createWorkshopSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        System.out.println("Button box created with children: " + buttonBox.getChildren().size()); // DEBUG

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 500); // Made taller for teacher field
        dialogStage.setScene(scene);

        // Load teachers and initial validation
        loadTeachers();
        validateForm();

        System.out.println("Dialog stage created successfully"); // DEBUG
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
        fromDatePicker.setValue(LocalDate.now());
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
        toDatePicker.setValue(LocalDate.now());
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
        Label teacherNote = new Label("You can assign or change the teacher later");
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

    private HBox createButtonBox() {
        System.out.println("Creating button box..."); // DEBUG

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        System.out.println("Cancel button created"); // DEBUG

        saveButton = new Button("Save"); // Create as class field
        saveButton.setPrefWidth(80);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        System.out.println("Save button created"); // DEBUG

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        System.out.println("Buttons added to box. Total children: " + buttonBox.getChildren().size()); // DEBUG

        return buttonBox;
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

            // Set "No Teacher" as default selection
            teacherComboBox.setValue(noTeacher);

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
            teacherComboBox.setValue(noTeacher);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Clear all error messages
        nameErrorLabel.setVisible(false);
        fromDateErrorLabel.setVisible(false);
        toDateErrorLabel.setVisible(false);

        // Validate name
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            if (nameErrorLabel != null) {
                nameErrorLabel.setText("Workshop name is required");
                nameErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        // Validate dates
        LocalDate fromDate = fromDatePicker != null ? fromDatePicker.getValue() : null;
        LocalDate toDate = toDatePicker != null ? toDatePicker.getValue() : null;

        if (fromDate == null) {
            if (fromDateErrorLabel != null) {
                fromDateErrorLabel.setText("From date is required");
                fromDateErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        if (toDate == null) {
            if (toDateErrorLabel != null) {
                toDateErrorLabel.setText("To date is required");
                toDateErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            if (toDateErrorLabel != null) {
                toDateErrorLabel.setText("To date must be after from date");
                toDateErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        // Enable/disable save button based on validation
        if (saveButton != null) {
            saveButton.setDisable(!isValid);
            if (isValid) {
                saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
                System.out.println("Save button ENABLED (orange)"); // DEBUG
            } else {
                saveButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
                System.out.println("Save button DISABLED (gray)"); // DEBUG
            }
        } else {
            System.out.println("WARNING: saveButton is null!"); // DEBUG
        }

        return isValid;
    }

    private void handleSave() {
        System.out.println("Save button clicked!"); // DEBUG

        if (validateInput()) {
            try {
                Workshop newWorkshop = createWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.createWorkshop(newWorkshop);

                if (success) {
                    result = newWorkshop;
                    okClicked = true;
                    dialogStage.close();

                    // Show success message with teacher info
                    String successMessage = "Workshop '" + newWorkshop.getName() + "' has been created successfully!";
                    if (newWorkshop.hasTeacher()) {
                        Teacher selectedTeacher = teacherComboBox.getValue();
                        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
                            successMessage += "\n\nTeacher assigned: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName();
                        }
                    } else {
                        successMessage += "\n\nNo teacher assigned. You can assign a teacher later from the workshops view.";
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Workshop Created");
                    successAlert.setContentText(successMessage);
                    successAlert.showAndWait();
                } else {
                    showErrorAlert("Failed to save workshop to database.");
                }

            } catch (Exception e) {
                showErrorAlert("Error saving workshop: " + e.getMessage());
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

    private Workshop createWorkshopFromInput() {
        Workshop workshop = new Workshop();

        // Set the basic fields that match your current model
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

        // NEW: Set teacher if selected
        Teacher selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            workshop.setTeacherId(selectedTeacher.getId());
            System.out.println("Teacher assigned: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
        } else {
            workshop.setTeacherId(null);
            System.out.println("No teacher assigned");
        }

        // Set timestamps using LocalDateTime.now().toString() format like your Contact dialog
        String now = LocalDateTime.now().toString();
        workshop.setCreatedAt(now);
        workshop.setUpdatedAt(now);

        return workshop;
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        System.out.println("Showing dialog..."); // DEBUG
        dialogStage.showAndWait();
        System.out.println("Dialog closed. okClicked: " + okClicked); // DEBUG
        return okClicked;
    }

    public Workshop getResult() {
        return result;
    }
}