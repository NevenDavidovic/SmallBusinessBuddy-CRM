package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

// Barcode generation imports - ZXing library for PDF417
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;

// Import your existing classes
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

public class BarcodeGeneratorViewController implements Initializable {

    // Form Fields - Note: bankCodeField and currencyCombo are removed from FXML
    @FXML private TextField amountField;
    @FXML private TextField referenceField;

    // Form Fields - Payer Information
    @FXML private TextField payerNameField;
    @FXML private TextField payerAddressField;
    @FXML private TextField payerCityField;

    // Form Fields - Recipient Information
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientAddressField;
    @FXML private TextField recipientCityField;
    @FXML private TextField ibanField;

    // Form Fields - Payment Details
    @FXML private TextField modelField;
    @FXML private TextField purposeCodeField;
    @FXML private TextArea descriptionField; // Changed to TextArea in FXML

    // Action Buttons
    @FXML private Button generateButton;
    @FXML private Button loadTemplateButton;
    @FXML private Button clearButton;
    @FXML private Button saveTemplateButton;

    // Display Controls
    @FXML private StackPane paymentSlipContainer;
    @FXML private VBox placeholderContent;
    @FXML private ImageView generatedBarcodeView;
    @FXML private HBox actionButtonsContainer;

    // Generated Slip Action Buttons
    @FXML private Button saveSlipButton;
    @FXML private Button printSlipButton;
    @FXML private Button copyDataButton;

    // Constants - Hidden from user but used internally
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";

    // Database access
    private OrganizationDAO organizationDAO;
    private Organization currentOrganization;

    // Data
    private String currentPaymentData;
    private BufferedImage currentBarcodeImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BarcodeGeneratorViewController.initialize() called");

        // Initialize database access
        initializeDatabase();

        setupInitialValues();
        setupEventHandlers();

