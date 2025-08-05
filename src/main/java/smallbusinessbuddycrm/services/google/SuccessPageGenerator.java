package smallbusinessbuddycrm.services.google;

/**
 * Generates HTML success pages for OAuth callbacks
 */
public class SuccessPageGenerator {

    /**
     * Generates a beautiful success page for OAuth completion
     * @return HTML string for success page
     */
    public static String generateSuccessPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Authentication Successful - Small Business Buddy CRM</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            min-height: 100vh; display: flex; align-items: center; justify-content: center;\n" +
                "        }\n" +
                "        .success-container {\n" +
                "            background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(20px);\n" +
                "            border-radius: 24px; padding: 60px 40px; text-align: center;\n" +
                "            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2); max-width: 500px; width: 90%;\n" +
                "            animation: slideUp 0.8s ease-out forwards;\n" +
                "        }\n" +
                "        .checkmark {\n" +
                "            width: 80px; height: 80px; border-radius: 50%;\n" +
                "            background: linear-gradient(135deg, #4CAF50, #45a049);\n" +
                "            margin: 0 auto 30px; position: relative;\n" +
                "            animation: bounceIn 0.6s ease-out 0.5s forwards;\n" +
                "            transform: scale(0);\n" +
                "        }\n" +
                "        .checkmark::after {\n" +
                "            content: '✓'; position: absolute; top: 50%; left: 50%;\n" +
                "            transform: translate(-50%, -50%); color: white;\n" +
                "            font-size: 40px; font-weight: bold;\n" +
                "        }\n" +
                "        h1 { \n" +
                "            font-size: 32px; color: #2c3e50; margin-bottom: 15px;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 0.3s forwards;\n" +
                "        }\n" +
                "        p { \n" +
                "            font-size: 18px; color: #7f8c8d; margin-bottom: 30px;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 0.6s forwards;\n" +
                "        }\n" +
                "        .app-info {\n" +
                "            background: linear-gradient(135deg, #f8f9fa, #e9ecef);\n" +
                "            border-radius: 16px; padding: 20px; margin: 30px 0;\n" +
                "            border-left: 4px solid #4CAF50;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 0.9s forwards;\n" +
                "        }\n" +
                "        .app-name { \n" +
                "            font-size: 20px; font-weight: 600; color: #2c3e50;\n" +
                "            margin-bottom: 5px;\n" +
                "        }\n" +
                "        .app-subtitle {\n" +
                "            font-size: 14px; color: #6c757d;\n" +
                "        }\n" +
                "        .actions { \n" +
                "            display: flex; gap: 15px; justify-content: center; margin-top: 30px;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 1.2s forwards;\n" +
                "        }\n" +
                "        .btn {\n" +
                "            padding: 12px 24px; border: none; border-radius: 12px;\n" +
                "            font-size: 16px; font-weight: 600; cursor: pointer;\n" +
                "            transition: all 0.3s ease; text-decoration: none;\n" +
                "        }\n" +
                "        .btn-primary { \n" +
                "            background: linear-gradient(135deg, #667eea, #764ba2); \n" +
                "            color: white; \n" +
                "        }\n" +
                "        .btn:hover { \n" +
                "            transform: translateY(-2px); \n" +
                "            box-shadow: 0 8px 25px rgba(0,0,0,0.2); \n" +
                "        }\n" +
                "        .auto-close {\n" +
                "            font-size: 12px; color: #adb5bd; margin-top: 20px;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 1.5s forwards;\n" +
                "        }\n" +
                "        .features {\n" +
                "            display: flex; justify-content: space-around; margin: 20px 0;\n" +
                "            opacity: 0; animation: fadeIn 0.8s ease-out 1.0s forwards;\n" +
                "        }\n" +
                "        .feature {\n" +
                "            text-align: center; flex: 1;\n" +
                "        }\n" +
                "        .feature-icon {\n" +
                "            font-size: 24px; margin-bottom: 8px;\n" +
                "        }\n" +
                "        .feature-text {\n" +
                "            font-size: 12px; color: #6c757d;\n" +
                "        }\n" +
                "        @keyframes slideUp { \n" +
                "            from { transform: translateY(30px); opacity: 0; }\n" +
                "            to { transform: translateY(0); opacity: 1; }\n" +
                "        }\n" +
                "        @keyframes bounceIn { \n" +
                "            0% { transform: scale(0); } \n" +
                "            50% { transform: scale(1.2); } \n" +
                "            100% { transform: scale(1); }\n" +
                "        }\n" +
                "        @keyframes fadeIn {\n" +
                "            to { opacity: 1; }\n" +
                "        }\n" +
                "        .countdown {\n" +
                "            display: inline-block;\n" +
                "            font-weight: bold;\n" +
                "            color: #667eea;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"success-container\">\n" +
                "        <div class=\"checkmark\"></div>\n" +
                "        <h1>🎉 Authentication Successful!</h1>\n" +
                "        <p>Your Google account has been securely connected to your CRM.</p>\n" +
                "        \n" +
                "        <div class=\"features\">\n" +
                "            <div class=\"feature\">\n" +
                "                <div class=\"feature-icon\">🔒</div>\n" +
                "                <div class=\"feature-text\">Secure OAuth 2.0</div>\n" +
                "            </div>\n" +
                "            <div class=\"feature\">\n" +
                "                <div class=\"feature-icon\">📧</div>\n" +
                "                <div class=\"feature-text\">Gmail Integration</div>\n" +
                "            </div>\n" +
                "            <div class=\"feature\">\n" +
                "                <div class=\"feature-icon\">💾</div>\n" +
                "                <div class=\"feature-text\">Auto-Save Tokens</div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"app-info\">\n" +
                "            <div class=\"app-name\">🏢 Small Business Buddy CRM</div>\n" +
                "            <div class=\"app-subtitle\">Gmail Integration Active</div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"actions\">\n" +
                "            <button class=\"btn btn-primary\" onclick=\"closeWindow()\">🚀 Return to CRM</button>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"auto-close\">\n" +
                "            Window will close automatically in <span class=\"countdown\" id=\"countdown\">3</span> seconds...\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        let timeLeft = 3;\n" +
                "        const countdownElement = document.getElementById('countdown');\n" +
                "        \n" +
                "        function updateCountdown() {\n" +
                "            countdownElement.textContent = timeLeft;\n" +
                "            timeLeft--;\n" +
                "            \n" +
                "            if (timeLeft < 0) {\n" +
                "                closeWindow();\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        function closeWindow() { \n" +
                "            try { \n" +
                "                window.close(); \n" +
                "            } catch(e) { \n" +
                "                console.log('Window close not supported, redirecting...'); \n" +
                "                document.body.innerHTML = '<div style=\"text-align: center; padding: 50px; font-family: Arial, sans-serif;\"><h2>✅ Success!</h2><p>You can close this window now.</p></div>';\n" +
                "            } \n" +
                "        }\n" +
                "        \n" +
                "        // Start countdown\n" +
                "        setInterval(updateCountdown, 1000);\n" +
                "        \n" +
                "        // Also try to close after 3 seconds regardless\n" +
                "        setTimeout(closeWindow, 3000);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Generates an error page for OAuth failures
     * @param errorMessage Error message to display
     * @return HTML string for error page
     */
    public static String generateErrorPage(String errorMessage) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Authentication Error - Small Business Buddy CRM</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);\n" +
                "            min-height: 100vh; display: flex; align-items: center; justify-content: center;\n" +
                "        }\n" +
                "        .error-container {\n" +
                "            background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(20px);\n" +
                "            border-radius: 24px; padding: 60px 40px; text-align: center;\n" +
                "            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2); max-width: 500px; width: 90%;\n" +
                "        }\n" +
                "        .error-icon {\n" +
                "            font-size: 64px; margin-bottom: 20px;\n" +
                "        }\n" +
                "        h1 { font-size: 28px; color: #e74c3c; margin-bottom: 15px; }\n" +
                "        p { font-size: 16px; color: #7f8c8d; margin-bottom: 30px; }\n" +
                "        .error-details {\n" +
                "            background: #f8f9fa; border-radius: 12px; padding: 20px;\n" +
                "            border-left: 4px solid #e74c3c; margin: 20px 0;\n" +
                "        }\n" +
                "        .btn {\n" +
                "            padding: 12px 24px; border: none; border-radius: 12px;\n" +
                "            font-size: 16px; font-weight: 600; cursor: pointer;\n" +
                "            background: #e74c3c; color: white;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"error-container\">\n" +
                "        <div class=\"error-icon\">❌</div>\n" +
                "        <h1>Authentication Failed</h1>\n" +
                "        <p>There was an error connecting your Google account.</p>\n" +
                "        <div class=\"error-details\">\n" +
                "            <strong>Error:</strong> " + (errorMessage != null ? errorMessage : "Unknown error") + "\n" +
                "        </div>\n" +
                "        <button class=\"btn\" onclick=\"window.close()\">Close Window</button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}