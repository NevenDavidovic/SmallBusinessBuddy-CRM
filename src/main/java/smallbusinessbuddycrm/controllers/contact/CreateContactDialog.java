package smallbusinessbuddycrm.controllers.contact;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smallbusinessbuddycrm.model.Contact;
import smallbusinessbuddycrm.model.UnderagedMember;
import smallbusinessbuddycrm.database.ContactDAO;
import smallbusinessbuddycrm.database.UnderagedDAO;
import smallbusinessbuddycrm.utilities.LanguageManager;

import java.time.LocalDate;

public class CreateContactDialog {

    private Stage dialogStage;
    private Contact result = null;
    private boolean okClicked = false;

    // Style constants
    private static final String FIELD_STYLE =
            "-fx-border-color: #dfe3eb; -fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-padding: 6 10; -fx-font-size: 12px;";

    private static final String SECTION_TITLE_STYLE =
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";

    private static final String LABEL_STYLE =
            "-fx-font-size: 12px; -fx-text-fill: #555555;";

    private static final String SECTION_STYLE =
            "-fx-border-color: #dfe3eb; -fx-border-radius: 6; -fx-border-width: 1;" +
                    "-fx-background-color: #ffffff; -fx-background-radius: 6; -fx-padding: 15;";

    private static final String BTN_PRIMARY =
            "-fx-background-color: #ff7a59; -fx-text-fill: white; -fx-border-radius: 4;" +
                    "-fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 7 18;";

    private static final String BTN_SECONDARY =
            "-fx-background-color: #ffffff; -fx-text-fill: #555555; -fx-border-radius: 4;" +
                    "-fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 7 18;" +
                    "-fx-border-color: #dfe3eb; -fx-border-width: 1;";

    private static final String BTN_SUCCESS =
            "-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4;" +
                    "-fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 7 18;";

    private static final String BTN_DANGER =
            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 4;" +
                    "-fx-background-radius: 4; -fx-font-size: 11px; -fx-padding: 4 10;";

    // Contact form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker birthdayPicker;
    private TextField pinField;
    private TextField emailField;
    private TextField phoneField;
    private TextField streetNameField;
    private TextField streetNumField;
    private TextField postalCodeField;
    private TextField cityField;
    private CheckBox memberCheckBox;
    private DatePicker memberSincePicker;
    private DatePicker memberUntilPicker;

    // Underaged members section
    private TableView<UnderagedMember> underagedTableView;
    private ObservableList<UnderagedMember> underagedMembersList = FXCollections.observableArrayList();

    // Underaged member form fields
    private TextField childFirstNameField;
    private TextField childLastNameField;
    private DatePicker childBirthDatePicker;
    private TextField childAgeField;
    private TextField childPinField;
    private ComboBox<String> childGenderComboBox;
    private CheckBox childMemberCheckBox;
    private DatePicker childMemberSincePicker;
    private DatePicker childMemberUntilPicker;
    private TextArea childNoteTextArea;

    // UI Labels
    private Label titleLabel;
    private Label contactInfoLabel;
    private Label underagedSectionLabel;
    private Label optionalLabel;
    private Label childFormTitleLabel;

    private Label firstNameLabel;
    private Label lastNameLabel;
    private Label birthdayLabel;
    private Label pinLabel;
    private Label emailLabel;
    private Label phoneLabel;
    private Label streetNameLabel;
    private Label streetNumLabel;
    private Label postalCodeLabel;
    private Label cityLabel;
    private Label memberLabel;
    private Label memberSinceLabel;
    private Label memberUntilLabel;

    private Label childFirstNameLabel;
    private Label childLastNameLabel;
    private Label childBirthDateLabel;
    private Label childAgeLabel;
    private Label childPinLabel;
    private Label childGenderLabel;
    private Label childMemberSinceLabel;
    private Label childMemberUntilLabel;
    private Label childNoteLabel;

    private Button addChildButton;
    private Button clearFormButton;
    private Button cancelButton;
    private Button saveButton;

    public CreateContactDialog(Stage parentStage) {
        createDialogStage();
        dialogStage.initOwner(parentStage);
        updateTexts();
    }

