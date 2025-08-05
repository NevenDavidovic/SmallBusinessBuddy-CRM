package smallbusinessbuddycrm.services.google;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * HTTP server for handling OAuth callbacks
 */
public class CallbackServer {

    private static final int PORT = 8080;
    private static final String CALLBACK_PATH = "/callback";

    private HttpServer server;
    private Consumer<String> callbackHandler;

    /**
     * Starts the callback server
     * @param callbackHandler Handler for processing OAuth callbacks
     */
    public void start(Consumer<String> callbackHandler) {
        this.callbackHandler = callbackHandler;

        try {
            if (server != null) {
                server.stop(0);
            }

            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext(CALLBACK_PATH, new OAuthCallbackHandler());
            server.setExecutor(null);
            server.start();

            System.out.println("✅ Callback server started on http://localhost:" + PORT);
        } catch (IOException e) {
            System.err.println("❌ Failed to start callback server: " + e.getMessage());
            throw new RuntimeException("Failed to start callback server", e);
        }
    }

    /**
     * Stops the callback server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Callback server stopped");
        }
    }

    /**
     * Checks if the callback server is running
     * @return true if server is running
     */
    public boolean isRunning() {
        return server != null;
    }

    /**
     * Tests the callback server connectivity
     * @return true if server responds correctly
     */
    public boolean testConnection() {
        try {
            java.net.URL testUrl = new java.net.URL("http://localhost:" + PORT + CALLBACK_PATH + "?test=true");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) testUrl.openConnection();
            int responseCode = conn.getResponseCode();
            System.out.println("✅ Callback server test: HTTP " + responseCode);
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("❌ Callback server test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * HTTP handler for OAuth callbacks
     */
    private class OAuthCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("🎯 Callback received: " + (query != null ? "YES" : "NO"));

            // Send fancy success page
            String response = SuccessPageGenerator.generateSuccessPage();

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();

            // Process OAuth callback if it contains authorization code
            if (query != null && query.contains("code=") && callbackHandler != null) {
                Platform.runLater(() -> callbackHandler.accept(query));
            }
        }
    }

    /**
     * Extracts parameter value from query string
     * @param query URL query string
     * @param paramName Parameter name to extract
     * @return Parameter value or empty string if not found
     */
    public static String extractParameter(String query, String paramName) {
        try {
            int start = query.indexOf(paramName + "=") + paramName.length() + 1;
            int end = query.indexOf("&", start);
            if (end == -1) end = query.length();
            return java.net.URLDecoder.decode(query.substring(start, end), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}