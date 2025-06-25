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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private int listSize; // This will be calculated from list_contacts table

    // Constructors
    public List() {
        this.type = "CUSTOM";
        this.objectType = "CONTACT";
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public List(String name, String description, String creator) {
        this();
        this.name = name;
        this.description = description;
        this.creator = creator;
    }

    public List(int id, String name, String description, String type, String objectType,
                String creator, String folder, LocalDateTime createdAt, LocalDateTime updatedAt,
                boolean isDeleted, LocalDateTime deletedAt) {
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
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        if (deleted) {
            this.deletedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    // Utility methods
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedUpdatedAt() {
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedDeletedAt() {
        return deletedAt != null ? deletedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
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