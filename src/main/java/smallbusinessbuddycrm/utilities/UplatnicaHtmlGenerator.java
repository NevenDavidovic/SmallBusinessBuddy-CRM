package smallbusinessbuddycrm.utilities;

import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.PaymentTemplate;
import smallbusinessbuddycrm.model.Organization;
import smallbusinessbuddycrm.model.UnderagedMember;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class UplatnicaHtmlGenerator {

    private static final String HTML_TEMPLATE = """
            <!DOCTYPE html>
                    <html lang="hr">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Croatian Uplatnica</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; margin: 0; padding: 0 20px; background-color: #f5f5f5;">
                        <div style="border: 2px solid #ffffff; width: 931px; height: 380px; background-size: cover; background-image: url('https://raw.githubusercontent.com/NevenDavidovic/generator_uplatnica_dnd/refs/heads/main/jojo_crm/src/assets/images/uplatnica.png'); margin: 0 auto; position: relative;">
                            <!-- Payer Information -->
                            <div style="position: absolute; display: flex; flex-direction: column; max-width: 200px; left: 31px; top: 44px;">
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{PAYER_NAME}}</div>
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{PAYER_ADDRESS}}</div>
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{PAYER_CITY}}</div>
                            </div>
            
                            <!-- Currency -->
                            <div style="width: 50px; height: 27px; top: 32px; position: absolute; left: 345px; border: 0 solid; background: transparent; font-size: 12px; color: #000;">EUR</div>
            
                            <!-- Amount -->
                            <div style="left: 595px; position: absolute; top: 28px; background: transparent; font-size: 12px; letter-spacing: 0px; border: 0 solid; max-width: 200px; text-align: end; letter-spacing: 3px; height: 27px; padding-top: 5px; color: #000;">{{AMOUNT}}</div>
            
                            <!-- IBAN -->
                            <div style="position: absolute; top: 121px; left: 335px; border: 0 solid transparent; background-color: transparent; font-size: 12px; letter-spacing: 8px; max-width: 312px; width: 100%; height: 27px; color: #000;">{{RECIPIENT_IBAN}}</div>
            
                            <!-- Recipient Information -->
                            <div style="position: absolute; display: flex; flex-direction: column; max-width: 200px; left: 31px; top: 170px;">
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{RECIPIENT_NAME}}</div>
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{RECIPIENT_ADDRESS}}</div>
                                <div style="height: 20px; border: 0px solid #a26b6b; font-size: 12px; background: transparent; color: #000; margin-bottom: 2px;">{{RECIPIENT_CITY}}</div>
                            </div>
            
                            <!-- Payment Model -->
                            <div style="position: absolute; top: 160px; background: transparent; left: 240px; letter-spacing: 7px; width: 57px; height: 27px; border: 0 solid; font-size: 12px; color: #000;">{{MODEL}}</div>
            
                            <!-- Reference Number -->
                            <div style="height: 27px; width: 200px; position: absolute; top: 154px; left: 330px; border: 0 solid; background: transparent; font-size: 14px; color: #000; padding-top: 5px;">{{REFERENCE}}</div>
            
                            <!-- Purpose Code -->
                            <div style="height: 27px; position: absolute; top: 199px; left: 250px; border: 0 solid; background: transparent; font-size: 12px; color: #000;">{{PURPOSE}}</div>
            
                            <!-- Description -->
                            <div style="width: 249px; position: absolute; background: transparent; top: 180px; left: 384px; height: 50px; border: 0 solid; font-size: 12px; color: #000;">{{DESCRIPTION}}</div>
            
                            <!-- Barcode -->
                            <div style="position: absolute; width: 250px; left: 30px; bottom: 33px;">
                                <img src="data:image/png;base64,{{BARCODE_BASE64}}" alt="Generated Barcode" style="width: 100%;" />
                            </div>
            
                            <!-- Right side duplicated fields -->
                            <div style="height: 27px; padding-top: 5px; position: absolute; right: 19px; top: 26px; background: transparent; border: 0 solid; font-size: 12px; color: #000; text-align: right; letter-spacing: 3px;">{{AMOUNT}}</div>
                            <div style="position: absolute; right: 194px; top: 160px; background: transparent; border: 0; font-size: 12px; color: #000;">{{MODEL}}</div>
                            <div style="position: absolute; right: 30px; top: 120px; border: transparent; background: transparent; padding-top: 3px; font-size: 12px; height: 33px; color: #000;">{{RECIPIENT_IBAN}}</div>
                            <div style="position: absolute; right: 30px; top: 156px; border: 0 solid; background: transparent; padding-top: 3px; padding-bottom: 10px; height: 33px; font-size: 12px; color: #000;">{{REFERENCE}}</div>
                            <div style="position: absolute; bottom: 149px; right: 12px; width: 238px; border: 0 transparent; background: transparent; height: 45px; font-size: 12px; padding-top: 5px; word-wrap: break-word; word-break: break-all; color: #000;">{{DESCRIPTION}}</div>
                        </div>
                    </body>
                    </html>
            """;

    /**
     * Generates HTML for Croatian Uplatnica with underage member support
     *
     * @param contact The contact (payer) information
     * @param organization The organization (recipient) information
     * @param paymentTemplate The payment template with amount and other details
     * @param barcodeImage The generated barcode image
     * @param underagedMember The underage member information (can be null)
     * @return Complete HTML string for the uplatnica
     */
    public static String generateUplatnicaHtml(Contact contact, Organization organization,
                                               PaymentTemplate paymentTemplate, BufferedImage barcodeImage,
                                               UnderagedMember underagedMember) {

        String processedHtml = HTML_TEMPLATE;

        // Payer information
        String payerName = buildPayerName(contact);
        String payerAddress = buildPayerAddress(contact);
        String payerCity = buildPayerCity(contact);

        // Recipient information
        String recipientName = organization.getName();
        String recipientAddress = buildRecipientAddress(organization);
        String recipientCity = buildRecipientCity(organization);

        // Amount formatting
        String amount = paymentTemplate.getAmount().toString();

        // Reference number
        String reference = buildReference(paymentTemplate, contact);

        // Description - USE THE TEMPLATEPROCESSOR UTILITY
        String description = TemplateProcessor.processTemplate(
                paymentTemplate.getDescription(), contact, underagedMember);

        // Convert barcode to base64
        String barcodeBase64 = convertBarcodeToBase64(barcodeImage);

        // Replace all placeholders
        processedHtml = processedHtml.replace("{{PAYER_NAME}}", payerName);
        processedHtml = processedHtml.replace("{{PAYER_ADDRESS}}", payerAddress);
        processedHtml = processedHtml.replace("{{PAYER_CITY}}", payerCity);
        processedHtml = processedHtml.replace("{{AMOUNT}}", amount);
        processedHtml = processedHtml.replace("{{RECIPIENT_IBAN}}", organization.getIban());
        processedHtml = processedHtml.replace("{{RECIPIENT_NAME}}", recipientName);
        processedHtml = processedHtml.replace("{{RECIPIENT_ADDRESS}}", recipientAddress);
        processedHtml = processedHtml.replace("{{RECIPIENT_CITY}}", recipientCity);
        processedHtml = processedHtml.replace("{{MODEL}}", paymentTemplate.getModelOfPayment() != null ? paymentTemplate.getModelOfPayment() : "");
        processedHtml = processedHtml.replace("{{REFERENCE}}", reference);
        processedHtml = processedHtml.replace("{{PURPOSE}}", "");
        processedHtml = processedHtml.replace("{{DESCRIPTION}}", description);
        processedHtml = processedHtml.replace("{{BARCODE_BASE64}}", barcodeBase64);

        return processedHtml;
    }

    /**
     * Backward compatibility - generates HTML without explicit underage member
     * Automatically detects and loads underage member if contact is underage
     *
     * @param contact The contact (payer) information
     * @param organization The organization (recipient) information
     * @param paymentTemplate The payment template with amount and other details
     * @param barcodeImage The generated barcode image
     * @return Complete HTML string for the uplatnica
     */
    public static String generateUplatnicaHtml(Contact contact, Organization organization,
                                               PaymentTemplate paymentTemplate, BufferedImage barcodeImage) {

        // Use the TemplateProcessor utility to automatically detect and load underage member if needed
        UnderagedMember underagedMember = null;
        if (TemplateProcessor.isContactUnderage(contact)) {
            underagedMember = TemplateProcessor.getActiveUnderagedMember(contact.getId());
        }

        return generateUplatnicaHtml(contact, organization, paymentTemplate, barcodeImage, underagedMember);
    }

    private static String buildPayerName(Contact contact) {
        return contact.getFirstName() + " " + contact.getLastName();
    }

    private static String buildPayerAddress(Contact contact) {
        String payerAddress = "";
        if (contact.getStreetName() != null && !contact.getStreetName().trim().isEmpty()) {
            payerAddress = contact.getStreetName();
            if (contact.getStreetNum() != null && !contact.getStreetNum().trim().isEmpty()) {
                payerAddress += " " + contact.getStreetNum();
            }
        }
        return payerAddress;
    }

    private static String buildPayerCity(Contact contact) {
        String payerCity = "";
        if (contact.getPostalCode() != null && !contact.getPostalCode().trim().isEmpty()) {
            payerCity = contact.getPostalCode();
            if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
                payerCity += " " + contact.getCity();
            }
        } else if (contact.getCity() != null && !contact.getCity().trim().isEmpty()) {
            payerCity = contact.getCity();
        }
        return payerCity;
    }

    private static String buildRecipientAddress(Organization organization) {
        String recipientAddress = "";
        if (organization.getStreetName() != null && !organization.getStreetName().trim().isEmpty()) {
            recipientAddress = organization.getStreetName();
            if (organization.getStreetNum() != null && !organization.getStreetNum().trim().isEmpty()) {
                recipientAddress += " " + organization.getStreetNum();
            }
        }
        return recipientAddress;
    }

    private static String buildRecipientCity(Organization organization) {
        String recipientCity = "";
        if (organization.getPostalCode() != null && !organization.getPostalCode().trim().isEmpty()) {
            recipientCity = organization.getPostalCode();
            if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
                recipientCity += " " + organization.getCity();
            }
        } else if (organization.getCity() != null && !organization.getCity().trim().isEmpty()) {
            recipientCity = organization.getCity();
        }
        return recipientCity;
    }

    private static String buildReference(PaymentTemplate paymentTemplate, Contact contact) {
        String reference = paymentTemplate.getPozivNaBroj();
        if (reference == null || reference.trim().isEmpty()) {
            return "";
        }
        return reference.replace("{contact_id}", String.valueOf(contact.getId()));
    }

    private static String convertBarcodeToBase64(BufferedImage barcodeImage) {
        if (barcodeImage == null) {
            return "";
        }

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(barcodeImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("Error converting barcode to base64: " + e.getMessage());
            return "";
        }
    }


}