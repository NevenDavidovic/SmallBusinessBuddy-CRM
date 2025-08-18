package smallbusinessbuddycrm.controllers.utilities;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Clipboard;

import smallbusinessbuddycrm.model.NewsletterTemplate;
import smallbusinessbuddycrm.services.newsletter.NewsletterComponentBuilder;
import smallbusinessbuddycrm.services.newsletter.NewsletterHtmlGenerator;
import smallbusinessbuddycrm.services.newsletter.NewsletterService;
import smallbusinessbuddycrm.services.newsletter.TemplateManager;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Optional;

public class NewsletterBuilderController implements Initializable {

    // FXML Fields - Header and Navigation
    @FXML private TitledPane newslettersTemplatesTitle;
    @FXML private Label newsletterBuilderTitle;
    @FXML private Label itemCountLabel;
    @FXML private Label versionLabel;
    @FXML private Button newNewsletterButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private Button exportHtmlButton;
    @FXML private Button sendButton;

    // FXML Fields - Table
    @FXML private TableView<NewsletterTemplate> newsletterTable;
    @FXML private TableColumn<NewsletterTemplate, String> nameColumn;
    @FXML private TableColumn<NewsletterTemplate, String> typeColumn;
    @FXML private TableColumn<NewsletterTemplate, String> statusColumn;
    @FXML private TableColumn<NewsletterTemplate, String> createdColumn;
    @FXML private Button editButton;
    @FXML private Button duplicateButton;
    @FXML private Button deleteButton;

    // FXML Fields - Editor Tabs
    @FXML private TabPane editorTabPane;
    @FXML private Tab detailsTab;
    @FXML private Tab visualBuilderTab;
    @FXML private Tab previewTab;

    // FXML Fields - Form Elements
    @FXML private Label newsletterNameLabel;
    @FXML private TextField newsletterNameField;
    @FXML private Label subjectLineLabel;
    @FXML private TextField subjectField;
    @FXML private Label companyNameLabel;
    @FXML private TextField companyNameField;
    @FXML private Label newsletterTypeLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Label designSettingsLabel;
    @FXML private Label headerColorLabel;
    @FXML private ColorPicker headerColorPicker;
    @FXML private Label templateStyleLabel;
    @FXML private ComboBox<String> templateCombo;
    @FXML private Label contentLabel;
    @FXML private TextArea contentEditor;

    // FXML Fields - Visual Builder
    @FXML private Label componentsLabel;
    @FXML private Button addTextButton;
    @FXML private Button addHeadingButton;
    @FXML private Button addImageButton;
    @FXML private Button addButtonButton;
    @FXML private Button addDividerButton;
    @FXML private Label livePreviewLabel;
    @FXML private Button refreshPreviewButton;
    @FXML private WebView previewWebView;

    // FXML Fields - Preview Tab
    @FXML private Label finalPreviewLabel;
    @FXML private Label previewPlaceholderLabel;

    // FXML Fields - Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    // Services
    private NewsletterService newsletterService;
    private TemplateManager templateManager;
    private NewsletterHtmlGenerator htmlGenerator;
    private NewsletterComponentBuilder componentBuilder;
    private LanguageManager languageManager;

    // State
    private Connection dbConnection;
    private ObservableList<NewsletterTemplate> newsletterList;
    private NewsletterTemplate currentNewsletter;
    private boolean isEditing = false;
    private StringBuilder newsletterHTML;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NewsletterBuilderController.initialize() called");

