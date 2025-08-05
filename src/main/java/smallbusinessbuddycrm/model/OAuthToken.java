package smallbusinessbuddycrm.model;

/**
 * Model class representing OAuth token data
 */
public class OAuthToken {

    private String accessToken;
    private String refreshToken;
    private String userEmail;
    private String userName;
    private long createdAt;

    public OAuthToken() {
        this.createdAt = System.currentTimeMillis();
    }

    public OAuthToken(String accessToken, String refreshToken, String userEmail, String userName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userEmail = userEmail;
        this.userName = userName;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public boolean isValid() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public boolean hasUserInfo() {
        return userEmail != null && !userEmail.isEmpty();
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }

    public String getDisplayName() {
        if (userName != null && !userName.isEmpty()) {
            return userName;
        }
        return userEmail != null ? userEmail : "Unknown User";
    }

    @Override
    public String toString() {
        return "OAuthToken{" +
                "accessToken='" + (accessToken != null ? "***" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "***" : "null") + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userName='" + userName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Creates a copy of this token with updated access token
     * @param newAccessToken New access token
     * @return New OAuthToken instance
     */
    public OAuthToken withUpdatedAccessToken(String newAccessToken) {
        return new OAuthToken(newAccessToken, this.refreshToken, this.userEmail, this.userName);
    }

    /**
     * Creates a copy of this token with user information
     * @param email User email
     * @param name User name
     * @return New OAuthToken instance
     */
    public OAuthToken withUserInfo(String email, String name) {
        return new OAuthToken(this.accessToken, this.refreshToken, email, name);
    }
}