package entity;

import adt.DoublyLinkedList;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Diagnosis implements Serializable {

    private static int counter = 1; // Static counter to generate unique diagnosisId
    private String diagnosisId;
    private String patientId;
    private String doctorId;
    private DoublyLinkedList<String> symptoms;
    private String diagnosisDetails;
    private LocalDate diagnosisDate;

    public Diagnosis(String patientId, String doctorId, DoublyLinkedList<String> symptoms, String diagnosisDetails) {
        // Generate the diagnosisId by formatting the counter with leading zeros
        this.diagnosisId = "S" + String.format("%04d", counter++);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.symptoms = symptoms;
        this.diagnosisDetails = diagnosisDetails;
        this.diagnosisDate = LocalDate.now();
    }

    // Getters and Setters
    public String getDiagnosisId() {
        return diagnosisId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public DoublyLinkedList<String> getSymptoms() {
        return symptoms;
    }

    public String getDiagnosisDetails() {
        return diagnosisDetails;
    }

    public LocalDate getDiagnosisDate() {
        return diagnosisDate;
    }

    public String getFormattedDate() {
        return diagnosisDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Setters
    public void setSymptoms(DoublyLinkedList<String> symptoms) {
        this.symptoms = symptoms;
    }

    public void setDiagnosisDetails(String diagnosisDetails) {
        this.diagnosisDetails = diagnosisDetails;
    }

    @Override
    public String toString() {
        return String.format(
                "\n------------------------"
                + "\nDiagnosis Information:\n"
                + "------------------------\n"
                + "Diagnosis ID     : %s\n"
                + "Patient ID       : %s\n"
                + "Doctor ID        : %s\n"
                + "Symptoms         : %s\n"
                + "Diagnosis Details: %s\n"
                + "Date             : %s\n"
                + "------------------------\n",
                diagnosisId, patientId, doctorId, symptoms, diagnosisDetails, getFormattedDate());
    }
}
