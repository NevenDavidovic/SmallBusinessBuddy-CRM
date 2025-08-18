package smallbusinessbuddycrm.controllers.utilities;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.ListsDAO;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.List;
import smallbusinessbuddycrm.services.google.GoogleOAuthManager;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class NewsletterSendDialog {

    private Stage dialog;
    private GoogleOAuthManager oauthManager;
    private ListsDAO listsDAO;
    private ContactDAO contactDAO;
    private LanguageManager languageManager;

    // Dialog components
    private ComboBox<List> listSelector;
    private TextField subjectField;
    private TextArea previewArea;
    private Label statusLabel;
    private Label recipientCountLabel;
    private ProgressBar progressBar;
    private Button sendButton;
    private Button cancelButton;

    // UI Labels that need to be updated on language change
    private Label titleLabel;
    private Label gmailStatusTitleLabel;
    private Label gmailStatusLabel;
    private Label listTitleLabel;
    private Label selectListLabel;
    private Button refreshListsButton;
    private Label compositionTitleLabel;
    private Label subjectLabel;
    private Label fromLabel;
    private Label fromValueLabel;
    private Label previewTitleLabel;
    private Label previewNoteLabel;
    private Label progressTitleLabel;

    // Newsletter data
    private String newsletterTitle;
    private String newsletterHtml;
    private String companyName;

    // Send state
    private volatile boolean isSending = false;
    private Thread sendingThread;

    public NewsletterSendDialog(Stage parentStage, String title, String htmlContent, String company) {
        this.newsletterTitle = title != null ? title : "Newsletter";
        this.newsletterHtml = htmlContent;
        this.companyName = company != null ? company : "Your Company";
        this.languageManager = LanguageManager.getInstance();

        this.oauthManager = GoogleOAuthManager.getInstance();
        this.listsDAO = new ListsDAO();
        this.contactDAO = new ContactDAO();

        createDialog(parentStage);

        // Add language change listener
        languageManager.addLanguageChangeListener(this::updateTexts);
        updateTexts();
    }

    private void updateTexts() {
        // Update dialog title
        if (dialog != null) {
            dialog.setTitle("📧 " + languageManager.getText("newsletter.send.dialog.title")
                    .replace("{0}", newsletterTitle));
        }

        // Update main title
        if (titleLabel != null) {
            titleLabel.setText("📧 " + languageManager.getText("newsletter.send.main.title"));
        }

        // Update Gmail status
        if (gmailStatusTitleLabel != null) {
            gmailStatusTitleLabel.setText("📧 " + languageManager.getText("newsletter.send.gmail.status.title"));
        }
        if (gmailStatusLabel != null) {
            updateGmailStatusLabel();
        }

        // Update list selection
        if (listTitleLabel != null) {
            listTitleLabel.setText("👥 " + languageManager.getText("newsletter.send.list.title"));
        }
        if (selectListLabel != null) {
            selectListLabel.setText(languageManager.getText("newsletter.send.list.select.label"));
        }
        if (listSelector != null) {
            listSelector.setPromptText(languageManager.getText("newsletter.send.list.prompt"));
        }
        if (refreshListsButton != null) {
            refreshListsButton.setText("🔄 " + languageManager.getText("newsletter.send.button.refresh"));
        }

        // Update composition section
        if (compositionTitleLabel != null) {
            compositionTitleLabel.setText("✏️ " + languageManager.getText("newsletter.send.composition.title"));
        }
        if (subjectLabel != null) {
            subjectLabel.setText(languageManager.getText("newsletter.send.subject.label"));
        }
        if (subjectField != null) {
            subjectField.setPromptText(languageManager.getText("newsletter.send.subject.prompt"));
        }
        if (fromLabel != null) {
            fromLabel.setText(languageManager.getText("newsletter.send.from.label"));
        }
        if (fromValueLabel != null) {
            updateFromValueLabel();
        }

        // Update preview section
        if (previewTitleLabel != null) {
            previewTitleLabel.setText("👁️ " + languageManager.getText("newsletter.send.preview.title"));
        }
        if (previewNoteLabel != null) {
            previewNoteLabel.setText(languageManager.getText("newsletter.send.preview.note"));
        }
        if (previewArea != null) {
            previewArea.setText(generatePreviewText());
        }

        // Update progress section
        if (progressTitleLabel != null) {
            progressTitleLabel.setText("📊 " + languageManager.getText("newsletter.send.progress.title"));
        }

        // Update buttons
        if (cancelButton != null) {
            cancelButton.setText(languageManager.getText("newsletter.send.button.cancel"));
        }

        // Update recipient count and UI state
        updateRecipientCount();
        updateUI();
    }

    private void updateGmailStatusLabel() {
        if (gmailStatusLabel != null) {
            String statusText = oauthManager.isGmailConnected()
                    ? languageManager.getText("newsletter.send.gmail.connected").replace("{0}", oauthManager.getUserEmail())
                    : "❌ " + languageManager.getText("newsletter.send.gmail.not.connected");
            gmailStatusLabel.setText(statusText);
            gmailStatusLabel.setStyle("-fx-text-fill: " + (oauthManager.isGmailConnected() ? "#155724" : "#721c24") + "; -fx-font-size: 11px;");
        }
    }

    private void updateFromValueLabel() {
        if (fromValueLabel != null) {
            String fromText = oauthManager.isGmailConnected()
                    ? oauthManager.getUserEmail() + " (" + companyName + ")"
                    : languageManager.getText("newsletter.send.gmail.not.connected");
            fromValueLabel.setText(fromText);
        }
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("📧 " + languageManager.getText("newsletter.send.dialog.title")
                .replace("{0}", newsletterTitle));
        dialog.setResizable(true);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));

        // Header
        VBox headerBox = createHeaderSection();

        // List selection
        VBox listSelectionBox = createListSelectionSection();

        // Email composition
        VBox compositionBox = createCompositionSection();

        // Preview section
        VBox previewBox = createPreviewSection();

        // Progress section
        VBox progressBox = createProgressSection();

        // Buttons
        HBox buttonBox = createButtonSection();

        mainLayout.getChildren().addAll(
                headerBox,
                new Separator(),
                listSelectionBox,
                new Separator(),
                compositionBox,
                new Separator(),
                previewBox,
                progressBox,
                buttonBox
        );

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefWidth(800);
        scrollPane.setPrefHeight(700);

        Scene scene = new Scene(scrollPane, 800, 700);
        dialog.setScene(scene);

        // Load data
        loadContactLists();
        setupEventHandlers();
        updateUI();

        // Handle close request
        dialog.setOnCloseRequest(e -> {
            if (isSending) {
                e.consume();
                showStopSendingConfirmation();
            }
        });
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(10);

        // Title
        titleLabel = new Label("📧 " + languageManager.getText("newsletter.send.main.title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Gmail status
        VBox statusBox = new VBox(5);
        statusBox.setStyle("-fx-border-color: #d4edda; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #d1ecf1;");

        gmailStatusTitleLabel = new Label("📧 " + languageManager.getText("newsletter.send.gmail.status.title"));
        gmailStatusTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        gmailStatusTitleLabel.setStyle("-fx-text-fill: #155724;");

        gmailStatusLabel = new Label();
        updateGmailStatusLabel();

        statusBox.getChildren().addAll(gmailStatusTitleLabel, gmailStatusLabel);

        headerBox.getChildren().addAll(titleLabel, statusBox);
        return headerBox;
    }

    private VBox createListSelectionSection() {
        VBox listBox = new VBox(15);
        listBox.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #f8fdff;");

        listTitleLabel = new Label("👥 " + languageManager.getText("newsletter.send.list.title"));
        listTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        listTitleLabel.setStyle("-fx-text-fill: #1976d2;");

        // List selector
        HBox listSelectorBox = new HBox(10);
        listSelectorBox.setAlignment(Pos.CENTER_LEFT);

        selectListLabel = new Label(languageManager.getText("newsletter.send.list.select.label"));
        selectListLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        listSelector = new ComboBox<>();
        listSelector.setPrefWidth(300);
        listSelector.setPromptText(languageManager.getText("newsletter.send.list.prompt"));

        refreshListsButton = new Button("🔄 " + languageManager.getText("newsletter.send.button.refresh"));
        refreshListsButton.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-border-color: #1976d2; -fx-border-radius: 4;");
        refreshListsButton.setOnAction(e -> loadContactLists());

        listSelectorBox.getChildren().addAll(selectListLabel, listSelector, refreshListsButton);

        // Recipient count
        recipientCountLabel = new Label(languageManager.getText("newsletter.send.list.select.to.see.count"));
        recipientCountLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        listBox.getChildren().addAll(listTitleLabel, listSelectorBox, recipientCountLabel);
        return listBox;
    }

    private VBox createCompositionSection() {
        VBox compositionBox = new VBox(15);
        compositionBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #f8fff8;");

        compositionTitleLabel = new Label("✏️ " + languageManager.getText("newsletter.send.composition.title"));
        compositionTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        compositionTitleLabel.setStyle("-fx-text-fill: #2e7d32;");

        // Subject field
        VBox subjectBox = new VBox(5);
        subjectLabel = new Label(languageManager.getText("newsletter.send.subject.label"));
        subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        subjectField = new TextField();
        subjectField.setText(generateDefaultSubject());
        subjectField.setPromptText(languageManager.getText("newsletter.send.subject.prompt"));
        subjectField.setPrefWidth(400);

        subjectBox.getChildren().addAll(subjectLabel, subjectField);

        // From info
        VBox fromBox = new VBox(5);
        fromLabel = new Label(languageManager.getText("newsletter.send.from.label"));
        fromLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        fromValueLabel = new Label();
        fromValueLabel.setStyle("-fx-text-fill: #666;");
        updateFromValueLabel();

        fromBox.getChildren().addAll(fromLabel, fromValueLabel);

        compositionBox.getChildren().addAll(compositionTitleLabel, subjectBox, fromBox);
        return compositionBox;
    }

    private VBox createPreviewSection() {
        VBox previewBox = new VBox(15);
        previewBox.setStyle("-fx-border-color: #fff3e0; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #fffbf5;");

        previewTitleLabel = new Label("👁️ " + languageManager.getText("newsletter.send.preview.title"));
        previewTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        previewTitleLabel.setStyle("-fx-text-fill: #f57c00;");

        // Preview text area (read-only)
        previewArea = new TextArea();
        previewArea.setPrefRowCount(8);
        previewArea.setWrapText(true);
        previewArea.setEditable(false);
        previewArea.setText(generatePreviewText());
        previewArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px; -fx-background-color: #fafafa;");

        previewNoteLabel = new Label(languageManager.getText("newsletter.send.preview.note"));
        previewNoteLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-font-style: italic;");

        previewBox.getChildren().addAll(previewTitleLabel, previewArea, previewNoteLabel);
        return previewBox;
    }

    private VBox createProgressSection() {
        VBox progressBox = new VBox(10);
        progressBox.setVisible(false);
        progressBox.setStyle("-fx-border-color: #fce4ec; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fef7f7;");

        progressTitleLabel = new Label("📊 " + languageManager.getText("newsletter.send.progress.title"));
        progressTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        progressTitleLabel.setStyle("-fx-text-fill: #c2185b;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(20);

        statusLabel = new Label(languageManager.getText("newsletter.send.status.ready"));
        statusLabel.setStyle("-fx-text-fill: #666;");

        progressBox.getChildren().addAll(progressTitleLabel, progressBar, statusLabel);

        // Store reference for showing/hiding
        progressBox.managedProperty().bind(progressBox.visibleProperty());

        return progressBox;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        cancelButton = new Button(languageManager.getText("newsletter.send.button.cancel"));
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 4;");
        cancelButton.setOnAction(e -> handleCancel());

        sendButton = new Button("📧 " + languageManager.getText("newsletter.send.button.send"));
        sendButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 4; -fx-font-weight: bold;");
        sendButton.setOnAction(e -> handleSend());

        buttonBox.getChildren().addAll(cancelButton, sendButton);
        return buttonBox;
    }

    private void setupEventHandlers() {
        // List selection change
        listSelector.setOnAction(e -> {
            updateRecipientCount();
            updateUI();
        });

        // Subject field change
        subjectField.textProperty().addListener((obs, old, newVal) -> updateUI());
    }

    private void loadContactLists() {
        try {
            ArrayList<List> lists = listsDAO.getAllActiveLists();

            listSelector.getItems().clear();
            listSelector.getItems().addAll(lists);

            if (lists.isEmpty()) {
                recipientCountLabel.setText("⚠️ " + languageManager.getText("newsletter.send.no.lists.found"));
                recipientCountLabel.setStyle("-fx-text-fill: #856404;");
            } else {
                recipientCountLabel.setText(languageManager.getText("newsletter.send.list.select.to.see.count"));
                recipientCountLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            }

        } catch (Exception e) {
            System.err.println("Error loading contact lists: " + e.getMessage());
            recipientCountLabel.setText("❌ " + languageManager.getText("newsletter.send.error.loading.lists")
                    .replace("{0}", e.getMessage()));
            recipientCountLabel.setStyle("-fx-text-fill: #721c24;");
        }
    }

    private void updateRecipientCount() {
        List selectedList = listSelector.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            try {
                // Get contacts for this list
                ArrayList<Contact> contacts = getContactsForList(selectedList.getId());

                // Filter contacts with email addresses
                long emailCount = contacts.stream()
                        .filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty())
                        .count();

                recipientCountLabel.setText(
                        languageManager.getText("newsletter.send.recipient.count")
                                .replace("{0}", selectedList.getName())
                                .replace("{1}", String.valueOf(contacts.size()))
                                .replace("{2}", String.valueOf(emailCount))
                                .replace("{3}", String.valueOf(emailCount))
                );
                recipientCountLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

                if (emailCount == 0) {
                    recipientCountLabel.setText(recipientCountLabel.getText() + " ⚠️ " +
                            languageManager.getText("newsletter.send.no.email.addresses"));
                    recipientCountLabel.setStyle("-fx-text-fill: #856404; -fx-font-weight: bold;");
                }

            } catch (Exception e) {
                System.err.println("Error getting contact count: " + e.getMessage());
                recipientCountLabel.setText("❌ " + languageManager.getText("newsletter.send.error.loading.count"));
                recipientCountLabel.setStyle("-fx-text-fill: #721c24;");
            }
        }
    }

    private ArrayList<Contact> getContactsForList(int listId) {
        try {
            // Use existing ContactDAO method that you already have working
            java.util.List<Contact> contacts = contactDAO.getContactsInList(listId);
            return new ArrayList<>(contacts);
        } catch (Exception e) {
            System.err.println("Error getting contacts for list: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void updateUI() {
        boolean gmailConnected = oauthManager.isGmailConnected();
        boolean listSelected = listSelector.getSelectionModel().getSelectedItem() != null;
        boolean hasSubject = subjectField != null && !subjectField.getText().trim().isEmpty();
        boolean hasContent = newsletterHtml != null && !newsletterHtml.trim().isEmpty();

        boolean canSend = gmailConnected && listSelected && hasSubject && hasContent && !isSending;

        sendButton.setDisable(!canSend);

        if (!gmailConnected) {
            sendButton.setText("❌ " + languageManager.getText("newsletter.send.button.gmail.not.connected"));
        } else if (isSending) {
            sendButton.setText("⏳ " + languageManager.getText("newsletter.send.button.sending"));
        } else {
            sendButton.setText("📧 " + languageManager.getText("newsletter.send.button.send"));
        }
    }

    private String generateDefaultSubject() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        return newsletterTitle + " - " + timestamp;
    }

    private String generatePreviewText() {
        if (newsletterHtml == null || newsletterHtml.trim().isEmpty()) {
            return languageManager.getText("newsletter.send.preview.no.content");
        }

        // Extract text content from HTML (simple approach)
        String textContent = newsletterHtml
                .replaceAll("(?i)<[^>]*>", " ")  // Remove HTML tags
                .replaceAll("\\s+", " ")         // Normalize whitespace
                .trim();

        // Limit preview length
        if (textContent.length() > 500) {
            textContent = textContent.substring(0, 500) + "...";
        }

        return languageManager.getText("newsletter.send.preview.subject") + " " + subjectField.getText() + "\n" +
                languageManager.getText("newsletter.send.preview.from") + " " +
                (oauthManager.isGmailConnected() ? oauthManager.getUserEmail() : languageManager.getText("newsletter.send.not.connected")) + "\n" +
                languageManager.getText("newsletter.send.preview.content.type") + "\n\n" +
                languageManager.getText("newsletter.send.preview.text.preview") + "\n" +
                textContent;
    }

    private void handleSend() {
        // Validation
        if (!validateSendingConditions()) {
            return;
        }

        List selectedList = listSelector.getSelectionModel().getSelectedItem();
        String subject = subjectField.getText().trim();

        // Show confirmation dialog
        if (!showSendConfirmation(selectedList, subject)) {
            return;
        }

        // Start sending process
        startSending(selectedList, subject);
    }

    private boolean validateSendingConditions() {
        if (!oauthManager.isGmailConnected()) {
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("newsletter.send.validation.gmail.title"),
                    languageManager.getText("newsletter.send.validation.gmail.message"));
            return false;
        }

        List selectedList = listSelector.getSelectionModel().getSelectedItem();
        if (selectedList == null) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("newsletter.send.validation.no.list.title"),
                    languageManager.getText("newsletter.send.validation.no.list.message"));
            return false;
        }

        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    languageManager.getText("newsletter.send.validation.no.subject.title"),
                    languageManager.getText("newsletter.send.validation.no.subject.message"));
            subjectField.requestFocus();
            return false;
        }

        if (newsletterHtml == null || newsletterHtml.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("newsletter.send.validation.no.content.title"),
                    languageManager.getText("newsletter.send.validation.no.content.message"));
            return false;
        }

        return true;
    }

    private boolean showSendConfirmation(List selectedList, String subject) {
        try {
            ArrayList<Contact> contacts = getContactsForList(selectedList.getId());
            long emailCount = contacts.stream()
                    .filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty())
                    .count();

            if (emailCount == 0) {
                showAlert(Alert.AlertType.WARNING,
                        languageManager.getText("newsletter.send.confirmation.no.recipients.title"),
                        languageManager.getText("newsletter.send.confirmation.no.recipients.message"));
                return false;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(languageManager.getText("newsletter.send.confirmation.title"));
            confirmation.setHeaderText(languageManager.getText("newsletter.send.confirmation.header")
                    .replace("{0}", subject));
            confirmation.setContentText(
                    languageManager.getText("newsletter.send.confirmation.content")
                            .replace("{0}", selectedList.getName())
                            .replace("{1}", String.valueOf(emailCount))
                            .replace("{2}", oauthManager.getUserEmail())
                            .replace("{3}", subject)
            );

            confirmation.initOwner(dialog);

            Optional<ButtonType> result = confirmation.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;

        } catch (Exception e) {
            System.err.println("Error in send confirmation: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR,
                    languageManager.getText("newsletter.send.error.title"),
                    languageManager.getText("newsletter.send.error.checking.recipients")
                            .replace("{0}", e.getMessage()));
            return false;
        }
    }

    private void startSending(List selectedList, String subject) {
        isSending = true;
        updateUI();

        // Show progress section
        VBox progressBox = (VBox) progressBar.getParent();
        progressBox.setVisible(true);

        // Reset progress
        progressBar.setProgress(0);
        statusLabel.setText(languageManager.getText("newsletter.send.status.preparing"));

        // Start background thread
        sendingThread = new Thread(() -> performBulkSending(selectedList, subject));
        sendingThread.setDaemon(true);
        sendingThread.start();
    }

    private void performBulkSending(List selectedList, String subject) {
        try {
            // Get all contacts with email addresses
            ArrayList<Contact> allContacts = getContactsForList(selectedList.getId());
            ArrayList<Contact> emailContacts = allContacts.stream()
                    .filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            if (emailContacts.isEmpty()) {
                Platform.runLater(() -> {
                    finishSending(false, languageManager.getText("newsletter.send.result.no.emails"));
                });
                return;
            }

            Platform.runLater(() -> {
                statusLabel.setText(languageManager.getText("newsletter.send.status.sending")
                        .replace("{0}", String.valueOf(emailContacts.size())));
            });

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Send to each contact
            for (int i = 0; i < emailContacts.size(); i++) {
                if (Thread.currentThread().isInterrupted()) {
                    break; // Stop if interrupted
                }

                Contact contact = emailContacts.get(i);
                final int currentIndex = i;

                Platform.runLater(() -> {
                    double progress = (double) currentIndex / emailContacts.size();
                    progressBar.setProgress(progress);
                    statusLabel.setText(languageManager.getText("newsletter.send.status.sending.to")
                            .replace("{0}", contact.getEmail())
                            .replace("{1}", String.valueOf(currentIndex + 1))
                            .replace("{2}", String.valueOf(emailContacts.size())));
                });

                try {
                    // ✅ CHANGED: Use HTML newsletter method instead of plain text
                    boolean success = oauthManager.sendHtmlNewsletter(
                            contact.getEmail(),
                            subject,
                            newsletterHtml
                    );

                    if (success) {
                        successCount.incrementAndGet();
                        System.out.println("✅ Newsletter sent to: " + contact.getEmail());
                    } else {
                        failureCount.incrementAndGet();
                        System.err.println("❌ Failed to send newsletter to: " + contact.getEmail());
                    }

                    // Small delay between sends to avoid rate limiting
                    Thread.sleep(100);

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("❌ Error sending to " + contact.getEmail() + ": " + e.getMessage());
                }
            }

            // Finish sending
            final String resultMessage = languageManager.getText("newsletter.send.result.completed")
                    .replace("{0}", String.valueOf(successCount.get()))
                    .replace("{1}", String.valueOf(failureCount.get()))
                    .replace("{2}", String.valueOf(emailContacts.size()));

            Platform.runLater(() -> {
                finishSending(successCount.get() > 0, resultMessage);
            });

        } catch (Exception e) {
            System.err.println("Error in bulk sending: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                finishSending(false, languageManager.getText("newsletter.send.result.failed")
                        .replace("{0}", e.getMessage()));
            });
        }
    }

    private void finishSending(boolean success, String message) {
        isSending = false;
        progressBar.setProgress(1.0);
        statusLabel.setText(languageManager.getText("newsletter.send.status.completed"));
        updateUI();

        // Show result dialog
        Alert.AlertType alertType = success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        String title = success ?
                languageManager.getText("newsletter.send.result.title.success") :
                languageManager.getText("newsletter.send.result.title.failed");

        Alert resultAlert = new Alert(alertType);
        resultAlert.setTitle(title);
        resultAlert.setHeaderText(success ?
                "✅ " + languageManager.getText("newsletter.send.result.header.success") :
                "❌ " + languageManager.getText("newsletter.send.result.header.failed"));
        resultAlert.setContentText(message);
        resultAlert.initOwner(dialog);
        resultAlert.showAndWait();

        if (success) {
            dialog.close();
        }
    }

    private void handleCancel() {
        if (isSending) {
            showStopSendingConfirmation();
        } else {
            dialog.close();
        }
    }

    private void showStopSendingConfirmation() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(languageManager.getText("newsletter.send.stop.title"));
        confirmation.setHeaderText(languageManager.getText("newsletter.send.stop.header"));
        confirmation.setContentText(languageManager.getText("newsletter.send.stop.content"));
        confirmation.initOwner(dialog);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sendingThread != null) {
                sendingThread.interrupt();
            }
            isSending = false;
            statusLabel.setText(languageManager.getText("newsletter.send.status.cancelled"));
            updateUI();
        }
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
}