package smallbusinessbuddycrm.controllers;

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
import smallbusinessbuddycrm.services.*;
import smallbusinessbuddycrm.services.newsletter.NewsletterComponentBuilder;
import smallbusinessbuddycrm.services.newsletter.NewsletterHtmlGenerator;
import smallbusinessbuddycrm.services.newsletter.NewsletterService;
import smallbusinessbuddycrm.services.newsletter.TemplateManager;

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

    // FXML Fields
    @FXML private Button newNewsletterButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private TableView<NewsletterTemplate> newsletterTable;
    @FXML private TableColumn<NewsletterTemplate, String> nameColumn;
    @FXML private TableColumn<NewsletterTemplate, String> typeColumn;
    @FXML private TableColumn<NewsletterTemplate, String> statusColumn;
    @FXML private TableColumn<NewsletterTemplate, String> createdColumn;
    @FXML private Button editButton;
    @FXML private Button duplicateButton;
    @FXML private Button deleteButton;
    @FXML private TabPane editorTabPane;
    @FXML private TextField newsletterNameField;
    @FXML private TextField subjectField;
    @FXML private TextField companyNameField;
    @FXML private ColorPicker headerColorPicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> templateCombo;
    @FXML private TextArea contentEditor;
    @FXML private WebView previewWebView;
    @FXML private Button addTextButton;
    @FXML private Button addHeadingButton;
    @FXML private Button addImageButton;
    @FXML private Button addButtonButton;
    @FXML private Button addDividerButton;
    @FXML private Button refreshPreviewButton;
    @FXML private Button exportHtmlButton;
    @FXML private Button sendButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    @FXML private Label itemCountLabel;

    // Services
    private NewsletterService newsletterService;
    private TemplateManager templateManager;
    private NewsletterHtmlGenerator htmlGenerator;
    private NewsletterComponentBuilder componentBuilder;

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
            initializeServices();
            initializeUI();
            loadInitialData();

            System.out.println("Newsletter Builder initialized successfully");
        } catch (Exception e) {
            showError("Failed to initialize Newsletter Builder: " + e.getMessage());
            e.printStackTrace();
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
            showError("Database connection failed. Newsletter saving will be disabled.\n" + e.getMessage());
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

        // Filter combo
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
        companyNameField.setText("Your Company Name");
        headerColorPicker.setValue(javafx.scene.paint.Color.web("#007bff"));
        contentEditor.setText("<p>Start building your newsletter content here...</p>");
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
            updateStatusLabel("Loaded " + newsletterList.size() + " items");
            updateItemCount();
        } catch (Exception e) {
            showError("Failed to load newsletters: " + e.getMessage());
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
            showError("Search failed: " + e.getMessage());
        }
    }

    private void clearSearch() {
        searchField.clear();
        typeFilterCombo.setValue("All Items");
        loadNewsletters();
    }

    private void refreshNewsletters() {
        loadNewsletters();
        clearEditor();
        updateStatusLabel("Items refreshed");
    }

    // Newsletter Editing Methods
    private void createNewNewsletter() {
        clearEditor();
        isEditing = true;
        currentNewsletter = new NewsletterTemplate();

        newsletterNameField.setText("");
        subjectField.setText("");
        companyNameField.setText("Your Company Name");
        typeCombo.setValue("DRAFT"); // Start as draft
        templateCombo.setValue("Modern Clean");

        loadSelectedTemplate();
        updateUI();
        newsletterNameField.requestFocus();
        updateStatusLabel("Creating new newsletter");
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
        updateStatusLabel("Editing: " + newsletter.getName());
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
        updateStatusLabel("Viewing: " + newsletter.getName());
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
            TextInputDialog dialog = new TextInputDialog("Copy of " + selected.getName());
            dialog.setTitle("Duplicate Newsletter");
            dialog.setHeaderText("Create a copy of: " + selected.getName());
            dialog.setContentText("New name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newName = result.get().trim();
                try {
                    NewsletterTemplate duplicate = newsletterService.duplicateNewsletter(selected.getId(), newName);
                    if (duplicate != null) {
                        loadNewsletters();
                        selectNewsletterInTable(duplicate);
                        updateStatusLabel("Newsletter duplicated successfully");
                    } else {
                        showError("Failed to duplicate newsletter");
                    }
                } catch (Exception e) {
                    showError("Duplication failed: " + e.getMessage());
                }
            }
        }
    }

    private void deleteSelectedNewsletter() {
        NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Newsletter");
            confirmation.setHeaderText("Delete: " + selected.getName());
            confirmation.setContentText("This action cannot be undone. Are you sure?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (newsletterService.deleteNewsletter(selected.getId())) {
                        loadNewsletters();
                        clearEditor();
                        updateStatusLabel("Newsletter deleted");
                    } else {
                        if (!newsletterService.isDatabaseConnected()) {
                            newsletterList.remove(selected);
                            updateStatusLabel("Newsletter removed (demo mode)");
                        } else {
                            showError("Failed to delete newsletter");
                        }
                    }
                } catch (Exception e) {
                    showError("Deletion failed: " + e.getMessage());
                }
            }
        }
    }

    // Component Addition Methods
    private void addTextComponent() {
        TextInputDialog dialog = new TextInputDialog("Enter your text content here...");
        dialog.setTitle("Add Text Component");
        dialog.setHeaderText("Text Content");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            String component = componentBuilder.createTextComponent(text);
            insertComponent(component);
        });
    }

    private void addHeadingComponent() {
        TextInputDialog dialog = new TextInputDialog("Newsletter Heading");
        dialog.setTitle("Add Heading Component");
        dialog.setHeaderText("Heading Content");
        dialog.setContentText("Heading:");

        dialog.showAndWait().ifPresent(heading -> {
            String component = componentBuilder.createHeadingComponent(heading);
            insertComponent(component);
        });
    }

    private void addImageComponent() {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle("Add Image Component");
        dialog.setHeaderText("Image URL");
        dialog.setContentText("Enter image URL:");

        dialog.showAndWait().ifPresent(imageUrl -> {
            String component = componentBuilder.createImageComponent(imageUrl);
            insertComponent(component);
        });
    }

    private void addButtonComponent() {
        Dialog<NewsletterComponentBuilder.ButtonResult> dialog = new Dialog<>();
        dialog.setTitle("Add Button Component");
        dialog.setHeaderText("Button Configuration");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField buttonText = new TextField("Click Here");
        TextField buttonUrl = new TextField("https://");
        ComboBox<String> buttonColor = new ComboBox<>();
        buttonColor.getItems().addAll("Primary Blue", "Success Green", "Warning Orange", "Danger Red");
        buttonColor.setValue("Primary Blue");

        grid.add(new Label("Button Text:"), 0, 0);
        grid.add(buttonText, 1, 0);
        grid.add(new Label("Link URL:"), 0, 1);
        grid.add(buttonUrl, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
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
            showError("Newsletter name is required");
            newsletterNameField.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            showError("Subject line is required");
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
                updateStatusLabel("Newsletter saved successfully" +
                        (newsletterService.isDatabaseConnected() ? "" : " (demo mode)"));
            } else {
                showError("Failed to save newsletter");
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
                NewsletterTemplate selected = newsletterTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadNewsletterForViewing(selected);
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
        currentNewsletter = null;
        newsletterNameField.clear();
        subjectField.clear();
        companyNameField.setText("Your Company Name");
        typeCombo.setValue("DRAFT");
        templateCombo.setValue("Modern Clean");
        contentEditor.setText("<p>Start building your newsletter content here...</p>");
        previewWebView.getEngine().loadContent("");
        updateUI();
    }

    private void exportToHTML() {
        if (newsletterHTML == null || newsletterHTML.toString().trim().isEmpty()) {
            generatePreview();
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Newsletter as HTML");
            fileChooser.setInitialFileName("newsletter_export.html");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            Stage stage = (Stage) exportHtmlButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(newsletterHTML.toString());

                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(newsletterHTML.toString());
                    Clipboard.getSystemClipboard().setContent(clipboardContent);

                    updateStatusLabel("Newsletter exported and copied to clipboard!");
                }
            }
        } catch (Exception e) {
            showError("Failed to export newsletter: " + e.getMessage());
        }
    }

    private void sendNewsletter() {
        generatePreview();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Send Newsletter");
        info.setHeaderText("Email Integration");
        info.setContentText("Email sending functionality would be integrated here.\n\n" +
                "The newsletter HTML has been copied to your clipboard.\n" +
                "You can paste it into your email marketing platform.");

        if (newsletterHTML != null) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(newsletterHTML.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }

        info.showAndWait();
        updateStatusLabel("Newsletter copied to clipboard for sending");
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
        itemCountLabel.setText(count + " item" + (count != 1 ? "s" : ""));
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);

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
}