package smallbusinessbuddycrm.controllers;

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

    // Dialog components
    private ComboBox<List> listSelector;
    private TextField subjectField;
    private TextArea previewArea;
    private Label statusLabel;
    private Label recipientCountLabel;
    private ProgressBar progressBar;
    private Button sendButton;
    private Button cancelButton;

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

        this.oauthManager = GoogleOAuthManager.getInstance();
        this.listsDAO = new ListsDAO();
        this.contactDAO = new ContactDAO();

        createDialog(parentStage);
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("📧 Send Newsletter - " + newsletterTitle);
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
        Label titleLabel = new Label("📧 Send Newsletter to Contact List");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Gmail status
        VBox statusBox = new VBox(5);
        statusBox.setStyle("-fx-border-color: #d4edda; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #d1ecf1;");

        Label statusTitle = new Label("📧 Gmail Status");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusTitle.setStyle("-fx-text-fill: #155724;");

        String statusText = oauthManager.isGmailConnected()
                ? "Connected as: " + oauthManager.getUserEmail()
                : "❌ Not connected to Gmail";
        Label gmailStatusLabel = new Label(statusText);
        gmailStatusLabel.setStyle("-fx-text-fill: " + (oauthManager.isGmailConnected() ? "#155724" : "#721c24") + "; -fx-font-size: 11px;");

        statusBox.getChildren().addAll(statusTitle, gmailStatusLabel);

        headerBox.getChildren().addAll(titleLabel, statusBox);
        return headerBox;
    }

    private VBox createListSelectionSection() {
        VBox listBox = new VBox(15);
        listBox.setStyle("-fx-border-color: #e3f2fd; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #f8fdff;");

        Label listTitle = new Label("👥 Select Recipient List");
        listTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        listTitle.setStyle("-fx-text-fill: #1976d2;");

        // List selector
        HBox listSelectorBox = new HBox(10);
        listSelectorBox.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("Contact List:");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        listSelector = new ComboBox<>();
        listSelector.setPrefWidth(300);
        listSelector.setPromptText("Choose a contact list...");

        Button refreshListsButton = new Button("🔄 Refresh");
        refreshListsButton.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-border-color: #1976d2; -fx-border-radius: 4;");
        refreshListsButton.setOnAction(e -> loadContactLists());

        listSelectorBox.getChildren().addAll(selectLabel, listSelector, refreshListsButton);

        // Recipient count
        recipientCountLabel = new Label("Select a list to see recipient count");
        recipientCountLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        listBox.getChildren().addAll(listTitle, listSelectorBox, recipientCountLabel);
        return listBox;
    }

    private VBox createCompositionSection() {
        VBox compositionBox = new VBox(15);
        compositionBox.setStyle("-fx-border-color: #e8f5e8; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #f8fff8;");

        Label compositionTitle = new Label("✏️ Email Composition");
        compositionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        compositionTitle.setStyle("-fx-text-fill: #2e7d32;");

        // Subject field
        VBox subjectBox = new VBox(5);
        Label subjectLabel = new Label("Subject Line:");
        subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        subjectField = new TextField();
        subjectField.setText(generateDefaultSubject());
        subjectField.setPromptText("Enter email subject...");
        subjectField.setPrefWidth(400);

        subjectBox.getChildren().addAll(subjectLabel, subjectField);

        // From info
        VBox fromBox = new VBox(5);
        Label fromLabel = new Label("From:");
        fromLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        String fromText = oauthManager.isGmailConnected()
                ? oauthManager.getUserEmail() + " (" + companyName + ")"
                : "Gmail not connected";
        Label fromValueLabel = new Label(fromText);
        fromValueLabel.setStyle("-fx-text-fill: #666;");

        fromBox.getChildren().addAll(fromLabel, fromValueLabel);

        compositionBox.getChildren().addAll(compositionTitle, subjectBox, fromBox);
        return compositionBox;
    }

    private VBox createPreviewSection() {
        VBox previewBox = new VBox(15);
        previewBox.setStyle("-fx-border-color: #fff3e0; -fx-border-radius: 5; -fx-padding: 20; -fx-background-color: #fffbf5;");

        Label previewTitle = new Label("👁️ Newsletter Preview");
        previewTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        previewTitle.setStyle("-fx-text-fill: #f57c00;");

        // Preview text area (read-only)
        previewArea = new TextArea();
        previewArea.setPrefRowCount(8);
        previewArea.setWrapText(true);
        previewArea.setEditable(false);
        previewArea.setText(generatePreviewText());
        previewArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px; -fx-background-color: #fafafa;");

        Label previewNote = new Label("Note: This is a text preview. Recipients will receive the formatted HTML version.");
        previewNote.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-font-style: italic;");

        previewBox.getChildren().addAll(previewTitle, previewArea, previewNote);
        return previewBox;
    }

    private VBox createProgressSection() {
        VBox progressBox = new VBox(10);
        progressBox.setVisible(false);
        progressBox.setStyle("-fx-border-color: #fce4ec; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #fef7f7;");

        Label progressTitle = new Label("📊 Sending Progress");
        progressTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        progressTitle.setStyle("-fx-text-fill: #c2185b;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(20);

        statusLabel = new Label("Ready to send...");
        statusLabel.setStyle("-fx-text-fill: #666;");

        progressBox.getChildren().addAll(progressTitle, progressBar, statusLabel);

        // Store reference for showing/hiding
        progressBox.managedProperty().bind(progressBox.visibleProperty());

        return progressBox;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 4;");
        cancelButton.setOnAction(e -> handleCancel());

        sendButton = new Button("📧 Send Newsletter");
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
                recipientCountLabel.setText("⚠️ No contact lists found. Please create a contact list first.");
                recipientCountLabel.setStyle("-fx-text-fill: #856404;");
            } else {
                recipientCountLabel.setText("Select a list to see recipient count");
                recipientCountLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            }

        } catch (Exception e) {
            System.err.println("Error loading contact lists: " + e.getMessage());
            recipientCountLabel.setText("❌ Error loading lists: " + e.getMessage());
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
                        String.format("📊 List: %s | Total contacts: %d | With email: %d | Will receive newsletter: %d",
                                selectedList.getName(), contacts.size(), emailCount, emailCount)
                );
                recipientCountLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

                if (emailCount == 0) {
                    recipientCountLabel.setText(recipientCountLabel.getText() + " ⚠️ No contacts with email addresses!");
                    recipientCountLabel.setStyle("-fx-text-fill: #856404; -fx-font-weight: bold;");
                }

            } catch (Exception e) {
                System.err.println("Error getting contact count: " + e.getMessage());
                recipientCountLabel.setText("❌ Error loading contact count");
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
            sendButton.setText("❌ Gmail Not Connected");
        } else if (isSending) {
            sendButton.setText("⏳ Sending...");
        } else {
            sendButton.setText("📧 Send Newsletter");
        }
    }

    private String generateDefaultSubject() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        return newsletterTitle + " - " + timestamp;
    }

    private String generatePreviewText() {
        if (newsletterHtml == null || newsletterHtml.trim().isEmpty()) {
            return "No newsletter content available for preview.";
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

        return "Subject: " + subjectField.getText() + "\n" +
                "From: " + (oauthManager.isGmailConnected() ? oauthManager.getUserEmail() : "Not connected") + "\n" +
                "Content Type: HTML Newsletter\n\n" +
                "Text Preview:\n" +
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
            showAlert(Alert.AlertType.ERROR, "Gmail Not Connected",
                    "Please connect to Gmail in Settings before sending newsletters.");
            return false;
        }

        List selectedList = listSelector.getSelectionModel().getSelectedItem();
        if (selectedList == null) {
            showAlert(Alert.AlertType.WARNING, "No List Selected",
                    "Please select a contact list to send the newsletter to.");
            return false;
        }

        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Subject",
                    "Please enter an email subject line.");
            subjectField.requestFocus();
            return false;
        }

        if (newsletterHtml == null || newsletterHtml.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No Content",
                    "Newsletter content is missing. Please generate the newsletter first.");
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
                showAlert(Alert.AlertType.WARNING, "No Recipients",
                        "The selected list has no contacts with email addresses.");
                return false;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Newsletter Sending");
            confirmation.setHeaderText("Send Newsletter: " + subject);
            confirmation.setContentText(
                    String.format(
                            "You're about to send the newsletter to:\n\n" +
                                    "📋 List: %s\n" +
                                    "👥 Recipients: %d contacts\n" +
                                    "📧 From: %s\n" +
                                    "📝 Subject: %s\n\n" +
                                    "This action cannot be undone. Continue?",
                            selectedList.getName(), emailCount,
                            oauthManager.getUserEmail(), subject
                    )
            );

            confirmation.initOwner(dialog);

            Optional<ButtonType> result = confirmation.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;

        } catch (Exception e) {
            System.err.println("Error in send confirmation: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error checking recipient list: " + e.getMessage());
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
        statusLabel.setText("Preparing to send newsletter...");

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
                    finishSending(false, "No contacts with email addresses found.");
                });
                return;
            }

            Platform.runLater(() -> {
                statusLabel.setText(String.format("Sending newsletter to %d recipients...", emailContacts.size()));
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
                    statusLabel.setText(String.format(
                            "Sending to %s (%d/%d)...",
                            contact.getEmail(), currentIndex + 1, emailContacts.size()
                    ));
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
            final String resultMessage = String.format(
                    "Newsletter sending completed!\n\nSuccessful: %d\nFailed: %d\nTotal: %d",
                    successCount.get(), failureCount.get(), emailContacts.size()
            );

            Platform.runLater(() -> {
                finishSending(successCount.get() > 0, resultMessage);
            });

        } catch (Exception e) {
            System.err.println("Error in bulk sending: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                finishSending(false, "Sending failed: " + e.getMessage());
            });
        }
    }

    private void finishSending(boolean success, String message) {
        isSending = false;
        progressBar.setProgress(1.0);
        statusLabel.setText("Sending completed");
        updateUI();

        // Show result dialog
        Alert.AlertType alertType = success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        String title = success ? "Newsletter Sent" : "Sending Failed";

        Alert resultAlert = new Alert(alertType);
        resultAlert.setTitle(title);
        resultAlert.setHeaderText(success ? "✅ Newsletter Sent Successfully" : "❌ Newsletter Sending Failed");
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
        confirmation.setTitle("Stop Sending?");
        confirmation.setHeaderText("Newsletter sending in progress");
        confirmation.setContentText("Do you want to stop sending the newsletter?\n\nEmails already sent will not be recalled.");
        confirmation.initOwner(dialog);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sendingThread != null) {
                sendingThread.interrupt();
            }
            isSending = false;
            statusLabel.setText("Sending cancelled by user");
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