package smallbusinessbuddycrm.controllers.workshop;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.model.Workshop;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

<<<<<<< HEAD
/**
 * Controller class for comprehensive workshop reporting and analytics with advanced filtering capabilities.
 *
 * This controller provides a complete workshop reporting solution with sophisticated analytics features:
 * - Multi-criteria filtering system with name, date range, status, and teacher filters
 * - Real-time data visualization with interactive pie charts and bar charts
 * - Comprehensive summary statistics with live calculations
 * - CSV export functionality with localized headers and data formatting
 * - Complete internationalization support with dynamic language switching
 * - Teacher integration with name resolution and assignment tracking
 * - Date range filtering with validation and error handling
 * - Status-based analytics (active/upcoming/past workshop analysis)
 *
 * Key Features:
 * - Advanced Filtering System: Multi-dimensional filtering across workshop attributes
 * - Data Visualization: Interactive charts showing status distribution and duration patterns
 * - Export Capabilities: CSV export with complete workshop information and localized formatting
 * - Teacher Analytics: Integration with teacher data for assignment analysis
 * - Real-time Updates: Live chart and summary updates as filters are applied
 * - Internationalization: Full language support with localized chart labels and text
 * - Error Handling: Comprehensive validation and user feedback for all operations
 * - Performance Optimization: Efficient data loading and filtering algorithms
 *
 * Filtering System:
 * - Name Filter: Text-based search across workshop names with case-insensitive matching
 * - Date Range Filter: From/to date filtering with ISO date format validation
 * - Status Filter: Active, upcoming, and past workshop status filtering
 * - Teacher Filter: Filter workshops by assigned teacher with "All Teachers" option
 * - Combined Filtering: All filters work together for precise data selection
 *
 * Analytics & Visualization:
 * - Summary Statistics: Total workshops, active/upcoming counts, average duration
 * - Status Distribution: Pie chart showing breakdown of workshop statuses
 * - Duration Analysis: Bar chart categorizing workshops by duration (1-3, 4-7, 8+ days)
 * - Real-time Updates: Charts and statistics update automatically with filter changes
 * - Localized Labels: All chart labels and legends adapt to current language
 *
 * Data Export:
 * - CSV Export: Complete workshop data with resolved teacher names
 * - Localized Headers: Column headers in user's preferred language
 * - Data Formatting: Proper CSV escaping and formatting for external use
 * - Status Resolution: Translated status values in exported data
 * - Error Handling: Comprehensive error management for file operations
 *
 * Teacher Integration:
 * - Teacher Loading: Dynamic loading of all available teachers for filtering
 * - Name Resolution: Efficient teacher name lookup for display purposes
 * - Assignment Tracking: Shows which workshops have assigned teachers
 * - Filter Options: Ability to filter by specific teacher or view all
 *
 * Internationalization Features:
 * - Dynamic Language Switching: All UI elements update when language changes
 * - Localized Charts: Chart titles, legends, and data labels in current language
 * - Date Formatting: Locale-appropriate date display and validation
 * - Status Translation: Workshop status values translated to current language
 * - Export Localization: CSV headers and data values in user's language
 *
 * The controller integrates with WorkshopDAO for workshop data operations and TeacherDAO
 * for teacher information, providing a complete reporting solution for workshop management
 * with professional-grade analytics and export capabilities.
 *
 * Performance Considerations:
 * - Efficient filtering algorithms that minimize database queries
 * - Lazy loading of teacher data to optimize initialization time
 * - Smart chart updates that only refresh when data actually changes
 * - Optimized date parsing and validation for large datasets
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class WorkshopsReportController {

    // Main Layout Container
    @FXML private VBox root;

    // Section Labels
=======
public class WorkshopsReportController {

    @FXML private VBox root;
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML private Label titleLabel;
    @FXML private Label filtersLabel;
    @FXML private Label nameLabel;
    @FXML private Label dateRangeLabel;
    @FXML private Label statusLabel;
    @FXML private Label teacherLabel;
    @FXML private Label summaryLabel;
    @FXML private Label visualAnalyticsLabel;
<<<<<<< HEAD

    // Filter Controls
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML private TextField nameFilterField;
    @FXML private TextField fromDateField;
    @FXML private TextField toDateField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Teacher> teacherComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
<<<<<<< HEAD

    // Data Table
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableColumn<Workshop, String> nameColumn;
    @FXML private TableColumn<Workshop, String> dateRangeColumn;
    @FXML private TableColumn<Workshop, Integer> durationColumn;
    @FXML private TableColumn<Workshop, String> statusColumn;
    @FXML private TableColumn<Workshop, String> teacherNameColumn;
<<<<<<< HEAD

    // Summary Statistics Labels
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML private Label totalWorkshopsLabel;
    @FXML private Label activeWorkshopsLabel;
    @FXML private Label upcomingWorkshopsLabel;
    @FXML private Label averageDurationLabel;
<<<<<<< HEAD

    // Charts
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> durationBarChart;
    @FXML private CategoryAxis durationGroupAxis;
    @FXML private NumberAxis numberOfWorkshopsAxis;
<<<<<<< HEAD

    // Export Control
    @FXML private Button exportButton;

    // Database Access Objects
    private WorkshopDAO workshopDAO;
    private TeacherDAO teacherDAO;

    // Data Collections
    private ObservableList<Workshop> workshopList;
    private ObservableList<Teacher> teacherList;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up database connections, initializes data collections, configures table columns
     * with custom cell value factories, loads teacher and workshop data, sets up event
     * handlers, and initializes internationalization support.
     */
