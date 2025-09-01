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

/**
 * Controller for the Contacts Report view, providing comprehensive contact analysis and reporting features.
 *
 * This controller manages:
 * - Contact filtering by name, age range, upcoming birthdays, and custom lists
 * - Data visualization through pie charts and bar charts
 * - Summary statistics (total contacts, members, average age)
 * - CSV export functionality
 * - Real-time data updates based on filter changes
 * - Internationalization support for all UI elements
 *
 * Features:
 * - Multi-criteria filtering with instant results
 * - Visual analytics showing member distribution and age demographics
 * - Export to CSV with all contact details
 * - Dynamic chart updates based on filtered data
 * - Responsive UI with proper error handling
 *
 * The view displays contacts in a detailed table and provides two main visualizations:
 * 1. Pie chart showing member vs non-member distribution
 * 2. Bar chart showing age group distribution (0-18, 19-30, 31-50, 51+)
 */
public class ContactsReportController {

    // ===== ROOT CONTAINER =====

    /** Root container for the entire report view */
    @FXML private VBox root;

    // ===== SECTION LABELS =====

    /** Main title label for the report */
    @FXML private Label titleLabel;

    /** Section header for filter controls */
    @FXML private Label filtersLabel;

    /** Label for name filter field */
    @FXML private Label nameLabel;

    /** Label for age range filter section */
    @FXML private Label ageRangeLabel;

    /** Label for upcoming birthdays filter */
    @FXML private Label upcomingBirthdaysLabel;

    /** Label for list selection filter */
    @FXML private Label listLabel;

    /** Section header for summary statistics */
    @FXML private Label summaryLabel;

    /** Section header for visual analytics */
    @FXML private Label visualAnalyticsLabel;

    // ===== FILTER CONTROLS =====

    /** Text field for filtering contacts by name (first or last name) */
    @FXML private TextField nameFilterField;

    /** Text field for minimum age in age range filter */
    @FXML private TextField minAgeField;

    /** Text field for maximum age in age range filter */
    @FXML private TextField maxAgeField;

    /** Text field for number of days ahead for birthday filter */
    @FXML private TextField birthdayDaysField;

    /** Dropdown for selecting specific contact lists */
    @FXML private ComboBox<String> listComboBox;

    /** Button to apply all active filters */
    @FXML private Button applyFilterButton;

    /** Button to clear all filters and show all contacts */
    @FXML private Button clearFilterButton;

    // ===== CONTACTS TABLE =====

    /** Main table displaying filtered contact results */
    @FXML private TableView<Contact> contactsTable;

    /** Column displaying contact first names */
    @FXML private TableColumn<Contact, String> firstNameColumn;

    /** Column displaying contact last names */
    @FXML private TableColumn<Contact, String> lastNameColumn;

    /** Column displaying email addresses */
    @FXML private TableColumn<Contact, String> emailColumn;

    /** Column displaying phone numbers */
    @FXML private TableColumn<Contact, String> phoneColumn;

    /** Column displaying formatted addresses */
    @FXML private TableColumn<Contact, String> addressColumn;

    /** Column displaying birth dates */
    @FXML private TableColumn<Contact, LocalDate> birthdayColumn;

    /** Column displaying calculated ages */
    @FXML private TableColumn<Contact, Integer> ageColumn;

    /** Column displaying PIN/identification numbers */
    @FXML private TableColumn<Contact, String> pinColumn;

    /** Column displaying member status (Yes/No) */
    @FXML private TableColumn<Contact, String> memberColumn;

    /** Column displaying membership start dates */
    @FXML private TableColumn<Contact, LocalDate> memberSinceColumn;

    /** Column displaying membership end dates */
    @FXML private TableColumn<Contact, LocalDate> memberUntilColumn;

    // ===== SUMMARY STATISTICS LABELS =====

    /** Label showing total number of contacts */
    @FXML private Label totalContactsLabel;

    /** Label showing total number of members */
    @FXML private Label totalMembersLabel;

    /** Label showing total number of non-members */
    @FXML private Label totalNonMembersLabel;

    /** Label showing average age of all contacts */
    @FXML private Label averageAgeLabel;

    // ===== VISUALIZATION COMPONENTS =====

    /** Pie chart showing member vs non-member distribution */
    @FXML private PieChart memberPieChart;

    /** Bar chart showing age group distribution */
    @FXML private BarChart<String, Number> ageBarChart;

    /** X-axis for age group categories in bar chart */
    @FXML private CategoryAxis ageGroupAxis;

    /** Y-axis for contact counts in bar chart */
    @FXML private NumberAxis contactsCountAxis;

