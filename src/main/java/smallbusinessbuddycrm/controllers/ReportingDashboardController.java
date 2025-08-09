package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import java.io.IOException;

// Import your DAO classes
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.database.ListsDAO;

// Import your model classes
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.model.Workshop;
import smallbusinessbuddycrm.model.Teacher;

// Import for database connection in helper method
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Controller class for the Reporting Analytics Dashboard
 * Handles data population and button actions for the main dashboard view
 */
public class ReportingDashboardController {

    // FXML injected labels for displaying counts
    @FXML private Label totalContacts;
    @FXML private Label activeMembers;
    @FXML private Label totalUnderaged;
    @FXML private Label underagedMembers;
    @FXML private Label totalWorkshops;
    @FXML private Label activeWorkshops;
    @FXML private Label totalTeachers;
    @FXML private Label totalLists;
    @FXML private Label activeLists;

    // FXML injected buttons for navigation
    @FXML private Button contactsReportButton;
    @FXML private Button underagedReportButton;
    @FXML private Button workshopsReportButton;
    @FXML private Button teachersReportButton;
    @FXML private Button listsReportButton;

    // FXML injected content area for navigation
    @FXML private VBox contentArea; // Add this if you have a content area to navigate to

    // DAO instances - all sections
    private ContactDAO contactDAO;
    private UnderagedDAO underagedDAO;
    private WorkshopDAO workshopDAO;
    private TeacherDAO teacherDAO;
    private ListsDAO listsDAO;

    /**
     * Initialize the controller and load dashboard data
     * Call this method after FXML loading is complete
     */
    public void initialize() {


        // Initialize DAO instance
        initializeDAOs();

        // Load and display dashboard data
        loadDashboardData();
    }

