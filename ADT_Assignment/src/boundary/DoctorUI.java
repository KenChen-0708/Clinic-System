package boundary;

import control.MaintainDoctor;
import entity.Doctor;
import utility.MessageUI;

import java.util.Scanner;

public class DoctorUI {
    private final Scanner scanner = new Scanner(System.in);
    private final MaintainDoctor maintainDoctor;

    public DoctorUI(MaintainDoctor control) {
        this.maintainDoctor = control;
    }

    public void run() {
        int choice;
        do {
            System.out.println("\n===============================");
            System.out.println("     DOCTOR MANAGEMENT MENU    ");
            System.out.println("===============================");
            System.out.println("1. Add New Doctor");
            System.out.println("2. Remove Doctor");
            System.out.println("3. View All Doctors");
            System.out.println("4. Search Doctor by ID");
            System.out.println("5. Search Doctor by Name");
            System.out.println("6. Search Doctor by Specialization");
            System.out.println("7. Update Doctor Details");
            System.out.println("8. Sort Doctors by Name");
            System.out.println("9. Sort Doctors by Specialization");
            System.out.println("10. Set Doctor Schedule");
            System.out.println("11. View Doctor Schedule");
            System.out.println("12. View Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------");
            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1: maintainDoctor.addDoctor(); break;
                case 2: maintainDoctor.removeDoctor(); break;
                case 3: maintainDoctor.displayAllDoctors(); break;
                case 4: maintainDoctor.searchDoctorById(); break;
                case 5: maintainDoctor.searchDoctorByName(); break;
                case 6: maintainDoctor.searchDoctorBySpecialization(); break;
                case 7: maintainDoctor.updateDoctorDetails(); break;
                case 8: maintainDoctor.sortDoctorsByName(); break;
                case 9: maintainDoctor.sortDoctorsBySpecialization(); break;
                case 10: maintainDoctor.setDoctorSchedule(); break;
                case 11: maintainDoctor.displayDoctorSchedule(); break;
                case 12: maintainDoctor.displayStatistics(); break;
                case 0: MessageUI.displayExitMessage(); break;
                default: MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    // =========================
    // Input Methods
    // =========================
    public Doctor inputDoctorDetails() {
        System.out.print("Enter ID            : ");
        String id = scanner.nextLine();
        System.out.print("Enter Name          : ");
        String name = scanner.nextLine();
        System.out.print("Enter Specialization: ");
        String specialization = scanner.nextLine();
        return new Doctor(id, name, specialization);
    }

    public String inputDoctorId() {
        System.out.print("Enter Doctor ID: ");
        return scanner.nextLine();
    }

    public String inputDoctorName() {
        System.out.print("Enter Doctor Name: ");
        return scanner.nextLine();
    }

    public String inputSpecialization() {
        System.out.print("Enter Specialization: ");
        return scanner.nextLine();
    }

    public String inputSchedule() {
        System.out.print("Enter Schedule (e.g., Mon-Fri 9am-5pm): ");
        return scanner.nextLine();
    }

    // =========================
    // Display Methods
    // =========================
    public void displayDoctorList(String output) {
        System.out.println("\n--- Doctor List ---\n" + output);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }
}
