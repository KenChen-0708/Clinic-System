package boundary;

import control.MaintainPatient;
import entity.Patient;
import utility.MessageUI;

import java.util.Scanner;

public class PatientUI {

    private final Scanner scanner = new Scanner(System.in);
    private final MaintainPatient maintainPatient;

    public PatientUI(MaintainPatient control) {
        this.maintainPatient = control;
    }

    public void run() {
        int choice;
        do {
            displayMenu();
            choice = getMenuChoice();
            maintainPatient.handleMenuChoice(choice);
        } while (choice != 0);
    }

    public void displayMenu() {
        System.out.println("\n===============================");
        System.out.println("     PATIENT MANAGEMENT MENU   ");
        System.out.println("===============================");
        System.out.println("1. Register New Patient");
        System.out.println("2. Remove Patient");
        System.out.println("3. View All Patients");
        System.out.println("4. Search Patient by ID");
        System.out.println("5. Simulate Patient Queue");
        System.out.println("6. Patient Statistics Report");
        System.out.println("7. Patient Contact Directory");
        System.out.println("0. Back to Main Menu");
        System.out.println("-------------------------------");
    }

    public int getMenuChoice() {
        System.out.print("Enter your choice: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
    }

    public Patient inputPatientDetails() {
        System.out.print("Enter Name      : ");
        String name = scanner.nextLine();
        System.out.print("Enter IC Number : ");
        String icNumber = scanner.nextLine();
        System.out.print("Enter Phone     : ");
        String phone = scanner.nextLine();
        System.out.print("Enter Gender    : ");
        String gender = scanner.nextLine();
        System.out.println();
        return new Patient(name, icNumber, phone, gender);
    }

    public String inputPatientId() {
        System.out.print("Enter Patient ID: ");
        return scanner.nextLine();
    }

    public void displayPatientList(String output) {
        System.out.println("\n--- Patient List ---\n" + output);
    }

    public void displayPatient(Patient patient) {
        System.out.println("\n--- Patient Found ---");
        System.out.println("ID        : " + patient.getId());
        System.out.println("Name      : " + patient.getName());
        System.out.println("IC Number : " + patient.getIcNumber());
        System.out.println("Phone     : " + patient.getPhone());
        System.out.println("Gender    : " + patient.getGender());
        System.out.println("Reg Date  : " + patient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }
}
