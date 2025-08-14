package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.TreatmentUI;
import dao.TreatmentDAO;
import entity.Treatment;

public class MaintainTreatment {
    private final ListInterface<Treatment> treatments;
    private final TreatmentDAO treatmentDAO;
    private final TreatmentUI treatmentUI;

    public MaintainTreatment() {
        this.treatmentDAO = new TreatmentDAO();
        this.treatments = treatmentDAO.retrieveFromFile();
        this.treatmentUI = new TreatmentUI(this);
    }

    public void addTreatment() {
        Treatment newTreatment = treatmentUI.inputTreatmentDetails();
        newTreatment.setTreatmentId(generateNextTreatmentId());
        
        if (validateTreatment(newTreatment)) {
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            treatmentUI.displayMessage("Treatment added successfully! ID: " + newTreatment.getTreatmentId());
        }
    }

    private String generateNextTreatmentId() {
        if (treatments.isEmpty()) {
            return "T0001";
        }

        int maxId = 0;
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            String currentId = treatments.getEntry(i).getTreatmentId();
            if (currentId != null && currentId.matches("T\\d{4}")) {
                int num = Integer.parseInt(currentId.substring(1));
                maxId = Math.max(maxId, num);
            }
        }

        if (maxId >= 9999) {
            throw new IllegalStateException("Maximum treatment ID limit reached (T9999)");
        }

