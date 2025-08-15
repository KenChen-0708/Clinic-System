//Ronaldo Loh Yu Fei
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
import utility.FormatUtil;

public class MaintainPatient {

    private final ListInterface<Patient> patientList;
    private final PatientDAO patientDAO;
    private final PatientUI patientUI;
    private final QueueInterface<Patient> patientQueue; // Line-up queue for patients
    private final MapInterface<String, Patient> patientMap; // Map for efficient patient lookup by ID
    private final MapInterface<String, Patient> icMap; // Map for efficient patient lookup by IC
    private final MapInterface<String, Boolean> queueMap; // Map for O(1) queue patient checks
    private final MapInterface<String, Patient> queueNameMap; // Map for O(1) queue search by name
    private final MapInterface<String, Patient> queueIcMap; // Map for O(1) queue search by IC

    public MaintainPatient() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.retrieveFromFile();
        patientUI = new PatientUI(this);
        patientQueue = new LinkedQueue<>(); // Initialize the line-up queue
        patientMap = new HashMap<>(); // Initialize patient ID map
        icMap = new HashMap<>(); // Initialize IC map
        queueMap = new HashMap<>(); // Initialize queue duplicate prevention map (PatientID -> true if in queue)
        queueNameMap = new HashMap<>(); // Initialize queue name search map
        queueIcMap = new HashMap<>(); // Initialize queue IC search map

