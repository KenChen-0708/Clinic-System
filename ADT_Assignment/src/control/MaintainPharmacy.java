package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.PharmacyUI;
import dao.MedicineDAO;
import dao.PatientDAO;
import entity.Medicine;
import utility.MessageUI;
import dao.TreatmentDAO;
import entity.Patient;
import entity.Treatment;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.LocalDateTime;

import entity.DispenseRecord;
import dao.DispenseRecordDAO;

public class MaintainPharmacy {

    private final DispenseRecordDAO dispenseDAO = new DispenseRecordDAO();
    private ListInterface<DispenseRecord> dispenseLog = dispenseDAO.retrieveFromFile();
    private ListInterface<Medicine> medicineList = new DoublyLinkedList<>();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private PharmacyUI pharmacyUI = new PharmacyUI();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();

    private static class NameTotal {

        String name;
        int total;

        NameTotal(String name, int total) {
            this.name = name;
            this.total = total;
        }
    }

    private static class Pair<A, B> {

        A a;
        B b;

        Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }

    private DoublyLinkedList<Medicine> copyList() {
        DoublyLinkedList<Medicine> copy = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            copy.add(medicineList.getEntry(i));
        }
        return copy;
    }

    private String renderList(ListInterface<Medicine> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            sb.append(list.getEntry(i).toString()).append("\n");
        }
        return sb.toString();
    }

    private final Comparator<Medicine> BY_ID_NUM = (a, b) -> {
        int ai = Integer.parseInt(a.getMedId().substring(1));
        int bi = Integer.parseInt(b.getMedId().substring(1));
        return Integer.compare(ai, bi);
    };

    private final Comparator<Medicine> BY_EXPIRY_THEN_ID = (a, b) -> {
        int c = a.getExpiryDate().compareTo(b.getExpiryDate());
        return (c != 0) ? c : BY_ID_NUM.compare(a, b);
    };

    private final Comparator<Medicine> BY_PRICE_THEN_ID = (a, b) -> {
        int c = Double.compare(a.getPrice(), b.getPrice());
        if (c != 0) {
            return c;
        }
        return BY_ID_NUM.compare(a, b);
    };

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

    private int indexOfName(ListInterface<NameTotal> list, String name) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            if (list.getEntry(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private String renderLowStock(int threshold) {
        // collect matches into temp list then sort by stock asc, then ID
        DoublyLinkedList<Medicine> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (m.getStock() <= threshold) {
                tmp.add(m);
            }
        }
        if (tmp.isEmpty()) {
            return "(No items at or below threshold)";
        }
        tmp.mergeSort((a, b) -> {
            int c = Integer.compare(a.getStock(), b.getStock());
            if (c != 0) {
                return c;
            }
            return BY_ID_NUM.compare(a, b);
        });
        return renderList(tmp);
    }

    private String renderNearExpiry(int days) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate cutoff = today.plusDays(days);

        DoublyLinkedList<Medicine> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (!m.getExpiryDate().isAfter(cutoff)) {
                tmp.add(m);
            }
        }
        if (tmp.isEmpty()) {
            return "(No items expiring within " + days + " days)";
        }
        tmp.mergeSort(BY_EXPIRY_THEN_ID);
        return renderList(tmp);
    }

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

    private String renderDispenseDetail(ListInterface<DispenseRecord> list) {
        // sort by timestamp asc, then patient, then med
        DoublyLinkedList<DispenseRecord> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            tmp.add(list.getEntry(i));
        }
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
        sb.append(String.format("%-20s %-20s %-12s %-25s %8s%n",
                "DateTime", "Patient", "Med ID", "Medicine", "Qty"));
        sb.append("-------------------------------------------------------------------------------\n");

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu HH:mm");
        for (int i = 1; i <= tmp.getNumberOfEntries(); i++) {
            DispenseRecord r = tmp.getEntry(i);
            sb.append(String.format("%-20s %-20s %-12s %-25s %8d%n",
                    r.getTimestamp().format(dtf),
                    r.getPatientName(),
                    r.getMedId(),
                    r.getMedName(),
                    r.getQuantity()));
        }
        return sb.toString();
    }

    private String renderDispenseSummaryByMedicine(ListInterface<DispenseRecord> list) {
        // accumulate into NameTotal list (by medName)
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
        // sort by total desc, then name asc
        totals.mergeSort((a, b) -> {
            int c = Integer.compare(b.total, a.total);
            if (c != 0) {
                return c;
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %10s%n", "Medicine", "Total Qty"));
        sb.append("-----------------------------------------------\n");
        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            sb.append(String.format("%-30s %10d%n", nt.name, nt.total));
        }
        return sb.toString();
    }

    private String renderDispenseSummaryByPatient(ListInterface<DispenseRecord> list) {
        // accumulate by patientName
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
        totals.mergeSort((a, b) -> {
            int c = Integer.compare(b.total, a.total);
            if (c != 0) {
                return c;
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %10s%n", "Patient", "Total Qty"));
        sb.append("-----------------------------------------------\n");
        for (int i = 1; i <= totals.getNumberOfEntries(); i++) {
            NameTotal nt = totals.getEntry(i);
            sb.append(String.format("%-30s %10d%n", nt.name, nt.total));
        }
        return sb.toString();
    }

    // --- Helpers for KPIs (no Java Collections) ---
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

// --- Build a report snapshot text ---
    private String buildInventorySnapshotText() {
        StringBuilder sb = new StringBuilder();

        java.time.format.DateTimeFormatter dtf
                = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu HH:mm");

        sb.append("INVENTORY SNAPSHOT\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now().format(dtf)).append("\n\n");

        // KPIs
        sb.append("== KPIs ==\n");
        sb.append(String.format("Total SKUs     : %d%n", countSkus()));
        sb.append(String.format("Total Units    : %d%n", sumUnits()));
        sb.append(String.format("Total Valuation: RM %.2f%n", totalValuation()));

        Medicine earliest = getEarliestExpiry();
        Medicine latest = getLatestExpiry();
        if (earliest != null) {
            sb.append(String.format("Earliest Expiry: %s (ID %s, %s)%n",
                    earliest.getExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu")),
                    earliest.getMedId(), earliest.getName()));
        }
        if (latest != null) {
            sb.append(String.format("Latest Expiry  : %s (ID %s, %s)%n",
                    latest.getExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu")),
                    latest.getMedId(), latest.getName()));
        }
        sb.append("\n");

        // By ID section (fixed order to keep report consistent)
        DoublyLinkedList<Medicine> byId = copyList();
        byId.mergeSort(BY_ID_NUM);
        sb.append("== Inventory (By ID) ==\n");
        sb.append(renderList(byId)).append("\n");

        // Totals by Name
        sb.append("== Totals By Name ==\n");
        sb.append(buildGroupedTotalsText()).append("\n");

        return sb.toString();
    }

    private String exportReportToFile(String title, String body) {
        java.time.format.DateTimeFormatter fnf = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String filename = title.toLowerCase().replace(' ', '_') + "_"
                + java.time.LocalDateTime.now().format(fnf) + ".txt";
        java.io.File file = new java.io.File(filename);
        try (java.io.PrintWriter pw = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            pw.println(title);
            pw.println();
            pw.println(body);
        } catch (java.io.IOException e) {
            return null; // signal failure
        }
        return file.getAbsolutePath();
    }

    private String[][] toRowsById(ListInterface<Medicine> list) {
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu");
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
        Medicine earliest = getEarliestExpiry();
        Medicine latest = getLatestExpiry();
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-uuuu");

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
        // Build totals (reuse your existing NameTotal logic)
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
        java.time.LocalDate cutoff = java.time.LocalDate.now().plusDays(days);
        DoublyLinkedList<Medicine> tmp = new DoublyLinkedList<>();
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine m = medicineList.getEntry(i);
            if (!m.getExpiryDate().isAfter(cutoff)) {
                tmp.add(m);
            }
        }
        if (tmp.isEmpty()) {
            return new String[0][0];
        }
        tmp.mergeSort(BY_EXPIRY_THEN_ID);
        return toRowsById(tmp);
    }

    public MaintainPharmacy() {
        medicineList = medicineDAO.retrieveFromFile();
    }

    public void runPharmacyMenu() {
        int choice = -1;
        do {
            choice = pharmacyUI.getMenuChoice();
            switch (choice) {
                case 1:
                    viewAllMedicines();
                    break;
                case 2:
                    addMedicine();
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
            }
        } while (choice != 0);
    }

    public void addMedicine() {
        MessageUI.clearConsole();
        String newId = generateNextMedicineId();
        Medicine newMed = pharmacyUI.inputMedicineDetails(newId);
        if (newMed == null) {
            return;
        }

        MessageUI.clearConsole();

        while (true) {
            int action = pharmacyUI.confirmAddMedicine(newMed);

            if (action == 1) { // Confirm
                medicineList.add(newMed);
                medicineDAO.saveToFile(medicineList);
                return;
            } else if (action == 2) { // Edit
                newMed = pharmacyUI.editMedicineDetails(newMed);
                if (newMed == null) {
                    return; // Cancelled while editing
                }
            } else if (action == 3) { // Discard
                return;
            }
        }
    }

    private String generateNextMedicineId() {
        int max = 0;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            String id = medicineList.getEntry(i).getMedId().substring(1); // skip 'M'
            int num = Integer.parseInt(id);
            if (num > max) {
                max = num;
            }
        }
        return String.format("M%04d", max + 1);
    }

    public void viewAllMedicines() {
        MessageUI.clearConsole();

        if (medicineList.isEmpty()) {
            MessageUI.displayEmptyMessage();
            MessageUI.pressEnterToContinue();
            return;
        }

        int choice = pharmacyUI.getViewChoice();
        switch (choice) {
            case 0:
                return;

            case 1: { // ID
                int dir = pharmacyUI.getSortDirection("by ID",
                        "ID ascending (M0001 → M9999)",
                        "ID descending (M9999 → M0001)");
                DoublyLinkedList<Medicine> tmp = copyList();
                if (dir == 1) {
                    tmp.mergeSort(BY_ID_NUM);
                } else {
                    tmp.mergeSort((a, b) -> BY_ID_NUM.compare(b, a)); // DESC
                }
                pharmacyUI.displayMedicineInventory(renderList(tmp));
                break;
            }

            case 2: { // Expiry
                int dir = pharmacyUI.getSortDirection("by Expiry Date",
                        "Earliest first",
                        "Latest first");
                DoublyLinkedList<Medicine> tmp = copyList();
                if (dir == 1) {
                    tmp.mergeSort(BY_EXPIRY_THEN_ID);
                } else {
                    tmp.mergeSort((a, b) -> BY_EXPIRY_THEN_ID.compare(b, a)); // DESC
                }
                pharmacyUI.displayMedicineInventory(renderList(tmp));
                break;
            }

            case 3: { // Price
                int dir = pharmacyUI.getSortDirection("by Price",
                        "Lowest first",
                        "Highest first");
                DoublyLinkedList<Medicine> tmp = copyList();
                if (dir == 1) {
                    tmp.mergeSort(BY_PRICE_THEN_ID);
                } else {
                    tmp.mergeSort((a, b) -> BY_PRICE_THEN_ID.compare(b, a)); // DESC
                }
                pharmacyUI.displayMedicineInventory(renderList(tmp));
                break;
            }

            case 4: { // Group by name: total qty
                String totalsText = buildGroupedTotalsText();
                pharmacyUI.displayGroupedTotals(totalsText);
                break;
            }
        }
        MessageUI.pressEnterToContinue();
    }

    public void dispenseMedicine() {
        MessageUI.clearConsole();
        StringBuilder patientOutput = new StringBuilder();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            patientOutput.append(i).append(". ")
                    .append(p.getId()).append(" - ")
                    .append(p.getName()).append("\n");
        }
        pharmacyUI.displayMessage("Select a patient:\n" + patientOutput);

        int selectedIndex = pharmacyUI.inputPatientSelection();
        if (selectedIndex < 1 || selectedIndex > patientList.getNumberOfEntries()) {
            pharmacyUI.displayMessage("Invalid selection.");
            return;
        }

        Patient selectedPatient = patientList.getEntry(selectedIndex);

        ListInterface<Treatment> treatments = treatmentDAO.retrieveFromFile();
        String prescription = "";
        for (int i = treatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment t = treatments.getEntry(i);
            if (t.getPatientId().equals(selectedPatient.getId())) {
                prescription = t.getPrescription();
                break;
            }
        }

        if (prescription.isEmpty()) {
            pharmacyUI.displayMessage("\nNo prescription found for this patient.");
            return;
        }

        String[] meds = prescription.split(",");
        for (String med : meds) {
            String[] parts = med.split(":");
            String medId = parts[0].trim();
            int qty = Integer.parseInt(parts[1].trim());

            boolean found = false;
            for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
                Medicine m = medicineList.getEntry(i);
                if (m.getMedId().equals(medId)) {
                    found = true;
                    if (m.getStock() >= qty) {
                        m.setStock(m.getStock() - qty);
                        pharmacyUI.displayMessage("Dispensed " + qty + " units of " + m.getName());
                        DispenseRecord rec = new DispenseRecord(
                                selectedPatient.getId(),
                                selectedPatient.getName(),
                                m.getMedId(),
                                m.getName(),
                                qty,
                                LocalDateTime.now()
                        );
                        dispenseLog.add(rec);
                    } else {
                        pharmacyUI.displayMessage("Insufficient stock for " + m.getName());
                    }
                    break;
                }
            }

            if (!found) {
                pharmacyUI.displayMessage("Medicine " + medId + " not found in inventory.");
            }
        }

        medicineDAO.saveToFile(medicineList);
        dispenseDAO.saveToFile(dispenseLog);
    }

    public void generateInventoryReport() {
        MessageUI.clearConsole();
        if (medicineList.isEmpty()) {
            pharmacyUI.displayReport("Inventory Report", "");
            MessageUI.pressEnterToContinue();
            return;
        }

        int choice = pharmacyUI.getInventoryReportChoice();
        switch (choice) {
            case 0:
                return;

            case 1: { // Snapshot (tables)
                // KPIs table
                pharmacyUI.displayTable("== KPIs ==", new String[]{"Metric", "Value"}, toRowsKpis());

                // Inventory by ID table (stable ascending)
                DoublyLinkedList<Medicine> byId = copyList();
                byId.mergeSort(BY_ID_NUM);
                pharmacyUI.displayTable("== Inventory (By ID) ==",
                        new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                        toRowsById(byId));

                // Totals by Name table
                pharmacyUI.displayTable("== Totals By Name ==",
                        new String[]{"Medicine Name", "Total Qty"},
                        toRowsTotalsByName());
                break;
            }

            case 2: { // Low-stock table
                int threshold = pharmacyUI.inputLowStockThreshold();
                String[][] rows = toRowsLowStock(threshold);
                if (rows.length == 0) {
                    pharmacyUI.displayReport("Inventory Report (Low-stock)", "(No items at or below threshold)");
                } else {
                    pharmacyUI.displayTable("== Low-stock (<= " + threshold + ") ==",
                            new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                            rows);
                }
                break;
            }

            case 3: { // Near-expiry table
                int days = pharmacyUI.inputNearExpiryDays();
                String[][] rows = toRowsNearExpiry(days);
                if (rows.length == 0) {
                    pharmacyUI.displayReport("Inventory Report (Near-expiry)", "(No items expiring within " + days + " days)");
                } else {
                    pharmacyUI.displayTable("== Near-expiry (within " + days + " days) ==",
                            new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                            rows);
                }
                break;
            }

            case 4: { // Snapshot + export (still export your existing text version)
                String body = buildInventorySnapshotText(); // keep your text snapshot for file
                String path = exportReportToFile("Inventory Report (Snapshot)", body);

                // On-screen: also show the tables for a nicer UI
                pharmacyUI.displayTable("== KPIs ==", new String[]{"Metric", "Value"}, toRowsKpis());
                DoublyLinkedList<Medicine> byId = copyList();
                byId.mergeSort(BY_ID_NUM);
                pharmacyUI.displayTable("== Inventory (By ID) ==",
                        new String[]{"Med ID", "Name", "Qty", "Price/Unit", "Expiry"},
                        toRowsById(byId));
                pharmacyUI.displayTable("== Totals By Name ==",
                        new String[]{"Medicine Name", "Total Qty"},
                        toRowsTotalsByName());

                if (path == null) {
                    pharmacyUI.displayMessage("\nFailed to export report.\n");
                } else {
                    pharmacyUI.displayExportResult(path);
                }
                break;
            }
        }
        MessageUI.pressEnterToContinue();
    }

    public void generateDispensedReport() {
        MessageUI.clearConsole();

        // refresh log from file in case other ops wrote
        dispenseLog = dispenseDAO.retrieveFromFile();
        if (dispenseLog.isEmpty()) {
            pharmacyUI.displayReport("Dispensed Medicines Report", "");
            MessageUI.pressEnterToContinue();
            return;
        }

        int choice = pharmacyUI.getDispensedReportChoice();
        if (choice == 0) {
            return;
        }

        switch (choice) {
            case 1: { // all time detail
                pharmacyUI.displayReport("Dispensed (All Time - Detail)", renderDispenseDetail(dispenseLog));
                break;
            }
            case 2: { // today summary by medicine
                LocalDate today = LocalDate.now();
                pharmacyUI.displayReport("Dispensed Today (By Medicine)", renderDispenseSummaryByMedicine(filterByDateRange(today, today)));
                break;
            }
            case 3: { // last 7 days summary by medicine
                LocalDate end = LocalDate.now();
                LocalDate start = end.minusDays(6);
                pharmacyUI.displayReport("Dispensed Last 7 Days (By Medicine)", renderDispenseSummaryByMedicine(filterByDateRange(start, end)));
                break;
            }
            case 4: { // custom range by medicine
                LocalDate start = pharmacyUI.inputDate("Start date");
                LocalDate end = pharmacyUI.inputDate("End date");
                pharmacyUI.displayReport("Dispensed (" + start + " to " + end + ") (By Medicine)", renderDispenseSummaryByMedicine(filterByDateRange(start, end)));
                break;
            }
            case 5: { // custom range by patient
                LocalDate start = pharmacyUI.inputDate("Start date");
                LocalDate end = pharmacyUI.inputDate("End date");
                pharmacyUI.displayReport("Dispensed (" + start + " to " + end + ") (By Patient)", renderDispenseSummaryByPatient(filterByDateRange(start, end)));
                break;
            }
        }
        MessageUI.pressEnterToContinue();
    }

    public static void main(String[] args) {
        MaintainPharmacy pharmacy = new MaintainPharmacy();
        pharmacy.runPharmacyMenu();
    }
}
