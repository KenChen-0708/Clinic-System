package control;

import adt.DoublyLinkedList;
import dao.DiagnosisDAO;
import entity.Diagnosis;
import entity.TreatmentMedicineList;
import boundary.PharmacyUI;
import adt.ListInterface;

public class MaintainDiagnosis {

    private final DiagnosisDAO diagnosisDao = new DiagnosisDAO();
    
    // ADT-based caching system for performance optimization
    private final DoublyLinkedList<String> statusOptions;
    private final DoublyLinkedList<String> mealTimingOptions;
    private DoublyLinkedList<String> availableMedicines;
    
    public MaintainDiagnosis() {
        // Initialize cached options using DoublyLinkedList ADT
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
            PharmacyUI pharmacyUI = new PharmacyUI();
            ListInterface<String> medicineNames = pharmacyUI.getMedicineNames();
            // Copy using ADT operations
            for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
                availableMedicines.add(medicineNames.getEntry(i));
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load medicines from pharmacy.");
        }
    }
    
    // ===== CRUD OPERATIONS USING ADT =====
    
    // CREATE
    public boolean addDiagnosis(Diagnosis newDiagnosis) {
        if (newDiagnosis != null) {
            DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
            diagnosisList.add(newDiagnosis);
            diagnosisDao.saveToFile(diagnosisList);
            return true;
        }
        return false;
    }

    public boolean createDiagnosis(Diagnosis newDiagnosis) {
        return addDiagnosis(newDiagnosis);
    }

    // READ - Enhanced with ADT operations
    public DoublyLinkedList<Diagnosis> getAllDiagnoses() {
        return (DoublyLinkedList<Diagnosis>) diagnosisDao.retrieveFromFile();
    }
    
    public Diagnosis findById(String diagnosisId) {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        return findInList(diagnosisList, diagnosisId, "id");
    }

    public Diagnosis findDiagnosisById(String diagnosisId) {
        return findById(diagnosisId);
    }
    
    public DoublyLinkedList<Diagnosis> findByPatientId(String patientId) {
        return filterDiagnoses(patientId, "patient");
    }

    public DoublyLinkedList<Diagnosis> findDiagnosesByPatientId(String patientId) {
        return findByPatientId(patientId);
    }
    
    public DoublyLinkedList<Diagnosis> findByDoctorId(String doctorId) {
        return filterDiagnoses(doctorId, "doctor");
    }
    
    // UPDATE - Using ADT search and replace operations
    public boolean updateDiagnosis(String diagnosisId, DoublyLinkedList<String> newSymptoms, String newDetails) {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        
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
    
    // DELETE - Enhanced with ADT remove operations
    public boolean deleteById(String diagnosisId) {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeDiagnosisFromList(diagnosisList, diagnosisId, "id", false);
    }

    public boolean deleteDiagnosisById(String diagnosisId) {
        return deleteById(diagnosisId);
    }
    
    public int deleteByPatientId(String patientId) {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeMultipleDiagnosesFromList(diagnosisList, patientId, "patient");
    }

    public int deleteDiagnosesByPatientId(String patientId) {
        return deleteByPatientId(patientId);
    }
    
    public int deleteByDoctorId(String doctorId) {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeMultipleDiagnosesFromList(diagnosisList, doctorId, "doctor");
    }
    
    public boolean deleteAll() {
        DoublyLinkedList<Diagnosis> emptyList = new DoublyLinkedList<>();
        diagnosisDao.saveToFile(emptyList);
        return true;
    }

    public boolean deleteAllDiagnoses() {
        return deleteAll();
    }
    
    // ===== UTILITY METHODS USING ADT =====
    
    public boolean exists(String diagnosisId) {
        return findById(diagnosisId) != null;
    }
    
    public int getCount() {
        return getAllDiagnoses().getNumberOfEntries();
    }
    
    // Enhanced reporting using ADT operations
    public String getReport() {
        DoublyLinkedList<Diagnosis> diagnosisList = getAllDiagnoses();
        
        if (diagnosisList.isEmpty()) {
            return "No diagnoses found in the system.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== DIAGNOSIS SYSTEM REPORT ===\n");
        report.append("Total Diagnoses: ").append(diagnosisList.getNumberOfEntries()).append("\n\n");
        
        DoublyLinkedList<String> uniquePatients = getUniqueFieldValues(diagnosisList, "patient");
        DoublyLinkedList<String> uniqueDoctors = getUniqueFieldValues(diagnosisList, "doctor");
        
        report.append("Unique Patients: ").append(uniquePatients.getNumberOfEntries()).append("\n");
        report.append("Unique Doctors: ").append(uniqueDoctors.getNumberOfEntries()).append("\n");
        
        return report.toString();
    }
    
    // ===== CACHED OPTIONS ACCESS =====
    
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
    
    // ===== MEDICINE MANAGEMENT USING ADT =====
    
    public DoublyLinkedList<TreatmentMedicineList> createMedicineList(
            DoublyLinkedList<String> prescriptions,
            DoublyLinkedList<Integer> tablets,
            DoublyLinkedList<String> mealTimings,
            DoublyLinkedList<Integer> timesPerDay,
            DoublyLinkedList<Integer> totalDays) {
        
        DoublyLinkedList<TreatmentMedicineList> medicines = new DoublyLinkedList<>();
        
        int size = prescriptions.getNumberOfEntries();
        if (!validateListSizes(tablets, mealTimings, timesPerDay, totalDays, size)) {
            return medicines;
        }
        
        for (int i = 1; i <= size; i++) {
            TreatmentMedicineList medicine = new TreatmentMedicineList(
                "", 
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
    
    // ===== VALIDATION METHODS =====
    
    public boolean isValidMedicineChoice(int choice) {
        return choice >= 1 && choice <= availableMedicines.getNumberOfEntries();
    }
    
    public String getMedicineByChoice(int choice) {
        return isValidMedicineChoice(choice) ? availableMedicines.getEntry(choice) : null;
    }
    
    public boolean isValidStatusChoice(int choice) {
        return choice >= 1 && choice <= statusOptions.getNumberOfEntries();
    }
    
    public String getStatusByChoice(int choice) {
        return isValidStatusChoice(choice) ? statusOptions.getEntry(choice) : null;
    }
    
    public boolean isValidMealTimingChoice(int choice) {
        return choice >= 1 && choice <= mealTimingOptions.getNumberOfEntries();
    }
    
    public String getMealTimingByChoice(int choice) {
        return isValidMealTimingChoice(choice) ? mealTimingOptions.getEntry(choice) : null;
    }
    
    // ===== DATA PROCESSING =====
    
    public DoublyLinkedList<String> processSymptoms(String symptomInput) {
        DoublyLinkedList<String> symptoms = new DoublyLinkedList<>();
        if (isValidInput(symptomInput)) {
            symptoms.add(symptomInput.trim());
        }
        return symptoms;
    }
    
    public boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    // ===== PRIVATE HELPER METHODS USING ADT OPERATIONS =====
    
    private DoublyLinkedList<Diagnosis> filterDiagnoses(String value, String fieldType) {
        DoublyLinkedList<Diagnosis> allDiagnoses = getAllDiagnoses();
        DoublyLinkedList<Diagnosis> filtered = new DoublyLinkedList<>();
        
        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                filtered.add(diagnosis);
            }
        }
        return filtered;
    }
    
    private Diagnosis findInList(DoublyLinkedList<Diagnosis> list, String value, String fieldType) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = list.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                return diagnosis;
            }
        }
        return null;
    }
    
    private boolean removeDiagnosisFromList(DoublyLinkedList<Diagnosis> list, String value, String fieldType, boolean removeAll) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = list.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                list.remove(i);
                diagnosisDao.saveToFile(list);
                return true;
            }
        }
        return false;
    }
    
    private int removeMultipleDiagnosesFromList(DoublyLinkedList<Diagnosis> list, String value, String fieldType) {
        int deletedCount = 0;
        
        // Use reverse iteration to avoid index shifting issues
        for (int i = list.getNumberOfEntries(); i >= 1; i--) {
            Diagnosis diagnosis = list.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                list.remove(i);
                deletedCount++;
            }
        }
        
        if (deletedCount > 0) {
            diagnosisDao.saveToFile(list);
        }
        return deletedCount;
    }
    
    private DoublyLinkedList<String> getUniqueFieldValues(DoublyLinkedList<Diagnosis> list, String fieldType) {
        DoublyLinkedList<String> uniqueValues = new DoublyLinkedList<>();
        
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = list.getEntry(i);
            String value = getFieldValue(diagnosis, fieldType);
            
            if (!uniqueValues.contains(value)) {
                uniqueValues.add(value);
            }
        }
        return uniqueValues;
    }
    
    private String getFieldValue(Diagnosis diagnosis, String fieldType) {
        switch (fieldType) {
            case "id": return diagnosis.getDiagnosisId();
            case "patient": return diagnosis.getPatientId();
            case "doctor": return diagnosis.getDoctorId();
            default: return "";
        }
    }
    
    private boolean validateListSizes(DoublyLinkedList<Integer> tablets, DoublyLinkedList<String> mealTimings, 
            DoublyLinkedList<Integer> timesPerDay, DoublyLinkedList<Integer> totalDays, int expectedSize) {
        return tablets.getNumberOfEntries() == expectedSize && 
               mealTimings.getNumberOfEntries() == expectedSize &&
               timesPerDay.getNumberOfEntries() == expectedSize &&
               totalDays.getNumberOfEntries() == expectedSize;
    }
}