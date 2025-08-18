package smallbusinessbuddycrm.controllers.contact;

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
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContactsReportController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label filtersLabel;
    @FXML private Label nameLabel;
    @FXML private Label ageRangeLabel;
    @FXML private Label upcomingBirthdaysLabel;
    @FXML private Label listLabel;
    @FXML private Label summaryLabel;
    @FXML private Label visualAnalyticsLabel;
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
    @FXML private CategoryAxis ageGroupAxis;
    @FXML private NumberAxis contactsCountAxis;
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

        // Add language change listener and initial text update
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main labels
        if (titleLabel != null) titleLabel.setText(languageManager.getText("contacts.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("contacts.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("contacts.report.filter.name"));
        if (ageRangeLabel != null) ageRangeLabel.setText(languageManager.getText("contacts.report.filter.age.range"));
        if (upcomingBirthdaysLabel != null) upcomingBirthdaysLabel.setText(languageManager.getText("contacts.report.filter.upcoming.birthdays"));
        if (listLabel != null) listLabel.setText(languageManager.getText("contacts.report.filter.list"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("contacts.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("contacts.report.visual.analytics"));

        // Update text field prompts
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("contacts.report.filter.name.placeholder"));
        if (minAgeField != null) minAgeField.setPromptText(languageManager.getText("contacts.report.filter.min.age"));
        if (maxAgeField != null) maxAgeField.setPromptText(languageManager.getText("contacts.report.filter.max.age"));
        if (birthdayDaysField != null) birthdayDaysField.setPromptText(languageManager.getText("contacts.report.filter.days.ahead"));

        // Update buttons
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("contacts.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("contacts.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("contacts.export.button"));

        // Update table columns
        if (firstNameColumn != null) firstNameColumn.setText(languageManager.getText("contacts.table.first.name"));
        if (lastNameColumn != null) lastNameColumn.setText(languageManager.getText("contacts.table.last.name"));
        if (emailColumn != null) emailColumn.setText(languageManager.getText("contacts.table.email"));
        if (phoneColumn != null) phoneColumn.setText(languageManager.getText("contacts.table.phone"));
        if (addressColumn != null) addressColumn.setText(languageManager.getText("contacts.table.address"));
        if (birthdayColumn != null) birthdayColumn.setText(languageManager.getText("contacts.table.birthday"));
        if (ageColumn != null) ageColumn.setText(languageManager.getText("contacts.table.age"));
        if (pinColumn != null) pinColumn.setText(languageManager.getText("contacts.table.pin"));
        if (memberColumn != null) memberColumn.setText(languageManager.getText("contacts.table.member"));
        if (memberSinceColumn != null) memberSinceColumn.setText(languageManager.getText("contacts.table.member.since"));
        if (memberUntilColumn != null) memberUntilColumn.setText(languageManager.getText("contacts.table.member.until"));

        // Update chart titles and axes
        if (memberPieChart != null) memberPieChart.setTitle(languageManager.getText("contacts.report.member.distribution"));
        if (ageBarChart != null) ageBarChart.setTitle(languageManager.getText("contacts.report.age.distribution"));
        if (ageGroupAxis != null) ageGroupAxis.setLabel(languageManager.getText("contacts.report.age.group"));
        if (contactsCountAxis != null) contactsCountAxis.setLabel(languageManager.getText("contacts.report.contacts.count"));

        // Update summary labels with current data
        updateSummaryLabels();
    }

    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        int totalContacts = contactsList.size();
        long totalMembers = contactsList.stream().filter(Contact::isMember).count();
        long totalNonMembers = totalContacts - totalMembers;
        double averageAge = contactsList.stream()
                .filter(c -> c.getAge() > 0)
                .mapToInt(Contact::getAge)
                .average()
                .orElse(0.0);

        if (totalContactsLabel != null)
            totalContactsLabel.setText(languageManager.getText("contacts.report.total.contacts") + totalContacts);
        if (totalMembersLabel != null)
            totalMembersLabel.setText(languageManager.getText("contacts.report.total.members") + totalMembers);
        if (totalNonMembersLabel != null)
            totalNonMembersLabel.setText(languageManager.getText("contacts.report.total.nonmembers") + totalNonMembers);
        if (averageAgeLabel != null)
            averageAgeLabel.setText(languageManager.getText("contacts.report.average.age") + String.format("%.1f", averageAge));
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
        updateSummaryLabels();

        int totalContacts = contactsList.size();
        long totalMembers = contactsList.stream().filter(Contact::isMember).count();
        long totalNonMembers = totalContacts - totalMembers;

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