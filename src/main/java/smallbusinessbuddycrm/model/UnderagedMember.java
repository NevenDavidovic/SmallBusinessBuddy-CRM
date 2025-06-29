package smallbusinessbuddycrm.model;

import java.time.LocalDate;

public class UnderagedMember {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private int age;
    private String pin; // Personal identification number
    private String gender;
    private boolean isMember;
    private LocalDate memberSince;
    private LocalDate memberUntil;
    private String note;
    private String createdAt;
    private String updatedAt;
    private int contactId; // Foreign key to contacts table
    private boolean selected; // For checkbox in UI

    public UnderagedMember() {}

    public UnderagedMember(int id, String firstName, String lastName, LocalDate birthDate, int age, String gender, boolean isMember, int contactId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.age = age;
        this.gender = gender;
        this.isMember = isMember;
        this.contactId = contactId;
    }

    // Constructor with PIN
    public UnderagedMember(int id, String firstName, String lastName, LocalDate birthDate, int age, String pin, String gender, boolean isMember, int contactId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.age = age;
        this.pin = pin;
        this.gender = gender;
        this.isMember = isMember;
        this.contactId = contactId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isMember() { return isMember; }
    public void setMember(boolean member) { isMember = member; }

    public LocalDate getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDate memberSince) { this.memberSince = memberSince; }

    public LocalDate getMemberUntil() { return memberUntil; }
    public void setMemberUntil(LocalDate memberUntil) { this.memberUntil = memberUntil; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}