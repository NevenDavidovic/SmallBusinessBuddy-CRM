package smallbusinessbuddycrm.model;

import java.time.LocalDateTime;

public class Organization {
    private int id;
    private String name;
    private String iban;
    private String streetName;
    private String streetNum;
    private String postalCode;
    private String city;
    private String email;
    private byte[] image;
    private String phoneNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Organization() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public Organization(String name, String iban) {
        this();
        this.name = name;
        this.iban = iban;
    }

    // Full constructor
    public Organization(int id, String name, String iban, String streetName, String streetNum,
                        String postalCode, String city, String email, byte[] image,
                        String phoneNum, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.iban = iban;
        this.streetName = streetName;
        this.streetNum = streetNum;
        this.postalCode = postalCode;
        this.city = city;
        this.email = email;
        this.image = image;
        this.phoneNum = phoneNum;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getStreetNum() {
        return streetNum;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getImage() {
        return image;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void setIban(String iban) {
        this.iban = iban;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStreetNum(String streetNum) {
        this.streetNum = streetNum;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCity(String city) {
        this.city = city;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void setImage(byte[] image) {
        this.image = image;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetName != null && !streetName.isEmpty()) {
            address.append(streetName);
            if (streetNum != null && !streetNum.isEmpty()) {
                address.append(" ").append(streetNum);
            }
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(postalCode);
        }
        if (city != null && !city.isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(city);
        }
        return address.toString();
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iban='" + iban + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phoneNum + '\'' +
                ", address='" + getFullAddress() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Organization that = (Organization) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}