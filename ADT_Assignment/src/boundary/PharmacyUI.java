package boundary;

import entity.Medicine;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import utility.MessageUI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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
                    System.out.println("Canceled. No medicine added.");
                    return null; // exit early
                }
                if (choice >= 1 && choice <= medicineNames.length) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.\n");
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
                    System.out.println("Expiry must be AFTER today (" + today.format(fmt) + "). Try again.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use format DD-MM-YYYY (e.g., 05-11-2026).");
            }
        }

        return new Medicine(generatedId, name, quantity, price, expiry);
    }

    public void displayAddConfirmation(Medicine med) {
        System.out.println("\nMedicine successfully added!\n");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------\n");
    }

    public void displayMedicineInventory(String allMedicines) {
        System.out.println("\n--- Current Medicine Inventory ---\n");
        if (allMedicines == null || allMedicines.trim().isEmpty()) {
            MessageUI.displayEmptyMessage();
        } else {
            System.out.println(allMedicines);
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

}
