package smallbusinessbuddycrm.model;

import java.time.LocalDate;

public class Contact {
    private int id;
    private String firstName;
    private String lastName;
    private String streetName;
    private String streetNum;
    private String postalCode;
    private String city;
    private String email;
    private String phoneNum;
    private boolean isMember;
    private LocalDate memberSince;
    private LocalDate memberUntil;
    private String createdAt;
    private String updatedAt;

    // Constructors, getters and setters

    public Contact() {}

    public Contact(int id, String firstName, String lastName, String email, boolean isMember) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isMember = isMember;
    }

    // ... Add all getters and setters here ...
}
