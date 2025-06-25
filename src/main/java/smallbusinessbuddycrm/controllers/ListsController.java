package smallbusinessbuddycrm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import smallbusinessbuddycrm.dao.ListsDAO;
import smallbusinessbuddycrm.model.List;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListsController implements Initializable {

    // Top section controls
    @FXML private Button whatsNewButton;
    @FXML private ComboBox<String> adminSettingsComboBox;
    @FXML private Button importButton;
    @FXML private ComboBox<String> quickCreateComboBox;
    @FXML private Button createListButton;

    // Tab buttons
    @FXML private Button allListsButton;
    @FXML private Button unusedListsButton;
    @FXML private Button recentlyDeletedButton;

    // Filter controls
    @FXML private ComboBox<String> creatorsComboBox;
    @FXML private ComboBox<String> typesComboBox;
    @FXML private ComboBox<String> objectsComboBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionsComboBox;

    // Table
    @FXML private TableView<ListWrapper> listsTable;
    @FXML private TableColumn<ListWrapper, Boolean> selectColumn;
    @FXML private TableColumn<ListWrapper, String> nameColumn;
    @FXML private TableColumn<ListWrapper, Integer> listSizeColumn;
    @FXML private TableColumn<ListWrapper, String> typeColumn;
    @FXML private TableColumn<ListWrapper, String> objectColumn;
    @FXML private TableColumn<ListWrapper, String> lastUpdatedColumn;
    @FXML private TableColumn<ListWrapper, String> creatorColumn;
    @FXML private TableColumn<ListWrapper, String> folderColumn;
    @FXML private TableColumn<ListWrapper, String> usedInColumn;

    private ListsDAO listsDAO;
    private ObservableList<ListWrapper> listsData;
    private String currentView = "ALL"; // ALL, UNUSED, DELETED

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listsDAO = new ListsDAO();
        listsData = FXCollections.observableArrayList();

        initializeTable();
        initializeComboBoxes();
        initializeEventHandlers();

        loadLists();
    }

    private void initializeTable() {
        // Setup table columns
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        listSizeColumn.setCellValueFactory(new PropertyValueFactory<>("listSize"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        objectColumn.setCellValueFactory(new PropertyValueFactory<>("objectType"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("formattedUpdatedAt"));
        creatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));
        usedInColumn.setCellValueFactory(new PropertyValueFactory<>("usedIn"));

        listsTable.setItems(listsData);
        listsTable.setEditable(true);

        // Add context menu for right-click actions
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editSelectedList());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteSelectedLists());

        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setOnAction(e -> duplicateSelectedList());

        contextMenu.getItems().addAll(editItem, deleteItem, duplicateItem);
        listsTable.setContextMenu(contextMenu);
    }

    private void initializeComboBoxes() {
        // Admin settings
        adminSettingsComboBox.setItems(FXCollections.observableArrayList(
                "List Settings", "User Permissions", "Export Settings"
        ));

        // Quick create
        quickCreateComboBox.setItems(FXCollections.observableArrayList(
                "Contact List", "Custom List", "Import from CSV"
        ));

        // Filter comboboxes
        creatorsComboBox.setItems(FXCollections.observableArrayList(
                "All creators", "Me", "Team Members"
        ));

        typesComboBox.setItems(FXCollections.observableArrayList(
                "All types", "Custom", "Smart", "Import"
        ));

        objectsComboBox.setItems(FXCollections.observableArrayList(
                "All objects", "Contact", "Lead", "Account"
        ));

        // Actions
        actionsComboBox.setItems(FXCollections.observableArrayList(
                "Actions", "Export Selected", "Delete Selected", "Move to Folder"
        ));
    }

    private void initializeEventHandlers() {
        // Tab buttons
        allListsButton.setOnAction(e -> switchToAllLists());
        unusedListsButton.setOnAction(e -> switchToUnusedLists());
        recentlyDeletedButton.setOnAction(e -> switchToRecentlyDeleted());

        // Main action buttons
        createListButton.setOnAction(e -> createNewList());
        importButton.setOnAction(e -> importList());

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadLists();
            } else {
                searchLists(newValue.trim());
            }
        });

        // Combo box actions
        quickCreateComboBox.setOnAction(e -> handleQuickCreate());
        actionsComboBox.setOnAction(e -> handleActions());
    }

    private void switchToAllLists() {
        currentView = "ALL";
        updateTabButtonStyles();
        loadLists();
    }

    private void switchToUnusedLists() {
        currentView = "UNUSED";
        updateTabButtonStyles();
        loadLists();
    }

    private void switchToRecentlyDeleted() {
        currentView = "DELETED";
        updateTabButtonStyles();
        loadLists();
    }

    private void updateTabButtonStyles() {
        // Remove selected style from all buttons
        allListsButton.getStyleClass().remove("tab-button-selected");
        unusedListsButton.getStyleClass().remove("tab-button-selected");
        recentlyDeletedButton.getStyleClass().remove("tab-button-selected");

        // Add selected style to current button
        switch (currentView) {
            case "ALL":
                allListsButton.getStyleClass().add("tab-button-selected");
                break;
            case "UNUSED":
                unusedListsButton.getStyleClass().add("tab-button-selected");
                break;
            case "DELETED":
                recentlyDeletedButton.getStyleClass().add("tab-button-selected");
                break;
        }
    }

    private void loadLists() {
        listsData.clear();
        ArrayList<List> lists = new ArrayList<>();

        switch (currentView) {
            case "ALL":
                lists = listsDAO.getAllActiveLists();
                break;
            case "UNUSED":
                lists = listsDAO.getUnusedLists();
                break;
            case "DELETED":
                lists = listsDAO.getRecentlyDeletedLists();
                break;
        }

        for (List list : lists) {
            listsData.add(new ListWrapper(list));
        }
    }

    private void searchLists(String searchTerm) {
        if (currentView.equals("ALL")) {
            listsData.clear();
            ArrayList<List> lists = listsDAO.searchListsByName(searchTerm);
            for (List list : lists) {
                listsData.add(new ListWrapper(list));
            }
        }
    }

    private void createNewList() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New List");
        dialog.setHeaderText("Create a new contact list");
        dialog.setContentText("List name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String listName = result.get().trim();

            // Create description dialog
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("List Description");
            descDialog.setHeaderText("Add a description for: " + listName);
            descDialog.setContentText("Description (optional):");

            Optional<String> descResult = descDialog.showAndWait();
            String description = descResult.orElse("");

            List newList = new List(listName, description, "Current User");

            if (listsDAO.createList(newList)) {
                showSuccessAlert("List created successfully!");
                loadLists();
            } else {
                showErrorAlert("Failed to create list. Please try again.");
            }
        }
    }

    private void editSelectedList() {
        ListWrapper selectedWrapper = listsTable.getSelectionModel().getSelectedItem();
        if (selectedWrapper != null) {
            List selectedList = selectedWrapper.getList();

            TextInputDialog dialog = new TextInputDialog(selectedList.getName());
            dialog.setTitle("Edit List");
            dialog.setHeaderText("Edit list: " + selectedList.getName());
            dialog.setContentText("List name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                selectedList.setName(result.get().trim());

                if (listsDAO.updateList(selectedList)) {
                    showSuccessAlert("List updated successfully!");
                    loadLists();
                } else {
                    showErrorAlert("Failed to update list. Please try again.");
                }
            }
        } else {
            showWarningAlert("Please select a list to edit.");
        }
    }

    private void deleteSelectedLists() {
        ArrayList<ListWrapper> selectedLists = new ArrayList<>();
        for (ListWrapper wrapper : listsData) {
            if (wrapper.isSelected()) {
                selectedLists.add(wrapper);
            }
        }

        if (selectedLists.isEmpty()) {
            showWarningAlert("Please select at least one list to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Selected Lists");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedLists.size() + " list(s)?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deletedCount = 0;
            for (ListWrapper wrapper : selectedLists) {
                if (currentView.equals("DELETED")) {
                    // Permanently delete if in deleted view
                    if (listsDAO.permanentlyDeleteList(wrapper.getList().getId())) {
                        deletedCount++;
                    }
                } else {
                    // Soft delete if in active views
                    if (listsDAO.deleteList(wrapper.getList().getId())) {
                        deletedCount++;
                    }
                }
            }

            if (deletedCount > 0) {
                showSuccessAlert(deletedCount + " list(s) deleted successfully!");
                loadLists();
            } else {
                showErrorAlert("Failed to delete lists. Please try again.");
            }
        }
    }

    private void duplicateSelectedList() {
        ListWrapper selectedWrapper = listsTable.getSelectionModel().getSelectedItem();
        if (selectedWrapper != null) {
            List originalList = selectedWrapper.getList();

            TextInputDialog dialog = new TextInputDialog(originalList.getName() + " (Copy)");
            dialog.setTitle("Duplicate List");
            dialog.setHeaderText("Duplicate list: " + originalList.getName());
            dialog.setContentText("New list name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                List duplicateList = new List(
                        result.get().trim(),
                        originalList.getDescription(),
                        originalList.getCreator()
                );
                duplicateList.setType(originalList.getType());
                duplicateList.setObjectType(originalList.getObjectType());
                duplicateList.setFolder(originalList.getFolder());

                if (listsDAO.createList(duplicateList)) {
                    showSuccessAlert("List duplicated successfully!");
                    loadLists();
                } else {
                    showErrorAlert("Failed to duplicate list. Please try again.");
                }
            }
        } else {
            showWarningAlert("Please select a list to duplicate.");
        }
    }

    private void importList() {
        showInfoAlert("Import functionality will be implemented in future updates.");
    }

    private void handleQuickCreate() {
        String selected = quickCreateComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            switch (selected) {
                case "Contact List":
                    createNewList();
                    break;
                case "Custom List":
                    createNewList();
                    break;
                case "Import from CSV":
                    importList();
                    break;
            }
            quickCreateComboBox.getSelectionModel().clearSelection();
        }
    }

    private void handleActions() {
        String selected = actionsComboBox.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.equals("Actions")) {
            switch (selected) {
                case "Export Selected":
                    exportSelectedLists();
                    break;
                case "Delete Selected":
                    deleteSelectedLists();
                    break;
                case "Move to Folder":
                    moveToFolder();
                    break;
            }
            actionsComboBox.getSelectionModel().clearSelection();
        }
    }

    private void exportSelectedLists() {
        showInfoAlert("Export functionality will be implemented in future updates.");
    }

    private void moveToFolder() {
        ArrayList<ListWrapper> selectedLists = new ArrayList<>();
        for (ListWrapper wrapper : listsData) {
            if (wrapper.isSelected()) {
                selectedLists.add(wrapper);
            }
        }

        if (selectedLists.isEmpty()) {
            showWarningAlert("Please select at least one list to move.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Move to Folder");
        dialog.setHeaderText("Move " + selectedLists.size() + " list(s) to folder");
        dialog.setContentText("Folder name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String folderName = result.get().trim();
            int movedCount = 0;

            for (ListWrapper wrapper : selectedLists) {
                List list = wrapper.getList();
                list.setFolder(folderName);
                if (listsDAO.updateList(list)) {
                    movedCount++;
                }
            }

            if (movedCount > 0) {
                showSuccessAlert(movedCount + " list(s) moved to folder: " + folderName);
                loadLists();
            } else {
                showErrorAlert("Failed to move lists to folder.");
            }
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

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to wrap List objects for table display with selection
    public static class ListWrapper {
        private final List list;
        private final BooleanProperty selected;

        public ListWrapper(List list) {
            this.list = list;
            this.selected = new SimpleBooleanProperty(false);
        }

        public List getList() {
            return list;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        // Delegate methods for table columns
        public String getName() {
            return list.getName();
        }

        public int getListSize() {
            return list.getListSize();
        }

        public String getType() {
            return list.getType();
        }

        public String getObjectType() {
            return list.getObjectType();
        }

        public String getFormattedUpdatedAt() {
            return list.getFormattedUpdatedAt();
        }

        public String getCreator() {
            return list.getCreator();
        }

        public String getFolder() {
            return list.getFolder() != null ? list.getFolder() : "";
        }

        public String getUsedIn() {
            // This would be calculated based on where the list is used
            // For now, return empty string
            return "";
        }
    }
}