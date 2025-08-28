// Lee Siu Yee

package entity;

import java.io.Serializable;

public class TreatmentMedicineList implements Serializable{
    private String treatmentId;  // Link to Treatment
    private String prescription;
    private int tablets;          // Number of tablets to take
    private String mealTiming;    // Before/after meal
    private int timesPerDay;      // How many times a day the medicine is taken
    private int totalDay;

    // Constructor to store medicine-related information
    public TreatmentMedicineList(String treatmentId, String prescription, int tablets, String mealTiming, int timesPerDay, int totalDay) {
        this.treatmentId = treatmentId;
        this.prescription = prescription;
        this.tablets = tablets;
        this.mealTiming = mealTiming;
        this.timesPerDay = timesPerDay;
        this.totalDay = totalDay;
    }

    // Getters and Setters
    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public int getTablets() {
        return tablets;
    }

    public void setTablets(int tablets) {
        this.tablets = tablets;
    }

    public String getMealTiming() {
        return mealTiming;
    }

    public void setMealTiming(String mealTiming) {
        this.mealTiming = mealTiming;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }
    
    public int getTotalDay() {
        return totalDay;
    }

    public void setTotalDay(int totalDay) {
        this.totalDay = totalDay;
    }
}
