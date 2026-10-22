package equipmentloan.core.common;

import java.io.Serializable;

/**
 * Small enum used to avoid passing category names around as raw strings.
 */
public enum Category implements Serializable {
    LAPTOP(2.50),
    CAMERA(3.00),
    LAB_INSTRUMENT(2.00),
    ROBOT(4.00),
    SENSOR(1.50),
    OTHER(2.00);

    private final double dailyOverdueFee;

    Category(double dailyOverdueFee) {
        this.dailyOverdueFee = dailyOverdueFee;
    }

    public double getDailyOverdueFee() {
        return dailyOverdueFee;
    }
}
