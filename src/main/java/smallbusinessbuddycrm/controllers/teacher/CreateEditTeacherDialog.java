package smallbusinessbuddycrm.controllers.teacher;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Teacher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A modal dialog for creating new teachers or editing existing teacher information.
 * This dialog provides a user-friendly interface with form validation and styling
 * consistent with the application's design theme.
 *
 * <p>The dialog supports two modes:</p>
 * <ul>
 *   <li><strong>Create Mode:</strong> When no existing teacher is provided, allows creation of a new teacher</li>
 *   <li><strong>Edit Mode:</strong> When an existing teacher is provided, allows editing of teacher details</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Create new teacher
 * CreateEditTeacherDialog dialog = new CreateEditTeacherDialog(parentStage, null);
 * if (dialog.showAndWait()) {
 *     Teacher newTeacher = dialog.getTeacher();
 *     // Save the new teacher
 * }
 *
 * // Edit existing teacher
 * CreateEditTeacherDialog editDialog = new CreateEditTeacherDialog(parentStage, existingTeacher);
 * if (editDialog.showAndWait()) {
 *     Teacher updatedTeacher = editDialog.getTeacher();
 *     // Update the teacher in database
 * }
 * }</pre>
 *
 * @author Small Business Buddy CRM Team
 * @version 1.0
 * @since 1.0
 */
public class CreateEditTeacherDialog {

    /** The modal dialog stage */
    private Stage dialog;

    /** Flag indicating whether the user confirmed the operation (true) or cancelled (false) */
    private boolean result = false;

    /** The teacher object being created or edited */
    private Teacher teacher;

    /** Flag indicating whether this dialog is in edit mode (true) or create mode (false) */
    private boolean isEdit;

    // Form fields
    /** Text field for teacher's first name (required) */
    private TextField firstNameField;

    /** Text field for teacher's last name (required) */
    private TextField lastNameField;

    /** Text field for teacher's email address (optional) */
    private TextField emailField;

    /** Text field for teacher's phone number (optional) */
    private TextField phoneField;

    /**
     * Constructs a new CreateEditTeacherDialog.
     *
     * @param parent the parent stage that owns this dialog
     * @param existingTeacher the teacher to edit, or null to create a new teacher
     *                       If null, the dialog will be in create mode.
     *                       If not null, the dialog will be in edit mode and fields will be pre-populated.
     */
    public CreateEditTeacherDialog(Stage parent, Teacher existingTeacher) {
        this.isEdit = existingTeacher != null;
        this.teacher = isEdit ? existingTeacher : new Teacher();
        createDialog(parent);

        if (isEdit) {
            populateFields();
        }
    }

