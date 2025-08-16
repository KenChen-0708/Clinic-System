package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.TreatmentUI;
import dao.TreatmentDAO;
import entity.Doctor;
import entity.Patient;
import entity.Treatment;
import entity.TreatmentMedicineList;
import java.lang.reflect.Field;
import java.time.LocalDate;

public class MaintainTreatment {

    private ListInterface<Treatment> treatments;
    private final TreatmentDAO treatmentDAO;
    private final TreatmentUI treatmentUI;
    private static Patient servingPatient;
    private static Doctor doctorConsulting;
    private final MaintainDiagnosis maintainDiagnosis; // Add this to check diagnosis

    public MaintainTreatment() {
        this.treatmentDAO = new TreatmentDAO();
        this.treatments = treatmentDAO.retrieveFromFile();
        this.maintainDiagnosis = new MaintainDiagnosis(); // Initialize diagnosis controller
        this.treatmentUI = new TreatmentUI(this);
    }

    private void initializeTreatmentCounter() {
        try {
            // Use reflection to access the private static counter field
            Field counterField = Treatment.class.getDeclaredField("counter");
            counterField.setAccessible(true);

            treatments = treatmentDAO.retrieveFromFile();
            int maxId = 0;

            for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
                Treatment treatment = treatments.getEntry(i);
                String treatmentId = treatment.getTreatmentId();
                if (treatmentId != null && treatmentId.startsWith("T")) {
                    try {
                        int idNumber = Integer.parseInt(treatmentId.substring(1));
                        if (idNumber > maxId) {
                            maxId = idNumber;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                        System.err.println("Skipping invalid treatment ID: " + treatmentId);
                    }
                }
            }

            // Set counter to next available number
            counterField.setInt(null, maxId + 1);
            System.out.println("Treatment counter initialized to: " + (maxId + 1));

        } catch (Exception e) {
            System.err.println("Error initializing treatment counter: " + e.getMessage());
            // If reflection fails, counter will remain at default value (1)
        }
    }

    public void runTreatmentMenu() {
        treatmentUI.run();
        return;
    }

    // CREATE - Create new treatment (only if patient has diagnosis)
    public boolean createTreatment(String patientId, String doctorId, String status, LocalDate treatmentDate) {
        // Check if patient has existing diagnosis
        if (!maintainDiagnosis.findDiagnosesByPatientId(patientId).isEmpty()) {
            Treatment newTreatment = new Treatment(patientId, doctorId, status, treatmentDate);
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            return true;
        }
        return false; // Patient has no diagnosis
    }

    // CREATE - Create treatment with medicine list
    public boolean createTreatmentWithMedicines(String patientId, String doctorId, String status,
            LocalDate treatmentDate, DoublyLinkedList<TreatmentMedicineList> medicines) {
        // Check if patient has existing diagnosis
        if (!maintainDiagnosis.findDiagnosesByPatientId(patientId).isEmpty()) {
            Treatment newTreatment = new Treatment(patientId, doctorId, status, treatmentDate);
            newTreatment.setMedicineList(medicines);
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            return true;
        }
        return false; // Patient has no diagnosis
    }

    // READ - Get all treatments
    public ListInterface<Treatment> getAllTreatments() {
        treatments = treatmentDAO.retrieveFromFile();
        return treatments;
    }

