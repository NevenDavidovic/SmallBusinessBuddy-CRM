package smallbusinessbuddycrm.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import smallbusinessbuddycrm.model.OAuthToken;
import smallbusinessbuddycrm.services.google.CallbackServer;
import smallbusinessbuddycrm.services.google.GmailService;
import smallbusinessbuddycrm.services.google.GoogleOAuthManager;
import smallbusinessbuddycrm.services.google.OAuthService;
import smallbusinessbuddycrm.services.google.TokenManager;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Refactored controller for Google OAuth integration
 * Now uses separate service classes for better organization and OAuth manager for global state
 */
public class GoogleOAuthController implements Initializable {

    // UI Components
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

    // Services
    private OAuthService oauthService;
    private TokenManager tokenManager;
    private GmailService gmailService;
    private CallbackServer callbackServer;
    private GoogleOAuthManager oauthManager;

    // State
    private OAuthToken currentToken;
    private boolean isConnected = false;
    private boolean processingCallback = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the singleton OAuth manager
        oauthManager = GoogleOAuthManager.getInstance();

        // Initialize services
        oauthService = new OAuthService();
        tokenManager = new TokenManager();
        gmailService = new GmailService();
        callbackServer = new CallbackServer();

        setupWebView();
        startCallbackServer();

        // Try to load saved tokens first
        loadSavedTokensAndConnect();

