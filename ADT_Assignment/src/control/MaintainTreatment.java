package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.TreatmentUI;
import dao.TreatmentDAO;
import entity.Treatment;
import utility.MessageUI;

public class MaintainTreatment {
    private final ListInterface<Treatment> treatmentList;
    private final TreatmentDAO treatmentDAO;
    private final TreatmentUI treatmentUI;

    public MaintainTreatment() {
        treatmentDAO = new TreatmentDAO();
        treatmentList = treatmentDAO.retrieveFromFile();
        treatmentUI = new TreatmentUI(this);
    }

    public void addTreatment() {
        Treatment t = treatmentUI.inputTreatmentDetails();
        if (!treatmentList.contains(t)) {
            treatmentList.add(t);
            treatmentDAO.saveToFile(treatmentList);
            treatmentUI.displayMessage("✅ Treatment recorded successfully.");
        } else {
            treatmentUI.displayMessage("❌ Treatment ID already exists.");
        }
    }

    public void removeTreatment() {
        String id = treatmentUI.inputTreatmentId();
        for (int i = 1; i <= treatmentList.getNumberOfEntries(); i++) {
            Treatment t = treatmentList.getEntry(i);
            if (t.getTreatmentId().equalsIgnoreCase(id)) {
                treatmentList.remove(i);
                treatmentDAO.saveToFile(treatmentList);
                treatmentUI.displayMessage("✅ Treatment removed successfully.");
                return;
            }
        }
        treatmentUI.displayMessage("❌ Treatment ID not found.");
    }

    public void displayAllTreatments() {
        if (treatmentList.isEmpty()) {
            treatmentUI.displayMessage("No treatment records found.");
            return;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= treatmentList.getNumberOfEntries(); i++) {
            Treatment t = treatmentList.getEntry(i);
            output.append(i).append(". ")
                  .append("Treatment ID: ").append(t.getTreatmentId()).append("\n")
                  .append("Patient ID  : ").append(t.getPatientId()).append("\n")
                  .append("Diagnosis   : ").append(t.getDiagnosis()).append("\n")
                  .append("Prescription: ").append(t.getPrescription()).append("\n")
                  .append("------------------------------\n");
        }

        treatmentUI.displayTreatmentList(output.toString());
    }

    public void searchTreatmentByPatientId() {
        String pid = treatmentUI.inputPatientId();
        boolean found = false;
        StringBuilder output = new StringBuilder();

        for (int i = 1; i <= treatmentList.getNumberOfEntries(); i++) {
            Treatment t = treatmentList.getEntry(i);
            if (t.getPatientId().equalsIgnoreCase(pid)) {
                found = true;
                output.append("Treatment ID: ").append(t.getTreatmentId()).append("\n")
                      .append("Diagnosis   : ").append(t.getDiagnosis()).append("\n")
                      .append("Prescription: ").append(t.getPrescription()).append("\n")
                      .append("------------------------------\n");
            }
        }

        if (found) {
            treatmentUI.displayTreatmentList(output.toString());
        } else {
            treatmentUI.displayMessage("No treatment history found for patient ID: " + pid);
        }
    }

    public void runTreatmentMenu() {
        treatmentUI.run();
    }
}
