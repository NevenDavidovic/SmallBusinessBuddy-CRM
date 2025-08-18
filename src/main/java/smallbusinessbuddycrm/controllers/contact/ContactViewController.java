package smallbusinessbuddycrm.controllers.contact;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import smallbusinessbuddycrm.controllers.utilities.BarcodePaymentDialog;
import smallbusinessbuddycrm.controllers.utilities.GoogleOAuthController;
import smallbusinessbuddycrm.controllers.utilities.MultipleGenerationBarcodeDialog;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.database.ContactDAO;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ContactViewController {

    @FXML private TableView<Contact> contactsTable;

    // All table columns
    @FXML private TableColumn<Contact, Boolean> selectColumn;
    @FXML private TableColumn<Contact, Void> editColumn;
    @FXML private TableColumn<Contact, String> firstNameColumn;
    @FXML private TableColumn<Contact, String> lastNameColumn;
    @FXML private TableColumn<Contact, String> birthdayColumn;
    @FXML private TableColumn<Contact, String> ageColumn;
    @FXML private TableColumn<Contact, String> pinColumn;
    @FXML private TableColumn<Contact, String> emailColumn;
    @FXML private TableColumn<Contact, String> phoneColumn;
    @FXML private TableColumn<Contact, String> streetNameColumn;
    @FXML private TableColumn<Contact, String> streetNumColumn;
    @FXML private TableColumn<Contact, String> postalCodeColumn;
    @FXML private TableColumn<Contact, String> cityColumn;
    @FXML private TableColumn<Contact, String> memberStatusColumn;
    @FXML private TableColumn<Contact, String> memberSinceColumn;
    @FXML private TableColumn<Contact, String> memberUntilColumn;
    @FXML private TableColumn<Contact, String> createdAtColumn;
    @FXML private TableColumn<Contact, String> updatedAtColumn;
    @FXML private TableColumn<Contact, Void> barcodeColumn;

    // UI Controls
    @FXML private Button createContactButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button allContactsButton;
    @FXML private Button membersButton;
    @FXML private Button nonMembersButton;
    @FXML private Button exportButton;
    @FXML private Button editColumnsButton;
    @FXML private Button upcomingBirthdaysButton;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;
    @FXML private Button importButton;
    @FXML private Button generateBarcodeButton;
    @FXML private Label contactsPageTitle;

    // Data lists
    private ObservableList<Contact> allContactsList = FXCollections.observableArrayList();
    private FilteredList<Contact> filteredContactsList;

    // ⭐ NEW: Column visibility management integrated with settings
    private Map<String, TableColumn<Contact, String>> columnMap = new HashMap<>();
    private Map<String, Boolean> columnVisibility = new HashMap<>();
    private static final String COLUMN_PREFS_KEY = "contacts_column_visibility";

    // ⭐ NEW: Language management
    private LanguageManager languageManager;
    private Runnable languageChangeListener;

    @FXML
    public void initialize() {
        // ⭐ NEW: Initialize language manager
        languageManager = LanguageManager.getInstance();
        languageChangeListener = this::updateTexts;
        languageManager.addLanguageChangeListener(languageChangeListener);

        // ⭐ NEW: Load column visibility settings from shared preferences
        loadColumnVisibilitySettings();

        initializeColumnMapping();
        setupTable();
        setupSearchAndFilters();
        loadContacts();
        setupEventHandlers();

        // ⭐ UPDATED: Apply settings-based column visibility instead of defaults
        applyColumnVisibilityFromSettings();

        updateTexts();
    }

    // ⭐ NEW: Load column visibility settings from shared preferences (same as GoogleOAuthController)
    private void loadColumnVisibilitySettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GoogleOAuthController.class);

            // Default column visibility settings
            String[] columns = {
                    "First Name", "Last Name", "Birthday", "Age", "PIN",
                    "Email", "Phone Number", "Street Name", "Street Number",
                    "Postal Code", "City", "Member Status", "Member Since",
                    "Member Until", "Created", "Updated"
            };

            // Load saved settings or use defaults
            for (String column : columns) {
                String prefKey = COLUMN_PREFS_KEY + "." + column.replace(" ", "_");

                // Default visible columns (PIN and some others hidden by default)
                boolean defaultVisible = !column.equals("PIN") &&
                        !column.equals("Street Number") &&
                        !column.equals("Member Since") &&
                        !column.equals("Member Until") &&
                        !column.equals("Created") &&
                        !column.equals("Updated");

                boolean isVisible = prefs.getBoolean(prefKey, defaultVisible);
                columnVisibility.put(column, isVisible);
            }

            System.out.println("ContactViewController: Column visibility settings loaded: " + columnVisibility);

        } catch (Exception e) {
            System.err.println("Error loading column visibility settings: " + e.getMessage());
            initializeDefaultColumnVisibility();
        }
    }

    // ⭐ NEW: Initialize default column visibility
    private void initializeDefaultColumnVisibility() {
        String[] columns = {
                "First Name", "Last Name", "Birthday", "Age", "PIN",
                "Email", "Phone Number", "Street Name", "Street Number",
                "Postal Code", "City", "Member Status", "Member Since",
                "Member Until", "Created", "Updated"
        };

        for (String column : columns) {
            boolean defaultVisible = !column.equals("PIN") &&
                    !column.equals("Street Number") &&
                    !column.equals("Member Since") &&
                    !column.equals("Member Until") &&
                    !column.equals("Created") &&
                    !column.equals("Updated");
            columnVisibility.put(column, defaultVisible);
        }
    }

    // ⭐ UPDATED: Apply column visibility from settings instead of defaults
    private void applyColumnVisibilityFromSettings() {
        System.out.println("ContactViewController: Applying column visibility from settings...");

        for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
            String columnName = entry.getKey();
            boolean visible = entry.getValue();
            TableColumn<Contact, String> column = columnMap.get(columnName);

            if (column != null) {
                column.setVisible(visible);
                System.out.println("ContactViewController: Set " + columnName + " visibility to: " + visible);
            } else {
                System.out.println("WARNING: Column not found in map: " + columnName);
            }
        }
    }

    // ⭐ UPDATED: Enhanced handleEditColumns method that updates settings
    private void handleEditColumns() {
        System.out.println("ContactViewController: handleEditColumns() called");

        try {
            Stage currentStage = (Stage) editColumnsButton.getScene().getWindow();
            System.out.println("Creating EditColumnsDialog...");

            // Create a copy of current visibility settings
            Map<String, Boolean> currentVisibility = new HashMap<>(columnVisibility);
            EditColumnsDialog dialog = new EditColumnsDialog(currentStage, currentVisibility);

            System.out.println("Showing dialog...");
            if (dialog.showAndWait()) {
                System.out.println("Dialog returned OK, updating columns...");

                Map<String, Boolean> newVisibility = dialog.getColumnVisibility();

                // Update our internal map
                columnVisibility.putAll(newVisibility);

                // ⭐ NEW: Save settings to shared preferences
                saveColumnVisibilitySettings();

                // Apply the new visibility
                applyColumnVisibilityFromSettings();

                System.out.println("ContactViewController: Columns updated successfully");

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle(languageManager.getText("contacts.edit.columns.success.title"));
                successAlert.setHeaderText(languageManager.getText("contacts.edit.columns.success.header"));
                successAlert.setContentText(languageManager.getText("contacts.edit.columns.success.content"));
                successAlert.showAndWait();

            } else {
                System.out.println("Dialog was cancelled");
            }
        } catch (Exception e) {
            System.err.println("Error in handleEditColumns: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Edit Columns Failed");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    // ⭐ NEW: Save column visibility settings to shared preferences
    private void saveColumnVisibilitySettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GoogleOAuthController.class);

            for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
                String prefKey = COLUMN_PREFS_KEY + "." + entry.getKey().replace(" ", "_");
                prefs.putBoolean(prefKey, entry.getValue());
            }

            System.out.println("ContactViewController: Column visibility settings saved: " + columnVisibility);

        } catch (Exception e) {
            System.err.println("Error saving column visibility settings: " + e.getMessage());
        }
    }

    // ⭐ NEW: Method to refresh column visibility from settings (called externally)
    public void refreshColumnVisibilityFromSettings() {
        loadColumnVisibilitySettings();
        applyColumnVisibilityFromSettings();
        System.out.println("ContactViewController: Column visibility refreshed from settings");
    }

    // ⭐ NEW: Cleanup method to prevent memory leaks
    public void cleanup() {
        if (languageManager != null && languageChangeListener != null) {
            languageManager.removeLanguageChangeListener(languageChangeListener);
            System.out.println("ContactViewController: Language change listener removed");
        }
    }

    // ⭐ UPDATED: Enhanced updateTexts method
    private void updateTexts() {
        try {
            System.out.println("ContactViewController: Updating texts for language: " +
                    (languageManager.isEnglish() ? "English" : "Croatian"));

            // Update labels and buttons
            if (contactsPageTitle != null) contactsPageTitle.setText(languageManager.getText("contacts.page.title"));
            if (deleteSelectedButton != null) deleteSelectedButton.setText(languageManager.getText("contacts.delete.selected"));
            if (generateBarcodeButton != null) generateBarcodeButton.setText(languageManager.getText("contacts.generate.barcode"));
            if (importButton != null) importButton.setText(languageManager.getText("contacts.import"));
            if (createContactButton != null) createContactButton.setText(languageManager.getText("contacts.create.contact"));

            if (allContactsButton != null) allContactsButton.setText(languageManager.getText("contacts.all.contacts"));
            if (membersButton != null) membersButton.setText(languageManager.getText("contacts.members"));
            if (nonMembersButton != null) nonMembersButton.setText(languageManager.getText("contacts.non.members"));
            if (upcomingBirthdaysButton != null) upcomingBirthdaysButton.setText(languageManager.getText("contacts.upcoming.birthdays"));

            if (searchField != null) searchField.setPromptText(languageManager.getText("contacts.search.placeholder"));
            if (exportButton != null) exportButton.setText(languageManager.getText("contacts.export"));
            if (editColumnsButton != null) editColumnsButton.setText(languageManager.getText("contacts.edit.columns"));

            // Update table columns
            if (editColumn != null) editColumn.setText(languageManager.getText("contacts.column.edit"));
            if (barcodeColumn != null) barcodeColumn.setText(languageManager.getText("contacts.column.barcode"));
            if (firstNameColumn != null) firstNameColumn.setText(languageManager.getText("contacts.column.first.name"));
            if (lastNameColumn != null) lastNameColumn.setText(languageManager.getText("contacts.column.last.name"));
            if (birthdayColumn != null) birthdayColumn.setText(languageManager.getText("contacts.column.birthday"));
            if (ageColumn != null) ageColumn.setText(languageManager.getText("contacts.column.age"));
            if (pinColumn != null) pinColumn.setText(languageManager.getText("contacts.column.pin"));
            if (emailColumn != null) emailColumn.setText(languageManager.getText("contacts.column.email"));
            if (phoneColumn != null) phoneColumn.setText(languageManager.getText("contacts.column.phone"));
            if (streetNameColumn != null) streetNameColumn.setText(languageManager.getText("contacts.column.street.name"));
            if (streetNumColumn != null) streetNumColumn.setText(languageManager.getText("contacts.column.street.number"));
            if (postalCodeColumn != null) postalCodeColumn.setText(languageManager.getText("contacts.column.postal.code"));
            if (cityColumn != null) cityColumn.setText(languageManager.getText("contacts.column.city"));
            if (memberStatusColumn != null) memberStatusColumn.setText(languageManager.getText("contacts.column.member.status"));
            if (memberSinceColumn != null) memberSinceColumn.setText(languageManager.getText("contacts.column.member.since"));
            if (memberUntilColumn != null) memberUntilColumn.setText(languageManager.getText("contacts.column.member.until"));
            if (createdAtColumn != null) createdAtColumn.setText(languageManager.getText("contacts.column.created"));
            if (updatedAtColumn != null) updatedAtColumn.setText(languageManager.getText("contacts.column.updated"));

            System.out.println("ContactViewController: Texts updated successfully");

        } catch (Exception e) {
            System.err.println("Error updating ContactViewController texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateBarcode() {
        System.out.println("🔧 Generate Barcode button clicked");

        // Get all selected contacts from the filtered list
        List<Contact> selectedContacts = filteredContactsList.stream()
                .filter(Contact::isSelected)
                .collect(Collectors.toList());

        if (selectedContacts.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No contacts selected");
            alert.setContentText("Please select one or more contacts to generate barcodes for using the checkboxes.");
            alert.showAndWait();
            return;
        }

        try {
            System.out.println("🔧 First selecting payment template...");

            // Step 1: Load and show payment template selection
            PaymentTemplate selectedTemplate = showPaymentTemplateSelectionDialog();
            if (selectedTemplate == null) {
                System.out.println("🔧 No payment template selected, cancelling barcode generation");
                return; // User cancelled template selection
            }

            System.out.println("🔧 Selected payment template: " + selectedTemplate.getName());
            System.out.println("🔧 Opening MultipleGenerationBarcodeDialog with " + selectedContacts.size() + " selected contacts");

            // Step 2: Open the main barcode generation dialog with template and contacts
            Stage currentStage = (Stage) generateBarcodeButton.getScene().getWindow();
            MultipleGenerationBarcodeDialog dialog = new MultipleGenerationBarcodeDialog(currentStage, selectedContacts, selectedTemplate);
            dialog.showAndWait();

            System.out.println("🔧 MultipleGenerationBarcodeDialog closed");

        } catch (Exception e) {
            System.err.println("Error opening barcode generation dialog: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Barcode Generation Failed");
            errorAlert.setContentText("An error occurred while opening the barcode generator: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private PaymentTemplate showPaymentTemplateSelectionDialog() {
        try {
            System.out.println("🔧 Loading payment templates...");
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            List<PaymentTemplate> templates = dao.getActivePaymentTemplates();

            System.out.println("🔧 Found " + templates.size() + " active payment templates");

            if (templates.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Payment Templates");
                alert.setHeaderText("No active payment templates found");
                alert.setContentText("Please create at least one active payment template before generating barcodes.");
                alert.showAndWait();
                return null;
            }

            // Create choice dialog
            ChoiceDialog<PaymentTemplate> dialog = new ChoiceDialog<>(templates.get(0), templates);
            dialog.setTitle("Select Payment Template");
            dialog.setHeaderText("Choose a payment template for barcode generation:");
            dialog.setContentText("Payment Template:");

            dialog.getDialogPane().setPrefWidth(500);
            Stage currentStage = (Stage) generateBarcodeButton.getScene().getWindow();
            dialog.initOwner(currentStage);

            // "Show Details" section
            VBox infoPane = createTemplateInfoPane(templates.get(0));
            infoPane.setStyle("-fx-text-fill: white; -fx-background-color: #ff7a59;");
            dialog.getDialogPane().setExpandableContent(infoPane);

            // Result label (to display user action in white)
            Label resultLabel = new Label();
            resultLabel.setStyle("-fx-text-fill: white; -fx-padding: 10 0 0 0;");

            // Combine dialog content + result label into one VBox
            VBox combinedContent = new VBox(10);
            combinedContent.getChildren().addAll(dialog.getDialogPane().getContent(), resultLabel);
            dialog.getDialogPane().setContent(combinedContent);

            // Style components after dialog loads
            Platform.runLater(() -> {
                // White label for "Payment Template:"
                dialog.getDialogPane().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label label && label.getText().contains("Payment Template:")) {
                        label.setStyle("-fx-text-fill: white;");
                    }
                });

                // Orange OK button with white text
                dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                        "-fx-background-color: #ff7a59; -fx-text-fill: white;"
                );
            });

            // Show the dialog and handle result
            Optional<PaymentTemplate> result = dialog.showAndWait();
            if (result.isPresent()) {
                PaymentTemplate selectedTemplate = result.get();
                resultLabel.setText("✅ Selected: " + selectedTemplate.getName());
                resultLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                System.out.println("🔧 User selected template: " + selectedTemplate.getName());
                return selectedTemplate;
            } else {
                resultLabel.setText("❌ You cancelled the selection.");
                resultLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
                System.out.println("🔧 User cancelled template selection");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error loading payment templates: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Template Loading Error");
            errorAlert.setHeaderText("Failed to load payment templates");
            errorAlert.setContentText("An error occurred while loading payment templates: " + e.getMessage());
            errorAlert.showAndWait();

            return null;
        }
    }

    private VBox createTemplateInfoPane(PaymentTemplate template) {
        VBox infoPane = new VBox(8);
        infoPane.setPadding(new Insets(10, 10, 10, 10));
        infoPane.setStyle("-fx-background-color: transparent;");

        if (template != null) {
            Label nameLabel = new Label("Template: " + template.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");

            Label amountLabel = new Label("Amount: " + template.getAmount() + " EUR");
            amountLabel.setStyle("-fx-text-fill: white;");

            Label modelLabel = new Label("Payment Model: " +
                    (template.getModelOfPayment() != null ? template.getModelOfPayment() : "N/A"));
            modelLabel.setStyle("-fx-text-fill: white;");

            Label descLabel = new Label("Description: " +
                    (template.getDescription() != null && !template.getDescription().trim().isEmpty()
                            ? template.getDescription() : "N/A"));
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(450);
            descLabel.setStyle("-fx-text-fill: white;");

            infoPane.getChildren().addAll(nameLabel, amountLabel, modelLabel, descLabel);
        } else {
            Label noInfoLabel = new Label("No template information available");
            noInfoLabel.setStyle("-fx-text-fill: #6c757d;");
            infoPane.getChildren().add(noInfoLabel);
        }

        return infoPane;
    }

    private void handleImportContacts() {
        try {
            Stage currentStage = (Stage) importButton.getScene().getWindow();
            ImportContactsDialog dialog = new ImportContactsDialog(currentStage);

            if (dialog.showAndWait()) {
                List<Contact> importedContacts = dialog.getImportedContacts();

                if (!importedContacts.isEmpty()) {
                    // Add imported contacts to the list
                    allContactsList.addAll(importedContacts);
                    updateRecordCount();

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Import Successful");
                    successAlert.setHeaderText("Contacts Imported");
                    successAlert.setContentText("Successfully imported " + importedContacts.size() +
                            " contact" + (importedContacts.size() != 1 ? "s" : "") +
                            " from CSV file.");
                    successAlert.showAndWait();

                    // Refresh the table to show new data
                    contactsTable.refresh();
                    System.out.println("Imported " + importedContacts.size() + " contacts successfully");
                }
            }

        } catch (Exception e) {
            System.err.println("Error in handleImportContacts: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Import Error");
            errorAlert.setHeaderText("Import Failed");
            errorAlert.setContentText("An error occurred while importing contacts: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void initializeColumnMapping() {
        System.out.println("Initializing column mapping...");

        // EXACT mapping - column names must match exactly with dialog
        columnMap.put("First Name", firstNameColumn);
        columnMap.put("Last Name", lastNameColumn);
        columnMap.put("Birthday", birthdayColumn);
        columnMap.put("Age", ageColumn);
        columnMap.put("PIN", pinColumn);
        columnMap.put("Email", emailColumn);
        columnMap.put("Phone Number", phoneColumn);
        columnMap.put("Street Name", streetNameColumn);
        columnMap.put("Street Number", streetNumColumn);
        columnMap.put("Postal Code", postalCodeColumn);
        columnMap.put("City", cityColumn);
        columnMap.put("Member Status", memberStatusColumn);
        columnMap.put("Member Since", memberSinceColumn);
        columnMap.put("Member Until", memberUntilColumn);
        columnMap.put("Created", createdAtColumn);
        columnMap.put("Updated", updatedAtColumn);

        System.out.println("Column mapping completed. Mapped " + columnMap.size() + " columns");
    }

    private void setupTable() {
        // Set up checkbox column
        selectColumn.setCellFactory(tc -> new TableCell<Contact, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getIndex() >= contactsTable.getItems().size()) {
                    setGraphic(null);
                } else {
                    Contact contact = contactsTable.getItems().get(getIndex());
                    checkBox.setSelected(contact.isSelected());
                    checkBox.setOnAction(event -> contact.setSelected(checkBox.isSelected()));
                    setGraphic(checkBox);
                }
            }
        });

        barcodeColumn.setCellFactory(tc -> new TableCell<Contact, Void>() {
            private final Button barcodeButton = new Button("💳");

            {
                barcodeButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                barcodeButton.setPrefWidth(50);
                barcodeButton.setTooltip(new Tooltip("Generate HUB-3 Payment Barcode"));
                barcodeButton.setOnAction(event -> {
                    Contact contact = getTableView().getItems().get(getIndex());
                    handleGenerateHUB3Barcode(contact);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(barcodeButton);
                }
            }
        });

        // Set up edit button column
        editColumn.setCellFactory(tc -> new TableCell<Contact, Void>() {
            private final Button editButton = new Button(languageManager.getText("contacts.edit.button"));

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                editButton.setPrefWidth(50);
                editButton.setOnAction(event -> {
                    Contact contact = getTableView().getItems().get(getIndex());
                    handleEditContact(contact);
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

        // Set up all column bindings
        firstNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        // Birthday column with formatted date
        birthdayColumn.setCellValueFactory(cellData -> {
            Contact contact = cellData.getValue();
            return new SimpleStringProperty(contact.getBirthday() != null ?
                    contact.getBirthday().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        });

        // Age column
        ageColumn.setCellValueFactory(cellData -> {
            Contact contact = cellData.getValue();
            return new SimpleStringProperty(contact.getBirthday() != null ?
                    String.valueOf(contact.getAge()) : "");
        });

        // PIN column
        pinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPin()));

        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhoneNum()));
        streetNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStreetName()));
        streetNumColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStreetNum()));
        postalCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPostalCode()));
        cityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCity()));
        memberStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ?
                        languageManager.getText("contacts.member.status.member") :
                        languageManager.getText("contacts.member.status.non.member")));
        memberSinceColumn.setCellValueFactory(cellData -> {
            Contact contact = cellData.getValue();
            return new SimpleStringProperty(contact.getMemberSince() != null ?
                    contact.getMemberSince().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        });
        memberUntilColumn.setCellValueFactory(cellData -> {
            Contact contact = cellData.getValue();
            return new SimpleStringProperty(contact.getMemberUntil() != null ?
                    contact.getMemberUntil().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        });
        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));
        updatedAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUpdatedAt()));
    }

    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredContactsList = new FilteredList<>(allContactsList, p -> true);

        // Set the table to use the filtered list
        contactsTable.setItems(filteredContactsList);

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredContactsList.setPredicate(contact -> {
            // If no search text, show all contacts based on current filter
            if (searchText.isEmpty()) {
                return matchesCurrentFilter(contact);
            }

            // Check if search text matches name, phone, email, PIN, or age
            boolean matchesSearch = false;

            if (contact.getFirstName() != null && contact.getFirstName().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (contact.getLastName() != null && contact.getLastName().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (contact.getEmail() != null && contact.getEmail().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (contact.getPhoneNum() != null && contact.getPhoneNum().toLowerCase().contains(searchText)) {
                matchesSearch = true;
            } else if (contact.getPin() != null && contact.getPin().toLowerCase().contains(searchText)) {
                matchesSearch = true; // Search by PIN
            } else if (contact.getBirthday() != null && String.valueOf(contact.getAge()).contains(searchText)) {
                matchesSearch = true; // Search by age
            }

            // Return true only if matches both search and current filter
            return matchesSearch && matchesCurrentFilter(contact);
        });

        updateRecordCount();
    }

    private boolean matchesCurrentFilter(Contact contact) {
        // Check which filter button is active based on their style
        String allContactsStyle = allContactsButton.getStyle();
        String membersStyle = membersButton.getStyle();
        String nonMembersStyle = nonMembersButton.getStyle();
        String birthdaysStyle = upcomingBirthdaysButton != null ? upcomingBirthdaysButton.getStyle() : "";

        // If "All contacts" is active (has #f5f8fa background)
        if (allContactsStyle.contains("#f5f8fa")) {
            return true; // Show all contacts
        }
        // If "Members" is active
        else if (membersStyle.contains("#f5f8fa")) {
            return contact.isMember();
        }
        // If "Non-members" is active
        else if (nonMembersStyle.contains("#f5f8fa")) {
            return !contact.isMember();
        }
        // If "Upcoming Birthdays" is active
        else if (birthdaysStyle.contains("#f5f8fa")) {
            return hasUpcomingBirthday(contact, 30); // Next 30 days
        }

        return true;
    }

    private boolean hasUpcomingBirthday(Contact contact, int daysAhead) {
        if (contact.getBirthday() == null) {
            return false;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate futureDate = today.plusDays(daysAhead);

        // Calculate this year's birthday
        java.time.LocalDate thisYearBirthday = contact.getBirthday().withYear(today.getYear());

        // If birthday already passed this year, check next year's birthday
        if (thisYearBirthday.isBefore(today)) {
            thisYearBirthday = thisYearBirthday.plusYears(1);
        }

        // Check if birthday falls within the specified range
        return !thisYearBirthday.isBefore(today) && !thisYearBirthday.isAfter(futureDate);
    }

    private void loadContacts() {
        try {
            ContactDAO dao = new ContactDAO();
            List<Contact> contacts = dao.getAllContacts();

            System.out.println("DAO returned " + contacts.size() + " contacts");

            allContactsList.setAll(contacts);
            updateRecordCount();

        } catch (Exception e) {
            System.err.println("Error loading contacts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRecordCount() {
        int count = filteredContactsList.size();
        recordCountLabel.setText(count + " record" + (count != 1 ? "s" : ""));
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createContactButton.setOnAction(e -> handleCreateContact());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        exportButton.setOnAction(e -> handleExportContacts());
        importButton.setOnAction(e -> handleImportContacts());
        generateBarcodeButton.setOnAction(e -> handleGenerateBarcode());

        // IMPORTANT: Make sure edit columns button handler is set
        if (editColumnsButton != null) {
            editColumnsButton.setOnAction(e -> handleEditColumns());
            System.out.println("Edit columns button handler set successfully");
        } else {
            System.err.println("ERROR: editColumnsButton is null! Check FXML fx:id");
        }

        // Filter buttons
        allContactsButton.setOnAction(e -> handleFilterButton(allContactsButton));
        membersButton.setOnAction(e -> handleFilterButton(membersButton));
        nonMembersButton.setOnAction(e -> handleFilterButton(nonMembersButton));

        // Birthday filter button (if exists)
        if (upcomingBirthdaysButton != null) {
            upcomingBirthdaysButton.setOnAction(e -> handleFilterButton(upcomingBirthdaysButton));
        }

        System.out.println("Event handlers setup completed");
    }

    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive
        allContactsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        membersButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        nonMembersButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        if (upcomingBirthdaysButton != null) {
            upcomingBirthdaysButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        }

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter
        updateFilters();
    }

    private void handleExportContacts() {
        try {
            // Get currently visible contacts (filtered/searched)
            List<Contact> contactsToExport = new ArrayList<>(filteredContactsList);

            if (contactsToExport.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Data");
                alert.setHeaderText("No contacts to export");
                alert.setContentText("There are no contacts visible to export. Please check your filters or add some contacts first.");
                alert.showAndWait();
                return;
            }

            // File chooser for save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Contacts to CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            // Set default filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            fileChooser.setInitialFileName("contacts_export_" + timestamp + ".csv");

            Stage currentStage = (Stage) exportButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(currentStage);

            if (file != null) {
                exportContactsToCSV(contactsToExport, file);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Export Successful");
                successAlert.setHeaderText("Contacts exported successfully");
                successAlert.setContentText("Exported " + contactsToExport.size() + " contact(s) to:\n" + file.getAbsolutePath());
                successAlert.showAndWait();
            }

        } catch (Exception e) {
            System.err.println("Error exporting contacts: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Export Failed");
            errorAlert.setHeaderText("Failed to export contacts");
            errorAlert.setContentText("An error occurred while exporting: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void exportContactsToCSV(List<Contact> contacts, File file) throws IOException {
        // Use UTF-8 encoding with BOM for proper Croatian character support
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // Write UTF-8 BOM (Byte Order Mark) so Excel recognizes UTF-8 encoding
            writer.write('\ufeff');

            // Write CSV header in Croatian (including birthday, age, and PIN)
            writer.write("Ime,Prezime,Rođendan,Godine,PIN,Email,Telefon,Ulica,Broj,Poštanski kod,Grad,Status članstva,Član od,Član do,Kreiran,Ažuriran");
            writer.newLine();

            // Write contact data
            for (Contact contact : contacts) {
                StringBuilder line = new StringBuilder();

                // Helper method to escape CSV values
                line.append(escapeCsvValue(contact.getFirstName())).append(",");
                line.append(escapeCsvValue(contact.getLastName())).append(",");
                line.append(escapeCsvValue(contact.getBirthday() != null ? contact.getBirthday().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "")).append(",");
                line.append(escapeCsvValue(contact.getBirthday() != null ? String.valueOf(contact.getAge()) : "")).append(",");
                line.append(escapeCsvValue(contact.getPin())).append(",");
                line.append(escapeCsvValue(contact.getEmail())).append(",");
                line.append(escapeCsvValue(contact.getPhoneNum())).append(",");
                line.append(escapeCsvValue(contact.getStreetName())).append(",");
                line.append(escapeCsvValue(contact.getStreetNum())).append(",");
                line.append(escapeCsvValue(contact.getPostalCode())).append(",");
                line.append(escapeCsvValue(contact.getCity())).append(",");
                line.append(escapeCsvValue(contact.isMember() ? "Član" : "Nije član")).append(",");
                line.append(escapeCsvValue(contact.getMemberSince() != null ? contact.getMemberSince().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "")).append(",");
                line.append(escapeCsvValue(contact.getMemberUntil() != null ? contact.getMemberUntil().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "")).append(",");
                line.append(escapeCsvValue(contact.getCreatedAt())).append(",");
                line.append(escapeCsvValue(contact.getUpdatedAt()));

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private void handleCreateContact() {
        try {
            Stage currentStage = (Stage) createContactButton.getScene().getWindow();
            CreateContactDialog dialog = new CreateContactDialog(currentStage);

            if (dialog.showAndWait()) {
                Contact newContact = dialog.getResult();
                if (newContact != null) {
                    allContactsList.add(newContact);
                    updateRecordCount();
                    contactsTable.getSelectionModel().select(newContact);
                    contactsTable.scrollTo(newContact);
                    System.out.println("New contact added: " + newContact.getFirstName() + " " + newContact.getLastName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening create contact dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        // Get all selected contacts from the filtered list
        List<Contact> selectedContacts = filteredContactsList.stream()
                .filter(Contact::isSelected)
                .collect(Collectors.toList());

        if (selectedContacts.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageManager.getText("contacts.delete.no.selection.title"));
            alert.setHeaderText(languageManager.getText("contacts.delete.no.selection.header"));
            alert.setContentText(languageManager.getText("contacts.delete.no.selection.content"));
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager.getText("contacts.delete.confirm.title"));
        confirmAlert.setHeaderText(languageManager.getText("contacts.delete.confirm.header"));

        String contentText = languageManager.getText("contacts.delete.confirm.content.part1") + " " +
                selectedContacts.size() + " " +
                (selectedContacts.size() > 1 ?
                        languageManager.getText("contacts.delete.confirm.content.contacts") :
                        languageManager.getText("contacts.delete.confirm.content.contact")) + "? " +
                languageManager.getText("contacts.delete.confirm.content.part2");

        confirmAlert.setContentText(contentText);

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                ContactDAO dao = new ContactDAO();
                List<Integer> contactIds = selectedContacts.stream()
                        .map(Contact::getId)
                        .collect(Collectors.toList());

                boolean success = dao.deleteContacts(contactIds);

                if (success) {
                    // Remove from the original list (filtered list will update automatically)
                    allContactsList.removeAll(selectedContacts);
                    updateRecordCount();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager.getText("contacts.delete.success.title"));
                    successAlert.setHeaderText(languageManager.getText("contacts.delete.success.header"));

                    String successContent = languageManager.getText("contacts.delete.success.content.part1") + " " +
                            selectedContacts.size() + " " +
                            (selectedContacts.size() > 1 ?
                                    languageManager.getText("contacts.delete.success.content.contacts") :
                                    languageManager.getText("contacts.delete.success.content.contact")) + ".";

                    successAlert.setContentText(successContent);
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle(languageManager.getText("contacts.delete.error.title"));
                    errorAlert.setHeaderText(languageManager.getText("contacts.delete.error.header"));
                    errorAlert.setContentText(languageManager.getText("contacts.delete.error.content"));
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting contacts: " + e.getMessage());
                e.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(languageManager.getText("contacts.delete.error.title"));
                errorAlert.setHeaderText(languageManager.getText("contacts.delete.error.header"));
                errorAlert.setContentText(languageManager.getText("contacts.delete.error.content"));
                errorAlert.showAndWait();
            }
        }
    }

    private void handleEditContact(Contact contact) {
        try {
            Stage currentStage = (Stage) createContactButton.getScene().getWindow();
            EditContactDialog dialog = new EditContactDialog(currentStage, contact);

            if (dialog.showAndWait()) {
                // Refresh the table to show updated data
                contactsTable.refresh();
                updateFilters(); // Re-apply filters in case membership status changed
                System.out.println("Contact updated: " + contact.getFirstName() + " " + contact.getLastName());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Contact Updated");
                successAlert.setContentText("Contact has been successfully updated.");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error opening edit contact dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New method to show upcoming birthdays
    @FXML
    private void handleUpcomingBirthdays() {
        try {
            ContactDAO dao = new ContactDAO();
            List<Contact> upcomingBirthdays = dao.getContactsWithUpcomingBirthdays(30); // Next 30 days

            if (upcomingBirthdays.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Upcoming Birthdays");
                alert.setHeaderText("No upcoming birthdays");
                alert.setContentText("There are no birthdays in the next 30 days.");
                alert.showAndWait();
            } else {
                StringBuilder message = new StringBuilder();
                message.append("Upcoming birthdays in the next 30 days:\n\n");

                for (Contact contact : upcomingBirthdays) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate thisYearBirthday = contact.getBirthday().withYear(today.getYear());
                    if (thisYearBirthday.isBefore(today)) {
                        thisYearBirthday = thisYearBirthday.plusYears(1);
                    }

                    message.append("• ").append(contact.getFullName())
                            .append(" - ").append(thisYearBirthday.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            .append(" (").append(contact.getAge() + 1).append(" years old)\n");
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Upcoming Birthdays");
                alert.setHeaderText("Found " + upcomingBirthdays.size() + " upcoming birthdays");
                alert.setContentText(message.toString());
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error getting upcoming birthdays: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGenerateHUB3Barcode(Contact contact) {
        try {
            Stage currentStage = (Stage) createContactButton.getScene().getWindow();
            BarcodePaymentDialog dialog = new BarcodePaymentDialog(currentStage, contact);
            dialog.showAndWait();

            System.out.println("HUB-3 barcode dialog closed for contact: " + contact.getFirstName() + " " + contact.getLastName());

        } catch (Exception e) {
            System.err.println("Error opening HUB-3 barcode dialog: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("HUB-3 Barcode Generation Failed");
            errorAlert.setContentText("An error occurred while opening the HUB-3 barcode generator: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}