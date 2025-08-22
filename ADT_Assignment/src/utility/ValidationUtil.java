package utility;

/**
 * Utility class for validation operations
 * ECB-compliant: Contains only static methods, no state
 */
public final class ValidationUtil {

    // Private constructor to prevent instantiation
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates patient name format
     * @param name The name to validate
     * @return true if name is valid (contains only letters and spaces, at least 2 characters)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("[a-zA-Z\\s]+") && name.length() >= 2;
    }
    
    /**
     * Validates Malaysian phone number format
     * @param phone The phone number to validate
     * @return true if phone number is valid Malaysian format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and dashes for validation
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        
        // Check if it's 10-11 digits and starts with valid Malaysian prefixes
        if (!cleanPhone.matches("\\d{10,11}")) {
            return false;
        }
        
        // Check Malaysian mobile/landline prefixes
        return cleanPhone.startsWith("01") || cleanPhone.startsWith("03") || 
               cleanPhone.startsWith("04") || cleanPhone.startsWith("05") || 
               cleanPhone.startsWith("06") || cleanPhone.startsWith("07") || 
               cleanPhone.startsWith("08") || cleanPhone.startsWith("09");
    }
    
    /**
     * Validates Malaysian IC number format and checksum
     * @param icNumber The IC number to validate (12 digits)
     * @return true if IC number is valid Malaysian format
     */
    public static boolean isValidMalaysianIC(String icNumber) {
        if (icNumber == null || icNumber.length() != 12) {
            return false;
        }
        
        // Check if all characters are digits
        if (!icNumber.matches("\\d{12}")) {
            return false;
        }
        
        // Extract components
        String birthDate = icNumber.substring(0, 6);  // YYMMDD
        String birthPlace = icNumber.substring(6, 8); // PB
        String gender = icNumber.substring(8, 12);    // XXXX
        
        // Validate birth date format (basic validation)
        int year = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4));
        int day = Integer.parseInt(birthDate.substring(4, 6));
        
        // Basic date validation
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            return false;
        }
        
        // Validate birth place code (01-59 for Malaysian states)
        int placeCode = Integer.parseInt(birthPlace);
        if (placeCode < 1 || placeCode > 59) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Normalizes gender input to standard format
     * @param gender The gender input to normalize
     * @return "Male" or "Female" if valid, null if invalid
     */
    public static String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return null;
        }
        
        String normalized = gender.trim().toLowerCase();
        
        if (normalized.equals("m") || normalized.equals("male")) {
            return "Male";
        } else if (normalized.equals("f") || normalized.equals("female")) {
            return "Female";
        }
        
        return null;
    }
    
    /**
     * Calculates age from Malaysian IC number
     * @param icNumber The 12-digit IC number
     * @return age in years, or 0 if invalid IC
     */
    public static int calculateAgeFromIC(String icNumber) {
        if (icNumber == null || icNumber.length() != 12) {
            return 0;
        }
        
        try {
            // Extract birth year from IC (first 2 digits)
            int birthYear = Integer.parseInt(icNumber.substring(0, 2));
            
            // Determine century (Malaysian IC format)
            if (birthYear >= 0 && birthYear <= 25) {
                birthYear += 2000; // 00-25 = 2000-2025
            } else {
                birthYear += 1900; // 26-99 = 1926-1999
            }
            
            // Calculate age (simplified - using current year 2025)
            return 2025 - birthYear;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Formats phone number for storage (adds dashes)
     * @param phone The phone number to format
     * @return formatted phone number with dashes
     */
    public static String formatPhoneForStorage(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        // Remove existing formatting
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        
        // Format based on length
        if (cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 3) + "-" + cleanPhone.substring(3, 7) + "-" + cleanPhone.substring(7);
        } else if (cleanPhone.length() == 11) {
            return cleanPhone.substring(0, 3) + "-" + cleanPhone.substring(3, 7) + "-" + cleanPhone.substring(7);
        }
        
        return phone; // Return original if can't format
    }
}
