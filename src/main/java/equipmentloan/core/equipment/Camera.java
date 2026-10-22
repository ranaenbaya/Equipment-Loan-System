package equipmentloan.core.equipment;

import equipmentloan.core.common.Category;
import equipmentloan.core.validation.ValidationUtils;

import java.time.LocalDate;

/**
 * Camera equipment item.
 */
public class Camera extends Equipment {
    private static final long serialVersionUID = 2L;

    private String resolution;

    public Camera(int id,
                  String itemName,
                  String manufacturer,
                  String model,
                  LocalDate acquisitionDate,
                  String resolution) {
        super(id, itemName, manufacturer, model, acquisitionDate, Category.CAMERA);
        this.resolution = ValidationUtils.requireNonBlank(resolution, "Resolution");
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = ValidationUtils.requireNonBlank(resolution, "Resolution");
    }

    @Override
    public String getSpecialDetails() {
        return "Resolution: " + resolution;
    }
}