    // ===== ACTION BUTTONS =====

    /** Button to export current data to CSV file */
    @FXML private Button exportButton;

    // ===== DATA MANAGEMENT =====

    /** Data access object for contact database operations */
    private ContactDAO contactDAO;

    /** Observable list of contacts currently displayed in table */
    private ObservableList<Contact> contactsList;

    /** List of available contact list IDs for filtering */
    private List<Integer> availableLists;

    /**
     * Initializes the controller after FXML loading.
     * Sets up table columns, data binding, event handlers, and loads initial data.
     */
    @FXML
    public void initialize() {
        // Initialize data access and collections
        contactDAO = new ContactDAO();
        contactsList = FXCollections.observableArrayList();
        availableLists = new ArrayList<>();

        // Configure table columns with property bindings
        setupTableColumns();

        // Load all contacts initially
        loadAllContacts();

        // Initialize list combo box with default option
        listComboBox.setItems(FXCollections.observableArrayList("All Contacts"));
        listComboBox.getSelectionModel().selectFirst();

        // Setup event handlers for buttons
        setupEventHandlers();

        // Initialize charts with current data
        updateCharts();

        // Setup internationalization
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    /**
     * Configures all table columns with appropriate cell value factories.
     * Sets up both simple property bindings and custom cell factories for complex data.
     */
    private void setupTableColumns() {
        // Simple property columns
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));

        // Complex address column - combines multiple address fields
        addressColumn.setCellValueFactory(cellData -> {
            Contact c = cellData.getValue();
            String address = (c.getStreetName() != null ? c.getStreetName() + " " : "") +
                    (c.getStreetNum() != null ? c.getStreetNum() + ", " : "") +
                    (c.getCity() != null ? c.getCity() + ", " : "") +
                    (c.getPostalCode() != null ? c.getPostalCode() : "");
            return new SimpleStringProperty(address.trim());
        });

