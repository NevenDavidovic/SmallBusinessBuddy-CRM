package smallbusinessbuddycrm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class NewsletterBuilderController implements Initializable {

    // Header Section Controls
    @FXML private TextField newsletterTitleField;
    @FXML private TextField companyNameField;
    @FXML private ColorPicker headerColorPicker;
    @FXML private ComboBox<String> templateCombo;

    // Content Building Controls
    @FXML private VBox componentsPanel;
    @FXML private ScrollPane contentCanvas;
    @FXML private VBox contentContainer;
    @FXML private HTMLEditor contentEditor;

    // Component Buttons
    @FXML private Button addTextButton;
    @FXML private Button addImageButton;
    @FXML private Button addHeadingButton;
    @FXML private Button addButtonButton;
    @FXML private Button addDividerButton;

    // Action Buttons
    @FXML private Button previewButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button exportHtmlButton;
    @FXML private Button sendButton;

    // Preview Controls
    @FXML private WebView previewWebView;
    @FXML private VBox previewContainer;

    // Newsletter Data
    private StringBuilder newsletterHTML;
    private String currentTemplate = "modern";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NewsletterBuilderController.initialize() called");

        setupInitialValues();
        setupEventHandlers();
        setupDragAndDrop();
        generatePreview();

        System.out.println("Newsletter Builder initialized successfully");
    }

    private void setupInitialValues() {
        // Setup template options
        templateCombo.getItems().addAll(
                "Modern Clean",
                "Professional Blue",
                "Elegant Dark",
                "Colorful Marketing",
                "Minimalist White"
        );
        templateCombo.setValue("Modern Clean");

        // Setup default values
        newsletterTitleField.setText("Weekly Newsletter");
        companyNameField.setText("Your Company Name");

        // Initialize HTML content
        newsletterHTML = new StringBuilder();

        // Setup content editor with some default content
        contentEditor.setHtmlText("<p>Start building your newsletter content here...</p>");
    }

    private void setupEventHandlers() {
        // Template selection
        templateCombo.setOnAction(e -> {
            currentTemplate = templateCombo.getValue().toLowerCase().replace(" ", "");
            generatePreview();
        });

        // Header controls
        newsletterTitleField.textProperty().addListener((obs, old, newVal) -> generatePreview());
        companyNameField.textProperty().addListener((obs, old, newVal) -> generatePreview());
        headerColorPicker.setOnAction(e -> generatePreview());

        // Component addition buttons
        addTextButton.setOnAction(e -> addTextComponent());
        addImageButton.setOnAction(e -> addImageComponent());
        addHeadingButton.setOnAction(e -> addHeadingComponent());
        addButtonButton.setOnAction(e -> addButtonComponent());
        addDividerButton.setOnAction(e -> addDividerComponent());

        // Action buttons
        previewButton.setOnAction(e -> generatePreview());
        saveButton.setOnAction(e -> saveNewsletter());
        loadButton.setOnAction(e -> loadNewsletter());
        exportHtmlButton.setOnAction(e -> exportToHTML());
        sendButton.setOnAction(e -> sendNewsletter());

        // Content editor updates
        contentEditor.setOnKeyReleased(e -> generatePreview());
    }

    private void setupDragAndDrop() {
        // Make component buttons draggable
        setupDraggableButton(addTextButton, "text");
        setupDraggableButton(addImageButton, "image");
        setupDraggableButton(addHeadingButton, "heading");
        setupDraggableButton(addButtonButton, "button");
        setupDraggableButton(addDividerButton, "divider");

        // Make content container accept drops
        contentContainer.setOnDragOver(event -> {
            if (event.getGestureSource() != contentContainer && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        contentContainer.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String componentType = db.getString();
                addComponentByType(componentType);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupDraggableButton(Button button, String componentType) {
        button.setOnDragDetected(event -> {
            Dragboard db = button.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(componentType);
            db.setContent(content);
            event.consume();
        });
    }

    private void addComponentByType(String type) {
        switch (type) {
            case "text": addTextComponent(); break;
            case "image": addImageComponent(); break;
            case "heading": addHeadingComponent(); break;
            case "button": addButtonComponent(); break;
            case "divider": addDividerComponent(); break;
        }
    }

    private void addTextComponent() {
        TextInputDialog dialog = new TextInputDialog("Enter your text content here...");
        dialog.setTitle("Add Text Component");
        dialog.setHeaderText("Text Content");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            String currentContent = contentEditor.getHtmlText();
            String newContent = currentContent.replace("</body>",
                    "<div class='text-component' style='margin: 15px 0; padding: 10px; border-left: 3px solid #007bff;'>" +
                            "<p>" + text + "</p>" +
                            "</div></body>");
            contentEditor.setHtmlText(newContent);
            generatePreview();
        });
    }

    private void addImageComponent() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) addImageButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String imagePath = file.toURI().toString();
            String currentContent = contentEditor.getHtmlText();
            String newContent = currentContent.replace("</body>",
                    "<div class='image-component' style='margin: 20px 0; text-align: center;'>" +
                            "<img src='" + imagePath + "' style='max-width: 100%; height: auto; border-radius: 8px;' alt='Newsletter Image'>" +
                            "</div></body>");
            contentEditor.setHtmlText(newContent);
            generatePreview();
        }
    }

    private void addHeadingComponent() {
        TextInputDialog dialog = new TextInputDialog("Newsletter Heading");
        dialog.setTitle("Add Heading Component");
        dialog.setHeaderText("Heading Content");
        dialog.setContentText("Heading:");

        dialog.showAndWait().ifPresent(heading -> {
            String currentContent = contentEditor.getHtmlText();
            String newContent = currentContent.replace("</body>",
                    "<div class='heading-component' style='margin: 25px 0 15px 0;'>" +
                            "<h2 style='color: #333; font-family: Arial, sans-serif; font-size: 24px; font-weight: bold; margin: 0;'>" +
                            heading + "</h2>" +
                            "</div></body>");
            contentEditor.setHtmlText(newContent);
            generatePreview();
        });
    }

    private void addButtonComponent() {
        Dialog<ButtonResult> dialog = new Dialog<>();
        dialog.setTitle("Add Button Component");
        dialog.setHeaderText("Button Configuration");

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField buttonText = new TextField("Click Here");
        TextField buttonUrl = new TextField("https://");
        ComboBox<String> buttonColor = new ComboBox<>();
        buttonColor.getItems().addAll("Primary Blue", "Success Green", "Warning Orange", "Danger Red");
        buttonColor.setValue("Primary Blue");

        grid.add(new Label("Button Text:"), 0, 0);
        grid.add(buttonText, 1, 0);
        grid.add(new Label("Link URL:"), 0, 1);
        grid.add(buttonUrl, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(buttonColor, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new ButtonResult(buttonText.getText(), buttonUrl.getText(), buttonColor.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String colorStyle = getButtonColorStyle(result.color);
            String currentContent = contentEditor.getHtmlText();
            String newContent = currentContent.replace("</body>",
                    "<div class='button-component' style='margin: 20px 0; text-align: center;'>" +
                            "<a href='" + result.url + "' style='" + colorStyle +
                            " text-decoration: none; padding: 12px 24px; border-radius: 6px; display: inline-block; font-weight: bold;'>" +
                            result.text + "</a>" +
                            "</div></body>");
            contentEditor.setHtmlText(newContent);
            generatePreview();
        });
    }

    private void addDividerComponent() {
        String currentContent = contentEditor.getHtmlText();
        String newContent = currentContent.replace("</body>",
                "<div class='divider-component' style='margin: 30px 0;'>" +
                        "<hr style='border: none; height: 2px; background: linear-gradient(to right, #007bff, #6c757d); margin: 0;'>" +
                        "</div></body>");
        contentEditor.setHtmlText(newContent);
        generatePreview();
    }

    private String getButtonColorStyle(String color) {
        switch (color) {
            case "Success Green": return "background-color: #28a745; color: white;";
            case "Warning Orange": return "background-color: #fd7e14; color: white;";
            case "Danger Red": return "background-color: #dc3545; color: white;";
            default: return "background-color: #007bff; color: white;";
        }
    }

    private void generatePreview() {
        String template = getNewsletterTemplate();
        String content = contentEditor.getHtmlText();

        // Extract body content from editor
        String bodyContent = extractBodyContent(content);

        // Replace template placeholders
        String finalHTML = template
                .replace("{{TITLE}}", newsletterTitleField.getText())
                .replace("{{COMPANY}}", companyNameField.getText())
                .replace("{{CONTENT}}", bodyContent)
                .replace("{{DATE}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        previewWebView.getEngine().loadContent(finalHTML);
        newsletterHTML = new StringBuilder(finalHTML);
    }

    private String extractBodyContent(String htmlText) {
        if (htmlText == null) return "";

        // Remove HTML and BODY tags, keep content
        String content = htmlText.replaceAll("(?i)<html[^>]*>", "")
                .replaceAll("(?i)</html>", "")
                .replaceAll("(?i)<body[^>]*>", "")
                .replaceAll("(?i)</body>", "")
                .trim();

        return content;
    }

    private String getNewsletterTemplate() {
        // Modern, responsive email template
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>{{TITLE}}</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #007bff, #0056b3); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; font-weight: 300; }" +
                ".header p { margin: 5px 0 0 0; opacity: 0.9; }" +
                ".content { padding: 30px; line-height: 1.6; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }" +
                ".text-component { margin: 15px 0; }" +
                ".heading-component { margin: 25px 0 15px 0; }" +
                ".image-component { margin: 20px 0; text-align: center; }" +
                ".button-component { margin: 20px 0; text-align: center; }" +
                ".divider-component { margin: 30px 0; }" +
                "@media only screen and (max-width: 600px) {" +
                "  .container { width: 100% !important; }" +
                "  .content { padding: 20px !important; }" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>{{TITLE}}</h1>" +
                "<p>{{COMPANY}} • {{DATE}}</p>" +
                "</div>" +
                "<div class='content'>" +
                "{{CONTENT}}" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2025 {{COMPANY}}. All rights reserved.</p>" +
                "<p>You're receiving this newsletter because you subscribed to our updates.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private void saveNewsletter() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Newsletter");
            fileChooser.setInitialFileName("newsletter_" + System.currentTimeMillis() + ".html");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            Stage stage = (Stage) saveButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(newsletterHTML.toString());
                    showSuccess("Newsletter saved successfully!");
                }
            }
        } catch (Exception e) {
            showAlert("Save Error", "Failed to save newsletter: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadNewsletter() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Newsletter");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            Stage stage = (Stage) loadButton.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    previewWebView.getEngine().loadContent(content.toString());
                    newsletterHTML = content;
                    showSuccess("Newsletter loaded successfully!");
                }
            }
        } catch (Exception e) {
            showAlert("Load Error", "Failed to load newsletter: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportToHTML() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Newsletter as HTML");
            fileChooser.setInitialFileName("newsletter_export.html");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            Stage stage = (Stage) exportHtmlButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(newsletterHTML.toString());

                    // Also copy to clipboard
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(newsletterHTML.toString());
                    Clipboard.getSystemClipboard().setContent(clipboardContent);

                    showSuccess("Newsletter exported and copied to clipboard!");
                }
            }
        } catch (Exception e) {
            showAlert("Export Error", "Failed to export newsletter: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void sendNewsletter() {
        // Placeholder for email sending functionality
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Send Newsletter");
        info.setHeaderText("Email Integration");
        info.setContentText("Email sending functionality would be integrated here.\n\n" +
                "The newsletter HTML has been copied to your clipboard.\n" +
                "You can paste it into your email marketing platform.");

        // Copy to clipboard
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(newsletterHTML.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        info.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();

        // Auto-close after 3 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> alert.close())
        );
        timeline.play();
    }

    // Helper class for button component dialog
    private static class ButtonResult {
        String text;
        String url;
        String color;

        ButtonResult(String text, String url, String color) {
            this.text = text;
            this.url = url;
            this.color = color;
        }
    }
}