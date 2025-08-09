package smallbusinessbuddycrm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.model.Contact;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContactsReportController {

    @FXML private VBox root;
    @FXML private TextField nameFilterField;
    @FXML private TextField minAgeField;
    @FXML private TextField maxAgeField;
    @FXML private TextField birthdayDaysField;
    @FXML private ComboBox<String> listComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<Contact> contactsTable;
    @FXML private TableColumn<Contact, String> firstNameColumn;
    @FXML private TableColumn<Contact, String> lastNameColumn;
    @FXML private TableColumn<Contact, String> emailColumn;
    @FXML private TableColumn<Contact, String> phoneColumn;
    @FXML private TableColumn<Contact, String> addressColumn;
    @FXML private TableColumn<Contact, LocalDate> birthdayColumn;
    @FXML private TableColumn<Contact, Integer> ageColumn;
    @FXML private TableColumn<Contact, String> pinColumn;
    @FXML private TableColumn<Contact, String> memberColumn;
    @FXML private TableColumn<Contact, LocalDate> memberSinceColumn;
    @FXML private TableColumn<Contact, LocalDate> memberUntilColumn;
    @FXML private Label totalContactsLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalNonMembersLabel;
    @FXML private Label averageAgeLabel;
    @FXML private PieChart memberPieChart;
    @FXML private BarChart<String, Number> ageBarChart;
    @FXML private Button exportButton;

    private ContactDAO contactDAO;
    private ObservableList<Contact> contactsList;
    private List<Integer> availableLists;

    @FXML
    public void initialize() {
        contactDAO = new ContactDAO();
        contactsList = FXCollections.observableArrayList();
        availableLists = new ArrayList<>();

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        addressColumn.setCellValueFactory(cellData -> {
            Contact c = cellData.getValue();
            String address = (c.getStreetName() != null ? c.getStreetName() + " " : "") +
                    (c.getStreetNum() != null ? c.getStreetNum() + ", " : "") +
                    (c.getCity() != null ? c.getCity() + ", " : "") +
                    (c.getPostalCode() != null ? c.getPostalCode() : "");
            return new SimpleStringProperty(address.trim());
        });
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));
        memberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ? "Yes" : "No"));
        memberSinceColumn.setCellValueFactory(new PropertyValueFactory<>("memberSince"));
        memberUntilColumn.setCellValueFactory(new PropertyValueFactory<>("memberUntil"));

        loadAllContacts();

        listComboBox.setItems(FXCollections.observableArrayList("All Contacts"));
        listComboBox.getSelectionModel().selectFirst();

        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

        updateCharts();
    }

    private void loadAllContacts() {
        contactsList.setAll(contactDAO.getAllContacts());
        contactsTable.setItems(contactsList);
        updateSummaryAndCharts();
    }

    private void applyFilters() {
        List<Contact> filteredContacts = contactDAO.getAllContacts();

        String nameFilter = nameFilterField.getText().trim().toLowerCase();
        if (!nameFilter.isEmpty()) {
            filteredContacts = filteredContacts.stream()
                    .filter(c -> (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(nameFilter)) ||
                            (c.getLastName() != null && c.getLastName().toLowerCase().contains(nameFilter)))
                    .collect(Collectors.toList());
        }

        try {
            if (!minAgeField.getText().trim().isEmpty() && !maxAgeField.getText().trim().isEmpty()) {
                int minAge = Integer.parseInt(minAgeField.getText().trim());
                int maxAge = Integer.parseInt(maxAgeField.getText().trim());
                filteredContacts = contactDAO.getContactsByAgeRange(minAge, maxAge);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Age", "Please enter valid numbers for age range.");
        }

        try {
            if (!birthdayDaysField.getText().trim().isEmpty()) {
                int daysAhead = Integer.parseInt(birthdayDaysField.getText().trim());
                filteredContacts = contactDAO.getContactsWithUpcomingBirthdays(daysAhead);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Days", "Please enter a valid number for days ahead.");
        }

        String selectedList = listComboBox.getValue();
        if (selectedList != null && !selectedList.equals("All Contacts")) {
            int listId = availableLists.get(listComboBox.getSelectionModel().getSelectedIndex() - 1);
            filteredContacts = contactDAO.getContactsInList(listId);
        }

        contactsList.setAll(filteredContacts);
        updateSummaryAndCharts();
    }

    private void clearFilters() {
        nameFilterField.clear();
        minAgeField.clear();
        maxAgeField.clear();
        birthdayDaysField.clear();
        listComboBox.getSelectionModel().selectFirst();
        loadAllContacts();
    }

    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts_report.csv"))) {
            writer.write("First Name,Last Name,Email,Phone,Address,Birthday,Age,PIN,Member,Member Since,Member Until\n");

            for (Contact c : contactsList) {
                String address = (c.getStreetName() != null ? c.getStreetName() + " " : "") +
                        (c.getStreetNum() != null ? c.getStreetNum() + ", " : "") +
                        (c.getCity() != null ? c.getCity() + ", " : "") +
                        (c.getPostalCode() != null ? c.getPostalCode() : "");
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        c.getFirstName() != null ? c.getFirstName() : "",
                        c.getLastName() != null ? c.getLastName() : "",
                        c.getEmail() != null ? c.getEmail() : "",
                        c.getPhoneNum() != null ? c.getPhoneNum() : "",
                        address.trim(),
                        c.getBirthday() != null ? c.getBirthday().toString() : "",
                        c.getAge(),
                        c.getPin() != null ? c.getPin() : "",
                        c.isMember() ? "Yes" : "No",
                        c.getMemberSince() != null ? c.getMemberSince().toString() : "",
                        c.getMemberUntil() != null ? c.getMemberUntil().toString() : "");
                writer.write(line);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Contacts exported to contacts_report.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    private void updateSummaryAndCharts() {
        int totalContacts = contactsList.size();
        long totalMembers = contactsList.stream().filter(Contact::isMember).count();
        long totalNonMembers = totalContacts - totalMembers;
        double averageAge = contactsList.stream()
                .filter(c -> c.getAge() > 0)
                .mapToInt(Contact::getAge)
                .average()
                .orElse(0.0);

        totalContactsLabel.setText("Total Contacts: " + totalContacts);
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

        int ageGroup0to18 = (int) contactsList.stream().filter(c -> c.getAge() <= 18).count();
        int ageGroup19to30 = (int) contactsList.stream().filter(c -> c.getAge() >= 19 && c.getAge() <= 30).count();
        int ageGroup31to50 = (int) contactsList.stream().filter(c -> c.getAge() >= 31 && c.getAge() <= 50).count();
        int ageGroup51plus = (int) contactsList.stream().filter(c -> c.getAge() >= 51).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Contacts");
        series.getData().add(new XYChart.Data<>("0-18", ageGroup0to18));
        series.getData().add(new XYChart.Data<>("19-30", ageGroup19to30));
        series.getData().add(new XYChart.Data<>("31-50", ageGroup31to50));
        series.getData().add(new XYChart.Data<>("51+", ageGroup51plus));

        ageBarChart.getData().clear();
        ageBarChart.getData().add(series);
    }

    private void updateCharts() {
        updateSummaryAndCharts();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}