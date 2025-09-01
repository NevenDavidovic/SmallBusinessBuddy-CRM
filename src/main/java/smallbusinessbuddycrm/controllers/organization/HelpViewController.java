package smallbusinessbuddycrm.controllers.organization;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;

public class HelpViewController implements Initializable {

    // Main header
    @FXML private Label mainTitleLabel;
    @FXML private Label subtitleLabel;

    // Welcome section
    @FXML private Label welcomeTitleLabel;
    @FXML private Label welcomeDescriptionLabel;

    // Manual download section
    @FXML private Label manualDownloadTitleLabel;
    @FXML private Label manualDownloadDescriptionLabel;
    @FXML private Button downloadManualHrButton;
    @FXML private Button downloadManualEnButton;
    @FXML private Label manualNoteLabel;

    // Payment templates section
    @FXML private Label paymentTemplatesTitleLabel;
    @FXML private Label paymentTemplatesDescriptionLabel;
    @FXML private Label childrenPaymentNoteLabel;

    // App purpose section
    @FXML private Label appPurposeTitleLabel;
    @FXML private Label appPurposeDescriptionLabel;
    @FXML private Label forBusinessesLabel;
    @FXML private Label forBusinessesDescriptionLabel;
    @FXML private Label forAssociationsLabel;
    @FXML private Label forAssociationsDescriptionLabel;

    // Key features section
    @FXML private Label keyFeaturesTitleLabel;
    @FXML private Label contactsFeatureLabel;
    @FXML private Label contactsFeatureDescriptionLabel;
    @FXML private Label paymentsFeatureLabel;
    @FXML private Label paymentsFeatureDescriptionLabel;
    @FXML private Label emailFeatureLabel;
    @FXML private Label emailFeatureDescriptionLabel;
    @FXML private Label workshopsFeatureLabel;
    @FXML private Label workshopsFeatureDescriptionLabel;

    // FAQ section
    @FXML private Label faqTitleLabel;
    @FXML private Label faqQuestion1Label;
    @FXML private Label faqAnswer1Label;
    @FXML private Label faqQuestion2Label;
    @FXML private Label faqAnswer2Label;
    @FXML private Label faqQuestion3Label;
    @FXML private Label faqAnswer3Label;

    // Support section
    @FXML private Label supportTitleLabel;
    @FXML private Label supportDescriptionLabel;
    @FXML private Label contactInfoTitleLabel;
    @FXML private Label emailSupportLabel;
    @FXML private Label versionLabel;

    // Quick tips
    @FXML private Label quickTipsLabel;
    @FXML private Label tip1Label;
    @FXML private Label tip2Label;
    @FXML private Label tip3Label;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register for language change notifications
        LanguageManager.getInstance().addLanguageChangeListener(this::updateTexts);