    /**
     * Creates and configures the main dialog window with all UI components.
     * Sets up the modal dialog with appropriate title, styling, and layout.
     *
     * @param parent the parent stage for this modal dialog
     */
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEdit ? "Edit Teacher" : "Add New Teacher");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Label titleLabel = new Label(isEdit ? "Edit Teacher" : "Add New Teacher");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Form grid
        GridPane formGrid = createFormGrid();

        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(titleLabel, formGrid, buttonBox);

        Scene scene = new Scene(root, 420, 350);
        dialog.setScene(scene);
    }

    /**
     * Creates and configures the form grid containing all input fields.
     * The grid includes fields for first name, last name, email, and phone number
     * with appropriate labels, styling, and validation indicators.
     *
     * @return a configured GridPane containing all form fields
     */
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        // First Name
        Label firstNameLabel = new Label("First Name *");
        firstNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        firstNameField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        firstNameField.setPrefWidth(250);

        // Last Name
        Label lastNameLabel = new Label("Last Name *");
        lastNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        lastNameField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        lastNameField.setPrefWidth(250);

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        emailField.setPrefWidth(250);

        // Phone
        Label phoneLabel = new Label("Phone Number");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        phoneField.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        phoneField.setPrefWidth(250);

        // Add to grid
        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(phoneLabel, 0, 3);
        grid.add(phoneField, 1, 3);

        return grid;
    }

    /**
     * Creates and configures the button box containing Cancel and Save/Update buttons.
     * The buttons are styled consistently and positioned at the bottom right of the dialog.
     * The save button is set as the default button and responds to Enter key press.
     *
     * @return an HBox containing the configured Cancel and Save buttons
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4; -fx-padding: 8 16;");
        cancelButton.setOnAction(e -> {
            result = false;
            dialog.close();
        });

        Button saveButton = new Button(isEdit ? "Update Teacher" : "Add Teacher");
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4; -fx-padding: 8 16;");
        saveButton.setOnAction(e -> handleSave());

        // Make save button default
        saveButton.setDefaultButton(true);

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    /**
     * Populates the form fields with data from the existing teacher object.
     * This method is called only in edit mode to pre-fill the form with current teacher information.
     * Handles null values gracefully by setting empty strings for null fields.
     */
    private void populateFields() {
        if (teacher != null) {
            firstNameField.setText(teacher.getFirstName() != null ? teacher.getFirstName() : "");
            lastNameField.setText(teacher.getLastName() != null ? teacher.getLastName() : "");
            emailField.setText(teacher.getEmail() != null ? teacher.getEmail() : "");
            phoneField.setText(teacher.getPhoneNum() != null ? teacher.getPhoneNum() : "");
        }
    }

    /**
     * Handles the save button action by validating input and updating the teacher object.
     * Performs form validation, updates the teacher object with form data,
     * sets appropriate timestamps, and closes the dialog if successful.
     * If validation fails, the dialog remains open with error messages displayed.
     */
    private void handleSave() {
        // Validate required fields
        if (!validateFields()) {
            return;
        }

        // Update teacher object
        teacher.setFirstName(firstNameField.getText().trim());
        teacher.setLastName(lastNameField.getText().trim());
        teacher.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
        teacher.setPhoneNum(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());

        // Set timestamps
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        if (!isEdit) {
            teacher.setCreatedAt(timestamp);
        }
        teacher.setUpdatedAt(timestamp);

        result = true;
        dialog.close();
    }

    /**
     * Validates all form fields according to business rules.
     * Checks that required fields (first name and last name) are not empty,
     * validates email format if an email is provided, and provides visual feedback
     * by highlighting invalid fields in red.
     *
     * @return true if all validation passes, false if there are validation errors
     */
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("• First name is required\n");
            highlightField(firstNameField, true);
        } else {
            highlightField(firstNameField, false);
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("• Last name is required\n");
            highlightField(lastNameField, true);
        } else {
            highlightField(lastNameField, false);
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            errors.append("• Please enter a valid email address\n");
            highlightField(emailField, true);
        } else {
            highlightField(emailField, false);
        }

        // Show errors if any
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    /**
     * Applies visual highlighting to a text field to indicate validation status.
     * Changes the border color to red for errors or returns to normal styling for valid fields.
     *
     * @param field the TextField to highlight
     * @param isError true to highlight as error (red border), false to return to normal styling
     */
    private void highlightField(TextField field, boolean isError) {
        if (isError) {
            field.setStyle("-fx-border-color: #dc3545; -fx-border-radius: 4; -fx-padding: 8; -fx-border-width: 2;");
        } else {
            field.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 4; -fx-padding: 8;");
        }
    }

    /**
     * Performs basic email validation using simple pattern matching.
     * Checks that the email contains '@' and '.' characters and has a minimum length.
     *
     * <p><strong>Note:</strong> This is a simple validation and may not catch all invalid email formats.
     * For production use, consider using a more robust email validation library.</p>
     *
     * @param email the email string to validate
     * @return true if the email appears to be in a valid format, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Displays the dialog modally and waits for user interaction.
     * Blocks execution until the user either saves the data or cancels the dialog.
     *
     * @return true if the user clicked Save/Update and validation passed,
     *         false if the user cancelled or closed the dialog
     */
    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }

    /**
     * Returns the teacher object that was created or modified by this dialog.
     * The returned object contains the user's input data and appropriate timestamps.
     *
     * <p><strong>Important:</strong> This method should only be called after {@link #showAndWait()}
     * returns true, indicating that the user successfully saved the data.</p>
     *
     * @return the Teacher object containing the form data
     */
    public Teacher getTeacher() {
        return teacher;
    }
}