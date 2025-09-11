package smallbusinessbuddycrm.controllers.utilities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller class for managing payment templates in a comprehensive table-based interface.
 *
 * This controller provides full CRUD operations for payment templates with advanced features:
 * - Interactive table view with selectable rows and inline edit buttons
 * - Real-time search functionality across multiple template fields
 * - Filter system for viewing all, active, or inactive templates
 * - Batch operations for deletion and status toggling
 * - Template creation and editing via modal dialogs
 * - Complete internationalization support with dynamic language switching
 * - Responsive UI with live record counting and status indicators
 *
 * Key Features:
 * - Advanced Table Management: Checkbox selection, inline editing, conditional styling
 * - Search & Filter System: Real-time filtering by text search and status categories
 * - CRUD Operations: Create, read, update, delete with user confirmation dialogs
 * - Batch Operations: Multi-select for bulk deletion and status changes
 * - Status Management: Toggle between active/inactive states with visual indicators
 * - Internationalization: Full language support with real-time UI updates
 * - Error Handling: Comprehensive error management with user-friendly feedback
 *
 * Table Features:
 * - Selection checkboxes for batch operations
 * - Inline edit buttons for quick template modification
 * - Status column with color-coded active/inactive indicators
 * - Sortable columns for name, description, amount, model, and dates
 * - Responsive layout with automatic column sizing
 *
 * Filter System:
 * - Text search across name, description, amount, and payment model
 * - Category filters: All Templates, Active Only, Inactive Only
 * - Real-time filter application with instant results
 * - Visual filter button states with active/inactive styling
 *
 * The controller integrates with PaymentTemplateDAO for database operations and
 * provides a complete template management solution with professional UI/UX.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class PaymentTemplateViewController implements Initializable {

    // Table View and Columns
    @FXML private TableView<PaymentTemplate> templatesTable;
    @FXML private TableColumn<PaymentTemplate, Boolean> selectColumn;
    @FXML private TableColumn<PaymentTemplate, Void> editColumn;
    @FXML private TableColumn<PaymentTemplate, String> nameColumn;
    @FXML private TableColumn<PaymentTemplate, String> descriptionColumn;
    @FXML private TableColumn<PaymentTemplate, String> amountColumn;
    @FXML private TableColumn<PaymentTemplate, String> modelColumn;
    @FXML private TableColumn<PaymentTemplate, String> referenceColumn;
    @FXML private TableColumn<PaymentTemplate, String> statusColumn;
    @FXML private TableColumn<PaymentTemplate, String> createdAtColumn;
    @FXML private TableColumn<PaymentTemplate, String> updatedAtColumn;

    // UI Controls
    @FXML private Label titleLabel;
    @FXML private Button createTemplateButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button toggleStatusButton;
    @FXML private Button allTemplatesButton;
    @FXML private Button activeTemplatesButton;
    @FXML private Button inactiveTemplatesButton;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;

    // Data Collections
    private ObservableList<PaymentTemplate> allTemplatesList = FXCollections.observableArrayList();
    private FilteredList<PaymentTemplate> filteredTemplatesList;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up table columns, search and filter functionality, loads payment templates,
     * configures event handlers, and initializes internationalization support.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PaymentTemplateViewController.initialize() called");

        setupTable();
        setupSearchAndFilters();
        loadPaymentTemplates();
        setupEventHandlers();
        updateTexts();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);

        System.out.println("PaymentTemplateViewController initialized successfully");
    }

    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh labels, buttons, table headers,
     * placeholders, and record count display with localized text.
     */
    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update main title
        if (titleLabel != null) {
            titleLabel.setText(lm.getText("payment.template.title"));
        }

        // Update action buttons
        if (createTemplateButton != null) {
            createTemplateButton.setText(lm.getText("payment.template.button.create"));
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setText(lm.getText("payment.template.button.delete.selected"));
        }
        if (toggleStatusButton != null) {
            toggleStatusButton.setText(lm.getText("payment.template.button.toggle.status"));
        }
        if (allTemplatesButton != null) {
            allTemplatesButton.setText(lm.getText("payment.template.filter.all"));
        }
        if (activeTemplatesButton != null) {
            activeTemplatesButton.setText(lm.getText("payment.template.filter.active"));
        }
        if (inactiveTemplatesButton != null) {
            inactiveTemplatesButton.setText(lm.getText("payment.template.filter.inactive"));
        }

        // Update search field placeholder
        if (searchField != null) {
            searchField.setPromptText(lm.getText("payment.template.search.placeholder"));
        }

        // Update table column headers
        if (editColumn != null) {
            editColumn.setText(lm.getText("payment.template.column.edit"));
        }
        if (nameColumn != null) {
            nameColumn.setText(lm.getText("payment.template.column.name"));
        }
        if (descriptionColumn != null) {
            descriptionColumn.setText(lm.getText("payment.template.column.description"));
        }
        if (amountColumn != null) {
            amountColumn.setText(lm.getText("payment.template.column.amount"));
        }
        if (modelColumn != null) {
            modelColumn.setText(lm.getText("payment.template.column.model"));
        }
        if (referenceColumn != null) {
            referenceColumn.setText(lm.getText("payment.template.column.reference"));
        }
        if (statusColumn != null) {
            statusColumn.setText(lm.getText("payment.template.column.status"));
        }
        if (createdAtColumn != null) {
            createdAtColumn.setText(lm.getText("payment.template.column.created"));
        }
        if (updatedAtColumn != null) {
            updatedAtColumn.setText(lm.getText("payment.template.column.updated"));
        }

        // Update table placeholder text
        if (templatesTable != null) {
            templatesTable.setPlaceholder(new Label(lm.getText("payment.template.table.placeholder")));
        }

        // Update record count display
        updateRecordCount();

        System.out.println("Payment Template view texts updated");
    }

    /**
     * Sets up table columns with appropriate cell factories and value factories.
     * Configures checkbox selection column, edit button column, data columns,
     * and applies conditional styling for status column based on active state.
     */
    private void setupTable() {
        LanguageManager lm = LanguageManager.getInstance();

        // Configure checkbox selection column
        selectColumn.setCellFactory(tc -> new TableCell<PaymentTemplate, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getIndex() >= templatesTable.getItems().size()) {
                    setGraphic(null);
                } else {
                    PaymentTemplate template = templatesTable.getItems().get(getIndex());
                    checkBox.setSelected(template.isSelected());
                    checkBox.setOnAction(event -> template.setSelected(checkBox.isSelected()));
                    setGraphic(checkBox);
                }
            }
        });

        // Configure edit button column
        editColumn.setCellFactory(tc -> new TableCell<PaymentTemplate, Void>() {
            private final Button editButton = new Button(lm.getText("payment.template.button.edit"));

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                editButton.setPrefWidth(50);
                editButton.setOnAction(event -> {
                    PaymentTemplate template = getTableView().getItems().get(getIndex());
                    handleEditTemplate(template);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // Configure data column bindings
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAmount() + " EUR"));
        modelColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getModelOfPayment()));
        referenceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPozivNaBroj()));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            String statusText = isActive ?
                    lm.getText("payment.template.status.active") :
                    lm.getText("payment.template.status.inactive");
            return new SimpleStringProperty(statusText);
        });
        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));
        updatedAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUpdatedAt()));

        // Apply conditional styling to status column
        statusColumn.setCellFactory(column -> new TableCell<PaymentTemplate, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals(lm.getText("payment.template.status.active"))) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    /**
     * Initializes search functionality and filtering system.
     * Creates filtered list wrapper around the main data list and sets up
     * real-time search listener that triggers filter updates on text changes.
     */
    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredTemplatesList = new FilteredList<>(allTemplatesList, p -> true);

        // Set the table to use the filtered list
        templatesTable.setItems(filteredTemplatesList);

        // Set up real-time search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    /**
     * Updates the filtered list based on current search text and active filter.
     * Applies search criteria across name, description, amount, and model fields,
     * combined with current filter state (all/active/inactive templates).
     */
    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredTemplatesList.setPredicate(template -> {
            // If no search text, show all templates based on current filter
            if (searchText.isEmpty()) {
                return matchesCurrentFilter(template);
            }

            // Check if search text matches any searchable fields
            boolean matchesSearch = false;

            if (template.getName() != null && template.getName().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (template.getDescription() != null && template.getDescription().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (template.getAmount() != null && template.getAmount().toString().contains(searchText)) {
                matchesSearch = true;
            } else if (template.getModelOfPayment() != null && template.getModelOfPayment().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            }

            // Return true only if matches both search and current filter
            return matchesSearch && matchesCurrentFilter(template);
        });

        updateRecordCount();
    }

    /**
     * Determines if a template matches the currently active filter button.
     * Checks which filter button is active based on styling and returns
     * whether the template should be displayed according to that filter.
     *
     * @param template The PaymentTemplate to check against current filter
     * @return true if template matches current filter criteria, false otherwise
     */
    private boolean matchesCurrentFilter(PaymentTemplate template) {
        // Check which filter button is active based on their styling
        String allTemplatesStyle = allTemplatesButton.getStyle();
        String activeTemplatesStyle = activeTemplatesButton.getStyle();
        String inactiveTemplatesStyle = inactiveTemplatesButton.getStyle();

        // If "All templates" is active (has #f5f8fa background)
        if (allTemplatesStyle.contains("#f5f8fa")) {
            return true; // Show all templates
        }
        // If "Active templates" is active
        else if (activeTemplatesStyle.contains("#f5f8fa")) {
            return template.isActive();
        }
        // If "Inactive templates" is active
        else if (inactiveTemplatesStyle.contains("#f5f8fa")) {
            return !template.isActive();
        }

        return true; // Default: show all templates
    }

    /**
     * Loads all payment templates from database and populates the table.
     * Fetches templates via DAO, initializes selection state, updates the
     * observable list, and handles any loading errors with user feedback.
     */
    private void loadPaymentTemplates() {
        try {
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            List<PaymentTemplate> templates = dao.getAllPaymentTemplates();

            // Initialize selection property for all templates
            templates.forEach(template -> template.setSelected(false));

            System.out.println("DAO returned " + templates.size() + " payment templates");

            allTemplatesList.setAll(templates);
            updateRecordCount();

        } catch (Exception e) {
            System.err.println("Error loading payment templates: " + e.getMessage());
            e.printStackTrace();
            LanguageManager lm = LanguageManager.getInstance();
            showErrorAlert(lm.getText("payment.template.error.load.failed") + ": " + e.getMessage());
        }
    }

    /**
     * Updates the record count label with current filtered template count.
     * Displays localized count text with appropriate singular/plural forms
     * based on the number of templates currently visible in the filtered list.
     */
    private void updateRecordCount() {
        if (recordCountLabel != null) {
            int count = filteredTemplatesList != null ? filteredTemplatesList.size() : 0;
            LanguageManager lm = LanguageManager.getInstance();
            String countText = count == 1 ?
                    lm.getText("payment.template.count.single").replace("{0}", String.valueOf(count)) :
                    lm.getText("payment.template.count.multiple").replace("{0}", String.valueOf(count));
            recordCountLabel.setText(countText);
        }
    }

    /**
     * Configures event handlers for all interactive UI components.
     * Sets up button click handlers for create, delete, toggle status,
     * and filter buttons with appropriate styling and behavior.
     */
    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        // Main action button handlers
        createTemplateButton.setOnAction(e -> handleCreateTemplate());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        toggleStatusButton.setOnAction(e -> handleToggleStatus());

        // Filter button handlers
        allTemplatesButton.setOnAction(e -> handleFilterButton(allTemplatesButton));
        activeTemplatesButton.setOnAction(e -> handleFilterButton(activeTemplatesButton));
        inactiveTemplatesButton.setOnAction(e -> handleFilterButton(inactiveTemplatesButton));

        System.out.println("Event handlers setup completed");
    }

    /**
     * Handles filter button clicks and updates active filter state.
     * Resets all filter button styles to inactive, sets clicked button to active,
     * and triggers filter update to refresh the displayed template list.
     *
     * @param clickedButton The filter button that was clicked
     */
    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive state
        allTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        activeTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        inactiveTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter to reflect new selection
        updateFilters();
    }

    /**
     * Handles creating a new payment template via dialog.
     * Opens create template dialog, processes the result if successful,
     * adds new template to the list, and updates table selection and display.
     */
    private void handleCreateTemplate() {
        try {
            Stage currentStage = (Stage) createTemplateButton.getScene().getWindow();
            CreatePaymentTemplateDialog dialog = new CreatePaymentTemplateDialog(currentStage);

            if (dialog.showAndWait()) {
                PaymentTemplate newTemplate = dialog.getResult();
                if (newTemplate != null) {
                    allTemplatesList.add(newTemplate);
                    updateRecordCount();
                    templatesTable.getSelectionModel().select(newTemplate);
                    templatesTable.scrollTo(newTemplate);
                    System.out.println("New payment template added: " + newTemplate.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening create template dialog: " + e.getMessage());
            e.printStackTrace();
            LanguageManager lm = LanguageManager.getInstance();
            showErrorAlert(lm.getText("payment.template.error.create.dialog.failed") + ": " + e.getMessage());
        }
    }

    /**
     * Handles editing an existing payment template via dialog.
     * Opens edit template dialog with current template data, processes updates,
     * refreshes table display, and provides user feedback on completion.
     *
     * @param template The PaymentTemplate to edit
     */
    private void handleEditTemplate(PaymentTemplate template) {
        try {
            Stage currentStage = (Stage) createTemplateButton.getScene().getWindow();
            EditPaymentTemplateDialog dialog = new EditPaymentTemplateDialog(currentStage, template);

            if (dialog.showAndWait()) {
                // Refresh the table to show updated data
                templatesTable.refresh();
                updateFilters(); // Re-apply filters in case status changed
                System.out.println("Payment template updated: " + template.getName());

                LanguageManager lm = LanguageManager.getInstance();
                showSuccessAlert(lm.getText("payment.template.success.template.updated"));
            }
        } catch (Exception e) {
            System.err.println("Error opening edit template dialog: " + e.getMessage());
            e.printStackTrace();
            LanguageManager lm = LanguageManager.getInstance();
            showErrorAlert(lm.getText("payment.template.error.edit.dialog.failed") + ": " + e.getMessage());
        }
    }

    /**
     * Handles deletion of selected payment templates.
     * Identifies selected templates, shows confirmation dialog, performs
     * batch deletion via DAO, updates UI, and provides user feedback.
     */
    private void handleDeleteSelected() {
        LanguageManager lm = LanguageManager.getInstance();

        // Get all selected templates from the filtered list
        List<PaymentTemplate> selectedTemplates = filteredTemplatesList.stream()
                .filter(PaymentTemplate::isSelected)
                .collect(Collectors.toList());

        if (selectedTemplates.isEmpty()) {
            showWarningAlert(lm.getText("payment.template.warning.no.selection.delete"));
            return;
        }

        // Show confirmation dialog
        String confirmMessage = lm.getText("payment.template.confirm.delete.content")
                .replace("{0}", String.valueOf(selectedTemplates.size()))
                .replace("{1}", selectedTemplates.size() > 1 ? "s" : "");

        if (showConfirmDialog(
                lm.getText("payment.template.confirm.delete.title"),
                lm.getText("payment.template.confirm.delete.header"),
                confirmMessage)) {

            try {
                PaymentTemplateDAO dao = new PaymentTemplateDAO();
                List<Integer> templateIds = selectedTemplates.stream()
                        .map(PaymentTemplate::getId)
                        .collect(Collectors.toList());

                boolean success = dao.deleteTemplates(templateIds);

                if (success) {
                    // Remove from the original list (filtered list will update automatically)
                    allTemplatesList.removeAll(selectedTemplates);
                    updateRecordCount();

                    String successMessage = lm.getText("payment.template.success.deleted")
                            .replace("{0}", String.valueOf(selectedTemplates.size()));
                    showSuccessAlert(successMessage);
                } else {
                    showErrorAlert(lm.getText("payment.template.error.delete.failed"));
                }

            } catch (Exception e) {
                System.err.println("Error deleting templates: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert(lm.getText("payment.template.error.delete.exception") + ": " + e.getMessage());
            }
        }
    }

    /**
     * Handles toggling active status of selected payment templates.
     * Identifies selected templates, toggles their active status via DAO,
     * updates local objects and UI display, and provides user feedback.
     */
    private void handleToggleStatus() {
        LanguageManager lm = LanguageManager.getInstance();

        // Get all selected templates from the filtered list
        List<PaymentTemplate> selectedTemplates = filteredTemplatesList.stream()
                .filter(PaymentTemplate::isSelected)
                .collect(Collectors.toList());

        if (selectedTemplates.isEmpty()) {
            showWarningAlert(lm.getText("payment.template.warning.no.selection.toggle"));
            return;
        }

        try {
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            int successCount = 0;

            for (PaymentTemplate template : selectedTemplates) {
                if (dao.toggleActiveStatus(template.getId())) {
                    template.setActive(!template.isActive()); // Update local object
                    successCount++;
                }
            }

            if (successCount > 0) {
                // Refresh the table and filters
                templatesTable.refresh();
                updateFilters();

                String successMessage = lm.getText("payment.template.success.status.toggled")
                        .replace("{0}", String.valueOf(successCount));
                showSuccessAlert(successMessage);
            } else {
                showErrorAlert(lm.getText("payment.template.error.toggle.failed"));
            }

        } catch (Exception e) {
            System.err.println("Error toggling template status: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert(lm.getText("payment.template.error.toggle.exception") + ": " + e.getMessage());
        }
    }

    /**
     * Refreshes the view when language settings change.
     * Updates all text elements and reloads templates to ensure
     * proper localization of all displayed content.
     */
    public void refreshLanguage() {
        updateTexts();
        loadPaymentTemplates(); // Reload to update count text and status text
    }

    /**
     * Displays a success message dialog with localized title.
     * Shows an information alert with the specified success message
     * using localized dialog title and formatting.
     *
     * @param message The success message to display
     */
    private void showSuccessAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lm.getText("payment.template.alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error message dialog with localized title.
     * Shows an error alert with the specified error message
     * using localized dialog title and formatting.
     *
     * @param message The error message to display
     */
    private void showErrorAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lm.getText("payment.template.alert.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a warning message dialog with localized title.
     * Shows a warning alert with the specified warning message
     * using localized dialog title and formatting.
     *
     * @param message The warning message to display
     */
    private void showWarningAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(lm.getText("payment.template.alert.warning.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog and returns user's choice.
     * Shows a confirmation alert with specified title, header, and content,
     * and returns true if user confirms (OK), false if cancelled.
     *
     * @param title The dialog title
     * @param header The dialog header text
     * @param content The dialog content message
     * @return true if user clicked OK, false if cancelled
     */
    private boolean showConfirmDialog(String title, String header, String content) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(header);
        confirmAlert.setContentText(content);
        return confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}