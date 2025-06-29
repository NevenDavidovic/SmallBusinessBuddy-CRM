package smallbusinessbuddycrm.controllers;

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

import java.util.ArrayList;
import java.util.List;

public class ContactSelectionDialog extends Dialog<List<Contact>> {

    private TableView<ContactWrapper> contactTable;
    private ObservableList<ContactWrapper> contactData;
    private ObservableList<ContactWrapper> allContactData;
    private TextField searchField;
    private Label countLabel;

    public ContactSelectionDialog(List<Contact> availableContacts) {
        System.out.println("🔧 ContactSelectionDialog: Creating dialog with " + availableContacts.size() + " contacts");

        setTitle("Select Contacts");
        setHeaderText("Choose contacts to add to the list:");

        // Create the main content
        VBox content = createContent(availableContacts);
        getDialogPane().setContent(content);

        // Add buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set dialog size
        getDialogPane().setPrefSize(900, 700);

        // Set result converter
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                List<Contact> selected = getSelectedContacts();
                System.out.println("🔧 ContactSelectionDialog: Returning " + selected.size() + " selected contacts");
                return selected;
            }
            return null;
        });

        System.out.println("🔧 ContactSelectionDialog: Dialog created successfully");
    }

    private VBox createContent(List<Contact> availableContacts) {
        System.out.println("🔧 ContactSelectionDialog: Creating content...");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Debug info at the top
        Label debugLabel = new Label("🔧 Debug: " + availableContacts.size() + " contacts available");
        debugLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        // Info label
        Label infoLabel = new Label("🔍 Select contacts to add to the list. Use the search field to filter contacts.");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 14px;");

        // Search field
        searchField = new TextField();
        searchField.setPromptText("🔍 Search by name, email, or phone...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        // Contact table
        contactTable = new TableView<>();
        contactData = FXCollections.observableArrayList();
        allContactData = FXCollections.observableArrayList();

        System.out.println("🔧 ContactSelectionDialog: Setting up table...");
        setupTable();

        System.out.println("🔧 ContactSelectionDialog: Loading contacts...");
        loadContacts(availableContacts);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("🔧 Search changed to: " + newValue);
            filterContacts(newValue);
        });

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button selectAllBtn = new Button("✅ Select All");
        selectAllBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px;");

        Button deselectAllBtn = new Button("❌ Deselect All");
        deselectAllBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 8px 16px;");

        Button selectVisibleBtn = new Button("👁️ Select Visible");
        selectVisibleBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8px 16px;");

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

        // Contact count label
        countLabel = new Label();
        updateCountLabel(countLabel, availableContacts.size(), 0);

        vbox.getChildren().addAll(
                debugLabel,  // Added debug info
                infoLabel,
                searchField,
                new Label("Available Contacts:"),
                contactTable,
                buttonBox,
                countLabel
        );

        System.out.println("🔧 ContactSelectionDialog: Content created successfully");
        return vbox;
    }

    private void setupTable() {
        System.out.println("🔧 ContactSelectionDialog: Setting up table columns...");

        // COMPLETELY REWRITTEN: Simpler checkbox approach
        TableColumn<ContactWrapper, CheckBox> selectColumn = new TableColumn<>("☑");
        selectColumn.setPrefWidth(50);
        selectColumn.setSortable(false);

        selectColumn.setCellValueFactory(cellData -> {
            ContactWrapper wrapper = cellData.getValue();
            CheckBox checkBox = new CheckBox();

            // Bind checkbox to wrapper's selected property
            checkBox.selectedProperty().bindBidirectional(wrapper.selectedProperty());

            // Add listener to update count when changed
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("🔧 Checkbox for " + wrapper.getFullName() + " changed to: " + newVal);
                updateCountDisplay();
            });

            return new javafx.beans.property.SimpleObjectProperty<>(checkBox);
        });

        selectColumn.setEditable(false);
        selectColumn.setSortable(false);

        TableColumn<ContactWrapper, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(200);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<ContactWrapper, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setPrefWidth(250);
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<ContactWrapper, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setPrefWidth(150);
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<ContactWrapper, String> memberColumn = new TableColumn<>("Member");
        memberColumn.setPrefWidth(80);
        memberColumn.setCellValueFactory(new PropertyValueFactory<>("memberStatus"));

        TableColumn<ContactWrapper, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setPrefWidth(200);
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        contactTable.getColumns().addAll(selectColumn, nameColumn, emailColumn, phoneColumn, memberColumn, addressColumn);
        contactTable.setItems(contactData);
        contactTable.setEditable(false); // Changed to false since we're using direct binding
        contactTable.setPrefHeight(400);

        // Style the table
        contactTable.setStyle("-fx-font-size: 13px;");

        // Set placeholder
        contactTable.setPlaceholder(new Label("📭 No contacts available"));

        System.out.println("🔧 ContactSelectionDialog: Table setup completed");
    }

    private void loadContacts(List<Contact> contacts) {
        System.out.println("🔧 ContactSelectionDialog: Loading " + contacts.size() + " contacts...");

        allContactData.clear();
        contactData.clear();

        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            System.out.println("🔧 Processing contact " + (i+1) + ": " + contact.getFirstName() + " " + contact.getLastName());

            try {
                ContactWrapper wrapper = new ContactWrapper(contact);
                allContactData.add(wrapper);
                contactData.add(wrapper);

                // Add listener to update count when selection changes
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

        // Force table refresh
        contactTable.refresh();
        System.out.println("🔧 ContactSelectionDialog: Table refreshed");
    }

    private void filterContacts(String searchTerm) {
        System.out.println("🔧 filterContacts called with: '" + searchTerm + "'");

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("🔧 Search term empty, showing all contacts");
            contactData.setAll(allContactData);
            System.out.println("🔧 Filter cleared, contactData now has " + contactData.size() + " items");
            return;
        }

        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        System.out.println("🔧 Searching for: '" + lowerSearchTerm + "'");

        ObservableList<ContactWrapper> filteredContacts = FXCollections.observableArrayList();

        for (ContactWrapper wrapper : allContactData) {
            Contact contact = wrapper.getContact();
            String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
            String email = contact.getEmail() != null ? contact.getEmail().toLowerCase() : "";
            String phone = contact.getPhoneNum() != null ? contact.getPhoneNum().toLowerCase() : "";
            String address = wrapper.getAddress().toLowerCase();

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

    private void updateCountDisplay() {
        if (countLabel != null) {
            int selectedCount = getSelectedContacts().size();
            updateCountLabel(countLabel, allContactData.size(), selectedCount);
        }
    }

    private void updateCountLabel(Label countLabel, int totalCount, int selectedCount) {
        countLabel.setText(String.format("📊 Total: %d contacts | Selected: %d contacts", totalCount, selectedCount));
        countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
    }

    // Wrapper class for contacts in the table
    public static class ContactWrapper {
        private final Contact contact;
        private final BooleanProperty selected;
        private final String fullName;
        private final String email;
        private final String phone;
        private final String memberStatus;
        private final String address;

        public ContactWrapper(Contact contact) {
            System.out.println("🔧 Creating ContactWrapper for: " + contact.getFirstName() + " " + contact.getLastName());

            this.contact = contact;
            this.selected = new SimpleBooleanProperty(false);
            this.fullName = contact.getFirstName() + " " + contact.getLastName();
            this.email = contact.getEmail() != null ? contact.getEmail() : "";
            this.phone = contact.getPhoneNum() != null ? contact.getPhoneNum() : "";
            this.memberStatus = contact.isMember() ? "✅ Yes" : "❌ No";

            // Build address string
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
            this.address = addressBuilder.toString().replaceAll(", $", "");

            System.out.println("🔧 ContactWrapper created: " + this.fullName + " (" + this.email + ")");
        }

        // Property getters
        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getMemberStatus() {
            return memberStatus;
        }

        public String getAddress() {
            return address;
        }

        public Contact getContact() {
            return contact;
        }
    }
}