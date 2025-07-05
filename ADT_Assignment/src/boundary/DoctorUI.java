/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import control.MaintainDoctor;
import entity.Doctor;
import java.util.Scanner;

public class DoctorUI {
    private MaintainDoctor maintainDoctor = new MaintainDoctor();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        int choice;
        do {
            System.out.println("\n--- Doctor Management ---");
            System.out.println("1. Add Doctor");
            System.out.println("2. Remove Doctor");
            System.out.println("3. View All Doctors");
            System.out.println("4. Summary by Specialization");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1: addDoctor(); break;
                case 2: removeDoctor(); break;
                case 3: maintainDoctor.displayAllDoctors(); break;
                case 4: maintainDoctor.summaryBySpecialization(); break;
                case 0: System.out.println("Back to Main Menu"); break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 0);
    }

    private void addDoctor() {
        System.out.print("Enter ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter specialization: ");
        String spec = scanner.nextLine();
        maintainDoctor.addDoctor(new Doctor(id, name, spec));
    }

    private void removeDoctor() {
        System.out.print("Enter ID to remove: ");
        String id = scanner.nextLine();
        if (maintainDoctor.removeDoctor(id))
            System.out.println("Removed.");
        else
            System.out.println("Not found.");
    }

    public static void main(String[] args) {
        new DoctorUI().run();
    }
}
