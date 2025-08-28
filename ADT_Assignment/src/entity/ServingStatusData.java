// Lee Siu Yee

package entity;

import java.io.Serializable;

/**
 * Entity class to represent the current serving status data
 * Used for displaying consultation queue information in the UI
 */
public class ServingStatusData implements Serializable {
    
    private final Patient currentPatient;
    private final Doctor currentDoctor;
    private final int waitingPatients;

    public ServingStatusData(Patient currentPatient, Doctor currentDoctor, int waitingPatients) {
        this.currentPatient = currentPatient;
        this.currentDoctor = currentDoctor;
        this.waitingPatients = waitingPatients;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public Doctor getCurrentDoctor() {
        return currentDoctor;
    }

    public int getWaitingPatients() {
        return waitingPatients;
    }

    public boolean hasActiveConsultation() {
        return currentPatient != null && currentDoctor != null;
    }

    @Override
    public String toString() {
        if (hasActiveConsultation()) {
            return String.format(
                "Current Consultation:\n" +
                "Patient: %s (%s)\n" +
                "Doctor: %s (%s)\n" +
                "Waiting Patients: %d",
                currentPatient.getName(), currentPatient.getId(),
                currentDoctor.getName(), currentDoctor.getId(),
                waitingPatients
            );
        } else {
            return "No active consultation";
        }
    }
}