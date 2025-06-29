package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import smallbusinessbuddycrm.database.DatabaseConnection;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.database.WorkshopParticipantDAO;
import smallbusinessbuddycrm.model.Workshop;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WorkshopsViewController implements Initializable {

    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableColumn<Workshop, Boolean> selectColumn;
    @FXML private TableColumn<Workshop, Void> editColumn;
    @FXML private TableColumn<Workshop, String> nameColumn;
    @FXML private TableColumn<Workshop, String> fromDateColumn;
    @FXML private TableColumn<Workshop, String> toDateColumn;
    @FXML private TableColumn<Workshop, String> durationColumn;
    @FXML private TableColumn<Workshop, String> statusColumn;
    @FXML private TableColumn<Workshop, String> participantCountColumn;
    @FXML private TableColumn<Workshop, Void> manageParticipantsColumn;
    @FXML private TableColumn<Workshop, String> createdAtColumn;

    // UI Controls
    @FXML private Button createWorkshopButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button allWorkshopsButton;
    @FXML private Button activeWorkshopsButton;
    @FXML private Button upcomingWorkshopsButton;
    @FXML private Button pastWorkshopsButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;

    // Data lists
    private ObservableList<Workshop> allWorkshopsList = FXCollections.observableArrayList();
    private FilteredList<Workshop> filteredWorkshopsList;

    // DAOs
    private WorkshopDAO workshopDAO = new WorkshopDAO();
    private WorkshopParticipantDAO participantDAO = new WorkshopParticipantDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("WorkshopsViewController.initialize() called");

        // Initialize database first
        DatabaseConnection.initializeDatabase();

        setupTable();
        setupSearchAndFilters();
        loadWorkshops();
        setupEventHandlers();

        System.out.println("WorkshopsViewController initialized successfully");
    }

    private void setupTable() {
        // Set up checkbox column
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setEditable(true);

        // Set up edit button column
        editColumn.setCellFactory(tc -> new TableCell<Workshop, Void>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                editButton.setPrefWidth(50);
                editButton.setOnAction(event -> {
                    Workshop workshop = getTableView().getItems().get(getIndex());
                    handleEditWorkshop(workshop);
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

        // Set up manage participants column
        manageParticipantsColumn.setCellFactory(tc -> new TableCell<Workshop, Void>() {
            private final Button manageButton = new Button("Manage");

            {
                manageButton.setStyle("-fx-background-color: #0099cc; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 10px;");
                manageButton.setPrefWidth(60);
                manageButton.setOnAction(event -> {
                    Workshop workshop = getTableView().getItems().get(getIndex());
                    handleManageParticipants(workshop);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(manageButton);
                }
            }
        });

        // Set up column bindings
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        fromDateColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            return new SimpleStringProperty(workshop.getFormattedFromDate());
        });

        toDateColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            return new SimpleStringProperty(workshop.getFormattedToDate());
        });

        durationColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            long days = workshop.getDurationInDays();
            return new SimpleStringProperty(days > 0 ? days + " days" : "");
        });

        statusColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            String status;
            String style = "";

            if (workshop.isActive()) {
                status = "ACTIVE";
                style = "-fx-text-fill: #28a745; -fx-font-weight: bold;";
            } else if (workshop.isUpcoming()) {
                status = "UPCOMING";
                style = "-fx-text-fill: #0099cc; -fx-font-weight: bold;";
            } else if (workshop.isPast()) {
                status = "PAST";
                style = "-fx-text-fill: #6c757d;";
            } else {
                status = "DRAFT";
                style = "-fx-text-fill: #ffc107;";
            }

            return new SimpleStringProperty(status);
        });

        participantCountColumn.setCellValueFactory(cellData -> {
            Workshop workshop = cellData.getValue();
            // Get participant count from database
            Map<String, Integer> stats = participantDAO.getWorkshopStatistics(workshop.getId());
            int count = stats.getOrDefault("total_participants", 0);
            return new SimpleStringProperty(String.valueOf(count));
        });

        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Make table editable
        workshopsTable.setEditable(true);
        workshopsTable.setItems(filteredWorkshopsList);
    }

    private void setupSearchAndFilters() {
        // Create filtered list wrapping the original list
        filteredWorkshopsList = new FilteredList<>(allWorkshopsList, p -> true);

        // Set the table to use the filtered list
        workshopsTable.setItems(filteredWorkshopsList);

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredWorkshopsList.setPredicate(workshop -> {
            // If no search text, show workshops based on current filter
            if (searchText.isEmpty()) {
                return matchesCurrentFilter(workshop);
            }

            // Check if search text matches workshop name
            boolean matchesSearch = workshop.getName() != null &&
                    workshop.getName().toLowerCase().contains(searchText);

            // Return true only if matches both search and current filter
            return matchesSearch && matchesCurrentFilter(workshop);
        });

        updateRecordCount();
    }

    private boolean matchesCurrentFilter(Workshop workshop) {
        // Check which filter button is active based on their style
        String allStyle = allWorkshopsButton.getStyle();
        String activeStyle = activeWorkshopsButton.getStyle();
        String upcomingStyle = upcomingWorkshopsButton.getStyle();
        String pastStyle = pastWorkshopsButton.getStyle();

        if (allStyle.contains("#f5f8fa")) {
            return true; // Show all workshops
        } else if (activeStyle.contains("#f5f8fa")) {
            return workshop.isActive();
        } else if (upcomingStyle.contains("#f5f8fa")) {
            return workshop.isUpcoming();
        } else if (pastStyle.contains("#f5f8fa")) {
            return workshop.isPast();
        }

        return true; // Default: show all
    }

    private void loadWorkshops() {
        try {
            List<Workshop> workshops = workshopDAO.getAllWorkshops();
            System.out.println("DAO returned " + workshops.size() + " workshops");

            allWorkshopsList.setAll(workshops);
            updateRecordCount();

        } catch (Exception e) {
            System.err.println("Error loading workshops: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRecordCount() {
        int count = filteredWorkshopsList.size();
        recordCountLabel.setText(count + " record" + (count != 1 ? "s" : ""));
    }

    private void setupEventHandlers() {
        System.out.println("Setting up event handlers...");

        createWorkshopButton.setOnAction(e -> handleCreateWorkshop());
        deleteSelectedButton.setOnAction(e -> handleDeleteSelected());
        exportButton.setOnAction(e -> handleExportWorkshops());
        refreshButton.setOnAction(e -> handleRefresh());

        // Filter buttons
        allWorkshopsButton.setOnAction(e -> handleFilterButton(allWorkshopsButton));
        activeWorkshopsButton.setOnAction(e -> handleFilterButton(activeWorkshopsButton));
        upcomingWorkshopsButton.setOnAction(e -> handleFilterButton(upcomingWorkshopsButton));
        pastWorkshopsButton.setOnAction(e -> handleFilterButton(pastWorkshopsButton));

        System.out.println("Event handlers setup completed");
    }

    private void handleFilterButton(Button clickedButton) {
        // Reset all button styles to inactive
        allWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        activeWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        upcomingWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");
        pastWorkshopsButton.setStyle("-fx-background-color: white; -fx-border-color: #dfe3eb;");

        // Set clicked button to active style
        clickedButton.setStyle("-fx-background-color: #f5f8fa; -fx-border-color: #dfe3eb;");

        // Update the filter
        updateFilters();
    }

    private void handleCreateWorkshop() {
        try {
            Stage currentStage = (Stage) createWorkshopButton.getScene().getWindow();
            CreateWorkshopDialog dialog = new CreateWorkshopDialog(currentStage);

            if (dialog.showAndWait()) {
                Workshop newWorkshop = dialog.getResult();
                if (newWorkshop != null) {
                    allWorkshopsList.add(newWorkshop);
                    updateRecordCount();
                    workshopsTable.getSelectionModel().select(newWorkshop);
                    workshopsTable.scrollTo(newWorkshop);
                    System.out.println("New workshop added: " + newWorkshop.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening create workshop dialog: " + e.getMessage());
            e.printStackTrace();

            // Show error to user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Create Workshop Failed");
            errorAlert.setContentText("An error occurred while creating the workshop dialog: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void handleEditWorkshop(Workshop workshop) {
        try {
            Stage currentStage = (Stage) createWorkshopButton.getScene().getWindow();
            EditWorkshopDialog dialog = new EditWorkshopDialog(currentStage, workshop);

            if (dialog.showAndWait()) {
                // Refresh the table to show updated data
                workshopsTable.refresh();
                updateFilters(); // Re-apply filters in case status changed
                System.out.println("Workshop updated: " + workshop.getName());
            }
        } catch (Exception e) {
            System.err.println("Error opening edit workshop dialog: " + e.getMessage());
            e.printStackTrace();

            // Show error to user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Edit Workshop Failed");
            errorAlert.setContentText("An error occurred while opening the edit dialog: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void handleDeleteSelected() {
        // Get all selected workshops from the filtered list
        List<Workshop> selectedWorkshops = filteredWorkshopsList.stream()
                .filter(Workshop::isSelected)
                .collect(Collectors.toList());

        if (selectedWorkshops.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No workshops selected");
            alert.setContentText("Please select one or more workshops to delete using the checkboxes.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete selected workshops?");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedWorkshops.size() +
                " workshop" + (selectedWorkshops.size() > 1 ? "s" : "") + "? This will also delete all participants. This action cannot be undone.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                List<Integer> workshopIds = selectedWorkshops.stream()
                        .map(Workshop::getId)
                        .collect(Collectors.toList());

                boolean success = workshopDAO.deleteWorkshops(workshopIds);

                if (success) {
                    // Remove from the original list (filtered list will update automatically)
                    allWorkshopsList.removeAll(selectedWorkshops);
                    updateRecordCount();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Workshops deleted");
                    successAlert.setContentText("Successfully deleted " + selectedWorkshops.size() + " workshop(s).");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Delete Failed");
                    errorAlert.setContentText("Failed to delete the selected workshops from the database.");
                    errorAlert.showAndWait();
                }

            } catch (Exception e) {
                System.err.println("Error deleting workshops: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleManageParticipants(Workshop workshop) {
        try {
            // For now, show a message. Later this will open the participants view
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Manage Participants");
            info.setHeaderText("Workshop: " + workshop.getName());

            // Get current participant statistics
            Map<String, Integer> stats = participantDAO.getWorkshopStatistics(workshop.getId());
            String message = String.format(
                    "Current Participants:\n" +
                            "Total: %d\n" +
                            "Adults: %d\n" +
                            "Children: %d\n" +
                            "Paid: %d\n" +
                            "Pending Payment: %d\n\n" +
                            "Participant management interface coming soon!",
                    stats.getOrDefault("total_participants", 0),
                    stats.getOrDefault("adult_count", 0),
                    stats.getOrDefault("child_count", 0),
                    stats.getOrDefault("paid_count", 0),
                    stats.getOrDefault("pending_count", 0)
            );

            info.setContentText(message);
            info.showAndWait();

            // TODO: Later this will navigate to workshop-participants-view
            // MainController.navigateToWorkshopParticipants(workshop);

        } catch (Exception e) {
            System.err.println("Error opening participant management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleExportWorkshops() {
        try {
            // Get currently visible workshops (filtered/searched)
            List<Workshop> workshopsToExport = new ArrayList<>(filteredWorkshopsList);

            if (workshopsToExport.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Data");
                alert.setHeaderText("No workshops to export");
                alert.setContentText("There are no workshops visible to export. Please check your filters or add some workshops first.");
                alert.showAndWait();
                return;
            }

            // For now, show export info. Later implement actual CSV export
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export Workshops");
            info.setHeaderText("Export functionality");
            info.setContentText("Will export " + workshopsToExport.size() + " workshops to CSV.\nExport functionality coming soon!");
            info.showAndWait();

        } catch (Exception e) {
            System.err.println("Error exporting workshops: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRefresh() {
        loadWorkshops();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText("Data Refreshed");
        successAlert.setContentText("Workshop data has been refreshed successfully!");
        successAlert.showAndWait();
    }
}