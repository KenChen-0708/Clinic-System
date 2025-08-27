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

    // Patient data management
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();
    private final MaintainTreatment maintainTreatment = new MaintainTreatment();

    // Appointment management
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ListInterface<Appointment> appointmentList = appointmentDAO.retrieveFromFile();
    private final MaintainDoctor maintainDoctor = new MaintainDoctor();

    // Patient queue for managing waiting patients in FIFO order
    private final QueueInterface<Patient> patientQueue = new LinkedQueue<>();

    // Patient lookup maps - enable instant patient retrieval without list traversal
    private final MapInterface<String, Patient> patientMap = new HashMap<>(); // Find patient by ID instantly
    private final MapInterface<String, Patient> icMap = new HashMap<>(); // Find patient by IC number instantly
    private final MapInterface<String, Patient> nameMap = new HashMap<>(); // Find patient by exact name instantly

    // Queue management maps - track queue state without disrupting queue operations
    private final MapInterface<String, Boolean> queueMap = new HashMap<>(); // Check if patient is queued without searching
    private final MapInterface<String, Patient> queueNameMap = new HashMap<>(); // Find queued patients by name without queue traversal
    private final MapInterface<String, Patient> queueIcMap = new HashMap<>(); // Find queued patients by IC without queue traversal
    private final MapInterface<String, Integer> queuePositionMap = new HashMap<>(); // Get queue position instantly without counting

    public MaintainPatient() {
        populateMaps();
    }

    /**
     * Initialize lookup maps from patient list for fast searching
     */
    private void populateMaps() {
        patientMap.clear();
        icMap.clear();
        nameMap.clear();

        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
                patientMap.put(patient.getId(), patient);
                icMap.put(patient.getIcNumber(), patient);
                nameMap.put(patient.getName().toLowerCase(), patient);
            }
        }
    }

    /**
     * Update patient in all data structures atomically
     */
    private boolean updateAllDataStructures(Patient oldPatient, Patient newPatient) {
        if (oldPatient == null || newPatient == null ||
            oldPatient.getId() == null || newPatient.getId() == null ||
            oldPatient.getIcNumber() == null || newPatient.getIcNumber() == null) {
            return false;
        }

        if (!oldPatient.getIcNumber().equals(newPatient.getIcNumber())) {
            Patient existingPatient = icMap.get(newPatient.getIcNumber());
            if (existingPatient != null && !existingPatient.getId().equals(oldPatient.getId())) {
                return false;
            }
        }

        // Store backup data for rollback if needed
        Patient backupPatientFromMap = patientMap.get(oldPatient.getId());
        Patient backupPatientFromIcMap = icMap.get(oldPatient.getIcNumber());
        Patient backupPatientFromNameMap = nameMap.get(oldPatient.getName().toLowerCase());

        try {
            boolean listUpdated = false;
            // Update patient in the list
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
                return false;
            }

            // Update all maps with new patient data
            synchronized (this) {
                patientMap.remove(oldPatient.getId());
                icMap.remove(oldPatient.getIcNumber());
                nameMap.remove(oldPatient.getName().toLowerCase());

                patientMap.put(newPatient.getId(), newPatient);
                icMap.put(newPatient.getIcNumber(), newPatient);
                nameMap.put(newPatient.getName().toLowerCase(), newPatient);
            }

            patientDAO.saveToFile(patientList);
            return true;

        } catch (Exception e) {
            // Restore backup data on failure
            if (backupPatientFromMap != null) {
                patientMap.put(oldPatient.getId(), backupPatientFromMap);
            }
            if (backupPatientFromIcMap != null) {
                icMap.put(oldPatient.getIcNumber(), backupPatientFromIcMap);
            }
            if (backupPatientFromNameMap != null) {
                nameMap.put(oldPatient.getName().toLowerCase(), backupPatientFromNameMap);
            }

            // Restore list from file
            ListInterface<Patient> restoredList = patientDAO.retrieveFromFile();
            patientList.clear();
            java.util.Iterator<Patient> restoreIterator = restoredList.iterator();
            while (restoreIterator.hasNext()) {
                patientList.add(restoreIterator.next());
            }

            return false;
        }
    }

    /**
     * Add patient to all data structures
     */
    private void addToAllDataStructures(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
            try {
                patientList.add(patient);

                synchronized (this) {
                    patientMap.put(patient.getId(), patient);
                    icMap.put(patient.getIcNumber(), patient);
                    nameMap.put(patient.getName().toLowerCase(), patient);
                }

                patientDAO.saveToFile(patientList);
            } catch (Exception e) {
                // Remove from list and maps if save fails
                patientList.remove(patientList.getNumberOfEntries());
                synchronized (this) {
                    patientMap.remove(patient.getId());
                    icMap.remove(patient.getIcNumber());
                    nameMap.remove(patient.getName().toLowerCase());
                }
            }
        }
    }

    /**
     * Remove patient from all data structures
     */
    private void removeFromAllDataStructures(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return;
        }

        // Find and remove from list
        java.util.Iterator<Patient> iterator = patientList.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            Patient current = iterator.next();
            if (current != null && current.getId() != null && current.getId().equals(patient.getId())) {
                patientList.remove(index);
                break;
            }
            index++;
        }

        // Remove from all maps
        synchronized (this) {
            if (patient.getId() != null) {
                patientMap.remove(patient.getId());
            }
            if (patient.getIcNumber() != null) {
                icMap.remove(patient.getIcNumber());
            }
            if (patient.getName() != null) {
                nameMap.remove(patient.getName().toLowerCase());
            }
        }

        queueMap.remove(patient.getId());

        // Save changes to file
        patientDAO.saveToFile(patientList);
    }

    /**
     * Add patient to queue with tracking
     */
    private void enqueuePatient(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getName() != null && patient.getIcNumber() != null) {
            patientQueue.enqueueWithNumber(patient);
            int newPosition = patientQueue.size();

            synchronized (this) {
                queueMap.put(patient.getId(), true);
                queueNameMap.put(patient.getName().toLowerCase(), patient);
                queueIcMap.put(patient.getIcNumber(), patient);
                queuePositionMap.put(patient.getId(), newPosition);
            }
        }
    }

    /**
     * Remove and return next patient from queue
     */
    private Patient dequeuePatient() {
        if (!patientQueue.isEmpty()) {
            Patient patient = patientQueue.dequeue();
            if (patient != null) {
                synchronized (this) {
                    if (patient.getId() != null) {
                        queueMap.remove(patient.getId());
                        queuePositionMap.remove(patient.getId());
                    }
                    if (patient.getName() != null) {
                        queueNameMap.remove(patient.getName().toLowerCase());
                    }
                    if (patient.getIcNumber() != null) {
                        queueIcMap.remove(patient.getIcNumber());
                    }

                    // Update positions for remaining patients
                    updateQueuePositions();
                }
            }
            return patient;
        }
        return null;
    }

    /**
     * Check if patient is currently in queue
     */
    private boolean isInQueue(Patient patient) {
        return patient != null && patient.getId() != null && queueMap.containsKey(patient.getId());
    }

    /**
     * Clear all queue data and tracking maps
     */
    private void clearAllQueueMaps() {
        patientQueue.clear();
        queueMap.clear();
        queueNameMap.clear();
        queueIcMap.clear();
        queuePositionMap.clear();
    }

    /**
     * Update queue positions after dequeue operation
     */
    private void updateQueuePositions() {
        ListInterface<Patient> currentQueue = ((LinkedQueue<Patient>) patientQueue).createSnapshot();
        queuePositionMap.clear();

        for (int i = 1; i <= currentQueue.getNumberOfEntries(); i++) {
            Patient patient = currentQueue.getEntry(i);
            if (patient != null && patient.getId() != null) {
                queuePositionMap.put(patient.getId(), i);
            }
        }
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
     * Serve next patient in queue
     * @return QueueOperationResult with served patient data
     */
    public QueueOperationResult serveNextPatient() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, "QUEUE_EMPTY", null, null, 0);
        }

        // Get queue number before dequeuing
        Patient nextPatient = patientQueue.peek();
        String queueNum = patientQueue.getQueueNumber(nextPatient);

        // Remove patient from queue
        nextPatient = dequeuePatient();
        if (nextPatient != null) {
            // Find available doctor
            MaintainDoctor maintainDoctor = new MaintainDoctor();
            Doctor availableDoctor = maintainDoctor.findAvailableDoctor();

            if (availableDoctor != null) {
                boolean setOnDuty = maintainDoctor.setDoctorAvailability(availableDoctor.getId(), "On Duty");

                if (setOnDuty) {
                    // Set serving patient for treatment module
                    if (maintainTreatment != null) {
                        maintainTreatment.addServingPatientDoctorPair(nextPatient, availableDoctor);
                    }

                    maintainDoctor.setDoctorAvailability(availableDoctor.getId(), "Available");

                    return new QueueOperationResult(true, "PATIENT_SERVED", nextPatient, queueNum, patientQueue.size());
                } else {
                    // Failed to set doctor on duty - put patient back in queue
                    patientQueue.enqueueWithExistingNumber(nextPatient, queueNum);
                    return new QueueOperationResult(false, "DOCTOR_UNAVAILABLE", null, null, patientQueue.size());
                }

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
     * Get queue data for UI display - Preserves original queue numbers
     * UI layer handles formatting
     * Safe traversal that preserves FIFO order and original queue numbers
     */
    /**
     * Get a read-only snapshot of the queue
     * @return ListInterface containing queue elements in FIFO order
     */
    public ListInterface<Patient> getQueueList() {
        return ((LinkedQueue<Patient>) patientQueue).createSnapshot();
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

        Integer position = queuePositionMap.get(patient.getId());
        return position != null ? position : 0;
    }



    /**
     * Generate next available patient ID by finding highest existing ID
     */
    private String generateNextPatientId() {
        int max = 0;
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
        // Check if patient is in queue
        if (!isInQueue(patient)) {
            return false; // Patient not in queue
        }

        // Find the patient object in the queue
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
            return false;
        }

        // Remove patient from queue
        boolean removed = ((LinkedQueue<Patient>) patientQueue).removeByValue(patientToRemove);

        if (removed) {
            synchronized (this) {
                queueMap.remove(patient.getId());
                queueNameMap.remove(patient.getName().toLowerCase());
                queueIcMap.remove(patient.getIcNumber());
                queuePositionMap.remove(patient.getId());

                // Update positions for remaining patients
                updateQueuePositions();
            }
        }

        return removed;
    }

    /**
     * Check if any patients are registered in the system
     */
    public boolean hasPatients() {
        return patientList.getNumberOfEntries() > 0;
    }


    /**
     * Handle Patient ID sorting options
     * @param choice Sort choice (1=ascending, 2=descending)
     * @return Sorted list for UI to display, empty list for invalid choice
     */
    public ListInterface<Patient> handleSortByIdChoice(int choice) {
        switch (choice) {
            case 1:
                return sortPatientsByID(true);
            case 2:
                return sortPatientsByID(false);
            default:
                return new DoublyLinkedList<>();
        }
    }

    /**
     * Handle Registration Date sorting options
     * @param choice Sort choice (1=ascending, 2=descending)
     * @return Sorted list for UI to display, empty list for invalid choice
     */
    public ListInterface<Patient> handleSortByDateChoice(int choice) {
        switch (choice) {
            case 1:
                return sortPatientsByRegistrationDate(true);  // Ascending (Oldest first)
            case 2:
                return sortPatientsByRegistrationDate(false); // Descending (Newest first)
            default:
                return new DoublyLinkedList<>(); // Return empty list for invalid choice
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
     * Get all registered patients for display
     */
    public ListInterface<Patient> getPatientList() {
        return patientList;
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
     * Find patient by ID
     */
    public Patient findPatientById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return patientMap.get(id.trim());
    }

    /**
     * Find patients by name
     * @param name Patient name to search for
     * @return List of patients with matching names
     */
    /**
     * Find patients by name using exact and partial matching
     * @param name Patient name to search for
     * @return List of patients with matching names
     */
    public ListInterface<Patient> findPatientsByName(String name) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();

        if (name == null || name.trim().isEmpty()) {
            return foundPatients;
        }

        String searchName = name.trim().toLowerCase();

        // Try exact name lookup first
        Patient exactMatch = nameMap.get(searchName);
        if (exactMatch != null && exactMatch.getName() != null) {
            foundPatients.add(exactMatch);
            // If exact match found and it's the complete search term, return immediately
            if (exactMatch.getName().toLowerCase().equals(searchName)) {
                return foundPatients;
            }
        }

        // Search for partial matches if needed
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();

            if (patient != null && patient.getName() != null) {
                String patientName = patient.getName().toLowerCase();
                // Add if contains search term and not already added
                if (patientName.contains(searchName) && !patientName.equals(searchName)) {
                    foundPatients.add(patient);
                }
            }
        }
        return foundPatients;
    }

    /**
     * Find patient by exact name
     * @param name Exact patient name to search for
     * @return Patient if found, null otherwise
     */
    public Patient findPatientByExactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

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
     * Register new patient and return with generated ID
     */
    public Patient registerNewPatient(Patient patient) {
        if (patient == null) {
            return null;
        }

        // Check IC uniqueness
        if (checkExistingPatient(patient.getIcNumber()) != null) {
            return null;
        }

        // Generate unique ID and create patient with today's registration date
        String newId = generateNextPatientId();
        Patient patientWithId = new Patient(newId, patient.getName(), patient.getIcNumber(),
                                          patient.getPhone(), patient.getGender(), java.time.LocalDate.now());

        // Add to all data structures and save to file
        addToAllDataStructures(patientWithId);

        return patientWithId;
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

        // Check if patient is already in queue
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
     * Get number of patients currently waiting in queue
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
     * Sort patients by registration date
     */
    private ListInterface<Patient> sortPatientsByRegistrationDate(boolean ascending) {
        ListInterface<Patient> sortedList = createPatientListCopy();

        sortedList.mergeSort((p1, p2) -> p1.getRegistrationDate().compareTo(p2.getRegistrationDate()));

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

        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by ID
        sortedList.mergeSort((p1, p2) -> p1.getId().compareTo(p2.getId()));

        // Reverse for descending order
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

        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by name (case-insensitive)
        sortedList.mergeSort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

        return sortedList;
    }

    /**
     * Sort patients by Age (ascending order only)
     * @return sorted list of patients by age (youngest first)
     */
    private ListInterface<Patient> sortPatientsByAge() {

        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by age
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
    /**
     * Create copy of patient list
     */
    private ListInterface<Patient> createPatientListCopy() {
        ListInterface<Patient> copyList = new DoublyLinkedList<>();


        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            copyList.add(iterator.next());
        }
        return copyList;
    }


    /**
     * Find patient in queue by name
     */
    private Patient findPatientInQueueByName(String name) {
        return queueNameMap.get(name.toLowerCase());
    }

    /**
     * Find patient in queue by IC number
     */
    private Patient findPatientInQueueByIC(String ic) {
        String searchIC = ic.replaceAll("-", "");
        return queueIcMap.get(searchIC);
    }




    /**
     * Generate patient treatment statistics data using custom ADT sorting
     * @return PatientTreatmentData object with treatment statistics
     */
    public PatientTreatmentData generatePatientTreatmentData() {
        if (patientList.isEmpty()) {
            return null;
        }

        // Create sorted list of patients by ID
        ListInterface<Patient> sortedPatients = createPatientListCopy();
        sortedPatients.mergeSort((p1, p2) -> p1.getId().compareToIgnoreCase(p2.getId()));

        // Build treatment statistics
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
     * Check if patients exist for generating reports
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

        // Get first and last patients
        Patient firstPatient = sortedList.getEntry(1);
        Patient lastPatient = sortedList.getEntry(sortedList.getNumberOfEntries());

        // Count gender distribution
        int maleCount = 0, femaleCount = 0;
        java.util.Iterator<Patient> genderIterator = sortedList.iterator();
        while (genderIterator.hasNext()) {
            if (genderIterator.next().getGender().equalsIgnoreCase("Male")) {
                maleCount++;
            } else {
                femaleCount++;
            }
        }

        // Calculate monthly breakdown
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

        // Getter methods for accessing report data
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

        return sortPatientsByRegistrationDate(true);
    }
    /**
     * Book new appointment with validation
     */
    public AppointmentResult bookAppointment(String patientId, String doctorId, String timeString) {

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return new AppointmentResult(false, "Patient not found. Please register the patient first.", null);
        }

        Doctor doctor = maintainDoctor.findDoctorById(doctorId); // Validate doctor exists
        if (doctor == null) {
            return new AppointmentResult(false, "Doctor not found.", null);
        }

        if (!"Available".equalsIgnoreCase(doctor.getAvailability())) { // Check doctor availability
            return new AppointmentResult(false, "Doctor is not available for appointments.", null);
        }

        LocalTime appointmentTime; // Parse and validate time
        try {
            appointmentTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return new AppointmentResult(false, "Invalid time format. Please use HH:mm format (e.g., 14:30).", null);
        }

        if (appointmentTime.isBefore(LocalTime.of(9, 0)) || appointmentTime.isAfter(LocalTime.of(17, 0))) { // Check business hours
            return new AppointmentResult(false, "Appointment time must be between 09:00 and 17:00.", null);
        }

        if (hasTimeConflict(doctorId, LocalDate.now(), appointmentTime)) { // Check time conflicts with 30-minute gap
            return new AppointmentResult(false, "Doctor has another appointment within 30 minutes.", null);
        }

        String appointmentId = generateAppointmentId(); // Generate unique ID

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

    public boolean isDoctorValid(String doctorId) { // Check if doctor exists
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return false;
        }
        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        return doctor != null;
    }

    public boolean isDoctorAvailable(String doctorId) { // Check if doctor is available for appointments
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return false;
        }
        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        return doctor != null && "Available".equalsIgnoreCase(doctor.getAvailability());
    }

    /**
     * Check if doctor has time conflict
     * @param doctorId Doctor ID
     * @param date Appointment date
     * @param time Appointment time
     * @return true if conflict exists, false otherwise
     */
    private boolean hasTimeConflict(String doctorId, LocalDate date, LocalTime time) { // Check for time conflicts with 30-minute gap validation
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment existing = iterator.next();
            if (existing.getDoctorId().equals(doctorId) &&
                existing.getAppointmentDate().equals(date)) {

                LocalTime existingTime = existing.getAppointmentTime();

                // Check if new appointment is within 30 minutes of existing appointment
                if (isWithinThirtyMinutes(time, existingTime)) {
                    return true; // Conflict: appointments too close together
                }
            }
        }
        return false;
    }

    private boolean isWithinThirtyMinutes(LocalTime time1, LocalTime time2) { // Check if two times are within 30 minutes of each other
        long minutesBetween = Math.abs(java.time.Duration.between(time1, time2).toMinutes());
        return minutesBetween < 30; // Return true if less than 30 minutes apart
    }

    /**
     * Generate unique appointment ID
     * @return New appointment ID
     */
    private String generateAppointmentId() {
        int maxId = 0;

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
     * Get appointments for today sorted by time
     * @return List of today's appointments sorted by appointment time
     */
    public ListInterface<Appointment> getTodayAppointments() {
        ListInterface<Appointment> todayAppointments = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();

        // Use iterator for efficient O(n) traversal instead of O(n²) getEntry() calls
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getAppointmentDate().equals(today)) {
                todayAppointments.add(appointment);
            }
        }

        // Sort appointments by time
        if (!todayAppointments.isEmpty()) {
            todayAppointments.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
        }

        return todayAppointments;
    }

    /**
     * Cancel an appointment
     * @param appointmentId Appointment ID to cancel
     * @return AppointmentResult with success status and message
     */
    public AppointmentResult cancelAppointment(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return new AppointmentResult(false, "Appointment ID cannot be empty.", null);
        }

        // Find and remove the appointment from the list
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment appointment = appointmentList.getEntry(i);
            if (appointment != null && appointment.getAppointmentId().equals(appointmentId.trim())) {
                // Store appointment details before removal
                Appointment removedAppointment = appointment;

                // Remove from list
                appointmentList.remove(i);
                appointmentDAO.saveToFile(appointmentList);

                return new AppointmentResult(true, "Appointment cancelled successfully.", removedAppointment);
            }
        }

        return new AppointmentResult(false, "Appointment not found.", null);
    }

    /**
     * Get all appointments sorted by date and time (for cancellation display)
     * @return List of all appointments sorted by date and time
     */
    public ListInterface<Appointment> getAllAppointments() {

        ListInterface<Appointment> sortedAppointments = new DoublyLinkedList<>();


        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            sortedAppointments.add(iterator.next());
        }

        // Sort appointments by date and time
        if (!sortedAppointments.isEmpty()) {
            sortedAppointments.mergeSort((a1, a2) -> {
                int dateComparison = a1.getAppointmentDate().compareTo(a2.getAppointmentDate());
                if (dateComparison != 0) {
                    return dateComparison;
                }
                // Compare by time if same date
                return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
            });
        }

        return sortedAppointments;
    }



    /**
     * Get sorted appointments by specified criteria
     * @param appointments Original appointment list
     * @param sortType 1=by ID, 2=by Time, other=no sort
     * @return Sorted appointment list
     */
    public ListInterface<Appointment> getSortedAppointments(ListInterface<Appointment> appointments, int sortType) {

        ListInterface<Appointment> sortedList = new DoublyLinkedList<>();


        java.util.Iterator<Appointment> iterator = appointments.iterator();
        while (iterator.hasNext()) {
            sortedList.add(iterator.next());
        }

        switch (sortType) {
            case 1:
                // Sort by Appointment ID
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentId().compareTo(a2.getAppointmentId()));
                break;
            case 2:
                // Sort by Time
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
                break;
            default:
                // No sorting
                break;
        }

        return sortedList;
    }

    /**
     * Clear all appointments - Since appointments are only for current date, clear entire appointment.dat
     * @return AppointmentResult with success status and message
     */
    public AppointmentResult clearAllAppointments() {
        int totalAppointments = appointmentList.getNumberOfEntries();

        if (totalAppointments == 0) {
            return new AppointmentResult(false, "No appointments found to clear.", null);
        }

        // Clear all appointments from the list
        appointmentList.clear();

        // Save empty list to file (clears appointment.dat)
        appointmentDAO.saveToFile(appointmentList);

        String message = String.format("Successfully cleared all %d appointment(s) from the system.",
                                     totalAppointments);
        return new AppointmentResult(true, message, null);
    }

    /**
     * Search appointments by patient ID
     * @param patientId Patient ID to search appointments for
     * @return List of appointments for the specified patient
     */
    public ListInterface<Appointment> searchAppointmentsByPatientId(String patientId) {
        ListInterface<Appointment> patientAppointments = new DoublyLinkedList<>();

        if (patientId == null || patientId.trim().isEmpty()) {
            return patientAppointments; // Return empty list
        }


        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return patientAppointments;
        }

        // Find all appointments for this patient
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getPatientId().equals(patientId.trim())) {
                patientAppointments.add(appointment);
            }
        }

        // Sort appointments by time
        if (!patientAppointments.isEmpty()) {
            patientAppointments.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
        }

        return patientAppointments;
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

        public boolean isSuccess() { 
            return success; 
        }
        public String getMessage() { 
            return message; 
        }
        public Appointment getAppointment() { 
            return appointment; 
        }
    }
}
