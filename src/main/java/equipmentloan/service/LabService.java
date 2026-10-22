package equipmentloan.service;

import equipmentloan.core.equipment.Camera;
import equipmentloan.core.equipment.Equipment;
import equipmentloan.core.equipment.Laptop;
import equipmentloan.core.loan.LoanRecord;
import equipmentloan.core.user.Staff;
import equipmentloan.core.user.Student;
import equipmentloan.core.user.User;
import equipmentloan.core.validation.ValidationUtils;
import equipmentloan.persistence.FileManager;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main service layer for catalogue, user and loan operations.
 *
 * <p>This class is where the main business rules are enforced so the menu code
 * can stay focused on user interaction.</p>
 */
public class LabService implements Serializable {
    private static final long serialVersionUID = 3L;
    private static final String DATA_FILE = "data.ser";

    private List<Equipment> equipmentList = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<LoanRecord> loans = new ArrayList<>();

    private int equipmentId = 1;
    private int userId = 1;
    private int loanId = 1;

    public Laptop addLaptop(String itemName,
                            String manufacturer,
                            String model,
                            LocalDate acquisitionDate,
                            String operatingSystem) {
        Laptop laptop = new Laptop(equipmentId++, itemName, manufacturer, model, acquisitionDate, operatingSystem);
        equipmentList.add(laptop);
        return laptop;
    }

    public Camera addCamera(String itemName,
                            String manufacturer,
                            String model,
                            LocalDate acquisitionDate,
                            String resolution) {
        Camera camera = new Camera(equipmentId++, itemName, manufacturer, model, acquisitionDate, resolution);
        equipmentList.add(camera);
        return camera;
    }

    public Student registerStudent(String name, int universityId, String email, String phone) {
        ensureUniqueUniversityId(universityId, null);
        Student student = new Student(userId++, name, universityId, email, phone);
        users.add(student);
        return student;
    }

