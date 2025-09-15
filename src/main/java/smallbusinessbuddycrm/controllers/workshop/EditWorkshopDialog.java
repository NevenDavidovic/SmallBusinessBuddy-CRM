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

<<<<<<< HEAD
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
=======
public class EditWorkshopDialog {

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private Stage dialogStage;
    private Workshop workshop;
    private boolean okClicked = false;
    private LanguageManager languageManager;

<<<<<<< HEAD
    // Form Controls
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox;

    // Validation Error Labels
=======
    // Workshop form fields - matching your current Workshop model
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox; // NEW: Teacher selection

    // Error labels for validation
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

<<<<<<< HEAD
    // UI Labels for Internationalization
=======
    // UI Labels that need to be updated on language change
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private Label titleLabel;
    private Label sectionTitleLabel;
    private Label nameLabel;
    private Label fromDateLabel;
    private Label toDateLabel;
    private Label teacherLabel;
    private Label teacherNoteLabel;
    private Button cancelButton;
    private Button updateButton;

<<<<<<< HEAD
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
=======
    // DAOs
    private TeacherDAO teacherDAO = new TeacherDAO();

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    public EditWorkshopDialog(Stage parentStage, Workshop workshop) {
        this.workshop = workshop;
        this.languageManager = LanguageManager.getInstance();

        createDialogStage();
        dialogStage.initOwner(parentStage);
        loadTeachers();
        populateFields();

<<<<<<< HEAD
        // Set up internationalization support
=======
        // Add language change listener
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

<<<<<<< HEAD
    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh dialog title, labels, buttons,
     * placeholders, validation messages, and teacher combo box converter.
     * Provides complete internationalization support for the dialog.
     */
    private void updateTexts() {
        // Update dialog window title
=======
    private void updateTexts() {
        // Update dialog title
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
        // Update field labels with localized text
=======
        // Update field labels
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (nameLabel != null) nameLabel.setText(languageManager.getText("workshop.field.name"));
        if (fromDateLabel != null) fromDateLabel.setText(languageManager.getText("workshop.field.from.date"));
        if (toDateLabel != null) toDateLabel.setText(languageManager.getText("workshop.field.to.date"));
        if (teacherLabel != null) teacherLabel.setText(languageManager.getText("workshop.field.teacher"));

<<<<<<< HEAD
        // Update field placeholder text
        if (nameField != null) nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        if (teacherComboBox != null) teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Update teacher assignment note
=======
        // Update field prompts
        if (nameField != null) nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        if (teacherComboBox != null) teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

        // Update teacher note
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (teacherNoteLabel != null) {
            teacherNoteLabel.setText(languageManager.getText("workshop.teacher.note"));
        }

<<<<<<< HEAD
        // Update action buttons
        if (cancelButton != null) cancelButton.setText(languageManager.getText("workshop.button.cancel"));
        if (updateButton != null) updateButton.setText(languageManager.getText("workshop.button.update"));

        // Update teacher combo box converter for localized display
=======
        // Update buttons
        if (cancelButton != null) cancelButton.setText(languageManager.getText("workshop.button.cancel"));
        if (updateButton != null) updateButton.setText(languageManager.getText("workshop.button.update"));

        // Update teacher combobox display
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (teacherComboBox != null) {
            updateTeacherComboBoxConverter();
        }
    }

<<<<<<< HEAD
    /**
     * Creates and configures the main dialog stage with internationalized content.
     * Sets up the modal dialog window, creates the layout structure with
     * title, form section, and buttons. Configures stage properties and scene.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.setTitle(languageManager.getText("workshop.edit.dialog.title"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

<<<<<<< HEAD
        // Create the main layout structure
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Dialog title with styling
        titleLabel = new Label(languageManager.getText("workshop.edit.dialog.title"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form section
        VBox workshopSection = createWorkshopSection();

        // Action buttons
=======
        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Title
        titleLabel = new Label(languageManager.getText("workshop.edit.dialog.title"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form
        VBox workshopSection = createWorkshopSection();

        // Buttons
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

<<<<<<< HEAD
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
=======
        Scene scene = new Scene(mainLayout, 500, 500); // Made bigger for teacher field
        dialogStage.setScene(scene);
    }

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private VBox createWorkshopSection() {
        VBox section = new VBox(15);

        sectionTitleLabel = new Label(languageManager.getText("workshop.edit.section.title"));
        sectionTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

<<<<<<< HEAD
        // Workshop Name Field
=======
        // Workshop Name
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        nameLabel = new Label(languageManager.getText("workshop.field.name"));
        grid.add(nameLabel, 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText(languageManager.getText("workshop.prompt.name"));
        grid.add(nameField, 1, row++);

<<<<<<< HEAD
        // Name validation error label
=======
        // Name error label
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

<<<<<<< HEAD
        // From Date Field
=======
        // From Date
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        fromDateLabel = new Label(languageManager.getText("workshop.field.from.date"));
        grid.add(fromDateLabel, 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

<<<<<<< HEAD
        // From date validation error label
=======
        // From date error label
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

<<<<<<< HEAD
        // To Date Field
=======
        // To Date
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        toDateLabel = new Label(languageManager.getText("workshop.field.to.date"));
        grid.add(toDateLabel, 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

<<<<<<< HEAD
        // To date validation error label
=======
        // To date error label
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

<<<<<<< HEAD
        // Teacher Selection Field
=======
        // NEW: Teacher Selection
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        teacherLabel = new Label(languageManager.getText("workshop.field.teacher"));
        grid.add(teacherLabel, 0, row);
        teacherComboBox = new ComboBox<>();
        teacherComboBox.setPrefWidth(280);
        teacherComboBox.setPromptText(languageManager.getText("workshop.prompt.teacher"));

<<<<<<< HEAD
        // Set up teacher combo box converter (will be updated by updateTexts())
=======
        // Custom string converter for teacher display - will be updated by updateTexts()
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        updateTeacherComboBoxConverter();

        grid.add(teacherComboBox, 1, row++);

<<<<<<< HEAD
        // Teacher assignment note
=======
        // Add a note about teacher assignment
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        teacherNoteLabel = new Label(languageManager.getText("workshop.teacher.note"));
        teacherNoteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        grid.add(teacherNoteLabel, 1, row++);

<<<<<<< HEAD
        // Set up real-time validation listeners
=======
        // Add date validation listeners
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
            validateForm();
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
<<<<<<< HEAD
=======

        // Real-time validation for name
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        section.getChildren().addAll(sectionTitleLabel, grid);
        return section;
    }

<<<<<<< HEAD
    /**
     * Updates the teacher combo box string converter with localized text.
     * Refreshes the display converter to show proper teacher names and
     * localized "No Teacher" option based on current language settings.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Loads all available teachers from database for teacher selection.
     * Fetches teachers via TeacherDAO, adds localized "No Teacher" option,
     * and populates the teacher combo box. Handles loading errors gracefully
     * with fallback to "No Teacher" option only.
     */
=======
    // NEW: Load teachers for selection
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();

<<<<<<< HEAD
            // Create localized "No Teacher" option
=======
            // Add "No Teacher" option
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Populates form fields with data from the workshop being edited.
     * Sets name, date range, and current teacher assignment in form fields.
     * Handles teacher assignment by finding and selecting the current teacher
     * or defaulting to "No Teacher" if none assigned or teacher not found.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void populateFields() {
        if (workshop != null) {
            nameField.setText(workshop.getName() != null ? workshop.getName() : "");
            fromDatePicker.setValue(workshop.getFromDate());
            toDatePicker.setValue(workshop.getToDate());

<<<<<<< HEAD
            // Set current teacher selection
=======
            // NEW: Set current teacher selection
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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
<<<<<<< HEAD
                        teacherComboBox.setValue(teacherComboBox.getItems().get(0));
=======
                        teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher" is first
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
                    }
                } catch (Exception e) {
                    System.err.println("Error loading current teacher: " + e.getMessage());
                    teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher"
                }
            } else {
                // No teacher assigned, select "No Teacher"
<<<<<<< HEAD
                teacherComboBox.setValue(teacherComboBox.getItems().get(0));
=======
                teacherComboBox.setValue(teacherComboBox.getItems().get(0)); // "No Teacher" is first
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            }
        }
    }

