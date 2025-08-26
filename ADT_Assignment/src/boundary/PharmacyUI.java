package boundary;

import adt.ListInterface;
import dao.MedicineNamesDAO;
import entity.Medicine;
import utility.MessageUI;
import utility.TablePrinter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/**
 * # PharmacyUI Console-based user interface for the Pharmacy module.
 *
 * Responsibilities: - Show menus and collect inputs safely (with validation &
 * "0 to Back" patterns). - Display tables/reports using TablePrinter. -
 * Delegate data changes to DAO classes (no business logic here).
 *
 * Design choices: - Centralized input helpers to avoid duplicated
 * parsing/validation code. - Clear sections and short, descriptive methods to
 * make live demos easier. - Comments explain *why* not only *what* to help
 * during presentation.
 */
public class PharmacyUI {

    /* =========================================================
     * Fields & Data Access
     * ========================================================= */
    private final Scanner scanner = new Scanner(System.in);

    private final MedicineNamesDAO medicineNamesDAO = new MedicineNamesDAO();
    private ListInterface<String> medicineNames; // loaded at construction

    /* =========================================================
     * Construction
     * ========================================================= */
    public PharmacyUI() {
        // Load the list of medicine names when the UI is created
        medicineNames = medicineNamesDAO.retrieveFromFile();
    }

    /* =========================================================
     * Top-Level Menus
     * ========================================================= */
    /**
     * Main Pharmacy menu (Pharmacy Management Menu).
     *
     * @return user choice 0..5 (already validated)
     */
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

