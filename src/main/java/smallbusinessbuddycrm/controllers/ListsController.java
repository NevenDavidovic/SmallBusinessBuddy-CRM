package smallbusinessbuddycrm.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import smallbusinessbuddycrm.database.ListsDAO;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.List;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListsController implements Initializable {

    // FXML Controls
    @FXML private Label listsTitle;
    @FXML private Label listsCount;
    @FXML private Button createListButton;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private TableView<ListRow> listsTable;
    @FXML private TableColumn<ListRow, String> nameColumn;
    @FXML private TableColumn<ListRow, Integer> listSizeColumn;
    @FXML private TableColumn<ListRow, String> typeColumn;
    @FXML private TableColumn<ListRow, String> creatorColumn;
    @FXML private TableColumn<ListRow, String> lastUpdatedColumn;
    @FXML private TableColumn<ListRow, HBox> actionsColumn;

    private ListsDAO listsDAO;
    private ContactDAO contactDAO;
    private ObservableList<ListRow> listsData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing Lists Controller...");

        try {
            listsDAO = new ListsDAO();
            contactDAO = new ContactDAO();
            listsData = FXCollections.observableArrayList();

            updateTexts();
            LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);

            setupTable();
            setupEventHandlers();
            loadLists();

            System.out.println("Lists Controller initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing Lists Controller: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog to user
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Initialization Error");
                alert.setHeaderText("Failed to initialize Lists");
                alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check the console for details.");
                alert.showAndWait();
            });
        }
    }

    private void setupTable() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        listSizeColumn.setCellValueFactory(new PropertyValueFactory<>("contactCount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        creatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));

        listsTable.setItems(listsData);

        // Set placeholder text
        listsTable.setPlaceholder(new Label("No lists found. Click 'Create List' to get started!"));

        System.out.println("Table setup completed");
    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main labels and buttons
        if (listsTitle != null) listsTitle.setText(languageManager.getText("lists.page.title"));
        if (createListButton != null) createListButton.setText(languageManager.getText("lists.create.new"));
        if (refreshButton != null) refreshButton.setText(languageManager.getText("lists.refresh"));
        if (searchField != null) searchField.setPromptText(languageManager.getText("lists.search.placeholder"));

        // Update table columns
        if (nameColumn != null) nameColumn.setText(languageManager.getText("lists.column.name"));
        if (listSizeColumn != null) listSizeColumn.setText(languageManager.getText("lists.column.contacts"));
        if (typeColumn != null) typeColumn.setText(languageManager.getText("lists.column.type"));
        if (creatorColumn != null) creatorColumn.setText(languageManager.getText("lists.column.creator"));
        if (lastUpdatedColumn != null) lastUpdatedColumn.setText(languageManager.getText("lists.column.last.updated"));
        if (actionsColumn != null) actionsColumn.setText(languageManager.getText("lists.column.actions"));

        // Update table placeholder
        if (listsTable != null) {
            listsTable.setPlaceholder(new Label(languageManager.getText("lists.no.lists.found")));
        }

        System.out.println("Lists view texts updated");
    }


    private void setupEventHandlers() {
        if (createListButton != null) {
            createListButton.setOnAction(e -> createNewList());
            System.out.println("Create button handler set");
        } else {
            System.err.println("Create button is null!");
        }

        if (refreshButton != null) {
            refreshButton.setOnAction(e -> loadLists());
            System.out.println("Refresh button handler set");
        } else {
            System.err.println("Refresh button is null!");
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadLists();
                } else {
                    searchLists(newValue.trim());
                }
            });
            System.out.println("Search field handler set");
        } else {
            System.err.println("Search field is null!");
        }
    }

    private void loadLists() {
        try {
            System.out.println("=== ListsController.loadLists() Debug ===");
            System.out.println("Clearing existing table data...");
            listsData.clear();

            System.out.println("Calling ListsDAO.getAllActiveLists()...");
            ArrayList<List> lists = listsDAO.getAllActiveLists();
            System.out.println("DAO returned " + lists.size() + " lists");

            System.out.println("Processing lists for table display:");
            for (int i = 0; i < lists.size(); i++) {
                List list = lists.get(i);
                System.out.println("  Processing list " + (i+1) + ": " + list.getName() + " (ID: " + list.getId() + ")");

                try {
                    ListRow row = new ListRow(list, this);
                    listsData.add(row);
                    System.out.println("    ✅ Added to table successfully");
                } catch (Exception e) {
                    System.err.println("    ❌ Error creating ListRow: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Update count label with translation
            if (listsCount != null) {
                LanguageManager languageManager = LanguageManager.getInstance();
                String countText = languageManager.getText("lists.count").replace("{0}", String.valueOf(lists.size()));
                listsCount.setText(countText);
                System.out.println("Updated count label to: " + countText);
            } else {
                System.err.println("⚠️ listsCount label is null!");
            }

            System.out.println("Final table data size: " + listsData.size());
            System.out.println("Table items property size: " +
                    (listsTable != null && listsTable.getItems() != null ?
                            listsTable.getItems().size() : "table is null"));

            // Force table refresh
            if (listsTable != null) {
                listsTable.refresh();
                System.out.println("Table refreshed");
            }

            System.out.println("=== loadLists() completed ===");
        } catch (Exception e) {
            System.err.println("Error loading lists: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error loading lists: " + e.getMessage());
        }
    }

    // Add this method to refresh translations when language changes
    public void refreshLanguage() {
        updateTexts();
        loadLists(); // Reload to update count text
    }

    private void searchLists(String searchTerm) {
        try {
            System.out.println("Searching lists for: " + searchTerm);
            listsData.clear();
            ArrayList<List> lists = listsDAO.searchListsByName(searchTerm);

            for (List list : lists) {
                ListRow row = new ListRow(list, this);
                listsData.add(row);
            }

            System.out.println("Search found " + lists.size() + " lists for: " + searchTerm);
        } catch (Exception e) {
            System.err.println("Error searching lists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void createNewList() {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create New List");
            dialog.setHeaderText("Create a new contact list");
            dialog.setContentText("List name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String listName = result.get().trim();

                // Ask for description (optional)
                TextInputDialog descDialog = new TextInputDialog();
                descDialog.setTitle("List Description");
                descDialog.setHeaderText("Add a description for: " + listName);
                descDialog.setContentText("Description (optional):");

                Optional<String> descResult = descDialog.showAndWait();
                String description = descResult.orElse("");

                // Create the list
                List newList = new List(listName, description, "Current User");

                if (listsDAO.createList(newList)) {
                    showSuccessAlert("List '" + listName + "' created successfully!");
                    loadLists();
                } else {
                    showErrorAlert("Failed to create list. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating list: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error creating list: " + e.getMessage());
        }
    }

    // ✅ UPDATED METHOD: Shows contacts in beautiful modal WITH REMOVE BUTTONS
    public void openListDetailModal(List list) {
        try {
            System.out.println("🔍 Opening detail modal for list: " + list.getName() + " (ID: " + list.getId() + ")");

            // Get contacts in this list using your existing ContactDAO
            java.util.List<Contact> contacts = contactDAO.getContactsInList(list.getId());
            System.out.println("📊 Found " + contacts.size() + " contacts in this list");

            // Create modal dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("👥 Contacts in List: " + list.getName());
            dialog.setWidth(1000); // Made wider for remove button column
            dialog.setHeight(650);

            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));
            dialogVbox.setStyle("-fx-background-color: #f8f9fa;");

            // Header section
            VBox headerBox = new VBox(5);
            Label headerLabel = new Label("📋 " + list.getName());
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label contactCountLabel = new Label("👥 " + contacts.size() + " contacts");
            contactCountLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

            Label descLabel = new Label("📄 " + (list.getDescription() != null && !list.getDescription().trim().isEmpty()
                    ? list.getDescription()
                    : "No description provided"));
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
            descLabel.setWrapText(true);

            headerBox.getChildren().addAll(headerLabel, contactCountLabel, descLabel);

            // Separator
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #bdc3c7;");

            if (contacts.isEmpty()) {
                // Empty state
                VBox emptyStateBox = new VBox(20);
                emptyStateBox.setAlignment(Pos.CENTER);
                emptyStateBox.setPadding(new Insets(50));

                Label emptyIcon = new Label("📭");
                emptyIcon.setStyle("-fx-font-size: 48px;");

                Label emptyLabel = new Label("No contacts in this list yet");
                emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

                Label emptyDesc = new Label("Use the '➕ Add' button to add contacts to this list");
                emptyDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

                emptyStateBox.getChildren().addAll(emptyIcon, emptyLabel, emptyDesc);
                dialogVbox.getChildren().addAll(headerBox, separator, emptyStateBox);
            } else {
                // Create contacts table WITH REMOVE BUTTONS
                TableView<Contact> contactsTable = new TableView<>();
                contactsTable.setPrefHeight(400);
                contactsTable.setStyle("-fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-width: 1px;");

                // Define columns
                TableColumn<Contact, String> nameCol = new TableColumn<>("👤 Full Name");
                nameCol.setPrefWidth(160);
                nameCol.setCellValueFactory(cellData -> {
                    Contact contact = cellData.getValue();
                    String fullName = (contact.getFirstName() != null ? contact.getFirstName() : "") +
                            " " +
                            (contact.getLastName() != null ? contact.getLastName() : "");
                    return new SimpleStringProperty(fullName.trim());
                });

                TableColumn<Contact, String> emailCol = new TableColumn<>("📧 Email");
                emailCol.setPrefWidth(200);
                emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

                TableColumn<Contact, String> phoneCol = new TableColumn<>("📞 Phone");
                phoneCol.setPrefWidth(130);
                phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));

                TableColumn<Contact, String> addressCol = new TableColumn<>("🏠 Address");
                addressCol.setPrefWidth(180);
                addressCol.setCellValueFactory(cellData -> {
                    Contact contact = cellData.getValue();
                    String address = "";
                    if (contact.getStreetNum() != null) address += contact.getStreetNum() + " ";
                    if (contact.getStreetName() != null) address += contact.getStreetName() + ", ";
                    if (contact.getCity() != null) address += contact.getCity();
                    return new SimpleStringProperty(address.trim().replaceAll(",$", ""));
                });

                TableColumn<Contact, String> memberCol = new TableColumn<>("🏷️ Member");
                memberCol.setPrefWidth(80);
                memberCol.setCellValueFactory(cellData -> {
                    boolean isMember = cellData.getValue().isMember();
                    return new SimpleStringProperty(isMember ? "✅ Yes" : "❌ No");
                });

                // 🔥 NEW: Remove button column
                TableColumn<Contact, Void> removeCol = new TableColumn<>("Remove");
                removeCol.setPrefWidth(100);
                removeCol.setSortable(false);
                removeCol.setCellFactory(column -> new TableCell<Contact, Void>() {
                    private final Button removeBtn = new Button("❌ Remove");

                    {
                        removeBtn.setOnAction(e -> {
                            Contact contact = getTableView().getItems().get(getIndex());

                            // Show confirmation dialog
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmAlert.setTitle("Remove Contact");
                            confirmAlert.setHeaderText("Remove from List");
                            confirmAlert.setContentText("Are you sure you want to remove " +
                                    contact.getFirstName() + " " + contact.getLastName() + " from the list '" + list.getName() + "'?");

                            ButtonType yesButton = new ButtonType("Yes, Remove", ButtonBar.ButtonData.YES);
                            ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
                            confirmAlert.getButtonTypes().setAll(yesButton, noButton);

                            confirmAlert.showAndWait().ifPresent(response -> {
                                if (response == yesButton) {
                                    try {
                                        // Remove from database
                                        if (listsDAO.removeContactFromList(list.getId(), contact.getId())) {
                                            // Remove from table
                                            getTableView().getItems().remove(contact);

                                            // Update contact count label
                                            contactCountLabel.setText("👥 " + getTableView().getItems().size() + " contacts");

                                            // Show success message
                                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                            successAlert.setTitle("Contact Removed");
                                            successAlert.setHeaderText(null);
                                            successAlert.setContentText(contact.getFirstName() + " " + contact.getLastName() +
                                                    " has been removed from the list '" + list.getName() + "'.");
                                            successAlert.show();

                                            // Refresh the main lists table to update contact counts
                                            Platform.runLater(() -> loadLists());

                                            System.out.println("✅ Successfully removed " + contact.getFirstName() + " " + contact.getLastName() + " from list");
                                        } else {
                                            showErrorAlert("Failed to remove contact from list. Please try again.");
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("❌ Error removing contact from list: " + ex.getMessage());
                                        ex.printStackTrace();
                                        showErrorAlert("Error removing contact: " + ex.getMessage());
                                    }
                                }
                            });
                        });

                        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                "-fx-padding: 4px 8px; -fx-font-size: 11px; -fx-border-radius: 4px;");

                        // Hover effects
                        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                                "-fx-background-color: #c0392b; -fx-text-fill: white; " +
                                        "-fx-padding: 4px 8px; -fx-font-size: 11px; -fx-border-radius: 4px;"));

                        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                        "-fx-padding: 4px 8px; -fx-font-size: 11px; -fx-border-radius: 4px;"));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                            setGraphic(null);
                        } else {
                            setGraphic(removeBtn);
                        }
                    }
                });

                contactsTable.getColumns().addAll(nameCol, emailCol, phoneCol, addressCol, memberCol, removeCol);

                // Style the table
                contactsTable.setRowFactory(tv -> {
                    TableRow<Contact> row = new TableRow<>();
                    row.setStyle("-fx-background-color: white;");
                    row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ecf0f1;"));
                    row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white;"));
                    return row;
                });

                // Add data to table
                ObservableList<Contact> contactData = FXCollections.observableArrayList(contacts);
                contactsTable.setItems(contactData);

                dialogVbox.getChildren().addAll(headerBox, separator, contactsTable);
            }

            // Button section
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(20, 0, 0, 0));

            Button addContactsBtn = getButton(list, dialog);
            Button closeButton = new Button("✖️ Close");
            closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
            closeButton.setOnAction(e -> dialog.close());

            buttonBox.getChildren().addAll(addContactsBtn, closeButton);
            dialogVbox.getChildren().add(buttonBox);

            Scene dialogScene = new Scene(dialogVbox);
            dialog.setScene(dialogScene);
            dialog.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ Error opening list detail modal: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to load contacts for this list: " + e.getMessage());
        }
    }

    @NotNull
    private Button getButton(List list, Stage dialog) {
        Button addContactsBtn = new Button("➕ Add Contacts");

        addContactsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
        addContactsBtn.setOnAction(e -> {
            System.out.println("🔧 Add Contacts button clicked from modal");


            dialog.close();


            Platform.runLater(() -> {
                System.out.println("🔧 Modal closed, now opening ContactSelectionDialog");
                addContactsToList(list);
            });
        });
        return addContactsBtn;
    }


    public void addContactsToList(List list) {
        try {
            System.out.println("🔄 Adding contacts to list: " + list.getName());


            java.util.List<Contact> allContacts = contactDAO.getAllContacts();
            System.out.println("📊 Found " + allContacts.size() + " total contacts in database");

            if (allContacts.isEmpty()) {
                showWarningAlert("No contacts available. Please add contacts first.");
                return;
            }

            // Get contacts already in this list to filter them out
            java.util.List<Contact> contactsInList = contactDAO.getContactsInList(list.getId());
            System.out.println("📋 " + contactsInList.size() + " contacts already in this list");

            // Filter out contacts that are already in the list
            java.util.List<Contact> availableContacts = new ArrayList<>();
            for (Contact contact : allContacts) {
                boolean alreadyInList = false;
                for (Contact existingContact : contactsInList) {
                    if (contact.getId() == existingContact.getId()) {
                        alreadyInList = true;
                        break;
                    }
                }
                if (!alreadyInList) {
                    availableContacts.add(contact);
                }
            }

            System.out.println("✅ " + availableContacts.size() + " contacts available to add");

            if (availableContacts.isEmpty()) {
                showWarningAlert("All contacts are already in this list!");
                return;
            }

            // Create selection dialog with your advanced ContactSelectionDialog
            ContactSelectionDialog dialog = new ContactSelectionDialog(availableContacts);
            Optional<java.util.List<Contact>> result = dialog.showAndWait();

            if (result.isPresent()) {
                java.util.List<Contact> selectedContacts = result.get();
                System.out.println("👤 User selected " + selectedContacts.size() + " contacts");

                if (selectedContacts.isEmpty()) {
                    showWarningAlert("No contacts were selected.");
                    return;
                }

                int addedCount = 0;
                int failedCount = 0;

                for (Contact contact : selectedContacts) {
                    try {
                        if (listsDAO.addContactToList(list.getId(), contact.getId())) {
                            addedCount++;
                            System.out.println("✅ Added " + contact.getFirstName() + " " + contact.getLastName() + " to list");
                        } else {
                            failedCount++;
                            System.err.println("❌ Failed to add " + contact.getFirstName() + " " + contact.getLastName() + " to list");
                        }
                    } catch (Exception e) {
                        failedCount++;
                        System.err.println("❌ Exception adding contact " + contact.getId() + ": " + e.getMessage());
                    }
                }

                // Show appropriate message based on results
                if (addedCount > 0 && failedCount == 0) {
                    showSuccessAlert("✅ Successfully added " + addedCount + " contact(s) to list '" + list.getName() + "'!");
                    loadLists(); // Refresh to update contact counts
                } else if (addedCount > 0 && failedCount > 0) {
                    showWarningAlert("⚠️ Added " + addedCount + " contact(s) successfully, but " + failedCount + " failed. Please try again for the failed contacts.");
                    loadLists(); // Still refresh to show successful additions
                } else {
                    showErrorAlert("❌ Failed to add any contacts to the list. Please try again.");
                }
            } else {
                System.out.println("🚫 User cancelled contact selection");
            }
        } catch (Exception e) {
            System.err.println("❌ Error adding contacts to list: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error adding contacts to list: " + e.getMessage());
        }
    }

    // Method to edit a list
    public void editList(List list) {
        try {
            TextInputDialog dialog = new TextInputDialog(list.getName());
            dialog.setTitle("Edit List");
            dialog.setHeaderText("Edit list: " + list.getName());
            dialog.setContentText("List name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                list.setName(result.get().trim());

                if (listsDAO.updateList(list)) {
                    showSuccessAlert("List updated successfully!");
                    loadLists();
                } else {
                    showErrorAlert("Failed to update list.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error editing list: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error editing list: " + e.getMessage());
        }
    }

    // Method to delete a list
    public void deleteList(List list) {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete List");
            confirmAlert.setContentText("Are you sure you want to delete the list '" + list.getName() + "'?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (listsDAO.deleteList(list.getId())) {
                    showSuccessAlert("List '" + list.getName() + "' deleted successfully!");
                    loadLists();
                } else {
                    showErrorAlert("Failed to delete list.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting list: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error deleting list: " + e.getMessage());
        }
    }

    // Utility methods for alerts
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table rows
    public static class ListRow {
        private final List list;
        private final String name;
        private final Integer contactCount;
        private final String type;
        private final String creator;
        private final String lastUpdated;
        private final HBox actions;

        public ListRow(List list, ListsController controller) {
            this.list = list;
            this.name = list.getName();
            this.contactCount = list.getListSize();
            this.type = list.getType();
            this.creator = list.getCreator();
            this.lastUpdated = list.getFormattedUpdatedAt();

            // Create action buttons
            this.actions = createActionButtons(controller);
        }

        private HBox createActionButtons(ListsController controller) {
            HBox buttonBox = new HBox(5);
            buttonBox.setAlignment(Pos.CENTER);

            // View Button - Blue/Teal
            Button viewBtn = new Button("View");
            viewBtn.setPrefWidth(60);
            viewBtn.setMinWidth(60);
            viewBtn.setStyle(
                    "-fx-background-color: #17a2b8; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-font-size: 11px; " +
                            "-fx-cursor: hand;"
            );
            viewBtn.setOnAction(e -> controller.openListDetailModal(list));

            // Add Contacts Button - Green
            Button addContactsBtn = new Button("Add");
            addContactsBtn.setPrefWidth(60);
            addContactsBtn.setMinWidth(60);
            addContactsBtn.setStyle(
                    "-fx-background-color: #28a745; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-font-size: 11px; " +
                            "-fx-cursor: hand;"
            );
            addContactsBtn.setOnAction(e -> controller.addContactsToList(list));

            // Edit Button - Orange
            Button editBtn = new Button("Edit");
            editBtn.setPrefWidth(60);
            editBtn.setMinWidth(60);
            editBtn.setStyle(
                    "-fx-background-color: #fd7e14; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-font-size: 11px; " +
                            "-fx-cursor: hand;"
            );
            editBtn.setOnAction(e -> controller.editList(list));

            // Delete Button - Red
            Button deleteBtn = new Button("Delete");
            deleteBtn.setPrefWidth(70);
            deleteBtn.setMinWidth(70);
            deleteBtn.setStyle(
                    "-fx-background-color: #dc3545; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-font-size: 11px; " +
                            "-fx-cursor: hand;"
            );
            deleteBtn.setOnAction(e -> controller.deleteList(list));

            buttonBox.getChildren().addAll(viewBtn, addContactsBtn, editBtn, deleteBtn);
            return buttonBox;
        }

        // Helper method to add hover effects (add this to your ListsController class)
        private void addHoverEffect(Button button, String hoverColor, String originalColor) {
            button.setOnMouseEntered(e -> {
                String currentStyle = button.getStyle();
                String newStyle = currentStyle.replaceAll("-fx-background-color: #[a-fA-F0-9]{6}",
                        "-fx-background-color: " + hoverColor);
                button.setStyle(newStyle);
            });

            button.setOnMouseExited(e -> {
                String currentStyle = button.getStyle();
                String newStyle = currentStyle.replaceAll("-fx-background-color: #[a-fA-F0-9]{6}",
                        "-fx-background-color: " + originalColor);
                button.setStyle(newStyle);
            });
        }

        // Getters for table columns
        public String getName() { return name; }
        public Integer getContactCount() { return contactCount; }
        public String getType() { return type; }
        public String getCreator() { return creator; }
        public String getLastUpdated() { return lastUpdated; }
        public HBox getActions() { return actions; }
        public List getList() { return list; }
    }
}