        // Populate maps with existing patients for efficient lookup
        populateMaps();
    }

    private void populateMaps() {
        // Clear maps first to ensure clean state
        patientMap.clear();
        icMap.clear();

        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient patient = patientList.getEntry(i);
            if (patient != null) {
                patientMap.put(patient.getId(), patient);
                icMap.put(patient.getIcNumber(), patient);
            }
        }
    }

    // Unified method to update all data structures consistently
    private boolean updateAllDataStructures(Patient oldPatient, Patient newPatient) {
        // CRITICAL: Validate IC number uniqueness before update
        if (!oldPatient.getIcNumber().equals(newPatient.getIcNumber())) {
            // Check if new IC already exists for a different patient
            Patient existingPatient = icMap.get(newPatient.getIcNumber());
            if (existingPatient != null && !existingPatient.getId().equals(oldPatient.getId())) {
                // IC already exists for another patient - REJECT UPDATE
                return false;
            }
        }

        // Update list
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            if (patientList.getEntry(i).getId().equals(oldPatient.getId())) {
                patientList.replace(i, newPatient);
                break;
            }
        }

        // Update maps - remove old entries first
        if (oldPatient != null) {
            patientMap.remove(oldPatient.getId());
            icMap.remove(oldPatient.getIcNumber());
        }

        // Add new entries
        patientMap.put(newPatient.getId(), newPatient);
        icMap.put(newPatient.getIcNumber(), newPatient);

        // Save to file
        patientDAO.saveToFile(patientList);
        return true; // Success
    }

    // Unified method to add patient to all data structures
    private void addToAllDataStructures(Patient patient) {
        patientList.add(patient);
        patientMap.put(patient.getId(), patient);
        icMap.put(patient.getIcNumber(), patient);
        patientDAO.saveToFile(patientList);
    }

    // Unified method to remove patient from all data structures
    private void removeFromAllDataStructures(Patient patient) {
        // Remove from list
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            if (patientList.getEntry(i).getId().equals(patient.getId())) {
                patientList.remove(i);
                break;
            }
        }

        // Remove from maps
        patientMap.remove(patient.getId());
        icMap.remove(patient.getIcNumber());

        // Remove from queue map if present
        queueMap.remove(patient.getId());

        // Save to file
        patientDAO.saveToFile(patientList);
    }

    // Queue management methods with O(1) operations
    private void enqueuePatient(Patient patient) {
        patientQueue.enqueueWithNumber(patient);
        queueMap.put(patient.getId(), true); // O(1) patient tracking
        queueNameMap.put(patient.getName().toLowerCase(), patient); // O(1) name search
        queueIcMap.put(patient.getIcNumber(), patient); // O(1) IC search
    }

    private Patient dequeuePatient() {
        if (!patientQueue.isEmpty()) {
            Patient patient = patientQueue.dequeue();
            queueMap.remove(patient.getId()); // O(1) patient removal
            queueNameMap.remove(patient.getName().toLowerCase()); // O(1) name removal
            queueIcMap.remove(patient.getIcNumber()); // O(1) IC removal
            return patient;
        }
        return null;
    }

    private boolean isInQueue(Patient patient) {
        return queueMap.containsKey(patient.getId()); // O(1) check
    }

    private void clearAllQueueMaps() {
        patientQueue.clear();
        queueMap.clear(); // O(1) clear all patients
        queueNameMap.clear(); // O(1) clear name map
        queueIcMap.clear(); // O(1) clear IC map
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
                generateAgeDemographicsReport();
                break;
            case 4:
                generateRegistrationTimelineReport();
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
                    if (!isInQueue(existingPatient)) { // O(1) check
                        enqueuePatient(existingPatient); // O(1) add
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

                // Use unified method to ensure data consistency
                addToAllDataStructures(patient);
                patientUI.displayMessage("\nPatient registered successfully with ID: " + newId);

                // Enhanced registration with queue option:
                boolean joinQueue = patientUI.askToJoinQueue();
                if (joinQueue) {
                    enqueuePatient(patient); // O(1) efficient add
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
                    // Use unified method to ensure data consistency
                    removeFromAllDataStructures(patientToRemove);
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
        // Check if patient is in queue using O(1) method
        if (!isInQueue(patient)) {
            return false; // Patient not in queue
        }

        // Patient is in queue, need to remove it
        // Create temporary queue to traverse and rebuild without the patient
        QueueInterface<Patient> tempQueue = new LinkedQueue<>();
        boolean found = false;

        // Move patients to temp queue, excluding the patient to remove
        while (!patientQueue.isEmpty()) {
            Patient current = patientQueue.dequeue();
            if (current.equals(patient)) {
                found = true; // Don't add this patient to temp queue
                queueMap.remove(current.getId()); // O(1) patient removal
                queueNameMap.remove(current.getName().toLowerCase()); // O(1) name removal
                queueIcMap.remove(current.getIcNumber()); // O(1) IC removal
            } else {
                tempQueue.enqueue(current);
            }
        }

        // Restore queue without the removed patient
        while (!tempQueue.isEmpty()) {
            Patient p = tempQueue.dequeue();
            patientQueue.enqueue(p);
            // Note: patient map already has these patients, no need to re-add
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

            if (id == null || id.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            // Validate input
            if (id.trim().isEmpty()) {
                patientUI.displayMessage("\nInvalid input. Please enter a valid Patient ID.");
                continue;
            }

            Patient patient = patientMap.get(id); // Use map for O(1) lookup

            if (patient != null) {
                // Validate data consistency
                if (validatePatientConsistency(patient)) {
                    patientUI.displayPatient(patient);
                } else {
                    patientUI.displayMessage("\nData inconsistency detected. Refreshing data...");
                    populateMaps();
                    patient = patientMap.get(id);
                    if (patient != null) {
                        patientUI.displayPatient(patient);
                    } else {
                        patientUI.displayMessage("\nPatient with ID '" + id + "' not found.");
                    }
                }
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

            if (ic == null || ic.equals("0")) {
                patientUI.displayMessage("\nExiting search...");
                break;
            }

            // Validate input
            if (ic.trim().isEmpty()) {
                patientUI.displayMessage("\nInvalid input. Please enter a valid IC number.");
                continue;
            }

            Patient patient = icMap.get(ic); // Use map for O(1) lookup

            if (patient != null) {
                // Validate data consistency
                if (validatePatientConsistency(patient)) {
                    patientUI.displayPatient(patient);
                } else {
                    patientUI.displayMessage("\nData inconsistency detected. Refreshing data...");
                    populateMaps();
                    patient = icMap.get(ic);
                    if (patient != null) {
                        patientUI.displayPatient(patient);
                    } else {
                        String displayIC = ic.length() == 12 ?
                            ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8) : ic;
                        patientUI.displayMessage("Patient with IC '" + displayIC + "' not found.");
                    }
                }
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

                // Use unified method to ensure data consistency with IC validation
                boolean updateSuccess = updateAllDataStructures(patient, updatedPatient);

                if (updateSuccess) {
                    patientUI.displayMessage("\nPatient updated successfully.");
                } else {
                    // IC number conflict detected
                    String displayIC = updatedPatient.getIcNumber().length() == 12 ?
                        updatedPatient.getIcNumber().substring(0, 6) + "-" +
                        updatedPatient.getIcNumber().substring(6, 8) + "-" +
                        updatedPatient.getIcNumber().substring(8) : updatedPatient.getIcNumber();
                    patientUI.displayMessage("\nERROR: IC number " + displayIC + " already exists for another patient.");
                    patientUI.displayMessage("Update cancelled to prevent duplicate IC numbers.");
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
                if (!isInQueue(patient)) { // FAST O(1) check
                    enqueuePatient(patient); // FAST O(1) add with patient tracking
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
            Patient nextPatient = dequeuePatient(); // FAST O(1) removal with patient tracking
            if (nextPatient != null) {
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
            } else {
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
                clearAllQueueMaps(); // O(1) efficient clear
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

        // Use map values for more efficient iteration
        ListInterface<Patient> allPatients = patientMap.getValues();
        for (int i = 1; i <= allPatients.getNumberOfEntries(); i++) {
            Patient p = allPatients.getEntry(i);
            if (p != null && p.getName().toLowerCase().contains(name.toLowerCase())) {
                foundPatients.add(p);
            }
        }
        return foundPatients;
    }

    // Method removed - using icMap.get(ic) for O(1) lookup instead

    private Patient findPatientInQueueByName(String name) {
        // O(1) lookup using secondary queue name map
        return queueNameMap.get(name.toLowerCase());
    }

    private Patient findPatientInQueueByIC(String ic) {
        // O(1) lookup using secondary queue IC map
        String searchIC = ic.replaceAll("-", ""); // Remove dashes for consistent lookup
        return queueIcMap.get(searchIC);
    }

    private void simulatePatientQueue() {
        // Use custom LinkedQueue from ADT folder - business logic
        QueueInterface<Patient> simulationQueue = new LinkedQueue<>();

        // Add all patients to simulation queue - business logic
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            simulationQueue.enqueue(patientList.getEntry(i));
        }

        // Delegate to UI for display and simulation
        patientUI.displaySimulationResult(simulationQueue);
    }

    public ListInterface<Patient> getPatientList() {
        return patientList;
    }

    // Data consistency validation
    private boolean validatePatientConsistency(Patient patient) {
        if (patient == null) return false;

        // Check if patient exists in both maps
        Patient fromIdMap = patientMap.get(patient.getId());
        Patient fromIcMap = icMap.get(patient.getIcNumber());

        // Check if all references point to the same patient
        return fromIdMap != null && fromIcMap != null &&
               fromIdMap.equals(patient) && fromIcMap.equals(patient);
    }

    // Data integrity check method
    public void validateDataIntegrity() {
        int listSize = patientList.getNumberOfEntries();
        int idMapSize = patientMap.size();
        int icMapSize = icMap.size();

        if (listSize != idMapSize || listSize != icMapSize) {
            String warningMessage = "WARNING: Data inconsistency detected!\n" +
                                  "List size: " + listSize + ", ID Map size: " + idMapSize + ", IC Map size: " + icMapSize + "\n" +
                                  "Maps have been refreshed from the list.";
            populateMaps(); // Auto-fix by repopulating maps
            patientUI.displayMessage(warningMessage);
        }
    }

    private void generateAgeDemographicsReport() {
        // Calculate age demographics (business logic)
        int under18 = 0;
        int age18to30 = 0;
        int age31to50 = 0;
        int age51to65 = 0;
        int over65 = 0;

        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            int age = calculateAgeFromIC(p.getIcNumber());

            if (age < 18) {
                under18++;
            } else if (age <= 30) {
                age18to30++;
            } else if (age <= 50) {
                age31to50++;
            } else if (age <= 65) {
                age51to65++;
            } else {
                over65++;
            }
        }

        // Delegate to UI for display and user interaction
        patientUI.displayAgeDemographicsReport(patientList.getNumberOfEntries(), under18, age18to30, age31to50, age51to65, over65);
    }



    private void generateRegistrationTimelineReport() {
        if (patientList.getNumberOfEntries() == 0) {
            patientUI.displayMessage("\nNo patients registered yet.");
            patientUI.askToReturnToMainMenu();
            return;
        }

        // Sort patients by registration date (oldest first) - business logic
        ListInterface<Patient> sortedList = sortPatientsByDate();

        // Calculate statistics - business logic
        Patient firstPatient = sortedList.getEntry(1);
        Patient lastPatient = sortedList.getEntry(sortedList.getNumberOfEntries());

        int maleCount = 0, femaleCount = 0;
        for (int i = 1; i <= sortedList.getNumberOfEntries(); i++) {
            if (sortedList.getEntry(i).getGender().equalsIgnoreCase("Male")) {
                maleCount++;
            } else {
                femaleCount++;
            }
        }

        // Calculate monthly breakdown - business logic
        MapInterface<String, Integer> monthlyCount = new HashMap<>();
        for (int i = 1; i <= sortedList.getNumberOfEntries(); i++) {
            Patient p = sortedList.getEntry(i);
            String monthYear = p.getRegistrationDate().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));

            Integer count = monthlyCount.get(monthYear);
            if (count == null) {
                monthlyCount.put(monthYear, 1);
            } else {
                monthlyCount.put(monthYear, count + 1);
            }
        }

        // Delegate to UI for display
        patientUI.displayRegistrationTimelineReport(sortedList, firstPatient, lastPatient, maleCount, femaleCount, monthlyCount);
    }

    private ListInterface<Patient> sortPatientsByDate() {
        ListInterface<Patient> sortedList = new DoublyLinkedList<>();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            sortedList.add(patientList.getEntry(i));
        }

        // Bubble sort by registration date (oldest first)
        for (int i = 1; i <= sortedList.getNumberOfEntries() - 1; i++) {
            for (int j = 1; j <= sortedList.getNumberOfEntries() - i; j++) {
                if (sortedList.getEntry(j).getRegistrationDate().isAfter(
                        sortedList.getEntry(j + 1).getRegistrationDate())) {
                    Patient temp = sortedList.getEntry(j);
                    sortedList.replace(j, sortedList.getEntry(j + 1));
                    sortedList.replace(j + 1, temp);
                }
            }
        }
        return sortedList;
    }







    // Business logic methods moved from PatientUI
    public boolean isValidMalaysianIC(String ic) {
        if (ic.length() != 12) return false;
        if (!ic.matches("\\d{12}")) return false;

        // Extract birth date components
        String year = ic.substring(0, 2);
        String month = ic.substring(2, 4);
        String day = ic.substring(4, 6);

        int monthInt = Integer.parseInt(month);
        int dayInt = Integer.parseInt(day);

        // Basic validation for month and day
        if (monthInt < 1 || monthInt > 12) return false;
        if (dayInt < 1 || dayInt > 31) return false;

        // Check place of birth (7th and 8th digits) - should be valid state codes
        String placeCode = ic.substring(6, 8);
        int placeInt = Integer.parseInt(placeCode);

        // Valid Malaysian state codes: 01-16, 21-59, 82-83
        if ((placeInt >= 1 && placeInt <= 16) ||
            (placeInt >= 21 && placeInt <= 59) ||
            (placeInt >= 82 && placeInt <= 83)) {
            return true;
        }

        return false;
    }

    public Patient createPatient(String name, String icNumber, String phone, String gender) {
        return new Patient(name, icNumber, phone, gender);
    }

    public Patient createPatientWithId(String id, String name, String icNumber, String phone, String gender, java.time.LocalDate regDate) {
        return new Patient(id, name, icNumber, phone, gender, regDate);
    }

    // Validation methods moved from PatientUI
    public boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z\\s]+") && name.length() >= 2;
    }

    public boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return cleanPhone.matches("\\d{10,11}") &&
               (cleanPhone.startsWith("01") || cleanPhone.startsWith("03") ||
                cleanPhone.startsWith("04") || cleanPhone.startsWith("05") ||
                cleanPhone.startsWith("06") || cleanPhone.startsWith("07") ||
                cleanPhone.startsWith("08") || cleanPhone.startsWith("09"));
    }

    public String normalizeGender(String gender) {
        if (gender == null) return null;
        String normalized = gender.trim().toLowerCase();
        if (normalized.equals("m") || normalized.equals("male")) {
            return "Male";
        } else if (normalized.equals("f") || normalized.equals("female")) {
            return "Female";
        }
        return null; // Invalid gender
    }

    public String formatPhoneForStorage(String phone) {
        if (phone == null) return phone;
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        if (cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 3) + "-" + cleanPhone.substring(3, 7) + "-" + cleanPhone.substring(7);
        } else if (cleanPhone.length() == 11) {
            return cleanPhone.substring(0, 3) + "-" + cleanPhone.substring(3, 7) + "-" + cleanPhone.substring(7);
        }
        return phone;
    }

    public int calculateAgeFromIC(String icNumber) {
        if (icNumber.length() != 12) return 0;

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
    }

    public String getQueueTableData(QueueInterface<Patient> queue) {
        StringBuilder tableData = new StringBuilder();
        tableData.append("\n--- QUEUE TABLE ---\n");

        // Table header
        tableData.append(String.format("%-8s %-8s %-20s %-8s %-16s %-16s%n",
                         "Queue#", "ID", "Name", "Gender", "IC Number", "Phone Number"));
        tableData.append("================================================================================\n");

        // Create temporary queue to traverse without modifying original
        QueueInterface<Patient> tempQueue = new LinkedQueue<>();
        int position = 1;

        // Move patients to temp queue while building data
        while (!queue.isEmpty()) {
            Patient p = queue.dequeue();
            String queueNum = queue.getQueueNumber(p);
            if (queueNum == null) {
                queueNum = String.format("%04d", position);
            }

            tableData.append(String.format("%-8s %-8s %-20s %-8s %-16s %-16s%n",
                             queueNum,
                             p.getId(),
                             FormatUtil.truncateString(p.getName(), 20),
                             p.getGender(),
                             FormatUtil.formatICForDisplay(p.getIcNumber()),
                             p.getPhone()));

            tempQueue.enqueue(p);
            position++;
        }

        // Restore original queue
        while (!tempQueue.isEmpty()) {
            queue.enqueue(tempQueue.dequeue());
        }

        tableData.append("================================================================================");
        return tableData.toString();
    }

    public String getSimulationData(QueueInterface<Patient> simulationQueue) {
        StringBuilder simulationData = new StringBuilder();
        simulationData.append("\n--- Simulating Patient Queue ---\n");
        simulationData.append("Total patients to serve: ").append(simulationQueue.size()).append("\n");

        int servedCount = 0;
        while (!simulationQueue.isEmpty()) {
            Patient current = simulationQueue.dequeue();
            servedCount++;
            simulationData.append(servedCount).append(". Now serving: ")
                          .append(current.getName()).append(" (ID: ")
                          .append(current.getId()).append(")\n");
        }

        simulationData.append("All ").append(servedCount).append(" patients have been served.");
        return simulationData.toString();
    }
}
