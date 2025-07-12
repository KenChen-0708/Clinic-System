package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.PatientUI;
import dao.PatientDAO;
import entity.Patient;
import utility.MessageUI;

import java.util.LinkedList;
import java.util.Queue;

public class MaintainPatient {

    private final ListInterface<Patient> patientList;
    private final PatientDAO patientDAO;
    private final PatientUI patientUI;

    public MaintainPatient() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.retrieveFromFile();
        patientUI = new PatientUI(this);
    }

    public void runPatientMenu() {
        patientUI.run();
    }

    public void handleMenuChoice(int choice) {
        switch (choice) {
            case 1:
                registerNewPatient();
                break;
            case 2:
                removePatient();
                break;
            case 3:
                viewAllPatients();
                break;
            case 4:
                searchPatientById();
                break;
            case 5:
                simulatePatientQueue();
                break;
            case 0:
                MessageUI.displayExitMessage();
                break;
            default:
                MessageUI.displayInvalidChoiceMessage();
        }

    }

    private void registerNewPatient() {
        Patient patient = patientUI.inputPatientDetails();
        if (!patientList.contains(patient)) {
            patientList.add(patient);
            patientDAO.saveToFile(patientList);
            patientUI.displayMessage("Patient registered successfully.");
        } else {
            patientUI.displayMessage("Patient already exists.");
        }
    }

    private void removePatient() {
        String id = patientUI.inputPatientId();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            if (patientList.getEntry(i).getId().equals(id)) {
                patientList.remove(i);
                patientDAO.saveToFile(patientList);
                patientUI.displayMessage("Patient removed successfully.");
                return;
            }
        }
        patientUI.displayMessage("Patient ID not found.");
    }

    private void viewAllPatients() {
        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            output.append("Patient ID : ").append(p.getId()).append("\n")
                    .append("Name       : ").append(p.getName()).append("\n")
                    .append("Phone      : ").append(p.getPhone()).append("\n")
                    .append("------------------------------\n");
        }
        patientUI.displayPatientList(output.toString());
    }

    private void searchPatientById() {
        String id = patientUI.inputPatientId();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            if (p.getId().equalsIgnoreCase(id)) {
                patientUI.displayPatient(p);
                return;
            }
        }
        patientUI.displayMessage("Patient not found.");
    }

    private void simulatePatientQueue() {
        Queue<Patient> queue = new LinkedList<>();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            queue.add(patientList.getEntry(i));
        }

        System.out.println("\n--- Simulating Patient Queue ---");
        while (!queue.isEmpty()) {
            Patient current = queue.poll();
            System.out.println("Now serving: " + current.getName() + " (ID: " + current.getId() + ")");
        }

        System.out.println("All patients have been served.");
    }

    public ListInterface<Patient> getPatientList() {
        return patientList;
    }
}