        // Date and numeric columns
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));

        // Member status column - converts boolean to Yes/No
        memberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isMember() ? "Yes" : "No"));

        // Membership date columns
        memberSinceColumn.setCellValueFactory(new PropertyValueFactory<>("memberSince"));
        memberUntilColumn.setCellValueFactory(new PropertyValueFactory<>("memberUntil"));
    }

    /**
     * Sets up event handlers for all interactive buttons.
     */
    private void setupEventHandlers() {
        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());
    }

    /**
     * Updates all translatable text elements using the LanguageManager.
     * This method handles internationalization for labels, buttons, table headers, and chart titles.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main section labels
        if (titleLabel != null) titleLabel.setText(languageManager.getText("contacts.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("contacts.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("contacts.report.filter.name"));
        if (ageRangeLabel != null) ageRangeLabel.setText(languageManager.getText("contacts.report.filter.age.range"));
        if (upcomingBirthdaysLabel != null) upcomingBirthdaysLabel.setText(languageManager.getText("contacts.report.filter.upcoming.birthdays"));
        if (listLabel != null) listLabel.setText(languageManager.getText("contacts.report.filter.list"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("contacts.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("contacts.report.visual.analytics"));

        // Update input field placeholder text
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("contacts.report.filter.name.placeholder"));
        if (minAgeField != null) minAgeField.setPromptText(languageManager.getText("contacts.report.filter.min.age"));
        if (maxAgeField != null) maxAgeField.setPromptText(languageManager.getText("contacts.report.filter.max.age"));
        if (birthdayDaysField != null) birthdayDaysField.setPromptText(languageManager.getText("contacts.report.filter.days.ahead"));

        // Update button text
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("contacts.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("contacts.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("contacts.export.button"));

        // Update table column headers
        updateTableColumnHeaders(languageManager);

        // Update chart titles and axis labels
        updateChartLabels(languageManager);

        // Update summary labels with current data and new language
        updateSummaryLabels();
    }

    /**
     * Updates table column headers with translated text.
     *
     * @param languageManager LanguageManager instance for text retrieval
     */
    private void updateTableColumnHeaders(LanguageManager languageManager) {
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
    }

    /**
     * Updates chart titles and axis labels with translated text.
     *
     * @param languageManager LanguageManager instance for text retrieval
     */
    private void updateChartLabels(LanguageManager languageManager) {
        if (memberPieChart != null) memberPieChart.setTitle(languageManager.getText("contacts.report.member.distribution"));
        if (ageBarChart != null) ageBarChart.setTitle(languageManager.getText("contacts.report.age.distribution"));
        if (ageGroupAxis != null) ageGroupAxis.setLabel(languageManager.getText("contacts.report.age.group"));
        if (contactsCountAxis != null) contactsCountAxis.setLabel(languageManager.getText("contacts.report.contacts.count"));
    }

    /**
     * Updates summary statistic labels with current data and translated text.
     * Calculates and displays total contacts, members, non-members, and average age.
     */
    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Calculate current statistics
        int totalContacts = contactsList.size();
        long totalMembers = contactsList.stream().filter(Contact::isMember).count();
        long totalNonMembers = totalContacts - totalMembers;
        double averageAge = contactsList.stream()
                .filter(c -> c.getAge() > 0)
                .mapToInt(Contact::getAge)
                .average()
                .orElse(0.0);

        // Update labels with translated text and current values
        if (totalContactsLabel != null)
            totalContactsLabel.setText(languageManager.getText("contacts.report.total.contacts") + totalContacts);
        if (totalMembersLabel != null)
            totalMembersLabel.setText(languageManager.getText("contacts.report.total.members") + totalMembers);
        if (totalNonMembersLabel != null)
            totalNonMembersLabel.setText(languageManager.getText("contacts.report.total.nonmembers") + totalNonMembers);
        if (averageAgeLabel != null)
            averageAgeLabel.setText(languageManager.getText("contacts.report.average.age") + String.format("%.1f", averageAge));
    }

    /**
     * Loads all contacts from the database and updates the display.
     * This method refreshes the table, summary statistics, and charts.
     */
    private void loadAllContacts() {
        contactsList.setAll(contactDAO.getAllContacts());
        contactsTable.setItems(contactsList);
        updateSummaryAndCharts();
    }

    /**
     * Applies all active filters to the contact list.
     * Processes name, age range, birthday, and list filters in combination.
     * Shows appropriate error messages for invalid input.
     */
    private void applyFilters() {
        List<Contact> filteredContacts = contactDAO.getAllContacts();

        // Apply name filter
        filteredContacts = applyNameFilter(filteredContacts);

        // Apply age range filter
        filteredContacts = applyAgeRangeFilter(filteredContacts);

        // Apply birthday filter
        filteredContacts = applyBirthdayFilter(filteredContacts);

        // Apply list filter
        filteredContacts = applyListFilter(filteredContacts);

        // Update display with filtered results
        contactsList.setAll(filteredContacts);
        updateSummaryAndCharts();
    }

    /**
     * Applies name filter to the contact list.
     * Searches both first and last names (case-insensitive).
     *
     * @param contacts List of contacts to filter
     * @return Filtered list of contacts matching the name criteria
     */
    private List<Contact> applyNameFilter(List<Contact> contacts) {
        String nameFilter = nameFilterField.getText().trim().toLowerCase();
        if (!nameFilter.isEmpty()) {
            return contacts.stream()
                    .filter(c -> (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(nameFilter)) ||
                            (c.getLastName() != null && c.getLastName().toLowerCase().contains(nameFilter)))
                    .collect(Collectors.toList());
        }
        return contacts;
    }

    /**
     * Applies age range filter to the contact list.
     * Uses database query for efficient filtering when both min and max ages are provided.
     *
     * @param contacts List of contacts to filter
     * @return Filtered list of contacts within the specified age range
     */
    private List<Contact> applyAgeRangeFilter(List<Contact> contacts) {
        try {
            if (!minAgeField.getText().trim().isEmpty() && !maxAgeField.getText().trim().isEmpty()) {
                int minAge = Integer.parseInt(minAgeField.getText().trim());
                int maxAge = Integer.parseInt(maxAgeField.getText().trim());
                return contactDAO.getContactsByAgeRange(minAge, maxAge);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Age", "Please enter valid numbers for age range.");
        }
        return contacts;
    }

    /**
     * Applies upcoming birthday filter to the contact list.
     * Finds contacts with birthdays within the specified number of days.
     *
     * @param contacts List of contacts to filter
     * @return Filtered list of contacts with upcoming birthdays
     */
    private List<Contact> applyBirthdayFilter(List<Contact> contacts) {
        try {
            if (!birthdayDaysField.getText().trim().isEmpty()) {
                int daysAhead = Integer.parseInt(birthdayDaysField.getText().trim());
                return contactDAO.getContactsWithUpcomingBirthdays(daysAhead);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Days", "Please enter a valid number for days ahead.");
        }
        return contacts;
    }

    /**
     * Applies list filter to the contact list.
     * Filters contacts based on their membership in specific contact lists.
     *
     * @param contacts List of contacts to filter
     * @return Filtered list of contacts in the selected list
     */
    private List<Contact> applyListFilter(List<Contact> contacts) {
        String selectedList = listComboBox.getValue();
        if (selectedList != null && !selectedList.equals("All Contacts")) {
            int listId = availableLists.get(listComboBox.getSelectionModel().getSelectedIndex() - 1);
            return contactDAO.getContactsInList(listId);
        }
        return contacts;
    }

    /**
     * Clears all filter inputs and reloads all contacts.
     * Resets the view to show the complete contact database.
     */
    private void clearFilters() {
        nameFilterField.clear();
        minAgeField.clear();
        maxAgeField.clear();
        birthdayDaysField.clear();
        listComboBox.getSelectionModel().selectFirst();
        loadAllContacts();
    }

    /**
     * Exports the currently displayed contacts to a CSV file.
     * Creates a comprehensive export with all contact fields.
     * Shows success or error messages based on operation result.
     */
    private void exportToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts_report.csv"))) {
            // Write CSV header
            writer.write("First Name,Last Name,Email,Phone,Address,Birthday,Age,PIN,Member,Member Since,Member Until\n");

            // Write contact data
            for (Contact c : contactsList) {
                String csvLine = buildCSVLine(c);
                writer.write(csvLine);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Contacts exported to contacts_report.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error exporting to CSV: " + e.getMessage());
        }
    }

    /**
     * Builds a CSV line for a single contact with proper escaping and formatting.
     *
     * @param contact The contact to convert to CSV format
     * @return Formatted CSV line string
     */
    private String buildCSVLine(Contact contact) {
        // Build formatted address
        String address = (contact.getStreetName() != null ? contact.getStreetName() + " " : "") +
                (contact.getStreetNum() != null ? contact.getStreetNum() + ", " : "") +
                (contact.getCity() != null ? contact.getCity() + ", " : "") +
                (contact.getPostalCode() != null ? contact.getPostalCode() : "");

        // Format CSV line with proper quoting
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                contact.getFirstName() != null ? contact.getFirstName() : "",
                contact.getLastName() != null ? contact.getLastName() : "",
                contact.getEmail() != null ? contact.getEmail() : "",
                contact.getPhoneNum() != null ? contact.getPhoneNum() : "",
                address.trim(),
                contact.getBirthday() != null ? contact.getBirthday().toString() : "",
                contact.getAge(),
                contact.getPin() != null ? contact.getPin() : "",
                contact.isMember() ? "Yes" : "No",
                contact.getMemberSince() != null ? contact.getMemberSince().toString() : "",
                contact.getMemberUntil() != null ? contact.getMemberUntil().toString() : "");
    }

    /**
     * Updates summary statistics and all charts with current contact data.
     * This method should be called whenever the displayed contact list changes.
     */
    private void updateSummaryAndCharts() {
        updateSummaryLabels();
        updateMembershipPieChart();
        updateAgeDistributionBarChart();
    }

    /**
     * Updates the membership distribution pie chart.
     * Shows the proportion of members vs non-members in the current data set.
     */
    private void updateMembershipPieChart() {
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
    }

    /**
     * Updates the age distribution bar chart.
     * Shows contact counts across predefined age groups: 0-18, 19-30, 31-50, 51+.
     */
    private void updateAgeDistributionBarChart() {
        // Calculate age group counts
        int ageGroup0to18 = (int) contactsList.stream().filter(c -> c.getAge() <= 18).count();
        int ageGroup19to30 = (int) contactsList.stream().filter(c -> c.getAge() >= 19 && c.getAge() <= 30).count();
        int ageGroup31to50 = (int) contactsList.stream().filter(c -> c.getAge() >= 31 && c.getAge() <= 50).count();
        int ageGroup51plus = (int) contactsList.stream().filter(c -> c.getAge() >= 51).count();

        // Create chart data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Contacts");
        series.getData().add(new XYChart.Data<>("0-18", ageGroup0to18));
        series.getData().add(new XYChart.Data<>("19-30", ageGroup19to30));
        series.getData().add(new XYChart.Data<>("31-50", ageGroup31to50));
        series.getData().add(new XYChart.Data<>("51+", ageGroup51plus));

        // Update chart
        ageBarChart.getData().clear();
        ageBarChart.getData().add(series);
    }

    /**
     * Updates all charts with current data.
     * Convenience method that calls updateSummaryAndCharts().
     */
    private void updateCharts() {
        updateSummaryAndCharts();
    }

    /**
     * Displays an alert dialog with the specified type, title, and content.
     * Used for showing success messages, warnings, and error notifications.
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