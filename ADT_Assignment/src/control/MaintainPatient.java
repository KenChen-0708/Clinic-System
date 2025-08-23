// Ronaldo Loh Yu Fei
package control;

import adt.DoublyLinkedList;
import adt.LinkedQueue;
import adt.ListInterface;
import adt.QueueInterface;
import adt.HashMap;
import adt.MapInterface;
import boundary.PatientUI;
import dao.PatientDAO;
import entity.Doctor;
import entity.Patient;
import utility.ValidationUtil;
import java.util.Comparator;

public class MaintainPatient {

    private final ListInterface<Patient> patientList;
    private final PatientDAO patientDAO;
    private final MaintainTreatment maintainTreatment;

    private final PatientUI patientUI = new PatientUI(this);

    private final QueueInterface<Patient> patientQueue;
    private final MapInterface<String, Patient> patientMap;
    private final MapInterface<String, Patient> icMap;
    private final MapInterface<String, Patient> nameMap;  // O(1) exact name lookup
    private final MapInterface<String, Boolean> queueMap;
    private final MapInterface<String, Patient> queueNameMap;
    private final MapInterface<String, Patient> queueIcMap;

    /**
     * Initialize all data structures and load existing data
     */
    public MaintainPatient() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.retrieveFromFile();
        patientQueue = new LinkedQueue<>();
        patientMap = new HashMap<>();
        icMap = new HashMap<>();
        nameMap = new HashMap<>();
        queueMap = new HashMap<>();
        queueNameMap = new HashMap<>();
        queueIcMap = new HashMap<>();
        maintainTreatment = new MaintainTreatment();

