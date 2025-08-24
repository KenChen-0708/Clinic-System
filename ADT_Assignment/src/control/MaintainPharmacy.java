package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.PharmacyUI;
import dao.DispenseRecordDAO;
import dao.MedicineDAO;
import dao.PatientDAO;
import dao.TreatmentDAO;
import entity.DispenseRecord;
import entity.Medicine;
import entity.Patient;
import entity.Treatment;
import entity.TreatmentMedicineList;
import utility.MessageUI;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Application layer (controller) for pharmacy operations. - Manages inventory
 * list and persistence. - Drives UI flows (menus / input). - Generates text
 * reports (no Java Collections used in core logic).
 *
 * DESIGN NOTES 1) Uses our custom ListInterface over a DoublyLinkedList to
 * stick to project constraints. 2) Sorting/grouping is done via custom
 * comparators and the list's in-place merge sort. 3) Export is plain-text with
 * a safe filename slug.
 */
public class MaintainPharmacy implements Serializable {

    /* =========================
     * Data Access / State
     * ========================= */
    private final DispenseRecordDAO dispenseDAO = new DispenseRecordDAO();
    private ListInterface<DispenseRecord> dispenseLog = dispenseDAO.retrieveFromFile();

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private ListInterface<Medicine> medicineList = new DoublyLinkedList<>();

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();

    private final PharmacyUI pharmacyUI = new PharmacyUI();

    /* =========================
     * Local helper model
     * ========================= */
    /**
     * Simple name→total pair for grouping/aggregating quantities.
     */
    private static class NameTotal {

        String name;
        int total;

        NameTotal(String name, int total) {
            this.name = name;
            this.total = total;
        }
    }

    /* =========================
     * Comparators (Inventory Sorting)
     * ========================= */
    // Compare by numeric part of ID: M0007 < M0100
    private final Comparator<Medicine> BY_ID_NUM = (a, b) -> {
        int ai = Integer.parseInt(a.getMedId().substring(1));
        int bi = Integer.parseInt(b.getMedId().substring(1));
        return Integer.compare(ai, bi);
    };

    // Expiry earlier first; tie-break: ID
    private final Comparator<Medicine> BY_EXPIRY_THEN_ID = (a, b) -> {
        int c = a.getExpiryDate().compareTo(b.getExpiryDate());
        return (c != 0) ? c : BY_ID_NUM.compare(a, b);
    };

    // Price lower first; tie-break: ID
    private final Comparator<Medicine> BY_PRICE_THEN_ID = (a, b) -> {
        int c = Double.compare(a.getPrice(), b.getPrice());
        return (c != 0) ? c : BY_ID_NUM.compare(a, b);
    };

    // Name A→Z; tie-break: ID
    private final Comparator<Medicine> BY_NAME_THEN_ID = (a, b) -> {
        int c = a.getName().compareToIgnoreCase(b.getName());
        return (c != 0) ? c : BY_ID_NUM.compare(a, b);
    };


    /* =========================
     * Construction
     * ========================= */
    public MaintainPharmacy() {
        // refresh medicines from storage at startup
        medicineList = medicineDAO.retrieveFromFile();
    }

    /* =========================
     * Main Menu Flow
     * ========================= */
    public void runPharmacyMenu() {
        int choice;
        do {
            choice = pharmacyUI.getMenuChoice();
            switch (choice) {
                case 1:
                    runInventoryMenu();
                    break;
                case 2:
                    MessageUI.clearConsole();
                    pharmacyUI.runManageMedicineNames();
                    break;
                case 3:
                    dispenseMedicine();
                    break;
                case 4:
                    generateInventoryReport();
                    break;
                case 5:
                    generateDispensedReport();
                    break;
                case 0:
                    MessageUI.displayMainMenuMessage();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
                    break;
            }
        } while (choice != 0);
    }

    /* =========================
     * Inventory Submenu
     * ========================= */
    public void runInventoryMenu() {
        MessageUI.clearConsole();
        int choice;
        do {
            choice = pharmacyUI.getInventoryManagementChoice();
            switch (choice) {
                case 1:
                    viewAllMedicines();
                    break;
                case 2:
                    addMedicine();
                    break;
                case 3:
                    editMedicineStock();
                    break;
                case 4:
                    removeMedicineStock();
                    break;
                case 5:
                    searchMedicines();
                    break;
                case 0:
                    MessageUI.clearConsole();
                    return;
                default:
                    MessageUI.displayInvalidChoiceMessage();
                    break;
            }
        } while (choice != 0);
    }


