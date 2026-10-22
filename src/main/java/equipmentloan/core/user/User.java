package equipmentloan.core.user;

import equipmentloan.core.validation.ValidationUtils;

import java.io.Serializable;

/**
 * Shared user data for anyone allowed to borrow equipment.
 *
 * <p>The class keeps the common details in one place so student and staff
 * accounts only need to provide the parts that are different, such as the role
 * name and loan rules.</p>
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 3L;

    private final int id;
    private String name;
    private int universityId;
    private String email;
    private String phone;

    protected User(int id, String name, int universityId, String email, String phone) {
        this.id = ValidationUtils.requirePositive(id, "User ID");
        this.name = ValidationUtils.requireNonBlank(name, "Name");
        this.universityId = ValidationUtils.requirePositive(universityId, "University ID");
        this.email = ValidationUtils.requireEmail(email);
        this.phone = ValidationUtils.requirePhone(phone);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ValidationUtils.requireNonBlank(name, "Name");
    }

    public int getUniversityId() {
        return universityId;
    }

    public void setUniversityId(int universityId) {
        this.universityId = ValidationUtils.requirePositive(universityId, "University ID");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = ValidationUtils.requireEmail(email);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = ValidationUtils.requirePhone(phone);
    }

    /**
     * Display name used in menus and reports.
     */
    public abstract String getRole();

    /**
     * Simple business rule used to stop one user taking too many items at once.
     */
    public abstract int getMaxActiveLoans();

    /**
     * Default loan length in days for this user type.
     */
    public abstract int getDefaultLoanDays();

    @Override
    public String toString() {
        return String.format(
                "%d | %s | %s | University ID: %d | Email: %s | Phone: %s | Max loans: %d | Default loan: %d day(s)",
                id,
                name,
                getRole(),
                universityId,
                email,
                phone,
                getMaxActiveLoans(),
                getDefaultLoanDays()
        );
    }
}
