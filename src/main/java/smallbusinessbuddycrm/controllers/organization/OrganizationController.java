package smallbusinessbuddycrm.controllers.organization;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.*;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Organization Profile interface providing comprehensive organization management.
 * Features organization information editing, image upload and management, form validation,
 * and complete localization support. Handles both creation of new organizations and
 * updating existing ones with proper database persistence and user feedback.
 */
public class OrganizationController implements Initializable {

    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button changeImageButton;
    @FXML private Button removeImageButton;

    @FXML private ImageView organizationImageView;
    @FXML private Label imageStatusLabel;

    // Title labels for translation
    @FXML private Label organizationProfileTitle;
    @FXML private Label organizationSubtitle;
    @FXML private Label organizationImageTitle;
    @FXML private Label basicInfoTitle;
    @FXML private Label addressTitle;
    @FXML private Label recordInfoTitle;
    @FXML private Label recordInfoSubtitle;

    // Field labels for translation
    @FXML private Label nameFieldLabel;
    @FXML private Label ibanFieldLabel;
    @FXML private Label emailFieldLabel;
    @FXML private Label phoneFieldLabel;
    @FXML private Label streetFieldLabel;
    @FXML private Label postalCodeFieldLabel;
    @FXML private Label cityFieldLabel;
    @FXML private Label createdFieldLabel;
    @FXML private Label updatedFieldLabel;

    // Display labels
    @FXML private Label nameLabel;
    @FXML private Label ibanLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label streetNameLabel;
    @FXML private Label streetNumLabel;
    @FXML private Label postalCodeLabel;
    @FXML private Label cityLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;

    // Edit fields
    @FXML private TextField nameField;
    @FXML private TextField ibanField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField streetNameField;
    @FXML private TextField streetNumField;
    @FXML private TextField postalCodeField;
    @FXML private TextField cityField;

    private OrganizationDAO organizationDAO;
    private Organization currentOrganization;
    private boolean isEditMode = false;
    private byte[] newImageData = null;
    private LanguageManager languageManager;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Initializes the OrganizationController after FXML loading.
     * Sets up DAO connections, initializes image view, loads organization data,
     * and configures language management with change listeners for dynamic updates.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageManager = LanguageManager.getInstance();
        organizationDAO = new OrganizationDAO();
        setupImageView();
        loadOrganization();

        // Add language change listener
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements based on current language settings.
     * Refreshes buttons, title labels, field labels, and image status text
     * when language changes between English and Croatian. Ensures complete
     * interface localization across all organization management components.
     */
    private void updateTexts() {
        // Update button texts
        if (editButton != null) editButton.setText(languageManager.getText("organization.button.edit"));
        if (saveButton != null) saveButton.setText(languageManager.getText("organization.button.save"));
        if (cancelButton != null) cancelButton.setText(languageManager.getText("organization.button.cancel"));
        if (changeImageButton != null) changeImageButton.setText(languageManager.getText("organization.button.change.image"));
        if (removeImageButton != null) removeImageButton.setText(languageManager.getText("organization.button.remove.image"));

        // Update title labels
        if (organizationProfileTitle != null) organizationProfileTitle.setText(languageManager.getText("organization.title.profile"));
        if (organizationSubtitle != null) organizationSubtitle.setText(languageManager.getText("organization.subtitle.manage"));
        if (organizationImageTitle != null) organizationImageTitle.setText(languageManager.getText("organization.title.image"));
        if (basicInfoTitle != null) basicInfoTitle.setText(languageManager.getText("organization.title.basic.info"));
        if (addressTitle != null) addressTitle.setText(languageManager.getText("organization.title.address"));
        if (recordInfoTitle != null) recordInfoTitle.setText(languageManager.getText("organization.title.record.info"));
        if (recordInfoSubtitle != null) recordInfoSubtitle.setText(languageManager.getText("organization.subtitle.timestamps"));

        // Update field labels
        if (nameFieldLabel != null) nameFieldLabel.setText(languageManager.getText("organization.field.name"));
        if (ibanFieldLabel != null) ibanFieldLabel.setText(languageManager.getText("organization.field.iban"));
        if (emailFieldLabel != null) emailFieldLabel.setText(languageManager.getText("organization.field.email"));
        if (phoneFieldLabel != null) phoneFieldLabel.setText(languageManager.getText("organization.field.phone"));
        if (streetFieldLabel != null) streetFieldLabel.setText(languageManager.getText("organization.field.street"));
        if (postalCodeFieldLabel != null) postalCodeFieldLabel.setText(languageManager.getText("organization.field.postal.code"));
        if (cityFieldLabel != null) cityFieldLabel.setText(languageManager.getText("organization.field.city"));
        if (createdFieldLabel != null) createdFieldLabel.setText(languageManager.getText("organization.field.created"));
        if (updatedFieldLabel != null) updatedFieldLabel.setText(languageManager.getText("organization.field.updated"));

        // Update image status if needed
        updateImageStatusText();
    }

