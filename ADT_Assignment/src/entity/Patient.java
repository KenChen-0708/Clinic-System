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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public void setIcNumber(String icNumber) {
        this.icNumber = icNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

   

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
        return icNumber != null ? icNumber.equals(p.icNumber) : p.icNumber == null;
    }

    @Override
    public int hashCode() {
        return icNumber != null ? icNumber.hashCode() : 0;
    }

    /**
     * Calculate age from IC number (business logic belongs in entity)
     * @return calculated age in years
     */
    public int calculateAge() {
        if (icNumber == null || icNumber.length() < 6) {
            return 0;
        }
        
        try {
            // Extract birth year from IC (first 6 digits: YYMMDD)
            String birthYearStr = icNumber.substring(0, 2);
            int birthYear = Integer.parseInt(birthYearStr);
            
            // Determine century (assume 00-29 = 2000s, 30-99 = 1900s)
            int fullBirthYear = (birthYear <= 29) ? (2000 + birthYear) : (1900 + birthYear);
            
            // Calculate age
            int currentYear = java.time.LocalDate.now().getYear();
            return currentYear - fullBirthYear;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Format phone number for storage (business logic belongs in entity)
     * @param phone raw phone input
     * @return formatted phone number
     */
    public static String formatPhoneForStorage(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        // Remove all non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        
        // Keep Malaysian phone numbers in their original format
        if (cleaned.startsWith("01") && (cleaned.length() == 10 || cleaned.length() == 11)) {
            // Malaysian format: 01XXXXXXXX or 01XXXXXXXXX - keep as-is
            return cleaned;
        } else if (cleaned.startsWith("60") && cleaned.length() == 11) {
            // International format: 60XXXXXXXXX - keep as-is
            return cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            // Local format: 0XXXXXXXXX - keep as-is (don't convert to 60...)
            return cleaned;
        } else if (cleaned.length() == 9) {
            // Local format without leading 0: XXXXXXXXX - keep as-is
            return cleaned;
        }
        
        return cleaned;
    }

    /**
     * Normalize gender input (business logic belongs in entity)
     * @param gender raw gender input
     * @return normalized gender (M/F)
     */
    public static String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return null;
        }
        
        String normalized = gender.trim().toLowerCase();
        if (normalized.equals("m") || normalized.equals("male")) {
            return "M";
        } else if (normalized.equals("f") || normalized.equals("female")) {
            return "F";
        }
        
        return null;
    }
}
