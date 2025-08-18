package smallbusinessbuddycrm.controllers.utilities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.itextpdf.html2pdf.HtmlConverter;
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
import javafx.application.Platform;
import smallbusinessbuddycrm.database.PaymentAttachmentDAO;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.model.*;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.services.google.GoogleOAuthManager;
import smallbusinessbuddycrm.utilities.UplatnicaHtmlGenerator;
import smallbusinessbuddycrm.utilities.TemplateProcessor;
import smallbusinessbuddycrm.utilities.LanguageManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Base64;

public class MultipleGenerationBarcodeDialog {
    private Stage dialog;
    private List<Contact> selectedContacts;
    private PaymentTemplate paymentTemplate;
    private Organization organization;
    private LanguageManager languageManager;

    // OAuth manager for email services
    private GoogleOAuthManager oauthManager;

    // UI Components
    private ListView<ContactItem> contactListView;
    private ScrollPane allPreviewsScrollPane;
    private VBox allPreviewsContainer;
    private VBox previewPlaceholder;
    private CheckBox selectAllCheckBox;
    private Button showAllPreviewsButton;

    // Current preview data - KEEPING ORIGINAL STRUCTURE
    private Map<Contact, String> contactUplatnicaHtmlMap = new HashMap<>();
    private Map<Contact, BufferedImage> contactBarcodeImageMap = new HashMap<>();
    private Map<Contact, UnderagedMember> contactUnderagedMap = new HashMap<>();

    // NEW: Store all generated slips for email sending
    private Map<String, String> allGeneratedHtmlMap = new HashMap<>(); // Key: contactId_childId or contactId
    private Map<String, BufferedImage> allGeneratedBarcodeMap = new HashMap<>();
    private Map<String, UnderagedMember> allGeneratedUnderagedMap = new HashMap<>();
    private ComboBox<PaymentTemplate> paymentTemplateCombo;
    private VBox templateSelectionBox;
    private PaymentAttachmentDAO paymentAttachmentDAO;
    private PaymentAttachment selectedPaymentSlipTemplate;

    // Constants
    private static final String FIXED_BANK_CODE = "HRVHUB30";
    private static final String FIXED_CURRENCY = "EUR";

    public MultipleGenerationBarcodeDialog(Stage parentStage, List<Contact> selectedContacts) {
        this.selectedContacts = new ArrayList<>(selectedContacts);
        this.paymentTemplate = null; // Will be selected by user
        this.languageManager = LanguageManager.getInstance();

        // Initialize OAuth manager
        this.oauthManager = GoogleOAuthManager.getInstance();

        loadOrganizationData();
        loadUnderagedMembersForContacts();
        createDialog(parentStage);
    }

    public MultipleGenerationBarcodeDialog(Stage parentStage, List<Contact> selectedContacts, PaymentTemplate paymentTemplate) {
        this.selectedContacts = new ArrayList<>(selectedContacts);
        this.paymentTemplate = paymentTemplate;
        this.languageManager = LanguageManager.getInstance();

        // Initialize OAuth manager
        this.oauthManager = GoogleOAuthManager.getInstance();

        loadOrganizationData();
        loadUnderagedMembersForContacts();
        createDialog(parentStage);
    }

    // Check if Gmail is connected
    private boolean isGmailConnected() {
        return oauthManager.isGmailConnected();
    }

    // Get current Gmail user email
    private String getGmailUserEmail() {
        return oauthManager.getUserEmail();
    }

