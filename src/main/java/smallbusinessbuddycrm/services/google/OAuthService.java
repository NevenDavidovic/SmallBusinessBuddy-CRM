package smallbusinessbuddycrm.services.google;

import io.github.cdimascio.dotenv.Dotenv;
import smallbusinessbuddycrm.utilities.JsonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Service responsible for OAuth 2.0 authentication flow with Google
 */
public class OAuthService {

    // Load environment variables
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String CLIENT_ID = dotenv.get("GOOGLE_OAUTH_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("GOOGLE_OAUTH_CLIENT_SECRET");
    private static final String REDIRECT_URI = dotenv.get("GOOGLE_OAUTH_REDIRECT_URI");
    private static final String SCOPE = "openid email profile https://www.googleapis.com/auth/gmail.send";

    private final HttpClient httpClient;
    private String currentState;

    public OAuthService() {
        // Validate environment variables
        if (CLIENT_ID == null || CLIENT_SECRET == null || REDIRECT_URI == null) {
            throw new IllegalStateException("Missing required environment variables: GOOGLE_OAUTH_CLIENT_ID, GOOGLE_OAUTH_CLIENT_SECRET, or GOOGLE_OAUTH_REDIRECT_URI");
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Builds the OAuth authorization URL
     * @return OAuth URL for user authorization
     */
    public String buildAuthorizationUrl() {
        try {
            currentState = UUID.randomUUID().toString();

            return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&state=" + currentState +
                    "&access_type=offline" +
                    "&prompt=consent";
        } catch (Exception e) {
            System.err.println("❌ Error building OAuth URL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates the state parameter for security
     * @param receivedState State received from OAuth callback
     * @return true if state is valid
     */
    public boolean validateState(String receivedState) {
        return currentState != null && currentState.equals(receivedState);
    }

    /**
     * Exchanges authorization code for access token
     * @param authCode Authorization code from OAuth callback
     * @return Token response body
     */
    public String exchangeAuthorizationCode(String authCode) {
        try {
            System.out.println("🔄 Exchanging authorization code...");

            String tokenRequestBody =
                    "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                            "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                            "&code=" + URLEncoder.encode(authCode, StandardCharsets.UTF_8) +
                            "&grant_type=authorization_code" +
                            "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 Token response status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ Token exchange successful");
                return response.body();
            } else {
                System.err.println("❌ Token exchange failed: " + response.statusCode());
                System.err.println("❌ Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Token exchange error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Refreshes an expired access token
     * @param refreshToken Refresh token
     * @return New access token or null if failed
     */
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            System.out.println("❌ No refresh token available");
            return null;
        }

        try {
            System.out.println("🔄 Attempting to refresh access token...");

            String refreshRequestBody =
                    "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                            "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                            "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                            "&grant_type=refresh_token";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(refreshRequestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String newAccessToken = JsonUtils.extractValue(response.body(), "access_token");
                if (newAccessToken != null && !newAccessToken.isEmpty()) {
                    System.out.println("✅ Access token refreshed successfully");
                    return newAccessToken;
                }
            } else {
                System.err.println("❌ Token refresh failed with status: " + response.statusCode());
                System.err.println("❌ Response: " + response.body());
            }

            return null;
        } catch (Exception e) {
            System.err.println("❌ Token refresh failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates if an access token is still valid
     * @param accessToken Access token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 Token validation status: " + response.statusCode());
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("❌ Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets user information from Google's userinfo endpoint
     * @param accessToken Access token
     * @return User info response body
     */
    public String getUserInfo(String accessToken) {
        try {
            System.out.println("🔍 Getting user info with access token...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 UserInfo status: " + response.statusCode());
            System.out.println("🔍 UserInfo response: " + response.body());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("❌ UserInfo request failed: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ UserInfo request error: " + e.getMessage());
            return null;
        }
    }

    public String getCurrentState() {
        return currentState;
    }

    public void cleanup() {
        // Cleanup any resources if needed
    }
}