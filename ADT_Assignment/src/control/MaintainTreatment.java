// Lee Siu Yee

package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import adt.LinkedQueue;
import adt.QueueInterface;
import adt.HashMap;
import adt.MapInterface;
import dao.TreatmentDAO;
import entity.Doctor;
import entity.Patient;
import entity.Treatment;
import entity.TreatmentMedicineList;
import entity.Diagnosis;
import entity.ServingStatusData;
import entity.TreatmentReportData;
import entity.DiagnosisDistribution;
import entity.MedicineDistribution;
import java.time.LocalDate;
import java.util.Comparator;

public class MaintainTreatment {

    private ListInterface<Treatment> treatments;
    private final TreatmentDAO treatmentDAO;
    private final MaintainDiagnosis maintainDiagnosis;

    // Enhanced Queue Management using ADT
    private static QueueInterface<Patient> servingPatientsQueue = new LinkedQueue<>();
    private static QueueInterface<Doctor> servingDoctorsQueue = new LinkedQueue<>();
    private static Patient currentServingPatient = null;
    private static Doctor currentConsultingDoctor = null;

    // Cached ADT options for performance optimization
    private ListInterface<String> statusOptions;
    private ListInterface<String> mealTimingOptions;
    private ListInterface<String> availableMedicines;

    public MaintainTreatment() {
        this.treatmentDAO = new TreatmentDAO();
        this.treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        this.maintainDiagnosis = new MaintainDiagnosis();
        initializeCachedOptions();
        initializeTreatmentCounter();
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
            // Silently handle - UI will manage error display
        }
    }

    // Getters for cached ADT options
    public ListInterface<String> getStatusOptions() {
        return statusOptions;
    }

    public ListInterface<String> getMealTimingOptions() {
        return mealTimingOptions;
    }

    public ListInterface<String> getAvailableMedicines() {
        return availableMedicines;
    }

    private void initializeTreatmentCounter() {
        if (!treatments.isEmpty()) {
            int maxCounter = 0;
            for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
                String treatmentId = treatments.getEntry(i).getTreatmentId();
                int idNumber = Integer.parseInt(treatmentId.substring(1));
                maxCounter = Math.max(maxCounter, idNumber);
            }
            setTreatmentCounter(maxCounter + 1);
        } else {
            setTreatmentCounter(1); // Reset to 1 if no treatments exist
        }
    }

    private void setTreatmentCounter(int newCounter) {
        try {
            java.lang.reflect.Field counterField = entity.Treatment.class.getDeclaredField("counter");
            counterField.setAccessible(true);
            counterField.setInt(null, newCounter);
        } catch (Exception e) {
            // Handle silently - counter will start from 1
        }
    }

    // ================================
    // SEARCH FUNCTIONS USING LINEAR SEARCH ALGORITHM
    // ================================
    
    /**
     * Get all unique medicines used in treatments
     * Creates a list of medicines for user selection
     * 
     * @return list of unique medicine names used in treatments
     */
    public ListInterface<String> getAllMedicinesUsedInTreatments() {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        ListInterface<String> uniqueMedicines = new DoublyLinkedList<>();
        
        // Linear search through all treatments to collect medicines
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment currentTreatment = treatments.getEntry(i);
            ListInterface<TreatmentMedicineList> medicines = currentTreatment.getMedicineList();
            
            if (medicines != null && !medicines.isEmpty()) {
                // Check each medicine in this treatment
                for (int j = 1; j <= medicines.getNumberOfEntries(); j++) {
                    String medicineName = medicines.getEntry(j).getPrescription();
                    
                    // Use linear search to check if medicine already exists in our list
                    boolean alreadyExists = false;
                    for (int k = 1; k <= uniqueMedicines.getNumberOfEntries(); k++) {
                        if (uniqueMedicines.getEntry(k).equalsIgnoreCase(medicineName)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    
                    // Add medicine if not already in list
                    if (!alreadyExists) {
                        uniqueMedicines.add(medicineName);
                    }
                }
            }
        }
        
        return uniqueMedicines;
    }
    
    public ListInterface<Treatment> searchTreatmentsBySelectedMedicine(String medicineName) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        ListInterface<Treatment> foundTreatments = new DoublyLinkedList<>();
        
        // Linear search through all treatments
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment currentTreatment = treatments.getEntry(i);
            
            // Check if this treatment has the medicine we're looking for
            if (treatmentHasMedicine(currentTreatment, medicineName)) {
                foundTreatments.add(currentTreatment);
            }
        }
        
        return foundTreatments;
    }
    
    /**
     * Helper method to check if treatment contains specific medicine
     * Uses linear search pattern similar to ADT contains method
     */
    private boolean treatmentHasMedicine(Treatment treatment, String medicineName) {
        ListInterface<TreatmentMedicineList> medicines = treatment.getMedicineList();
        
        if (medicines == null || medicines.isEmpty()) {
            return false;
        }
        
        // Linear search through medicine list
        for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
            TreatmentMedicineList medicine = medicines.getEntry(i);
            if (medicine.getPrescription().equalsIgnoreCase(medicineName)) {
                return true;
            }
        }
        return false;
    }
    
    public ListInterface<Treatment> searchTreatmentById(String treatmentId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        ListInterface<Treatment> foundTreatments = new DoublyLinkedList<>();
        
        // Linear search through all treatments
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment currentTreatment = treatments.getEntry(i);
            
            // Check if treatment ID matches
            if (currentTreatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                foundTreatments.add(currentTreatment);
                break; // Found the treatment, no need to continue searching
            }
        }
        
        return foundTreatments;
    }
    
    public ListInterface<Treatment> searchPatientTreatmentHistory(String patientId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        ListInterface<Treatment> foundTreatments = new DoublyLinkedList<>();
        
        // Linear search through all treatments
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment currentTreatment = treatments.getEntry(i);
            
            // Check if treatment belongs to the specified patient
            if (currentTreatment.getPatientId().equalsIgnoreCase(patientId)) {
                foundTreatments.add(currentTreatment);
            }
        }
        
        // Use reverse() method to show most recent treatments first
        // This makes it easier to see recent treatment history
        foundTreatments.reverse();
        
        return foundTreatments;
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

    // Return data for UI to display serving status
    public static ServingStatusData getServingStatusData() {
        return new ServingStatusData(
                currentServingPatient,
                currentConsultingDoctor,
                servingPatientsQueue.size() > 1 ? (servingPatientsQueue.size() - 1) : 0
        );
    }

    /**
     * Generate Treatment Summary Report Data using mergeSort for sorting
     * Collects and processes all treatment data for comprehensive reporting
     * 
     * @return TreatmentReportData containing all necessary report information
     */
    public TreatmentReportData generateTreatmentSummaryReportData() {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        TreatmentReportData reportData = new TreatmentReportData();
        
        // Calculate basic statistics
        calculateBasicStatistics(reportData);
        
        // Generate diagnosis distributions using mergeSort
        ListInterface<DiagnosisDistribution> diagnosisDistributions = generateDiagnosisDistribution();
        reportData.setDiagnosisDistributions(diagnosisDistributions);
        
        // Generate medicine distributions using mergeSort
        ListInterface<MedicineDistribution> medicineDistributions = generateMedicineDistribution();
        reportData.setMedicineDistributions(medicineDistributions);
        
        // Calculate top performers using sorted data
        calculateTopStatistics(reportData, diagnosisDistributions, medicineDistributions);
        
        return reportData;
    }
    
    public TreatmentReportData generateTreatmentDistributionReportData() {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        TreatmentReportData reportData = new TreatmentReportData();
        
        // Calculate treatment type distribution
        calculateTreatmentTypeDistribution(reportData);
        
        // Generate diagnosis distributions using mergeSort
        ListInterface<DiagnosisDistribution> diagnosisDistributions = generateDiagnosisDistribution();
        reportData.setDiagnosisDistributions(diagnosisDistributions);
        
        // Generate medicine distributions using mergeSort  
        ListInterface<MedicineDistribution> medicineDistributions = generateMedicineDistribution();
        reportData.setMedicineDistributions(medicineDistributions);
        
        return reportData;
    }
    
    /**
     * Calculate basic statistics for reports
     */
    private void calculateBasicStatistics(TreatmentReportData reportData) {
        ListInterface<String> uniquePatients = new DoublyLinkedList<>();
        ListInterface<String> uniqueDoctors = new DoublyLinkedList<>();
        int treatmentsWithMedicine = 0;
        int treatmentsWithoutMedicine = 0;
        
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            
            // Count unique patients
            if (!uniquePatients.contains(treatment.getPatientId())) {
                uniquePatients.add(treatment.getPatientId());
            }
            
            // Count unique doctors
            if (!uniqueDoctors.contains(treatment.getDoctorId())) {
                uniqueDoctors.add(treatment.getDoctorId());
            }
            
            // Count treatments with/without medicine
            if (treatment.getMedicineList() != null && !treatment.getMedicineList().isEmpty()) {
                treatmentsWithMedicine++;
            } else {
                treatmentsWithoutMedicine++;
            }
        }
        
        reportData.setTotalPatients(uniquePatients.getNumberOfEntries());
        reportData.setTotalTreatments(treatments.getNumberOfEntries());
        reportData.setTotalDoctors(uniqueDoctors.getNumberOfEntries());
        reportData.setTreatmentsWithMedicine(treatmentsWithMedicine);
        reportData.setTreatmentsWithoutMedicine(treatmentsWithoutMedicine);
    }
    
    /**
     * Calculate treatment type distribution for distribution report
     */
    private void calculateTreatmentTypeDistribution(TreatmentReportData reportData) {
        int treatmentsWithMedicine = 0;
        int treatmentsWithoutMedicine = 0;
        
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            
            if (treatment.getMedicineList() != null && !treatment.getMedicineList().isEmpty()) {
                treatmentsWithMedicine++;
            } else {
                treatmentsWithoutMedicine++;
            }
        }
        
        reportData.setTreatmentsWithMedicine(treatmentsWithMedicine);
        reportData.setTreatmentsWithoutMedicine(treatmentsWithoutMedicine);
    }
    
    /**
     * Generate diagnosis distribution using ADT HashMap counting and mergeSort for sorting
     * Uses ADT HashMap to count occurrences, then mergeSort to sort diagnoses by count (highest first)
     */
    private ListInterface<DiagnosisDistribution> generateDiagnosisDistribution() {
        // Use ADT HashMap for efficient counting
        MapInterface<String, Integer> diagnosisCount = new HashMap<>();
        
        // Count diagnoses from all treatments
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            
            // Get diagnosis for this treatment from MaintainDiagnosis
            ListInterface<Diagnosis> patientDiagnoses = maintainDiagnosis.findDiagnosesByPatientId(treatment.getPatientId());
            
            for (int j = 1; j <= patientDiagnoses.getNumberOfEntries(); j++) {
                Diagnosis diagnosis = patientDiagnoses.getEntry(j);
                if (diagnosis.getDoctorId().equals(treatment.getDoctorId())) {
                    String diagnosisName = diagnosis.getDiagnosisDetails();
                    Integer currentCount = diagnosisCount.get(diagnosisName);
                    if (currentCount == null) {
                        currentCount = 0;
                    }
                    diagnosisCount.put(diagnosisName, currentCount + 1);
                    break;
                }
            }
        }
        
        // Convert to ADT list and calculate percentages
        ListInterface<DiagnosisDistribution> distributions = new DoublyLinkedList<>();
        int totalTreatments = treatments.getNumberOfEntries();
        
        ListInterface<String> diagnosisNames = diagnosisCount.getKeys();
        for (int i = 1; i <= diagnosisNames.getNumberOfEntries(); i++) {
            String diagnosisName = diagnosisNames.getEntry(i);
            Integer count = diagnosisCount.get(diagnosisName);
            double percentage = (count * 100.0) / totalTreatments;
            distributions.add(new DiagnosisDistribution(diagnosisName, count, percentage));
        }
        
        // Sort using ADT mergeSort - highest count first
        distributions.mergeSort(new Comparator<DiagnosisDistribution>() {
            @Override
            public int compare(DiagnosisDistribution d1, DiagnosisDistribution d2) {
                return Integer.compare(d2.getCount(), d1.getCount()); // Descending order
            }
        });
        
        return distributions;
    }
    
    /**
     * Generate medicine distribution using ADT HashMap counting and mergeSort for sorting
     * Uses ADT HashMap to count occurrences, then mergeSort to sort medicines by count (highest first)
     */
    private ListInterface<MedicineDistribution> generateMedicineDistribution() {
        // Use ADT HashMap for efficient counting
        MapInterface<String, Integer> medicineCount = new HashMap<>();
        
        // Count medicines from all treatments
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            ListInterface<TreatmentMedicineList> medicines = treatment.getMedicineList();
            
            if (medicines != null && !medicines.isEmpty()) {
                for (int j = 1; j <= medicines.getNumberOfEntries(); j++) {
                    String medicineName = medicines.getEntry(j).getPrescription();
                    Integer currentCount = medicineCount.get(medicineName);
                    if (currentCount == null) {
                        currentCount = 0;
                    }
                    medicineCount.put(medicineName, currentCount + 1);
                }
            }
        }
        
        // Convert to ADT list and calculate percentages
        ListInterface<MedicineDistribution> distributions = new DoublyLinkedList<>();
        int totalMedicinePrescriptions = 0;
        
        // Calculate total prescriptions using ADT HashMap values
        ListInterface<Integer> counts = medicineCount.getValues();
        for (int i = 1; i <= counts.getNumberOfEntries(); i++) {
            totalMedicinePrescriptions += counts.getEntry(i);
        }
        
        ListInterface<String> medicineNames = medicineCount.getKeys();
        for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
            String medicineName = medicineNames.getEntry(i);
            Integer count = medicineCount.get(medicineName);
            double percentage = (count * 100.0) / totalMedicinePrescriptions;
            distributions.add(new MedicineDistribution(medicineName, count, percentage));
        }
        
        // Sort using ADT mergeSort - highest count first
        distributions.mergeSort(new Comparator<MedicineDistribution>() {
            @Override
            public int compare(MedicineDistribution m1, MedicineDistribution m2) {
                return Integer.compare(m2.getCount(), m1.getCount()); // Descending order
            }
        });
        
        return distributions;
    }
    
    /**
     * Calculate top statistics from sorted distribution data
     * Uses the already sorted lists to get top performers efficiently
     */
    private void calculateTopStatistics(TreatmentReportData reportData, 
                                       ListInterface<DiagnosisDistribution> diagnosisDistributions,
                                       ListInterface<MedicineDistribution> medicineDistributions) {
        
        // Calculate top doctor using ADT HashMap and sorting
        MapInterface<String, Integer> doctorTreatmentCount = new HashMap<>();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            String doctorId = treatment.getDoctorId();
            Integer currentCount = doctorTreatmentCount.get(doctorId);
            if (currentCount == null) {
                currentCount = 0;
            }
            doctorTreatmentCount.put(doctorId, currentCount + 1);
        }
        
        // Find top doctor
        String topDoctor = "";
        int maxDoctorTreatments = 0;
        ListInterface<String> doctorIds = doctorTreatmentCount.getKeys();
        for (int i = 1; i <= doctorIds.getNumberOfEntries(); i++) {
            String doctorId = doctorIds.getEntry(i);
            Integer count = doctorTreatmentCount.get(doctorId);
            if (count > maxDoctorTreatments) {
                maxDoctorTreatments = count;
                topDoctor = doctorId;
            }
        }
        
        // Calculate top patient using ADT HashMap
        MapInterface<String, Integer> patientTreatmentCount = new HashMap<>();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            String patientId = treatment.getPatientId();
            Integer currentCount = patientTreatmentCount.get(patientId);
            if (currentCount == null) {
                currentCount = 0;
            }
            patientTreatmentCount.put(patientId, currentCount + 1);
        }
        
        // Find top patient
        String topPatient = "";
        int maxPatientTreatments = 0;
        ListInterface<String> patientIds = patientTreatmentCount.getKeys();
        for (int i = 1; i <= patientIds.getNumberOfEntries(); i++) {
            String patientId = patientIds.getEntry(i);
            Integer count = patientTreatmentCount.get(patientId);
            if (count > maxPatientTreatments) {
                maxPatientTreatments = count;
                topPatient = patientId;
            }
        }
        
        // Set top statistics using sorted data (first item is highest due to descending sort)
        reportData.setTopDoctor(topDoctor);
        reportData.setTopDoctorTreatments(maxDoctorTreatments);
        reportData.setTopPatient(topPatient);
        reportData.setTopPatientTreatments(maxPatientTreatments);
        
        // Get top diagnosis from sorted list
        if (!diagnosisDistributions.isEmpty()) {
            DiagnosisDistribution topDiagnosisData = diagnosisDistributions.getEntry(1);
            reportData.setTopDiagnosis(topDiagnosisData.getDiagnosisName());
            reportData.setTopDiagnosisCount(topDiagnosisData.getCount());
        }
        
        // Get top medicine from sorted list
        if (!medicineDistributions.isEmpty()) {
            MedicineDistribution topMedicineData = medicineDistributions.getEntry(1);
            reportData.setTopMedicine(topMedicineData.getMedicineName());
            reportData.setTopMedicineCount(topMedicineData.getCount());
        }
    }
    
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
            LocalDate treatmentDate, ListInterface<TreatmentMedicineList> medicines) {
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
        ListInterface<Diagnosis> patientDoctorDiagnoses = getPatientDoctorDiagnoses(patientId, doctorId);
        if (patientDoctorDiagnoses.isEmpty()) {
            return false;
        }

        ListInterface<Treatment> existingTreatments = findTreatmentsByPatientAndDoctor(patientId, doctorId);
        if (existingTreatments.isEmpty()) {
            return true;
        }

        return patientDoctorDiagnoses.getNumberOfEntries() > existingTreatments.getNumberOfEntries();
    }

    // Get diagnoses using ADT filtering
    private ListInterface<Diagnosis> getPatientDoctorDiagnoses(String patientId, String doctorId) {
        ListInterface<Diagnosis> allDiagnoses = maintainDiagnosis.getAllDiagnoses();
        ListInterface<Diagnosis> filtered = new DoublyLinkedList<>();

        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            if (diagnosis.getPatientId().equalsIgnoreCase(patientId)
                    && diagnosis.getDoctorId().equalsIgnoreCase(doctorId)) {
                filtered.add(diagnosis);
            }
        }
        return filtered;
    }

    // READ - Get all treatments
    public ListInterface<Treatment> getAllTreatments() {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return treatments;
    }

    // READ - Find by ID using ADT search
    public Treatment findTreatmentById(String treatmentId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getTreatmentId().equalsIgnoreCase(treatmentId)) {
                return treatment;
            }
        }
        return null;
    }

    // READ - Find by Patient ID using ADT filtering
    public ListInterface<Treatment> findTreatmentsByPatientId(String patientId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(patientId, "patient");
    }

    // READ - Find by Doctor ID using ADT filtering
    public ListInterface<Treatment> findTreatmentsByDoctorId(String doctorId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(doctorId, "doctor");
    }

    // READ - Find by Status using ADT filtering
    public ListInterface<Treatment> findTreatmentsByStatus(String status) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return filterTreatments(status, "status");
    }

    // READ - Find by both Patient and Doctor using ADT filtering
    public ListInterface<Treatment> findTreatmentsByPatientAndDoctor(String patientId, String doctorId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        ListInterface<Treatment> result = new DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            Treatment treatment = treatments.getEntry(i);
            if (treatment.getPatientId().equalsIgnoreCase(patientId)
                    && treatment.getDoctorId().equalsIgnoreCase(doctorId)) {
                result.add(treatment);
            }
        }
        return result;
    }

    // Helper method for filtering treatments using ADT
    private ListInterface<Treatment> filterTreatments(String value, String field) {
        ListInterface<Treatment> filtered = new DoublyLinkedList<>();

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
            case "patient":
                return treatment.getPatientId();
            case "doctor":
                return treatment.getDoctorId();
            case "status":
                return treatment.getStatus();
            default:
                return "";
        }
    }

    // UPDATE - Update treatment status
    public boolean updateTreatmentStatus(String treatmentId, String newStatus) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return updateTreatmentField(treatmentId, newStatus, "status");
    }

    // UPDATE - Update treatment date
    public boolean updateTreatmentDate(String treatmentId, LocalDate newDate) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
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
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
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
    public boolean updateTreatmentMedicines(String treatmentId, ListInterface<TreatmentMedicineList> newMedicines) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
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
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
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
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return deleteMultipleTreatments(patientId, "patient");
    }

    public int deleteTreatmentsByDoctorId(String doctorId) {
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
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
        treatments = (ListInterface<Treatment>) treatmentDAO.retrieveFromFile();
        return treatments.getNumberOfEntries();
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