        // Uses a simple readInt helper + range check
        int choice = readIntAllowAny(); // allow any int; caller validates range
        System.out.println();
        return choice;
    }

    /**
     * Inventory sub-menu (safe loop with validation).
     */
    public int getInventoryManagementChoice() {
        System.out.println("============================================");
        System.out.println("     PHARMACY INVENTORY MANAGEMENT MENU");
        System.out.println("============================================");
        System.out.println("1. View Medicine Stock");
        System.out.println("2. Add New Medicine Stock");
        System.out.println("3. Edit Medicine Stock");
        System.out.println("4. Remove Medicine Stock");
        System.out.println("5. Search Medicine"); // <-- NEW
        System.out.println("0. Back to PHARMACY Menu");
        System.out.println("--------------------------------------------");
        System.out.print("Enter your choice: ");

        // expand to 0..5
        return readIntInRangeLoop(0, 5, "Please enter 0-5: ");
    }

    /**
     * Manage medicine names (Add/Edit/Remove) loop. Keeps returning to the list
     * after each operation.
     */
    public void runManageMedicineNames() {
        int choice;
        do {
            // Header
            System.out.println("================================");
            System.out.println("     MANAGE MEDICINE NAMES");
            System.out.println("================================");

            // Show current names
            displayMedicineNames(medicineNames);

            // Options
            System.out.println("\nOptions:");
            System.out.println("1. Add New Medicine Name");
            System.out.println("2. Edit Medicine Name");
            System.out.println("3. Remove Medicine Name");
            System.out.println("0. Back to PHARMACY Menu");
            System.out.println("--------------------------------");
            System.out.print("Enter your choice: ");

            choice = readIntAllowAny();

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

    /* =========================================================
     * Inventory: Sort/Group Menus
     * ========================================================= */
    /**
     * Sort/group selection for inventory table.
     */
    public int getSortChoice() {
        System.out.println("\n--- Sort / Group Inventory ---");
        System.out.println("1. Sort by ID");
        System.out.println("2. Sort by Expiry Date");
        System.out.println("3. Sort by Price");
        System.out.println("4. Group by Medicine Name (Total Quantity)");
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");

        return readIntInRangeLoop(0, 4, "Please enter 0-4: ");
    }

    /**
     * Generic direction picker for a given sort.
     *
     * @param title e.g. "by ID" / "by Price"
     * @param ascLabel label for ascending option
     * @param descLabel label for descending option
     * @return 0=Back, 1=asc, 2=desc
     */
    public int getSortDirection(String title, String ascLabel, String descLabel) {
        MessageUI.clearConsole();

        System.out.println("\nSort " + title + " order:");
        System.out.println("1. " + ascLabel);
        System.out.println("2. " + descLabel);
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");

        // Only allow 0..2
        return readIntInRangeLoop(0, 2, "Please enter 0-2: ");
    }

    /* =========================================================
     * Inventory: Add/Edit/Remove UI/Search
     * ========================================================= */
    /**
     * Guided input for creating a new Medicine object. - Follows the same "0 to
     * Back" pattern at each step. - Validates name, quantity, price, and
     * expiry.
     *
     * @param generatedId ID supplied by controller (e.g. M0001)
     */
    public Medicine inputMedicineDetails(String generatedId) {
        System.out.println("\n== Add New Inventory (0 to back) ==");

        displayMedicineNames(medicineNames);

        final int PROMPT_WIDTH = 35;

        // 1) Choose Name from list
        int choice;
        while (true) {
            System.out.printf("\n%-" + PROMPT_WIDTH + "s: ", "Select medicine name");
            String raw = scanner.nextLine().trim();

            if ("0".equals(raw)) {
                cancel("Canceled. No medicine added.");
                return null;
            }
            try {
                choice = Integer.parseInt(raw);
                if (choice >= 1 && choice <= medicineNames.getNumberOfEntries()) {
                    break;
                }
                System.out.println("Invalid choice. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        String name = medicineNames.getEntry(choice);

        // 2) Quantity (>0)
        int quantity;
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter quantity");
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                cancel("Canceled. No medicine added.");
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

        // 3) Price (>=0)
        double price;
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter price per unit");
            String s = scanner.nextLine().trim();
            if ("0".equals(s)) {
                cancel("Canceled. No medicine added.");
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

        // 4) Expiry (strict dd-MM-uuuu and must be after today)
        LocalDate expiry;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        LocalDate today = LocalDate.now();
        while (true) {
            System.out.printf("%-" + PROMPT_WIDTH + "s: ", "Enter expiry date (DD-MM-YYYY)");
            String expiryStr = scanner.nextLine().trim();
            if ("0".equals(expiryStr)) {
                cancel("Canceled. No medicine added.");
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

    /**
     * Confirmation step for adding a medicine.
     *
     * @return 1=Confirm, 2=Edit, 3=Discard
     */
    public int confirmAddMedicine(Medicine med) {
        System.out.println("\n--- Confirm New Medicine ---");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------");
        System.out.println("1. Confirm and save");
        System.out.println("2. Edit details");
        System.out.println("0. Discard");
        System.out.println("--------------------------------");
        while (true) {
            System.out.print("Enter choice (0-2): ");
            int choice = readIntAllowAnySafe();

            switch (choice) {
                case 1:
                    MessageUI.clearConsole();
                    System.out.println("Details confirmed. Saved.");
                    return 1;
                case 2:
                    MessageUI.clearConsole();
                    return 2;
                case 0: // user picks Discard with 0
                    MessageUI.clearConsole();
                    System.out.println("Medicine discarded. Nothing will be saved.");
                    return 0;
                default:
                    System.out.println("Invalid choice. Please enter 0, 1, or 2.\n");
            }
        }
    }

    /**
     * Guided editor for a Medicine (in-place update). Follows "0 to Back" in
     * each field editor.
     */
    public Medicine editMedicineDetails(Medicine med) {
        DateTimeFormatter fmtStrict = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate today = LocalDate.now();

        // Backup so we can rollback if the user discards from the main menu.
        Medicine original = new Medicine(
                med.getMedId(), med.getName(), med.getStock(), med.getPrice(), med.getExpiryDate()
        );

        while (true) {
            System.out.println("\n--- Edit Medicine Details " + med.getMedId() + " ---");
            System.out.println("1. Name           : " + med.getName());
            System.out.println("2. Quantity       : " + med.getStock());
            System.out.println("3. Price per unit : RM " + med.getPrice());
            System.out.println("4. Expiry Date    : " + med.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MMM-uuuu")));
            System.out.println("--------------------------------------------");
            System.out.println("5. Done editing");
            System.out.println("0. Discard changes");
            System.out.println("--------------------------------");
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
                case "1": { // Change name from master list
                    ListInterface<String> refreshed = medicineNamesDAO.retrieveFromFile();
                    System.out.println("\nSelect medicine name (0 to Back):");
                    displayMedicineNames(refreshed);

                    while (true) {
                        System.out.print("Enter choice (0-" + refreshed.getNumberOfEntries() + "): ");
                        int ch = readIntAllowAnySafe();
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
                    }
                    break;
                }

                case "2": { // Change quantity (>0)
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

                case "3": { // Change price (>=0)
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

                case "4": { // Change expiry (must be after today)
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

    /**
     * Removal confirmation dialog.
     *
     * @return 1=Confirm, 0=Cancel
     */
    public int confirmRemoveMedicine(Medicine med) {
        System.out.println("\n--- Remove Medicine ---");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------");
        System.out.println("1. Confirm removal");
        System.out.println("0. Cancel removal");
        System.out.println("--------------------------------");

        while (true) {
            System.out.print("Enter choice (0-1): ");
            int choice = readIntAllowAnySafe();
            switch (choice) {
                case 1:
                    return 1;
                case 0:
                    return 0;
                default:
                    System.out.println("Invalid choice. Please enter 0 or 1.\n");
            }
        }
    }

    /**
     * Search mode: by ID or by Name (contains).
     */
    public int getSearchChoice() {
        System.out.println("\n--- Search Medicines ---");
        System.out.println("1. By ID");
        System.out.println("2. By Name");
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");
        return readIntInRangeLoop(0, 2, "Please enter 0-2: ");
    }

    public int confirmDispenseAll() {
        System.out.println("1. Dispense all medicines");
        System.out.println("0. Discard");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice (0-1): ");
        return readIntInRangeLoop(0, 1, "Please enter 0 or 1: ");
    }


    /* =========================================================
     * Reporting Menus & Printers
     * ========================================================= */
    /**
     * Inventory reports menu (KPI / low-stock / near-expiry).
     */
    public int getInventoryReportChoice() {
        System.out.println("\n=== Inventory Report ===");
        System.out.println("1. KPI Snapshot Report");
        System.out.println("2. Low-stock section (choose threshold)");
        System.out.println("3. Near-expiry section (choose days)");
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");

        return readIntInRangeLoop(0, 3, "Please enter 0-3: ");
    }

    /**
     * Dispensed-medicines reports menu (detail & summaries).
     */
    public int getDispensedReportChoice() {
        System.out.println("\n=== Dispensed Medicines Report ===");
        System.out.println("1. Detail Report");
        System.out.println("2. By Medicine");
        System.out.println("3. By Patient");
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter choice: ");

        return readIntInRangeLoop(0, 3, "Please enter 0-3: ");
    }

    public int getDateRangeChoice() {
        System.out.println("\nSelect the time period for the report:");
        System.out.println("1. All");
        System.out.println("2. Today");
        System.out.println("3. This Week");
        System.out.println("4. Last 30 Days");
        System.out.println("5. Custom Range");
        System.out.println("0. Back");
        System.out.println("--------------------------------");
        System.out.print("Enter choice: ");

        return readIntInRangeLoop(0, 5, "Please enter 0-5: ");
    }

    /**
     * Post-report action (export / back).
     */
    public int getPostReportAction() {
        System.out.println("\n--- What would you like to do? ---");
        System.out.println("1. Export to TXT");
        System.out.println("2. Export to CSV");
        System.out.println("0. Back");
        System.out.println("----------------------------------");
        System.out.print("Enter choice: ");

        while (true) {
            String s = scanner.nextLine().trim();
            switch (s) {
                case "0":
                    return 0;
                case "1":
                    return 1;
                case "2":
                    return 2;
                default:
                    System.out.print("Please enter 0, 1 or 2: ");
            }
        }
    }

    /**
     * Table wrapper — delegates to TablePrinter.
     */
    public void displayTable(String title, String[] headers, String[][] rows) {
        TablePrinter.printTable(title, headers, rows);
    }

    /**
     * Generic report printer (for pre-formatted bodies).
     */
    public void displayReport(String body) {
        if (isBlank(body)) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(body);
        }
    }

    /**
     * Export result acknowledgement.
     */
    public void displayExportResult(String path) {
        System.out.println("\nReport exported to: " + path + "\n");
    }

    /* =========================================================
     * Simple Inputs (IDs, names, thresholds)
     * ========================================================= */
    /**
     * Read a Medicine ID as digits only; auto-prefixes 'M' and zero-pads to 4.
     * Examples user can type: 7, 007, 1234, M9, m0042. Enter exactly "0" to
     * Back.
     */
    public String inputMedicineID() {
        // Visual hint that M is auto-applied
        System.out.print("Enter Medicine ID number (0 to Back): M");

        String typed = scanner.nextLine().trim();

        // Back (only if the user literally typed 0 after the printed 'M')
        if ("0".equals(typed)) {
            return null;
        }

        // Allow accidental 'M' or 'm' (because we printed M)
        String digits = typed.replaceFirst("^[Mm]", "").trim();

        // Keep only digits; reject if nothing left
        digits = digits.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null; // treat invalid as Back
        }
        // Limit to 4 (your IDs are M####). If you need >4, adjust here.
        int num = Integer.parseInt(digits);
        if (num <= 0) {
            return null; // 0 is Back; negative not allowed
        }
        return String.format("M%04d", num);
    }

    /**
     * Name pattern input for search; returns null on Back.
     */
    public String inputNamePattern() {
        System.out.print("Enter medicine name or part (0 to Back): ");
        String s = scanner.nextLine().trim();
        return "0".equals(s) ? null : s;
    }

    /**
     * Patient selection for dispensing.
     *
     * @return selected patient number (0 = Back)
     */
    public int inputPatientSelection() {
        System.out.print("Enter patient number to dispense (0 to Back): ");
        return readIntInRangeLoop(0, Integer.MAX_VALUE, "Please enter a whole number (0 to Back): ");
    }

    public String inputMedicineName() {
        System.out.print("Enter Medicine Name: ");
        return scanner.nextLine();
    }

    public int inputMedicineQuantity() {
        System.out.print("Enter Stock Quantity: ");
        int qty = readIntAllowAnySafe();
        return qty;
    }

    public int inputDispenseQuantity() {
        System.out.print("Enter quantity to dispense: ");
        return readIntAllowAnySafe();
    }

    public int inputStockThreshold() {
        System.out.print("Enter stock threshold: ");
        return readIntAllowAnySafe();
    }

    /**
     * Low-stock threshold, using 0 as "Back". Returns -1 if Back.
     */
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
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
        }
    }

    /**
     * Near-expiry days, using 0 as "Back". Returns -1 if Back.
     */
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
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter a whole number (> 0) or 0 to Back: ");
        }
    }

    /**
     * Prompt for a date (controller decides the label). Returns null on Back.
     */
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

    /* =========================================================
     * Display Helpers
     * ========================================================= */
    /**
     * Prints a numbered list of medicine names or a friendly message if empty.
     */
    private void displayMedicineNames(ListInterface<String> names) {
        if (names == null || names.isEmpty()) {
            System.out.println("No medicines available.");
            return;
        }
        for (int i = 1; i <= names.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + names.getEntry(i));
        }
    }

    public ListInterface<String> getMedicineNames() {
        return medicineNames;  // This exposes the list of medicine names
    }

    /**
     * Inventory text dumper (used by some controllers).
     */
    public void displayMedicineInventory(String allMedicines) {
        System.out.println("\n--- Current Medicine Inventory ---\n");
        if (isBlank(allMedicines)) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(allMedicines);
        }
    }

    public void displayMedicineList(String formattedList) {
        System.out.println("Medicine Inventory:\n");
        if (isBlank(formattedList)) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(formattedList);
        }
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    /* =========================================================
     * Medicine Names CRUD (UI layer only; DAO does the saving)
     * ========================================================= */
    /**
     * Add a new name (letters + spaces only).
     */
    private void addMedicineName() {
        while (true) {
            System.out.print("\nEnter the name of the new medicine (0 to Back): ");
            String newMedicineName = scanner.nextLine().trim();

            if ("0".equals(newMedicineName)) {
                cancel("Operation cancelled.");
                return;
            }
            if (newMedicineName.isEmpty()) {
                System.out.print("\nMedicine name cannot be empty. Please try again.");
                continue;
            }
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

    /**
     * Edit an existing name by index.
     */
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
            cancel("Operation cancelled.");
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
                cancel("Operation cancelled.");
                return;
            }
            if (newMedicineName.isEmpty()) {
                System.out.print("\nMedicine name cannot be empty. Please try again.");
                continue;
            }
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

    /**
     * Remove a name by index (with confirmation).
     */
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
            cancel("Operation cancelled.");
            return;
        }
        if (index < 1 || index > medicineNames.getNumberOfEntries()) {
            System.out.println("Invalid medicine number.");
            return;
        }

        String selectedMedicine = medicineNames.getEntry(index);

        // Confirmation loop
        while (true) {
            System.out.println("\nYou have selected: " + "[" + selectedMedicine + "]");
            System.out.println("1. Confirm removal");
            System.out.println("0. Cancel removal");
            System.out.println("--------------------------------");
            System.out.print("Enter choice (0-1): ");

            String input = scanner.nextLine().trim();

            if ("0".equals(input)) {
                cancel("Operation cancelled.");
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

    /* =========================================================
     * Low-level Input Helpers (keep UI code DRY & readable)
     * ========================================================= */
    /**
     * Reads any integer (no validation); if invalid, re-prompts.
     */
    private int readIntAllowAnySafe() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Reads an int without re-prompting (used where caller does its own
     * validation).
     */
    private int readIntAllowAny() {
        int n = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return n;
        // Note: only used in a couple of places mirroring your original behavior.
    }

    /**
     * Loop until user enters an int within [min..max]. Shows the given prompt
     * again for invalid input.
     */
    private int readIntInRangeLoop(int min, int max, String retryPrompt) {
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(s);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print(retryPrompt);
        }
    }

    /* =========================================================
     * Tiny Utility Helpers
     * ========================================================= */
    /**
     * Friendly cancel pattern with clear screen + message.
     */
    private void cancel(String msg) {
        MessageUI.clearConsole();
        System.out.println(msg);
    }

    /**
     * Null/blank checker to keep prints clean.
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
