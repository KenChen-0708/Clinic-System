package control;

import adt.DoublyLinkedList;
import dao.DiagnosisDAO;
import entity.Diagnosis;
import entity.TreatmentMedicineList;
import boundary.PharmacyUI;
import adt.ListInterface;

public class MaintainDiagnosis {

    private DoublyLinkedList<Diagnosis> diagnosisList = new DoublyLinkedList<>();
    private final DiagnosisDAO diagnosisDao = new DiagnosisDAO();
    
    // ADT for caching frequently used data
    private DoublyLinkedList<String> statusOptions;
    private DoublyLinkedList<String> mealTimingOptions;
    private DoublyLinkedList<String> availableMedicines;
    
    public MaintainDiagnosis() {
        initializeOptions();
    }

    // Initialize common options using ADT for better performance
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
            PharmacyUI pharmacyUI = new PharmacyUI();
            ListInterface<String> medicineNames = pharmacyUI.getMedicineNames();

            // Copy to our cached list
            for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
                availableMedicines.add(medicineNames.getEntry(i));
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load medicines from pharmacy.");
        }
    }
    
    // CREATE - Add new diagnosis
    public boolean addDiagnosis(Diagnosis newDiagnosis) {
        if (newDiagnosis != null) {
            diagnosisList.add(newDiagnosis);
            diagnosisDao.saveToFile(diagnosisList);
            return true;
        }
        return false;
    }

    // CREATE - Create diagnosis (alternative method for compatibility)
    public boolean createDiagnosis(Diagnosis newDiagnosis) {
        return addDiagnosis(newDiagnosis);
    }

    // READ - Get all diagnoses
    public DoublyLinkedList<Diagnosis> getAllDiagnoses() {
        diagnosisList = (DoublyLinkedList<Diagnosis>) diagnosisDao.retrieveFromFile();
        return diagnosisList;
    }
    
    // READ - Find diagnosis by ID
    public Diagnosis findById(String diagnosisId) {
        refreshData();
        
        for (int i = 1; i <= diagnosisList.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            if (diagnosis.getDiagnosisId().equalsIgnoreCase(diagnosisId)) {
                return diagnosis;
            }
        }
        return null;
    }

    // READ - Find diagnosis by ID (alternative method name for compatibility)
    public Diagnosis findDiagnosisById(String diagnosisId) {
        return findById(diagnosisId);
    }
    
    // READ - Find diagnoses by Patient ID
    public DoublyLinkedList<Diagnosis> findByPatientId(String patientId) {
        refreshData();
        return filterByField(patientId, "patient");
    }

    // READ - Find diagnoses by Patient ID (alternative method name for compatibility)
    public DoublyLinkedList<Diagnosis> findDiagnosesByPatientId(String patientId) {
        return findByPatientId(patientId);
    }
    
    // READ - Find diagnoses by Doctor ID
    public DoublyLinkedList<Diagnosis> findByDoctorId(String doctorId) {
        refreshData();
        return filterByField(doctorId, "doctor");
    }
    
    // UPDATE - Update diagnosis
    public boolean updateDiagnosis(String diagnosisId, DoublyLinkedList<String> newSymptoms, String newDetails) {
        refreshData();
        
        for (int i = 1; i <= diagnosisList.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            if (diagnosis.getDiagnosisId().equalsIgnoreCase(diagnosisId)) {
                if (newSymptoms != null) {
                    diagnosis.setSymptoms(newSymptoms);
                }
                if (newDetails != null) {
                    diagnosis.setDiagnosisDetails(newDetails);
                }
                diagnosisDao.saveToFile(diagnosisList);
                return true;
            }
        }
        return false;
    }
    
    // DELETE - Delete specific diagnosis
    public boolean deleteById(String diagnosisId) {
        refreshData();
        return removeByCondition(diagnosisId, "id");
    }

    // DELETE - Delete specific diagnosis (alternative method name for compatibility)
    public boolean deleteDiagnosisById(String diagnosisId) {
        return deleteById(diagnosisId);
    }
    
    // DELETE - Delete by patient ID
    public int deleteByPatientId(String patientId) {
        refreshData();
        return removeMultipleByCondition(patientId, "patient");
    }

    // DELETE - Delete by patient ID (alternative method name for compatibility)
    public int deleteDiagnosesByPatientId(String patientId) {
        return deleteByPatientId(patientId);
    }
    
    // DELETE - Delete by doctor ID
    public int deleteByDoctorId(String doctorId) {
        refreshData();
        return removeMultipleByCondition(doctorId, "doctor");
    }
    
    // DELETE - Delete all diagnoses
    public boolean deleteAll() {
        diagnosisList = new DoublyLinkedList<>();
        diagnosisDao.saveToFile(diagnosisList);
        return true;
    }

    // DELETE - Delete all diagnoses (alternative method name for compatibility)
    public boolean deleteAllDiagnoses() {
        return deleteAll();
    }
    
    // UTILITY - Check if diagnosis exists
    public boolean exists(String diagnosisId) {
        return findById(diagnosisId) != null;
    }
    
    // UTILITY - Get count
    public int getCount() {
        refreshData();
        return diagnosisList.getNumberOfEntries();
    }
    
    // UTILITY - Get report
    public String getReport() {
        refreshData();
        
        if (diagnosisList.isEmpty()) {
            return "No diagnoses found in the system.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== DIAGNOSIS SYSTEM REPORT ===\n");
        report.append("Total Diagnoses: ").append(diagnosisList.getNumberOfEntries()).append("\n\n");
        
        DoublyLinkedList<String> uniquePatients = getUniqueValues("patient");
        DoublyLinkedList<String> uniqueDoctors = getUniqueValues("doctor");
        
        report.append("Unique Patients: ").append(uniquePatients.getNumberOfEntries()).append("\n");
        report.append("Unique Doctors: ").append(uniqueDoctors.getNumberOfEntries()).append("\n");
        
        return report.toString();
    }
    
    // MEDICINE MANAGEMENT - Get cached options
    public DoublyLinkedList<String> getStatusOptions() {
        return statusOptions;
    }
    
    public DoublyLinkedList<String> getMealTimingOptions() {
        return mealTimingOptions;
    }
    
    public DoublyLinkedList<String> getAvailableMedicines() {
        return availableMedicines;
    }
    
    public void refreshMedicineCache() {
        loadAvailableMedicines();
    }
    
    // MEDICINE MANAGEMENT - Create medicine list
    public DoublyLinkedList<TreatmentMedicineList> createMedicineList(
            DoublyLinkedList<String> prescriptions,
            DoublyLinkedList<Integer> tablets,
            DoublyLinkedList<String> mealTimings,
            DoublyLinkedList<Integer> timesPerDay,
            DoublyLinkedList<Integer> totalDays) {
        
        DoublyLinkedList<TreatmentMedicineList> medicines = new DoublyLinkedList<>();
        
        // Validate input lists have same size
        int size = prescriptions.getNumberOfEntries();
        if (tablets.getNumberOfEntries() != size || 
            mealTimings.getNumberOfEntries() != size ||
            timesPerDay.getNumberOfEntries() != size ||
            totalDays.getNumberOfEntries() != size) {
            return medicines; // Return empty list if sizes don't match
        }
        
        // Create medicine objects
        for (int i = 1; i <= size; i++) {
            TreatmentMedicineList medicine = new TreatmentMedicineList(
                "", // Treatment ID will be set later
                prescriptions.getEntry(i),
                tablets.getEntry(i),
                mealTimings.getEntry(i),
                timesPerDay.getEntry(i),
                totalDays.getEntry(i)
            );
            medicines.add(medicine);
        }
        
        return medicines;
    }
    
    // VALIDATION METHODS - Data validation logic
    public boolean isValidMedicineChoice(int choice) {
        return choice >= 1 && choice <= availableMedicines.getNumberOfEntries();
    }
    
    public String getMedicineByChoice(int choice) {
        if (isValidMedicineChoice(choice)) {
            return availableMedicines.getEntry(choice);
        }
        return null;
    }
    
    public boolean isValidStatusChoice(int choice) {
        return choice >= 1 && choice <= statusOptions.getNumberOfEntries();
    }
    
    public String getStatusByChoice(int choice) {
        if (isValidStatusChoice(choice)) {
            return statusOptions.getEntry(choice);
        }
        return null;
    }
    
    public boolean isValidMealTimingChoice(int choice) {
        return choice >= 1 && choice <= mealTimingOptions.getNumberOfEntries();
    }
    
    public String getMealTimingByChoice(int choice) {
        if (isValidMealTimingChoice(choice)) {
            return mealTimingOptions.getEntry(choice);
        }
        return null;
    }
    
    // DATA PROCESSING - Business logic for data processing
    public DoublyLinkedList<String> processSymptoms(String symptomInput) {
        DoublyLinkedList<String> symptoms = new DoublyLinkedList<>();
        if (symptomInput != null && !symptomInput.trim().isEmpty()) {
            symptoms.add(symptomInput.trim());
        }
        return symptoms;
    }
    
    public boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    // PRIVATE HELPER METHODS - Eliminate code duplication
    private void refreshData() {
        diagnosisList = (DoublyLinkedList<Diagnosis>) diagnosisDao.retrieveFromFile();
    }
    
    private DoublyLinkedList<Diagnosis> filterByField(String value, String fieldType) {
        DoublyLinkedList<Diagnosis> filteredList = new DoublyLinkedList<>();
        
        for (int i = 1; i <= diagnosisList.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            String compareValue = getFieldValue(diagnosis, fieldType);
            
            if (compareValue.equalsIgnoreCase(value)) {
                filteredList.add(diagnosis);
            }
        }
        return filteredList;
    }
    
    private boolean removeByCondition(String value, String fieldType) {
        for (int i = 1; i <= diagnosisList.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            String compareValue = getFieldValue(diagnosis, fieldType);
            
            if (compareValue.equalsIgnoreCase(value)) {
                diagnosisList.remove(i);
                diagnosisDao.saveToFile(diagnosisList);
                return true;
            }
        }
        return false;
    }
    
    private int removeMultipleByCondition(String value, String fieldType) {
        int deletedCount = 0;
        
        for (int i = diagnosisList.getNumberOfEntries(); i >= 1; i--) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            String compareValue = getFieldValue(diagnosis, fieldType);
            
            if (compareValue.equalsIgnoreCase(value)) {
                diagnosisList.remove(i);
                deletedCount++;
            }
        }
        
        if (deletedCount > 0) {
            diagnosisDao.saveToFile(diagnosisList);
        }
        return deletedCount;
    }
    
    private DoublyLinkedList<String> getUniqueValues(String fieldType) {
        DoublyLinkedList<String> uniqueValues = new DoublyLinkedList<>();
        
        for (int i = 1; i <= diagnosisList.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = diagnosisList.getEntry(i);
            String value = getFieldValue(diagnosis, fieldType);
            
            if (!uniqueValues.contains(value)) {
                uniqueValues.add(value);
            }
        }
        return uniqueValues;
    }
    
    private String getFieldValue(Diagnosis diagnosis, String fieldType) {
        switch (fieldType) {
            case "id":
                return diagnosis.getDiagnosisId();
            case "patient":
                return diagnosis.getPatientId();
            case "doctor":
                return diagnosis.getDoctorId();
            default:
                return "";
        }
    }
}