package smallbusinessbuddycrm.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Workshop {
    private int id;
    private String name;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String createdAt;
    private String updatedAt;
    private boolean selected; // For checkbox in UI

    public Workshop() {}

    public Workshop(int id, String name, LocalDate fromDate, LocalDate toDate) {
        this.id = id;
        this.name = name;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Helper methods
    public String getFormattedFromDate() {
        return fromDate != null ? fromDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
    }

    public String getFormattedToDate() {
        return toDate != null ? toDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
    }

    public String getDateRange() {
        if (fromDate != null && toDate != null) {
            return getFormattedFromDate() + " - " + getFormattedToDate();
        } else if (fromDate != null) {
            return getFormattedFromDate();
        } else if (toDate != null) {
            return "Until " + getFormattedToDate();
        }
        return "";
    }

    public long getDurationInDays() {
        if (fromDate != null && toDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1; // +1 to include both start and end dates
        }
        return 0;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        if (fromDate != null && toDate != null) {
            return !today.isBefore(fromDate) && !today.isAfter(toDate);
        }
        return false;
    }

    public boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        return fromDate != null && today.isBefore(fromDate);
    }

    public boolean isPast() {
        LocalDate today = LocalDate.now();
        return toDate != null && today.isAfter(toDate);
    }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Workshop";
    }
}