    private String normalizeTextForHUB3(String text) {
        if (text == null) return "";

        return text
                .replace("č", "c").replace("Č", "C")
                .replace("ć", "c").replace("Ć", "C")
                .replace("ž", "z").replace("Ž", "Z")
                .replace("š", "s").replace("Š", "S")
                .replace("đ", "d").replace("Đ", "D");
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
        dialog.setTitle(languageManager.getText("barcode.dialog.title").replace("{0}", String.valueOf(selectedContacts.size())));
        dialog.setResizable(true);

        // Create main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("🔢 " + languageManager.getText("barcode.dialog.main.title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // ADD: Template selection section (only if no template pre-selected)
        if (paymentTemplate == null) {
            templateSelectionBox = createTemplateSelectionSection();
            mainLayout.getChildren().add(templateSelectionBox);
            mainLayout.getChildren().add(new Separator());
        } else {
            // Template info section for pre-selected template
            VBox templateInfoSection = createTemplateInfoSection();
            mainLayout.getChildren().add(templateInfoSection);
            mainLayout.getChildren().add(new Separator());
        }

        // Contact list section
        VBox contactListSection = createContactListSection();

        // Preview section
        VBox previewSection = createPreviewSection();

        mainLayout.getChildren().addAll(
                titleLabel,
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

        // Load payment templates if needed
        if (paymentTemplate == null) {
            loadPaymentTemplates();
        }
    }

    private VBox createTemplateSelectionSection() {
        VBox templateSection = new VBox(15);
        templateSection.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

        Label templateTitle = new Label("💳 " + languageManager.getText("barcode.template.select.title"));
        templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        templateTitle.setStyle("-fx-text-fill: #1976d2;");

        paymentTemplateCombo = new ComboBox<>();
        paymentTemplateCombo.setPrefWidth(400);
        paymentTemplateCombo.setPromptText(languageManager.getText("barcode.template.combo.prompt"));
        paymentTemplateCombo.setOnAction(e -> handleTemplateSelection());

        // Template details display
        Label templateDetailsLabel = new Label(languageManager.getText("barcode.template.details.default"));
        templateDetailsLabel.setId("templateDetailsLabel");
        templateDetailsLabel.setWrapText(true);
        templateDetailsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        templateSection.getChildren().addAll(templateTitle, paymentTemplateCombo, templateDetailsLabel);
        return templateSection;
    }

    private void loadPaymentTemplates() {
        try {
            System.out.println("DEBUG: Loading payment templates...");
            PaymentTemplateDAO dao = new PaymentTemplateDAO();

            List<PaymentTemplate> templates = dao.getActivePaymentTemplates();
            System.out.println("DEBUG: getActivePaymentTemplates() returned " + templates.size() + " templates");

            paymentTemplateCombo.getItems().clear();
            paymentTemplateCombo.getItems().addAll(templates);

            if (templates.isEmpty()) {
                Label detailsLabel = (Label) templateSelectionBox.lookup("#templateDetailsLabel");
                if (detailsLabel != null) {
                    detailsLabel.setText(languageManager.getText("barcode.template.no.active"));
                    detailsLabel.setStyle("-fx-text-fill: #856404; -fx-font-size: 12px;");
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading payment templates: " + e.getMessage());
            e.printStackTrace();

            Label detailsLabel = (Label) templateSelectionBox.lookup("#templateDetailsLabel");
            if (detailsLabel != null) {
                detailsLabel.setText(languageManager.getText("barcode.template.load.error").replace("{0}", e.getMessage()));
                detailsLabel.setStyle("-fx-text-fill: #721c24; -fx-font-size: 12px;");
            }
        }
    }

    private void handleTemplateSelection() {
        PaymentTemplate selectedTemplate = paymentTemplateCombo.getSelectionModel().getSelectedItem();
        if (selectedTemplate != null) {
            this.paymentTemplate = selectedTemplate;

            StringBuilder info = new StringBuilder();
            info.append(languageManager.getText("barcode.template.info.name").replace("{0}", selectedTemplate.getName())).append("\n");
            info.append(languageManager.getText("barcode.template.info.amount").replace("{0}", selectedTemplate.getAmount().toString())).append("\n");
            info.append(languageManager.getText("barcode.template.info.model").replace("{0}",
                    selectedTemplate.getModelOfPayment() != null ? selectedTemplate.getModelOfPayment() : "N/A")).append("\n");

            String description = selectedTemplate.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                info.append(languageManager.getText("barcode.template.info.description").replace("{0}",
                        description.length() > 100 ? description.substring(0, 100) + "..." : description)).append("\n");
            }

            String reference = selectedTemplate.getPozivNaBroj();
            if (reference != null && !reference.trim().isEmpty()) {
                info.append(languageManager.getText("barcode.template.info.reference").replace("{0}", reference)).append("\n");
            }

            Label detailsLabel = (Label) templateSelectionBox.lookup("#templateDetailsLabel");
            if (detailsLabel != null) {
                detailsLabel.setText(info.toString());
                detailsLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
            }

            // Enable generation button
            showAllPreviewsButton.setDisable(false);
        }
    }

    private VBox createTemplateInfoSection() {
        VBox templateSection = new VBox(10);
        templateSection.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

        Label templateTitle = new Label("📋 " + languageManager.getText("barcode.template.info.title"));
        templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        templateTitle.setStyle("-fx-text-fill: #1976d2;");

        Label templateName = new Label(languageManager.getText("barcode.template.info.name").replace("{0}", paymentTemplate.getName()));
        templateName.setStyle("-fx-font-weight: bold;");

        Label templateAmount = new Label(languageManager.getText("barcode.template.info.amount").replace("{0}", paymentTemplate.getAmount().toString()));
        Label templateModel = new Label(languageManager.getText("barcode.template.info.model").replace("{0}",
                paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "N/A"));

        Label templateDesc = new Label(languageManager.getText("barcode.template.info.description").replace("{0}",
                paymentTemplate.getDescription() != null && !paymentTemplate.getDescription().trim().isEmpty()
                        ? paymentTemplate.getDescription() : "N/A"));
        templateDesc.setWrapText(true);

        // Show reference template info
        Label templateRef = new Label(languageManager.getText("barcode.template.info.reference").replace("{0}",
                paymentTemplate.getPozivNaBroj() != null && !paymentTemplate.getPozivNaBroj().trim().isEmpty()
                        ? paymentTemplate.getPozivNaBroj() : languageManager.getText("barcode.template.info.auto.generated")));
        templateRef.setWrapText(true);

        templateSection.getChildren().addAll(templateTitle, templateName, templateAmount, templateModel, templateDesc, templateRef);
        return templateSection;
    }

    private VBox createContactListSection() {
        VBox leftColumn = new VBox(15);

        // Header with select all checkbox
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label contactsTitle = new Label("👥 " + languageManager.getText("barcode.contacts.title"));
        contactsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        contactsTitle.setStyle("-fx-text-fill: #2e7d32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        selectAllCheckBox = new CheckBox(languageManager.getText("barcode.contacts.select.all"));
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

        Label previewTitle = new Label("🔍 " + languageManager.getText("barcode.preview.title"));
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

        Label placeholder = new Label("🧾\n" + languageManager.getText("barcode.preview.placeholder"));
        placeholder.setStyle("-fx-text-alignment: center; -fx-text-fill: #6c757d; -fx-font-size: 14px;");
        previewPlaceholder.getChildren().add(placeholder);

        // Initially show placeholder
        allPreviewsScrollPane.setContent(previewPlaceholder);

        // Preview buttons with EMAIL BUTTON
        HBox previewButtonBox = new HBox(10);
        previewButtonBox.setAlignment(Pos.CENTER);

        showAllPreviewsButton = new Button("👁️ " + languageManager.getText("barcode.button.generate.all"));
        showAllPreviewsButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-border-radius: 4;");
        showAllPreviewsButton.setOnAction(e -> generateAllPreviews());

        if (paymentTemplate == null) {
            showAllPreviewsButton.setDisable(true);
        }

        Button clearPreviewsButton = new Button("🗑️ " + languageManager.getText("barcode.button.clear.previews"));
        clearPreviewsButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;");
        clearPreviewsButton.setOnAction(e -> clearAllPreviews());

        Button emailAllButton = new Button("📧 " + languageManager.getText("barcode.button.email.all"));
        emailAllButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4;");
        emailAllButton.setOnAction(e -> emailAllGeneratedSlips());

        previewButtonBox.getChildren().addAll(showAllPreviewsButton, clearPreviewsButton, emailAllButton);

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
        if (paymentTemplate == null) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("barcode.alert.no.template.title"),
                    languageManager.getText("barcode.alert.no.template.message"));
            return;
        }

        List<ContactItem> selectedItems = contactListView.getItems().stream()
                .filter(ContactItem::isSelected)
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("barcode.alert.no.selection.title"),
                    languageManager.getText("barcode.alert.no.selection.message"));
            return;
        }

