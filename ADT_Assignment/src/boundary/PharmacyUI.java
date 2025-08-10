package boundary;

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

    public int getMenuChoice() {
        System.out.println("================================");
        System.out.println("   PHARMACY MANAGEMENT MENU");
        System.out.println("================================");
        System.out.println("1. View Medicine Inventory");
        System.out.println("2. Add New Medicine Stock");
        System.out.println("3. Dispense Medicine to Patient");
        System.out.println("4. Generate Inventory Report");
        System.out.println("5. Generate Dispensed Medicines Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("--------------------------------");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println();
        return choice;
    }

    public int getViewChoice() {
        System.out.println("\n--- View / Sort / Group Inventory ---");
        System.out.println("1. View by ID");
        System.out.println("2. View by Expiry Date");
        System.out.println("3. View by Price");
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
        System.out.println("\nSort " + title + " order:");
        System.out.println("1. " + ascLabel);
        System.out.println("2. " + descLabel);
        System.out.print("Enter your choice: ");
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(s);
                if (choice == 1 || choice == 2) {
                    return choice;
                }
                System.out.print("Please enter 1 or 2: ");
            } catch (NumberFormatException e) {
                System.out.print("Please enter 1 or 2: ");
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
        // Predefined list of medicine names
        String[] medicineNames = {
            "Paracetamol",
            "Ibuprofen",
            "Amoxicillin",
            "Cetirizine",
            "Vitamin C",
            "Loratadine",
            "Aspirin",
            "Omeprazole",
            "Metformin",
            "Atorvastatin",
            "Losartan",
            "Amlodipine",
            "Hydrochlorothiazide",
            "Azithromycin",
            "Diclofenac"
        };

        System.out.println("\n--- Add New Medicine ---");
        System.out.println("Select medicine name (0. Exit / Cancel):");
        for (int i = 0; i < medicineNames.length; i++) {
            System.out.printf("%d. %s%n", i + 1, medicineNames[i]);
        }

        int choice;
        while (true) {
            System.out.print("\nEnter choice (1-" + medicineNames.length + ")               : ");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    MessageUI.clearConsole();
                    System.out.println("Canceled. No medicine added.");
                    return null; // exit early
                }
                if (choice >= 1 && choice <= medicineNames.length) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        String name = medicineNames[choice - 1];

        int quantity;
        while (true) {
            System.out.print("Enter quantity                    : ");
            try {
                quantity = Integer.parseInt(scanner.nextLine().trim());
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
            System.out.print("Enter price per unit (RM)         : ");
            try {
                price = Double.parseDouble(scanner.nextLine().trim());
                if (price >= 0) {
                    break;
                }
                System.out.println("Price cannot be negative.\n");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (e.g., 12.50).\n");
            }
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate today = LocalDate.now();
        LocalDate expiry;

        while (true) {
            System.out.print("Enter expiry date (DD-MM-YYYY)    : ");
            String expiryStr = scanner.nextLine().trim();
            try {
                expiry = LocalDate.parse(expiryStr, fmt);
                if (expiry.isAfter(today)) {
                    break;
                } else {
                    System.out.println("Expiry must be AFTER today (" + today.format(fmt) + "). Try again.\n");
                }
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
                if (choice == 1) {
                    MessageUI.clearConsole();
                    System.out.println("Details confirmed. Saved.");
                    return 1;
                } else if (choice == 2) {
                    MessageUI.clearConsole();
                    return 2;
                } else if (choice == 3) {
                    MessageUI.clearConsole();
                    System.out.println("Medicine discarded. Nothing will be saved.");
                    return 3;
                } else {
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
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

        while (true) {
            System.out.println("\n--- Edit Medicine Details ---");
            System.out.println("1. Name           : " + med.getName());
            System.out.println("2. Quantity       : " + med.getStock());
            System.out.println("3. Price per unit : RM " + med.getPrice());
            System.out.println("4. Expiry Date    : " + med.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MMM-uuuu")));
            System.out.println("5. Done editing");

            System.out.print("Enter choice (1-5): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": {
                    String[] medicineNames = {
                        "Paracetamol", "Ibuprofen", "Amoxicillin", "Cetirizine", "Vitamin C",
                        "Loratadine", "Aspirin", "Omeprazole", "Metformin", "Atorvastatin",
                        "Losartan", "Amlodipine", "Hydrochlorothiazide", "Azithromycin", "Diclofenac"
                    };
                    System.out.println("\nSelect medicine name:");
                    for (int i = 0; i < medicineNames.length; i++) {
                        System.out.printf("%d. %s%n", i + 1, medicineNames[i]);
                    }
                    while (true) {
                        System.out.print("Enter choice (1-" + medicineNames.length + "): ");
                        try {
                            int ch = Integer.parseInt(scanner.nextLine().trim());
                            if (ch >= 1 && ch <= medicineNames.length) {
                                med.setName(medicineNames[ch - 1]);
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
                        System.out.print("\nEnter new quantity: ");
                        try {
                            int qty = Integer.parseInt(scanner.nextLine().trim());
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
                        System.out.print("\nEnter new price per unit (RM): ");
                        try {
                            double p = Double.parseDouble(scanner.nextLine().trim());
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
                        System.out.print("\nEnter new expiry date (DD-MM-YYYY): ");
                        String ds = scanner.nextLine().trim();
                        try {
                            LocalDate d = LocalDate.parse(ds, fmtStrict);
                            if (d.isAfter(today)) {
                                med.setExpiryDate(d);
                                break;
                            } else {
                                System.out.println("Expiry must be AFTER today (" + today.format(fmtStrict) + ").\n");
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date. Use format DD-MM-YYYY.\n");
                        }
                    }
                    break;
                }

                case "5":
                    return med;

                default:
                    System.out.println("Invalid choice. Try again.\n");
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
        System.out.println("1. Snapshot (KPIs + By ID + Totals)");
        System.out.println("2. Low-stock section (choose threshold)");
        System.out.println("3. Near-expiry section (choose days)");
        System.out.println("4. Snapshot + Export to file");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");
        while (true) {
            try {
                int c = Integer.parseInt(scanner.nextLine().trim());
                if (c >= 0 && c <= 4) {
                    return c;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter 0-4: ");
        }
    }

    public void displayExportResult(String path) {
        System.out.println("\nReport exported to: " + path + "\n");
    }

    public int inputLowStockThreshold() {
        System.out.print("Enter low-stock threshold: ");
        while (true) {
            try {
                int t = Integer.parseInt(scanner.nextLine().trim());
                if (t >= 0) {
                    return t;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter a whole number (>= 0): ");
        }
    }

    public int inputNearExpiryDays() {
        System.out.print("Enter days from today (e.g., 30): ");
        while (true) {
            try {
                int d = Integer.parseInt(scanner.nextLine().trim());
                if (d >= 0) {
                    return d;
                }
            } catch (NumberFormatException ignore) {
            }
            System.out.print("Please enter a whole number (>= 0): ");
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
            System.out.print(label + " (DD-MM-YYYY): ");
            String s = scanner.nextLine().trim();
            try {
                return LocalDate.parse(s, fmt);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Try again (DD-MM-YYYY).");
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
        System.out.print("Enter Medicine ID: ");
        return scanner.nextLine();
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
        System.out.print("Enter patient number to dispense: ");
        return scanner.nextInt();
    }

    public void displayTable(String title, String[] headers, String[][] rows) {
        TablePrinter.printTable(title, headers, rows);
    }
}
