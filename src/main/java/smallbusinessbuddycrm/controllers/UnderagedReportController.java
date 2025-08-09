package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
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
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class UnderagedReportController {

    @FXML private VBox root;
    @FXML private TextField nameFilterField;
    @FXML private TextField minAgeField;
    @FXML private TextField maxAgeField;
    @FXML private ComboBox<String> memberStatusComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<UnderagedMember> underagedTable;
    @FXML private TableColumn<UnderagedMember, String> firstNameColumn;
    @FXML private TableColumn<UnderagedMember, String> lastNameColumn;
    @FXML private TableColumn<UnderagedMember, Integer> ageColumn;
    @FXML private TableColumn<UnderagedMember, String> genderColumn;
    @FXML private TableColumn<UnderagedMember, String> memberColumn;
    @FXML private TableColumn<UnderagedMember, String> parentNameColumn;
    @FXML private TableColumn<UnderagedMember, String> pinColumn;
    @FXML private TableColumn<UnderagedMember, LocalDate> memberSinceColumn;
    @FXML private TableColumn<UnderagedMember, LocalDate> memberUntilColumn;
    @FXML private Label totalUnderagedLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalNonMembersLabel;
    @FXML private Label averageAgeLabel;
    @FXML private PieChart memberPieChart;
    @FXML private BarChart<String, Number> ageBarChart;
    @FXML private Button exportButton;

    private UnderagedDAO underagedDAO;
    private ContactDAO contactDAO;
    private ObservableList<UnderagedMember> underagedList;

    @FXML
    public void initialize() {
        underagedDAO = new UnderagedDAO();
        contactDAO = new ContactDAO();
        underagedList = FXCollections.observableArrayList();

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        memberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ? "Yes" : "No"));
        parentNameColumn.setCellValueFactory(cellData -> {
            UnderagedMember u = cellData.getValue();
            Contact parent = contactDAO.getContactById(u.getContactId());
            String parentName = parent != null ?
                    (parent.getFirstName() != null ? parent.getFirstName() : "") + " " +
                            (parent.getLastName() != null ? parent.getLastName() : "") : "";
            return new SimpleStringProperty(parentName.trim());
        });
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));
        memberSinceColumn.setCellValueFactory(new PropertyValueFactory<>("memberSince"));
        memberUntilColumn.setCellValueFactory(new PropertyValueFactory<>("memberUntil"));

        memberStatusComboBox.setItems(FXCollections.observableArrayList("All", "Members", "Non-Members"));
        memberStatusComboBox.getSelectionModel().selectFirst();

        loadAllUnderaged();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());
    }

    private void loadAllUnderaged() {
        underagedList.setAll(underagedDAO.getAllUnderagedMembers());
        underagedTable.setItems(underagedList);
        updateSummaryAndCharts();
    }

    private void applyFilters() {
        List<UnderagedMember> filteredUnderaged = underagedDAO.getAllUnderagedMembers();

        String nameFilter = nameFilterField.getText().trim().toLowerCase();
        if (!nameFilter.isEmpty()) {
            filteredUnderaged = filteredUnderaged.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(nameFilter)) ||
                            (u.getLastName() != null && u.getLastName().toLowerCase().contains(nameFilter)))
                    .collect(Collectors.toList());
        }

        try {
            if (!minAgeField.getText().trim().isEmpty() && !maxAgeField.getText().trim().isEmpty()) {
                int minAge = Integer.parseInt(minAgeField.getText().trim());
                int maxAge = Integer.parseInt(maxAgeField.getText().trim());
                filteredUnderaged = filteredUnderaged.stream()
                        .filter(u -> u.getAge() >= minAge && u.getAge() <= maxAge)
                        .collect(Collectors.toList());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Age", "Please enter valid numbers for age range.");
        }

        String memberStatus = memberStatusComboBox.getValue();
        if (memberStatus != null && !memberStatus.equals("All")) {
            boolean isMember = memberStatus.equals("Members");
            filteredUnderaged = filteredUnderaged.stream()
                    .filter(u -> u.isMember() == isMember)
                    .collect(Collectors.toList());
        }

        underagedList.setAll(filteredUnderaged);
        updateSummaryAndCharts();
    }

    private void clearFilters() {
        nameFilterField.clear();
        minAgeField.clear();
        maxAgeField.clear();
        memberStatusComboBox.getSelectionModel().selectFirst();
        loadAllUnderaged();
    }

    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("underaged_report.csv"))) {
            writer.write("First Name,Last Name,Age,Gender,Member,Parent Name,PIN,Member Since,Member Until\n");

            for (UnderagedMember u : underagedList) {
                Contact parent = contactDAO.getContactById(u.getContactId());
                String parentName = parent != null ?
                        (parent.getFirstName() != null ? parent.getFirstName() : "") + " " +
                                (parent.getLastName() != null ? parent.getLastName() : "") : "";
                String line = String.format("\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        u.getFirstName() != null ? u.getFirstName() : "",
                        u.getLastName() != null ? u.getLastName() : "",
                        u.getAge(),
                        u.getGender() != null ? u.getGender() : "",
                        u.isMember() ? "Yes" : "No",
                        parentName.trim(),
                        u.getPin() != null ? u.getPin() : "",
                        u.getMemberSince() != null ? u.getMemberSince().toString() : "",
                        u.getMemberUntil() != null ? u.getMemberUntil().toString() : "");
                writer.write(line);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Underaged members exported to underaged_report.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    private void updateSummaryAndCharts() {
        int totalUnderaged = underagedList.size();
        long totalMembers = underagedList.stream().filter(UnderagedMember::isMember).count();
        long totalNonMembers = totalUnderaged - totalMembers;
        double averageAge = underagedList.stream()
                .filter(u -> u.getAge() > 0)
                .mapToInt(UnderagedMember::getAge)
                .average()
                .orElse(0.0);

        totalUnderagedLabel.setText("Total Underaged: " + totalUnderaged);
        totalMembersLabel.setText("Total Members: " + totalMembers);
        totalNonMembersLabel.setText("Total Non-Members: " + totalNonMembers);
        averageAgeLabel.setText(String.format("Average Age: %.1f", averageAge));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Members", totalMembers),
                new PieChart.Data("Non-Members", totalNonMembers)
        );
        memberPieChart.setData(pieChartData);
        memberPieChart.setLabelLineLength(10);
        memberPieChart.setLabelsVisible(true);

        int ageGroup0to5 = (int) underagedList.stream().filter(u -> u.getAge() <= 5).count();
        int ageGroup6to12 = (int) underagedList.stream().filter(u -> u.getAge() >= 6 && u.getAge() <= 12).count();
        int ageGroup13to17 = (int) underagedList.stream().filter(u -> u.getAge() >= 13 && u.getAge() <= 17).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Underaged Members");
        series.getData().add(new XYChart.Data<>("0-5", ageGroup0to5));
        series.getData().add(new XYChart.Data<>("6-12", ageGroup6to12));
        series.getData().add(new XYChart.Data<>("13-17", ageGroup13to17));

        ageBarChart.getData().clear();
        ageBarChart.getData().add(series);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}