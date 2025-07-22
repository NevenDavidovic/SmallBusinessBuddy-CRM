package smallbusinessbuddycrm.model;

import java.math.BigDecimal;

public class PaymentTemplate {
    private int id;
    private String name;
    private String description;
    private BigDecimal amount;
    private String modelOfPayment;
    private String pozivNaBroj;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    // UI property for table selection
    private boolean selected = false;

    // Constructors
    public PaymentTemplate() {}

    public PaymentTemplate(String name, String description, BigDecimal amount, String modelOfPayment) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.modelOfPayment = modelOfPayment;
        this.isActive = true;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getModelOfPayment() { return modelOfPayment; }
    public void setModelOfPayment(String modelOfPayment) { this.modelOfPayment = modelOfPayment; }

    public String getPozivNaBroj() { return pozivNaBroj; }
    public void setPozivNaBroj(String pozivNaBroj) { this.pozivNaBroj = pozivNaBroj; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // UI selection property
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public String toString() {
        return name + " - " + amount + " EUR";
    }
}