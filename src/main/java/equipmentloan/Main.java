package equipmentloan;

import equipmentloan.service.LabService;
import equipmentloan.ui.MainMenu;

/**
 * Application entry point for the Equipment Loan System.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        LabService service = new LabService();
        service.loadData();
        new MainMenu(service).start();
        service.saveData();
    }
}