        return String.format("T%04d", maxId + 1);
    }

    private boolean validateTreatment(Treatment treatment) {
        if (treatment.getTreatmentId() == null || !treatment.getTreatmentId().matches("T\\d{4}")) {
            treatmentUI.displayMessage("Error: Invalid treatment ID format.");
            return false;
        }
        
        if (treatments.contains(treatment)) {
            treatmentUI.displayMessage("Error: Treatment ID already exists.");
            return false;
        }
        
        if (treatment.getSeverityLevel() < 1 || treatment.getSeverityLevel() > 5) {
            treatmentUI.displayMessage("Error: Severity must be between 1-5.");
            return false;
        }
        
        return true;
    }

    public void updateTreatment() {
        String id = treatmentUI.inputTreatmentId();
        Treatment existing = findTreatmentById(id);
        
        if (existing != null) {
            Treatment updated = treatmentUI.inputUpdatedDetails(existing);
            if (replaceTreatment(existing, updated)) {
                treatmentDAO.saveToFile(treatments);
                treatmentUI.displayMessage("Treatment updated successfully!");
            }
        } else {
            treatmentUI.displayMessage("Treatment not found.");
        }
    }

    public void deleteTreatment() {
        String id = treatmentUI.inputTreatmentId();
        boolean removed = false;
        
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            if (treatments.getEntry(i).getTreatmentId().equals(id)) {
                treatments.remove(i);
                treatmentDAO.saveToFile(treatments);
                removed = true;
                break;
            }
        }
        
        treatmentUI.displayMessage(removed ? "Treatment deleted successfully!" : "Treatment not found.");
    }

    public void searchTreatment() {
        String id = treatmentUI.inputTreatmentId();
        Treatment treatment = findTreatmentById(id);
        treatmentUI.displayTreatment(treatment);
    }

    public void viewAllTreatments() {
        if (treatments.isEmpty()) {
            treatmentUI.displayMessage("No treatments found.");
            return;
        }
        
        StringBuilder output = new StringBuilder("ALL TREATMENTS:\n");
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            output.append(treatments.getEntry(i).toString()).append("\n----------------\n");
        }
        treatmentUI.displayMessage(output.toString());
    }

    public void generateTreatmentAnalysis() {
        if (treatments.isEmpty()) {
            treatmentUI.displayMessage("No treatment records available.");
            return;
        }

        int[] severityCounts = new int[5];
        int totalPrescriptions = 0;
        
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment t = treatments.getEntry(i);
            severityCounts[t.getSeverityLevel()-1]++;
            if (!t.getPrescription().trim().isEmpty()) {
                totalPrescriptions++;
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("=== TREATMENT ANALYSIS REPORT ===\n\n");
        report.append("Case Severity Overview:\n");
        for (int i = 0; i < 5; i++) {
            report.append(String.format("Level %d (%-7s): %d cases\n", 
                i+1, getSeverityLabel(i+1), severityCounts[i]));
        }
        
        report.append("\nRecent Treatment Activity:\n");
        int displayCount = Math.min(5, treatments.getNumberOfEntries());
        for (int i = 1; i <= displayCount; i++) {
            Treatment t = treatments.getEntry(i);
            report.append(String.format("- %s: %s (Severity %d)\n",
                t.getTreatmentId(), t.getDiagnosis(), t.getSeverityLevel()));
        }

        treatmentUI.displayReport(report.toString());
    }

    public void generateDoctorWorkload() {
        if (treatments.isEmpty()) {
            treatmentUI.displayMessage("No treatment records available.");
            return;
        }

        DoublyLinkedList<String> doctorIds = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> treatmentCounts = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> prescriptionCounts = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment t = treatments.getEntry(i);
            String doctorId = t.getDoctorId();
            
            int index = doctorIds.linearSearch(doctorId);
            if (index == -1) {
                doctorIds.add(doctorId);
                treatmentCounts.add(1);
                prescriptionCounts.add(t.getPrescription().trim().isEmpty() ? 0 : 1);
            } else {
                treatmentCounts.replace(index, treatmentCounts.getEntry(index) + 1);
                if (!t.getPrescription().trim().isEmpty()) {
                    prescriptionCounts.replace(index, prescriptionCounts.getEntry(index) + 1);
                }
            }
        }

        sortDoctorsByActivity(doctorIds, treatmentCounts, prescriptionCounts);

        StringBuilder report = new StringBuilder();
        report.append("=== DOCTOR WORKLOAD REPORT ===\n\n");
        report.append(String.format("%-10s %-15s %-15s %s\n", 
            "Doctor ID", "Cases Handled", "Prescriptions", "Workload %"));

        int totalTreatments = treatments.getNumberOfEntries();
        for (int i = 1; i <= doctorIds.getNumberOfEntries(); i++) {
            String doctorId = doctorIds.getEntry(i);
            int count = treatmentCounts.getEntry(i);
            int prescriptions = prescriptionCounts.getEntry(i);
            
            report.append(String.format("%-10s %-15d %-15d %.1f%%\n",
                doctorId, count, prescriptions, percentage(count, totalTreatments)));
        }

        treatmentUI.displayReport(report.toString());
    }

    private String getSeverityLabel(int level) {
        switch(level) {
            case 1: return "Mild";
            case 2: return "Moderate";
            case 3: return "Serious";
            case 4: return "Severe";
            case 5: return "Critical";
            default: return "Unknown";
        }
    }

    private void sortDoctorsByActivity(DoublyLinkedList<String> doctorIds, 
                                     DoublyLinkedList<Integer> treatmentCounts,
                                     DoublyLinkedList<Integer> prescriptionCounts) {
        boolean swapped;
        do {
            swapped = false;
            for (int i = 1; i < doctorIds.getNumberOfEntries(); i++) {
                if (treatmentCounts.getEntry(i) < treatmentCounts.getEntry(i+1)) {
                    swap(doctorIds, i, i+1);
                    swap(treatmentCounts, i, i+1);
                    swap(prescriptionCounts, i, i+1);
                    swapped = true;
                }
            }
        } while (swapped);
    }

    private <T> void swap(DoublyLinkedList<T> list, int i, int j) {
        T temp = list.getEntry(i);
        list.replace(i, list.getEntry(j));
        list.replace(j, temp);
    }

    public Treatment findTreatmentById(String id) {
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment t = treatments.getEntry(i);
            if (t.getTreatmentId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    private boolean replaceTreatment(Treatment old, Treatment updated) {
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            if (treatments.getEntry(i).equals(old)) {
                treatments.replace(i, updated);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    private double percentage(int part, int whole) {
        return whole == 0 ? 0 : (part * 100.0) / whole;
    }

    public void runTreatmentMenu() {
        treatmentUI.run();
    }
}