    /* =========================
     * Inventory: View/Sort/Group
     * ========================= */
    /**
     * Shows the inventory in a table and allows repeated sort/group actions
     * until the user presses Back.
     */
    public void viewAllMedicines() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            MessageUI.displayEmptyMessage();
            MessageUI.pressEnterToContinue();
            return;
        }

        // Always display a baseline "by ID" table first
        DoublyLinkedList<Medicine> byId = copyList();
        byId.mergeSort(BY_ID_NUM);
        pharmacyUI.displayTable(
                "== Inventory (By ID) ==",
                new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                toRowsById(byId)
        );

        // Sort/group loop (Back returns to Inventory menu)
        while (true) {
            int choice = pharmacyUI.getSortChoice();
            if (choice == 0) {
                MessageUI.clearConsole();
                return;
            }

            String title = "== Inventory (";
            DoublyLinkedList<Medicine> tmp;

            switch (choice) {
                case 1: { // Sort by ID
                    int dir = pharmacyUI.getSortDirection("by ID",
                            "ID ascending (M0001 → M9999)",
                            "ID descending (M9999 → M0001)");
                    MessageUI.clearConsole();
                    if (dir == 0) {
                        break;
                    }

                    tmp = copyList();
                    if (dir == 1) {
                        tmp.mergeSort(BY_ID_NUM);
                        title += "Sorted by ID Ascending)";
                    } else {
                        tmp.mergeSort((a, b) -> BY_ID_NUM.compare(b, a));
                        title += "Sorted by ID Descending)";
                    }

                    pharmacyUI.displayTable(title,
                            new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                            toRowsById(tmp));
                    MessageUI.pressEnterToContinue();
                    break;
                }
                case 2: { // Sort by Expiry
                    int dir = pharmacyUI.getSortDirection("by Expiry Date",
                            "Earliest first", "Latest first");
                    MessageUI.clearConsole();
                    if (dir == 0) {
                        break;
                    }

                    tmp = copyList();
                    if (dir == 1) {
                        tmp.mergeSort(BY_EXPIRY_THEN_ID);
                        title += "Sorted by Expiry Date (Earliest first))";
                    } else {
                        tmp.mergeSort((a, b) -> BY_EXPIRY_THEN_ID.compare(b, a));
                        title += "Sorted by Expiry Date (Latest first))";
                    }

                    pharmacyUI.displayTable(title,
                            new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                            toRowsByExpiryDate(tmp));
                    MessageUI.pressEnterToContinue();
                    break;
                }
                case 3: { // Sort by Price
                    int dir = pharmacyUI.getSortDirection("by Price",
                            "Lowest first", "Highest first");
                    MessageUI.clearConsole();
                    if (dir == 0) {
                        break;
                    }

                    tmp = copyList();
                    if (dir == 1) {
                        tmp.mergeSort(BY_PRICE_THEN_ID);
                        title += "Sorted by Price (Lowest first))";
                    } else {
                        tmp.mergeSort((a, b) -> BY_PRICE_THEN_ID.compare(b, a));
                        title += "Sorted by Price (Highest first))";
                    }

                    pharmacyUI.displayTable(title,
                            new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                            toRowsByPrice(tmp));
                    MessageUI.pressEnterToContinue();
                    break;
                }
                case 4: { // Group by Name
                    MessageUI.clearConsole();
                    title += "Grouped by Name)";
                    pharmacyUI.displayTable(title,
                            new String[]{"Name", "Total Qty"},
                            toRowsTotalsByName());
                    MessageUI.pressEnterToContinue();
                    break;
                }
                default:
                    MessageUI.displayInvalidChoiceMessage();
                    break;
            }
        }
    }

    /* =========================
     * Inventory: CRUD
     * ========================= */
    public void addMedicine() {
        MessageUI.clearConsole();

        String newId = generateNextMedicineId();
        Medicine newMed = pharmacyUI.inputMedicineDetails(newId);
        if (newMed == null) {
            return; // user backed out
        }
        MessageUI.clearConsole();
        while (true) {
            int action = pharmacyUI.confirmAddMedicine(newMed);
            switch (action) {
                case 1: // confirm & save
                    medicineList.add(newMed);
                    medicineDAO.saveToFile(medicineList);
                    return;
                case 2: // edit then re-confirm
                    newMed = pharmacyUI.editMedicineDetails(newMed);
                    if (newMed == null) {
                        return;
                    }
                    break;
                case 0: // discard
                    return;
                default:
                    break;
            }
        }
    }

    public void editMedicineStock() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            MessageUI.displayEmptyMessage();
            MessageUI.pressEnterToContinue();
            return;
        }

        DoublyLinkedList<Medicine> byId = copyList();
        byId.mergeSort(BY_ID_NUM);
        pharmacyUI.displayTable("== Edit Inventory ==",
                new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                toRowsById(byId));

        String medId = pharmacyUI.inputMedicineID();
        if (medId == null) {
            MessageUI.clearConsole();
            return;
        }

        // Locate medicine by ID
        Medicine medToEdit = null;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getMedId().equals(medId)) {
                medToEdit = m;
                break;
            }
        }

        MessageUI.clearConsole();
        if (medToEdit == null) {
            pharmacyUI.displayMessage("Medicine ID not found.");
            return;
        }

        // Let UI edit fields; if kept, persist
        medToEdit = pharmacyUI.editMedicineDetails(medToEdit);
        if (medToEdit != null) {
            medicineDAO.saveToFile(medicineList);
            pharmacyUI.displayMessage("Medicine details updated successfully.");
        } else {
            pharmacyUI.displayMessage("Medicine editing canceled.");
        }
    }

    public void removeMedicineStock() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            MessageUI.displayEmptyMessage();
            MessageUI.pressEnterToContinue();
            return;
        }

        DoublyLinkedList<Medicine> byId = copyList();
        byId.mergeSort(BY_ID_NUM);
        pharmacyUI.displayTable("== Remove Inventory ==",
                new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                toRowsById(byId));

        String medId = pharmacyUI.inputMedicineID();
        if (medId == null) {
            MessageUI.clearConsole();
            return;
        }

        MessageUI.clearConsole();

        // Find position for removal (ListInterface is 1-based)
        int positionToRemove = -1;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            if (medicineList.getEntry(i).getMedId().equals(medId)) {
                positionToRemove = i;
                break;
            }
        }
        if (positionToRemove == -1) {
            pharmacyUI.displayMessage("Medicine ID not found.");
            return;
        }

        int action = pharmacyUI.confirmRemoveMedicine(medicineList.getEntry(positionToRemove));
        MessageUI.clearConsole();
        if (action == 1) {
            medicineList.remove(positionToRemove);
            medicineDAO.saveToFile(medicineList);
            pharmacyUI.displayMessage("Medicine removed from inventory.");
        } else {
            pharmacyUI.displayMessage("Medicine removal canceled.");
        }
    }

    /**
     * Search for a medicine by ID using linear search.
     *
     * @return Medicine object if found, null otherwise.
     */
    public Medicine searchMedicineById(String medId) {
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getMedId().equalsIgnoreCase(medId)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Search for medicines by Name (could be multiple matches).
     *
     * @return List of matching medicines.
     */
    public ListInterface<Medicine> searchMedicineByName(String name) {
        DoublyLinkedList<Medicine> results = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getName().equalsIgnoreCase(name)) {
                results.add(m);
            }
        }
        return results;
    }

    public void searchMedicines() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            MessageUI.displayEmptyMessage();
            MessageUI.pressEnterToContinue();
            return; // back to Inventory menu (one level)
        }

        final String[] headers = {"Med ID", "Name", "Qty", "Price/Unit", "Expiry"};

        // Stay in the search flow until user chooses Back here
        while (true) {
            int mode = pharmacyUI.getSearchChoice();
            if (mode == 0) {
                MessageUI.clearConsole();
                return; // back ONE level (to Inventory menu)
            }

            switch (mode) {
                case 1: { // By ID (exact; UI already normalizes to M####)
                    String medId = pharmacyUI.inputMedicineID();
                    if (medId == null) { // user pressed Back once
                        MessageUI.clearConsole();
                        break; // back to search menu (NOT inventory)
                    }

                    Medicine found = searchMedicineById(medId);
                    DoublyLinkedList<Medicine> results = new DoublyLinkedList<>();
                    if (found != null) {
                        results.add(found);
                    }

                    MessageUI.clearConsole();
                    if (results.isEmpty()) {
                        pharmacyUI.displayMessage("No medicine found with ID: " + medId);
                    } else {
                        results.mergeSort(BY_ID_NUM);
                        pharmacyUI.displayTable(
                                "== Search Results (By ID: " + medId + ") ==",
                                headers,
                                toRowsById(results)
                        );
                        MessageUI.pressEnterToContinue();
                    }
                    break; // show search menu again
                }

                case 2: { // By Name (contains, case-insensitive)
                    String pat = pharmacyUI.inputNamePattern();
                    if (pat == null) {
                        MessageUI.clearConsole();
                        break; // back to search menu (NOT inventory)
                    }

                    String needle = pat.toLowerCase();
                    DoublyLinkedList<Medicine> results = new DoublyLinkedList<>();
                    for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
                        Medicine m = medicineList.getEntry(i);
                        if (m.getName().toLowerCase().contains(needle)) {
                            results.add(m);
                        }
                    }

                    MessageUI.clearConsole();
                    if (results.isEmpty()) {
                        pharmacyUI.displayMessage("No medicines matched: \"" + pat + "\"");
                    } else {
                        results.mergeSort((a, b) -> {
                            int c = a.getName().compareToIgnoreCase(b.getName());
                            return (c != 0) ? c : BY_ID_NUM.compare(a, b);
                        });
                        pharmacyUI.displayTable(
                                "== Search Results (Name contains: \"" + pat + "\") ==",
                                headers,
                                toRowsById(results)
                        );
                        MessageUI.pressEnterToContinue();
                    }
                    break; // show search menu again
                }

                default:
                    MessageUI.displayInvalidChoiceMessage();
                // fall through to loop; search menu will be shown again
            }
        }
    }


    /* =========================
     * Dispensing
     * ========================= */
    /**
     * Dispense per the latest prescription of the selected patient.
     * Prescription format: "M0001:3,M0002:5"
     */
    public void dispenseMedicine() {
        MessageUI.clearConsole();

        // Find patients with pending treatments
        StringBuilder patientOutput = new StringBuilder();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);

            // Check if the patient has any pending treatments
            boolean hasPendingTreatment = false;
            ListInterface<Treatment> treatments = treatmentDAO.retrieveFromFile();
            for (int j = 1; j <= treatments.getNumberOfEntries(); j++) {
                Treatment treatment = treatments.getEntry(j);
                if (treatment.getPatientId().equalsIgnoreCase(p.getId())
                        && treatment.getStatus().equalsIgnoreCase("Pending")
                        && treatment.getMedicineList() != null && !treatment.getMedicineList().isEmpty()) {
                    hasPendingTreatment = true;
                    break;
                }
            }

            if (hasPendingTreatment) {
                patientOutput.append(i).append(". ")
                        .append(p.getId()).append(" - ")
                        .append(p.getName()).append("\n");
            }
        }

        if (patientOutput.length() == 0) {
            pharmacyUI.displayMessage("No patients with pending treatments found.");
            return;
        }

        pharmacyUI.displayMessage("Select a patient with pending treatments:\n" + patientOutput);

        // Get patient selection (0 = Back ONE level)
        int selectedIndex = pharmacyUI.inputPatientSelection();
        if (selectedIndex == 0) { // <-- back one level
            MessageUI.clearConsole();
            return;
        }
        MessageUI.clearConsole();
        if (selectedIndex < 1 || selectedIndex > patientList.getNumberOfEntries()) {
            pharmacyUI.displayMessage("Invalid selection.");
            return;
        }

        Patient selectedPatient = patientList.getEntry(selectedIndex);
        pharmacyUI.displayMessage("Selected Patient: " + selectedPatient.getName() + " (ID: " + selectedPatient.getId() + ")");

        // Get all treatments for the selected patient
        ListInterface<Treatment> treatments = treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> patientTreatments = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(selectedPatient.getId())) {
                patientTreatments.add(treatment);
            }
        }

        if (patientTreatments.isEmpty()) {
            pharmacyUI.displayMessage("\nNo treatments found for this patient.");
            return;
        }

        // Find the most recent treatment with "Pending" status and medicines
        Treatment treatmentToDispense = null;
        for (int i = patientTreatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment treatment = patientTreatments.getEntry(i);

            // Only process treatments with "Pending" status
            if (treatment.getStatus().equalsIgnoreCase("Pending")
                    && treatment.getMedicineList() != null
                    && !treatment.getMedicineList().isEmpty()) {
                treatmentToDispense = treatment;
                break;
            }
        }

        if (treatmentToDispense == null) {
            pharmacyUI.displayMessage("\nNo pending treatments with prescribed medicines found for this patient.");

            // Check if there are completed treatments
            boolean hasCompletedTreatments = false;
            for (int i = 1; i <= patientTreatments.getNumberOfEntries(); i++) {
                Treatment treatment = patientTreatments.getEntry(i);
                if (treatment.getStatus().equalsIgnoreCase("Completed")) {
                    hasCompletedTreatments = true;
                    break;
                }
            }

            if (hasCompletedTreatments) {
                pharmacyUI.displayMessage("Note: This patient has completed treatments that don't require medicine dispensing.");
            }
            return;
        }

        // Display treatment information
        pharmacyUI.displayMessage("\nTreatment ID: " + treatmentToDispense.getTreatmentId());
        pharmacyUI.displayMessage("Treatment Date: " + treatmentToDispense.getTreatmentDate());

        // Get the medicine list from treatment
        DoublyLinkedList<TreatmentMedicineList> prescribedMedicines = treatmentToDispense.getMedicineList();

        // --- PRESCRIBED MEDICINES (table view) ---
        if (prescribedMedicines.getNumberOfEntries() == 0) {
            pharmacyUI.displayMessage("\n=== PRESCRIBED MEDICINES ===");
            pharmacyUI.displayMessage("No prescribed medicines.");
        } else {
            final String title = "=== PRESCRIBED MEDICINES ===";
            final String[] headers = {"#", "Medicine", "Tablets"};

            String[][] rows = new String[prescribedMedicines.getNumberOfEntries()][headers.length];
            int grandTotal = 0;

            for (int i = 1; i <= prescribedMedicines.getNumberOfEntries(); i++) {
                TreatmentMedicineList med = prescribedMedicines.getEntry(i);

                rows[i - 1][0] = String.valueOf(i);
                rows[i - 1][1] = med.getPrescription();
                rows[i - 1][2] = String.valueOf(med.getTablets());

            }

            pharmacyUI.displayTable(title, headers, rows);
            pharmacyUI.displayMessage("Grand total tablets prescribed: " + grandTotal);
        }

        // Ask for confirmation before dispensing
        pharmacyUI.displayMessage("1. Dispense all medicines");
        pharmacyUI.displayMessage("0. Discard");
        pharmacyUI.displayMessage("--------------------------------");
        System.out.print("Enter your choice: ");

        int confirmDispense = 0;
        try {
            confirmDispense = new Scanner(System.in).nextInt();
        } catch (Exception e) {
            confirmDispense = 2; // Default to cancel
        }

        if (confirmDispense != 1) {
            MessageUI.clearConsole();
            pharmacyUI.displayMessage("Dispensing cancelled.");
            return;
        }

        // ===== Dispense processing =====
        // 1) Pre-check availability without mutating state
        boolean allAvailable = true;
        StringBuilder insufficientMsg = new StringBuilder();

        for (int i = 1; i <= prescribedMedicines.getNumberOfEntries(); i++) {
            TreatmentMedicineList prescribedMed = prescribedMedicines.getEntry(i);
            String medicineName = prescribedMed.getPrescription();
            int totalTabletsNeeded = prescribedMed.getTablets(); // already the correct total

            Medicine foundMedicine = null;
            for (int j = 1; j <= medicineList.getNumberOfEntries(); j++) {
                Medicine m = medicineList.getEntry(j);
                if (m.getName().equalsIgnoreCase(medicineName)) {
                    foundMedicine = m;
                    break;
                }
            }

            int available = (foundMedicine == null) ? 0 : foundMedicine.getStock();
            if (foundMedicine == null || available < totalTabletsNeeded) {
                allAvailable = false;
                insufficientMsg.append(medicineName)
                        .append(" - INSUFFICIENT STOCK (Needed: ")
                        .append(totalTabletsNeeded)
                        .append(", Available: ")
                        .append(available)
                        .append(")\n");
            }
        }

        // 2) If any issue, clear and show one concise message, then exit
        if (!allAvailable) {
            MessageUI.clearConsole();
            pharmacyUI.displayMessage("INSUFFICIENT STOCK\n" + insufficientMsg.toString());
            return;
        }

        // 3) All good — perform the dispense, record, save, and show success
        for (int i = 1; i <= prescribedMedicines.getNumberOfEntries(); i++) {
            TreatmentMedicineList prescribedMed = prescribedMedicines.getEntry(i);
            String medicineName = prescribedMed.getPrescription();
            int totalTabletsNeeded = prescribedMed.getTablets();

            // Find medicine (exists by pre-check)
            Medicine foundMedicine = null;
            for (int j = 1; j <= medicineList.getNumberOfEntries(); j++) {
                Medicine m = medicineList.getEntry(j);
                if (m.getName().equalsIgnoreCase(medicineName)) {
                    foundMedicine = m;
                    break;
                }
            }

            // Deduct stock
            foundMedicine.setStock(foundMedicine.getStock() - totalTabletsNeeded);

            // Log dispense
            DispenseRecord rec = new DispenseRecord(
                    selectedPatient.getId(),
                    selectedPatient.getName(),
                    foundMedicine.getMedId(),
                    foundMedicine.getName(),
                    totalTabletsNeeded,
                    LocalDateTime.now()
            );
            dispenseLog.add(rec);
        }

        // Mark treatment completed and persist
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equals(treatmentToDispense.getTreatmentId())) {
                treatment.setStatus("Completed");
                break;
            }
        }
        treatmentDAO.saveToFile(treatments);
        medicineDAO.saveToFile(medicineList);
        dispenseDAO.saveToFile(dispenseLog);

        // Final success message
        MessageUI.clearConsole();
        pharmacyUI.displayMessage("All medicines dispensed successfully");

    }

    /* =========================
     * Reports: Inventory
     * ========================= */
    public void generateInventoryReport() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            pharmacyUI.displayReport("Inventory Report", "(No inventory items)");
            MessageUI.pressEnterToContinue();
            return;
        }

        int choice;
        do {
            choice = pharmacyUI.getInventoryReportChoice();
            switch (choice) {
                case 0:
                    MessageUI.clearConsole();
                    break;

                case 1: { // KPI Snapshot
                    MessageUI.clearConsole();
                    String[] headers = {"Metric", "Value"};
                    String[][] rows = toRowsKpis();
                    pharmacyUI.displayTable("== KPIs ==", headers, rows);

                    String tableText = buildTableText("KPIs", headers, rows);
                    offerExportAfterDisplay("Inventory Report - KPI Snapshot", tableText);
                    break;
                }

                case 2: { // Low-stock
                    int threshold = pharmacyUI.inputLowStockThreshold();
                    if (threshold == -1) {
                        MessageUI.clearConsole();
                        break;
                    }

                    MessageUI.clearConsole();
                    String[] headers = {"Med ID", "Name", "Qty", "Price/Unit", "Expiry"};
                    String[][] rows = toRowsLowStock(threshold);

                    if (rows.length == 0) {
                        String body = "(No items at or below threshold)";
                        pharmacyUI.displayReport("Inventory Report (Low-stock)", body);
                        // Do NOT offer export when there are no items
                        MessageUI.pressEnterToContinue();
                    } else {
                        String title = "== Low-stock (<= " + threshold + ") ==";
                        pharmacyUI.displayTable(title, headers, rows);
                        String tableText = buildTableText(title, headers, rows);
                        offerExportAfterDisplay("Inventory Report - Low Stock (<= " + threshold + ")", tableText);
                    }
                    break;
                }

                case 3: { // Near-expiry
                    int days = pharmacyUI.inputNearExpiryDays();
                    if (days == -1) {
                        MessageUI.clearConsole();
                        break;
                    }

                    MessageUI.clearConsole();
                    String[] headers = {"Med ID", "Name", "Qty", "Price/Unit", "Expiry"};
                    String[][] rows = toRowsNearExpiry(days);

                    if (rows.length == 0) {
                        String body = "(No items expiring within " + days + " days)";
                        pharmacyUI.displayReport("Inventory Report (Near-expiry)", body);
                        // Do NOT offer export when there are no items
                        MessageUI.pressEnterToContinue();
                    } else {
                        String title = "== Near-expiry (within " + days + " days) ==";
                        pharmacyUI.displayTable(title, headers, rows);
                        String tableText = buildTableText(title, headers, rows);
                        offerExportAfterDisplay("Inventory Report - Near Expiry (within " + days + " days)", tableText);
                    }
                    break;
                }

                default:
                    break;
            }
        } while (choice != 0);
    }


    /* =========================
     * Reports: Dispensed Log
     * ========================= */
    public void generateDispensedReport() {
        MessageUI.clearConsole();

        dispenseLog = dispenseDAO.retrieveFromFile();

        if (dispenseLog.isEmpty()) {
            pharmacyUI.displayReport("Dispensed Medicines Report", "(No records)");
            MessageUI.pressEnterToContinue();
            return;
        }

        int choice;
        do {
            choice = pharmacyUI.getDispensedReportChoice();
            MessageUI.clearConsole();

            if (choice == 0) {
                break;
            }

            // Get the date range choice from the user
            int dateRangeChoice = pharmacyUI.getDateRangeChoice();
            MessageUI.clearConsole();
            LocalDate startDate = null;
            LocalDate endDate = null;

            // Handling date range options
            switch (dateRangeChoice) {
                case 1: // All (no date filter)
                    // If "All" is selected, we want to include all records
                    startDate = null;
                    endDate = null;
                    break;
                case 2: // Today
                    startDate = LocalDate.now();
                    endDate = LocalDate.now();
                    break;
                case 3: // This Week
                    startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1); // Start of the week
                    endDate = LocalDate.now().plusDays(7 - LocalDate.now().getDayOfWeek().getValue()); // End of the week
                    break;
                case 4: // Last 30 Days
                    startDate = LocalDate.now().minusDays(30);
                    endDate = LocalDate.now();
                    break;
                case 5: // Custom Range
                    startDate = pharmacyUI.inputDate("Start date");
                    if (startDate == null) {
                        break;
                    }
                    endDate = pharmacyUI.inputDate("End date");
                    if (endDate == null) {
                        break;
                    }
                    break;
                case 0:
                    break;
            }

            // If "All" is selected, we do not filter by date, so we pass all records
            ListInterface<DispenseRecord> filteredRecords = (startDate == null && endDate == null)
                    ? dispenseLog // No filter, all records
                    : filterByDateRange(startDate, endDate);

            switch (choice) {
                case 1: { // Detail Report
                    String title = "Dispensed (Detail) from " + (startDate == null ? "All time" : startDate) + " to " + (endDate == null ? "All time" : endDate);
                    String body = renderDispenseDetail(filteredRecords);
                    pharmacyUI.displayReport(title, body);
                    offerExportAfterDisplay(title, body);
                    break;
                }

                case 2: { // By Medicine
                    String title = "Dispensed (By Medicine) from " + (startDate == null ? "All time" : startDate) + " to " + (endDate == null ? "All time" : endDate);
                    String body = renderDispenseSummaryByMedicine(filteredRecords);
                    pharmacyUI.displayReport(title, body);
                    offerExportAfterDisplay(title, body);
                    break;
                }

                case 3: { // By Patient
                    String title = "Dispensed (By Patient) from " + (startDate == null ? "All time" : startDate) + " to " + (endDate == null ? "All time" : endDate);
                    String body = renderDispenseSummaryByPatient(filteredRecords);
                    pharmacyUI.displayReport(title, body);
                    offerExportAfterDisplay(title, body);
                    break;
                }

                default:
                    MessageUI.clearConsole();
                    break;
            }
        } while (choice != 0);
    }

    /* =========================
     * Utility: Copy/Build/Format
     * ========================= */
    /**
     * Make a shallow copy of the medicine list (for sorting/transforming
     * without touching the live list).
     */
    private DoublyLinkedList<Medicine> copyList() {
        DoublyLinkedList<Medicine> copy = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            copy.add(medicineList.getEntry(i));
        }
        return copy;
    }

    /**
     * Find index of a name (case-insensitive) in a NameTotal list; returns -1
     * when not found.
     */
    private int indexOfName(ListInterface<NameTotal> list, String name) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            if (list.getEntry(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Group totals by medicine name (for a summary table text).
     */
    private String buildGroupedTotalsText() {
        DoublyLinkedList<NameTotal> totals = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            int idx = indexOfName(totals, m.getName());
            if (idx == -1) {
                totals.add(new NameTotal(m.getName(), m.getStock()));
            } else {
                NameTotal nt = totals.getEntry(idx);
                nt.total += m.getStock();
                totals.replace(idx, nt);
            }
        }
        totals.mergeSort((a, b) -> a.name.compareToIgnoreCase(b.name));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %10s%n", "Medicine Name", "Total Qty"));
        sb.append("-----------------------------------------------\n");
        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            sb.append(String.format("%-30s %10d%n", nt.name, nt.total));
        }
        return sb.toString();
    }

    /**
     * Filter dispense records within [start, end] dates (inclusive).
     */
    private ListInterface<DispenseRecord> filterByDateRange(LocalDate start, LocalDate end) {
        DoublyLinkedList<DispenseRecord> out = new DoublyLinkedList<>();
        for (int i = 1; i <= dispenseLog.getNumberOfEntries(); i++) {
            DispenseRecord r = dispenseLog.getEntry(i);
            LocalDate d = r.getTimestamp().toLocalDate();
            if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                out.add(r);
            }
        }
        return out;
    }

    /**
     * Detail view: sort by timestamp, then patient, then medicine, with better
     * formatting.
     */
    private String renderDispenseDetail(ListInterface<DispenseRecord> list) {
        DoublyLinkedList<DispenseRecord> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            tmp.add(list.getEntry(i));
        }

        // Sorting by timestamp, patient name, and medicine name
        tmp.mergeSort((a, b) -> {
            int c = a.getTimestamp().compareTo(b.getTimestamp());
            if (c != 0) {
                return c;
            }
            c = a.getPatientName().compareToIgnoreCase(b.getPatientName());
            if (c != 0) {
                return c;
            }
            return a.getMedName().compareToIgnoreCase(b.getMedName());
        });

        StringBuilder sb = new StringBuilder();
        sb.append("==== DISPENSE DETAILS REPORT ====\n\n");

        // Header for the report
        sb.append(String.format("%-25s %-30s %-15s %-30s %10s%n",
                "DateTime", "Patient", "Med ID", "Medicine", "Qty"));
        sb.append("---------------------------------------------------------------"
                + "---------------------------------------------------------------\n");

        var dtf = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu HH:mm");
        for (int i = 1; i <= tmp.getNumberOfEntries(); i++) {
            DispenseRecord r = tmp.getEntry(i);
            sb.append(String.format("%-25s %-30s %-15s %-30s %10d%n",
                    r.getTimestamp().format(dtf), r.getPatientName(), r.getMedId(), r.getMedName(), r.getQuantity()));
        }

        // Footer with total count
        sb.append("\nTotal Records: ").append(tmp.getNumberOfEntries()).append(" dispensations\n");

        return sb.toString();
    }

    /**
     * Summary by medicine: totals (desc), tie-breaker by name (asc), enhanced
     * layout.
     */
    private String renderDispenseSummaryByMedicine(ListInterface<DispenseRecord> list) {
        DoublyLinkedList<NameTotal> totals = new DoublyLinkedList<>();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            DispenseRecord r = list.getEntry(i);
            int idx = indexOfName(totals, r.getMedName());
            if (idx == -1) {
                totals.add(new NameTotal(r.getMedName(), r.getQuantity()));
            } else {
                NameTotal nt = totals.getEntry(idx);
                nt.total += r.getQuantity();
                totals.replace(idx, nt);
            }
        }
        if (totals.isEmpty()) {
            return "(No records)";
        }

        // Sorting by total quantity descending, and by name ascending
        totals.mergeSort((a, b) -> {
            int c = Integer.compare(b.total, a.total);
            return (c != 0) ? c : a.name.compareToIgnoreCase(b.name);
        });

        StringBuilder sb = new StringBuilder();
        sb.append("==== MEDICINE DISPENSE SUMMARY ====\n\n");
        sb.append(String.format("%-30s %15s%n", "Medicine", "Total Qty"));
        sb.append("---------------------------------------------------------------\n");

        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            sb.append(String.format("%-30s %15d%n", nt.name, nt.total));
        }

        // Footer with total count of medicines
        sb.append("\nTotal Medicines: ").append(totals.getNumberOfEntries()).append(" unique medicines\n");

        return sb.toString();
    }

    /**
     * Summary by patient: totals (desc), tie-breaker by name (asc), enhanced
     * layout.
     */
    private String renderDispenseSummaryByPatient(ListInterface<DispenseRecord> list) {
        DoublyLinkedList<NameTotal> totals = new DoublyLinkedList<>();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            DispenseRecord r = list.getEntry(i);
            int idx = indexOfName(totals, r.getPatientName());
            if (idx == -1) {
                totals.add(new NameTotal(r.getPatientName(), r.getQuantity()));
            } else {
                NameTotal nt = totals.getEntry(idx);
                nt.total += r.getQuantity();
                totals.replace(idx, nt);
            }
        }
        if (totals.isEmpty()) {
            return "(No records)";
        }

        // Sorting by total quantity descending, and by name ascending
        totals.mergeSort((a, b) -> {
            int c = Integer.compare(b.total, a.total);
            return (c != 0) ? c : a.name.compareToIgnoreCase(b.name);
        });

        StringBuilder sb = new StringBuilder();
        sb.append("==== PATIENT DISPENSE SUMMARY ====\n\n");
        sb.append(String.format("%-30s %15s%n", "Patient", "Total Qty"));
        sb.append("---------------------------------------------------------------\n");

        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            sb.append(String.format("%-30s %15d%n", nt.name, nt.total));
        }

        // Footer with total count of patients
        sb.append("\nTotal Patients: ").append(totals.getNumberOfEntries()).append(" unique patients\n");

        return sb.toString();
    }


    /* =========================
     * KPIs
     * ========================= */
    private int countSkus() {
        return medicineList.getNumberOfEntries();
    }

    private int sumUnits() {
        int total = 0;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            total += medicineList.getEntry(i).getStock();
        }
        return total;
    }

    private double totalValuation() {
        double sum = 0.0;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            sum += m.getStock() * m.getPrice();
        }
        return sum;
    }

    private Medicine getEarliestExpiry() {
        if (medicineList.isEmpty()) {
            return null;
        }
        Medicine best = medicineList.getEntry(1);
        for (int i = 2; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getExpiryDate().isBefore(best.getExpiryDate())) {
                best = m;
            }
        }
        return best;
    }

    private Medicine getLatestExpiry() {
        if (medicineList.isEmpty()) {
            return null;
        }
        Medicine best = medicineList.getEntry(1);
        for (int i = 2; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getExpiryDate().isAfter(best.getExpiryDate())) {
                best = m;
            }
        }
        return best;
    }

    /* =========================
     * Table Builders / Export
     * ========================= */
    /**
     * Build a plain-text table (no dependency on TablePrinter) for exporting.
     */
    private String buildTableText(String title, String[] headers, String[][] rows) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isEmpty()) {
            sb.append(title).append("\n\n");
        }

        // Column widths
        int cols = headers.length;
        int[] w = new int[cols];
        for (int c = 0; c < cols; c++) {
            w[c] = headers[c].length();
        }
        for (String[] r : rows) {
            for (int c = 0; c < cols; c++) {
                if (r[c] != null && r[c].length() > w[c]) {
                    w[c] = r[c].length();
                }
            }
        }

        // Header
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%-" + (w[c] + 2) + "s", headers[c]));
        }
        sb.append("\n");

        // Separator
        for (int c = 0; c < cols; c++) {
            sb.append("-".repeat(w[c])).append("  ");
        }
        sb.append("\n");

        // Rows
        for (String[] r : rows) {
            for (int c = 0; c < cols; c++) {
                String cell = (r[c] == null) ? "" : r[c];
                sb.append(String.format("%-" + (w[c] + 2) + "s", cell));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * After displaying a report/table, optionally export as text file.
     */
    private void offerExportAfterDisplay(String exportTitle, String exportBody) {
        int act = pharmacyUI.getPostReportAction();
        if (act == 1) {
            String path = exportReportToFile(exportTitle, exportBody);
            if (path == null) {
                pharmacyUI.displayMessage("\nFailed to export report.\n");
            } else {
                pharmacyUI.displayExportResult(path);
            }
            MessageUI.pressEnterToContinue();
        } else {
            MessageUI.clearConsole();
        }
    }

    /**
     * Export the given title/body to a filesystem-safe .txt; returns absolute
     * path or null on failure.
     */
    private String exportReportToFile(String title, String body) {
        var fnf = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        // Make a safe filename slug from the title
        String safeTitle = title
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        String filename = safeTitle.toLowerCase() + "_" + java.time.LocalDateTime.now().format(fnf) + ".txt";
        java.io.File file = new java.io.File(filename);

        try (java.io.PrintWriter pw
                = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            pw.println(title);
            pw.println();
            pw.println(body);
        } catch (java.io.IOException e) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /* =========================
     * Row Mappers (for TablePrinter)
     * ========================= */
    private String[][] toRowsById(ListInterface<Medicine> list) {
        var df = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu");
        String[][] rows = new String[list.getNumberOfEntries()][5];
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Medicine m = list.getEntry(i);
            rows[i - 1][0] = m.getMedId();
            rows[i - 1][1] = m.getName();
            rows[i - 1][2] = String.valueOf(m.getStock());
            rows[i - 1][3] = String.format("RM %.2f", m.getPrice());
            rows[i - 1][4] = m.getExpiryDate().format(df);
        }
        return rows;
    }

    private String[][] toRowsKpis() {
        var df = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu");
        Medicine earliest = getEarliestExpiry();
        Medicine latest = getLatestExpiry();

        String[][] rows = new String[5][2];
        rows[0][0] = "Total SKUs";
        rows[0][1] = String.valueOf(countSkus());
        rows[1][0] = "Total Units";
        rows[1][1] = String.valueOf(sumUnits());
        rows[2][0] = "Total Valuation";
        rows[2][1] = String.format("RM %.2f", totalValuation());
        rows[3][0] = "Earliest Expiry";
        rows[3][1] = (earliest == null) ? "-"
                : earliest.getExpiryDate().format(df) + " (" + earliest.getMedId() + ", " + earliest.getName() + ")";
        rows[4][0] = "Latest Expiry";
        rows[4][1] = (latest == null) ? "-"
                : latest.getExpiryDate().format(df) + " (" + latest.getMedId() + ", " + latest.getName() + ")";
        return rows;
    }

    private String[][] toRowsTotalsByName() {
        // Build totals by name
        DoublyLinkedList<NameTotal> totals = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            int idx = indexOfName(totals, m.getName());
            if (idx == -1) {
                totals.add(new NameTotal(m.getName(), m.getStock()));
            } else {
                NameTotal nt = totals.getEntry(idx);
                nt.total += m.getStock();
                totals.replace(idx, nt);
            }
        }
        if (!totals.isEmpty()) {
            totals.mergeSort((a, b) -> a.name.compareToIgnoreCase(b.name));
        }

        String[][] rows = new String[totals.getNumberOfEntries()][2];
        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            rows[i - 1][0] = nt.name;
            rows[i - 1][1] = String.valueOf(nt.total);
        }
        return rows;
    }

    private String[][] toRowsLowStock(int threshold) {
        DoublyLinkedList<Medicine> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getStock() <= threshold) {
                tmp.add(m);
            }
        }
        if (tmp.isEmpty()) {
            return new String[0][0];
        }

        tmp.mergeSort((a, b) -> {
            int c = Integer.compare(a.getStock(), b.getStock());
            return (c != 0) ? c : BY_ID_NUM.compare(a, b);
        });
        return toRowsById(tmp);
    }

    private String[][] toRowsNearExpiry(int days) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(days);

        DoublyLinkedList<Medicine> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            LocalDate d = m.getExpiryDate();
            // Include date range [today .. cutoff]
            if (!d.isBefore(today) && !d.isAfter(cutoff)) {
                tmp.add(m);
            }
        }
        if (tmp.isEmpty()) {
            return new String[0][0];
        }

        tmp.mergeSort(BY_EXPIRY_THEN_ID);
        return toRowsById(tmp);
    }

    private String[][] toRowsByExpiryDate(ListInterface<Medicine> list) {
        // identical to toRowsById, just keeping method for readability/semantics
        return toRowsById(list);
    }

    private String[][] toRowsByPrice(ListInterface<Medicine> list) {
        // identical to toRowsById, just keeping method for readability/semantics
        return toRowsById(list);
    }

    /* =========================
     * IDs
     * ========================= */
    /**
     * Generate next medicine ID in the format M#### (1-based, padded to 4).
     */
    private String generateNextMedicineId() {
        int max = 0;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            String idNum = medicineList.getEntry(i).getMedId().substring(1); // skip 'M'
            int num = Integer.parseInt(idNum);
            if (num > max) {
                max = num;
            }
        }
        return String.format("M%04d", max + 1);
    }

    /* =========================
     * Entry point (manual run)
     * ========================= */
    public static void main(String[] args) {
        MaintainPharmacy pharmacy = new MaintainPharmacy();
        pharmacy.runPharmacyMenu();
    }
}
