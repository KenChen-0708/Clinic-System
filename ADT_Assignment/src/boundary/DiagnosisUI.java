/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import adt.DoublyLinkedList;
import control.MaintainDiagnosis;
import control.MaintainTreatment;
import entity.Diagnosis;
import java.util.Scanner;

/**
 *
 * @author siuye
 */
public class DiagnosisUI {

    private static Scanner scanner = new Scanner(System.in);
    private static MaintainDiagnosis md = new MaintainDiagnosis();
    private final MaintainTreatment mt = new MaintainTreatment();

    public void displayDiagnosisMenu() {
        int choice;

        do {
            System.out.println("\n=== DIAGNOSIS MANAGEMENT ===");
            System.out.println("1. Create Diagnosis");
            System.out.println("2. View Diagnosis");
            System.out.println("3. Update Diagnosis");
            System.out.println("4. Delete Diagnosis");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();  // Consume the newline character after integer input

            switch (choice) {
                case 1:
                    createDiagnosis();
                    break;
                case 2:
                    viewDiagnosis();
                    break;
                case 3:
                    updateDiagnosis();
                    break;
                case 4:
                    deleteDiagnosis();
                    break;
                case 0:
                    System.out.println("Exiting... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    public void createDiagnosis() {
        System.out.println("\n--- CREATE DIAGNOSIS ---");

        // Collecting data from the user
        System.out.println("Patient ID: " + mt.getServingPatient().getId());

        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();

        DoublyLinkedList<String> symptoms = new DoublyLinkedList<>();
        String symptom;
        do {
            System.out.print("Enter symptom (or press Enter to finish): ");
            symptom = scanner.nextLine();
            if (!symptom.isEmpty()) {
                symptoms.add(symptom);
            }
        } while (!symptom.isEmpty());

        System.out.print("Enter diagnosis details: ");
        String diagnosisDetails = scanner.nextLine();

//        Diagnosis newDiagnosis = new Diagnosis(patientId, doctorId, symptoms, diagnosisDetails);
        Diagnosis newDiagnosis = new Diagnosis(mt.getServingPatient().getId(), doctorId, symptoms, diagnosisDetails);

        boolean success = md.createDiagnosis(newDiagnosis);

        if (!success) {
            System.out.println("Diagnosis create not successful");
        } else {
            System.out.println("Diagnosis created successfully");
            System.out.println(newDiagnosis.toString());
        }

        return;

    }

    public void viewDiagnosis() {
        System.out.println("\n--- VIEW DIAGNOSIS ---");
        System.out.println("1. View all diagnoses");
        System.out.println("2. View diagnosis by ID");
        System.out.println("3. View diagnoses by Patient ID");
        System.out.print("Enter choice: ");
        
        try {
            int viewChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (viewChoice) {
                case 1:
                    viewAllDiagnoses();
                    break;
                case 2:
                    viewDiagnosisById();
                    break;
                case 3:
                    viewDiagnosesByPatientId();
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    private void viewAllDiagnoses() {
        DoublyLinkedList<Diagnosis> diagnoses = md.getAllDiagnoses();
        
        if (diagnoses.isEmpty()) {
            System.out.println("No diagnoses found.");
            return;
        }
        
        System.out.println("\n=== ALL DIAGNOSES ===");
        for (int i = 1; i <= diagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnoses.getEntry(i);
            System.out.println(diagnosis);
            System.out.println("------------------------");
        }
        System.out.println("Total diagnoses: " + diagnoses.getNumberOfEntries());
    }
    
    private void viewDiagnosisById() {
        System.out.print("Enter Diagnosis ID: ");
        String diagnosisId = scanner.nextLine();
        
        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);
        
        if (diagnosis != null) {
            System.out.println("\n=== DIAGNOSIS FOUND ===");
            System.out.println(diagnosis);
        } else {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
        }
    }
    
    private void viewDiagnosesByPatientId() {
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();
        
        DoublyLinkedList<Diagnosis> patientDiagnoses = md.findDiagnosesByPatientId(patientId);
        
        if (patientDiagnoses.isEmpty()) {
            System.out.println("No diagnoses found for patient ID: " + patientId);
            return;
        }
        
        System.out.println("\n=== DIAGNOSES FOR PATIENT " + patientId + " ===");
        for (int i = 1; i <= patientDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = patientDiagnoses.getEntry(i);
            System.out.println(diagnosis);
            System.out.println("------------------------");
        }
        System.out.println("Total diagnoses: " + patientDiagnoses.getNumberOfEntries());
    }
    
    public void updateDiagnosis() {
        System.out.println("\n--- UPDATE DIAGNOSIS ---");
        System.out.print("Enter Diagnosis ID to update: ");
        String diagnosisId = scanner.nextLine();
        
        Diagnosis existingDiagnosis = md.findDiagnosisById(diagnosisId);
        
        if (existingDiagnosis == null) {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
            return;
        }
        
        System.out.println("Current diagnosis details:");
        System.out.println(existingDiagnosis);
        
        try {
            System.out.println("\nWhat would you like to update?");
            System.out.println("1. Symptoms");
            System.out.println("2. Diagnosis Details");
            System.out.println("3. Both");
            System.out.print("Enter choice: ");
            
            int updateChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            DoublyLinkedList<String> newSymptoms = null;
            String newDiagnosisDetails = null;
            
            if (updateChoice == 1 || updateChoice == 3) {
                System.out.println("\nEnter new symptoms (press Enter on empty line to finish):");
                newSymptoms = new DoublyLinkedList<>();
                String symptom;
                do {
                    System.out.print("Symptom: ");
                    symptom = scanner.nextLine();
                    if (!symptom.trim().isEmpty()) {
                        newSymptoms.add(symptom);
                    }
                } while (!symptom.trim().isEmpty());
                
                if (newSymptoms.isEmpty()) {
                    System.out.println("At least one symptom is required!");
                    return;
                }
            }
            
            if (updateChoice == 2 || updateChoice == 3) {
                System.out.print("Enter new diagnosis details: ");
                newDiagnosisDetails = scanner.nextLine();
                
                if (newDiagnosisDetails.trim().isEmpty()) {
                    System.out.println("Diagnosis details cannot be empty!");
                    return;
                }
            }
            
            boolean success = md.updateDiagnosis(diagnosisId, newSymptoms, newDiagnosisDetails);
            
            if (success) {
                System.out.println("Diagnosis updated successfully!");
                Diagnosis updatedDiagnosis = md.findDiagnosisById(diagnosisId);
                System.out.println("\nUpdated diagnosis:");
                System.out.println(updatedDiagnosis);
            } else {
                System.out.println("Failed to update diagnosis.");
            }
            
        } catch (Exception e) {
            System.out.println("Error updating diagnosis: " + e.getMessage());
            scanner.nextLine(); // Clear any remaining input
        }
    }
    
    public void deleteDiagnosis() {
        System.out.println("\n--- DELETE DIAGNOSIS ---");
        System.out.println("1. Delete specific diagnosis by ID");
        System.out.println("2. Delete all diagnoses for a patient");
        System.out.println("3. Delete all diagnoses");
        System.out.print("Enter choice: ");
        
        try {
            int deleteChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (deleteChoice) {
                case 1:
                    deleteDiagnosisById();
                    break;
                case 2:
                    deleteDiagnosesByPatientId();
                    break;
                case 3:
                    deleteAllDiagnoses();
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    private void deleteDiagnosisById() {
        System.out.print("Enter Diagnosis ID to delete: ");
        String diagnosisId = scanner.nextLine();
        
        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);
        
        if (diagnosis == null) {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
            return;
        }
        
        System.out.println("Diagnosis to be deleted:");
        System.out.println(diagnosis);
        
        System.out.print("Are you sure you want to delete this diagnosis? (y/n): ");
        String confirmation = scanner.nextLine().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            boolean success = md.deleteDiagnosisById(diagnosisId);
            
            if (success) {
                System.out.println("Diagnosis deleted successfully!");
            } else {
                System.out.println("Failed to delete diagnosis.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    private void deleteDiagnosesByPatientId() {
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();
        
        DoublyLinkedList<Diagnosis> patientDiagnoses = md.findDiagnosesByPatientId(patientId);
        
        if (patientDiagnoses.isEmpty()) {
            System.out.println("No diagnoses found for patient ID: " + patientId);
            return;
        }
        
        System.out.println("Diagnoses to be deleted for patient " + patientId + ":");
        for (int i = 1; i <= patientDiagnoses.getNumberOfEntries(); i++) {
            System.out.println((i) + ". " + patientDiagnoses.getEntry(i).getDiagnosisId());
        }
        
        System.out.print("Are you sure you want to delete ALL diagnoses for this patient? (y/n): ");
        String confirmation = scanner.nextLine().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            int deletedCount = md.deleteDiagnosesByPatientId(patientId);
            System.out.println("Deleted " + deletedCount + " diagnoses successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    private void deleteAllDiagnoses() {
        DoublyLinkedList<Diagnosis> allDiagnoses = md.getAllDiagnoses();
        
        if (allDiagnoses.isEmpty()) {
            System.out.println("No diagnoses to delete.");
            return;
        }
        
        System.out.println("Total diagnoses to be deleted: " + allDiagnoses.getNumberOfEntries());
        System.out.print("Are you sure you want to delete ALL diagnoses? This cannot be undone! (y/n): ");
        String confirmation = scanner.nextLine().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            System.out.print("Type 'DELETE ALL' to confirm: ");
            String finalConfirmation = scanner.nextLine();
            
            if (finalConfirmation.equals("DELETE ALL")) {
                boolean success = md.deleteAllDiagnoses();
                
                if (success) {
                    System.out.println("All diagnoses deleted successfully!");
                } else {
                    System.out.println("Failed to delete all diagnoses.");
                }
            } else {
                System.out.println("Deletion cancelled - incorrect confirmation text.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
}
