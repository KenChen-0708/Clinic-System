package utility;

import entity.Patient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Universal formatting utilities for patient data
 * Extracted from MaintainPatient for better code organization
 */
public class PatientFormatUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    /**
     * Format IC number for display
     * Universal IC formatting logic
     */
    public static String formatICForDisplay(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return ic;
        }
        
        String cleanIC = ic.replaceAll("-", "").trim();
        
        if (cleanIC.length() == 12) {
            // Format as XXXXXX-XX-XXXX
            return cleanIC.substring(0, 6) + "-" + 
                   cleanIC.substring(6, 8) + "-" + 
                   cleanIC.substring(8);
        }
        
        return ic; // Return original if not standard format
    }
    
    /**
     * Clean IC number for storage/comparison
     * Universal IC cleaning logic
     */
    public static String cleanICNumber(String ic) {
        if (ic == null) return null;
        return ic.replaceAll("-", "").trim();
    }
    
    /**
     * Format phone number for display
     * Universal phone formatting logic
     */
    public static String formatPhoneForDisplay(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        String cleanPhone = phone.replaceAll("[\\s-()]", "").trim();
        
        if (cleanPhone.startsWith("+60")) {
            cleanPhone = "0" + cleanPhone.substring(3);
        }
        
        if (cleanPhone.length() == 10 || cleanPhone.length() == 11) {
            // Format as 01X-XXX XXXX
            if (cleanPhone.length() == 10) {
                return cleanPhone.substring(0, 3) + "-" + 
                       cleanPhone.substring(3, 6) + " " + 
                       cleanPhone.substring(6);
            } else {
                return cleanPhone.substring(0, 3) + "-" + 
                       cleanPhone.substring(3, 7) + " " + 
                       cleanPhone.substring(7);
            }
        }
        
        return phone; // Return original if not standard format
    }
    
    /**
     * Format date for display
     * Universal date formatting logic
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Format patient name for display
     * Universal name formatting logic
     */
    public static String formatNameForDisplay(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        // Capitalize first letter of each word
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) formatted.append(" ");
            
            if (words[i].length() > 0) {
                formatted.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    formatted.append(words[i].substring(1));
                }
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Format gender for display
     * Universal gender formatting logic
     */
    public static String formatGenderForDisplay(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return gender;
        }
        
        String clean = gender.trim().toLowerCase();
        switch (clean) {
            case "m":
            case "male":
                return "Male";
            case "f":
            case "female":
                return "Female";
            default:
                return gender;
        }
    }
    
    /**
     * Calculate age from IC number
     * Universal age calculation logic
     */
    public static int calculateAgeFromIC(String ic) {
        if (ic == null) return 0;
        
        String cleanIC = cleanICNumber(ic);
        if (cleanIC.length() != 12) return 0;
        
        try {
            // Extract birth year (first 2 digits)
            int yearSuffix = Integer.parseInt(cleanIC.substring(0, 2));
            int month = Integer.parseInt(cleanIC.substring(2, 4));
            int day = Integer.parseInt(cleanIC.substring(4, 6));
            
            // Determine full year (assume 1900s for 00-99, adjust as needed)
            int birthYear = (yearSuffix <= 30) ? 2000 + yearSuffix : 1900 + yearSuffix;
            
            LocalDate birthDate = LocalDate.of(birthYear, month, day);
            LocalDate currentDate = LocalDate.now();
            
            int age = currentDate.getYear() - birthDate.getYear();
            
            // Adjust if birthday hasn't occurred this year
            if (currentDate.getDayOfYear() < birthDate.getDayOfYear()) {
                age--;
            }
            
            return Math.max(0, age);
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Generate next patient ID
     * Universal ID generation logic
     */
    public static String generateNextPatientId(int maxExistingNumber) {
        return String.format("P%04d", maxExistingNumber + 1);
    }
    
    /**
     * Extract number from patient ID
     * Universal ID parsing logic
     */
    public static int extractNumberFromPatientId(String id) {
        if (id == null || !id.startsWith("P")) {
            return 0;
        }
        
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Clean and normalize name for searching
     * Universal name normalization logic
     */
    public static String normalizeNameForSearch(String name) {
        if (name == null) return null;
        return name.toLowerCase().trim();
    }
}
