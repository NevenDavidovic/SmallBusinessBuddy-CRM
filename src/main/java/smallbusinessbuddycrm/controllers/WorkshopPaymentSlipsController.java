package smallbusinessbuddycrm.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import smallbusinessbuddycrm.database.*;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.utilities.UplatnicaHtmlGenerator;
import smallbusinessbuddycrm.utilities.TemplateProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WorkshopPaymentSlipsController implements Initializable {

    // FXML Components
    @FXML private ComboBox<Workshop> workshopComboBox;
    @FXML private Button loadParticipantsButton;
    @FXML private VBox workshopDetailsContainer;
    @FXML private Label workshopNameLabel;
    @FXML private Label workshopDatesLabel;
    @FXML private Label workshopTeacherLabel;
    @FXML private Label participantCountLabel;

    @FXML private ComboBox<PaymentTemplate> paymentTemplateComboBox;
    @FXML private VBox templateDetailsContainer;
    @FXML private Label templateNameLabel;
    @FXML private Label templateAmountLabel;
    @FXML private Label templateModelLabel;
    @FXML private Label templateDescriptionLabel;

    @FXML private CheckBox selectAllParticipantsCheckBox;
    @FXML private ComboBox<String> participantTypeFilterComboBox;
    @FXML private ComboBox<String> paymentStatusFilterComboBox;
    @FXML private Label selectedCountLabel;
    @FXML private ListView<WorkshopParticipantItem> participantsListView;
    @FXML private VBox noParticipantsContainer;

    @FXML private Button previewSelectedButton;
    @FXML private Button previewAllButton;
    @FXML private Button generateAllButton;
    @FXML private Button exportSelectedButton;

    @FXML private VBox progressContainer;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressDetailsLabel;

    // Data
    private WorkshopDAO workshopDAO;
    private WorkshopParticipantDAO participantDAO;
    private PaymentTemplateDAO paymentTemplateDAO;
    private TeacherDAO teacherDAO;
    private ContactDAO contactDAO;
    private UnderagedDAO underagedDAO;
    private OrganizationDAO organizationDAO;

    private ObservableList<Workshop> workshops;
    private ObservableList<PaymentTemplate> paymentTemplates;
    private ObservableList<WorkshopParticipantItem> allParticipants;
    private ObservableList<WorkshopParticipantItem> filteredParticipants;

    private Organization organization;
    private Workshop selectedWorkshop;
    private PaymentTemplate selectedPaymentTemplate;

    // Preview data storage
    private Map<WorkshopParticipantItem, String> participantUplatnicaHtmlMap = new HashMap<>();
    private Map<WorkshopParticipantItem, BufferedImage> participantBarcodeImageMap = new HashMap<>();

    // Constants
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🎓 Initializing Workshop Payment Slips Controller");

        initializeDAOs();
        loadOrganizationData();
        setupComboBoxes();
        setupParticipantsList();
        loadInitialData();

        System.out.println("✅ Workshop Payment Slips Controller initialized successfully");
    }

    private void initializeDAOs() {
        workshopDAO = new WorkshopDAO();
        participantDAO = new WorkshopParticipantDAO();
        paymentTemplateDAO = new PaymentTemplateDAO();
        teacherDAO = new TeacherDAO();
        contactDAO = new ContactDAO();
        underagedDAO = new UnderagedDAO();
        organizationDAO = new OrganizationDAO();
    }

    private void loadOrganizationData() {
        try {
            Optional<Organization> orgOptional = organizationDAO.getFirst();
            if (orgOptional.isPresent()) {
                organization = orgOptional.get();
                System.out.println("✅ Loaded organization: " + organization.getName());
            } else {
                System.out.println("⚠️ No organization found, creating default");
                createDefaultOrganization();
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading organization: " + e.getMessage());
            e.printStackTrace();
            createDefaultOrganization();
        }
    }

    private void createDefaultOrganization() {
        organization = new Organization();
        organization.setName("Your Company Name");
        organization.setIban("HR1234567890123456789");
        organization.setStreetName("Your Street");
        organization.setStreetNum("123");
        organization.setPostalCode("10000");
        organization.setCity("Zagreb");
        organization.setEmail("info@yourcompany.com");
        organization.setPhoneNum("+385 1 234 5678");
    }

    private void setupComboBoxes() {
        // Setup workshop combo box
        workshops = FXCollections.observableArrayList();
        workshopComboBox.setItems(workshops);
        workshopComboBox.setConverter(new StringConverter<Workshop>() {
            @Override
            public String toString(Workshop workshop) {
                if (workshop == null) return "";
                return workshop.getName() + " (" + workshop.getDateRange() + ")";
            }

            @Override
            public Workshop fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        workshopComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedWorkshop = newVal;
            updateWorkshopDetails();
            clearParticipants();
            updateButtonStates();
        });

        // Setup payment template combo box
        paymentTemplates = FXCollections.observableArrayList();
        paymentTemplateComboBox.setItems(paymentTemplates);
        paymentTemplateComboBox.setConverter(new StringConverter<PaymentTemplate>() {
            @Override
            public String toString(PaymentTemplate template) {
                if (template == null) return "";
                return template.getName() + " (" + template.getAmount() + " EUR)";
            }

            @Override
            public PaymentTemplate fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        paymentTemplateComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPaymentTemplate = newVal;
            updatePaymentTemplateDetails();
            updateButtonStates();
        });
    }

    private void setupParticipantsList() {
        allParticipants = FXCollections.observableArrayList();
        filteredParticipants = FXCollections.observableArrayList();
        participantsListView.setItems(filteredParticipants);

        // Initialize filter combo boxes
        participantTypeFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "ADULT", "CHILD"));
        participantTypeFilterComboBox.setValue("ALL");

        paymentStatusFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "PENDING", "PAID", "REFUNDED", "CANCELLED"));
        paymentStatusFilterComboBox.setValue("PENDING");

        // Custom cell factory for participant items
        participantsListView.setCellFactory(listView -> new WorkshopParticipantListCell());

        // Setup filter change listeners
        participantTypeFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterParticipants();
        });

        paymentStatusFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterParticipants();
        });

        // Select all checkbox
        selectAllParticipantsCheckBox.setOnAction(e -> handleSelectAllParticipants());
    }

    private void loadInitialData() {
        loadWorkshops();
        loadPaymentTemplates();
    }

    private void loadWorkshops() {
        try {
            List<Workshop> workshopList = workshopDAO.getAllWorkshops();
            workshops.clear();
            workshops.addAll(workshopList);
            System.out.println("✅ Loaded " + workshops.size() + " workshops");
        } catch (Exception e) {
            System.err.println("❌ Error loading workshops: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Loading Error", "Failed to load workshops: " + e.getMessage());
        }
    }

    private void loadPaymentTemplates() {
        try {
            List<PaymentTemplate> templateList = paymentTemplateDAO.getAllPaymentTemplates();
            paymentTemplates.clear();
            paymentTemplates.addAll(templateList);
            System.out.println("✅ Loaded " + paymentTemplates.size() + " payment templates");
        } catch (Exception e) {
            System.err.println("❌ Error loading payment templates: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Loading Error", "Failed to load payment templates: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadParticipants() {
        if (selectedWorkshop == null) {
            showAlert(Alert.AlertType.WARNING, "No Workshop Selected", "Please select a workshop first.");
            return;
        }

        loadParticipantsButton.setDisable(true);
        progressContainer.setVisible(true);
        progressLabel.setText("Loading participants...");

        // Unbind progress bar before setting value manually
        progressBar.progressProperty().unbind();
        progressBar.setProgress(-1); // Indeterminate

        Task<List<Map<String, Object>>> loadTask = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                updateMessage("Fetching workshop participants...");
                return participantDAO.getWorkshopParticipantsWithDetails(selectedWorkshop.getId());
            }

            @Override
            protected void succeeded() {
                List<Map<String, Object>> participantData = getValue();
                loadParticipantsData(participantData);
                loadParticipantsButton.setDisable(false);
                progressContainer.setVisible(false);
                System.out.println("✅ Successfully loaded " + participantData.size() + " participants");
            }

            @Override
            protected void failed() {
                loadParticipantsButton.setDisable(false);
                progressContainer.setVisible(false);
                Throwable exception = getException();
                System.err.println("❌ Failed to load participants: " + exception.getMessage());
                exception.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load participants: " + exception.getMessage());
            }
        };

        progressDetailsLabel.textProperty().bind(loadTask.messageProperty());

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadParticipantsData(List<Map<String, Object>> participantDataList) {
        allParticipants.clear();

        for (Map<String, Object> data : participantDataList) {
            WorkshopParticipantItem item = new WorkshopParticipantItem(data);
            allParticipants.add(item);
        }

        filterParticipants();
        updateParticipantCount();
        updateButtonStates();

        // Show participants list, hide placeholder
        if (allParticipants.isEmpty()) {
            noParticipantsContainer.setVisible(true);
            participantsListView.setVisible(false);
        } else {
            noParticipantsContainer.setVisible(false);
            participantsListView.setVisible(true);
        }
    }

    @FXML
    private void onFilterParticipants() {
        filterParticipants();
    }

    @FXML
    private void onSelectAllParticipants() {
        handleSelectAllParticipants();
    }

    private void handleSelectAllParticipants() {
        boolean selectAll = selectAllParticipantsCheckBox.isSelected();

        for (WorkshopParticipantItem item : filteredParticipants) {
            item.setSelected(selectAll);
        }

        participantsListView.refresh();
        updateSelectedCount();
        updateButtonStates();
    }

    private void filterParticipants() {
        String typeFilter = participantTypeFilterComboBox.getValue();
        String statusFilter = paymentStatusFilterComboBox.getValue();

        filteredParticipants.clear();

        for (WorkshopParticipantItem item : allParticipants) {
            boolean typeMatch = "ALL".equals(typeFilter) || typeFilter.equals(item.getParticipantType());
            boolean statusMatch = "ALL".equals(statusFilter) || statusFilter.equals(item.getPaymentStatus());

            if (typeMatch && statusMatch) {
                filteredParticipants.add(item);
            }
        }

        updateSelectedCount();
        updateButtonStates();
    }

    @FXML
    private void onPreviewSelected() {
        List<WorkshopParticipantItem> selectedItems = getSelectedParticipants();

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one participant.");
            return;
        }

        if (selectedPaymentTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "No Template Selected", "Please select a payment template.");
            return;
        }

        generatePreviewsInSameWindow(selectedItems);
    }

    @FXML
    private void onPreviewAll() {
        if (selectedPaymentTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "No Template Selected", "Please select a payment template.");
            return;
        }

        if (filteredParticipants.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Participants", "Please load participants first.");
            return;
        }

        // Preview all filtered participants (not just selected ones)
        List<WorkshopParticipantItem> allFilteredParticipants = new ArrayList<>(filteredParticipants);
        generatePreviewsInSameWindow(allFilteredParticipants);
    }

    @FXML
    private void onGenerateAll() {
        List<WorkshopParticipantItem> selectedItems = getSelectedParticipants();

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one participant.");
            return;
        }

        if (selectedPaymentTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "No Template Selected", "Please select a payment template.");
            return;
        }

        showBulkGenerationDialog(selectedItems);
    }

    @FXML
    private void onExportSelected() {
        List<WorkshopParticipantItem> selectedItems = getSelectedParticipants();

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one participant.");
            return;
        }

        if (selectedPaymentTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "No Template Selected", "Please select a payment template.");
            return;
        }

        exportSelectedParticipants(selectedItems);
    }

    private void updateWorkshopDetails() {
        if (selectedWorkshop == null) {
            workshopDetailsContainer.setVisible(false);
            return;
        }

        workshopNameLabel.setText("Workshop: " + selectedWorkshop.getName());
        workshopDatesLabel.setText("Dates: " + selectedWorkshop.getDateRange());

        // Load teacher information using the same pattern as MultipleGenerationBarcodeDialog
        String teacherInfo = "Teacher: ";
        if (selectedWorkshop.getTeacherId() != null) {
            try {
                // Use the existing DAO method pattern
                List<Teacher> allTeachers = teacherDAO.getAllTeachers();
                Teacher teacher = allTeachers.stream()
                        .filter(t -> t.getId() == selectedWorkshop.getTeacherId())
                        .findFirst()
                        .orElse(null);

                if (teacher != null) {
                    teacherInfo += teacher.getFirstName() + " " + teacher.getLastName();
                } else {
                    teacherInfo += "Unknown (ID: " + selectedWorkshop.getTeacherId() + ")";
                }
            } catch (Exception e) {
                teacherInfo += "Error loading teacher";
            }
        } else {
            teacherInfo += "Not assigned";
        }
        workshopTeacherLabel.setText(teacherInfo);

        workshopDetailsContainer.setVisible(true);
    }

    private void updatePaymentTemplateDetails() {
        if (selectedPaymentTemplate == null) {
            templateDetailsContainer.setVisible(false);
            return;
        }

        templateNameLabel.setText("Template: " + selectedPaymentTemplate.getName());
        templateAmountLabel.setText("Amount: " + selectedPaymentTemplate.getAmount() + " EUR");
        templateModelLabel.setText("Payment Model: " +
                (selectedPaymentTemplate.getModelOfPayment() != null ? selectedPaymentTemplate.getModelOfPayment() : "N/A"));
        templateDescriptionLabel.setText("Description: " +
                (selectedPaymentTemplate.getDescription() != null && !selectedPaymentTemplate.getDescription().trim().isEmpty()
                        ? selectedPaymentTemplate.getDescription() : "N/A"));

        templateDetailsContainer.setVisible(true);
    }

    private void updateParticipantCount() {
        participantCountLabel.setText("Participants: " + allParticipants.size());
    }

    private void updateSelectedCount() {
        long selectedCount = filteredParticipants.stream().mapToLong(item -> item.isSelected() ? 1 : 0).sum();
        selectedCountLabel.setText("Selected: " + selectedCount + " / " + filteredParticipants.size());
    }

    private void clearParticipants() {
        allParticipants.clear();
        filteredParticipants.clear();
        participantUplatnicaHtmlMap.clear();
        participantBarcodeImageMap.clear();
        noParticipantsContainer.setVisible(true);
        participantsListView.setVisible(false);
        updateSelectedCount();
    }

    private void updateButtonStates() {
        boolean hasWorkshop = selectedWorkshop != null;
        boolean hasTemplate = selectedPaymentTemplate != null;
        boolean hasParticipants = !allParticipants.isEmpty();
        boolean hasSelection = getSelectedParticipants().size() > 0;

        // Debug logging
        System.out.println("🔍 Button States Debug:");
        System.out.println("  hasWorkshop: " + hasWorkshop);
        System.out.println("  hasTemplate: " + hasTemplate);
        System.out.println("  hasParticipants: " + hasParticipants + " (total: " + allParticipants.size() + ")");
        System.out.println("  hasSelection: " + hasSelection + " (selected: " + getSelectedParticipants().size() + ")");

        loadParticipantsButton.setDisable(!hasWorkshop);

        // Enable buttons when we have template, participants, and selection
        boolean enableButtons = hasTemplate && hasParticipants && hasSelection;
        previewSelectedButton.setDisable(!enableButtons);
        generateAllButton.setDisable(!enableButtons);
        exportSelectedButton.setDisable(!enableButtons);

        // Preview All button only needs template and participants (no selection required)
        boolean enablePreviewAll = hasTemplate && hasParticipants;
        previewAllButton.setDisable(!enablePreviewAll);

        System.out.println("  Buttons enabled: preview=" + enableButtons +
                ", previewAll=" + enablePreviewAll +
                ", generate=" + enableButtons +
                ", export=" + enableButtons);
    }

    private List<WorkshopParticipantItem> getSelectedParticipants() {
        return filteredParticipants.stream()
                .filter(WorkshopParticipantItem::isSelected)
                .collect(Collectors.toList());
    }

    private void generatePreviewsInSameWindow(List<WorkshopParticipantItem> participants) {
        // Clear existing previews
        participantUplatnicaHtmlMap.clear();
        participantBarcodeImageMap.clear();

        // Show progress and disable buttons
        progressContainer.setVisible(true);
        progressLabel.setText("Generating previews...");

        // Unbind progress bar before setting value manually
        progressBar.progressProperty().unbind();
        progressBar.setProgress(-1); // Indeterminate

        previewSelectedButton.setDisable(true);
        previewAllButton.setDisable(true);

        Task<List<String>> previewTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> htmlPreviews = new ArrayList<>();
                int current = 0;
                int total = participants.size();

                for (WorkshopParticipantItem participant : participants) {
                    updateMessage("Generating preview for: " + participant.getParticipantName());

                    try {
                        String uplatnicaHtml = generateUplatnicaForParticipant(participant);
                        htmlPreviews.add(uplatnicaHtml);

                        // Store for later use
                        participantUplatnicaHtmlMap.put(participant, uplatnicaHtml);

                    } catch (Exception e) {
                        System.err.println("Error generating preview for participant: " + e.getMessage());
                        htmlPreviews.add("<html><body><h2>Error generating preview for " +
                                participant.getParticipantName() + "</h2></body></html>");
                    }

                    current++;
                    updateProgress(current, total);
                }

                return htmlPreviews;
            }

            @Override
            protected void succeeded() {
                List<String> htmlPreviews = getValue();
                showPreviewsInMainWindow(participants, htmlPreviews);
                progressContainer.setVisible(false);
                updateButtonStates(); // Re-enable buttons
                System.out.println("✅ Successfully generated " + htmlPreviews.size() + " previews");
            }

            @Override
            protected void failed() {
                progressContainer.setVisible(false);
                updateButtonStates(); // Re-enable buttons
                Throwable exception = getException();
                showAlert(Alert.AlertType.ERROR, "Preview Error",
                        "Failed to generate previews: " + exception.getMessage());
            }
        };

        progressDetailsLabel.textProperty().bind(previewTask.messageProperty());
        progressBar.progressProperty().bind(previewTask.progressProperty());

        Thread thread = new Thread(previewTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showPreviewsInMainWindow(List<WorkshopParticipantItem> participants, List<String> htmlPreviews) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initOwner(workshopComboBox.getScene().getWindow());
        previewStage.setTitle("🔍 Payment Slips Preview - " + participants.size() + " participants");
        previewStage.setResizable(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        Label titleLabel = new Label("🔍 Croatian Uplatnica Previews");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #c2185b;");

        VBox allPreviewsContainer = new VBox(20);
        allPreviewsContainer.setPadding(new Insets(10));

        for (int i = 0; i < participants.size() && i < htmlPreviews.size(); i++) {
            WorkshopParticipantItem participant = participants.get(i);
            String uplatnicaHtml = htmlPreviews.get(i);
            addParticipantPreviewToContainer(allPreviewsContainer, participant, uplatnicaHtml);
        }

        ScrollPane allPreviewsScrollPane = new ScrollPane(allPreviewsContainer);
        allPreviewsScrollPane.setPrefHeight(500);
        allPreviewsScrollPane.setPrefWidth(Double.MAX_VALUE);
        allPreviewsScrollPane.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-radius: 5;");
        allPreviewsScrollPane.setFitToWidth(true);
        allPreviewsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        allPreviewsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button closeButton = new Button("❌ Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4;");
        closeButton.setOnAction(e -> previewStage.close());

        Button clearButton = new Button("🗑️ Clear Previews");
        clearButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        clearButton.setOnAction(e -> {
            allPreviewsContainer.getChildren().clear();
            participantUplatnicaHtmlMap.clear();
        });

        buttonBox.getChildren().addAll(clearButton, closeButton);
        mainLayout.getChildren().addAll(titleLabel, allPreviewsScrollPane, buttonBox);

        ScrollPane mainScrollPane = new ScrollPane(mainLayout);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(mainScrollPane, 1200, 700);
        previewStage.setScene(scene);
        previewStage.showAndWait();
    }

    private void addParticipantPreviewToContainer(VBox container, WorkshopParticipantItem participant, String uplatnicaHtml) {
        VBox participantPreviewContainer = new VBox(10);
        participantPreviewContainer.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-color: white; -fx-padding: 15;");

        String headerText = participant.getTypeIcon() + " " + participant.getParticipantName() +
                " (" + participant.getParticipantType() + ")";

        if ("CHILD".equals(participant.getParticipantType())) {
            String parentName = (String) participant.getParticipantData().get("parent_name");
            if (parentName != null && !parentName.trim().isEmpty()) {
                headerText += " 👶 (Parent: " + parentName + ")";
            }
        }

        Label participantHeader = new Label(headerText);
        participantHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        participantHeader.setStyle("CHILD".equals(participant.getParticipantType()) ?
                "-fx-text-fill: #ff6b35;" : "-fx-text-fill: #495057;");

        StringBuilder participantInfo = new StringBuilder();
        String email = participant.getParticipantEmail();
        if (!email.isEmpty()) {
            participantInfo.append("📧 ").append(email).append("  ");
        }
        String phone = participant.getParticipantPhone();
        if (!phone.isEmpty()) {
            participantInfo.append("📞 ").append(phone);
        }

        if ("CHILD".equals(participant.getParticipantType())) {
            Object age = participant.getParticipantData().get("participant_age");
            if (age != null) {
                participantInfo.append("\n👶 Child Details: Age ").append(age);
            }
        }

        Label participantInfoLabel = new Label(participantInfo.toString());
        participantInfoLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        webView.setPrefHeight(500);
        webView.setPrefWidth(Double.MAX_VALUE);
        webView.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 1; -fx-border-radius: 4;");
        webView.getEngine().loadContent(uplatnicaHtml);

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button printIndividualButton = new Button("🖨️ Print This");
        printIndividualButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
        printIndividualButton.setOnAction(e -> printIndividualUplatnica(webView));

        Button saveIndividualButton = new Button("💾 Save This");
        saveIndividualButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px;");
        saveIndividualButton.setOnAction(e -> saveIndividualUplatnica(participant));

        actionButtons.getChildren().addAll(printIndividualButton, saveIndividualButton);

        participantPreviewContainer.getChildren().addAll(participantHeader, participantInfoLabel, webView, actionButtons);
        container.getChildren().add(participantPreviewContainer);
    }

    private void printIndividualUplatnica(javafx.scene.web.WebView webView) {
        try {
            javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(workshopComboBox.getScene().getWindow())) {
                webView.getEngine().print(job);
                job.endJob();
            }
        } catch (Exception e) {
            System.err.println("Error printing individual uplatnica: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Print Error", "Error printing uplatnica: " + e.getMessage());
        }
    }

    private void saveIndividualUplatnica(WorkshopParticipantItem participant) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Save Uplatnica");
        File selectedDirectory = directoryChooser.showDialog(workshopComboBox.getScene().getWindow());

        if (selectedDirectory != null) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String participantName = participant.getParticipantName().replaceAll("[^a-zA-Z0-9_-]", "_");
                String fileName = "workshop_" + selectedWorkshop.getId() + "_" + participantName + "_" + timestamp;

                String uplatnicaHtml = participantUplatnicaHtmlMap.get(participant);
                if (uplatnicaHtml != null) {
                    File htmlFile = new File(selectedDirectory, fileName + ".html");
                    try (FileWriter writer = new FileWriter(htmlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                        writer.write(uplatnicaHtml);
                    }
                }

                BufferedImage barcodeImage = participantBarcodeImageMap.get(participant);
                if (barcodeImage != null) {
                    File imageFile = new File(selectedDirectory, "barcode_" + fileName + ".png");
                    ImageIO.write(barcodeImage, "png", imageFile);
                }

                showAlert(Alert.AlertType.INFORMATION, "Save Complete",
                        "Uplatnica saved successfully for " + participant.getParticipantName());

            } catch (Exception e) {
                System.err.println("Error saving individual uplatnica: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Save Error", "Error saving uplatnica: " + e.getMessage());
            }
        }
    }

    private void showBulkGenerationDialog(List<WorkshopParticipantItem> participants) {
        List<Contact> contacts = new ArrayList<>();

        for (WorkshopParticipantItem participant : participants) {
            try {
                if ("ADULT".equals(participant.getParticipantType())) {
                    Integer contactId = (Integer) participant.getParticipantData().get("contact_id");
                    if (contactId != null) {
                        Contact contact = getContactById(contactId);
                        if (contact != null) {
                            contacts.add(contact);
                        }
                    }
                } else if ("CHILD".equals(participant.getParticipantType())) {
                    Integer parentContactId = (Integer) participant.getParticipantData().get("parent_contact_id");
                    if (parentContactId != null) {
                        Contact parentContact = getContactById(parentContactId);
                        if (parentContact != null) {
                            contacts.add(parentContact);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading contact for participant: " + e.getMessage());
            }
        }

        if (contacts.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load contact information for selected participants.");
            return;
        }

        Stage currentStage = (Stage) workshopComboBox.getScene().getWindow();
        MultipleGenerationBarcodeDialog dialog = new MultipleGenerationBarcodeDialog(
                currentStage, contacts, selectedPaymentTemplate);
        dialog.showAndWait();
    }

    private void exportSelectedParticipants(List<WorkshopParticipantItem> participants) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Export Directory");
        Window window = workshopComboBox.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory != null) {
            exportParticipantsToDirectory(participants, selectedDirectory);
        }
    }

    private void exportParticipantsToDirectory(List<WorkshopParticipantItem> participants, File directory) {
        progressContainer.setVisible(true);
        progressLabel.setText("Exporting payment slips...");

        // Unbind progress bar before setting value manually
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);

        Task<Void> exportTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int total = participants.size();
                int current = 0;

                for (WorkshopParticipantItem participant : participants) {
                    updateMessage("Exporting: " + participant.getParticipantName());

                    try {
                        String uplatnicaHtml = generateUplatnicaForParticipant(participant);
                        BufferedImage barcodeImage = generateBarcodeForParticipant(participant);

                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String fileName = "workshop_" + selectedWorkshop.getId() + "_" +
                                participant.getParticipantName().replaceAll("[^a-zA-Z0-9_-]", "_") +
                                "_" + timestamp;

                        File htmlFile = new File(directory, fileName + ".html");
                        try (FileWriter writer = new FileWriter(htmlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                            writer.write(uplatnicaHtml);
                        }

                        if (barcodeImage != null) {
                            File imageFile = new File(directory, "barcode_" + fileName + ".png");
                            ImageIO.write(barcodeImage, "png", imageFile);
                        }

                    } catch (Exception e) {
                        System.err.println("Error exporting participant " + participant.getParticipantName() + ": " + e.getMessage());
                    }

                    current++;
                    updateProgress(current, total);
                }

                return null;
            }

            @Override
            protected void succeeded() {
                progressContainer.setVisible(false);
                showAlert(Alert.AlertType.INFORMATION, "Export Complete",
                        "Successfully exported " + participants.size() + " payment slips.");
            }

            @Override
            protected void failed() {
                progressContainer.setVisible(false);
                Throwable exception = getException();
                showAlert(Alert.AlertType.ERROR, "Export Error",
                        "Failed to export payment slips: " + exception.getMessage());
            }
        };

        progressDetailsLabel.textProperty().bind(exportTask.messageProperty());
        progressBar.progressProperty().bind(exportTask.progressProperty());

        Thread thread = new Thread(exportTask);
        thread.setDaemon(true);
        thread.start();
    }

    private String generateUplatnicaForParticipant(WorkshopParticipantItem participant) throws WriterException {
        String hub3Data = generateHUB3DataForParticipant(participant);
        BufferedImage barcodeImage = generateBarcodeImageForData(hub3Data);

        Contact contact = null;
        UnderagedMember underagedMember = null;

        if ("ADULT".equals(participant.getParticipantType())) {
            Integer contactId = (Integer) participant.getParticipantData().get("contact_id");
            if (contactId != null) {
                contact = getContactById(contactId);
            }
        } else if ("CHILD".equals(participant.getParticipantType())) {
            Integer underagedId = (Integer) participant.getParticipantData().get("underaged_id");
            Integer parentContactId = (Integer) participant.getParticipantData().get("parent_contact_id");

            if (underagedId != null) {
                underagedMember = getUnderagedMemberById(underagedId);
            }
            if (parentContactId != null) {
                contact = getContactById(parentContactId);
            }
        }

        if (contact == null) {
            throw new RuntimeException("Could not load contact information for participant");
        }

        return UplatnicaHtmlGenerator.generateUplatnicaHtml(
                contact, organization, selectedPaymentTemplate, barcodeImage, underagedMember);
    }

    private BufferedImage generateBarcodeForParticipant(WorkshopParticipantItem participant) throws WriterException {
        String hub3Data = generateHUB3DataForParticipant(participant);
        return generateBarcodeImageForData(hub3Data);
    }

    private String generateHUB3DataForParticipant(WorkshopParticipantItem participant) {
        StringBuilder hub3Data = new StringBuilder();

        hub3Data.append(FIXED_BANK_CODE).append("\n");
        hub3Data.append(FIXED_CURRENCY).append("\n");

        String amountCents = selectedPaymentTemplate.getAmount().multiply(new BigDecimal("100")).toBigInteger().toString();
        hub3Data.append(String.format("%015d", Long.parseLong(amountCents))).append("\n");

        String payerName = participant.getParticipantName();
        if ("CHILD".equals(participant.getParticipantType())) {
            String parentName = (String) participant.getParticipantData().get("parent_name");
            if (parentName != null && !parentName.trim().isEmpty()) {
                payerName = parentName;
            }
        }
        hub3Data.append(payerName).append("\n");

        String payerAddress = "";
        String payerCity = "";

        try {
            Contact payerContact = null;

            if ("ADULT".equals(participant.getParticipantType())) {
                Integer contactId = (Integer) participant.getParticipantData().get("contact_id");
                if (contactId != null) {
                    payerContact = getContactById(contactId);
                }
            } else if ("CHILD".equals(participant.getParticipantType())) {
                Integer parentContactId = (Integer) participant.getParticipantData().get("parent_contact_id");
                if (parentContactId != null) {
                    payerContact = getContactById(parentContactId);
                }
            }

            if (payerContact != null) {
                if (payerContact.getStreetName() != null && !payerContact.getStreetName().trim().isEmpty()) {
                    payerAddress = payerContact.getStreetName();
                    if (payerContact.getStreetNum() != null && !payerContact.getStreetNum().trim().isEmpty()) {
                        payerAddress += " " + payerContact.getStreetNum();
                    }
                }

                if (payerContact.getPostalCode() != null && !payerContact.getPostalCode().trim().isEmpty()) {
                    payerCity = payerContact.getPostalCode();
                    if (payerContact.getCity() != null && !payerContact.getCity().trim().isEmpty()) {
                        payerCity += " " + payerContact.getCity();
                    }
                } else if (payerContact.getCity() != null && !payerContact.getCity().trim().isEmpty()) {
                    payerCity = payerContact.getCity();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading payer contact details: " + e.getMessage());
        }

        hub3Data.append(payerAddress).append("\n");
        hub3Data.append(payerCity).append("\n");

        hub3Data.append(organization.getName()).append("\n");

        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        hub3Data.append(recipientAddress).append("\n");

        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }
        hub3Data.append(recipientCity).append("\n");

        hub3Data.append(organization.getIban()).append("\n");

        hub3Data.append(selectedPaymentTemplate.getModelOfPayment() != null ? selectedPaymentTemplate.getModelOfPayment() : "").append("\n");

        String reference = processReferenceTemplateForParticipant(selectedPaymentTemplate.getPozivNaBroj(), participant);
        hub3Data.append(reference).append("\n");

        hub3Data.append("").append("\n");

        String processedDescription = processDescriptionTemplateForParticipant(selectedPaymentTemplate.getDescription(), participant);
        hub3Data.append(processedDescription);

        return hub3Data.toString();
    }

    private String processReferenceTemplateForParticipant(String referenceTemplate, WorkshopParticipantItem participant) {
        if (referenceTemplate == null || referenceTemplate.trim().isEmpty()) {
            return "";
        }

        String template = referenceTemplate.trim();

        if (template.startsWith("{{") && template.endsWith("}}")) {
            String placeholder = template.substring(2, template.length() - 2);

            try {
                if (placeholder.equals("contact_attributes.pin")) {
                    if ("ADULT".equals(participant.getParticipantType())) {
                        Integer contactId = (Integer) participant.getParticipantData().get("contact_id");
                        if (contactId != null) {
                            Contact contact = getContactById(contactId);
                            return contact != null && contact.getPin() != null ? contact.getPin() : "";
                        }
                    } else if ("CHILD".equals(participant.getParticipantType())) {
                        Integer parentContactId = (Integer) participant.getParticipantData().get("parent_contact_id");
                        if (parentContactId != null) {
                            Contact parentContact = getContactById(parentContactId);
                            return parentContact != null && parentContact.getPin() != null ? parentContact.getPin() : "";
                        }
                    }
                    return "";
                } else if (placeholder.equals("underaged_attributes.pin")) {
                    if ("CHILD".equals(participant.getParticipantType())) {
                        Integer underagedId = (Integer) participant.getParticipantData().get("underaged_id");
                        if (underagedId != null) {
                            UnderagedMember underaged = getUnderagedMemberById(underagedId);
                            return underaged != null && underaged.getPin() != null ? underaged.getPin() : "";
                        }
                    }
                    return "";
                }

                System.out.println("Warning: Unknown reference placeholder '" + placeholder + "', returning empty string");
                return "";
            } catch (Exception e) {
                System.err.println("Error processing reference placeholder: " + e.getMessage());
                return "";
            }
        } else {
            Integer participantId = (Integer) participant.getParticipantData().get("participant_id");
            String processedReference = template.replace("{contact_id}", String.valueOf(participantId != null ? participantId : 0));

            if (processedReference.matches("\\d*")) {
                return processedReference;
            } else {
                System.out.println("Warning: Reference template contains non-numeric characters. Using participant ID as fallback.");
                return String.valueOf(participantId != null ? participantId : 0);
            }
        }
    }

    private String processDescriptionTemplateForParticipant(String template, WorkshopParticipantItem participant) {
        if (template == null || template.trim().isEmpty()) {
            return "";
        }

        try {
            Contact contact = null;
            UnderagedMember underagedMember = null;

            if ("ADULT".equals(participant.getParticipantType())) {
                Integer contactId = (Integer) participant.getParticipantData().get("contact_id");
                if (contactId != null) {
                    contact = getContactById(contactId);
                }
            } else if ("CHILD".equals(participant.getParticipantType())) {
                Integer underagedId = (Integer) participant.getParticipantData().get("underaged_id");
                Integer parentContactId = (Integer) participant.getParticipantData().get("parent_contact_id");

                if (underagedId != null) {
                    underagedMember = getUnderagedMemberById(underagedId);
                }
                if (parentContactId != null) {
                    contact = getContactById(parentContactId);
                }
            }

            if (contact != null) {
                return TemplateProcessor.processTemplate(template, contact, underagedMember);
            }
        } catch (Exception e) {
            System.err.println("Error processing description template: " + e.getMessage());
        }

        return template;
    }

    private BufferedImage generateBarcodeImageForData(String data) throws WriterException {
        PDF417Writer writer = new PDF417Writer();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, 2);
        hints.put(EncodeHintType.PDF417_COMPACT, false);
        hints.put(EncodeHintType.MARGIN, 10);

        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.PDF_417, 450, 150, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private Contact getContactById(int contactId) {
        try {
            List<Contact> allContacts = contactDAO.getAllContacts();
            return allContacts.stream()
                    .filter(contact -> contact.getId() == contactId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error loading contact by ID " + contactId + ": " + e.getMessage());
            return null;
        }
    }

    private UnderagedMember getUnderagedMemberById(int underagedId) {
        try {
            List<UnderagedMember> allMembers = underagedDAO.getAllUnderagedMembers();
            return allMembers.stream()
                    .filter(member -> member.getId() == underagedId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error loading underage member by ID " + underagedId + ": " + e.getMessage());
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(workshopComboBox.getScene().getWindow());
        alert.showAndWait();
    }

    public static class WorkshopParticipantItem {
        private final Map<String, Object> participantData;
        private boolean selected;

        public WorkshopParticipantItem(Map<String, Object> participantData) {
            this.participantData = new HashMap<>(participantData);
            this.selected = false;
        }

        public Map<String, Object> getParticipantData() {
            return participantData;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getParticipantName() {
            return (String) participantData.getOrDefault("participant_name", "Unknown");
        }

        public String getParticipantType() {
            return (String) participantData.getOrDefault("participant_type", "UNKNOWN");
        }

        public String getPaymentStatus() {
            return (String) participantData.getOrDefault("payment_status", "UNKNOWN");
        }

        public String getParticipantEmail() {
            String email = (String) participantData.get("participant_email");
            if (email == null || email.trim().isEmpty()) {
                email = (String) participantData.get("parent_email");
            }
            return email != null ? email : "";
        }

        public String getParticipantPhone() {
            String phone = (String) participantData.get("participant_phone");
            if (phone == null || phone.trim().isEmpty()) {
                phone = (String) participantData.get("parent_phone");
            }
            return phone != null ? phone : "";
        }

        public String getDisplayInfo() {
            StringBuilder info = new StringBuilder();
            info.append(getParticipantName());

            if ("CHILD".equals(getParticipantType())) {
                String parentName = (String) participantData.get("parent_name");
                if (parentName != null && !parentName.trim().isEmpty()) {
                    info.append(" (Parent: ").append(parentName).append(")");
                }

                Object age = participantData.get("participant_age");
                if (age != null) {
                    info.append(" Age: ").append(age);
                }
            }

            String email = getParticipantEmail();
            if (!email.isEmpty()) {
                info.append(" • ").append(email);
            }

            String phone = getParticipantPhone();
            if (!phone.isEmpty()) {
                info.append(" • ").append(phone);
            }

            return info.toString();
        }

        public String getStatusText() {
            String status = getPaymentStatus();
            return switch (status) {
                case "PENDING" -> "⏳ Pending Payment";
                case "PAID" -> "✅ Paid";
                case "REFUNDED" -> "↩️ Refunded";
                case "CANCELLED" -> "❌ Cancelled";
                default -> "❓ " + status;
            };
        }

        public String getTypeIcon() {
            return "CHILD".equals(getParticipantType()) ? "👶" : "👤";
        }
    }

    private class WorkshopParticipantListCell extends ListCell<WorkshopParticipantItem> {
        private CheckBox checkBox;
        private Label nameLabel;
        private Label infoLabel;
        private Label statusLabel;
        private HBox container;

        public WorkshopParticipantListCell() {
            createCell();
        }

        private void createCell() {
            checkBox = new CheckBox();
            checkBox.setStyle("-fx-font-size: 12px;");

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            infoLabel = new Label();
            infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

            statusLabel = new Label();
            statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");

            VBox textContainer = new VBox(2);
            textContainer.getChildren().addAll(nameLabel, infoLabel, statusLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(5));
            container.getChildren().addAll(checkBox, textContainer, spacer);

            checkBox.setOnAction(e -> {
                WorkshopParticipantItem item = getItem();
                if (item != null) {
                    item.setSelected(checkBox.isSelected());
                    updateSelectedCount();
                    updateButtonStates();
                }
            });
        }

        @Override
        protected void updateItem(WorkshopParticipantItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(item.getTypeIcon() + " " + item.getParticipantName() +
                        " (" + item.getParticipantType() + ")");
                infoLabel.setText(item.getDisplayInfo());
                statusLabel.setText(item.getStatusText());
                checkBox.setSelected(item.isSelected());

                if ("CHILD".equals(item.getParticipantType())) {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #ff6b35;");
                } else {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
                }

                String paymentStatus = item.getPaymentStatus();
                switch (paymentStatus) {
                    case "PAID" -> statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 10px; -fx-font-weight: bold;");
                    case "PENDING" -> statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 10px; -fx-font-weight: bold;");
                    case "CANCELLED" -> statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px; -fx-font-weight: bold;");
                    case "REFUNDED" -> statusLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-size: 10px; -fx-font-weight: bold;");
                    default -> statusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px; -fx-font-weight: bold;");
                }

                setGraphic(container);
            }
        }
    }
}