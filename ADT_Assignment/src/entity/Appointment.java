package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment entity class
 * Represents an appointment between a patient and doctor
 */
public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED"
    
    /**
     * Default constructor
     */
    public Appointment() {
        this.appointmentDate = LocalDate.now();
        this.status = "SCHEDULED";
    }
    
    /**
     * Constructor with all fields
     */
    public Appointment(String appointmentId, String patientId, String doctorId, 
                      LocalDate appointmentDate, LocalTime appointmentTime, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }
    
    /**
     * Constructor for new appointment
     */
    public Appointment(String appointmentId, String patientId, String doctorId, 
                      LocalTime appointmentTime) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = LocalDate.now();
        this.appointmentTime = appointmentTime;
        this.status = "SCHEDULED";
    }
    
    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    
    // Setters
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("Appointment{id='%s', patientId='%s', doctorId='%s', date=%s, time=%s, status='%s'}", 
                           appointmentId, patientId, doctorId, appointmentDate, appointmentTime, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Appointment that = (Appointment) obj;
        return appointmentId != null ? appointmentId.equals(that.appointmentId) : that.appointmentId == null;
    }
    
    @Override
    public int hashCode() {
        return appointmentId != null ? appointmentId.hashCode() : 0;
    }
}
