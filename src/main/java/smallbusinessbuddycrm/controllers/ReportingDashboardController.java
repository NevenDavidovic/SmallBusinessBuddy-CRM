package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import smallbusinessbuddycrm.utilities.LanguageManager;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.database.WorkshopDAO;
import smallbusinessbuddycrm.database.TeacherDAO;
import smallbusinessbuddycrm.database.ListsDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.model.Workshop;
import smallbusinessbuddycrm.model.Teacher;
import smallbusinessbuddycrm.model.List;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportingDashboardController implements Initializable {

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

    // FXML injected labels for text content
    @FXML private Label analyticsOverviewTitle;
    @FXML private Label realtimeInsightsSubtitle;
    @FXML private Label contactsLabel;
    @FXML private Label pulseMonitorLabel;
    @FXML private Label totalRecordsLabel1;
    @FXML private Label activeMembersLabel1;
    @FXML private Label youthLabel;
    @FXML private Label insightsHubLabel;
    @FXML private Label totalRecordsLabel2;
    @FXML private Label activeMembersLabel2;
    @FXML private Label workshopsLabel;
    @FXML private Label activityPulseLabel;
    @FXML private Label totalRecordsLabel3;
    @FXML private Label activeSessionsLabel;
    @FXML private Label teachersLabel;
    @FXML private Label educatorHubLabel;
    @FXML private Label totalRecordsLabel4;
    @FXML private Label listsLabel;
    @FXML private Label dataManagerLabel;
    @FXML private Label totalRecordsLabel5;
    @FXML private Label activeListsLabel;
    @FXML private Label supportLabel;
    @FXML private Label helpCenterLabel;
    @FXML private Label reportsManualLabel;
    @FXML private Label howToUseReportsLabel;
    @FXML private Label usefulInfoLabel;
    @FXML private Label contactsWorkshopsLabel;
    @FXML private Label viewHelpButton;
    @FXML private Label exportDataButton;
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label dataSyncLabel;

    // FXML injected content area for navigation
    @FXML private VBox contentArea;

    // DAO instances
    private ContactDAO contactDAO;
    private UnderagedDAO underagedDAO;
    private WorkshopDAO workshopDAO;
    private TeacherDAO teacherDAO;
    private ListsDAO listsDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAO instances
        initializeDAOs();

        // Load and display dashboard data
        loadDashboardData();

        // Update last updated time
        updateLastUpdatedTime();

        // Set up language support
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Header section
        if (analyticsOverviewTitle != null) {
            analyticsOverviewTitle.setText(languageManager.getText("dashboard.analytics.overview"));
        }
        if (realtimeInsightsSubtitle != null) {
            realtimeInsightsSubtitle.setText(languageManager.getText("dashboard.realtime.insights"));
        }

        // Contacts card
        if (contactsLabel != null) contactsLabel.setText(languageManager.getText("dashboard.contacts"));
        if (pulseMonitorLabel != null) pulseMonitorLabel.setText(languageManager.getText("dashboard.pulse.monitor"));
        if (totalRecordsLabel1 != null) totalRecordsLabel1.setText(languageManager.getText("dashboard.total.records"));
        if (activeMembersLabel1 != null) activeMembersLabel1.setText(languageManager.getText("dashboard.active.members"));
        if (contactsReportButton != null) contactsReportButton.setText(languageManager.getText("dashboard.view.analytics"));

        // Youth card
        if (youthLabel != null) youthLabel.setText(languageManager.getText("dashboard.youth"));
        if (insightsHubLabel != null) insightsHubLabel.setText(languageManager.getText("dashboard.insights.hub"));
        if (totalRecordsLabel2 != null) totalRecordsLabel2.setText(languageManager.getText("dashboard.total.records"));
        if (activeMembersLabel2 != null) activeMembersLabel2.setText(languageManager.getText("dashboard.active.members"));
        if (underagedReportButton != null) underagedReportButton.setText(languageManager.getText("dashboard.view.analytics"));

        // Workshops card
        if (workshopsLabel != null) workshopsLabel.setText(languageManager.getText("dashboard.workshops"));
        if (activityPulseLabel != null) activityPulseLabel.setText(languageManager.getText("dashboard.activity.pulse"));
        if (totalRecordsLabel3 != null) totalRecordsLabel3.setText(languageManager.getText("dashboard.total.records"));
        if (activeSessionsLabel != null) activeSessionsLabel.setText(languageManager.getText("dashboard.active.sessions"));
        if (workshopsReportButton != null) workshopsReportButton.setText(languageManager.getText("dashboard.view.analytics"));

        // Teachers card
        if (teachersLabel != null) teachersLabel.setText(languageManager.getText("dashboard.teachers"));
        if (educatorHubLabel != null) educatorHubLabel.setText(languageManager.getText("dashboard.educator.hub"));
        if (totalRecordsLabel4 != null) totalRecordsLabel4.setText(languageManager.getText("dashboard.total.records"));
        if (teachersReportButton != null) teachersReportButton.setText(languageManager.getText("dashboard.view.analytics"));

        // Lists card
        if (listsLabel != null) listsLabel.setText(languageManager.getText("dashboard.lists"));
        if (dataManagerLabel != null) dataManagerLabel.setText(languageManager.getText("dashboard.data.manager"));
        if (totalRecordsLabel5 != null) totalRecordsLabel5.setText(languageManager.getText("dashboard.total.records"));
        if (activeListsLabel != null) activeListsLabel.setText(languageManager.getText("dashboard.active.lists"));
        if (listsReportButton != null) listsReportButton.setText(languageManager.getText("dashboard.view.analytics"));

        // Support card
        if (supportLabel != null) supportLabel.setText(languageManager.getText("dashboard.support"));
        if (helpCenterLabel != null) helpCenterLabel.setText(languageManager.getText("dashboard.help.center"));
        if (reportsManualLabel != null) reportsManualLabel.setText(languageManager.getText("dashboard.reports.manual"));
        if (howToUseReportsLabel != null) howToUseReportsLabel.setText(languageManager.getText("dashboard.how.to.use.reports"));
        if (usefulInfoLabel != null) usefulInfoLabel.setText(languageManager.getText("dashboard.useful.information"));
        if (contactsWorkshopsLabel != null) contactsWorkshopsLabel.setText(languageManager.getText("dashboard.contacts.workshops.etc"));
        if (viewHelpButton != null) viewHelpButton.setText(languageManager.getText("dashboard.view.help"));
        if (exportDataButton != null) exportDataButton.setText(languageManager.getText("dashboard.export.data"));

        // Footer
        if (systemStatusLabel != null) systemStatusLabel.setText(languageManager.getText("dashboard.all.systems.operational"));
        if (lastUpdatedLabel != null) lastUpdatedLabel.setText(languageManager.getText("dashboard.last.updated.now"));
        if (dataSyncLabel != null) dataSyncLabel.setText(languageManager.getText("dashboard.data.sync.active"));

        System.out.println("Dashboard texts updated");
    }

    private void updateLastUpdatedTime() {
        if (lastUpdatedLabel != null) {
            String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lastUpdatedLabel.setText("Last Updated: " + formattedTime);
        }
    }

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

    private void loadContactsData() {
        try {
            if (contactDAO != null) {
                java.util.List<Contact> allContacts = contactDAO.getAllContacts();
                int totalContactsCount = allContacts.size();
                long activeMembersCount = allContacts.stream()
                        .filter(Contact::isMember)
                        .count();

                totalContacts.setText(String.valueOf(totalContactsCount));
                activeMembers.setText(String.valueOf(activeMembersCount));

                System.out.println("Loaded contacts data - Total: " + totalContactsCount + ", Active: " + activeMembersCount);
            } else {
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

    private void loadUnderagedData() {
        try {
            if (underagedDAO != null) {
                java.util.List<UnderagedMember> allUnderaged = underagedDAO.getAllUnderagedMembers();
                int totalUnderagedCount = allUnderaged.size();
                long activeUnderagedCount = allUnderaged.stream()
                        .filter(UnderagedMember::isMember)
                        .count();

                totalUnderaged.setText(String.valueOf(totalUnderagedCount));
                underagedMembers.setText(String.valueOf(activeUnderagedCount));

                System.out.println("Loaded underaged data - Total: " + totalUnderagedCount + ", Active: " + activeUnderagedCount);
            } else {
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

    private void loadWorkshopsData() {
        try {
            if (workshopDAO != null) {
                java.util.List<Workshop> allWorkshops = workshopDAO.getAllWorkshops();
                int totalWorkshopsCount = allWorkshops.size();
                java.util.List<Workshop> currentlyActiveWorkshops = workshopDAO.getActiveWorkshops();
                int activeWorkshopsCount = currentlyActiveWorkshops.size();

                totalWorkshops.setText(String.valueOf(totalWorkshopsCount));
                activeWorkshops.setText(String.valueOf(activeWorkshopsCount));

                System.out.println("Loaded workshops data - Total: " + totalWorkshopsCount + ", Active: " + activeWorkshopsCount);
            } else {
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

    private void loadTeachersData() {
        try {
            if (teacherDAO != null) {
                java.util.List<Teacher> allTeachers = teacherDAO.getAllTeachers();
                int totalTeachersCount = allTeachers.size();

                totalTeachers.setText(String.valueOf(totalTeachersCount));

                System.out.println("Loaded teachers data - Total: " + totalTeachersCount);
            } else {
                totalTeachers.setText("0");
                System.out.println("TeacherDAO is null, using fallback values");
            }
        } catch (Exception e) {
            System.err.println("Error loading teachers data: " + e.getMessage());
            e.printStackTrace();
            totalTeachers.setText("0");
        }
    }

    private void loadListsData() {
        try {
            if (listsDAO != null) {
                java.util.List<List> allLists = listsDAO.getAllActiveLists();
                int totalListsCount = allLists.size();
                long activeListsCount = allLists.stream()
                        .filter(list -> list.getListSize() > 0)
                        .count();

                totalLists.setText(String.valueOf(totalListsCount));
                activeLists.setText(String.valueOf(activeListsCount));

                System.out.println("Loaded lists data - Total: " + totalListsCount + ", Active: " + activeListsCount);
            } else {
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

    @FXML
    private void handleContactReportingScreen() {
        navigateTo("/views/reporting/contacts-report.fxml");
    }

    @FXML
    private void handleHelpReportingScreen() {
        navigateTo("/views/general/help-view.fxml");
    }

    @FXML
    private void handleUnderagedReportingScreen() {
        navigateTo("/views/reporting/underaged-report.fxml");
    }

    @FXML
    private void handleWorkshopReportingScreen() {
        navigateTo("/views/reporting/workshops-report.fxml");
    }

    @FXML
    private void handleTeachersReportAction() {
        navigateTo("/views/reporting/teachers-report.fxml");
    }

    @FXML
    private void handleListReportingScreen() {
        navigateTo("/views/reporting/lists-report.fxml");
    }

    public void navigateTo(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node view = loader.load();

            if (contentArea == null) {
                System.err.println("ContentArea is null - make sure it's defined in FXML with fx:id=\"contentArea\"");
                return;
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("Navigated to: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error navigating to: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshDashboard() {
        System.out.println("Refreshing dashboard data...");
        loadDashboardData();
        updateLastUpdatedTime();
    }
}