        System.out.println("=== OAuth Controller Initialized (Refactored with Manager) ===");
    }

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
            showStatus("❌ Failed to start callback server. Try restarting the app.", "error");
        }
    }

    private void loadSavedTokensAndConnect() {
        // Check if the OAuth manager already has a valid connection
        if (oauthManager.isGmailConnected()) {
            currentToken = oauthManager.getCurrentToken();
            isConnected = true;
            updateConnectionState(true);
            showStatus("✅ Welcome back! Using existing OAuth session.", "success");
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
                showStatus("✅ Welcome back! Using saved credentials.", "success");
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
                    showStatus("✅ Token refreshed successfully!", "success");
                    return;
                }
            }
        }

        // No valid saved tokens
        updateConnectionState(false);
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        showStatus("Ready to connect! Click 'Connect with Google' to get started.", "info");
    }

    // ======================== OAUTH FLOW METHODS ========================

    @FXML
    private void handleSignIn() {
        System.out.println("=== Sign In Button Clicked ===");

        if (processingCallback) {
            showStatus("⚠️ Already processing sign-in. Please wait...", "info");
            return;
        }

        openInSystemBrowser();
    }

    private void openInSystemBrowser() {
        try {
            String oauthUrl = oauthService.buildAuthorizationUrl();

            if (oauthUrl != null) {
                System.out.println("🌐 Opening OAuth URL: " + oauthUrl);

                showStatus("🌐 Opening Google sign-in in browser...", "info");
                signInButton.setDisable(true);
                signInButton.setText("Waiting for sign-in...");

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(oauthUrl));
                    System.out.println("✅ Browser opened successfully");
                }

                showStatus("🌐 Complete the sign-in in your browser, then return here.", "info");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening browser: " + e.getMessage());
            showStatus("❌ Error opening browser: " + e.getMessage(), "error");
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
                    showStatus("❌ Security error: Invalid state parameter", "error");
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
                showStatus("❌ OAuth error: " + error, "error");
                resetSignInButton();
                processingCallback = false;
            });
        }
    }

    private void handleOAuthSuccess(String authCode) {
        showStatus("🔄 Exchanging authorization code for access token...", "info");

        new Thread(() -> {
            try {
                // Exchange auth code for tokens
                String tokenResponse = oauthService.exchangeAuthorizationCode(authCode);
                if (tokenResponse == null) {
                    Platform.runLater(() -> {
                        showStatus("❌ Failed to get access token", "error");
                        resetSignInButton();
                        processingCallback = false;
                    });
                    return;
                }

                // Parse token response
                OAuthToken token = tokenManager.parseTokenResponse(tokenResponse);
                if (token == null || !token.isValid()) {
                    Platform.runLater(() -> {
                        showStatus("❌ Failed to parse access token", "error");
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
                    showStatus("🎉 Successfully connected as " + currentToken.getUserEmail() + "!", "success");
                    processingCallback = false;
                });

            } catch (Exception e) {
                System.err.println("❌ OAuth error: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showStatus("❌ Authentication error: " + e.getMessage(), "error");
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
            showStatus("❌ Please sign in first", "error");
            return;
        }

        sendTestEmailButton.setDisable(true);
        sendTestEmailButton.setText("Sending...");
        showStatus("📧 Sending test email...", "info");

        new Thread(() -> {
            try {
                boolean success = oauthManager.sendTestEmail();

                Platform.runLater(() -> {
                    sendTestEmailButton.setDisable(false);
                    sendTestEmailButton.setText("📧 Send Test Email");
                    if (success) {
                        showStatus("✅ Test email sent successfully! Check your inbox.", "success");
                    } else {
                        showStatus("❌ Failed to send test email. Check console for details.", "error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    sendTestEmailButton.setDisable(false);
                    sendTestEmailButton.setText("📧 Send Test Email");
                    showStatus("❌ Test email error: " + e.getMessage(), "error");
                });
            }
        }).start();
    }

    @FXML
    private void handleQuickTestEmail() {
        if (!oauthManager.isGmailConnected()) {
            showStatus("❌ Please sign in first", "error");
            return;
        }

        new Thread(() -> {
            try {
                boolean success = oauthManager.sendQuickTestEmail();

                Platform.runLater(() -> {
                    if (success) {
                        showStatus("⚡ Quick test email sent successfully!", "success");
                    } else {
                        showStatus("❌ Quick test failed. Check your connection.", "error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("❌ Quick test error: " + e.getMessage(), "error");
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
        showStatus("👋 Signed out successfully. All saved credentials cleared.", "info");
    }

    // ======================== TESTING AND UTILITY METHODS ========================

    @FXML
    private void testConnection() {
        if (!isConnected || currentToken == null) {
            showStatus("❌ Not connected", "error");
            return;
        }

        showStatus("🔍 Testing connection...", "info");
        new Thread(() -> {
            boolean valid = oauthService.validateToken(currentToken.getAccessToken());
            Platform.runLater(() -> {
                if (valid) {
                    showStatus("✅ Connection test successful! Token is valid.", "success");
                } else {
                    showStatus("❌ Connection test failed - token may be expired", "error");
                }
            });
        }).start();
    }

    @FXML
    private void checkTokenStatus() {
        if (!isConnected || currentToken == null) {
            showStatus("❌ Not connected", "error");
            return;
        }

        new Thread(() -> {
            boolean valid = oauthService.validateToken(currentToken.getAccessToken());
            Platform.runLater(() -> {
                if (valid) {
                    showStatus("✅ Token is valid and active", "success");
                } else {
                    showStatus("⚠️ Token may be expired. Trying to refresh...", "info");
                    forceRefreshToken();
                }
            });
        }).start();
    }

    @FXML
    private void handleTestDemoMode() {
        showStatus("🎭 Demo mode - simulating successful connection...", "info");

        // Create demo token
        currentToken = new OAuthToken(
                "demo_token_" + System.currentTimeMillis(),
                "demo_refresh_token",
                "demo@businessbuddy.com",
                "Demo User"
        );
        isConnected = true;

        // NOTIFY THE OAUTH MANAGER
        oauthManager.updateConnection(currentToken, true);

        updateConnectionState(true);
        showStatus("🎭 Demo mode active! Connected as: " + currentToken.getUserEmail(), "success");
    }

    @FXML
    private void testCallback() {
        showStatus("🔧 Testing callback server...", "info");
        boolean serverRunning = callbackServer.testConnection();
        if (serverRunning) {
            showStatus("✅ Callback server is running properly", "success");
        } else {
            showStatus("❌ Callback server test failed", "error");
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

        showStatus("🧪 Debug info printed to console. Check connection state above.", "info");
        System.out.println("==============================\n");
    }

    @FXML
    private void forceGetUserInfo() {
        if (!isConnected || currentToken == null || !currentToken.isValid()) {
            showStatus("❌ Not connected or no access token", "error");
            return;
        }

        showStatus("🔍 Force getting user info...", "info");

        new Thread(() -> {
            String userInfoResponse = oauthService.getUserInfo(currentToken.getAccessToken());
            if (userInfoResponse != null) {
                currentToken = tokenManager.updateTokenWithUserInfo(currentToken, userInfoResponse);
                tokenManager.saveTokens(currentToken);

                // NOTIFY THE OAUTH MANAGER
                oauthManager.updateConnection(currentToken, true);

                Platform.runLater(() -> {
                    updateConnectionState(true);
                    showStatus("✅ User info retrieved: " + currentToken.getUserEmail(), "success");
                });
            } else {
                Platform.runLater(() -> {
                    showStatus("❌ Failed to get user info", "error");
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
                statusText.setText("Connected");
                statusText.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-font-weight: 500;");
            }

            String displayEmail = currentToken.getUserEmail() != null ? currentToken.getUserEmail() : "Unknown Email";
            String displayName = currentToken.getDisplayName();

            // Update main status label
            if (statusLabel != null) {
                statusLabel.setText("🎉 Connected to " + displayEmail + " - Gmail integration is ready!");
                statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-wrap-text: true; -fx-padding: 15 0 0 0;");
            }

            // Update connected section labels
            if (connectedEmailLabel != null) {
                connectedEmailLabel.setText("📧 Connected as: " + displayEmail);
                connectedEmailLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-text-alignment: center;");
            }

            if (connectedNameLabel != null) {
                connectedNameLabel.setText("👤 " + displayName);
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
                statusText.setText("Not Connected");
                statusText.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545; -fx-font-weight: 500;");
            }

            // Update main status label for disconnected state
            if (statusLabel != null) {
                statusLabel.setText("Connect your Google account to enable Gmail integration");
                statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #495057; -fx-wrap-text: true; -fx-padding: 15 0 0 0;");
            }

            // Clear connected section labels
            if (connectedEmailLabel != null) {
                connectedEmailLabel.setText("📧 No account connected");
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
            signInButton.setText("🚀 Connect with Google");
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

    public void cleanup() {
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

    // ======================== ADDITIONAL UTILITY METHODS ========================

    public void forceRefreshToken() {
        if (currentToken == null || !currentToken.hasRefreshToken()) {
            showStatus("❌ No refresh token available", "error");
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
                    showStatus("✅ Token refreshed successfully", "success");
                } else {
                    showStatus("❌ Token refresh failed. Please sign in again.", "error");
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
}