    private void createDialogStage() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(true);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: #f5f8fa;");

        VBox mainLayout = new VBox(14);
        mainLayout.setPadding(new Insets(22));
        mainLayout.setStyle("-fx-background-color: #f5f8fa;");

        // Page title
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #dfe3eb;");

        VBox contactSection = createContactSection();
        VBox underagedSection = createUnderagedMembersSection();
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, sep, contactSection, underagedSection, buttonBox);
        scrollPane.setContent(mainLayout);

        Scene scene = new Scene(scrollPane, 600, 500);
        dialogStage.setScene(scene);
        dialogStage.setMinWidth(560);
        dialogStage.setMinHeight(500);
    }

    private VBox createContactSection() {
        VBox section = new VBox(10);
        section.setStyle(SECTION_STYLE);

        contactInfoLabel = new Label();
        contactInfoLabel.setStyle(SECTION_TITLE_STYLE);

        GridPane formGrid = createContactFormGrid();

        section.getChildren().addAll(contactInfoLabel, formGrid);
        return section;
    }

    private GridPane createContactFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        int row = 0;

        firstNameLabel = new Label();
        firstNameLabel.setStyle(LABEL_STYLE);
        grid.add(firstNameLabel, 0, row);
        firstNameField = new TextField();
        firstNameField.setPrefWidth(240);
        firstNameField.setStyle(FIELD_STYLE);
        grid.add(firstNameField, 1, row++);

        lastNameLabel = new Label();
        lastNameLabel.setStyle(LABEL_STYLE);
        grid.add(lastNameLabel, 0, row);
        lastNameField = new TextField();
        lastNameField.setStyle(FIELD_STYLE);
        grid.add(lastNameField, 1, row++);

        birthdayLabel = new Label();
        birthdayLabel.setStyle(LABEL_STYLE);
        grid.add(birthdayLabel, 0, row);
        birthdayPicker = new DatePicker();
        birthdayPicker.setPrefWidth(240);
        birthdayPicker.setStyle(FIELD_STYLE);
        grid.add(birthdayPicker, 1, row++);

        pinLabel = new Label();
        pinLabel.setStyle(LABEL_STYLE);
        grid.add(pinLabel, 0, row);
        pinField = new TextField();
        pinField.setStyle(FIELD_STYLE);
        grid.add(pinField, 1, row++);

        emailLabel = new Label();
        emailLabel.setStyle(LABEL_STYLE);
        grid.add(emailLabel, 0, row);
        emailField = new TextField();
        emailField.setStyle(FIELD_STYLE);
        grid.add(emailField, 1, row++);

        phoneLabel = new Label();
        phoneLabel.setStyle(LABEL_STYLE);
        grid.add(phoneLabel, 0, row);
        phoneField = new TextField();
        phoneField.setStyle(FIELD_STYLE);
        grid.add(phoneField, 1, row++);

        streetNameLabel = new Label();
        streetNameLabel.setStyle(LABEL_STYLE);
        grid.add(streetNameLabel, 0, row);
        streetNameField = new TextField();
        streetNameField.setStyle(FIELD_STYLE);
        grid.add(streetNameField, 1, row++);

        streetNumLabel = new Label();
        streetNumLabel.setStyle(LABEL_STYLE);
        grid.add(streetNumLabel, 0, row);
        streetNumField = new TextField();
        streetNumField.setStyle(FIELD_STYLE);
        grid.add(streetNumField, 1, row++);

        postalCodeLabel = new Label();
        postalCodeLabel.setStyle(LABEL_STYLE);
        grid.add(postalCodeLabel, 0, row);
        postalCodeField = new TextField();
        postalCodeField.setStyle(FIELD_STYLE);
        grid.add(postalCodeField, 1, row++);

        cityLabel = new Label();
        cityLabel.setStyle(LABEL_STYLE);
        grid.add(cityLabel, 0, row);
        cityField = new TextField();
        cityField.setStyle(FIELD_STYLE);
        grid.add(cityField, 1, row++);

        memberLabel = new Label();
        memberLabel.setStyle(LABEL_STYLE);
        grid.add(memberLabel, 0, row);
        memberCheckBox = new CheckBox();
        grid.add(memberCheckBox, 1, row++);

        memberSinceLabel = new Label();
        memberSinceLabel.setStyle(LABEL_STYLE);
        grid.add(memberSinceLabel, 0, row);
        memberSincePicker = new DatePicker();
        memberSincePicker.setDisable(true);
        memberSincePicker.setStyle(FIELD_STYLE);
        grid.add(memberSincePicker, 1, row++);

        memberUntilLabel = new Label();
        memberUntilLabel.setStyle(LABEL_STYLE);
        grid.add(memberUntilLabel, 0, row);
        memberUntilPicker = new DatePicker();
        memberUntilPicker.setDisable(true);
        memberUntilPicker.setStyle(FIELD_STYLE);
        grid.add(memberUntilPicker, 1, row++);

        memberCheckBox.setOnAction(e -> {
            boolean isMember = memberCheckBox.isSelected();
            memberSincePicker.setDisable(!isMember);
            memberUntilPicker.setDisable(!isMember);
            if (!isMember) {
                memberSincePicker.setValue(null);
                memberUntilPicker.setValue(null);
            }
        });

        return grid;
    }

    private VBox createUnderagedMembersSection() {
        VBox section = new VBox(10);
        section.setStyle(SECTION_STYLE);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        underagedSectionLabel = new Label();
        underagedSectionLabel.setStyle(SECTION_TITLE_STYLE);
        optionalLabel = new Label();
        optionalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #adb5bd; -fx-font-style: italic;");
        titleBox.getChildren().addAll(underagedSectionLabel, optionalLabel);

        underagedTableView = createUnderagedMembersTable();
        VBox addChildForm = createAddChildForm();

        section.getChildren().addAll(titleBox, underagedTableView, addChildForm);
        return section;
    }

    private TableView<UnderagedMember> createUnderagedMembersTable() {
        TableView<UnderagedMember> table = new TableView<>();
        table.setPrefHeight(140);
        table.setStyle(
                "-fx-border-color: #dfe3eb; -fx-border-radius: 4;" +
                        "-fx-background-color: white; -fx-font-size: 12px;");
        table.setItems(underagedMembersList);

        TableColumn<UnderagedMember, String> firstNameCol = new TableColumn<>();
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(95);

        TableColumn<UnderagedMember, String> lastNameCol = new TableColumn<>();
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(95);

        TableColumn<UnderagedMember, Integer> ageCol = new TableColumn<>();
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(45);

        TableColumn<UnderagedMember, String> pinCol = new TableColumn<>();
        pinCol.setCellValueFactory(new PropertyValueFactory<>("pin"));
        pinCol.setPrefWidth(80);

        TableColumn<UnderagedMember, String> genderCol = new TableColumn<>();
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(65);

        TableColumn<UnderagedMember, String> memberCol = new TableColumn<>();
        memberCol.setCellValueFactory(cellData -> {
            boolean isMember = cellData.getValue().isMember();
            LanguageManager lm = LanguageManager.getInstance();
            String memberText = isMember
                    ? lm.getText("underaged.table.member.yes")
                    : lm.getText("underaged.table.member.no");
            return new javafx.beans.property.SimpleStringProperty(memberText);
        });
        memberCol.setPrefWidth(65);

        TableColumn<UnderagedMember, Void> actionsCol = new TableColumn<>();
        actionsCol.setCellFactory(param -> new TableCell<UnderagedMember, Void>() {
            private final Button deleteBtn = new Button();
            {
                deleteBtn.setStyle(BTN_DANGER);
                deleteBtn.setOnAction(event -> {
                    UnderagedMember member = getTableView().getItems().get(getIndex());
                    underagedMembersList.remove(member);
                });
                LanguageManager.getInstance().addLanguageChangeListener(() ->
                        deleteBtn.setText(LanguageManager.getInstance().getText("underaged.table.remove")));
                deleteBtn.setText(LanguageManager.getInstance().getText("underaged.table.remove"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionsCol.setPrefWidth(75);

        table.getColumns().addAll(firstNameCol, lastNameCol, ageCol, pinCol, genderCol, memberCol, actionsCol);
        return table;
    }

    private VBox createAddChildForm() {
        VBox formSection = new VBox(10);
        formSection.setStyle(
                "-fx-border-color: #f0f2f5; -fx-border-radius: 4; -fx-border-width: 1;" +
                        "-fx-background-color: #f8f9fa; -fx-background-radius: 4; -fx-padding: 12;");

        childFormTitleLabel = new Label();
        childFormTitleLabel.setStyle(SECTION_TITLE_STYLE);

        GridPane childGrid = new GridPane();
        childGrid.setHgap(10);
        childGrid.setVgap(8);

        int row = 0;

        childFirstNameLabel = new Label();
        childFirstNameLabel.setStyle(LABEL_STYLE);
        childGrid.add(childFirstNameLabel, 0, row);
        childFirstNameField = new TextField();
        childFirstNameField.setPrefWidth(140);
        childFirstNameField.setStyle(FIELD_STYLE);
        childGrid.add(childFirstNameField, 1, row);

        childLastNameLabel = new Label();
        childLastNameLabel.setStyle(LABEL_STYLE);
        childGrid.add(childLastNameLabel, 2, row);
        childLastNameField = new TextField();
        childLastNameField.setPrefWidth(140);
        childLastNameField.setStyle(FIELD_STYLE);
        childGrid.add(childLastNameField, 3, row++);

        childBirthDateLabel = new Label();
        childBirthDateLabel.setStyle(LABEL_STYLE);
        childGrid.add(childBirthDateLabel, 0, row);
        childBirthDatePicker = new DatePicker();
        childBirthDatePicker.setPrefWidth(140);
        childBirthDatePicker.setStyle(FIELD_STYLE);
        childGrid.add(childBirthDatePicker, 1, row);

        childAgeLabel = new Label();
        childAgeLabel.setStyle(LABEL_STYLE);
        childGrid.add(childAgeLabel, 2, row);
        childAgeField = new TextField();
        childAgeField.setPrefWidth(50);
        childAgeField.setDisable(true);
        childAgeField.setStyle(FIELD_STYLE);
        childGrid.add(childAgeField, 3, row++);

        childPinLabel = new Label();
        childPinLabel.setStyle(LABEL_STYLE);
        childGrid.add(childPinLabel, 0, row);
        childPinField = new TextField();
        childPinField.setPrefWidth(140);
        childPinField.setStyle(FIELD_STYLE);
        childGrid.add(childPinField, 1, row);

        childGenderLabel = new Label();
        childGenderLabel.setStyle(LABEL_STYLE);
        childGrid.add(childGenderLabel, 2, row);
        childGenderComboBox = new ComboBox<>();
        childGenderComboBox.setPrefWidth(140);
        childGenderComboBox.setStyle(FIELD_STYLE);
        childGrid.add(childGenderComboBox, 3, row++);

        childMemberCheckBox = new CheckBox();
        childMemberCheckBox.setStyle("-fx-font-size: 12px;");
        childGrid.add(childMemberCheckBox, 0, row, 2, 1);
        row++;

        childMemberSinceLabel = new Label();
        childMemberSinceLabel.setStyle(LABEL_STYLE);
        childGrid.add(childMemberSinceLabel, 0, row);
        childMemberSincePicker = new DatePicker();
        childMemberSincePicker.setPrefWidth(140);
        childMemberSincePicker.setDisable(true);
        childMemberSincePicker.setStyle(FIELD_STYLE);
        childGrid.add(childMemberSincePicker, 1, row);

        childMemberUntilLabel = new Label();
        childMemberUntilLabel.setStyle(LABEL_STYLE);
        childGrid.add(childMemberUntilLabel, 2, row);
        childMemberUntilPicker = new DatePicker();
        childMemberUntilPicker.setPrefWidth(140);
        childMemberUntilPicker.setDisable(true);
        childMemberUntilPicker.setStyle(FIELD_STYLE);
        childGrid.add(childMemberUntilPicker, 3, row++);

        childNoteLabel = new Label();
        childNoteLabel.setStyle(LABEL_STYLE);
        childGrid.add(childNoteLabel, 0, row);
        childNoteTextArea = new TextArea();
        childNoteTextArea.setPrefRowCount(2);
        childNoteTextArea.setPrefWidth(300);
        childNoteTextArea.setStyle(FIELD_STYLE);
        childGrid.add(childNoteTextArea, 1, row, 3, 1);

        childBirthDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                int age = LocalDate.now().getYear() - newDate.getYear();
                if (LocalDate.now().getDayOfYear() < newDate.getDayOfYear()) age--;
                childAgeField.setText(String.valueOf(age));
            }
        });

        childMemberCheckBox.setOnAction(e -> {
            boolean isMember = childMemberCheckBox.isSelected();
            childMemberSincePicker.setDisable(!isMember);
            childMemberUntilPicker.setDisable(!isMember);
            if (!isMember) {
                childMemberSincePicker.setValue(null);
                childMemberUntilPicker.setValue(null);
            }
        });

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        addChildButton = new Button();
        addChildButton.setStyle(BTN_SUCCESS);
        addChildButton.setOnAction(e -> handleAddChild());

        clearFormButton = new Button();
        clearFormButton.setStyle(BTN_SECONDARY);
        clearFormButton.setOnAction(e -> clearChildForm());

        buttonBox.getChildren().addAll(addChildButton, clearFormButton);
        formSection.getChildren().addAll(childFormTitleLabel, childGrid, buttonBox);
        return formSection;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(4, 0, 0, 0));

        cancelButton = new Button();
        cancelButton.setStyle(BTN_SECONDARY);
        cancelButton.setPrefWidth(90);
        cancelButton.setOnAction(e -> dialogStage.close());

        saveButton = new Button();
        saveButton.setStyle(BTN_PRIMARY);
        saveButton.setPrefWidth(90);
        saveButton.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        if (dialogStage != null) dialogStage.setTitle(lm.getText("contact.dialog.title"));
        if (titleLabel != null) titleLabel.setText(lm.getText("contact.dialog.title"));
        if (contactInfoLabel != null) contactInfoLabel.setText(lm.getText("contact.dialog.contact.info"));
        if (underagedSectionLabel != null) underagedSectionLabel.setText(lm.getText("contact.dialog.underaged.section"));
        if (optionalLabel != null) optionalLabel.setText(lm.getText("contact.dialog.optional"));

        if (firstNameLabel != null) firstNameLabel.setText(lm.getText("contact.form.first.name"));
        if (lastNameLabel != null) lastNameLabel.setText(lm.getText("contact.form.last.name"));
        if (birthdayLabel != null) birthdayLabel.setText(lm.getText("contact.form.birthday"));
        if (pinLabel != null) pinLabel.setText(lm.getText("contact.form.pin"));
        if (emailLabel != null) emailLabel.setText(lm.getText("contact.form.email"));
        if (phoneLabel != null) phoneLabel.setText(lm.getText("contact.form.phone"));
        if (streetNameLabel != null) streetNameLabel.setText(lm.getText("contact.form.street.name"));
        if (streetNumLabel != null) streetNumLabel.setText(lm.getText("contact.form.street.number"));
        if (postalCodeLabel != null) postalCodeLabel.setText(lm.getText("contact.form.postal.code"));
        if (cityLabel != null) cityLabel.setText(lm.getText("contact.form.city"));
        if (memberLabel != null) memberLabel.setText(lm.getText("contact.form.member"));
        if (memberSinceLabel != null) memberSinceLabel.setText(lm.getText("contact.form.member.since"));
        if (memberUntilLabel != null) memberUntilLabel.setText(lm.getText("contact.form.member.until"));

        if (pinField != null) pinField.setPromptText(lm.getText("contact.form.pin.placeholder"));
        if (memberCheckBox != null) memberCheckBox.setText(lm.getText("contact.form.is.member"));

        if (underagedTableView != null && underagedTableView.getColumns().size() >= 7) {
            underagedTableView.getColumns().get(0).setText(lm.getText("underaged.table.first.name"));
            underagedTableView.getColumns().get(1).setText(lm.getText("underaged.table.last.name"));
            underagedTableView.getColumns().get(2).setText(lm.getText("underaged.table.age"));
            underagedTableView.getColumns().get(3).setText(lm.getText("underaged.table.pin"));
            underagedTableView.getColumns().get(4).setText(lm.getText("underaged.table.gender"));
            underagedTableView.getColumns().get(5).setText(lm.getText("underaged.table.member"));
            underagedTableView.getColumns().get(6).setText(lm.getText("underaged.table.actions"));
        }

        if (childFormTitleLabel != null) childFormTitleLabel.setText(lm.getText("child.form.title"));
        if (childFirstNameLabel != null) childFirstNameLabel.setText(lm.getText("child.form.first.name"));
        if (childLastNameLabel != null) childLastNameLabel.setText(lm.getText("child.form.last.name"));
        if (childBirthDateLabel != null) childBirthDateLabel.setText(lm.getText("child.form.birth.date"));
        if (childAgeLabel != null) childAgeLabel.setText(lm.getText("child.form.age"));
        if (childPinLabel != null) childPinLabel.setText(lm.getText("child.form.pin"));
        if (childGenderLabel != null) childGenderLabel.setText(lm.getText("child.form.gender"));
        if (childMemberSinceLabel != null) childMemberSinceLabel.setText(lm.getText("child.form.member.since"));
        if (childMemberUntilLabel != null) childMemberUntilLabel.setText(lm.getText("child.form.member.until"));
        if (childNoteLabel != null) childNoteLabel.setText(lm.getText("child.form.note"));

        if (childPinField != null) childPinField.setPromptText(lm.getText("child.form.pin.placeholder"));
        if (childMemberCheckBox != null) childMemberCheckBox.setText(lm.getText("child.form.is.member"));

        if (childGenderComboBox != null) {
            String selectedValue = childGenderComboBox.getValue();
            ObservableList<String> genderOptions = FXCollections.observableArrayList(
                    lm.getText("gender.male"),
                    lm.getText("gender.female"),
                    lm.getText("gender.other")
            );
            childGenderComboBox.setItems(genderOptions);
            if (selectedValue != null) childGenderComboBox.getSelectionModel().select(selectedValue);
        }

        if (addChildButton != null) addChildButton.setText(lm.getText("child.form.add.button"));
        if (clearFormButton != null) clearFormButton.setText(lm.getText("child.form.clear.button"));
        if (cancelButton != null) cancelButton.setText(lm.getText("button.cancel"));
        if (saveButton != null) saveButton.setText(lm.getText("button.save"));

        if (underagedTableView != null) underagedTableView.refresh();
    }

    private void handleAddChild() {
        if (validateChildInput()) {
            UnderagedMember child = createUnderagedMemberFromInput();
            underagedMembersList.add(child);
            clearChildForm();

            LanguageManager lm = LanguageManager.getInstance();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lm.getText("dialog.success"));
            alert.setHeaderText(null);
            alert.setContentText(lm.getText("dialog.child.added"));
            alert.showAndWait();
        }
    }

    private boolean validateChildInput() {
        StringBuilder errors = new StringBuilder();
        LanguageManager lm = LanguageManager.getInstance();

        if (childFirstNameField.getText().trim().isEmpty())
            errors.append(lm.getText("validation.child.first.name.required")).append("\n");
        if (childLastNameField.getText().trim().isEmpty())
            errors.append(lm.getText("validation.child.last.name.required")).append("\n");
        if (childBirthDatePicker.getValue() == null)
            errors.append(lm.getText("validation.child.birth.date.required")).append("\n");

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lm.getText("dialog.validation.error"));
            alert.setHeaderText(lm.getText("dialog.validation.fix.errors"));
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private UnderagedMember createUnderagedMemberFromInput() {
        UnderagedMember child = new UnderagedMember();
        child.setFirstName(childFirstNameField.getText().trim());
        child.setLastName(childLastNameField.getText().trim());
        child.setBirthDate(childBirthDatePicker.getValue());
        child.setPin(childPinField.getText().trim());
        try {
            child.setAge(Integer.parseInt(childAgeField.getText().trim()));
        } catch (NumberFormatException e) {
            child.setAge(0);
        }
        child.setGender(childGenderComboBox.getValue());
        child.setMember(childMemberCheckBox.isSelected());
        child.setMemberSince(childMemberSincePicker.getValue());
        child.setMemberUntil(childMemberUntilPicker.getValue());
        child.setNote(childNoteTextArea.getText().trim());
        String now = java.time.LocalDateTime.now().toString();
        child.setCreatedAt(now);
        child.setUpdatedAt(now);
        return child;
    }

    private void clearChildForm() {
        childFirstNameField.clear();
        childLastNameField.clear();
        childBirthDatePicker.setValue(null);
        childAgeField.clear();
        childPinField.clear();
        childGenderComboBox.setValue(null);
        childMemberCheckBox.setSelected(false);
        childMemberSincePicker.setValue(null);
        childMemberUntilPicker.setValue(null);
        childNoteTextArea.clear();
        childMemberSincePicker.setDisable(true);
        childMemberUntilPicker.setDisable(true);
    }

    private void handleSave() {
        if (validateInput()) {
            try {
                Contact newContact = createContactFromInput();
                ContactDAO contactDAO = new ContactDAO();
                boolean success = contactDAO.createContact(newContact);

                if (success) {
                    if (!underagedMembersList.isEmpty()) {
                        UnderagedDAO underagedDAO = new UnderagedDAO();
                        for (UnderagedMember child : underagedMembersList) {
                            child.setContactId(newContact.getId());
                            boolean childSuccess = underagedDAO.createUnderagedMember(child);
                            if (!childSuccess) {
                                System.err.println("Failed to save child: " + child.getFullName());
                            }
                        }
                    }
                    result = newContact;
                    okClicked = true;
                    dialogStage.close();
                } else {
                    showErrorAlert("Failed to save contact to database.");
                }
            } catch (Exception e) {
                showErrorAlert("Error saving contact: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        LanguageManager lm = LanguageManager.getInstance();

        if (firstNameField.getText().trim().isEmpty())
            errors.append(lm.getText("validation.first.name.required")).append("\n");
        if (lastNameField.getText().trim().isEmpty())
            errors.append(lm.getText("validation.last.name.required")).append("\n");
        if (emailField.getText().trim().isEmpty())
            errors.append(lm.getText("validation.email.required")).append("\n");
        else if (!isValidEmail(emailField.getText().trim()))
            errors.append(lm.getText("validation.email.invalid")).append("\n");

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lm.getText("dialog.validation.error"));
            alert.setHeaderText(lm.getText("dialog.validation.fix.errors"));
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private Contact createContactFromInput() {
        Contact contact = new Contact();
        contact.setFirstName(firstNameField.getText().trim());
        contact.setLastName(lastNameField.getText().trim());
        contact.setBirthday(birthdayPicker.getValue());
        contact.setPin(pinField.getText().trim());
        contact.setEmail(emailField.getText().trim());
        contact.setPhoneNum(phoneField.getText().trim());
        contact.setStreetName(streetNameField.getText().trim());
        contact.setStreetNum(streetNumField.getText().trim());
        contact.setPostalCode(postalCodeField.getText().trim());
        contact.setCity(cityField.getText().trim());
        contact.setMember(memberCheckBox.isSelected());
        if (memberCheckBox.isSelected()) {
            contact.setMemberSince(memberSincePicker.getValue());
            contact.setMemberUntil(memberUntilPicker.getValue());
        }
        String now = java.time.LocalDateTime.now().toString();
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);
        return contact;
    }

    private void showErrorAlert(String message) {
        LanguageManager lm = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lm.getText("dialog.error.title"));
        alert.setHeaderText(lm.getText("dialog.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        updateTexts();
        dialogStage.showAndWait();
        return okClicked;
    }

    public Contact getResult() {
        return result;
    }

    public ObservableList<UnderagedMember> getUnderagedMembers() {
        return underagedMembersList;
    }
}