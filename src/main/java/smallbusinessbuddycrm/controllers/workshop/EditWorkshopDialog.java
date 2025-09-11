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

/**
 * Modal dialog class for editing existing workshops with comprehensive internationalization support.
 *
 * This dialog provides a complete workshop editing interface with advanced features:
 * - Modal dialog window with internationalized text and professional styling
 * - Form validation with real-time feedback and localized error messaging
 * - Teacher assignment management with change tracking and user feedback
 * - Date range validation ensuring logical from/to date relationships
 * - Database integration for workshop updates and teacher loading
 * - Complete internationalization with dynamic language switching
 * - Success/error feedback with detailed teacher assignment change notifications
 * - Responsive UI with conditional validation and real-time updates
 *
 * Key Features:
 * - Workshop Data Editing: Modify name, date range, and teacher assignments
 * - Internationalization: Full language support with real-time UI updates
 * - Teacher Management: Change teacher assignments with detailed change tracking
 * - Real-time Validation: Immediate feedback on form field changes with localized messages
 * - Change Tracking: Monitors teacher assignment changes for detailed user feedback
 * - Database Integration: Updates workshops via WorkshopDAO with comprehensive error handling
 * - Professional UI: Consistent styling and layout with other application dialogs
 *
 * Internationalization Features:
 * - Dynamic text updates for all UI elements when language changes
 * - Localized validation messages and error feedback
 * - Internationalized teacher combo box with "No Teacher" option
 * - Localized success/error dialogs with detailed change information
 * - Language-aware date and text formatting
 *
 * Validation System:
 * - Required field validation for name and dates with localized messages
 * - Date range logic validation (from date cannot be after to date)
 * - Real-time UI updates with localized error label display
 * - Pre-save validation with comprehensive error collection and reporting
 * - Visual feedback with color-coded validation states
 *
 * Teacher Assignment Management:
 * - Optional teacher selection via combo box with current teacher pre-selection
 * - Change tracking to detect teacher assignment modifications
 * - Detailed success messages indicating teacher assignment changes
 * - "No Teacher" option for workshops without assigned teachers
 * - Fallback handling if current teacher data cannot be loaded
 *
 * The dialog integrates with WorkshopDAO for database operations, TeacherDAO for
 * loading available teachers, and LanguageManager for internationalization,
 * providing a complete workshop editing solution.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class EditWorkshopDialog {

    // Dialog Management
    private Stage dialogStage;
    private Workshop workshop;
    private boolean okClicked = false;
    private LanguageManager languageManager;

    // Form Controls
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox;

    // Validation Error Labels
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    // UI Labels for Internationalization
    private Label titleLabel;
    private Label sectionTitleLabel;
    private Label nameLabel;
    private Label fromDateLabel;
    private Label toDateLabel;
    private Label teacherLabel;
    private Label teacherNoteLabel;
    private Button cancelButton;
    private Button updateButton;

    // Database Access Objects
    private TeacherDAO teacherDAO = new TeacherDAO();

    /**
     * Constructs a new EditWorkshopDialog for editing an existing workshop.
     * Initializes the dialog with workshop data, sets up internationalization,
     * creates the UI components, loads teacher data, and populates form fields.
     *
     * @param parentStage The parent stage that owns this dialog
     * @param workshop The Workshop object to edit
     */
    public EditWorkshopDialog(Stage parentStage, Workshop workshop) {
        this.workshop = workshop;
        this.languageManager = LanguageManager.getInstance();

        createDialogStage();
        dialogStage.initOwner(parentStage);
        loadTeachers();
        populateFields();

        // Set up internationalization support
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh dialog title, labels, buttons,
     * placeholders, validation messages, and teacher combo box converter.
     * Provides complete internationalization support for the dialog.
     */
    private void updateTexts() {
        // Update dialog window title
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

        // Update field labels with localized text
        if (nameLabel != null) nameLabel.setText(languageManager.getText("workshop.field.name"));
        if (fromDateLabel != null) fromDateLabel.setText(languageManager.getText("workshop.field.from.date"));
        if (toDateLabel != null) toDateLabel.setText(languageManager.getText("workshop.field.to.date"));
        if (teacherLabel != null) teacherLabel.setText(languageManager.getText("workshop.field.teacher"));

        // Update field placeholder text
        if (nameField != null) nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        if (teacherComboBox != null) teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Update teacher assignment note
        if (teacherNoteLabel != null) {
            teacherNoteLabel.setText(languageManager.getText("workshop.teacher.note"));
        }

        // Update action buttons
        if (cancelButton != null) cancelButton.setText(languageManager.getText("workshop.button.cancel"));
        if (updateButton != null) updateButton.setText(languageManager.getText("workshop.button.update"));

        // Update teacher combo box converter for localized display
        if (teacherComboBox != null) {
            updateTeacherComboBoxConverter();
        }
    }

    /**
     * Creates and configures the main dialog stage with internationalized content.
     * Sets up the modal dialog window, creates the layout structure with
     * title, form section, and buttons. Configures stage properties and scene.
     */
    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle(languageManager.getText("workshop.edit.dialog.title"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout structure
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Dialog title with styling
        titleLabel = new Label(languageManager.getText("workshop.edit.dialog.title"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form section
        VBox workshopSection = createWorkshopSection();

        // Action buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 500);
        dialogStage.setScene(scene);
    }

    /**
     * Creates the workshop information form section with input fields and validation.
     * Builds a grid layout containing workshop name, date range, and teacher selection
     * with localized labels, validation error displays, and real-time listeners.
     *
     * @return VBox containing the complete workshop form section
     */
    private VBox createWorkshopSection() {
        VBox section = new VBox(15);

        sectionTitleLabel = new Label(languageManager.getText("workshop.edit.section.title"));
        sectionTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Workshop Name Field
        nameLabel = new Label(languageManager.getText("workshop.field.name"));
        grid.add(nameLabel, 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        grid.add(nameField, 1, row++);

        // Name validation error label
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

        // From Date Field
        fromDateLabel = new Label(languageManager.getText("workshop.field.from.date"));
        grid.add(fromDateLabel, 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

        // From date validation error label
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

        // To Date Field
        toDateLabel = new Label(languageManager.getText("workshop.field.to.date"));
        grid.add(toDateLabel, 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

        // To date validation error label
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

        // Teacher Selection Field
        teacherLabel = new Label(languageManager.getText("workshop.field.teacher"));
        grid.add(teacherLabel, 0, row);
        teacherComboBox = new ComboBox<>();
        teacherComboBox.setPrefWidth(280);
        teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Set up teacher combo box converter (will be updated by updateTexts())
        updateTeacherComboBoxConverter();

        grid.add(teacherComboBox, 1, row++);

        // Teacher assignment note
        teacherNoteLabel = new Label(languageManager.getText("workshop.teacher.note"));
        teacherNoteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        grid.add(teacherNoteLabel, 1, row++);

        // Set up real-time validation listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
            validateForm();
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        section.getChildren().addAll(sectionTitleLabel, grid);
        return section;
    }

    /**
     * Updates the teacher combo box string converter with localized text.
     * Refreshes the display converter to show proper teacher names and
     * localized "No Teacher" option based on current language settings.
     */
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

    /**
     * Loads all available teachers from database for teacher selection.
     * Fetches teachers via TeacherDAO, adds localized "No Teacher" option,
     * and populates the teacher combo box. Handles loading errors gracefully
     * with fallback to "No Teacher" option only.
     */
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();

            // Create localized "No Teacher" option
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

    /**
     * Populates form fields with data from the workshop being edited.
     * Sets name, date range, and current teacher assignment in form fields.
     * Handles teacher assignment by finding and selecting the current teacher
     * or defaulting to "No Teacher" if none assigned or teacher not found.
     */
    private void populateFields() {
        if (workshop != null) {
            nameField.setText(workshop.getName() != null ? workshop.getName() : "");
            fromDatePicker.setValue(workshop.getFromDate());
            toDatePicker.setValue(workshop.getToDate());

            // Set current teacher selection
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
                        teacherComboBox.setValue(teacherComboBox.getItems().get(0));
                    }
                } catch (Exception e) {
                    System.err.println("Error loading current teacher: " + e.getMessage());
                    teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher"
                }
            } else {
                // No teacher assigned, select "No Teacher"
                teacherComboBox.setValue(teacherComboBox.getItems().get(0));
            }
        }
    }

    /**
     * Creates the button box containing Cancel and Update action buttons.
     * Configures localized button text, styling, event handlers, and layout
     * positioning for the dialog's primary actions.
     *
     * @return HBox containing the Cancel and Update buttons
     */
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

    /**
     * Validates the entire form and updates UI feedback accordingly.
     * Checks required fields, date range validity using localized error messages.
     * Updates error label visibility to provide immediate user feedback.
     *
     * @return true if all form fields are valid, false otherwise
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Clear all error messages
        nameErrorLabel.setVisible(false);
        fromDateErrorLabel.setVisible(false);
        toDateErrorLabel.setVisible(false);

        // Validate workshop name
        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText(languageManager.getText("workshop.validation.name.required"));
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate date fields
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

    /**
     * Handles the Update button click event and workshop update process.
     * Validates input, updates workshop object, saves to database via DAO,
     * and provides localized user feedback with teacher assignment change details.
     * Tracks teacher assignment changes for detailed success messaging.
     */
    private void handleUpdate() {
        if (validateInput()) {
            try {
                // Store original teacher for change tracking
                Integer originalTeacherId = workshop.getTeacherId();

                updateWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.updateWorkshop(workshop);

                if (success) {
                    okClicked = true;
                    dialogStage.close();

                    // Create success message with teacher change information
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

    /**
     * Performs comprehensive input validation before saving with localized messages.
     * Validates required fields and business rules, collects all validation
     * errors with localized text, and displays them in a warning dialog.
     *
     * @return true if all input is valid and ready for saving, false otherwise
     */
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

    /**
     * Updates the workshop object with current form input values.
     * Extracts data from all form fields, handles teacher assignment changes,
     * sets update timestamp, and prepares the workshop for database persistence.
     * Logs teacher assignment changes for debugging purposes.
     */
    private void updateWorkshopFromInput() {
        // Update the existing workshop object with form data
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

        // Handle teacher assignment changes
        Teacher selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            workshop.setTeacherId(selectedTeacher.getId());
            System.out.println("Teacher updated: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
        } else {
            workshop.setTeacherId(null);
            System.out.println("Teacher assignment removed");
        }

        // Update the modification timestamp
        workshop.setUpdatedAt(LocalDateTime.now().toString());
    }

    /**
     * Displays a localized error alert dialog with the specified message.
     * Shows a standardized error dialog with localized title, header, and content
     * for consistent error reporting throughout the dialog.
     *
     * @param message The error message to display to the user
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(languageManager.getText("workshop.alert.error.title"));
        alert.setHeaderText(languageManager.getText("workshop.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows the dialog and waits for user interaction.
     * Displays the modal dialog window and blocks until the user
     * either updates the workshop or cancels the operation.
     *
     * @return true if user clicked Update and workshop was saved, false if cancelled
     */
    public boolean showAndWait() {
        dialogStage.showAndWait();
        return okClicked;
    }
}