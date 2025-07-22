package smallbusinessbuddycrm.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

// Barcode generation imports - ZXing library for PDF417
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class BarcodePaymentDialog {
    private Stage dialog;
    private Contact contact;
    private ComboBox<PaymentTemplate> paymentTemplateCombo;
    private TextArea barcodeTextArea;
    private ImageView barcodeImageView;
    private Label paymentInfoLabel;
    private VBox placeholderContent;
    private HBox actionButtonsContainer;
    private TextField amountField;
    private WebView htmlWebView;

    // Current data
    private String currentHUB3Data;
    private BufferedImage currentBarcodeImage;
    private PaymentTemplate selectedTemplate;
    private Organization organization;

    // Constants
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public BarcodePaymentDialog(Stage parentStage, Contact contact) {
        this.contact = contact;
        loadOrganizationData();
        createDialog(parentStage);
    }

    private void loadOrganizationData() {
        try {
            OrganizationDAO organizationDAO = new OrganizationDAO();
            Optional<Organization> orgOptional = organizationDAO.getFirst();
            if (orgOptional.isPresent()) {
                organization = orgOptional.get();
                System.out.println("Loaded organization: " + organization.getName());
            } else {
                System.out.println("No organization found in database");
                createDefaultOrganization();
            }
        } catch (Exception e) {
            System.err.println("Error loading organization: " + e.getMessage());
            e.printStackTrace();
            createDefaultOrganization();
        }
    }

    private void createDefaultOrganization() {
        organization = new Organization();
        organization.setName("Your Company Name");
        organization.setIban("HR1234567890123456789");
        organization.setStreetName("Your Street");
        organization.setStreetNum("123");
        organization.setPostalCode("10000");
        organization.setCity("Zagreb");
        organization.setEmail("info@yourcompany.com");
        organization.setPhoneNum("+385 1 234 5678");
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("🇭🇷 Croatian HUB-3 Payment Barcode - " + contact.getFirstName() + " " + contact.getLastName());
        dialog.setResizable(true);

        // Create main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("🇭🇷 Croatian HUB-3 Payment Barcode Generator");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Two-column layout
        HBox twoColumnLayout = new HBox(20);

        // Left column
        VBox leftColumn = createLeftColumn();
        leftColumn.setPrefWidth(350);
        leftColumn.setMinWidth(350);
        leftColumn.setMaxWidth(350);

        // Right column
        VBox rightColumn = createRightColumn();
        rightColumn.setPrefWidth(350);
        rightColumn.setMinWidth(350);
        rightColumn.setMaxWidth(350);

        twoColumnLayout.getChildren().addAll(leftColumn, rightColumn);

        // Barcode display section
        VBox barcodeSection = createBarcodeSection();

        // Buttons
        HBox buttonBox = createButtonSection();

        mainLayout.getChildren().addAll(
                titleLabel,
                twoColumnLayout,
                new Separator(),
                barcodeSection,
                buttonBox
        );

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1000, 700);
        dialog.setScene(scene);

        // Load payment templates
        loadPaymentTemplates();
    }

    private VBox createLeftColumn() {
        VBox leftColumn = new VBox(15);

        // Contact info section
        VBox contactInfoBox = new VBox(10);
        contactInfoBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fff8;");

        Label contactTitle = new Label("👤 Payer Information (Contact)");
        contactTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactTitle.setStyle("-fx-text-fill: #2e7d32;");

        Label nameLabel = new Label("Name: " + contact.getFirstName() + " " + contact.getLastName());
        Label emailLabel = new Label("Email: " + (contact.getEmail() != null ? contact.getEmail() : "N/A"));
        Label phoneLabel = new Label("Phone: " + (contact.getPhoneNum() != null ? contact.getPhoneNum() : "N/A"));

        // Build address
        StringBuilder address = new StringBuilder();
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            address.append(contact.getStreetName());
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                address.append(" ").append(contact.getStreetNum());
            }
        }

        String cityInfo = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            cityInfo = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                cityInfo += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            cityInfo = contact.getCity();
        }

        Label addressLabel = new Label("Address: " + (address.length() > 0 ? address.toString() : "N/A"));
        Label cityLabel = new Label("City: " + (!cityInfo.isEmpty() ? cityInfo : "N/A"));

        contactInfoBox.getChildren().addAll(contactTitle, nameLabel, emailLabel, phoneLabel, addressLabel, cityLabel);

        // Payment template selection
        VBox paymentTemplateBox = new VBox(10);
        paymentTemplateBox.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

        Label templateTitle = new Label("💳 Payment Template");
        templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        templateTitle.setStyle("-fx-text-fill: #1976d2;");

        paymentTemplateCombo = new ComboBox<>();
        paymentTemplateCombo.setPrefWidth(300);
        paymentTemplateCombo.setPromptText("Choose a payment template...");
        paymentTemplateCombo.setOnAction(e -> handleTemplateSelection());

        paymentTemplateBox.getChildren().addAll(templateTitle, paymentTemplateCombo);

        // Amount input section
        VBox amountInputBox = createAmountInputSection();

        // Generate button
        VBox generateButtonBox = new VBox(10);
        generateButtonBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f0fff0;");

        Button generateButton = new Button("Generate Croatian Uplatnica");
        generateButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-pref-width: 300;");
        generateButton.setOnAction(e -> generateHUB3Barcode());

        generateButtonBox.getChildren().add(generateButton);

        leftColumn.getChildren().addAll(contactInfoBox, paymentTemplateBox, amountInputBox, generateButtonBox);

        return leftColumn;
    }

    private VBox createAmountInputSection() {
        VBox amountBox = new VBox(10);
        amountBox.setStyle("-fx-border-color: #e8f4fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fcff;");

        Label amountTitle = new Label("💰 Payment Amount");
        amountTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        amountTitle.setStyle("-fx-text-fill: #1565c0;");

        amountField = new TextField();
        amountField.setPromptText("0,00");
        amountField.setPrefWidth(200);
        amountField.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Setup currency formatting
        setupCurrencyFormatting();

        Label currencyLabel = new Label("EUR");
        currencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

        HBox amountContainer = new HBox(10);
        amountContainer.setAlignment(Pos.CENTER_LEFT);
        amountContainer.getChildren().addAll(amountField, currencyLabel);

        Label instructionLabel = new Label("Override template amount (optional)");
        instructionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        amountBox.getChildren().addAll(amountTitle, amountContainer, instructionLabel);
        return amountBox;
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

    private VBox createRightColumn() {
        VBox rightColumn = new VBox(15);

        // Organization info section
        VBox orgInfoBox = new VBox(10);
        orgInfoBox.setStyle("-fx-border-color: #fff3e0; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fffbf5;");

        Label orgTitle = new Label("🏢 Recipient Information (Organization)");
        orgTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        orgTitle.setStyle("-fx-text-fill: #f57c00;");

        Label orgNameLabel = new Label("Company: " + (organization != null ? organization.getName() : "N/A"));
        Label orgIbanLabel = new Label("IBAN: " + (organization != null ? organization.getIban() : "N/A"));

        String orgAddress = "";
        String orgCity = "";
        if (organization != null) {
            StringBuilder addr = new StringBuilder();
            if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
                addr.append(organization.getStreetName());
                if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                    addr.append(" ").append(organization.getStreetNum());
                }
            }
            orgAddress = addr.toString();

            if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
                orgCity = organization.getPostalCode();
                if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                    orgCity += " " + organization.getCity();
                }
            } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                orgCity = organization.getCity();
            }
        }

        Label orgAddressLabel = new Label("Address: " + (!orgAddress.isEmpty() ? orgAddress : "N/A"));
        Label orgCityLabel = new Label("City: " + (!orgCity.isEmpty() ? orgCity : "N/A"));

        orgInfoBox.getChildren().addAll(orgTitle, orgNameLabel, orgIbanLabel, orgAddressLabel, orgCityLabel);

        // Payment details display
        VBox paymentDetailsBox = new VBox(10);
        paymentDetailsBox.setStyle("-fx-border-color: #fce4ec; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fef7f7;");

        Label detailsTitle = new Label("📋 Payment Details");
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsTitle.setStyle("-fx-text-fill: #c2185b;");

        paymentInfoLabel = new Label("Select a payment template to see details...");
        paymentInfoLabel.setWrapText(true);

        paymentDetailsBox.getChildren().addAll(detailsTitle, paymentInfoLabel);

        rightColumn.getChildren().addAll(orgInfoBox, paymentDetailsBox);

        return rightColumn;
    }

    private VBox createBarcodeSection() {
        VBox barcodeSection = new VBox(15);

        Label barcodeTitle = new Label("Generated Croatian Uplatnica");
        barcodeTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        barcodeTitle.setStyle("-fx-text-fill: #333;");

        // Create tab pane for different views
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(420);

        // Uplatnica HTML view tab
        Tab htmlTab = new Tab("🇭🇷 Uplatnica Preview");
        htmlTab.setClosable(false);

        htmlWebView = new WebView();
        htmlWebView.setPrefHeight(380);
        htmlWebView.setPrefWidth(931);

        // Create container for WebView with placeholder
        StackPane htmlContainer = new StackPane();
        htmlContainer.setPrefHeight(380);
        htmlContainer.setPrefWidth(931);
        htmlContainer.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #f8f9fa;");

        // Placeholder content for HTML tab
        VBox htmlPlaceholder = new VBox(10);
        htmlPlaceholder.setAlignment(Pos.CENTER);
        Label htmlPlaceholderIcon = new Label("🇭🇷");
        htmlPlaceholderIcon.setFont(Font.font(48));
        htmlPlaceholderIcon.setStyle("-fx-text-fill: #6c757d;");

        Label htmlPlaceholderText1 = new Label("Croatian Uplatnica will appear here");
        htmlPlaceholderText1.setFont(Font.font(14));
        htmlPlaceholderText1.setStyle("-fx-text-fill: #6c757d;");

        Label htmlPlaceholderText2 = new Label("Select a payment template and click 'Generate Croatian Uplatnica'");
        htmlPlaceholderText2.setFont(Font.font(12));
        htmlPlaceholderText2.setStyle("-fx-text-fill: #adb5bd;");

        htmlPlaceholder.getChildren().addAll(htmlPlaceholderIcon, htmlPlaceholderText1, htmlPlaceholderText2);

        htmlWebView.setVisible(false);
        htmlContainer.getChildren().addAll(htmlPlaceholder, htmlWebView);
        htmlTab.setContent(htmlContainer);

        // Barcode view tab
        Tab barcodeTab = new Tab("📊 Barcode Data");
        barcodeTab.setClosable(false);

        // Barcode display container
        StackPane barcodeContainer = new StackPane();
        barcodeContainer.setPrefHeight(250);
        barcodeContainer.setPrefWidth(700);
        barcodeContainer.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #f8f9fa;");

        // Placeholder content
        placeholderContent = new VBox(10);
        placeholderContent.setAlignment(Pos.CENTER);
        Label placeholderIcon = new Label("📊");
        placeholderIcon.setFont(Font.font(48));
        placeholderIcon.setStyle("-fx-text-fill: #6c757d;");

        Label placeholderText1 = new Label("HUB-3 PDF417 Barcode will appear here");
        placeholderText1.setFont(Font.font(14));
        placeholderText1.setStyle("-fx-text-fill: #6c757d;");

        Label placeholderText2 = new Label("Generate uplatnica to see the barcode data");
        placeholderText2.setFont(Font.font(12));
        placeholderText2.setStyle("-fx-text-fill: #adb5bd;");

        placeholderContent.getChildren().addAll(placeholderIcon, placeholderText1, placeholderText2);

        // Barcode image view
        barcodeImageView = new ImageView();
        barcodeImageView.setPreserveRatio(true);
        barcodeImageView.setFitWidth(680);
        barcodeImageView.setFitHeight(230);
        barcodeImageView.setVisible(false);

        barcodeContainer.getChildren().addAll(placeholderContent, barcodeImageView);

        // HUB-3 data text area for barcode tab
        barcodeTextArea = new TextArea();
        barcodeTextArea.setPrefRowCount(6);
        barcodeTextArea.setWrapText(true);
        barcodeTextArea.setEditable(false);
        barcodeTextArea.setPromptText("HUB-3 data will appear here after generation...");
        barcodeTextArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 10px;");

        VBox barcodeTabContent = new VBox(10);
        barcodeTabContent.getChildren().addAll(barcodeContainer, barcodeTextArea);
        barcodeTab.setContent(barcodeTabContent);

        tabPane.getTabs().addAll(htmlTab, barcodeTab);

        // Action buttons for generated content
        actionButtonsContainer = new HBox(10);
        actionButtonsContainer.setAlignment(Pos.CENTER);
        actionButtonsContainer.setVisible(false);

        Button saveHtmlButton = new Button("Save Uplatnica HTML");
        saveHtmlButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4;");
        saveHtmlButton.setOnAction(e -> saveUplatnicaHtml());

        Button saveImageButton = new Button("Save Barcode Image");
        saveImageButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        saveImageButton.setOnAction(e -> saveBarcodeImage());

        Button copyDataButton = new Button("Copy HUB-3 Data");
        copyDataButton.setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white; -fx-border-radius: 4;");
        copyDataButton.setOnAction(e -> copyHUB3Data());

        Button printButton = new Button("Print Uplatnica");
        printButton.setStyle("-fx-background-color: #0099cc; -fx-text-fill: white; -fx-border-radius: 4;");
        printButton.setOnAction(e -> printUplatnica());

        actionButtonsContainer.getChildren().addAll(saveHtmlButton, saveImageButton, copyDataButton, printButton);

        barcodeSection.getChildren().addAll(barcodeTitle, tabPane, actionButtonsContainer);

        return barcodeSection;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20;");
        closeButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().add(closeButton);

        return buttonBox;
    }

    private void loadPaymentTemplates() {
        try {
            System.out.println("DEBUG: Loading payment templates...");
            PaymentTemplateDAO dao = new PaymentTemplateDAO();

            List<PaymentTemplate> templates = dao.getActivePaymentTemplates();
            System.out.println("DEBUG: getActivePaymentTemplates() returned " + templates.size() + " templates");

            paymentTemplateCombo.getItems().clear();
            paymentTemplateCombo.getItems().addAll(templates);

            if (templates.isEmpty()) {
                paymentInfoLabel.setText("No active payment templates found. Please create a payment template first.");
                paymentInfoLabel.setStyle("-fx-text-fill: #856404;");
            }

        } catch (Exception e) {
            System.err.println("Error loading payment templates: " + e.getMessage());
            e.printStackTrace();

            paymentInfoLabel.setText("Error loading payment templates: " + e.getMessage());
            paymentInfoLabel.setStyle("-fx-text-fill: #721c24;");
        }
    }

    private void handleTemplateSelection() {
        selectedTemplate = paymentTemplateCombo.getSelectionModel().getSelectedItem();
        if (selectedTemplate != null) {
            StringBuilder info = new StringBuilder();
            info.append("Template: ").append(selectedTemplate.getName()).append("\n");

            // Process the description template and show the result
            String processedDescription = processDescriptionTemplate(selectedTemplate.getDescription());
            info.append("Description: ").append(processedDescription).append("\n");

            // Use amount from field if available, otherwise use template amount
            java.math.BigDecimal displayAmount;
            if (amountField != null && !amountField.getText().isEmpty()) {
                String digitsOnly = amountField.getText().replaceAll("[^0-9]", "");
                if (!digitsOnly.isEmpty()) {
                    displayAmount = new java.math.BigDecimal(digitsOnly).divide(new java.math.BigDecimal("100"));
                } else {
                    displayAmount = selectedTemplate.getAmount();
                }
            } else {
                displayAmount = selectedTemplate.getAmount();
                // Set the template amount in the field
                if (amountField != null && amountField.getText().isEmpty()) {
                    String templateAmountCents = selectedTemplate.getAmount().multiply(new java.math.BigDecimal("100")).toBigInteger().toString();
                    String formatted = formatCurrency(templateAmountCents);
                    amountField.setText(formatted);
                }
            }

            info.append("Amount: ").append(displayAmount).append(" EUR\n");
            info.append("Model of Payment: ").append(selectedTemplate.getModelOfPayment()).append("\n");

            // Generate reference number
            String reference = selectedTemplate.getPozivNaBroj();
            if (reference == null || reference.trim().isEmpty()) {
                reference = generatePozivNaBroj(contact.getId());
            } else {
                reference = reference.replace("{contact_id}", String.valueOf(contact.getId()));
            }
            info.append("Reference Number: ").append(reference);

            paymentInfoLabel.setText(info.toString());
            paymentInfoLabel.setStyle("-fx-text-fill: #333;");
        }
    }

    private String processDescriptionTemplate(String template) {
        if (template == null || template.trim().isEmpty()) {
            return "N/A";
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());
            String placeholder = matcher.group(1);
            String replacement = processPlaceholder(placeholder);
            result.append(replacement);
            lastEnd = matcher.end();
        }

        result.append(template.substring(lastEnd));
        return result.toString().trim();
    }

    private String processPlaceholder(String placeholder) {
        try {
            if (placeholder.startsWith("contact_attributes.")) {
                String attribute = placeholder.substring("contact_attributes.".length());
                return getContactAttribute(attribute);
            } else if (placeholder.startsWith("underaged_attributes.")) {
                String attribute = placeholder.substring("underaged_attributes.".length());
                return getUnderagedAttribute(attribute);
            } else if (placeholder.startsWith("custom_text.")) {
                String text = placeholder.substring("custom_text.".length());
                return text;
            }
        } catch (Exception e) {
            System.err.println("Error processing placeholder '" + placeholder + "': " + e.getMessage());
        }

        return "";
    }

    private String getContactAttribute(String attribute) {
        if (contact == null) return "";

        switch (attribute) {
            case "first_name": return contact.getFirstName() != null ? contact.getFirstName() : "";
            case "last_name": return contact.getLastName() != null ? contact.getLastName() : "";
            case "email": return contact.getEmail() != null ? contact.getEmail() : "";
            case "phone_num": return contact.getPhoneNum() != null ? contact.getPhoneNum() : "";
            case "birthday": return contact.getBirthday() != null ? contact.getBirthday().toString() : "";
            case "pin": return contact.getPin() != null ? contact.getPin() : "";
            case "street_name": return contact.getStreetName() != null ? contact.getStreetName() : "";
            case "street_num": return contact.getStreetNum() != null ? contact.getStreetNum() : "";
            case "postal_code": return contact.getPostalCode() != null ? contact.getPostalCode() : "";
            case "city": return contact.getCity() != null ? contact.getCity() : "";
            case "member_since": return contact.getMemberSince() != null ? contact.getMemberSince().toString() : "";
            case "member_until": return contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : "";
            default: return "";
        }
    }

    private String getUnderagedAttribute(String attribute) {
        if (contact == null) return "";

        try {
            UnderagedDAO underagedDAO = new UnderagedDAO();
            List<UnderagedMember> underagedList = underagedDAO.getUnderagedMembersByContactId(contact.getId());

            if (underagedList.isEmpty()) {
                return "";
            }

            UnderagedMember underaged = underagedList.get(0);

            switch (attribute) {
                case "first_name": return underaged.getFirstName() != null ? underaged.getFirstName() : "";
                case "last_name": return underaged.getLastName() != null ? underaged.getLastName() : "";
                case "birth_date": return underaged.getBirthDate() != null ? underaged.getBirthDate().toString() : "";
                case "age": return String.valueOf(underaged.getAge());
                case "pin": return underaged.getPin() != null ? underaged.getPin() : "";
                case "gender": return underaged.getGender() != null ? underaged.getGender() : "";
                case "is_member": return underaged.isMember() ? "Yes" : "No";
                case "member_since": return underaged.getMemberSince() != null ? underaged.getMemberSince().toString() : "";
                case "member_until": return underaged.getMemberUntil() != null ? underaged.getMemberUntil().toString() : "";
                case "note": return underaged.getNote() != null ? underaged.getNote() : "";
                default: return "";
            }
        } catch (Exception e) {
            System.err.println("Error getting underaged attribute '" + attribute + "': " + e.getMessage());
            return "";
        }
    }

    private void generateHUB3Barcode() {
        if (selectedTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "No Template Selected", "Please select a payment template first.");
            return;
        }

        if (organization == null) {
            showAlert(Alert.AlertType.ERROR, "No Organization", "Organization data is required for uplatnica generation.");
            return;
        }

        try {
            // Generate HUB-3 data string
            currentHUB3Data = generateHUB3Data();

            // Generate PDF417 barcode image
            generateBarcodeImage();

            // Display the barcode and HTML
            showGeneratedContent();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Croatian uplatnica generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating uplatnica: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Generation Failed", "Failed to generate uplatnica: " + e.getMessage());
        }
    }

    private String generateHUB3Data() {
        StringBuilder hub3Data = new StringBuilder();

        // 1. Bank code (hardcoded)
        hub3Data.append(FIXED_BANK_CODE).append("\n");

        // 2. Currency (hardcoded to EUR)
        hub3Data.append(FIXED_CURRENCY).append("\n");

        // 3. Transaction amount (in cents, 15 digits) - Use amount from field if available
        String amountCents;
        if (amountField != null && !amountField.getText().isEmpty()) {
            amountCents = getCurrencyValueForHUB3();
        } else {
            amountCents = selectedTemplate.getAmount().multiply(new java.math.BigDecimal("100")).toBigInteger().toString();
        }
        hub3Data.append(String.format("%015d", Long.parseLong(amountCents))).append("\n");

        // 4. Payer name
        hub3Data.append(contact.getFirstName() + " " + contact.getLastName()).append("\n");

        // 5. Payer address
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        hub3Data.append(payerAddress).append("\n");

        // 6. Payer postal code and city
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }
        hub3Data.append(payerCity).append("\n");

        // 7. Recipient name
        hub3Data.append(organization.getName()).append("\n");

        // 8. Recipient address
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        hub3Data.append(recipientAddress).append("\n");

        // 9. Recipient postal code and city
        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }
        hub3Data.append(recipientCity).append("\n");

        // 10. Recipient IBAN
        hub3Data.append(organization.getIban()).append("\n");

        // 11. Payment model
        hub3Data.append(selectedTemplate.getModelOfPayment() != null ? selectedTemplate.getModelOfPayment() : "").append("\n");

        // 12. Reference number
        String reference = selectedTemplate.getPozivNaBroj();
        if (reference == null || reference.trim().isEmpty()) {
            reference = generatePozivNaBroj(contact.getId());
        } else {
            reference = reference.replace("{contact_id}", String.valueOf(contact.getId()));
        }
        hub3Data.append(reference).append("\n");

        // 13. Purpose code
        hub3Data.append("").append("\n"); // Usually empty

        // 14. Payment description (no newline at the end) - Process the template
        String processedDescription = processDescriptionTemplate(selectedTemplate.getDescription());
        hub3Data.append(processedDescription);

        System.out.println("Generated HUB-3 data:");
        System.out.println(hub3Data.toString());

        return hub3Data.toString();
    }

    private void generateBarcodeImage() throws WriterException {
        PDF417Writer writer = new PDF417Writer();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, 2);
        hints.put(EncodeHintType.PDF417_COMPACT, false);
        hints.put(EncodeHintType.MARGIN, 10);

        BitMatrix bitMatrix = writer.encode(
                currentHUB3Data,
                BarcodeFormat.PDF_417,
                680,  // Width
                220,  // Height
                hints
        );

        currentBarcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private String generateUplatnicaHtml() {
        String htmlTemplate = """
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
                        background-image: url('https://raw.githubusercontent.com/NevenDavidovic/generator_uplatnica_dnd/refs/heads/main/jojo_crm/src/assets/images/uplatnica.png');
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
            </html>
            """;

        // Replace placeholders with actual data
        String processedHtml = htmlTemplate;

        // Payer information
        String payerName = contact.getFirstName() + " " + contact.getLastName();
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }

        // Recipient information
        String recipientName = organization.getName();
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }

        // Amount formatting
        String amount = "";
        if (amountField != null && !amountField.getText().isEmpty()) {
            amount = amountField.getText();
        } else if (selectedTemplate != null) {
            String templateAmountCents = selectedTemplate.getAmount().multiply(new java.math.BigDecimal("100")).toBigInteger().toString();
            amount = formatCurrency(templateAmountCents);
        }

        // Reference number
        String reference = "";
        if (selectedTemplate != null) {
            reference = selectedTemplate.getPozivNaBroj();
            if (reference == null || reference.trim().isEmpty()) {
                reference = generatePozivNaBroj(contact.getId());
            } else {
                reference = reference.replace("{contact_id}", String.valueOf(contact.getId()));
            }
        }

        // Description
        String description = "";
        if (selectedTemplate != null) {
            description = processDescriptionTemplate(selectedTemplate.getDescription());
        }

        // Convert barcode to base64
        String barcodeBase64 = "";
        if (currentBarcodeImage != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(currentBarcodeImage, "png", baos);
                byte[] bytes = baos.toByteArray();
                barcodeBase64 = Base64.getEncoder().encodeToString(bytes);
            } catch (Exception e) {
                System.err.println("Error converting barcode to base64: " + e.getMessage());
            }
        }

        // Replace all placeholders
        processedHtml = processedHtml.replace("{{PAYER_NAME}}", payerName);
        processedHtml = processedHtml.replace("{{PAYER_ADDRESS}}", payerAddress);
        processedHtml = processedHtml.replace("{{PAYER_CITY}}", payerCity);
        processedHtml = processedHtml.replace("{{AMOUNT}}", amount);
        processedHtml = processedHtml.replace("{{RECIPIENT_IBAN}}", organization.getIban());
        processedHtml = processedHtml.replace("{{RECIPIENT_NAME}}", recipientName);
        processedHtml = processedHtml.replace("{{RECIPIENT_ADDRESS}}", recipientAddress);
        processedHtml = processedHtml.replace("{{RECIPIENT_CITY}}", recipientCity);
        processedHtml = processedHtml.replace("{{MODEL}}", selectedTemplate != null ? selectedTemplate.getModelOfPayment() : "");
        processedHtml = processedHtml.replace("{{REFERENCE}}", reference);
        processedHtml = processedHtml.replace("{{PURPOSE}}", ""); // Usually empty
        processedHtml = processedHtml.replace("{{DESCRIPTION}}", description);
        processedHtml = processedHtml.replace("{{BARCODE_BASE64}}", barcodeBase64);

        return processedHtml;
    }

    private void showGeneratedContent() {
        // Hide barcode placeholder
        placeholderContent.setVisible(false);

        // Convert BufferedImage to JavaFX Image and display
        Image fxImage = SwingFXUtils.toFXImage(currentBarcodeImage, null);
        barcodeImageView.setImage(fxImage);
        barcodeImageView.setVisible(true);

        // Generate and display HTML uplatnica
        String htmlContent = generateUplatnicaHtml();
        htmlWebView.getEngine().loadContent(htmlContent);
        htmlWebView.setVisible(true);

        // Show action buttons
        actionButtonsContainer.setVisible(true);

        // Display HUB-3 data in text area
        barcodeTextArea.setText("=== HUB-3 PDF417 BARCODE DATA ===\n\n" + currentHUB3Data);
    }

    private String generatePozivNaBroj(int contactId) {
        String baseNumber = String.format("%010d", contactId);
        int checkDigit = calculateMod11CheckDigit(baseNumber);
        return baseNumber + checkDigit;
    }

    private int calculateMod11CheckDigit(String number) {
        int sum = 0;
        int weight = 2;

        for (int i = number.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(number.charAt(i)) * weight;
            weight++;
            if (weight > 7) weight = 2;
        }

        int remainder = sum % 11;
        if (remainder < 2) {
            return 0;
        } else {
            return 11 - remainder;
        }
    }

    private void saveUplatnicaHtml() {
        if (selectedTemplate == null || currentBarcodeImage == null) {
            showAlert(Alert.AlertType.WARNING, "No Data", "Please generate the uplatnica first.");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Uplatnica HTML");
            fileChooser.setInitialFileName("uplatnica_" + contact.getLastName() + "_" + System.currentTimeMillis() + ".html");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            File file = fileChooser.showSaveDialog(dialog);
            if (file != null) {
                String htmlContent = generateUplatnicaHtml();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(htmlContent);
                }
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Uplatnica HTML saved successfully!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to save HTML: " + e.getMessage());
        }
    }

    private void saveBarcodeImage() {
        if (currentBarcodeImage == null) {
            showAlert(Alert.AlertType.WARNING, "No Barcode", "Please generate a uplatnica first.");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Barcode Image");
            fileChooser.setInitialFileName("hub3_barcode_" + contact.getLastName() + "_" + System.currentTimeMillis() + ".png");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Images", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG Images", "*.jpg")
            );

            File file = fileChooser.showSaveDialog(dialog);
            if (file != null) {
                String format = file.getName().toLowerCase().endsWith(".jpg") ? "jpg" : "png";
                ImageIO.write(currentBarcodeImage, format, file);
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Barcode image saved successfully!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to save barcode image: " + e.getMessage());
        }
    }

    private void copyHUB3Data() {
        if (currentHUB3Data != null) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(currentHUB3Data);
            clipboard.setContent(content);

            showAlert(Alert.AlertType.INFORMATION, "Copied", "HUB-3 data copied to clipboard!");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Data", "Please generate a uplatnica first.");
        }
    }

    private void printUplatnica() {
        if (selectedTemplate == null || currentBarcodeImage == null) {
            showAlert(Alert.AlertType.WARNING, "No Data", "Please generate the uplatnica first.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Print Instructions",
                "To print the Croatian uplatnica:\n\n" +
                        "1. Click 'Save Uplatnica HTML'\n" +
                        "2. Open the saved HTML file in your web browser\n" +
                        "3. Use your browser's print function (Ctrl+P)\n" +
                        "4. Select appropriate paper size and margins\n\n" +
                        "This ensures perfect formatting and print quality.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialog);
        alert.showAndWait();
    }

    public void showAndWait() {
        dialog.showAndWait();
    }
}