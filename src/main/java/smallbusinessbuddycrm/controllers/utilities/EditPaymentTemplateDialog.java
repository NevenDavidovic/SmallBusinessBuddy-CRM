package smallbusinessbuddycrm.controllers.utilities;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.database.PaymentTemplateDAO;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EditPaymentTemplateDialog {
    private Stage dialog;
    private PaymentTemplate template;
    private boolean confirmed = false;

    // Form fields
    private TextField nameField;
    private TextField amountField;
    private ComboBox<String> modelCombo;
    private CheckBox activeCheckBox;

    // Description fields
    private List<DescriptionField> descriptionFields;
    private VBox descriptionContainer;
    private TextArea descriptionPreview;

    // Reference field
    private ReferenceField referenceField;
    private TextArea referencePreview;

    // Contact and underaged attributes for description
    private static final String[] CONTACT_ATTRIBUTES = {
            "first_name", "last_name", "email", "phone_num", "birthday", "pin",
            "street_name", "street_num", "postal_code", "city", "member_since", "member_until"
    };

    private static final String[] UNDERAGED_ATTRIBUTES = {
            "first_name", "last_name", "birth_date", "age", "pin", "gender",
            "is_member", "member_since", "member_until", "note"
    };

    // Reference-specific attributes (only PIN allowed)
    private static final String[] CONTACT_REFERENCE_ATTRIBUTES = {"pin"};
    private static final String[] UNDERAGED_REFERENCE_ATTRIBUTES = {"pin"};

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    // UI Labels for translation
    private Label titleLabel;
    private Label infoTitleLabel;
    private Label idLabel;
    private Label createdLabel;
    private Label updatedLabel;

    // Form labels
    private Label nameLabel;
    private Label descLabel;
    private Label amountLabel;
    private Label modelLabel;
    private Label refLabel;
    private Label activeLabel;
    private Label requiredNote;

    // Section labels
    private Label descPreviewLabel;
    private Label refPreviewLabel;
    private Label refHint;

    // Buttons
    private Button addButton;
    private Button removeButton;
    private Button cancelButton;
    private Button saveButton;

    public EditPaymentTemplateDialog(Stage parentStage, PaymentTemplate template) {
        this.template = template;
        descriptionFields = new ArrayList<>();
        createDialog(parentStage);
        loadTemplateData();
        updateTexts(); // Initial translation
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setResizable(false);

        // Create main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));

        // Title
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Template info
        VBox infoBox = createTemplateInfoSection();

        // Form
        GridPane formGrid = createFormGrid();

        // Buttons
        HBox buttonBox = createButtonSection();

        mainLayout.getChildren().addAll(titleLabel, infoBox, formGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 900);
        dialog.setScene(scene);

        // Focus on name field
        nameField.requestFocus();
    }

    private VBox createTemplateInfoSection() {
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        infoTitleLabel = new Label();
        infoTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        infoTitleLabel.setStyle("-fx-text-fill: #495057;");

        idLabel = new Label();
        createdLabel = new Label();
        updatedLabel = new Label();

        idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
        createdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
        updatedLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");

        infoBox.getChildren().addAll(infoTitleLabel, idLabel, createdLabel, updatedLabel);

        return infoBox;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.TOP_LEFT);

        // Column constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(120);
        labelCol.setPrefWidth(120);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        int row = 0;

        // Template Name (required)
        nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameField = new TextField();
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);

        // Dynamic Description Section
        descLabel = new Label();
        descLabel.setStyle("-fx-font-weight: bold;");

        VBox descriptionSection = createDescriptionSection();

        grid.add(descLabel, 0, row);
        grid.add(descriptionSection, 1, row++);

        // Amount (required)
        amountLabel = new Label();
        amountLabel.setStyle("-fx-font-weight: bold;");
        amountField = new TextField();
        setupCurrencyFormatting(amountField);
        grid.add(amountLabel, 0, row);
        grid.add(amountField, 1, row++);

        // Model of Payment
        modelLabel = new Label();
        modelLabel.setStyle("-fx-font-weight: bold;");
        modelCombo = new ComboBox<>();
        modelCombo.getItems().addAll("", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "HR00", "HR01", "HR65");
        modelCombo.setEditable(true);
        modelCombo.setPrefWidth(200);
        grid.add(modelLabel, 0, row);
        grid.add(modelCombo, 1, row++);

        // Reference Number Template - ENHANCED VERSION
        refLabel = new Label();
        refLabel.setStyle("-fx-font-weight: bold;");

        VBox referenceSection = createReferenceSection();

        grid.add(refLabel, 0, row);
        grid.add(referenceSection, 1, row++);

        // Active Status
        activeLabel = new Label();
        activeLabel.setStyle("-fx-font-weight: bold;");
        activeCheckBox = new CheckBox();
        grid.add(activeLabel, 0, row);
        grid.add(activeCheckBox, 1, row++);

        // Add some visual separation
        Separator separator = new Separator();
        grid.add(separator, 0, row, 2, 1);
        row++;

        // Required fields note
        requiredNote = new Label();
        requiredNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #dc3545; -fx-font-style: italic;");
        grid.add(requiredNote, 0, row, 2, 1);

        return grid;
    }

    private VBox createDescriptionSection() {
        VBox section = new VBox(8);

        // Fields container
        descriptionContainer = new VBox(5);

        // Add/Remove buttons
        HBox buttonBox = new HBox(5);
        addButton = new Button();
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
        addButton.setOnAction(e -> addDescriptionField());

        removeButton = new Button();
        removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
        removeButton.setOnAction(e -> removeDescriptionField());

        buttonBox.getChildren().addAll(addButton, removeButton);

        // Preview
        descPreviewLabel = new Label();
        descPreviewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        descriptionPreview = new TextArea();
        descriptionPreview.setPrefRowCount(2);
        descriptionPreview.setEditable(false);
        descriptionPreview.setStyle("-fx-background-color: #f8f9fa; -fx-font-size: 10px;");

        section.getChildren().addAll(descriptionContainer, buttonBox, descPreviewLabel, descriptionPreview);

        return section;
    }

    private VBox createReferenceSection() {
        VBox section = new VBox(8);

        // Single reference field
        referenceField = new ReferenceField();

        // Preview
        refPreviewLabel = new Label();
        refPreviewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        referencePreview = new TextArea();
        referencePreview.setPrefRowCount(1);
        referencePreview.setEditable(false);
        referencePreview.setStyle("-fx-background-color: #f8f9fa; -fx-font-size: 10px;");

        // Hint
        refHint = new Label();
        refHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");

        section.getChildren().addAll(referenceField, refPreviewLabel, referencePreview, refHint);

        return section;
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        // Update dialog title
        if (dialog != null) {
            dialog.setTitle(lm.getText("payment.template.dialog.edit.title") + " - " + template.getName());
        }

        // Update main labels
        if (titleLabel != null) titleLabel.setText("✏️ " + lm.getText("payment.template.dialog.edit.title"));
        if (infoTitleLabel != null) infoTitleLabel.setText("📋 " + lm.getText("payment.template.info.title"));

        // Update info labels with current data
        if (idLabel != null) idLabel.setText(lm.getText("payment.template.info.id") + ": " + template.getId());
        if (createdLabel != null) createdLabel.setText(lm.getText("payment.template.info.created") + ": " + (template.getCreatedAt() != null ? template.getCreatedAt() : lm.getText("common.not.available")));
        if (updatedLabel != null) updatedLabel.setText(lm.getText("payment.template.info.updated") + ": " + (template.getUpdatedAt() != null ? template.getUpdatedAt() : lm.getText("common.not.available")));

        // Update form labels
        if (nameLabel != null) nameLabel.setText(lm.getText("payment.template.form.name.required"));
        if (descLabel != null) descLabel.setText(lm.getText("payment.template.form.description"));
        if (amountLabel != null) amountLabel.setText(lm.getText("payment.template.form.amount.required"));
        if (modelLabel != null) modelLabel.setText(lm.getText("payment.template.form.model"));
        if (refLabel != null) refLabel.setText(lm.getText("payment.template.form.reference"));
        if (activeLabel != null) activeLabel.setText(lm.getText("payment.template.form.status"));
        if (requiredNote != null) requiredNote.setText(lm.getText("payment.template.form.required.fields"));

        // Update form fields
        if (nameField != null) nameField.setPromptText(lm.getText("payment.template.form.name.placeholder"));
        if (amountField != null) amountField.setPromptText(lm.getText("payment.template.form.amount.placeholder"));
        if (activeCheckBox != null) activeCheckBox.setText(lm.getText("payment.template.form.active"));

        // Update section labels
        if (descPreviewLabel != null) descPreviewLabel.setText(lm.getText("payment.template.preview.label"));
        if (refPreviewLabel != null) refPreviewLabel.setText(lm.getText("payment.template.preview.label"));
        if (refHint != null) refHint.setText("💡 " + lm.getText("payment.template.reference.hint"));

        // Update buttons
        if (addButton != null) addButton.setText(lm.getText("payment.template.description.add.button"));
        if (removeButton != null) removeButton.setText(lm.getText("payment.template.description.remove.button"));
        if (cancelButton != null) cancelButton.setText(lm.getText("button.cancel"));
        if (saveButton != null) saveButton.setText(lm.getText("payment.template.save.button"));

        // Update description fields
        for (DescriptionField field : descriptionFields) {
            field.updateTexts();
        }

        // Update reference field
        if (referenceField != null) {
            referenceField.updateTexts();
        }
    }

    private void addDescriptionField() {
        LanguageManager lm = LanguageManager.getInstance();
        if (descriptionFields.size() >= 4) {
            showAlert(Alert.AlertType.WARNING,
                    lm.getText("payment.template.description.max.title"),
                    lm.getText("payment.template.description.max.message"));
            return;
        }

        DescriptionField field = new DescriptionField(descriptionFields.size() + 1);
        descriptionFields.add(field);
        descriptionContainer.getChildren().add(field);
        updatePreview();
    }

    private void removeDescriptionField() {
        LanguageManager lm = LanguageManager.getInstance();
        if (descriptionFields.size() <= 1) {
            showAlert(Alert.AlertType.WARNING,
                    lm.getText("payment.template.description.min.title"),
                    lm.getText("payment.template.description.min.message"));
            return;
        }

        DescriptionField lastField = descriptionFields.get(descriptionFields.size() - 1);
        descriptionFields.remove(lastField);
        descriptionContainer.getChildren().remove(lastField);
        updatePreview();
    }

    private void updatePreview() {
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < descriptionFields.size(); i++) {
            DescriptionField field = descriptionFields.get(i);
            String value = field.getTemplateValue();
            if (!value.isEmpty()) {
                if (preview.length() > 0) preview.append(" ");
                preview.append(value);
            }
        }
        descriptionPreview.setText(preview.toString());
    }

    private void updateReferencePreview() {
        String value = referenceField.getTemplateValue();
        referencePreview.setText(value);
    }

    private String buildDescriptionTemplate() {
        return descriptionPreview.getText();
    }

    private String buildReferenceTemplate() {
        return referencePreview.getText();
    }

    private void parseDescriptionTemplate(String template) {
        // Clear existing fields
        descriptionFields.clear();
        descriptionContainer.getChildren().clear();

        if (template == null || template.trim().isEmpty()) {
            addDescriptionField(); // Add one empty field
            return;
        }

        // Split by spaces but keep placeholders intact
        String[] parts = template.split(" ");

        for (String part : parts) {
            if (part.trim().isEmpty()) continue;

            DescriptionField field = new DescriptionField(descriptionFields.size() + 1);

            if (part.startsWith("{{") && part.endsWith("}}")) {
                // It's a placeholder
                String placeholder = part.substring(2, part.length() - 2);
                if (placeholder.startsWith("contact_attributes.")) {
                    String attribute = placeholder.substring("contact_attributes.".length());
                    field.setToContactAttribute(attribute);
                } else if (placeholder.startsWith("underaged_attributes.")) {
                    String attribute = placeholder.substring("underaged_attributes.".length());
                    field.setToUnderagedAttribute(attribute);
                } else if (placeholder.startsWith("custom_text.")) {
                    String text = placeholder.substring("custom_text.".length());
                    field.setToCustomText(text);
                }
            } else {
                // It's plain text
                field.setToCustomText(part);
            }

            descriptionFields.add(field);
            descriptionContainer.getChildren().add(field);
        }

        // Ensure at least one field
        if (descriptionFields.isEmpty()) {
            addDescriptionField();
        }

        updatePreview();
    }

    private void parseReferenceTemplate(String template) {
        if (template == null || template.trim().isEmpty()) {
            referenceField.setToEmpty();
            return;
        }

        if (template.startsWith("{{") && template.endsWith("}}")) {
            // It's a placeholder
            String placeholder = template.substring(2, template.length() - 2);
            if (placeholder.equals("contact_attributes.pin")) {
                referenceField.setToContactAttribute("pin");
            } else if (placeholder.equals("underaged_attributes.pin")) {
                referenceField.setToUnderagedAttribute("pin");
            }
        } else {
            // It's custom text - check if it's all numbers
            if (template.matches("\\d*")) {
                referenceField.setToCustomText(template);
            } else {
                // If existing template contains non-numbers, set to empty and show warning
                referenceField.setToEmpty();
                System.out.println("Warning: Existing reference template contains non-numeric characters, setting to empty");
            }
        }

        updateReferencePreview();
    }

    private void loadTemplateData() {
        // Load existing template data into form fields
        nameField.setText(template.getName() != null ? template.getName() : "");

        // Parse and load description template
        parseDescriptionTemplate(template.getDescription());

        // Parse and load reference template
        parseReferenceTemplate(template.getPozivNaBroj());

        // Format amount for display (convert 12.34 to 12,34)
        if (template.getAmount() != null) {
            String amountStr = template.getAmount().toString().replace(".", ",");
            amountField.setText(amountStr);
        }

        modelCombo.setValue(template.getModelOfPayment() != null ? template.getModelOfPayment() : "");
        activeCheckBox.setSelected(template.isActive());
    }

    private void setupCurrencyFormatting(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            // Remove all non-digit characters
            String digitsOnly = newValue.replaceAll("[^0-9]", "");

            if (digitsOnly.isEmpty()) {
                field.setText("");
                return;
            }

            // Limit to reasonable amount (max 999999.99)
            if (digitsOnly.length() > 8) {
                digitsOnly = digitsOnly.substring(0, 8);
            }

            // Format as currency with automatic decimal placement
            String formatted = formatCurrency(digitsOnly);

            // Avoid infinite loop by checking if text actually changed
            if (!formatted.equals(newValue)) {
                field.setText(formatted);
                field.positionCaret(formatted.length());
            }
        });
    }

    private String formatCurrency(String digitsOnly) {
        if (digitsOnly == null || digitsOnly.isEmpty()) {
            return "";
        }

        // Pad with leading zeros if needed (minimum 3 digits for 0,01)
        while (digitsOnly.length() < 3) {
            digitsOnly = "0" + digitsOnly;
        }

        // Split into euros and cents
        int length = digitsOnly.length();
        String cents = digitsOnly.substring(length - 2);
        String euros = digitsOnly.substring(0, length - 2);

        // Remove leading zeros from euros part, but keep at least one digit
        euros = euros.replaceFirst("^0+", "");
        if (euros.isEmpty()) {
            euros = "0";
        }

        // Return formatted amount (European style: 12,34)
        return euros + "," + cents;
    }

    private String getAmountForDatabase() {
        String displayText = amountField.getText();
        if (displayText == null || displayText.isEmpty()) {
            return "";
        }

        // Convert from display format (12,34) to database format (12.34)
        return displayText.replace(",", ".");
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        cancelButton = new Button();
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 16;");
        cancelButton.setOnAction(e -> dialog.close());

        saveButton = new Button();
        saveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 8 16; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        return buttonBox;
    }

    private void handleSave() {
        try {
            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Check if name already exists (excluding current template)
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            if (dao.nameExists(nameField.getText().trim(), template.getId())) {
                LanguageManager lm = LanguageManager.getInstance();
                showAlert(Alert.AlertType.WARNING,
                        lm.getText("payment.template.validation.duplicate.title"),
                        lm.getText("payment.template.validation.duplicate.message"));
                return;
            }

            // Update template with form data
            template.setName(nameField.getText().trim());
            template.setDescription(buildDescriptionTemplate());

            // Parse amount
            String amountText = getAmountForDatabase();
            if (!amountText.isEmpty()) {
                try {
                    BigDecimal amount = new BigDecimal(amountText);
                    template.setAmount(amount);
                } catch (NumberFormatException e) {
                    LanguageManager lm = LanguageManager.getInstance();
                    showAlert(Alert.AlertType.ERROR,
                            lm.getText("payment.template.validation.amount.invalid.title"),
                            lm.getText("payment.template.validation.amount.invalid.message"));
                    return;
                }
            }

            template.setModelOfPayment(modelCombo.getValue() != null ? modelCombo.getValue().trim() : "");

            // Use the new reference system instead of the old referenceField
            template.setPozivNaBroj(buildReferenceTemplate());

            template.setActive(activeCheckBox.isSelected());

            // Save to database
            if (dao.save(template)) {
                confirmed = true;
                dialog.close();
            } else {
                LanguageManager lm = LanguageManager.getInstance();
                showAlert(Alert.AlertType.ERROR,
                        lm.getText("payment.template.save.failed.title"),
                        lm.getText("payment.template.save.failed.message"));
            }

        } catch (Exception e) {
            System.err.println("Error updating payment template: " + e.getMessage());
            e.printStackTrace();
            LanguageManager lm = LanguageManager.getInstance();
            showAlert(Alert.AlertType.ERROR,
                    lm.getText("common.error.title"),
                    lm.getText("payment.template.save.error.message") + ": " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        LanguageManager lm = LanguageManager.getInstance();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("• ").append(lm.getText("payment.template.validation.name.required")).append("\n");
        }

        if (amountField.getText().trim().isEmpty()) {
            errors.append("• ").append(lm.getText("payment.template.validation.amount.required")).append("\n");
        } else {
            try {
                BigDecimal amount = new BigDecimal(getAmountForDatabase());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("• ").append(lm.getText("payment.template.validation.amount.positive")).append("\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• ").append(lm.getText("payment.template.validation.amount.valid")).append("\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING,
                    lm.getText("common.validation.error.title"),
                    lm.getText("common.validation.error.message") + "\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialog);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        updateTexts(); // Update translations before showing
        dialog.showAndWait();
        return confirmed;
    }

    // Inner class for description fields
    private class DescriptionField extends HBox {
        private ComboBox<String> typeCombo;
        private ComboBox<String> attributeCombo;
        private TextField customField;
        private StackPane inputContainer;
        private Label fieldLabel;

        public DescriptionField(int fieldNumber) {
            setSpacing(5);
            setStyle("-fx-padding: 5; -fx-border-color: #dee2e6; -fx-border-radius: 3; -fx-background-color: white;");

            fieldLabel = new Label();
            fieldLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            fieldLabel.setMinWidth(50);

            typeCombo = new ComboBox<>();
            typeCombo.setPrefWidth(120);
            typeCombo.setOnAction(e -> handleTypeChange());

            inputContainer = new StackPane();
            inputContainer.setPrefWidth(150);

            attributeCombo = new ComboBox<>();
            attributeCombo.setPrefWidth(150);
            attributeCombo.setOnAction(e -> updatePreview());
            attributeCombo.setVisible(false);

            customField = new TextField();
            customField.setPrefWidth(150);
            customField.textProperty().addListener((obs, old, text) -> updatePreview());
            customField.setVisible(false);

            inputContainer.getChildren().addAll(attributeCombo, customField);

            getChildren().addAll(fieldLabel, typeCombo, inputContainer);
        }

        public void updateTexts() {
            LanguageManager lm = LanguageManager.getInstance();

            // Update field label with current field number
            int fieldNumber = descriptionFields.indexOf(this) + 1;
            fieldLabel.setText(lm.getText("payment.template.description.field") + " " + fieldNumber + ":");

            // Store current selections
            String selectedType = typeCombo.getValue();
            String selectedAttribute = attributeCombo.getValue();

            // Update type combo options
            typeCombo.getItems().clear();
            typeCombo.getItems().addAll(
                    lm.getText("payment.template.description.type.contact"),
                    lm.getText("payment.template.description.type.underaged"),
                    lm.getText("payment.template.description.type.custom")
            );

            // Set prompt text
            typeCombo.setPromptText(lm.getText("payment.template.description.type.select"));
            customField.setPromptText(lm.getText("payment.template.description.custom.placeholder"));
            attributeCombo.setPromptText(lm.getText("payment.template.description.attribute.select"));

            // Restore selections if they existed
            if (selectedType != null) {
                if (selectedType.equals("Contact Attribute")) {
                    typeCombo.setValue(lm.getText("payment.template.description.type.contact"));
                } else if (selectedType.equals("Underaged Attribute")) {
                    typeCombo.setValue(lm.getText("payment.template.description.type.underaged"));
                } else if (selectedType.equals("Custom Text")) {
                    typeCombo.setValue(lm.getText("payment.template.description.type.custom"));
                }
                handleTypeChange();

                if (selectedAttribute != null) {
                    attributeCombo.setValue(selectedAttribute);
                }
            }
        }

        private void handleTypeChange() {
            LanguageManager lm = LanguageManager.getInstance();
            String type = typeCombo.getValue();

            if (lm.getText("payment.template.description.type.contact").equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(CONTACT_ATTRIBUTES);
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if (lm.getText("payment.template.description.type.underaged").equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(UNDERAGED_ATTRIBUTES);
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if (lm.getText("payment.template.description.type.custom").equals(type)) {
                attributeCombo.setVisible(false);
                customField.setVisible(true);
            }

            updatePreview();
        }

        public String getTemplateValue() {
            LanguageManager lm = LanguageManager.getInstance();
            String type = typeCombo.getValue();

            if (lm.getText("payment.template.description.type.contact").equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{contact_attributes." + attr + "}}" : "";
            } else if (lm.getText("payment.template.description.type.underaged").equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{underaged_attributes." + attr + "}}" : "";
            } else if (lm.getText("payment.template.description.type.custom").equals(type)) {
                String text = customField.getText();
                return text != null && !text.trim().isEmpty() ? "{{custom_text." + text.trim() + "}}" : "";
            }

            return "";
        }

        public void setToContactAttribute(String attribute) {
            LanguageManager lm = LanguageManager.getInstance();
            typeCombo.setValue(lm.getText("payment.template.description.type.contact"));
            handleTypeChange();
            attributeCombo.setValue(attribute);
        }

        public void setToUnderagedAttribute(String attribute) {
            LanguageManager lm = LanguageManager.getInstance();
            typeCombo.setValue(lm.getText("payment.template.description.type.underaged"));
            handleTypeChange();
            attributeCombo.setValue(attribute);
        }

        public void setToCustomText(String text) {
            LanguageManager lm = LanguageManager.getInstance();
            typeCombo.setValue(lm.getText("payment.template.description.type.custom"));
            handleTypeChange();
            customField.setText(text);
        }
    }

    // Inner class for reference field
    private class ReferenceField extends HBox {
        private ComboBox<String> typeCombo;
        private ComboBox<String> attributeCombo;
        private TextField customField;
        private StackPane inputContainer;
        private Label fieldLabel;

        public ReferenceField() {
            setSpacing(5);
            setStyle("-fx-padding: 5; -fx-border-color: #dee2e6; -fx-border-radius: 3; -fx-background-color: white;");

            fieldLabel = new Label();
            fieldLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            fieldLabel.setMinWidth(70);

            typeCombo = new ComboBox<>();
            typeCombo.setPrefWidth(120);
            typeCombo.setOnAction(e -> handleTypeChange());

            inputContainer = new StackPane();
            inputContainer.setPrefWidth(180);

            attributeCombo = new ComboBox<>();
            attributeCombo.setPrefWidth(180);
            attributeCombo.setOnAction(e -> updateReferencePreview());
            attributeCombo.setVisible(false);

            customField = new TextField();
            customField.setPrefWidth(180);
            customField.textProperty().addListener((obs, old, text) -> updateReferencePreview());

            // Restrict input to numbers only
            customField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.matches("\\d*")) {
                    customField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            customField.setVisible(false);

            inputContainer.getChildren().addAll(attributeCombo, customField);

            getChildren().addAll(fieldLabel, typeCombo, inputContainer);
        }

        public void updateTexts() {
            LanguageManager lm = LanguageManager.getInstance();

            fieldLabel.setText(lm.getText("payment.template.reference.field") + ":");

            // Store current selections
            String selectedType = typeCombo.getValue();
            String selectedAttribute = attributeCombo.getValue();

            // Update type combo options
            typeCombo.getItems().clear();
            typeCombo.getItems().addAll(
                    lm.getText("payment.template.reference.type.contact"),
                    lm.getText("payment.template.reference.type.underaged"),
                    lm.getText("payment.template.reference.type.custom"),
                    lm.getText("payment.template.reference.type.empty")
            );

            // Set prompt texts
            typeCombo.setPromptText(lm.getText("payment.template.reference.type.select"));
            customField.setPromptText(lm.getText("payment.template.reference.custom.placeholder"));
            attributeCombo.setPromptText(lm.getText("payment.template.reference.attribute.select"));

            // Restore selections if they existed
            if (selectedType != null) {
                if (selectedType.equals("Contact Attribute")) {
                    typeCombo.setValue(lm.getText("payment.template.reference.type.contact"));
                } else if (selectedType.equals("Underaged Attribute")) {
                    typeCombo.setValue(lm.getText("payment.template.reference.type.underaged"));
                } else if (selectedType.equals("Custom Text")) {
                    typeCombo.setValue(lm.getText("payment.template.reference.type.custom"));
                } else if (selectedType.equals("Empty")) {
                    typeCombo.setValue(lm.getText("payment.template.reference.type.empty"));
                }
                handleTypeChange();

                if (selectedAttribute != null) {
                    attributeCombo.setValue(selectedAttribute);
                }
            }
        }

        private void handleTypeChange() {
            LanguageManager lm = LanguageManager.getInstance();
            String type = typeCombo.getValue();

            if (lm.getText("payment.template.reference.type.contact").equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(CONTACT_REFERENCE_ATTRIBUTES); // Only PIN
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if (lm.getText("payment.template.reference.type.underaged").equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(UNDERAGED_REFERENCE_ATTRIBUTES); // Only PIN
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if (lm.getText("payment.template.reference.type.custom").equals(type)) {
                attributeCombo.setVisible(false);
                customField.setVisible(true);
            } else if (lm.getText("payment.template.reference.type.empty").equals(type)) {
                attributeCombo.setVisible(false);
                customField.setVisible(false);
            }

            updateReferencePreview();
        }

        public String getTemplateValue() {
            LanguageManager lm = LanguageManager.getInstance();
            String type = typeCombo.getValue();

            if (lm.getText("payment.template.reference.type.contact").equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{contact_attributes." + attr + "}}" : "";
            } else if (lm.getText("payment.template.reference.type.underaged").equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{underaged_attributes." + attr + "}}" : "";
            } else if (lm.getText("payment.template.reference.type.custom").equals(type)) {
                String text = customField.getText();
                return text != null && !text.trim().isEmpty() ? text.trim() : "";
            } else if (lm.getText("payment.template.reference.type.empty").equals(type)) {
                return "";
            }

            return "";
        }

        public void setToContactAttribute(String attribute) {
            if ("pin".equals(attribute)) { // Only allow PIN
                LanguageManager lm = LanguageManager.getInstance();
                typeCombo.setValue(lm.getText("payment.template.reference.type.contact"));
                handleTypeChange();
                attributeCombo.setValue(attribute);
            }
        }

        public void setToUnderagedAttribute(String attribute) {
            if ("pin".equals(attribute)) { // Only allow PIN
                LanguageManager lm = LanguageManager.getInstance();
                typeCombo.setValue(lm.getText("payment.template.reference.type.underaged"));
                handleTypeChange();
                attributeCombo.setValue(attribute);
            }
        }

        public void setToCustomText(String text) {
            // Validate that text contains only numbers
            if (text != null && text.matches("\\d*")) {
                LanguageManager lm = LanguageManager.getInstance();
                typeCombo.setValue(lm.getText("payment.template.reference.type.custom"));
                handleTypeChange();
                customField.setText(text);
            }
        }

        public void setToEmpty() {
            LanguageManager lm = LanguageManager.getInstance();
            typeCombo.setValue(lm.getText("payment.template.reference.type.empty"));
            handleTypeChange();
        }
    }
}