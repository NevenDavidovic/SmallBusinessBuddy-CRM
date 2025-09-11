package smallbusinessbuddycrm.controllers.underaged;

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
import smallbusinessbuddycrm.utilities.LanguageManager;

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

    //Translation FXML Fields
    @FXML private Label pageTitle;
    @FXML private Label amountLabel;
    @FXML private Label ibanLabel;
    @FXML private Label referenceLabel;
    @FXML private TitledPane additionalDetailsPane;
    @FXML private Label payerLabel;
    @FXML private Label recipientLabel;
    @FXML private Label modelLabel;
    @FXML private Label purposeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label paymentSlipTitle;
    @FXML private Label statusLabel;
    @FXML private Label placeholderMainText;
    @FXML private Label placeholderSubText;
    @FXML private Label tipsTitle;
    @FXML private Label tipsText1;
    @FXML private Label tipsText2;
    @FXML private Label hub3InfoTitle;
    @FXML private Label hub3InfoText1;
    @FXML private Label hub3InfoText2;

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

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up payment attachment templates, initial values, event handlers, and language listeners.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BarcodeGeneratorViewController.initialize() called");


        // NEW: Initialize payment attachment templates
        initializePaymentAttachments();

        setupInitialValues();
        setupEventHandlers();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh labels, buttons, and placeholders.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Page title and main labels
        if (pageTitle != null) {
            pageTitle.setText(languageManager.getText("barcode.generator.page.title"));
        }
        if (amountLabel != null) {
            amountLabel.setText(languageManager.getText("barcode.generator.amount.label"));
        }
        if (ibanLabel != null) {
            ibanLabel.setText(languageManager.getText("barcode.generator.iban.label"));
        }
        if (referenceLabel != null) {
            referenceLabel.setText(languageManager.getText("barcode.generator.reference.label"));
        }

        // Additional details section
        if (additionalDetailsPane != null) {
            additionalDetailsPane.setText(languageManager.getText("barcode.generator.additional.details"));
        }
        if (payerLabel != null) {
            payerLabel.setText(languageManager.getText("barcode.generator.payer.label"));
        }
        if (recipientLabel != null) {
            recipientLabel.setText(languageManager.getText("barcode.generator.recipient.label"));
        }
        if (modelLabel != null) {
            modelLabel.setText(languageManager.getText("barcode.generator.model.label"));
        }
        if (purposeLabel != null) {
            purposeLabel.setText(languageManager.getText("barcode.generator.purpose.label"));
        }
        if (descriptionLabel != null) {
            descriptionLabel.setText(languageManager.getText("barcode.generator.description.label"));
        }

        // Buttons
        if (generateButton != null) {
            generateButton.setText(languageManager.getText("barcode.generator.generate"));
        }
        if (clearButton != null) {
            clearButton.setText(languageManager.getText("barcode.generator.clear"));
        }
        if (loadTemplateButton != null) {
            loadTemplateButton.setText(languageManager.getText("barcode.generator.load"));
        }
        if (saveTemplateButton != null) {
            saveTemplateButton.setText(languageManager.getText("barcode.generator.save"));
        }
        if (saveSlipButton != null) {
            saveSlipButton.setText(languageManager.getText("barcode.generator.save.barcode"));
        }
        if (savePaymentSlipButton != null) {
            savePaymentSlipButton.setText(languageManager.getText("barcode.generator.save.slip"));
        }
        if (printSlipButton != null) {
            printSlipButton.setText(languageManager.getText("barcode.generator.print"));
        }
        if (copyDataButton != null) {
            copyDataButton.setText(languageManager.getText("barcode.generator.copy"));
        }

        // Form field placeholders
        if (amountField != null) {
            amountField.setPromptText(languageManager.getText("barcode.generator.amount.placeholder"));
        }
        if (ibanField != null) {
            ibanField.setPromptText(languageManager.getText("barcode.generator.iban.placeholder"));
        }
        if (referenceField != null) {
            referenceField.setPromptText(languageManager.getText("barcode.generator.reference.placeholder"));
        }
        if (payerNameField != null) {
            payerNameField.setPromptText(languageManager.getText("barcode.generator.payer.name.placeholder"));
        }
        if (payerAddressField != null) {
            payerAddressField.setPromptText(languageManager.getText("barcode.generator.payer.address.placeholder"));
        }
        if (payerCityField != null) {
            payerCityField.setPromptText(languageManager.getText("barcode.generator.payer.city.placeholder"));
        }
        if (recipientNameField != null) {
            recipientNameField.setPromptText(languageManager.getText("barcode.generator.recipient.name.placeholder"));
        }
        if (recipientAddressField != null) {
            recipientAddressField.setPromptText(languageManager.getText("barcode.generator.recipient.address.placeholder"));
        }
        if (recipientCityField != null) {
            recipientCityField.setPromptText(languageManager.getText("barcode.generator.recipient.city.placeholder"));
        }
        if (descriptionField != null) {
            descriptionField.setPromptText(languageManager.getText("barcode.generator.description.placeholder"));
        }

        // Display section
        if (paymentSlipTitle != null) {
            paymentSlipTitle.setText(languageManager.getText("barcode.generator.payment.slip.title"));
        }
        if (statusLabel != null) {
            statusLabel.setText(languageManager.getText("barcode.generator.status.ready"));
        }
        if (placeholderMainText != null) {
            placeholderMainText.setText(languageManager.getText("barcode.generator.placeholder.main"));
        }
        if (placeholderSubText != null) {
            placeholderSubText.setText(languageManager.getText("barcode.generator.placeholder.sub"));
        }

        // Tips section
        if (tipsTitle != null) {
            tipsTitle.setText(languageManager.getText("barcode.generator.tips.title"));
        }
        if (tipsText1 != null) {
            tipsText1.setText(languageManager.getText("barcode.generator.tips.text1"));
        }
        if (tipsText2 != null) {
            tipsText2.setText(languageManager.getText("barcode.generator.tips.text2"));
        }

        // HUB-3 info section
        if (hub3InfoTitle != null) {
            hub3InfoTitle.setText(languageManager.getText("barcode.generator.hub3.info.title"));
        }
        if (hub3InfoText1 != null) {
            hub3InfoText1.setText(languageManager.getText("barcode.generator.hub3.info.text1"));
        }
        if (hub3InfoText2 != null) {
            hub3InfoText2.setText(languageManager.getText("barcode.generator.hub3.info.text2"));
        }

        System.out.println("Barcode generator view texts updated");
    }

    /**
     * Initializes database connection and loads organization data.
     * Creates a default organization if none exists in the database.
     */
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

    /**
     * Initializes payment attachment template system.
     * Loads default template or falls back to first available template.
     */
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

    /**
     * Creates and saves a default organization to the database.
     * Used when no organization exists in the system.
     */
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

    /**
     * Sets up initial form values and loads organization data into recipient fields.
     * Sets default payment model to "HR01".
     */
    private void setupInitialValues() {
        // Set default values
        modelField.setText("HR01");

        // Load organization data into recipient fields
        loadOrganizationData();

        // Load example template by default

    }

    /**
     * Populates recipient form fields with current organization data.
     * Includes name, address, city, and IBAN information.
     */
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

    /**
     * Configures all UI event handlers for buttons and form interactions.
     * Sets up currency formatting and double-click refresh functionality.
     */
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

    /**
     * Configures real-time Croatian currency formatting for the amount field.
     * Formats input as 1.234,56 EUR and handles keyboard events.
     */
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

    /**
     * Formats a numeric string as Croatian currency.
     * Converts digits to format: 1.234,56 with thousand separators.
     *
     * @param digitsOnly String containing only numeric digits
     * @return Formatted currency string in Croatian format
     */
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

    /**
     * Converts display currency format back to cents for HUB-3 encoding.
     * Extracts numeric digits from formatted display text.
     *
     * @return String representing amount in cents for HUB-3 standard
     */
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


    /**
     * Handles the main barcode generation process.
     * Validates form data, generates HUB-3 string, creates PDF417 barcode, and displays result.
     */
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

            LanguageManager languageManager = LanguageManager.getInstance();
            showSuccess(languageManager.getText("barcode.generator.success.generated"));

        } catch (Exception e) {
            System.err.println("Error generating barcode: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.generation.error");
            showAlert(errorTitle, "Failed to generate barcode: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Validates required form fields before barcode generation.
     * Checks for payer name, recipient name, amount, and IBAN.
     *
     * @return true if all required fields are filled, false otherwise
     */
    private boolean validateFields() {
        LanguageManager languageManager = LanguageManager.getInstance();
        StringBuilder errors = new StringBuilder();

        if (payerNameField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("barcode.generator.error.payer.name")).append("\n");
        }
        if (recipientNameField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("barcode.generator.error.recipient.name")).append("\n");
        }
        if (amountField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("barcode.generator.error.amount")).append("\n");
        }
        if (ibanField.getText().trim().isEmpty()) {
            errors.append(languageManager.getText("barcode.generator.error.iban")).append("\n");
        }

        if (errors.length() > 0) {
            String errorTitle = languageManager.getText("barcode.generator.validation.error");
            String errorMessage = languageManager.getText("barcode.generator.error.fix.errors") + "\n\n" + errors.toString();
            showAlert(errorTitle, errorMessage, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Generates HUB-3 standard payment data string.
     * Creates 14-line format with fixed EUR currency and HRVHUB30 bank code.
     *
     * @return HUB-3 formatted payment data string
     */
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

    /**
     * Generates PDF417 barcode image from current payment data.
     *
     * @throws WriterException if barcode generation fails
     */
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

    /**
     * Creates PDF417 barcode using ZXing library.
     * Configured for HUB-3 standard with proper dimensions and error correction.
     *
     * @return BufferedImage containing the generated PDF417 barcode
     * @throws WriterException if barcode encoding fails
     */
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

    /**
     * Displays generated barcode in Croatian uplatnica template format.
     * Creates WebView with full payment slip layout and embedded barcode.
     */
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

    /**
     * Handles saving complete payment slip with template selection.
     * Shows template selection dialog and exports as PDF or HTML.
     */
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

    /**
     * Shows dialog for selecting payment attachment template.
     *
     * @return Selected PaymentAttachment template or null if cancelled
     */
    private PaymentAttachment showTemplateSelectionDialog() {
        try {
            List<PaymentAttachment> templates = paymentAttachmentDAO.findAll();

            if (templates.isEmpty()) {
                LanguageManager languageManager = LanguageManager.getInstance();
                String title = languageManager.getText("barcode.generator.no.templates");
                showAlert(title, "No payment templates found. Please create at least one template first.", Alert.AlertType.WARNING);
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
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.template.error");
            showAlert(errorTitle, "Failed to load templates: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        return null; // User cancelled or error occurred
    }

    /**
     * Saves payment slip as PDF using selected template.
     *
     * @param file Output file for PDF
     * @param template PaymentAttachment template to use
     * @throws Exception if PDF generation fails
     */
    private void savePaymentSlipAsPDF(File file, PaymentAttachment template) throws Exception {
        // Create HTML content using selected template
        String htmlContent = generatePaymentSlipHTML(template);

        // Convert HTML to PDF using iText
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
        }
    }

    /**
     * Saves payment slip as HTML using selected template.
     *
     * @param file Output file for HTML
     * @param template PaymentAttachment template to use
     * @throws Exception if HTML generation fails
     */
    private void savePaymentSlipAsHTML(File file, PaymentAttachment template) throws Exception {
        String htmlContent = generatePaymentSlipHTML(template);

        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
    }

    /**
     * Generates HTML content using selected payment attachment template.
     *
     * @param template PaymentAttachment template for processing
     * @return Processed HTML content with variables replaced
     * @throws Exception if template processing fails
     */
    private String generatePaymentSlipHTML(PaymentAttachment template) throws Exception {
        // Convert barcode image to base64 for embedding
        String barcodeBase64 = encodeImageToBase64(currentBarcodeImage);

        // Create variables map for template processing
        Map<String, String> variables = createVariableMap(barcodeBase64);

        // Process template variables
        return processTemplate(template.getHtmlContent(), variables);
    }


    /**
     * Creates variable map for template processing.
     * Maps all form field values and system data to template variables.
     *
     * @param barcodeBase64 Base64-encoded barcode image
     * @return Map of template variables and their values
     */
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

    /**
     * Returns hardcoded Croatian uplatnica HTML template.
     * Complete HTML template with CSS styling for official Croatian payment slip format.
     *
     * @return HTML template string with variable placeholders
     */
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


    /**
     * Processes HTML template by replacing variable placeholders.
     * Replaces all {{VARIABLE_NAME}} placeholders with actual values.
     *
     * @param htmlTemplate HTML template with variable placeholders
     * @param variables Map of variable names and values
     * @return Processed HTML with variables replaced
     */
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

    /**
     * Generates HTML using default fallback template.
     * Used when no payment attachment template is available.
     *
     * @return HTML content using hardcoded template
     * @throws Exception if HTML generation fails
     */
    private String generatePaymentSlipHTML() throws Exception {
        if (selectedTemplate == null) {
            // Fallback to hardcoded template if no template selected
            return generateFallbackHTML();
        }
        return generatePaymentSlipHTML(selectedTemplate);
    }

    /**
     * Creates basic HTML without template system.
     * Fallback method for backward compatibility.
     *
     * @return Basic HTML payment slip
     * @throws Exception if HTML generation fails
     */
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

    /**
     * Converts BufferedImage to Base64 encoded string.
     * Used for embedding images in HTML templates.
     *
     * @param image BufferedImage to encode
     * @return Base64 encoded image string
     * @throws Exception if image encoding fails
     */
    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Handles loading payment template from Properties file.
     * Opens file chooser and populates form fields with template data.
     */
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

                    LanguageManager languageManager = LanguageManager.getInstance();
                    showSuccess(languageManager.getText("barcode.generator.success.template.loaded"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading template: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.load.error");
            showAlert(errorTitle, "Failed to load template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles saving current form data as Properties template file.
     * Opens file chooser and saves all form field values.
     */
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
                    LanguageManager languageManager = LanguageManager.getInstance();
                    showSuccess(languageManager.getText("barcode.generator.success.template.saved"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving template: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.save.error");
            showAlert(errorTitle, "Failed to save template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Clears all form fields except recipient information.
     * Keeps organization data and reloads default values.
     */
    private void handleClearAll() {
        // Clear all fields
        amountField.clear();
        referenceField.clear();

        payerNameField.clear();
        payerAddressField.clear();
        payerCityField.clear();

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

    /**
     * Handles saving barcode image as PNG or JPEG file.
     * Opens file chooser for image export.
     */
    private void handleSaveBarcode() {
        if (currentBarcodeImage == null) {
            LanguageManager languageManager = LanguageManager.getInstance();
            String title = languageManager.getText("barcode.generator.no.barcode");
            String message = languageManager.getText("barcode.generator.generate.first");
            showAlert(title, message, Alert.AlertType.WARNING);
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
                LanguageManager languageManager = LanguageManager.getInstance();
                showSuccess(languageManager.getText("barcode.generator.success.barcode.saved"));
            }
        } catch (Exception e) {
            System.err.println("Error saving barcode: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.save.error");
            showAlert(errorTitle, "Failed to save barcode: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Placeholder method for print functionality.
     * Shows information dialog about future implementation.
     */
    private void handlePrintBarcode() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Print");
        info.setHeaderText("Print Barcode");
        info.setContentText("Print functionality will be implemented soon!");
        info.showAndWait();
    }

    /**
     * Copies HUB-3 payment data string to system clipboard.
     * Allows users to paste payment data elsewhere.
     */
    private void handleCopyData() {
        if (currentPaymentData != null) {
            // Copy to system clipboard
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(currentPaymentData);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

            LanguageManager languageManager = LanguageManager.getInstance();
            showSuccess(languageManager.getText("barcode.generator.success.data.copied"));
        } else {
            LanguageManager languageManager = LanguageManager.getInstance();
            String title = languageManager.getText("barcode.generator.no.data");
            String message = languageManager.getText("barcode.generator.generate.first");
            showAlert(title, message, Alert.AlertType.WARNING);
        }
    }

    /**
     * Displays alert dialog with specified title, message, and type.
     *
     * @param title Dialog title
     * @param message Dialog message
     * @param type Alert type (ERROR, WARNING, INFORMATION)
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows auto-closing success notification.
     * Dialog automatically closes after 2 seconds.
     *
     * @param message Success message to display
     */
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

    /**
     * Refreshes organization data from database.
     * Reloads current organization and updates recipient fields.
     */
    private void refreshOrganizationData() {
        try {
            if (organizationDAO != null) {
                Optional<Organization> orgOptional = organizationDAO.getFirst();
                if (orgOptional.isPresent()) {
                    currentOrganization = orgOptional.get();
                    loadOrganizationData();
                    LanguageManager languageManager = LanguageManager.getInstance();
                    showSuccess(languageManager.getText("barcode.generator.success.org.refreshed"));
                } else {
                    LanguageManager languageManager = LanguageManager.getInstance();
                    String title = languageManager.getText("barcode.generator.no.organization");
                    showAlert(title, "No organization found in database.", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            System.err.println("Error refreshing organization data: " + e.getMessage());
            e.printStackTrace();
            LanguageManager languageManager = LanguageManager.getInstance();
            String errorTitle = languageManager.getText("barcode.generator.database.error");
            showAlert(errorTitle, "Failed to refresh organization data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Returns formatted organization information string.
     * Used for debugging and information display.
     *
     * @return Formatted organization details
     */
    public String getOrganizationInfo() {
        if (currentOrganization != null) {
            return String.format("Organization: %s | IBAN: %s | Address: %s",
                    currentOrganization.getName(),
                    currentOrganization.getIban(),
                    currentOrganization.getFullAddress());
        }
        return "No organization loaded";
    }

    /**
     * Refreshes payment attachment templates from database.
     * Public method for external calls to reload templates.
     */
    public void refreshPaymentTemplates() {
        initializePaymentAttachments();
    }

    /**
     * Returns currently selected payment attachment template.
     *
     * @return Current PaymentAttachment template
     */
    public PaymentAttachment getCurrentTemplate() {
        return selectedTemplate;
    }

    /**
     * Sets the active payment attachment template.
     *
     * @param template PaymentAttachment template to set as active
     */
    public void setTemplate(PaymentAttachment template) {
        if (template != null) {
            this.selectedTemplate = template;
            System.out.println("Template set to: " + template.getName());
        }
    }

    /**
     * Loads and encodes Croatian uplatnica background image to Base64.
     * Loads image from resources and converts for HTML embedding.
     *
     * @return Base64 encoded background image string
     */
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

    /**
     * Displays HTML content in WebView component.
     * Creates and configures WebView for template display.
     *
     * @param htmlContent HTML content to display
     */
    private void showFullTemplateInWebView(String htmlContent) {

        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        webView.setPrefSize(950, 400);
        webView.getEngine().loadContent(htmlContent);

        // Clear the container and add the WebView
        paymentSlipContainer.getChildren().clear();
        paymentSlipContainer.getChildren().add(webView);
    }

    /**
     * Fallback method to display barcode in simple ImageView format.
     * Used when WebView template display fails.
     */
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