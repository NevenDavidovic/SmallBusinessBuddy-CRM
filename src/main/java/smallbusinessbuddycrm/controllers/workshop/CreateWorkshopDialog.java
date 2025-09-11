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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modal dialog class for creating new workshops with comprehensive form validation.
 *
 * This dialog provides a complete workshop creation interface with the following features:
 * - Modal dialog window with professional styling and layout
 * - Form validation with real-time feedback and error messaging
 * - Teacher assignment with optional selection from available teachers
 * - Date range validation ensuring logical from/to date relationships
 * - Database integration for workshop creation and teacher loading
 * - Success/error feedback with detailed user messages
 * - Responsive UI with conditional button enabling based on validation
 *
 * Key Components:
 * - Workshop Information Form: Name, date range, and teacher selection
 * - Real-time Validation: Immediate feedback on form field changes
 * - Teacher Integration: Loads available teachers with "No Teacher" option
 * - Date Logic: Automatically adjusts to date when from date changes
 * - Error Handling: Comprehensive error management with user-friendly messages
 * - Database Operations: Creates workshops via WorkshopDAO with proper error handling
 *
 * Validation Features:
 * - Required field validation for name and dates
 * - Date range logic validation (from date cannot be after to date)
 * - Real-time UI updates with error label display
 * - Conditional save button enabling based on validation state
 * - Visual feedback with color-coded button states
 *
 * Teacher Assignment:
 * - Optional teacher selection via combo box
 * - "No Teacher" option for workshops without assigned teachers
 * - Custom string converter for proper teacher display
 * - Fallback handling if teacher loading fails
 *
 * The dialog integrates with WorkshopDAO for database operations and TeacherDAO
 * for loading available teachers, providing a complete workshop creation solution.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class CreateWorkshopDialog {

    // Dialog Management
    private Stage dialogStage;
    private Workshop result = null;
    private boolean okClicked = false;

    // Form Controls
    private TextField nameField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<Teacher> teacherComboBox;
    private Button saveButton;

    // Validation Error Labels
    private Label nameErrorLabel;
    private Label fromDateErrorLabel;
    private Label toDateErrorLabel;

    // Database Access Objects
    private TeacherDAO teacherDAO = new TeacherDAO();

    /**
     * Constructs a new CreateWorkshopDialog with the specified parent stage.
     * Initializes the dialog stage, sets up the UI components, and configures
     * the modal dialog window with proper owner relationship.
     *
     * @param parentStage The parent stage that owns this dialog
     */
    public CreateWorkshopDialog(Stage parentStage) {
        System.out.println("CreateWorkshopDialog constructor called");
        createDialogStage();
        dialogStage.initOwner(parentStage);
    }

    /**
     * Creates and configures the main dialog stage with all UI components.
     * Sets up the modal dialog window, creates the layout structure,
     * loads teacher data, and initializes form validation.
     */
    private void createDialogStage() {
        System.out.println("Creating dialog stage...");

        dialogStage = new Stage();
        dialogStage.setTitle("Create Workshop");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        // Create the main layout structure
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // Dialog title
        Label titleLabel = new Label("Create New Workshop");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");

        // Workshop form section
        VBox workshopSection = createWorkshopSection();

        // Action buttons
        HBox buttonBox = createButtonBox();

        System.out.println("Button box created with children: " + buttonBox.getChildren().size());

        mainLayout.getChildren().addAll(titleLabel, workshopSection, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 500);
        dialogStage.setScene(scene);

        // Initialize data and validation
        loadTeachers();
        validateForm();

        System.out.println("Dialog stage created successfully");
    }

    /**
     * Creates the workshop information form section with input fields.
     * Builds a grid layout containing workshop name, date range, and teacher selection
     * with appropriate labels, validation error displays, and real-time listeners.
     *
     * @return VBox containing the complete workshop form section
     */
    private VBox createWorkshopSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Workshop Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Workshop Name Field
        grid.add(new Label("Workshop Name *:"), 0, row);
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPromptText("Enter workshop name...");
        grid.add(nameField, 1, row++);

        // Name validation error label
        nameErrorLabel = new Label();
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        nameErrorLabel.setVisible(false);
        grid.add(nameErrorLabel, 1, row++);

        // From Date Field
        grid.add(new Label("From Date *:"), 0, row);
        fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now());
        fromDatePicker.setPrefWidth(130);
        grid.add(fromDatePicker, 1, row++);

        // From date validation error label
        fromDateErrorLabel = new Label();
        fromDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        fromDateErrorLabel.setVisible(false);
        grid.add(fromDateErrorLabel, 1, row++);

        // To Date Field
        grid.add(new Label("To Date *:"), 0, row);
        toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        toDatePicker.setPrefWidth(130);
        grid.add(toDatePicker, 1, row++);

        // To date validation error label
        toDateErrorLabel = new Label();
        toDateErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px;");
        toDateErrorLabel.setVisible(false);
        grid.add(toDateErrorLabel, 1, row++);

        // Teacher Selection Field
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

        // Teacher assignment note
        Label teacherNote = new Label("You can assign or change the teacher later");
        teacherNote.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        grid.add(teacherNote, 1, row++);

        // Set up real-time validation listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
            validateForm();
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    /**
     * Creates the button box containing Cancel and Save action buttons.
     * Configures button styling, event handlers, and layout positioning
     * for the dialog's primary actions.
     *
     * @return HBox containing the Cancel and Save buttons
     */
    private HBox createButtonBox() {
        System.out.println("Creating button box...");

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> dialogStage.close());

        System.out.println("Cancel button created");

        saveButton = new Button("Save");
        saveButton.setPrefWidth(80);
        saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());

        System.out.println("Save button created");

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        System.out.println("Buttons added to box. Total children: " + buttonBox.getChildren().size());

        return buttonBox;
    }

    /**
     * Loads all available teachers from database for teacher selection.
     * Fetches teachers via TeacherDAO, adds "No Teacher" option for optional selection,
     * and populates the teacher combo box with appropriate string converter.
     * Handles loading errors gracefully with fallback options.
     */
    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.getAllTeachers();

            // Create "No Teacher" option for optional selection
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

    /**
     * Validates the entire form and updates UI feedback accordingly.
     * Checks required fields, date range validity, and enables/disables
     * the save button based on validation results. Updates error labels
     * and button styling to provide immediate user feedback.
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
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            if (nameErrorLabel != null) {
                nameErrorLabel.setText("Workshop name is required");
                nameErrorLabel.setVisible(true);
            }
            isValid = false;
        }

        // Validate date fields
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

        // Update save button state based on validation
        if (saveButton != null) {
            saveButton.setDisable(!isValid);
            if (isValid) {
                saveButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");
                System.out.println("Save button ENABLED (orange)");
            } else {
                saveButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
                System.out.println("Save button DISABLED (gray)");
            }
        } else {
            System.out.println("WARNING: saveButton is null!");
        }

        return isValid;
    }

    /**
     * Handles the Save button click event and workshop creation process.
     * Validates input, creates workshop object, saves to database via DAO,
     * and provides user feedback with success or error messages including
     * teacher assignment information.
     */
    private void handleSave() {
        System.out.println("Save button clicked!");

        if (validateInput()) {
            try {
                Workshop newWorkshop = createWorkshopFromInput();
                WorkshopDAO workshopDAO = new WorkshopDAO();

                boolean success = workshopDAO.createWorkshop(newWorkshop);

                if (success) {
                    result = newWorkshop;
                    okClicked = true;
                    dialogStage.close();

                    // Show success message with teacher information
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

    /**
     * Performs comprehensive input validation before saving.
     * Validates required fields and business rules, collects all validation
     * errors, and displays them in a warning dialog if any are found.
     *
     * @return true if all input is valid and ready for saving, false otherwise
     */
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

    /**
     * Creates a Workshop object from the current form input values.
     * Extracts data from all form fields, handles teacher assignment,
     * sets creation and update timestamps, and returns a complete
     * Workshop object ready for database persistence.
     *
     * @return Workshop object populated with form data
     */
    private Workshop createWorkshopFromInput() {
        Workshop workshop = new Workshop();

        // Set basic workshop information
        workshop.setName(nameField.getText().trim());
        workshop.setFromDate(fromDatePicker.getValue());
        workshop.setToDate(toDatePicker.getValue());

        // Handle teacher assignment
        Teacher selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            workshop.setTeacherId(selectedTeacher.getId());
            System.out.println("Teacher assigned: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
        } else {
            workshop.setTeacherId(null);
            System.out.println("No teacher assigned");
        }

        // Set creation and update timestamps
        String now = LocalDateTime.now().toString();
        workshop.setCreatedAt(now);
        workshop.setUpdatedAt(now);

        return workshop;
    }

    /**
     * Displays an error alert dialog with the specified message.
     * Shows a standardized error dialog with title, header, and content
     * for consistent error reporting throughout the dialog.
     *
     * @param message The error message to display to the user
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows the dialog and waits for user interaction.
     * Displays the modal dialog window and blocks until the user
     * either saves the workshop or cancels the operation.
     *
     * @return true if user clicked Save and workshop was created, false if cancelled
     */
    public boolean showAndWait() {
        System.out.println("Showing dialog...");
        dialogStage.showAndWait();
        System.out.println("Dialog closed. okClicked: " + okClicked);
        return okClicked;
    }

    /**
     * Returns the workshop that was created by this dialog.
     * Provides access to the Workshop object after successful creation,
     * or null if no workshop was created.
     *
     * @return The created Workshop object, or null if cancelled or failed
     */
    public Workshop getResult() {
        return result;
    }
}