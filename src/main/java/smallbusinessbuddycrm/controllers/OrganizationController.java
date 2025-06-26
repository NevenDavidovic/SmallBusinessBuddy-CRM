package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;

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

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        organizationDAO = new OrganizationDAO();
        setupImageView();
        loadOrganization();
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
        currentOrganization = new Organization("Nova organizacija", "");
        setEditMode(true);
        showAlert(Alert.AlertType.INFORMATION, "Dobrodošli",
                "Molimo unesite podatke o vašoj organizaciji.");
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
                System.err.println("Greška pri učitavanju slike: " + e.getMessage());
                organizationImageView.setImage(null);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setText("Greška pri učitavanju slike");
                    imageStatusLabel.setVisible(true);
                }
            }
        } else {
            organizationImageView.setImage(null);
            if (imageStatusLabel != null) {
                imageStatusLabel.setText("Nema slike");
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
            showAlert(Alert.AlertType.INFORMATION, "Uspjeh", "Organizacija je uspješno spremljena.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Greška", "Došlo je do greške pri spremanju organizacije.");
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
        fileChooser.setTitle("Odaberite sliku organizacije");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(changeImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Validate file size (max 5MB)
                long fileSizeInMB = selectedFile.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    showAlert(Alert.AlertType.WARNING, "Slika prevelika",
                            "Slika je prevelika. Maksimalna veličina je 5MB.");
                    return;
                }

                newImageData = convertFileToByteArray(selectedFile);

                // Display preview using JavaFX Image
                Image previewImage = new Image(selectedFile.toURI().toString());
                organizationImageView.setImage(previewImage);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setText("Nova slika odabrana");
                    imageStatusLabel.setVisible(true);
                }

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Greška",
                        "Došlo je do greške pri učitavanju slike: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveImage() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potvrda");
        confirmation.setHeaderText("Uklanjanje slike");
        confirmation.setContentText("Jeste li sigurni da želite ukloniti sliku organizacije?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            newImageData = new byte[0]; // Empty array to indicate removal
            organizationImageView.setImage(null);
            if (imageStatusLabel != null) {
                imageStatusLabel.setText("Slika uklonjena");
                imageStatusLabel.setVisible(true);
            }
        }
    }

    private void setEditMode(boolean editMode) {
        this.isEditMode = editMode;

        // Toggle visibility of buttons (with null checks)
        if (editButton != null) editButton.setVisible(!editMode);
        if (saveButton != null) saveButton.setVisible(editMode);
        if (cancelButton != null) cancelButton.setVisible(editMode);
        if (changeImageButton != null) changeImageButton.setVisible(editMode);
        if (removeImageButton != null) {
            removeImageButton.setVisible(editMode && currentOrganization != null &&
                    currentOrganization.getImage() != null && currentOrganization.getImage().length > 0);
        }

        // Toggle visibility of labels vs fields (with null checks)
        if (nameLabel != null) nameLabel.setVisible(!editMode);
        if (nameField != null) nameField.setVisible(editMode);

        if (ibanLabel != null) ibanLabel.setVisible(!editMode);
        if (ibanField != null) ibanField.setVisible(editMode);

        if (emailLabel != null) emailLabel.setVisible(!editMode);
        if (emailField != null) emailField.setVisible(editMode);

        if (phoneLabel != null) phoneLabel.setVisible(!editMode);
        if (phoneField != null) phoneField.setVisible(editMode);

        if (streetNameLabel != null) streetNameLabel.setVisible(!editMode);
        if (streetNameField != null) streetNameField.setVisible(editMode);

        if (streetNumLabel != null) streetNumLabel.setVisible(!editMode);
        if (streetNumField != null) streetNumField.setVisible(editMode);

        if (postalCodeLabel != null) postalCodeLabel.setVisible(!editMode);
        if (postalCodeField != null) postalCodeField.setVisible(editMode);

        if (cityLabel != null) cityLabel.setVisible(!editMode);
        if (cityField != null) cityField.setVisible(editMode);

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
            errors.append("Naziv organizacije je obavezan.\n");
        }

        if (ibanField.getText().trim().isEmpty()) {
            errors.append("IBAN je obavezan.\n");
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("Email adresa nije u ispravnom formatu.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Greška u validaciji", errors.toString());
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