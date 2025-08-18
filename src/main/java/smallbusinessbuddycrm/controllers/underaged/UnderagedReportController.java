package smallbusinessbuddycrm.controllers.underaged;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class UnderagedReportController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label filtersLabel;
    @FXML private Label nameLabel;
    @FXML private Label ageRangeLabel;
    @FXML private Label memberStatusLabel;
    @FXML private Label summaryLabel;
    @FXML private Label visualAnalyticsLabel;
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
    @FXML private CategoryAxis ageGroupAxis;
    @FXML private NumberAxis numberOfUnderagedAxis;
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
        memberColumn.setCellValueFactory(cellData -> {
            LanguageManager lm = LanguageManager.getInstance();
            String memberStatus = cellData.getValue().isMember() ?
                    lm.getText("underaged.member.yes") : lm.getText("underaged.member.no");
            return new SimpleStringProperty(memberStatus);
        });
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

        loadAllUnderaged();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main labels
        if (titleLabel != null) titleLabel.setText(languageManager.getText("underaged.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("underaged.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("underaged.report.filter.name"));
        if (ageRangeLabel != null) ageRangeLabel.setText(languageManager.getText("underaged.report.filter.age.range"));
        if (memberStatusLabel != null) memberStatusLabel.setText(languageManager.getText("underaged.report.filter.member.status"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("underaged.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("underaged.report.visual.analytics"));

        // Update text field prompts
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("underaged.report.filter.name.placeholder"));
        if (minAgeField != null) minAgeField.setPromptText(languageManager.getText("underaged.report.filter.min.age"));
        if (maxAgeField != null) maxAgeField.setPromptText(languageManager.getText("underaged.report.filter.max.age"));

        // Update buttons
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("underaged.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("underaged.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("underaged.export.button"));

        // Update table columns
        if (firstNameColumn != null) firstNameColumn.setText(languageManager.getText("underaged.table.first.name"));
        if (lastNameColumn != null) lastNameColumn.setText(languageManager.getText("underaged.table.last.name"));
        if (ageColumn != null) ageColumn.setText(languageManager.getText("underaged.table.age"));
        if (genderColumn != null) genderColumn.setText(languageManager.getText("underaged.table.gender"));
        if (memberColumn != null) memberColumn.setText(languageManager.getText("underaged.table.member"));
        if (parentNameColumn != null) parentNameColumn.setText(languageManager.getText("underaged.table.parent.name"));
        if (pinColumn != null) pinColumn.setText(languageManager.getText("underaged.table.pin"));
        if (memberSinceColumn != null) memberSinceColumn.setText(languageManager.getText("underaged.table.member.since"));
        if (memberUntilColumn != null) memberUntilColumn.setText(languageManager.getText("underaged.table.member.until"));

        // Update chart titles and axes
        if (memberPieChart != null) memberPieChart.setTitle(languageManager.getText("underaged.report.member.distribution"));
        if (ageBarChart != null) ageBarChart.setTitle(languageManager.getText("underaged.report.age.distribution"));
        if (ageGroupAxis != null) ageGroupAxis.setLabel(languageManager.getText("underaged.report.age.group"));
        if (numberOfUnderagedAxis != null) numberOfUnderagedAxis.setLabel(languageManager.getText("underaged.report.number.underaged"));

        // Update member status combo box
        updateMemberStatusComboBox();

        // Update summary labels with current data
        updateSummaryLabels();

        // Refresh table to update member status column
        underagedTable.refresh();
    }

    private void updateMemberStatusComboBox() {
        LanguageManager languageManager = LanguageManager.getInstance();

        String selectedValue = memberStatusComboBox.getValue();
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                languageManager.getText("underaged.status.all"),
                languageManager.getText("underaged.status.members"),
                languageManager.getText("underaged.status.nonmembers")
        );

        memberStatusComboBox.setItems(statusOptions);

        // Try to maintain selection
        if (selectedValue != null) {
            memberStatusComboBox.getSelectionModel().select(selectedValue);
        } else {
            memberStatusComboBox.getSelectionModel().selectFirst();
        }
    }

    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        int totalUnderaged = underagedList.size();
        long totalMembers = underagedList.stream().filter(UnderagedMember::isMember).count();
        long totalNonMembers = totalUnderaged - totalMembers;
        double averageAge = underagedList.stream()
                .filter(u -> u.getAge() > 0)
                .mapToInt(UnderagedMember::getAge)
                .average()
                .orElse(0.0);

        if (totalUnderagedLabel != null)
            totalUnderagedLabel.setText(languageManager.getText("underaged.report.total.underaged") + totalUnderaged);
        if (totalMembersLabel != null)
            totalMembersLabel.setText(languageManager.getText("underaged.report.total.members") + totalMembers);
        if (totalNonMembersLabel != null)
            totalNonMembersLabel.setText(languageManager.getText("underaged.report.total.nonmembers") + totalNonMembers);
        if (averageAgeLabel != null)
            averageAgeLabel.setText(languageManager.getText("underaged.report.average.age") + String.format("%.1f", averageAge));
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
        LanguageManager lm = LanguageManager.getInstance();
        if (memberStatus != null && !memberStatus.equals(lm.getText("underaged.status.all"))) {
            boolean isMember = memberStatus.equals(lm.getText("underaged.status.members"));
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
            LanguageManager lm = LanguageManager.getInstance();
            writer.write(lm.getText("underaged.table.first.name") + "," +
                    lm.getText("underaged.table.last.name") + "," +
                    lm.getText("underaged.table.age") + "," +
                    lm.getText("underaged.table.gender") + "," +
                    lm.getText("underaged.table.member") + "," +
                    lm.getText("underaged.table.parent.name") + "," +
                    lm.getText("underaged.table.pin") + "," +
                    lm.getText("underaged.table.member.since") + "," +
                    lm.getText("underaged.table.member.until") + "\n");

            for (UnderagedMember u : underagedList) {
                Contact parent = contactDAO.getContactById(u.getContactId());
                String parentName = parent != null ?
                        (parent.getFirstName() != null ? parent.getFirstName() : "") + " " +
                                (parent.getLastName() != null ? parent.getLastName() : "") : "";
                String memberStatus = u.isMember() ? lm.getText("underaged.member.yes") : lm.getText("underaged.member.no");
                String line = String.format("\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        u.getFirstName() != null ? u.getFirstName() : "",
                        u.getLastName() != null ? u.getLastName() : "",
                        u.getAge(),
                        u.getGender() != null ? u.getGender() : "",
                        memberStatus,
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
        updateSummaryLabels();

        int totalUnderaged = underagedList.size();
        long totalMembers = underagedList.stream().filter(UnderagedMember::isMember).count();
        long totalNonMembers = totalUnderaged - totalMembers;

        LanguageManager lm = LanguageManager.getInstance();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(lm.getText("underaged.status.members"), totalMembers),
                new PieChart.Data(lm.getText("underaged.status.nonmembers"), totalNonMembers)
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