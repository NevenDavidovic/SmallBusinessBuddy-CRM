package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import smallbusinessbuddycrm.database.TeacherDAO;  // Changed from ContactDAO
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.model.Teacher;  // Changed from Contact
import smallbusinessbuddycrm.model.Workshop;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class WorkshopsReportController {

    @FXML private VBox root;
    @FXML private TextField nameFilterField;
    @FXML private TextField fromDateField;
    @FXML private TextField toDateField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Teacher> teacherComboBox;  // Changed from Contact to Teacher
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableColumn<Workshop, String> nameColumn;
    @FXML private TableColumn<Workshop, String> dateRangeColumn;
    @FXML private TableColumn<Workshop, Integer> durationColumn;
    @FXML private TableColumn<Workshop, String> statusColumn;
    @FXML private TableColumn<Workshop, String> teacherNameColumn;
    @FXML private Label totalWorkshopsLabel;
    @FXML private Label activeWorkshopsLabel;
    @FXML private Label upcomingWorkshopsLabel;
    @FXML private Label averageDurationLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> durationBarChart;
    @FXML private Button exportButton;

    private WorkshopDAO workshopDAO;
    private TeacherDAO teacherDAO;  // Changed from ContactDAO
    private ObservableList<Workshop> workshopList;
    private ObservableList<Teacher> teacherList;  // Changed from Contact to Teacher

    @FXML
    public void initialize() {
        System.out.println("🚀 WorkshopsReportController initializing...");

        workshopDAO = new WorkshopDAO();
        teacherDAO = new TeacherDAO();  // Changed from ContactDAO
        workshopList = FXCollections.observableArrayList();
        teacherList = FXCollections.observableArrayList();

        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateRangeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateRange()));
        durationColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty((int) cellData.getValue().getDurationInDays()).asObject());
        statusColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            String status = w.isActive() ? "Active" : w.isUpcoming() ? "Upcoming" : "Past";
            return new SimpleStringProperty(status);
        });

        // Updated teacher name column to use TeacherDAO
        teacherNameColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            Teacher teacher = w.getTeacherId() != null ? teacherDAO.getTeacherById(w.getTeacherId()) : null;
            String teacherName = teacher != null ?
                    (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                            (teacher.getLastName() != null ? teacher.getLastName() : "") : "No Teacher";
            return new SimpleStringProperty(teacherName.trim());
        });

        // Set up status combo box
        statusComboBox.setItems(FXCollections.observableArrayList("All", "Active", "Upcoming", "Past"));
        statusComboBox.getSelectionModel().selectFirst();

        // Load teachers using TeacherDAO
        loadTeachers();

        // Set up teacher combo box display
        teacherComboBox.setCellFactory(param -> new ListCell<Teacher>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.getId() == -1) {
                        setText("All Teachers");
                    } else {
                        String name = (item.getFirstName() != null ? item.getFirstName() : "") + " " +
                                (item.getLastName() != null ? item.getLastName() : "");
                        setText(name.trim().isEmpty() ? "Unknown Teacher" : name.trim());
                    }
                }
            }
        });

        teacherComboBox.setButtonCell(new ListCell<Teacher>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.getId() == -1) {
                        setText("All Teachers");
                    } else {
                        String name = (item.getFirstName() != null ? item.getFirstName() : "") + " " +
                                (item.getLastName() != null ? item.getLastName() : "");
                        setText(name.trim().isEmpty() ? "Unknown Teacher" : name.trim());
                    }
                }
            }
        });

        teacherComboBox.getSelectionModel().selectFirst();

        // Load initial data
        loadAllWorkshops();

        // Set up event handlers
        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

        System.out.println("✅ WorkshopsReportController initialization complete");
    }

    private void loadTeachers() {
        System.out.println("📚 Loading teachers from TeacherDAO...");

        try {
            List<Teacher> allTeachers = teacherDAO.getAllTeachers();
            System.out.println("📊 Retrieved " + allTeachers.size() + " teachers from database");

            // Create "All Teachers" option
            Teacher allTeachersOption = new Teacher();
            allTeachersOption.setId(-1);
            allTeachersOption.setFirstName("All");
            allTeachersOption.setLastName("Teachers");

            // Clear and populate teacher list
            teacherList.clear();
            teacherList.add(allTeachersOption);
            teacherList.addAll(allTeachers);

            // Set items to combo box
            teacherComboBox.setItems(teacherList);

            System.out.println("✅ Teacher combo box populated with " + teacherList.size() + " items");

            // Debug: Print first few teachers
            for (int i = 0; i < Math.min(3, teacherList.size()); i++) {
                Teacher t = teacherList.get(i);
                System.out.println("   Teacher " + i + ": " + t.getFirstName() + " " + t.getLastName() + " (ID: " + t.getId() + ")");
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading teachers: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            showAlert(Alert.AlertType.ERROR, "Error Loading Teachers",
                    "Failed to load teachers from database: " + e.getMessage());
        }
    }

    private void loadAllWorkshops() {
        System.out.println("📋 Loading all workshops...");

        try {
            List<Workshop> allWorkshops = workshopDAO.getAllWorkshops();
            System.out.println("📊 Retrieved " + allWorkshops.size() + " workshops from database");

            workshopList.setAll(allWorkshops);
            workshopsTable.setItems(workshopList);
            updateSummaryAndCharts();

            System.out.println("✅ Workshops loaded and displayed");

        } catch (Exception e) {
            System.err.println("❌ Error loading workshops: " + e.getMessage());
            e.printStackTrace();

            showAlert(Alert.AlertType.ERROR, "Error Loading Workshops",
                    "Failed to load workshops from database: " + e.getMessage());
        }
    }

    private void applyFilters() {
        System.out.println("🔍 Applying filters...");

        try {
            List<Workshop> filteredWorkshops = workshopDAO.getAllWorkshops();

            // Apply name filter
            String nameFilter = nameFilterField.getText().trim();
            if (!nameFilter.isEmpty()) {
                System.out.println("   Filtering by name: " + nameFilter);
                filteredWorkshops = workshopDAO.searchWorkshops(nameFilter);
            }

            // Apply date range filter
            try {
                LocalDate fromDate = null, toDate = null;
                if (!fromDateField.getText().trim().isEmpty()) {
                    fromDate = LocalDate.parse(fromDateField.getText().trim());
                    System.out.println("   From date: " + fromDate);
                }
                if (!toDateField.getText().trim().isEmpty()) {
                    toDate = LocalDate.parse(toDateField.getText().trim());
                    System.out.println("   To date: " + toDate);
                }

                if (fromDate != null || toDate != null) {
                    LocalDate finalFromDate = fromDate;
                    LocalDate finalToDate = toDate;
                    filteredWorkshops = filteredWorkshops.stream()
                            .filter(w -> {
                                if (finalFromDate != null && w.getFromDate() != null && w.getFromDate().isBefore(finalFromDate)) {
                                    return false;
                                }
                                if (finalToDate != null && w.getToDate() != null && w.getToDate().isAfter(finalToDate)) {
                                    return false;
                                }
                                return true;
                            })
                            .collect(Collectors.toList());
                }
            } catch (DateTimeParseException e) {
                System.err.println("❌ Invalid date format: " + e.getMessage());
                showAlert(Alert.AlertType.WARNING, "Invalid Date", "Please enter valid dates in YYYY-MM-DD format.");
                return;
            }

            // Apply status filter
            String status = statusComboBox.getValue();
            if (status != null && !status.equals("All")) {
                System.out.println("   Filtering by status: " + status);
                switch (status) {
                    case "Active":
                        filteredWorkshops = workshopDAO.getActiveWorkshops();
                        break;
                    case "Upcoming":
                        filteredWorkshops = workshopDAO.getUpcomingWorkshops(30);
                        break;
                    case "Past":
                        filteredWorkshops = filteredWorkshops.stream()
                                .filter(w -> !w.isActive() && !w.isUpcoming())
                                .collect(Collectors.toList());
                        break;
                }
            }

            // Apply teacher filter
            Teacher selectedTeacher = teacherComboBox.getValue();
            if (selectedTeacher != null && selectedTeacher.getId() != -1) {
                System.out.println("   Filtering by teacher: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
                filteredWorkshops = workshopDAO.getWorkshopsByTeacher(selectedTeacher.getId());
            } else if (selectedTeacher != null && selectedTeacher.getId() == -1) {
                System.out.println("   Showing all teachers");
                // Keep all workshops - no teacher filter
            }

            System.out.println("📊 Filter results: " + filteredWorkshops.size() + " workshops");

            workshopList.setAll(filteredWorkshops);
            updateSummaryAndCharts();

        } catch (Exception e) {
            System.err.println("❌ Error applying filters: " + e.getMessage());
            e.printStackTrace();

            showAlert(Alert.AlertType.ERROR, "Filter Error",
                    "An error occurred while applying filters: " + e.getMessage());
        }
    }

    private void clearFilters() {
        System.out.println("🧹 Clearing all filters...");

        nameFilterField.clear();
        fromDateField.clear();
        toDateField.clear();
        statusComboBox.getSelectionModel().selectFirst();
        teacherComboBox.getSelectionModel().selectFirst();
        loadAllWorkshops();

        System.out.println("✅ Filters cleared");
    }

    private void exportToCSV() {
        System.out.println("💾 Exporting workshops to CSV...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("workshops_report.csv"))) {
            writer.write("Name,Date Range,Duration (Days),Status,Teacher Name\n");

            for (Workshop w : workshopList) {
                Teacher teacher = w.getTeacherId() != null ? teacherDAO.getTeacherById(w.getTeacherId()) : null;
                String teacherName = teacher != null ?
                        (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                                (teacher.getLastName() != null ? teacher.getLastName() : "") : "No Teacher";
                String status = w.isActive() ? "Active" : w.isUpcoming() ? "Upcoming" : "Past";
                String line = String.format("\"%s\",\"%s\",\"%d\",\"%s\",\"%s\"\n",
                        w.getName() != null ? w.getName() : "",
                        w.getDateRange() != null ? w.getDateRange() : "",
                        (int) w.getDurationInDays(),
                        status,
                        teacherName.trim());
                writer.write(line);
            }

            System.out.println("✅ Export successful: " + workshopList.size() + " workshops exported");
            showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                    "Workshops exported to workshops_report.csv\n" + workshopList.size() + " workshops exported.");

        } catch (IOException e) {
            System.err.println("❌ Export failed: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    private void updateSummaryAndCharts() {
        System.out.println("📈 Updating summary statistics and charts...");

        int totalWorkshops = workshopList.size();
        long activeWorkshops = workshopList.stream().filter(Workshop::isActive).count();
        long upcomingWorkshops = workshopList.stream().filter(Workshop::isUpcoming).count();
        double averageDuration = workshopList.stream()
                .mapToInt(w -> (int) w.getDurationInDays())
                .average()
                .orElse(0.0);

        // Update summary labels
        totalWorkshopsLabel.setText("Total Workshops: " + totalWorkshops);
        activeWorkshopsLabel.setText("Active Workshops: " + activeWorkshops);
        upcomingWorkshopsLabel.setText("Upcoming Workshops: " + upcomingWorkshops);
        averageDurationLabel.setText(String.format("Average Duration: %.1f days", averageDuration));

        // Update pie chart
        long pastWorkshops = totalWorkshops - activeWorkshops - upcomingWorkshops;
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Active", activeWorkshops),
                new PieChart.Data("Upcoming", upcomingWorkshops),
                new PieChart.Data("Past", pastWorkshops)
        );
        statusPieChart.setData(pieChartData);
        statusPieChart.setLabelLineLength(10);
        statusPieChart.setLabelsVisible(true);

        // Update bar chart
        int duration1to3 = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 1 && w.getDurationInDays() <= 3).count();
        int duration4to7 = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 4 && w.getDurationInDays() <= 7).count();
        int duration8plus = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 8).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Workshops");
        series.getData().add(new XYChart.Data<>("1-3 Days", duration1to3));
        series.getData().add(new XYChart.Data<>("4-7 Days", duration4to7));
        series.getData().add(new XYChart.Data<>("8+ Days", duration8plus));

        durationBarChart.getData().clear();
        durationBarChart.getData().add(series);

        System.out.println("📊 Summary: " + totalWorkshops + " total, " + activeWorkshops + " active, " + upcomingWorkshops + " upcoming");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        System.out.println("🚨 Showing alert: " + title + " - " + content);

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}