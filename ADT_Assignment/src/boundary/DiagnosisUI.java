package boundary;

import adt.DoublyLinkedList;
import control.MaintainDiagnosis;
import control.MaintainTreatment;
import entity.Diagnosis;
import entity.Treatment;
import entity.TreatmentMedicineList;
import java.util.Scanner;
import control.MaintainDoctor;
import entity.Doctor;
import entity.Patient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DiagnosisUI - User Interface for Diagnosis Management Clean UI layer focused
 * only on user interaction and display All ADT operations delegated to
 * MaintainDiagnosis control layer
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
            System.out.println("\n==============================");
            System.out.println("     DIAGNOSIS MANAGEMENT     ");
            System.out.println("==============================");
            System.out.println("1. Create Diagnosis");
            System.out.println("2. View Diagnosis");
            System.out.println("3. Update Diagnosis");
            System.out.println("4. Delete Diagnosis");
            System.out.println("0. Back to Treatment Management Menu");
            System.out.print("Enter choice: ");
            try {
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
                        System.out.println("Returning to Treatment Management...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1;
            }
        } while (choice != 0);
    }

    public void createDiagnosis() {
        System.out.println("\n==============================");
        System.out.println("     CREATE DIAGNOSIS     ");
        System.out.println("==============================");

        // Check if there are patients in the serving queue
        if (!mt.hasServingPatients()) {
            System.out.println("No patients are currently being served.");
            System.out.println("Please serve patients from the queue first before creating a diagnosis.");
            return;
        }

        // Get current serving patient and doctor from the queue
        Patient currentServingPatient = mt.getServingPatient();
        Doctor currentConsultingDoctor = mt.getDoctorConsulting();

        if (currentServingPatient == null || currentConsultingDoctor == null) {
            System.out.println("Error: No valid patient-doctor pair found in serving queue.");
            System.out.println("Please check the serving queue status.");
            return;
        }

        // Display current patient and doctor information
        displayCurrentPatientInfo(currentServingPatient, currentConsultingDoctor);

        // Get symptoms input
        DoublyLinkedList<String> symptoms = getSymptomsInput();
        if (symptoms.isEmpty()) {
            System.out.println("Symptoms cannot be empty.");
            return;
        }

        // Get diagnosis details
        String diagnosisDetails = getDiagnosisDetailsInput();
        if (!md.isValidInput(diagnosisDetails)) {
            System.out.println("Diagnosis details cannot be empty.");
            return;
        }

        // Create diagnosis
        Diagnosis newDiagnosis = new Diagnosis(
                currentServingPatient.getId(),
                currentConsultingDoctor.getId(),
                symptoms,
                diagnosisDetails
        );

        boolean diagnosisSuccess = md.createDiagnosis(newDiagnosis);

        if (!diagnosisSuccess) {
            System.out.println("Diagnosis creation not successful");
            return;
        }

        System.out.println("Diagnosis created successfully!");
        System.out.println(newDiagnosis.toString());

        // Create automatic treatment
        createAutomaticTreatment(currentServingPatient, currentConsultingDoctor);

        // Complete the consultation process
        completeConsultation(currentServingPatient, currentConsultingDoctor);
    }

    private void displayCurrentPatientInfo(Patient patient, Doctor doctor) {
        System.out.println("=== CURRENT PATIENT BEING SERVED ===");
        System.out.println("Patient ID: " + patient.getId());
        System.out.println("Patient Name: " + patient.getName());
        System.out.println("Doctor ID: " + doctor.getId());
        System.out.println("Doctor Name: " + doctor.getName());
        System.out.println("Doctor Specialization: " + doctor.getSpecialization());
        System.out.println("Remaining patients in serving queue: " + (mt.getServingPatientsCount() - 1));
        
        // Display serving status for better debugging
        mt.displayServingStatus();
    }

    private DoublyLinkedList<String> getSymptomsInput() {
        System.out.print("Enter symptoms: ");
        String symptomInput = scanner.nextLine();
        return md.processSymptoms(symptomInput);
    }

    private String getDiagnosisDetailsInput() {
        System.out.print("Enter diagnosis details: ");
        return scanner.nextLine();
    }

    private void createAutomaticTreatment(Patient patient, Doctor doctor) {
        System.out.println("\n========================================");
        System.out.println("     AUTOMATICALLY CREATING TREATMENT     ");
        System.out.println("========================================");

        String patientId = patient.getId();
        String doctorId = doctor.getId();

        // Get treatment date - auto-fill with today's date
        LocalDate treatmentDate = LocalDate.now();
        System.out.println("Treatment Date: " + treatmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Ask if user wants to add medicines immediately
        boolean addMedicines = askYesNo("Do you want to add medicines to this treatment?");

        DoublyLinkedList<TreatmentMedicineList> medicines = new DoublyLinkedList<>();

        if (addMedicines && !md.getAvailableMedicines().isEmpty()) {
            medicines = getMedicinesFromUser();
        }

        // CORRECTED: Determine status based on whether medicines are added
        String status = medicines.isEmpty() ? "Completed" : "Pending";

        // Create treatment
        boolean treatmentSuccess = createTreatmentWithMedicines(patientId, doctorId, status, treatmentDate, medicines);

        if (treatmentSuccess) {
            displayTreatmentCreationResult(status, treatmentDate, medicines, patientId, doctorId);
        } else {
            System.out.println("Warning: Treatment could not be created automatically.");
        }
    }

    private DoublyLinkedList<TreatmentMedicineList> getMedicinesFromUser() {
        DoublyLinkedList<TreatmentMedicineList> medicines = new DoublyLinkedList<>();
        System.out.println("\n==========================");
        System.out.println("     ADD MEDICINES     ");
        System.out.println("==========================");

        DoublyLinkedList<String> availableMedicines = md.getAvailableMedicines();
        if (availableMedicines.isEmpty()) {
            System.out.println("No available medicines in the pharmacy.");
            return medicines;
        }

        displayMedicineOptions(availableMedicines);

        while (true) {
            String prescription = selectMedicine(availableMedicines);
            if (prescription == null) {
                break;
            }

            int tablets = getPositiveNumber("Enter number of tablets: ");
            if (tablets == -1) {
                continue;
            }

            String mealTiming = selectMealTiming();
            if (mealTiming == null) {
                continue;
            }

            int timesPerDay = getPositiveNumber("Enter times per day: ");
            if (timesPerDay == -1) {
                continue;
            }

            int totalDay = getPositiveNumber("Enter total day taking medicine: ");
            if (totalDay == -1) {
                continue;
            }

            TreatmentMedicineList medicine = new TreatmentMedicineList(
                    "", prescription, tablets, mealTiming, timesPerDay, totalDay);
            medicines.add(medicine);

            System.out.println("Medicine added successfully!");

            if (!askYesNo("Add another medicine?")) {
                break;
            }
        }

        return medicines;
    }

    private void displayMedicineOptions(DoublyLinkedList<String> medicines) {
        System.out.println("Available Medicines:");
        for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + medicines.getEntry(i));
        }
    }

    private String selectMedicine(DoublyLinkedList<String> availableMedicines) {
        try {
            System.out.print("Select medicine by number (1-" + availableMedicines.getNumberOfEntries() + ") or 0 to skip: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                return null; // User chose to skip
            }

            if (md.isValidMedicineChoice(choice)) {
                return md.getMedicineByChoice(choice);
            } else {
                System.out.println("Invalid choice! Please select a number between 1-" + availableMedicines.getNumberOfEntries());
                return selectMedicine(availableMedicines); // Recursive call
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
            return selectMedicine(availableMedicines); // Recursive call
        }
    }

    private String selectMealTiming() {
        DoublyLinkedList<String> mealTimingOptions = md.getMealTimingOptions();

        System.out.println("Meal timing:");
        for (int i = 1; i <= mealTimingOptions.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + mealTimingOptions.getEntry(i));
        }

        try {
            System.out.print("Choose option (1-" + mealTimingOptions.getNumberOfEntries() + "): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (md.isValidMealTimingChoice(choice)) {
                return md.getMealTimingByChoice(choice);
            } else {
                System.out.println("Invalid choice!");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
            scanner.nextLine();
            return null;
        }
    }

    private boolean createTreatmentWithMedicines(String patientId, String doctorId, String status,
            LocalDate treatmentDate, DoublyLinkedList<TreatmentMedicineList> medicines) {
        if (!medicines.isEmpty()) {
            return mt.createTreatmentWithMedicines(patientId, doctorId, status, treatmentDate, medicines);
        } else {
            return mt.createTreatment(patientId, doctorId, status, treatmentDate);
        }
    }

    // CORRECTED: Treatment creation result display
    private void displayTreatmentCreationResult(String status, LocalDate treatmentDate,
            DoublyLinkedList<TreatmentMedicineList> medicines,
            String patientId, String doctorId) {
        System.out.println("Treatment created automatically!");
        System.out.println("Treatment Status: " + status);

        if (status.equals("Completed")) {
            System.out.println("(Treatment marked as completed - no medicines required)");
            System.out.println("(Doctor consultation finished - doctor is now available)");
        } else {
            System.out.println("(Treatment pending - patient needs to collect medicines from pharmacy)");
            System.out.println("(Doctor consultation finished - doctor is now available)");
        }

        System.out.println("Treatment Date: " + treatmentDate);

        // Show the created treatment details
        DoublyLinkedList<Treatment> patientTreatments = mt.findTreatmentsByPatientAndDoctor(patientId, doctorId);
        if (!patientTreatments.isEmpty()) {
            Treatment latestTreatment = patientTreatments.getEntry(patientTreatments.getNumberOfEntries());
            System.out.println("Created Treatment ID: " + latestTreatment.getTreatmentId());

            if (!medicines.isEmpty()) {
                System.out.println("Medicines prescribed: " + medicines.getNumberOfEntries());
                System.out.println("Patient should collect medicines from pharmacy to complete treatment.");
            }
        }
    }

    private void completeConsultation(Patient patient, Doctor doctor) {
        // CORRECTED: Remove current patient-doctor pair from serving queue after successful diagnosis
        mt.removeCurrentServingPair();

        // CORRECTED: Make doctor available again after completing diagnosis
        // Whether the treatment needs medicine or not, the doctor has completed their consultation
        MaintainDoctor maintainDoctor = new MaintainDoctor();
        doctor.setAvailability("Available");
        maintainDoctor.updateDoctorInList(doctor);

        System.out.println("Doctor " + doctor.getName() + " is now available for next patient.");

        // Show remaining patients in serving queue
        displayRemainingQueue();
    }

    private void displayRemainingQueue() {
        if (mt.hasServingPatients()) {
            Patient nextPatient = mt.getServingPatient();
            Doctor nextDoctor = mt.getDoctorConsulting();
            if (nextPatient != null && nextDoctor != null) {
                System.out.println("Next patient to serve: " + nextPatient.getName() + " (ID: " + nextPatient.getId() + ")");
                System.out.println("With doctor: " + nextDoctor.getName() + " (" + nextDoctor.getSpecialization() + ")");
                System.out.println("Remaining patients in queue: " + mt.getServingPatientsCount());
            } else {
                System.out.println("Queue status unclear - please check serving queue management.");
            }
        } else {
            System.out.println("All patients have been diagnosed and treated. No more patients in serving queue.");
        }
    }

    // VIEW DIAGNOSIS METHODS
    public void viewDiagnosis() {
        System.out.println("\n==========================");
        System.out.println("     VIEW DIAGNOSIS     ");
        System.out.println("==========================");

        System.out.println("1. View all diagnoses");
        System.out.println("2. View diagnosis by ID");
        System.out.println("3. View diagnoses by Patient ID");
        System.out.println("4. Back");
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
                case 4:
                    return;
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

        System.out.println("\n==========================");
        System.out.println("     ALL DIAGNOSES     ");
        System.out.println("==========================");
        for (int i = 1; i <= diagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnoses.getEntry(i);
            System.out.println(diagnosis);
        }
        System.out.println("Total diagnoses: " + diagnoses.getNumberOfEntries());
    }

    private void viewDiagnosisById() {
        System.out.print("Enter Diagnosis ID: ");
        String diagnosisId = scanner.nextLine();

        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);

        if (diagnosis != null) {
            System.out.println("\n==========================");
            System.out.println("     DIAGNOSIS FOUND     ");
            System.out.println("==========================");
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

    // UPDATE DIAGNOSIS METHODS
    public void updateDiagnosis() {
        System.out.println("\n==========================");
        System.out.println("     UPDATE DIAGNOSIS     ");
        System.out.println("==========================");
        System.out.print("Enter Diagnosis ID to update: ");
        String diagnosisId = scanner.nextLine();
        System.out.print("\n");
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
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            int updateChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (updateChoice == 4) {
                return; // Go back to previous menu
            }

            DoublyLinkedList<String> newSymptoms = null;
            String newDiagnosisDetails = null;
            if (updateChoice == 1 || updateChoice == 3) {
                newSymptoms = getUpdatedSymptoms();
                if (newSymptoms == null) {
                    return;
                }
            }
            if (updateChoice == 2 || updateChoice == 3) {
                newDiagnosisDetails = getUpdatedDiagnosisDetails();
                if (newDiagnosisDetails == null) {
                    return;
                }
            }

            if (updateChoice >= 1 && updateChoice <= 3) {
                boolean success = md.updateDiagnosis(diagnosisId, newSymptoms, newDiagnosisDetails);
                if (success) {
                    System.out.println("Diagnosis updated successfully!");
                    Diagnosis updatedDiagnosis = md.findDiagnosisById(diagnosisId);
                    System.out.println("\nUpdated diagnosis:");
                    System.out.println(updatedDiagnosis);
                } else {
                    System.out.println("Failed to update diagnosis.");
                }
            } else {
                System.out.println("Invalid choice. Please select 1-4.");
            }
        } catch (Exception e) {
            System.out.println("Error updating diagnosis: " + e.getMessage());
            scanner.nextLine(); // Clear any remaining input
        }
    }

    private DoublyLinkedList<String> getUpdatedSymptoms() {
        System.out.print("Enter new symptoms: ");
        String symptomInput = scanner.nextLine();

        if (!md.isValidInput(symptomInput)) {
            System.out.println("Symptoms cannot be empty!");
            return null;
        }

        return md.processSymptoms(symptomInput);
    }

    private String getUpdatedDiagnosisDetails() {
        System.out.print("Enter new diagnosis details: ");
        String diagnosisDetails = scanner.nextLine();

        if (!md.isValidInput(diagnosisDetails)) {
            System.out.println("Diagnosis details cannot be empty!");
            return null;
        }

        return diagnosisDetails;
    }

    // DELETE DIAGNOSIS METHODS
    public void deleteDiagnosis() {
        System.out.println("\n==========================");
        System.out.println("     DELETE DIAGNOSIS     ");
        System.out.println("==========================");

        System.out.println("1. Delete specific diagnosis by ID");
        System.out.println("2. Delete all diagnoses for a patient");
        System.out.println("3. Delete all diagnoses");
        System.out.println("4. Back");
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
                case 4:
                    return;
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
        System.out.print("\n");

        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);

        if (diagnosis == null) {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
            return;
        }

        System.out.println("Diagnosis to be deleted:");
        System.out.println(diagnosis);

        if (confirmAction("Are you sure you want to delete this diagnosis?")) {
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

        System.out.println("\nDiagnoses to be deleted for patient " + patientId + ":");
        for (int i = 1; i <= patientDiagnoses.getNumberOfEntries(); i++) {
            System.out.println((i) + ". " + patientDiagnoses.getEntry(i).getDiagnosisId());
        }

        if (confirmAction("Are you sure you want to delete ALL diagnoses for this patient?")) {
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

        if (confirmAction("Are you sure you want to delete ALL diagnoses? This cannot be undone!")) {
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

    // UTILITY METHODS - Clean helper methods for UI operations
    private int getPositiveNumber(String prompt) {
        try {
            System.out.print(prompt);
            int number = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (number <= 0) {
                System.out.println("Number must be positive!");
                return -1;
            }
            return number;
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
            return -1;
        }
    }

    private boolean askYesNo(String question) {
        System.out.print(question + " (y/n): ");
        String answer = scanner.nextLine().toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }

    private boolean confirmAction(String message) {
        System.out.print(message + " (y/n): ");
        String confirmation = scanner.nextLine().toLowerCase();
        return confirmation.equals("y") || confirmation.equals("yes");
    }
}