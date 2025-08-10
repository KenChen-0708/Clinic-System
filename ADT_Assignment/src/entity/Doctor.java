package entity;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String id;
    private String name;
    private String specialization;
    private String schedule; // New field

    public Doctor(String id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.schedule = null; // Default: no schedule
    }

    // =========================
    // Getters
    // =========================
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getSchedule() { return schedule; }

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
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor d = (Doctor) o;
        return id.equalsIgnoreCase(d.id);
    }
}
