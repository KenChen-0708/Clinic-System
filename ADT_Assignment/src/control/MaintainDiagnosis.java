package control;

import adt.DoublyLinkedList;
import dao.DiagnosisDAO;
import entity.Diagnosis;

public class MaintainDiagnosis {

    private DoublyLinkedList<Diagnosis> diagnosisList = new DoublyLinkedList<>();
    private final DiagnosisDAO diagnosisDao = new DiagnosisDAO();
    
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