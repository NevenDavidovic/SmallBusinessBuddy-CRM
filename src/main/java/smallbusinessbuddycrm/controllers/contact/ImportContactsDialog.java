package smallbusinessbuddycrm.controllers.contact;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ImportContactsDialog {

    private Stage dialog;
    private boolean result = false;
    private List<Contact> importedContacts = new ArrayList<>();
    private TextArea previewArea;
    private Label statusLabel;
    private Button importButton;
    private File selectedFile;
    private LanguageManager languageManager;

    public ImportContactsDialog(Stage parent) {
        languageManager = LanguageManager.getInstance();
        createDialog(parent);
    }

    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(languageManager.getText("import.dialog.title"));
        dialog.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Label titleLabel = new Label(languageManager.getText("import.dialog.header"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Instructions
        Label instructionsLabel = new Label(languageManager.getText("import.dialog.instructions"));
        instructionsLabel.setStyle("-fx-text-fill: #666666; -fx-wrap-text: true;");
        instructionsLabel.setWrapText(true);

        // Template download section
        HBox templateBox = new HBox(10);
        templateBox.setAlignment(Pos.CENTER_LEFT);
        Label templateLabel = new Label(languageManager.getText("import.template.question"));
        templateLabel.setStyle("-fx-text-fill: #666666;");

        Button downloadTemplateButton = new Button(languageManager.getText("import.template.download"));
        downloadTemplateButton.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-border-radius: 4; -fx-padding: 8 16;"
        );
        downloadTemplateButton.setOnAction(e -> handleDownloadTemplate());

        templateBox.getChildren().addAll(templateLabel, downloadTemplateButton);

        // File selection section
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);

        Button selectFileButton = new Button(languageManager.getText("import.select.file"));
        selectFileButton.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; " +
                        "-fx-border-radius: 4; -fx-padding: 8 16;"
        );
        selectFileButton.setOnAction(e -> handleSelectFile());

        statusLabel = new Label(languageManager.getText("import.no.file.selected"));
        statusLabel.setStyle("-fx-text-fill: #666666;");

        fileBox.getChildren().addAll(selectFileButton, statusLabel);

        // Preview area
        Label previewLabel = new Label(languageManager.getText("import.preview.label"));
        previewLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        previewArea = new TextArea();
        previewArea.setPrefRowCount(8);
        previewArea.setPrefColumnCount(60);
        previewArea.setEditable(false);
        previewArea.setStyle("-fx-control-inner-background: white; -fx-border-color: #cccccc;");
        previewArea.setText(languageManager.getText("import.preview.placeholder"));

        // Button section
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button(languageManager.getText("import.button.cancel"));
        cancelButton.setStyle(
                "-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-border-radius: 4; -fx-padding: 8 16;"
        );
        cancelButton.setOnAction(e -> {
            result = false;
            dialog.close();
        });

        importButton = new Button(languageManager.getText("import.button.import"));
        importButton.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-border-radius: 4; -fx-padding: 8 16;"
        );
        importButton.setDisable(true);
        importButton.setOnAction(e -> handleImportContacts());

        buttonBox.getChildren().addAll(cancelButton, importButton);

        // Add all components to root
        root.getChildren().addAll(
                titleLabel,
                instructionsLabel,
                new Separator(),
                templateBox,
                new Separator(),
                fileBox,
                previewLabel,
                previewArea,
                buttonBox
        );

        Scene scene = new Scene(root, 700, 500);
        dialog.setScene(scene);
    }

    private void handleDownloadTemplate() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(languageManager.getText("import.template.save.title"));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(languageManager.getText("import.file.filter.csv"), "*.csv")
            );
            fileChooser.setInitialFileName("contacts_template.csv");

            File file = fileChooser.showSaveDialog(dialog);
            if (file != null) {
                createCsvTemplate(file);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle(languageManager.getText("import.template.success.title"));
                successAlert.setHeaderText(languageManager.getText("import.template.success.header"));
                successAlert.setContentText(languageManager.getText("import.template.success.content")
                        .replace("{0}", file.getAbsolutePath()));
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            showError(languageManager.getText("import.template.error.title"), e.getMessage());
        }
    }

    private void createCsvTemplate(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // Write UTF-8 BOM
            writer.write('\ufeff');

            // Write header (use current language)
            String header = languageManager.getText("import.template.header");
            writer.write(header);
            writer.newLine();

            // Write example data
            String exampleData1 = languageManager.getText("import.template.example1");
            String exampleData2 = languageManager.getText("import.template.example2");
            String exampleData3 = languageManager.getText("import.template.example3");

            writer.write(exampleData1);
            writer.newLine();
            writer.write(exampleData2);
            writer.newLine();
            writer.write(exampleData3);
            writer.newLine();

            // Add instructions as comments
            writer.newLine();
            writer.write("# " + languageManager.getText("import.template.instructions.title"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.birthday"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.pin"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.member"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.dates"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.empty"));
            writer.newLine();
            writer.write("# - " + languageManager.getText("import.template.instructions.remove"));
        }
    }

    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(languageManager.getText("import.select.file.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(languageManager.getText("import.file.filter.csv"), "*.csv"),
                new FileChooser.ExtensionFilter(languageManager.getText("import.file.filter.all"), "*.*")
        );

        selectedFile = fileChooser.showOpenDialog(dialog);
        if (selectedFile != null) {
            statusLabel.setText(languageManager.getText("import.file.selected") + ": " + selectedFile.getName());
            previewCsvFile(selectedFile);
            importButton.setDisable(false);
        }
    }

    private void previewCsvFile(File file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            StringBuilder preview = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 10) {
                // Skip UTF-8 BOM if present
                if (lineCount == 0 && line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }

                // Skip comment lines
                if (line.trim().startsWith("#")) {
                    continue;
                }

                preview.append(line).append("\n");
                lineCount++;
            }

            if (lineCount == 10) {
                preview.append(languageManager.getText("import.preview.more"));
            }

            previewArea.setText(preview.toString());

        } catch (Exception e) {
            previewArea.setText(languageManager.getText("import.preview.error") + ": " + e.getMessage());
        }
    }

    private void handleImportContacts() {
        if (selectedFile == null) {
            showError(languageManager.getText("import.error.no.file.title"),
                    languageManager.getText("import.error.no.file.content"));
            return;
        }

        try {
            List<Contact> contacts = parseCsvFile(selectedFile);
            if (contacts.isEmpty()) {
                showError(languageManager.getText("import.error.no.data.title"),
                        languageManager.getText("import.error.no.data.content"));
                return;
            }

            // Show confirmation
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle(languageManager.getText("import.confirm.title"));
            confirmAlert.setHeaderText(languageManager.getText("import.confirm.header")
                    .replace("{0}", String.valueOf(contacts.size())));
            confirmAlert.setContentText(languageManager.getText("import.confirm.content")
                    .replace("{0}", String.valueOf(contacts.size())));

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Import to database
                ContactDAO dao = new ContactDAO();
                int successCount = 0;
                List<String> errors = new ArrayList<>();

                for (Contact contact : contacts) {
                    try {
                        boolean success = dao.createContact(contact);
                        if (success) {
                            successCount++;
                            importedContacts.add(contact);
                        } else {
                            errors.add(languageManager.getText("import.error.failed")
                                    .replace("{0}", contact.getFirstName() + " " + contact.getLastName()));
                        }
                    } catch (Exception e) {
                        errors.add(languageManager.getText("import.error.exception")
                                .replace("{0}", contact.getFirstName() + " " + contact.getLastName())
                                .replace("{1}", e.getMessage()));
                    }
                }

                // Show results
                StringBuilder message = new StringBuilder();
                message.append(languageManager.getText("import.result.success")
                        .replace("{0}", String.valueOf(successCount)));

                if (!errors.isEmpty()) {
                    message.append("\n\n").append(languageManager.getText("import.result.errors")).append("\n");
                    for (String error : errors.subList(0, Math.min(5, errors.size()))) {
                        message.append("• ").append(error).append("\n");
                    }
                    if (errors.size() > 5) {
                        message.append(languageManager.getText("import.result.more.errors")
                                .replace("{0}", String.valueOf(errors.size() - 5)));
                    }
                }

                Alert resultAlert = new Alert(
                        errors.isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING
                );
                resultAlert.setTitle(languageManager.getText("import.result.title"));
                resultAlert.setHeaderText(languageManager.getText("import.result.header"));
                resultAlert.setContentText(message.toString());
                resultAlert.showAndWait();

                if (successCount > 0) {
                    result = true;
                    dialog.close();
                }
            }

        } catch (Exception e) {
            showError(languageManager.getText("import.error.failed.title"),
                    languageManager.getText("import.error.failed.content") + ": " + e.getMessage());
        }
    }

    private List<Contact> parseCsvFile(File file) throws IOException {
        List<Contact> contacts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line = reader.readLine();
            if (line == null) {
                return contacts;
            }

            // Skip UTF-8 BOM if present
            if (line.startsWith("\ufeff")) {
                line = line.substring(1);
            }

            // Skip header line and comment lines
            while (line != null && (line.trim().isEmpty() || line.trim().startsWith("#"))) {
                line = reader.readLine();
            }

            // This should be the header line - skip it
            if (line != null) {
                line = reader.readLine();
            }

            int lineNumber = 2;
            while (line != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    line = reader.readLine();
                    lineNumber++;
                    continue;
                }

                try {
                    Contact contact = parseCsvLine(line, lineNumber);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                }

                line = reader.readLine();
                lineNumber++;
            }
        }

        return contacts;
    }

    private Contact parseCsvLine(String line, int lineNumber) {
        String[] fields = parseCsvFields(line);

        if (fields.length < 2) {
            throw new IllegalArgumentException(languageManager.getText("import.parse.insufficient.data"));
        }

        Contact contact = new Contact();

        // Required fields
        contact.setFirstName(getField(fields, 0, "").trim());
        contact.setLastName(getField(fields, 1, "").trim());

        if (contact.getFirstName().isEmpty() || contact.getLastName().isEmpty()) {
            throw new IllegalArgumentException(languageManager.getText("import.parse.required.fields"));
        }

        // Optional fields
        contact.setBirthday(parseDate(getField(fields, 2, "")));
        contact.setPin(getField(fields, 3, ""));
        contact.setEmail(getField(fields, 4, ""));
        contact.setPhoneNum(getField(fields, 5, ""));
        contact.setStreetName(getField(fields, 6, ""));
        contact.setStreetNum(getField(fields, 7, ""));
        contact.setPostalCode(getField(fields, 8, ""));
        contact.setCity(getField(fields, 9, ""));

        // Member status
        String memberStr = getField(fields, 10, "").toLowerCase();
        contact.setMember(memberStr.equals("yes") || memberStr.equals("true") || memberStr.equals("1") ||
                memberStr.equals(languageManager.getText("import.parse.member.yes").toLowerCase()));

        // Member dates
        contact.setMemberSince(parseDate(getField(fields, 11, "")));
        contact.setMemberUntil(parseDate(getField(fields, 12, "")));

        // Set creation timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        contact.setCreatedAt(timestamp);
        contact.setUpdatedAt(timestamp);

        return contact;
    }

    private String[] parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        // Add final field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    private String getField(String[] fields, int index, String defaultValue) {
        if (index < fields.length && fields[index] != null) {
            return fields[index].trim();
        }
        return defaultValue;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try DD.MM.YYYY format
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e1) {
            try {
                // Try DD/MM/YYYY format
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                try {
                    // Try YYYY-MM-DD format
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e3) {
                    System.err.println("Could not parse date: " + dateStr);
                    return null;
                }
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(languageManager.getText("import.alert.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return result;
    }

    public List<Contact> getImportedContacts() {
        return importedContacts;
    }
}