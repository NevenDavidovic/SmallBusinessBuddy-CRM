package smallbusinessbuddycrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class List {
    private int id;
    private String name;
    private String description;
    private String type;
    private String objectType;
    private String creator;
    private String folder;
    private String createdAt;
    private String updatedAt;
    private boolean isDeleted;
    private String deletedAt;
    private int listSize;

    // Constructors
    public List() {
        this.type = "CUSTOM";
        this.objectType = "CONTACT";
        this.isDeleted = false;
        String currentTime = LocalDateTime.now().toString();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }

    public List(String name, String description, String creator) {
        this();
        this.name = name;
        this.description = description;
        this.creator = creator;
    }

    public List(int id, String name, String description, String type, String objectType,
                String creator, String folder, String createdAt, String updatedAt,
                boolean isDeleted, String deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.objectType = objectType;
        this.creator = creator;
        this.folder = folder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now().toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now().toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now().toString();
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
        this.updatedAt = LocalDateTime.now().toString();
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
        this.updatedAt = LocalDateTime.now().toString();
    }

    // ✅ FIXED: Date getters and setters now work with Strings
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        if (deleted) {
            this.deletedAt = LocalDateTime.now().toString();
        }
        this.updatedAt = LocalDateTime.now().toString();
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    // ✅ FIXED: Utility methods that handle String to LocalDateTime conversion
    public String getFormattedCreatedAt() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Unknown";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(createdAt);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return createdAt; // Return original if parsing fails
        }
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null || updatedAt.isEmpty()) {
            return "Unknown";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(updatedAt);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return updatedAt; // Return original if parsing fails
        }
    }

    public String getFormattedDeletedAt() {
        if (deletedAt == null || deletedAt.isEmpty()) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(deletedAt);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return deletedAt; // Return original if parsing fails
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        List list = (List) obj;
        return id == list.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}