    /**
     * Initialize DAO instances - all sections
     */
    private void initializeDAOs() {
        try {
            contactDAO = new ContactDAO();
            System.out.println("ContactDAO initialized successfully");

            underagedDAO = new UnderagedDAO();
            System.out.println("UnderagedDAO initialized successfully");

            workshopDAO = new WorkshopDAO();
            System.out.println("WorkshopDAO initialized successfully");

            teacherDAO = new TeacherDAO();
            System.out.println("TeacherDAO initialized successfully");

            listsDAO = new ListsDAO();
            System.out.println("ListsDAO initialized successfully");

            System.out.println("All DAOs initialized successfully!");

        } catch (Exception e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load dashboard data from database - all sections
     */
    private void loadDashboardData() {
        try {
            loadContactsData();
            loadUnderagedData();
            loadWorkshopsData();
            loadTeachersData();
            loadListsData();

            System.out.println("Dashboard data loading completed!");
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            setDefaultValues();
        }
    }

    /**
     * Load contacts data and update UI
     */
    private void loadContactsData() {
        try {
            if (contactDAO != null) {
                java.util.List<Contact> allContacts = contactDAO.getAllContacts();
                int totalContactsCount = allContacts.size();

                // Count active members (contacts where is_member = true)
                long activeMembersCount = allContacts.stream()
                        .filter(Contact::isMember)
                        .count();

                // Update UI labels
                totalContacts.setText(String.valueOf(totalContactsCount));
                activeMembers.setText(String.valueOf(activeMembersCount));

                System.out.println("Loaded contacts data - Total: " + totalContactsCount + ", Active: " + activeMembersCount);
            } else {
                // Fallback values if DAO is null
                totalContacts.setText("0");
                activeMembers.setText("0");
                System.out.println("ContactDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading contacts data: " + e.getMessage());
            e.printStackTrace();
            totalContacts.setText("0");
            activeMembers.setText("0");
        }
    }

    /**
     * Load underaged members data and update UI
     */
    private void loadUnderagedData() {
        try {
            if (underagedDAO != null) {
                java.util.List<UnderagedMember> allUnderaged = underagedDAO.getAllUnderagedMembers();
                int totalUnderagedCount = allUnderaged.size();

                // Count active underaged members (where is_member = true)
                long activeUnderagedCount = allUnderaged.stream()
                        .filter(UnderagedMember::isMember)
                        .count();

                // Update UI labels
                totalUnderaged.setText(String.valueOf(totalUnderagedCount));
                underagedMembers.setText(String.valueOf(activeUnderagedCount));

                System.out.println("Loaded underaged data - Total: " + totalUnderagedCount + ", Active: " + activeUnderagedCount);
            } else {
                // Fallback values if DAO is null
                totalUnderaged.setText("0");
                underagedMembers.setText("0");
                System.out.println("UnderagedDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading underaged data: " + e.getMessage());
            e.printStackTrace();
            totalUnderaged.setText("0");
            underagedMembers.setText("0");
        }
    }

    /**
     * Load workshops data and update UI
     */
    private void loadWorkshopsData() {
        try {
            if (workshopDAO != null) {
                java.util.List<Workshop> allWorkshops = workshopDAO.getAllWorkshops();
                int totalWorkshopsCount = allWorkshops.size();

                // Count active workshops (currently running)
                java.util.List<Workshop> currentlyActiveWorkshops = workshopDAO.getActiveWorkshops();
                int activeWorkshopsCount = currentlyActiveWorkshops.size();

                // Update UI labels
                totalWorkshops.setText(String.valueOf(totalWorkshopsCount));
                activeWorkshops.setText(String.valueOf(activeWorkshopsCount));

                System.out.println("Loaded workshops data - Total: " + totalWorkshopsCount + ", Active: " + activeWorkshopsCount);
            } else {
                // Fallback values if DAO is null
                totalWorkshops.setText("0");
                activeWorkshops.setText("0");
                System.out.println("WorkshopDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading workshops data: " + e.getMessage());
            e.printStackTrace();
            totalWorkshops.setText("0");
            activeWorkshops.setText("0");
        }
    }

    /**
     * Load teachers data and update UI
     */
    private void loadTeachersData() {
        try {
            if (teacherDAO != null) {
                java.util.List<Teacher> allTeachers = teacherDAO.getAllTeachers();
                int totalTeachersCount = allTeachers.size();

                // Count active teachers (teachers assigned to workshops)
                int activeTeachersCount = getActiveTeachersCount();

                // Update UI labels
                totalTeachers.setText(String.valueOf(totalTeachersCount));


                System.out.println("Loaded teachers data - Total: " + totalTeachersCount + ", Active: " + activeTeachersCount);
            } else {
                // Fallback values if DAO is null
                totalTeachers.setText("0");

                System.out.println("TeacherDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading teachers data: " + e.getMessage());
            e.printStackTrace();
            totalTeachers.setText("0");

        }
    }

    /**
     * Helper method to count active teachers (those assigned to workshops)
     */
    private int getActiveTeachersCount() {
        try (Connection conn = smallbusinessbuddycrm.database.DatabaseConnection.getConnection()) {
            String query = """
                SELECT COUNT(DISTINCT w.teacher_id) as active_count 
                FROM workshops w 
                WHERE w.teacher_id IS NOT NULL
                """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                if (rs.next()) {
                    return rs.getInt("active_count");
                }
            }
        } catch (Exception e) {
            System.err.println("Error counting active teachers: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Load lists data and update UI
     */
    private void loadListsData() {
        try {
            if (listsDAO != null) {
                java.util.ArrayList<smallbusinessbuddycrm.model.List> allLists = listsDAO.getAllActiveLists();
                int totalListsCount = allLists.size();

                // Count lists that have contacts (non-empty lists)
                long activeListsCount = allLists.stream()
                        .filter(list -> list.getListSize() > 0)
                        .count();

                // Update UI labels
                totalLists.setText(String.valueOf(totalListsCount));
                activeLists.setText(String.valueOf(activeListsCount));

                System.out.println("Loaded lists data - Total: " + totalListsCount + ", Active: " + activeListsCount);
            } else {
                // Fallback values if DAO is null
                totalLists.setText("0");
                activeLists.setText("0");
                System.out.println("ListsDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading lists data: " + e.getMessage());
            e.printStackTrace();
            totalLists.setText("0");
            activeLists.setText("0");
        }
    }

    /**
     * Set all default values on error
     */
    private void setDefaultValues() {
        totalContacts.setText("0");
        activeMembers.setText("0");
        totalUnderaged.setText("0");
        underagedMembers.setText("0");
        totalWorkshops.setText("0");
        activeWorkshops.setText("0");
        totalTeachers.setText("0");
        totalLists.setText("0");
        activeLists.setText("0");
    }

    // Button action handlers

    @FXML
    private void handleContactsReportAction() {
        navigateTo("/views/reporting/contacts-report.fxml");
    }

    @FXML
    private void handleUnderagedReportAction() {
        navigateTo("/views/underaged-report.fxml");
    }

    @FXML
    private void handleWorkshopsReportAction() {
        navigateTo("/views/workshops-report.fxml");
    }

    @FXML
    private void handleTeachersReportAction() {
        navigateTo("/views/teachers-report.fxml");
    }

    @FXML
    private void handleListsReportAction() {
        navigateTo("/views/ListsReport.fxml");
    }

    /**
     * Navigate to a different view
     * @param fxmlPath Path to the FXML file
     */
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Clear current content and add the new view
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            } else {
                System.err.println("ContentArea is null - make sure it's defined in FXML and injected");
            }

        } catch (IOException e) {
            System.err.println("Error navigating to: " + fxmlPath);
            e.printStackTrace();
            // Handle the error appropriately in your application
        }
    }

    /**
     * Refresh all dashboard data
     */
    public void refreshDashboard() {
        System.out.println("Refreshing dashboard data...");
        loadDashboardData();
    }
}