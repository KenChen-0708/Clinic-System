// Lee Siu Yee

package entity;

import java.io.Serializable;

/**
 * Entity class to store diagnosis distribution data for reporting
 * Used in Treatment Summary and Distribution Reports
 */
public class DiagnosisDistribution implements Serializable {
    
    private String diagnosisName;
    private int count;
    private double percentage;
    
    public DiagnosisDistribution() {
    }
    
    public DiagnosisDistribution(String diagnosisName, int count, double percentage) {
        this.diagnosisName = diagnosisName;
        this.count = count;
        this.percentage = percentage;
    }
    
    // Getters and Setters
    public String getDiagnosisName() {
        return diagnosisName;
    }
    
    public void setDiagnosisName(String diagnosisName) {
        this.diagnosisName = diagnosisName;
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
        return String.format("%s: %d (%.0f%%)", diagnosisName, count, percentage);
    }
}