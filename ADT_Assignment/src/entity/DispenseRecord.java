// Author: Chen Kai Jian
package entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DispenseRecord implements Serializable {
    private String patientId;
    private String patientName;
    private String medId;
    private String medName;
    private int quantity;
    private LocalDateTime timestamp;

    public DispenseRecord(String patientId, String patientName,
                          String medId, String medName, int quantity,
                          LocalDateTime timestamp) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.medId = medId;
        this.medName = medName;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getMedId() { return medId; }
    public String getMedName() { return medName; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
