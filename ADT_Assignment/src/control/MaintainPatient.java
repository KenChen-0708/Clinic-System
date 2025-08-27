// Ronaldo Loh Yu Fei
package control;

import adt.DoublyLinkedList;
import adt.LinkedQueue;
import adt.ListInterface;
import adt.QueueInterface;
import adt.HashMap;
import adt.MapInterface;

import control.MaintainDoctor;
import control.MaintainTreatment;
import dao.PatientDAO;
import dao.AppointmentDAO;
import entity.Doctor;
import entity.Patient;
import entity.Appointment;
import utility.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MaintainPatient {

    // Initialize all instance variables at declaration point
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();
    private final MaintainTreatment maintainTreatment = new MaintainTreatment();

    // Appointment management
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ListInterface<Appointment> appointmentList = appointmentDAO.retrieveFromFile();
    private final MaintainDoctor maintainDoctor = new MaintainDoctor();



    private final QueueInterface<Patient> patientQueue = new LinkedQueue<>();
    private final MapInterface<String, Patient> patientMap = new HashMap<>();
    private final MapInterface<String, Patient> icMap = new HashMap<>();
    private final MapInterface<String, Patient> nameMap = new HashMap<>();  // O(1) exact name lookup
    private final MapInterface<String, Boolean> queueMap = new HashMap<>();
    private final MapInterface<String, Patient> queueNameMap = new HashMap<>();
    private final MapInterface<String, Patient> queueIcMap = new HashMap<>();

    /**
     * Clean constructor - all initialization done at declaration
     */
    public MaintainPatient() {
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
     * Atomic update method to ensure data consistency across all data structures
     * Uses rollback mechanism to prevent partial updates
     * @param oldPatient the patient to be updated
     * @param newPatient the updated patient data
     * @return true if update successful, false if IC conflict detected or update failed
     */
    private boolean updateAllDataStructures(Patient oldPatient, Patient newPatient) {
        // Defensive null checks to prevent NPE
        if (oldPatient == null || newPatient == null ||
            oldPatient.getId() == null || newPatient.getId() == null ||
            oldPatient.getIcNumber() == null || newPatient.getIcNumber() == null) {
            return false; // Cannot update with null data
        }

        // CRITICAL: Validate IC number uniqueness before update
        if (!oldPatient.getIcNumber().equals(newPatient.getIcNumber())) {
            // Check if new IC already exists for a different patient
            Patient existingPatient = icMap.get(newPatient.getIcNumber());
            if (existingPatient != null && !existingPatient.getId().equals(oldPatient.getId())) {
                // IC already exists for another patient - REJECT UPDATE
                return false;
            }
        }

        // ATOMIC UPDATE: Store backup data for rollback
        Patient backupPatientFromMap = patientMap.get(oldPatient.getId());
        Patient backupPatientFromIcMap = icMap.get(oldPatient.getIcNumber());
        Patient backupPatientFromNameMap = nameMap.get(oldPatient.getName().toLowerCase());

        try {
            // Step 1: Update list (find and replace in-place)
            boolean listUpdated = false;
            for (int index = 1; index <= patientList.getNumberOfEntries(); index++) {
                Patient current = patientList.getEntry(index);
                if (current != null && current.getId() != null && current.getId().equals(oldPatient.getId())) {
                    patientList.remove(index);
                    patientList.add(index, newPatient);
                    listUpdated = true;
                    break;
                }
            }

            if (!listUpdated) {
                // Patient not found in list - this shouldn't happen
                return false;
            }

            // Step 2: Update maps atomically
            // Remove old entries
            patientMap.remove(oldPatient.getId());
            icMap.remove(oldPatient.getIcNumber());
            nameMap.remove(oldPatient.getName().toLowerCase());

            // Add new entries
            patientMap.put(newPatient.getId(), newPatient);
            icMap.put(newPatient.getIcNumber(), newPatient);
            nameMap.put(newPatient.getName().toLowerCase(), newPatient);

            // Step 3: Save to file (final step)
            patientDAO.saveToFile(patientList);
            return true;

        } catch (Exception e) {
            // ROLLBACK: Restore original data if any step fails
            if (backupPatientFromMap != null) {
                patientMap.put(oldPatient.getId(), backupPatientFromMap);
            }
            if (backupPatientFromIcMap != null) {
                icMap.put(oldPatient.getIcNumber(), backupPatientFromIcMap);
            }
            if (backupPatientFromNameMap != null) {
                nameMap.put(oldPatient.getName().toLowerCase(), backupPatientFromNameMap);
            }

            // Restore list by reloading from file (safest approach)
            ListInterface<Patient> restoredList = patientDAO.retrieveFromFile();
            patientList.clear();
            java.util.Iterator<Patient> restoreIterator = restoredList.iterator();
            while (restoreIterator.hasNext()) {
                patientList.add(restoreIterator.next());
            }

            return false; // Update failed
        }
    }

    // Atomic method to add patient to all data structures
    private void addToAllDataStructures(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
            try {
                // Add to all data structures atomically
                patientList.add(patient);
                patientMap.put(patient.getId(), patient);
                icMap.put(patient.getIcNumber(), patient);
                nameMap.put(patient.getName().toLowerCase(), patient);

                // Save to file as final step
                patientDAO.saveToFile(patientList);
            } catch (Exception e) {
                // Rollback on failure - remove from all structures
                patientList.remove(patientList.getNumberOfEntries()); // Remove last added
                patientMap.remove(patient.getId());
                icMap.remove(patient.getIcNumber());
                nameMap.remove(patient.getName().toLowerCase());
                // Note: File remains unchanged if save failed
            }
        }
    }

    // Unified method to remove patient from all data structures
    private void removeFromAllDataStructures(Patient patient) {
        // Defensive null checks to prevent NPE
        if (patient == null || patient.getId() == null) {
            return; // Cannot remove null patient or patient with null ID
        }

        // Remove from list using iterator with null safety
        java.util.Iterator<Patient> iterator = patientList.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            Patient current = iterator.next();
            // Null-safe comparison
            if (current != null && current.getId() != null && current.getId().equals(patient.getId())) {
                patientList.remove(index);
                break;
            }
            index++;
        }

        // Remove from all maps with null safety
        if (patient.getId() != null) {
            patientMap.remove(patient.getId());
        }
        if (patient.getIcNumber() != null) {
            icMap.remove(patient.getIcNumber());
        }
        if (patient.getName() != null) {
            nameMap.remove(patient.getName().toLowerCase());  // Remove from name map
        }

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
     * Add patient to queue by ID
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
     * Serve next patient in queue - FIXED: Capture queue number before dequeuing
     * @return QueueOperationResult with served patient data
     */
    public QueueOperationResult serveNextPatient() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, "QUEUE_EMPTY", null, null, 0);
        }

        // FIXED: Get queue number BEFORE dequeuing the patient
        Patient nextPatient = patientQueue.peek(); // Get patient without removing
        String queueNum = patientQueue.getQueueNumber(nextPatient); // Get queue number while still in queue

        // Now safely dequeue the patient using helper method
        nextPatient = dequeuePatient();
        if (nextPatient != null) {
            // Check for available doctor
            MaintainDoctor maintainDoctor = new MaintainDoctor();
            Doctor availableDoctor = maintainDoctor.findAvailableDoctor();

            if (availableDoctor != null) {
                // Update doctor availability to "On Duty"
                availableDoctor.setAvailability("On Duty");
                maintainDoctor.updateDoctorInList(availableDoctor);

                // Set serving patient for treatment module
                if (maintainTreatment != null) {
                    maintainTreatment.addServingPatientDoctorPair(nextPatient, availableDoctor);
                }

                // FIXED: Reset doctor availability back to "Available" after serving
                availableDoctor.setAvailability("Available");
                maintainDoctor.updateDoctorInList(availableDoctor);

                return new QueueOperationResult(true, "PATIENT_SERVED", nextPatient, queueNum, patientQueue.size());

            } else {
                // No available doctor - put patient back in queue
                enqueuePatient(nextPatient);
                return new QueueOperationResult(false, "NO_AVAILABLE_DOCTOR", null, null, patientQueue.size());
            }
        }
        return new QueueOperationResult(false, "OPERATION_FAILED", null, null, patientQueue.size());
    }

    /**
     * Get current queue status
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

        public int getTotalWaiting() { return totalWaiting; }
        public Patient getNextPatient() { return nextPatient; }
        public String getNextQueueNumber() { return nextQueueNumber; }
        public boolean isEmpty() { return totalWaiting == 0; }
    }

    /**
     * Search patient in queue by name
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
     * Get queue data for UI display - FIXED: Preserves original queue numbers
     * UI layer handles formatting
     * FIXED: Safe traversal that preserves FIFO order and original queue numbers
     */
    public ListInterface<Patient> getQueueList() {
        ListInterface<Patient> queueList = new DoublyLinkedList<>();

        // FIXED: Safe FIFO traversal preserving original queue numbers
        if (!patientQueue.isEmpty()) {
            // Store patients and their original queue numbers
            ListInterface<Patient> tempPatients = new DoublyLinkedList<>();
            ListInterface<String> tempQueueNumbers = new DoublyLinkedList<>();

            // First pass: Extract patients and capture their queue numbers
            while (!patientQueue.isEmpty()) {
                Patient patient = patientQueue.peek(); // Get patient without removing
                String queueNum = patientQueue.getQueueNumber(patient); // Get queue number while still in queue

                patient = patientQueue.dequeue(); // Now remove patient

                queueList.add(patient);  // Add to result list in FIFO order
                tempPatients.add(patient);  // Store for restoration
                tempQueueNumbers.add(queueNum);  // Store original queue number
            }

            // Second pass: Restore original queue with exact same queue numbers
            for (int i = 1; i <= tempPatients.getNumberOfEntries(); i++) {
                Patient patient = tempPatients.getEntry(i);
                String originalQueueNum = tempQueueNumbers.getEntry(i);
                patientQueue.enqueueWithExistingNumber(patient, originalQueueNum);  // Restore with original number
            }
        }

        return queueList;
    }

    /**
     * Get queue numbers for display
     */
    public String[] getQueueNumbers() {
        return patientQueue.getQueueNumbers();
    }

    /**
     * Check if queue is empty
     * @return true if queue is empty, false otherwise
     */
    public boolean isQueueEmpty() {
        return patientQueue.isEmpty();
    }

    /**
     * Get patient's queue number
     * @param patient Patient to check
     * @return queue number or null if not in queue
     */
    public String getPatientQueueNumber(Patient patient) {
        if (patient == null || !isInQueue(patient)) {
            return null;
        }
        return patientQueue.getQueueNumber(patient);
    }

    /**
     * Get patient's position in queue (1-based)
     * @param patient Patient to check
     * @return queue position or 0 if not in queue
     */
    public int getPatientQueuePosition(Patient patient) {
        if (patient == null || !isInQueue(patient)) {
            return 0;
        }

        ListInterface<Patient> queueList = getQueueList();
        for (int i = 1; i <= queueList.getNumberOfEntries(); i++) {
            Patient p = queueList.getEntry(i);
            if (p.getId().equals(patient.getId())) {
                return i;
            }
        }
        return 0;
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

        // FIXED: Simple and reliable queue removal using LinkedQueue's removeByValue
        // First, find the actual patient object in the queue by ID
        Patient patientToRemove = null;
        ListInterface<Patient> queueList = getQueueList();

        for (int i = 1; i <= queueList.getNumberOfEntries(); i++) {
            Patient p = queueList.getEntry(i);
            if (p.getId().equals(patient.getId())) {
                patientToRemove = p;
                break;
            }
        }

        if (patientToRemove == null) {
            return false; // Patient not found in queue
        }

        // Remove using the actual patient object from queue
        boolean removed = ((LinkedQueue<Patient>) patientQueue).removeByValue(patientToRemove);

        if (removed) {
            // Clean up tracking maps
            queueMap.remove(patient.getId()); // O(1) patient removal
            queueNameMap.remove(patient.getName().toLowerCase()); // O(1) name removal
            queueIcMap.remove(patient.getIcNumber()); // O(1) IC removal
        }

        return removed;
    }

    /**
     * Check if patients exist for viewing
     * returns boolean for UI to handle
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
     * Handle Registration Date sorting submenu with ascending/descending options
     * Returns the sorted list for UI to display
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
     * Get patient list for UI display - returns raw patient objects
     * UI layer handles formatting
     */
    public ListInterface<Patient> getPatientList() {
        return patientList;
    }


    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
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

        public boolean isFound() { return found; }
        public Patient getPatient() { return patient; }
        public String getQueueNumber() { return queueNumber; }
        public int getQueuePosition() { return queuePosition; }
    }

    /**
     * Validate IC number format
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
     * Calculate patient age from IC number
     * @param icNumber IC number to calculate age from
     * @return calculated age in years
     */
    public int getPatientAge(String icNumber) {
        return ValidationUtil.calculateAgeFromIC(icNumber);
    }

    /**
     * Format phone number for storage
     * @param phone Phone number to format
     * @return formatted phone number
     */
    public String formatPhoneForStorage(String phone) {
        return ValidationUtil.formatPhoneForStorage(phone);
    }

    /**
     * Normalize gender input to standard format
     * @param gender Gender input to normalize
     * @return normalized gender string
     */
    public String normalizeGender(String gender) {
        return ValidationUtil.normalizeGender(gender);
    }

    /**
     * Check if IC number already exists
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
     * Register new patient - accepts patient entity from boundary layer
     * @param patient Patient entity to register
     * @return registered patient with ID if successful, null if IC already exists
     */
    public Patient registerNewPatient(Patient patient) {
        if (patient == null) {
            return null;
        }

        // Check if IC already exists
        if (checkExistingPatient(patient.getIcNumber()) != null) {
            return null; // IC already exists
        }

        // Generate new patient ID and create patient with ID
        String newId = generateNextPatientId();
        Patient patientWithId = new Patient(newId, patient.getName(), patient.getIcNumber(),
                                          patient.getPhone(), patient.getGender(), java.time.LocalDate.now());

        // Add to all data structures
        addToAllDataStructures(patientWithId);

        return patientWithId; // Return the patient with generated ID
    }

    /**
     * Check if patient is already in queue
     * @param patient Patient to check
     * @return true if patient is in queue, false otherwise
     */
    public boolean isPatientInQueue(Patient patient) {
        return isInQueue(patient);
    }

    /**
     * Add patient to queue
     * @param patient Patient to add to queue
     * @return queue number assigned to patient
     */
    public String addPatientToQueue(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return null;
        }

        // FIXED: Only check if patient is already in queue if they have valid data
        if (patient.getName() != null && patient.getIcNumber() != null) {
            // Check if patient is already in queue
            if (isInQueue(patient)) {
                return null; // Patient already in queue
            }

            enqueuePatient(patient);
            return patientQueue.getQueueNumber(patient);
        }

        return null;
    }

    /**
     * Get current queue size
     * @return number of patients in queue
     */
    public int getQueueSize() {
        return patientQueue.size();
    }

    /**
     * Update patient information
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
     * @param ascending true for ascending (oldest first), false for descending (newest first)
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
     * @return sorted list of patients by age (youngest first)
     */
    private ListInterface<Patient> sortPatientsByAge() {
        // Create a copy of the patient list for sorting
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by Age using List ADT sorting function
        sortedList.mergeSort((p1, p2) -> {
            int age1 = ValidationUtil.calculateAgeFromIC(p1.getIcNumber());
            int age2 = ValidationUtil.calculateAgeFromIC(p2.getIcNumber());
            return Integer.compare(age1, age2);
        });

        return sortedList;
    }

    /**
     * Helper method to create a copy of the patient list
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


    // Removed unnecessary consistency checking methods
    // HashMap is trusted for O(1) lookups without validation overhead

    /**
     * Generate patient treatment statistics data using custom ADT sorting
     * @return PatientTreatmentData object with treatment statistics
     */
    public PatientTreatmentData generatePatientTreatmentData() {
        if (patientList.isEmpty()) {
            return null;
        }

        // Create sorted list of patients by ID using custom ADT merge sort
        ListInterface<Patient> sortedPatients = createPatientListCopy();
        sortedPatients.mergeSort((p1, p2) -> p1.getId().compareToIgnoreCase(p2.getId()));

        // Build treatment statistics using iterator
        ListInterface<PatientTreatmentInfo> treatmentStats = new DoublyLinkedList<>();
        int totalPatients = 0;
        int totalTreatments = 0;
        int patientsWithTreatments = 0;
        int patientsWithoutTreatments = 0;

        java.util.Iterator<Patient> iterator = sortedPatients.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            totalPatients++;

            // Get treatment count for this patient from treatment module
            DoublyLinkedList<entity.Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patient.getId());
            int treatmentCount = patientTreatments.getNumberOfEntries();
            totalTreatments += treatmentCount;

            if (treatmentCount > 0) {
                patientsWithTreatments++;
            } else {
                patientsWithoutTreatments++;
            }

            // Create treatment info for this patient
            PatientTreatmentInfo info = new PatientTreatmentInfo(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                ValidationUtil.calculateAgeFromIC(patient.getIcNumber()),
                treatmentCount
            );
            treatmentStats.add(info);
        }

        return new PatientTreatmentData(
            totalPatients,
            totalTreatments,
            patientsWithTreatments,
            patientsWithoutTreatments,
            treatmentStats
        );
    }

    /**
     * Generate patient treatment report data
     * @return PatientTreatmentData object for UI to display
     */
    public PatientTreatmentData generatePatientTreatmentReport() {
        return generatePatientTreatmentData();
    }

    /**
     * Patient treatment data transfer object
     */
    public static class PatientTreatmentData {
        private final int totalPatients;
        private final int totalTreatments;
        private final int patientsWithTreatments;
        private final int patientsWithoutTreatments;
        private final ListInterface<PatientTreatmentInfo> treatmentStats;

        public PatientTreatmentData(int totalPatients, int totalTreatments, int patientsWithTreatments,
                                  int patientsWithoutTreatments, ListInterface<PatientTreatmentInfo> treatmentStats) {
            this.totalPatients = totalPatients;
            this.totalTreatments = totalTreatments;
            this.patientsWithTreatments = patientsWithTreatments;
            this.patientsWithoutTreatments = patientsWithoutTreatments;
            this.treatmentStats = treatmentStats;
        }

        public int getTotalPatients() {
            return totalPatients;
        }
        public int getTotalTreatments() {
            return totalTreatments;
        }
        public int getPatientsWithTreatments() {
            return patientsWithTreatments;
        }
        public int getPatientsWithoutTreatments() {
            return patientsWithoutTreatments;
        }
        public ListInterface<PatientTreatmentInfo> getTreatmentStats() {
            return treatmentStats;
        }
    }

    /**
     * Individual patient treatment information
     */
    public static class PatientTreatmentInfo {
        private final String patientId;
        private final String patientName;
        private final String gender;
        private final int age;
        private final int treatmentCount;

        public PatientTreatmentInfo(String patientId, String patientName, String gender, int age, int treatmentCount) {
            this.patientId = patientId;
            this.patientName = patientName;
            this.gender = gender;
            this.age = age;
            this.treatmentCount = treatmentCount;
        }

        public String getPatientId() {
            return patientId;
        }
        public String getPatientName() {
            return patientName;
        }
        public String getGender() {
            return gender;
        }
        public int getAge() {
            return age;
        }
        public int getTreatmentCount() {
            return treatmentCount;
        }
    }



    /**
     * Check if patients exist for reports
     * @return true if patients exist, false otherwise
     */
    public boolean hasPatientsForReport() {
        return patientList.getNumberOfEntries() > 0;
    }

    /**
     * Generate registration timeline report data
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

        // Return structured data object with raw patient list (not formatted UI data)
        return new RegistrationTimelineData(sortedList, firstPatient, lastPatient, maleCount, femaleCount, monthlyCount);
    }

    /**
     * Registration timeline data transfer object - contains raw business data
     */
    public static class RegistrationTimelineData {
        private final ListInterface<Patient> patientList;     // Raw patient data
        private final Patient firstPatient;
        private final Patient lastPatient;
        private final int maleCount;
        private final int femaleCount;
        private final MapInterface<String, Integer> monthlyCount;

        public RegistrationTimelineData(ListInterface<Patient> patientList, Patient firstPatient, Patient lastPatient,
                                      int maleCount, int femaleCount, MapInterface<String, Integer> monthlyCount) {
            this.patientList = patientList;
            this.firstPatient = firstPatient;
            this.lastPatient = lastPatient;
            this.maleCount = maleCount;
            this.femaleCount = femaleCount;
            this.monthlyCount = monthlyCount;
        }

        // Getter methods for UI layer to access raw data
        public ListInterface<Patient> getPatientList() {
            return patientList;
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
     * @return sorted list of patients by registration date (oldest first)
     */
    private ListInterface<Patient> sortPatientsByDate() {
        // Use the new parameterized method with ascending order (oldest first)
        return sortPatientsByRegistrationDate(true);
    }
    // ================================
    // APPOINTMENT MANAGEMENT METHODS
    // ================================

    /**
     * Book a new appointment
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @param timeString Time in HH:mm format
     * @return AppointmentResult with success status and message
     */
    public AppointmentResult bookAppointment(String patientId, String doctorId, String timeString) {
        // Validate patient exists
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return new AppointmentResult(false, "Patient not found. Please register the patient first.", null);
        }

        // Validate doctor exists and is available
        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        if (doctor == null) {
            return new AppointmentResult(false, "Doctor not found.", null);
        }

        if (!"Available".equalsIgnoreCase(doctor.getAvailability())) {
            return new AppointmentResult(false, "Doctor is not available for appointments.", null);
        }

        // Validate and parse time
        LocalTime appointmentTime;
        try {
            appointmentTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return new AppointmentResult(false, "Invalid time format. Please use HH:mm format (e.g., 14:30).", null);
        }

        // Validate business hours (9 AM to 5 PM)
        if (appointmentTime.isBefore(LocalTime.of(9, 0)) || appointmentTime.isAfter(LocalTime.of(17, 0))) {
            return new AppointmentResult(false, "Appointment time must be between 09:00 and 17:00.", null);
        }

        // Check for time conflicts
        if (hasTimeConflict(doctorId, LocalDate.now(), appointmentTime)) {
            return new AppointmentResult(false, "Doctor already has an appointment at this time.", null);
        }

        // Generate appointment ID
        String appointmentId = generateAppointmentId();

        // Create appointment
        Appointment appointment = new Appointment(appointmentId, patientId, doctorId, appointmentTime);

        // Add to list and save
        appointmentList.add(appointment);
        appointmentDAO.saveToFile(appointmentList);

        return new AppointmentResult(true, "Appointment booked successfully!", appointment);
    }

    /**
     * Get all available doctors
     * @return List of available doctors
     */
    public ListInterface<Doctor> getAvailableDoctors() {
        return maintainDoctor.getAvailableDoctors();
    }

    /**
     * Check if doctor has time conflict - OPTIMIZED with iterator for O(n) performance
     * @param doctorId Doctor ID
     * @param date Appointment date
     * @param time Appointment time
     * @return true if conflict exists, false otherwise
     */
    private boolean hasTimeConflict(String doctorId, LocalDate date, LocalTime time) {
        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment existing = iterator.next();
            if (existing.getDoctorId().equals(doctorId) &&
                existing.getAppointmentDate().equals(date) &&
                existing.getAppointmentTime().equals(time) &&
                "SCHEDULED".equals(existing.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate unique appointment ID - OPTIMIZED with iterator for O(n) performance
     * @return New appointment ID
     */
    private String generateAppointmentId() {
        int maxId = 0;
        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getAppointmentId().startsWith("A")) {
                try {
                    int id = Integer.parseInt(appointment.getAppointmentId().substring(1));
                    if (id > maxId) {
                        maxId = id;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        return String.format("A%04d", maxId + 1);
    }

    /**
     * Get appointments for today - OPTIMIZED with iterator and sorted by time
     * @return List of today's appointments sorted by appointment time
     */
    public ListInterface<Appointment> getTodayAppointments() {
        ListInterface<Appointment> todayAppointments = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();

        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getAppointmentDate().equals(today) &&
                "SCHEDULED".equals(appointment.getStatus())) {
                todayAppointments.add(appointment);
            }
        }

        // FIXED: Sort appointments by time using merge sort from ADT
        if (!todayAppointments.isEmpty()) {
            todayAppointments.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
        }

        return todayAppointments;
    }

    /**
     * Cancel an appointment - OPTIMIZED with iterator for O(n) performance
     * @param appointmentId Appointment ID to cancel
     * @return AppointmentResult with success status and message
     */
    public AppointmentResult cancelAppointment(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return new AppointmentResult(false, "Appointment ID cannot be empty.", null);
        }

        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getAppointmentId().equals(appointmentId.trim())) {
                if ("CANCELLED".equals(appointment.getStatus())) {
                    return new AppointmentResult(false, "Appointment is already cancelled.", appointment);
                }
                if ("COMPLETED".equals(appointment.getStatus())) {
                    return new AppointmentResult(false, "Cannot cancel a completed appointment.", appointment);
                }

                // Update appointment status
                appointment.setStatus("CANCELLED");
                appointmentDAO.saveToFile(appointmentList);

                return new AppointmentResult(true, "Appointment cancelled successfully.", appointment);
            }
        }

        return new AppointmentResult(false, "Appointment not found.", null);
    }

    /**
     * Get all appointments sorted by date and time (for cancellation display)
     * @return List of all appointments sorted by date and time
     */
    public ListInterface<Appointment> getAllAppointments() {
        // Create a copy of the appointment list for sorting
        ListInterface<Appointment> sortedAppointments = new DoublyLinkedList<>();

        // Copy all appointments
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            sortedAppointments.add(iterator.next());
        }

        // FIXED: Sort appointments by date first, then by time using merge sort from ADT
        if (!sortedAppointments.isEmpty()) {
            sortedAppointments.mergeSort((a1, a2) -> {
                // First compare by date
                int dateComparison = a1.getAppointmentDate().compareTo(a2.getAppointmentDate());
                if (dateComparison != 0) {
                    return dateComparison;
                }
                // If dates are equal, compare by time
                return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
            });
        }

        return sortedAppointments;
    }

    /**
     * Get scheduled appointments only (for cancellation)
     * ECB COMPLIANT: Business logic in control layer
     * @return List of scheduled appointments
     */
    public ListInterface<Appointment> getScheduledAppointments() {
        ListInterface<Appointment> scheduledAppointments = new DoublyLinkedList<>();
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if ("SCHEDULED".equals(appointment.getStatus())) {
                scheduledAppointments.add(appointment);
            }
        }
        return scheduledAppointments;
    }

    /**
     * Get sorted appointments by specified criteria
     * ECB COMPLIANT: Business logic in control layer
     * @param appointments Original appointment list
     * @param sortType 1=by ID, 2=by Time, other=no sort
     * @return Sorted appointment list
     */
    public ListInterface<Appointment> getSortedAppointments(ListInterface<Appointment> appointments, int sortType) {
        // Create a copy for sorting to preserve original
        ListInterface<Appointment> sortedList = new DoublyLinkedList<>();

        // Copy all appointments
        java.util.Iterator<Appointment> iterator = appointments.iterator();
        while (iterator.hasNext()) {
            sortedList.add(iterator.next());
        }

        switch (sortType) {
            case 1:
                // Sort by Appointment ID using merge sort from ADT
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentId().compareTo(a2.getAppointmentId()));
                break;
            case 2:
                // Sort by Time using merge sort from ADT
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
                break;
            default:
                // No sorting - return copy as is
                break;
        }

        return sortedList;
    }

    /**
     * Appointment booking result class
     */
    public static class AppointmentResult {
        private final boolean success;
        private final String message;
        private final Appointment appointment;

        public AppointmentResult(boolean success, String message, Appointment appointment) {
            this.success = success;
            this.message = message;
            this.appointment = appointment;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Appointment getAppointment() { return appointment; }
    }






}