        try {
            languageManager = LanguageManager.getInstance();
            initializeServices();
            initializeUI();
            loadInitialData();

            // Add language change listener
            languageManager.addLanguageChangeListener(this::updateTexts);
            updateTexts();

            System.out.println("Newsletter Builder initialized successfully");
        } catch (Exception e) {
            showError(languageManager.getText("newsletter.init.error") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTexts() {
        // Update titles and headers
        if (newslettersTemplatesTitle != null) {
            newslettersTemplatesTitle.setText(languageManager.getText("newsletter.title.newsletters.templates"));
        }
        if (newsletterBuilderTitle != null) {
            newsletterBuilderTitle.setText(languageManager.getText("newsletter.title.builder"));
        }
        if (versionLabel != null) {
            versionLabel.setText(languageManager.getText("newsletter.label.version"));
        }

        // Update buttons
        if (newNewsletterButton != null) newNewsletterButton.setText(languageManager.getText("newsletter.button.new"));
        if (refreshButton != null) refreshButton.setText(languageManager.getText("newsletter.button.refresh"));
        if (clearSearchButton != null) clearSearchButton.setText(languageManager.getText("newsletter.button.clear.search"));
        if (exportHtmlButton != null) exportHtmlButton.setText(languageManager.getText("newsletter.button.export.html"));
        if (sendButton != null) sendButton.setText(languageManager.getText("newsletter.button.send"));
        if (editButton != null) editButton.setText(languageManager.getText("newsletter.button.edit"));
        if (duplicateButton != null) duplicateButton.setText(languageManager.getText("newsletter.button.duplicate"));
        if (deleteButton != null) deleteButton.setText(languageManager.getText("newsletter.button.delete"));
        if (saveButton != null) saveButton.setText(languageManager.getText("newsletter.button.save"));
        if (cancelButton != null) cancelButton.setText(languageManager.getText("newsletter.button.cancel"));

        // Update table columns
        if (nameColumn != null) nameColumn.setText(languageManager.getText("newsletter.column.name"));
        if (typeColumn != null) typeColumn.setText(languageManager.getText("newsletter.column.type"));
        if (statusColumn != null) statusColumn.setText(languageManager.getText("newsletter.column.status"));
        if (createdColumn != null) createdColumn.setText(languageManager.getText("newsletter.column.created"));

        // Update tabs
        if (detailsTab != null) detailsTab.setText(languageManager.getText("newsletter.tab.details"));
        if (visualBuilderTab != null) visualBuilderTab.setText(languageManager.getText("newsletter.tab.visual.builder"));
        if (previewTab != null) previewTab.setText(languageManager.getText("newsletter.tab.preview"));

        // Update form labels
        if (newsletterNameLabel != null) newsletterNameLabel.setText(languageManager.getText("newsletter.label.name"));
        if (subjectLineLabel != null) subjectLineLabel.setText(languageManager.getText("newsletter.label.subject"));
        if (companyNameLabel != null) companyNameLabel.setText(languageManager.getText("newsletter.label.company"));
        if (newsletterTypeLabel != null) newsletterTypeLabel.setText(languageManager.getText("newsletter.label.type"));
        if (designSettingsLabel != null) designSettingsLabel.setText(languageManager.getText("newsletter.label.design.settings"));
        if (headerColorLabel != null) headerColorLabel.setText(languageManager.getText("newsletter.label.header.color"));
        if (templateStyleLabel != null) templateStyleLabel.setText(languageManager.getText("newsletter.label.template.style"));
        if (contentLabel != null) contentLabel.setText(languageManager.getText("newsletter.label.content"));
        if (componentsLabel != null) componentsLabel.setText(languageManager.getText("newsletter.label.components"));
        if (livePreviewLabel != null) livePreviewLabel.setText(languageManager.getText("newsletter.label.live.preview"));
        if (finalPreviewLabel != null) finalPreviewLabel.setText(languageManager.getText("newsletter.label.final.preview"));
        if (previewPlaceholderLabel != null) previewPlaceholderLabel.setText(languageManager.getText("newsletter.label.preview.placeholder"));

        // Update component buttons
        if (addTextButton != null) addTextButton.setText(languageManager.getText("newsletter.button.add.text"));
        if (addHeadingButton != null) addHeadingButton.setText(languageManager.getText("newsletter.button.add.heading"));
        if (addImageButton != null) addImageButton.setText(languageManager.getText("newsletter.button.add.image"));
        if (addButtonButton != null) addButtonButton.setText(languageManager.getText("newsletter.button.add.button"));
        if (addDividerButton != null) addDividerButton.setText(languageManager.getText("newsletter.button.add.divider"));
        if (refreshPreviewButton != null) refreshPreviewButton.setText(languageManager.getText("newsletter.button.refresh.preview"));

        // Update field placeholders
        if (searchField != null) searchField.setPromptText(languageManager.getText("newsletter.search.placeholder"));
        if (newsletterNameField != null) newsletterNameField.setPromptText(languageManager.getText("newsletter.placeholder.name"));
        if (subjectField != null) subjectField.setPromptText(languageManager.getText("newsletter.placeholder.subject"));
        if (companyNameField != null) companyNameField.setPromptText(languageManager.getText("newsletter.placeholder.company"));
        if (contentEditor != null) contentEditor.setPromptText(languageManager.getText("newsletter.placeholder.content"));

        // Update combo boxes
        updateComboBoxTexts();

        // Update item count
        updateItemCount();

        // Update status if it shows "Ready"
        if (statusLabel != null && (statusLabel.getText().equals("Ready") || statusLabel.getText().equals("Spreman"))) {
            statusLabel.setText(languageManager.getText("newsletter.status.ready"));
        }

        // Update table placeholder
        if (newsletterTable != null) {
            // Since there's no direct translation key for table placeholder, use a generic one
            newsletterTable.setPlaceholder(new Label(languageManager.getText("newsletter.filter.all") + " - " + languageManager.getText("newsletter.status.ready")));
        }
    }

    private void updateComboBoxTexts() {
        if (typeFilterCombo != null) {
            String currentSelection = typeFilterCombo.getValue();
            typeFilterCombo.getItems().clear();
            typeFilterCombo.getItems().addAll(
                    languageManager.getText("newsletter.filter.all"),
                    languageManager.getText("newsletter.filter.templates"),
                    languageManager.getText("newsletter.filter.newsletters")
            );

            // Restore selection or set default
            if (currentSelection != null) {
                if (currentSelection.equals("All Items") || currentSelection.equals(languageManager.getText("newsletter.filter.all"))) {
                    typeFilterCombo.setValue(languageManager.getText("newsletter.filter.all"));
                } else if (currentSelection.equals("Templates") || currentSelection.equals(languageManager.getText("newsletter.filter.templates"))) {
                    typeFilterCombo.setValue(languageManager.getText("newsletter.filter.templates"));
                } else if (currentSelection.equals("Newsletters") || currentSelection.equals(languageManager.getText("newsletter.filter.newsletters"))) {
                    typeFilterCombo.setValue(languageManager.getText("newsletter.filter.newsletters"));
                }
            } else {
                typeFilterCombo.setValue(languageManager.getText("newsletter.filter.all"));
            }
        }
    }

    private void initializeServices() {
        // Initialize database connection
        initializeDatabase();

        // Initialize services
        newsletterService = new NewsletterService(dbConnection);
        templateManager = new TemplateManager();
        htmlGenerator = new NewsletterHtmlGenerator();
        componentBuilder = new NewsletterComponentBuilder();

        // Initialize data structures
        newsletterList = FXCollections.observableArrayList();
        newsletterHTML = new StringBuilder();
    }

    private void initializeDatabase() {
        try {
            String dbPath = "newsletter_builder.db";
            String url = "jdbc:sqlite:" + dbPath;

            this.dbConnection = DriverManager.getConnection(url);
            createTablesIfNotExist();

            System.out.println("Database connection initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            showError(languageManager.getText("newsletter.database.error") + "\n" + e.getMessage());
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS newsletter_template (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255) NOT NULL,
                subject VARCHAR(500),
                content TEXT,
                template_type VARCHAR(100),
                is_active INTEGER DEFAULT 1,
                created_at TEXT,
                updated_at TEXT,
                description TEXT,
                status VARCHAR(50) DEFAULT 'DRAFT'
            )
            """;

        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute(createTableSQL);

            // Add status column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE newsletter_template ADD COLUMN status VARCHAR(50) DEFAULT 'DRAFT'");
            } catch (SQLException e) {
                // Column might already exist, ignore error
                System.out.println("Status column already exists or couldn't be added: " + e.getMessage());
            }

            System.out.println("Database tables created/verified successfully");
        }
    }

    private void initializeUI() {
        setupTableColumns();
        setupComboBoxes();
        setupEventHandlers();
        setupFormValidation();
        setupInitialValues();
    }

    private void setupComboBoxes() {
        // Template combo
        templateCombo.getItems().addAll(templateManager.getAvailableTemplates());
        templateCombo.setValue("Modern Clean");

        // Type/Status combo (using template type as status)
        typeCombo.getItems().addAll(newsletterService.getAvailableStatuses());
        typeCombo.setValue("DRAFT");

        // Filter combo - will be updated in updateTexts()
        typeFilterCombo.getItems().addAll("All Items", "Templates", "Newsletters");
        typeFilterCombo.setValue("All Items");
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("templateType"));

        // Use a custom cell value factory for status column
        statusColumn.setCellValueFactory(cellData -> {
            NewsletterTemplate template = cellData.getValue();
            String status = newsletterService.getNewsletterStatus(template);
            return new SimpleStringProperty(status);
        });

        createdColumn.setCellValueFactory(cellData -> {
            LocalDateTime created = cellData.getValue().getCreatedAt();
            if (created != null) {
                return new SimpleStringProperty(created.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
            return new SimpleStringProperty("");
        });

        newsletterTable.setItems(newsletterList);
        newsletterTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    updateActionButtons(newSelection != null);
                    if (newSelection != null && !isEditing) {
                        loadNewsletterForViewing(newSelection);
                    }
                }
        );
    }

    private void setupEventHandlers() {
        // Header buttons
        newNewsletterButton.setOnAction(e -> createNewNewsletter());
        refreshButton.setOnAction(e -> refreshNewsletters());
        clearSearchButton.setOnAction(e -> clearSearch());

        // Search and filter
        searchField.textProperty().addListener((obs, oldText, newText) -> performSearch());
        typeFilterCombo.setOnAction(e -> performSearch());

        // Table actions
        editButton.setOnAction(e -> editSelectedNewsletter());
        duplicateButton.setOnAction(e -> duplicateSelectedNewsletter());
        deleteButton.setOnAction(e -> deleteSelectedNewsletter());

        // Component buttons
        addTextButton.setOnAction(e -> addTextComponent());
        addHeadingButton.setOnAction(e -> addHeadingComponent());
        addImageButton.setOnAction(e -> addImageComponent());
        addButtonButton.setOnAction(e -> addButtonComponent());
        addDividerButton.setOnAction(e -> addDividerComponent());

        // Editor buttons
        refreshPreviewButton.setOnAction(e -> generatePreview());
        exportHtmlButton.setOnAction(e -> exportToHTML());
        sendButton.setOnAction(e -> sendNewsletter());
        saveButton.setOnAction(e -> saveCurrentNewsletter());
        cancelButton.setOnAction(e -> cancelEditing());

        // Template selection
        templateCombo.setOnAction(e -> loadSelectedTemplate());

        // Auto-refresh preview
        contentEditor.textProperty().addListener((obs, oldText, newText) -> {
            if (isEditing) generatePreview();
        });
        subjectField.textProperty().addListener((obs, old, newVal) -> generatePreview());
        companyNameField.textProperty().addListener((obs, old, newVal) -> generatePreview());
        headerColorPicker.setOnAction(e -> generatePreview());
    }

    private void setupFormValidation() {
        newsletterNameField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
        subjectField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
        contentEditor.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
    }

    private void setupInitialValues() {
        companyNameField.setText(languageManager.getText("newsletter.default.company"));
        headerColorPicker.setValue(javafx.scene.paint.Color.web("#007bff"));
        contentEditor.setText(languageManager.getText("newsletter.default.content"));
    }

    private void loadInitialData() {
        loadNewsletters();
        clearEditor();
        updateUI();
    }

    // Newsletter Management Methods
    private void loadNewsletters() {
        try {
            newsletterList.clear();
            newsletterList.addAll(newsletterService.getAllNewsletters());
            updateStatusLabel(languageManager.getText("newsletter.status.loaded").replace("{0}", String.valueOf(newsletterList.size())));
            updateItemCount();
        } catch (Exception e) {
            showError(languageManager.getText("newsletter.error.load") + ": " + e.getMessage());
        }
    }

    private void performSearch() {
        try {
            String searchTerm = searchField.getText();
            String typeFilter = typeFilterCombo.getValue();

            newsletterList.clear();
            newsletterList.addAll(newsletterService.searchNewsletters(searchTerm, typeFilter));
            updateItemCount();
        } catch (Exception e) {
            showError(languageManager.getText("newsletter.error.search") + ": " + e.getMessage());
        }
    }

    private void clearSearch() {
        searchField.clear();
        typeFilterCombo.setValue(languageManager.getText("newsletter.filter.all"));
        loadNewsletters();
    }

    private void refreshNewsletters() {
        loadNewsletters();
        clearEditor();
        updateStatusLabel(languageManager.getText("newsletter.status.refreshed"));
    }

    // Newsletter Editing Methods
    private void createNewNewsletter() {
        clearEditor();
        isEditing = true;
        currentNewsletter = new NewsletterTemplate();

        newsletterNameField.setText("");
        subjectField.setText("");
        companyNameField.setText(languageManager.getText("newsletter.default.company"));
        typeCombo.setValue("DRAFT"); // Start as draft
        templateCombo.setValue("Modern Clean");

        loadSelectedTemplate();
        updateUI();
        newsletterNameField.requestFocus();
        updateStatusLabel(languageManager.getText("newsletter.status.creating"));
    }

    private void editSelectedNewsletter() {
        NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadNewsletterForEditing(selected);
        }
    }

    private void loadNewsletterForEditing(NewsletterTemplate newsletter) {
        isEditing = true;
        currentNewsletter = newsletter;

        newsletterNameField.setText(newsletter.getName());
        subjectField.setText(newsletter.getSubject());
        typeCombo.setValue(newsletter.getTemplateType());
        contentEditor.setText(htmlGenerator.extractContentFromHtml(newsletter.getContent()));

        htmlGenerator.extractDetailsFromContent(newsletter.getContent(),
                companyName -> companyNameField.setText(companyName));

        generatePreview();
        updateUI();
        updateStatusLabel(languageManager.getText("newsletter.status.editing").replace("{0}", newsletter.getName()));
    }

    private void loadNewsletterForViewing(NewsletterTemplate newsletter) {
        isEditing = false;
        currentNewsletter = newsletter;

        newsletterNameField.setText(newsletter.getName());
        subjectField.setText(newsletter.getSubject());
        typeCombo.setValue(newsletter.getTemplateType());
        contentEditor.setText(htmlGenerator.extractContentFromHtml(newsletter.getContent()));

        htmlGenerator.extractDetailsFromContent(newsletter.getContent(),
                companyName -> companyNameField.setText(companyName));

        generatePreview();
        updateUI();
        updateStatusLabel(languageManager.getText("newsletter.status.viewing").replace("{0}", newsletter.getName()));
    }

    private void loadSelectedTemplate() {
        String selectedTemplate = templateCombo.getValue();
        if (selectedTemplate != null && isEditing) {
            String templateContent = templateManager.getTemplateContent(selectedTemplate);
            contentEditor.setText(templateContent);
            generatePreview();
        }
    }

    private void duplicateSelectedNewsletter() {
        NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(languageManager.getText("newsletter.duplicate.prefix") + " " + selected.getName());
            dialog.setTitle(languageManager.getText("newsletter.duplicate.title"));
            dialog.setHeaderText(languageManager.getText("newsletter.duplicate.header").replace("{0}", selected.getName()));
            dialog.setContentText(languageManager.getText("newsletter.duplicate.content"));

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newName = result.get().trim();
                try {
                    NewsletterTemplate duplicate = newsletterService.duplicateNewsletter(selected.getId(), newName);
                    if (duplicate != null) {
                        loadNewsletters();
                        selectNewsletterInTable(duplicate);
                        updateStatusLabel(languageManager.getText("newsletter.status.duplicated"));
                    } else {
                        showError(languageManager.getText("newsletter.error.duplicate"));
                    }
                } catch (Exception e) {
                    showError(languageManager.getText("newsletter.error.duplicate") + ": " + e.getMessage());
                }
            }
        }
    }

    private void deleteSelectedNewsletter() {
        NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(languageManager.getText("newsletter.delete.title"));
            confirmation.setHeaderText(languageManager.getText("newsletter.delete.header").replace("{0}", selected.getName()));
            confirmation.setContentText(languageManager.getText("newsletter.delete.content"));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (newsletterService.deleteNewsletter(selected.getId())) {
                        loadNewsletters();
                        clearEditor();
                        updateStatusLabel(languageManager.getText("newsletter.status.deleted"));
                    } else {
                        if (!newsletterService.isDatabaseConnected()) {
                            newsletterList.remove(selected);
                            updateStatusLabel(languageManager.getText("newsletter.status.removed.demo"));
                        } else {
                            showError(languageManager.getText("newsletter.error.delete"));
                        }
                    }
                } catch (Exception e) {
                    showError(languageManager.getText("newsletter.error.delete") + ": " + e.getMessage());
                }
            }
        }
    }

    // Component Addition Methods
    private void addTextComponent() {
        TextInputDialog dialog = new TextInputDialog(languageManager.getText("newsletter.component.text.default"));
        dialog.setTitle(languageManager.getText("newsletter.component.text.title"));
        dialog.setHeaderText(languageManager.getText("newsletter.component.text.header"));
        dialog.setContentText(languageManager.getText("newsletter.component.text.content"));

        dialog.showAndWait().ifPresent(text -> {
            String component = componentBuilder.createTextComponent(text);
            insertComponent(component);
        });
    }

    private void addHeadingComponent() {
        TextInputDialog dialog = new TextInputDialog(languageManager.getText("newsletter.component.heading.default"));
        dialog.setTitle(languageManager.getText("newsletter.component.heading.title"));
        dialog.setHeaderText(languageManager.getText("newsletter.component.heading.header"));
        dialog.setContentText(languageManager.getText("newsletter.component.heading.content"));

        dialog.showAndWait().ifPresent(heading -> {
            String component = componentBuilder.createHeadingComponent(heading);
            insertComponent(component);
        });
    }

    private void addImageComponent() {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle(languageManager.getText("newsletter.component.image.title"));
        dialog.setHeaderText(languageManager.getText("newsletter.component.image.header"));
        dialog.setContentText(languageManager.getText("newsletter.component.image.content"));

        dialog.showAndWait().ifPresent(imageUrl -> {
            String component = componentBuilder.createImageComponent(imageUrl);
            insertComponent(component);
        });
    }

    private void addButtonComponent() {
        Dialog<NewsletterComponentBuilder.ButtonResult> dialog = new Dialog<>();
        dialog.setTitle(languageManager.getText("newsletter.component.button.title"));
        dialog.setHeaderText(languageManager.getText("newsletter.component.button.header"));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField buttonText = new TextField(languageManager.getText("newsletter.component.button.default.text"));
        TextField buttonUrl = new TextField("https://");
        ComboBox<String> buttonColor = new ComboBox<>();
        buttonColor.getItems().addAll(
                languageManager.getText("newsletter.component.button.color.blue"),
                languageManager.getText("newsletter.component.button.color.green"),
                languageManager.getText("newsletter.component.button.color.orange"),
                languageManager.getText("newsletter.component.button.color.red")
        );
        buttonColor.setValue(languageManager.getText("newsletter.component.button.color.blue"));

        grid.add(new Label(languageManager.getText("newsletter.component.button.text.label")), 0, 0);
        grid.add(buttonText, 1, 0);
        grid.add(new Label(languageManager.getText("newsletter.component.button.url.label")), 0, 1);
        grid.add(buttonUrl, 1, 1);
        grid.add(new Label(languageManager.getText("newsletter.component.button.color.label")), 0, 2);
        grid.add(buttonColor, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new NewsletterComponentBuilder.ButtonResult(
                        buttonText.getText(), buttonUrl.getText(), buttonColor.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String colorStyle = componentBuilder.getButtonColorStyle(result.getColor());
            String component = componentBuilder.createButtonComponent(
                    result.getText(), result.getUrl(), colorStyle);
            insertComponent(component);
        });
    }

    private void addDividerComponent() {
        String component = componentBuilder.createDividerComponent();
        insertComponent(component);
    }

    private void insertComponent(String component) {
        String currentContent = contentEditor.getText();
        int caretPosition = contentEditor.getCaretPosition();
        String newContent = currentContent.substring(0, caretPosition) + component +
                currentContent.substring(caretPosition);
        contentEditor.setText(newContent);
        contentEditor.positionCaret(caretPosition + component.length());
        generatePreview();
    }

    // HTML Generation and Preview
    private void generatePreview() {
        String finalHTML = htmlGenerator.generateNewsletterHtml(
                subjectField.getText(),
                companyNameField.getText(),
                contentEditor.getText(),
                headerColorPicker.getValue()
        );

        previewWebView.getEngine().loadContent(finalHTML);
        newsletterHTML = new StringBuilder(finalHTML);
    }

    // Save and Export Methods
    private void saveCurrentNewsletter() {
        if (!isEditing || currentNewsletter == null) return;

        String name = newsletterNameField.getText().trim();
        String subject = subjectField.getText().trim();
        String type = typeCombo.getValue();

        if (name.isEmpty()) {
            showError(languageManager.getText("newsletter.validation.name.required"));
            newsletterNameField.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            showError(languageManager.getText("newsletter.validation.subject.required"));
            subjectField.requestFocus();
            return;
        }

        try {
            currentNewsletter.setName(name);
            currentNewsletter.setSubject(subject);
            currentNewsletter.setContent(newsletterHTML.toString());
            currentNewsletter.setTemplateType(type);

            NewsletterTemplate saved = newsletterService.saveNewsletter(currentNewsletter);
            if (saved != null) {
                if (newsletterService.isDatabaseConnected()) {
                    loadNewsletters();
                    selectNewsletterInTable(saved);
                } else {
                    // Demo mode
                    if (!newsletterList.contains(currentNewsletter)) {
                        newsletterList.add(currentNewsletter);
                    }
                }
                isEditing = false;
                updateUI();
                String statusMessage = languageManager.getText("newsletter.status.saved");
                if (!newsletterService.isDatabaseConnected()) {
                    statusMessage += " " + languageManager.getText("newsletter.status.demo.mode");
                }
                updateStatusLabel(statusMessage);
            } else {
                showError(languageManager.getText("newsletter.error.save"));
            }
        } catch (Exception e) {
            showError(languageManager.getText("newsletter.error.save") + ": " + e.getMessage());
        }
    }

    private void cancelEditing() {
        if (isEditing) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(languageManager.getText("newsletter.cancel.title"));
            confirmation.setHeaderText(languageManager.getText("newsletter.cancel.header"));
            confirmation.setContentText(languageManager.getText("newsletter.cancel.content"));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                isEditing = false;
                NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadNewsletterForViewing(selected);
                } else {
                    clearEditor();
                }
                updateUI();
                updateStatusLabel(languageManager.getText("newsletter.status.cancelled"));
            }
        }
    }

    private void clearEditor() {
        isEditing = false;
        currentNewsletter = null;
        newsletterNameField.clear();
        subjectField.clear();
        companyNameField.setText(languageManager.getText("newsletter.default.company"));
        typeCombo.setValue("DRAFT");
        templateCombo.setValue("Modern Clean");
        contentEditor.setText(languageManager.getText("newsletter.default.content"));
        previewWebView.getEngine().loadContent("");
        updateUI();
    }

    private void exportToHTML() {
        if (newsletterHTML == null || newsletterHTML.toString().trim().isEmpty()) {
            generatePreview();
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(languageManager.getText("newsletter.export.title"));
            fileChooser.setInitialFileName("newsletter_export.html");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(languageManager.getText("newsletter.export.filter"), "*.html")
            );

            Stage stage = (Stage) exportHtmlButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(newsletterHTML.toString());

                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(newsletterHTML.toString());
                    Clipboard.getSystemClipboard().setContent(clipboardContent);

                    updateStatusLabel(languageManager.getText("newsletter.status.exported"));
                }
            }
        } catch (Exception e) {
            showError(languageManager.getText("newsletter.error.export") + ": " + e.getMessage());
        }
    }

    private void sendNewsletter() {
        // Generate the latest preview to ensure we have current content
        generatePreview();

        // Validate that we have content to send
        if (newsletterHTML == null || newsletterHTML.toString().trim().isEmpty()) {
            showError(languageManager.getText("newsletter.send.no.content"));
            return;
        }

        // Validate required fields
        String title = subjectField.getText().trim();
        if (title.isEmpty()) {
            showError(languageManager.getText("newsletter.send.no.subject"));
            subjectField.requestFocus();
            return;
        }

        String company = companyNameField.getText().trim();
        if (company.isEmpty()) {
            showError(languageManager.getText("newsletter.send.no.company"));
            companyNameField.requestFocus();
            return;
        }

        try {
            // Create and show the newsletter send dialog
            Stage currentStage = (Stage) sendButton.getScene().getWindow();
            NewsletterSendDialog sendDialog = new NewsletterSendDialog(
                    currentStage,
                    title,
                    newsletterHTML.toString(),
                    company
            );

            sendDialog.showAndWait();

            // Update status after dialog closes
            updateStatusLabel(languageManager.getText("newsletter.status.send.dialog.closed"));

        } catch (Exception e) {
            System.err.println("Error showing newsletter send dialog: " + e.getMessage());
            e.printStackTrace();
            showError(languageManager.getText("newsletter.error.send.dialog") + ": " + e.getMessage());
        }
    }

    // UI Helper Methods
    private void updateSaveButtonState() {
        boolean isValid = !newsletterNameField.getText().trim().isEmpty() &&
                !subjectField.getText().trim().isEmpty() &&
                !contentEditor.getText().trim().isEmpty();
        saveButton.setDisable(!isValid || !isEditing);
    }

    private void updateActionButtons(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    private void updateUI() {
        newsletterNameField.setEditable(isEditing);
        subjectField.setEditable(isEditing);
        companyNameField.setEditable(isEditing);
        typeCombo.setDisable(!isEditing);
        templateCombo.setDisable(!isEditing);
        headerColorPicker.setDisable(!isEditing);
        contentEditor.setEditable(isEditing);

        addTextButton.setDisable(!isEditing);
        addHeadingButton.setDisable(!isEditing);
        addImageButton.setDisable(!isEditing);
        addButtonButton.setDisable(!isEditing);
        addDividerButton.setDisable(!isEditing);
        saveButton.setDisable(!isEditing);
        cancelButton.setDisable(!isEditing);

        updateSaveButtonState();

        if (isEditing && editorTabPane.getSelectionModel().getSelectedIndex() == 2) {
            editorTabPane.getSelectionModel().select(0);
        }
    }

    private void selectNewsletterInTable(NewsletterTemplate newsletter) {
        if (newsletter != null) {
            newsletterTable.getSelectionModel().select(newsletter);
            newsletterTable.scrollTo(newsletter);
        }
    }

    private void updateItemCount() {
        int count = newsletterList.size();
        String countText = count + " " + (count != 1 ?
                languageManager.getText("newsletter.count.items") :
                languageManager.getText("newsletter.count.item"));
        itemCountLabel.setText(countText);
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                    if (statusLabel.getText().equals(message)) {
                        statusLabel.setText(languageManager.getText("newsletter.status.ready"));
                    }
                })
        );
        timeline.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(languageManager.getText("newsletter.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        updateStatusLabel(languageManager.getText("newsletter.error.prefix") + ": " + message);
    }

    // Public API Methods
    public void setDatabaseConnection(Connection connection) {
        this.dbConnection = connection;
        this.newsletterService = new NewsletterService(connection);
        loadNewsletters();
    }

    public void refreshData() {
        loadNewsletters();
    }

    public NewsletterTemplate getSelectedNewsletter() {
        return newsletterTable.getSelectionModel().getSelectedItem();
    }

    // Method to update translations from external calls
    public void refreshTranslations() {
        updateTexts();
    }
}