    public Staff registerStaff(String name, int universityId, String email, String phone) {
        ensureUniqueUniversityId(universityId, null);
        Staff staff = new Staff(userId++, name, universityId, email, phone);
        users.add(staff);
        return staff;
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipmentList);
    }

    public List<Equipment> getAvailableEquipment() {
        return equipmentList.stream().filter(Equipment::isAvailable).toList();
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<LoanRecord> getLoans() {
        return Collections.unmodifiableList(loans);
    }

    public Equipment findEquipment(int id) {
        return equipmentList.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    public User findUser(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public LoanRecord findLoan(int id) {
        return loans.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    public void updateLaptop(int equipmentId,
                             String itemName,
                             String manufacturer,
                             String model,
                             LocalDate acquisitionDate,
                             String operatingSystem) {
        Equipment equipment = requireEquipment(equipmentId);
        if (!(equipment instanceof Laptop laptop)) {
            throw new IllegalArgumentException("Selected equipment is not a laptop.");
        }
        laptop.setItemName(itemName);
        laptop.setManufacturer(manufacturer);
        laptop.setModel(model);
        laptop.setAcquisitionDate(acquisitionDate);
        laptop.setOperatingSystem(operatingSystem);
    }

    public void updateCamera(int equipmentId,
                             String itemName,
                             String manufacturer,
                             String model,
                             LocalDate acquisitionDate,
                             String resolution) {
        Equipment equipment = requireEquipment(equipmentId);
        if (!(equipment instanceof Camera camera)) {
            throw new IllegalArgumentException("Selected equipment is not a camera.");
        }
        camera.setItemName(itemName);
        camera.setManufacturer(manufacturer);
        camera.setModel(model);
        camera.setAcquisitionDate(acquisitionDate);
        camera.setResolution(resolution);
    }

    public void deleteEquipment(int equipmentId) {
        Equipment equipment = requireEquipment(equipmentId);
        boolean hasActiveLoan = loans.stream()
                .anyMatch(loan -> loan.getEquipmentId() == equipmentId && !loan.isReturned());
        if (hasActiveLoan) {
            throw new IllegalStateException("Cannot delete equipment with an active loan.");
        }
        equipmentList.remove(equipment);
    }

    public void updateUser(int userId, String name, int universityId, String email, String phone) {
        User user = requireUser(userId);
        ensureUniqueUniversityId(universityId, user);
        user.setName(name);
        user.setUniversityId(universityId);
        user.setEmail(email);
        user.setPhone(phone);
    }

    public void deleteUser(int userId) {
        User user = requireUser(userId);
        boolean hasActiveLoan = loans.stream()
                .anyMatch(loan -> loan.getUserId() == userId && !loan.isReturned());
        if (hasActiveLoan) {
            throw new IllegalStateException("Cannot delete a user with an active loan.");
        }
        users.remove(user);
    }

    public LoanRecord borrow(int userId, int equipmentId, LocalDate checkoutDate, LocalDate dueDate) {
        User user = requireUser(userId);
        Equipment equipment = requireEquipment(equipmentId);
        checkoutDate = ValidationUtils.requireNonFutureDate(checkoutDate, "Checkout date");
        dueDate = ValidationUtils.requireDateOrder(checkoutDate, dueDate, "Checkout date", "Due date");

        if (!equipment.isAvailable()) {
            throw new IllegalStateException("This item is already on loan.");
        }
        long activeLoansForUser = countActiveLoansForUser(userId);
        if (activeLoansForUser >= user.getMaxActiveLoans()) {
            throw new IllegalStateException(user.getRole() + " users can only hold " + user.getMaxActiveLoans() + " active loan(s)." );
        }

        equipment.setAvailable(false);
        LoanRecord record = new LoanRecord(loanId++, userId, equipmentId, checkoutDate, dueDate, equipment.getCategory());
        loans.add(record);
        return record;
    }

    public LoanRecord borrowWithSuggestedDueDate(int userId, int equipmentId, LocalDate checkoutDate) {
        User user = requireUser(userId);
        LocalDate safeCheckoutDate = ValidationUtils.requireNonFutureDate(checkoutDate, "Checkout date");
        LocalDate suggestedDueDate = safeCheckoutDate.plusDays(user.getDefaultLoanDays());
        return borrow(userId, equipmentId, safeCheckoutDate, suggestedDueDate);
    }

    public double returnItem(int loanId, LocalDate returnDate) {
        LoanRecord record = requireLoan(loanId);
        Equipment equipment = requireEquipment(record.getEquipmentId());
        double fee = record.returnItem(returnDate);
        equipment.setAvailable(true);
        return fee;
    }

    public String generateOverdueReminder(int loanId) {
        return requireLoan(loanId).generateReminderMessage();
    }

    public List<LoanRecord> getActiveLoans() {
        return loans.stream().filter(loan -> !loan.isReturned()).toList();
    }

    public List<LoanRecord> getOverdueLoans() {
        return loans.stream().filter(LoanRecord::isOverdue).toList();
    }

    public long countActiveLoansForUser(int userId) {
        return loans.stream().filter(loan -> loan.getUserId() == userId && !loan.isReturned()).count();
    }

    public void saveData() {
        FileManager.save(DATA_FILE, this);
    }

    public void loadData() {
        LabService loaded = FileManager.load(DATA_FILE);
        if (loaded != null) {
            this.equipmentList = new ArrayList<>(loaded.equipmentList);
            this.users = new ArrayList<>(loaded.users);
            this.loans = new ArrayList<>(loaded.loans);
            this.equipmentId = loaded.equipmentId;
            this.userId = loaded.userId;
            this.loanId = loaded.loanId;
        }
    }

    private Equipment requireEquipment(int equipmentId) {
        Equipment equipment = findEquipment(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment ID " + equipmentId + " was not found.");
        }
        return equipment;
    }

    private User requireUser(int userId) {
        User user = findUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User ID " + userId + " was not found.");
        }
        return user;
    }

    private LoanRecord requireLoan(int loanId) {
        LoanRecord record = findLoan(loanId);
        if (record == null) {
            throw new IllegalArgumentException("Loan ID " + loanId + " was not found.");
        }
        return record;
    }

    private void ensureUniqueUniversityId(int universityId, User currentUser) {
        boolean alreadyExists = users.stream()
                .filter(user -> currentUser == null || user.getId() != currentUser.getId())
                .anyMatch(user -> user.getUniversityId() == universityId);
        if (alreadyExists) {
            throw new IllegalArgumentException("University ID already exists.");
        }
    }
}
