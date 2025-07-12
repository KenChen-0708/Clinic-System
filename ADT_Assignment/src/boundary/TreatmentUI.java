package boundary;

import control.MaintainTreatment;
import entity.Treatment;
import utility.MessageUI;

import java.util.Scanner;

public class TreatmentUI {
    private final Scanner scanner = new Scanner(System.in);
    private final MaintainTreatment maintainTreatment;

    public TreatmentUI(MaintainTreatment control) {
        this.maintainTreatment = control;
    }

    public void run() {
        int choice;
        do {
            System.out.println("\n===============================");
            System.out.println("     MEDICAL TREATMENT MENU    ");
            System.out.println("===============================");
            System.out.println("1. Record Diagnosis & Prescription");
            System.out.println("2. Remove Treatment");
            System.out.println("3. View Treatment History");
            System.out.println("4. Search Treatment by Patient ID");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1: maintainTreatment.addTreatment(); break;
                case 2: maintainTreatment.removeTreatment(); break;
                case 3: maintainTreatment.displayAllTreatments(); break;
                case 4: maintainTreatment.searchTreatmentByPatientId(); break;
                case 0: MessageUI.displayExitMessage(); break;
                default: MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    public Treatment inputTreatmentDetails() {
        System.out.print("Enter Treatment ID : ");
        String tid = scanner.nextLine();
        System.out.print("Enter Patient ID   : ");
        String pid = scanner.nextLine();
        System.out.print("Enter Diagnosis    : ");
        String diagnosis = scanner.nextLine();
        System.out.print("Enter Prescription : ");
        String prescription = scanner.nextLine();
        return new Treatment(tid, pid, diagnosis, prescription);
    }

    public String inputTreatmentId() {
        System.out.print("Enter Treatment ID to remove: ");
        return scanner.nextLine();
    }

    public String inputPatientId() {
        System.out.print("Enter Patient ID: ");
        return scanner.nextLine();
    }

    public void displayTreatmentList(String output) {
        System.out.println("\n--- Treatment List ---\n" + output);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }
}
