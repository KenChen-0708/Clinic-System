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
            System.out.println("5. Set Doctor Schedule");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1: maintainDoctor.addDoctor(); break;
                case 2: maintainDoctor.removeDoctor(); break;
                case 3: maintainDoctor.displayAllDoctors(); break;
                case 4: maintainDoctor.searchDoctorById(); break;
                case 5: maintainDoctor.setDoctorSchedule(); break;
                case 0: MessageUI.displayExitMessage(); break;
                default: MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

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

    public void displayDoctorList(String output) {
        System.out.println("\n--- Doctor List ---\n" + output);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }
}