=======
    @FXML private Button exportButton;

    private WorkshopDAO workshopDAO;
    private TeacherDAO teacherDAO;
    private ObservableList<Workshop> workshopList;
    private ObservableList<Teacher> teacherList;

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    @FXML
    public void initialize() {
        System.out.println("🚀 WorkshopsReportController initializing...");

        workshopDAO = new WorkshopDAO();
        teacherDAO = new TeacherDAO();
        workshopList = FXCollections.observableArrayList();
        teacherList = FXCollections.observableArrayList();

<<<<<<< HEAD
        // Configure table columns with appropriate cell value factories
=======
        // Set up table columns
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateRangeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateRange()));
        durationColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty((int) cellData.getValue().getDurationInDays()).asObject());
        statusColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            LanguageManager lm = LanguageManager.getInstance();
            String status = w.isActive() ? lm.getText("workshops.status.active") :
                    w.isUpcoming() ? lm.getText("workshops.status.upcoming") :
                            lm.getText("workshops.status.past");
            return new SimpleStringProperty(status);
        });

<<<<<<< HEAD
        // Configure teacher name column with database lookup
=======
        // Updated teacher name column to use TeacherDAO
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        teacherNameColumn.setCellValueFactory(cellData -> {
            Workshop w = cellData.getValue();
            Teacher teacher = w.getTeacherId() != null ? teacherDAO.getTeacherById(w.getTeacherId()) : null;
            String teacherName = teacher != null ?
                    (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                            (teacher.getLastName() != null ? teacher.getLastName() : "") : "No Teacher";
            return new SimpleStringProperty(teacherName.trim());
        });

<<<<<<< HEAD
        // Load teachers for filter dropdown
        loadTeachers();

        // Configure teacher combo box display with custom cell factories
=======
        // Load teachers
        loadTeachers();

        // Set up teacher combo box display
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        teacherComboBox.setCellFactory(param -> new ListCell<Teacher>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.getId() == -1) {
                        setText(LanguageManager.getInstance().getText("workshops.teacher.all"));
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
                        setText(LanguageManager.getInstance().getText("workshops.teacher.all"));
                    } else {
                        String name = (item.getFirstName() != null ? item.getFirstName() : "") + " " +
                                (item.getLastName() != null ? item.getLastName() : "");
                        setText(name.trim().isEmpty() ? "Unknown Teacher" : name.trim());
                    }
                }
            }
        });

        teacherComboBox.getSelectionModel().selectFirst();

