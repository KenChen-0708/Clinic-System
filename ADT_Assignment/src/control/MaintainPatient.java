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
            case 6:
                generatePatientStatisticsReport();
                break;
            case 7:
                generatePatientContactReport();
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

        // Check if patient already exists (by IC number)
        if (!patientList.contains(patient)) {
            // Auto-generate patient ID
            String newId = generateNextPatientId();
            patient.setId(newId);

            patientList.add(patient);
            patientDAO.saveToFile(patientList);
            patientUI.displayMessage("Patient registered successfully with ID: " + newId);
        } else {
            patientUI.displayMessage("Patient with this IC number already exists.");
        }
    }

    private String generateNextPatientId() {
        int max = 0;
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            String id = patientList.getEntry(i).getId();
            if (id != null && id.startsWith("P")) {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) {
                    max = num;
                }
            }
        }
        return String.format("P%03d", max + 1);
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
        if (patientList.getNumberOfEntries() == 0) {
            patientUI.displayMessage("No patients registered yet.");
            return;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            output.append("Patient ID : ").append(p.getId()).append("\n")
                    .append("Name       : ").append(p.getName()).append("\n")
                    .append("IC Number  : ").append(p.getIcNumber()).append("\n")
                    .append("Phone      : ").append(p.getPhone()).append("\n")
                    .append("Gender     : ").append(p.getGender()).append("\n")
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

    private void generatePatientStatisticsReport() {
        System.out.println("\n=== PATIENT STATISTICS REPORT ===");
        System.out.println("Total Registered Patients: " + patientList.getNumberOfEntries());

        // Analyze phone number patterns
        int mobileCount = 0;
        int landlineCount = 0;

        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            String phone = p.getPhone().replaceAll("[^0-9]", "");
            if (phone.startsWith("01")) {
                mobileCount++;
            } else {
                landlineCount++;
            }
        }

        System.out.println("Mobile Numbers: " + mobileCount);
        System.out.println("Landline Numbers: " + landlineCount);
        System.out.println("================================\n");
    }

    private void generatePatientContactReport() {
        System.out.println("\n=== PATIENT CONTACT DIRECTORY ===");

        // Sort patients by name for better readability
        ListInterface<Patient> sortedList = new DoublyLinkedList<>();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            sortedList.add(patientList.getEntry(i));
        }

        // Simple bubble sort by name
        for (int i = 1; i <= sortedList.getNumberOfEntries() - 1; i++) {
            for (int j = 1; j <= sortedList.getNumberOfEntries() - i; j++) {
                if (sortedList.getEntry(j).getName().compareToIgnoreCase(
                        sortedList.getEntry(j + 1).getName()) > 0) {
                    Patient temp = sortedList.getEntry(j);
                    sortedList.replace(j, sortedList.getEntry(j + 1));
                    sortedList.replace(j + 1, temp);
                }
            }
        }

        System.out.printf("%-15s %-20s %-15s%n", "Patient ID", "Name", "Phone");
        System.out.println("--------------------------------------------------");

        for (int i = 1; i <= sortedList.getNumberOfEntries(); i++) {
            Patient p = sortedList.getEntry(i);
            System.out.printf("%-15s %-20s %-15s%n",
                    p.getId(), p.getName(), p.getPhone());
        }
        System.out.println("==================================\n");
    }
}
