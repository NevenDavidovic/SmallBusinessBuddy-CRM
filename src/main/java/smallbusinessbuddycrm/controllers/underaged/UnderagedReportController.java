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

/**
 * Controller class for generating comprehensive reports and analytics for underaged members.
 *
 * This controller provides advanced reporting capabilities including:
 * - Interactive filtering by name, age range, and member status
 * - Real-time summary statistics (totals, averages, distributions)
 * - Visual analytics with pie charts and bar charts
 * - CSV export functionality with localized headers
 * - Internationalization support with dynamic text updates
 * - Parent/guardian contact resolution and display
 *
 * Key Features:
 * - Dynamic filtering: Filter by name (first/last), age range (min-max), member status
 * - Summary Analytics: Total counts, member ratios, average age calculations
 * - Visual Charts: Member status pie chart, age group distribution bar chart
 * - Data Export: CSV export with complete member information and localized headers
 * - Internationalization: Full support for language switching with real-time UI updates
 * - Contact Integration: Resolves and displays parent/guardian names from contact database
 *
 * Chart Analytics:
 * - Pie Chart: Shows distribution of members vs non-members with percentages
 * - Bar Chart: Displays age group distribution (0-5, 6-12, 13-17 years)
 * - Real-time updates: Charts automatically refresh when filters are applied
 *
 * The controller integrates with UnderagedDAO for member data and ContactDAO for
 * parent/guardian contact information, providing a complete reporting solution.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class UnderagedReportController {

    // Main Layout Container
    @FXML private VBox root;

    // Section Labels
    @FXML private Label titleLabel;
    @FXML private Label filtersLabel;
    @FXML private Label nameLabel;
    @FXML private Label ageRangeLabel;
    @FXML private Label memberStatusLabel;
    @FXML private Label summaryLabel;
    @FXML private Label visualAnalyticsLabel;

    // Filter Controls
    @FXML private TextField nameFilterField;
    @FXML private TextField minAgeField;
    @FXML private TextField maxAgeField;
    @FXML private ComboBox<String> memberStatusComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    // Data Table
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

    // Summary Statistics Labels
    @FXML private Label totalUnderagedLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalNonMembersLabel;
    @FXML private Label averageAgeLabel;

    // Charts
    @FXML private PieChart memberPieChart;
    @FXML private BarChart<String, Number> ageBarChart;
    @FXML private CategoryAxis ageGroupAxis;
    @FXML private NumberAxis numberOfUnderagedAxis;

    // Export Control
    @FXML private Button exportButton;

    // Database Access Objects
    private UnderagedDAO underagedDAO;
    private ContactDAO contactDAO;

    // Data Collection
    private ObservableList<UnderagedMember> underagedList;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up database access objects, observable lists, table column cell value factories,
     * loads initial data, configures event handlers, and sets up language change listeners.
     */
    @FXML
    public void initialize() {
        underagedDAO = new UnderagedDAO();
        contactDAO = new ContactDAO();
        underagedList = FXCollections.observableArrayList();

        // Setup table columns with appropriate cell value factories
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        // Member status column with localized yes/no values
        memberColumn.setCellValueFactory(cellData -> {
            LanguageManager lm = LanguageManager.getInstance();
            String memberStatus = cellData.getValue().isMember() ?
                    lm.getText("underaged.member.yes") : lm.getText("underaged.member.no");
            return new SimpleStringProperty(memberStatus);
        });

        // Parent name column with contact resolution
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

        // Load initial data
        loadAllUnderaged();

        // Setup event handlers
        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

        // Setup internationalization
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh labels, buttons, placeholders, table headers,
     * chart titles, and combo box options. Also refreshes summary data and table content.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main section labels
        if (titleLabel != null) titleLabel.setText(languageManager.getText("underaged.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("underaged.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("underaged.report.filter.name"));
        if (ageRangeLabel != null) ageRangeLabel.setText(languageManager.getText("underaged.report.filter.age.range"));
        if (memberStatusLabel != null) memberStatusLabel.setText(languageManager.getText("underaged.report.filter.member.status"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("underaged.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("underaged.report.visual.analytics"));

        // Update form field placeholders
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("underaged.report.filter.name.placeholder"));
        if (minAgeField != null) minAgeField.setPromptText(languageManager.getText("underaged.report.filter.min.age"));
        if (maxAgeField != null) maxAgeField.setPromptText(languageManager.getText("underaged.report.filter.max.age"));

        // Update action buttons
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("underaged.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("underaged.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("underaged.export.button"));

        // Update table column headers
        if (firstNameColumn != null) firstNameColumn.setText(languageManager.getText("underaged.table.first.name"));
        if (lastNameColumn != null) lastNameColumn.setText(languageManager.getText("underaged.table.last.name"));
        if (ageColumn != null) ageColumn.setText(languageManager.getText("underaged.table.age"));
        if (genderColumn != null) genderColumn.setText(languageManager.getText("underaged.table.gender"));
        if (memberColumn != null) memberColumn.setText(languageManager.getText("underaged.table.member"));
        if (parentNameColumn != null) parentNameColumn.setText(languageManager.getText("underaged.table.parent.name"));
        if (pinColumn != null) pinColumn.setText(languageManager.getText("underaged.table.pin"));
        if (memberSinceColumn != null) memberSinceColumn.setText(languageManager.getText("underaged.table.member.since"));
        if (memberUntilColumn != null) memberUntilColumn.setText(languageManager.getText("underaged.table.member.until"));

        // Update chart titles and axis labels
        if (memberPieChart != null) memberPieChart.setTitle(languageManager.getText("underaged.report.member.distribution"));
        if (ageBarChart != null) ageBarChart.setTitle(languageManager.getText("underaged.report.age.distribution"));
        if (ageGroupAxis != null) ageGroupAxis.setLabel(languageManager.getText("underaged.report.age.group"));
        if (numberOfUnderagedAxis != null) numberOfUnderagedAxis.setLabel(languageManager.getText("underaged.report.number.underaged"));

        // Update dynamic content
        updateMemberStatusComboBox();
        updateSummaryLabels();

        // Refresh table to update localized cell values
        underagedTable.refresh();
    }

    /**
     * Updates the member status combo box with localized options.
     * Refreshes combo box items with translated status options (All, Members, Non-members)
     * while attempting to maintain the current selection.
     */
    private void updateMemberStatusComboBox() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Store current selection to maintain after update
        String selectedValue = memberStatusComboBox.getValue();

        // Create localized status options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                languageManager.getText("underaged.status.all"),
                languageManager.getText("underaged.status.members"),
                languageManager.getText("underaged.status.nonmembers")
        );

        memberStatusComboBox.setItems(statusOptions);

        // Attempt to maintain previous selection, fallback to first option
        if (selectedValue != null) {
            memberStatusComboBox.getSelectionModel().select(selectedValue);
        } else {
            memberStatusComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Updates summary statistic labels with current filtered data.
     * Calculates and displays total underaged count, member/non-member counts,
     * and average age using localized text formatting.
     */
    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Calculate summary statistics from current filtered data
        int totalUnderaged = underagedList.size();
        long totalMembers = underagedList.stream().filter(UnderagedMember::isMember).count();
        long totalNonMembers = totalUnderaged - totalMembers;
        double averageAge = underagedList.stream()
                .filter(u -> u.getAge() > 0)
                .mapToInt(UnderagedMember::getAge)
                .average()
                .orElse(0.0);

        // Update summary labels with localized text and calculated values
        if (totalUnderagedLabel != null)
            totalUnderagedLabel.setText(languageManager.getText("underaged.report.total.underaged") + totalUnderaged);
        if (totalMembersLabel != null)
            totalMembersLabel.setText(languageManager.getText("underaged.report.total.members") + totalMembers);
        if (totalNonMembersLabel != null)
            totalNonMembersLabel.setText(languageManager.getText("underaged.report.total.nonmembers") + totalNonMembers);
        if (averageAgeLabel != null)
            averageAgeLabel.setText(languageManager.getText("underaged.report.average.age") + String.format("%.1f", averageAge));
    }

    /**
     * Loads all underaged members from database and refreshes the display.
     * Fetches complete list from DAO, updates table view, and refreshes
     * summary statistics and charts.
     */
    private void loadAllUnderaged() {
        underagedList.setAll(underagedDAO.getAllUnderagedMembers());
        underagedTable.setItems(underagedList);
        updateSummaryAndCharts();
    }

    /**
     * Applies user-specified filters to the underaged members list.
     * Filters by name (first or last name contains text), age range (min-max),
     * and member status. Updates table view and analytics after filtering.
     * Shows warning alert for invalid age range input.
     */
    private void applyFilters() {
        List<UnderagedMember> filteredUnderaged = underagedDAO.getAllUnderagedMembers();

        // Apply name filter (case-insensitive, matches first or last name)
        String nameFilter = nameFilterField.getText().trim().toLowerCase();
        if (!nameFilter.isEmpty()) {
            filteredUnderaged = filteredUnderaged.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(nameFilter)) ||
                            (u.getLastName() != null && u.getLastName().toLowerCase().contains(nameFilter)))
                    .collect(Collectors.toList());
        }

        // Apply age range filter with error handling
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
            return;
        }

        // Apply member status filter using localized values
        String memberStatus = memberStatusComboBox.getValue();
        LanguageManager lm = LanguageManager.getInstance();
        if (memberStatus != null && !memberStatus.equals(lm.getText("underaged.status.all"))) {
            boolean isMember = memberStatus.equals(lm.getText("underaged.status.members"));
            filteredUnderaged = filteredUnderaged.stream()
                    .filter(u -> u.isMember() == isMember)
                    .collect(Collectors.toList());
        }

        // Update display with filtered results
        underagedList.setAll(filteredUnderaged);
        updateSummaryAndCharts();
    }

    /**
     * Clears all filter inputs and reloads complete dataset.
     * Resets name filter field, age range fields, member status combo box,
     * and reloads all underaged members from database.
     */
    private void clearFilters() {
        nameFilterField.clear();
        minAgeField.clear();
        maxAgeField.clear();
        memberStatusComboBox.getSelectionModel().selectFirst();
        loadAllUnderaged();
    }

    /**
     * Exports current filtered underaged members data to CSV file.
     * Creates CSV file with localized headers and all member information
     * including resolved parent/guardian names. Shows success or error alerts.
     * File is saved as "underaged_report.csv" in current directory.
     */
    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("underaged_report.csv"))) {
            LanguageManager lm = LanguageManager.getInstance();

            // Write CSV header with localized column names
            writer.write(lm.getText("underaged.table.first.name") + "," +
                    lm.getText("underaged.table.last.name") + "," +
                    lm.getText("underaged.table.age") + "," +
                    lm.getText("underaged.table.gender") + "," +
                    lm.getText("underaged.table.member") + "," +
                    lm.getText("underaged.table.parent.name") + "," +
                    lm.getText("underaged.table.pin") + "," +
                    lm.getText("underaged.table.member.since") + "," +
                    lm.getText("underaged.table.member.until") + "\n");

            // Write data rows with proper CSV escaping
            for (UnderagedMember u : underagedList) {
                // Resolve parent contact information
                Contact parent = contactDAO.getContactById(u.getContactId());
                String parentName = parent != null ?
                        (parent.getFirstName() != null ? parent.getFirstName() : "") + " " +
                                (parent.getLastName() != null ? parent.getLastName() : "") : "";

                // Format member status with localized text
                String memberStatus = u.isMember() ? lm.getText("underaged.member.yes") : lm.getText("underaged.member.no");

                // Create CSV line with proper quoting to handle commas in data
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

    /**
     * Updates summary statistics and visual charts with current dataset.
     * Refreshes summary labels, member status pie chart with localized labels,
     * and age distribution bar chart with three age groups (0-5, 6-12, 13-17).
     */
    private void updateSummaryAndCharts() {
        updateSummaryLabels();

        // Calculate statistics for chart data
        int totalUnderaged = underagedList.size();
        long totalMembers = underagedList.stream().filter(UnderagedMember::isMember).count();
        long totalNonMembers = totalUnderaged - totalMembers;

        // Update pie chart with member status distribution
        LanguageManager lm = LanguageManager.getInstance();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(lm.getText("underaged.status.members"), totalMembers),
                new PieChart.Data(lm.getText("underaged.status.nonmembers"), totalNonMembers)
        );
        memberPieChart.setData(pieChartData);
        memberPieChart.setLabelLineLength(10);
        memberPieChart.setLabelsVisible(true);

        // Calculate age group distributions
        int ageGroup0to5 = (int) underagedList.stream().filter(u -> u.getAge() <= 5).count();
        int ageGroup6to12 = (int) underagedList.stream().filter(u -> u.getAge() >= 6 && u.getAge() <= 12).count();
        int ageGroup13to17 = (int) underagedList.stream().filter(u -> u.getAge() >= 13 && u.getAge() <= 17).count();

        // Update bar chart with age group distribution
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Underaged Members");
        series.getData().add(new XYChart.Data<>("0-5", ageGroup0to5));
        series.getData().add(new XYChart.Data<>("6-12", ageGroup6to12));
        series.getData().add(new XYChart.Data<>("13-17", ageGroup13to17));

        ageBarChart.getData().clear();
        ageBarChart.getData().add(series);
    }

    /**
     * Displays an alert dialog with specified type, title, and content.
     * Utility method for showing information, warning, or error messages
     * to the user in a standardized format.
     *
     * @param type The type of alert (INFORMATION, WARNING, ERROR)
     * @param title The alert dialog title
     * @param content The alert message content
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}