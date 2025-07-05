/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import control.MaintainTreatment;
import entity.Treatment;
import java.util.Scanner;

public class TreatmentUI {
    private MaintainTreatment maintainTreatment = new MaintainTreatment();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        int choice;
        do {
            System.out.println("\n--- Treatment Management ---");
            System.out.println("1. Add Treatment");
            System.out.println("2. Remove Treatment");
            System.out.println("3. View All Treatments");
            System.out.println("4. Summary - Treatments per Patient");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1: addTreatment(); break;
                case 2: removeTreatment(); break;
                case 3: maintainTreatment.displayAllTreatments(); break;
                case 4: maintainTreatment.summaryTreatmentsPerPatient(); break;
                case 0: System.out.println("Back to Main Menu"); break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 0);
    }

    private void addTreatment() {
        System.out.print("Enter Treatment ID: ");
        String tid = scanner.nextLine();
        System.out.print("Enter Patient ID: ");
        String pid = scanner.nextLine();
        System.out.print("Enter Diagnosis: ");
        String diagnosis = scanner.nextLine();
        System.out.print("Enter Prescription: ");
        String prescription = scanner.nextLine();

        Treatment t = new Treatment(tid, pid, diagnosis, prescription);
        maintainTreatment.addTreatment(t);
    }

    private void removeTreatment() {
        System.out.print("Enter Treatment ID to remove: ");
        String tid = scanner.nextLine();
        if (maintainTreatment.removeTreatment(tid))
            System.out.println("Removed.");
        else
            System.out.println("Not found.");
    }

    public static void main(String[] args) {
        new TreatmentUI().run();
    }
}