    /**
     * Updates image status label text based on current state and language.
     * Checks current image status text and updates it with appropriate localized
     * message for new image selection, image removal, errors, or no image states.
     */
    private void updateImageStatusText() {
        if (imageStatusLabel != null && imageStatusLabel.isVisible()) {
            String currentText = imageStatusLabel.getText();

            // Update based on current state
            if (currentText.contains("Nova slika") || currentText.contains("New image")) {
                imageStatusLabel.setText(languageManager.getText("organization.image.new.selected"));
            } else if (currentText.contains("Slika uklonjena") || currentText.contains("Image removed")) {
                imageStatusLabel.setText(languageManager.getText("organization.image.removed"));
            } else if (currentText.contains("Greška") || currentText.contains("Error")) {
                imageStatusLabel.setText(languageManager.getText("organization.image.error"));
            } else if (currentText.contains("Nema slike") || currentText.contains("No image")) {
                imageStatusLabel.setText(languageManager.getText("organization.image.no.image"));
            }
        }
    }

    /**
     * Sets up initial styling for the organization image view.
     * Applies default background color and border styling to provide
     * visual feedback when no image is present.
     */
    private void setupImageView() {
        // Set default styling for image view
        organizationImageView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
    }

    /**
     * Loads organization data from database or creates default organization.
     * Retrieves the first organization record from database and displays it,
     * or creates a new default organization if none exists and enters edit mode.
     */
    private void loadOrganization() {
        Optional<Organization> orgOptional = organizationDAO.getFirst();

        if (orgOptional.isPresent()) {
            currentOrganization = orgOptional.get();
            displayOrganization();
        } else {
            // Create default organization or show setup dialog
            createDefaultOrganization();
        }
    }

    /**
     * Creates a new default organization for first-time setup.
     * Initializes organization with default name, enters edit mode automatically,
     * and shows welcome dialog to guide user through initial setup process.
     */
    private void createDefaultOrganization() {
        currentOrganization = new Organization(
                languageManager.getText("organization.default.name"), "");
        setEditMode(true);
        showAlert(Alert.AlertType.INFORMATION,
                languageManager.getText("organization.welcome.title"),
                languageManager.getText("organization.welcome.content"));
    }

    /**
     * Displays current organization data in the UI labels.
     * Updates all display labels with organization information including
     * basic info, address details, timestamp metadata, and organization image.
     * Handles null values gracefully by displaying empty strings.
     */
    private void displayOrganization() {
        if (currentOrganization == null) return;

        // Display basic info
        nameLabel.setText(currentOrganization.getName() != null ? currentOrganization.getName() : "");
        ibanLabel.setText(currentOrganization.getIban() != null ? currentOrganization.getIban() : "");
        emailLabel.setText(currentOrganization.getEmail() != null ? currentOrganization.getEmail() : "");
        phoneLabel.setText(currentOrganization.getPhoneNum() != null ? currentOrganization.getPhoneNum() : "");

        // Display address
        streetNameLabel.setText(currentOrganization.getStreetName() != null ? currentOrganization.getStreetName() : "");
        streetNumLabel.setText(currentOrganization.getStreetNum() != null ? currentOrganization.getStreetNum() : "");
        postalCodeLabel.setText(currentOrganization.getPostalCode() != null ? currentOrganization.getPostalCode() : "");
        cityLabel.setText(currentOrganization.getCity() != null ? currentOrganization.getCity() : "");

        // Display metadata
        if (currentOrganization.getCreatedAt() != null) {
            createdAtLabel.setText(currentOrganization.getCreatedAt().format(DISPLAY_FORMATTER));
        }
        if (currentOrganization.getUpdatedAt() != null) {
            updatedAtLabel.setText(currentOrganization.getUpdatedAt().format(DISPLAY_FORMATTER));
        }

        // Display image
        displayImage();
    }

