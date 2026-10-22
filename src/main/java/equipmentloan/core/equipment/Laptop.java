package equipmentloan.core.equipment;

import equipmentloan.core.common.Category;
import equipmentloan.core.validation.ValidationUtils;

import java.time.LocalDate;

/**
 * Laptop equipment item.
 */
public class Laptop extends Equipment {
    private static final long serialVersionUID = 2L;

    private String operatingSystem;

    public Laptop(int id,
                  String itemName,
                  String manufacturer,
                  String model,
                  LocalDate acquisitionDate,
                  String operatingSystem) {
        super(id, itemName, manufacturer, model, acquisitionDate, Category.LAPTOP);
        this.operatingSystem = ValidationUtils.requireNonBlank(operatingSystem, "Operating system");
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = ValidationUtils.requireNonBlank(operatingSystem, "Operating system");
    }

    @Override
    public String getSpecialDetails() {
        return "OS: " + operatingSystem;
    }
}
