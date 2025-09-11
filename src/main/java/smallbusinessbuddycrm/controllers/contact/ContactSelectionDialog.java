package smallbusinessbuddycrm.controllers.contact;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom dialog for selecting multiple contacts from a list.
 *
 * This dialog provides:
 * - A searchable table of contacts
 * - Multi-selection capabilities with checkboxes
 * - Bulk selection options (select all, deselect all, select visible)
 * - Real-time filtering and search functionality
 * - Internationalization support through LanguageManager
 *
 * The dialog displays contact information in a table format with columns for:
 * - Selection checkbox
 * - Full name
 * - Email address
 * - Phone number
 * - Member status
 * - Address
 *
 * Usage:
 * ContactSelectionDialog dialog = new ContactSelectionDialog(contactList);
 * Optional<List<Contact>> result = dialog.showAndWait();
 * if (result.isPresent()) {
 *     List<Contact> selectedContacts = result.get();
 *     // Process selected contacts
 * }
 */
public class ContactSelectionDialog extends Dialog<List<Contact>> {

    // ===== TABLE AND DATA COMPONENTS =====

    /** Main table displaying contacts with selection capabilities */
    private TableView<ContactWrapper> contactTable;

    /** Observable list containing currently visible contacts (filtered) */
    private ObservableList<ContactWrapper> contactData;

    /** Observable list containing all contacts (unfiltered) */
    private ObservableList<ContactWrapper> allContactData;

    // ===== UI COMPONENTS =====

    /** Search field for filtering contacts by name, email, phone, or address */
    private TextField searchField;

    /** Label showing total and selected contact counts */
    private Label countLabel;

    // ===== TRANSLATABLE UI LABELS =====

    /** Debug label showing technical information (for development) */
    private Label debugLabel;

    /** Informational label explaining the dialog purpose */
    private Label infoLabel;

    /** Label indicating available contacts section */
    private Label availableContactsLabel;

    /** Button to select all contacts */
    private Button selectAllBtn;

    /** Button to deselect all contacts */
    private Button deselectAllBtn;

    /** Button to select only currently visible contacts */
    private Button selectVisibleBtn;

    // ===== TABLE COLUMNS FOR TRANSLATION =====

    /** Column containing selection checkboxes */
    private TableColumn<ContactWrapper, CheckBox> selectColumn;

    /** Column displaying full names */
    private TableColumn<ContactWrapper, String> nameColumn;

    /** Column displaying email addresses */
    private TableColumn<ContactWrapper, String> emailColumn;

    /** Column displaying phone numbers */
    private TableColumn<ContactWrapper, String> phoneColumn;

    /** Column displaying member status */
    private TableColumn<ContactWrapper, String> memberColumn;

    /** Column displaying addresses */
    private TableColumn<ContactWrapper, String> addressColumn;

