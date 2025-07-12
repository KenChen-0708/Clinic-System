package entity;

import java.io.Serializable;

public class Treatment implements Serializable {
    private String treatmentId;
    private String patientId;
    private String diagnosis;
    private String prescription;

    public Treatment(String treatmentId, String patientId, String diagnosis, String prescription) {
        this.treatmentId = treatmentId;
        this.patientId = patientId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public String getTreatmentId() { return treatmentId; }
    public String getPatientId() { return patientId; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }

    @Override
    public String toString() {
        return String.format(
            "Treatment ID : %s\nPatient ID   : %s\nDiagnosis     : %s\nPrescription  : %s",
            treatmentId, patientId, diagnosis, prescription
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Treatment)) return false;
        Treatment t = (Treatment) o;
        return treatmentId.equals(t.treatmentId);
    }
}
