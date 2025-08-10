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
import smallbusinessbuddycrm.database.ListsDAO;
import smallbusinessbuddycrm.model.List;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

public class ListsReportController {

    @FXML private VBox root;
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
    @FXML private Button exportButton;

    private ListsDAO listsDAO;
    private ObservableList<List> listData;
    private ObservableList<String> typeOptions;
    private ObservableList<String> objectTypeOptions;
    private ObservableList<String> folderOptions;

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

        typeComboBox.setItems(typeOptions);
        typeComboBox.getItems().add(0, "All");
        typeComboBox.getSelectionModel().selectFirst();

        objectTypeComboBox.setItems(objectTypeOptions);
        objectTypeComboBox.getItems().add(0, "All");
        objectTypeComboBox.getSelectionModel().selectFirst();

        folderComboBox.setItems(folderOptions);
        folderComboBox.getItems().add(0, "All");
        folderComboBox.getSelectionModel().selectFirst();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());
    }

    private void loadAllLists() {
        listData.setAll(listsDAO.getAllActiveLists());
        listsTable.setItems(listData);

        typeOptions.setAll(listData.stream()
                .map(List::getType)
                .filter(type -> type != null)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        typeOptions.add(0, "All");
        typeComboBox.getSelectionModel().selectFirst();

        objectTypeOptions.setAll(listData.stream()
                .map(List::getObjectType)
                .filter(objectType -> objectType != null)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        objectTypeOptions.add(0, "All");
        objectTypeComboBox.getSelectionModel().selectFirst();

        folderOptions.setAll(listData.stream()
                .map(List::getFolder)
                .filter(folder -> folder != null && !folder.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        folderOptions.add(0, "All");
        folderComboBox.getSelectionModel().selectFirst();

        updateSummaryAndCharts();
    }

    private void applyFilters() {
        java.util.List<List> filteredLists = listsDAO.getAllActiveLists();

        String nameFilter = nameFilterField.getText().trim();
        if (!nameFilter.isEmpty()) {
            filteredLists = listsDAO.searchListsByName(nameFilter);
        }

        String typeFilter = typeComboBox.getValue();
        if (typeFilter != null && !typeFilter.equals("All")) {
            filteredLists = filteredLists.stream()
                    .filter(list -> typeFilter.equals(list.getType()))
                    .collect(Collectors.toList());
        }

        String objectTypeFilter = objectTypeComboBox.getValue();
        if (objectTypeFilter != null && !objectTypeFilter.equals("All")) {
            filteredLists = filteredLists.stream()
                    .filter(list -> objectTypeFilter.equals(list.getObjectType()))
                    .collect(Collectors.toList());
        }

        String folderFilter = folderComboBox.getValue();
        if (folderFilter != null && !folderFilter.equals("All")) {
            filteredLists = filteredLists.stream()
                    .filter(list -> folderFilter.equals(list.getFolder()))
                    .collect(Collectors.toList());
        }

        listData.setAll(filteredLists);
        updateSummaryAndCharts();
    }

    private void clearFilters() {
        nameFilterField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        objectTypeComboBox.getSelectionModel().selectFirst();
        folderComboBox.getSelectionModel().selectFirst();
        loadAllLists();
    }

    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("lists_report.csv"))) {
            writer.write("Name,Description,Type,Object Type,Creator,Folder,Size,Created At,Updated At\n");

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

    private void updateSummaryAndCharts() {
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

        totalListsLabel.setText("Total Lists: " + totalLists);
        averageListSizeLabel.setText(String.format("Average List Size: %.1f", averageListSize));
        uniqueTypesLabel.setText("Unique Types: " + uniqueTypes);
        uniqueFoldersLabel.setText("Unique Folders: " + uniqueFolders);

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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}