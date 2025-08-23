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

    private ListInterface<Treatment> treatments;
    private final TreatmentDAO treatmentDAO;
    private final MaintainDiagnosis maintainDiagnosis;
    
    // ================================
    // CORRECTED SERVING QUEUE MANAGEMENT WITH SEQUENCE TRACKING
    // ================================
    private static QueueInterface<Patient> servingPatientsQueue = new LinkedQueue<>();
    private static QueueInterface<Doctor> servingDoctorsQueue = new LinkedQueue<>();

    // Track current serving pair separately from the queue
    private static Patient currentServingPatient = null;
    private static Doctor currentConsultingDoctor = null;

    // ADT for caching frequently used data (moved from TreatmentUI)
    private DoublyLinkedList<String> statusOptions;
    private DoublyLinkedList<String> mealTimingOptions;
    private DoublyLinkedList<String> availableMedicines;

    public MaintainTreatment() {
        this.treatmentDAO = new TreatmentDAO();
        this.treatments = treatmentDAO.retrieveFromFile();
        this.maintainDiagnosis = new MaintainDiagnosis();
        initializeOptions();
    }

    // Initialize common options using ADT for better performance (moved from TreatmentUI)
    private void initializeOptions() {
        // Status options
        statusOptions = new DoublyLinkedList<>();
        statusOptions.add("Pending");
        statusOptions.add("Completed");

        // Meal timing options
        mealTimingOptions = new DoublyLinkedList<>();
        mealTimingOptions.add("Before meal");
        mealTimingOptions.add("After meal");

        // Load available medicines (cache for better performance)
        loadAvailableMedicines();
    }

    private void loadAvailableMedicines() {
        availableMedicines = new DoublyLinkedList<>();
        try {
            boundary.PharmacyUI pharmacyUI = new boundary.PharmacyUI();
            ListInterface<String> medicineNames = pharmacyUI.getMedicineNames();

            // Copy to our cached list
            for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
                availableMedicines.add(medicineNames.getEntry(i));
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load medicines from pharmacy.");
        }
    }

    // Getters for ADT options (for UI to access)
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
    // MAIN SERVING METHODS - MAINTAINS PROPER SEQUENCE
    // ================================

    /**
     * Get the current patient being served (the one who should get the next diagnosis)
     * This maintains sequence by tracking the current serving pair separately
     */
    public static Patient getServingPatient() {
        // If no current patient is being served, get the next one from queue
        if (currentServingPatient == null && !servingPatientsQueue.isEmpty()) {
            currentServingPatient = servingPatientsQueue.peek();
        }
        return currentServingPatient;
    }

    /**
     * Get the current doctor doing consultation (paired with current serving patient)
     * This maintains sequence by tracking the current consulting pair separately
     */
    public static Doctor getDoctorConsulting() {
        // If no current doctor is consulting, get the next one from queue
        if (currentConsultingDoctor == null && !servingDoctorsQueue.isEmpty()) {
            currentConsultingDoctor = servingDoctorsQueue.peek();
        }
        return currentConsultingDoctor;
    }

    /**
     * Add patient and doctor pair to serving queues
     * This is called when a patient is served from the main queue
     */
    public static void addServingPatientDoctorPair(Patient patient, Doctor doctor) {
        if (patient != null && doctor != null) {
            servingPatientsQueue.enqueue(patient);
            servingDoctorsQueue.enqueue(doctor);
            
            // If this is the first pair, set them as current serving pair
            if (currentServingPatient == null && currentConsultingDoctor == null) {
                currentServingPatient = patient;
                currentConsultingDoctor = doctor;
            }
            
            System.out.println("Added to serving queue: Patient " + patient.getId() + 
                              " with Doctor " + doctor.getId());
        }
    }

    /**
     * Complete current consultation and move to next patient-doctor pair
     * This is called after successful diagnosis and treatment creation
     */
    public static void removeCurrentServingPair() {
        if (!servingPatientsQueue.isEmpty() && !servingDoctorsQueue.isEmpty()) {
            // Remove the completed pair from the queue
            Patient completedPatient = servingPatientsQueue.dequeue();
            Doctor completedDoctor = servingDoctorsQueue.dequeue();
            
            System.out.println("Completed consultation: Patient " + completedPatient.getId() + 
                              " with Doctor " + completedDoctor.getId());
            
            // Move to next patient-doctor pair
            if (!servingPatientsQueue.isEmpty() && !servingDoctorsQueue.isEmpty()) {
                currentServingPatient = servingPatientsQueue.peek();
                currentConsultingDoctor = servingDoctorsQueue.peek();
                
                System.out.println("Moving to next pair: Patient " + currentServingPatient.getId() + 
                                  " with Doctor " + currentConsultingDoctor.getId());
            } else {
                // No more patients in serving queue
                currentServingPatient = null;
                currentConsultingDoctor = null;
                System.out.println("All consultations completed. Serving queue is empty.");
            }
        }
    }

    /**
     * Check if there are patients waiting for diagnosis
     */
    public static boolean hasServingPatients() {
        return !servingPatientsQueue.isEmpty();
    }

    /**
     * Get number of patients currently in serving queue
     */
    public static int getServingPatientsCount() {
        return servingPatientsQueue.size();
    }

    /**
     * Clear all serving queues (for emergency or reset)
     */
    public static void clearAllServingQueues() {
        servingPatientsQueue.clear();
        servingDoctorsQueue.clear();
        currentServingPatient = null;
        currentConsultingDoctor = null;
        System.out.println("All serving queues cleared.");
    }

    /**
     * Display current serving status and queue
     */
    public static void displayServingStatus() {
        System.out.println("\n=== CURRENT SERVING STATUS ===");
        
        if (currentServingPatient != null && currentConsultingDoctor != null) {
            System.out.println("Currently serving:");
            System.out.println("  Patient: " + currentServingPatient.getId() + " (" + currentServingPatient.getName() + ")");
            System.out.println("  Doctor: " + currentConsultingDoctor.getId() + " (" + currentConsultingDoctor.getName() + ")");
        } else {
            System.out.println("No active consultation at the moment.");
        }
        
        if (servingPatientsQueue.size() > 1) {
            System.out.println("Waiting in serving queue: " + (servingPatientsQueue.size() - 1) + " patients");
        } else if (servingPatientsQueue.isEmpty()) {
            System.out.println("Serving queue is empty.");
        }
        System.out.println("===============================\n");
    }

    // ================================
    // BACKWARD COMPATIBILITY METHODS (DEPRECATED)
    // ================================

    @Deprecated
    public static void setServingPatient(Patient patient) {
        // This method is kept for backward compatibility but should not be used
        // Use addServingPatientDoctorPair instead
        System.out.println("Warning: setServingPatient is deprecated. Use addServingPatientDoctorPair instead.");
    }

    @Deprecated  
    public static void setDoctorConsulting(Doctor doctor) {
        // This method is kept for backward compatibility but should not be used
        // Use addServingPatientDoctorPair instead
        System.out.println("Warning: setDoctorConsulting is deprecated. Use addServingPatientDoctorPair instead.");
    }

    private void initializeTreatmentCounter() {
        try {
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
                        System.err.println("Skipping invalid treatment ID: " + treatmentId);
                    }
                }
            }

            counterField.setInt(null, maxId + 1);
            System.out.println("Treatment counter initialized to: " + (maxId + 1));

        } catch (Exception e) {
            System.err.println("Error initializing treatment counter: " + e.getMessage());
        }
    }

    // CREATE - Create new treatment with automatic diagnosis validation
    public boolean createTreatment(String patientId, String doctorId, String status, LocalDate treatmentDate) {
        if (hasAvailableDiagnosisForTreatment(patientId, doctorId)) {
            Treatment newTreatment = new Treatment(patientId, doctorId, status, treatmentDate);
            treatments.add(newTreatment);
            treatmentDAO.saveToFile(treatments);
            return true;
        }
        return false;
    }

    // CREATE - Create treatment with medicine list and enhanced validation
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

    // Enhanced diagnosis validation using ADT
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

    // Get diagnoses for specific patient-doctor combination using ADT
    private DoublyLinkedList<Diagnosis> getPatientDoctorDiagnoses(String patientId, String doctorId) {
        DoublyLinkedList<Diagnosis> allDiagnoses = maintainDiagnosis.getAllDiagnoses();
        DoublyLinkedList<Diagnosis> patientDoctorDiagnoses = new DoublyLinkedList<>();
        
        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            if (diagnosis.getPatientId().equalsIgnoreCase(patientId) && 
                diagnosis.getDoctorId().equalsIgnoreCase(doctorId)) {
                patientDoctorDiagnoses.add(diagnosis);
            }
        }
        
        return patientDoctorDiagnoses;
    }

    // Find treatments by both patient and doctor using ADT
    public DoublyLinkedList<Treatment> findTreatmentsByPatientAndDoctor(String patientId, String doctorId) {
        treatments = treatmentDAO.retrieveFromFile();
        DoublyLinkedList<Treatment> patientDoctorTreatments = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(patientId) && 
                treatment.getDoctorId().equalsIgnoreCase(doctorId)) {
                patientDoctorTreatments.add(treatment);
            }
        }
        return patientDoctorTreatments;
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

    // UTILITY - Check if patient has diagnosis available for new treatment
    public boolean patientHasDiagnosisForTreatment(String patientId, String doctorId) {
        return hasAvailableDiagnosisForTreatment(patientId, doctorId);
    }

    // UTILITY - Check if patient has diagnosis (backward compatibility)
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

    // Get count of available diagnoses for treatment creation using ADT
    public int getAvailableDiagnosesCount() {
        DoublyLinkedList<Diagnosis> allDiagnoses = maintainDiagnosis.getAllDiagnoses();
        DoublyLinkedList<Treatment> allTreatments = (DoublyLinkedList<Treatment>) getAllTreatments();
        
        DoublyLinkedList<String> usedCombinations = new DoublyLinkedList<>();
        
        for (int i = 1; i <= allTreatments.getNumberOfEntries(); i++) {
            Treatment treatment = allTreatments.getEntry(i);
            String combination = treatment.getPatientId() + "-" + treatment.getDoctorId();
            if (!usedCombinations.contains(combination)) {
                usedCombinations.add(combination);
            }
        }
        
        int availableCount = 0;
        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            String combination = diagnosis.getPatientId() + "-" + diagnosis.getDoctorId();
            if (!usedCombinations.contains(combination)) {
                availableCount++;
            }
        }
        
        return availableCount;
    }

    // ================================
    // COMPREHENSIVE TREATMENT REPORT USING ADT AND MERGE SORT
    // ================================
    public String getTreatmentReport() {
        treatments = treatmentDAO.retrieveFromFile();

        if (treatments.isEmpty()) {
            return formatEmptyReport();
        }

        // Convert to DoublyLinkedList for ADT operations
        DoublyLinkedList<Treatment> treatmentList = (DoublyLinkedList<Treatment>) treatments;
        
        // Create sorted copy for analysis using merge sort
        DoublyLinkedList<Treatment> sortedByDate = createSortedCopy(treatmentList, 
            (t1, t2) -> t1.getTreatmentDate().compareTo(t2.getTreatmentDate()));
        
        DoublyLinkedList<Treatment> sortedByStatus = createSortedCopy(treatmentList, 
            (t1, t2) -> t1.getStatus().compareToIgnoreCase(t2.getStatus()));

        StringBuilder report = new StringBuilder();
        
        // Header
        report.append(formatReportHeader());
        
        // Basic Statistics Section
        report.append(formatBasicStats(treatmentList));
        
        // Status Analysis Section (using sorted data)
        report.append(formatStatusAnalysis(sortedByStatus));
        
        // Date Analysis Section (using sorted data)  
        report.append(formatDateAnalysis(sortedByDate));
        
        // Patient and Doctor Analysis
        report.append(formatPatientDoctorAnalysis(treatmentList));
        
        // Medicine Analysis
        report.append(formatMedicineAnalysis(treatmentList));
        
        // Footer
        report.append(formatReportFooter());
        
        return report.toString();
    }

    // Helper method to create sorted copy using ADT merge sort
    private DoublyLinkedList<Treatment> createSortedCopy(DoublyLinkedList<Treatment> original, 
                                                          Comparator<Treatment> comparator) {
        DoublyLinkedList<Treatment> copy = new DoublyLinkedList<>();
        
        // Copy all elements
        for (int i = 1; i <= original.getNumberOfEntries(); i++) {
            copy.add(original.getEntry(i));
        }
        
        // Sort using ADT merge sort
        copy.mergeSort(comparator);
        
        return copy;
    }

    private String formatReportHeader() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String currentDate = LocalDate.now().format(dateFormatter);
        
        StringBuilder header = new StringBuilder();
        header.append("================================================================================\n");
        header.append("                        HOSPITAL MANAGEMENT SYSTEM\n");
        header.append("                         TREATMENT ANALYSIS REPORT\n");
        header.append("================================================================================\n");
        header.append("Generated on: ").append(currentDate).append("\n");
        header.append("Report Type : TREATMENT MANAGEMENT ANALYSIS\n");
        header.append("================================================================================\n\n");
        
        return header.toString();
    }

    private String formatBasicStats(DoublyLinkedList<Treatment> treatmentList) {
        StringBuilder stats = new StringBuilder();
        stats.append("BASIC STATISTICS:\n");
        stats.append("================================================================================\n");
        stats.append(String.format("%-40s | %10d\n", "Total Treatments", treatmentList.getNumberOfEntries()));
        stats.append(String.format("%-40s | %10d\n", "Available Diagnoses for Treatment", getAvailableDiagnosesCount()));
        stats.append("--------------------------------------------------------------------------------\n\n");
        
        return stats.toString();
    }

    private String formatStatusAnalysis(DoublyLinkedList<Treatment> sortedByStatus) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("STATUS ANALYSIS (Sorted by Status):\n");
        analysis.append("================================================================================\n");
        
        // Count treatments by status using ADT
        DoublyLinkedList<String> statusList = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> statusCounts = new DoublyLinkedList<>();
        
        for (int i = 1; i <= sortedByStatus.getNumberOfEntries(); i++) {
            Treatment treatment = sortedByStatus.getEntry(i);
            String status = treatment.getStatus().toLowerCase();
            
            // Find existing status or add new one
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
        
        // Display status counts
        for (int i = 1; i <= statusList.getNumberOfEntries(); i++) {
            String status = statusList.getEntry(i);
            int count = statusCounts.getEntry(i);
            double percentage = (count * 100.0) / sortedByStatus.getNumberOfEntries();
            
            analysis.append(String.format("%-20s | %8d | %6.1f%%\n", 
                status.toUpperCase(), count, percentage));
        }
        
        analysis.append("--------------------------------------------------------------------------------\n\n");
        
        return analysis.toString();
    }

    private String formatDateAnalysis(DoublyLinkedList<Treatment> sortedByDate) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("DATE ANALYSIS (Sorted by Treatment Date):\n");
        analysis.append("================================================================================\n");
        
        if (sortedByDate.getNumberOfEntries() > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            Treatment earliest = sortedByDate.getEntry(1);
            Treatment latest = sortedByDate.getEntry(sortedByDate.getNumberOfEntries());
            
            analysis.append(String.format("%-25s | %s\n", "Earliest Treatment Date", 
                earliest.getTreatmentDate().format(formatter)));
            analysis.append(String.format("%-25s | %s\n", "Latest Treatment Date", 
                latest.getTreatmentDate().format(formatter)));
            
            // Count treatments by month using ADT
            DoublyLinkedList<String> months = new DoublyLinkedList<>();
            DoublyLinkedList<Integer> monthlyCounts = new DoublyLinkedList<>();
            
            for (int i = 1; i <= sortedByDate.getNumberOfEntries(); i++) {
                Treatment treatment = sortedByDate.getEntry(i);
                String monthYear = treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("MM/yyyy"));
                
                boolean found = false;
                for (int j = 1; j <= months.getNumberOfEntries(); j++) {
                    if (months.getEntry(j).equals(monthYear)) {
                        monthlyCounts.replace(j, monthlyCounts.getEntry(j) + 1);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    months.add(monthYear);
                    monthlyCounts.add(1);
                }
            }
            
            analysis.append("\nMonthly Distribution:\n");
            for (int i = 1; i <= months.getNumberOfEntries(); i++) {
                analysis.append(String.format("%-10s | %8d treatments\n", 
                    months.getEntry(i), monthlyCounts.getEntry(i)));
            }
        }
        
        analysis.append("--------------------------------------------------------------------------------\n\n");
        
        return analysis.toString();
    }

    private String formatPatientDoctorAnalysis(DoublyLinkedList<Treatment> treatmentList) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("PATIENT & DOCTOR ANALYSIS:\n");
        analysis.append("================================================================================\n");
        
        // Count unique patients and doctors using ADT
        DoublyLinkedList<String> uniquePatients = new DoublyLinkedList<>();
        DoublyLinkedList<String> uniqueDoctors = new DoublyLinkedList<>();
        DoublyLinkedList<String> patientCounts = new DoublyLinkedList<>();
        DoublyLinkedList<String> doctorCounts = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> patientTreatmentCounts = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> doctorTreatmentCounts = new DoublyLinkedList<>();
        
        for (int i = 1; i <= treatmentList.getNumberOfEntries(); i++) {
            Treatment treatment = treatmentList.getEntry(i);
            
            // Count unique patients
            if (!uniquePatients.contains(treatment.getPatientId())) {
                uniquePatients.add(treatment.getPatientId());
                patientCounts.add(treatment.getPatientId());
                patientTreatmentCounts.add(1);
            } else {
                // Find and increment patient count
                for (int j = 1; j <= patientCounts.getNumberOfEntries(); j++) {
                    if (patientCounts.getEntry(j).equals(treatment.getPatientId())) {
                        patientTreatmentCounts.replace(j, patientTreatmentCounts.getEntry(j) + 1);
                        break;
                    }
                }
            }
            
            // Count unique doctors
            if (!uniqueDoctors.contains(treatment.getDoctorId())) {
                uniqueDoctors.add(treatment.getDoctorId());
                doctorCounts.add(treatment.getDoctorId());
                doctorTreatmentCounts.add(1);
            } else {
                // Find and increment doctor count
                for (int j = 1; j <= doctorCounts.getNumberOfEntries(); j++) {
                    if (doctorCounts.getEntry(j).equals(treatment.getDoctorId())) {
                        doctorTreatmentCounts.replace(j, doctorTreatmentCounts.getEntry(j) + 1);
                        break;
                    }
                }
            }
        }
        
        analysis.append(String.format("%-30s | %10d\n", "Unique Patients", uniquePatients.getNumberOfEntries()));
        analysis.append(String.format("%-30s | %10d\n", "Unique Doctors", uniqueDoctors.getNumberOfEntries()));
        
        // Find most active patient and doctor
        if (!patientTreatmentCounts.isEmpty()) {
            int maxPatientTreatments = 0;
            String mostActivePatient = "";
            for (int i = 1; i <= patientTreatmentCounts.getNumberOfEntries(); i++) {
                if (patientTreatmentCounts.getEntry(i) > maxPatientTreatments) {
                    maxPatientTreatments = patientTreatmentCounts.getEntry(i);
                    mostActivePatient = patientCounts.getEntry(i);
                }
            }
            analysis.append(String.format("%-30s | %s (%d treatments)\n", 
                "Most Active Patient", mostActivePatient, maxPatientTreatments));
        }
        
        if (!doctorTreatmentCounts.isEmpty()) {
            int maxDoctorTreatments = 0;
            String mostActiveDoctor = "";
            for (int i = 1; i <= doctorTreatmentCounts.getNumberOfEntries(); i++) {
                if (doctorTreatmentCounts.getEntry(i) > maxDoctorTreatments) {
                    maxDoctorTreatments = doctorTreatmentCounts.getEntry(i);
                    mostActiveDoctor = doctorCounts.getEntry(i);
                }
            }
            analysis.append(String.format("%-30s | %s (%d treatments)\n", 
                "Most Active Doctor", mostActiveDoctor, maxDoctorTreatments));
        }
        
        analysis.append("--------------------------------------------------------------------------------\n\n");
        
        return analysis.toString();
    }

    private String formatMedicineAnalysis(DoublyLinkedList<Treatment> treatmentList) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("MEDICINE ANALYSIS:\n");
        analysis.append("================================================================================\n");
        
        int treatmentsWithMedicines = 0;
        int totalMedicinesPrescribed = 0;
        DoublyLinkedList<String> uniqueMedicines = new DoublyLinkedList<>();
        
        for (int i = 1; i <= treatmentList.getNumberOfEntries(); i++) {
            Treatment treatment = treatmentList.getEntry(i);
            DoublyLinkedList<TreatmentMedicineList> medicines = treatment.getMedicineList();
            
            if (medicines != null && !medicines.isEmpty()) {
                treatmentsWithMedicines++;
                totalMedicinesPrescribed += medicines.getNumberOfEntries();
                
                // Collect unique medicines
                for (int j = 1; j <= medicines.getNumberOfEntries(); j++) {
                    String medicine = medicines.getEntry(j).getPrescription();
                    if (!uniqueMedicines.contains(medicine)) {
                        uniqueMedicines.add(medicine);
                    }
                }
            }
        }
        
        analysis.append(String.format("%-35s | %10d\n", "Treatments with Medicines", treatmentsWithMedicines));
        analysis.append(String.format("%-35s | %10d\n", "Treatments without Medicines", 
            treatmentList.getNumberOfEntries() - treatmentsWithMedicines));
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
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                        HOSPITAL MANAGEMENT SYSTEM\n");
        report.append("                         TREATMENT ANALYSIS REPORT\n");
        report.append("================================================================================\n");
        report.append("No treatments found in the system.\n");
        report.append("Please create treatments before generating reports.\n");
        report.append("================================================================================\n");
        
        return report.toString();
    }

    private String formatReportFooter() {
        StringBuilder footer = new StringBuilder();
        footer.append("================================================================================\n");
        footer.append("                            END OF REPORT\n");
        footer.append("================================================================================\n");
        
        return footer.toString();
    }

    // ================================
    // UI ACCESS METHOD (FOR MAINUI INTEGRATION)
    // ================================
    public void runTreatmentMenu() {
        boundary.TreatmentUI treatmentUI = new boundary.TreatmentUI(this);
        treatmentUI.run();
    }

    public boundary.TreatmentUI getTreatmentUI() {
        return new boundary.TreatmentUI(this);
    }
}