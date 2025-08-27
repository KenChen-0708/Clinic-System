package entity;

import java.io.Serializable;

public class Doctor implements Serializable {

    private String id;
    private String name;
    private String specialization;
    private String schedule; // New field
    private String availability;

    public Doctor(String id, String name, String specialization, String schedule, String availability) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.schedule = schedule;
        this.availability = availability;
    }

    // =========================
    // Getters
    // =========================
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getSchedule() {
        return schedule;
    }

    // =========================
    // Setters
    // =========================
    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setId(String id) {
        this.id = id;
    }

    // =========================
    // Overrides
    // =========================
    @Override
    public String toString() {
        return String.format(
                "ID            : %s\nName          : %s\nSpecialization: %s\nSchedule      : %s\n",
                id, name, specialization, (schedule != null ? schedule : "Not set")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Doctor)) {
            return false;
        }
        Doctor d = (Doctor) o;
        return id.equalsIgnoreCase(d.id);
    }
}
