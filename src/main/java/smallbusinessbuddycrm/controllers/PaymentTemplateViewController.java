package smallbusinessbuddycrm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.database.DatabaseConnection;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentTemplateViewController {

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

    @FXML
    public void initialize() {
        System.out.println("PaymentTemplateViewController.initialize() called");

        // Initialize database first
        DatabaseConnection.initializeDatabase();

        setupTable();
        setupSearchAndFilters();
        loadPaymentTemplates();
        setupEventHandlers();

        System.out.println("PaymentTemplateViewController initialized successfully");
    }

    private void setupTable() {
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
            private final Button editButton = new Button("Edit");

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
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
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
                    if ("Active".equals(item)) {
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
        }
    }

    private void updateRecordCount() {
        int count = filteredTemplatesList.size();
        recordCountLabel.setText(count + " template" + (count != 1 ? "s" : ""));
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

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Template Updated");
                successAlert.setContentText("Payment template has been successfully updated.");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error opening edit template dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        // Get all selected templates from the filtered list
        List<PaymentTemplate> selectedTemplates = filteredTemplatesList.stream()
                .filter(PaymentTemplate::isSelected)
                .collect(Collectors.toList());

        if (selectedTemplates.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No templates selected");
            alert.setContentText("Please select one or more payment templates to delete using the checkboxes.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete selected payment templates?");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedTemplates.size() +
                " template" + (selectedTemplates.size() > 1 ? "s" : "") + "? This action cannot be undone.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
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

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Templates deleted");
                    successAlert.setContentText("Successfully deleted " + selectedTemplates.size() + " template(s).");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Delete Failed");
                    errorAlert.setContentText("Failed to delete the selected templates from the database.");
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting templates: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleToggleStatus() {
        // Get all selected templates from the filtered list
        List<PaymentTemplate> selectedTemplates = filteredTemplatesList.stream()
                .filter(PaymentTemplate::isSelected)
                .collect(Collectors.toList());

        if (selectedTemplates.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No templates selected");
            alert.setContentText("Please select one or more payment templates to toggle their status using the checkboxes.");
            alert.showAndWait();
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

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Status Updated");
                successAlert.setContentText("Successfully toggled status for " + successCount + " template(s).");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Toggle Failed");
                errorAlert.setContentText("Failed to toggle status for the selected templates.");
                errorAlert.showAndWait();
            }

        } catch (Exception e) {
            System.err.println("Error toggling template status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}