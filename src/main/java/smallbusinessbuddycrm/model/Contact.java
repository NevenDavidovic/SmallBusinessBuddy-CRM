package smallbusinessbuddycrm.model;

import java.time.LocalDate;

public class Contact {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate birthday; // Added birthday field
    private String pin; // Personal identification number
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
    private boolean selected; // For checkbox in UI

    public Contact() {}

    public Contact(int id, String firstName, String lastName, String email, String phoneNum, boolean isMember) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNum = phoneNum;
        this.isMember = isMember;
    }

    // Constructor with birthday
    public Contact(int id, String firstName, String lastName, LocalDate birthday, String email, String phoneNum, boolean isMember) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.email = email;
        this.phoneNum = phoneNum;
        this.isMember = isMember;
    }

    // Constructor with birthday and PIN
    public Contact(int id, String firstName, String lastName, LocalDate birthday, String pin, String email, String phoneNum, boolean isMember) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.pin = pin;
        this.email = email;
        this.phoneNum = phoneNum;
        this.isMember = isMember;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getStreetNum() { return streetNum; }
    public void setStreetNum(String streetNum) { this.streetNum = streetNum; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNum() { return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    public boolean isMember() { return isMember; }
    public void setMember(boolean member) { isMember = member; }

    public LocalDate getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDate memberSince) { this.memberSince = memberSince; }

    public LocalDate getMemberUntil() { return memberUntil; }
    public void setMemberUntil(LocalDate memberUntil) { this.memberUntil = memberUntil; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    // Helper method to calculate age from birthday
    public int getAge() {
        if (birthday == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthday.getYear();
        if (now.getDayOfYear() < birthday.getDayOfYear()) {
            age--;
        }
        return age;
    }
}