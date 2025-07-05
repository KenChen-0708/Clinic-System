/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import control.SummaryReport;
import java.util.Scanner;

public class MainUI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n====== Clinic Management System ======");
            System.out.println("1. Patient Management");
            System.out.println("2. Doctor Management");
            System.out.println("3. Treatment Management");
            System.out.println("4. Pharmacy Management");
            System.out.println("5. Generate Summary Report");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // flush newline

            switch (choice) {
                case 1:
                    PatientUI patientUI = new PatientUI();
                    patientUI.run();
                    break;
                case 2:
                    DoctorUI doctorUI = new DoctorUI();
                    doctorUI.run();
                    break;
                case 3:
                    TreatmentUI treatmentUI = new TreatmentUI();
                    treatmentUI.run();
                    break;
                case 4:
                    PharmacyUI pharmacyUI = new PharmacyUI();
                    pharmacyUI.run();
                    break;
                case 5:
                    SummaryReport report = new SummaryReport();
                    report.generate();
                    break;

                case 0:
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select again.");
            }

        } while (choice != 0);

        scanner.close();
    }
}
