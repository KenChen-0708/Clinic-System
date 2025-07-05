/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import control.MaintainPatient;
import java.util.Scanner;
import entity.Patient;

public class PatientUI {
    private MaintainPatient maintainPatient = new MaintainPatient();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        int choice;
        do {
            System.out.println("\n--- Patient Management ---");
            System.out.println("1. Add Patient");
            System.out.println("2. Remove Patient");
            System.out.println("3. View All Patients");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1: addPatient(); break;
                case 2: removePatient(); break;
                case 3: maintainPatient.displayAllPatients(); break;
                case 0: System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 0);
    }

    private void addPatient() {
        System.out.print("Enter ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        maintainPatient.addPatient(new Patient(id, name, phone));
    }

    private void removePatient() {
        System.out.print("Enter ID to remove: ");
        String id = scanner.nextLine();
        if (maintainPatient.removePatient(id))
            System.out.println("Removed.");
        else
            System.out.println("Not found.");
    }

    public static void main(String[] args) {
        new PatientUI().run();
    }
}
