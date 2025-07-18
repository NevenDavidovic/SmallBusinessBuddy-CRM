package smallbusinessbuddycrm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import smallbusinessbuddycrm.database.OrganizationDAO;
import smallbusinessbuddycrm.model.Organization;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private TitledPane bookmarks;

    @FXML
    private MenuButton userProfileButton; // Reference to the MenuButton in FXML

    @FXML
    private Circle userAvatar; // Reference to the Circle for organization logo

    private OrganizationDAO organizationDAO = new OrganizationDAO();

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {


      loadOrganizationName();


    }

    /**
     * Loads the organization name and image, sets them to the user profile button
     */
    private void loadOrganizationName() {

        try {
            Optional<Organization> organization = organizationDAO.getFirst();

            if (organization.isPresent()) {
                Organization org = organization.get();
                String orgName = org.getName();

                System.out.println("Organization found: " + orgName);
                System.out.println("Organization has image: " + (org.getImage() != null));
                if (org.getImage() != null) {
                    System.out.println("Image size: " + org.getImage().length + " bytes");
                }

                // Set organization name
                if (userProfileButton != null) {
                    userProfileButton.setText(orgName);
                    System.out.println("Organization name set to button: " + orgName);
                } else {
                    System.err.println("ERROR: userProfileButton is null - check FXML fx:id");
                }

                // Set organization image
                loadOrganizationImage(org);

            } else {
                // No organization found - set default
                System.out.println("No organization found in database");
                if (userProfileButton != null) {
                    userProfileButton.setText("No Organization");
                }
                setDefaultAvatar();
            }

        } catch (Exception e) {
            System.err.println("Error loading organization data: " + e.getMessage());
            e.printStackTrace();

            // Set fallback text and image
            if (userProfileButton != null) {
                userProfileButton.setText("Error Loading");
            }
            setDefaultAvatar();
        }
        System.out.println("=== Organization Data Loading Complete ===");
    }

    /**
     * Loads organization image and sets it as the avatar background
     */
    private void loadOrganizationImage(Organization organization) {
        System.out.println("=== Loading Organization Image ===");
        try {
            if (userAvatar == null) {
                System.err.println("ERROR: userAvatar is null - check FXML fx:id");
                return;
            }

            if (organization.getImage() != null && organization.getImage().length > 0) {
                System.out.println("Converting byte array to JavaFX Image...");

                // Convert byte array to JavaFX Image
                javafx.scene.image.Image orgImage = new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(organization.getImage())
                );

                // Check if image loaded successfully
                if (orgImage.isError()) {
                    System.err.println("Error loading image: " + orgImage.getException().getMessage());
                    setDefaultAvatar();
                    return;
                }

                System.out.println("Image loaded successfully. Width: " + orgImage.getWidth() + ", Height: " + orgImage.getHeight());

                // Create ImagePattern to fill the circle
                javafx.scene.paint.ImagePattern imagePattern = new javafx.scene.paint.ImagePattern(orgImage);
                userAvatar.setFill(imagePattern);

                System.out.println("Organization image applied to avatar successfully");
            } else {
                // No image available, use default
                System.out.println("No organization image available, using default");
                setDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Error loading organization image: " + e.getMessage());
            e.printStackTrace();
            setDefaultAvatar();
        }
        System.out.println("=== Organization Image Loading Complete ===");
    }

    /**
     * Sets a default avatar (color or pattern) when no organization image is available
     */
    private void setDefaultAvatar() {
        System.out.println("Setting default avatar...");
        if (userAvatar != null) {
            // Use a solid color
            userAvatar.setFill(javafx.scene.paint.Color.web("#0099cc")); // Blue color
            System.out.println("Default blue avatar set");

            // Alternative: Use initials or gradient
            // You could also create a text-based avatar with organization initials here
        } else {
            System.err.println("Cannot set default avatar - userAvatar is null");
        }
    }

    /**
     * Public method to refresh organization name and image (useful after updating organization)
     */
    public void refreshOrganizationData() {
        System.out.println("Refreshing organization data...");
        loadOrganizationName();
    }

    /**
     * Navigates to a new view by loading it into the content area
     * while keeping the sidebar and top navigation intact.
     *
     * @param fxmlPath The path to the FXML file to load
     */
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Clear current content and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately in your application
        }
    }

    // Handler methods for sidebar buttons

    @FXML
    private void handleContactsAction() {
        navigateTo("/views/crm/contacts-view.fxml");
    }

    @FXML
    private void handleOrganizationAction() {
        navigateTo("/views/general/organization-view.fxml");
    }

    @FXML
    private void handleListsAction() {
        navigateTo("/views/crm/lists-view.fxml");
    }

    @FXML
    private void handleWorkshopsAction() {
        navigateTo("/views/crm/workshops-view.fxml");
    }

    @FXML
    private void handleEmailAction() {
        navigateTo("/views/marketing/email-view.fxml");
    }

    @FXML
    private void handleEmailStatisticsAction() {
        navigateTo("/views/marketing/email-statistics-view.fxml");
    }

    @FXML
    private void handleBarcodeAppAction() {
        navigateTo("/views/commerce/barcode-generator-view.fxml");
    }

    @FXML
    private void handlePaymentSlipsAction() {
        navigateTo("/views/commerce/payment-slips-view.fxml");
    }

    @FXML
    private void handlePaymentHistoryAction() {
        navigateTo("/views/commerce/payment-history-view.fxml");
    }

    @FXML
    private void handleContactStatisticsAction() {
        navigateTo("/views/reporting/contact-statistics-view.fxml");
    }

    @FXML
    private void handleWorkshopsStatisticsAction() {
        navigateTo("/views/reporting/workshop-statistics-view.fxml");
    }

    @FXML
    private void handleEStatisticsAction() {
        navigateTo("/views/reporting/email-statistics-view.fxml");
    }

    @FXML
    private void handleTeachersAction() {
        navigateTo("/views/crm/teacher-view.fxml");
    }


    @FXML
    private void handleEmailTemplateAction() {
        navigateTo("/views/marketing/email-builder.fxml");
    }


    @FXML
    private void handleSettingsAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/settings_view.fxml");
    }

    @FXML
    private void handleProfileAction() {
        navigateTo("/com/example/smallbusinessbuddy_crm/views/profile_view.fxml");
    }

    @FXML
    private void handleLogoutAction() {
        // Handle logout logic
        System.out.println("Logout requested");
    }
}