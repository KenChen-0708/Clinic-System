// Lee Siu Yee

package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import dao.DiagnosisDAO;
import entity.Diagnosis;
import entity.TreatmentMedicineList;
import boundary.PharmacyUI;

public class MaintainDiagnosis {

    private final DiagnosisDAO diagnosisDao = new DiagnosisDAO();

    // ADT-based caching system for performance optimization
    private ListInterface<String> statusOptions;
    private ListInterface<String> mealTimingOptions;
    private ListInterface<String> availableMedicines;

    public MaintainDiagnosis() {
        // Initialize cached options using DoublyLinkedList ADT
        statusOptions = new DoublyLinkedList<>();
        statusOptions.add("Pending");
        statusOptions.add("Completed");

        mealTimingOptions = new DoublyLinkedList<>();
        mealTimingOptions.add("Before meal");
        mealTimingOptions.add("After meal");

        loadAvailableMedicines();
        initializeDiagnosisCounter();
    }

    private void initializeDiagnosisCounter() {
        ListInterface<Diagnosis> diagnoses = getAllDiagnoses();
        if (!diagnoses.isEmpty()) {
            int maxCounter = 0;
            for (int i = 1; i <= diagnoses.getNumberOfEntries(); i++) {
                String diagnosisId = diagnoses.getEntry(i).getDiagnosisId();
                int idNumber = Integer.parseInt(diagnosisId.substring(1));
                maxCounter = Math.max(maxCounter, idNumber);
            }
            setDiagnosisCounter(maxCounter + 1);
        } else {
            setDiagnosisCounter(1); // Reset to 1 if no diagnoses exist
        }
    }

    private void setDiagnosisCounter(int newCounter) {
        try {
            java.lang.reflect.Field counterField = entity.Diagnosis.class.getDeclaredField("counter");
            counterField.setAccessible(true);
            counterField.setInt(null, newCounter);
        } catch (Exception e) {
            // Handle silently - counter will start from 1
        }
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
            // Silently handle exception - UI layer will handle error display
        }
    }

    // ===== CRUD OPERATIONS USING ADT =====
    // CREATE
    public boolean addDiagnosis(Diagnosis newDiagnosis) {
        if (newDiagnosis != null) {
            ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();
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
    public ListInterface<Diagnosis> getAllDiagnoses() {
        return (ListInterface<Diagnosis>) diagnosisDao.retrieveFromFile();
    }

    private Diagnosis findInList(ListInterface<Diagnosis> list, String value) {
        return findInList(list, value, "id");
    }

    public Diagnosis findById(String diagnosisId) {
        ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();
        return findInList(diagnosisList, diagnosisId);
    }

    public Diagnosis findDiagnosisById(String diagnosisId) {
        return findById(diagnosisId);
    }

    public ListInterface<Diagnosis> findByPatientId(String patientId) {
        return filterDiagnoses(patientId, "patient");
    }

    public ListInterface<Diagnosis> findDiagnosesByPatientId(String patientId) {
        return findByPatientId(patientId);
    }

    public ListInterface<Diagnosis> findByDoctorId(String doctorId) {
        return filterDiagnoses(doctorId, "doctor");
    }

    // UPDATE - Using ADT search and replace operations
    public boolean updateDiagnosis(String diagnosisId, ListInterface<String> newSymptoms, String newDetails) {
        ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();

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
        ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeDiagnosisFromList(diagnosisList, diagnosisId, "id", false);
    }

    public boolean deleteDiagnosisById(String diagnosisId) {
        return deleteById(diagnosisId);
    }

    public int deleteByPatientId(String patientId) {
        ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeMultipleDiagnosesFromList(diagnosisList, patientId, "patient");
    }

    public int deleteDiagnosesByPatientId(String patientId) {
        return deleteByPatientId(patientId);
    }

    public int deleteByDoctorId(String doctorId) {
        ListInterface<Diagnosis> diagnosisList = getAllDiagnoses();
        return removeMultipleDiagnosesFromList(diagnosisList, doctorId, "doctor");
    }

    public boolean deleteAll() {
        ListInterface<Diagnosis> emptyList = new DoublyLinkedList<>();
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

    // ===== CACHED OPTIONS ACCESS =====
    public ListInterface<String> getStatusOptions() {
        return statusOptions;
    }

    public ListInterface<String> getMealTimingOptions() {
        return mealTimingOptions;
    }

    public ListInterface<String> getAvailableMedicines() {
        return availableMedicines;
    }

    public void refreshMedicineCache() {
        loadAvailableMedicines();
    }

    // ===== MEDICINE MANAGEMENT USING ADT =====
    public ListInterface<TreatmentMedicineList> createMedicineList(
            ListInterface<String> prescriptions,
            ListInterface<Integer> tablets,
            ListInterface<String> mealTimings,
            ListInterface<Integer> timesPerDay,
            ListInterface<Integer> totalDays) {

        ListInterface<TreatmentMedicineList> medicines = new DoublyLinkedList<>();

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
    public ListInterface<String> processSymptoms(String symptomInput) {
        ListInterface<String> symptoms = new DoublyLinkedList<>();
        if (isValidInput(symptomInput)) {
            symptoms.add(symptomInput.trim());
        }
        return symptoms;
    }

    public boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    // ===== PRIVATE HELPER METHODS USING ADT OPERATIONS =====
    private ListInterface<Diagnosis> filterDiagnoses(String value, String fieldType) {
        ListInterface<Diagnosis> allDiagnoses = getAllDiagnoses();
        ListInterface<Diagnosis> filtered = new DoublyLinkedList<>();

        for (int i = 1; i <= allDiagnoses.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = allDiagnoses.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                filtered.add(diagnosis);
            }
        }
        return filtered;
    }

    private Diagnosis findInList(ListInterface<Diagnosis> list, String value, String fieldType) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Diagnosis diagnosis = list.getEntry(i);
            if (getFieldValue(diagnosis, fieldType).equalsIgnoreCase(value)) {
                return diagnosis;
            }
        }
        return null;
    }

    private boolean removeDiagnosisFromList(ListInterface<Diagnosis> list, String value, String fieldType, boolean removeAll) {
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

    private int removeMultipleDiagnosesFromList(ListInterface<Diagnosis> list, String value, String fieldType) {
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

    private ListInterface<String> getUniqueFieldValues(ListInterface<Diagnosis> list, String fieldType) {
        ListInterface<String> uniqueValues = new DoublyLinkedList<>();

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

    private boolean validateListSizes(ListInterface<Integer> tablets, ListInterface<String> mealTimings,
            ListInterface<Integer> timesPerDay, ListInterface<Integer> totalDays, int expectedSize) {
        return tablets.getNumberOfEntries() == expectedSize
                && mealTimings.getNumberOfEntries() == expectedSize
                && timesPerDay.getNumberOfEntries() == expectedSize
                && totalDays.getNumberOfEntries() == expectedSize;
    }
}
