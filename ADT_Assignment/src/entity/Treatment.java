package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Treatment implements Serializable {
    private String treatmentId;
    private String patientId;
    private String doctorId;
    private String symptoms;
    private String diagnosis;
    private int severityLevel;
    private String prescription;
    private LocalDate treatmentDate;
    private String status;

    public Treatment(String treatmentId, String patientId, String doctorId,
            String symptoms, String diagnosis, int severityLevel,
            String prescription, String status) {
        this.treatmentId = treatmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.severityLevel = severityLevel;
        this.prescription = prescription;
        this.treatmentDate = LocalDate.now();
        this.status = status;
    }

    // Getters
    public String getTreatmentId() {
        return treatmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public String getPrescription() {
        return prescription;
    }

    public String getFormattedDate() {
        return treatmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format(
                "Treatment ID: %s\nPatient ID: %s\nDoctor ID: %s\nDate: %s\n"
                + "Symptoms: %s\nDiagnosis: %s\nSeverity: %d\nPrescription: %s\nStatus: %s\n",
                treatmentId, patientId, doctorId, getFormattedDate(), symptoms,
                diagnosis, severityLevel, prescription, status
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Treatment)) {
            return false;
        }
        Treatment treatment = (Treatment) o;
        return treatmentId.equals(treatment.treatmentId);
    }
}