package utility;

import entity.Patient;

/**
 * Universal validation utilities for patient data
 * Extracted from MaintainPatient for better code organization
 */
public class PatientValidationUtil {
    
    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Validate patient data for registration
     * Universal validation logic that can be reused
     */
    public static ValidationResult validatePatientForRegistration(Patient patient) {
        if (patient == null) {
            return new ValidationResult(false, "Patient data cannot be null");
        }
        
        // Validate name
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Patient name is required");
        }
        
        if (patient.getName().trim().length() < 2) {
            return new ValidationResult(false, "Patient name must be at least 2 characters");
        }
        
        // Validate IC number
        if (patient.getIcNumber() == null || patient.getIcNumber().trim().isEmpty()) {
            return new ValidationResult(false, "IC number is required");
        }
        
        if (!isValidICFormat(patient.getIcNumber())) {
            return new ValidationResult(false, "Invalid IC number format");
        }
        
        // Validate phone
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        if (!isValidPhoneFormat(patient.getPhone())) {
            return new ValidationResult(false, "Invalid phone number format");
        }
        
        // Validate gender
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            return new ValidationResult(false, "Gender is required");
        }
        
        if (!isValidGender(patient.getGender())) {
            return new ValidationResult(false, "Gender must be Male or Female");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate IC number format
     * Universal IC validation logic
     */
    public static boolean isValidICFormat(String ic) {
        if (ic == null) return false;
        
        String cleanIC = ic.replaceAll("-", "").trim();
        
        // Malaysian IC: 12 digits (YYMMDD-PB-###G)
        if (cleanIC.length() != 12) return false;
        
        // Check if all characters are digits
        for (char c : cleanIC.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        
        // Basic date validation (YYMMDD)
        try {
            int month = Integer.parseInt(cleanIC.substring(2, 4));
            int day = Integer.parseInt(cleanIC.substring(4, 6));
            
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate phone number format
     * Universal phone validation logic
     */
    public static boolean isValidPhoneFormat(String phone) {
        if (phone == null) return false;
        
        String cleanPhone = phone.replaceAll("[\\s-()]", "").trim();
        
        // Malaysian phone: 10-11 digits, starting with 01 or +60
        if (cleanPhone.startsWith("+60")) {
            cleanPhone = "0" + cleanPhone.substring(3);
        }
        
        if (cleanPhone.length() < 10 || cleanPhone.length() > 11) return false;
        
        if (!cleanPhone.startsWith("01")) return false;
        
        // Check if all characters are digits
        for (char c : cleanPhone.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        
        return true;
    }
    
    /**
     * Validate gender value
     * Universal gender validation logic
     */
    public static boolean isValidGender(String gender) {
        if (gender == null) return false;
        
        String cleanGender = gender.trim().toLowerCase();
        return cleanGender.equals("male") || cleanGender.equals("female") || 
               cleanGender.equals("m") || cleanGender.equals("f");
    }
    
    /**
     * Check if patient data is valid for mapping operations
     * Universal validation for data structure operations
     */
    public static boolean isValidPatientForMapping(Patient patient) {
        return patient != null && 
               patient.getId() != null && !patient.getId().trim().isEmpty() &&
               patient.getIcNumber() != null && !patient.getIcNumber().trim().isEmpty() &&
               patient.getName() != null && !patient.getName().trim().isEmpty();
    }
    
    /**
     * Validate search parameters
     * Universal search validation logic
     */
    public static boolean isValidSearchParameter(String parameter) {
        return parameter != null && !parameter.trim().isEmpty();
    }
}
