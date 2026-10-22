package equipmentloan.core.validation;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Shared validation helpers used across the project.
 *
 * <p>Keeping checks here helps the rest of the code stay smaller and makes it
 * easier to apply the same rules everywhere.</p>
 */
public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtils() {
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    public static LocalDate requireNonFutureDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the future.");
        }
        return date;
    }

    public static LocalDate requireTodayOrFuture(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the past.");
        }
        return date;
    }

    public static LocalDate requireDateOrder(LocalDate earlier, LocalDate later,
                                             String earlierField, String laterField) {
        if (earlier == null || later == null) {
            throw new IllegalArgumentException("Both " + earlierField + " and " + laterField + " are required.");
        }
        if (later.isBefore(earlier)) {
            throw new IllegalArgumentException(laterField + " cannot be before " + earlierField + ".");
        }
        return later;
    }

    public static String requireEmail(String email) {
        String cleaned = requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Email format is invalid.");
        }
        return cleaned;
    }

    public static String requirePhone(String phone) {
        String cleaned = requireNonBlank(phone, "Phone number");
        if (!cleaned.matches("^[+0-9()\\- ]{7,20}$")) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }
        return cleaned;
    }
}