        // Set initial texts
        updateTexts();
    }

    @FXML
    private void downloadManualHr() {
        downloadManual("manual_hr.pdf", "SmallBusinessBuddy_Priručnik_HR.pdf");
    }

    @FXML
    private void downloadManualEn() {
        downloadManual("manual_en.pdf", "SmallBusinessBuddy_Manual_EN.pdf");
    }

    private void downloadManual(String resourcePath, String defaultFileName) {
        try {
            // Get the resource as stream
            InputStream inputStream = getClass().getResourceAsStream("/manuals/" + resourcePath);

            if (inputStream == null) {
                // Show error - manual not found
                System.err.println("Manual not found: " + resourcePath);
                showErrorAlert("help.manual.error.not.found", "Manual file not found: " + resourcePath);
                return;
            }

            // Open file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(LanguageManager.getInstance().getText("help.manual.save.title"));
            fileChooser.setInitialFileName(defaultFileName);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf")
            );

            // Get current stage
            File selectedFile = fileChooser.showSaveDialog(downloadManualHrButton.getScene().getWindow());

            if (selectedFile != null) {
                // Copy the resource to selected file
                Files.copy(inputStream, selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Show success message
                showInfoAlert("help.manual.success.title", "help.manual.success.message");

                // Optional: Open the file after download
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(selectedFile);
                    } catch (Exception e) {
                        System.out.println("Cannot open file automatically: " + e.getMessage());
                    }
                }

                System.out.println("Manual downloaded to: " + selectedFile.getAbsolutePath());
            }

            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("help.manual.error.download", "Error downloading manual: " + e.getMessage());
        }
    }

    private void showErrorAlert(String titleKey, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(LanguageManager.getInstance().getText(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String titleKey, String messageKey) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.getInstance().getText(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.getInstance().getText(messageKey));
        alert.showAndWait();
    }

    private void updateTexts() {
        LanguageManager languageManager = LanguageManager.getInstance();

        // Main header
        if (mainTitleLabel != null) {
            mainTitleLabel.setText(languageManager.getText("help.main.title"));
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(languageManager.getText("help.subtitle"));
        }

        // Welcome section
        if (welcomeTitleLabel != null) {
            welcomeTitleLabel.setText(languageManager.getText("help.welcome.title"));
        }
        if (welcomeDescriptionLabel != null) {
            welcomeDescriptionLabel.setText(languageManager.getText("help.welcome.description"));
        }

        // Manual download section
        if (manualDownloadTitleLabel != null) {
            manualDownloadTitleLabel.setText(languageManager.getText("help.manual.download.title"));
        }
        if (manualDownloadDescriptionLabel != null) {
            manualDownloadDescriptionLabel.setText(languageManager.getText("help.manual.download.description"));
        }
        if (downloadManualHrButton != null) {
            downloadManualHrButton.setText(languageManager.getText("help.manual.download.hr"));
        }
        if (downloadManualEnButton != null) {
            downloadManualEnButton.setText(languageManager.getText("help.manual.download.en"));
        }
        if (manualNoteLabel != null) {
            manualNoteLabel.setText(languageManager.getText("help.manual.note"));
        }

        // Payment templates section
        if (paymentTemplatesTitleLabel != null) {
            paymentTemplatesTitleLabel.setText(languageManager.getText("help.payment.templates.title"));
        }
        if (paymentTemplatesDescriptionLabel != null) {
            paymentTemplatesDescriptionLabel.setText(languageManager.getText("help.payment.templates.description"));
        }
        if (childrenPaymentNoteLabel != null) {
            childrenPaymentNoteLabel.setText(languageManager.getText("help.children.payment.note"));
        }

        // App purpose section
        if (appPurposeTitleLabel != null) {
            appPurposeTitleLabel.setText(languageManager.getText("help.app.purpose.title"));
        }
        if (appPurposeDescriptionLabel != null) {
            appPurposeDescriptionLabel.setText(languageManager.getText("help.app.purpose.description"));
        }
        if (forBusinessesLabel != null) {
            forBusinessesLabel.setText(languageManager.getText("help.for.businesses"));
        }
        if (forBusinessesDescriptionLabel != null) {
            forBusinessesDescriptionLabel.setText(languageManager.getText("help.for.businesses.description"));
        }
        if (forAssociationsLabel != null) {
            forAssociationsLabel.setText(languageManager.getText("help.for.associations"));
        }
        if (forAssociationsDescriptionLabel != null) {
            forAssociationsDescriptionLabel.setText(languageManager.getText("help.for.associations.description"));
        }

        // Key features section
        if (keyFeaturesTitleLabel != null) {
            keyFeaturesTitleLabel.setText(languageManager.getText("help.key.features.title"));
        }
        if (contactsFeatureLabel != null) {
            contactsFeatureLabel.setText(languageManager.getText("help.contacts.feature"));
        }
        if (contactsFeatureDescriptionLabel != null) {
            contactsFeatureDescriptionLabel.setText(languageManager.getText("help.contacts.feature.description"));
        }
        if (paymentsFeatureLabel != null) {
            paymentsFeatureLabel.setText(languageManager.getText("help.payments.feature"));
        }
        if (paymentsFeatureDescriptionLabel != null) {
            paymentsFeatureDescriptionLabel.setText(languageManager.getText("help.payments.feature.description"));
        }
        if (emailFeatureLabel != null) {
            emailFeatureLabel.setText(languageManager.getText("help.email.feature"));
        }
        if (emailFeatureDescriptionLabel != null) {
            emailFeatureDescriptionLabel.setText(languageManager.getText("help.email.feature.description"));
        }
        if (workshopsFeatureLabel != null) {
            workshopsFeatureLabel.setText(languageManager.getText("help.workshops.feature"));
        }
        if (workshopsFeatureDescriptionLabel != null) {
            workshopsFeatureDescriptionLabel.setText(languageManager.getText("help.workshops.feature.description"));
        }

        // FAQ section
        if (faqTitleLabel != null) {
            faqTitleLabel.setText(languageManager.getText("help.faq.title"));
        }
        if (faqQuestion1Label != null) {
            faqQuestion1Label.setText(languageManager.getText("help.faq.question1"));
        }
        if (faqAnswer1Label != null) {
            faqAnswer1Label.setText(languageManager.getText("help.faq.answer1"));
        }
        if (faqQuestion2Label != null) {
            faqQuestion2Label.setText(languageManager.getText("help.faq.question2"));
        }
        if (faqAnswer2Label != null) {
            faqAnswer2Label.setText(languageManager.getText("help.faq.answer2"));
        }
        if (faqQuestion3Label != null) {
            faqQuestion3Label.setText(languageManager.getText("help.faq.question3"));
        }
        if (faqAnswer3Label != null) {
            faqAnswer3Label.setText(languageManager.getText("help.faq.answer3"));
        }

        // Support section
        if (supportTitleLabel != null) {
            supportTitleLabel.setText(languageManager.getText("help.support.title"));
        }
        if (supportDescriptionLabel != null) {
            supportDescriptionLabel.setText(languageManager.getText("help.support.description"));
        }
        if (contactInfoTitleLabel != null) {
            contactInfoTitleLabel.setText(languageManager.getText("help.contact.info.title"));
        }
        if (emailSupportLabel != null) {
            emailSupportLabel.setText(languageManager.getText("help.email.support"));
        }
        if (versionLabel != null) {
            versionLabel.setText(languageManager.getText("help.version"));
        }

        // Quick tips
        if (quickTipsLabel != null) {
            quickTipsLabel.setText(languageManager.getText("help.quick.tips"));
        }
        if (tip1Label != null) {
            tip1Label.setText(languageManager.getText("help.tip1"));
        }
        if (tip2Label != null) {
            tip2Label.setText(languageManager.getText("help.tip2"));
        }
        if (tip3Label != null) {
            tip3Label.setText(languageManager.getText("help.tip3"));
        }

        System.out.println("Help view texts updated");
    }
}