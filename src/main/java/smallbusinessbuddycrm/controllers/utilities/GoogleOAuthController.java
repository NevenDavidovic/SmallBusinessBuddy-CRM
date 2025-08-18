package smallbusinessbuddycrm.controllers.utilities;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import smallbusinessbuddycrm.controllers.contact.EditColumnsDialog;
import smallbusinessbuddycrm.model.OAuthToken;
import smallbusinessbuddycrm.services.google.CallbackServer;
import smallbusinessbuddycrm.services.google.GmailService;
import smallbusinessbuddycrm.services.google.GoogleOAuthManager;
import smallbusinessbuddycrm.services.google.OAuthService;
import smallbusinessbuddycrm.services.google.TokenManager;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Enhanced controller for Google OAuth integration with full language support
 * and column settings management
 */
public class GoogleOAuthController implements Initializable {

    // ======================== MAIN UI COMPONENTS ========================
    @FXML private VBox mainContent;
    @FXML private VBox statusIndicator;
    @FXML private Label statusText;
    @FXML private Label statusLabel;
    @FXML private VBox signInSection;
    @FXML private Button signInButton;
    @FXML private VBox connectedSection;
    @FXML private Label connectedEmailLabel;
    @FXML private Label connectedNameLabel;
    @FXML private Button sendTestEmailButton;
    @FXML private Button signOutButton;
    @FXML private VBox oauthOverlay;
    @FXML private WebView oauthWebView;
    @FXML private Button closeOAuthButton;
    @FXML private Label statusMessageLabel;

    // ======================== HEADER ELEMENTS ========================
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;

    // ======================== LANGUAGE SECTION ELEMENTS ========================
    @FXML private Label languageTitle;
    @FXML private Label languageDescription;
    @FXML private Button englishButton;
    @FXML private Button croatianButton;
    @FXML private Label currentLanguageLabel;

    // ======================== GOOGLE ACCOUNT SECTION ELEMENTS ========================
    @FXML private Label googleAccountTitle;

    // ======================== CONNECT ACCOUNT SECTION ELEMENTS ========================
    @FXML private Label connectAccountTitle;
    @FXML private Label connectAccountDescription;

    // ======================== FEATURE LABELS ========================
    @FXML private Label feature1Label;
    @FXML private Label feature2Label;
    @FXML private Label feature3Label;

    // ======================== DEVELOPMENT TOOLS ELEMENTS ========================
    @FXML private Label devToolsTitle;
    @FXML private Button demoModeButton;
    @FXML private Button testServerButton;
    @FXML private Button debugButton;
    @FXML private Button userInfoButton;

    // ======================== CONNECTED SECTION ELEMENTS ========================
    @FXML private Label connectedSuccessTitle;
    @FXML private Button quickTestButton;
    @FXML private Label oauth20Label;
    @FXML private Label authenticatedLabel;
    @FXML private Label gmailApiLabel;
    @FXML private Label readyLabel;
    @FXML private Label autoRefreshLabel;
    @FXML private Label enabledLabel;

    // ======================== COLUMNS SECTION ELEMENTS ========================
    @FXML private Label columnsTitle;
    @FXML private Label columnsDescription;
    @FXML private Button editColumnsButton;
    @FXML private VBox columnStatusContainer;
    @FXML private Label columnStatusLabel;
    @FXML private Label columnCountLabel;
    @FXML private Label visibleColumnsTitle;
    @FXML private Label visibleColumnsLabel;
    @FXML private Label hiddenColumnsTitle;
    @FXML private Label hiddenColumnsLabel;
    @FXML private VBox hiddenColumnsContainer;
    @FXML private Button resetColumnsButton;
    @FXML private Button showAllColumnsButton;
    @FXML private Label columnsChangeNote;

    // ======================== OAUTH OVERLAY ELEMENTS ========================
    @FXML private Label authenticationTitle;
    @FXML private Label authenticationDescription;

    // ======================== SERVICES ========================
    private OAuthService oauthService;
    private TokenManager tokenManager;
    private GmailService gmailService;
    private CallbackServer callbackServer;
    private GoogleOAuthManager oauthManager;

    // ======================== LANGUAGE MANAGEMENT ========================
    private LanguageManager languageManager;
    private Runnable languageChangeListener;

    // ======================== COLUMN VISIBILITY SETTINGS ========================
    private Map<String, Boolean> columnVisibility = new HashMap<>();
    private static final String COLUMN_PREFS_KEY = "contacts_column_visibility";

    // ======================== STATE ========================
    private OAuthToken currentToken;
    private boolean isConnected = false;
    private boolean processingCallback = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize language manager and listener
        languageManager = LanguageManager.getInstance();
        languageChangeListener = this::updateTexts;
        languageManager.addLanguageChangeListener(languageChangeListener);

        // Load column visibility settings
        loadColumnVisibilitySettings();

        // Get the singleton OAuth manager
        oauthManager = GoogleOAuthManager.getInstance();

        // Initialize services
        oauthService = new OAuthService();
        tokenManager = new TokenManager();
        gmailService = new GmailService();
        callbackServer = new CallbackServer();

        setupWebView();
        startCallbackServer();

        // Setup language buttons
        setupLanguageButtons();

        // Initialize translations
        updateTexts();

        // Try to load saved tokens first
        loadSavedTokensAndConnect();

