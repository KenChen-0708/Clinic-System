// Lee Siu Yee

package entity;

import adt.DoublyLinkedList;
import adt.ListInterface;
import java.io.Serializable;
import java.time.LocalDate;

public class Treatment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static int counter = 1; // Static counter to generate unique treatmentId
    private String treatmentId;
    private String patientId;
    private String doctorId;
    private String status;
    private LocalDate treatmentDate;  // Field for treatment date
    private ListInterface<TreatmentMedicineList> medicineList;  // List of medicines

    // Constructor for creating a new treatment with treatment date and medicine list
    public Treatment(String patientId, String doctorId, String status, LocalDate treatmentDate) {
        this.treatmentId = "T" + String.format("%04d", counter++);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = status;
        this.treatmentDate = treatmentDate;
        this.medicineList = new DoublyLinkedList<>();  // Initialize the medicine list
    }

    // Constructor to create Treatment with just treatmentId (for retrieval purposes)
    public Treatment(String treatmentId) {
        this.treatmentId = treatmentId;
        this.medicineList = new DoublyLinkedList<>();  // Initialize the medicine list
    }

    // Getters and Setters
    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(LocalDate treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public ListInterface<TreatmentMedicineList> getMedicineList() {
        return medicineList;
    }

    public void setMedicineList(ListInterface<TreatmentMedicineList> medicineList) {
        this.medicineList = medicineList;
    }

    // Add a medicine to the list
    public void addMedicine(TreatmentMedicineList medicine) {
        this.medicineList.add(medicine);
    }
}
