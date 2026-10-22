package equipmentloan.core.user;

/**
 * Student accounts are given slightly stricter borrowing limits than staff.
 */
public class Student extends User {
    private static final long serialVersionUID = 3L;
    private static final int MAX_ACTIVE_LOANS = 2;
    private static final int DEFAULT_LOAN_DAYS = 7;

    public Student(int id, String name, int universityId, String email, String phone) {
        super(id, name, universityId, email, phone);
    }

    @Override
    public String getRole() {
        return "Student";
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
