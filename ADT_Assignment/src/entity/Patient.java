package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Patient implements Serializable {
    private String id;
    private String name;
    private String icNumber;
    private String phone;
    private String gender;
    private LocalDate registrationDate;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Constructor for new patients (ID will be auto-generated)
    public Patient(String name, String icNumber, String phone, String gender) {
        this.name = name;
        this.icNumber = icNumber;
        this.phone = phone;
        this.gender = gender;
        this.registrationDate = LocalDate.now();
    }

    // Constructor with ID (for loading from file)
    public Patient(String id, String name, String icNumber, String phone, String gender, LocalDate registrationDate) {
        this.id = id;
        this.name = name;
        this.icNumber = icNumber;
        this.phone = phone;
        this.gender = gender;
        this.registrationDate = registrationDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getIcNumber() { return icNumber; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public LocalDate getRegistrationDate() { return registrationDate; }

    @Override
    public String toString() {
        return String.format("ID     : %s\nName   : %s\nIC     : %s\nPhone  : %s\nGender : %s\nReg Date: %s", 
                           id, name, icNumber, phone, gender, registrationDate.format(formatter));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient p = (Patient) o;
        return icNumber.equals(p.icNumber);
    }
}
