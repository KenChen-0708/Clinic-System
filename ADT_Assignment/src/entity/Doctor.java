package entity;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String id;
    private String name;
    private String specialization;

    public Doctor(String id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() {
        return String.format("ID            : %s\nName          : %s\nSpecialization: %s\n", id, name, specialization);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor d = (Doctor) o;
        return id.equals(d.id);
    }
}
