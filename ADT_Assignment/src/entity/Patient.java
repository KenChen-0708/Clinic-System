package entity;

import java.io.Serializable;

public class Patient implements Serializable {
    private String id;
    private String name;
    private String phone;

    public Patient(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format("ID   : %s\nName : %s\nPhone: %s", id, name, phone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient p = (Patient) o;
        return id.equals(p.id);
    }
}
