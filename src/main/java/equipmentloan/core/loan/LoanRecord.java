package equipmentloan.core.loan;

import equipmentloan.core.common.Category;
import equipmentloan.core.validation.ValidationUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Stores one borrowing transaction from checkout to return.
 *
 * <p>The record keeps the category fee at the point the loan was created so
 * the fee calculation stays stable even if policy changes later on.</p>
 */
public class LoanRecord implements Serializable {
    private static final long serialVersionUID = 3L;

    private final int id;
    private final int userId;
    private final int equipmentId;
    private final LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean reminderGenerated;
    private final Category category;
    private final double dailyOverdueFee;

    public LoanRecord(int id,
                      int userId,
                      int equipmentId,
                      LocalDate checkoutDate,
                      LocalDate dueDate,
                      Category category) {
        this.id = ValidationUtils.requirePositive(id, "Loan ID");
        this.userId = ValidationUtils.requirePositive(userId, "User ID");
        this.equipmentId = ValidationUtils.requirePositive(equipmentId, "Equipment ID");
        this.checkoutDate = ValidationUtils.requireNonFutureDate(
                checkoutDate == null ? LocalDate.now() : checkoutDate,
                "Checkout date"
        );
        this.dueDate = ValidationUtils.requireDateOrder(this.checkoutDate, dueDate, "Checkout date", "Due date");
        this.category = category == null ? Category.OTHER : category;
        this.dailyOverdueFee = this.category.getDailyOverdueFee();
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = ValidationUtils.requireDateOrder(checkoutDate, dueDate, "Checkout date", "Due date");
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    public Category getCategory() {
        return category;
    }

    public double getDailyOverdueFee() {
        return dailyOverdueFee;
    }

    public long getDaysLate(LocalDate actualReturnDate) {
        if (actualReturnDate == null || !actualReturnDate.isAfter(dueDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, actualReturnDate);
    }

    public double calculateOverdueFee(LocalDate actualReturnDate) {
        return getDaysLate(actualReturnDate) * dailyOverdueFee;
    }

    public double returnItem(LocalDate actualReturnDate) {
        ValidationUtils.requireDateOrder(checkoutDate, actualReturnDate, "Checkout date", "Return date");
        if (isReturned()) {
            throw new IllegalStateException("This loan has already been returned.");
        }
        this.returnDate = actualReturnDate;
        return calculateOverdueFee(actualReturnDate);
    }

    public String generateReminderMessage() {
        if (!isOverdue()) {
            return "Loan " + id + " is not overdue.";
        }
        reminderGenerated = true;
        return String.format(
                "Reminder: loan %d for equipment %d is overdue by %d day(s). Current fee: £%.2f.",
                id,
                equipmentId,
                ChronoUnit.DAYS.between(dueDate, LocalDate.now()),
                calculateOverdueFee(LocalDate.now())
        );
    }

    public boolean isReminderGenerated() {
        return reminderGenerated;
    }

    @Override
    public String toString() {
        return String.format(
                "Loan %d | User: %d | Item: %d | Category: %s | Checkout: %s | Due: %s | Return: %s | Fee/day: £%.2f | Status: %s",
                id,
                userId,
                equipmentId,
                category,
                checkoutDate,
                dueDate,
                returnDate == null ? "Not returned" : returnDate,
                dailyOverdueFee,
                isReturned() ? "Closed" : "Active"
        );
    }
}
