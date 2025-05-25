package smallbusinessbuddycrm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ContactViewController {



    @FXML
    private ComboBox<String> actionsComboBox;

    @FXML
    private Button importButton;

    @FXML
    private Button createContactButton;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Contact> contactsTable;

    @FXML
    private TableColumn<Contact, Boolean> selectColumn;

    @FXML
    private TableColumn<Contact, String> nameColumn;

    @FXML
    private TableColumn<Contact, String> emailColumn;

    @FXML
    private TableColumn<Contact, String> phoneColumn;

    @FXML
    private TableColumn<Contact, String> statusColumn;

    @FXML
    private TableColumn<Contact, String> contentColumn;

    @FXML
    private TableColumn<Contact, String> preferenceColumn;

    @FXML
    private Pagination contactsPagination;

    @FXML
    private ComboBox<String> perPageComboBox;

    @FXML
    private ComboBox<String> contactOwnerComboBox;

    @FXML
    private ComboBox<String> createDateComboBox;

    @FXML
    private ComboBox<String> lastActivityComboBox;

    @FXML
    private ComboBox<String> leadStatusComboBox;

    private ObservableList<Contact> contactsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadSampleData();
        setupComboBoxes();
        setupButtons();
        setupSearch();
        setupPagination();
    }

    private void setupTable() {
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(createCheckBoxCellFactory());

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("contentTopic"));
        preferenceColumn.setCellValueFactory(new PropertyValueFactory<>("preference"));

        contactsTable.setItems(contactsList);
    }

    private Callback<TableColumn<Contact, Boolean>, TableCell<Contact, Boolean>> createCheckBoxCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Contact, Boolean> call(TableColumn<Contact, Boolean> param) {
                return new TableCell<>() {
                    private final CheckBox checkBox = new CheckBox();

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            Contact contact = getTableView().getItems().get(getIndex());
                            checkBox.setSelected(contact.isSelected());
                            checkBox.setOnAction(event -> contact.setSelected(checkBox.isSelected()));
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        };
    }

    private void loadSampleData() {
        contactsList.addAll(
                new Contact("ŠK Skalice", "bozo.miskovic@sszsd.hr", "--", "--", "--", "--"),
                new Contact("ŠK Knez Mislav", "ured@sk-knez-mislav.hrsk-knez.hr", "--", "--", "--", "--"),
                new Contact("ŠK Trilj", "tadinacsasa@gmail.com", "--", "--", "--", "--"),
                new Contact("ŠK Student Split", "igor.vukovic@hotmail.com", "--", "--", "--", "--"),
                new Contact("ŠK Sinj", "sahovskiklubsinj@gmail.com", "--", "--", "--", "--"),
                new Contact("ŠK Mornar Split", "skmornar@gmail.com", "--", "--", "--", "--"),
                new Contact("ŠK Brda", "mateo.ivic@24sata.hr", "--", "New", "--", "--"),
                new Contact("ŠK popovača", "danijel.markesic@gmail.com", "--", "New", "--", "--")
        );
    }

    private void setupComboBoxes() {
        perPageComboBox.getItems().addAll("10 per page", "25 per page", "50 per page", "100 per page");
        perPageComboBox.setValue("25 per page");

        actionsComboBox.getItems().addAll("Delete", "Export", "Edit");

        contactOwnerComboBox.getItems().addAll("Any owner", "Me", "Unassigned");
        createDateComboBox.getItems().addAll("Any time", "Today", "Yesterday", "Last 7 days", "Last 30 days");
        lastActivityComboBox.getItems().addAll("Any time", "Today", "Yesterday", "Last 7 days", "Last 30 days");
        leadStatusComboBox.getItems().addAll("Any status", "New", "Open", "In progress", "Qualified");
    }

    private void setupButtons() {
        createContactButton.setOnAction(event -> System.out.println("Create contact clicked"));
        importButton.setOnAction(event -> System.out.println("Import clicked"));

    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Searching: " + newValue);
            // Implement search functionality here
        });
    }

    private void setupPagination() {
        contactsPagination.setPageCount(4);
        contactsPagination.setCurrentPageIndex(0);
        contactsPagination.setPageFactory(pageIndex -> {
            System.out.println("Loading page: " + pageIndex);
            // Implement pagination logic here
            return new HBox(); // Placeholder
        });
    }

    // Model class for contacts
    public static class Contact {
        private boolean selected;
        private final String name;
        private final String email;
        private final String phone;
        private final String status;
        private final String contentTopic;
        private final String preference;

        public Contact(String name, String email, String phone, String status, String contentTopic, String preference) {
            this.selected = false;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.status = status;
            this.contentTopic = contentTopic;
            this.preference = preference;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getStatus() {
            return status;
        }

        public String getContentTopic() {
            return contentTopic;
        }

        public String getPreference() {
            return preference;
        }
    }
}