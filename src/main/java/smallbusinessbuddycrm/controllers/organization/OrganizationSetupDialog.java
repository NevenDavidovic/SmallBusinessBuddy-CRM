package smallbusinessbuddycrm.controllers.organization;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OrganizationSetupDialog {

    private Organization result = null;
    private Stage dialogStage;
    private byte[] selectedImageData = null;
    private ImageView imagePreview;
    private Label imageStatusLabel;
    private LanguageManager languageManager;

    // Form fields
    private TextField nameField;
    private TextField ibanField;
    private TextField emailField;
    private TextField phoneField;
    private TextField streetNameField;
    private TextField streetNumField;
    private TextField postalCodeField;
    private TextField cityField;

    // UI Labels that need to be updated on language change
    private Label titleLabel;
    private Label subtitleLabel;
    private Label imageSectionTitleLabel;
    private Label basicInfoSectionTitleLabel;
    private Label addressSectionTitleLabel;
    private Label nameLabel;
    private Label ibanLabel;
    private Label emailLabel;
    private Label phoneLabel;
    private Label streetLabel;
    private Label postalCodeLabel;
    private Label cityLabel;
    private Button selectImageButton;
    private Label requiredNoteLabel;
    private Button cancelButton;
    private Button saveButton;
    private FileChooser fileChooser;

    public OrganizationSetupDialog() {
        this.languageManager = LanguageManager.getInstance();
    }

    public Optional<Organization> showAndWait(Stage parentStage) {
        createDialog(parentStage);

        // Add language change listener after dialog is created
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();

        dialogStage.showAndWait();
        return Optional.ofNullable(result);
    }

    private void updateTexts() {
        // Update dialog title
        if (dialogStage != null) {
            dialogStage.setTitle("🏢 " + languageManager.getText("organization.setup.dialog.title"));
        }

        // Update header labels
        if (titleLabel != null) {
            titleLabel.setText(languageManager.getText("organization.setup.welcome.title"));
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(languageManager.getText("organization.setup.welcome.subtitle"));
        }

        // Update section titles
        if (imageSectionTitleLabel != null) {
            imageSectionTitleLabel.setText("📸 " + languageManager.getText("organization.setup.image.section.title"));
        }
        if (basicInfoSectionTitleLabel != null) {
            basicInfoSectionTitleLabel.setText("ℹ️ " + languageManager.getText("organization.setup.basic.info.section.title"));
        }
        if (addressSectionTitleLabel != null) {
            addressSectionTitleLabel.setText("📍 " + languageManager.getText("organization.setup.address.section.title"));
        }

        // Update field labels
        if (nameLabel != null) nameLabel.setText("🏢 " + languageManager.getText("organization.setup.field.name"));
        if (ibanLabel != null) ibanLabel.setText("🏦 " + languageManager.getText("organization.setup.field.iban"));
        if (emailLabel != null) emailLabel.setText("📧 " + languageManager.getText("organization.setup.field.email"));
        if (phoneLabel != null) phoneLabel.setText("📞 " + languageManager.getText("organization.setup.field.phone"));
        if (streetLabel != null) streetLabel.setText("🛣️ " + languageManager.getText("organization.setup.field.street"));
        if (postalCodeLabel != null) postalCodeLabel.setText("📮 " + languageManager.getText("organization.setup.field.postal.code"));
        if (cityLabel != null) cityLabel.setText("🏙️ " + languageManager.getText("organization.setup.field.city"));

        // Update field prompts
        if (nameField != null) nameField.setPromptText(languageManager.getText("organization.setup.prompt.name"));
        if (ibanField != null) ibanField.setPromptText(languageManager.getText("organization.setup.prompt.iban"));
        if (emailField != null) emailField.setPromptText(languageManager.getText("organization.setup.prompt.email"));
        if (phoneField != null) phoneField.setPromptText(languageManager.getText("organization.setup.prompt.phone"));
        if (streetNameField != null) streetNameField.setPromptText(languageManager.getText("organization.setup.prompt.street.name"));
        if (streetNumField != null) streetNumField.setPromptText(languageManager.getText("organization.setup.prompt.street.number"));
        if (postalCodeField != null) postalCodeField.setPromptText(languageManager.getText("organization.setup.prompt.postal.code"));
        if (cityField != null) cityField.setPromptText(languageManager.getText("organization.setup.prompt.city"));

        // Update image status
        updateImageStatusLabel();

        // Update buttons
        if (selectImageButton != null) {
            selectImageButton.setText("🖼️ " + languageManager.getText("organization.setup.button.select.image"));
        }
        if (requiredNoteLabel != null) {
            requiredNoteLabel.setText(languageManager.getText("organization.setup.required.note"));
        }
        if (cancelButton != null) {
            cancelButton.setText("❌ " + languageManager.getText("organization.setup.button.cancel"));
        }
        if (saveButton != null) {
            saveButton.setText("💾 " + languageManager.getText("organization.setup.button.save"));
        }

        // Update file chooser
        updateFileChooser();
    }

    private void updateImageStatusLabel() {
        if (imageStatusLabel != null) {
            String currentText = imageStatusLabel.getText();

            // Check current state and update accordingly
            if (selectedImageData == null) {
                imageStatusLabel.setText(languageManager.getText("organization.setup.image.no.image"));
                imageStatusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            } else if (currentText.contains("selected") || currentText.contains("odabrana")) {
                // Keep the filename if image is selected, just update the prefix
                String filename = extractFilenameFromStatus(currentText);
                imageStatusLabel.setText(languageManager.getText("organization.setup.image.selected")
                        .replace("{0}", filename));
                imageStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            }
        }
    }

    private String extractFilenameFromStatus(String statusText) {
        // Extract filename from status text like "Image selected (filename.jpg)"
        int start = statusText.indexOf('(');
        int end = statusText.indexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return statusText.substring(start + 1, end);
        }
        return "image.jpg"; // fallback
    }

    private void updateFileChooser() {
        if (fileChooser != null) {
            fileChooser.setTitle(languageManager.getText("organization.setup.file.chooser.title"));
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(
                            languageManager.getText("organization.setup.file.filter.image"),
                            "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter(
                            languageManager.getText("organization.setup.file.filter.png"),
                            "*.png"),
                    new FileChooser.ExtensionFilter(
                            languageManager.getText("organization.setup.file.filter.jpg"),
                            "*.jpg", "*.jpeg")
            );
        }
    }

    private void createDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("🏢 " + languageManager.getText("organization.setup.dialog.title"));
        dialogStage.setResizable(false);

        VBox mainContent = createMainContent();

        Scene scene = new Scene(mainContent, 600, 750);
        dialogStage.setScene(scene);

        // Center the dialog on parent stage
        dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - 600) / 2);
        dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - 750) / 2);
    }

    private VBox createMainContent() {
        VBox mainVBox = new VBox(0);
        mainVBox.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);");

        // Header
        VBox header = createHeader();

        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = createContentForm();
        scrollPane.setContent(content);

        // Footer with buttons
        HBox footer = createFooter();

        mainVBox.getChildren().addAll(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return mainVBox;
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 40, 20, 40));
        header.setStyle("-fx-background-color: linear-gradient(to right, #4a90e2, #357abd);");

        Label titleIcon = new Label("🏢");
        titleIcon.setStyle("-fx-font-size: 36px;");

        titleLabel = new Label(languageManager.getText("organization.setup.welcome.title"));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px;");

        subtitleLabel = new Label(languageManager.getText("organization.setup.welcome.subtitle"));
        subtitleLabel.setStyle("-fx-text-fill: #e3f2fd; -fx-font-size: 14px;");

        header.getChildren().addAll(titleIcon, titleLabel, subtitleLabel);
        return header;
    }

    private VBox createContentForm() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));

        // Image section
        VBox imageSection = createImageSection();

        // Basic info section
        VBox basicInfoSection = createBasicInfoSection();

        // Address section
        VBox addressSection = createAddressSection();

        content.getChildren().addAll(imageSection, basicInfoSection, addressSection);
        return content;
    }

    private VBox createImageSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        imageSectionTitleLabel = new Label("📸 " + languageManager.getText("organization.setup.image.section.title"));
        imageSectionTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        VBox imageContainer = new VBox(10);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-border-color: #dee2e6; -fx-border-radius: 8; " +
                "-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        imagePreview = new ImageView();
        imagePreview.setFitHeight(120);
        imagePreview.setFitWidth(120);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        imageStatusLabel = new Label(languageManager.getText("organization.setup.image.no.image"));
        imageStatusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");

        selectImageButton = new Button("🖼️ " + languageManager.getText("organization.setup.button.select.image"));
        selectImageButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16; " +
                "-fx-cursor: hand;");
        selectImageButton.setOnAction(e -> selectImage());

        imageContainer.getChildren().addAll(imagePreview, imageStatusLabel, selectImageButton);
        section.getChildren().addAll(imageSectionTitleLabel, imageContainer);

        return section;
    }

    private VBox createBasicInfoSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        basicInfoSectionTitleLabel = new Label("ℹ️ " + languageManager.getText("organization.setup.basic.info.section.title"));
        basicInfoSectionTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        // Create form fields
        nameField = createStyledTextField(languageManager.getText("organization.setup.prompt.name"));
        ibanField = createStyledTextField(languageManager.getText("organization.setup.prompt.iban"));
        emailField = createStyledTextField(languageManager.getText("organization.setup.prompt.email"));
        phoneField = createStyledTextField(languageManager.getText("organization.setup.prompt.phone"));

        // Create labels
        nameLabel = new Label("🏢 " + languageManager.getText("organization.setup.field.name"));
        ibanLabel = new Label("🏦 " + languageManager.getText("organization.setup.field.iban"));
        emailLabel = new Label("📧 " + languageManager.getText("organization.setup.field.email"));
        phoneLabel = new Label("📞 " + languageManager.getText("organization.setup.field.phone"));

        // Add to grid
        addFormRow(grid, 0, nameLabel, nameField);
        addFormRow(grid, 1, ibanLabel, ibanField);
        addFormRow(grid, 2, emailLabel, emailField);
        addFormRow(grid, 3, phoneLabel, phoneField);

        section.getChildren().addAll(basicInfoSectionTitleLabel, grid);
        return section;
    }

    private VBox createAddressSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        addressSectionTitleLabel = new Label("📍 " + languageManager.getText("organization.setup.address.section.title"));
        addressSectionTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        // Create address fields
        streetNameField = createStyledTextField(languageManager.getText("organization.setup.prompt.street.name"));
        streetNumField = createStyledTextField(languageManager.getText("organization.setup.prompt.street.number"));
        streetNumField.setPrefWidth(80);
        postalCodeField = createStyledTextField(languageManager.getText("organization.setup.prompt.postal.code"));
        cityField = createStyledTextField(languageManager.getText("organization.setup.prompt.city"));

        HBox streetBox = new HBox(10);
        streetBox.getChildren().addAll(streetNameField, streetNumField);
        HBox.setHgrow(streetNameField, Priority.ALWAYS);

        // Create labels
        streetLabel = new Label("🛣️ " + languageManager.getText("organization.setup.field.street"));
        postalCodeLabel = new Label("📮 " + languageManager.getText("organization.setup.field.postal.code"));
        cityLabel = new Label("🏙️ " + languageManager.getText("organization.setup.field.city"));

        addFormRow(grid, 0, streetLabel, streetBox);
        addFormRow(grid, 1, postalCodeLabel, postalCodeField);
        addFormRow(grid, 2, cityLabel, cityField);

        section.getChildren().addAll(addressSectionTitleLabel, grid);
        return section;
    }

    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle("-fx-background-radius: 8; -fx-border-color: #ced4da; -fx-border-radius: 8; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;");
        return field;
    }

    private void addFormRow(GridPane grid, int row, Label label, Region field) {
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057; -fx-font-size: 14px;");

        grid.add(label, 0, row);
        grid.add(field, 1, row);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setPrefWidth(120);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().setAll(col1, col2);
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 40, 30, 40));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        requiredNoteLabel = new Label(languageManager.getText("organization.setup.required.note"));
        requiredNoteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cancelButton = new Button("❌ " + languageManager.getText("organization.setup.button.cancel"));
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 24; " +
                "-fx-cursor: hand;");
        cancelButton.setOnAction(e -> dialogStage.close());

        saveButton = new Button("💾 " + languageManager.getText("organization.setup.button.save"));
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 24; " +
                "-fx-cursor: hand;");
        saveButton.setOnAction(e -> handleSave());

        footer.getChildren().addAll(requiredNoteLabel, spacer, cancelButton, saveButton);
        return footer;
    }

    private void selectImage() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
        }
        updateFileChooser(); // Ensure it has current language

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Validate file size (max 5MB)
                long fileSizeInMB = selectedFile.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("organization.setup.image.error.too.large.title"),
                            languageManager.getText("organization.setup.image.error.too.large.message"));
                    return;
                }

                selectedImageData = convertFileToByteArray(selectedFile);

                // Display preview
                Image previewImage = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(previewImage);
                imageStatusLabel.setText(languageManager.getText("organization.setup.image.selected")
                        .replace("{0}", selectedFile.getName()));
                imageStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR,
                        languageManager.getText("organization.setup.error.title"),
                        languageManager.getText("organization.setup.image.error.loading")
                                .replace("{0}", e.getMessage()));
            }
        }
    }

    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        // Create organization object
        Organization organization = new Organization(
                nameField.getText().trim(),
                ibanField.getText().trim()
        );

        // Set optional fields
        String email = emailField.getText().trim();
        if (!email.isEmpty()) organization.setEmail(email);

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()) organization.setPhoneNum(phone);

        String streetName = streetNameField.getText().trim();
        if (!streetName.isEmpty()) organization.setStreetName(streetName);

        String streetNum = streetNumField.getText().trim();
        if (!streetNum.isEmpty()) organization.setStreetNum(streetNum);

        String postalCode = postalCodeField.getText().trim();
        if (!postalCode.isEmpty()) organization.setPostalCode(postalCode);

        String city = cityField.getText().trim();
        if (!city.isEmpty()) organization.setCity(city);

        // Set image if selected
        if (selectedImageData != null) {
            organization.setImage(selectedImageData);
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);

        result = organization;
        dialogStage.close();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("• ").append(languageManager.getText("organization.setup.validation.name.required")).append("\n");
        }

        if (ibanField.getText().trim().isEmpty()) {
            errors.append("• ").append(languageManager.getText("organization.setup.validation.iban.required")).append("\n");
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("• ").append(languageManager.getText("organization.setup.validation.email.invalid")).append("\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("organization.setup.validation.error.title"),
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
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}