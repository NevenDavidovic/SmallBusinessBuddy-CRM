package smallbusinessbuddycrm.controllers.list;

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
import smallbusinessbuddycrm.database.ListsDAO;
import smallbusinessbuddycrm.model.List;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Controller for the Lists Report interface providing comprehensive analytics and reporting
 * for contact lists. Features filtering capabilities, visual analytics with charts,
 * summary statistics, and CSV export functionality. Supports full localization
 * with dynamic language switching and real-time data updates.
 */
public class ListsReportController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label filtersLabel;
    @FXML private Label nameLabel;
    @FXML private Label typeLabel;
    @FXML private Label objectTypeLabel;
    @FXML private Label folderLabel;
    @FXML private Label summaryLabel;
    @FXML private Label visualAnalyticsLabel;
    @FXML private TextField nameFilterField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> objectTypeComboBox;
    @FXML private ComboBox<String> folderComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<List> listsTable;
    @FXML private TableColumn<List, String> nameColumn;
    @FXML private TableColumn<List, String> descriptionColumn;
    @FXML private TableColumn<List, String> typeColumn;
    @FXML private TableColumn<List, String> objectTypeColumn;
    @FXML private TableColumn<List, String> creatorColumn;
    @FXML private TableColumn<List, String> folderColumn;
    @FXML private TableColumn<List, Integer> listSizeColumn;
    @FXML private TableColumn<List, String> createdAtColumn;
    @FXML private TableColumn<List, String> updatedAtColumn;
    @FXML private Label totalListsLabel;
    @FXML private Label averageListSizeLabel;
    @FXML private Label uniqueTypesLabel;
    @FXML private Label uniqueFoldersLabel;
    @FXML private PieChart typePieChart;
    @FXML private BarChart<String, Number> sizeBarChart;
    @FXML private CategoryAxis sizeGroupAxis;
    @FXML private NumberAxis numberOfListsAxis;
    @FXML private Button exportButton;

    private ListsDAO listsDAO;
    private ObservableList<List> listData;
    private ObservableList<String> typeOptions;
    private ObservableList<String> objectTypeOptions;
    private ObservableList<String> folderOptions;

    /**
     * Initializes the Lists Report Controller after FXML loading.
     * Sets up DAO connections, observable lists, table column bindings,
     * loads initial data, configures event handlers, and sets up language
     * management with initial text updates.
     */
    @FXML
    public void initialize() {
        listsDAO = new ListsDAO();
        listData = FXCollections.observableArrayList();
        typeOptions = FXCollections.observableArrayList();
        objectTypeOptions = FXCollections.observableArrayList();
        folderOptions = FXCollections.observableArrayList();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        objectTypeColumn.setCellValueFactory(new PropertyValueFactory<>("objectType"));
        creatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));
        folderColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFolder() != null ? cellData.getValue().getFolder() : ""));
        listSizeColumn.setCellValueFactory(new PropertyValueFactory<>("listSize"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        loadAllLists();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

        // Add language change listener and initial text update
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Updates all UI text elements based on current language settings.
     * Refreshes labels, buttons, table headers, chart titles, field placeholders,
     * and combo box options when language changes. Also updates summary labels
     * and combo box localization to maintain consistency.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main labels
        if (titleLabel != null) titleLabel.setText(languageManager.getText("lists.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("lists.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("lists.report.filter.name"));
        if (typeLabel != null) typeLabel.setText(languageManager.getText("lists.report.filter.type"));
        if (objectTypeLabel != null) objectTypeLabel.setText(languageManager.getText("lists.report.filter.object.type"));
        if (folderLabel != null) folderLabel.setText(languageManager.getText("lists.report.filter.folder"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("lists.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("lists.report.visual.analytics"));

        // Update text field prompts
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("lists.report.filter.name.placeholder"));

        // Update buttons
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("lists.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("lists.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("lists.report.export.button"));

        // Update table columns
        if (nameColumn != null) nameColumn.setText(languageManager.getText("lists.report.table.name"));
        if (descriptionColumn != null) descriptionColumn.setText(languageManager.getText("lists.report.table.description"));
        if (typeColumn != null) typeColumn.setText(languageManager.getText("lists.report.table.type"));
        if (objectTypeColumn != null) objectTypeColumn.setText(languageManager.getText("lists.report.table.object.type"));
        if (creatorColumn != null) creatorColumn.setText(languageManager.getText("lists.report.table.creator"));
        if (folderColumn != null) folderColumn.setText(languageManager.getText("lists.report.table.folder"));
        if (listSizeColumn != null) listSizeColumn.setText(languageManager.getText("lists.report.table.size"));
        if (createdAtColumn != null) createdAtColumn.setText(languageManager.getText("lists.report.table.created.at"));
        if (updatedAtColumn != null) updatedAtColumn.setText(languageManager.getText("lists.report.table.updated.at"));

        // Update chart titles and axes
        if (typePieChart != null) typePieChart.setTitle(languageManager.getText("lists.report.type.distribution"));
        if (sizeBarChart != null) sizeBarChart.setTitle(languageManager.getText("lists.report.size.distribution"));
        if (sizeGroupAxis != null) sizeGroupAxis.setLabel(languageManager.getText("lists.report.size.group"));
        if (numberOfListsAxis != null) numberOfListsAxis.setLabel(languageManager.getText("lists.report.number.lists"));

        // Update combo boxes
        updateComboBoxes();

        // Update summary labels with current data
        updateSummaryLabels();
    }

    /**
     * Updates combo box options with localized "All" text and maintains selections.
     * Refreshes the first option in each filter combo box to use current language
     * "All" text while preserving user selections. Resets selection to first item
     * if current selection is no longer valid after language change.
     */
    private void updateComboBoxes() {
        LanguageManager languageManager = LanguageManager.getInstance();
        String allText = languageManager.getText("lists.report.filter.all");

        // Update type combo box
        String selectedType = typeComboBox.getValue();
        if (typeOptions.size() > 0 && !typeOptions.get(0).equals(allText)) {
            typeOptions.set(0, allText);
        }
        if (selectedType != null && !typeOptions.contains(selectedType)) {
            typeComboBox.getSelectionModel().selectFirst();
        }

        // Update object type combo box
        String selectedObjectType = objectTypeComboBox.getValue();
        if (objectTypeOptions.size() > 0 && !objectTypeOptions.get(0).equals(allText)) {
            objectTypeOptions.set(0, allText);
        }
        if (selectedObjectType != null && !objectTypeOptions.contains(selectedObjectType)) {
            objectTypeComboBox.getSelectionModel().selectFirst();
        }

        // Update folder combo box
        String selectedFolder = folderComboBox.getValue();
        if (folderOptions.size() > 0 && !folderOptions.get(0).equals(allText)) {
            folderOptions.set(0, allText);
        }
        if (selectedFolder != null && !folderOptions.contains(selectedFolder)) {
            folderComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Updates summary statistics labels with current filtered data.
     * Calculates and displays total lists count, average list size,
     * number of unique types, and number of unique folders based on
     * currently filtered data with proper localization.
     */
    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        int totalLists = listData.size();
        double averageListSize = listData.stream()
                .mapToInt(List::getListSize)
                .average()
                .orElse(0.0);
        long uniqueTypes = listData.stream()
                .map(List::getType)
                .filter(type -> type != null)
                .distinct()
                .count();
        long uniqueFolders = listData.stream()
                .map(List::getFolder)
                .filter(folder -> folder != null && !folder.isEmpty())
                .distinct()
                .count();

        if (totalListsLabel != null)
            totalListsLabel.setText(languageManager.getText("lists.report.total.lists") + totalLists);
        if (averageListSizeLabel != null)
            averageListSizeLabel.setText(languageManager.getText("lists.report.average.list.size") + String.format("%.1f", averageListSize));
        if (uniqueTypesLabel != null)
            uniqueTypesLabel.setText(languageManager.getText("lists.report.unique.types") + uniqueTypes);
        if (uniqueFoldersLabel != null)
            uniqueFoldersLabel.setText(languageManager.getText("lists.report.unique.folders") + uniqueFolders);
    }

    /**
     * Loads all active lists from database and populates filter options.
     * Retrieves all lists via DAO, populates table and filter combo boxes with
     * unique values, adds localized "All" option to each filter, and updates
     * summary statistics and charts with complete dataset.
     */
    private void loadAllLists() {
        listData.setAll(listsDAO.getAllActiveLists());
        listsTable.setItems(listData);

        LanguageManager languageManager = LanguageManager.getInstance();
        String allText = languageManager.getText("lists.report.filter.all");

        typeOptions.setAll(listData.stream()
                .map(List::getType)
                .filter(type -> type != null)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        typeOptions.add(0, allText);
        typeComboBox.setItems(typeOptions);
        typeComboBox.getSelectionModel().selectFirst();

        objectTypeOptions.setAll(listData.stream()
                .map(List::getObjectType)
                .filter(objectType -> objectType != null)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        objectTypeOptions.add(0, allText);
        objectTypeComboBox.setItems(objectTypeOptions);
        objectTypeComboBox.getSelectionModel().selectFirst();

        folderOptions.setAll(listData.stream()
                .map(List::getFolder)
                .filter(folder -> folder != null && !folder.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        folderOptions.add(0, allText);
        folderComboBox.setItems(folderOptions);
        folderComboBox.getSelectionModel().selectFirst();

        updateSummaryAndCharts();
    }

    /**
     * Applies current filter settings to the lists data.
     * Processes name text filter, type filter, object type filter, and folder filter
     * in sequence. Updates table data with filtered results and refreshes
     * summary statistics and charts to reflect filtered dataset.
     */
    private void applyFilters() {
        java.util.List<List> filteredLists = listsDAO.getAllActiveLists();

        String nameFilter = nameFilterField.getText().trim();
        if (!nameFilter.isEmpty()) {
            filteredLists = listsDAO.searchListsByName(nameFilter);
        }

        LanguageManager languageManager = LanguageManager.getInstance();
        String allText = languageManager.getText("lists.report.filter.all");

        String typeFilter = typeComboBox.getValue();
        if (typeFilter != null && !typeFilter.equals(allText)) {
            filteredLists = filteredLists.stream()
                    .filter(list -> typeFilter.equals(list.getType()))
                    .collect(Collectors.toList());
        }

        String objectTypeFilter = objectTypeComboBox.getValue();
        if (objectTypeFilter != null && !objectTypeFilter.equals(allText)) {
            filteredLists = filteredLists.stream()
                    .filter(list -> objectTypeFilter.equals(list.getObjectType()))
                    .collect(Collectors.toList());
        }

        String folderFilter = folderComboBox.getValue();
        if (folderFilter != null && !folderFilter.equals(allText)) {
            filteredLists = filteredLists.stream()
                    .filter(list -> folderFilter.equals(list.getFolder()))
                    .collect(Collectors.toList());
        }

        listData.setAll(filteredLists);
        updateSummaryAndCharts();
    }

    /**
     * Clears all filter settings and reloads complete dataset.
     * Resets name filter text field, resets all combo box selections to "All",
     * and reloads all lists from database to restore original unfiltered view.
     */
    private void clearFilters() {
        nameFilterField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        objectTypeComboBox.getSelectionModel().selectFirst();
        folderComboBox.getSelectionModel().selectFirst();
        loadAllLists();
    }

    /**
     * Exports current filtered data to CSV file with proper formatting.
     * Creates CSV file with localized headers, handles quote escaping for text fields,
     * formats all list data according to CSV standards, and shows success/error
     * alert based on export operation result.
     */
    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("lists_report.csv"))) {
            LanguageManager lm = LanguageManager.getInstance();
            writer.write(lm.getText("lists.report.table.name") + "," +
                    lm.getText("lists.report.table.description") + "," +
                    lm.getText("lists.report.table.type") + "," +
                    lm.getText("lists.report.table.object.type") + "," +
                    lm.getText("lists.report.table.creator") + "," +
                    lm.getText("lists.report.table.folder") + "," +
                    lm.getText("lists.report.table.size") + "," +
                    lm.getText("lists.report.table.created.at") + "," +
                    lm.getText("lists.report.table.updated.at") + "\n");

            for (List list : listData) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"\n",
                        list.getName() != null ? list.getName().replace("\"", "\"\"") : "",
                        list.getDescription() != null ? list.getDescription().replace("\"", "\"\"") : "",
                        list.getType() != null ? list.getType() : "",
                        list.getObjectType() != null ? list.getObjectType() : "",
                        list.getCreator() != null ? list.getCreator() : "",
                        list.getFolder() != null ? list.getFolder() : "",
                        list.getListSize(),
                        list.getCreatedAt() != null ? list.getCreatedAt() : "",
                        list.getUpdatedAt() != null ? list.getUpdatedAt() : "");
                writer.write(line);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Lists exported to lists_report.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    /**
     * Updates summary statistics and visual analytics charts with current data.
     * Refreshes summary labels, regenerates pie chart for type distribution
     * with proper data grouping, and updates bar chart for size distribution
     * with predefined size ranges (0-10, 11-50, 51+ contacts).
     */
    private void updateSummaryAndCharts() {
        updateSummaryLabels();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        java.util.List<String> types = listData.stream()
                .map(List::getType)
                .filter(type -> type != null)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        for (String type : types) {
            long count = listData.stream().filter(list -> type.equals(list.getType())).count();
            pieChartData.add(new PieChart.Data(type, count));
        }
        typePieChart.setData(pieChartData);
        typePieChart.setLabelLineLength(10);
        typePieChart.setLabelsVisible(true);

        int size0to10 = (int) listData.stream().filter(list -> list.getListSize() >= 0 && list.getListSize() <= 10).count();
        int size11to50 = (int) listData.stream().filter(list -> list.getListSize() >= 11 && list.getListSize() <= 50).count();
        int size51plus = (int) listData.stream().filter(list -> list.getListSize() >= 51).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lists");
        series.getData().add(new XYChart.Data<>("0-10 Contacts", size0to10));
        series.getData().add(new XYChart.Data<>("11-50 Contacts", size11to50));
        series.getData().add(new XYChart.Data<>("51+ Contacts", size51plus));

        sizeBarChart.getData().clear();
        sizeBarChart.getData().add(series);
    }

    /**
     * Displays alert dialog with specified type, title, and content.
     * Creates and shows modal alert dialog with no header text for clean appearance.
     * Used for showing export results and other user notifications.
     *
     * @param type The type of alert (INFORMATION, ERROR, WARNING, etc.)
     * @param title The title text for the alert dialog
     * @param content The main content message to display
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}