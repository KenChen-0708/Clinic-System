package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import adt.LinkedQueue;
import adt.QueueInterface;
import dao.TreatmentDAO;
import entity.Doctor;
import entity.Patient;
import entity.Treatment;
import entity.TreatmentMedicineList;
import entity.Diagnosis;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class MaintainTreatment {

    private DoublyLinkedList<Treatment> treatments;
    private final TreatmentDAO treatmentDAO;
    private final MaintainDiagnosis maintainDiagnosis;

    // Enhanced Queue Management using ADT
    private static QueueInterface<Patient> servingPatientsQueue = new LinkedQueue<>();
    private static QueueInterface<Doctor> servingDoctorsQueue = new LinkedQueue<>();
    private static Patient currentServingPatient = null;
    private static Doctor currentConsultingDoctor = null;

    // Cached ADT options for performance optimization
    private DoublyLinkedList<String> statusOptions;
    private DoublyLinkedList<String> mealTimingOptions;
    private DoublyLinkedList<String> availableMedicines;

    public MaintainTreatment() {
        this.treatmentDAO = new TreatmentDAO();
        this.treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        this.maintainDiagnosis = new MaintainDiagnosis();
        initializeCachedOptions();
    }

    // Initialize cached options using ADT for better performance
    private void initializeCachedOptions() {
        statusOptions = new DoublyLinkedList<>();
        statusOptions.add("Pending");
        statusOptions.add("Completed");

        mealTimingOptions = new DoublyLinkedList<>();
        mealTimingOptions.add("Before meal");
        mealTimingOptions.add("After meal");

        loadAvailableMedicines();
    }

    private void loadAvailableMedicines() {
        availableMedicines = new DoublyLinkedList<>();
        try {
            boundary.PharmacyUI pharmacyUI = new boundary.PharmacyUI();
            ListInterface<String> medicineNames = pharmacyUI.getMedicineNames();

            for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
                availableMedicines.add(medicineNames.getEntry(i));
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load medicines from pharmacy.");
        }
    }

    // Getters for cached ADT options
    public DoublyLinkedList<String> getStatusOptions() {
        return statusOptions;
    }

    public DoublyLinkedList<String> getMealTimingOptions() {
        return mealTimingOptions;
    }

    public DoublyLinkedList<String> getAvailableMedicines() {
        return availableMedicines;
    }

    // ================================
    // SERVING QUEUE MANAGEMENT
    // ================================

    public static Patient getServingPatient() {
        if (currentServingPatient == null && !servingPatientsQueue.isEmpty()) {
            currentServingPatient = servingPatientsQueue.peek();
        }
        return currentServingPatient;
    }

    public static Doctor getDoctorConsulting() {
        if (currentConsultingDoctor == null && !servingDoctorsQueue.isEmpty()) {
            currentConsultingDoctor = servingDoctorsQueue.peek();
        }
        return currentConsultingDoctor;
    }

    public static void addServingPatientDoctorPair(Patient patient, Doctor doctor) {
        if (patient != null && doctor != null) {
            servingPatientsQueue.enqueue(patient);
            servingDoctorsQueue.enqueue(doctor);

            if (currentServingPatient == null && currentConsultingDoctor == null) {
                currentServingPatient = patient;
                currentConsultingDoctor = doctor;
            }
        }
    }

    public static void removeCurrentServingPair() {
        if (!servingPatientsQueue.isEmpty() && !servingDoctorsQueue.isEmpty()) {
            servingPatientsQueue.dequeue();
            servingDoctorsQueue.dequeue();

            if (!servingPatientsQueue.isEmpty() && !servingDoctorsQueue.isEmpty()) {
                currentServingPatient = servingPatientsQueue.peek();
                currentConsultingDoctor = servingDoctorsQueue.peek();
            } else {
                currentServingPatient = null;
                currentConsultingDoctor = null;
            }
        }
    }

    public static boolean hasServingPatients() {
        return !servingPatientsQueue.isEmpty();
    }

    public static int getServingPatientsCount() {
        return servingPatientsQueue.size();
    }

    public static void clearAllServingQueues() {
        servingPatientsQueue.clear();
        servingDoctorsQueue.clear();
        currentServingPatient = null;
        currentConsultingDoctor = null;
    }

    public static void displayServingStatus() {
        System.out.println("\n=== CURRENT SERVING STATUS ===");
        if (currentServingPatient != null && currentConsultingDoctor != null) {
            System.out.println("Currently serving:");
            System.out.println("  Patient: " + currentServingPatient.getId() + " (" + currentServingPatient.getName() + ")");
            System.out.println("  Doctor: " + currentConsultingDoctor.getId() + " (" + currentConsultingDoctor.getName() + ")");
        } else {
            System.out.println("No active consultation.");
        }
        System.out.println("Waiting: " + (servingPatientsQueue.size() > 1 ? (servingPatientsQueue.size() - 1) : 0) + " patients");
        System.out.println("===============================\n");
    }

    // ================================
    // CRUD OPERATIONS USING ADT
    // ================================

    // CREATE - Basic treatment creation
    public boolean createTreatment(String patientId, String doctorId, String status, LocalDate treatmentDate) {
        if (hasAvailableDiagnosisForTreatment(patientId, doctorId)) {
            Treatment newTreatment = new Treatment(patientId, doctorId, status, treatmentDate);
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            return true;
        }
        return false;
    }

    // CREATE - Treatment with medicines
    public boolean createTreatmentWithMedicines(String patientId, String doctorId, String status,
            LocalDate treatmentDate, DoublyLinkedList<TreatmentMedicineList> medicines) {
        if (hasAvailableDiagnosisForTreatment(patientId, doctorId)) {
            Treatment newTreatment = new Treatment(patientId, doctorId, status, treatmentDate);
            newTreatment.setMedicineList(medicines);
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            return true;
        }
        return false;
    }

    // Enhanced diagnosis validation using ADT operations
    private boolean hasAvailableDiagnosisForTreatment(String patientId, String doctorId) {
        DoublyLinkedList<Diagnosis> patientDoctorDiagnoses = getPatientDoctorDiagnoses(patientId, doctorId);
        if (patientDoctorDiagnoses.isEmpty()) {
            return false;
        }

        DoublyLinkedList<Treatment> existingTreatments = findTreatmentsByPatientAndDoctor(patientId, doctorId);
        if (existingTreatments.isEmpty()) {
            return true;
        }

        return patientDoctorDiagnoses.getNumberOfEntries() > existingTreatments.getNumberOfEntries();
    }

    // Get diagnoses using ADT filtering
    private DoublyLinkedList<Diagnosis> getPatientDoctorDiagnoses(String patientId, String doctorId) {
        DoublyLinkedList<Diagnosis> allDiagnoses = maintainDiagnosis.getAllDiagnoses();
        DoublyLinkedList<Diagnosis> filtered = new DoublyLinkedList<>();

        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            if (diagnosis.getPatientId().equalsIgnoreCase(patientId) && 
                diagnosis.getDoctorId().equalsIgnoreCase(doctorId)) {
                filtered.add(diagnosis);
            }
        }
        return filtered;
    }

    // READ - Get all treatments
    public DoublyLinkedList<Treatment> getAllTreatments() {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return treatments;
    }

    // READ - Find by ID using ADT search
    public Treatment findTreatmentById(String treatmentId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                return treatment;
            }
        }
        return null;
    }

    // READ - Find by Patient ID using ADT filtering
    public DoublyLinkedList<Treatment> findTreatmentsByPatientId(String patientId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(patientId, "patient");
    }

    // READ - Find by Doctor ID using ADT filtering
    public DoublyLinkedList<Treatment> findTreatmentsByDoctorId(String doctorId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(doctorId, "doctor");
    }

    // READ - Find by Status using ADT filtering
    public DoublyLinkedList<Treatment> findTreatmentsByStatus(String status) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(status, "status");
    }

    // READ - Find by both Patient and Doctor using ADT filtering
    public DoublyLinkedList<Treatment> findTreatmentsByPatientAndDoctor(String patientId, String doctorId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> result = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(patientId) && 
                treatment.getDoctorId().equalsIgnoreCase(doctorId)) {
                result.add(treatment);
            }
        }
        return result;
    }

    // Helper method for filtering treatments using ADT
    private DoublyLinkedList<Treatment> filterTreatments(String value, String field) {
        DoublyLinkedList<Treatment> filtered = new DoublyLinkedList<>();
        
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            String compareValue = getFieldValue(treatment, field);
            
            if (compareValue.equalsIgnoreCase(value)) {
                filtered.add(treatment);
            }
        }
        return filtered;
    }

    private String getFieldValue(Treatment treatment, String field) {
        switch (field) {
            case "patient": return treatment.getPatientId();
            case "doctor": return treatment.getDoctorId();
            case "status": return treatment.getStatus();
            default: return "";
        }
    }

    // UPDATE - Update treatment status
    public boolean updateTreatmentStatus(String treatmentId, String newStatus) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return updateTreatmentField(treatmentId, newStatus, "status");
    }

    // UPDATE - Update treatment date
    public boolean updateTreatmentDate(String treatmentId, LocalDate newDate) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                treatment.setTreatmentDate(newDate);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // UPDATE - Add medicine to treatment
    public boolean addMedicineToTreatment(String treatmentId, TreatmentMedicineList medicine) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                treatment.addMedicine(medicine);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // UPDATE - Update entire medicine list
    public boolean updateTreatmentMedicines(String treatmentId, DoublyLinkedList<TreatmentMedicineList> newMedicines) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                treatment.setMedicineList(newMedicines);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // Helper method for updating treatment fields
    private boolean updateTreatmentField(String treatmentId, String newValue, String field) {
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                if (field.equals("status")) {
                    treatment.setStatus(newValue);
                }
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // DELETE - Delete by ID using ADT remove operation
    public boolean deleteTreatmentById(String treatmentId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                treatments.remove(i);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // DELETE - Delete multiple treatments using ADT reverse iteration
    public int deleteTreatmentsByPatientId(String patientId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return deleteMultipleTreatments(patientId, "patient");
    }

    public int deleteTreatmentsByDoctorId(String doctorId) {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return deleteMultipleTreatments(doctorId, "doctor");
    }

    // Helper method for deleting multiple treatments
    private int deleteMultipleTreatments(String value, String field) {
        int deletedCount = 0;
        
        // Reverse iteration to avoid index shifting issues
        for (int i = treatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment treatment = treatments.getEntry(i);
            String compareValue = getFieldValue(treatment, field);
            
            if (compareValue.equalsIgnoreCase(value)) {
                treatments.remove(i);
                deletedCount++;
            }
        }
        
        if (deletedCount > 0) {
            treatmentDAO.saveToFile(treatments);
        }
        return deletedCount;
    }

    // DELETE - Clear all treatments
    public boolean deleteAllTreatments() {
        treatments.clear();
        treatmentDAO.saveToFile(treatments);
        return true;
    }

    // ================================
    // UTILITY METHODS
    // ================================

    public boolean patientHasDiagnosisForTreatment(String patientId, String doctorId) {
        return hasAvailableDiagnosisForTreatment(patientId, doctorId);
    }

    public boolean patientHasDiagnosis(String patientId) {
        return !maintainDiagnosis.findDiagnosesByPatientId(patientId).isEmpty();
    }

    public boolean treatmentExists(String treatmentId) {
        return findTreatmentById(treatmentId) != null;
    }

    public int getTreatmentCount() {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        return treatments.getNumberOfEntries();
    }

    // ================================
    // ADVANCED REPORTING WITH ADT OPERATIONS
    // ================================

    public String getTreatmentReport() {
        treatments = (DoublyLinkedList<Treatment>) treatmentDAO.retrieveFromFile();
        
        if (treatments.isEmpty()) {
            return formatEmptyReport();
        }

        StringBuilder report = new StringBuilder();
        report.append(formatReportHeader());
        report.append(formatBasicStats());
        report.append(formatStatusAnalysis());
        report.append(formatPatientDoctorAnalysis());
        report.append(formatMedicineAnalysis());
        report.append(formatReportFooter());

        return report.toString();
    }

    private String formatReportHeader() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return "================================================================================\n" +
               "                        HOSPITAL MANAGEMENT SYSTEM\n" +
               "                         TREATMENT ANALYSIS REPORT\n" +
               "================================================================================\n" +
               "Generated on: " + currentDate + "\n" +
               "Report Type : TREATMENT MANAGEMENT ANALYSIS\n" +
               "================================================================================\n\n";
    }

    private String formatBasicStats() {
        return "BASIC STATISTICS:\n" +
               "================================================================================\n" +
               String.format("%-40s | %10d\n", "Total Treatments", treatments.getNumberOfEntries()) +
               "--------------------------------------------------------------------------------\n\n";
    }

    private String formatStatusAnalysis() {
        DoublyLinkedList<String> statusList = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> statusCounts = new DoublyLinkedList<>();

        // Count treatments by status using ADT
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            String status = treatment.getStatus().toLowerCase();

            boolean found = false;
            for (int j = 1; j <= statusList.getNumberOfEntries(); j++) {
                if (statusList.getEntry(j).equals(status)) {
                    statusCounts.replace(j, statusCounts.getEntry(j) + 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                statusList.add(status);
                statusCounts.add(1);
            }
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("STATUS ANALYSIS:\n");
        analysis.append("================================================================================\n");

        for (int i = 1; i <= statusList.getNumberOfEntries(); i++) {
            String status = statusList.getEntry(i);
            int count = statusCounts.getEntry(i);
            double percentage = (count * 100.0) / treatments.getNumberOfEntries();
            analysis.append(String.format("%-20s | %8d | %6.1f%%\n", 
                status.toUpperCase(), count, percentage));
        }

        analysis.append("--------------------------------------------------------------------------------\n\n");
        return analysis.toString();
    }

    private String formatPatientDoctorAnalysis() {
        DoublyLinkedList<String> uniquePatients = new DoublyLinkedList<>();
        DoublyLinkedList<String> uniqueDoctors = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);

            if (!uniquePatients.contains(treatment.getPatientId())) {
                uniquePatients.add(treatment.getPatientId());
            }

            if (!uniqueDoctors.contains(treatment.getDoctorId())) {
                uniqueDoctors.add(treatment.getDoctorId());
            }
        }

        return "PATIENT & DOCTOR ANALYSIS:\n" +
               "================================================================================\n" +
               String.format("%-30s | %10d\n", "Unique Patients", uniquePatients.getNumberOfEntries()) +
               String.format("%-30s | %10d\n", "Unique Doctors", uniqueDoctors.getNumberOfEntries()) +
               "--------------------------------------------------------------------------------\n\n";
    }

    private String formatMedicineAnalysis() {
        int treatmentsWithMedicines = 0;
        int totalMedicinesPrescribed = 0;
        DoublyLinkedList<String> uniqueMedicines = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            DoublyLinkedList<TreatmentMedicineList> medicines = treatment.getMedicineList();

            if (medicines != null && !medicines.isEmpty()) {
                treatmentsWithMedicines++;
                totalMedicinesPrescribed += medicines.getNumberOfEntries();

                for (int j = 1; j <= medicines.getNumberOfEntries(); j++) {
                    String medicine = medicines.getEntry(j).getPrescription();
                    if (!uniqueMedicines.contains(medicine)) {
                        uniqueMedicines.add(medicine);
                    }
                }
            }
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("MEDICINE ANALYSIS:\n");
        analysis.append("================================================================================\n");
        analysis.append(String.format("%-35s | %10d\n", "Treatments with Medicines", treatmentsWithMedicines));
        analysis.append(String.format("%-35s | %10d\n", "Treatments without Medicines", 
            treatments.getNumberOfEntries() - treatmentsWithMedicines));
        analysis.append(String.format("%-35s | %10d\n", "Total Medicines Prescribed", totalMedicinesPrescribed));
        analysis.append(String.format("%-35s | %10d\n", "Unique Medicine Types", uniqueMedicines.getNumberOfEntries()));

        if (treatmentsWithMedicines > 0) {
            double avgMedicinesPerTreatment = (double) totalMedicinesPrescribed / treatmentsWithMedicines;
            analysis.append(String.format("%-35s | %10.1f\n", "Avg Medicines per Treatment", avgMedicinesPerTreatment));
        }

        analysis.append("--------------------------------------------------------------------------------\n\n");
        return analysis.toString();
    }

    private String formatEmptyReport() {
        return "================================================================================\n" +
               "                        HOSPITAL MANAGEMENT SYSTEM\n" +
               "                         TREATMENT ANALYSIS REPORT\n" +
               "================================================================================\n" +
               "No treatments found in the system.\n" +
               "Please create treatments before generating reports.\n" +
               "================================================================================\n";
    }

    private String formatReportFooter() {
        return "================================================================================\n" +
               "                            END OF REPORT\n" +
               "================================================================================\n";
    }

    // UI Integration
    public void runTreatmentMenu() {
        boundary.TreatmentUI treatmentUI = new boundary.TreatmentUI(this);
        treatmentUI.run();
    }

    public boundary.TreatmentUI getTreatmentUI() {
        return new boundary.TreatmentUI(this);
    }
}