        // Clear existing previews
        allPreviewsContainer.getChildren().clear();
        contactUplatnicaHtmlMap.clear();
        contactBarcodeImageMap.clear();
        // ALSO clear the new maps for email functionality
        allGeneratedHtmlMap.clear();
        allGeneratedBarcodeMap.clear();
        allGeneratedUnderagedMap.clear();

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

                    updateMessage(languageManager.getText("barcode.processing.contact").replace("{0}",
                            contact.getFirstName() + " " + contact.getLastName()));

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
                                    System.out.println(languageManager.getText("barcode.template.underage.fallback").replace("{0}", String.valueOf(contact.getId())));
                                    currentGeneration++;
                                    generateSingleUplatnica(contact, null, currentGeneration, totalGenerations);
                                } else {
                                    System.out.println(languageManager.getText("barcode.template.skip.no.members").replace("{0}", String.valueOf(contact.getId())));
                                }
                            } else {
                                // Generate one uplatnica per underage member
                                System.out.println("🔍 Generating " + membersList.size() + " uplatnicas for contact: " + contact.getId());
                                for (UnderagedMember underagedMember : membersList) {
                                    currentGeneration++;
                                    updateMessage(languageManager.getText("barcode.processing.generating")
                                            .replace("{0}", contact.getFirstName() + " " + contact.getLastName())
                                            .replace("{1}", underagedMember.getFirstName() + " " + underagedMember.getLastName()));

                                    generateSingleUplatnica(contact, underagedMember, currentGeneration, totalGenerations);
                                }
                            }
                        } else {
                            // Template doesn't use underage data - generate single uplatnica for contact ONLY if contact is member
                            if (contact.isMember()) {
                                currentGeneration++;
                                generateSingleUplatnica(contact, null, currentGeneration, totalGenerations);
                            } else {
                                System.out.println(languageManager.getText("barcode.template.skip.not.member").replace("{0}", String.valueOf(contact.getId())));
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
                showAlert(Alert.AlertType.ERROR,
                        languageManager.getText("barcode.alert.generation.failed.title"),
                        languageManager.getText("barcode.alert.generation.failed.message").replace("{0}", getException().getMessage()));
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

            // ORIGINAL: Store in original maps (this will be overwritten for multiple children, but that's OK for UI)
            contactUplatnicaHtmlMap.put(contact, uplatnicaHtml);
            contactBarcodeImageMap.put(contact, barcodeImage);

            // NEW: ALSO store in email-specific maps with unique keys
            String emailKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");
            allGeneratedHtmlMap.put(emailKey, uplatnicaHtml);
            allGeneratedBarcodeMap.put(emailKey, barcodeImage);
            if (underagedMember != null) {
                allGeneratedUnderagedMap.put(emailKey, underagedMember);
            }

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

    private String generateUplatnicaHTMLWithTemplate(Contact contact, UnderagedMember underagedMember,
                                                     PaymentAttachment template) throws Exception {
        // Get barcode image
        String slipKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");
        BufferedImage barcodeImage = allGeneratedBarcodeMap.get(slipKey);
        if (barcodeImage == null) {
            barcodeImage = contactBarcodeImageMap.get(contact);
        }

        // Convert barcode image to base64 for embedding
        String barcodeBase64 = encodeImageToBase64(barcodeImage);

        // Create variables map for template processing
        Map<String, String> variables = createVariableMap(contact, underagedMember, barcodeBase64);

        // Process template variables
        return processTemplate(template.getHtmlContent(), variables);
    }

    private Map<String, String> createVariableMap(Contact contact, UnderagedMember underagedMember, String barcodeBase64) {
        Map<String, String> variables = new HashMap<>();

        // Payer information
        String payerName = contact.getFirstName() + " " + contact.getLastName();
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }

        // Recipient information
        String recipientName = organization.getName();
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }

        // Amount formatting
        String amountDisplay = paymentTemplate.getAmount().toString();

        // Reference number
        String reference = processReferenceTemplate(paymentTemplate.getPozivNaBroj(), contact, underagedMember);

        // Description
        String description = TemplateProcessor.processTemplate(
                paymentTemplate.getDescription(), contact, underagedMember);

        // Populate variables map
        variables.put("PAYER_NAME", payerName);
        variables.put("PAYER_ADDRESS", payerAddress);
        variables.put("PAYER_CITY", payerCity);
        variables.put("AMOUNT", amountDisplay);
        variables.put("RECIPIENT_IBAN", organization.getIban());
        variables.put("RECIPIENT_NAME", recipientName);
        variables.put("RECIPIENT_ADDRESS", recipientAddress);
        variables.put("RECIPIENT_CITY", recipientCity);
        variables.put("MODEL", paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "");
        variables.put("REFERENCE", reference);
        variables.put("PURPOSE", "");
        variables.put("DESCRIPTION", description);
        variables.put("BARCODE_BASE64", barcodeBase64);

        return variables;
    }

    private String processTemplate(String template, Map<String, String> variables) {
        String result = template;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private void addPreviewToContainer(Contact contact, String uplatnicaHtml, UnderagedMember underagedMember) {
        // Create a container for this contact's preview
        VBox contactPreviewContainer = new VBox(10);
        contactPreviewContainer.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-color: white; -fx-padding: 15;");

        // Contact header - show if underage member is specified
        String headerText = "👤 " + contact.getFirstName() + " " + contact.getLastName();
        if (underagedMember != null) {
            headerText += " 👶 (" + languageManager.getText("barcode.contact.child.label").replace("{0}",
                    underagedMember.getFirstName() + " " + underagedMember.getLastName()) + ")";
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
            contactInfo.append("\n👶 ").append(languageManager.getText("barcode.contact.child.details").replace("{0}", String.valueOf(underagedMember.getAge())));
            if (underagedMember.getBirthDate() != null) {
                contactInfo.append(languageManager.getText("barcode.contact.child.born").replace("{0}", underagedMember.getBirthDate().toString()));
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

        Button printIndividualButton = new Button("🖨️ " + languageManager.getText("barcode.button.print.this"));
        printIndividualButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px;");
        printIndividualButton.setOnAction(e -> printIndividualUplatnica(webView));

        Button saveIndividualButton = new Button("💾 " + languageManager.getText("barcode.button.save.this"));
        saveIndividualButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px;");
        saveIndividualButton.setOnAction(e -> saveIndividualUplatnica(contact, underagedMember));

        Button emailIndividualButton = new Button("📧 " + languageManager.getText("barcode.button.email.this"));
        emailIndividualButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 11px;");
        emailIndividualButton.setOnAction(e -> emailIndividualUplatnica(contact, underagedMember));

        actionButtons.getChildren().addAll(printIndividualButton, saveIndividualButton, emailIndividualButton);

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
        // ALSO clear email maps
        allGeneratedHtmlMap.clear();
        allGeneratedBarcodeMap.clear();
        allGeneratedUnderagedMap.clear();
        allPreviewsScrollPane.setContent(previewPlaceholder);
    }

    // EMAIL FUNCTIONALITY STARTS HERE - FIXED TO WORK WITH MULTIPLE SLIPS

    private void emailAllGeneratedSlips() {
        if (allGeneratedHtmlMap.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("barcode.email.no.slips.title"),
                    languageManager.getText("barcode.email.no.slips.message"));
            return;
        }

        // Check Gmail connection
        if (!isGmailConnected()) {
            showAlert(Alert.AlertType.INFORMATION,
                    languageManager.getText("barcode.email.not.connected.title"),
                    languageManager.getText("barcode.email.not.connected.message"));
            return;
        }

        // Show email composition dialog for bulk emails
        showBulkEmailCompositionDialog();
    }

    private void showBulkEmailCompositionDialog() {
        try {
            // Create custom dialog
            Dialog<ButtonType> emailDialog = new Dialog<>();
            emailDialog.setTitle(languageManager.getText("barcode.email.bulk.title"));
            emailDialog.setHeaderText(languageManager.getText("barcode.email.bulk.header"));
            emailDialog.initOwner(dialog);

            // Create form content
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            // Gmail status
            VBox statusBox = new VBox(5);
            statusBox.setStyle("-fx-border-color: #d4edda; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #d1ecf1;");

            Label statusTitle = new Label("📧 " + languageManager.getText("barcode.email.status.title"));
            statusTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            statusTitle.setStyle("-fx-text-fill: #155724;");

            String statusText = languageManager.getText("barcode.email.status.connected").replace("{0}", getGmailUserEmail());
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle("-fx-text-fill: #155724; -fx-font-size: 11px;");

            statusBox.getChildren().addAll(statusTitle, statusLabel);

            // Template selection
            VBox templateBox = new VBox(10);
            templateBox.setStyle("-fx-border-color: #fff3e0; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fffbf5;");

            Label templateTitle = new Label("📄 " + languageManager.getText("barcode.email.template.title"));
            templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            templateTitle.setStyle("-fx-text-fill: #f57c00;");

            ComboBox<PaymentAttachment> templateCombo = new ComboBox<>();
            templateCombo.setPromptText(languageManager.getText("barcode.email.template.prompt"));
            templateCombo.setPrefWidth(400);

            // Load templates
            try {
                if (paymentAttachmentDAO == null) {
                    paymentAttachmentDAO = new PaymentAttachmentDAO();
                }
                List<PaymentAttachment> templates = paymentAttachmentDAO.findAll();
                templateCombo.getItems().addAll(templates);
                if (selectedPaymentSlipTemplate != null) {
                    templateCombo.setValue(selectedPaymentSlipTemplate);
                }
            } catch (Exception e) {
                System.err.println("Error loading templates: " + e.getMessage());
            }

            templateBox.getChildren().addAll(templateTitle, templateCombo);

            // Email content customization
            VBox emailContentBox = new VBox(10);
            emailContentBox.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

            Label contentTitle = new Label("✉️ " + languageManager.getText("barcode.email.content.title"));
            contentTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            contentTitle.setStyle("-fx-text-fill: #1976d2;");

            // Subject field
            TextField subjectField = new TextField(languageManager.getText("barcode.email.subject.default").replace("{0}", organization.getName()));
            subjectField.setPromptText(languageManager.getText("barcode.email.subject.prompt"));

            // Message options
            RadioButton useTemplateMessage = new RadioButton(languageManager.getText("barcode.email.message.template"));
            RadioButton useCustomMessage = new RadioButton(languageManager.getText("barcode.email.message.custom"));
            ToggleGroup messageGroup = new ToggleGroup();
            useTemplateMessage.setToggleGroup(messageGroup);
            useCustomMessage.setToggleGroup(messageGroup);
            useTemplateMessage.setSelected(true);

            // Custom message area
            TextArea customMessageArea = new TextArea();
            customMessageArea.setPrefRowCount(8);
            customMessageArea.setWrapText(true);
            customMessageArea.setDisable(true);
            customMessageArea.setPromptText(languageManager.getText("barcode.email.message.prompt"));

            // Toggle custom message area
            useCustomMessage.setOnAction(e -> customMessageArea.setDisable(false));
            useTemplateMessage.setOnAction(e -> customMessageArea.setDisable(true));

            emailContentBox.getChildren().addAll(
                    contentTitle,
                    new Label(languageManager.getText("barcode.email.subject.label")), subjectField,
                    new Label(languageManager.getText("barcode.email.message.label")),
                    useTemplateMessage, useCustomMessage,
                    customMessageArea
            );

            // Attachment options
            VBox attachmentBox = new VBox(10);
            attachmentBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fff8;");

            Label attachmentTitle = new Label("📎 " + languageManager.getText("barcode.email.attachments.title"));
            attachmentTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            attachmentTitle.setStyle("-fx-text-fill: #2e7d32;");

            CheckBox includePdfCheckBox = new CheckBox(languageManager.getText("barcode.email.attachments.pdf"));
            includePdfCheckBox.setSelected(true);

            CheckBox includeBarcodeCheckBox = new CheckBox(languageManager.getText("barcode.email.attachments.barcode"));
            includeBarcodeCheckBox.setSelected(true);

            attachmentBox.getChildren().addAll(
                    attachmentTitle,
                    includePdfCheckBox,
                    includeBarcodeCheckBox
            );

            content.getChildren().addAll(statusBox, templateBox, emailContentBox, attachmentBox);

            emailDialog.getDialogPane().setContent(content);
            emailDialog.getDialogPane().setPrefWidth(700);
            emailDialog.getDialogPane().setPrefHeight(700);

            // Add buttons
            ButtonType sendButton = new ButtonType(languageManager.getText("barcode.email.button.send.all"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType(languageManager.getText("barcode.email.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            emailDialog.getDialogPane().getButtonTypes().addAll(sendButton, cancelButton);

            // Handle send button click
            Optional<ButtonType> result = emailDialog.showAndWait();
            if (result.isPresent() && result.get() == sendButton) {
                // Validate fields
                if (subjectField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("barcode.email.validation.subject.title"),
                            languageManager.getText("barcode.email.validation.subject.message"));
                    return;
                }

                if (useCustomMessage.isSelected() && customMessageArea.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("barcode.email.validation.message.title"),
                            languageManager.getText("barcode.email.validation.message.content"));
                    return;
                }

                // Send the emails with user's settings
                sendBulkEmailsWithSettings(
                        templateCombo.getValue(),
                        subjectField.getText().trim(),
                        useCustomMessage.isSelected() ? customMessageArea.getText().trim() : null,
                        includePdfCheckBox.isSelected(),
                        includeBarcodeCheckBox.isSelected()
                );
            }

        } catch (Exception e) {
            System.err.println("Error showing bulk email dialog: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("barcode.dialog.error.title"),
                    languageManager.getText("barcode.dialog.error.message").replace("{0}", e.getMessage()));
        }
    }

    private void sendBulkEmailsWithSettings(PaymentAttachment selectedTemplate, String subject,
                                            String customMessage, boolean includePdf, boolean includeBarcode) {
        // Create progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle(languageManager.getText("barcode.email.progress.title"));
        progressAlert.setHeaderText(languageManager.getText("barcode.email.progress.header"));
        progressAlert.setContentText(languageManager.getText("barcode.email.progress.wait"));
        progressAlert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        progressAlert.initOwner(dialog);
        progressAlert.show();

        // Send emails in background thread
        new Thread(() -> {
            int successCount = 0;
            int failCount = 0;
            int totalCount = 0;

            // Process each generated slip individually
            for (Map.Entry<String, String> entry : allGeneratedHtmlMap.entrySet()) {
                String slipKey = entry.getKey();

                // Get contact for this slip
                int contactId = getContactIdFromKey(slipKey);
                Optional<Contact> contactOpt = selectedContacts.stream()
                        .filter(c -> c.getId() == contactId)
                        .findFirst();

                if (contactOpt.isEmpty()) {
                    continue; // Skip if contact not found
                }

                Contact contact = contactOpt.get();

                if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
                    continue; // Skip contacts without email
                }

                totalCount++;
                int finalTotalCount = totalCount;

                // Get underage member for this slip (if any)
                UnderagedMember underagedMember = allGeneratedUnderagedMap.get(slipKey);

                Platform.runLater(() -> {
                    String emailTarget = contact.getEmail();
                    if (underagedMember != null) {
                        emailTarget += " " + languageManager.getText("barcode.email.progress.for.child").replace("{0}",
                                underagedMember.getFirstName() + " " + underagedMember.getLastName());
                    }
                    progressAlert.setContentText(languageManager.getText("barcode.email.progress.sending")
                            .replace("{0}", String.valueOf(finalTotalCount))
                            .replace("{1}", emailTarget));
                });

                try {
                    // Generate email message
                    String emailMessage = customMessage != null ? customMessage :
                            generateDefaultEmailMessage(contact, underagedMember);

                    // Prepare attachments
                    byte[] pdfContent = null;
                    BufferedImage barcodeImage = null;

                    if (includePdf) {
                        pdfContent = generatePdfContentWithTemplate(contact, underagedMember, selectedTemplate);
                    }

                    if (includeBarcode) {
                        barcodeImage = allGeneratedBarcodeMap.get(slipKey);
                    }

                    // Send email using OAuth Manager
                    boolean success;
                    if (pdfContent != null || barcodeImage != null) {
                        // Send with attachments
                        String payerName = contact.getFirstName() + " " + contact.getLastName();
                        String organizationName = organization.getName();
                        String amountDisplay = paymentTemplate.getAmount().toString();
                        String description = TemplateProcessor.processTemplate(
                                paymentTemplate.getDescription(), contact, underagedMember);

                        success = oauthManager.sendPaymentSlip(
                                contact.getEmail(), payerName, organizationName,
                                amountDisplay + " EUR", description, pdfContent, barcodeImage
                        );
                    } else {
                        // Send simple email
                        success = oauthManager.sendEmail(contact.getEmail(), subject, emailMessage);
                    }

                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }

                } catch (Exception e) {
                    System.err.println("Error sending email to " + contact.getEmail() + ": " + e.getMessage());
                    failCount++;
                }

                // Small delay between emails
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Update UI on completion
            final int finalSuccessCount = successCount;
            final int finalFailCount = failCount;
            Platform.runLater(() -> {
                progressAlert.close();

                String templateName = selectedTemplate != null ? selectedTemplate.getName() : "Default Croatian Template";
                if (finalFailCount == 0) {
                    showAlert(Alert.AlertType.INFORMATION,
                            languageManager.getText("barcode.email.success.title"),
                            languageManager.getText("barcode.email.success.message")
                                    .replace("{0}", String.valueOf(finalSuccessCount))
                                    .replace("{1}", templateName));
                } else {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("barcode.email.partial.title"),
                            languageManager.getText("barcode.email.partial.message")
                                    .replace("{0}", String.valueOf(finalSuccessCount))
                                    .replace("{1}", String.valueOf(finalFailCount))
                                    .replace("{2}", templateName));
                }
            });

        }).start();
    }

    private String generateDefaultEmailMessage(Contact contact, UnderagedMember underagedMember) {
        String payerName = contact.getFirstName() + " " + contact.getLastName();
        String organizationName = organization.getName();

        // Get amount for display
        String amountDisplay = paymentTemplate.getAmount().toString();

        // Get description using TemplateProcessor
        String description = "";
        if (paymentTemplate != null) {
            description = TemplateProcessor.processTemplate(
                    paymentTemplate.getDescription(), contact, underagedMember);
        }

        return languageManager.getText("barcode.email.default.dear").replace("{0}", payerName) + ",\n\n" +
                languageManager.getText("barcode.email.default.intro").replace("{0}", organizationName) + "\n\n" +
                languageManager.getText("barcode.email.default.details") + "\n" +
                languageManager.getText("barcode.email.default.amount").replace("{0}", amountDisplay) + "\n" +
                languageManager.getText("barcode.email.default.description").replace("{0}", description) + "\n" +
                languageManager.getText("barcode.email.default.generated").replace("{0}",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) + "\n\n" +
                languageManager.getText("barcode.email.default.instructions") + "\n" +
                languageManager.getText("barcode.email.default.instruction.1") + "\n" +
                languageManager.getText("barcode.email.default.instruction.2") + "\n" +
                languageManager.getText("barcode.email.default.instruction.3") + "\n\n" +
                languageManager.getText("barcode.email.default.questions").replace("{0}", organizationName) + "\n\n" +
                languageManager.getText("barcode.email.default.regards") + "\n" +
                organizationName + "\n\n" +
                "---\n" +
                languageManager.getText("barcode.email.default.automated");
    }

    private byte[] generatePdfContentWithTemplate(Contact contact, UnderagedMember underagedMember,
                                                  PaymentAttachment selectedTemplate) throws Exception {
        String htmlContent;

        // Use selected template or fallback to Croatian template
        if (selectedTemplate != null && selectedTemplate.getHtmlContent() != null && !selectedTemplate.getHtmlContent().trim().isEmpty()) {
            htmlContent = generateUplatnicaHTMLWithTemplate(contact, underagedMember, selectedTemplate);
        } else {
            // Use the Croatian template from UplatnicaHtmlGenerator utility
            String slipKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");
            BufferedImage barcodeImage = allGeneratedBarcodeMap.get(slipKey);
            if (barcodeImage == null) {
                barcodeImage = contactBarcodeImageMap.get(contact);
            }

            htmlContent = UplatnicaHtmlGenerator.generateUplatnicaHtml(
                    contact, organization, paymentTemplate, barcodeImage, underagedMember);
        }

        // Convert HTML to PDF bytes
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        try {
            HtmlConverter.convertToPdf(htmlContent, pdfOutputStream);
            return pdfOutputStream.toByteArray();
        } finally {
            pdfOutputStream.close();
        }
    }

    private int getContactIdFromKey(String key) {
        if (key.contains("_child_")) {
            return Integer.parseInt(key.substring(0, key.indexOf("_child_")));
        } else {
            return Integer.parseInt(key);
        }
    }

    private long getSlipCountForContact(int contactId) {
        return allGeneratedHtmlMap.keySet().stream()
                .filter(key -> getContactIdFromKey(key) == contactId)
                .count();
    }

    private void emailIndividualUplatnica(Contact contact, UnderagedMember underagedMember) {
        // Check if contact has email
        if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("barcode.email.no.address.title"),
                    languageManager.getText("barcode.email.no.address.message"));
            return;
        }

        // Check Gmail connection
        if (!isGmailConnected()) {
            showAlert(Alert.AlertType.INFORMATION,
                    languageManager.getText("barcode.email.not.connected.title"),
                    languageManager.getText("barcode.email.not.connected.message"));
            return;
        }

        // Show individual email composition dialog
        showIndividualEmailCompositionDialog(contact, underagedMember);
    }

    private void showIndividualEmailCompositionDialog(Contact contact, UnderagedMember underagedMember) {
        try {
            // Create custom dialog
            Dialog<ButtonType> emailDialog = new Dialog<>();
            emailDialog.setTitle(languageManager.getText("barcode.email.individual.title"));
            emailDialog.setHeaderText(languageManager.getText("barcode.email.individual.header").replace("{0}", contact.getEmail()));
            emailDialog.initOwner(dialog);

            // Create form content
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            // Gmail status
            VBox statusBox = new VBox(5);
            statusBox.setStyle("-fx-border-color: #d4edda; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #d1ecf1;");

            Label statusTitle = new Label("📧 " + languageManager.getText("barcode.email.status.title"));
            statusTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            statusTitle.setStyle("-fx-text-fill: #155724;");

            String statusText = languageManager.getText("barcode.email.status.connected").replace("{0}", getGmailUserEmail());
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle("-fx-text-fill: #155724; -fx-font-size: 11px;");

            statusBox.getChildren().addAll(statusTitle, statusLabel);

            // Template selection
            VBox templateBox = new VBox(10);
            templateBox.setStyle("-fx-border-color: #fff3e0; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fffbf5;");

            Label templateTitle = new Label("📄 " + languageManager.getText("barcode.email.template.title"));
            templateTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            templateTitle.setStyle("-fx-text-fill: #f57c00;");

            ComboBox<PaymentAttachment> templateCombo = new ComboBox<>();
            templateCombo.setPromptText(languageManager.getText("barcode.email.template.prompt"));
            templateCombo.setPrefWidth(400);

            // Load templates
            try {
                if (paymentAttachmentDAO == null) {
                    paymentAttachmentDAO = new PaymentAttachmentDAO();
                }
                List<PaymentAttachment> templates = paymentAttachmentDAO.findAll();
                templateCombo.getItems().addAll(templates);
                if (selectedPaymentSlipTemplate != null) {
                    templateCombo.setValue(selectedPaymentSlipTemplate);
                }
            } catch (Exception e) {
                System.err.println("Error loading templates: " + e.getMessage());
            }

            templateBox.getChildren().addAll(templateTitle, templateCombo);

            // Email fields
            VBox emailFieldsBox = new VBox(10);
            emailFieldsBox.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fdff;");

            Label fieldsTitle = new Label("📝 " + languageManager.getText("barcode.email.details.title"));
            fieldsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            fieldsTitle.setStyle("-fx-text-fill: #1976d2;");

            // To field (read-only)
            TextField toField = new TextField(contact.getEmail());
            toField.setEditable(false);
            toField.setStyle("-fx-background-color: #f5f5f5;");

            // Subject field (pre-filled, editable)
            TextField subjectField = new TextField(languageManager.getText("barcode.email.subject.default").replace("{0}", organization.getName()));
            subjectField.setPromptText(languageManager.getText("barcode.email.subject.prompt"));

            // Message options
            RadioButton useTemplateMessage = new RadioButton(languageManager.getText("barcode.email.message.template"));
            RadioButton useCustomMessage = new RadioButton(languageManager.getText("barcode.email.message.custom"));
            ToggleGroup messageGroup = new ToggleGroup();
            useTemplateMessage.setToggleGroup(messageGroup);
            useCustomMessage.setToggleGroup(messageGroup);
            useTemplateMessage.setSelected(true);

            // Message field (pre-filled, editable)
            TextArea messageArea = new TextArea();
            messageArea.setPrefRowCount(8);
            messageArea.setWrapText(true);
            messageArea.setText(generateDefaultEmailMessage(contact, underagedMember));
            messageArea.setDisable(true);
            messageArea.setPromptText(languageManager.getText("barcode.email.message.enter"));

            // Toggle message area
            useCustomMessage.setOnAction(e -> {
                messageArea.setDisable(false);
                if (messageArea.getText().equals(generateDefaultEmailMessage(contact, underagedMember))) {
                    messageArea.clear();
                }
            });
            useTemplateMessage.setOnAction(e -> {
                messageArea.setDisable(true);
                messageArea.setText(generateDefaultEmailMessage(contact, underagedMember));
            });

            emailFieldsBox.getChildren().addAll(
                    fieldsTitle,
                    new Label(languageManager.getText("barcode.email.to.label")), toField,
                    new Label(languageManager.getText("barcode.email.subject.label")), subjectField,
                    new Label(languageManager.getText("barcode.email.message.label")),
                    useTemplateMessage, useCustomMessage,
                    messageArea
            );

            // Attachment options
            VBox attachmentBox = new VBox(10);
            attachmentBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f8fff8;");

            Label attachmentTitle = new Label("📎 " + languageManager.getText("barcode.email.attachments.title"));
            attachmentTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            attachmentTitle.setStyle("-fx-text-fill: #2e7d32;");

            CheckBox includePdfCheckBox = new CheckBox(languageManager.getText("barcode.email.attachments.pdf"));
            includePdfCheckBox.setSelected(true);

            CheckBox includeBarcodeCheckBox = new CheckBox(languageManager.getText("barcode.email.attachments.barcode"));
            includeBarcodeCheckBox.setSelected(true);

            attachmentBox.getChildren().addAll(
                    attachmentTitle,
                    includePdfCheckBox,
                    includeBarcodeCheckBox
            );

            content.getChildren().addAll(statusBox, templateBox, emailFieldsBox, attachmentBox);

            emailDialog.getDialogPane().setContent(content);
            emailDialog.getDialogPane().setPrefWidth(700);
            emailDialog.getDialogPane().setPrefHeight(700);

            // Add buttons
            ButtonType sendButton = new ButtonType(languageManager.getText("barcode.email.button.send"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType(languageManager.getText("barcode.email.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            emailDialog.getDialogPane().getButtonTypes().addAll(sendButton, cancelButton);

            // Handle send button click
            Optional<ButtonType> result = emailDialog.showAndWait();
            if (result.isPresent() && result.get() == sendButton) {
                // Validate fields
                if (subjectField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("barcode.email.validation.subject.title"),
                            languageManager.getText("barcode.email.validation.subject.message"));
                    return;
                }

                if (useCustomMessage.isSelected() && messageArea.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING,
                            languageManager.getText("barcode.email.validation.message.title"),
                            languageManager.getText("barcode.email.validation.message.individual"));
                    return;
                }

                // Send the email with user's input
                sendIndividualEmailWithSettings(
                        contact, underagedMember,
                        templateCombo.getValue(),
                        subjectField.getText().trim(),
                        useCustomMessage.isSelected() ? messageArea.getText().trim() : generateDefaultEmailMessage(contact, underagedMember),
                        includePdfCheckBox.isSelected(),
                        includeBarcodeCheckBox.isSelected()
                );
            }

        } catch (Exception e) {
            System.err.println("Error showing individual email dialog: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("barcode.dialog.error.title"),
                    languageManager.getText("barcode.dialog.error.message").replace("{0}", e.getMessage()));
        }
    }

    private void sendIndividualEmailWithSettings(Contact contact, UnderagedMember underagedMember,
                                                 PaymentAttachment selectedTemplate, String subject, String message,
                                                 boolean includePdf, boolean includeBarcode) {
        // Show progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle(languageManager.getText("barcode.email.progress.individual.title"));
        progressAlert.setHeaderText(languageManager.getText("barcode.email.progress.individual.header"));
        String templateName = selectedTemplate != null ? selectedTemplate.getName() : "Default Croatian Template";
        progressAlert.setContentText(languageManager.getText("barcode.email.progress.individual.sending")
                .replace("{0}", contact.getEmail())
                .replace("{1}", templateName));
        progressAlert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        progressAlert.initOwner(dialog);
        progressAlert.show();

        // Send email in background thread
        new Thread(() -> {
            try {
                // Prepare attachments
                byte[] pdfContent = null;
                BufferedImage barcodeImage = null;

                if (includePdf) {
                    pdfContent = generatePdfContentWithTemplate(contact, underagedMember, selectedTemplate);
                }

                if (includeBarcode) {
                    String slipKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");
                    barcodeImage = allGeneratedBarcodeMap.get(slipKey);
                    if (barcodeImage == null) {
                        barcodeImage = contactBarcodeImageMap.get(contact);
                    }
                }

                // Send email using OAuth Manager
                boolean success;
                if (pdfContent != null || barcodeImage != null) {
                    // Send with attachments using the payment slip method
                    String payerName = contact.getFirstName() + " " + contact.getLastName();
                    String organizationName = organization.getName();
                    String amountDisplay = paymentTemplate.getAmount().toString();
                    String description = TemplateProcessor.processTemplate(
                            paymentTemplate.getDescription(), contact, underagedMember);

                    success = oauthManager.sendPaymentSlip(
                            contact.getEmail(), payerName, organizationName,
                            amountDisplay + " EUR", description, pdfContent, barcodeImage
                    );
                } else {
                    // Send simple email
                    success = oauthManager.sendEmail(contact.getEmail(), subject, message);
                }

                Platform.runLater(() -> {
                    progressAlert.close();
                    if (success) {
                        String successMessage = languageManager.getText("barcode.email.success.individual.message")
                                .replace("{0}", contact.getEmail())
                                .replace("{1}", templateName);
                        if (underagedMember != null) {
                            successMessage += " " + languageManager.getText("barcode.email.success.individual.child")
                                    .replace("{0}", underagedMember.getFirstName() + " " + underagedMember.getLastName());
                        }
                        showAlert(Alert.AlertType.INFORMATION,
                                languageManager.getText("barcode.email.success.individual.title"),
                                successMessage);
                    } else {
                        showAlert(Alert.AlertType.ERROR,
                                languageManager.getText("barcode.email.failed.title"),
                                languageManager.getText("barcode.email.failed.message").replace("{0}", contact.getEmail()));
                    }
                });

            } catch (Exception e) {
                System.err.println("Error sending individual email: " + e.getMessage());
                Platform.runLater(() -> {
                    progressAlert.close();
                    showAlert(Alert.AlertType.ERROR,
                            languageManager.getText("barcode.email.failed.title"),
                            languageManager.getText("barcode.email.failed.error").replace("{0}", e.getMessage()));
                });
            }
        }).start();
    }

    private byte[] convertHtmlToPdf(String htmlContent) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        try {
            HtmlConverter.convertToPdf(htmlContent, pdfOutputStream);
            return pdfOutputStream.toByteArray();
        } finally {
            pdfOutputStream.close();
        }
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
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("barcode.print.error.title"),
                    languageManager.getText("barcode.print.error.message").replace("{0}", e.getMessage()));
        }
    }

    private void saveIndividualUplatnica(Contact contact, UnderagedMember underagedMember) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(languageManager.getText("barcode.save.title"));
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

                // Try to get HTML from generated maps first, then fallback to original maps
                String slipKey = contact.getId() + (underagedMember != null ? "_child_" + underagedMember.getId() : "");
                String uplatnicaHtml = allGeneratedHtmlMap.get(slipKey);
                BufferedImage barcodeImage = allGeneratedBarcodeMap.get(slipKey);

                if (uplatnicaHtml == null) {
                    uplatnicaHtml = contactUplatnicaHtmlMap.get(contact);
                    barcodeImage = contactBarcodeImageMap.get(contact);
                }

                // Save HTML
                if (uplatnicaHtml != null) {
                    File htmlFile = new File(selectedDirectory, fileName + ".html");
                    try (FileWriter writer = new FileWriter(htmlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                        writer.write(uplatnicaHtml);
                    }
                }

                // Save barcode image
                if (barcodeImage != null) {
                    File imageFile = new File(selectedDirectory, "barcode_" + fileName + ".png");
                    ImageIO.write(barcodeImage, "png", imageFile);
                }

                String successMessage = languageManager.getText("barcode.save.success.message").replace("{0}",
                        contact.getFirstName() + " " + contact.getLastName());
                if (underagedMember != null) {
                    successMessage += " " + languageManager.getText("barcode.save.success.child").replace("{0}",
                            underagedMember.getFirstName() + " " + underagedMember.getLastName());
                }
                showAlert(Alert.AlertType.INFORMATION,
                        languageManager.getText("barcode.save.success.title"),
                        successMessage);

            } catch (Exception e) {
                System.err.println("Error saving individual uplatnica: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR,
                        languageManager.getText("barcode.save.error.title"),
                        languageManager.getText("barcode.save.error.message").replace("{0}", e.getMessage()));
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

        // 4. Payer name - normalize for HUB3
        hub3Data.append(normalizeTextForHUB3(contact.getFirstName() + " " + contact.getLastName())).append("\n");

        // 5. Payer address - normalize for HUB3
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        hub3Data.append(normalizeTextForHUB3(payerAddress)).append("\n");

        // 6. Payer city - normalize for HUB3
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }
        hub3Data.append(normalizeTextForHUB3(payerCity)).append("\n");

        // 7. Recipient name - normalize for HUB3
        hub3Data.append(normalizeTextForHUB3(organization.getName())).append("\n");

        // 8. Recipient address - normalize for HUB3
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        hub3Data.append(normalizeTextForHUB3(recipientAddress)).append("\n");

        // 9. Recipient city - normalize for HUB3
        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }
        hub3Data.append(normalizeTextForHUB3(recipientCity)).append("\n");

        // 10. Recipient IBAN
        hub3Data.append(organization.getIban()).append("\n");

        // 11. Payment model
        hub3Data.append(paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "").append("\n");

        // 12. Reference number - UPDATED TO HANDLE DYNAMIC REFERENCES
        String reference = processReferenceTemplate(paymentTemplate.getPozivNaBroj(), contact, underagedMember);
        hub3Data.append(reference).append("\n");

        // 13. Purpose code
        hub3Data.append("").append("\n");

        // 14. Description (no newline at end) - Process with specific underage member and normalize for HUB3
        String processedDescription = TemplateProcessor.processTemplate(
                paymentTemplate.getDescription(), contact, underagedMember);
        hub3Data.append(normalizeTextForHUB3(processedDescription));

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
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-2");
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
                info.append(" 👶 ").append(LanguageManager.getInstance().getText("barcode.contact.underage"));
                if (underagedInfo != null) {
                    info.append(" (").append(LanguageManager.getInstance().getText("barcode.contact.member").replace("{0}", underagedInfo)).append(")");
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

                // Update status with localized text
                if (item.isGenerated()) {
                    statusLabel.setText("✅ " + LanguageManager.getInstance().getText("barcode.contact.status.generated"));
                    statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 10px; -fx-font-weight: bold;");
                } else if (item.getError() != null) {
                    statusLabel.setText("❌ " + LanguageManager.getInstance().getText("barcode.contact.status.error").replace("{0}", item.getError()));
                    statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 10px; -fx-font-weight: bold;");
                } else {
                    statusLabel.setText("⏳ " + LanguageManager.getInstance().getText("barcode.contact.status.pending"));
                    statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 10px; -fx-font-weight: bold;");
                }

                setGraphic(container);
            }
        }
    }
}