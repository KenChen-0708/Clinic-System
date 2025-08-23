package boundary;

import adt.DoublyLinkedList;
import adt.ListInterface;
import control.MaintainTreatment;
import control.MaintainDiagnosis;
import entity.Treatment;
import entity.TreatmentMedicineList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class TreatmentUI {

    private final Scanner scanner;
    private final MaintainTreatment maintainTreatment;
    private final MaintainDiagnosis maintainDiagnosis;
    private static DiagnosisUI DUI = new DiagnosisUI();

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
        System.out.println("1. Diagnosis Management");
        System.out.println("2. Treatment Management");
        System.out.println("3. Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");
    }

    private void processMenuChoice(int choice) {
        switch (choice) {
            case 1:
                DUI.displayDiagnosisMenu();
                break;
            case 2:
                displayTreatmentMenu();
                break;
            case 3:
                displayTreatmentReport();
                break;
            case 0:
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("Invalid choice! Please try again.");
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
    // VIEW TREATMENT METHODS
    // ================================
    public void viewTreatment() {
        System.out.println("\n==========================");
        System.out.println("     VIEW TREATMENT     ");
        System.out.println("==========================");

        DoublyLinkedList<String> viewOptions = new DoublyLinkedList<>();
        viewOptions.add("View all treatments");
        viewOptions.add("View treatment by ID");
        viewOptions.add("View treatments by Patient ID");
        viewOptions.add("View treatments by Doctor ID");
        viewOptions.add("View treatments by Status");
        viewOptions.add("Back");

        int choice = selectFromNumberedOptions("Select view option", viewOptions);
        if (choice == -1) {
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
            case 6:
                return;
            default:
                System.out.println("Invalid option selected.");
                break;
        }
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

        DoublyLinkedList<Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patientId);
        displayTreatmentList(patientTreatments, "TREATMENTS FOR PATIENT " + patientId);
    }

    private void viewTreatmentsByDoctorId() {
        String doctorId = getValidInput("Enter Doctor ID: ");
        if (doctorId == null) {
            return;
        }

        DoublyLinkedList<Treatment> doctorTreatments = maintainTreatment.findTreatmentsByDoctorId(doctorId);
        displayTreatmentList(doctorTreatments, "TREATMENTS FOR DOCTOR " + doctorId);
    }

    private void viewTreatmentsByStatus() {
        String status = selectFromOptions("Select Status", maintainTreatment.getStatusOptions());
        if (status == null) {
            return;
        }

        DoublyLinkedList<Treatment> statusTreatments = maintainTreatment.findTreatmentsByStatus(status);
        displayTreatmentList(statusTreatments, "TREATMENTS WITH STATUS: " + status.toUpperCase());
    }

    // ================================
    // UPDATE TREATMENT METHODS
    // ================================
    public void updateTreatment() {
        System.out.println("\n==========================");
        System.out.println("     UPDATE TREATMENT    ");
        System.out.println("==========================");
        String treatmentId = getValidInput("Enter Treatment ID to update: ");
        System.out.print("\n");
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
        DoublyLinkedList<String> updateOptions = new DoublyLinkedList<>();
        updateOptions.add("Update Status");
        updateOptions.add("Update Treatment Date");
        updateOptions.add("Add Medicine");
        updateOptions.add("Update All Medicines");
        updateOptions.add("Back");
        int choice = selectFromNumberedOptions("What would you like to update?", updateOptions);
        if (choice == -1) {
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
            case 5:
                return;
            default:
                System.out.println("Invalid option selected.");
                break;
        }
    }

    private void updateTreatmentStatus(String treatmentId) {
        String newStatus = selectFromOptions("Select new status", maintainTreatment.getStatusOptions());
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

        DoublyLinkedList<TreatmentMedicineList> newMedicines = createMedicineList();
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
    // DELETE TREATMENT METHODS
    // ================================
    public void deleteTreatment() {

        System.out.println("\n==========================");
        System.out.println("     DELETE TREATMENT    ");
        System.out.println("==========================");

        DoublyLinkedList<String> deleteOptions = new DoublyLinkedList<>();
        deleteOptions.add("Delete specific treatment by ID");
        deleteOptions.add("Delete all treatments for a patient");
        deleteOptions.add("Delete all treatments for a doctor");
        deleteOptions.add("Delete all treatments");
        deleteOptions.add("Back");

        int choice = selectFromNumberedOptions("Select delete option", deleteOptions);
        if (choice == -1) {
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
            case 5:
                return;
            default:
                System.out.println("Invalid option selected.");
                break;
        }
    }

    private void deleteTreatmentById() {
        String treatmentId = getValidInput("Enter Treatment ID to delete: ");
        System.out.print("\n");
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

        DoublyLinkedList<Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patientId);
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

        DoublyLinkedList<Treatment> doctorTreatments = maintainTreatment.findTreatmentsByDoctorId(doctorId);
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
    // REPORT METHODS
    // ================================
    private void displayTreatmentReport() {
        System.out.println("\n==========================");
        System.out.println("     TREATMENT REPORT    ");
        System.out.println("==========================");
        String report = maintainTreatment.getTreatmentReport();
        System.out.println(report);

        // Wait for user input before returning to menu
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    // ================================
    // MEDICINE CREATION METHODS
    // ================================
    private DoublyLinkedList<TreatmentMedicineList> createMedicineList() {
        DoublyLinkedList<TreatmentMedicineList> medicines = new DoublyLinkedList<>();

        System.out.println("\n========================");
        System.out.println("     ADD MEDICINES    ");
        System.out.println("========================");

        if (maintainTreatment.getAvailableMedicines().isEmpty()) {
            System.out.println("No available medicines in the pharmacy.");
            return medicines;
        }

        // Display available medicines
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

            String mealTiming = selectFromOptions("Meal timing", maintainTreatment.getMealTimingOptions());
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

        String mealTiming = selectFromOptions("Meal timing", maintainTreatment.getMealTimingOptions());
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
    // DISPLAY METHODS
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

    private void displayTreatmentSummary(DoublyLinkedList<Treatment> treatments, String title) {
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

        DoublyLinkedList<TreatmentMedicineList> medicines = treatment.getMedicineList();
        if (medicines != null && !medicines.isEmpty()) {
            System.out.print("Medicines        : ");
            for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
                TreatmentMedicineList medicine = medicines.getEntry(i);
                System.out.println("  " + i + ". " + medicine.getPrescription()
                        + " - " + medicine.getTablets() + " tablets, "
                        + medicine.getTimesPerDay() + " times per day, "
                        + medicine.getMealTiming() + ", "
                        + medicine.getTotalDay() + " total day\n");
            }
        } else {
            System.out.println("Medicines        : No medicines prescribed\n");
        }
    }

    private void displayMedicineOptions() {
        System.out.println("Available Medicines:");
        DoublyLinkedList<String> availableMedicines = maintainTreatment.getAvailableMedicines();
        for (int i = 1; i <= availableMedicines.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + availableMedicines.getEntry(i));
        }
    }

    // ================================
    // INPUT UTILITY METHODS
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

    // ================================
    // SELECTION UTILITY METHODS
    // ================================
    private String selectFromOptions(String title, DoublyLinkedList<String> options) {
        System.out.println(title + ":");
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }

        try {
            System.out.print("Choose option (1-" + options.getNumberOfEntries() + "): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice >= 1 && choice <= options.getNumberOfEntries()) {
                return options.getEntry(choice);
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

    private int selectFromNumberedOptions(String title, DoublyLinkedList<String> options) {
        System.out.println(title + ":");
        for (int i = 1; i <= options.getNumberOfEntries(); i++) {
            System.out.println(i + ". " + options.getEntry(i));
        }

        try {
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice >= 1 && choice <= options.getNumberOfEntries()) {
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

    private String selectMedicine() {
        DoublyLinkedList<String> availableMedicines = maintainTreatment.getAvailableMedicines();

        try {
            System.out.print("Select medicine by number (1-" + availableMedicines.getNumberOfEntries() + ") or 0 to skip: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                return null; // User chose to skip
            }

            if (choice >= 1 && choice <= availableMedicines.getNumberOfEntries()) {
                return availableMedicines.getEntry(choice);
            } else {
                System.out.println("Invalid choice! Please select a number between 1-" + availableMedicines.getNumberOfEntries());
                return selectMedicine(); // Recursive call for retry
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a valid number.");
            scanner.nextLine();
            return selectMedicine(); // Recursive call for retry
        }
    }
}