/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

public class Treatment {
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

    public String toString() {
        return treatmentId + "," + patientId + "," + diagnosis + "," + prescription;
    }

    public static Treatment fromString(String line) {
        String[] parts = line.split(",");
        return new Treatment(parts[0], parts[1], parts[2], parts[3]);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Treatment)) return false;
        Treatment t = (Treatment) o;
        return treatmentId.equals(t.treatmentId);
    }
}
