
package control;

import adt.DoublyLinkedList;
import adt.LinkedQueue;
import adt.ListInterface;
import adt.QueueInterface;
import adt.HashMap;
import adt.MapInterface;
import boundary.PatientUI;
import dao.PatientDAO;
import entity.Patient;
import utility.MessageUI;

public class MaintainPatient {

    private final ListInterface<Patient> patientList;
    private final PatientDAO patientDAO;
    private final PatientUI patientUI;
    private final QueueInterface<Patient> patientQueue; // Line-up queue for patients
    private final MapInterface<String, Patient> patientMap; // Map for efficient patient lookup by ID
    private final MapInterface<String, Patient> icMap; // Map for efficient patient lookup by IC

    public MaintainPatient() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.retrieveFromFile();
        patientUI = new PatientUI(this);
        patientQueue = new LinkedQueue<>(); // Initialize the line-up queue
        patientMap = new HashMap<>(); // Initialize patient ID map
        icMap = new HashMap<>(); // Initialize IC map

        // Populate maps with existing patients for efficient lookup
        populateMaps();
    }

    private void populateMaps() {
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient patient = patientList.getEntry(i);
            patientMap.put(patient.getId(), patient);
            icMap.put(patient.getIcNumber(), patient);
        }
    }

    public void runPatientMenu() {
        patientUI.run();
    }

    public void handleMenuChoice(int choice) {
        switch (choice) {
            case 1:
                managePatientData(); // Dedicated Patient Management Menu
                break;
            case 2:
                managePatientQueue(); // Dedicated Queue Management Menu
                break;
            case 3:
                generatePatientStatisticsReport();
                break;
            case 4:
                generatePatientContactReport();
                break;
            case 0:
                MessageUI.displayExitMessage();
                break;
            default:
                MessageUI.displayInvalidChoiceMessage();
        }
    }

    private void managePatientData() {
        int choice;
        do {
            choice = patientUI.displayPatientManagementMenu();
            switch (choice) {
                case 1:
                    registerNewPatient();
                    break;
                case 2:
                    viewAllPatients();
                    break;
                case 3:
                    searchPatientMenu();
                    break;
                case 4:
                    updatePatient();
                    break;
                case 5:
                    removePatient();
                    break;
                case 0:
                    patientUI.displayMessage("Returning to Main Patient Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    private void searchPatientMenu() {
        int choice;
        do {
            choice = patientUI.displaySearchPatientMenu();
            switch (choice) {
                case 1:
                    searchPatientById();
                    break;
                case 2:
                    searchPatientByName();
                    break;
                case 3:
                    searchPatientByIC();
                    break;
                case 0:
                    patientUI.displayMessage("\nReturning to Patient Management Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    private void registerNewPatient() {
        boolean continueRegistering = true;

        while (continueRegistering) {
            // First, get IC number and check if user wants to exit
            String icNumber = patientUI.inputICForRegistration();

            if (icNumber.equals("0")) {
                patientUI.displayMessage("\nExiting patient registration...\n");
                break;
            }

            // Check if patient already exists (by IC number)
            Patient existingPatient = icMap.get(icNumber); // Use map for O(1) lookup

            if (existingPatient != null) {
                // Patient already exists - show existing patient details
                patientUI.displayMessage("\n=== PATIENT ALREADY EXISTS ===");
                patientUI.displayPatient(existingPatient);

                // Ask if they want to add existing patient to queue
                boolean addToQueue = patientUI.askToAddExistingPatientToQueue(existingPatient.getName());
                if (addToQueue) {
                    if (!patientQueue.contains(existingPatient)) {
                        patientQueue.enqueueWithNumber(existingPatient);
                        String queueNum = patientQueue.getQueueNumber(existingPatient);
                        patientUI.displayMessage("\nExisting patient " + existingPatient.getName() + " has been added to the line-up queue.");
                        patientUI.displayMessage("Queue Number: " + queueNum);
                        patientUI.displayMessage("Total patients in queue: " + patientQueue.size() + "\n");
                    } else {
                        patientUI.displayMessage("\nPatient is already in the queue.\n");
                    }
                } else {
                    patientUI.displayMessage("\nPatient not added to queue.\n");
                }
            } else {
                // New patient - continue with registration
                patientUI.displayMessage("\n=== NEW PATIENT REGISTRATION ===");
                Patient patient = patientUI.inputRemainingPatientDetails(icNumber);

                // Auto-generate patient ID
                String newId = generateNextPatientId();
                patient.setId(newId);

                patientList.add(patient);
                patientMap.put(newId, patient); // Add to ID map
                icMap.put(patient.getIcNumber(), patient); // Add to IC map
                patientDAO.saveToFile(patientList);
                patientUI.displayMessage("\nPatient registered successfully with ID: " + newId);

                // Enhanced registration with queue option:
                boolean joinQueue = patientUI.askToJoinQueue();
                if (joinQueue) {
                    patientQueue.enqueueWithNumber(patient);
                    String queueNum = patientQueue.getQueueNumber(patient);
                    patientUI.displayMessage("\nPatient " + patient.getName() + " has been added to the line-up queue.");
                    patientUI.displayMessage("Queue Number: " + queueNum);
                    patientUI.displayMessage("Total patients in queue: " + patientQueue.size() + "\n");
                } else {
                    patientUI.displayMessage("\nPatient not added to queue.\n");
                }
            }

            continueRegistering = patientUI.askToContinueOperation("register another patient");
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
        return String.format("P%04d", max + 1);
    }

    private void removePatient() {
        boolean continueRemoving = true;

        while (continueRemoving) {
            String id = patientUI.inputPatientId();

            if (id.equals("0")) {
                patientUI.displayMessage("\nExiting remove patient...");
                break;
            }

            Patient patientToRemove = patientMap.get(id); // Use map for O(1) lookup

            if (patientToRemove != null) {
                // Show patient details and ask for confirmation
                patientUI.displayMessage("Patient to be removed:");
                patientUI.displayPatient(patientToRemove);

                boolean confirmed = patientUI.confirmRemovePatient(patientToRemove.getName());

                if (confirmed) {
                    // Remove from list
                    for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
                        if (patientList.getEntry(i).getId().equals(id)) {
                            patientList.remove(i);
                            break;
                        }
                    }

                    // Remove from maps
                    patientMap.remove(id);
                    icMap.remove(patientToRemove.getIcNumber());

                    patientDAO.saveToFile(patientList);
                    patientUI.displayMessage("\nPatient removed successfully.");

                    // Also remove from queue if present
                    if (removePatientFromQueue(patientToRemove)) {
                        patientUI.displayMessage("Patient also removed from line-up queue.");
                    }
                } else {
                    patientUI.displayMessage("\nPatient removal cancelled.");
                }
            } else {
                patientUI.displayMessage("\nPatient with ID '" + id + "' not found.\n");
            }

            continueRemoving = patientUI.askToContinueOperation("remove another patient");
        }
    }

    private boolean removePatientFromQueue(Patient patient) {
        // Create temporary queue to traverse and rebuild without the patient
        QueueInterface<Patient> tempQueue = new LinkedQueue<>();
        boolean found = false;

        // Move patients to temp queue, excluding the patient to remove
        while (!patientQueue.isEmpty()) {
            Patient current = patientQueue.dequeue();
            if (current.equals(patient)) {
                found = true; // Don't add this patient to temp queue
            } else {
                tempQueue.enqueue(current);
            }
        }

        // Restore queue without the removed patient
        while (!tempQueue.isEmpty()) {
            patientQueue.enqueue(tempQueue.dequeue());
        }

        return found;
    }

    private void viewAllPatients() {
        if (patientList.getNumberOfEntries() == 0) {
            patientUI.displayMessage("No patients registered yet.");
            patientUI.askToContinueOrReturnToMenu();
            return;
        }

        // Sort patients by registration date (earliest first)
        ListInterface<Patient> sortedList = sortPatientsByRegistrationDate();

        // Display in table format
        patientUI.displayPatientsTable(sortedList);
        patientUI.askToContinueOrReturnToMenu();
    }

    private ListInterface<Patient> sortPatientsByRegistrationDate() {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = new DoublyLinkedList<>();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            sortedList.add(patientList.getEntry(i));
        }

        // Simple bubble sort by registration date (earliest first)
        for (int i = 1; i <= sortedList.getNumberOfEntries() - 1; i++) {
            for (int j = 1; j <= sortedList.getNumberOfEntries() - i; j++) {
                Patient current = sortedList.getEntry(j);
                Patient next = sortedList.getEntry(j + 1);

                // Compare registration dates (earlier dates come first)
                if (current.getRegistrationDate().isAfter(next.getRegistrationDate())) {
                    sortedList.replace(j, next);
                    sortedList.replace(j + 1, current);
                }
            }
        }

        return sortedList;
    }

    private void searchPatientById() {
        boolean continueSearching = true;

        while (continueSearching) {
            String id = patientUI.inputPatientId();

            if (id.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            Patient patient = patientMap.get(id); // Use map for O(1) lookup

            if (patient != null) {
                patientUI.displayPatient(patient);
            } else {
                patientUI.displayMessage("\nPatient with ID '" + id + "' not found.");
            }

            continueSearching = patientUI.askToContinueOperation("search another patient by ID");
        }
    }

    private void searchPatientByName() {
        boolean continueSearching = true;

        while (continueSearching) {
            String name = patientUI.inputPatientName();

            if (name.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            ListInterface<Patient> foundPatients = findPatientsByName(name);

            if (foundPatients.getNumberOfEntries() > 0) {
                patientUI.displayMessage("\nFound " + foundPatients.getNumberOfEntries() + " patient(s) with name '" + name + "':");
                for (int i = 1; i <= foundPatients.getNumberOfEntries(); i++) {
                    patientUI.displayMessage("\n--- Patient " + i + " ---");
                    patientUI.displayPatient(foundPatients.getEntry(i));
                }
            } else {
                patientUI.displayMessage("No patients found with name '" + name + "'.");
            }

            continueSearching = patientUI.askToContinueOperation("search another patient by name");
        }
    }

    private void searchPatientByIC() {
        boolean continueSearching = true;

        while (continueSearching) {
            String ic = patientUI.inputPatientIC();

            if (ic.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            Patient patient = icMap.get(ic); // Use map for O(1) lookup

            if (patient != null) {
                patientUI.displayPatient(patient);
            } else {
                // Format IC for display in error message
                String displayIC = ic.length() == 12 ?
                    ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8) : ic;
                patientUI.displayMessage("Patient with IC '" + displayIC + "' not found.");
            }

            continueSearching = patientUI.askToContinueOperation("search another patient by IC");
        }
    }

    private void updatePatient() {
        boolean continueUpdating = true;

        while (continueUpdating) {
            String id = patientUI.inputPatientId();

            if (id.equals("0")) {
                patientUI.displayMessage("\nExiting update patient...");
                break;
            }

            Patient patient = patientMap.get(id); // Use map for O(1) lookup

            if (patient != null) {
                patientUI.displayMessage("Current patient details:");
                patientUI.displayPatient(patient);

                Patient updatedPatient = patientUI.inputUpdatedPatientDetails(patient);

                // Update maps if IC changed
                if (!patient.getIcNumber().equals(updatedPatient.getIcNumber())) {
                    icMap.remove(patient.getIcNumber());
                    icMap.put(updatedPatient.getIcNumber(), updatedPatient);
                }
                patientMap.put(id, updatedPatient);

                // Find and replace the patient in the list
                for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
                    if (patientList.getEntry(i).getId().equals(id)) {
                        patientList.replace(i, updatedPatient);
                        patientDAO.saveToFile(patientList);
                        patientUI.displayMessage("\nPatient updated successfully.");
                        break;
                    }
                }
            } else {
                patientUI.displayMessage("Patient with ID '" + id + "' not found.");
            }

            continueUpdating = patientUI.askToContinueOperation("update another patient");
        }
    }

    private void managePatientQueue() {
        int choice;
        do {
            choice = patientUI.displayQueueManagementMenu();
            switch (choice) {
                case 1:
                    addPatientToQueue();
                    break;
                case 2:
                    serveNextPatient();
                    break;
                case 3:
                    viewCurrentQueue();
                    break;
                case 4:
                    searchPatientInQueueMenu();
                    break;
                case 5:
                    simulatePatientQueue();
                    break;
                case 6:
                    clearQueue();
                    break;
                case 0:
                    patientUI.displayMessage("Returning to Patient Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    private void addPatientToQueue() {
        boolean continueAdding = true;

        while (continueAdding) {
            String id = patientUI.inputPatientId();

            if (id.equals("0")) {
                patientUI.displayMessage("Exiting add to queue...");
                break;
            }

            Patient patient = patientMap.get(id); // Use map for O(1) lookup

            if (patient != null) {
                if (!patientQueue.contains(patient)) {
                    patientQueue.enqueueWithNumber(patient);
                    String queueNum = patientQueue.getQueueNumber(patient);
                    patientUI.displayMessage("\nPatient " + patient.getName() + " added to line-up queue.");
                    patientUI.displayMessage("Queue Number: " + queueNum);
                    patientUI.displayMessage("Total patients in queue: " + patientQueue.size());
                } else {
                    patientUI.displayMessage("Patient is already in the queue.");
                }
            } else {
                patientUI.displayMessage("\nPatient ID not found.");
            }

            continueAdding = patientUI.askToContinueOperation("add another patient to queue");
        }
    }

    private void serveNextPatient() {
        boolean continueServing = true;

        while (continueServing && !patientQueue.isEmpty()) {
            Patient nextPatient = patientQueue.dequeue();
            String queueNum = patientQueue.getQueueNumber(nextPatient);
            if (queueNum != null) {
                patientUI.displayMessage("\nNow serving: Queue #" + queueNum +
                                       " - " + nextPatient.getName() + " (ID: " + nextPatient.getId() + ")");
            } else {
                patientUI.displayMessage("\nNow serving: " + nextPatient.getName() + " (ID: " + nextPatient.getId() + ")");
            }
            patientUI.displayMessage("Remaining patients in queue: " + patientQueue.size());

            if (!patientQueue.isEmpty()) {
                continueServing = patientUI.askToContinueOperation("serve next patient");
            } else {
                patientUI.displayMessage("All patients have been served. Queue is now empty.");
                continueServing = false;
            }
        }

        if (patientQueue.isEmpty() && continueServing) {
            patientUI.displayMessage("\nNo patients in the queue.");
        }
    }

    private void viewCurrentQueue() {
        if (patientQueue.isEmpty()) {
            patientUI.displayMessage("\nNo patients in the queue.");
        } else {
            patientUI.displayMessage("\n=== CURRENT LINE-UP QUEUE ===");
            patientUI.displayMessage("Total patients waiting: " + patientQueue.size());
            Patient nextPatient = patientQueue.peek();
            String queueNum = patientQueue.getQueueNumber(nextPatient);
            if (queueNum != null) {
                patientUI.displayMessage("Next to serve: Queue #" + queueNum +
                                       " - " + nextPatient.getName() +
                                       " (ID: " + nextPatient.getId() + ")");
            } else {
                patientUI.displayMessage("Next to serve: " + nextPatient.getName() +
                                       " (ID: " + nextPatient.getId() + ")");
            }

            // Display queue in table format
            patientUI.displayQueueTable(patientQueue);
        }
        patientUI.askToContinueOrReturnToMenu();
    }

    private void searchPatientInQueueMenu() {
        int choice;
        do {
            choice = patientUI.displaySearchInQueueMenu();
            switch (choice) {
                case 1:
                    searchPatientInQueueByName();
                    break;
                case 2:
                    searchPatientInQueueByIC();
                    break;
                case 0:
                    patientUI.displayMessage("Returning to Queue Management Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    private void searchPatientInQueueByName() {
        boolean continueSearching = true;

        while (continueSearching) {
            String name = patientUI.inputPatientName();

            if (name.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            Patient foundPatient = findPatientInQueueByName(name);

            if (foundPatient != null) {
                patientUI.displayMessage("\nPatient FOUND in queue:");
                String queueNum = patientQueue.getQueueNumber(foundPatient);
                if (queueNum != null) {
                    patientUI.displayMessage("Queue Number: " + queueNum);
                }
                patientUI.displayMessage("Name: " + foundPatient.getName());
                patientUI.displayMessage("ID: " + foundPatient.getId());
                patientUI.displayMessage("IC: " + foundPatient.getIcNumber());
                int position = patientQueue.getPosition(foundPatient);
                patientUI.displayMessage("Queue Position: " + position);
            } else {
                patientUI.displayMessage("No patient with name '" + name + "' found in the queue.");
            }

            continueSearching = patientUI.askToContinueOperation("search another patient by name");
        }
    }

    private void searchPatientInQueueByIC() {
        boolean continueSearching = true;

        while (continueSearching) {
            String ic = patientUI.inputPatientIC();

            if (ic.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            Patient foundPatient = findPatientInQueueByIC(ic);

            if (foundPatient != null) {
                patientUI.displayMessage("\nPatient FOUND in queue:");
                String queueNum = patientQueue.getQueueNumber(foundPatient);
                if (queueNum != null) {
                    patientUI.displayMessage("Queue Number: " + queueNum);
                }
                patientUI.displayMessage("Name: " + foundPatient.getName());
                patientUI.displayMessage("ID: " + foundPatient.getId());
                patientUI.displayMessage("IC: " + foundPatient.getIcNumber());
                int position = patientQueue.getPosition(foundPatient);
                patientUI.displayMessage("Queue Position: " + position);
            } else {
                String displayIC = ic.length() == 12 ?
                    ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8) : ic;
                patientUI.displayMessage("No patient with IC '" + displayIC + "' found in the queue.");
            }

            continueSearching = patientUI.askToContinueOperation("search another patient by IC");
        }
    }

    private void clearQueue() {
        if (patientQueue.isEmpty()) {
            patientUI.displayMessage("\nQueue is already empty.");
        } else {
            boolean confirm = patientUI.confirmClearQueue();
            if (confirm) {
                int clearedCount = patientQueue.size();
                patientQueue.clear();
                patientUI.displayMessage("Queue cleared successfully. " + clearedCount + " patients removed.");
                patientUI.displayMessage("Queue numbers will restart from 0001 for new entries.");
            } else {
                patientUI.displayMessage("Queue clear cancelled.");
            }
        }
        patientUI.askToContinueOrReturnToMenu();
    }

    // Method removed - using patientMap.get(id) for O(1) lookup instead

    private ListInterface<Patient> findPatientsByName(String name) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            if (p.getName().toLowerCase().contains(name.toLowerCase())) {
                foundPatients.add(p);
            }
        }
        return foundPatients;
    }

    // Method removed - using icMap.get(ic) for O(1) lookup instead

    private Patient findPatientInQueueByName(String name) {
        // Create temporary queue to traverse without modifying original
        QueueInterface<Patient> tempQueue = new LinkedQueue<>();
        Patient foundPatient = null;

        // Move patients to temp queue while searching
        while (!patientQueue.isEmpty()) {
            Patient current = patientQueue.dequeue();
            if (current.getName().equalsIgnoreCase(name)) {
                foundPatient = current;
            }
            tempQueue.enqueue(current);
        }

        // Restore original queue
        while (!tempQueue.isEmpty()) {
            patientQueue.enqueue(tempQueue.dequeue());
        }

        return foundPatient;
    }

    private Patient findPatientInQueueByIC(String ic) {
        // Create temporary queue to traverse without modifying original
        QueueInterface<Patient> tempQueue = new LinkedQueue<>();
        Patient foundPatient = null;
        String searchIC = ic.replaceAll("-", "");

        // Move patients to temp queue while searching
        while (!patientQueue.isEmpty()) {
            Patient current = patientQueue.dequeue();
            String patientIC = current.getIcNumber().replaceAll("-", "");
            if (patientIC.equalsIgnoreCase(searchIC)) {
                foundPatient = current;
            }
            tempQueue.enqueue(current);
        }

        // Restore original queue
        while (!tempQueue.isEmpty()) {
            patientQueue.enqueue(tempQueue.dequeue());
        }

        return foundPatient;
    }

    private void simulatePatientQueue() {
        // Use custom LinkedQueue from ADT folder
        QueueInterface<Patient> simulationQueue = new LinkedQueue<>();

        // Add all patients to simulation queue
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            simulationQueue.enqueue(patientList.getEntry(i));
        }

        System.out.println("\n--- Simulating Patient Queue ---");
        System.out.println("Total patients to serve: " + simulationQueue.size());

        int servedCount = 0;
        while (!simulationQueue.isEmpty()) {
            Patient current = simulationQueue.dequeue();
            servedCount++;
            System.out.println(servedCount + ". Now serving: " + current.getName() + " (ID: " + current.getId() + ")");
        }

        System.out.println("All " + servedCount + " patients have been served.");
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