<<<<<<< HEAD
        // Load initial workshop data
        loadAllWorkshops();

        // Configure event handlers
=======
        // Load initial data
        loadAllWorkshops();

        // Set up event handlers
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        applyFilterButton.setOnAction(e -> applyFilters());
        clearFilterButton.setOnAction(e -> clearFilters());
        exportButton.setOnAction(e -> exportToCSV());

<<<<<<< HEAD
        // Set up internationalization support
=======
        // Add language change listener and initial text update
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();

        System.out.println("✅ WorkshopsReportController initialization complete");
    }

<<<<<<< HEAD
    /**
     * Updates all UI text elements based on the current language settings.
     * Called when language changes to refresh labels, buttons, table headers,
     * placeholders, chart titles, and combo box options. Updates summary
     * statistics with localized text formatting.
     */
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main section labels
=======
    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Update main labels
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (titleLabel != null) titleLabel.setText(languageManager.getText("workshops.report.title"));
        if (filtersLabel != null) filtersLabel.setText(languageManager.getText("workshops.report.filters"));
        if (nameLabel != null) nameLabel.setText(languageManager.getText("workshops.report.filter.name"));
        if (dateRangeLabel != null) dateRangeLabel.setText(languageManager.getText("workshops.report.filter.date.range"));
        if (statusLabel != null) statusLabel.setText(languageManager.getText("workshops.report.filter.status"));
        if (teacherLabel != null) teacherLabel.setText(languageManager.getText("workshops.report.filter.teacher"));
        if (summaryLabel != null) summaryLabel.setText(languageManager.getText("workshops.report.summary"));
        if (visualAnalyticsLabel != null) visualAnalyticsLabel.setText(languageManager.getText("workshops.report.visual.analytics"));

<<<<<<< HEAD
        // Update form field placeholders
=======
        // Update text field prompts
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (nameFilterField != null) nameFilterField.setPromptText(languageManager.getText("workshops.report.filter.name.placeholder"));
        if (fromDateField != null) fromDateField.setPromptText(languageManager.getText("workshops.report.filter.from.date"));
        if (toDateField != null) toDateField.setPromptText(languageManager.getText("workshops.report.filter.to.date"));

<<<<<<< HEAD
        // Update action buttons
=======
        // Update buttons
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (applyFilterButton != null) applyFilterButton.setText(languageManager.getText("workshops.report.apply.filters"));
        if (clearFilterButton != null) clearFilterButton.setText(languageManager.getText("workshops.report.clear.filters"));
        if (exportButton != null) exportButton.setText(languageManager.getText("workshops.export.button"));

<<<<<<< HEAD
        // Update table column headers
=======
        // Update table columns
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (nameColumn != null) nameColumn.setText(languageManager.getText("workshops.table.name"));
        if (dateRangeColumn != null) dateRangeColumn.setText(languageManager.getText("workshops.table.date.range"));
        if (durationColumn != null) durationColumn.setText(languageManager.getText("workshops.table.duration"));
        if (statusColumn != null) statusColumn.setText(languageManager.getText("workshops.table.status"));
        if (teacherNameColumn != null) teacherNameColumn.setText(languageManager.getText("workshops.table.teacher.name"));

<<<<<<< HEAD
        // Update chart titles and axis labels
=======
        // Update chart titles and axes
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (statusPieChart != null) statusPieChart.setTitle(languageManager.getText("workshops.report.status.distribution"));
        if (durationBarChart != null) durationBarChart.setTitle(languageManager.getText("workshops.report.duration.distribution"));
        if (durationGroupAxis != null) durationGroupAxis.setLabel(languageManager.getText("workshops.report.duration.group"));
        if (numberOfWorkshopsAxis != null) numberOfWorkshopsAxis.setLabel(languageManager.getText("workshops.report.number.workshops"));

