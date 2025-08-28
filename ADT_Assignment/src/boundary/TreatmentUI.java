// Lee Siu Yee

package boundary;

import adt.DoublyLinkedList;
import adt.ListInterface;
import control.MaintainTreatment;
import control.MaintainDiagnosis;
import entity.Diagnosis;
import entity.DiagnosisDistribution;
import entity.MedicineDistribution;
import entity.ServingStatusData;
import entity.Treatment;
import entity.TreatmentMedicineList;
import entity.TreatmentReportData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class TreatmentUI {

    private final Scanner scanner;
    private final MaintainTreatment maintainTreatment;
    private final MaintainDiagnosis maintainDiagnosis;
    private static DiagnosisUI diagnosisUI = new DiagnosisUI();

    public TreatmentUI(MaintainTreatment maintainTreatment) {
        this.scanner = new Scanner(System.in);
        this.maintainTreatment = maintainTreatment;
        this.maintainDiagnosis = new MaintainDiagnosis();
    }

    // ================================
    // MAIN MENU METHODS
    // ================================
    public void run() {
        int choice;
        do {
            displayMainMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                processMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("Error occurred...");
                scanner.nextLine();
                choice = -1;
            }
        } while (choice != 0);
    }

    public void displayMainMenu() {
        System.out.println("\n==================================");
        System.out.println("     TREATMENT MANAGEMENT MENU    ");
        System.out.println("==================================");
        System.out.println("1. Manage Diagnosis");
        System.out.println("2. Manage Treatment");
        System.out.println("3. Generate Treatment Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");
    }

    private void processMenuChoice(int choice) {
        switch (choice) {
            case 1:
                diagnosisUI.displayDiagnosisMenu();
                break;
            case 2:
                displayTreatmentMenu();
                break;
            case 3:
                generateTreatmentReport();
                break;
            case 0:
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }

    // ================================
    // SEARCH MENU USING ADT
    // ================================
    public void displaySearchMenu() {
        int choice;
        do {
            System.out.println("\n==============================");
            System.out.println("     SEARCH TREATMENTS        ");
            System.out.println("==============================");
            System.out.println("1. Search by Medicine");
            System.out.println("2. Search by Treatment ID");
            System.out.println("3. Search Patient Treatment History");
            System.out.println("0. Back to Treatment Menu");
            System.out.print("Enter choice: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        searchByMedicineSelection();
                        break;
                    case 2:
                        searchByTreatmentId();
                        break;
                    case 3:
                        searchPatientHistory();
                        break;
                    case 0:
                        System.out.println("Returning to Treatment Menu...");
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

    // ================================
    // SEARCH METHODS USING LINEAR SEARCH
    // ================================
    /**
     * Search treatments by selecting medicine from numbered list Shows all
     * medicines used in treatments for user selection
     */
    private void searchByMedicineSelection() {
        System.out.println("\n===============================");
        System.out.println("     SEARCH BY MEDICINE        ");
        System.out.println("===============================");

        // Get all medicines used in treatments
        ListInterface<String> medicinesUsed = maintainTreatment.getAllMedicinesUsedInTreatments();

        if (medicinesUsed.isEmpty()) {
            System.out.println("No medicines found in any treatments.");
            return;
        }

        // Display medicines with numbering
        System.out.println("Select medicine from the list:");
        for (int i = 1; i <= medicinesUsed.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + medicinesUsed.getEntry(i));
        }
        System.out.println("0. Back");

        try {
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return;
            }

            if (choice >= 1 && choice <= medicinesUsed.getNumberOfEntries()) {
                String selectedMedicine = medicinesUsed.getEntry(choice);
                System.out.println("Searching treatments with medicine: " + selectedMedicine);

                ListInterface<Treatment> foundTreatments = maintainTreatment.searchTreatmentsBySelectedMedicine(selectedMedicine);

                if (foundTreatments.isEmpty()) {
                    System.out.println("No treatments found with medicine: " + selectedMedicine);
                } else {
                    displayTreatmentList(foundTreatments, "TREATMENTS WITH MEDICINE: " + selectedMedicine.toUpperCase());
                }
            } else {
                System.out.println("Invalid choice! Please select a number from the list.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
        }
    }

    /**
     * Search treatment by treatment ID Simple and direct search for specific
     * treatment
     */
    private void searchByTreatmentId() {
        System.out.println("\n===============================");
        System.out.println("     SEARCH BY TREATMENT ID    ");
        System.out.println("===============================");

        String treatmentId = getValidInput("Enter Treatment ID to search: ");
        if (treatmentId == null) {
            return;
        }

        System.out.println("Searching for treatment: " + treatmentId);
        ListInterface<Treatment> foundTreatments = maintainTreatment.searchTreatmentById(treatmentId);

        if (foundTreatments.isEmpty()) {
            System.out.println("No treatment found with ID: " + treatmentId);
        } else {
            System.out.println("\n==========================");
            System.out.println("     TREATMENT FOUND       ");
            System.out.println("==========================");
            displayTreatmentDetails(foundTreatments.getEntry(1));
        }
    }

    /**
     * Search patient treatment history Shows all treatments for a patient in
     * recent-first order
     */
    private void searchPatientHistory() {
        System.out.println("\n===============================");
        System.out.println("   PATIENT TREATMENT HISTORY   ");
        System.out.println("===============================");

        String patientId = getValidInput("Enter Patient ID: ");
        if (patientId == null) {
            return;
        }

        System.out.println("Searching treatment history for Patient: " + patientId);

        ListInterface<Treatment> foundTreatments = maintainTreatment.searchPatientTreatmentHistory(patientId);

        if (foundTreatments.isEmpty()) {
            System.out.println("No treatment history found for Patient: " + patientId);
        } else {
            System.out.println("\nFound " + foundTreatments.getNumberOfEntries()
                    + " treatments (showing most recent first):");
            displayTreatmentList(foundTreatments, "TREATMENT HISTORY - PATIENT: " + patientId);
        }
    }

    // ================================
    // TREATMENT MANAGEMENT MENU
    // ================================
    public void displayTreatmentMenu() {
        int choice;
        do {
            System.out.println("\n==============================");
            System.out.println("     TREATMENT MANAGEMENT     ");
            System.out.println("==============================");
            System.out.println("1. View Treatment");
            System.out.println("2. Update Treatment");
            System.out.println("3. Delete Treatment");
            System.out.println("4. Search Treatment");
            System.out.println("0. Back to Treatment Management Menu");
            System.out.print("Enter choice: ");
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        viewTreatment();
                        break;
                    case 2:
                        updateTreatment();
                        break;
                    case 3:
                        deleteTreatment();
                        break;
                    case 4:
                        displaySearchMenu();
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

    // ================================
    // VIEW TREATMENT METHODS USING ADT
    // ================================
    public void viewTreatment() {
        System.out.println("\n==========================");
        System.out.println("     VIEW TREATMENT     ");
        System.out.println("==========================");

        ListInterface<String> viewOptions = createViewOptions();
        int choice = selectFromOptions("Select view option", viewOptions);

        if (choice == -1 || choice == 0) {
            return;
        }

        switch (choice) {
            case 1:
                viewAllTreatments();
                break;
            case 2:
                viewTreatmentById();
                break;
            case 3:
                viewTreatmentsByPatientId();
                break;
            case 4:
                viewTreatmentsByDoctorId();
                break;
            case 5:
                viewTreatmentsByStatus();
                break;
            case 0: // Back
                System.out.println("Returning back...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private ListInterface<String> createViewOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("View all treatments");
        options.add("View treatment by ID");
        options.add("View treatments by Patient ID");
        options.add("View treatments by Doctor ID");
        options.add("View treatments by Status");
        return options;
    }

    private void viewAllTreatments() {
        ListInterface<Treatment> treatments = maintainTreatment.getAllTreatments();
        displayTreatmentList(treatments, "ALL TREATMENTS");
    }

    private void viewTreatmentById() {
        String treatmentId = getValidInput("Enter Treatment ID: ");
        if (treatmentId == null) {
            return;
        }

        Treatment treatment = maintainTreatment.findTreatmentById(treatmentId);
        if (treatment != null) {
            System.out.println("\n==========================");
            System.out.println("     TREATMENT FOUND    ");
            System.out.println("==========================");
            displayTreatmentDetails(treatment);
        } else {
            System.out.println("Treatment with ID '" + treatmentId + "' not found.");
        }
    }

    private void viewTreatmentsByPatientId() {
        String patientId = getValidInput("Enter Patient ID: ");
        if (patientId == null) {
            return;
        }

        ListInterface<Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patientId);
        displayTreatmentList(patientTreatments, "TREATMENTS FOR PATIENT " + patientId);
    }

    private void viewTreatmentsByDoctorId() {
        String doctorId = getValidInput("Enter Doctor ID: ");
        if (doctorId == null) {
            return;
        }

        ListInterface<Treatment> doctorTreatments = maintainTreatment.findTreatmentsByDoctorId(doctorId);
        displayTreatmentList(doctorTreatments, "TREATMENTS FOR DOCTOR " + doctorId);
    }

    private void viewTreatmentsByStatus() {
        String status = selectFromStatusOptions("Select Status");
        if (status == null) {
            return;
        }

        ListInterface<Treatment> statusTreatments = maintainTreatment.findTreatmentsByStatus(status);
        displayTreatmentList(statusTreatments, "TREATMENTS WITH STATUS: " + status.toUpperCase());
    }

    // ================================
    // UPDATE TREATMENT METHODS USING ADT
    // ================================
    public void updateTreatment() {
        System.out.println("\n==========================");
        System.out.println("     UPDATE TREATMENT    ");
        System.out.println("==========================");

        String treatmentId = getValidInput("Enter Treatment ID to update: ");
        if (treatmentId == null) {
            return;
        }

        Treatment existingTreatment = maintainTreatment.findTreatmentById(treatmentId);
        if (existingTreatment == null) {
            System.out.println("Treatment with ID '" + treatmentId + "' not found.");
            return;
        }

        System.out.println("Current treatment details:");
        displayTreatmentDetails(existingTreatment);

        ListInterface<String> updateOptions = createUpdateOptions();
        int choice = selectFromOptions("What would you like to update?", updateOptions);

        if (choice == -1 || choice == 0) {
            return;
        }

        switch (choice) {
            case 1:
                updateTreatmentStatus(treatmentId);
                break;
            case 2:
                updateTreatmentDate(treatmentId);
                break;
            case 3:
                addMedicineToTreatment(treatmentId);
                break;
            case 4:
                updateAllMedicines(treatmentId);
                break;
            case 0: // Back
                System.out.println("Returning back...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private ListInterface<String> createUpdateOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("Update Status");
        options.add("Update Treatment Date");
        options.add("Add Medicine");
        options.add("Update All Medicines");
        return options;
    }

    private void updateTreatmentStatus(String treatmentId) {
        String newStatus = selectFromStatusOptions("Select new status");
        if (newStatus == null) {
            return;
        }

        boolean success = maintainTreatment.updateTreatmentStatus(treatmentId, newStatus);
        if (success) {
            System.out.println("Treatment status updated successfully to: " + newStatus);
        } else {
            System.out.println("Failed to update treatment status.");
        }
    }

    private void updateTreatmentDate(String treatmentId) {
        LocalDate newDate = getTreatmentDate();
        if (newDate == null) {
            return;
        }

        boolean success = maintainTreatment.updateTreatmentDate(treatmentId, newDate);
        if (success) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            System.out.println("Treatment date updated successfully to: " + newDate.format(formatter));
        } else {
            System.out.println("Failed to update treatment date.");
        }
    }

    private void addMedicineToTreatment(String treatmentId) {
        System.out.println("\n====================================");
        System.out.println("     ADD MEDICINE TO TREATMENT    ");
        System.out.println("====================================");

        TreatmentMedicineList medicine = createSingleMedicine(treatmentId);
        if (medicine == null) {
            return;
        }

        boolean success = maintainTreatment.addMedicineToTreatment(treatmentId, medicine);
        if (success) {
            System.out.println("Medicine added to treatment successfully!");
        } else {
            System.out.println("Failed to add medicine to treatment.");
        }
    }

    private void updateAllMedicines(String treatmentId) {
        System.out.println("\n============================");
        System.out.println("     UPDATE ALL MEDICINES    ");
        System.out.println("============================");
        System.out.println("This will replace all existing medicines for this treatment.");

        if (!askYesNo("Do you want to continue?")) {
            System.out.println("Operation cancelled.");
            return;
        }

        ListInterface<TreatmentMedicineList> newMedicines = createMedicineList();
        if (newMedicines.isEmpty()) {
            System.out.println("No medicines added. Update cancelled.");
            return;
        }

        boolean success = maintainTreatment.updateTreatmentMedicines(treatmentId, newMedicines);
        if (success) {
            System.out.println("All medicines updated successfully!");
        } else {
            System.out.println("Failed to update medicines.");
        }
    }

    // ================================
    // DELETE TREATMENT METHODS USING ADT
    // ================================
    public void deleteTreatment() {
        System.out.println("\n==========================");
        System.out.println("     DELETE TREATMENT    ");
        System.out.println("==========================");

        ListInterface<String> deleteOptions = createDeleteOptions();
        int choice = selectFromOptions("Select delete option", deleteOptions);

        if (choice == -1 || choice == 0) {
            return;
        }

        switch (choice) {
            case 1:
                deleteTreatmentById();
                break;
            case 2:
                deleteTreatmentsByPatientId();
                break;
            case 3:
                deleteTreatmentsByDoctorId();
                break;
            case 4:
                deleteAllTreatments();
                break;
            case 0: // Back
                System.out.println("Returning back...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private ListInterface<String> createDeleteOptions() {
        ListInterface<String> options = new DoublyLinkedList<>();
        options.add("Delete specific treatment by ID");
        options.add("Delete all treatments for a patient");
        options.add("Delete all treatments for a doctor");
        options.add("Delete all treatments");
        return options;
    }

    private void deleteTreatmentById() {
        String treatmentId = getValidInput("Enter Treatment ID to delete: ");
        if (treatmentId == null) {
            return;
        }

        Treatment treatment = maintainTreatment.findTreatmentById(treatmentId);
        if (treatment == null) {
            System.out.println("Treatment with ID '" + treatmentId + "' not found.");
            return;
        }

        System.out.println("Treatment to be deleted:");
        displayTreatmentDetails(treatment);

        if (askYesNo("Are you sure you want to delete this treatment?")) {
            boolean success = maintainTreatment.deleteTreatmentById(treatmentId);
            if (success) {
                System.out.println("Treatment deleted successfully!");
            } else {
                System.out.println("Failed to delete treatment.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void deleteTreatmentsByPatientId() {
        String patientId = getValidInput("Enter Patient ID: ");
        if (patientId == null) {
            return;
        }

        ListInterface<Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patientId);
        if (patientTreatments.isEmpty()) {
            System.out.println("No treatments found for patient ID: " + patientId);
            return;
        }

        displayTreatmentSummary(patientTreatments, "\nTreatments to be deleted for patient " + patientId);

        if (askYesNo("Are you sure you want to delete ALL treatments for this patient?")) {
            int deletedCount = maintainTreatment.deleteTreatmentsByPatientId(patientId);
            System.out.println("Deleted " + deletedCount + " treatments successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void deleteTreatmentsByDoctorId() {
        String doctorId = getValidInput("Enter Doctor ID: ");
        if (doctorId == null) {
            return;
        }

        ListInterface<Treatment> doctorTreatments = maintainTreatment.findTreatmentsByDoctorId(doctorId);
        if (doctorTreatments.isEmpty()) {
            System.out.println("No treatments found for doctor ID: " + doctorId);
            return;
        }

        displayTreatmentSummary(doctorTreatments, "\nTreatments to be deleted for doctor " + doctorId);

        if (askYesNo("Are you sure you want to delete ALL treatments for this doctor?")) {
            int deletedCount = maintainTreatment.deleteTreatmentsByDoctorId(doctorId);
            System.out.println("Deleted " + deletedCount + " treatments successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void deleteAllTreatments() {
        ListInterface<Treatment> allTreatments = maintainTreatment.getAllTreatments();
        if (allTreatments.isEmpty()) {
            System.out.println("No treatments to delete.");
            return;
        }

        System.out.println("Total treatments to be deleted: " + allTreatments.getNumberOfEntries());

        if (!askYesNo("Are you sure you want to delete ALL treatments? This cannot be undone!")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        System.out.print("Type 'DELETE ALL' to confirm: ");
        String finalConfirmation = scanner.nextLine();

        if (finalConfirmation.equals("DELETE ALL")) {
            boolean success = maintainTreatment.deleteAllTreatments();
            if (success) {
                System.out.println("All treatments deleted successfully!");
            } else {
                System.out.println("Failed to delete all treatments.");
            }
        } else {
            System.out.println("Deletion cancelled - incorrect confirmation text.");
        }
    }

    // ================================
    // SERVING STATUS DISPLAY
    // ================================
    public void displayServingStatus() {
        System.out.println("\n=== CURRENT SERVING STATUS ===");
        ServingStatusData statusData = maintainTreatment.getServingStatusData();
        if (statusData.hasActiveConsultation()) {
            System.out.println("Currently serving:");
            System.out.println("  Patient: " + statusData.getCurrentPatient().getId() + " (" + statusData.getCurrentPatient().getName() + ")");
            System.out.println("  Doctor: " + statusData.getCurrentDoctor().getId() + " (" + statusData.getCurrentDoctor().getName() + ")");
        } else {
            System.out.println("No active consultation.");
        }
        System.out.println("Waiting: " + statusData.getWaitingPatients() + " patients");
        System.out.println("===============================\n");
    }

    // ================================
    // MEDICINE CREATION METHODS USING ADT
    // ================================
    private ListInterface<TreatmentMedicineList> createMedicineList() {
        ListInterface<TreatmentMedicineList> medicines = new DoublyLinkedList<>();

        System.out.println("\n========================");
        System.out.println("     ADD MEDICINES    ");
        System.out.println("========================");

        if (maintainTreatment.getAvailableMedicines().isEmpty()) {
            System.out.println("No available medicines in the pharmacy.");
            return medicines;
        }

        displayMedicineOptions();

        while (true) {
            String prescription = selectMedicine();
            if (prescription == null) {
                break;
            }

            int tablets = getPositiveNumber("Enter number of tablets: ");
            if (tablets == -1) {
                continue;
            }

            String mealTiming = selectFromMealTimingOptions("Meal timing");
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

            TreatmentMedicineList medicine = new TreatmentMedicineList("", prescription, tablets, mealTiming, timesPerDay, totalDay);
            medicines.add(medicine);

            System.out.println("Medicine added successfully!");

            if (!askYesNo("Add another medicine?")) {
                break;
            }
        }

        return medicines;
    }

    private TreatmentMedicineList createSingleMedicine(String treatmentId) {
        String prescription = selectMedicine();
        if (prescription == null) {
            return null;
        }

        int tablets = getPositiveNumber("Enter number of tablets: ");
        if (tablets == -1) {
            return null;
        }

        String mealTiming = selectFromMealTimingOptions("Meal timing");
        if (mealTiming == null) {
            return null;
        }

        int timesPerDay = getPositiveNumber("Enter times per day: ");
        if (timesPerDay == -1) {
            return null;
        }

        int totalDay = getPositiveNumber("Enter total day taking medicine: ");
        if (totalDay == -1) {
            return null;
        }

        return new TreatmentMedicineList(treatmentId, prescription, tablets, mealTiming, timesPerDay, totalDay);
    }

    // ================================
    // DISPLAY METHODS USING ADT
    // ================================
    private void displayTreatmentList(ListInterface<Treatment> treatments, String title) {
        if (treatments.isEmpty()) {
            System.out.println("No treatments found.");
            return;
        }

        System.out.println("\n=== " + title + " ===");
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            displayTreatmentDetails(treatment);
        }
        System.out.println("Total treatments: " + treatments.getNumberOfEntries());
    }

    private void displayTreatmentSummary(ListInterface<Treatment> treatments, String title) {
        System.out.println(title + ":");
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            System.out.println(i + ". " + treatment.getTreatmentId() + " - " + treatment.getStatus());
        }
    }

    private void displayTreatmentDetails(Treatment treatment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("------------------------");
        System.out.println("Treatment Information:");
        System.out.println("------------------------");
        System.out.println("Treatment ID     : " + treatment.getTreatmentId());
        System.out.println("Patient ID       : " + treatment.getPatientId());
        System.out.println("Doctor ID        : " + treatment.getDoctorId());
        System.out.println("Status           : " + treatment.getStatus());
        System.out.println("Treatment Date   : " + (treatment.getTreatmentDate() != null
                ? treatment.getTreatmentDate().format(formatter) : "Not set"));

        ListInterface<TreatmentMedicineList> medicines = treatment.getMedicineList();
        if (medicines != null && !medicines.isEmpty()) {
            System.out.println("Medicines        :");
            for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
                TreatmentMedicineList medicine = medicines.getEntry(i);
                System.out.println("  " + i + ". " + medicine.getPrescription()
                        + " - " + medicine.getTablets() + " tablets, "
                        + medicine.getTimesPerDay() + " times per day, "
                        + medicine.getMealTiming() + ", "
                        + medicine.getTotalDay() + " total days");
            }
        } else {
            System.out.println("Medicines        : No medicines prescribed");
        }
        System.out.println();
    }

    private void displayMedicineOptions() {
        System.out.println("Available Medicines:");
        ListInterface<String> availableMedicines = maintainTreatment.getAvailableMedicines();
        for (int i = 1; i <= availableMedicines.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + availableMedicines.getEntry(i));
        }
    }

    // ================================
    // INPUT UTILITY METHODS USING ADT
    // ================================
    private String getValidInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("Input cannot be empty!");
            return null;
        }
        return input;
    }

    private int getPositiveNumber(String prompt) {
        try {
            System.out.print(prompt);
            int number = scanner.nextInt();
            scanner.nextLine();
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

    private LocalDate getTreatmentDate() {
        System.out.print("Enter Treatment Date (dd/MM/yyyy) or press Enter for today's date: ");
        String dateInput = scanner.nextLine().trim();

        if (dateInput.isEmpty()) {
            return LocalDate.now();
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateInput, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Using today's date.");
            return LocalDate.now();
        }
    }

    // Helper method for search functionality date input
    private LocalDate getDateInput(String prompt) {
        System.out.print(prompt);
        String dateInput = scanner.nextLine().trim();

        if (dateInput.isEmpty()) {
            System.out.println("Date input cannot be empty!");
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateInput, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use dd/MM/yyyy format.");
            return null;
        }
    }

    // ================================
    // SELECTION UTILITY METHODS USING ADT
    // ================================
    private int selectFromOptions(String title, ListInterface<String> options) {
        System.out.println(title + ":");
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }
        System.out.println("0. Back");

        try {
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice >= 0 && choice <= options.getNumberOfEntries()) {
                return choice;
            } else {
                System.out.println("Invalid choice!");
                return -1;
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
            return -1;
        }
    }

    private String selectFromStatusOptions(String title) {
        ListInterface<String> statusOptions = maintainTreatment.getStatusOptions();
        System.out.println(title + ":");
        for (int i = 1; i <= statusOptions.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + statusOptions.getEntry(i));
        }
        System.out.println("0. Back");

        try {
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return null;
            }

            if (choice >= 1 && choice <= statusOptions.getNumberOfEntries()) {
                return statusOptions.getEntry(choice);
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

    private String selectFromMealTimingOptions(String title) {
        ListInterface<String> mealTimingOptions = maintainTreatment.getMealTimingOptions();
        System.out.println(title + ":");
        for (int i = 1; i <= mealTimingOptions.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + mealTimingOptions.getEntry(i));
        }
        System.out.println("0. Back");

        try {
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return null;
            }

            if (choice >= 1 && choice <= mealTimingOptions.getNumberOfEntries()) {
                return mealTimingOptions.getEntry(choice);
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

    private String selectMedicine() {
        ListInterface<String> availableMedicines = maintainTreatment.getAvailableMedicines();

        try {
            System.out.print("Select medicine by number (1-" + availableMedicines.getNumberOfEntries() + ") or 0 to skip: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return null;
            }

            if (choice >= 1 && choice <= availableMedicines.getNumberOfEntries()) {
                return availableMedicines.getEntry(choice);
            } else {
                System.out.println("Invalid choice! Please select a number between 1-" + availableMedicines.getNumberOfEntries());
                return selectMedicine();
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
            return selectMedicine();
        }
    }

    // ================================
    // REPORT GENERATION METHODS USING MERGE SORT
    // ================================
    /**
     * Display report generation menu and handle user selection
     */
    public void generateTreatmentReport() {
        int choice;
        do {
            System.out.println("\n==============================");
            System.out.println("     TREATMENT REPORTS        ");
            System.out.println("==============================");
            System.out.println("1. Treatment Summary Report");
            System.out.println("2. Treatment Distribution Report");
            System.out.println("0. Back to Treatment Menu");
            System.out.print("Enter choice: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        displayTreatmentSummaryReport();
                        break;
                    case 2:
                        displayTreatmentDistributionReport();
                        break;
                    case 0:
                        System.out.println("Returning to Treatment Menu...");
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

    /**
     * Display Treatment Summary Report using mergeSort sorted data Shows
     * comprehensive treatment statistics and insights
     */
    private void displayTreatmentSummaryReport() {
        System.out.println("\nGenerating Treatment Summary Report...");
        TreatmentReportData reportData = maintainTreatment.generateTreatmentSummaryReportData();

        if (reportData.getTotalTreatments() == 0) {
            System.out.println("No treatment data available to generate report.");
            return;
        }

        displayTreatmentSummaryHeader();
        displayTreatmentTable(reportData);
        displaySummaryStatistics(reportData);
        displayKeyInsights(reportData);
        displayTreatmentSummaryFooter();
    }

    /**
     * Display Treatment Distribution Report using mergeSort sorted data Shows
     * distribution patterns with visual representations
     */
    private void displayTreatmentDistributionReport() {
        System.out.println("\nGenerating Treatment Distribution Report...");
        TreatmentReportData reportData = maintainTreatment.generateTreatmentDistributionReportData();

        if (reportData.getDiagnosisDistributions() == null || reportData.getDiagnosisDistributions().isEmpty()) {
            System.out.println("No treatment data available to generate distribution report.");
            return;
        }

        displayDistributionHeader();
        displayDiagnosisDistribution(reportData);
        displayMedicineDistribution(reportData);
        displayTreatmentTypeDistribution(reportData);
        displayDistributionFooter();
    }

    // ================================
    // TREATMENT SUMMARY REPORT DISPLAY METHODS
    // ================================
    private void displayTreatmentSummaryHeader() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                    TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.println("                                      CLINIC MANAGEMENT SYSTEM");
        System.out.println("=".repeat(100));
        System.out.println("                                      TREATMENT SUMMARY REPORT");
        System.out.println("=".repeat(100));
        System.out.println("Generated at: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a")));
        System.out.println("-".repeat(100));
        System.out.println();
    }

    private void displayTreatmentTable(TreatmentReportData reportData) {
        System.out.println("-".repeat(100));
        System.out.println("TreatmentID   | PatientID   | Diagnosis    | DoctorID    | Medicines          | Status   ");
        System.out.println("-".repeat(100));
        // Get all treatments for table display
        ListInterface<Treatment> allTreatments = maintainTreatment.getAllTreatments();

        for (int i = 1; i <= allTreatments.getNumberOfEntries(); i++) {
            Treatment treatment = allTreatments.getEntry(i);

            // Get diagnosis for this treatment
            String diagnosisName = getDiagnosisForTreatment(treatment);

            // Get medicines list
            String medicinesStr = getMedicinesStringForTreatment(treatment);

            // Format and display row
            System.out.printf("%-13s | %-11s | %-12s | %-11s | %-18s | %-10s%n",
                    treatment.getTreatmentId(),
                    treatment.getPatientId(),
                    truncateString(diagnosisName, 12),
                    treatment.getDoctorId(),
                    truncateString(medicinesStr, 38),
                    treatment.getStatus()
            );
        }
        System.out.println("-".repeat(100));
        System.out.println();
    }

    private void displaySummaryStatistics(TreatmentReportData reportData) {
        System.out.printf("Total patients treated        : %d%n", reportData.getTotalPatients());
        System.out.printf("Total treatments administered : %d%n", reportData.getTotalTreatments());
        System.out.printf("Total doctors involved        : %d%n", reportData.getTotalDoctors());
        System.out.println();
    }

    private void displayKeyInsights(TreatmentReportData reportData) {
        System.out.println("Key Insights:");

        if (reportData.getTopDoctor() != null) {
            System.out.printf("* Doctor with highest workload  : %s (%d treatments)%n",
                    reportData.getTopDoctor(), reportData.getTopDoctorTreatments());
        }

        if (reportData.getTopPatient() != null) {
            System.out.printf("* Patient with most treatments  : %s (%d treatments)%n",
                    reportData.getTopPatient(), reportData.getTopPatientTreatments());
        }

        if (reportData.getTopDiagnosis() != null) {
            System.out.printf("* Most common diagnosis         : %s (%d cases)%n",
                    reportData.getTopDiagnosis(), reportData.getTopDiagnosisCount());
        }

        if (reportData.getTopMedicine() != null) {
            System.out.printf("* Most prescribed medicine      : %s (%d times)%n",
                    reportData.getTopMedicine(), reportData.getTopMedicineCount());
        }
        System.out.println();
    }

    private void displayTreatmentSummaryFooter() {
        System.out.println("-".repeat(100));
        System.out.println("                                        END OF REPORT");
        System.out.println("=".repeat(100));
    }

    // ================================
    // TREATMENT DISTRIBUTION REPORT DISPLAY METHODS
    // ================================
    private void displayDistributionHeader() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                    TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.println("                                      CLINIC MANAGEMENT SYSTEM");
        System.out.println("=".repeat(100));
        System.out.println("                                    TREATMENT DISTRIBUTION REPORT");
        System.out.println("=".repeat(100));
        System.out.println("Generated at: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a")));
        System.out.println("-".repeat(100));
        System.out.println();
    }

    private void displayDiagnosisDistribution(TreatmentReportData reportData) {
        System.out.println("-".repeat(100));
        System.out.println("Diagnosis Distribution");
        System.out.println("-".repeat(100));

        ListInterface<DiagnosisDistribution> distributions = reportData.getDiagnosisDistributions();

        if (distributions != null && !distributions.isEmpty()) {
            for (int i = 1; i <= distributions.getNumberOfEntries(); i++) {
                DiagnosisDistribution dist = distributions.getEntry(i);
                String visualBar = generateVisualBar(dist.getCount());
                System.out.printf("%-20s [%-40s] %d (%.0f%%)%n",
                        truncateString(dist.getDiagnosisName(), 20),
                        visualBar,
                        dist.getCount(),
                        dist.getPercentage()
                );
            }

            System.out.println("Scale: # = 1 treatment");
            System.out.println("-".repeat(100));

            // Add insight for top diagnosis
            if (distributions.getNumberOfEntries() > 0) {
                DiagnosisDistribution top = distributions.getEntry(1);
                System.out.printf("* %s is the most common diagnosis, making up %.0f%% of all cases.%n",
                        top.getDiagnosisName(), top.getPercentage());
            }
        }
        System.out.println();
        System.out.println();
    }

    private void displayMedicineDistribution(TreatmentReportData reportData) {
        System.out.println("-".repeat(100));
        System.out.println("Medicine Distribution");
        System.out.println("-".repeat(100));

        ListInterface<MedicineDistribution> distributions = reportData.getMedicineDistributions();

        if (distributions != null && !distributions.isEmpty()) {
            for (int i = 1; i <= distributions.getNumberOfEntries(); i++) {
                MedicineDistribution dist = distributions.getEntry(i);
                String visualBar = generateVisualBar(dist.getCount());
                System.out.printf("%-20s [%-40s] %d (%.0f%%)%n",
                        truncateString(dist.getMedicineName(), 20),
                        visualBar,
                        dist.getCount(),
                        dist.getPercentage()
                );
            }

            System.out.println("Scale: # = 1 prescription");
            System.out.println("-".repeat(100));

            // Add insight for top medicine
            if (distributions.getNumberOfEntries() > 0) {
                MedicineDistribution top = distributions.getEntry(1);
                System.out.printf("* %s is the most prescribed medicine, used in %.0f%% of treatments.%n",
                        top.getMedicineName(), top.getPercentage());
            }
        }
        System.out.println();
        System.out.println();
    }

    private void displayTreatmentTypeDistribution(TreatmentReportData reportData) {
        System.out.println("-".repeat(100));
        System.out.println("Treatment Type Distribution");
        System.out.println("-".repeat(100));

        int withMedicine = reportData.getTreatmentsWithMedicine();
        int withoutMedicine = reportData.getTreatmentsWithoutMedicine();
        int total = withMedicine + withoutMedicine;

        if (total > 0) {
            double withMedicinePercent = (withMedicine * 100.0) / total;
            double withoutMedicinePercent = (withoutMedicine * 100.0) / total;

            String withMedicineBar = generateVisualBar(withMedicine);
            String withoutMedicineBar = generateVisualBar(withoutMedicine);

            System.out.printf("%-20s [%-40s] %d (%.0f%%)%n",
                    "With Medicine", withMedicineBar, withMedicine, withMedicinePercent);
            System.out.printf("%-20s [%-40s] %d (%.0f%%)%n",
                    "Without Medicine", withoutMedicineBar, withoutMedicine, withoutMedicinePercent);

            System.out.println("Scale: # = 1 treatment");
            System.out.println("-".repeat(100));

            if (withMedicine > withoutMedicine) {
                System.out.println("* Most treatments include medicine, while only a few are without medication.");
                System.out.println();
            } else if (withoutMedicine > withMedicine) {
                System.out.println("* Most treatments are without medicine, focusing on non-pharmaceutical approaches.");
                System.out.println();
            } else {
                System.out.println("* Treatments are evenly split between with and without medicine.");
                System.out.println();
            }
        }
    }

    private void displayDistributionFooter() {
        System.out.println("-".repeat(100));
        System.out.println("                                        END OF REPORT");
        System.out.println("=".repeat(100));
    }

    // ================================
    // HELPER METHODS FOR REPORT DISPLAY
    // ================================
    /**
     * Get diagnosis name for a treatment by matching patient and doctor
     */
    private String getDiagnosisForTreatment(Treatment treatment) {
        ListInterface<Diagnosis> patientDiagnoses = maintainDiagnosis.findDiagnosesByPatientId(treatment.getPatientId());

        for (int i = 1; i <= patientDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = patientDiagnoses.getEntry(i);
            if (diagnosis.getDoctorId().equals(treatment.getDoctorId())) {
                return diagnosis.getDiagnosisDetails();
            }
        }
        return "-";
    }

    /**
     * Get medicines string for a treatment (comma-separated)
     */
    private String getMedicinesStringForTreatment(Treatment treatment) {
        ListInterface<TreatmentMedicineList> medicines = treatment.getMedicineList();

        if (medicines == null || medicines.isEmpty()) {
            return "-";
        }

        StringBuilder medicinesStr = new StringBuilder();
        for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
            if (i > 1) {
                medicinesStr.append(", ");
            }
            medicinesStr.append(medicines.getEntry(i).getPrescription());
        }

        return medicinesStr.toString();
    }

    /**
     * Generate visual bar representation for counts
     */
    private String generateVisualBar(int count) {
        StringBuilder bar = new StringBuilder();
        int maxBars = 10; // Maximum number of # symbols
        int barsToShow = Math.min(count, maxBars);

        for (int i = 0; i < barsToShow; i++) {
            bar.append("#");
        }

        // Fill remaining spaces
        for (int i = barsToShow; i < maxBars; i++) {
            bar.append(" ");
        }

        return bar.toString();
    }

    /**
     * Truncate string to specified length with ellipsis if needed
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "-";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

}