    /**
     * Displays organization image in the image view or shows appropriate status.
     * Loads image from byte array data, handles loading errors gracefully,
     * and displays appropriate status messages for missing or invalid images.
     */
    private void displayImage() {
        if (currentOrganization.getImage() != null && currentOrganization.getImage().length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(currentOrganization.getImage());
                Image image = new Image(bis);
                organizationImageView.setImage(image);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                organizationImageView.setImage(null);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setText(languageManager.getText("organization.image.error"));
                    imageStatusLabel.setVisible(true);
                }
            }
        } else {
            organizationImageView.setImage(null);
            if (imageStatusLabel != null) {
                imageStatusLabel.setText(languageManager.getText("organization.image.no.image"));
                imageStatusLabel.setVisible(true);
            }
        }
    }

    /**
     * Handles the edit button action to enter edit mode.
     * Enables editing interface by switching to edit mode which shows
     * text fields instead of labels and displays edit action buttons.
     */
    @FXML
    private void handleEdit() {
        setEditMode(true);
    }

    /**
     * Handles the save button action to persist organization changes.
     * Validates input data, updates organization object from form fields,
     * saves to database, handles image updates, and provides user feedback.
     * Returns to display mode on successful save or shows error messages.
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        updateOrganizationFromFields();

        boolean success;
        if (currentOrganization.getId() == 0) {
            success = organizationDAO.save(currentOrganization);
        } else {
            success = organizationDAO.update(currentOrganization);
        }

        if (success) {
            if (newImageData != null) {
                if (newImageData.length == 0) {
                    // Empty array means remove image
                    currentOrganization.setImage(null);
                } else {
                    // New image data
                    currentOrganization.setImage(newImageData);
                }
                newImageData = null;
            }

            setEditMode(false);
            displayOrganization();
            showAlert(Alert.AlertType.INFORMATION,
                    languageManager.getText("organization.save.success.title"),
                    languageManager.getText("organization.save.success.content"));
        } else {
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("organization.save.error.title"),
                    languageManager.getText("organization.save.error.content"));
        }
    }

    /**
     * Handles the cancel button action to discard changes.
     * Resets any pending image changes, exits edit mode, and restores
     * the display to show current saved organization data.
     */
    @FXML
    private void handleCancel() {
        newImageData = null;
        setEditMode(false);
        displayOrganization();
    }

    /**
     * Handles organization image selection and upload.
     * Shows file chooser dialog with image format filters, validates file size,
     * converts selected image to byte array, displays preview, and updates
     * status label with appropriate feedback messages.
     */
    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(languageManager.getText("organization.image.select.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        languageManager.getText("organization.image.filter.all"),
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter(
                        languageManager.getText("organization.image.filter.png"), "*.png"),
                new FileChooser.ExtensionFilter(
                        languageManager.getText("organization.image.filter.jpg"), "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(changeImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Validate file size (max 5MB)
                long fileSizeInMB = selectedFile.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("organization.image.size.error.title"),
                            languageManager.getText("organization.image.size.error.content"));
                    return;
                }

                newImageData = convertFileToByteArray(selectedFile);

                // Display preview using JavaFX Image
                Image previewImage = new Image(selectedFile.toURI().toString());
                organizationImageView.setImage(previewImage);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setText(languageManager.getText("organization.image.new.selected"));
                    imageStatusLabel.setVisible(true);
                }

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR,
                        languageManager.getText("organization.image.load.error.title"),
                        languageManager.getText("organization.image.load.error.content") + ": " + e.getMessage());
            }
        }
    }

    /**
     * Handles organization image removal with confirmation.
     * Shows confirmation dialog, marks image for removal if confirmed,
     * clears the image view, and updates status label to indicate removal.
     */
    @FXML
    private void handleRemoveImage() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(languageManager.getText("organization.image.remove.confirm.title"));
        confirmation.setHeaderText(languageManager.getText("organization.image.remove.confirm.header"));
        confirmation.setContentText(languageManager.getText("organization.image.remove.confirm.content"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            newImageData = new byte[0]; // Empty array to indicate removal
            organizationImageView.setImage(null);
            if (imageStatusLabel != null) {
                imageStatusLabel.setText(languageManager.getText("organization.image.removed"));
                imageStatusLabel.setVisible(true);
            }
        }
    }

    /**
     * Toggles between edit mode and display mode for the organization interface.
     * Controls visibility of edit buttons vs action buttons, switches between
     * display labels and input fields, and populates edit fields when entering
     * edit mode. Manages remove image button visibility based on current image.
     *
     * @param editMode true to enter edit mode, false to enter display mode
     */
    private void setEditMode(boolean editMode) {
        this.isEditMode = editMode;

        // Toggle visibility of buttons
        editButton.setVisible(!editMode);
        saveButton.setVisible(editMode);
        cancelButton.setVisible(editMode);
        changeImageButton.setVisible(editMode);
        removeImageButton.setVisible(editMode && currentOrganization.getImage() != null && currentOrganization.getImage().length > 0);

        // Toggle visibility of labels vs fields
        nameLabel.setVisible(!editMode);
        nameField.setVisible(editMode);

        ibanLabel.setVisible(!editMode);
        ibanField.setVisible(editMode);

        emailLabel.setVisible(!editMode);
        emailField.setVisible(editMode);

        phoneLabel.setVisible(!editMode);
        phoneField.setVisible(editMode);

        streetNameLabel.setVisible(!editMode);
        streetNameField.setVisible(editMode);

        streetNumLabel.setVisible(!editMode);
        streetNumField.setVisible(editMode);

        postalCodeLabel.setVisible(!editMode);
        postalCodeField.setVisible(editMode);

        cityLabel.setVisible(!editMode);
        cityField.setVisible(editMode);

        if (editMode) {
            // Populate fields with current values
            populateEditFields();
        }
    }

    /**
     * Populates edit form fields with current organization data.
     * Fills all text fields with existing organization information,
     * handling null values by setting empty strings to prevent
     * null pointer exceptions during editing.
     */
    private void populateEditFields() {
        if (currentOrganization == null) return;

        nameField.setText(currentOrganization.getName() != null ? currentOrganization.getName() : "");
        ibanField.setText(currentOrganization.getIban() != null ? currentOrganization.getIban() : "");
        emailField.setText(currentOrganization.getEmail() != null ? currentOrganization.getEmail() : "");
        phoneField.setText(currentOrganization.getPhoneNum() != null ? currentOrganization.getPhoneNum() : "");
        streetNameField.setText(currentOrganization.getStreetName() != null ? currentOrganization.getStreetName() : "");
        streetNumField.setText(currentOrganization.getStreetNum() != null ? currentOrganization.getStreetNum() : "");
        postalCodeField.setText(currentOrganization.getPostalCode() != null ? currentOrganization.getPostalCode() : "");
        cityField.setText(currentOrganization.getCity() != null ? currentOrganization.getCity() : "");
    }

    /**
     * Updates organization object with data from edit form fields.
     * Transfers all form field values to the organization object,
     * handles empty fields by setting null values, and processes
     * any pending image changes for save operation.
     */
    private void updateOrganizationFromFields() {
        if (currentOrganization == null) return;

        currentOrganization.setName(nameField.getText().trim());
        currentOrganization.setIban(ibanField.getText().trim());
        currentOrganization.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
        currentOrganization.setPhoneNum(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
        currentOrganization.setStreetName(streetNameField.getText().trim().isEmpty() ? null : streetNameField.getText().trim());
        currentOrganization.setStreetNum(streetNumField.getText().trim().isEmpty() ? null : streetNumField.getText().trim());
        currentOrganization.setPostalCode(postalCodeField.getText().trim().isEmpty() ? null : postalCodeField.getText().trim());
        currentOrganization.setCity(cityField.getText().trim().isEmpty() ? null : cityField.getText().trim());

        if (newImageData != null) {
            if (newImageData.length == 0) {
                // Empty array means remove image
                currentOrganization.setImage(null);
            } else {
                // New image data
                currentOrganization.setImage(newImageData);
            }
        }
    }

    /**
     * Validates organization form input data before saving.
     * Checks required fields (name and IBAN), validates email format if provided,
     * and shows validation error dialog with specific field requirements.
     *
     * @return true if all validation passes, false if validation errors exist
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("organization.validation.name.required")).append("\n");
        }

        if (ibanField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("organization.validation.iban.required")).append("\n");
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append(languageManager.getText("organization.validation.email.invalid")).append("\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("organization.validation.error.title"),
                    errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Converts selected image file to byte array for database storage.
     * Reads file using FileInputStream and converts to byte array using
     * ByteArrayOutputStream with buffered reading for memory efficiency.
     *
     * @param file The image file to convert to byte array
     * @return byte array containing the image data
     * @throws IOException if file reading operations fail
     */
    private byte[] convertFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Displays alert dialog with specified type, title, and message.
     * Creates and shows modal alert dialog with no header text for clean appearance.
     * Used throughout the controller for user feedback and error notifications.
     *
     * @param type The type of alert (INFORMATION, ERROR, WARNING, CONFIRMATION)
     * @param title The title text for the alert dialog
     * @param message The main content message to display to the user
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}