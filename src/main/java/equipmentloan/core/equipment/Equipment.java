package equipmentloan.core.equipment;

import equipmentloan.core.common.Category;
import equipmentloan.core.validation.ValidationUtils;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Base class for all equipment items stored in the lab catalogue.
 *
 * <p>Common fields such as name, manufacturer and availability live here so
 * subclasses only need to store their specialist details.</p>
 */
public abstract class Equipment implements Serializable {
    private static final long serialVersionUID = 3L;

    private final int id;
    private String itemName;
    private String manufacturer;
    private String model;
    private LocalDate acquisitionDate;
    private Category category;
    private boolean available;

    protected Equipment(int id,
                        String itemName,
                        String manufacturer,
                        String model,
                        LocalDate acquisitionDate,
                        Category category) {
        this.id = ValidationUtils.requirePositive(id, "Equipment ID");
        this.itemName = ValidationUtils.requireNonBlank(itemName, "Item name");
        this.manufacturer = ValidationUtils.requireNonBlank(manufacturer, "Manufacturer");
        this.model = ValidationUtils.requireNonBlank(model, "Model");
        this.acquisitionDate = ValidationUtils.requireNonFutureDate(acquisitionDate, "Acquisition date");
        this.category = category == null ? Category.OTHER : category;
        this.available = true;
    }

    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = ValidationUtils.requireNonBlank(itemName, "Item name");
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = ValidationUtils.requireNonBlank(manufacturer, "Manufacturer");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = ValidationUtils.requireNonBlank(model, "Model");
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = ValidationUtils.requireNonFutureDate(acquisitionDate, "Acquisition date");
    }

    public Category getCategory() {
        return category;
    }

    protected void setCategory(Category category) {
        this.category = category == null ? Category.OTHER : category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Subclasses use this to show the one extra detail that matters for that item.
     */
    public abstract String getSpecialDetails();

    @Override
    public String toString() {
        return String.format(
                "%d | %s | %s %s | Category: %s | Bought: %s | Fee/day: £%.2f | %s | %s",
                id,
                itemName,
                manufacturer,
                model,
                category,
                acquisitionDate,
                category.getDailyOverdueFee(),
                getSpecialDetails(),
                available ? "Available" : "On loan"
        );
    }
}
