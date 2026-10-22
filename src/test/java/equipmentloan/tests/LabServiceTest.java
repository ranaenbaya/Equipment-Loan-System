package equipmentloan.tests;

import equipmentloan.core.equipment.Camera;
import equipmentloan.core.equipment.Equipment;
import equipmentloan.core.equipment.Laptop;
import equipmentloan.core.loan.LoanRecord;
import equipmentloan.core.user.User;
import equipmentloan.persistence.FileManager;
import equipmentloan.service.LabService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the service layer.
 * These tests check the main business rules without using the console menu.
 */
class LabServiceTest {

    private LabService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new LabService();
    }

    @Test
    void shouldAddEquipmentAndUsers() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.addCamera("Canon DSLR", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");

        assertEquals(2, service.getEquipment().size());
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void shouldSuggestSevenDayLoanForStudent() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");

        LocalDate checkout = LocalDate.now().minusDays(3);
        LoanRecord loan = service.borrowWithSuggestedDueDate(1, 1, checkout);

        assertEquals(checkout.plusDays(7), loan.getDueDate());
    }

    @Test
    void shouldSuggestFourteenDayLoanForStaff() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");

        LocalDate checkout = LocalDate.now().minusDays(3);
        LoanRecord loan = service.borrowWithSuggestedDueDate(1, 1, checkout);

        assertEquals(checkout.plusDays(14), loan.getDueDate());
    }

    @Test
    void shouldCalculateLateLaptopFeeCorrectly() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");

        LocalDate checkout = LocalDate.now().minusDays(10);
        LocalDate dueDate = LocalDate.now().minusDays(2);

        service.borrow(1, 1, checkout, dueDate);
        double fee = service.returnItem(1, LocalDate.now());

        assertEquals(5.0, fee, 0.0001);
        assertTrue(service.findEquipment(1).isAvailable());
    }

    @Test
    void shouldCalculateLateCameraFeeCorrectly() {
        service.addCamera("Canon DSLR", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");

        LocalDate checkout = LocalDate.now().minusDays(10);
        LocalDate dueDate = LocalDate.now().minusDays(2);

        service.borrow(1, 1, checkout, dueDate);
        double fee = service.returnItem(1, LocalDate.now());

        assertEquals(6.0, fee, 0.0001);
        assertTrue(service.findEquipment(1).isAvailable());
    }

    @Test
    void shouldBlockBorrowingUnavailableEquipment() {
        service.addCamera("Canon DSLR", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");

        service.borrow(2, 1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.borrow(1, 1, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3)));

        assertEquals("This item is already on loan.", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registerStudent("Test User", 10003, "bad-email", "+12025550103"));
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registerStudent("Test User", 10003, "test@example.com", "abc123"));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registerStudent("   ", 10003, "test@example.com", "+12025550103"));
    }

    @Test
    void shouldRejectDuplicateUniversityId() {
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");

        assertThrows(IllegalArgumentException.class, () ->
                service.registerStaff("Jamie Taylor", 10001, "jamie.taylor@example.edu", "+12025550102"));
    }

    @Test
    void shouldEnforceStudentLoanLimit() {
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");

        service.addLaptop("Laptop 1", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.addCamera("Camera 1", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.addLaptop("Laptop 2", "HP", "840",
                LocalDate.now().minusMonths(5), "Windows 11");

        service.borrow(1, 1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));
        service.borrow(1, 2, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));

        assertThrows(IllegalStateException.class, () ->
                service.borrow(1, 3, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3)));
    }

    @Test
    void shouldEnforceStaffLoanLimit() {
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");

        service.addLaptop("Laptop 1", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.addLaptop("Laptop 2", "HP", "840",
                LocalDate.now().minusMonths(5), "Windows 11");
        service.addCamera("Camera 1", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.addCamera("Camera 2", "Sony", "A6000",
                LocalDate.now().minusMonths(7), "24MP");
        service.addLaptop("Laptop 3", "Lenovo", "T14",
                LocalDate.now().minusMonths(4), "Windows 11");

        service.borrow(1, 1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));
        service.borrow(1, 2, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));
        service.borrow(1, 3, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));
        service.borrow(1, 4, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));

        assertThrows(IllegalStateException.class, () ->
                service.borrow(1, 5, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3)));
    }

    @Test
    void shouldPreventDeletingUserWithActiveLoan() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");
        service.borrow(1, 1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));

        assertThrows(IllegalStateException.class, () -> service.deleteUser(1));
    }

    @Test
    void shouldPreventDeletingEquipmentWithActiveLoan() {
        service.addLaptop("Dell Latitude", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 11");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");
        service.borrow(1, 1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2));

        assertThrows(IllegalStateException.class, () -> service.deleteEquipment(1));
    }

    @Test
    void shouldUpdateLaptopDetails() {
        service.addLaptop("Old Laptop", "Dell", "7420",
                LocalDate.now().minusMonths(6), "Windows 10");

        LocalDate newDate = LocalDate.now().minusMonths(2);
        service.updateLaptop(1, "New Laptop", "Lenovo", "ThinkPad X1", newDate, "Windows 11");

        Equipment equipment = service.findEquipment(1);

        assertInstanceOf(Laptop.class, equipment);
        assertEquals("New Laptop", equipment.getItemName());
        assertEquals("Lenovo", equipment.getManufacturer());
        assertEquals("ThinkPad X1", equipment.getModel());
        assertEquals(newDate, equipment.getAcquisitionDate());
        assertEquals("Windows 11", ((Laptop) equipment).getOperatingSystem());
    }

    @Test
    void shouldUpdateCameraDetails() {
        service.addCamera("Old Camera", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(6), "24MP");

        LocalDate newDate = LocalDate.now().minusMonths(2);
        service.updateCamera(1, "New Camera", "Sony", "A6000", newDate, "32MP");

        Equipment equipment = service.findEquipment(1);

        assertInstanceOf(Camera.class, equipment);
        assertEquals("New Camera", equipment.getItemName());
        assertEquals("Sony", equipment.getManufacturer());
        assertEquals("A6000", equipment.getModel());
        assertEquals(newDate, equipment.getAcquisitionDate());
        assertEquals("32MP", ((Camera) equipment).getResolution());
    }

    @Test
    void shouldUpdateUserDetails() {
        service.registerStudent("Morgan Lee", 10001, "morgan.lee@example.edu", "+12025550101");

        service.updateUser(1, "New Name", 10005, "morgan.new@example.edu", "+12025550105");

        User user = service.findUser(1);

        assertNotNull(user);
        assertEquals("New Name", user.getName());
        assertEquals(10005, user.getUniversityId());
        assertEquals("morgan.new@example.edu", user.getEmail());
        assertEquals("+12025550105", user.getPhone());
    }

    @Test
    void shouldGenerateOverdueReminder() {
        service.addCamera("Canon DSLR", "Canon", "EOS 250D",
                LocalDate.now().minusMonths(8), "24.1MP");
        service.registerStaff("Jamie Taylor", 10002, "jamie.taylor@example.edu", "+12025550102");

        LocalDate checkout = LocalDate.now().minusDays(10);
        LocalDate dueDate = LocalDate.now().minusDays(2);

        service.borrow(1, 1, checkout, dueDate);

        String reminder = service.generateOverdueReminder(1);

        assertTrue(reminder.contains("overdue"));
    }

    @Test
    void shouldSaveAndLoadServiceData() {
        service.addLaptop("Library Laptop", "ExampleTech", "Model A",
                LocalDate.now().minusMonths(6), "ExampleOS");
        service.registerStudent("Alex Rivera", 10001, "alex.rivera@example.edu", "+12025550101");
        service.borrow(1, 1, LocalDate.now().minusDays(2), LocalDate.now().plusDays(5));

        String file = tempDir.resolve("equipment-loans.ser").toString();
        FileManager.save(file, service);
        LabService loaded = FileManager.load(file);

        assertNotNull(loaded);
        assertEquals(1, loaded.getEquipment().size());
        assertEquals(1, loaded.getUsers().size());
        assertEquals(1, loaded.getActiveLoans().size());
        assertFalse(loaded.findEquipment(1).isAvailable());
    }

    @Test
    void shouldReturnNullWhenSavedDataDoesNotExist() {
        String file = tempDir.resolve("missing.ser").toString();

        assertNull(FileManager.load(file));
    }
}
