package smallbusinessbuddycrm.services.google;

import smallbusinessbuddycrm.model.OAuthToken;

/**
 * Singleton class to manage Google OAuth state across the entire application
 * This ensures that all dialogs and controllers can access the same OAuth session
 */
public class GoogleOAuthManager {
    private static GoogleOAuthManager instance;

    // Services
    private TokenManager tokenManager;
    private OAuthService oauthService;
    private GmailService gmailService;

    // Current state
    private OAuthToken currentToken;
    private boolean isConnected = false;

    private GoogleOAuthManager() {
        this.tokenManager = new TokenManager();
        this.oauthService = new OAuthService();
        this.gmailService = new GmailService();

        // Try to load saved tokens on initialization
        loadSavedTokens();
    }

    public static GoogleOAuthManager getInstance() {
        if (instance == null) {
            synchronized (GoogleOAuthManager.class) {
                if (instance == null) {
                    instance = new GoogleOAuthManager();
                }
            }
        }
        return instance;
    }

    /**
     * Load saved tokens from storage
     */
    private void loadSavedTokens() {
        try {
            OAuthToken savedToken = tokenManager.loadSavedTokens();
            if (savedToken != null && savedToken.isValid()) {
                // Validate token is still good
                if (oauthService.validateToken(savedToken.getAccessToken())) {
                    currentToken = savedToken;
                    isConnected = true;
                    System.out.println("✅ OAuth Manager: Loaded valid saved token for " + savedToken.getUserEmail());
                } else if (savedToken.hasRefreshToken()) {
                    // Try to refresh the token
                    String newAccessToken = oauthService.refreshAccessToken(savedToken.getRefreshToken());
                    if (newAccessToken != null) {
                        currentToken = savedToken.withUpdatedAccessToken(newAccessToken);
                        tokenManager.saveTokens(currentToken);
                        isConnected = true;
                        System.out.println("✅ OAuth Manager: Refreshed token for " + currentToken.getUserEmail());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading saved tokens in OAuth Manager: " + e.getMessage());
        }
    }

    /**
     * Update the OAuth state (called by GoogleOAuthController)
     */
    public void updateConnection(OAuthToken token, boolean connected) {
        this.currentToken = token;
        this.isConnected = connected;

        if (connected && token != null) {
            System.out.println("✅ OAuth Manager: Connection updated - " + token.getUserEmail());
        } else {
            System.out.println("❌ OAuth Manager: Connection cleared");
        }
    }

    /**
     * Check if Gmail is connected
     */
    public boolean isGmailConnected() {
        if (!isConnected || currentToken == null || !currentToken.isValid()) {
            return false;
        }

        // Validate token is still good
        try {
            return oauthService.validateToken(currentToken.getAccessToken());
        } catch (Exception e) {
            System.err.println("Error validating token in OAuth Manager: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get current access token
     */
    public String getAccessToken() {
        return currentToken != null ? currentToken.getAccessToken() : null;
    }

    /**
     * Get current user email
     */
    public String getUserEmail() {
        return currentToken != null ? currentToken.getUserEmail() : null;
    }

    /**
     * Get current user name
     */
    public String getUserName() {
        return currentToken != null ? currentToken.getUserName() : null;
    }

    /**
     * Get current token
     */
    public OAuthToken getCurrentToken() {
        return currentToken;
    }

    /**
     * Get Gmail service instance
     */
    public GmailService getGmailService() {
        return gmailService;
    }

    /**
     * Get OAuth service instance
     */
    public OAuthService getOAuthService() {
        return oauthService;
    }

    /**
     * Get Token manager instance
     */
    public TokenManager getTokenManager() {
        return tokenManager;
    }

    /**
     * Clear connection (called when signing out)
     */
    public void clearConnection() {
        this.currentToken = null;
        this.isConnected = false;
        tokenManager.clearSavedTokens();
        System.out.println("🧹 OAuth Manager: Connection cleared");
    }

    /**
     * Refresh access token if needed
     */
    public boolean refreshTokenIfNeeded() {
        if (currentToken == null || !currentToken.hasRefreshToken()) {
            return false;
        }

        try {
            String newAccessToken = oauthService.refreshAccessToken(currentToken.getRefreshToken());
            if (newAccessToken != null) {
                currentToken = currentToken.withUpdatedAccessToken(newAccessToken);
                tokenManager.saveTokens(currentToken);
                isConnected = true;
                System.out.println("✅ OAuth Manager: Token refreshed successfully");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error refreshing token: " + e.getMessage());
        }

        return false;
    }

    /**
     * Send email using the current OAuth session
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send email: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendEmail(currentToken.getAccessToken(), to, subject, body);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send payment slip email with attachments
     */
    public boolean sendPaymentSlip(String to, String payerName, String organizationName,
                                   String amount, String description,
                                   byte[] pdfContent, java.awt.image.BufferedImage barcodeImage) {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send payment slip: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendPaymentSlip(
                    currentToken.getAccessToken(), to, payerName, organizationName,
                    amount, description, pdfContent, barcodeImage
            );
        } catch (Exception e) {
            System.err.println("Error sending payment slip: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send test email
     */
    public boolean sendTestEmail() {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send test email: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendTestEmail(
                    currentToken.getAccessToken(),
                    currentToken.getUserEmail(),
                    currentToken.getUserName()
            );
        } catch (Exception e) {
            System.err.println("Error sending test email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send quick test email
     */
    public boolean sendQuickTestEmail() {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send quick test email: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendQuickTestEmail(
                    currentToken.getAccessToken(),
                    currentToken.getUserEmail(),
                    currentToken.getUserName()
            );
        } catch (Exception e) {
            System.err.println("Error sending quick test email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate Gmail access
     */
    public boolean validateGmailAccess() {
        if (!isGmailConnected()) {
            return false;
        }

        try {
            return gmailService.validateGmailAccess(currentToken.getAccessToken());
        } catch (Exception e) {
            System.err.println("Error validating Gmail access: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force refresh token
     */
    public boolean forceRefreshToken() {
        if (currentToken == null || !currentToken.hasRefreshToken()) {
            System.err.println("❌ No refresh token available");
            return false;
        }

        try {
            String newAccessToken = oauthService.refreshAccessToken(currentToken.getRefreshToken());
            if (newAccessToken != null) {
                currentToken = currentToken.withUpdatedAccessToken(newAccessToken);
                tokenManager.saveTokens(currentToken);
                isConnected = true;
                System.out.println("✅ OAuth Manager: Token force refreshed successfully");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error force refreshing token: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get connection status details
     */
    public String getConnectionStatus() {
        if (!isConnected || currentToken == null) {
            return "Not connected";
        }

        return "Connected as: " + currentToken.getUserEmail();
    }

    /**
     * Check if has valid connection with user info
     */
    public boolean hasValidConnection() {
        return isConnected && currentToken != null && currentToken.isValid() && currentToken.hasUserInfo();
    }

    /**
     * Debug information
     */
    public void printDebugInfo() {
        System.out.println("\n=== OAUTH MANAGER DEBUG INFO ===");
        System.out.println("Connected: " + isConnected);
        System.out.println("Current Token: " + (currentToken != null ? "Present" : "null"));
        if (currentToken != null) {
            System.out.println("User Email: " + currentToken.getUserEmail());
            System.out.println("User Name: " + currentToken.getUserName());
            System.out.println("Token Valid: " + currentToken.isValid());
            System.out.println("Has Refresh Token: " + currentToken.hasRefreshToken());
            System.out.println("Has User Info: " + currentToken.hasUserInfo());
        }
        System.out.println("Token Manager Has Saved Tokens: " + tokenManager.hasSavedTokens());
        System.out.println("Gmail Service Validation: " + (isConnected ? validateGmailAccess() : "N/A"));
        System.out.println("================================\n");
    }

    /**
     * Send HTML newsletter using the current OAuth session
     */
    public boolean sendHtmlNewsletter(String to, String subject, String htmlContent) {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send newsletter: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendHtmlNewsletter(currentToken.getAccessToken(), to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Error sending HTML newsletter: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send multipart newsletter (HTML + plain text) using the current OAuth session
     */
    public boolean sendMultipartNewsletter(String to, String subject, String htmlContent, String plainTextContent) {
        if (!isGmailConnected()) {
            System.err.println("❌ Cannot send newsletter: Gmail not connected");
            return false;
        }

        try {
            return gmailService.sendMultipartNewsletter(currentToken.getAccessToken(), to, subject, htmlContent, plainTextContent);
        } catch (Exception e) {
            System.err.println("Error sending multipart newsletter: " + e.getMessage());
            return false;
        }
    }
}