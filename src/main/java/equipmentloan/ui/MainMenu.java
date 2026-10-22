package equipmentloan.ui;

import equipmentloan.core.equipment.Camera;
import equipmentloan.core.equipment.Equipment;
import equipmentloan.core.equipment.Laptop;
import equipmentloan.core.loan.LoanRecord;
import equipmentloan.core.user.User;
import equipmentloan.service.LabService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console menu for the Equipment Loan System.
 *
 * <p>The menu keeps input handling in one place and passes real work to the
 * service layer. That makes it easier to test the logic separately.</p>
 */
public class MainMenu {
    private final LabService service;
    private final Scanner sc = new Scanner(System.in);

    public MainMenu(LabService service) {
        this.service = service;
    }

    public void start() {
        System.out.println("Welcome to the University Lab Equipment Loan System.");
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> equipmentMenu();
                    case 2 -> userMenu();
                    case 3 -> borrowEquipment();
                    case 4 -> returnEquipment();
                    case 5 -> loansMenu();
                    case 6 -> viewAllData();
                    case 7 -> {
                        running = false;
                        System.out.println("Goodbye.");
                    }
                    default -> System.out.println("Please enter a number from 1 to 7.");
                }
            } catch (RuntimeException e) {
                System.out.println("Action failed: " + e.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("""

                ===== Main Menu =====
                1. Manage equipment catalogue
                2. Manage users
                3. Borrow equipment
                4. Return equipment
                5. Loan tools
                6. View all records
                7. Exit
                """);
    }

    private void equipmentMenu() {
        System.out.println("""

                ----- Equipment Menu -----
                1. Add equipment
                2. Edit equipment
                3. Delete equipment
                4. View all equipment
                5. View available equipment
                6. Back
                """);
        int choice = readInt("Choose an option: ");
        switch (choice) {
            case 1 -> addEquipment();
            case 2 -> editEquipment();
            case 3 -> deleteEquipment();
            case 4 -> printList("Equipment", service.getEquipment());
            case 5 -> printList("Available equipment", service.getAvailableEquipment());
            case 6 -> { }
            default -> System.out.println("Please enter a number from 1 to 6.");
        }
    }

    private void userMenu() {
        System.out.println("""

                ----- User Menu -----
                1. Add user
                2. Edit user
                3. Delete user
                4. View users
                5. Back
                """);
        int choice = readInt("Choose an option: ");
        switch (choice) {
            case 1 -> addUser();
            case 2 -> editUser();
            case 3 -> deleteUser();
            case 4 -> printList("Users", service.getUsers());
            case 5 -> { }
            default -> System.out.println("Please enter a number from 1 to 5.");
        }
    }

    private void loansMenu() {
        System.out.println("""

                ----- Loan Tools -----
                1. View active loans
                2. View overdue loans
                3. Generate overdue reminder
                4. Back
                """);
        int choice = readInt("Choose an option: ");
        switch (choice) {
            case 1 -> printList("Active loans", service.getActiveLoans());
            case 2 -> printList("Overdue loans", service.getOverdueLoans());
            case 3 -> generateReminder();
            case 4 -> { }
            default -> System.out.println("Please enter a number from 1 to 4.");
        }
    }

    private void addEquipment() {
        System.out.println("1. Laptop\n2. Camera");
        int type = readInt("Select equipment type: ");
        String itemName = readNonBlank("Item name: ");
        String manufacturer = readNonBlank("Manufacturer: ");
        String model = readNonBlank("Model: ");
        LocalDate acquisitionDate = readPastOrTodayDate("Acquisition date (yyyy-mm-dd): ");

        switch (type) {
            case 1 -> {
                String operatingSystem = readNonBlank("Operating system: ");
                System.out.println("Added laptop with ID " +
                        service.addLaptop(itemName, manufacturer, model, acquisitionDate, operatingSystem).getId() + ".");
            }
            case 2 -> {
                String resolution = readNonBlank("Resolution: ");
                System.out.println("Added camera with ID " +
                        service.addCamera(itemName, manufacturer, model, acquisitionDate, resolution).getId() + ".");
            }
            default -> System.out.println("Invalid equipment type.");
        }
    }

    private void editEquipment() {
        printList("Equipment", service.getEquipment());
        int equipmentId = readInt("Enter equipment ID to edit: ");
        Equipment equipment = service.findEquipment(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment ID not found.");
        }

        String itemName = readNonBlank("New item name: ");
        String manufacturer = readNonBlank("New manufacturer: ");
        String model = readNonBlank("New model: ");
        LocalDate acquisitionDate = readPastOrTodayDate("New acquisition date (yyyy-mm-dd): ");

        if (equipment instanceof Laptop) {
            String operatingSystem = readNonBlank("New operating system: ");
            service.updateLaptop(equipmentId, itemName, manufacturer, model, acquisitionDate, operatingSystem);
        } else if (equipment instanceof Camera) {
            String resolution = readNonBlank("New resolution: ");
            service.updateCamera(equipmentId, itemName, manufacturer, model, acquisitionDate, resolution);
        }
        System.out.println("Equipment updated successfully.");
    }

    private void deleteEquipment() {
        printList("Equipment", service.getEquipment());
        int equipmentId = readInt("Enter equipment ID to delete: ");
        service.deleteEquipment(equipmentId);
        System.out.println("Equipment deleted successfully.");
    }

    private void addUser() {
        System.out.println("1. Student\n2. Staff");
        int type = readInt("Select user type: ");
        String name = readNonBlank("Name: ");
        int universityId = readInt("University ID: ");
        String email = readNonBlank("Email: ");
        String phone = readNonBlank("Phone: ");

        switch (type) {
            case 1 -> System.out.println("Registered student with system ID " +
                    service.registerStudent(name, universityId, email, phone).getId() + ".");
            case 2 -> System.out.println("Registered staff member with system ID " +
                    service.registerStaff(name, universityId, email, phone).getId() + ".");
            default -> System.out.println("Invalid user type.");
        }
    }

    private void editUser() {
        printList("Users", service.getUsers());
        int userId = readInt("Enter user ID to edit: ");
        User user = service.findUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User ID not found.");
        }
        String name = readNonBlank("New name: ");
        int universityId = readInt("New university ID: ");
        String email = readNonBlank("New email: ");
        String phone = readNonBlank("New phone: ");
        service.updateUser(userId, name, universityId, email, phone);
        System.out.println("User updated successfully.");
    }

    private void deleteUser() {
        printList("Users", service.getUsers());
        int userId = readInt("Enter user ID to delete: ");
        service.deleteUser(userId);
        System.out.println("User deleted successfully.");
    }

    private void borrowEquipment() {
        printList("Users", service.getUsers());
        printList("Available equipment", service.getAvailableEquipment());
        int userId = readInt("Enter user ID: ");
        int equipmentId = readInt("Enter equipment ID: ");
        User user = service.findUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User ID not found.");
        }

        LocalDate checkoutDate = readPastOrTodayDate("Checkout date (yyyy-mm-dd): ");
        System.out.println("1. Use suggested due date for this user type");
        System.out.println("2. Enter due date manually");
        int dueChoice = readInt("Choose an option: ");

        LoanRecord record;
        if (dueChoice == 1) {
            record = service.borrowWithSuggestedDueDate(userId, equipmentId, checkoutDate);
        } else if (dueChoice == 2) {
            LocalDate dueDate = readDate("Due date (yyyy-mm-dd): ");
            record = service.borrow(userId, equipmentId, checkoutDate, dueDate);
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        System.out.println("Loan created successfully. Loan ID: " + record.getId());
        System.out.println("This user's active loan count is now: " + service.countActiveLoansForUser(userId));
    }

    private void returnEquipment() {
        printList("Active loans", service.getActiveLoans());
        int loanId = readInt("Enter loan ID to return: ");
        LocalDate returnDate = readDate("Return date (yyyy-mm-dd): ");
        double fee = service.returnItem(loanId, returnDate);
        System.out.printf("Equipment returned successfully. Overdue fee: GBP %.2f%n", fee);
    }

    private void generateReminder() {
        printList("Overdue loans", service.getOverdueLoans());
        int loanId = readInt("Enter overdue loan ID: ");
        String reminder = service.generateOverdueReminder(loanId);
        System.out.println(reminder);
    }

    private void viewAllData() {
        printList("Equipment", service.getEquipment());
        printList("Users", service.getUsers());
        printList("Loans", service.getLoans());
    }


    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be blank.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter the date in yyyy-mm-dd format.");
            }
        }
    }

    private LocalDate readPastOrTodayDate(String prompt) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (!date.isAfter(LocalDate.now())) {
                return date;
            }
            System.out.println("The date cannot be in the future.");
        }
    }

    private void printList(String title, List<?> items) {
        System.out.println("\n--- " + title + " ---");
        if (items.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        items.forEach(System.out::println);
    }
}
