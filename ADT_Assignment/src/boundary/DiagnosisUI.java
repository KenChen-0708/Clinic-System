// Lee Siu Yee
package boundary;

import adt.DoublyLinkedList;
import adt.ListInterface;
import control.MaintainDiagnosis;
import control.MaintainTreatment;
import control.MaintainDoctor;
import entity.Diagnosis;
import entity.Treatment;
import entity.TreatmentMedicineList;
import entity.Doctor;
import entity.Patient;
import entity.ServingStatusData;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiagnosisUI {

    private static final Scanner scanner = new Scanner(System.in);
    private static final MaintainDiagnosis md = new MaintainDiagnosis();
    private static final MaintainTreatment mt = new MaintainTreatment();

    // ===== MAIN MENU SYSTEM =====
    public void displayDiagnosisMenu() {
        ListInterface<String> menuOptions = createMainMenuOptions();
        int choice;

        do {
            System.out.println("\n==================================");
            System.out.println("       DIAGNOSIS MANAGEMENT       ");
            System.out.println("==================================");
            displayMenuOptions(menuOptions);
            choice = getMenuChoice(menuOptions.getNumberOfEntries());

            if (choice != -1) {
                processMainMenuChoice(choice);
            }
        } while (choice != 0); // Back option is now 0
    }

    private ListInterface<String> createMainMenuOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("Create Diagnosis");
        options.add("View Diagnosis");
        options.add("Update Diagnosis");
        options.add("Delete Diagnosis");
        return options;
    }

    private void processMainMenuChoice(int choice) {
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
            case 0: // Back to Treatment Management Menu
                System.out.println("Returning to Treatment Management...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // ===== CREATE DIAGNOSIS USING ADT OPERATIONS =====
    public void createDiagnosis() {
        System.out.println("\n==================================");
        System.out.println("         CREATE DIAGNOSIS         ");
        System.out.println("==================================");

        if (!validateServingQueue()) {
            return;
        }

        Patient currentServingPatient = mt.getServingPatient();
        Doctor currentConsultingDoctor = mt.getDoctorConsulting();

        if (!validatePatientDoctorPair(currentServingPatient, currentConsultingDoctor)) {
            return;
        }

        displayCurrentPatientInfo(currentServingPatient, currentConsultingDoctor);

        // Get input using ADT operations
        ListInterface<String> symptoms = getSymptomsInput();
        if (symptoms.isEmpty()) {
            System.out.println("Symptoms cannot be empty.");
            return;
        }

        String diagnosisDetails = getDiagnosisDetailsInput();
        if (!md.isValidInput(diagnosisDetails)) {
            System.out.println("Diagnosis details cannot be empty.");
            return;
        }

        // Create and save diagnosis using ADT
        Diagnosis newDiagnosis = new Diagnosis(
                currentServingPatient.getId(),
                currentConsultingDoctor.getId(),
                symptoms,
                diagnosisDetails
        );

        if (md.createDiagnosis(newDiagnosis)) {
            System.out.println("Diagnosis created successfully!");
            System.out.println(newDiagnosis.toString());
            createAutomaticTreatment(currentServingPatient, currentConsultingDoctor);
            completeConsultation(currentServingPatient, currentConsultingDoctor);
        } else {
            System.out.println("Diagnosis creation failed.");
        }
    }

    private boolean validateServingQueue() {
        if (!mt.hasServingPatients()) {
            System.out.println("No patients are currently being served.");
            System.out.println("Please serve patients from the queue first before creating a diagnosis.");
            return false;
        }
        return true;
    }

    private boolean validatePatientDoctorPair(Patient patient, Doctor doctor) {
        if (patient == null || doctor == null) {
            System.out.println("Error: No valid patient-doctor pair found in serving queue.");
            System.out.println("Please check the serving queue status.");
            return false;
        }
        return true;
    }

    private void displayCurrentPatientInfo(Patient patient, Doctor doctor) {
        System.out.println("\n=== CURRENT PATIENT BEING SERVED ===");
        System.out.println("Patient ID: " + patient.getId());
        System.out.println("Patient Name: " + patient.getName());
        System.out.println("Doctor ID: " + doctor.getId());
        System.out.println("Doctor Name: " + doctor.getName());
        System.out.println("Doctor Specialization: " + doctor.getSpecialization());
        System.out.println("Remaining patients in serving queue: " + (mt.getServingPatientsCount() - 1));
        displayServingStatus();
    }

    // ===== SERVING STATUS DISPLAY (UI LAYER) =====
    public void displayServingStatus() {
        System.out.println("\n=== CURRENT SERVING STATUS ===");
        ServingStatusData statusData = MaintainTreatment.getServingStatusData();

        if (statusData.hasActiveConsultation()) {
            System.out.println("Currently serving:");
            System.out.println("  Patient: " + statusData.getCurrentPatient().getId() + " (" + statusData.getCurrentPatient().getName() + ")");
            System.out.println("  Doctor: " + statusData.getCurrentDoctor().getId() + " (" + statusData.getCurrentDoctor().getName() + ")");
        } else {
            System.out.println("No active consultation.");
        }
        System.out.println("Waiting: " + statusData.getWaitingPatients() + " patients");
        System.out.println("==================================\n");
    }

    private void createAutomaticTreatment(Patient patient, Doctor doctor) {
        System.out.println("\n==================================");
        System.out.println(" AUTOMATICALLY CREATING TREATMENT ");
        System.out.println("==================================");

        LocalDate treatmentDate = LocalDate.now();
        System.out.println("Treatment Date: " + treatmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        ListInterface<TreatmentMedicineList> medicines = new DoublyLinkedList<>();

        if (askYesNo("Do you want to add medicines to this treatment?")
                && !md.getAvailableMedicines().isEmpty()) {
            medicines = getMedicinesFromUser();
        }

        String status = medicines.isEmpty() ? "Completed" : "Pending";

        if (createTreatmentWithMedicines(patient.getId(), doctor.getId(), status, treatmentDate, medicines)) {
            displayTreatmentCreationResult(status, treatmentDate, medicines, patient.getId(), doctor.getId());
        } else {
            System.out.println("Warning: Treatment could not be created automatically.");
        }
    }

    private ListInterface<TreatmentMedicineList> getMedicinesFromUser() {
        ListInterface<TreatmentMedicineList> medicines = new DoublyLinkedList<>();
        System.out.println("\n==================================");
        System.out.println("           ADD MEDICINES          ");
        System.out.println("==================================");

        ListInterface<String> availableMedicines = md.getAvailableMedicines();
        if (availableMedicines.isEmpty()) {
            System.out.println("No available medicines in the pharmacy.");
            return medicines;
        }

        while (true) {
            String prescription = selectFromList(availableMedicines, "medicine", true);
            if (prescription == null) {
                break;
            }

            TreatmentMedicineList medicine = createMedicineEntry(prescription);
            if (medicine != null) {
                medicines.add(medicine);
                System.out.println("Medicine added successfully!");
            }

            if (!askYesNo("Add another medicine?")) {
                break;
            }
        }

        return medicines;
    }

    private TreatmentMedicineList createMedicineEntry(String prescription) {
        int tablets = getPositiveNumber("Enter number of tablets: ");
        if (tablets == -1) {
            return null;
        }

        String mealTiming = selectFromList(md.getMealTimingOptions(), "meal timing", false);
        if (mealTiming == null) {
            return null;
        }

        int timesPerDay = getPositiveNumber("Enter times per day: ");
        if (timesPerDay == -1) {
            return null;
        }

        int totalDay = getPositiveNumber("Enter total days taking medicine: ");
        if (totalDay == -1) {
            return null;
        }

        return new TreatmentMedicineList("", prescription, tablets, mealTiming, timesPerDay, totalDay);
    }

    private void completeConsultation(Patient patient, Doctor doctor) {
        mt.removeCurrentServingPair();

        MaintainDoctor maintainDoctor = new MaintainDoctor();
        doctor.setAvailability("Available");
        maintainDoctor.updateDoctorInList(doctor);

        System.out.println("Doctor " + doctor.getName() + " is now available for next patient.");
        displayRemainingQueue();
    }

    // ===== VIEW DIAGNOSIS USING ADT OPERATIONS =====
    public void viewDiagnosis() {
        ListInterface<String> viewOptions = createViewOptions();
        System.out.println("\n==================================");
        System.out.println("          VIEW DIAGNOSIS          ");
        System.out.println("==================================");

        displayViewOptions(viewOptions);

        int choice = getMenuChoice(viewOptions.getNumberOfEntries());
        if (choice != -1) {
            processViewChoice(choice);
        }
    }

    private ListInterface<String> createViewOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("View all diagnoses");
        options.add("View diagnosis by ID");
        options.add("View diagnoses by Patient ID");
        return options;
    }

    private void displayViewOptions(ListInterface<String> options) {
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    private void processViewChoice(int choice) {
        switch (choice) {
            case 1:
                viewAllDiagnoses();
                break;
            case 2:
                viewDiagnosisById();
                break;
            case 3:
                viewDiagnosesByPatientId();
                break;
            case 0: // Back
                System.out.println("Returning back...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void viewAllDiagnoses() {
        ListInterface<Diagnosis> diagnoses = md.getAllDiagnoses();
        System.out.println("\n==================================");
        System.out.println("           ALL DIAGNOSES          ");
        System.out.println("==================================");
        displayDiagnosisList(diagnoses, "       ALL DIAGNOSES");
    }

    private void viewDiagnosisById() {
        String diagnosisId = getValidInput("Enter Diagnosis ID: ");
        if (diagnosisId == null) {
            return;
        }

        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);
        if (diagnosis != null) {
            System.out.println("\n==================================");
            System.out.println("          DIAGNOSIS FOUND         ");
            System.out.println("==================================");
            System.out.println(diagnosis);
        } else {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
        }
    }

    private void viewDiagnosesByPatientId() {
        String patientId = getValidInput("Enter Patient ID: ");
        if (patientId == null) {
            return;
        }

        ListInterface<Diagnosis> patientDiagnoses = md.findDiagnosesByPatientId(patientId);
        displayDiagnosisList(patientDiagnoses, "DIAGNOSES FOR PATIENT " + patientId);
    }

    // ===== UPDATE DIAGNOSIS USING ADT OPERATIONS =====
    public void updateDiagnosis() {
        System.out.println("\n==================================");
        System.out.println("         UPDATE DIAGNOSIS         ");
        System.out.println("==================================");

        String diagnosisId = getValidInput("Enter Diagnosis ID to update: ");
        if (diagnosisId == null) {
            return;
        }

        Diagnosis existingDiagnosis = md.findDiagnosisById(diagnosisId);
        if (existingDiagnosis == null) {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
            return;
        }

        System.out.println("Current diagnosis details:");
        System.out.println(existingDiagnosis);

        ListInterface<String> updateOptions = createUpdateOptions();
        displayUpdateOptions(updateOptions);

        int choice = getMenuChoice(updateOptions.getNumberOfEntries());
        if (choice != -1 && choice != 0) {
            processUpdateChoice(choice, diagnosisId);
        }
    }

    private ListInterface<String> createUpdateOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("Symptoms");
        options.add("Diagnosis Details");
        options.add("Both");
        return options;
    }

    private void displayUpdateOptions(ListInterface<String> options) {
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    private void processUpdateChoice(int choice, String diagnosisId) {
        ListInterface<String> newSymptoms = null;
        String newDiagnosisDetails = null;

        if (choice == 1 || choice == 3) {
            newSymptoms = getUpdatedSymptoms();
            if (newSymptoms == null) {
                return;
            }
        }

        if (choice == 2 || choice == 3) {
            newDiagnosisDetails = getUpdatedDiagnosisDetails();
            if (newDiagnosisDetails == null) {
                return;
            }
        }

        if (md.updateDiagnosis(diagnosisId, newSymptoms, newDiagnosisDetails)) {
            System.out.println("Diagnosis updated successfully!");
            Diagnosis updatedDiagnosis = md.findDiagnosisById(diagnosisId);
            System.out.println("\nUpdated diagnosis:");
            System.out.println(updatedDiagnosis);
        } else {
            System.out.println("Failed to update diagnosis.");
        }
    }

    // ===== DELETE DIAGNOSIS USING ADT OPERATIONS =====
    public void deleteDiagnosis() {
        ListInterface<String> deleteOptions = createDeleteOptions();
        System.out.println("\n==================================");
        System.out.println("         DELETE DIAGNOSIS         ");
        System.out.println("==================================");
        displayDeleteOptions(deleteOptions);

        int choice = getMenuChoice(deleteOptions.getNumberOfEntries());
        if (choice != -1) {
            processDeleteChoice(choice);
        }
    }

    private ListInterface<String> createDeleteOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("Delete specific diagnosis by ID");
        options.add("Delete all diagnoses for a patient");
        options.add("Delete all diagnoses");
        return options;
    }

    private void displayDeleteOptions(ListInterface<String> options) {
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    private void processDeleteChoice(int choice) {
        switch (choice) {
            case 1:
                deleteDiagnosisById();
                break;
            case 2:
                deleteDiagnosesByPatientId();
                break;
            case 3:
                deleteAllDiagnoses();
                break;
            case 0: // Back
                System.out.println("Returning back...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void deleteDiagnosisById() {
        String diagnosisId = getValidInput("Enter Diagnosis ID to delete: ");
        if (diagnosisId == null) {
            return;
        }

        Diagnosis diagnosis = md.findDiagnosisById(diagnosisId);
        if (diagnosis == null) {
            System.out.println("Diagnosis with ID '" + diagnosisId + "' not found.");
            return;
        }

        System.out.println("Diagnosis to be deleted:");
        System.out.println(diagnosis);

        if (confirmAction("Are you sure you want to delete this diagnosis?")
                && md.deleteDiagnosisById(diagnosisId)) {
            System.out.println("Diagnosis deleted successfully!");
        } else {
            System.out.println("Deletion cancelled or failed.");
        }
    }

    private void deleteDiagnosesByPatientId() {
        String patientId = getValidInput("Enter Patient ID: ");
        if (patientId == null) {
            return;
        }

        ListInterface<Diagnosis> patientDiagnoses = md.findDiagnosesByPatientId(patientId);
        if (patientDiagnoses.isEmpty()) {
            System.out.println("No diagnoses found for patient ID: " + patientId);
            return;
        }

        displayDiagnosisSummary(patientDiagnoses, "Diagnoses to be deleted for patient " + patientId);

        if (confirmAction("Are you sure you want to delete ALL diagnoses for this patient?")) {
            int deletedCount = md.deleteDiagnosesByPatientId(patientId);
            System.out.println("Deleted " + deletedCount + " diagnoses successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void deleteAllDiagnoses() {
        ListInterface<Diagnosis> allDiagnoses = md.getAllDiagnoses();
        if (allDiagnoses.isEmpty()) {
            System.out.println("No diagnoses to delete.");
            return;
        }

        System.out.println("Total diagnoses to be deleted: " + allDiagnoses.getNumberOfEntries());

        if (confirmAction("Are you sure you want to delete ALL diagnoses? This cannot be undone!")) {
            System.out.print("Type 'DELETE ALL' to confirm: ");
            String finalConfirmation = scanner.nextLine();

            if (finalConfirmation.equals("DELETE ALL") && md.deleteAllDiagnoses()) {
                System.out.println("All diagnoses deleted successfully!");
            } else {
                System.out.println("Deletion cancelled or failed.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ===== DISPLAY METHODS USING ADT OPERATIONS =====
    private void displayDiagnosisList(ListInterface<Diagnosis> diagnoses, String title) {
        if (diagnoses.isEmpty()) {
            System.out.println("No diagnoses found.");
            return;
        }

        System.out.println("\n==================================");
        System.out.println("           " + title + "           ");
        System.out.println("==================================");
        for (int i = 1; i <= diagnoses.getNumberOfEntries(); i++) {
            System.out.println(diagnoses.getEntry(i));
        }
        System.out.println("Total diagnoses: " + diagnoses.getNumberOfEntries());
    }

    private void displayDiagnosisSummary(ListInterface<Diagnosis> diagnoses, String title) {
        System.out.println("\n" + title + ":");
        for (int i = 1; i <= diagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnoses.getEntry(i);
            System.out.println(i + ". " + diagnosis.getDiagnosisId() + " - " + diagnosis.getDiagnosisDetails());
        }
    }

    // ===== INPUT UTILITY METHODS USING ADT =====
    private ListInterface<String> getSymptomsInput() {
        System.out.print("Enter symptoms: ");
        String symptomInput = scanner.nextLine();
        return md.processSymptoms(symptomInput);
    }

    private String getDiagnosisDetailsInput() {
        System.out.print("Enter diagnosis details: ");
        return scanner.nextLine();
    }

    private ListInterface<String> getUpdatedSymptoms() {
        System.out.print("Enter new symptoms: ");
        String symptomInput = scanner.nextLine();
        return md.isValidInput(symptomInput) ? md.processSymptoms(symptomInput) : null;
    }

    private String getUpdatedDiagnosisDetails() {
        System.out.print("Enter new diagnosis details: ");
        String diagnosisDetails = scanner.nextLine();
        return md.isValidInput(diagnosisDetails) ? diagnosisDetails : null;
    }

    private String selectFromList(ListInterface<String> options, String itemType, boolean allowSkip) {
        displayListOptions(itemType.substring(0, 1).toUpperCase() + itemType.substring(1) + " Options", options);

        try {
            String prompt = allowSkip
                    ? "Select " + itemType + " by number (1-" + options.getNumberOfEntries() + ") or 0 to skip: "
                    : "Choose option (1-" + options.getNumberOfEntries() + "): ";

            System.out.print(prompt);
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (allowSkip && choice == 0) {
                return null;
            }

            if (choice >= 1 && choice <= options.getNumberOfEntries()) {
                return options.getEntry(choice);
            } else {
                System.out.println("Invalid choice! Please select a number between 1-" + options.getNumberOfEntries());
                return selectFromList(options, itemType, allowSkip);
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
            return selectFromList(options, itemType, allowSkip);
        }
    }

    private void displayMenuOptions(ListInterface<String> options) {
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
        System.out.println("0. Back to Treatment Management Menu");
        System.out.print("Enter choice: ");
    }

    private void displayListOptions(String title, ListInterface<String> options) {
        System.out.println(title + ":");
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
    }

    private int getMenuChoice(int maxChoice) {
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            return (choice >= 0 && choice <= maxChoice) ? choice : -1;
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
            return -1;
        }
    }

    private String getValidInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private int getPositiveNumber(String prompt) {
        try {
            System.out.print(prompt);
            int number = scanner.nextInt();
            scanner.nextLine();
            return number > 0 ? number : -1;
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
        return askYesNo(message);
    }

    // ===== TREATMENT INTEGRATION METHODS =====
    private boolean createTreatmentWithMedicines(String patientId, String doctorId, String status,
            LocalDate treatmentDate, ListInterface<TreatmentMedicineList> medicines) {
        return medicines.isEmpty()
                ? mt.createTreatment(patientId, doctorId, status, treatmentDate)
                : mt.createTreatmentWithMedicines(patientId, doctorId, status, treatmentDate, medicines);
    }

    private void displayTreatmentCreationResult(String status, LocalDate treatmentDate,
            ListInterface<TreatmentMedicineList> medicines, String patientId, String doctorId) {
        System.out.println("Treatment created automatically!");
        System.out.println("Treatment Status: " + status);
        System.out.println("Treatment Date: " + treatmentDate);

        if (status.equals("Completed")) {
            System.out.println("\n(Treatment marked as completed - no medicines required)");
        } else {
            System.out.println("\n(Treatment pending - patient needs to collect medicines from pharmacy)");
        }
        System.out.println("(Doctor consultation finished - doctor is now available)");

        ListInterface<Treatment> patientTreatments = mt.findTreatmentsByPatientAndDoctor(patientId, doctorId);
        if (!patientTreatments.isEmpty()) {
            Treatment latestTreatment = patientTreatments.getEntry(patientTreatments.getNumberOfEntries());
            System.out.println("Created Treatment ID: " + latestTreatment.getTreatmentId());

            if (!medicines.isEmpty()) {
                System.out.println("Medicines prescribed: " + medicines.getNumberOfEntries());
                System.out.println("Patient should collect medicines from pharmacy to complete treatment.");
            }
        }
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
}