        System.out.println("BarcodeGeneratorViewController initialized successfully");
    }

    private void initializeDatabase() {
        try {
            organizationDAO = new OrganizationDAO();

            // Try to get the first organization from database
            Optional<Organization> orgOptional = organizationDAO.getFirst();
            if (orgOptional.isPresent()) {
                currentOrganization = orgOptional.get();
                System.out.println("Loaded organization: " + currentOrganization.getName());
            } else {
                System.out.println("No organization found in database");
                // Create a default organization if none exists
                createDefaultOrganization();
            }
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            // Continue without database connection
        }
    }

    private void createDefaultOrganization() {
        try {
            // Create a default organization
            currentOrganization = new Organization();
            currentOrganization.setName("Your Company Name");
            currentOrganization.setIban("HR1234567890123456789");
            currentOrganization.setStreetName("Your Street");
            currentOrganization.setStreetNum("123");
            currentOrganization.setPostalCode("10000");
            currentOrganization.setCity("Zagreb");
            currentOrganization.setEmail("info@yourcompany.com");
            currentOrganization.setPhoneNum("+385 1 234 5678");

            // Save to database
            if (organizationDAO != null && organizationDAO.save(currentOrganization)) {
                System.out.println("Default organization created and saved");
            } else {
                System.out.println("Failed to save default organization");
            }
        } catch (Exception e) {
            System.err.println("Error creating default organization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupInitialValues() {
        // Set default values
        modelField.setText("HR01");

        // Load organization data into recipient fields
        loadOrganizationData();

        // Load example template by default
        loadExampleTemplate();
    }

    private void loadOrganizationData() {
        if (currentOrganization != null) {
            // Populate recipient fields with organization data
            recipientNameField.setText(currentOrganization.getName());
            recipientAddressField.setText(
                    (currentOrganization.getStreetName() != null ? currentOrganization.getStreetName() : "") +
                            (currentOrganization.getStreetNum() != null ? " " + currentOrganization.getStreetNum() : "")
            );
            recipientCityField.setText(
                    (currentOrganization.getPostalCode() != null ? currentOrganization.getPostalCode() : "") +
                            (currentOrganization.getCity() != null ? " " + currentOrganization.getCity() : "")
            );
            ibanField.setText(currentOrganization.getIban());

            System.out.println("Organization data loaded into recipient fields");
        } else {
            System.out.println("No organization data available");
        }
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        // Main action buttons
        generateButton.setOnAction(e -> handleGenerateBarcode());
        loadTemplateButton.setOnAction(e -> handleLoadTemplate());
        clearButton.setOnAction(e -> handleClearAll());
        saveTemplateButton.setOnAction(e -> handleSaveTemplate());

        // Generated slip action buttons
        saveSlipButton.setOnAction(e -> handleSaveBarcode());
        printSlipButton.setOnAction(e -> handlePrintBarcode());
        copyDataButton.setOnAction(e -> handleCopyData());

        // Add double-click handler to refresh organization data
        recipientNameField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                refreshOrganizationData();
            }
        });

        // Setup currency formatting for amount field
        setupCurrencyFormatting();

        System.out.println("Event handlers setup completed");
    }

    private void setupCurrencyFormatting() {
        // Add listener to format amount as currency while typing
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            // Remove all non-digit characters
            String digitsOnly = newValue.replaceAll("[^0-9]", "");

            if (digitsOnly.isEmpty()) {
                amountField.setText("");
                return;
            }

            // Limit to reasonable amount (max 999,999.99)
            if (digitsOnly.length() > 8) {
                digitsOnly = digitsOnly.substring(0, 8);
            }

            // Format as currency
            String formatted = formatCurrency(digitsOnly);

            // Avoid infinite loop by checking if text actually changed
            if (!formatted.equals(newValue)) {
                amountField.setText(formatted);
                amountField.positionCaret(formatted.length());
            }
        });

        // Handle backspace and delete keys
        amountField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case BACK_SPACE:
                case DELETE:
                    String currentText = amountField.getText();
                    if (currentText != null && !currentText.isEmpty()) {
                        // Remove last digit
                        String digitsOnly = currentText.replaceAll("[^0-9]", "");
                        if (digitsOnly.length() > 1) {
                            digitsOnly = digitsOnly.substring(0, digitsOnly.length() - 1);
                            String formatted = formatCurrency(digitsOnly);
                            amountField.setText(formatted);
                            amountField.positionCaret(formatted.length());
                        } else {
                            amountField.setText("");
                        }
                        event.consume();
                    }
                    break;
            }
        });
    }

    private String formatCurrency(String digitsOnly) {
        if (digitsOnly == null || digitsOnly.isEmpty()) {
            return "";
        }

        // Pad with leading zeros if needed (minimum 3 digits for 0,01)
        while (digitsOnly.length() < 3) {
            digitsOnly = "0" + digitsOnly;
        }

        // Split into euros and cents
        int length = digitsOnly.length();
        String cents = digitsOnly.substring(length - 2);
        String euros = digitsOnly.substring(0, length - 2);

        // Format euros with thousand separators (Croatian style: 1.234)
        if (euros.length() > 3) {
            StringBuilder formattedEuros = new StringBuilder();
            int count = 0;
            for (int i = euros.length() - 1; i >= 0; i--) {
                if (count > 0 && count % 3 == 0) {
                    formattedEuros.insert(0, ".");
                }
                formattedEuros.insert(0, euros.charAt(i));
                count++;
            }
            euros = formattedEuros.toString();
        }

        // Remove leading zeros from euros part, but keep at least one digit
        euros = euros.replaceFirst("^0+", "");
        if (euros.isEmpty()) {
            euros = "0";
        }

        // Return formatted amount (Croatian style: 1.234,56)
        return euros + "," + cents;
    }

    private String getCurrencyValueForHUB3() {
        // Convert display format back to cents for HUB-3 encoding
        String displayText = amountField.getText();
        if (displayText == null || displayText.isEmpty()) {
            return "0";
        }

        // Extract only digits
        String digitsOnly = displayText.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return "0";
        }

        // Return as string of cents (for HUB-3 format)
        return digitsOnly;
    }

    private void loadExampleTemplate() {
        // Load example data for EUR currency (only payer info, recipient is from organization)
        amountField.setText("0,12"); // Set as formatted currency
        referenceField.setText("");

        // Only populate payer information - recipient comes from organization
        payerNameField.setText("Katarina Kadum");
        payerAddressField.setText("Anke Butorac 2");
        payerCityField.setText("52440 Poreč");

        // Recipient information is loaded from organization data
        // Don't override organization data with example data

        modelField.setText(""); // Can be null/empty
        purposeCodeField.setText("");
        descriptionField.setText("plaćanje članarine");
    }

    private void handleGenerateBarcode() {
        try {
            System.out.println("Generate barcode clicked");

            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Generate the HUB-3 data string
            currentPaymentData = generateHUB3Data();

            // Generate PDF417 barcode image
            generateBarcodeImage();

            // Show the generated barcode
            showGeneratedBarcode();

            showSuccess("HUB-3 PDF417 Barcode generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating barcode: " + e.getMessage());
            e.printStackTrace();
            showAlert("Generation Error", "Failed to generate barcode: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (payerNameField.getText().trim().isEmpty()) {
            errors.append("• Payer name is required\n");
        }
        if (recipientNameField.getText().trim().isEmpty()) {
            errors.append("• Recipient name is required\n");
        }
        if (amountField.getText().trim().isEmpty()) {
            errors.append("• Amount is required\n");
        }
        if (ibanField.getText().trim().isEmpty()) {
            errors.append("• IBAN is required\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private String generateHUB3Data() {
        // Generate HUB-3 format data string with fixed bank code and EUR currency
        StringBuilder hub3Data = new StringBuilder();

        // 1. bazniKod: Bank code (hardcoded)
        hub3Data.append(FIXED_BANK_CODE).append("\n");

        // 2. Currency (hardcoded to EUR)
        hub3Data.append(FIXED_CURRENCY).append("\n");

        // 3. iznosTransakcije: Transaction amount
        String amount = getCurrencyValueForHUB3(); // Get the value in cents
        if (!amount.isEmpty() && !amount.equals("0")) {
            // Format amount - pad with zeros to 15 digits
            amount = String.format("%015d", Long.parseLong(amount));
        } else {
            amount = "000000000000000"; // 15 zeros for empty amount
        }
        hub3Data.append(amount).append("\n");

        // 4. imePlatitelja: Payer name
        hub3Data.append(payerNameField.getText().trim()).append("\n");

        // 5. adresaPlatitelja: Payer address
        hub3Data.append(payerAddressField.getText().trim()).append("\n");

        // 6. postanskiBrojIMjestoPlatitelja: Payer postal code and city
        hub3Data.append(payerCityField.getText().trim()).append("\n");

        // 7. imePrimatelja: Recipient name
        hub3Data.append(recipientNameField.getText().trim()).append("\n");

        // 8. adresaPrimatelja: Recipient address
        hub3Data.append(recipientAddressField.getText().trim()).append("\n");

        // 9. postanskiBrojIMjestoPrimatelja: Recipient postal code and city
        hub3Data.append(recipientCityField.getText().trim()).append("\n");

        // 10. ibanPrimatelja: Recipient IBAN
        hub3Data.append(ibanField.getText().trim()).append("\n");

        // 11. modelPlacanja: Payment model (can be null/empty)
        hub3Data.append(modelField.getText().trim()).append("\n");

        // 12. pozivNaBroj: Reference number
        hub3Data.append(referenceField.getText().trim()).append("\n");

        // 13. sifraNamjene: Purpose code
        hub3Data.append(purposeCodeField.getText().trim()).append("\n");

        // 14. opisPlacanja: Payment description (no newline at the end)
        hub3Data.append(descriptionField.getText().trim());

        System.out.println("Generated HUB-3 data (EUR currency, fixed bank code):");
        System.out.println(hub3Data.toString());
        System.out.println("Total length: " + hub3Data.length() + " characters");

        return hub3Data.toString();
    }

    private void generateBarcodeImage() throws WriterException {
        try {
            // Generate PDF417 barcode
            currentBarcodeImage = generatePDF417Barcode();
            System.out.println("PDF417 barcode generated successfully for HUB-3 data");

        } catch (Exception e) {
            System.err.println("PDF417 generation failed: " + e.getMessage());
            e.printStackTrace();
            throw new WriterException("Failed to generate PDF417 barcode: " + e.getMessage());
        }
    }

    private BufferedImage generatePDF417Barcode() throws WriterException {
        PDF417Writer writer = new PDF417Writer();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, 2); // Medium error correction
        hints.put(EncodeHintType.PDF417_COMPACT, false); // Full PDF417, not compact
        hints.put(EncodeHintType.MARGIN, 10);

        // Generate PDF417 barcode with proper HUB-3 dimensions
        BitMatrix bitMatrix = writer.encode(
                currentPaymentData,
                BarcodeFormat.PDF_417,
                750,  // Width - matching your display area
                220,  // Height - good for PDF417 format
                hints
        );

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private void showGeneratedBarcode() {
        // Hide placeholder
        placeholderContent.setVisible(false);

        // Convert BufferedImage to JavaFX Image and display
        Image fxImage = SwingFXUtils.toFXImage(currentBarcodeImage, null);
        generatedBarcodeView.setImage(fxImage);
        generatedBarcodeView.setVisible(true);

        // Show action buttons
        actionButtonsContainer.setVisible(true);
    }

    private void handleLoadTemplate() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Payment Template");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Properties Files", "*.properties")
            );

            Stage stage = (Stage) loadTemplateButton.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);

                    // Load amount field with currency formatting
                    String savedAmount = props.getProperty("amount", "");
                    if (!savedAmount.isEmpty()) {
                        // If saved amount is in cents, format it
                        try {
                            String digitsOnly = savedAmount.replaceAll("[^0-9]", "");
                            if (!digitsOnly.isEmpty()) {
                                String formatted = formatCurrency(digitsOnly);
                                amountField.setText(formatted);
                            }
                        } catch (Exception ex) {
                            amountField.setText("");
                        }
                    } else {
                        amountField.setText("");
                    }
                    referenceField.setText(props.getProperty("reference", ""));

                    payerNameField.setText(props.getProperty("payerName", ""));
                    payerAddressField.setText(props.getProperty("payerAddress", ""));
                    payerCityField.setText(props.getProperty("payerCity", ""));

                    recipientNameField.setText(props.getProperty("recipientName", ""));
                    recipientAddressField.setText(props.getProperty("recipientAddress", ""));
                    recipientCityField.setText(props.getProperty("recipientCity", ""));
                    ibanField.setText(props.getProperty("iban", ""));

                    modelField.setText(props.getProperty("model", ""));
                    purposeCodeField.setText(props.getProperty("purposeCode", ""));
                    descriptionField.setText(props.getProperty("description", ""));

                    showSuccess("Template loaded successfully!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading template: " + e.getMessage());
            e.printStackTrace();
            showAlert("Load Error", "Failed to load template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSaveTemplate() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Payment Template");
            fileChooser.setInitialFileName("payment_template_" + System.currentTimeMillis() + ".properties");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Properties Files", "*.properties")
            );

            Stage stage = (Stage) saveTemplateButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                Properties props = new Properties();

                // Save amount as cents value (for consistent storage)
                props.setProperty("amount", getCurrencyValueForHUB3());
                props.setProperty("reference", referenceField.getText());

                props.setProperty("payerName", payerNameField.getText());
                props.setProperty("payerAddress", payerAddressField.getText());
                props.setProperty("payerCity", payerCityField.getText());

                props.setProperty("recipientName", recipientNameField.getText());
                props.setProperty("recipientAddress", recipientAddressField.getText());
                props.setProperty("recipientCity", recipientCityField.getText());
                props.setProperty("iban", ibanField.getText());

                props.setProperty("model", modelField.getText());
                props.setProperty("purposeCode", purposeCodeField.getText());
                props.setProperty("description", descriptionField.getText());

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    props.store(fos, "HUB-3 Payment Template (EUR) - " + LocalDateTime.now());
                    showSuccess("Template saved successfully!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving template: " + e.getMessage());
            e.printStackTrace();
            showAlert("Save Error", "Failed to save template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleClearAll() {
        // Clear all fields
        amountField.clear();
        referenceField.clear();

        payerNameField.clear();
        payerAddressField.clear();
        payerCityField.clear();

        // Don't clear recipient fields - they should keep organization data
        // recipientNameField.clear();
        // recipientAddressField.clear();
        // recipientCityField.clear();
        // ibanField.clear();

        modelField.clear();
        purposeCodeField.clear();
        descriptionField.clear();

        // Reset display
        placeholderContent.setVisible(true);
        generatedBarcodeView.setVisible(false);
        actionButtonsContainer.setVisible(false);

        // Set some defaults back and reload organization data
        modelField.setText("HR01");
        loadOrganizationData(); // Reload organization data into recipient fields

        showSuccess("All fields cleared! (Recipient info kept from organization)");
    }

    private void handleSaveBarcode() {
        if (currentBarcodeImage == null) {
            showAlert("No Barcode", "Please generate a barcode first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Barcode");
            fileChooser.setInitialFileName("hub3_pdf417_barcode_" + System.currentTimeMillis() + ".png");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Images", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG Images", "*.jpg"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            Stage stage = (Stage) saveSlipButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String format = file.getName().toLowerCase().endsWith(".jpg") ? "jpg" : "png";
                javax.imageio.ImageIO.write(currentBarcodeImage, format, file);
                showSuccess("PDF417 barcode saved successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error saving barcode: " + e.getMessage());
            e.printStackTrace();
            showAlert("Save Error", "Failed to save barcode: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handlePrintBarcode() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Print");
        info.setHeaderText("Print Barcode");
        info.setContentText("Print functionality will be implemented soon!");
        info.showAndWait();
    }

    private void handleCopyData() {
        if (currentPaymentData != null) {
            // Copy to system clipboard
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(currentPaymentData);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

            showSuccess("Payment data copied to clipboard!");
        } else {
            showAlert("No Data", "Please generate a barcode first.", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Non-blocking

        // Auto-close after 2 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> alert.close())
        );
        timeline.play();
    }

    // Method to refresh organization data from database
    private void refreshOrganizationData() {
        try {
            if (organizationDAO != null) {
                Optional<Organization> orgOptional = organizationDAO.getFirst();
                if (orgOptional.isPresent()) {
                    currentOrganization = orgOptional.get();
                    loadOrganizationData();
                    showSuccess("Organization data refreshed from database!");
                } else {
                    showAlert("No Organization", "No organization found in database.", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            System.err.println("Error refreshing organization data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to refresh organization data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Method to get organization display info (for debugging/info purposes)
    public String getOrganizationInfo() {
        if (currentOrganization != null) {
            return String.format("Organization: %s | IBAN: %s | Address: %s",
                    currentOrganization.getName(),
                    currentOrganization.getIban(),
                    currentOrganization.getFullAddress());
        }
        return "No organization loaded";
    }
}