    // READ - Find treatment by ID
    public Treatment findTreatmentById(String treatmentId) {
        treatments = treatmentDAO.retrieveFromFile();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                return treatment;
            }
        }
        return null;
    }

    // READ - Find treatments by Patient ID
    public DoublyLinkedList<Treatment> findTreatmentsByPatientId(String patientId) {
        treatments = treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> patientTreatments = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(patientId)) {
                patientTreatments.add(treatment);
            }
        }
        return patientTreatments;
    }

    // READ - Find treatments by Doctor ID
    public DoublyLinkedList<Treatment> findTreatmentsByDoctorId(String doctorId) {
        treatments = treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> doctorTreatments = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getDoctorId().equalsIgnoreCase(doctorId)) {
                doctorTreatments.add(treatment);
            }
        }
        return doctorTreatments;
    }

    // READ - Find treatments by status
    public DoublyLinkedList<Treatment> findTreatmentsByStatus(String status) {
        treatments = treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> statusTreatments = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getStatus().equalsIgnoreCase(status)) {
                statusTreatments.add(treatment);
            }
        }
        return statusTreatments;
    }

    // UPDATE - Update treatment status
    public boolean updateTreatmentStatus(String treatmentId, String newStatus) {
        treatments = treatmentDAO.retrieveFromFile();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                treatment.setStatus(newStatus);
                treatmentDAO.saveToFile(treatments);
                return true;
            }
        }
        return false;
    }

    // UPDATE - Update treatment date
    public boolean updateTreatmentDate(String treatmentId, LocalDate newDate) {
        treatments = treatmentDAO.retrieveFromFile();

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
        treatments = treatmentDAO.retrieveFromFile();

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

    // UPDATE - Update entire medicine list for treatment
    public boolean updateTreatmentMedicines(String treatmentId, DoublyLinkedList<TreatmentMedicineList> newMedicines) {
        treatments = treatmentDAO.retrieveFromFile();

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

    // DELETE - Delete treatment by ID
    public boolean deleteTreatmentById(String treatmentId) {
        treatments = treatmentDAO.retrieveFromFile();

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

    // DELETE - Delete all treatments for a specific patient
    public int deleteTreatmentsByPatientId(String patientId) {
        treatments = treatmentDAO.retrieveFromFile();
        int deletedCount = 0;

        // Iterate backwards to avoid index issues when removing items
        for (int i = treatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(patientId)) {
                treatments.remove(i);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            treatmentDAO.saveToFile(treatments);
        }

        return deletedCount;
    }

    // DELETE - Delete all treatments for a specific doctor
    public int deleteTreatmentsByDoctorId(String doctorId) {
        treatments = treatmentDAO.retrieveFromFile();
        int deletedCount = 0;

        // Iterate backwards to avoid index issues when removing items
        for (int i = treatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getDoctorId().equalsIgnoreCase(doctorId)) {
                treatments.remove(i);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            treatmentDAO.saveToFile(treatments);
        }

        return deletedCount;
    }

    // DELETE - Delete all treatments
    public boolean deleteAllTreatments() {
        treatments = new DoublyLinkedList<>();
        treatmentDAO.saveToFile(treatments);
        return true;
    }

    // UTILITY - Check if patient has diagnosis (validation method)
    public boolean patientHasDiagnosis(String patientId) {
        return !maintainDiagnosis.findDiagnosesByPatientId(patientId).isEmpty();
    }

    // UTILITY - Check if treatment exists
    public boolean treatmentExists(String treatmentId) {
        return findTreatmentById(treatmentId) != null;
    }

    // UTILITY - Get count of treatments
    public int getTreatmentCount() {
        treatments = treatmentDAO.retrieveFromFile();
        return treatments.getNumberOfEntries();
    }

    // UTILITY - Get treatment report/statistics
    public String getTreatmentReport() {
        treatments = treatmentDAO.retrieveFromFile();

        if (treatments.isEmpty()) {
            return "No treatments found in the system.";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== TREATMENT SYSTEM REPORT ===\n");
        report.append("Total Treatments: ").append(treatments.getNumberOfEntries()).append("\n\n");

        // Count treatments by status
        DoublyLinkedList<String> uniquePatients = new DoublyLinkedList<>();
        DoublyLinkedList<String> uniqueDoctors = new DoublyLinkedList<>();
        int completedTreatments = 0;
        int ongoingTreatments = 0;

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);

            // Track unique patients
            if (!uniquePatients.contains(treatment.getPatientId())) {
                uniquePatients.add(treatment.getPatientId());
            }

            // Track unique doctors
            if (!uniqueDoctors.contains(treatment.getDoctorId())) {
                uniqueDoctors.add(treatment.getDoctorId());
            }

            // Count by status
            if (treatment.getStatus().equalsIgnoreCase("completed")) {
                completedTreatments++;
            } else if (treatment.getStatus().equalsIgnoreCase("ongoing")) {
                ongoingTreatments++;
            }
        }

        report.append("Unique Patients with Treatments: ").append(uniquePatients.getNumberOfEntries()).append("\n");
        report.append("Unique Doctors providing Treatments: ").append(uniqueDoctors.getNumberOfEntries()).append("\n");
        report.append("Completed Treatments: ").append(completedTreatments).append("\n");
        report.append("Ongoing Treatments: ").append(ongoingTreatments).append("\n");

        return report.toString();
    }

    // Getters for static fields (if needed)
    public static Patient getServingPatient() {
        return servingPatient;
    }

    public static void setServingPatient(Patient patient) {
        servingPatient = patient;
    }

    public static Doctor getDoctorConsulting() {
        return doctorConsulting;
    }

    public static void setDoctorConsulting(Doctor doctor) {
        doctorConsulting = doctor;
    }

    public TreatmentUI getTreatmentUI() {
        return treatmentUI;
    }
}
