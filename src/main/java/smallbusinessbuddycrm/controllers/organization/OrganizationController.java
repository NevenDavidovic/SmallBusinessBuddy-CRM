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

    private void setupImageView() {
        // Set default styling for image view
        organizationImageView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
    }

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

    private void createDefaultOrganization() {
        currentOrganization = new Organization(
                languageManager.getText("organization.default.name"), "");
        setEditMode(true);
        showAlert(Alert.AlertType.INFORMATION,
                languageManager.getText("organization.welcome.title"),
                languageManager.getText("organization.welcome.content"));
    }

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

    @FXML
    private void handleEdit() {
        setEditMode(true);
    }

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

    @FXML
    private void handleCancel() {
        newImageData = null;
        setEditMode(false);
        displayOrganization();
    }

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}