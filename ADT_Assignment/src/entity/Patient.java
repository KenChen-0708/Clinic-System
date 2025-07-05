/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

public class Patient {
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

    public String toString() {
        return id + "," + name + "," + phone;
    }

    public static Patient fromString(String line) {
        String[] parts = line.split(",");
        return new Patient(parts[0], parts[1], parts[2]);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient p = (Patient) o;
        return id.equals(p.id);
    }
}
