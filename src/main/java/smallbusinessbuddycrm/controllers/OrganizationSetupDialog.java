package smallbusinessbuddycrm.controllers;

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

import java.io.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OrganizationSetupDialog {

    private Organization result = null;
    private Stage dialogStage;
    private byte[] selectedImageData = null;
    private ImageView imagePreview;
    private Label imageStatusLabel;

    // Form fields
    private TextField nameField;
    private TextField ibanField;
    private TextField emailField;
    private TextField phoneField;
    private TextField streetNameField;
    private TextField streetNumField;
    private TextField postalCodeField;
    private TextField cityField;

    public Optional<Organization> showAndWait(Stage parentStage) {
        createDialog(parentStage);
        dialogStage.showAndWait();
        return Optional.ofNullable(result);
    }

    private void createDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("🏢 Organization Setup");
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

        Label title = new Label("Welcome to Small Business Buddy!");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px;");

        Label subtitle = new Label("Please enter your organization's basic information");
        subtitle.setStyle("-fx-text-fill: #e3f2fd; -fx-font-size: 14px;");

        header.getChildren().addAll(titleIcon, title, subtitle);
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

        Label sectionTitle = new Label("📸 Organization Image (optional)");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        VBox imageContainer = new VBox(10);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-border-color: #dee2e6; -fx-border-radius: 8; " +
                "-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        imagePreview = new ImageView();
        imagePreview.setFitHeight(120);
        imagePreview.setFitWidth(120);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        imageStatusLabel = new Label("No image");
        imageStatusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");

        Button selectImageButton = new Button("🖼️ Select Image");
        selectImageButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16; " +
                "-fx-cursor: hand;");
        selectImageButton.setOnAction(e -> selectImage());

        imageContainer.getChildren().addAll(imagePreview, imageStatusLabel, selectImageButton);
        section.getChildren().addAll(sectionTitle, imageContainer);

        return section;
    }

    private VBox createBasicInfoSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label sectionTitle = new Label("ℹ️ Basic Information");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        // Create form fields
        nameField = createStyledTextField("Enter organization name");
        ibanField = createStyledTextField("HR1234567890123456789");
        emailField = createStyledTextField("info@organization.com");
        phoneField = createStyledTextField("+385 1 234 5678");

        // Add to grid
        addFormRow(grid, 0, "🏢 Name *:", nameField);
        addFormRow(grid, 1, "🏦 IBAN *:", ibanField);
        addFormRow(grid, 2, "📧 Email:", emailField);
        addFormRow(grid, 3, "📞 Phone:", phoneField);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createAddressSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label sectionTitle = new Label("📍 Address");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 20;");

        // Create address fields
        streetNameField = createStyledTextField("Street name");
        streetNumField = createStyledTextField("Number");
        streetNumField.setPrefWidth(80);
        postalCodeField = createStyledTextField("10000");
        cityField = createStyledTextField("Zagreb");

        HBox streetBox = new HBox(10);
        streetBox.getChildren().addAll(streetNameField, streetNumField);
        HBox.setHgrow(streetNameField, Priority.ALWAYS);

        addFormRow(grid, 0, "🛣️ Street:", streetBox);
        addFormRow(grid, 1, "📮 Postal Code:", postalCodeField);
        addFormRow(grid, 2, "🏙️ City:", cityField);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle("-fx-background-radius: 8; -fx-border-color: #ced4da; -fx-border-radius: 8; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;");
        return field;
    }

    private void addFormRow(GridPane grid, int row, String labelText, Region field) {
        Label label = new Label(labelText);
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

        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelButton = new Button("❌ Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 24; " +
                "-fx-cursor: hand;");
        cancelButton.setOnAction(e -> dialogStage.close());

        Button saveButton = new Button("💾 Save & Continue");
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 12 24; " +
                "-fx-cursor: hand;");
        saveButton.setOnAction(e -> handleSave());

        footer.getChildren().addAll(requiredNote, spacer, cancelButton, saveButton);
        return footer;
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select organization image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Validate file size (max 5MB)
                long fileSizeInMB = selectedFile.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    showAlert(Alert.AlertType.WARNING, "Image too large",
                            "The image is too large. Maximum size is 5MB.");
                    return;
                }

                selectedImageData = convertFileToByteArray(selectedFile);

                // Display preview
                Image previewImage = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(previewImage);
                imageStatusLabel.setText("Image selected (" + selectedFile.getName() + ")");
                imageStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "An error occurred while loading the image: " + e.getMessage());
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
            errors.append("• Organization name is required\n");
        }

        if (ibanField.getText().trim().isEmpty()) {
            errors.append("• IBAN is required\n");
        }

        // Validate email format if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("• Email address is not in the correct format\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errors.toString());
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