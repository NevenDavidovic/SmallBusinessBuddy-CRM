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

// PDF generation imports
import com.itextpdf.html2pdf.HtmlConverter;


// NEW: Payment Attachment imports
import smallbusinessbuddycrm.database.PaymentAttachmentDAO;
import smallbusinessbuddycrm.model.PaymentAttachment;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

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
    @FXML private Button savePaymentSlipButton; // NEW BUTTON
    @FXML private Button printSlipButton;
    @FXML private Button copyDataButton;

    // Constants - Hidden from user but used internally
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";

    // Database access
    private OrganizationDAO organizationDAO;
    private Organization currentOrganization;

    // NEW: Payment Attachment template management
    private PaymentAttachmentDAO paymentAttachmentDAO;
    private PaymentAttachment selectedTemplate;

    // Data
    private String currentPaymentData;
    private BufferedImage currentBarcodeImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BarcodeGeneratorViewController.initialize() called");

        // Initialize database access
        initializeDatabase();

        // NEW: Initialize payment attachment templates
        initializePaymentAttachments();

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

    // NEW: Initialize Payment Attachment templates
    private void initializePaymentAttachments() {
        try {
            paymentAttachmentDAO = new PaymentAttachmentDAO();

            // Load default template
            Optional<PaymentAttachment> defaultTemplate = paymentAttachmentDAO.findDefault();
            if (defaultTemplate.isPresent()) {
                selectedTemplate = defaultTemplate.get();
                System.out.println("Loaded default payment template: " + selectedTemplate.getName());
            } else {
                // Fallback to first available template
                List<PaymentAttachment> templates = paymentAttachmentDAO.findAll();
                if (!templates.isEmpty()) {
                    selectedTemplate = templates.get(0);
                    System.out.println("Using first available template: " + selectedTemplate.getName());
                } else {
                    System.err.println("No payment attachment templates found!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing payment attachment templates: " + e.getMessage());
            e.printStackTrace();
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
        savePaymentSlipButton.setOnAction(e -> handleSavePaymentSlip()); // NEW HANDLER
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
        try {
            // Generate the full Croatian uplatnica template
            String barcodeBase64 = encodeImageToBase64(currentBarcodeImage);
            Map<String, String> variables = createVariableMap(barcodeBase64);
            String htmlTemplate = getCroatianUplatnicaTemplate();
            String processedHTML = processTemplate(htmlTemplate, variables);

            // Hide placeholder
            placeholderContent.setVisible(false);

            // Hide the original ImageView
            generatedBarcodeView.setVisible(false);

            // Create and show WebView with full template
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setPrefSize(950, 400);
            webView.getEngine().loadContent(processedHTML);

            // Clear the container and add the WebView
            paymentSlipContainer.getChildren().clear();
            paymentSlipContainer.getChildren().add(placeholderContent); // Keep placeholder for later use
            paymentSlipContainer.getChildren().add(generatedBarcodeView); // Keep ImageView for later use
            paymentSlipContainer.getChildren().add(webView);

            // Show action buttons
            actionButtonsContainer.setVisible(true);

            System.out.println("Croatian uplatnica template displayed successfully");

        } catch (Exception e) {
            System.err.println("Error showing Croatian template: " + e.getMessage());
            e.printStackTrace();

            // Fallback to original barcode display
            showOriginalBarcodeDisplay();
        }
    }

    // UPDATED: Handle Save Payment Slip with Template Selection
    private void handleSavePaymentSlip() {
        if (currentBarcodeImage == null || currentPaymentData == null) {
            showAlert("No Payment Data", "Please generate a barcode first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Show template selection dialog
            PaymentAttachment chosenTemplate = showTemplateSelectionDialog();
            if (chosenTemplate == null) {
                return; // User cancelled
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Payment Slip");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName("HUB3_Payment_Slip_" + timestamp);

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                    new FileChooser.ExtensionFilter("HTML Files", "*.html"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            Stage stage = (Stage) savePaymentSlipButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String fileName = file.getName().toLowerCase();

                if (fileName.endsWith(".pdf")) {
                    savePaymentSlipAsPDF(file, chosenTemplate);
                    showSuccess("Payment slip saved as PDF using template: " + chosenTemplate.getName());
                } else if (fileName.endsWith(".html")) {
                    savePaymentSlipAsHTML(file, chosenTemplate);
                    showSuccess("Payment slip saved as HTML using template: " + chosenTemplate.getName());
                } else {
                    // Default to PDF if no extension specified
                    File pdfFile = new File(file.getAbsolutePath() + ".pdf");
                    savePaymentSlipAsPDF(pdfFile, chosenTemplate);
                    showSuccess("Payment slip saved as PDF using template: " + chosenTemplate.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving payment slip: " + e.getMessage());
            e.printStackTrace();
            showAlert("Save Error", "Failed to save payment slip: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // NEW: Show template selection dialog
    private PaymentAttachment showTemplateSelectionDialog() {
        try {
            List<PaymentAttachment> templates = paymentAttachmentDAO.findAll();

            if (templates.isEmpty()) {
                showAlert("No Templates", "No payment templates found. Please create at least one template first.", Alert.AlertType.WARNING);
                return null;
            }

            // Create choice dialog
            ChoiceDialog<PaymentAttachment> dialog = new ChoiceDialog<>(selectedTemplate, templates);
            dialog.setTitle("Select Payment Template");
            dialog.setHeaderText("Choose a template for your payment slip:");
            dialog.setContentText("Template:");

            // Customize the dialog
            dialog.getDialogPane().setPrefWidth(400);

            Optional<PaymentAttachment> result = dialog.showAndWait();
            if (result.isPresent()) {
                selectedTemplate = result.get(); // Remember choice for next time
                return result.get();
            }
        } catch (Exception e) {
            System.err.println("Error loading templates: " + e.getMessage());
            showAlert("Template Error", "Failed to load templates: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        return null; // User cancelled or error occurred
    }

    private void savePaymentSlipAsPDF(File file, PaymentAttachment template) throws Exception {
        // Create HTML content using selected template
        String htmlContent = generatePaymentSlipHTML(template);

        // Convert HTML to PDF using iText
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
        }
    }

    private void savePaymentSlipAsHTML(File file, PaymentAttachment template) throws Exception {
        String htmlContent = generatePaymentSlipHTML(template);

        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
    }

    // UPDATED: Generate HTML using selected template
    private String generatePaymentSlipHTML(PaymentAttachment template) throws Exception {
        // Convert barcode image to base64 for embedding
        String barcodeBase64 = encodeImageToBase64(currentBarcodeImage);

        // Create variables map for template processing
        Map<String, String> variables = createVariableMap(barcodeBase64);

        // Process template variables
        return processTemplate(template.getHtmlContent(), variables);
    }


    private Map<String, String> createVariableMap(String barcodeBase64) {
        Map<String, String> variables = new HashMap<>();

        // Current date
        variables.put("CURRENT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        // Payment Information
        variables.put("AMOUNT", amountField.getText().isEmpty() ? "0,00" : amountField.getText());
        variables.put("REFERENCE", referenceField.getText());
        variables.put("MODEL", modelField.getText());
        variables.put("PURPOSE", purposeCodeField.getText());
        variables.put("DESCRIPTION", descriptionField.getText().replace("\n", "<br>"));

        // Payer Information
        variables.put("PAYER_NAME", payerNameField.getText());
        variables.put("PAYER_ADDRESS", payerAddressField.getText());
        variables.put("PAYER_CITY", payerCityField.getText());

        // Recipient Information
        variables.put("RECIPIENT_NAME", recipientNameField.getText());
        variables.put("RECIPIENT_ADDRESS", recipientAddressField.getText());
        variables.put("RECIPIENT_CITY", recipientCityField.getText());
        variables.put("RECIPIENT_IBAN", ibanField.getText());

        // Technical
        variables.put("BARCODE_BASE64", barcodeBase64);
        variables.put("BANK_CODE", FIXED_BANK_CODE);

        // NEW: Uplatnica background image as base64
        variables.put("BACKGROUND_IMAGE_BASE64", getUplatnicaBackgroundBase64());

        return variables;
    }

    // NEW: Get the Croatian Uplatnica template HTML
    private String getCroatianUplatnicaTemplate() {
        return """
<!DOCTYPE html>
<html lang="hr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Croatian Uplatnica</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0 20px;
            background-color: #f5f5f5;
        }
        
        .uplatnica-form-img {
            border: 2px solid #ffffff;
            width: 931px;
            height: 380px;
            background-size: cover;
            background-image: url('data:image/png;base64,{{BACKGROUND_IMAGE_BASE64}}');
            margin: 0 auto;
            position: relative;
        }
        
        .uplatnica-form-img .platitelj {
            position: absolute;
            display: flex;
            flex-direction: column;
            max-width: 200px;
            left: 31px;
            top: 44px;
        }
        
        .uplatnica-form-img .platitelj .field {
            height: 20px;
            border: 0px solid #a26b6b;
            font-size: 12px;
            background: transparent;
            color: #000;
            margin-bottom: 2px;
        }
        
        .uplatnica-form-img .iban-primatelja {
            position: absolute;
            top: 121px;
            left: 335px;
            border: 0 solid transparent;
            background-color: transparent;
            font-size: 12px;
            letter-spacing: 8px;
            max-width: 312px;
            width: 100%;
            height: 27px;
            color: #000;
        }
        
        .uplatnica-form-img .iznos {
            left: 595px;
            position: absolute;
            top: 28px;
            background: transparent;
            font-size: 12px;
            letter-spacing: 0px;
            border: 0 solid;
            max-width: 200px;
            text-align: end;
            letter-spacing: 3px;
            height: 27px;
            padding-top: 5px;
            color: #000;
        }
        
        .uplatnica-form-img .valuta {
            width: 50px;
            height: 27px;
            top: 32px;
            position: absolute;
            left: 345px;
            border: 0 solid;
            background: transparent;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .primatelj {
            position: absolute;
            display: flex;
            flex-direction: column;
            max-width: 200px;
            left: 31px;
            top: 170px;
        }
        
        .uplatnica-form-img .primatelj .field {
            height: 20px;
            border: 0px solid #a26b6b;
            font-size: 12px;
            background: transparent;
            color: #000;
            margin-bottom: 2px;
        }
        
        .uplatnica-form-img .model-placanja {
            position: absolute;
            top: 160px;
            background: transparent;
            left: 240px;
            letter-spacing: 7px;
            width: 57px;
            height: 27px;
            border: 0 solid;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .opis-placanja {
            width: 249px;
            position: absolute;
            background: transparent;
            top: 180px;
            left: 384px;
            height: 50px;
            border: 0 solid;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .poziv-na-broj-primatelja {
            height: 27px;
            width: 200px;
            position: absolute;
            top: 154px;
            left: 330px;
            border: 0 solid;
            background: transparent;
            font-size: 14px;
            color: #000;
            padding-top: 5px;
        }
        
        .uplatnica-form-img .sifra-namjene {
            height: 27px;
            position: absolute;
            top: 199px;
            left: 250px;
            border: 0 solid;
            background: transparent;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .generated-barcode {
            position: absolute;
            width: 250px;
            left: 30px;
            bottom: 33px;
        }
        
        .uplatnica-form-img .generated-barcode img {
            width: 100%;
        }
        
        .uplatnica-form-img .iznos-desno {
            height: 27px;
            padding-top: 5px;
            position: absolute;
            right: 19px;
            top: 26px;
            background: transparent;
            border: 0 solid;
            font-size: 12px;
            color: #000;
            text-align: right;
            letter-spacing: 3px;
        }
        
        .uplatnica-form-img .model-placanja-desno {
            position: absolute;
            right: 194px;
            top: 123px;
            background: transparent;
            border: 0;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .iban-primatelja-desno {
            position: absolute;
            right: 30px;
            top: 120px;
            border: transparent;
            background: transparent;
            padding-top: 3px;
            font-size: 12px;
            height: 33px;
            color: #000;
        }
        
        .uplatnica-form-img .poziv-na-broj-primatelja-desno {
            position: absolute;
            right: 30px;
            top: 156px;
            border: 0 solid;
            background: transparent;
            padding-top: 3px;
            padding-bottom: 10px;
            height: 33px;
            font-size: 12px;
            color: #000;
        }
        
        .uplatnica-form-img .opis-placanja-desno {
            position: absolute;
            bottom: 149px;
            right: 12px;
            width: 238px;
            border: 0 transparent;
            background: transparent;
            height: 45px;
            font-size: 12px;
            padding-top: 5px;
            word-wrap: break-word;
            word-break: break-all;
            color: #000;
        }
    </style>
</head>
<body>
    <div class="uplatnica-form-img">
        <!-- Payer Information -->
        <div class="platitelj">
            <div class="field">{{PAYER_NAME}}</div>
            <div class="field">{{PAYER_ADDRESS}}</div>
            <div class="field">{{PAYER_CITY}}</div>
        </div>

        <!-- Currency -->
        <div class="valuta">EUR</div>

        <!-- Amount -->
        <div class="iznos">{{AMOUNT}}</div>

        <!-- IBAN -->
        <div class="iban-primatelja">{{RECIPIENT_IBAN}}</div>

        <!-- Recipient Information -->
        <div class="primatelj">
            <div class="field">{{RECIPIENT_NAME}}</div>
            <div class="field">{{RECIPIENT_ADDRESS}}</div>
            <div class="field">{{RECIPIENT_CITY}}</div>
        </div>

        <!-- Payment Model -->
        <div class="model-placanja">{{MODEL}}</div>

        <!-- Reference Number -->
        <div class="poziv-na-broj-primatelja">{{REFERENCE}}</div>

        <!-- Purpose Code -->
        <div class="sifra-namjene">{{PURPOSE}}</div>

        <!-- Description -->
        <div class="opis-placanja">{{DESCRIPTION}}</div>

        <!-- Barcode -->
        <div class="generated-barcode">
            <img src="data:image/png;base64,{{BARCODE_BASE64}}" alt="Generated Barcode" />
        </div>

        <!-- Right side duplicated fields -->
        <div class="iznos-desno">{{AMOUNT}}</div>
        <div class="iban-primatelja-desno">{{RECIPIENT_IBAN}}</div>
        <div class="model-placanja-desno">{{MODEL}}</div>
        <div class="poziv-na-broj-primatelja-desno">{{REFERENCE}}</div>
        <div class="opis-placanja-desno">{{DESCRIPTION}}</div>
    </div>
</body>
</html>""";
    }


    // NEW: Process template by replacing variables
    private String processTemplate(String htmlTemplate, Map<String, String> variables) {
        String processed = htmlTemplate;

        // Replace all variables in the format {{VARIABLE_NAME}}
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            processed = processed.replace(placeholder, value);
        }

        return processed;
    }

    // Keep the old method for backward compatibility (fallback to default template)
    private String generatePaymentSlipHTML() throws Exception {
        if (selectedTemplate == null) {
            // Fallback to hardcoded template if no template selected
            return generateFallbackHTML();
        }
        return generatePaymentSlipHTML(selectedTemplate);
    }

    // Fallback HTML generation (your original method)
    private String generateFallbackHTML() throws Exception {
        String barcodeBase64 = encodeImageToBase64(currentBarcodeImage);
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        return String.format("""
<!DOCTYPE html>
<html lang="hr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HUB-3 Payment Slip</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            color: #333;
            line-height: 1.4;
        }
        .header {
            text-align: center;
            border-bottom: 2px solid #0099cc;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }
        .header h1 {
            color: #0099cc;
            margin: 0;
            font-size: 24px;
        }
        .payment-info {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 20px;
        }
        .section {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
            background-color: #f9f9f9;
        }
        .section h3 {
            margin-top: 0;
            color: #0099cc;
            border-bottom: 1px solid #ddd;
            padding-bottom: 5px;
        }
        .field {
            margin-bottom: 8px;
        }
        .field-label {
            font-weight: bold;
            display: inline-block;
            width: 100px;
            color: #555;
        }
        .field-value {
            color: #333;
        }
        .barcode-section {
            text-align: center;
            margin: 30px 0;
            padding: 20px;
            border: 2px dashed #ccc;
            background-color: #f8f9fa;
        }
        .barcode-image {
            max-width: 100%%;
            height: auto;
            border: 1px solid #333;
            background-color: white;
        }
        .footer {
            margin-top: 30px;
            text-align: center;
            font-size: 12px;
            color: #666;
            border-top: 1px solid #ddd;
            padding-top: 10px;
        }
        .amount-highlight {
            font-size: 18px;
            font-weight: bold;
            color: #28a745;
        }
        @media print {
            body { margin: 10px; }
            .header h1 { font-size: 20px; }
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🇭🇷 Croatian HUB-3 Payment Slip</h1>
        <p>Generated on: %s</p>
    </div>

    <div class="payment-info">
        <div class="section">
            <h3>💳 Payment Information</h3>
            <div class="field">
                <span class="field-label">Amount:</span>
                <span class="field-value amount-highlight">%s EUR</span>
            </div>
            <div class="field">
                <span class="field-label">Reference:</span>
                <span class="field-value">%s</span>
            </div>
            <div class="field">
                <span class="field-label">Model:</span>
                <span class="field-value">%s</span>
            </div>
            <div class="field">
                <span class="field-label">Purpose:</span>
                <span class="field-value">%s</span>
            </div>
            <div class="field">
                <span class="field-label">Description:</span>
                <span class="field-value">%s</span>
            </div>
        </div>

        <div class="section">
            <h3>👤 Payer Information</h3>
            <div class="field">
                <span class="field-label">Name:</span>
                <span class="field-value">%s</span>
            </div>
            <div class="field">
                <span class="field-label">Address:</span>
                <span class="field-value">%s</span>
            </div>
            <div class="field">
                <span class="field-label">City:</span>
                <span class="field-value">%s</span>
            </div>
        </div>
    </div>

    <div class="section">
        <h3>🏢 Recipient Information</h3>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
            <div>
                <div class="field">
                    <span class="field-label">Company:</span>
                    <span class="field-value">%s</span>
                </div>
                <div class="field">
                    <span class="field-label">Address:</span>
                    <span class="field-value">%s</span>
                </div>
            </div>
            <div>
                <div class="field">
                    <span class="field-label">City:</span>
                    <span class="field-value">%s</span>
                </div>
                <div class="field">
                    <span class="field-label">IBAN:</span>
                    <span class="field-value">%s</span>
                </div>
            </div>
        </div>
    </div>

    <div class="barcode-section">
        <h3>📊 HUB-3 PDF417 Barcode</h3>
        <p>Scan this barcode with your banking app to make the payment</p>
        <img src="data:image/png;base64,%s" alt="HUB-3 PDF417 Barcode" class="barcode-image">
    </div>

    <div class="footer">
        <p><strong>HUB-3 Payment Standard</strong> | Croatian Banking Association</p>
        <p>This barcode contains all payment information in PDF417 format</p>
        <p>Currency: EUR | Bank Code: %s</p>
    </div>
</body>
</html>""",
                currentDate,
                amountField.getText().isEmpty() ? "0,00" : amountField.getText(),
                referenceField.getText(),
                modelField.getText(),
                purposeCodeField.getText(),
                descriptionField.getText().replace("\n", "<br>"),
                payerNameField.getText(),
                payerAddressField.getText(),
                payerCityField.getText(),
                recipientNameField.getText(),
                recipientAddressField.getText(),
                recipientCityField.getText(),
                ibanField.getText(),
                barcodeBase64,
                FIXED_BANK_CODE
        );
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
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

    // NEW: Public method to refresh payment attachment templates
    public void refreshPaymentTemplates() {
        initializePaymentAttachments();
    }

    // NEW: Public method to get current selected template
    public PaymentAttachment getCurrentTemplate() {
        return selectedTemplate;
    }

    // NEW: Public method to set template (for external calls)
    public void setTemplate(PaymentAttachment template) {
        if (template != null) {
            this.selectedTemplate = template;
            System.out.println("Template set to: " + template.getName());
        }
    }

    private String getUplatnicaBackgroundBase64() {
        try {
            // Load the image from resources
            InputStream imageStream = getClass().getResourceAsStream("/images/uplatnica.png");
            if (imageStream == null) {
                System.err.println("Uplatnica background image not found: /images/uplatnica.png");
                return "";
            }

            // Read the image into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = imageStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            imageStream.close();

            // Convert to base64
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            return base64Image; // Return just the base64 string, not with data: prefix

        } catch (IOException e) {
            System.err.println("Error loading uplatnica background image: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private void showFullTemplateInWebView(String htmlContent) {
        // Option 1: If you have a WebView in your FXML
        // webView.getEngine().loadContent(htmlContent);

        // Option 2: Create WebView programmatically and replace the ImageView
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        webView.setPrefSize(950, 400);
        webView.getEngine().loadContent(htmlContent);

        // Clear the container and add the WebView
        paymentSlipContainer.getChildren().clear();
        paymentSlipContainer.getChildren().add(webView);
    }

    // Fallback method (your original display)
    private void showOriginalBarcodeDisplay() {
        // Hide placeholder
        placeholderContent.setVisible(false);

        // Convert BufferedImage to JavaFX Image and display
        Image fxImage = SwingFXUtils.toFXImage(currentBarcodeImage, null);
        generatedBarcodeView.setImage(fxImage);
        generatedBarcodeView.setVisible(true);

        // Show action buttons
        actionButtonsContainer.setVisible(true);
    }
}