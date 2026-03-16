package smallbusinessbuddycrm.controllers.contact;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.util.List;
import java.util.Optional;

public class PaymentTemplateSelectionDialog {

    private final Stage owner;
    private final List<PaymentTemplate> templates;
    private final LanguageManager languageManager;

    public PaymentTemplateSelectionDialog(Stage owner, List<PaymentTemplate> templates) {
        this.owner = owner;
        this.templates = templates;
        this.languageManager = LanguageManager.getInstance();
    }

    public Optional<PaymentTemplate> showAndWait() {
        ChoiceDialog<PaymentTemplate> dialog = new ChoiceDialog<>(templates.get(0), templates);
        dialog.setTitle(languageManager.getText("payment.template.dialog.title"));
        dialog.setHeaderText(languageManager.getText("payment.template.dialog.header"));
        dialog.setContentText(languageManager.getText("payment.template.dialog.content"));
        dialog.getDialogPane().setPrefWidth(500);
        dialog.initOwner(owner);

        VBox infoPane = createTemplateInfoPane(templates.get(0));
        infoPane.setStyle(
                "-fx-background-color: #1e2a3a;" +
                        "-fx-border-color: #2e3d50;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        dialog.getDialogPane().setExpandableContent(infoPane);

        dialog.getDialogPane().getStylesheets().add(
                "data:text/css," +
                        ".dialog-pane > .button-bar > .container > .details-button {" +
                        "    -fx-text-fill: white !important;" +
                        "}" +
                        ".dialog-pane .details-button {" +
                        "    -fx-text-fill: white !important;" +
                        "}" +
                        ".dialog-pane .details-button .label {" +
                        "    -fx-text-fill: white !important;" +
                        "}" +
                        ".dialog-pane .details-button:hover {" +
                        "    -fx-text-fill: white !important;" +
                        "}"
        );

        Platform.runLater(() -> {
            dialog.getDialogPane().lookupAll(".label").forEach(node -> {
                if (node instanceof Label label &&
                        label.getText().equals(languageManager.getText("payment.template.dialog.content"))) {
                    label.setStyle("-fx-text-fill: white;");
                }
            });

            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .setStyle("-fx-background-color: #ff7a59; -fx-text-fill: white;");

            dialog.getDialogPane().lookupAll("*").forEach(node -> {
                if (node instanceof Label label && label.getText().contains("Details")) {
                    label.setStyle("-fx-text-fill: white;");
                    label.textProperty().addListener((obs, oldText, newText) -> {
                        if (newText.contains("Details")) {
                            Platform.runLater(() -> label.setStyle("-fx-text-fill: white;"));
                        }
                    });
                }
                if (node instanceof Button button && button.getText() != null
                        && button.getText().contains("Details")) {
                    button.setStyle("-fx-text-fill: white;");
                }
            });
        });

        return dialog.showAndWait();
    }

    private VBox createTemplateInfoPane(PaymentTemplate template) {
        VBox infoPane = new VBox(10);
        infoPane.setPadding(new Insets(15, 15, 15, 15));

        if (template != null) {
            Label nameLabel = new Label(
                    languageManager.getText("payment.template.info.name") + " " + template.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");

            Label amountLabel = new Label(
                    languageManager.getText("payment.template.info.amount") + " " + template.getAmount() + " EUR");
            amountLabel.setStyle("-fx-text-fill: white;");

            Label modelLabel = new Label(
                    languageManager.getText("payment.template.info.model") + " " +
                            (template.getModelOfPayment() != null ? template.getModelOfPayment() : "N/A"));
            modelLabel.setStyle("-fx-text-fill: white;");

            Label descLabel = new Label(
                    languageManager.getText("payment.template.info.description") + " " +
                            (template.getDescription() != null && !template.getDescription().trim().isEmpty()
                                    ? template.getDescription() : "N/A"));
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(450);
            descLabel.setStyle("-fx-text-fill: white;");

            infoPane.getChildren().addAll(nameLabel, amountLabel, modelLabel, descLabel);
        } else {
            Label noInfoLabel = new Label(languageManager.getText("payment.template.info.empty"));
            noInfoLabel.setStyle("-fx-text-fill: white;");
            infoPane.getChildren().add(noInfoLabel);
        }

        return infoPane;
    }
}