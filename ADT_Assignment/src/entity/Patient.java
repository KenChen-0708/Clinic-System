// Ronaldo Loh Yu Fei

package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Patient Entity Class - Following ECB Pattern
 *
 * Responsibilities:
 * - Manage patient data and state
 * - Handle own formatting and validation
 * - Provide business logic related to patient operations
 * - Self-contained data management
 */
public class Patient implements Serializable {
    private String id;
    private String name;
    private String icNumber;
    private String phone;
    private String gender;
    private LocalDate registrationDate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
                           id, formatNameForDisplay(), formatICForDisplay(), formatPhoneForDisplay(),
                           formatGenderForDisplay(), registrationDate.format(DATE_FORMATTER));
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

    // ===== FORMATTING METHODS (ECB Pattern - Entity handles own formatting) =====

    /**
     * Format IC number for display
     * @return formatted IC (XXXXXX-XX-XXXX)
     */
    public String formatICForDisplay() {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return icNumber;
        }

        String cleanIC = cleanICNumber(icNumber);

        if (cleanIC.length() == 12) {
            // Format as XXXXXX-XX-XXXX
            return cleanIC.substring(0, 6) + "-" +
                   cleanIC.substring(6, 8) + "-" +
                   cleanIC.substring(8);
        }

        return icNumber; // Return original if not standard format
    }

    /**
     * Clean IC number (remove dashes and spaces)
     * @param ic raw IC input
     * @return cleaned IC number
     */
    public static String cleanICNumber(String ic) {
        if (ic == null) return null;
        return ic.replaceAll("[-\\s]", "").trim();
    }

    /**
     * Format phone number for display
     * @return formatted phone number
     */
    public String formatPhoneForDisplay() {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }

        String cleaned = phone.replaceAll("[^0-9]", "");

        // Malaysian mobile format: 01X-XXXXXXX
        if (cleaned.startsWith("01") && cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-" + cleaned.substring(3);
        }
        // Malaysian mobile format: 01X-XXXXXXXX
        else if (cleaned.startsWith("01") && cleaned.length() == 11) {
            return cleaned.substring(0, 3) + "-" + cleaned.substring(3);
        }
        // International format: +60-1X-XXXXXXX
        else if (cleaned.startsWith("60") && cleaned.length() == 11) {
            return "+60-" + cleaned.substring(2, 4) + "-" + cleaned.substring(4);
        }

        return phone; // Return original if no standard format
    }

    /**
     * Format name for display (proper case)
     * @return formatted name
     */
    public String formatNameForDisplay() {
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
     * @return formatted gender (Male/Female)
     */
    public String formatGenderForDisplay() {
        if (gender == null) return null;

        String normalized = gender.trim().toUpperCase();
        switch (normalized) {
            case "M":
            case "MALE":
                return "Male";
            case "F":
            case "FEMALE":
                return "Female";
            default:
                return gender;
        }
    }

    // ===== VALIDATION METHODS (ECB Pattern - Entity validates itself) =====

    /**
     * Validate patient data for registration
     * @return ValidationResult with status and error message
     */
    public ValidationResult validateForRegistration() {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Patient name is required");
        }

        if (name.trim().length() < 2) {
            return new ValidationResult(false, "Patient name must be at least 2 characters");
        }

        // Validate IC number
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return new ValidationResult(false, "IC number is required");
        }

        if (!isValidICFormat(icNumber)) {
            return new ValidationResult(false, "Invalid IC number format. Please use XXXXXX-XX-XXXX format");
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationResult(false, "Phone number is required");
        }

        if (!isValidPhoneFormat(phone)) {
            return new ValidationResult(false, "Invalid phone number format");
        }

        // Validate gender
        if (gender == null || gender.trim().isEmpty()) {
            return new ValidationResult(false, "Gender is required");
        }

        if (!isValidGender(gender)) {
            return new ValidationResult(false, "Gender must be Male/Female or M/F");
        }

        return new ValidationResult(true, "Valid");
    }

    /**
     * Validate IC number format
     * @param ic IC number to validate
     * @return true if valid format
     */
    public static boolean isValidICFormat(String ic) {
        if (ic == null) return false;

        String cleanIC = cleanICNumber(ic);

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
     * @param phone phone number to validate
     * @return true if valid format
     */
    public static boolean isValidPhoneFormat(String phone) {
        if (phone == null) return false;

        String cleaned = phone.replaceAll("[^0-9]", "");

        // Malaysian mobile: 01XXXXXXXX (10 digits) or 01XXXXXXXXX (11 digits)
        if (cleaned.startsWith("01") && (cleaned.length() == 10 || cleaned.length() == 11)) {
            return true;
        }

        // International: 60XXXXXXXXX (11 digits)
        if (cleaned.startsWith("60") && cleaned.length() == 11) {
            return true;
        }

        // Local without leading 0: XXXXXXXXX (9 digits)
        if (cleaned.length() == 9) {
            return true;
        }

        return false;
    }

    /**
     * Validate gender value
     * @param gender gender to validate
     * @return true if valid gender
     */
    public static boolean isValidGender(String gender) {
        if (gender == null) return false;

        String cleanGender = gender.trim().toLowerCase();
        return cleanGender.equals("male") || cleanGender.equals("female") ||
               cleanGender.equals("m") || cleanGender.equals("f");
    }

    /**
     * Check if patient data is valid for mapping operations
     * @return true if valid for data structure operations
     */
    public boolean isValidForMapping() {
        return id != null && !id.trim().isEmpty() &&
               icNumber != null && !icNumber.trim().isEmpty() &&
               name != null && !name.trim().isEmpty();
    }

    /**
     * Normalize name for searching (lowercase, trimmed)
     * @return normalized name
     */
    public String getNormalizedNameForSearch() {
        if (name == null) return null;
        return name.toLowerCase().trim();
    }

    // ===== ID GENERATION METHODS (ECB Pattern - Entity handles its own ID logic) =====

    /**
     * Extract number from patient ID
     * @param id patient ID to parse
     * @return extracted number or 0 if invalid
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
     * Generate next patient ID from number
     * @param maxExistingNumber highest existing patient number
     * @return formatted patient ID
     */
    public static String generateNextPatientId(int maxExistingNumber) {
        return String.format("P%04d", maxExistingNumber + 1);
    }

    // ===== INNER CLASS FOR VALIDATION RESULTS =====

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

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