    /**
     * Creates a new ContactSelectionDialog with the provided list of contacts.
     *
     * @param availableContacts List of Contact objects to display for selection
     *                         Must not be null, but can be empty
     */
    public ContactSelectionDialog(List<Contact> availableContacts) {
        System.out.println("🔧 ContactSelectionDialog: Creating dialog with " + availableContacts.size() + " contacts");

        // Create the main content layout
        VBox content = createContent(availableContacts);
        getDialogPane().setContent(content);

        // Add standard OK and Cancel buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set reasonable dialog dimensions
        getDialogPane().setPrefSize(900, 700);

        // Configure result conversion - returns selected contacts on OK, null on Cancel
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                List<Contact> selected = getSelectedContacts();
                System.out.println("🔧 ContactSelectionDialog: Returning " + selected.size() + " selected contacts");
                return selected;
            }
            return null;
        });

        // Apply initial translations
        updateTexts();

        System.out.println("🔧 ContactSelectionDialog: Dialog created successfully");
    }

    /**
     * Creates and configures the main content layout for the dialog.
     *
     * @param availableContacts List of contacts to populate the dialog with
     * @return VBox containing all dialog components
     */
    private VBox createContent(List<Contact> availableContacts) {
        System.out.println("🔧 ContactSelectionDialog: Creating content...");

        // Main container with spacing and padding
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Debug information for development troubleshooting
        debugLabel = new Label("🔧 Debug: " + availableContacts.size() + " contacts available");
        debugLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        // Informational label explaining dialog purpose
        infoLabel = new Label();
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 14px;");

        // Search field for filtering contacts
        searchField = new TextField();
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        // Initialize table and data structures
        contactTable = new TableView<>();
        contactData = FXCollections.observableArrayList();
        allContactData = FXCollections.observableArrayList();

        System.out.println("🔧 ContactSelectionDialog: Setting up table...");
        setupTable();

        System.out.println("🔧 ContactSelectionDialog: Loading contacts...");
        loadContacts(availableContacts);

        // Configure real-time search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("🔧 Search changed to: " + newValue);
            filterContacts(newValue);
        });

        // Create action buttons section
        HBox buttonBox = createActionButtons();

        // Label for available contacts section
        availableContactsLabel = new Label();

        // Contact count display
        countLabel = new Label();
        updateCountLabel(countLabel, availableContacts.size(), 0);

        // Add all components to main container
        vbox.getChildren().addAll(
                debugLabel,
                infoLabel,
                searchField,
                availableContactsLabel,
                contactTable,
                buttonBox,
                countLabel
        );

        System.out.println("🔧 ContactSelectionDialog: Content created successfully");
        return vbox;
    }

    /**
     * Creates and configures the action buttons (Select All, Deselect All, Select Visible).
     *
     * @return HBox containing styled action buttons
     */
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Select All button - selects every contact in the full list
        selectAllBtn = new Button();
        selectAllBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px;");

        // Deselect All button - deselects every contact in the full list
        deselectAllBtn = new Button();
        deselectAllBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 8px 16px;");

        // Select Visible button - selects only currently visible (filtered) contacts
        selectVisibleBtn = new Button();
        selectVisibleBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8px 16px;");

        // Configure button actions with debug logging
        selectAllBtn.setOnAction(e -> {
            System.out.println("🔧 Select All clicked - setting all to true");
            for (ContactWrapper wrapper : allContactData) {
                wrapper.setSelected(true);
                System.out.println("🔧 Set " + wrapper.getFullName() + " to selected");
            }
            updateCountDisplay();
            System.out.println("🔧 Select All completed");
        });

        deselectAllBtn.setOnAction(e -> {
            System.out.println("🔧 Deselect All clicked - setting all to false");
            for (ContactWrapper wrapper : allContactData) {
                wrapper.setSelected(false);
                System.out.println("🔧 Set " + wrapper.getFullName() + " to deselected");
            }
            updateCountDisplay();
            System.out.println("🔧 Deselect All completed");
        });

        selectVisibleBtn.setOnAction(e -> {
            System.out.println("🔧 Select Visible clicked - setting visible to true");
            for (ContactWrapper wrapper : contactData) {
                wrapper.setSelected(true);
                System.out.println("🔧 Set visible " + wrapper.getFullName() + " to selected");
            }
            updateCountDisplay();
            System.out.println("🔧 Select Visible completed");
        });

        buttonBox.getChildren().addAll(selectAllBtn, deselectAllBtn, selectVisibleBtn);
        return buttonBox;
    }

    /**
     * Configures the table structure and columns.
     * Creates columns for selection, name, email, phone, member status, and address.
     */
    private void setupTable() {
        System.out.println("🔧 ContactSelectionDialog: Setting up table columns...");

        // Selection column with checkboxes
        selectColumn = new TableColumn<>();
        selectColumn.setPrefWidth(50);
        selectColumn.setSortable(false);

        // Custom cell factory for checkboxes with bidirectional binding
        selectColumn.setCellValueFactory(cellData -> {
            ContactWrapper wrapper = cellData.getValue();
            CheckBox checkBox = new CheckBox();

            // Bind checkbox state to wrapper's selected property
            checkBox.selectedProperty().bindBidirectional(wrapper.selectedProperty());

            // Listen for changes to update count display
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("🔧 Checkbox for " + wrapper.getFullName() + " changed to: " + newVal);
                updateCountDisplay();
            });

            return new javafx.beans.property.SimpleObjectProperty<>(checkBox);
        });

        selectColumn.setEditable(false);
        selectColumn.setSortable(false);

        // Name column - displays full name (first + last)
        nameColumn = new TableColumn<>();
        nameColumn.setPrefWidth(200);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // Email column
        emailColumn = new TableColumn<>();
        emailColumn.setPrefWidth(250);
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Phone column
        phoneColumn = new TableColumn<>();
        phoneColumn.setPrefWidth(150);
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Member status column - shows translated Yes/No
        memberColumn = new TableColumn<>();
        memberColumn.setPrefWidth(80);
        memberColumn.setCellValueFactory(new PropertyValueFactory<>("memberStatus"));

        // Address column - formatted street + city
        addressColumn = new TableColumn<>();
        addressColumn.setPrefWidth(200);
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Add all columns to table
        contactTable.getColumns().addAll(selectColumn, nameColumn, emailColumn, phoneColumn, memberColumn, addressColumn);
        contactTable.setItems(contactData);
        contactTable.setEditable(false);
        contactTable.setPrefHeight(400);

        // Apply table styling
        contactTable.setStyle("-fx-font-size: 13px;");

        System.out.println("🔧 ContactSelectionDialog: Table setup completed");
    }

    /**
     * Loads contacts into the table by creating ContactWrapper objects.
     *
     * @param contacts List of Contact objects to load
     */
    private void loadContacts(List<Contact> contacts) {
        System.out.println("🔧 ContactSelectionDialog: Loading " + contacts.size() + " contacts...");

        // Clear existing data
        allContactData.clear();
        contactData.clear();

        // Process each contact
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            System.out.println("🔧 Processing contact " + (i+1) + ": " + contact.getFirstName() + " " + contact.getLastName());

            try {
                // Create wrapper for table display
                ContactWrapper wrapper = new ContactWrapper(contact);
                allContactData.add(wrapper);
                contactData.add(wrapper);

                // Listen for selection changes to update count
                wrapper.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    updateCountDisplay();
                });

                System.out.println("🔧 Successfully added wrapper for: " + wrapper.getFullName());
            } catch (Exception e) {
                System.err.println("🔧 Error creating wrapper for contact: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("🔧 ContactSelectionDialog: Loaded " + allContactData.size() + " contact wrappers");
        System.out.println("🔧 ContactSelectionDialog: contactData size: " + contactData.size());
        System.out.println("🔧 ContactSelectionDialog: Table items size: " + contactTable.getItems().size());

        // Force table refresh to ensure display updates
        contactTable.refresh();
        System.out.println("🔧 ContactSelectionDialog: Table refreshed");
    }

    /**
     * Filters the displayed contacts based on the search term.
     * Searches through name, email, phone, and address fields.
     *
     * @param searchTerm The text to search for (case-insensitive)
     */
    private void filterContacts(String searchTerm) {
        System.out.println("🔧 filterContacts called with: '" + searchTerm + "'");

        // If search is empty, show all contacts
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("🔧 Search term empty, showing all contacts");
            contactData.setAll(allContactData);
            System.out.println("🔧 Filter cleared, contactData now has " + contactData.size() + " items");
            return;
        }

        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        System.out.println("🔧 Searching for: '" + lowerSearchTerm + "'");

        ObservableList<ContactWrapper> filteredContacts = FXCollections.observableArrayList();

        // Search through all contacts
        for (ContactWrapper wrapper : allContactData) {
            Contact contact = wrapper.getContact();

            // Prepare searchable fields (case-insensitive)
            String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
            String email = contact.getEmail() != null ? contact.getEmail().toLowerCase() : "";
            String phone = contact.getPhoneNum() != null ? contact.getPhoneNum().toLowerCase() : "";
            String address = wrapper.getAddress().toLowerCase();

            // Check if search term matches any field
            boolean matches = fullName.contains(lowerSearchTerm) ||
                    email.contains(lowerSearchTerm) ||
                    phone.contains(lowerSearchTerm) ||
                    address.contains(lowerSearchTerm);

            if (matches) {
                filteredContacts.add(wrapper);
                System.out.println("🔧 MATCH: " + wrapper.getFullName() + " (searched in: " + fullName + ")");
            }
        }

        System.out.println("🔧 Found " + filteredContacts.size() + " matches out of " + allContactData.size() + " total");
        contactData.setAll(filteredContacts);
        System.out.println("🔧 contactData updated to " + contactData.size() + " items");
        System.out.println("🔧 Table items size after filter: " + contactTable.getItems().size());

        updateCountDisplay();
    }

    /**
     * Retrieves all currently selected contacts from the full list.
     *
     * @return List of Contact objects that are currently selected
     */
    private List<Contact> getSelectedContacts() {
        List<Contact> selectedContacts = new ArrayList<>();
        for (ContactWrapper wrapper : allContactData) {
            if (wrapper.isSelected()) {
                selectedContacts.add(wrapper.getContact());
            }
        }
        System.out.println("🔧 getSelectedContacts returning " + selectedContacts.size() + " contacts");
        return selectedContacts;
    }

    /**
     * Updates the count display label with current selection information.
     */
    private void updateCountDisplay() {
        if (countLabel != null) {
            int selectedCount = getSelectedContacts().size();
            updateCountLabel(countLabel, allContactData.size(), selectedCount);
        }
    }

    /**
     * Updates a count label with total and selected contact numbers.
     *
     * @param countLabel The label to update
     * @param totalCount Total number of available contacts
     * @param selectedCount Number of currently selected contacts
     */
    private void updateCountLabel(Label countLabel, int totalCount, int selectedCount) {
        LanguageManager lm = LanguageManager.getInstance();
        String countText = lm.getText("contact.selection.count.label");
        countLabel.setText(String.format(countText, totalCount, selectedCount));
        countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
    }

    /**
     * Updates all translatable text elements using the LanguageManager.
     * This method should be called when the language changes or during initialization.
     */
    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update dialog title and header
        setTitle(lm.getText("contact.selection.dialog.title"));
        setHeaderText(lm.getText("contact.selection.dialog.header"));

        // Update main labels
        if (infoLabel != null) {
            infoLabel.setText(lm.getText("contact.selection.info.label"));
        }

        if (availableContactsLabel != null) {
            availableContactsLabel.setText(lm.getText("contact.selection.available.label"));
        }

        // Update search field placeholder
        if (searchField != null) {
            searchField.setPromptText(lm.getText("contact.selection.search.placeholder"));
        }

        // Update action buttons
        if (selectAllBtn != null) {
            selectAllBtn.setText(lm.getText("contact.selection.button.select.all"));
        }

        if (deselectAllBtn != null) {
            deselectAllBtn.setText(lm.getText("contact.selection.button.deselect.all"));
        }

        if (selectVisibleBtn != null) {
            selectVisibleBtn.setText(lm.getText("contact.selection.button.select.visible"));
        }

        // Update table column headers
        if (selectColumn != null) {
            selectColumn.setText(lm.getText("contact.selection.table.select"));
        }

        if (nameColumn != null) {
            nameColumn.setText(lm.getText("contact.selection.table.name"));
        }

        if (emailColumn != null) {
            emailColumn.setText(lm.getText("contact.selection.table.email"));
        }

        if (phoneColumn != null) {
            phoneColumn.setText(lm.getText("contact.selection.table.phone"));
        }

        if (memberColumn != null) {
            memberColumn.setText(lm.getText("contact.selection.table.member"));
        }

        if (addressColumn != null) {
            addressColumn.setText(lm.getText("contact.selection.table.address"));
        }

        // Update table placeholder text (shown when empty)
        if (contactTable != null) {
            contactTable.setPlaceholder(new Label(lm.getText("contact.selection.table.placeholder")));
        }

        // Update standard dialog buttons
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setText(lm.getText("button.ok"));
        }

        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setText(lm.getText("button.cancel"));
        }

        // Refresh count display with new language
        updateCountDisplay();

        // Force table refresh to update member status column translations
        if (contactTable != null) {
            contactTable.refresh();
        }
    }

    /**
     * Public method to refresh translations when language changes.
     * Can be called externally to update dialog text after language switching.
     */
    public void refreshTranslations() {
        updateTexts();
    }

    /**
     * Wrapper class that adapts Contact objects for display in the TableView.
     *
     * This class provides:
     * - Observable selection state for checkboxes
     * - Formatted string properties for table columns
     * - Translated member status display
     * - Formatted address combining multiple fields
     *
     * The wrapper maintains a reference to the original Contact object
     * while providing table-friendly properties and selection state management.
     */
    public static class ContactWrapper {

        /** The original Contact object being wrapped */
        private final Contact contact;

        /** Observable property for selection state (checkbox binding) */
        private final BooleanProperty selected;

        /** Pre-formatted full name (first + last) for display */
        private final String fullName;

        /** Email address (or empty string if null) */
        private final String email;

        /** Phone number (or empty string if null) */
        private final String phone;

        /** Formatted address combining street number, name, and city */
        private final String address;

        /**
         * Creates a new ContactWrapper for the given Contact.
         *
         * @param contact The Contact object to wrap (must not be null)
         */
        public ContactWrapper(Contact contact) {
            System.out.println("🔧 Creating ContactWrapper for: " + contact.getFirstName() + " " + contact.getLastName());

            this.contact = contact;
            this.selected = new SimpleBooleanProperty(false);

            // Pre-format display strings
            this.fullName = contact.getFirstName() + " " + contact.getLastName();
            this.email = contact.getEmail() != null ? contact.getEmail() : "";
            this.phone = contact.getPhoneNum() != null ? contact.getPhoneNum() : "";

            // Build formatted address from available components
            StringBuilder addressBuilder = new StringBuilder();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                addressBuilder.append(contact.getStreetNum()).append(" ");
            }
            if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
                addressBuilder.append(contact.getStreetName()).append(", ");
            }
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                addressBuilder.append(contact.getCity());
            }
            // Remove trailing comma if present
            this.address = addressBuilder.toString().replaceAll(", $", "");

            System.out.println("🔧 ContactWrapper created: " + this.fullName + " (" + this.email + ")");
        }



        /**
         * Returns the observable boolean property for selection state.
         * Used for bidirectional binding with checkboxes.
         *
         * @return BooleanProperty representing selection state
         */
        public BooleanProperty selectedProperty() {
            return selected;
        }

        /**
         * Checks if this contact is currently selected.
         *
         * @return true if selected, false otherwise
         */
        public boolean isSelected() {
            return selected.get();
        }

        /**
         * Sets the selection state of this contact.
         *
         * @param selected true to select, false to deselect
         */
        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        // ===== TABLE DISPLAY PROPERTIES =====

        /**
         * Returns the formatted full name for table display.
         *
         * @return Combined first and last name
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Returns the email address for table display.
         *
         * @return Email address or empty string if not available
         */
        public String getEmail() {
            return email;
        }

        /**
         * Returns the phone number for table display.
         *
         * @return Phone number or empty string if not available
         */
        public String getPhone() {
            return phone;
        }

        /**
         * Returns the translated member status for table display.
         * Uses LanguageManager to provide localized Yes/No text.
         *
         * @return Localized "Yes" or "No" based on member status
         */
        public String getMemberStatus() {
            LanguageManager lm = LanguageManager.getInstance();
            return contact.isMember() ?
                    lm.getText("contact.selection.member.yes") :
                    lm.getText("contact.selection.member.no");
        }

        /**
         * Returns the formatted address for table display.
         * Combines street number, street name, and city into a readable format.
         *
         * @return Formatted address string
         */
        public String getAddress() {
            return address;
        }

        /**
         * Returns the original Contact object.
         *
         * @return The wrapped Contact instance
         */
        public Contact getContact() {
            return contact;
        }
    }
}