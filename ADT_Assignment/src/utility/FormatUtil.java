package utility;

/**
 * Utility class for formatting data display
 * Contains static methods for common formatting operations
 */
public class FormatUtil {
    
    /**
     * Format Malaysian IC number for display with dashes
     * @param icNumber 12-digit IC number without dashes
     * @return Formatted IC number with dashes (XXXXXX-XX-XXXX)
     */
    public static String formatICForDisplay(String icNumber) {
        if (icNumber != null && icNumber.length() == 12) {
            return icNumber.substring(0, 6) + "-" + icNumber.substring(6, 8) + "-" + icNumber.substring(8);
        }
        return icNumber; // Return as-is if not 12 digits
    }
    
    /**
     * Format Malaysian phone number for display
     * @param phone Phone number without formatting
     * @return Formatted phone number with dashes
     */
    public static String formatPhoneForDisplay(String phone) {
        if (phone != null && phone.length() >= 10) {
            if (phone.length() == 10) {
                return phone.substring(0, 3) + "-" + phone.substring(3, 7) + "-" + phone.substring(7);
            } else if (phone.length() == 11) {
                return phone.substring(0, 3) + "-" + phone.substring(3, 7) + "-" + phone.substring(7);
            }
        }
        return phone;
    }
    
    /**
     * Truncate string to specified length with ellipsis
     * @param str String to truncate
     * @param maxLength Maximum length including ellipsis
     * @return Truncated string with "..." if needed
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Format registration date for display
     * @param date LocalDate to format
     * @return Formatted date string (dd-MM-yyyy)
     */
    public static String formatDateForDisplay(java.time.LocalDate date) {
        if (date != null) {
            return date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }
}
