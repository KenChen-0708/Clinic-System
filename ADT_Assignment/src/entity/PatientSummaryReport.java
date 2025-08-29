package entity;

import adt.HashMap;
import adt.MapInterface;
import adt.DoublyLinkedList;
import adt.ListInterface;

/**
 * Entity: PatientSummaryReport
 * Holds aggregated statistics built from Patient data using custom ADTs.
 * This object is read-only for UI and produced by the Control layer.
 */
public class PatientSummaryReport {

    // Core counts
    private int totalPatients = 0;
    private double averageAge = 0.0;

    // Extremes
    private Patient youngestPatient = null;
    private Patient oldestPatient = null;

    // Distributions (use custom ADTs)
    private final MapInterface<String, Integer> genderDistribution = new HashMap<>();
    private final MapInterface<String, Integer> ageGroupDistribution = new HashMap<>();
    private final MapInterface<Integer, Integer> yearlyRegistrations = new HashMap<>();
    private final MapInterface<String, Integer> monthlyRegistrations = new HashMap<>(); // key: YYYY-MM
    private final MapInterface<String, Integer> ageDistribution = new HashMap<>(); // bucket: 0-10, 11-20, ...

    // Nested: year -> (gender -> count)
    private final MapInterface<String, MapInterface<String, Integer>> genderByYear = new HashMap<>();

    // Getters (no setters for maps; maps are mutable through map APIs if needed by control)
    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public double getAverageAge() { return averageAge; }
    public void setAverageAge(double averageAge) { this.averageAge = averageAge; }

    public Patient getYoungestPatient() { return youngestPatient; }
    public void setYoungestPatient(Patient youngestPatient) { this.youngestPatient = youngestPatient; }

    public Patient getOldestPatient() { return oldestPatient; }
    public void setOldestPatient(Patient oldestPatient) { this.oldestPatient = oldestPatient; }

    public MapInterface<String, Integer> getGenderDistribution() { return genderDistribution; }
    public MapInterface<String, Integer> getAgeGroupDistribution() { return ageGroupDistribution; }
    public MapInterface<Integer, Integer> getYearlyRegistrations() { return yearlyRegistrations; }
    public MapInterface<String, Integer> getMonthlyRegistrations() { return monthlyRegistrations; }
    public MapInterface<String, Integer> getAgeDistribution() { return ageDistribution; }
    public MapInterface<String, MapInterface<String, Integer>> getGenderByYear() { return genderByYear; }

    // Utility: compute patient growth (currentYear vs lastYear)
    public int getYearlyGrowthAbsolute(int currentYear) {
        Integer curr = yearlyRegistrations.get(currentYear);
        Integer prev = yearlyRegistrations.get(currentYear - 1);
        int c = curr == null ? 0 : curr;
        int p = prev == null ? 0 : prev;
        return c - p;
    }

    public double getYearlyGrowthPercent(int currentYear) {
        Integer prev = yearlyRegistrations.get(currentYear - 1);
        Integer curr = yearlyRegistrations.get(currentYear);
        int p = prev == null ? 0 : prev;
        int c = curr == null ? 0 : curr;
        if (p == 0) return c > 0 ? 100.0 : 0.0;
        return ((double)(c - p) / p) * 100.0;
    }

    // Utility: compute most common gender for a given year (returns null if none)
    public String getMostCommonGenderForYear(String year) {
        MapInterface<String, Integer> map = genderByYear.get(year);
        if (map == null) return null;
        ListInterface<String> keys = map.getKeys();
        java.util.Iterator<String> it = keys.iterator();
        String best = null;
        int bestCount = -1;
        while (it.hasNext()) {
            String g = it.next();
            Integer v = map.get(g);
            int cnt = v == null ? 0 : v;
            if (cnt > bestCount) {
                bestCount = cnt;
                best = g;
            }
        }
        return best;
    }

    // ECB FIX: Business logic moved to Entity layer
    /**
     * Classify age into business-defined groups
     * Entity layer handles business rules
     */
    public static String classifyAgeGroup(int age) {
        if (age <= 12) return "Children (0-12)";
        if (age <= 59) return "Adults (13-59)";
        return "Seniors (60+)";
    }

    /**
     * Create age bucket for histogram display
     * Entity layer handles data categorization rules
     */
    public static String createAgeBucket(int age) {
        if (age < 0) return "Unknown";
        int start = (age / 10) * 10 + (age % 10 == 0 ? 0 : 1);
        int end = start + 9;
        // Special-case for 0–10 inclusive bucket
        if (age <= 10) { start = 0; end = 10; }
        return start + "-" + end;
    }
}

