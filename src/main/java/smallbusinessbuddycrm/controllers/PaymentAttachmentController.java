package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Worker;

import smallbusinessbuddycrm.database.PaymentAttachmentDAO;
import smallbusinessbuddycrm.model.PaymentAttachment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentAttachmentController {

    // Header Controls
    @FXML private Button newTemplateButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;

    // Table and List Controls
    @FXML private TableView<PaymentAttachment> templateTable;
    @FXML private TableColumn<PaymentAttachment, String> nameColumn;
    @FXML private TableColumn<PaymentAttachment, String> descriptionColumn;
    @FXML private TableColumn<PaymentAttachment, String> defaultColumn;
    @FXML private TableColumn<PaymentAttachment, String> createdColumn;

    // Action Buttons
    @FXML private Button editButton;
    @FXML private Button duplicateButton;
    @FXML private Button setDefaultButton;
    @FXML private Button deleteButton;
    @FXML private Button showVariablesButton;
    @FXML private Button variablesHelpButton;

    // Editor Controls
    @FXML private TabPane editorTabPane;
    @FXML private TextField templateNameField;
    @FXML private TextArea templateDescriptionField;
    @FXML private CheckBox isDefaultCheckbox;
    @FXML private TextArea htmlContentField;
    @FXML private WebView previewWebView;

    // Editor Buttons
    @FXML private Button formatHtmlButton;
    @FXML private Button insertVariableButton;
    @FXML private Button refreshPreviewButton;
    @FXML private Button exportPreviewButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label templateCountLabel;

    // Data and State
    private PaymentAttachmentDAO attachmentDAO;
    private ObservableList<PaymentAttachment> templateList;
    private PaymentAttachment currentTemplate;
    private boolean isEditing = false;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PaymentAttachmentController.initialize() called");

        // Initialize DAO
        attachmentDAO = new PaymentAttachmentDAO();
        templateList = FXCollections.observableArrayList();

        // Setup UI components
        setupTableColumns();
        setupEventHandlers();
        setupFormValidation();

        // Load initial data
        loadTemplates();
        clearEditor();
        updateUI();

        System.out.println("PaymentAttachmentController initialized successfully");
    }

    private void setupTableColumns() {
        // Name column
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Description column (truncated)
        descriptionColumn.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(desc != null ? desc : "");
        });

        // Default column
        defaultColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isDefault() ? "✓" : ""));
        defaultColumn.setStyle("-fx-alignment: CENTER;");

        // Created column
        createdColumn.setCellValueFactory(cellData -> {
            LocalDateTime created = cellData.getValue().getCreatedAt();
            if (created != null) {
                return new SimpleStringProperty(created.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
            return new SimpleStringProperty("");
        });

        // Bind table to data
        templateTable.setItems(templateList);

        // Selection listener
        templateTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    updateActionButtons(newSelection != null);
                    if (newSelection != null && !isEditing) {
                        loadTemplateForViewing(newSelection);
                    }
                }
        );
    }

    private void setupEventHandlers() {
        // Header buttons
        newTemplateButton.setOnAction(e -> createNewTemplate());
        refreshButton.setOnAction(e -> refreshTemplates());
        clearSearchButton.setOnAction(e -> clearSearch());

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> performSearch(newText));

        // Table action buttons
        editButton.setOnAction(e -> editSelectedTemplate());
        duplicateButton.setOnAction(e -> duplicateSelectedTemplate());
        setDefaultButton.setOnAction(e -> setSelectedAsDefault());
        deleteButton.setOnAction(e -> deleteSelectedTemplate());

        // Editor buttons
        formatHtmlButton.setOnAction(e -> formatHtmlContent());
        insertVariableButton.setOnAction(e -> showInsertVariableDialog());
        refreshPreviewButton.setOnAction(e -> refreshPreview());
        exportPreviewButton.setOnAction(e -> exportPreviewAsHtml());
        saveButton.setOnAction(e -> saveCurrentTemplate());
        cancelButton.setOnAction(e -> cancelEditing());
        showVariablesButton.setOnAction(e -> showVariablesModal());
        variablesHelpButton.setOnAction(e -> showVariablesModal());

        // Auto-refresh preview when HTML content changes
        htmlContentField.textProperty().addListener((obs, oldText, newText) -> {
            if (isEditing) {
                refreshPreview();
            }
        });
    }

    private void showVariablesModal() {
        // Create a custom dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Payment Template Variables");
        dialog.setHeaderText("Available Variables for Payment Templates");

        // Set the button types
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label("Template Variables Reference");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");
        content.getChildren().add(titleLabel);

        // Description
        Label descLabel = new Label("Use these variables in your HTML templates. They will be automatically replaced with actual data when generating payment slips:");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        content.getChildren().add(descLabel);

        // Create sections
        content.getChildren().add(createVariableSection("📅 Date & Time", new String[][]{
                {"{{CURRENT_DATE}}", "Current date and time (dd.MM.yyyy HH:mm)", "21.01.2025 14:30"}
        }));

        content.getChildren().add(createVariableSection("💰 Payment Information", new String[][]{
                {"{{AMOUNT}}", "Payment amount with currency formatting", "123,45 EUR"},
                {"{{REFERENCE}}", "Payment reference number", "7269-68949637676-00019"},
                {"{{MODEL}}", "Payment model", "HR01"},
                {"{{PURPOSE}}", "Purpose code", "COST"},
                {"{{DESCRIPTION}}", "Payment description (HTML formatted)", "Payment for services"}
        }));

        content.getChildren().add(createVariableSection("👤 Payer Information", new String[][]{
                {"{{PAYER_NAME}}", "Name of the person making payment", "John Doe"},
                {"{{PAYER_ADDRESS}}", "Payer's street address", "Main Street 123"},
                {"{{PAYER_CITY}}", "Payer's postal code and city", "10000 Zagreb"}
        }));

        content.getChildren().add(createVariableSection("🏢 Recipient Information", new String[][]{
                {"{{RECIPIENT_NAME}}", "Company or person receiving payment", "ABC Company d.o.o."},
                {"{{RECIPIENT_ADDRESS}}", "Recipient's street address", "Business Street 456"},
                {"{{RECIPIENT_CITY}}", "Recipient's postal code and city", "10000 Zagreb"},
                {"{{RECIPIENT_IBAN}}", "Recipient's IBAN account number", "HR1210010051863000160"}
        }));

        content.getChildren().add(createVariableSection("🔧 Technical", new String[][]{
                {"{{BARCODE_BASE64}}", "Base64 encoded barcode image for embedding", "data:image/png;base64,iVBORw0K..."},
                {"{{BANK_CODE}}", "Fixed bank code for HUB-3 standard", "HRVHUB30"}
        }));

        // Usage example
        VBox exampleSection = new VBox(8);
        exampleSection.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-padding: 15;");

        Label exampleTitle = new Label("💡 Usage Example:");
        exampleTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label exampleCode = new Label("<div class=\"amount\">Amount: {{AMOUNT}}</div>\n<div class=\"payer\">From: {{PAYER_NAME}}</div>");
        exampleCode.setStyle("-fx-font-family: 'Courier New', monospace; -fx-background-color: white; -fx-padding: 8; -fx-border-color: #ccc; -fx-border-radius: 3; -fx-text-fill: #333;");

        exampleSection.getChildren().addAll(exampleTitle, exampleCode);
        content.getChildren().add(exampleSection);

        // Put content in ScrollPane for large dialogs
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefSize(600, 500);
        scrollPane.setMaxHeight(600);

        dialog.getDialogPane().setContent(scrollPane);

        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().setPrefSize(650, 550);

        // Show the dialog
        dialog.showAndWait();
    }

    private VBox createVariableSection(String sectionTitle, String[][] variables) {
        VBox section = new VBox(8);
        section.setStyle("-fx-border-color: #e9ecef; -fx-border-radius: 4; -fx-padding: 12; -fx-background-color: #fdfdfd;");

        // Section title
        Label titleLabel = new Label(sectionTitle);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        section.getChildren().add(titleLabel);

        // Variables table
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(6);
        grid.setStyle("-fx-padding: 5 0 0 0;");

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(150);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(150);
        col3.setPrefWidth(150);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        for (int i = 0; i < variables.length; i++) {
            String[] variable = variables[i];

            // Variable name
            Label varLabel = new Label(variable[0]);
            varLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-font-size: 11px;");

            // Description
            Label descLabel = new Label(variable[1]);
            descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            descLabel.setWrapText(true);

            // Example
            Label exampleLabel = new Label(variable[2]);
            exampleLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-text-fill: #28a745; -fx-font-size: 10px; -fx-background-color: #f8f9fa; -fx-padding: 2 4; -fx-border-radius: 2;");

            grid.add(varLabel, 0, i);
            grid.add(descLabel, 1, i);
            grid.add(exampleLabel, 2, i);
        }

        section.getChildren().add(grid);
        return section;
    }

    private void setupFormValidation() {
        // Enable/disable save button based on form validity
        templateNameField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
        htmlContentField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
    }

    private void updateSaveButtonState() {
        boolean isValid = !templateNameField.getText().trim().isEmpty() &&
                !htmlContentField.getText().trim().isEmpty();
        saveButton.setDisable(!isValid);
    }

    private void loadTemplates() {
        try {
            templateList.clear();
            List<PaymentAttachment> templates = attachmentDAO.findAll();
            templateList.addAll(templates);
            updateStatusLabel("Loaded " + templates.size() + " templates");
            updateTemplateCount();
        } catch (Exception e) {
            showError("Failed to load templates: " + e.getMessage());
        }
    }

    private void performSearch(String searchTerm) {
        try {
            templateList.clear();
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                templateList.addAll(attachmentDAO.findAll());
            } else {
                templateList.addAll(attachmentDAO.findByNameContaining(searchTerm.trim()));
            }
            updateTemplateCount();
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void clearSearch() {
        searchField.clear();
        loadTemplates();
    }

    private void refreshTemplates() {
        loadTemplates();
        clearEditor();
        updateStatusLabel("Templates refreshed");
    }

    private void createNewTemplate() {
        clearEditor();
        isEditing = true;
        currentTemplate = new PaymentAttachment();

        // Set default HTML template
        htmlContentField.setText(getDefaultHtmlTemplate());
        templateNameField.setText("");
        templateDescriptionField.setText("");
        isDefaultCheckbox.setSelected(false);

        updateUI();
        templateNameField.requestFocus();
        updateStatusLabel("Creating new template");
    }

    private void editSelectedTemplate() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadTemplateForEditing(selected);
        }
    }

    private void loadTemplateForEditing(PaymentAttachment template) {
        isEditing = true;
        currentTemplate = template;

        templateNameField.setText(template.getName());
        templateDescriptionField.setText(template.getDescription());
        htmlContentField.setText(template.getHtmlContent());
        isDefaultCheckbox.setSelected(template.isDefault());

        refreshPreview();
        updateUI();
        updateStatusLabel("Editing: " + template.getName());
    }

    private void loadTemplateForViewing(PaymentAttachment template) {
        isEditing = false;
        currentTemplate = template;

        templateNameField.setText(template.getName());
        templateDescriptionField.setText(template.getDescription());
        htmlContentField.setText(template.getHtmlContent());
        isDefaultCheckbox.setSelected(template.isDefault());

        refreshPreview();
        updateUI();
        updateStatusLabel("Viewing: " + template.getName());
    }

    private void duplicateSelectedTemplate() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog("Copy of " + selected.getName());
            dialog.setTitle("Duplicate Template");
            dialog.setHeaderText("Create a copy of: " + selected.getName());
            dialog.setContentText("New template name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newName = result.get().trim();

                // Check if name already exists
                if (attachmentDAO.nameExists(newName, null)) {
                    showError("A template with this name already exists.");
                    return;
                }

                try {
                    PaymentAttachment duplicate = attachmentDAO.duplicate(selected.getId(), newName);
                    if (duplicate != null) {
                        loadTemplates();
                        selectTemplateInTable(duplicate);
                        updateStatusLabel("Template duplicated successfully");
                    } else {
                        showError("Failed to duplicate template");
                    }
                } catch (Exception e) {
                    showError("Duplication failed: " + e.getMessage());
                }
            }
        }
    }

    private void setSelectedAsDefault() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isDefault()) {
            try {
                if (attachmentDAO.setAsDefault(selected.getId())) {
                    loadTemplates();
                    selectTemplateInTable(selected);
                    updateStatusLabel("Default template updated");
                } else {
                    showError("Failed to set as default");
                }
            } catch (Exception e) {
                showError("Failed to set default: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedTemplate() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.isDefault()) {
                showError("Cannot delete the default template");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Template");
            confirmation.setHeaderText("Delete template: " + selected.getName());
            confirmation.setContentText("This action cannot be undone. Are you sure?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (attachmentDAO.delete(selected.getId())) {
                        loadTemplates();
                        clearEditor();
                        updateStatusLabel("Template deleted");
                    } else {
                        showError("Failed to delete template");
                    }
                } catch (Exception e) {
                    showError("Deletion failed: " + e.getMessage());
                }
            }
        }
    }

    private void saveCurrentTemplate() {
        if (!isEditing || currentTemplate == null) {
            return;
        }

        String name = templateNameField.getText().trim();
        String description = templateDescriptionField.getText().trim();
        String htmlContent = htmlContentField.getText().trim();
        boolean isDefault = isDefaultCheckbox.isSelected();

        // Validation
        if (name.isEmpty()) {
            showError("Template name is required");
            templateNameField.requestFocus();
            return;
        }

        if (htmlContent.isEmpty()) {
            showError("HTML content is required");
            htmlContentField.requestFocus();
            return;
        }

        // Check for duplicate names
        if (attachmentDAO.nameExists(name, currentTemplate.getId())) {
            showError("A template with this name already exists");
            templateNameField.requestFocus();
            return;
        }

        try {
            // Update template object
            currentTemplate.setName(name);
            currentTemplate.setDescription(description);
            currentTemplate.setHtmlContent(htmlContent);
            currentTemplate.setDefault(isDefault);

            // Save to database
            if (attachmentDAO.save(currentTemplate)) {
                loadTemplates();
                selectTemplateInTable(currentTemplate);
                isEditing = false;
                updateUI();
                updateStatusLabel("Template saved successfully");
            } else {
                showError("Failed to save template");
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void cancelEditing() {
        if (isEditing) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Cancel Editing");
            confirmation.setHeaderText("Discard changes?");
            confirmation.setContentText("Any unsaved changes will be lost.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                isEditing = false;
                PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadTemplateForViewing(selected);
                } else {
                    clearEditor();
                }
                updateUI();
                updateStatusLabel("Editing cancelled");
            }
        }
    }

    private void clearEditor() {
        isEditing = false;
        currentTemplate = null;
        templateNameField.clear();
        templateDescriptionField.clear();
        htmlContentField.clear();
        isDefaultCheckbox.setSelected(false);
        previewWebView.getEngine().loadContent("");
        updateUI();
    }

    private void refreshPreview() {
        String htmlContent = htmlContentField.getText();
        if (!htmlContent.trim().isEmpty()) {
            // Replace variables with sample data for preview
            String previewHtml = processTemplateForPreview(htmlContent);
            previewWebView.getEngine().loadContent(previewHtml);
        } else {
            previewWebView.getEngine().loadContent("");
        }
    }

    private String processTemplateForPreview(String htmlTemplate) {
        Map<String, String> sampleData = getSampleVariableData();
        String processed = htmlTemplate;

        for (Map.Entry<String, String> entry : sampleData.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processed = processed.replace(placeholder, entry.getValue());
        }

        return processed;
    }

    private Map<String, String> getSampleVariableData() {
        Map<String, String> data = new HashMap<>();
        data.put("CURRENT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        data.put("AMOUNT", "123,45");
        data.put("REFERENCE", "7269-68949637676-00019");
        data.put("MODEL", "HR01");
        data.put("PURPOSE", "COST");
        data.put("DESCRIPTION", "Payment for services rendered");
        data.put("PAYER_NAME", "John Doe");
        data.put("PAYER_ADDRESS", "Main Street 123");
        data.put("PAYER_CITY", "10000 Zagreb");
        data.put("RECIPIENT_NAME", "ABC Company d.o.o.");
        data.put("RECIPIENT_ADDRESS", "Business Street 456");
        data.put("RECIPIENT_CITY", "10000 Zagreb");
        data.put("RECIPIENT_IBAN", "HR1210010051863000160");
        data.put("BANK_CODE", "HRVHUB30");
        data.put("BARCODE_BASE64", "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77yAAAAABJRU5ErkJggg=="); // Placeholder image

        return data;
    }

    private void formatHtmlContent() {
        String content = htmlContentField.getText();
        if (!content.trim().isEmpty()) {
            // Basic HTML formatting (simple indentation)
            String formatted = formatHtml(content);
            htmlContentField.setText(formatted);
            updateStatusLabel("HTML formatted");
        }
    }

    private String formatHtml(String html) {
        // Simple HTML formatter - adds basic indentation
        String[] lines = html.split("\n");
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                formatted.append("\n");
                continue;
            }

            // Decrease indent for closing tags
            if (trimmed.startsWith("</")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // Add indentation
            for (int i = 0; i < indentLevel; i++) {
                formatted.append("    ");
            }
            formatted.append(trimmed).append("\n");

            // Increase indent for opening tags (but not self-closing)
            if (trimmed.startsWith("<") && !trimmed.startsWith("</") &&
                    !trimmed.endsWith("/>") && !isSelfClosingTag(trimmed)) {
                indentLevel++;
            }
        }

        return formatted.toString();
    }

    private boolean isSelfClosingTag(String line) {
        String[] selfClosing = {"<br", "<hr", "<img", "<input", "<meta", "<link"};
        for (String tag : selfClosing) {
            if (line.toLowerCase().startsWith(tag)) {
                return true;
            }
        }
        return false;
    }

    private void showInsertVariableDialog() {
        String[] variables = {
                "{{CURRENT_DATE}}", "{{AMOUNT}}", "{{REFERENCE}}", "{{MODEL}}", "{{PURPOSE}}",
                "{{DESCRIPTION}}", "{{PAYER_NAME}}", "{{PAYER_ADDRESS}}", "{{PAYER_CITY}}",
                "{{RECIPIENT_NAME}}", "{{RECIPIENT_ADDRESS}}", "{{RECIPIENT_CITY}}",
                "{{RECIPIENT_IBAN}}", "{{BARCODE_BASE64}}", "{{BANK_CODE}}"
        };

        ChoiceDialog<String> dialog = new ChoiceDialog<>(variables[0], variables);
        dialog.setTitle("Insert Variable");
        dialog.setHeaderText("Select a variable to insert");
        dialog.setContentText("Variable:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Insert at current cursor position
            String variable = result.get();
            int caretPosition = htmlContentField.getCaretPosition();
            String currentText = htmlContentField.getText();
            String newText = currentText.substring(0, caretPosition) + variable +
                    currentText.substring(caretPosition);
            htmlContentField.setText(newText);
            htmlContentField.positionCaret(caretPosition + variable.length());
            updateStatusLabel("Variable inserted: " + variable);
        }
    }

    private void exportPreviewAsHtml() {
        String htmlContent = htmlContentField.getText();
        if (htmlContent.trim().isEmpty()) {
            showError("No HTML content to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Preview as HTML");
        fileChooser.setInitialFileName("payment_template_preview.html");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html")
        );

        Stage stage = (Stage) exportPreviewButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                String previewHtml = processTemplateForPreview(htmlContent);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(previewHtml);
                }
                updateStatusLabel("Preview exported to: " + file.getName());
            } catch (IOException e) {
                showError("Failed to export preview: " + e.getMessage());
            }
        }
    }

    private String getDefaultHtmlTemplate() {
        return """
            <!DOCTYPE html>
            <html lang="hr">
            <head>
                <meta charset="UTF-8">
                <title>Payment Slip</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; color: #333; }
                    .header { text-align: center; border-bottom: 2px solid #0099cc; padding-bottom: 10px; margin-bottom: 20px; }
                    .header h1 { color: #0099cc; margin: 0; font-size: 24px; }
                    .section { border: 1px solid #ddd; border-radius: 5px; padding: 15px; margin: 10px 0; background-color: #f9f9f9; }
                    .section h3 { margin-top: 0; color: #0099cc; }
                    .field { margin-bottom: 8px; }
                    .field-label { font-weight: bold; display: inline-block; width: 100px; }
                    .barcode-section { text-align: center; margin: 30px 0; padding: 20px; border: 2px dashed #ccc; }
                    .barcode-image { max-width: 100%; border: 1px solid #333; background: white; }
                    .amount-highlight { font-size: 18px; font-weight: bold; color: #28a745; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🇭🇷 Payment Slip</h1>
                    <p>Generated on: {{CURRENT_DATE}}</p>
                </div>
                
                <div class="section">
                    <h3>💳 Payment Information</h3>
                    <div class="field"><span class="field-label">Amount:</span> <span class="amount-highlight">{{AMOUNT}} EUR</span></div>
                    <div class="field"><span class="field-label">Reference:</span> {{REFERENCE}}</div>
                    <div class="field"><span class="field-label">Description:</span> {{DESCRIPTION}}</div>
                </div>
                
                <div class="section">
                    <h3>👤 Payer Information</h3>
                    <div class="field"><span class="field-label">Name:</span> {{PAYER_NAME}}</div>
                    <div class="field"><span class="field-label">Address:</span> {{PAYER_ADDRESS}}</div>
                    <div class="field"><span class="field-label">City:</span> {{PAYER_CITY}}</div>
                </div>
                
                <div class="section">
                    <h3>🏢 Recipient Information</h3>
                    <div class="field"><span class="field-label">Company:</span> {{RECIPIENT_NAME}}</div>
                    <div class="field"><span class="field-label">Address:</span> {{RECIPIENT_ADDRESS}}</div>
                    <div class="field"><span class="field-label">City:</span> {{RECIPIENT_CITY}}</div>
                    <div class="field"><span class="field-label">IBAN:</span> {{RECIPIENT_IBAN}}</div>
                </div>
                
                <div class="barcode-section">
                    <h3>📊 Payment Barcode</h3>
                    <img src="data:image/png;base64,{{BARCODE_BASE64}}" alt="Payment Barcode" class="barcode-image">
                </div>
            </body>
            </html>
            """;
    }

    private void updateActionButtons(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);

        if (hasSelection) {
            PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
            setDefaultButton.setDisable(selected.isDefault());
            deleteButton.setDisable(selected.isDefault()); // Can't delete default
        } else {
            setDefaultButton.setDisable(true);
        }
    }

    private void updateUI() {
        // Enable/disable editor fields based on editing state
        templateNameField.setEditable(isEditing);
        templateDescriptionField.setEditable(isEditing);
        htmlContentField.setEditable(isEditing);
        isDefaultCheckbox.setDisable(!isEditing);

        // Show/hide editor buttons
        formatHtmlButton.setDisable(!isEditing);
        insertVariableButton.setDisable(!isEditing);
        saveButton.setDisable(!isEditing);
        cancelButton.setDisable(!isEditing);

        // Update save button state
        updateSaveButtonState();

        // Set editor tab selection
        if (isEditing && editorTabPane.getSelectionModel().getSelectedIndex() == 2) {
            editorTabPane.getSelectionModel().select(0); // Switch to Details tab when editing
        }
    }

    private void selectTemplateInTable(PaymentAttachment template) {
        if (template != null) {
            templateTable.getSelectionModel().select(template);
            templateTable.scrollTo(template);
        }
    }

    private void updateTemplateCount() {
        int count = templateList.size();
        templateCountLabel.setText(count + " template" + (count != 1 ? "s" : ""));
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);

        // Auto-clear status after 5 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                    if (statusLabel.getText().equals(message)) {
                        statusLabel.setText("Ready");
                    }
                })
        );
        timeline.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        updateStatusLabel("Error: " + message);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    // Public methods for external access
    public void refreshData() {
        loadTemplates();
    }

    public PaymentAttachment getSelectedTemplate() {
        return templateTable.getSelectionModel().getSelectedItem();
    }

    public void selectTemplate(Long templateId) {
        if (templateId != null) {
            for (PaymentAttachment template : templateList) {
                if (template.getId().equals(templateId)) {
                    selectTemplateInTable(template);
                    break;
                }
            }
        }
    }
}