        System.out.println("=== OAuth Controller Initialized with Full Language Support ===");
    }

    // ======================== LANGUAGE MANAGEMENT ========================

    private void setupLanguageButtons() {
        if (englishButton != null) {
            englishButton.setOnAction(e -> switchToEnglish());
        }
        if (croatianButton != null) {
            croatianButton.setOnAction(e -> switchToCroatian());
        }
        if (editColumnsButton != null) {
            editColumnsButton.setOnAction(e -> handleEditColumns());
        }
        updateLanguageButtons();
    }

    @FXML
    private void switchToEnglish() {
        languageManager.setLanguage("en");
        updateLanguageButtons();
        showStatus(languageManager.getText("settings.language.switched.english"), "success");
        System.out.println("Settings: Switched to English");
    }

    @FXML
    private void switchToCroatian() {
        languageManager.setLanguage("hr");
        updateLanguageButtons();
        showStatus(languageManager.getText("settings.language.switched.croatian"), "success");
        System.out.println("Settings: Prebačeno na hrvatski");
    }

    private void updateLanguageButtons() {
        if (englishButton == null || croatianButton == null) {
            System.err.println("Language buttons are null - check FXML fx:id");
            return;
        }

        // Reset button styles
        englishButton.setStyle(getInactiveButtonStyle());
        croatianButton.setStyle(getInactiveButtonStyle());

        // Set active button style and update current language label
        if (languageManager.isEnglish()) {
            englishButton.setStyle(getActiveButtonStyle());
            if (currentLanguageLabel != null) {
                currentLanguageLabel.setText(languageManager.getText("settings.language.current") + " English");
            }
        } else {
            croatianButton.setStyle(getActiveButtonStyle());
            if (currentLanguageLabel != null) {
                currentLanguageLabel.setText(languageManager.getText("settings.language.current") + " Hrvatski");
            }
        }
    }

    private String getActiveButtonStyle() {
        return "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.25), 4, 0, 0, 2);";
    }

    private String getInactiveButtonStyle() {
        return "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;";
    }

    // ======================== COLUMN VISIBILITY MANAGEMENT ========================

    private void loadColumnVisibilitySettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GoogleOAuthController.class);

            // Default column visibility settings
            String[] columns = {
                    "First Name", "Last Name", "Birthday", "Age", "PIN",
                    "Email", "Phone Number", "Street Name", "Street Number",
                    "Postal Code", "City", "Member Status", "Member Since",
                    "Member Until", "Created", "Updated"
            };

            // Load saved settings or use defaults
            for (String column : columns) {
                String prefKey = COLUMN_PREFS_KEY + "." + column.replace(" ", "_");

                // Default visible columns (PIN and some others hidden by default)
                boolean defaultVisible = !column.equals("PIN") &&
                        !column.equals("Street Number") &&
                        !column.equals("Member Since") &&
                        !column.equals("Member Until") &&
                        !column.equals("Created") &&
                        !column.equals("Updated");

                boolean isVisible = prefs.getBoolean(prefKey, defaultVisible);
                columnVisibility.put(column, isVisible);
            }

            System.out.println("Column visibility settings loaded: " + columnVisibility);

            // Update display after loading (use Platform.runLater to ensure UI is ready)
            Platform.runLater(this::updateColumnStatusDisplay);

        } catch (Exception e) {
            System.err.println("Error loading column visibility settings: " + e.getMessage());
            // Initialize with defaults if loading fails
            initializeDefaultColumnVisibility();
            Platform.runLater(this::updateColumnStatusDisplay);
        }
    }

    private void initializeDefaultColumnVisibility() {
        String[] columns = {
                "First Name", "Last Name", "Birthday", "Age", "PIN",
                "Email", "Phone Number", "Street Name", "Street Number",
                "Postal Code", "City", "Member Status", "Member Since",
                "Member Until", "Created", "Updated"
        };

        for (String column : columns) {
            boolean defaultVisible = !column.equals("PIN") &&
                    !column.equals("Street Number") &&
                    !column.equals("Member Since") &&
                    !column.equals("Member Until") &&
                    !column.equals("Created") &&
                    !column.equals("Updated");
            columnVisibility.put(column, defaultVisible);
        }
    }

    private void updateColumnStatusDisplay() {
        try {
            if (columnCountLabel == null || visibleColumnsLabel == null || hiddenColumnsLabel == null) {
                return; // UI elements not loaded yet
            }

            // Count visible and total columns
            long visibleCount = columnVisibility.values().stream().mapToLong(visible -> visible ? 1 : 0).sum();
            int totalCount = columnVisibility.size();

            // Update count label with translation
            columnCountLabel.setText(visibleCount + " " + languageManager.getText("settings.columns.of") + " " + totalCount + " " + languageManager.getText("settings.columns.visible"));

            // Get visible and hidden column lists
            List<String> visibleColumns = new ArrayList<>();
            List<String> hiddenColumns = new ArrayList<>();

            for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
                String columnName = entry.getKey();
                if (entry.getValue()) {
                    visibleColumns.add(translateColumnName(columnName));
                } else {
                    hiddenColumns.add(translateColumnName(columnName));
                }
            }

            // Update visible columns display
            if (!visibleColumns.isEmpty()) {
                visibleColumnsLabel.setText(String.join(", ", visibleColumns));
            } else {
                visibleColumnsLabel.setText(languageManager.getText("settings.columns.none.visible"));
            }

            // Update hidden columns display
            if (!hiddenColumns.isEmpty()) {
                hiddenColumnsLabel.setText(String.join(", ", hiddenColumns));
                if (hiddenColumnsContainer != null) {
                    hiddenColumnsContainer.setVisible(true);
                    hiddenColumnsContainer.setManaged(true);
                }
            } else {
                hiddenColumnsLabel.setText(languageManager.getText("settings.columns.all.visible"));
                if (hiddenColumnsContainer != null) {
                    hiddenColumnsContainer.setVisible(false);
                    hiddenColumnsContainer.setManaged(false);
                }
            }

            System.out.println("Column status display updated: " + visibleCount + "/" + totalCount + " visible");

        } catch (Exception e) {
            System.err.println("Error updating column status display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String translateColumnName(String columnName) {
        Map<String, String> columnTranslationKeys = new HashMap<>();
        columnTranslationKeys.put("First Name", "edit.columns.column.first.name");
        columnTranslationKeys.put("Last Name", "edit.columns.column.last.name");
        columnTranslationKeys.put("Birthday", "edit.columns.column.birthday");
        columnTranslationKeys.put("Age", "edit.columns.column.age");
        columnTranslationKeys.put("PIN", "edit.columns.column.pin");
        columnTranslationKeys.put("Email", "edit.columns.column.email");
        columnTranslationKeys.put("Phone Number", "edit.columns.column.phone");
        columnTranslationKeys.put("Street Name", "edit.columns.column.street.name");
        columnTranslationKeys.put("Street Number", "edit.columns.column.street.number");
        columnTranslationKeys.put("Postal Code", "edit.columns.column.postal.code");
        columnTranslationKeys.put("City", "edit.columns.column.city");
        columnTranslationKeys.put("Member Status", "edit.columns.column.member.status");
        columnTranslationKeys.put("Member Since", "edit.columns.column.member.since");
        columnTranslationKeys.put("Member Until", "edit.columns.column.member.until");
        columnTranslationKeys.put("Created", "edit.columns.column.created");
        columnTranslationKeys.put("Updated", "edit.columns.column.updated");

        String translationKey = columnTranslationKeys.get(columnName);
        if (translationKey != null) {
            return languageManager.getText(translationKey);
        }
        return columnName; // Fallback to original name
    }

    private void saveColumnVisibilitySettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GoogleOAuthController.class);

            for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
                String prefKey = COLUMN_PREFS_KEY + "." + entry.getKey().replace(" ", "_");
                prefs.putBoolean(prefKey, entry.getValue());
            }

            System.out.println("Column visibility settings saved: " + columnVisibility);

        } catch (Exception e) {
            System.err.println("Error saving column visibility settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditColumns() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) editColumnsButton.getScene().getWindow();

            // Create and show the edit columns dialog
            EditColumnsDialog dialog = new EditColumnsDialog(currentStage, new HashMap<>(columnVisibility));

            boolean result = dialog.showAndWait();

            if (result) {
                // User clicked Apply - update our settings
                Map<String, Boolean> newVisibility = dialog.getColumnVisibility();
                columnVisibility.clear();
                columnVisibility.putAll(newVisibility);

                // Save the new settings
                saveColumnVisibilitySettings();

                // Update the status display
                updateColumnStatusDisplay();

                // Show success message
                showStatus(languageManager.getText("settings.columns.updated.successfully"), "success");

                System.out.println("Column visibility updated: " + columnVisibility);
            } else {
                // User cancelled - no changes
                System.out.println("Edit columns dialog cancelled");
            }

        } catch (Exception e) {
            System.err.println("Error opening edit columns dialog: " + e.getMessage());
            e.printStackTrace();
            showStatus(languageManager.getText("settings.error.opening.dialog") + ": " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleResetColumns() {
        try {
            resetColumnVisibilityToDefault();
            updateColumnStatusDisplay();
            showStatus(languageManager.getText("settings.columns.reset.success"), "success");
            System.out.println("Columns reset to default");
        } catch (Exception e) {
            System.err.println("Error resetting columns: " + e.getMessage());
            showStatus(languageManager.getText("settings.error.reset.columns") + ": " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleShowAllColumns() {
        try {
            // Set all columns to visible
            for (String columnName : columnVisibility.keySet()) {
                columnVisibility.put(columnName, true);
            }
            saveColumnVisibilitySettings();
            updateColumnStatusDisplay();
            showStatus(languageManager.getText("settings.columns.show.all.success"), "success");
            System.out.println("All columns set to visible");
        } catch (Exception e) {
            System.err.println("Error showing all columns: " + e.getMessage());
            showStatus(languageManager.getText("settings.error.show.all.columns") + ": " + e.getMessage(), "error");
        }
    }

    // ======================== ENHANCED TEXT UPDATES ========================

    private void updateTexts() {
        try {
            System.out.println("Updating GoogleOAuthController texts for language: " +
                    (languageManager.isEnglish() ? "English" : "Croatian"));

            // ======================== HEADER SECTION ========================
            if (headerTitle != null) {
                headerTitle.setText(languageManager.getText("settings.header.title"));
            }
            if (headerSubtitle != null) {
                headerSubtitle.setText(languageManager.getText("settings.header.subtitle"));
            }

            // ======================== LANGUAGE SECTION ========================
            if (languageTitle != null) {
                languageTitle.setText(languageManager.getText("settings.language.title"));
            }
            if (languageDescription != null) {
                languageDescription.setText(languageManager.getText("settings.language.description"));
            }

            // ======================== GOOGLE ACCOUNT SECTION ========================
            if (googleAccountTitle != null) {
                googleAccountTitle.setText(languageManager.getText("oauth.google.account.title"));
            }

            // ======================== CONNECT ACCOUNT SECTION ========================
            if (connectAccountTitle != null) {
                connectAccountTitle.setText(languageManager.getText("oauth.connect.account.title"));
            }
            if (connectAccountDescription != null) {
                connectAccountDescription.setText(languageManager.getText("oauth.connect.account.description"));
            }

            // ======================== FEATURES ========================
            if (feature1Label != null) {
                feature1Label.setText(languageManager.getText("oauth.feature.send.emails"));
            }
            if (feature2Label != null) {
                feature2Label.setText(languageManager.getText("oauth.feature.secure.auth"));
            }
            if (feature3Label != null) {
                feature3Label.setText(languageManager.getText("oauth.feature.auto.refresh"));
            }

            // ======================== MAIN BUTTONS ========================
            if (signInButton != null) {
                signInButton.setText(languageManager.getText("oauth.button.connect.google"));
            }
            if (sendTestEmailButton != null) {
                sendTestEmailButton.setText(languageManager.getText("oauth.button.send.test.email"));
            }
            if (quickTestButton != null) {
                quickTestButton.setText(languageManager.getText("oauth.button.quick.test"));
            }
            if (signOutButton != null) {
                signOutButton.setText(languageManager.getText("oauth.button.sign.out"));
            }
            if (closeOAuthButton != null) {
                closeOAuthButton.setText(languageManager.getText("oauth.button.close"));
            }

            // ======================== DEVELOPMENT TOOLS ========================
            if (devToolsTitle != null) {
                devToolsTitle.setText(languageManager.getText("oauth.dev.tools.title"));
            }
            if (demoModeButton != null) {
                demoModeButton.setText(languageManager.getText("oauth.dev.demo.mode"));
            }
            if (testServerButton != null) {
                testServerButton.setText(languageManager.getText("oauth.dev.test.server"));
            }
            if (debugButton != null) {
                debugButton.setText(languageManager.getText("oauth.dev.debug"));
            }
            if (userInfoButton != null) {
                userInfoButton.setText(languageManager.getText("oauth.dev.user.info"));
            }

            // ======================== CONNECTED SECTION ========================
            if (connectedSuccessTitle != null) {
                connectedSuccessTitle.setText(languageManager.getText("oauth.connected.successfully"));
            }
            if (oauth20Label != null) {
                oauth20Label.setText(languageManager.getText("oauth.status.oauth20"));
            }
            if (authenticatedLabel != null) {
                authenticatedLabel.setText(languageManager.getText("oauth.status.authenticated"));
            }
            if (gmailApiLabel != null) {
                gmailApiLabel.setText(languageManager.getText("oauth.status.gmail.api"));
            }
            if (readyLabel != null) {
                readyLabel.setText(languageManager.getText("oauth.status.ready"));
            }
            if (autoRefreshLabel != null) {
                autoRefreshLabel.setText(languageManager.getText("oauth.status.auto.refresh"));
            }
            if (enabledLabel != null) {
                enabledLabel.setText(languageManager.getText("oauth.status.enabled"));
            }

            // ======================== COLUMNS SECTION ========================
            if (columnsTitle != null) {
                columnsTitle.setText(languageManager.getText("settings.columns.title"));
            }
            if (columnsDescription != null) {
                columnsDescription.setText(languageManager.getText("settings.columns.description"));
            }
            if (columnStatusLabel != null) {
                columnStatusLabel.setText(languageManager.getText("settings.columns.current.status"));
            }
            if (visibleColumnsTitle != null) {
                visibleColumnsTitle.setText(languageManager.getText("settings.columns.visible.title"));
            }
            if (hiddenColumnsTitle != null) {
                hiddenColumnsTitle.setText(languageManager.getText("settings.columns.hidden.title"));
            }
            if (editColumnsButton != null) {
                editColumnsButton.setText(languageManager.getText("settings.edit.columns.button"));
            }
            if (resetColumnsButton != null) {
                resetColumnsButton.setText(languageManager.getText("settings.columns.reset.default"));
            }
            if (showAllColumnsButton != null) {
                showAllColumnsButton.setText(languageManager.getText("settings.columns.show.all"));
            }
            if (columnsChangeNote != null) {
                columnsChangeNote.setText(languageManager.getText("settings.columns.change.note"));
            }

            // ======================== OAUTH OVERLAY ========================
            if (authenticationTitle != null) {
                authenticationTitle.setText(languageManager.getText("oauth.authentication.title"));
            }
            if (authenticationDescription != null) {
                authenticationDescription.setText(languageManager.getText("oauth.authentication.description"));
            }

            // ======================== UPDATE DYNAMIC CONTENT ========================
            // Update language buttons
            updateLanguageButtons();

            // Update status text based on current state
            updateConnectionState(isConnected);

            // Update status message if showing default message
            if (statusMessageLabel != null && !isConnected && !processingCallback) {
                statusMessageLabel.setText(languageManager.getText("oauth.status.ready.to.connect"));
            }

            // Update column status display with new language
            updateColumnStatusDisplay();

            System.out.println("Google OAuth view texts updated successfully");

        } catch (Exception e) {
            System.err.println("Error updating GoogleOAuthController texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== OAUTH SETUP AND LIFECYCLE ========================

    private void setupWebView() {
        if (oauthWebView != null) {
            WebEngine webEngine = oauthWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        }
    }

    private void startCallbackServer() {
        try {
            callbackServer.start(this::processOAuthCallback);
        } catch (Exception e) {
            showStatus(languageManager.getText("oauth.error.callback.server.failed"), "error");
        }
    }

    private void loadSavedTokensAndConnect() {
        // Check if the OAuth manager already has a valid connection
        if (oauthManager.isGmailConnected()) {
            currentToken = oauthManager.getCurrentToken();
            isConnected = true;
            updateConnectionState(true);
            showStatus(languageManager.getText("oauth.status.welcome.back.existing"), "success");
            return;
        }

        // If no existing connection in manager, try to load from storage
        OAuthToken savedToken = tokenManager.loadSavedTokens();
        if (savedToken != null && savedToken.isValid()) {
            // Validate token is still good
            if (oauthService.validateToken(savedToken.getAccessToken())) {
                currentToken = savedToken;
                isConnected = true;
                // NOTIFY THE OAUTH MANAGER
                oauthManager.updateConnection(currentToken, true);
                updateConnectionState(true);
                showStatus(languageManager.getText("oauth.status.welcome.back.saved"), "success");
                return;
            } else if (savedToken.hasRefreshToken()) {
                // Try to refresh the token
                String newAccessToken = oauthService.refreshAccessToken(savedToken.getRefreshToken());
                if (newAccessToken != null) {
                    currentToken = savedToken.withUpdatedAccessToken(newAccessToken);
                    tokenManager.saveTokens(currentToken);
                    isConnected = true;
                    // NOTIFY THE OAUTH MANAGER
                    oauthManager.updateConnection(currentToken, true);
                    updateConnectionState(true);
                    showStatus(languageManager.getText("oauth.status.token.refreshed"), "success");
                    return;
                }
            }
        }

        // No valid saved tokens
        updateConnectionState(false);
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        showStatus(languageManager.getText("oauth.status.ready.to.connect"), "info");
    }

    // ======================== OAUTH FLOW METHODS ========================

    @FXML
    private void handleSignIn() {
        System.out.println("=== Sign In Button Clicked ===");

        if (processingCallback) {
            showStatus(languageManager.getText("oauth.status.already.processing"), "info");
            return;
        }

        openInSystemBrowser();
    }

    private void openInSystemBrowser() {
        try {
            String oauthUrl = oauthService.buildAuthorizationUrl();

            if (oauthUrl != null) {
                System.out.println("🌐 Opening OAuth URL: " + oauthUrl);

                showStatus(languageManager.getText("oauth.status.opening.browser"), "info");
                signInButton.setDisable(true);
                signInButton.setText(languageManager.getText("oauth.button.waiting.signin"));

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(oauthUrl));
                    System.out.println("✅ Browser opened successfully");
                }

                showStatus(languageManager.getText("oauth.status.complete.signin.browser"), "info");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening browser: " + e.getMessage());
            showStatus(languageManager.getText("oauth.error.opening.browser") + ": " + e.getMessage(), "error");
            resetSignInButton();
        }
    }

    private void processOAuthCallback(String query) {
        if (processingCallback) {
            System.out.println("⚠️ Already processing callback");
            return;
        }

        processingCallback = true;
        System.out.println("🔄 Starting OAuth processing...");

        if (query.contains("code=")) {
            String authCode = CallbackServer.extractParameter(query, "code");
            String state = CallbackServer.extractParameter(query, "state");

            System.out.println("✅ Auth code: " + authCode.substring(0, Math.min(10, authCode.length())) + "...");
            System.out.println("🔍 State validation: " + (oauthService.validateState(state) ? "PASS" : "FAIL"));

            if (!oauthService.validateState(state)) {
                System.err.println("❌ State mismatch - potential security issue");
                Platform.runLater(() -> {
                    showStatus(languageManager.getText("oauth.error.security.invalid.state"), "error");
                    resetSignInButton();
                    processingCallback = false;
                });
                return;
            }

            handleOAuthSuccess(authCode);
        } else if (query.contains("error=")) {
            String error = CallbackServer.extractParameter(query, "error");
            System.err.println("❌ OAuth error: " + error);
            Platform.runLater(() -> {
                showStatus(languageManager.getText("oauth.error.oauth") + ": " + error, "error");
                resetSignInButton();
                processingCallback = false;
            });
        }
    }

    private void handleOAuthSuccess(String authCode) {
        showStatus(languageManager.getText("oauth.status.exchanging.code"), "info");

        new Thread(() -> {
            try {
                // Exchange auth code for tokens
                String tokenResponse = oauthService.exchangeAuthorizationCode(authCode);
                if (tokenResponse == null) {
                    Platform.runLater(() -> {
                        showStatus(languageManager.getText("oauth.error.failed.get.token"), "error");
                        resetSignInButton();
                        processingCallback = false;
                    });
                    return;
                }

                // Parse token response
                OAuthToken token = tokenManager.parseTokenResponse(tokenResponse);
                if (token == null || !token.isValid()) {
                    Platform.runLater(() -> {
                        showStatus(languageManager.getText("oauth.error.failed.parse.token"), "error");
                        resetSignInButton();
                        processingCallback = false;
                    });
                    return;
                }

                // Get user information
                String userInfoResponse = oauthService.getUserInfo(token.getAccessToken());
                if (userInfoResponse != null) {
                    token = tokenManager.updateTokenWithUserInfo(token, userInfoResponse);
                }

                // Save tokens and update UI
                currentToken = token;
                tokenManager.saveTokens(currentToken);

                // NOTIFY THE OAUTH MANAGER
                oauthManager.updateConnection(currentToken, true);

                Platform.runLater(() -> {
                    isConnected = true;
                    updateConnectionState(true);
                    showStatus(languageManager.getText("oauth.status.successfully.connected")
                            .replace("{0}", currentToken.getUserEmail()), "success");
                    processingCallback = false;
                });

            } catch (Exception e) {
                System.err.println("❌ OAuth error: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showStatus(languageManager.getText("oauth.error.authentication") + ": " + e.getMessage(), "error");
                    resetSignInButton();
                    processingCallback = false;
                });
            }
        }).start();
    }

    // ======================== EMAIL SENDING METHODS ========================

    @FXML
    private void handleSendTestEmail() {
        if (!oauthManager.isGmailConnected()) {
            showStatus(languageManager.getText("oauth.error.please.signin.first"), "error");
            return;
        }

        sendTestEmailButton.setDisable(true);
        sendTestEmailButton.setText(languageManager.getText("oauth.button.sending"));
        showStatus(languageManager.getText("oauth.status.sending.test.email"), "info");

        new Thread(() -> {
            try {
                boolean success = oauthManager.sendTestEmail();

                Platform.runLater(() -> {
                    sendTestEmailButton.setDisable(false);
                    sendTestEmailButton.setText(languageManager.getText("oauth.button.send.test.email"));
                    if (success) {
                        showStatus(languageManager.getText("oauth.status.test.email.sent"), "success");
                    } else {
                        showStatus(languageManager.getText("oauth.error.test.email.failed"), "error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    sendTestEmailButton.setDisable(false);
                    sendTestEmailButton.setText(languageManager.getText("oauth.button.send.test.email"));
                    showStatus(languageManager.getText("oauth.error.test.email") + ": " + e.getMessage(), "error");
                });
            }
        }).start();
    }

    @FXML
    private void handleQuickTestEmail() {
        if (!oauthManager.isGmailConnected()) {
            showStatus(languageManager.getText("oauth.error.please.signin.first"), "error");
            return;
        }

        new Thread(() -> {
            try {
                boolean success = oauthManager.sendQuickTestEmail();

                Platform.runLater(() -> {
                    if (success) {
                        showStatus(languageManager.getText("oauth.status.quick.test.sent"), "success");
                    } else {
                        showStatus(languageManager.getText("oauth.error.quick.test.failed"), "error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus(languageManager.getText("oauth.error.quick.test") + ": " + e.getMessage(), "error");
                });
            }
        }).start();
    }

    @FXML
    private void handleSignOut() {
        isConnected = false;
        currentToken = null;
        processingCallback = false;

        tokenManager.clearSavedTokens();

        // NOTIFY THE OAUTH MANAGER
        oauthManager.clearConnection();

        updateConnectionState(false);
        showStatus(languageManager.getText("oauth.status.signed.out"), "info");
    }

    // ======================== TESTING AND UTILITY METHODS ========================

    @FXML
    private void testConnection() {
        if (!isConnected || currentToken == null) {
            showStatus(languageManager.getText("oauth.error.not.connected"), "error");
            return;
        }

        showStatus(languageManager.getText("oauth.status.testing.connection"), "info");
        new Thread(() -> {
            boolean valid = oauthService.validateToken(currentToken.getAccessToken());
            Platform.runLater(() -> {
                if (valid) {
                    showStatus(languageManager.getText("oauth.status.connection.test.successful"), "success");
                } else {
                    showStatus(languageManager.getText("oauth.error.connection.test.failed"), "error");
                }
            });
        }).start();
    }

    @FXML
    private void checkTokenStatus() {
        if (!isConnected || currentToken == null) {
            showStatus(languageManager.getText("oauth.error.not.connected"), "error");
            return;
        }

        new Thread(() -> {
            boolean valid = oauthService.validateToken(currentToken.getAccessToken());
            Platform.runLater(() -> {
                if (valid) {
                    showStatus(languageManager.getText("oauth.status.token.valid"), "success");
                } else {
                    showStatus(languageManager.getText("oauth.status.token.expired.refreshing"), "info");
                    forceRefreshToken();
                }
            });
        }).start();
    }

    @FXML
    private void handleTestDemoMode() {
        showStatus(languageManager.getText("oauth.status.demo.mode"), "info");

        // Create demo token
        currentToken = new OAuthToken(
                "demo_token_" + System.currentTimeMillis(),
                "demo_refresh_token",
                "demo@businessbuddy.com",
                languageManager.getText("oauth.demo.user")
        );
        isConnected = true;

        // NOTIFY THE OAUTH MANAGER
        oauthManager.updateConnection(currentToken, true);

        updateConnectionState(true);
        showStatus(languageManager.getText("oauth.status.demo.active")
                .replace("{0}", currentToken.getUserEmail()), "success");
    }

    @FXML
    private void testCallback() {
        showStatus(languageManager.getText("oauth.status.testing.callback"), "info");
        boolean serverRunning = callbackServer.testConnection();
        if (serverRunning) {
            showStatus(languageManager.getText("oauth.status.callback.running"), "success");
        } else {
            showStatus(languageManager.getText("oauth.error.callback.test.failed"), "error");
        }
    }

    @FXML
    private void handleCloseOAuth() {
        hideOAuthOverlay();
        resetSignInButton();
        processingCallback = false;
    }

    @FXML
    private void debugConnectionState() {
        System.out.println("\n=== DEBUG CONNECTION STATE ===");
        System.out.println("isConnected: " + isConnected);
        System.out.println("currentToken: " + (currentToken != null ? currentToken.toString() : "null"));
        System.out.println("processingCallback: " + processingCallback);

        if (connectedEmailLabel != null) {
            System.out.println("connectedEmailLabel text: '" + connectedEmailLabel.getText() + "'");
        }
        if (connectedNameLabel != null) {
            System.out.println("connectedNameLabel text: '" + connectedNameLabel.getText() + "'");
        }
        if (statusLabel != null) {
            System.out.println("statusLabel text: '" + statusLabel.getText() + "'");
        }

        // Print OAuth Manager debug info
        oauthManager.printDebugInfo();

        showStatus(languageManager.getText("oauth.status.debug.printed"), "info");
        System.out.println("==============================\n");
    }

    @FXML
    private void forceGetUserInfo() {
        if (!isConnected || currentToken == null || !currentToken.isValid()) {
            showStatus(languageManager.getText("oauth.error.no.access.token"), "error");
            return;
        }

        showStatus(languageManager.getText("oauth.status.getting.user.info"), "info");

        new Thread(() -> {
            String userInfoResponse = oauthService.getUserInfo(currentToken.getAccessToken());
            if (userInfoResponse != null) {
                currentToken = tokenManager.updateTokenWithUserInfo(currentToken, userInfoResponse);
                tokenManager.saveTokens(currentToken);

                // NOTIFY THE OAUTH MANAGER
                oauthManager.updateConnection(currentToken, true);

                Platform.runLater(() -> {
                    updateConnectionState(true);
                    showStatus(languageManager.getText("oauth.status.user.info.retrieved")
                            .replace("{0}", currentToken.getUserEmail()), "success");
                });
            } else {
                Platform.runLater(() -> {
                    showStatus(languageManager.getText("oauth.error.failed.get.user.info"), "error");
                });
            }
        }).start();
    }

    // ======================== UI UPDATE METHODS ========================

    private void showOAuthOverlay() {
        if (oauthOverlay != null) {
            oauthOverlay.setVisible(true);
            oauthOverlay.setManaged(true);
            if (mainContent != null) {
                mainContent.setDisable(true);
            }
        }
    }

    private void hideOAuthOverlay() {
        if (oauthOverlay != null) {
            oauthOverlay.setVisible(false);
            oauthOverlay.setManaged(false);
            if (mainContent != null) {
                mainContent.setDisable(false);
            }
        }
    }

    private void updateConnectionState(boolean connected) {
        System.out.println("=== UPDATE CONNECTION STATE ===");
        System.out.println("Connected parameter: " + connected);
        if (currentToken != null) {
            System.out.println("User Email: '" + currentToken.getUserEmail() + "'");
            System.out.println("User Name: '" + currentToken.getUserName() + "'");
        }
        System.out.println("isConnected: " + isConnected);
        System.out.println("================================");

        if (connected && currentToken != null) {
            // Update status indicator to green with modern styling
            if (statusIndicator != null) {
                statusIndicator.setStyle("-fx-background-color: #28a745; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(40,167,69,0.4), 8, 0, 0, 2);");
            }

            if (statusText != null) {
                statusText.setText(languageManager.getText("oauth.status.connected"));
                statusText.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-font-weight: 500;");
            }

            String displayEmail = currentToken.getUserEmail() != null ? currentToken.getUserEmail() :
                    languageManager.getText("oauth.error.unknown.email");
            String displayName = currentToken.getDisplayName();

            // Update main status label
            if (statusLabel != null) {
                statusLabel.setText(languageManager.getText("oauth.status.connected.to.gmail")
                        .replace("{0}", displayEmail));
                statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-wrap-text: true; -fx-padding: 15 0 0 0;");
            }

            // Update connected section labels
            if (connectedEmailLabel != null) {
                connectedEmailLabel.setText(languageManager.getText("oauth.connected.email")
                        .replace("{0}", displayEmail));
                connectedEmailLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-text-alignment: center;");
            }

            if (connectedNameLabel != null) {
                connectedNameLabel.setText(languageManager.getText("oauth.connected.name")
                        .replace("{0}", displayName));
                connectedNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #155724; -fx-text-alignment: center;");
            }

            // Show connected section, hide sign-in section
            if (signInSection != null) {
                signInSection.setVisible(false);
                signInSection.setManaged(false);
            }
            if (connectedSection != null) {
                connectedSection.setVisible(true);
                connectedSection.setManaged(true);
            }

            System.out.println("✅ UI Updated - Connected as: " + displayEmail);

        } else {
            // Update status indicator to red
            if (statusIndicator != null) {
                statusIndicator.setStyle("-fx-background-color: #dc3545; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.4), 8, 0, 0, 2);");
            }

            if (statusText != null) {
                statusText.setText(languageManager.getText("oauth.status.not.connected"));
                statusText.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545; -fx-font-weight: 500;");
            }

            // Update main status label for disconnected state
            if (statusLabel != null) {
                statusLabel.setText(languageManager.getText("oauth.status.connect.account.enable"));
                statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #495057; -fx-wrap-text: true; -fx-padding: 15 0 0 0;");
            }

            // Clear connected section labels
            if (connectedEmailLabel != null) {
                connectedEmailLabel.setText(languageManager.getText("oauth.status.no.account.connected"));
            }
            if (connectedNameLabel != null) {
                connectedNameLabel.setText("");
            }

            // Show sign-in section, hide connected section
            if (signInSection != null) {
                signInSection.setVisible(true);
                signInSection.setManaged(true);
            }
            if (connectedSection != null) {
                connectedSection.setVisible(false);
                connectedSection.setManaged(false);
            }

            System.out.println("❌ UI Updated - Disconnected");
        }

        resetSignInButton();
    }

    private void resetSignInButton() {
        if (signInButton != null) {
            signInButton.setDisable(false);
            signInButton.setText(languageManager.getText("oauth.button.connect.google"));
        }
    }

    private void showStatus(String message, String type) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            switch (type) {
                case "success":
                    statusMessageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-background-color: white; -fx-padding: 20 25 20 25; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 3); -fx-text-alignment: center; -fx-font-weight: bold;");
                    break;
                case "error":
                    statusMessageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #dc3545; -fx-background-color: white; -fx-padding: 20 25 20 25; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 3); -fx-text-alignment: center; -fx-font-weight: bold;");
                    break;
                default:
                    statusMessageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #007bff; -fx-background-color: white; -fx-padding: 20 25 20 25; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 3); -fx-text-alignment: center;");
                    break;
            }
        }
    }

    // ======================== CLEANUP AND LIFECYCLE METHODS ========================

    public void cleanup() {
        // Remove language change listener to prevent memory leaks
        if (languageManager != null && languageChangeListener != null) {
            languageManager.removeLanguageChangeListener(languageChangeListener);
            System.out.println("GoogleOAuthController: Language change listener removed");
        }

        // Cleanup other resources
        if (callbackServer != null) {
            callbackServer.stop();
        }
        if (oauthService != null) {
            oauthService.cleanup();
        }
        if (gmailService != null) {
            gmailService.cleanup();
        }
    }

    // ======================== PUBLIC METHODS ========================

    public boolean isConnected() {
        return isConnected;
    }

    public String getAccessToken() {
        return currentToken != null ? currentToken.getAccessToken() : null;
    }

    public String getUserEmail() {
        return currentToken != null ? currentToken.getUserEmail() : null;
    }

    public String getUserName() {
        return currentToken != null ? currentToken.getUserName() : null;
    }

    public OAuthToken getCurrentToken() {
        return currentToken;
    }

    // ======================== ADDITIONAL UTILITY METHODS ========================

    public void forceRefreshToken() {
        if (currentToken == null || !currentToken.hasRefreshToken()) {
            showStatus(languageManager.getText("oauth.error.no.refresh.token"), "error");
            return;
        }

        new Thread(() -> {
            String newAccessToken = oauthService.refreshAccessToken(currentToken.getRefreshToken());
            Platform.runLater(() -> {
                if (newAccessToken != null) {
                    currentToken = currentToken.withUpdatedAccessToken(newAccessToken);
                    tokenManager.saveTokens(currentToken);
                    // NOTIFY THE OAUTH MANAGER
                    oauthManager.updateConnection(currentToken, true);
                    showStatus(languageManager.getText("oauth.status.token.refreshed"), "success");
                } else {
                    showStatus(languageManager.getText("oauth.error.token.refresh.failed"), "error");
                    handleSignOut();
                }
            });
        }).start();
    }

    public void printConnectionInfo() {
        System.out.println("\n=== CONNECTION INFO ===");
        System.out.println("Connected: " + isConnected);
        if (currentToken != null) {
            System.out.println("User Email: '" + currentToken.getUserEmail() + "'");
            System.out.println("User Name: '" + currentToken.getUserName() + "'");
            System.out.println("Has Access Token: " + currentToken.isValid());
            System.out.println("Has Refresh Token: " + currentToken.hasRefreshToken());
        } else {
            System.out.println("Current Token: null");
        }
        System.out.println("Processing Callback: " + processingCallback);
        System.out.println("Saved Tokens Exist: " + tokenManager.hasSavedTokens());
        System.out.println("Config Directory: " + tokenManager.getConfigDirectoryPath());
        System.out.println("Column Visibility: " + columnVisibility);
        System.out.println("======================\n");
    }

    public boolean hasValidConnection() {
        return isConnected && currentToken != null && currentToken.isValid() && currentToken.hasUserInfo();
    }

    public void resetConnection() {
        System.out.println("🔄 Resetting connection...");
        handleSignOut();

        Platform.runLater(() -> {
            loadSavedTokensAndConnect();
        });
    }

    // ======================== ENHANCED EMAIL SENDING ========================

    public boolean sendEmail(String to, String subject, String body) {
        if (!oauthManager.isGmailConnected()) {
            System.err.println("❌ Cannot send email: Not connected");
            return false;
        }
        return oauthManager.sendEmail(to, subject, body);
    }

    public void sendEmailAsync(String to, String subject, String body, Runnable onSuccess, Runnable onFailure) {
        if (!oauthManager.isGmailConnected()) {
            Platform.runLater(onFailure);
            return;
        }

        new Thread(() -> {
            try {
                boolean success = oauthManager.sendEmail(to, subject, body);
                Platform.runLater(success ? onSuccess : onFailure);
            } catch (Exception e) {
                System.err.println("❌ Async email error: " + e.getMessage());
                Platform.runLater(onFailure);
            }
        }).start();
    }

    // ======================== LANGUAGE REFRESH METHOD ========================

    /**
     * Method to refresh language when language changes
     * This can be called from outside the controller if needed
     */
    public void refreshLanguage() {
        updateTexts();

        // Refresh the connection state to update all text with new language
        updateConnectionState(isConnected);

        // If showing default welcome message, update it
        if (!isConnected && !processingCallback) {
            showWelcomeMessage();
        }

        System.out.println("Google OAuth Controller language refreshed");
    }

    // ======================== PUBLIC METHODS FOR COLUMN VISIBILITY ACCESS ========================

    /**
     * Get the current column visibility settings for contacts view
     * @return Map of column names to visibility boolean values
     */
    public Map<String, Boolean> getColumnVisibility() {
        return new HashMap<>(columnVisibility);
    }

    /**
     * Update the column visibility settings for contacts view
     * @param newVisibility Map of column names to visibility boolean values
     */
    public void updateColumnVisibility(Map<String, Boolean> newVisibility) {
        columnVisibility.clear();
        columnVisibility.putAll(newVisibility);
        saveColumnVisibilitySettings();
        updateColumnStatusDisplay();
    }

    /**
     * Get the current column visibility settings for contacts view
     * @return Map of column names to visibility boolean values
     */
    public Map<String, Boolean> getContactsColumnVisibility() {
        return getColumnVisibility();
    }

    /**
     * Update the column visibility settings for contacts view
     * @param newVisibility Map of column names to visibility boolean values
     */
    public void updateContactsColumnVisibility(Map<String, Boolean> newVisibility) {
        updateColumnVisibility(newVisibility);
    }

    /**
     * Reset column visibility to default settings
     */
    public void resetColumnVisibilityToDefault() {
        initializeDefaultColumnVisibility();
        saveColumnVisibilitySettings();
        updateColumnStatusDisplay();
        showStatus(languageManager.getText("settings.columns.reset.to.default"), "success");
    }

    /**
     * Check if a specific column is currently visible
     * @param columnName The name of the column to check
     * @return true if visible, false if hidden or column doesn't exist
     */
    public boolean isColumnVisible(String columnName) {
        return columnVisibility.getOrDefault(columnName, true);
    }

    /**
     * Set visibility for a specific column
     * @param columnName The name of the column
     * @param isVisible true to show, false to hide
     */
    public void setColumnVisibility(String columnName, boolean isVisible) {
        columnVisibility.put(columnName, isVisible);
        saveColumnVisibilitySettings();
        updateColumnStatusDisplay();
    }

    /**
     * Get list of all available column names
     * @return Array of all column names that can be configured
     */
    public String[] getAvailableColumns() {
        return new String[]{
                "First Name", "Last Name", "Birthday", "Age", "PIN",
                "Email", "Phone Number", "Street Name", "Street Number",
                "Postal Code", "City", "Member Status", "Member Since",
                "Member Until", "Created", "Updated"
        };
    }

    /**
     * Manual refresh of the language in the UI
     * Useful for external controllers that might want to trigger a refresh
     */
    public void forceLanguageRefresh() {
        try {
            updateTexts();
            updateConnectionState(isConnected);
            System.out.println("GoogleOAuthController: Manual language refresh completed");
        } catch (Exception e) {
            System.err.println("Error during manual language refresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== DEBUG METHODS ========================

    public void printColumnVisibilityDebug() {
        System.out.println("\n=== COLUMN VISIBILITY DEBUG ===");
        System.out.println("Current settings:");
        for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("Total columns configured: " + columnVisibility.size());
        System.out.println("Preferences key: " + COLUMN_PREFS_KEY);
        System.out.println("Current language: " + (languageManager.isEnglish() ? "English" : "Croatian"));
        System.out.println("===============================\n");
    }

    /**
     * Print general debug information about the controller
     */
    public void printDebugInfo() {
        System.out.println("\n=== GOOGLE OAUTH CONTROLLER DEBUG ===");
        System.out.println("Language Manager: " + (languageManager != null ? "Initialized" : "NULL"));
        System.out.println("Current Language: " + (languageManager != null ? (languageManager.isEnglish() ? "English" : "Croatian") : "Unknown"));
        System.out.println("OAuth Manager: " + (oauthManager != null ? "Initialized" : "NULL"));
        System.out.println("Is Connected: " + isConnected);
        System.out.println("Processing Callback: " + processingCallback);
        System.out.println("Column Visibility Settings: " + columnVisibility.size() + " columns configured");
        System.out.println("UI Elements Status:");
        System.out.println("  Header Title: " + (headerTitle != null ? "OK" : "NULL"));
        System.out.println("  Language Buttons: " + (englishButton != null && croatianButton != null ? "OK" : "NULL"));
        System.out.println("  OAuth Buttons: " + (signInButton != null && signOutButton != null ? "OK" : "NULL"));
        System.out.println("  Column Buttons: " + (editColumnsButton != null ? "OK" : "NULL"));
        System.out.println("  Status Labels: " + (statusMessageLabel != null ? "OK" : "NULL"));
        System.out.println("=====================================\n");
    }
}