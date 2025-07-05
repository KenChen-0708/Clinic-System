/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import control.MaintainPharmacy;
import entity.Medicine;
import java.util.Scanner;

public class PharmacyUI {
    private MaintainPharmacy maintainPharmacy = new MaintainPharmacy();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        int choice;
        do {
            System.out.println("\n--- Pharmacy Management ---");
            System.out.println("1. Add Medicine");
            System.out.println("2. Dispense Medicine");
            System.out.println("3. View All Medicines");
            System.out.println("4. Summary - Low Stock Medicines");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1: addMedicine(); break;
                case 2: dispenseMedicine(); break;
                case 3: maintainPharmacy.viewAllMedicines(); break;
                case 4: summaryLowStock(); break;
                case 0: System.out.println("Back to Main Menu"); break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 0);
    }

    private void addMedicine() {
        System.out.print("Enter Medicine ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Medicine Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Stock Quantity: ");
        int stock = scanner.nextInt(); scanner.nextLine();
        maintainPharmacy.addMedicine(new Medicine(id, name, stock));
    }

    private void dispenseMedicine() {
        System.out.print("Enter Medicine ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter quantity to dispense: ");
        int qty = scanner.nextInt(); scanner.nextLine();
        maintainPharmacy.dispenseMedicine(id, qty);
    }

    private void summaryLowStock() {
        System.out.print("Enter stock threshold: ");
        int threshold = scanner.nextInt(); scanner.nextLine();
        maintainPharmacy.summaryLowStock(threshold);
    }

    public static void main(String[] args) {
        new PharmacyUI().run();
    }
}
