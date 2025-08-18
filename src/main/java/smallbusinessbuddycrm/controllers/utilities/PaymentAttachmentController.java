package smallbusinessbuddycrm.controllers.utilities;

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

import smallbusinessbuddycrm.database.PaymentAttachmentDAO;
import smallbusinessbuddycrm.model.PaymentAttachment;
import smallbusinessbuddycrm.utilities.LanguageManager;

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

public class PaymentAttachmentController implements Initializable {

    // Header Controls
    @FXML private Label titleLabel;
    @FXML private Button newTemplateButton;
    @FXML private Button refreshButton;
    @FXML private Label searchLabel;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;

    // Table and List Controls
    @FXML private Label templatesLabel;
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
    @FXML private Tab detailsTab;
    @FXML private Tab htmlEditorTab;
    @FXML private Tab previewTab;
    @FXML private Label templateNameLabel;
    @FXML private TextField templateNameField;
    @FXML private Label descriptionLabel;
    @FXML private TextArea templateDescriptionField;
    @FXML private CheckBox isDefaultCheckbox;
    @FXML private Label variablesHelpLabel;
    @FXML private Label htmlContentLabel;
    @FXML private TextArea htmlContentField;
    @FXML private WebView previewWebView;

    // Editor Buttons
    @FXML private Button formatHtmlButton;
    @FXML private Button insertVariableButton;
    @FXML private Label previewLabel;
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

    @Override
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

