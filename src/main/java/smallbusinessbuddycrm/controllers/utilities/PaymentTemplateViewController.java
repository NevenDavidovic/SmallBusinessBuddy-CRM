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

public class PaymentTemplateViewController implements Initializable {

    @FXML private TableView<PaymentTemplate> templatesTable;

    // Table columns
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

    // Data lists
    private ObservableList<PaymentTemplate> allTemplatesList = FXCollections.observableArrayList();
    private FilteredList<PaymentTemplate> filteredTemplatesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PaymentTemplateViewController.initialize() called");

        // Initialize translations first



        setupTable();
        setupSearchAndFilters();
        loadPaymentTemplates();
        setupEventHandlers();
        updateTexts();
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);

        System.out.println("PaymentTemplateViewController initialized successfully");
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update main title
        if (titleLabel != null) {
            titleLabel.setText(lm.getText("payment.template.title"));
        }

        // Update buttons
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

        // Update search field
        if (searchField != null) {
            searchField.setPromptText(lm.getText("payment.template.search.placeholder"));
        }

        // Update table columns
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

        // Update table placeholder
        if (templatesTable != null) {
            templatesTable.setPlaceholder(new Label(lm.getText("payment.template.table.placeholder")));
        }

        // Update record count
        updateRecordCount();

        System.out.println("Payment Template view texts updated");
    }

    private void setupTable() {
        LanguageManager lm = LanguageManager.getInstance();

        // Set up checkbox column
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

        // Set up edit button column
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

        // Set up column bindings
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

        // Style status column based on active status
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

    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredTemplatesList = new FilteredList<>(allTemplatesList, p -> true);

        // Set the table to use the filtered list
        templatesTable.setItems(filteredTemplatesList);

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredTemplatesList.setPredicate(template -> {
            // If no search text, show all templates based on current filter
            if (searchText.isEmpty()) {
                return matchesCurrentFilter(template);
            }

            // Check if search text matches name, description, or amount
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

    private boolean matchesCurrentFilter(PaymentTemplate template) {
        // Check which filter button is active based on their style
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

        return true; // Default: show all
    }

    private void loadPaymentTemplates() {
        try {
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            List<PaymentTemplate> templates = dao.getAllPaymentTemplates();

            // Add selection property to templates
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

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createTemplateButton.setOnAction(e -> handleCreateTemplate());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        toggleStatusButton.setOnAction(e -> handleToggleStatus());

        // Filter buttons
        allTemplatesButton.setOnAction(e -> handleFilterButton(allTemplatesButton));
        activeTemplatesButton.setOnAction(e -> handleFilterButton(activeTemplatesButton));
        inactiveTemplatesButton.setOnAction(e -> handleFilterButton(inactiveTemplatesButton));

        System.out.println("Event handlers setup completed");
    }

    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive
        allTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        activeTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        inactiveTemplatesButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter
        updateFilters();
    }

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

        // Confirm deletion
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

    // Refresh language when changed
    public void refreshLanguage() {
        updateTexts();
        loadPaymentTemplates(); // Reload to update count text and status text
    }

    // Utility methods for alerts
    private void showSuccessAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lm.getText("payment.template.alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lm.getText("payment.template.alert.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(lm.getText("payment.template.alert.warning.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String header, String content) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(header);
        confirmAlert.setContentText(content);
        return confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}