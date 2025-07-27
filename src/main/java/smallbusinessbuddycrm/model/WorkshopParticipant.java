package smallbusinessbuddycrm.model;

public class WorkshopParticipant {
    private int id;
    private int workshopId;
    private Integer underagedId; // Nullable - for child participants
    private Integer contactId;   // Nullable - for adult participants
    private ParticipantType participantType;
    private PaymentStatus paymentStatus;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private boolean selected; // For checkbox in UI

    // Enums for better type safety
    public enum ParticipantType {
        ADULT, CHILD
    }

    public enum PaymentStatus {
        PENDING, PAID, REFUNDED, CANCELLED
    }

    public WorkshopParticipant() {}

    public WorkshopParticipant(int workshopId, ParticipantType participantType, PaymentStatus paymentStatus) {
        this.workshopId = workshopId;
        this.participantType = participantType;
        this.paymentStatus = paymentStatus;
    }

    // Constructor for adult participant
    public WorkshopParticipant(int workshopId, int contactId, PaymentStatus paymentStatus) {
        this.workshopId = workshopId;
        this.contactId = contactId;
        this.participantType = ParticipantType.ADULT;
        this.paymentStatus = paymentStatus;
    }

    // Constructor for child participant
    public WorkshopParticipant(int workshopId, int underagedId, PaymentStatus paymentStatus, boolean isChild) {
        this.workshopId = workshopId;
        this.underagedId = underagedId;
        this.participantType = ParticipantType.CHILD;
        this.paymentStatus = paymentStatus;
    }

    // Constructor with all fields (removed teacherId parameter)
    public WorkshopParticipant(int workshopId, Integer contactId, Integer underagedId,
                               ParticipantType participantType, PaymentStatus paymentStatus) {
        this.workshopId = workshopId;
        this.contactId = contactId;
        this.underagedId = underagedId;
        this.participantType = participantType;
        this.paymentStatus = paymentStatus;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWorkshopId() { return workshopId; }
    public void setWorkshopId(int workshopId) { this.workshopId = workshopId; }

    public Integer getUnderagedId() { return underagedId; }
    public void setUnderagedId(Integer underagedId) {
        this.underagedId = underagedId;
        if (underagedId != null) {
            this.contactId = null; // Ensure only one is set
            this.participantType = ParticipantType.CHILD;
        }
    }

    public Integer getContactId() { return contactId; }
    public void setContactId(Integer contactId) {
        this.contactId = contactId;
        if (contactId != null) {
            this.underagedId = null; // Ensure only one is set
            this.participantType = ParticipantType.ADULT;
        }
    }

    public ParticipantType getParticipantType() { return participantType; }
    public void setParticipantType(ParticipantType participantType) { this.participantType = participantType; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Helper methods
    public boolean isAdult() {
        return participantType == ParticipantType.ADULT;
    }

    public boolean isChild() {
        return participantType == ParticipantType.CHILD;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public String getParticipantTypeString() {
        return participantType != null ? participantType.toString() : "";
    }

    public String getPaymentStatusString() {
        return paymentStatus != null ? paymentStatus.toString() : "";
    }

    // Get the actual participant ID (either contact or underaged)
    public Integer getParticipantId() {
        return isAdult() ? contactId : underagedId;
    }

    // Validation method
    public boolean isValid() {
        // Exactly one of contactId or underagedId must be set
        boolean hasContact = contactId != null;
        boolean hasUnderaged = underagedId != null;

        if (hasContact && hasUnderaged) {
            return false; // Both set - invalid
        }

        if (!hasContact && !hasUnderaged) {
            return false; // Neither set - invalid
        }

        // Check participant type matches the ID type
        if (hasContact && participantType != ParticipantType.ADULT) {
            return false;
        }

        if (hasUnderaged && participantType != ParticipantType.CHILD) {
            return false;
        }

        return workshopId > 0 && participantType != null && paymentStatus != null;
    }

    @Override
    public String toString() {
        return "WorkshopParticipant{" +
                "id=" + id +
                ", workshopId=" + workshopId +
                ", participantType=" + participantType +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}