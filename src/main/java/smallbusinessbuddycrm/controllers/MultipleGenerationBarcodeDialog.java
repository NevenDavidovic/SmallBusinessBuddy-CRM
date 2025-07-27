package smallbusinessbuddycrm.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.concurrent.Task;
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
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.utilities.UplatnicaHtmlGenerator;
import smallbusinessbuddycrm.utilities.TemplateProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MultipleGenerationBarcodeDialog {
    private Stage dialog;
    private List<Contact> selectedContacts;
    private PaymentTemplate paymentTemplate;
    private Organization organization;

    // UI Components
    private ListView<ContactItem> contactListView;
    private ScrollPane allPreviewsScrollPane;
    private VBox allPreviewsContainer;
    private VBox previewPlaceholder;
    private CheckBox selectAllCheckBox;
    private Button showAllPreviewsButton;

    // Current preview data
    private Map<Contact, String> contactUplatnicaHtmlMap = new HashMap<>();
    private Map<Contact, BufferedImage> contactBarcodeImageMap = new HashMap<>();
    private Map<Contact, UnderagedMember> contactUnderagedMap = new HashMap<>();

    // Constants
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";

    public MultipleGenerationBarcodeDialog(Stage parentStage, List<Contact> selectedContacts, PaymentTemplate paymentTemplate) {
        this.selectedContacts = new ArrayList<>(selectedContacts);
        this.paymentTemplate = paymentTemplate;
        loadOrganizationData();
        loadUnderagedMembersForContacts();
        createDialog(parentStage);
    }

    private void loadOrganizationData() {
        try {
            OrganizationDAO organizationDAO = new OrganizationDAO();
            Optional<Organization> orgOptional = organizationDAO.getFirst();
            if (orgOptional.isPresent()) {
                organization = orgOptional.get();
                System.out.println("Loaded organization: " + organization.getName());
            } else {
                System.out.println("No organization found in database");
                createDefaultOrganization();
            }
        } catch (Exception e) {
            System.err.println("Error loading organization: " + e.getMessage());
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

    private boolean isContactUnderage(Contact contact) {
        return TemplateProcessor.isContactUnderage(contact);
    }

    private void loadUnderagedMembersForContacts() {
        System.out.println("=== Loading underage members for " + selectedContacts.size() + " contacts ===");

        for (Contact contact : selectedContacts) {
            System.out.println("\nProcessing contact: " + contact.getId() + " - " + contact.getFirstName() + " " + contact.getLastName());
            System.out.println("Contact birthday: " + contact.getBirthday());

            boolean isUnderage = TemplateProcessor.isContactUnderage(contact);
            System.out.println("Is underage: " + isUnderage);

            if (isUnderage) {
                try {
                    UnderagedDAO underagedDAO = new UnderagedDAO();
                    List<UnderagedMember> allMembers = underagedDAO.getUnderagedMembersByContactId(contact.getId());
                    System.out.println("Total underage records found: " + allMembers.size());

                    for (UnderagedMember member : allMembers) {
                        System.out.println("  - " + member.getFirstName() + " " + member.getLastName() +
                                " (Age: " + member.getAge() + ", Is Member: " + member.isMember() + ")");
                    }

                    // Filter to only active members
                    UnderagedMember activeMember = allMembers.stream()
                            .filter(UnderagedMember::isMember)
                            .findFirst()
                            .orElse(null);

                    if (activeMember != null) {
                        contactUnderagedMap.put(contact, activeMember);
                        System.out.println("✅ Loaded: " + activeMember.getFirstName() + " " + activeMember.getLastName());
                    } else {
                        System.out.println("❌ No active underage members found");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error loading underage members: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("\n=== Final contactUnderagedMap size: " + contactUnderagedMap.size() + " ===");
        for (Map.Entry<Contact, UnderagedMember> entry : contactUnderagedMap.entrySet()) {
            System.out.println("  " + entry.getKey().getFirstName() + " -> " +
                    entry.getValue().getFirstName() + " " + entry.getValue().getLastName());
        }
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("🔢 Multiple Barcode Generation - " + selectedContacts.size() + " contacts");
        dialog.setResizable(true);

        // Create main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("🔢 Multiple HUB-3 Barcode Generation");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Template info section
        VBox templateInfoSection = createTemplateInfoSection();

        // Contact list section (now full width)
        VBox contactListSection = createContactListSection();

        // Preview section (now full width below contacts)
        VBox previewSection = createPreviewSection();

        mainLayout.getChildren().addAll(
                titleLabel,
                templateInfoSection,
                new Separator(),
                contactListSection,
                new Separator(),
                previewSection
        );

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 1200, 700);
        dialog.setScene(scene);

        // Initialize contact list
        initializeContactList();
    }

    private VBox createTemplateInfoSection() {
        VBox templateSection = new VBox(10);
        templateSection.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

        Label templateTitle = new Label("📋 Selected Payment Template");
        templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        templateTitle.setStyle("-fx-text-fill: #1976d2;");

        Label templateName = new Label("Template: " + paymentTemplate.getName());
        templateName.setStyle("-fx-font-weight: bold;");

        Label templateAmount = new Label("Default Amount: " + paymentTemplate.getAmount() + " EUR");
        Label templateModel = new Label("Payment Model: " +
                (paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "N/A"));

        Label templateDesc = new Label("Description: " +
                (paymentTemplate.getDescription() != null && !paymentTemplate.getDescription().trim().isEmpty()
                        ? paymentTemplate.getDescription() : "N/A"));
        templateDesc.setWrapText(true);

        // Show reference template info
        Label templateRef = new Label("Reference Template: " +
                (paymentTemplate.getPozivNaBroj() != null && !paymentTemplate.getPozivNaBroj().trim().isEmpty()
                        ? paymentTemplate.getPozivNaBroj() : "Auto-generated"));
        templateRef.setWrapText(true);

        templateSection.getChildren().addAll(templateTitle, templateName, templateAmount, templateModel, templateDesc, templateRef);
        return templateSection;
    }

    private VBox createContactListSection() {
        VBox leftColumn = new VBox(15);

        // Header with select all checkbox
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label contactsTitle = new Label("👥 Contacts for Uplatnica Generation");
        contactsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactsTitle.setStyle("-fx-text-fill: #2e7d32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setStyle("-fx-font-weight: bold;");
        selectAllCheckBox.setOnAction(e -> handleSelectAll());

        headerBox.getChildren().addAll(contactsTitle, spacer, selectAllCheckBox);

        // Contact list view
        contactListView = new ListView<>();
        contactListView.setPrefHeight(250);
        contactListView.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5;");

        // Custom cell factory for contact items
        contactListView.setCellFactory(listView -> new ContactListCell());

        leftColumn.getChildren().addAll(headerBox, contactListView);
        return leftColumn;
    }

    private VBox createPreviewSection() {
        VBox rightColumn = new VBox(15);

        Label previewTitle = new Label("🔍 Croatian Uplatnica Previews");
        previewTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        previewTitle.setStyle("-fx-text-fill: #c2185b;");

        // Container for all previews
        allPreviewsContainer = new VBox(20);
        allPreviewsContainer.setPadding(new Insets(10));

        // Scroll pane for all previews
        allPreviewsScrollPane = new ScrollPane(allPreviewsContainer);
        allPreviewsScrollPane.setPrefHeight(500);
        allPreviewsScrollPane.setPrefWidth(Double.MAX_VALUE);
        allPreviewsScrollPane.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-radius: 5;");
        allPreviewsScrollPane.setFitToWidth(true);
        allPreviewsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        allPreviewsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Placeholder for when no previews are generated
        previewPlaceholder = new VBox(10);
        previewPlaceholder.setAlignment(Pos.CENTER);
        previewPlaceholder.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #f8f9fa;");
        previewPlaceholder.setPrefHeight(300);

        Label placeholder = new Label("🧾\nCroatian Uplatnice will appear here\nafter generating previews");
        placeholder.setStyle("-fx-text-alignment: center; -fx-text-fill: #6c757d; -fx-font-size: 14px;");
        previewPlaceholder.getChildren().add(placeholder);

        // Initially show placeholder
        allPreviewsScrollPane.setContent(previewPlaceholder);

        // Preview buttons
        HBox previewButtonBox = new HBox(10);
        previewButtonBox.setAlignment(Pos.CENTER);

        showAllPreviewsButton = new Button("👁️ Generate All Previews");
        showAllPreviewsButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-border-radius: 4;");
        showAllPreviewsButton.setOnAction(e -> generateAllPreviews());

        Button clearPreviewsButton = new Button("🗑️ Clear Previews");
        clearPreviewsButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        clearPreviewsButton.setOnAction(e -> clearAllPreviews());

        previewButtonBox.getChildren().addAll(showAllPreviewsButton, clearPreviewsButton);

        rightColumn.getChildren().addAll(previewTitle, allPreviewsScrollPane, previewButtonBox);
        return rightColumn;
    }

    private void initializeContactList() {
        List<ContactItem> contactItems = new ArrayList<>();

        for (Contact contact : selectedContacts) {
            ContactItem item = new ContactItem(contact);
            item.setSelected(true); // Default to selected

            // Mark if contact is underage
            if (isContactUnderage(contact)) {
                item.setUnderage(true);
                if (contactUnderagedMap.containsKey(contact)) {
                    UnderagedMember underaged = contactUnderagedMap.get(contact);
                    item.setUnderagedInfo(underaged.getFirstName() + " " + underaged.getLastName());
                }
            }

            contactItems.add(item);
        }

        contactListView.getItems().setAll(contactItems);
        selectAllCheckBox.setSelected(true);
    }

    private void handleSelectAll() {
        boolean selectAll = selectAllCheckBox.isSelected();

        for (ContactItem item : contactListView.getItems()) {
            item.setSelected(selectAll);
        }

        contactListView.refresh();
    }

    private void generateAllPreviews() {
        List<ContactItem> selectedItems = contactListView.getItems().stream()
                .filter(ContactItem::isSelected)
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one contact for preview generation.");
            return;
        }

        // Clear existing previews
        allPreviewsContainer.getChildren().clear();
        contactUplatnicaHtmlMap.clear();
        contactBarcodeImageMap.clear();

        // Disable button during generation
        showAllPreviewsButton.setDisable(true);

        // Create background task for preview generation
        Task<Void> previewTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // FIXED: Check if payment template contains underage placeholders in EITHER description OR reference
                boolean templateHasUnderagedData = templateContainsUnderagedPlaceholders(paymentTemplate);
                System.out.println("🔍 Template contains underage data (description or reference): " + templateHasUnderagedData);

                int totalGenerations = calculateTotalGenerations(selectedItems, templateHasUnderagedData);
                int currentGeneration = 0;

                for (ContactItem item : selectedItems) {
                    Contact contact = item.getContact();

                    updateMessage("Processing contact: " + contact.getFirstName() + " " + contact.getLastName());

                    try {
                        if (templateHasUnderagedData) {
                            // Get all underage members for this contact - FILTER FOR MEMBERS ONLY
                            UnderagedDAO underagedDAO = new UnderagedDAO();
                            List<UnderagedMember> allUnderagedList = underagedDAO.getUnderagedMembersByContactId(contact.getId());

                            // Filter to only show members
                            List<UnderagedMember> membersList = allUnderagedList.stream()
                                    .filter(UnderagedMember::isMember)
                                    .toList();

                            if (membersList.isEmpty()) {
                                // No underage members but template expects them - generate for contact only IF contact is member
                                if (contact.isMember()) {
                                    System.out.println("⚠️ Template has underage data but no underage members found. Generating for contact (member): " + contact.getId());
                                    currentGeneration++;
                                    generateSingleUplatnica(contact, null, currentGeneration, totalGenerations);
                                } else {
                                    System.out.println("⚠️ Template has underage data, no underage members, and contact is not a member. Skipping: " + contact.getId());
                                }
                            } else {
                                // Generate one uplatnica per underage member
                                System.out.println("🔍 Generating " + membersList.size() + " uplatnicas for contact: " + contact.getId());
                                for (UnderagedMember underagedMember : membersList) {
                                    currentGeneration++;
                                    updateMessage("Generating for: " + contact.getFirstName() + " " + contact.getLastName() +
                                            " → Child: " + underagedMember.getFirstName() + " " + underagedMember.getLastName());

                                    generateSingleUplatnica(contact, underagedMember, currentGeneration, totalGenerations);
                                }
                            }
                        } else {
                            // Template doesn't use underage data - generate single uplatnica for contact ONLY if contact is member
                            if (contact.isMember()) {
                                currentGeneration++;
                                generateSingleUplatnica(contact, null, currentGeneration, totalGenerations);
                            } else {
                                System.out.println("⚠️ Contact is not a member, skipping: " + contact.getId());
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("❌ Error processing contact " + contact.getFirstName() + " " + contact.getLastName() + ": " + e.getMessage());
                        e.printStackTrace();
                        currentGeneration++; // Still increment to keep progress accurate
                    }

                    updateProgress(currentGeneration, totalGenerations);
                }

                return null;
            }

            @Override
            protected void succeeded() {
                showAllPreviewsButton.setDisable(false);
                allPreviewsScrollPane.setContent(allPreviewsContainer);
                System.out.println("🎉 Preview generation completed successfully!");
            }

            @Override
            protected void failed() {
                showAllPreviewsButton.setDisable(false);
                showAlert(Alert.AlertType.ERROR, "Preview Generation Failed", "Error generating previews: " + getException().getMessage());
                System.err.println("❌ Preview generation failed: " + getException().getMessage());
                getException().printStackTrace();
            }
        };

        Thread thread = new Thread(previewTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * UPDATED: Check if payment template description contains underage placeholders
     */
    private boolean templateContainsUnderagedPlaceholders(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }

        // Look for any {{underaged_attributes.*}} patterns
        return description.contains("{{underaged_attributes.");
    }

    /**
     * NEW: Check if template (description OR reference) contains underage placeholders
     */
    private boolean templateContainsUnderagedPlaceholders(PaymentTemplate template) {
        if (template == null) {
            return false;
        }

        // Check description for underage placeholders
        boolean descriptionHasUnderage = templateContainsUnderagedPlaceholders(template.getDescription());

        // Check reference template for underage placeholders
        boolean referenceHasUnderage = templateContainsUnderagedPlaceholders(template.getPozivNaBroj());

        System.out.println("🔍 Description has underage: " + descriptionHasUnderage + ", Reference has underage: " + referenceHasUnderage);

        return descriptionHasUnderage || referenceHasUnderage;
    }

    /**
     * Calculate total number of uplatnicas that will be generated
     */
    private int calculateTotalGenerations(List<ContactItem> selectedItems, boolean templateHasUnderagedData) {
        int total = 0;

        for (ContactItem item : selectedItems) {
            Contact contact = item.getContact();

            if (templateHasUnderagedData) {
                try {
                    // Count underage members who are members
                    UnderagedDAO underagedDAO = new UnderagedDAO();
                    List<UnderagedMember> allUnderagedList = underagedDAO.getUnderagedMembersByContactId(contact.getId());

                    long memberCount = allUnderagedList.stream()
                            .filter(UnderagedMember::isMember)
                            .count();

                    if (memberCount > 0) {
                        total += memberCount; // One per underage member
                    } else if (contact.isMember()) {
                        total += 1; // Fallback to contact if they're a member
                    }
                    // If no underage members and contact not member, add 0

                } catch (Exception e) {
                    System.err.println("Error counting underage members for contact " + contact.getId() + ": " + e.getMessage());
                    // Fallback: if contact is member, count as 1
                    if (contact.isMember()) {
                        total += 1;
                    }
                }
            } else {
                // Template doesn't use underage data - count only if contact is member
                if (contact.isMember()) {
                    total += 1;
                }
            }
        }

        return total;
    }

    /**
     * Generate a single uplatnica for a contact and optional underage member
     */
    private void generateSingleUplatnica(Contact contact, UnderagedMember underagedMember, int currentGeneration, int totalGenerations) throws WriterException {
        try {
            System.out.println("🔍 Generating uplatnica " + currentGeneration + "/" + totalGenerations +
                    " for contact: " + contact.getFirstName() + " " + contact.getLastName() +
                    (underagedMember != null ? " → Child: " + underagedMember.getFirstName() + " " + underagedMember.getLastName() : ""));

            // Generate HUB-3 data and barcode
            String hub3Data = generateHUB3DataForContact(contact, underagedMember);
            BufferedImage barcodeImage = generateBarcodeImageForData(hub3Data);

            // Generate HTML with underage member support using the utility
            String uplatnicaHtml = UplatnicaHtmlGenerator.generateUplatnicaHtml(
                    contact, organization, paymentTemplate, barcodeImage, underagedMember);

            // Create unique key for storage (contact + child combination)
            String storageKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");

            // Store in maps with unique keys
            contactUplatnicaHtmlMap.put(contact, uplatnicaHtml); // This will be overwritten for multiple children, but that's OK for now
            contactBarcodeImageMap.put(contact, barcodeImage);

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                addPreviewToContainer(contact, uplatnicaHtml, underagedMember);
            });

            System.out.println("✅ Successfully generated uplatnica for " +
                    (underagedMember != null ? underagedMember.getFirstName() + " " + underagedMember.getLastName() :
                            contact.getFirstName() + " " + contact.getLastName()));

        } catch (Exception e) {
            System.err.println("❌ Error generating uplatnica: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by caller
        }
    }

    private void addPreviewToContainer(Contact contact, String uplatnicaHtml, UnderagedMember underagedMember) {
        // Create a container for this contact's preview
        VBox contactPreviewContainer = new VBox(10);
        contactPreviewContainer.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-color: white; -fx-padding: 15;");

        // Contact header - show if underage member is specified
        String headerText = "👤 " + contact.getFirstName() + " " + contact.getLastName();
        if (underagedMember != null) {
            headerText += " 👶 (Child: " + underagedMember.getFirstName() + " " + underagedMember.getLastName() + ")";
        }

        Label contactHeader = new Label(headerText);
        contactHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactHeader.setStyle(underagedMember != null ? "-fx-text-fill: #ff6b35;" : "-fx-text-fill: #495057;");

        // Contact info
        StringBuilder contactInfo = new StringBuilder();
        if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
            contactInfo.append("📧 ").append(contact.getEmail()).append("  ");
        }
        if (contact.getPhoneNum() != null && !contact.getPhoneNum().trim().isEmpty()) {
            contactInfo.append("📞 ").append(contact.getPhoneNum());
        }

        // Add child info if applicable
        if (underagedMember != null) {
            contactInfo.append("\n👶 Child Details: Age ").append(underagedMember.getAge());
            if (underagedMember.getBirthDate() != null) {
                contactInfo.append(", Born: ").append(underagedMember.getBirthDate());
            }
        }

        Label contactInfoLabel = new Label(contactInfo.toString());
        contactInfoLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        // WebView for the uplatnica
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        webView.setPrefHeight(500);
        webView.setPrefWidth(Double.MAX_VALUE);
        webView.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 1; -fx-border-radius: 4;");

        // Load the HTML content
        webView.getEngine().loadContent(uplatnicaHtml);

        // Action buttons for individual uplatnica
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button printIndividualButton = new Button("🖨️ Print This");
        printIndividualButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
        printIndividualButton.setOnAction(e -> printIndividualUplatnica(webView));

        Button saveIndividualButton = new Button("💾 Save This");
        saveIndividualButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px;");
        saveIndividualButton.setOnAction(e -> saveIndividualUplatnica(contact, underagedMember));

        actionButtons.getChildren().addAll(printIndividualButton, saveIndividualButton);

        contactPreviewContainer.getChildren().addAll(contactHeader, contactInfoLabel, webView, actionButtons);
        allPreviewsContainer.getChildren().add(contactPreviewContainer);
    }

    // Add overload for backward compatibility
    private void addPreviewToContainer(Contact contact, String uplatnicaHtml) {
        addPreviewToContainer(contact, uplatnicaHtml, null);
    }

    private void clearAllPreviews() {
        allPreviewsContainer.getChildren().clear();
        contactUplatnicaHtmlMap.clear();
        contactBarcodeImageMap.clear();
        allPreviewsScrollPane.setContent(previewPlaceholder);
    }

    private void printIndividualUplatnica(javafx.scene.web.WebView webView) {
        try {
            javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(dialog)) {
                webView.getEngine().print(job);
                job.endJob();
            }
        } catch (Exception e) {
            System.err.println("Error printing individual uplatnica: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Print Error", "Error printing uplatnica: " + e.getMessage());
        }
    }

    private void saveIndividualUplatnica(Contact contact, UnderagedMember underagedMember) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Save Uplatnica");
        File selectedDirectory = directoryChooser.showDialog(dialog);

        if (selectedDirectory != null) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String contactName = contact.getFirstName() + "_" + contact.getLastName();
                contactName = contactName.replaceAll("[^a-zA-Z0-9_-]", "_");

                // Add child name if applicable
                String fileName = "uplatnica_" + contactName;
                if (underagedMember != null) {
                    String childName = underagedMember.getFirstName() + "_" + underagedMember.getLastName();
                    childName = childName.replaceAll("[^a-zA-Z0-9_-]", "_");
                    fileName += "_child_" + childName;
                }
                fileName += "_" + timestamp;

                // Save HTML
                String uplatnicaHtml = contactUplatnicaHtmlMap.get(contact);
                if (uplatnicaHtml != null) {
                    File htmlFile = new File(selectedDirectory, fileName + ".html");
                    try (FileWriter writer = new FileWriter(htmlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                        writer.write(uplatnicaHtml);
                    }
                }

                // Save barcode image
                BufferedImage barcodeImage = contactBarcodeImageMap.get(contact);
                if (barcodeImage != null) {
                    File imageFile = new File(selectedDirectory, "barcode_" + fileName + ".png");
                    ImageIO.write(barcodeImage, "png", imageFile);
                }

                String successMessage = "Uplatnica saved successfully for " + contact.getFirstName() + " " + contact.getLastName();
                if (underagedMember != null) {
                    successMessage += " (Child: " + underagedMember.getFirstName() + " " + underagedMember.getLastName() + ")";
                }
                showAlert(Alert.AlertType.INFORMATION, "Save Complete", successMessage);

            } catch (Exception e) {
                System.err.println("Error saving individual uplatnica: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Save Error", "Error saving uplatnica: " + e.getMessage());
            }
        }
    }

    // Add overload for backward compatibility
    private void saveIndividualUplatnica(Contact contact) {
        saveIndividualUplatnica(contact, null);
    }

    /**
     * UPDATED: Generate HUB-3 data for contact with dynamic reference support
     */
    private String generateHUB3DataForContact(Contact contact, UnderagedMember underagedMember) {
        StringBuilder hub3Data = new StringBuilder();

        // 1. Bank code
        hub3Data.append(FIXED_BANK_CODE).append("\n");

        // 2. Currency
        hub3Data.append(FIXED_CURRENCY).append("\n");

        // 3. Amount in cents
        String amountCents = paymentTemplate.getAmount().multiply(new java.math.BigDecimal("100")).toBigInteger().toString();
        hub3Data.append(String.format("%015d", Long.parseLong(amountCents))).append("\n");

        // 4. Payer name
        hub3Data.append(contact.getFirstName() + " " + contact.getLastName()).append("\n");

        // 5. Payer address
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        hub3Data.append(payerAddress).append("\n");

        // 6. Payer city
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }
        hub3Data.append(payerCity).append("\n");

        // 7. Recipient name
        hub3Data.append(organization.getName()).append("\n");

        // 8. Recipient address
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        hub3Data.append(recipientAddress).append("\n");

        // 9. Recipient city
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

        // 10. Recipient IBAN
        hub3Data.append(organization.getIban()).append("\n");

        // 11. Payment model
        hub3Data.append(paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "").append("\n");

        // 12. Reference number - UPDATED TO HANDLE DYNAMIC REFERENCES
        String reference = processReferenceTemplate(paymentTemplate.getPozivNaBroj(), contact, underagedMember);
        hub3Data.append(reference).append("\n");

        // 13. Purpose code
        hub3Data.append("").append("\n");

        // 14. Description (no newline at end) - Process with specific underage member
        String processedDescription = TemplateProcessor.processTemplate(
                paymentTemplate.getDescription(), contact, underagedMember);
        hub3Data.append(processedDescription);

        return hub3Data.toString();
    }

    /**
     * UPDATED: Process reference template with dynamic field support
     * @param referenceTemplate The reference template from PaymentTemplate
     * @param contact The contact data
     * @param underagedMember The underage member data (can be null)
     * @return Processed reference string
     */
    private String processReferenceTemplate(String referenceTemplate, Contact contact, UnderagedMember underagedMember) {
        if (referenceTemplate == null || referenceTemplate.trim().isEmpty()) {
            return "";
        }

        String template = referenceTemplate.trim();

        // Handle dynamic reference placeholders
        if (template.startsWith("{{") && template.endsWith("}}")) {
            String placeholder = template.substring(2, template.length() - 2);

            if (placeholder.equals("contact_attributes.pin")) {
                return contact.getPin() != null ? contact.getPin() : "";
            } else if (placeholder.equals("underaged_attributes.pin")) {
                if (underagedMember != null) {
                    return underagedMember.getPin() != null ? underagedMember.getPin() : "";
                }
                return "";
            }

            // If it's an unknown placeholder, return empty
            System.out.println("Warning: Unknown reference placeholder '" + placeholder + "', returning empty string");
            return "";
        } else {
            // It's either a static number or contains old-style {contact_id} placeholder
            // Handle backward compatibility with {contact_id}
            String processedReference = template.replace("{contact_id}", String.valueOf(contact.getId()));

            // Validate that the result contains only numbers (for banking compliance)
            if (processedReference.matches("\\d*")) {
                return processedReference;
            } else {
                // If it contains non-numeric characters, log warning and return contact ID as fallback
                System.out.println("Warning: Reference template '" + template + "' contains non-numeric characters. Using contact ID as fallback.");
                return String.valueOf(contact.getId());
            }
        }
    }

    // Add overload for backward compatibility
    private String generateHUB3DataForContact(Contact contact) {
        return generateHUB3DataForContact(contact, null);
    }

    private String processDescriptionTemplate(String template, Contact contact) {
        System.out.println("=== Processing template for contact: " + contact.getId() + " ===");
        System.out.println("Template: " + template);

        UnderagedMember underagedMember = contactUnderagedMap.get(contact);
        System.out.println("Found underage member in map: " + (underagedMember != null ?
                underagedMember.getFirstName() + " " + underagedMember.getLastName() : "None"));

        // The underage member should now be properly loaded in generateAllPreviews()
        // But keep fallback just in case
        if (underagedMember == null && TemplateProcessor.isContactUnderage(contact)) {
            System.out.println("⚠️ WARNING: Underage member not in map, but contact is underage!");
            underagedMember = TemplateProcessor.getActiveUnderagedMember(contact.getId());
            if (underagedMember != null) {
                contactUnderagedMap.put(contact, underagedMember);
                System.out.println("✅ Emergency fallback loaded: " + underagedMember.getFirstName() + " " + underagedMember.getLastName());
            }
        }

        String result = TemplateProcessor.processTemplate(template, contact, underagedMember);
        System.out.println("Final result: " + result);
        System.out.println("=== End processing ===");

        return result;
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialog);
        alert.showAndWait();
    }

    public void showAndWait() {
        dialog.showAndWait();
    }

    // Inner classes
    private static class ContactItem {
        private final Contact contact;
        private boolean selected;
        private boolean generated;
        private String error;
        private boolean underage;
        private String underagedInfo;

        public ContactItem(Contact contact) {
            this.contact = contact;
            this.selected = false;
            this.generated = false;
            this.error = null;
            this.underage = false;
            this.underagedInfo = null;
        }

        public Contact getContact() {
            return contact;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isGenerated() {
            return generated;
        }

        public void setGenerated(boolean generated) {
            this.generated = generated;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public boolean isUnderage() {
            return underage;
        }

        public void setUnderage(boolean underage) {
            this.underage = underage;
        }

        public String getUnderagedInfo() {
            return underagedInfo;
        }

        public void setUnderagedInfo(String underagedInfo) {
            this.underagedInfo = underagedInfo;
        }

        public String getDisplayName() {
            return contact.getFirstName() + " " + contact.getLastName();
        }

        public String getDisplayInfo() {
            StringBuilder info = new StringBuilder();
            info.append(getDisplayName());

            if (underage) {
                info.append(" 👶 UNDERAGE");
                if (underagedInfo != null) {
                    info.append(" (Member: ").append(underagedInfo).append(")");
                }
            }

            if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                info.append(" • ").append(contact.getEmail());
            }

            if (contact.getPhoneNum() != null && !contact.getPhoneNum().trim().isEmpty()) {
                info.append(" • ").append(contact.getPhoneNum());
            }

            return info.toString();
        }
    }

    private class ContactListCell extends ListCell<ContactItem> {
        private CheckBox checkBox;
        private Label nameLabel;
        private Label infoLabel;
        private Label statusLabel;
        private HBox container;

        public ContactListCell() {
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
                ContactItem item = getItem();
                if (item != null) {
                    item.setSelected(checkBox.isSelected());
                }
            });
        }

        @Override
        protected void updateItem(ContactItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(item.getDisplayName());
                infoLabel.setText(item.getDisplayInfo());
                checkBox.setSelected(item.isSelected());

                // Style underage contacts differently
                if (item.isUnderage()) {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #ff6b35;");
                    infoLabel.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 11px;");
                } else {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
                }

                // Update status
                if (item.isGenerated()) {
                    statusLabel.setText("✅ Generated");
                    statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 10px; -fx-font-weight: bold;");
                } else if (item.getError() != null) {
                    statusLabel.setText("❌ Error: " + item.getError());
                    statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px; -fx-font-weight: bold;");
                } else {
                    statusLabel.setText("⏳ Pending");
                    statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 10px; -fx-font-weight: bold;");
                }

                setGraphic(container);
            }
        }
    }
}