        // Populate maps with existing patients for efficient lookup
        populateMaps();
    }

    // Populate all HashMap indexes using iterator for O(n) traversal
    private void populateMaps() {
        patientMap.clear();
        icMap.clear();
        nameMap.clear();

        // Use iterator for efficient traversal of DoublyLinkedList
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
                patientMap.put(patient.getId(), patient);
                icMap.put(patient.getIcNumber(), patient);
                nameMap.put(patient.getName().toLowerCase(), patient);  // Case-insensitive exact name lookup
            }
        }
    }

    /**
     * Unified method to update all data structures consistently Ensures data
     * integrity across list, maps, and file storage
     *
     * @param oldPatient the patient to be updated
     * @param newPatient the updated patient data
     * @return true if update successful, false if IC conflict detected
     */
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

        // Update list using iterator for efficient traversal
        java.util.Iterator<Patient> iterator = patientList.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(oldPatient.getId())) {
                patientList.replace(index, newPatient);
                break;
            }
            index++;
        }

        // Update all maps - remove old entries first
        if (oldPatient != null) {
            patientMap.remove(oldPatient.getId());
            icMap.remove(oldPatient.getIcNumber());
            nameMap.remove(oldPatient.getName().toLowerCase());  // Remove old name mapping
        }

        // Add new entries to all maps
        if (newPatient.getId() != null && newPatient.getIcNumber() != null && newPatient.getName() != null) {
            patientMap.put(newPatient.getId(), newPatient);
            icMap.put(newPatient.getIcNumber(), newPatient);
            nameMap.put(newPatient.getName().toLowerCase(), newPatient);  // Add new name mapping
        }

        // Save to file
        patientDAO.saveToFile(patientList);
        return true; // Success
    }

    // Unified method to add patient to all data structures
    private void addToAllDataStructures(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
            patientList.add(patient);
            patientMap.put(patient.getId(), patient);
            icMap.put(patient.getIcNumber(), patient);
            nameMap.put(patient.getName().toLowerCase(), patient);  // Add to name map
            patientDAO.saveToFile(patientList);
        }
    }

    // Unified method to remove patient from all data structures
    private void removeFromAllDataStructures(Patient patient) {
        // Remove from list using iterator for efficient traversal
        java.util.Iterator<Patient> iterator = patientList.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(patient.getId())) {
                patientList.remove(index);
                break;
            }
            index++;
        }

        // Remove from all maps
        patientMap.remove(patient.getId());
        icMap.remove(patient.getIcNumber());
        nameMap.remove(patient.getName().toLowerCase());  // Remove from name map

        // Remove from queue map if present
        queueMap.remove(patient.getId());

        // Save to file
        patientDAO.saveToFile(patientList);
    }

    // Queue management methods with O(1) operations
    private void enqueuePatient(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getName() != null && patient.getIcNumber() != null) {
            patientQueue.enqueueWithNumber(patient);
            queueMap.put(patient.getId(), true); // O(1) patient tracking
            queueNameMap.put(patient.getName().toLowerCase(), patient); // O(1) name search
            queueIcMap.put(patient.getIcNumber(), patient); // O(1) IC search
        }
    }

    private Patient dequeuePatient() {
        if (!patientQueue.isEmpty()) {
            Patient patient = patientQueue.dequeue();
            if (patient != null) {
                if (patient.getId() != null) {
                    queueMap.remove(patient.getId()); // O(1) patient removal
                }
                if (patient.getName() != null) {
                    queueNameMap.remove(patient.getName().toLowerCase()); // O(1) name removal
                }
                if (patient.getIcNumber() != null) {
                    queueIcMap.remove(patient.getIcNumber()); // O(1) IC removal
                }
            }
            return patient;
        }
        return null;
    }

    private boolean isInQueue(Patient patient) {
        return patient != null && patient.getId() != null && queueMap.containsKey(patient.getId()); // O(1) check
    }

    private void clearAllQueueMaps() {
        patientQueue.clear();
        queueMap.clear(); // O(1) clear all patients
        queueNameMap.clear(); // O(1) clear name map
        queueIcMap.clear(); // O(1) clear IC map
    }

    /**
     * Run patient menu
     */
    public void runPatientMenu() {
        patientUI.run();
    }

    /**
     * Add patient to queue by ID
     *
     * @param patientId ID of patient to add
     * @return QueueOperationResult with status and data
     */
    public QueueOperationResult addPatientToQueueById(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return new QueueOperationResult(false, "INVALID_ID", null, null, 0);
        }

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return new QueueOperationResult(false, "PATIENT_NOT_FOUND", null, null, 0);
        }

        if (isPatientInQueue(patient)) {
            return new QueueOperationResult(false, "ALREADY_IN_QUEUE", patient, null, getQueueSize());
        }

        String queueNum = addPatientToQueue(patient);
        if (queueNum != null) {
            return new QueueOperationResult(true, "SUCCESS", patient, queueNum, getQueueSize());
        }

        return new QueueOperationResult(false, "OPERATION_FAILED", patient, null, getQueueSize());
    }

    /**
     * Serve next patient in queue
     *
     * @return QueueOperationResult with served patient data
     */
    public QueueOperationResult serveNextPatient() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, "QUEUE_EMPTY", null, null, 0);
        }

        // Use the helper method that properly cleans up all tracking maps
        Patient nextPatient = dequeuePatient();
        if (nextPatient != null) {
            //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
            MaintainDoctor maintainDoctor = new MaintainDoctor();
            //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
            Doctor availableDoctor = maintainDoctor.findAvailableDoctor();

            //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
            if (availableDoctor != null) {
                //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
                availableDoctor.setAvailability("On Duty");
                //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
                maintainDoctor.updateDoctorInList(availableDoctor);

                // Set serving patient for treatment module
                if (maintainTreatment != null) {
                    //Siuyee new changes - use new ADT queue system to store multiple serving patients and doctors
                    maintainTreatment.addServingPatientDoctorPair(nextPatient, availableDoctor);
                }

                String queueNum = patientQueue.getQueueNumber(nextPatient);
                return new QueueOperationResult(true, "PATIENT_SERVED", nextPatient, queueNum, patientQueue.size());

                //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
            } else {
                //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
                enqueuePatient(nextPatient);
                //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
                return new QueueOperationResult(false, "NO_AVAILABLE_DOCTOR", null, null, patientQueue.size());
            }
        }
        return new QueueOperationResult(false, "OPERATION_FAILED", null, null, patientQueue.size());
    }

    /**
     * Get current queue status
     *
     * @return QueueStatusData with queue information
     */
    public QueueStatusData getCurrentQueueStatus() {
        if (patientQueue.isEmpty()) {
            return new QueueStatusData(0, null, null);
        }

        Patient nextPatient = patientQueue.peek();
        String queueNum = patientQueue.getQueueNumber(nextPatient);

        return new QueueStatusData(patientQueue.size(), nextPatient, queueNum);
    }

    /**
     * Queue status data transfer object
     */
    public static class QueueStatusData {

        private final int totalWaiting;
        private final Patient nextPatient;
        private final String nextQueueNumber;

        public QueueStatusData(int totalWaiting, Patient nextPatient, String nextQueueNumber) {
            this.totalWaiting = totalWaiting;
            this.nextPatient = nextPatient;
            this.nextQueueNumber = nextQueueNumber;
        }

        public int getTotalWaiting() {
            return totalWaiting;
        }

        public Patient getNextPatient() {
            return nextPatient;
        }

        public String getNextQueueNumber() {
            return nextQueueNumber;
        }

        public boolean isEmpty() {
            return totalWaiting == 0;
        }
    }

    /**
     * Search patient in queue by name
     *
     * @param name Patient name to search
     * @return SearchResult with patient data
     */
    public SearchResult searchPatientInQueueByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new SearchResult(false, null, null, -1);
        }

        // Use existing method
        Patient foundPatient = findPatientInQueueByName(name);
        if (foundPatient != null) {
            String queueNum = patientQueue.getQueueNumber(foundPatient);
            int position = patientQueue.getPosition(foundPatient);
            return new SearchResult(true, foundPatient, queueNum, position);
        }

        return new SearchResult(false, null, null, -1);
    }

    /**
     * Search patient in queue by IC
     *
     * @param ic Patient IC to search
     * @return SearchResult with patient data
     */
    public SearchResult searchPatientInQueueByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return new SearchResult(false, null, null, -1);
        }

        // Use existing method
        Patient foundPatient = findPatientInQueueByIC(ic);
        if (foundPatient != null) {
            String queueNum = patientQueue.getQueueNumber(foundPatient);
            int position = patientQueue.getPosition(foundPatient);
            return new SearchResult(true, foundPatient, queueNum, position);
        }

        return new SearchResult(false, null, null, -1);
    }

    /**
     * Clear queue
     *
     * @return QueueOperationResult with clear operation data
     */
    public QueueOperationResult clearQueueLogic() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, "QUEUE_ALREADY_EMPTY", null, null, 0);
        }

        int clearedCount = patientQueue.size();
        clearAllQueueMaps();
        return new QueueOperationResult(true, "QUEUE_CLEARED", null, null, clearedCount);
    }

    /**
     * Get queue table data for display using completely safe approach
     * Uses queue tracking maps to avoid queue destruction
     * @return queue data formatted for table display
     */
    public String[][] getQueueTableData() {
        if (patientQueue.isEmpty()) {
            return new String[0][0];
        }

        // COMPLETELY SAFE APPROACH: Use tracking maps instead of queue destruction
        // Build data from queue tracking maps which maintain queue order
        ListInterface<Patient> queuePatients = new DoublyLinkedList<>();

        // Extract patients from queue safely by using queue name/IC maps
        // This is a workaround since LinkedQueue lacks safe traversal
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            // Check if patient is in queue using O(1) lookup
            if (queueMap.get(patient.getId()) != null && queueMap.get(patient.getId())) {
                queuePatients.add(patient);
            }
        }

        // Build table data from queue patients
        String[][] queueData = new String[queuePatients.getNumberOfEntries()][6];
        java.util.Iterator<Patient> queueIterator = queuePatients.iterator();
        int index = 0;

        while (queueIterator.hasNext()) {
            Patient p = queueIterator.next();
            int age = calculateAgeFromIC(p.getIcNumber());

            queueData[index][0] = String.format("%04d", index + 1);  // Queue position
            queueData[index][1] = p.getId();
            queueData[index][2] = p.getName();
            queueData[index][3] = p.getGender();
            queueData[index][4] = String.valueOf(age);
            queueData[index][5] = p.getPhone();
            index++;
        }

        return queueData;
    }

    /**
     * Check if queue is empty
     *
     * @return true if queue is empty, false otherwise
     */
    public boolean isQueueEmpty() {
        return patientQueue.isEmpty();
    }

    // Generate next patient ID using iterator for efficient traversal
    private String generateNextPatientId() {
        int max = 0;

        // Use iterator for efficient traversal of DoublyLinkedList
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next().getId();
            if (id != null && id.startsWith("P")) {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) {
                    max = num;
                }
            }
        }
        return String.format("P%04d", max + 1);
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

    /**
     * Check if patients exist for viewing returns boolean for UI to handle
     */
    public boolean hasPatients() {
        return patientList.getNumberOfEntries() > 0;
    }

    /**
     * Handle Patient ID sorting submenu with ascending/descending options
     * Returns the sorted list for UI to display
     */
    public ListInterface<Patient> handleSortByIdChoice(int choice) {
        switch (choice) {
            case 1:
                return sortPatientsByID(true);  // Ascending (A-Z)
            case 2:
                return sortPatientsByID(false); // Descending (Z-A)
            default:
                return null; // Invalid choice
        }
    }

    /**
     * Handle Registration Date sorting submenu with ascending/descending
     * options Returns the sorted list for UI to display
     */
    public ListInterface<Patient> handleSortByDateChoice(int choice) {
        switch (choice) {
            case 1:
                return sortPatientsByRegistrationDate(true);  // Ascending (Oldest first)
            case 2:
                return sortPatientsByRegistrationDate(false); // Descending (Newest first)
            default:
                return null; // Invalid choice
        }
    }

    /**
     * Get sorted patients by name (ascending only)
     */
    public ListInterface<Patient> getSortedPatientsByName() {
        return sortPatientsByName();
    }

    /**
     * Get sorted patients by age (ascending only)
     */
    public ListInterface<Patient> getSortedPatientsByAge() {
        return sortPatientsByAge();
    }

    /**
     * Get patient data formatted for table display using iterator for O(n) performance
     */
    public String[][] getPatientTableData(ListInterface<Patient> patients) {
        String[][] patientData = new String[patients.getNumberOfEntries()][7];

        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Patient> iterator = patients.iterator();
        int index = 0;

        while (iterator.hasNext()) {
            Patient p = iterator.next();
            int age = calculateAgeFromIC(p.getIcNumber());

            patientData[index][0] = p.getId();
            patientData[index][1] = p.getName();
            patientData[index][2] = p.getGender();
            patientData[index][3] = String.valueOf(age);
            patientData[index][4] = p.getIcNumber();
            patientData[index][5] = p.getPhone();
            patientData[index][6] = p.getRegistrationDate().toString();
            index++;
        }

        return patientData;
    }

    public static class ValidationResult {

        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Queue operation result class
     */
    public static class QueueOperationResult {

        private final boolean success;
        private final String statusCode;
        private final Patient patient;
        private final String queueNumber;
        private final int totalInQueue;

        public QueueOperationResult(boolean success, String statusCode, Patient patient, String queueNumber, int totalInQueue) {
            this.success = success;
            this.statusCode = statusCode;
            this.patient = patient;
            this.queueNumber = queueNumber;
            this.totalInQueue = totalInQueue;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public Patient getPatient() {
            return patient;
        }

        public String getQueueNumber() {
            return queueNumber;
        }

        public int getTotalInQueue() {
            return totalInQueue;
        }
    }

    /**
     * Search result class
     */
    public static class SearchResult {

        private final boolean found;
        private final Patient patient;
        private final String queueNumber;
        private final int queuePosition;

        public SearchResult(boolean found, Patient patient, String queueNumber, int queuePosition) {
            this.found = found;
            this.patient = patient;
            this.queueNumber = queueNumber;
            this.queuePosition = queuePosition;
        }

        public boolean isFound() {
            return found;
        }

        public Patient getPatient() {
            return patient;
        }

        public String getQueueNumber() {
            return queueNumber;
        }

        public int getQueuePosition() {
            return queuePosition;
        }
    }

    /**
     * Validate IC number format
     *
     * @param icNumber IC number to validate
     * @return ValidationResult with status and error message
     */
    public ValidationResult validateIC(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return new ValidationResult(false, "IC Number cannot be empty.");
        }

        String cleanIC = icNumber.replaceAll("-", "").trim();
        if (!cleanIC.matches("\\d{12}")) {
            return new ValidationResult(false, "IC Number must be exactly 12 digits (Malaysian format: YYMMDDPBXXXX).");
        }

        if (!ValidationUtil.isValidMalaysianIC(cleanIC)) {
            return new ValidationResult(false, "Invalid Malaysian IC format. Please enter a valid IC number.");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate name format
     *
     * @param name Name to validate
     * @return ValidationResult with status and error message
     */
    public ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Name cannot be empty.");
        }

        if (!ValidationUtil.isValidName(name.trim())) {
            return new ValidationResult(false, "Name must contain only letters and spaces and be at least 2 characters long.");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validate phone number format
     *
     * @param phone Phone number to validate
     * @return ValidationResult with status and error message
     */
    public ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationResult(false, "Phone number cannot be empty.");
        }

        String cleanPhone = phone.replaceAll("[\\s-]", "");
        if (!ValidationUtil.isValidPhone(cleanPhone)) {
            return new ValidationResult(false, "Invalid Malaysian phone number format. Must be 10-11 digits.");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Format phone for storage
     *
     * @param phone Phone number to format
     * @return formatted phone number
     */
    public String formatPhoneForStorage(String phone) {
        return ValidationUtil.formatPhoneForStorage(phone);
    }

    /**
     * Find patient by ID using O(1) HashMap lookup
     * @param id Patient ID to search for
     * @return Patient if found, null otherwise
     */
    public Patient findPatientById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        // O(1) HashMap lookup - fast and efficient
        return patientMap.get(id.trim());
    }

    /**
     * Find patients by name
     *
     * @param name Patient name to search for
     * @return List of patients with matching names
     */
    /**
     * Find patients by name using hybrid O(1) exact + O(n) partial search
     * @param name Patient name to search for
     * @return List of patients with matching names
     */
    public ListInterface<Patient> findPatientsByName(String name) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();

        if (name == null || name.trim().isEmpty()) {
            return foundPatients;
        }

        String searchName = name.trim().toLowerCase();

        // First try O(1) exact name lookup
        Patient exactMatch = nameMap.get(searchName);
        if (exactMatch != null) {
            foundPatients.add(exactMatch);
            // If exact match found and it's the complete search term, return immediately
            if (exactMatch.getName().toLowerCase().equals(searchName)) {
                return foundPatients;
            }
        }

        // If no exact match or need partial matching, do O(n) search for partial matches
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            String patientName = patient.getName().toLowerCase();
            // Add if contains search term and not already added (avoid duplicates)
            if (patientName.contains(searchName) && !patientName.equals(searchName)) {
                foundPatients.add(patient);
            }
        }
        return foundPatients;
    }

    /**
     * Find patient by exact name using O(1) HashMap lookup
     * @param name Exact patient name to search for
     * @return Patient if found, null otherwise
     */
    public Patient findPatientByExactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        // O(1) HashMap lookup for exact name match
        return nameMap.get(name.trim().toLowerCase());
    }

    /**
     * Find patient by IC number
     * @param ic IC number to search for
     * @return Patient if found, null otherwise
     */
    public Patient findPatientByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return null;
        }

        String cleanIC = ic.replaceAll("-", "").trim();
        // O(1) HashMap lookup - fast and efficient
        return icMap.get(cleanIC);
    }

    /**
     * Calculate age from IC number
     *
     * @param icNumber Malaysian IC number
     * @return calculated age
     */
    public int getPatientAge(String icNumber) {
        return calculateAgeFromIC(icNumber);
    }

    /**
     * Check if IC number already exists
     *
     * @param icNumber IC number to check
     * @return existing Patient if found, null otherwise
     */
    public Patient checkExistingPatient(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return null;
        }
        String cleanIC = icNumber.replaceAll("-", "").trim();
        return icMap.get(cleanIC);
    }

    /**
     * Register new patient
     *
     * @param name Patient name
     * @param icNumber Patient IC number
     * @param phone Patient phone number
     * @param gender Patient gender
     * @return newly created Patient object
     */
    public Patient registerNewPatient(String name, String icNumber, String phone, String gender) {
        // Generate new patient ID
        String newId = generateNextPatientId();

        // Create patient using control layer method
        Patient patient = createPatientWithId(newId, name, icNumber, phone, gender, java.time.LocalDate.now());

        // Add to all data structures
        addToAllDataStructures(patient);

        return patient;
    }

    /**
     * Check if patient is already in queue
     *
     * @param patient Patient to check
     * @return true if patient is in queue, false otherwise
     */
    public boolean isPatientInQueue(Patient patient) {
        return isInQueue(patient);
    }

    /**
     * Add patient to queue
     *
     * @param patient Patient to add to queue
     * @return queue number assigned to patient
     */
    public String addPatientToQueue(Patient patient) {
        if (patient == null || isInQueue(patient)) {
            return null;
        }

        enqueuePatient(patient);
        return patientQueue.getQueueNumber(patient);
    }

    /**
     * Get current queue size
     *
     * @return number of patients in queue
     */
    public int getQueueSize() {
        return patientQueue.size();
    }

    /**
     * Update patient information
     *
     * @param currentPatient Patient to update
     * @param updatedPatient New patient data
     * @return true if update successful, false if IC conflict
     */
    public boolean updatePatientInfo(Patient currentPatient, Patient updatedPatient) {
        if (currentPatient == null || updatedPatient == null) {
            return false;
        }

        return updateAllDataStructures(currentPatient, updatedPatient);
    }

    /**
     * Remove patient
     *
     * @param patient Patient to remove
     * @return true if removal successful
     */
    public boolean removePatient(Patient patient) {
        if (patient == null) {
            return false;
        }

        // Remove from all data structures
        removeFromAllDataStructures(patient);

        // Also remove from queue if present
        removePatientFromQueue(patient);

        return true;
    }

    /**
     * Sort patients by Registration Date with order option
     *
     * @param ascending true for ascending (oldest first), false for descending
     * (newest first)
     * @return sorted list of patients
     */
    private ListInterface<Patient> sortPatientsByRegistrationDate(boolean ascending) {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by Registration Date using List ADT sorting function
        sortedList.mergeSort((p1, p2) -> p1.getRegistrationDate().compareTo(p2.getRegistrationDate()));

        // Reverse if descending order is requested
        if (!ascending) {
            sortedList.reverse();
        }

        return sortedList;
    }

    /**
     * Sort patients by ID with order option
     *
     * @param ascending true for ascending (A-Z), false for descending (Z-A)
     * @return sorted list of patients
     */
    private ListInterface<Patient> sortPatientsByID(boolean ascending) {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by ID using List ADT sorting function
        sortedList.mergeSort((p1, p2) -> p1.getId().compareTo(p2.getId()));

        // Reverse if descending order is requested
        if (!ascending) {
            sortedList.reverse();
        }

        return sortedList;
    }

    /**
     * Sort patients by Name (ascending order only)
     *
     * @return sorted list of patients by name (A-Z)
     */
    private ListInterface<Patient> sortPatientsByName() {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by Name using List ADT sorting function (case-insensitive)
        sortedList.mergeSort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

        return sortedList;
    }

    /**
     * Sort patients by Age (ascending order only)
     *
     * @return sorted list of patients by age (youngest first)
     */
    private ListInterface<Patient> sortPatientsByAge() {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by Age using List ADT sorting function
        sortedList.mergeSort((p1, p2) -> {
            int age1 = calculateAgeFromIC(p1.getIcNumber());
            int age2 = calculateAgeFromIC(p2.getIcNumber());
            return Integer.compare(age1, age2);
        });

        return sortedList;
    }

    /**
     * Helper method to create a copy of the patient list
     *
     * @return new list containing all patients from the main list
     */
    // Create copy of patient list using iterator for efficient traversal
    private ListInterface<Patient> createPatientListCopy() {
        ListInterface<Patient> copyList = new DoublyLinkedList<>();

        // Use iterator for efficient traversal of DoublyLinkedList
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            copyList.add(iterator.next());
        }
        return copyList;
    }

    private Patient findPatientInQueueByName(String name) {
        // O(1) lookup using secondary queue name map
        return queueNameMap.get(name.toLowerCase());
    }

    private Patient findPatientInQueueByIC(String ic) {
        // O(1) lookup using secondary queue IC map
        String searchIC = ic.replaceAll("-", ""); // Remove dashes for consistent lookup
        return queueIcMap.get(searchIC);
    }

    public ListInterface<Patient> getPatientList() {
        return patientList;
    }

    // Removed unnecessary consistency checking methods
    // HashMap is trusted for O(1) lookups without validation overhead

    /**
     * Generate age demographics data
     *
     * @return array with age demographic counts [total, under18, age18to30,
     * age31to50, age51to65, over65]
     */
    public int[] generateAgeDemographicsData() {
        int under18 = 0;
        int age18to30 = 0;
        int age31to50 = 0;
        int age51to65 = 0;
        int over65 = 0;
        int totalPatients = 0;

        // Use iterator for efficient traversal of DoublyLinkedList
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient p = iterator.next();
            totalPatients++;
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

        return new int[]{totalPatients, under18, age18to30, age31to50, age51to65, over65};
    }

    /**
     * Generate age demographics report data
     *
     * @return AgeDemographicsData object for UI to display
     */
    public AgeDemographicsData generateAgeDemographicsReport() {
        int[] data = generateAgeDemographicsData();
        return new AgeDemographicsData(data[0], data[1], data[2], data[3], data[4], data[5]);
    }

    /**
     * Age demographics data transfer object
     */
    public static class AgeDemographicsData {

        private final int totalPatients;
        private final int under18;
        private final int age18to30;
        private final int age31to50;
        private final int age51to65;
        private final int over65;

        public AgeDemographicsData(int totalPatients, int under18, int age18to30, int age31to50, int age51to65, int over65) {
            this.totalPatients = totalPatients;
            this.under18 = under18;
            this.age18to30 = age18to30;
            this.age31to50 = age31to50;
            this.age51to65 = age51to65;
            this.over65 = over65;
        }

        public int getTotalPatients() {
            return totalPatients;
        }

        public int getUnder18() {
            return under18;
        }

        public int getAge18to30() {
            return age18to30;
        }

        public int getAge31to50() {
            return age31to50;
        }

        public int getAge51to65() {
            return age51to65;
        }

        public int getOver65() {
            return over65;
        }
    }

    /**
     * Check if patients exist for reports
     *
     * @return true if patients exist, false otherwise
     */
    public boolean hasPatientsForReport() {
        return patientList.getNumberOfEntries() > 0;
    }

    /**
     * Generate registration timeline report data
     *
     * @return RegistrationTimelineData object for UI to display
     */
    public RegistrationTimelineData generateRegistrationTimelineReport() {
        if (!hasPatientsForReport()) {
            return null; // Return data object, not UI message
        }

        // sort patients by registration date
        ListInterface<Patient> sortedList = sortPatientsByDate();

        // calculate statistics
        Patient firstPatient = sortedList.getEntry(1);
        Patient lastPatient = sortedList.getEntry(sortedList.getNumberOfEntries());

        // Count gender distribution using iterator
        int maleCount = 0, femaleCount = 0;
        java.util.Iterator<Patient> genderIterator = sortedList.iterator();
        while (genderIterator.hasNext()) {
            if (genderIterator.next().getGender().equalsIgnoreCase("Male")) {
                maleCount++;
            } else {
                femaleCount++;
            }
        }

        // Calculate monthly breakdown using iterator
        MapInterface<String, Integer> monthlyCount = new HashMap<>();
        java.util.Iterator<Patient> monthIterator = sortedList.iterator();
        while (monthIterator.hasNext()) {
            Patient p = monthIterator.next();
            String monthYear = p.getRegistrationDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));

            Integer count = monthlyCount.get(monthYear);
            if (count == null) {
                monthlyCount.put(monthYear, 1);
            } else {
                monthlyCount.put(monthYear, count + 1);
            }
        }

        // prepare patient data for display
        String[][] patientData = getPatientTableData(sortedList);

        // Return structured data object (not formatted UI string)
        return new RegistrationTimelineData(patientData, firstPatient, lastPatient, maleCount, femaleCount, monthlyCount);
    }

    /**
     * Registration timeline data transfer object
     */
    public static class RegistrationTimelineData {

        private final String[][] patientData;     // Formatted data for UI display
        private final Patient firstPatient;
        private final Patient lastPatient;
        private final int maleCount;
        private final int femaleCount;
        private final MapInterface<String, Integer> monthlyCount;

        public RegistrationTimelineData(String[][] patientData, Patient firstPatient, Patient lastPatient,
                int maleCount, int femaleCount, MapInterface<String, Integer> monthlyCount) {
            this.patientData = patientData;
            this.firstPatient = firstPatient;
            this.lastPatient = lastPatient;
            this.maleCount = maleCount;
            this.femaleCount = femaleCount;
            this.monthlyCount = monthlyCount;
        }

        // Getter methods for UI layer to access data
        public String[][] getPatientData() {
            return patientData;
        }

        public Patient getFirstPatient() {
            return firstPatient;
        }

        public Patient getLastPatient() {
            return lastPatient;
        }

        public int getMaleCount() {
            return maleCount;
        }

        public int getFemaleCount() {
            return femaleCount;
        }

        public MapInterface<String, Integer> getMonthlyCount() {
            return monthlyCount;
        }
    }

    /**
     * Sort patients by registration date for report generation (oldest first)
     * This method maintains backward compatibility for report generation
     *
     * @return sorted list of patients by registration date (oldest first)
     */
    private ListInterface<Patient> sortPatientsByDate() {
        // Use the new parameterized method with ascending order (oldest first)
        return sortPatientsByRegistrationDate(true);
    }

    /**
     * Create patient entity
     */
    public Patient createPatient(String name, String icNumber, String phone, String gender) {
        return new Patient(name, icNumber, phone, gender);
    }

    /**
     * Create patient entity with ID
     */
    public Patient createPatientWithId(String id, String name, String icNumber, String phone, String gender, java.time.LocalDate regDate) {
        return new Patient(id, name, icNumber, phone, gender, regDate);
    }

    // Convert gender input to standard format
    public String normalizeGender(String gender) {
        return ValidationUtil.normalizeGender(gender);
    }

    public int calculateAgeFromIC(String icNumber) {
        return ValidationUtil.calculateAgeFromIC(icNumber);
    }
}
