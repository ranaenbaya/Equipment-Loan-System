package equipmentloan.core.user;

/**
 * Staff can usually keep items a little longer and borrow more at once.
 */
public class Staff extends User {
    private static final long serialVersionUID = 3L;
    private static final int MAX_ACTIVE_LOANS = 4;
    private static final int DEFAULT_LOAN_DAYS = 14;

    public Staff(int id, String name, int universityId, String email, String phone) {
        super(id, name, universityId, email, phone);
    }

    @Override
    public String getRole() {
        return "Staff";
    }

    @Override
    public int getMaxActiveLoans() {
        return MAX_ACTIVE_LOANS;
    }

    @Override
    public int getDefaultLoanDays() {
        return DEFAULT_LOAN_DAYS;
    }
}
