// Lee Siu Yee

package entity;

import adt.ListInterface;
import java.io.Serializable;

/**
 * Entity class to hold all treatment report data
 * Used to transfer report data from control layer to UI layer
 */
public class TreatmentReportData implements Serializable {
    
    private int totalPatients;
    private int totalTreatments;
    private int totalDoctors;
    private int treatmentsWithMedicine;
    private int treatmentsWithoutMedicine;
    
    private String topDoctor;
    private int topDoctorTreatments;
    private String topPatient;
    private int topPatientTreatments;
    private String topDiagnosis;
    private int topDiagnosisCount;
    private String topMedicine;
    private int topMedicineCount;
    
    private ListInterface<DiagnosisDistribution> diagnosisDistributions;
    private ListInterface<MedicineDistribution> medicineDistributions;
    
    public TreatmentReportData() {
    }
    
    // Getters and Setters for basic statistics
    public int getTotalPatients() {
        return totalPatients;
    }
    
    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }
    
    public int getTotalTreatments() {
        return totalTreatments;
    }
    
    public void setTotalTreatments(int totalTreatments) {
        this.totalTreatments = totalTreatments;
    }
    
    public int getTotalDoctors() {
        return totalDoctors;
    }
    
    public void setTotalDoctors(int totalDoctors) {
        this.totalDoctors = totalDoctors;
    }
    
    public int getTreatmentsWithMedicine() {
        return treatmentsWithMedicine;
    }
    
    public void setTreatmentsWithMedicine(int treatmentsWithMedicine) {
        this.treatmentsWithMedicine = treatmentsWithMedicine;
    }
    
    public int getTreatmentsWithoutMedicine() {
        return treatmentsWithoutMedicine;
    }
    
    public void setTreatmentsWithoutMedicine(int treatmentsWithoutMedicine) {
        this.treatmentsWithoutMedicine = treatmentsWithoutMedicine;
    }
    
    // Getters and Setters for top statistics
    public String getTopDoctor() {
        return topDoctor;
    }
    
    public void setTopDoctor(String topDoctor) {
        this.topDoctor = topDoctor;
    }
    
    public int getTopDoctorTreatments() {
        return topDoctorTreatments;
    }
    
    public void setTopDoctorTreatments(int topDoctorTreatments) {
        this.topDoctorTreatments = topDoctorTreatments;
    }
    
    public String getTopPatient() {
        return topPatient;
    }
    
    public void setTopPatient(String topPatient) {
        this.topPatient = topPatient;
    }
    
    public int getTopPatientTreatments() {
        return topPatientTreatments;
    }
    
    public void setTopPatientTreatments(int topPatientTreatments) {
        this.topPatientTreatments = topPatientTreatments;
    }
    
    public String getTopDiagnosis() {
        return topDiagnosis;
    }
    
    public void setTopDiagnosis(String topDiagnosis) {
        this.topDiagnosis = topDiagnosis;
    }
    
    public int getTopDiagnosisCount() {
        return topDiagnosisCount;
    }
    
    public void setTopDiagnosisCount(int topDiagnosisCount) {
        this.topDiagnosisCount = topDiagnosisCount;
    }
    
    public String getTopMedicine() {
        return topMedicine;
    }
    
    public void setTopMedicine(String topMedicine) {
        this.topMedicine = topMedicine;
    }
    
    public int getTopMedicineCount() {
        return topMedicineCount;
    }
    
    public void setTopMedicineCount(int topMedicineCount) {
        this.topMedicineCount = topMedicineCount;
    }
    
    // Getters and Setters for distribution lists
    public ListInterface<DiagnosisDistribution> getDiagnosisDistributions() {
        return diagnosisDistributions;
    }
    
    public void setDiagnosisDistributions(ListInterface<DiagnosisDistribution> diagnosisDistributions) {
        this.diagnosisDistributions = diagnosisDistributions;
    }
    
    public ListInterface<MedicineDistribution> getMedicineDistributions() {
        return medicineDistributions;
    }
    
    public void setMedicineDistributions(ListInterface<MedicineDistribution> medicineDistributions) {
        this.medicineDistributions = medicineDistributions;
    }
}