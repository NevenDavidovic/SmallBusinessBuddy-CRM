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
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.model.Contact;
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
    @FXML private ComboBox<Contact> teacherComboBox;
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
    private ContactDAO contactDAO;
    private ObservableList<Workshop> workshopList;
    private ObservableList<Contact> teacherList;

    @FXML
    public void initialize() {
        workshopDAO = new WorkshopDAO();
        contactDAO = new ContactDAO();
        workshopList = FXCollections.observableArrayList();
        teacherList = FXCollections.observableArrayList();

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
        teacherNameColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            Contact teacher = w.getTeacherId() != null ? contactDAO.getContactById(w.getTeacherId()) : null;
            String teacherName = teacher != null ?
                    (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                            (teacher.getLastName() != null ? teacher.getLastName() : "") : "";
            return new SimpleStringProperty(teacherName.trim());
        });

        statusComboBox.setItems(FXCollections.observableArrayList("All", "Active", "Upcoming", "Past"));
        statusComboBox.getSelectionModel().selectFirst();

        teacherList.addAll(contactDAO.getAllContacts());
        Contact noTeacher = new Contact();
        noTeacher.setId(-1);
        noTeacher.setFirstName("None");
        teacherList.add(0, noTeacher);
        teacherComboBox.setItems(teacherList);
        teacherComboBox.setCellFactory(param -> new ListCell<Contact>() {
            @Override
            protected void updateItem(Contact item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String name = (item.getFirstName() != null ? item.getFirstName() : "") + " " +
                            (item.getLastName() != null ? item.getLastName() : "");
                    setText(name.trim().isEmpty() ? "None" : name.trim());
                }
            }
        });
        teacherComboBox.setButtonCell(new ListCell<Contact>() {
            @Override
            protected void updateItem(Contact item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String name = (item.getFirstName() != null ? item.getFirstName() : "") + " " +
                            (item.getLastName() != null ? item.getLastName() : "");
                    setText(name.trim().isEmpty() ? "None" : name.trim());
                }
            }
        });
        teacherComboBox.getSelectionModel().selectFirst();

        loadAllWorkshops();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());
    }

    private void loadAllWorkshops() {
        workshopList.setAll(workshopDAO.getAllWorkshops());
        workshopsTable.setItems(workshopList);
        updateSummaryAndCharts();
    }

    private void applyFilters() {
        List<Workshop> filteredWorkshops = workshopDAO.getAllWorkshops();

        String nameFilter = nameFilterField.getText().trim();
        if (!nameFilter.isEmpty()) {
            filteredWorkshops = workshopDAO.searchWorkshops(nameFilter);
        }

        try {
            LocalDate fromDate = null, toDate = null;
            if (!fromDateField.getText().trim().isEmpty()) {
                fromDate = LocalDate.parse(fromDateField.getText().trim());
            }
            if (!toDateField.getText().trim().isEmpty()) {
                toDate = LocalDate.parse(toDateField.getText().trim());
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
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Please enter valid dates in YYYY-MM-DD format.");
        }

        String status = statusComboBox.getValue();
        if (status != null && !status.equals("All")) {
            switch (status) {
                case "Active":
                    filteredWorkshops = workshopDAO.getActiveWorkshops();
                    break;
                case "Upcoming":
                    filteredWorkshops = workshopDAO.getUpcomingWorkshops(30); // Default to 30 days
                    break;
                case "Past":
                    filteredWorkshops = filteredWorkshops.stream()
                            .filter(w -> !w.isActive() && !w.isUpcoming())
                            .collect(Collectors.toList());
                    break;
            }
        }

        Contact selectedTeacher = teacherComboBox.getValue();
        if (selectedTeacher != null && selectedTeacher.getId() != -1) {
            filteredWorkshops = workshopDAO.getWorkshopsByTeacher(selectedTeacher.getId());
        } else if (selectedTeacher != null && selectedTeacher.getId() == -1) {
            filteredWorkshops = filteredWorkshops.stream()
                    .filter(w -> w.getTeacherId() == null)
                    .collect(Collectors.toList());
        }

        workshopList.setAll(filteredWorkshops);
        updateSummaryAndCharts();
    }

    private void clearFilters() {
        nameFilterField.clear();
        fromDateField.clear();
        toDateField.clear();
        statusComboBox.getSelectionModel().selectFirst();
        teacherComboBox.getSelectionModel().selectFirst();
        loadAllWorkshops();
    }

    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("workshops_report.csv"))) {
            writer.write("Name,Date Range,Duration (Days),Status,Teacher Name\n");

            for (Workshop w : workshopList) {
                Contact teacher = w.getTeacherId() != null ? contactDAO.getContactById(w.getTeacherId()) : null;
                String teacherName = teacher != null ?
                        (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                                (teacher.getLastName() != null ? teacher.getLastName() : "") : "";
                String status = w.isActive() ? "Active" : w.isUpcoming() ? "Upcoming" : "Past";
                String line = String.format("\"%s\",\"%s\",\"%d\",\"%s\",\"%s\"\n",
                        w.getName() != null ? w.getName() : "",
                        w.getDateRange() != null ? w.getDateRange() : "",
                        (int) w.getDurationInDays(),
                        status,
                        teacherName.trim());
                writer.write(line);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Workshops exported to workshops_report.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    private void updateSummaryAndCharts() {
        int totalWorkshops = workshopList.size();
        long activeWorkshops = workshopList.stream().filter(Workshop::isActive).count();
        long upcomingWorkshops = workshopList.stream().filter(Workshop::isUpcoming).count();
        double averageDuration = workshopList.stream()
                .mapToInt(w -> (int) w.getDurationInDays())
                .average()
                .orElse(0.0);

        totalWorkshopsLabel.setText("Total Workshops: " + totalWorkshops);
        activeWorkshopsLabel.setText("Active Workshops: " + activeWorkshops);
        upcomingWorkshopsLabel.setText("Upcoming Workshops: " + upcomingWorkshops);
        averageDurationLabel.setText(String.format("Average Duration: %.1f days", averageDuration));

        long pastWorkshops = totalWorkshops - activeWorkshops - upcomingWorkshops;
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Active", activeWorkshops),
                new PieChart.Data("Upcoming", upcomingWorkshops),
                new PieChart.Data("Past", pastWorkshops)
        );
        statusPieChart.setData(pieChartData);
        statusPieChart.setLabelLineLength(10);
        statusPieChart.setLabelsVisible(true);

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
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}