package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

public class BarcodeGeneratorViewController implements Initializable {

    // Form Fields - Banking Information
    @FXML private TextField bankCodeField;
    @FXML private ComboBox<String> currencyCombo;
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
    @FXML private TextField descriptionField;

    // Action Buttons
    @FXML private Button generateButton;
    @FXML private Button loadTemplateButton;
    @FXML private Button clearButton;
    @FXML private Button saveTemplateButton;

    // Display Controls
    @FXML private StackPane paymentSlipContainer;
    @FXML private VBox placeholderContent;
    @FXML private VBox generatedPaymentSlip;
    @FXML private HBox actionButtonsContainer;

    // Generated Slip Action Buttons
    @FXML private Button saveSlipButton;
    @FXML private Button printSlipButton;
    @FXML private Button copyDataButton;

    // Data
    private String currentPaymentData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BarcodeGeneratorViewController.initialize() called");

        setupInitialValues();
        setupEventHandlers();

        System.out.println("BarcodeGeneratorViewController initialized successfully");
    }

    private void setupInitialValues() {
        // Set up currency combo box
        currencyCombo.getItems().addAll("HRK", "EUR", "USD");
        currencyCombo.setValue("HRK");

        // Set default values
        bankCodeField.setText("HRVHUB30");
        modelField.setText("HR01");

        // Load example template by default
        loadExampleTemplate();
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        // Main action buttons
        generateButton.setOnAction(e -> handleGeneratePaymentSlip());
        loadTemplateButton.setOnAction(e -> handleLoadTemplate());
        clearButton.setOnAction(e -> handleClearAll());
        saveTemplateButton.setOnAction(e -> handleSaveTemplate());

        // Generated slip action buttons
        saveSlipButton.setOnAction(e -> handleSaveSlip());
        printSlipButton.setOnAction(e -> handlePrintSlip());
        copyDataButton.setOnAction(e -> handleCopyData());

        System.out.println("Event handlers setup completed");
    }

    private void loadExampleTemplate() {
        // Load the example data you provided
        bankCodeField.setText("HRVHUB30");
        currencyCombo.setValue("HRK");
        amountField.setText("000000000012355");
        referenceField.setText("7269-68949637676-00019");

        payerNameField.setText("ZELJKO SENEKOVIC");
        payerAddressField.setText("IVANECKA ULICA 125");
        payerCityField.setText("42000 VARAZDIN");

        recipientNameField.setText("2DBK d.d.");
        recipientAddressField.setText("ALKARSKI PROLAZ 13B");
        recipientCityField.setText("21230 SINJ");
        ibanField.setText("HR1210010051863000160");

        modelField.setText("HR01");
        purposeCodeField.setText("COST");
        descriptionField.setText("Troskovi za 1. mjesec");
    }

    private void handleGeneratePaymentSlip() {
        try {
            System.out.println("Generate payment slip clicked");

            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Generate the HUB-3 data string
            currentPaymentData = generateHUB3Data();

            // Generate payment slip visual
            generatePaymentSlipVisual();

            // Show the generated content
            showGeneratedContent();

            showSuccess("HUB-3 Payment Slip generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating payment slip: " + e.getMessage());
            e.printStackTrace();
            showAlert("Generation Error", "Failed to generate payment slip: " + e.getMessage(), Alert.AlertType.ERROR);
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
        // Generate HUB-3 format data string
        StringBuilder hub3Data = new StringBuilder();

        // Line 1: Bank code
        hub3Data.append(bankCodeField.getText().trim()).append("\n");

        // Line 2: Currency
        hub3Data.append(currencyCombo.getValue()).append("\n");

        // Line 3: Amount (15 digits, zero-padded)
        String amount = amountField.getText().trim();
        if (amount.length() < 15) {
            amount = String.format("%015d", Long.parseLong(amount.replaceAll("[^0-9]", "")));
        }
        hub3Data.append(amount).append("\n");

        // Line 4: Payer name
        hub3Data.append(payerNameField.getText().trim()).append("\n");

        // Line 5: Payer address
        hub3Data.append(payerAddressField.getText().trim()).append("\n");

        // Line 6: Payer city
        hub3Data.append(payerCityField.getText().trim()).append("\n");

        // Line 7: Recipient name
        hub3Data.append(recipientNameField.getText().trim()).append("\n");

        // Line 8: Recipient address
        hub3Data.append(recipientAddressField.getText().trim()).append("\n");

        // Line 9: Recipient city
        hub3Data.append(recipientCityField.getText().trim()).append("\n");

        // Line 10: IBAN
        hub3Data.append(ibanField.getText().trim()).append("\n");

        // Line 11: Model
        hub3Data.append(modelField.getText().trim()).append("\n");

        // Line 12: Reference
        hub3Data.append(referenceField.getText().trim()).append("\n");

        // Line 13: Purpose code
        hub3Data.append(purposeCodeField.getText().trim()).append("\n");

        // Line 14: Description
        hub3Data.append(descriptionField.getText().trim());

        return hub3Data.toString();
    }

    private void generatePaymentSlipVisual() {
        // Clear previous content
        generatedPaymentSlip.getChildren().clear();

        // Create payment slip layout
        VBox slipContent = new VBox(5);
        slipContent.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");

        // Header
        Label header = new Label("HRVATSKI STANDARD HUB-3");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1976d2;");
        slipContent.getChildren().add(header);

        // Add separator
        Separator sep1 = new Separator();
        slipContent.getChildren().add(sep1);

        // Payment information grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);
        infoGrid.setStyle("-fx-padding: 10;");

        int row = 0;

        // Banking info
        addInfoRow(infoGrid, "Bank Code:", bankCodeField.getText(), row++);
        addInfoRow(infoGrid, "Currency:", currencyCombo.getValue(), row++);
        addInfoRow(infoGrid, "Amount:", formatAmount(amountField.getText()), row++);
        addInfoRow(infoGrid, "Reference:", referenceField.getText(), row++);

        // Add separator
        Separator sep2 = new Separator();
        GridPane.setColumnSpan(sep2, 2);
        infoGrid.add(sep2, 0, row++);

        // Payer info
        Label payerHeader = new Label("PAYER INFORMATION:");
        payerHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        GridPane.setColumnSpan(payerHeader, 2);
        infoGrid.add(payerHeader, 0, row++);

        addInfoRow(infoGrid, "Name:", payerNameField.getText(), row++);
        addInfoRow(infoGrid, "Address:", payerAddressField.getText(), row++);
        addInfoRow(infoGrid, "City:", payerCityField.getText(), row++);

        // Add separator
        Separator sep3 = new Separator();
        GridPane.setColumnSpan(sep3, 2);
        infoGrid.add(sep3, 0, row++);

        // Recipient info
        Label recipientHeader = new Label("RECIPIENT INFORMATION:");
        recipientHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #f57c00;");
        GridPane.setColumnSpan(recipientHeader, 2);
        infoGrid.add(recipientHeader, 0, row++);

        addInfoRow(infoGrid, "Name:", recipientNameField.getText(), row++);
        addInfoRow(infoGrid, "Address:", recipientAddressField.getText(), row++);
        addInfoRow(infoGrid, "City:", recipientCityField.getText(), row++);
        addInfoRow(infoGrid, "IBAN:", ibanField.getText(), row++);

        // Add separator
        Separator sep4 = new Separator();
        GridPane.setColumnSpan(sep4, 2);
        infoGrid.add(sep4, 0, row++);

        // Payment details
        Label detailsHeader = new Label("PAYMENT DETAILS:");
        detailsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #c2185b;");
        GridPane.setColumnSpan(detailsHeader, 2);
        infoGrid.add(detailsHeader, 0, row++);

        addInfoRow(infoGrid, "Model:", modelField.getText(), row++);
        addInfoRow(infoGrid, "Purpose:", purposeCodeField.getText(), row++);
        addInfoRow(infoGrid, "Description:", descriptionField.getText(), row++);

        // Add timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        addInfoRow(infoGrid, "Generated:", timestamp, row++);

        slipContent.getChildren().add(infoGrid);

        // Add barcode representation (text-based)
        Label barcodeLabel = new Label("HUB-3 BARCODE DATA:");
        barcodeLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        slipContent.getChildren().add(barcodeLabel);

        TextArea barcodeArea = new TextArea(currentPaymentData);
        barcodeArea.setEditable(false);
        barcodeArea.setPrefRowCount(6);
        barcodeArea.setStyle("-fx-font-family: monospace; -fx-font-size: 10px; -fx-background-color: #f5f5f5;");
        slipContent.getChildren().add(barcodeArea);

        generatedPaymentSlip.getChildren().add(slipContent);
    }

    private void addInfoRow(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100px;");

        Label valueNode = new Label(value != null ? value : "");
        valueNode.setStyle("-fx-font-family: monospace;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private String formatAmount(String amount) {
        try {
            // Format amount with decimals
            if (amount != null && !amount.trim().isEmpty()) {
                String cleanAmount = amount.replaceAll("[^0-9]", "");
                if (cleanAmount.length() >= 2) {
                    String wholePart = cleanAmount.substring(0, cleanAmount.length() - 2);
                    String decimalPart = cleanAmount.substring(cleanAmount.length() - 2);
                    return wholePart + "." + decimalPart + " " + currencyCombo.getValue();
                }
            }
            return amount + " " + currencyCombo.getValue();
        } catch (Exception e) {
            return amount + " " + currencyCombo.getValue();
        }
    }

    private void showGeneratedContent() {
        // Hide placeholder, show generated content
        placeholderContent.setVisible(false);
        generatedPaymentSlip.setVisible(true);
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

                    // Load all fields from properties
                    bankCodeField.setText(props.getProperty("bankCode", ""));
                    currencyCombo.setValue(props.getProperty("currency", "HRK"));
                    amountField.setText(props.getProperty("amount", ""));
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

                // Save all fields to properties
                props.setProperty("bankCode", bankCodeField.getText());
                props.setProperty("currency", currencyCombo.getValue());
                props.setProperty("amount", amountField.getText());
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
                    props.store(fos, "HUB-3 Payment Template - " + LocalDateTime.now());
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
        bankCodeField.clear();
        currencyCombo.setValue("HRK");
        amountField.clear();
        referenceField.clear();

        payerNameField.clear();
        payerAddressField.clear();
        payerCityField.clear();

        recipientNameField.clear();
        recipientAddressField.clear();
        recipientCityField.clear();
        ibanField.clear();

        modelField.clear();
        purposeCodeField.clear();
        descriptionField.clear();

        // Reset display
        placeholderContent.setVisible(true);
        generatedPaymentSlip.setVisible(false);
        actionButtonsContainer.setVisible(false);

        // Set some defaults back
        bankCodeField.setText("HRVHUB30");
        modelField.setText("HR01");

        showSuccess("All fields cleared!");
    }

    private void handleSaveSlip() {
        if (currentPaymentData == null) {
            showAlert("No Data", "Please generate a payment slip first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Payment Slip");
            fileChooser.setInitialFileName("hub3_payment_slip_" + System.currentTimeMillis() + ".txt");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );

            Stage stage = (Stage) saveSlipButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("CROATIAN HUB-3 PAYMENT SLIP\n");
                    writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n");
                    writer.write("=====================================\n\n");
                    writer.write(currentPaymentData);
                    showSuccess("Payment slip saved successfully!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving slip: " + e.getMessage());
            e.printStackTrace();
            showAlert("Save Error", "Failed to save payment slip: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handlePrintSlip() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Print");
        info.setHeaderText("Print Payment Slip");
        info.setContentText("Print functionality will be implemented soon!");
        info.showAndWait();
    }

    private void handleCopyData() {
        if (currentPaymentData != null) {
            // Copy to system clipboard (simplified for JavaFX)
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(currentPaymentData);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

            showSuccess("Payment data copied to clipboard!");
        } else {
            showAlert("No Data", "Please generate a payment slip first.", Alert.AlertType.WARNING);
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
}