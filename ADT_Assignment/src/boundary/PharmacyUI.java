package boundary;

import entity.Medicine;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

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
        scanner.nextLine();  // consume newline
        System.out.println();
        return choice;
    }

    public Medicine inputMedicineDetails(String generatedId) {
        System.out.println("\n--- Add New Medicine ---");
        System.out.print("Enter medicine name               : ");
        String name = scanner.nextLine();
        System.out.print("Enter quantity                    : ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter price per unit (RM)         : ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter expiry date (DD-MM-YYYY)    : ");
        String expiryStr = scanner.nextLine();

        LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return new Medicine(generatedId, name, quantity, price, expiry);
    }

    public void displayAddConfirmation(Medicine med) {
        System.out.println("\nMedicine successfully added!\n");
        System.out.println(med.toString());
        System.out.println("--------------------------------------------");
    }

    public void displayMedicineInventory(String allMedicines) {
        System.out.println("\n--- Current Medicine Inventory ---\n");
        System.out.println(allMedicines);
        System.out.println("--------------------------------------------");
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
        System.out.println("Medicine Inventory:\n" + formattedList);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public int inputPatientSelection() {
        System.out.print("Enter patient number to dispense: ");
        return scanner.nextInt();
    }

}