        // Initialize translations
        updateTexts();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);

        System.out.println("PaymentAttachmentController initialized successfully");
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update header labels and buttons
        if (titleLabel != null) {
            titleLabel.setText(lm.getText("payment.attachment.title"));
        }

        if (newTemplateButton != null) {
            newTemplateButton.setText(lm.getText("payment.attachment.button.new.template"));
        }

        if (refreshButton != null) {
            refreshButton.setText(lm.getText("payment.attachment.button.refresh"));
        }

        if (searchLabel != null) {
            searchLabel.setText(lm.getText("payment.attachment.search.label"));
        }

        if (searchField != null) {
            searchField.setPromptText(lm.getText("payment.attachment.search.placeholder"));
        }

        if (clearSearchButton != null) {
            clearSearchButton.setText(lm.getText("payment.attachment.button.clear.search"));
        }

        if (templatesLabel != null) {
            templatesLabel.setText(lm.getText("payment.attachment.templates.label"));
        }

        // Update table columns
        if (nameColumn != null) {
            nameColumn.setText(lm.getText("payment.attachment.table.name"));
        }

        if (descriptionColumn != null) {
            descriptionColumn.setText(lm.getText("payment.attachment.table.description"));
        }

        if (defaultColumn != null) {
            defaultColumn.setText(lm.getText("payment.attachment.table.default"));
        }

        if (createdColumn != null) {
            createdColumn.setText(lm.getText("payment.attachment.table.created"));
        }

        // Update table placeholder
        if (templateTable != null) {
            templateTable.setPlaceholder(new Label(lm.getText("payment.attachment.table.placeholder")));
        }

        // Update action buttons
        if (editButton != null) {
            editButton.setText(lm.getText("payment.attachment.button.edit"));
        }

        if (duplicateButton != null) {
            duplicateButton.setText(lm.getText("payment.attachment.button.duplicate"));
        }

        if (setDefaultButton != null) {
            setDefaultButton.setText(lm.getText("payment.attachment.button.set.default"));
        }

        if (deleteButton != null) {
            deleteButton.setText(lm.getText("payment.attachment.button.delete"));
        }

        // Update tabs
        if (detailsTab != null) {
            detailsTab.setText(lm.getText("payment.attachment.tab.details"));
        }

        if (htmlEditorTab != null) {
            htmlEditorTab.setText(lm.getText("payment.attachment.tab.html.editor"));
        }

        if (previewTab != null) {
            previewTab.setText(lm.getText("payment.attachment.tab.preview"));
        }

        // Update form labels
        if (templateNameLabel != null) {
            templateNameLabel.setText(lm.getText("payment.attachment.field.template.name"));
        }

        if (templateNameField != null) {
            templateNameField.setPromptText(lm.getText("payment.attachment.field.template.name.placeholder"));
        }

        if (descriptionLabel != null) {
            descriptionLabel.setText(lm.getText("payment.attachment.field.description"));
        }

        if (templateDescriptionField != null) {
            templateDescriptionField.setPromptText(lm.getText("payment.attachment.field.description.placeholder"));
        }

        if (isDefaultCheckbox != null) {
            isDefaultCheckbox.setText(lm.getText("payment.attachment.checkbox.default"));
        }

        if (showVariablesButton != null) {
            showVariablesButton.setText(lm.getText("payment.attachment.button.available.variables"));
        }

        if (variablesHelpLabel != null) {
            variablesHelpLabel.setText(lm.getText("payment.attachment.variables.help.text"));
        }

        if (htmlContentLabel != null) {
            htmlContentLabel.setText(lm.getText("payment.attachment.field.html.content"));
        }

        if (htmlContentField != null) {
            htmlContentField.setPromptText(lm.getText("payment.attachment.field.html.content.placeholder"));
        }

        if (formatHtmlButton != null) {
            formatHtmlButton.setText(lm.getText("payment.attachment.button.format"));
        }

        if (insertVariableButton != null) {
            insertVariableButton.setText(lm.getText("payment.attachment.button.insert.variable"));
        }

        if (previewLabel != null) {
            previewLabel.setText(lm.getText("payment.attachment.label.preview"));
        }

        if (refreshPreviewButton != null) {
            refreshPreviewButton.setText(lm.getText("payment.attachment.button.refresh.preview"));
        }

        if (exportPreviewButton != null) {
            exportPreviewButton.setText(lm.getText("payment.attachment.button.export.html"));
        }

        if (saveButton != null) {
            saveButton.setText(lm.getText("payment.attachment.button.save"));
        }

        if (cancelButton != null) {
            cancelButton.setText(lm.getText("payment.attachment.button.cancel"));
        }

        // Update status
        updateTemplateCount();
        if (statusLabel != null && statusLabel.getText().equals("Ready")) {
            statusLabel.setText(lm.getText("payment.attachment.status.ready"));
        }
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
        LanguageManager lm = LanguageManager.getInstance();

        // Create a custom dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(lm.getText("payment.attachment.variables.dialog.title"));
        dialog.setHeaderText(lm.getText("payment.attachment.variables.dialog.header"));

        // Set the button types
        ButtonType closeButtonType = new ButtonType(lm.getText("payment.attachment.variables.dialog.close"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label(lm.getText("payment.attachment.variables.reference.title"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099cc;");
        content.getChildren().add(titleLabel);

        // Description
        Label descLabel = new Label(lm.getText("payment.attachment.variables.description"));
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        content.getChildren().add(descLabel);

        // Create sections
        content.getChildren().add(createVariableSection(lm.getText("payment.attachment.variables.section.datetime"), new String[][]{
                {"{{CURRENT_DATE}}", lm.getText("payment.attachment.variable.current.date.desc"), "21.01.2025 14:30"}
        }));

        content.getChildren().add(createVariableSection(lm.getText("payment.attachment.variables.section.payment"), new String[][]{
                {"{{AMOUNT}}", lm.getText("payment.attachment.variable.amount.desc"), "123,45 EUR"},
                {"{{REFERENCE}}", lm.getText("payment.attachment.variable.reference.desc"), "7269-68949637676-00019"},
                {"{{MODEL}}", lm.getText("payment.attachment.variable.model.desc"), "HR01"},
                {"{{PURPOSE}}", lm.getText("payment.attachment.variable.purpose.desc"), "COST"},
                {"{{DESCRIPTION}}", lm.getText("payment.attachment.variable.description.desc"), lm.getText("payment.attachment.sample.description")}
        }));

        content.getChildren().add(createVariableSection(lm.getText("payment.attachment.variables.section.payer"), new String[][]{
                {"{{PAYER_NAME}}", lm.getText("payment.attachment.variable.payer.name.desc"), lm.getText("payment.attachment.sample.payer.name")},
                {"{{PAYER_ADDRESS}}", lm.getText("payment.attachment.variable.payer.address.desc"), lm.getText("payment.attachment.sample.payer.address")},
                {"{{PAYER_CITY}}", lm.getText("payment.attachment.variable.payer.city.desc"), lm.getText("payment.attachment.sample.payer.city")}
        }));

        content.getChildren().add(createVariableSection(lm.getText("payment.attachment.variables.section.recipient"), new String[][]{
                {"{{RECIPIENT_NAME}}", lm.getText("payment.attachment.variable.recipient.name.desc"), lm.getText("payment.attachment.sample.recipient.name")},
                {"{{RECIPIENT_ADDRESS}}", lm.getText("payment.attachment.variable.recipient.address.desc"), lm.getText("payment.attachment.sample.recipient.address")},
                {"{{RECIPIENT_CITY}}", lm.getText("payment.attachment.variable.recipient.city.desc"), lm.getText("payment.attachment.sample.recipient.city")},
                {"{{RECIPIENT_IBAN}}", lm.getText("payment.attachment.variable.recipient.iban.desc"), "HR1210010051863000160"}
        }));

        content.getChildren().add(createVariableSection(lm.getText("payment.attachment.variables.section.technical"), new String[][]{
                {"{{BARCODE_BASE64}}", lm.getText("payment.attachment.variable.barcode.desc"), "data:image/png;base64,iVBORw0K..."},
                {"{{BANK_CODE}}", lm.getText("payment.attachment.variable.bank.code.desc"), "HRVHUB30"}
        }));

        // Usage example
        VBox exampleSection = new VBox(8);
        exampleSection.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-padding: 15;");

        Label exampleTitle = new Label(lm.getText("payment.attachment.variables.usage.title"));
        exampleTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label exampleCode = new Label("<div class=\"amount\">" + lm.getText("payment.attachment.default.template.amount") + " {{AMOUNT}}</div>\n<div class=\"payer\">" + lm.getText("payment.attachment.default.template.name") + " {{PAYER_NAME}}</div>");
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
            LanguageManager lm = LanguageManager.getInstance();
            updateStatusLabel(String.format(lm.getText("payment.attachment.status.loaded.templates"), templates.size()));
            updateTemplateCount();
        } catch (Exception e) {
            LanguageManager lm = LanguageManager.getInstance();
            showError(String.format(lm.getText("payment.attachment.error.load.failed"), e.getMessage()));
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
            LanguageManager lm = LanguageManager.getInstance();
            showError(String.format(lm.getText("payment.attachment.error.search.failed"), e.getMessage()));
        }
    }

    private void clearSearch() {
        searchField.clear();
        loadTemplates();
    }

    private void refreshTemplates() {
        loadTemplates();
        clearEditor();
        LanguageManager lm = LanguageManager.getInstance();
        updateStatusLabel(lm.getText("payment.attachment.status.templates.refreshed"));
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
        LanguageManager lm = LanguageManager.getInstance();
        updateStatusLabel(lm.getText("payment.attachment.status.creating.new"));
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
        LanguageManager lm = LanguageManager.getInstance();
        updateStatusLabel(String.format(lm.getText("payment.attachment.status.editing"), template.getName()));
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
        LanguageManager lm = LanguageManager.getInstance();
        updateStatusLabel(String.format(lm.getText("payment.attachment.status.viewing"), template.getName()));
    }

    private void duplicateSelectedTemplate() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LanguageManager lm = LanguageManager.getInstance();

            TextInputDialog dialog = new TextInputDialog(String.format(lm.getText("payment.attachment.dialog.duplicate.default.name"), selected.getName()));
            dialog.setTitle(lm.getText("payment.attachment.dialog.duplicate.title"));
            dialog.setHeaderText(String.format(lm.getText("payment.attachment.dialog.duplicate.header"), selected.getName()));
            dialog.setContentText(lm.getText("payment.attachment.dialog.duplicate.content"));

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newName = result.get().trim();

                // Check if name already exists
                if (attachmentDAO.nameExists(newName, null)) {
                    showError(lm.getText("payment.attachment.error.name.exists"));
                    return;
                }

                try {
                    PaymentAttachment duplicate = attachmentDAO.duplicate(selected.getId(), newName);
                    if (duplicate != null) {
                        loadTemplates();
                        selectTemplateInTable(duplicate);
                        updateStatusLabel(lm.getText("payment.attachment.status.duplicated"));
                    } else {
                        showError(lm.getText("payment.attachment.error.duplicate.failed"));
                    }
                } catch (Exception e) {
                    showError(String.format(lm.getText("payment.attachment.error.duplication.failed"), e.getMessage()));
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
                    LanguageManager lm = LanguageManager.getInstance();
                    updateStatusLabel(lm.getText("payment.attachment.status.default.updated"));
                } else {
                    LanguageManager lm = LanguageManager.getInstance();
                    showError(lm.getText("payment.attachment.error.set.default.failed"));
                }
            } catch (Exception e) {
                LanguageManager lm = LanguageManager.getInstance();
                showError(String.format(lm.getText("payment.attachment.error.set.default.failed.detailed"), e.getMessage()));
            }
        }
    }

    private void deleteSelectedTemplate() {
        PaymentAttachment selected = templateTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LanguageManager lm = LanguageManager.getInstance();

            if (selected.isDefault()) {
                showError(lm.getText("payment.attachment.error.cannot.delete.default"));
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(lm.getText("payment.attachment.dialog.delete.title"));
            confirmation.setHeaderText(String.format(lm.getText("payment.attachment.dialog.delete.header"), selected.getName()));
            confirmation.setContentText(lm.getText("payment.attachment.dialog.delete.content"));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (attachmentDAO.delete(selected.getId())) {
                        loadTemplates();
                        clearEditor();
                        updateStatusLabel(lm.getText("payment.attachment.status.deleted"));
                    } else {
                        showError(lm.getText("payment.attachment.error.delete.failed"));
                    }
                } catch (Exception e) {
                    showError(String.format(lm.getText("payment.attachment.error.deletion.failed"), e.getMessage()));
                }
            }
        }
    }

    private void saveCurrentTemplate() {
        if (!isEditing || currentTemplate == null) {
            return;
        }

        LanguageManager lm = LanguageManager.getInstance();
        String name = templateNameField.getText().trim();
        String description = templateDescriptionField.getText().trim();
        String htmlContent = htmlContentField.getText().trim();
        boolean isDefault = isDefaultCheckbox.isSelected();

        // Validation
        if (name.isEmpty()) {
            showError(lm.getText("payment.attachment.error.name.required"));
            templateNameField.requestFocus();
            return;
        }

        if (htmlContent.isEmpty()) {
            showError(lm.getText("payment.attachment.error.html.required"));
            htmlContentField.requestFocus();
            return;
        }

        // Check for duplicate names
        if (attachmentDAO.nameExists(name, currentTemplate.getId())) {
            showError(lm.getText("payment.attachment.error.name.exists"));
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
                updateStatusLabel(lm.getText("payment.attachment.status.saved"));
            } else {
                showError(lm.getText("payment.attachment.error.save.failed"));
            }
        } catch (Exception e) {
            showError(String.format(lm.getText("payment.attachment.error.save.failed.detailed"), e.getMessage()));
        }
    }

    private void cancelEditing() {
        if (isEditing) {
            LanguageManager lm = LanguageManager.getInstance();

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(lm.getText("payment.attachment.dialog.cancel.editing.title"));
            confirmation.setHeaderText(lm.getText("payment.attachment.dialog.cancel.editing.header"));
            confirmation.setContentText(lm.getText("payment.attachment.dialog.cancel.editing.content"));

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
                updateStatusLabel(lm.getText("payment.attachment.status.cancelled"));
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
        LanguageManager lm = LanguageManager.getInstance();
        Map<String, String> data = new HashMap<>();
        data.put("CURRENT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        data.put("AMOUNT", "123,45");
        data.put("REFERENCE", "7269-68949637676-00019");
        data.put("MODEL", "HR01");
        data.put("PURPOSE", "COST");
        data.put("DESCRIPTION", lm.getText("payment.attachment.sample.description"));
        data.put("PAYER_NAME", lm.getText("payment.attachment.sample.payer.name"));
        data.put("PAYER_ADDRESS", lm.getText("payment.attachment.sample.payer.address"));
        data.put("PAYER_CITY", lm.getText("payment.attachment.sample.payer.city"));
        data.put("RECIPIENT_NAME", lm.getText("payment.attachment.sample.recipient.name"));
        data.put("RECIPIENT_ADDRESS", lm.getText("payment.attachment.sample.recipient.address"));
        data.put("RECIPIENT_CITY", lm.getText("payment.attachment.sample.recipient.city"));
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
            LanguageManager lm = LanguageManager.getInstance();
            updateStatusLabel(lm.getText("payment.attachment.status.html.formatted"));
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

        LanguageManager lm = LanguageManager.getInstance();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(variables[0], variables);
        dialog.setTitle(lm.getText("payment.attachment.dialog.insert.variable.title"));
        dialog.setHeaderText(lm.getText("payment.attachment.dialog.insert.variable.header"));
        dialog.setContentText(lm.getText("payment.attachment.dialog.insert.variable.content"));

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
            updateStatusLabel(String.format(lm.getText("payment.attachment.status.variable.inserted"), variable));
        }
    }

    private void exportPreviewAsHtml() {
        String htmlContent = htmlContentField.getText();
        if (htmlContent.trim().isEmpty()) {
            LanguageManager lm = LanguageManager.getInstance();
            showError(lm.getText("payment.attachment.error.no.html.content"));
            return;
        }

        LanguageManager lm = LanguageManager.getInstance();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lm.getText("payment.attachment.file.chooser.export.title"));
        fileChooser.setInitialFileName(lm.getText("payment.attachment.file.chooser.export.filename"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(lm.getText("payment.attachment.file.chooser.html.files"), "*.html")
        );

        Stage stage = (Stage) exportPreviewButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                String previewHtml = processTemplateForPreview(htmlContent);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(previewHtml);
                }
                updateStatusLabel(String.format(lm.getText("payment.attachment.status.exported"), file.getName()));
            } catch (IOException e) {
                showError(String.format(lm.getText("payment.attachment.error.export.failed"), e.getMessage()));
            }
        }
    }

    private String getDefaultHtmlTemplate() {
        LanguageManager lm = LanguageManager.getInstance();
        return """
            <!DOCTYPE html>
            <html lang="hr">
            <head>
                <meta charset="UTF-8">
                <title>""" + lm.getText("payment.attachment.default.template.title") + """
                </title>
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
                    <h1>🇭🇷 """ + lm.getText("payment.attachment.default.template.title") + """
                    </h1>
                    <p>""" + lm.getText("payment.attachment.default.template.generated") + """
                    </p>
                </div>
                
                <div class="section">
                    <h3>""" + lm.getText("payment.attachment.default.template.payment.info") + """
                    </h3>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.amount") + """
                    </span> <span class="amount-highlight">{{AMOUNT}} EUR</span></div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.reference") + """
                    </span> {{REFERENCE}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.description.label") + """
                    </span> {{DESCRIPTION}}</div>
                </div>
                
                <div class="section">
                    <h3>""" + lm.getText("payment.attachment.default.template.payer.info") + """
                    </h3>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.name") + """
                    </span> {{PAYER_NAME}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.address") + """
                    </span> {{PAYER_ADDRESS}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.city") + """
                    </span> {{PAYER_CITY}}</div>
                </div>
                
                <div class="section">
                    <h3>""" + lm.getText("payment.attachment.default.template.recipient.info") + """
                    </h3>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.company") + """
                    </span> {{RECIPIENT_NAME}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.address") + """
                    </span> {{RECIPIENT_ADDRESS}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.city") + """
                    </span> {{RECIPIENT_CITY}}</div>
                    <div class="field"><span class="field-label">""" + lm.getText("payment.attachment.default.template.iban") + """
                    </span> {{RECIPIENT_IBAN}}</div>
                </div>
                
                <div class="barcode-section">
                    <h3>""" + lm.getText("payment.attachment.default.template.barcode.section") + """
                    </h3>
                    <img src="data:image/png;base64,{{BARCODE_BASE64}}" alt=\"""" + lm.getText("payment.attachment.default.template.barcode.alt") + """
                    \" class="barcode-image">
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
        LanguageManager lm = LanguageManager.getInstance();
        String countText = count == 1 ?
                String.format(lm.getText("payment.attachment.template.count.single"), count) :
                String.format(lm.getText("payment.attachment.template.count.multiple"), count);
        templateCountLabel.setText(countText);
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);

        // Auto-clear status after 5 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                    if (statusLabel.getText().equals(message)) {
                        LanguageManager lm = LanguageManager.getInstance();
                        statusLabel.setText(lm.getText("payment.attachment.status.ready"));
                    }
                })
        );
        timeline.play();
    }

    private void showError(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lm.getText("button.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        updateStatusLabel(lm.getText("button.error") + ": " + message);
    }

    private void showInfo(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lm.getText("button.information"));
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

    // Method to update translations from external calls
    public void refreshTranslations() {
        updateTexts();
    }
}