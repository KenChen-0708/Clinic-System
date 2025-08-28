// Lee Siu Yee

package entity;

import java.io.Serializable;

/**
 * Entity class to store medicine distribution data for reporting
 * Used in Treatment Summary and Distribution Reports
 */
public class MedicineDistribution implements Serializable {
    
    private String medicineName;
    private int count;
    private double percentage;
    
    public MedicineDistribution() {
    }
    
    public MedicineDistribution(String medicineName, int count, double percentage) {
        this.medicineName = medicineName;
        this.count = count;
        this.percentage = percentage;
    }
    
    // Getters and Setters
    public String getMedicineName() {
        return medicineName;
    }
    
    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d (%.0f%%)", medicineName, count, percentage);
    }
}