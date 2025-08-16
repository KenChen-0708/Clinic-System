package boundary;

import adt.ListInterface;
import dao.MedicineNamesDAO;
import entity.Medicine;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import utility.MessageUI;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import utility.TablePrinter;

public class PharmacyUI {

    Scanner scanner = new Scanner(System.in);

    private final MedicineNamesDAO medicineNamesDAO = new MedicineNamesDAO();
    private ListInterface<String> medicineNames;

    public PharmacyUI() {
        // Load the list of medicine names when the UI is created
        medicineNames = medicineNamesDAO.retrieveFromFile();
    }

    public int getMenuChoice() {
        System.out.println("================================");
        System.out.println("   PHARMACY MANAGEMENT MENU");
        System.out.println("================================");
        System.out.println("1. Manage Medicine Inventory");
        System.out.println("2. Manage Medicine Names");
        System.out.println("3. Dispense Medicine to Patient");
        System.out.println("4. Generate Inventory Report");
        System.out.println("5. Generate Dispensed Medicines Report");
        System.out.println("0. Back to MAIN Menu");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();  // consume the newline
        System.out.println();
        return choice;
    }

    public int getInventoryManagementChoice() {
        System.out.println("============================================");
        System.out.println("     PHARMACY INVENTORY MANAGEMENT MENU");
        System.out.println("============================================");
        System.out.println("1. View Medicine Stock");
        System.out.println("2. Add New Medicine Stock");
        System.out.println("3. Edit Medicine Stock");
        System.out.println("4. Remove Medicine Stock");
        System.out.println("0. Back to PHARMACY Menu");
        System.out.println("--------------------------------------------");
        System.out.print("Enter your choice: ");

        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(s);
                if (choice >= 0 && choice <= 4) {
                    return choice;
                }
                System.out.print("Please enter 0-4: ");
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number (0-4): ");
            }
        }
    }

    private void displayMedicineNames(ListInterface<String> names) {
        if (names == null || names.isEmpty()) {
            System.out.println("No medicines available.");
            return;
        }

        for (int i = 1; i <= names.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + names.getEntry(i));
        }
    }

    public void runManageMedicineNames() {
        int choice = -1;
        do {
            // Show all the medicines
            System.out.println("================================");
            System.out.println("     MANAGE MEDICINE NAMES");
            System.out.println("================================");

            // Display the medicine names
            displayMedicineNames(medicineNames);

            // Provide options for adding, editing, or removing medicine names
            System.out.println("\nOptions:");
            System.out.println("1. Add New Medicine Name");
            System.out.println("2. Edit Medicine Name");
            System.out.println("3. Remove Medicine Name");
            System.out.println("0. Back to PHARMACY Menu");
            System.out.print("Enter your choice: ");

            choice = Integer.parseInt(scanner.nextLine().trim());

            switch (choice) {
                case 1:
                    addMedicineName();
                    break;
                case 2:
                    editMedicineName();
                    break;
                case 3:
                    removeMedicineName();
                    break;
                case 0:
                    MessageUI.clearConsole();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void addMedicineName() {
        while (true) {
            System.out.print("\nEnter the name of the new medicine (0 to Back): ");
            String newMedicineName = scanner.nextLine().trim();

            if ("0".equals(newMedicineName)) {
                MessageUI.clearConsole();
                System.out.println("Operation cancelled.");
                return;
            }

            if (newMedicineName.isEmpty()) {
                System.out.print("\nMedicine name cannot be empty. Please try again.");
                continue;
            }

            // Validation: only letters and spaces allowed
            if (!newMedicineName.matches("[a-zA-Z ]+")) {
                System.out.print("\nMedicine name must contain only letters and spaces. Please try again.");
                continue;
            }

            medicineNamesDAO.addMedicine(medicineNames, newMedicineName);
            MessageUI.clearConsole();
            System.out.println("Medicine added successfully.");
            break;
        }
    }

    private void editMedicineName() {
        System.out.print("\nEnter the number of the medicine to edit (0 to Back): ");
        int index;

        try {
            index = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (index == 0) {
            MessageUI.clearConsole();
            System.out.println("Operation cancelled.");
            return;
        }

        if (index < 1 || index > medicineNames.getNumberOfEntries()) {
            System.out.println("Invalid medicine number.");
            return;
        }

        // Loop until valid name or cancel
        while (true) {
            System.out.print("\nEnter the new name of the medicine (0 to Back): ");
            String newMedicineName = scanner.nextLine().trim();

            if ("0".equals(newMedicineName)) {
                MessageUI.clearConsole();
                System.out.println("Operation cancelled.");
                return;
            }

            if (newMedicineName.isEmpty()) {
                System.out.print("\nMedicine name cannot be empty. Please try again.");
                continue;
            }

            // Validation: only letters and spaces allowed
            if (!newMedicineName.matches("[a-zA-Z ]+")) {
                System.out.print("\nMedicine name must contain only letters and spaces. Please try again.");
                continue;
            }

            medicineNamesDAO.editMedicine(medicineNames, index, newMedicineName);
            MessageUI.clearConsole();
            System.out.println("Medicine updated successfully.");
            break;
        }
    }

    private void removeMedicineName() {
        System.out.print("\nEnter the number of the medicine to remove (0 to Back): ");
        int index;

        try {
            index = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (index == 0) {
            MessageUI.clearConsole();
            System.out.println("Operation cancelled.");
            return;
        }

        if (index < 1 || index > medicineNames.getNumberOfEntries()) {
            System.out.println("Invalid medicine number.");
            return;
        }

        String selectedMedicine = medicineNames.getEntry(index);

        // Confirmation menu
        while (true) {
            System.out.println("\nYou have selected: " + "[" + selectedMedicine + "]");
            System.out.println("1. Confirm removal");
            System.out.println("0. Cancel removal");
            System.out.print("Enter choice (0-1): ");

            String input = scanner.nextLine().trim();

            if ("0".equals(input)) {
                MessageUI.clearConsole();
                System.out.println("Operation cancelled.");
                return;
            } else if ("1".equals(input)) {
                medicineNamesDAO.removeMedicine(medicineNames, index);
                MessageUI.clearConsole();
                System.out.println("Medicine removed successfully.");
                return;
            } else {
                System.out.println("Invalid choice. Please enter 0 or 1.\n");
            }
        }
    }

    public int getSortChoice() {
        System.out.println("\n--- Sort / Group Inventory ---");
        System.out.println("1. Sort by ID");
        System.out.println("2. Sort by Expiry Date");
        System.out.println("3. Sort by Price");
        System.out.println("4. Group by Medicine Name (Total Quantity)");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(s);
                if (choice >= 0 && choice <= 4) {
                    return choice;
                }
                System.out.print("Please enter 0-4: ");
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number (0-4): ");
            }
        }
    }

    public int getSortDirection(String title, String ascLabel, String descLabel) {
        MessageUI.clearConsole();

        System.out.println("\nSort " + title + " order:");
        System.out.println("1. " + ascLabel);
        System.out.println("2. " + descLabel);
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(s);
                if (choice == 0) {
                    return 0;  // Back one level (to the sort menu)
                } else if (choice == 1 || choice == 2) {
                    return choice;
                }
                System.out.print("Please enter 0-2: ");
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number (0-2): ");
            }
        }
    }

    public void displayGroupedTotals(String totalsText) {
        System.out.println("\n--- Grouped by Medicine Name (Total Quantity) ---\n");
        if (totalsText == null || totalsText.trim().isEmpty()) {
            MessageUI.displayEmptyMessage();
            return;
        }
        System.out.println(totalsText);
    }

    public Medicine inputMedicineDetails(String generatedId) {
        System.out.println("\n== Add New Inventory (0 to back) ==");

        displayMedicineNames(medicineNames);

        final int PROMPT_WIDTH = 35;

        int choice;
        while (true) {
            System.out.printf("\n%-" + PROMPT_WIDTH + "s: ", "Select medicine name");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    MessageUI.clearConsole();
                    System.out.println("Canceled. No medicine added.");
                    return null; // exit early
                }
                if (choice >= 1 && choice <= medicineNames.getNumberOfEntries()) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        String name = medicineNames.getEntry(choice);

        int quantity;
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter quantity");
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                MessageUI.clearConsole();
                System.out.println("Canceled. No medicine added.");
                return null;
            }
            try {
                quantity = Integer.parseInt(s);
                if (quantity > 0) {
                    break;
                }
                System.out.println("Quantity must be greater than 0.\n");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.\n");
            }
        }

        double price;
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter price per unit");
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                MessageUI.clearConsole();
                System.out.println("Canceled. No medicine added.");
                return null;
            }
            try {
                price = Double.parseDouble(s);
                if (price >= 0) {
                    break;
                }
                System.out.println("Price cannot be negative.\n");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (e.g., 12.50).\n");
            }
        }

        LocalDate expiry;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        LocalDate today = LocalDate.now();
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter expiry date (DD-MM-YYYY)");
            String expiryStr = scanner.nextLine().trim();
            if ("0".equals(expiryStr)) {
                MessageUI.clearConsole();
                System.out.println("Canceled. No medicine added.");
                return null;
            }
            try {
                expiry = LocalDate.parse(expiryStr, fmt);
                if (expiry.isAfter(today)) {
                    break;
                }
                System.out.println("Expiry must be AFTER today (" + today.format(fmt) + "). Try again.\n");
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use format DD-MM-YYYY (e.g., 05-11-2026).\n");
            }
        }

        return new Medicine(generatedId, name, quantity, price, expiry);
    }

    public void displayAddConfirmation(Medicine med) {
        System.out.println("\nMedicine successfully added!\n");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------\n");
    }

    public int confirmAddMedicine(Medicine med) {
        System.out.println("\n--- Confirm New Medicine ---");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------");
        System.out.println("1. Confirm and save");
        System.out.println("2. Edit details");
        System.out.println("3. Discard");

        int choice;
        while (true) {
            System.out.print("Enter choice (1-3): ");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        MessageUI.clearConsole();
                        System.out.println("Details confirmed. Saved.");
                        return 1;
                    case 2:
                        MessageUI.clearConsole();
                        return 2;
                    case 3:
                        MessageUI.clearConsole();
                        System.out.println("Medicine discarded. Nothing will be saved.");
                        return 3;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.\n");
            }
        }
    }

    public Medicine editMedicineDetails(Medicine med) {
        DateTimeFormatter fmtStrict = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate today = LocalDate.now();

        // Make a copy of the original details to restore if changes are discarded
        Medicine original = new Medicine(
                med.getMedId(),
                med.getName(),
                med.getStock(),
                med.getPrice(),
                med.getExpiryDate()
        );

        while (true) {
            System.out.println("\n--- Edit Medicine Details " + med.getMedId() + " ---");
            System.out.println("1. Name           : " + med.getName());
            System.out.println("2. Quantity       : " + med.getStock());
            System.out.println("3. Price per unit : RM " + med.getPrice());
            System.out.println("4. Expiry Date    : " + med.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MMM-uuuu")));
            System.out.println("5. Done editing");
            System.out.println("0. Discard changes");

            System.out.print("Enter choice (0-5): ");
            String input = scanner.nextLine().trim();

            if ("0".equals(input)) {
                // Restore original details
                med.setName(original.getName());
                med.setStock(original.getStock());
                med.setPrice(original.getPrice());
                med.setExpiryDate(original.getExpiryDate());
                MessageUI.clearConsole();
                System.out.println("Changes discarded.");
                return med;
            }

            switch (input) {
                case "1": {
                    ListInterface<String> refreshed = medicineNamesDAO.retrieveFromFile();
                    System.out.println("\nSelect medicine name (0 to Back):");
                    displayMedicineNames(refreshed);

                    while (true) {
                        System.out.print("Enter choice (0-" + refreshed.getNumberOfEntries() + "): ");
                        try {
                            int ch = Integer.parseInt(scanner.nextLine().trim());
                            if (ch == 0) {
                                MessageUI.clearConsole();
                                break;
                            }
                            if (ch >= 1 && ch <= refreshed.getNumberOfEntries()) {
                                med.setName(refreshed.getEntry(ch));
                                MessageUI.clearConsole();
                                break;
                            }
                            System.out.println("Invalid choice. Try again.\n");
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid number.\n");
                        }
                    }
                    break;
                }

                case "2": {
                    while (true) {
                        System.out.print("\nEnter new quantity (0 to Back): ");
                        String s = scanner.nextLine().trim();
                        MessageUI.clearConsole();
                        if ("0".equals(s)) {
                            break;
                        }
                        try {
                            int qty = Integer.parseInt(s);
                            if (qty > 0) {
                                med.setStock(qty);
                                break;
                            }
                            System.out.println("Quantity must be greater than 0.\n");
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid number.\n");
                        }
                    }
                    break;
                }

                case "3": {
                    while (true) {
                        System.out.print("\nEnter new price per unit (RM) (0 to Back): ");
                        String s = scanner.nextLine().trim();
                        MessageUI.clearConsole();
                        if ("0".equals(s)) {
                            break;
                        }
                        try {
                            double p = Double.parseDouble(s);
                            if (p >= 0) {
                                med.setPrice(p);
                                break;
                            }
                            System.out.println("Price cannot be negative.\n");
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid number.\n");
                        }
                    }
                    break;
                }

                case "4": {
                    while (true) {
                        System.out.print("\nEnter new expiry date (DD-MM-YYYY) (0 to Back): ");
                        String ds = scanner.nextLine().trim();
                        MessageUI.clearConsole();
                        if ("0".equals(ds)) {
                            break;
                        }
                        try {
                            LocalDate d = LocalDate.parse(ds, fmtStrict);
                            if (d.isAfter(today)) {
                                med.setExpiryDate(d);
                                break;
                            }
                            System.out.println("Expiry must be AFTER today (" + today.format(fmtStrict) + ").\n");
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date. Use format DD-MM-YYYY.\n");
                        }
                    }
                    break;
                }

                case "5":
                    MessageUI.clearConsole();
                    return med;

                default:
                    System.out.println("Invalid choice. Try again.\n");
            }
        }
    }

    public int confirmRemoveMedicine(Medicine med) {
        System.out.println("\n--- Remove Medicine ---");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------");
        System.out.println("1. Confirm removal");
        System.out.println("0. Cancel removal");

        int choice;
        while (true) {
            System.out.print("Enter choice (0-1): ");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        return 1;
                    case 0:
                        return 0;
                    default:
                        System.out.println("Invalid choice. Please enter 0 or 1.\n");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.\n");
            }
        }
    }

    public void displayMedicineInventory(String allMedicines) {
        System.out.println("\n--- Current Medicine Inventory ---\n");
        if (allMedicines == null || allMedicines.trim().isEmpty()) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(allMedicines);
        }
    }

    // Add in PharmacyUI
    public int getInventoryReportChoice() {
        System.out.println("\n=== Inventory Report ===");
        System.out.println("1. KPI Snapshot Report");
        System.out.println("2. Low-stock section (choose threshold)");
        System.out.println("3. Near-expiry section (choose days)");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");
        while (true) {
            try {
                int c = Integer.parseInt(scanner.nextLine().trim());
                if (c >= 0 && c <= 3) {
                    return c;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter 0-3: ");
        }
    }

    public void displayExportResult(String path) {
        System.out.println("\nReport exported to: " + path + "\n");
    }

    public int inputLowStockThreshold() {
        System.out.print("Enter low-stock threshold (0 to Back): ");
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int t = Integer.parseInt(s);
                if (t == 0) {
                    return -1; // signal Back
                }
                if (t > 0) {
                    return t;
                }
                System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
            } catch (NumberFormatException ignore) {
                System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
            }
        }
    }

    public int inputNearExpiryDays() {
        System.out.print("Enter days from today (0 to Back): ");
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int d = Integer.parseInt(s);
                if (d == 0) {
                    return -1; // signal Back
                }
                if (d > 0) {
                    return d;
                }
                System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
            } catch (NumberFormatException ignore) {
                System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
            }
        }
    }

    public int getDispensedReportChoice() {
        System.out.println("\n=== Dispensed Medicines Report ===");
        System.out.println("1. All time (detail)");
        System.out.println("2. Today (summary by medicine)");
        System.out.println("3. Last 7 days (summary by medicine)");
        System.out.println("4. Custom date range (summary by medicine)");
        System.out.println("5. Custom date range (summary by patient)");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        while (true) {
            try {
                int c = Integer.parseInt(scanner.nextLine().trim());
                if (c >= 0 && c <= 5) {
                    return c;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter 0-5: ");
        }
    }

    public LocalDate inputDate(String label) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu");
        while (true) {
            System.out.print(label + " (DD-MM-YYYY, 0 to Back): ");
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                return null; // Back
            }
            try {
                return LocalDate.parse(s, fmt);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Try again (DD-MM-YYYY) or 0 to Back.");
            }
        }
    }

    // Generic printers that take pre-formatted text
    public void displayReport(String title, String body) {
        System.out.println("\n=== " + title + " ===\n");
        if (body == null || body.trim().isEmpty()) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(body);
        }
    }

    public String inputMedicineID() {
        System.out.print("Enter Medicine ID (0 to Back): ");
        String id = scanner.nextLine().trim();
        return "0".equals(id) ? null : id;
    }

    public String inputMedicineName() {
        System.out.print("Enter Medicine Name: ");
        return scanner.nextLine();
    }

    public int inputMedicineQuantity() {
        System.out.print("Enter Stock Quantity: ");
        int qty = scanner.nextInt();
        scanner.nextLine();  // consume newline
        return qty;
    }

    public int inputDispenseQuantity() {
        System.out.print("Enter quantity to dispense: ");
        int qty = scanner.nextInt();
        scanner.nextLine();  // consume newline
        return qty;
    }

    public int inputStockThreshold() {
        System.out.print("Enter stock threshold: ");
        int threshold = scanner.nextInt();
        scanner.nextLine();  // consume newline
        return threshold;
    }

    public void displayMedicineList(String formattedList) {
        System.out.println("Medicine Inventory:\n");
        if (formattedList == null || formattedList.trim().isEmpty()) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(formattedList);
        }
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public int inputPatientSelection() {
        System.out.print("Enter patient number to dispense (0 to Back): ");
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n >= 0) {
                    return n; // 0 = Back
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter a whole number (0 to Back): ");
        }
    }

    public void displayTable(String title, String[] headers, String[][] rows) {
        TablePrinter.printTable(title, headers, rows);
    }

    // In PharmacyUI
    public int getPostReportAction() {
        System.out.println("\n--- What would you like to do? ---");
        System.out.println("1. Export to file");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        while (true) {
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                return 0;
            }
            if ("1".equals(s)) {
                return 1;
            }
            System.out.print("Please enter 0 or 1: ");
        }
    }

}
