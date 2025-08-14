package boundary;

import control.MaintainTreatment;
import entity.Treatment;
import java.util.Scanner;

public class TreatmentUI {

    private final Scanner scanner;
    private final MaintainTreatment maintainTreatment;

    public TreatmentUI(MaintainTreatment maintainTreatment) {
        this.scanner = new Scanner(System.in);
        this.maintainTreatment = maintainTreatment;
    }

    public void run() {
        int choice;
        do {
            displayMainMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                processMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1;
            }
        } while (choice != 0);
    }

    private void displayMainMenu() {
        System.out.println("\n=== TREATMENT MANAGEMENT ===");
        System.out.println("1. Add New Treatment");
        System.out.println("2. Update Treatment");
        System.out.println("3. Delete Treatment");
        System.out.println("4. Search Treatment by ID");
        System.out.println("5. View All Treatments");
        System.out.println("6. Generate Treatment Analysis");
        System.out.println("7. Generate Doctor Workload");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    private void processMenuChoice(int choice) {
        switch (choice) {
            case 1:
                maintainTreatment.addTreatment();
                break;
            case 2:
                maintainTreatment.updateTreatment();
                break;
            case 3:
                maintainTreatment.deleteTreatment();
                break;
            case 4:
                maintainTreatment.searchTreatment();
                break;
            case 5:
                maintainTreatment.viewAllTreatments();
                break;
            case 6:
                maintainTreatment.generateTreatmentAnalysis();
                break;
            case 7:
                maintainTreatment.generateDoctorWorkload();
                break;
            case 0:
                System.out.println("Exiting system...");
                break;
            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }

    public Treatment inputTreatmentDetails() {
        System.out.println("\n=== NEW TREATMENT ===");
        String pid = inputField("Patient ID");
        String did = inputField("Doctor ID");
        String symptoms = inputField("Symptoms");
        String diagnosis = inputField("Diagnosis");
        int severity = inputSeverity();
        String prescription = inputField("Prescription");

        System.out.println("Status automatically set to: Pending");
        // Return treatment with null ID (will be set by MaintainTreatment)
        return new Treatment(null, pid, did, symptoms, diagnosis, severity, prescription, "Pending");
    }

    public Treatment inputUpdatedDetails(Treatment existing) {
        System.out.println("\n=== UPDATE TREATMENT ===");
        System.out.println("Current details:");
        System.out.println(existing);

        System.out.println("\nSelect fields to update (leave blank to keep current):");
        String pid = updateField("Patient ID", existing.getPatientId());
        String did = updateField("Doctor ID", existing.getDoctorId());
        String symptoms = updateField("Symptoms", existing.getSymptoms());
        String diagnosis = updateField("Diagnosis", existing.getDiagnosis());
        int severity = updateSeverity(existing.getSeverityLevel());
        String prescription = updateField("Prescription", existing.getPrescription());

        return new Treatment(
                existing.getTreatmentId(), // Keep original ID
                pid.isEmpty() ? existing.getPatientId() : pid,
                did.isEmpty() ? existing.getDoctorId() : did,
                symptoms.isEmpty() ? existing.getSymptoms() : symptoms,
                diagnosis.isEmpty() ? existing.getDiagnosis() : diagnosis,
                severity,
                prescription.isEmpty() ? existing.getPrescription() : prescription,
                existing.getStatus() // Keep original status
        );
    }

    private String inputField(String fieldName) {
        System.out.printf("%-12s: ", fieldName);
        return scanner.nextLine();
    }

    private String updateField(String fieldName, String currentValue) {
        System.out.printf("%-12s [Current: %s]: ", fieldName, currentValue);
        return scanner.nextLine();
    }

    private int inputSeverity() {
        int severity = 0;
        while (severity < 1 || severity > 5) {
            System.out.print("Severity (1-5, 1=Low, 5=Critical): ");
            try {
                severity = scanner.nextInt();
                scanner.nextLine();
                if (severity < 1 || severity > 5) {
                    System.out.println("Please enter between 1-5");
                }
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
        return severity;
    }

    private int updateSeverity(int currentSeverity) {
        System.out.printf("Severity (1-5) [Current: %d]: ", currentSeverity);
        try {
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                int severity = Integer.parseInt(input);
                if (severity >= 1 && severity <= 5) {
                    return severity;
                }
                System.out.println("Invalid severity! Keeping current value.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number! Keeping current value.");
        }
        return currentSeverity;
    }

    public String inputTreatmentId() {
        return inputField("Treatment ID");
    }

    public void displayTreatment(Treatment treatment) {
        if (treatment != null) {
            System.out.println("\n=== TREATMENT DETAILS ===");
            System.out.println(treatment);
        } else {
            System.out.println("Treatment not found.");
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayReport(String report) {
        System.out.println("\n=== REPORT ===");
        System.out.println(report);
    }
}