<<<<<<< HEAD
    /**
     * Creates the button box containing Cancel and Update action buttons.
     * Configures localized button text, styling, event handlers, and layout
     * positioning for the dialog's primary actions.
     *
     * @return HBox containing the Cancel and Update buttons
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Validates the entire form and updates UI feedback accordingly.
     * Checks required fields, date range validity using localized error messages.
     * Updates error label visibility to provide immediate user feedback.
     *
     * @return true if all form fields are valid, false otherwise
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private boolean validateForm() {
        boolean isValid = true;

        // Clear all error messages
        nameErrorLabel.setVisible(false);
        fromDateErrorLabel.setVisible(false);
        toDateErrorLabel.setVisible(false);

<<<<<<< HEAD
        // Validate workshop name
=======
        // Validate name
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText(languageManager.getText("workshop.validation.name.required"));
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

<<<<<<< HEAD
        // Validate date fields
=======
        // Validate dates
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
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
=======
    private void handleUpdate() {
        if (validateInput()) {
            try {
                // Store original teacher for comparison
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
                Integer originalTeacherId = workshop.getTeacherId();

                updateWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.updateWorkshop(workshop);

                if (success) {
                    okClicked = true;
                    dialogStage.close();

<<<<<<< HEAD
                    // Create success message with teacher change information
=======
                    // Show success message with teacher change info
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Performs comprehensive input validation before saving with localized messages.
     * Validates required fields and business rules, collects all validation
     * errors with localized text, and displays them in a warning dialog.
     *
     * @return true if all input is valid and ready for saving, false otherwise
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Updates the workshop object with current form input values.
     * Extracts data from all form fields, handles teacher assignment changes,
     * sets update timestamp, and prepares the workshop for database persistence.
     * Logs teacher assignment changes for debugging purposes.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void updateWorkshopFromInput() {
        // Update the existing workshop object with form data
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

<<<<<<< HEAD
        // Handle teacher assignment changes
=======
        // NEW: Update teacher assignment
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        Teacher selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            workshop.setTeacherId(selectedTeacher.getId());
            System.out.println("Teacher updated: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
        } else {
            workshop.setTeacherId(null);
            System.out.println("Teacher assignment removed");
        }

<<<<<<< HEAD
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
=======
        // Update the timestamp
        workshop.setUpdatedAt(LocalDateTime.now().toString());
    }

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(languageManager.getText("workshop.alert.error.title"));
        alert.setHeaderText(languageManager.getText("workshop.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

<<<<<<< HEAD
    /**
     * Shows the dialog and waits for user interaction.
     * Displays the modal dialog window and blocks until the user
     * either updates the workshop or cancels the operation.
     *
     * @return true if user clicked Update and workshop was saved, false if cancelled
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    public boolean showAndWait() {
        dialogStage.showAndWait();
        return okClicked;
    }
}