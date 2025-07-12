package boundary;

import control.MaintainPatient;
import control.MaintainDoctor;
import control.MaintainTreatment;
import control.MaintainPharmacy;
import control.SummaryReport;
import utility.MessageUI;

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
            while (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                scanner.next(); // discard invalid input
                System.out.print("Enter your choice: ");
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // flush newline

            switch (choice) {
                case 1:
                    new MaintainPatient().runPatientMenu();
                    break;
                case 2:
                    new MaintainDoctor().runDoctorMenu();
                    break;
                case 3:
                    new MaintainTreatment().runTreatmentMenu();
                    break;
                case 4:
                    new MaintainPharmacy().runPharmacyMenu();
                    break;
                case 5:
                    new SummaryReport().generate();
                    break;
                case 0:
                    MessageUI.displayExitMessage();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }

        } while (choice != 0);

        scanner.close();
    }
}
