package smallbusinessbuddycrm.controllers;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreatePaymentTemplateDialog {
    private Stage dialog;
    private PaymentTemplate result;
    private boolean confirmed = false;

    // Form fields
    private TextField nameField;
    private TextField amountField;
    private ComboBox<String> modelCombo;
    private TextField referenceField;
    private CheckBox activeCheckBox;

    // Description fields
    private List<DescriptionField> descriptionFields;
    private VBox descriptionContainer;
    private TextArea descriptionPreview;

    // Contact and underaged attributes
    private static final String[] CONTACT_ATTRIBUTES = {
            "first_name", "last_name", "email", "phone_num", "birthday", "pin",
            "street_name", "street_num", "postal_code", "city", "member_since", "member_until"
    };

    private static final String[] UNDERAGED_ATTRIBUTES = {
            "first_name", "last_name", "birth_date", "age", "pin", "gender",
            "is_member", "member_since", "member_until", "note"
    };

    public CreatePaymentTemplateDialog(Stage parentStage) {
        descriptionFields = new ArrayList<>();
        createDialog(parentStage);
        addDescriptionField(); // Start with one field
    }

    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Create New Payment Template");
        dialog.setResizable(false);

        // Create main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));

        // Title
        Label titleLabel = new Label("💳 Create Payment Template");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #0099cc;");

        // Form
        GridPane formGrid = createFormGrid();

        // Buttons
        HBox buttonBox = createButtonSection();

        mainLayout.getChildren().addAll(titleLabel, formGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 700);
        dialog.setScene(scene);

        // Focus on name field
        nameField.requestFocus();
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
        Label nameLabel = new Label("Template Name:*");
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameField = new TextField();
        nameField.setPromptText("e.g., Membership Fee, Workshop Fee");
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);

        // Dynamic Description Section
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");

        VBox descriptionSection = createDescriptionSection();

        grid.add(descLabel, 0, row);
        grid.add(descriptionSection, 1, row++);

        // Amount (required)
        Label amountLabel = new Label("Amount (EUR):*");
        amountLabel.setStyle("-fx-font-weight: bold;");
        amountField = new TextField();
        amountField.setPromptText("0,00");
        setupCurrencyFormatting(amountField);
        grid.add(amountLabel, 0, row);
        grid.add(amountField, 1, row++);

        // Model of Payment
        Label modelLabel = new Label("Payment Model:");
        modelLabel.setStyle("-fx-font-weight: bold;");
        modelCombo = new ComboBox<>();
        modelCombo.getItems().addAll("", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "HR00", "HR01", "HR65");
        modelCombo.setValue("HR01"); // Default Croatian model
        modelCombo.setEditable(true);
        modelCombo.setPrefWidth(200);
        grid.add(modelLabel, 0, row);
        grid.add(modelCombo, 1, row++);

        // Reference Number Template
        Label refLabel = new Label("Reference Template:");
        refLabel.setStyle("-fx-font-weight: bold;");
        referenceField = new TextField();


        VBox refBox = new VBox(5);
        refBox.getChildren().add(referenceField);

        Label refHint = new Label("💡 Leave empty for auto-generation or use {contact_id} placeholder");
        refHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
        refBox.getChildren().add(refHint);

        grid.add(refLabel, 0, row);
        grid.add(refBox, 1, row++);

        // Active Status
        Label activeLabel = new Label("Status:");
        activeLabel.setStyle("-fx-font-weight: bold;");
        activeCheckBox = new CheckBox("Template is active");
        activeCheckBox.setSelected(true); // Default to active
        grid.add(activeLabel, 0, row);
        grid.add(activeCheckBox, 1, row++);

        // Add some visual separation
        Separator separator = new Separator();
        grid.add(separator, 0, row, 2, 1);
        row++;

        // Required fields note
        Label requiredNote = new Label("* Required fields");
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
        Button addButton = new Button("+ Add Field");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
        addButton.setOnAction(e -> addDescriptionField());

        Button removeButton = new Button("- Remove");
        removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
        removeButton.setOnAction(e -> removeDescriptionField());

        buttonBox.getChildren().addAll(addButton, removeButton);

        // Preview
        Label previewLabel = new Label("Preview:");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        descriptionPreview = new TextArea();
        descriptionPreview.setPrefRowCount(2);
        descriptionPreview.setEditable(false);
        descriptionPreview.setStyle("-fx-background-color: #f8f9fa; -fx-font-size: 10px;");

        section.getChildren().addAll(descriptionContainer, buttonBox, previewLabel, descriptionPreview);

        return section;
    }

    private void addDescriptionField() {
        if (descriptionFields.size() >= 4) {
            showAlert(Alert.AlertType.WARNING, "Maximum Fields", "Maximum 4 description fields allowed.");
            return;
        }

        DescriptionField field = new DescriptionField(descriptionFields.size() + 1);
        descriptionFields.add(field);
        descriptionContainer.getChildren().add(field);
        updatePreview();
    }

    private void removeDescriptionField() {
        if (descriptionFields.size() <= 1) {
            showAlert(Alert.AlertType.WARNING, "Minimum Fields", "At least one description field is required.");
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

    private String buildDescriptionTemplate() {
        return descriptionPreview.getText();
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

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 16;");
        cancelButton.setOnAction(e -> dialog.close());

        Button createButton = new Button("Create Template");
        createButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 8 16; -fx-font-weight: bold;");
        createButton.setOnAction(e -> handleCreate());

        buttonBox.getChildren().addAll(cancelButton, createButton);

        return buttonBox;
    }

    private void handleCreate() {
        try {
            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Check if name already exists
            PaymentTemplateDAO dao = new PaymentTemplateDAO();
            if (dao.nameExists(nameField.getText().trim(), 0)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Name",
                        "A payment template with this name already exists. Please choose a different name.");
                return;
            }

            // Create new template
            PaymentTemplate template = new PaymentTemplate();
            template.setName(nameField.getText().trim());
            template.setDescription(buildDescriptionTemplate());

            // Parse amount
            String amountText = getAmountForDatabase();
            if (!amountText.isEmpty()) {
                try {
                    BigDecimal amount = new BigDecimal(amountText);
                    template.setAmount(amount);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                            "Please enter a valid amount (e.g., 25,00)");
                    return;
                }
            }

            template.setModelOfPayment(modelCombo.getValue() != null ? modelCombo.getValue().trim() : "");
            template.setPozivNaBroj(referenceField.getText().trim());
            template.setActive(activeCheckBox.isSelected());

            // Save to database
            if (dao.save(template)) {
                result = template;
                confirmed = true;
                dialog.close();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Payment template '" + template.getName() + "' created successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Save Failed",
                        "Failed to save the payment template to the database.");
            }

        } catch (Exception e) {
            System.err.println("Error creating payment template: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred while creating the payment template: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("• Template name is required\n");
        }

        if (amountField.getText().trim().isEmpty()) {
            errors.append("• Amount is required\n");
        } else {
            try {
                BigDecimal amount = new BigDecimal(getAmountForDatabase());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("• Amount must be greater than 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Amount must be a valid number\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please fix the following errors:\n\n" + errors.toString());
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
        dialog.showAndWait();
        return confirmed;
    }

    public PaymentTemplate getResult() {
        return result;
    }

    // Inner class for description fields
    private class DescriptionField extends HBox {
        private ComboBox<String> typeCombo;
        private ComboBox<String> attributeCombo;
        private TextField customField;
        private StackPane inputContainer;

        public DescriptionField(int fieldNumber) {
            setSpacing(5);
            setStyle("-fx-padding: 5; -fx-border-color: #dee2e6; -fx-border-radius: 3; -fx-background-color: white;");

            Label fieldLabel = new Label("Field " + fieldNumber + ":");
            fieldLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            fieldLabel.setMinWidth(50);

            typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll("Contact Attribute", "Underaged Attribute", "Custom Text");
            typeCombo.setPromptText("Select type...");
            typeCombo.setPrefWidth(120);
            typeCombo.setOnAction(e -> handleTypeChange());

            inputContainer = new StackPane();
            inputContainer.setPrefWidth(150);

            attributeCombo = new ComboBox<>();
            attributeCombo.setPromptText("Select attribute...");
            attributeCombo.setPrefWidth(150);
            attributeCombo.setOnAction(e -> updatePreview());
            attributeCombo.setVisible(false);

            customField = new TextField();
            customField.setPromptText("Enter text...");
            customField.setPrefWidth(150);
            customField.textProperty().addListener((obs, old, text) -> updatePreview());
            customField.setVisible(false);

            inputContainer.getChildren().addAll(attributeCombo, customField);

            getChildren().addAll(fieldLabel, typeCombo, inputContainer);
        }

        private void handleTypeChange() {
            String type = typeCombo.getValue();

            if ("Contact Attribute".equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(CONTACT_ATTRIBUTES);
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if ("Underaged Attribute".equals(type)) {
                attributeCombo.getItems().clear();
                attributeCombo.getItems().addAll(UNDERAGED_ATTRIBUTES);
                attributeCombo.setVisible(true);
                customField.setVisible(false);
            } else if ("Custom Text".equals(type)) {
                attributeCombo.setVisible(false);
                customField.setVisible(true);
            }

            updatePreview();
        }

        public String getTemplateValue() {
            String type = typeCombo.getValue();

            if ("Contact Attribute".equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{contact_attributes." + attr + "}}" : "";
            } else if ("Underaged Attribute".equals(type)) {
                String attr = attributeCombo.getValue();
                return attr != null ? "{{underaged_attributes." + attr + "}}" : "";
            } else if ("Custom Text".equals(type)) {
                String text = customField.getText();
                return text != null && !text.trim().isEmpty() ? "{{custom_text." + text.trim() + "}}" : "";
            }

            return "";
        }
    }
}