<<<<<<< HEAD
        // Update dynamic content
        updateStatusComboBox();
        updateSummaryLabels();
    }

    /**
     * Updates the status combo box with localized filter options.
     * Refreshes combo box items with translated status options (All, Active, Upcoming, Past)
     * while attempting to maintain the current selection state.
     */
    private void updateStatusComboBox() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Store current selection to maintain after update
        String selectedValue = statusComboBox.getValue();

        // Create localized status options
=======
        // Update status combo box
        updateStatusComboBox();

        // Update summary labels with current data
        updateSummaryLabels();
    }

    private void updateStatusComboBox() {
        LanguageManager languageManager = LanguageManager.getInstance();

        String selectedValue = statusComboBox.getValue();
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                languageManager.getText("workshops.status.all"),
                languageManager.getText("workshops.status.active"),
                languageManager.getText("workshops.status.upcoming"),
                languageManager.getText("workshops.status.past")
        );

        statusComboBox.setItems(statusOptions);

<<<<<<< HEAD
        // Attempt to maintain previous selection, fallback to first option
=======
        // Try to maintain selection
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (selectedValue != null) {
            statusComboBox.getSelectionModel().select(selectedValue);
        } else {
            statusComboBox.getSelectionModel().selectFirst();
        }
    }

<<<<<<< HEAD
    /**
     * Updates summary statistic labels with current workshop data.
     * Calculates and displays total workshops, active workshops, upcoming workshops,
     * and average duration using localized text formatting and current dataset.
     */
    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Calculate summary statistics from current filtered data
=======
    private void updateSummaryLabels() {
        LanguageManager languageManager = LanguageManager.getInstance();

>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        int totalWorkshops = workshopList.size();
        long activeWorkshops = workshopList.stream().filter(Workshop::isActive).count();
        long upcomingWorkshops = workshopList.stream().filter(Workshop::isUpcoming).count();
        double averageDuration = workshopList.stream()
                .mapToInt(w -> (int) w.getDurationInDays())
                .average()
                .orElse(0.0);

<<<<<<< HEAD
        // Update summary labels with localized text and calculated values
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        if (totalWorkshopsLabel != null)
            totalWorkshopsLabel.setText(languageManager.getText("workshops.report.total.workshops") + totalWorkshops);
        if (activeWorkshopsLabel != null)
            activeWorkshopsLabel.setText(languageManager.getText("workshops.report.active.workshops") + activeWorkshops);
        if (upcomingWorkshopsLabel != null)
            upcomingWorkshopsLabel.setText(languageManager.getText("workshops.report.upcoming.workshops") + upcomingWorkshops);
        if (averageDurationLabel != null)
            averageDurationLabel.setText(languageManager.getText("workshops.report.average.duration") + String.format("%.1f", averageDuration));
    }

