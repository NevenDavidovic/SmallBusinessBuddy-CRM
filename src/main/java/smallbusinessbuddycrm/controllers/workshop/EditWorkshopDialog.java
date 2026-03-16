package smallbusinessbuddycrm.controllers.workshop;

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
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EditWorkshopDialog {

    private Stage dialogStage;
    private Workshop workshop;
    private boolean okClicked = false;
    private LanguageManager languageManager;

    // Workshop form fields - matching your current Workshop model
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox; // NEW: Teacher selection

    // Error labels for validation
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    // UI Labels that need to be updated on language change
    private Label titleLabel;
    private Label sectionTitleLabel;
    private Label nameLabel;
    private Label fromDateLabel;
    private Label toDateLabel;
    private Label teacherLabel;
    private Label teacherNoteLabel;
    private Button cancelButton;
    private Button updateButton;

    // DAOs
    private TeacherDAO teacherDAO = new TeacherDAO();

    public EditWorkshopDialog(Stage parentStage, Workshop workshop) {
        this.workshop = workshop;
        this.languageManager = LanguageManager.getInstance();

        createDialogStage();
        dialogStage.initOwner(parentStage);
        loadTeachers();
        populateFields();

        // Add language change listener
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    private void updateTexts() {
        // Update dialog title
        if (dialogStage != null) {
            dialogStage.setTitle(languageManager.getText("workshop.edit.dialog.title"));
        }

        // Update main title label
        if (titleLabel != null) {
            titleLabel.setText(languageManager.getText("workshop.edit.dialog.title"));
        }

        // Update section title
        if (sectionTitleLabel != null) {
            sectionTitleLabel.setText(languageManager.getText("workshop.edit.section.title"));
        }

        // Update field labels
        if (nameLabel != null) nameLabel.setText(languageManager.getText("workshop.field.name"));
        if (fromDateLabel != null) fromDateLabel.setText(languageManager.getText("workshop.field.from.date"));
        if (toDateLabel != null) toDateLabel.setText(languageManager.getText("workshop.field.to.date"));
        if (teacherLabel != null) teacherLabel.setText(languageManager.getText("workshop.field.teacher"));

        // Update field prompts
        if (nameField != null) nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        if (teacherComboBox != null) teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Update teacher note
        if (teacherNoteLabel != null) {
            teacherNoteLabel.setText(languageManager.getText("workshop.teacher.note"));
        }

        // Update buttons
        if (cancelButton != null) cancelButton.setText(languageManager.getText("workshop.button.cancel"));
        if (updateButton != null) updateButton.setText(languageManager.getText("workshop.button.update"));

        // Update teacher combobox display
        if (teacherComboBox != null) {
            updateTeacherComboBoxConverter();
        }
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle(languageManager.getText("workshop.edit.dialog.title"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Title
        titleLabel = new Label(languageManager.getText("workshop.edit.dialog.title"));
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

        sectionTitleLabel = new Label(languageManager.getText("workshop.edit.section.title"));
        sectionTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Workshop Name
        nameLabel = new Label(languageManager.getText("workshop.field.name"));
        grid.add(nameLabel, 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        grid.add(nameField, 1, row++);

        // Name error label
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

        // From Date
        fromDateLabel = new Label(languageManager.getText("workshop.field.from.date"));
        grid.add(fromDateLabel, 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

        // From date error label
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

        // To Date
        toDateLabel = new Label(languageManager.getText("workshop.field.to.date"));
        grid.add(toDateLabel, 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

        // To date error label
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

        // NEW: Teacher Selection
        teacherLabel = new Label(languageManager.getText("workshop.field.teacher"));
        grid.add(teacherLabel, 0, row);
        teacherComboBox = new ComboBox<>();
        teacherComboBox.setPrefWidth(280);
        teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Custom string converter for teacher display - will be updated by updateTexts()
        updateTeacherComboBoxConverter();

        grid.add(teacherComboBox, 1, row++);

        // Add a note about teacher assignment
        teacherNoteLabel = new Label(languageManager.getText("workshop.teacher.note"));
        teacherNoteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        grid.add(teacherNoteLabel, 1, row++);

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

        section.getChildren().addAll(sectionTitleLabel, grid);
        return section;
    }

    private void updateTeacherComboBoxConverter() {
        if (teacherComboBox != null) {
            teacherComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
                @Override
                public String toString(Teacher teacher) {
                    if (teacher == null) {
                        return languageManager.getText("workshop.teacher.none");
                    }
                    if (teacher.getId() == -1) {
                        return languageManager.getText("workshop.teacher.none");
                    }
                    return teacher.getFirstName() + " " + teacher.getLastName();
                }

                @Override
                public Teacher fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
        }
    }

    // NEW: Load teachers for selection
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();

            // Add "No Teacher" option
            Teacher noTeacher = new Teacher();
            noTeacher.setId(-1);
            noTeacher.setFirstName(languageManager.getText("workshop.teacher.none.first"));
            noTeacher.setLastName(languageManager.getText("workshop.teacher.none.last"));

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
            noTeacher.setFirstName(languageManager.getText("workshop.teacher.none.first"));
            noTeacher.setLastName(languageManager.getText("workshop.teacher.none.last"));
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

        cancelButton = new Button(languageManager.getText("workshop.button.cancel"));
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        updateButton = new Button(languageManager.getText("workshop.button.update"));
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
            nameErrorLabel.setText(languageManager.getText("workshop.validation.name.required"));
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate dates
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            fromDateErrorLabel.setText(languageManager.getText("workshop.validation.from.date.required"));
            fromDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (toDate == null) {
            toDateErrorLabel.setText(languageManager.getText("workshop.validation.to.date.required"));
            toDateErrorLabel.setVisible(true);
            isValid = false;
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            toDateErrorLabel.setText(languageManager.getText("workshop.validation.date.order"));
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
                    String successMessage = languageManager.getText("workshop.success.updated")
                            .replace("{0}", workshop.getName());

                    // Check if teacher assignment changed
                    Teacher selectedTeacher = teacherComboBox.getValue();
                    if (selectedTeacher != null) {
                        if (selectedTeacher.getId() == -1) {
                            // No teacher selected
                            if (originalTeacherId != null) {
                                successMessage += "\n\n" + languageManager.getText("workshop.success.teacher.removed");
                            }
                        } else {
                            // Teacher selected
                            if (originalTeacherId == null) {
                                successMessage += "\n\n" + languageManager.getText("workshop.success.teacher.assigned")
                                        .replace("{0}", selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
                            } else if (!originalTeacherId.equals(selectedTeacher.getId())) {
                                successMessage += "\n\n" + languageManager.getText("workshop.success.teacher.changed")
                                        .replace("{0}", selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
                            }
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("workshop.alert.success.title"));
                    successAlert.setHeaderText(languageManager.getText("workshop.alert.success.header"));
                    successAlert.setContentText(successMessage);
                    successAlert.showAndWait();
                } else {
                    showErrorAlert(languageManager.getText("workshop.error.update.failed"));
                }

            } catch (Exception e) {
                showErrorAlert(languageManager.getText("workshop.error.update.exception")
                        .replace("{0}", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- ").append(languageManager.getText("workshop.validation.name.required")).append("\n");
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null) {
            errors.append("- ").append(languageManager.getText("workshop.validation.from.date.required")).append("\n");
        }

        if (toDate == null) {
            errors.append("- ").append(languageManager.getText("workshop.validation.to.date.required")).append("\n");
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            errors.append("- ").append(languageManager.getText("workshop.validation.date.order")).append("\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageManager.getText("workshop.alert.validation.title"));
            alert.setHeaderText(languageManager.getText("workshop.alert.validation.header"));
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
        alert.setTitle(languageManager.getText("workshop.alert.error.title"));
        alert.setHeaderText(languageManager.getText("workshop.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialogStage.showAndWait();
        return okClicked;
    }
}