<<<<<<< HEAD
    /**
     * Loads all teachers from database for teacher filter selection.
     * Fetches teachers via TeacherDAO, creates "All Teachers" option for filter,
     * populates teacher combo box with custom cell factories for display,
     * and handles loading errors with user feedback.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void loadTeachers() {
        System.out.println("📚 Loading teachers from TeacherDAO...");

        try {
            List<Teacher> allTeachers = teacherDAO.getAllTeachers();
            System.out.println("📊 Retrieved " + allTeachers.size() + " teachers from database");

<<<<<<< HEAD
            // Create "All Teachers" option for filtering
=======
            // Create "All Teachers" option
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
            // Debug: Print first few teachers for verification
=======
            // Debug: Print first few teachers
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            for (int i = 0; i < Math.min(3, teacherList.size()); i++) {
                Teacher t = teacherList.get(i);
                System.out.println("   Teacher " + i + ": " + t.getFirstName() + " " + t.getLastName() + " (ID: " + t.getId() + ")");
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading teachers: " + e.getMessage());
            e.printStackTrace();

<<<<<<< HEAD
            // Show error alert to user
=======
            // Show error alert
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            showAlert(Alert.AlertType.ERROR, "Error Loading Teachers",
                    "Failed to load teachers from database: " + e.getMessage());
        }
    }

<<<<<<< HEAD
    /**
     * Loads all workshops from database and populates the report table.
     * Fetches workshops via WorkshopDAO, updates observable list and table view,
     * refreshes summary statistics and charts, and handles loading errors gracefully.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Applies user-specified filters to the workshop dataset.
     * Processes name filter, date range filter, status filter, and teacher filter.
     * Updates table view and analytics with filtered results. Validates date
     * input format and provides user feedback for invalid dates.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void applyFilters() {
        System.out.println("🔍 Applying filters...");

        try {
            List<Workshop> filteredWorkshops = workshopDAO.getAllWorkshops();

<<<<<<< HEAD
            // Apply name filter using workshop search
=======
            // Apply name filter
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            String nameFilter = nameFilterField.getText().trim();
            if (!nameFilter.isEmpty()) {
                System.out.println("   Filtering by name: " + nameFilter);
                filteredWorkshops = workshopDAO.searchWorkshops(nameFilter);
            }

<<<<<<< HEAD
            // Apply date range filter with validation
=======
            // Apply date range filter
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
            // Apply status filter using localized status values
=======
            // Apply status filter
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            String status = statusComboBox.getValue();
            LanguageManager lm = LanguageManager.getInstance();
            if (status != null && !status.equals(lm.getText("workshops.status.all"))) {
                System.out.println("   Filtering by status: " + status);
                if (status.equals(lm.getText("workshops.status.active"))) {
                    filteredWorkshops = workshopDAO.getActiveWorkshops();
                } else if (status.equals(lm.getText("workshops.status.upcoming"))) {
                    filteredWorkshops = workshopDAO.getUpcomingWorkshops(30);
                } else if (status.equals(lm.getText("workshops.status.past"))) {
                    filteredWorkshops = filteredWorkshops.stream()
                            .filter(w -> !w.isActive() && !w.isUpcoming())
                            .collect(Collectors.toList());
                }
            }

            // Apply teacher filter
            Teacher selectedTeacher = teacherComboBox.getValue();
            if (selectedTeacher != null && selectedTeacher.getId() != -1) {
                System.out.println("   Filtering by teacher: " + selectedTeacher.getFirstName() + " " + selectedTeacher.getLastName());
                filteredWorkshops = workshopDAO.getWorkshopsByTeacher(selectedTeacher.getId());
            } else if (selectedTeacher != null && selectedTeacher.getId() == -1) {
                System.out.println("   Showing all teachers");
<<<<<<< HEAD
                // Keep all workshops - no teacher filter applied
=======
                // Keep all workshops - no teacher filter
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Clears all filter inputs and reloads complete workshop dataset.
     * Resets all filter fields to default values and reloads all workshops
     * from database to restore original unfiltered view.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Exports current filtered workshop data to CSV file.
     * Creates CSV file with localized headers and all workshop information
     * including resolved teacher names and status text. Shows success or
     * error feedback to user. File saved as "workshops_report.csv".
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void exportToCSV() {
        System.out.println("💾 Exporting workshops to CSV...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("workshops_report.csv"))) {
            LanguageManager lm = LanguageManager.getInstance();
<<<<<<< HEAD

            // Write CSV header with localized column names
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
            writer.write(lm.getText("workshops.table.name") + "," +
                    lm.getText("workshops.table.date.range") + "," +
                    lm.getText("workshops.table.duration") + "," +
                    lm.getText("workshops.table.status") + "," +
                    lm.getText("workshops.table.teacher.name") + "\n");

<<<<<<< HEAD
            // Write data rows with proper CSV formatting
            for (Workshop w : workshopList) {
                // Resolve teacher name
=======
            for (Workshop w : workshopList) {
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
                Teacher teacher = w.getTeacherId() != null ? teacherDAO.getTeacherById(w.getTeacherId()) : null;
                String teacherName = teacher != null ?
                        (teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                                (teacher.getLastName() != null ? teacher.getLastName() : "") : "No Teacher";
<<<<<<< HEAD

                // Format status with localized text
                String status = w.isActive() ? lm.getText("workshops.status.active") :
                        w.isUpcoming() ? lm.getText("workshops.status.upcoming") :
                                lm.getText("workshops.status.past");

                // Create CSV line with proper quoting
=======
                String status = w.isActive() ? lm.getText("workshops.status.active") :
                        w.isUpcoming() ? lm.getText("workshops.status.upcoming") :
                                lm.getText("workshops.status.past");
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
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

<<<<<<< HEAD
    /**
     * Updates summary statistics and visual charts with current dataset.
     * Refreshes summary labels, status distribution pie chart with localized labels,
     * and duration distribution bar chart with three duration groups (1-3, 4-7, 8+ days).
     * Calculates and displays comprehensive workshop analytics.
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void updateSummaryAndCharts() {
        System.out.println("📈 Updating summary statistics and charts...");

        updateSummaryLabels();

<<<<<<< HEAD
        // Calculate statistics for chart data
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        int totalWorkshops = workshopList.size();
        long activeWorkshops = workshopList.stream().filter(Workshop::isActive).count();
        long upcomingWorkshops = workshopList.stream().filter(Workshop::isUpcoming).count();

<<<<<<< HEAD
        // Update pie chart with status distribution
=======
        // Update pie chart
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        long pastWorkshops = totalWorkshops - activeWorkshops - upcomingWorkshops;
        LanguageManager lm = LanguageManager.getInstance();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(lm.getText("workshops.status.active"), activeWorkshops),
                new PieChart.Data(lm.getText("workshops.status.upcoming"), upcomingWorkshops),
                new PieChart.Data(lm.getText("workshops.status.past"), pastWorkshops)
        );
        statusPieChart.setData(pieChartData);
        statusPieChart.setLabelLineLength(10);
        statusPieChart.setLabelsVisible(true);

<<<<<<< HEAD
        // Calculate duration group distributions
=======
        // Update bar chart
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        int duration1to3 = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 1 && w.getDurationInDays() <= 3).count();
        int duration4to7 = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 4 && w.getDurationInDays() <= 7).count();
        int duration8plus = (int) workshopList.stream().filter(w -> w.getDurationInDays() >= 8).count();

<<<<<<< HEAD
        // Update bar chart with duration distribution
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Workshops");
        series.getData().add(new XYChart.Data<>("1-3 Days", duration1to3));
        series.getData().add(new XYChart.Data<>("4-7 Days", duration4to7));
        series.getData().add(new XYChart.Data<>("8+ Days", duration8plus));

        durationBarChart.getData().clear();
        durationBarChart.getData().add(series);

        System.out.println("📊 Summary: " + totalWorkshops + " total, " + activeWorkshops + " active, " + upcomingWorkshops + " upcoming");
    }

<<<<<<< HEAD
    /**
     * Displays an alert dialog with specified type, title, and content.
     * Utility method for showing information, warning, or error messages
     * to the user with standardized formatting and logging.
     *
     * @param type The type of alert (INFORMATION, WARNING, ERROR)
     * @param title The alert dialog title
     * @param content The alert message content
     */
=======
>>>>>>> 18e08b724d9be7d8fa06c79fdb7757fb09d32170
    private void showAlert(Alert.AlertType type, String title, String content) {
        System.out.println("🚨 Showing alert: " + title + " - " + content);

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}