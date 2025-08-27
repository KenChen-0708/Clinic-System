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

/**
 * Patient Management Control Layer - ECB Architecture Compliant
 *
 * This class handles core patient business logic with clean separation of concerns:
 * - Patient registration, updates, and deletion
 * - Queue management with FIFO operations
 * - Appointment booking and management
 * - Patient search and sorting operations
 * - Report generation
 *
 * Design Philosophy (Like MaintainPharmacy):
 * - Simple, atomic operations without complex synchronization
 * - Clean ECB separation - no UI logic in control layer
 * - Appropriate data structures for the problem domain
 * - Focused single responsibility per method
 * - Efficient HashMap lookups for performance where needed
 *
 * @author Patient Module Team
 * @version 3.0 - ECB Compliant
 */
public class MaintainPatient {

    // ==================== DATA ACCESS LAYER ====================

    // Patient data management
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();
    private final MaintainTreatment maintainTreatment = new MaintainTreatment();

    // Appointment management
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ListInterface<Appointment> appointmentList = appointmentDAO.retrieveFromFile();
    private final MaintainDoctor maintainDoctor = new MaintainDoctor();

    // ==================== CORE DATA STRUCTURES ====================

    // Patient queue for managing waiting patients in FIFO order
    private final QueueInterface<Patient> patientQueue = new LinkedQueue<>();

    // Essential lookup maps for performance optimization
    private final MapInterface<String, Patient> patientMap = new HashMap<>(); // Primary lookup by patient ID
    private final MapInterface<String, Patient> icMap = new HashMap<>(); // Secondary lookup by IC number

    // Efficient queue tracking with O(1) position lookups
    private final MapInterface<String, Boolean> queueMap = new HashMap<>(); // Track queue membership efficiently
    private final MapInterface<String, Integer> queuePositionMap = new HashMap<>(); // O(1) position lookups

    // ==================== ERROR CODES FOR ECB COMPLIANCE ====================

    /**
     * Error codes for business operations - UI messages handled by boundary layer
     */
    public enum OperationResult {
        SUCCESS,
        VALID,
        INVALID_INPUT,
        PATIENT_NOT_FOUND,
        PATIENT_ALREADY_EXISTS,
        ALREADY_IN_QUEUE,
        QUEUE_EMPTY,
        DOCTOR_NOT_FOUND,
        DOCTOR_UNAVAILABLE,
        INVALID_TIME_FORMAT,
        INVALID_BUSINESS_HOURS,
        APPOINTMENT_NOT_FOUND,
        INVALID_IC_FORMAT,
        NAME_REQUIRED,
        NAME_TOO_SHORT,
        NAME_TOO_LONG,
        IC_REQUIRED,
        PHONE_REQUIRED,
        INVALID_PHONE_FORMAT,
        GENDER_REQUIRED,
        INVALID_GENDER,
        OPERATION_FAILED,
        INVALID_ID,
        PATIENT_SERVED,
        NO_AVAILABLE_DOCTOR,
        QUEUE_ALREADY_EMPTY,
        QUEUE_CLEARED
    }

    // ==================== INITIALIZATION ====================

    /**
     * Initialize MaintainPatient with proper error handling
     * ECB COMPLIANT: No UI output in control layer
     */
    public MaintainPatient() {
        try {
            populateMaps();
        } catch (Exception e) {
            // Initialize empty maps to prevent null pointer exceptions
            // Error logging should be handled by calling layer
            initializeEmptyMaps();
        }
    }

    /**
     * Initialize empty maps as fallback if data loading fails
     * ECB COMPLIANT: Simple, focused initialization
     */
    private void initializeEmptyMaps() {
        patientMap.clear();
        icMap.clear();
        queueMap.clear();
        queuePositionMap.clear();
    }

    /**
     * Initialize lookup maps from patient list for fast searching
     * @throws RuntimeException if critical data loading fails
     */
    /**
     * Initialize essential lookup maps from patient list
     * ECB COMPLIANT: Simple, focused data structure initialization
     */
    private void populateMaps() {
        try {
            patientMap.clear();
            icMap.clear();

            if (patientList == null) {
                throw new RuntimeException("Patient list is null - cannot initialize maps");
            }

            java.util.Iterator<Patient> iterator = patientList.iterator();
            while (iterator.hasNext()) {
                Patient patient = iterator.next();
                if (isValidPatientForMapping(patient)) {
                    patientMap.put(patient.getId(), patient);
                    icMap.put(patient.getIcNumber(), patient);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to populate patient maps: " + e.getMessage(), e);
        }
    }

    /**
     * Validate patient data for map insertion
     * @param patient Patient to validate
     * @return true if patient has all required fields
     */
    private boolean isValidPatientForMapping(Patient patient) {
        return patient != null &&
               patient.getId() != null && !patient.getId().trim().isEmpty() &&
               patient.getIcNumber() != null && !patient.getIcNumber().trim().isEmpty() &&
               patient.getName() != null && !patient.getName().trim().isEmpty();
    }

    // ==================== CORE BUSINESS OPERATIONS ====================

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

        synchronized (this) {
            // Store backup data for rollback - declare outside try block for catch access
            Patient backupPatientFromMap = patientMap.get(oldPatient.getId());
            Patient backupPatientFromIcMap = icMap.get(oldPatient.getIcNumber());

            try {

                // Update patient in the list safely without iterator modification
                boolean listUpdated = false;

                // Safe approach: find index first, then modify
                for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
                    Patient current = patientList.getEntry(i);
                    if (current != null && current.getId() != null && current.getId().equals(oldPatient.getId())) {
                        patientList.remove(i);
                        patientList.add(i, newPatient);
                        listUpdated = true;
                        break;
                    }
                }

                if (!listUpdated) {
                    return false;
                }

                // Update lookup maps atomically - add new before removing old
                patientMap.put(newPatient.getId(), newPatient);
                icMap.put(newPatient.getIcNumber(), newPatient);

                // Only remove old entries after new ones are successfully added
                if (!oldPatient.getId().equals(newPatient.getId())) {
                    patientMap.remove(oldPatient.getId());
                }
                if (!oldPatient.getIcNumber().equals(newPatient.getIcNumber())) {
                    icMap.remove(oldPatient.getIcNumber());
                }

                // Save to file - simple transaction like MaintainPharmacy
                patientDAO.saveToFile(patientList);
                return true;

            } catch (Exception e) {
                // Rollback on failure - restore original state
                try {
                    // Restore list if it was modified
                    for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
                        Patient current = patientList.getEntry(i);
                        if (current != null && current.getId() != null && current.getId().equals(newPatient.getId())) {
                            patientList.remove(i);
                            patientList.add(i, oldPatient);
                            break;
                        }
                    }

                    // Restore maps
                    if (backupPatientFromMap != null) {
                        patientMap.put(oldPatient.getId(), backupPatientFromMap);
                    }
                    if (backupPatientFromIcMap != null) {
                        icMap.put(oldPatient.getIcNumber(), backupPatientFromIcMap);
                    }
                } catch (Exception rollbackException) {
                    // Log rollback failure but don't throw
                }
                return false;
            }
        }
    }

    /**
     * Add patient to all data structures with thread safety
     * ECB COMPLIANT: Thread-safe atomic operation like MaintainPharmacy
     */
    private void addToAllDataStructures(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
            synchronized (this) {
                try {
                    // Add to main list
                    patientList.add(patient);

                    // Update lookup maps for performance
                    patientMap.put(patient.getId(), patient);
                    icMap.put(patient.getIcNumber(), patient);

                    // Save to file - simple transaction like MaintainPharmacy
                    patientDAO.saveToFile(patientList);
                } catch (Exception e) {
                    // Simple rollback - remove from list and maps
                    patientList.remove(patientList.getNumberOfEntries());
                    patientMap.remove(patient.getId());
                    icMap.remove(patient.getIcNumber());
                    throw new RuntimeException("Failed to add patient: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Remove patient from all data structures
     * ECB COMPLIANT: Safe removal without iterator modification issues
     */
    private void removeFromAllDataStructures(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return;
        }

        // Find and remove from list - safe approach without iterator modification
        Patient patientToRemove = null;
        int removeIndex = -1;

        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient current = patientList.getEntry(i);
            if (current != null && current.getId() != null && current.getId().equals(patient.getId())) {
                patientToRemove = current;
                removeIndex = i;
                break;
            }
        }

        // Remove from list if found
        if (removeIndex != -1) {
            patientList.remove(removeIndex);
        }

        // Remove from all maps
        synchronized (this) {
            if (patient.getId() != null) {
                patientMap.remove(patient.getId());
            }
            if (patient.getIcNumber() != null) {
                icMap.remove(patient.getIcNumber());
            }
            // Name map removed for simplicity - using linear search when needed
        }

        queueMap.remove(patient.getId());

        // Save changes to file
        patientDAO.saveToFile(patientList);
    }

    /**
     * Add patient to queue with efficient position tracking and thread safety
     * ECB COMPLIANT: Thread-safe atomic operation with O(1) position tracking
     */
    private void enqueuePatient(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getName() != null && patient.getIcNumber() != null) {
            synchronized (this) {
                int position = patientQueue.size() + 1;
                patientQueue.enqueueWithNumber(patient);
                queueMap.put(patient.getId(), true);
                queuePositionMap.put(patient.getId(), position); // O(1) position tracking
            }
        }
    }

    /**
     * Remove and return next patient from queue with thread safety
     * ECB COMPLIANT: Thread-safe efficient operation with optimized position updates
     */
    private Patient dequeuePatient() {
        synchronized (this) {
            if (!patientQueue.isEmpty()) {
                Patient patient = patientQueue.dequeue();
                if (patient != null && patient.getId() != null) {
                    queueMap.remove(patient.getId());
                    queuePositionMap.remove(patient.getId());
                    // Efficiently decrement all remaining positions by 1
                    decrementQueuePositions();
                }
                return patient;
            }
            return null;
        }
    }

    /**
     * Check if patient is currently in queue with thread safety
     * Note: This is called from synchronized methods, so synchronization is inherited
     */
    private boolean isInQueue(Patient patient) {
        return patient != null && patient.getId() != null && queueMap.containsKey(patient.getId());
    }

    /**
     * Clear all queue data
     * ECB COMPLIANT: Simple, focused operation
     */
    private void clearAllQueueMaps() {
        patientQueue.clear();
        queueMap.clear();
        queuePositionMap.clear();
    }

    /**
     * Efficiently decrement all queue positions by 1 after dequeue
     * True O(k) operation where k is number of patients in queue
     */
    private void decrementQueuePositions() {
        try {
            // Single pass: iterate through queue and update positions directly
            ListInterface<Patient> currentQueue = patientQueue.createSnapshot();
            int newPosition = 1;

            java.util.Iterator<Patient> iterator = currentQueue.iterator();
            while (iterator.hasNext()) {
                Patient patient = iterator.next();
                if (patient != null && patient.getId() != null) {
                    queuePositionMap.put(patient.getId(), newPosition);
                    newPosition++;
                }
            }
        } catch (Exception e) {
            // Fallback to full rebuild if needed
            updateQueuePositions();
        }
    }

    /**
     * Full queue position rebuild - used as fallback only
     * Maintains O(1) position lookups by updating position map
     */
    private void updateQueuePositions() {
        try {
            ListInterface<Patient> currentQueue = patientQueue.createSnapshot();
            queuePositionMap.clear();

            int position = 1;
            java.util.Iterator<Patient> iterator = currentQueue.iterator();
            while (iterator.hasNext()) {
                Patient patient = iterator.next();
                if (patient != null && patient.getId() != null) {
                    queuePositionMap.put(patient.getId(), position);
                    position++;
                }
            }
        } catch (Exception e) {
            // Silently handle error to avoid breaking queue operations
        }
    }



    /**
     * Add patient to queue by ID with thread safety
     * @param patientId ID of patient to add
     * @return QueueOperationResult with status and data
     */
    public synchronized QueueOperationResult addPatientToQueueById(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return new QueueOperationResult(false, OperationResult.INVALID_ID, null, null, 0);
        }

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return new QueueOperationResult(false, OperationResult.PATIENT_NOT_FOUND, null, null, 0);
        }

        if (isPatientInQueue(patient)) {
            return new QueueOperationResult(false, OperationResult.ALREADY_IN_QUEUE, patient, null, getQueueSize());
        }

        String queueNum = addPatientToQueue(patient);
        if (queueNum != null) {
            return new QueueOperationResult(true, OperationResult.SUCCESS, patient, queueNum, getQueueSize());
        }

        return new QueueOperationResult(false, OperationResult.OPERATION_FAILED, patient, null, getQueueSize());
    }

    /**
     * Serve next patient in queue with thread safety
     * @return QueueOperationResult with served patient data
     */
    public synchronized QueueOperationResult serveNextPatient() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, OperationResult.QUEUE_EMPTY, null, null, 0);
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

                    return new QueueOperationResult(true, OperationResult.PATIENT_SERVED, nextPatient, queueNum, patientQueue.size());
                } else {
                    // Failed to set doctor on duty - put patient back in queue
                    patientQueue.enqueueWithExistingNumber(nextPatient, queueNum);
                    return new QueueOperationResult(false, OperationResult.DOCTOR_UNAVAILABLE, null, null, patientQueue.size());
                }

            } else {
                // No available doctor - put patient back in queue
                enqueuePatient(nextPatient);
                return new QueueOperationResult(false, OperationResult.NO_AVAILABLE_DOCTOR, null, null, patientQueue.size());
            }
        }
        return new QueueOperationResult(false, OperationResult.OPERATION_FAILED, null, null, patientQueue.size());
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
     * Clear queue with thread safety
     * @return QueueOperationResult with clear operation data
     */
    public synchronized QueueOperationResult clearQueueLogic() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, OperationResult.QUEUE_ALREADY_EMPTY, null, null, 0);
        }

        int clearedCount = patientQueue.size();
        clearAllQueueMaps();
        return new QueueOperationResult(true, OperationResult.QUEUE_CLEARED, null, null, clearedCount);
    }

    /**
     * Get queue data for UI display - Preserves original queue numbers
     * UI layer handles formatting
     * Safe traversal that preserves FIFO order and original queue numbers
     */
    /**
     * Get a read-only snapshot of the queue
     * ECB COMPLIANT: Uses interface method, no casting required
     * @return ListInterface containing queue elements in FIFO order
     */
    public ListInterface<Patient> getQueueList() {
        return patientQueue.createSnapshot(); // No casting needed - uses interface method
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
     * Get patient's position in queue (1-based) with O(1) lookup
     * ECB COMPLIANT: Efficient operation using position map
     * @param patient Patient to check
     * @return queue position or 0 if not in queue
     */
    public int getPatientQueuePosition(Patient patient) {
        if (patient == null || !isInQueue(patient)) {
            return 0;
        }

        // O(1) position lookup using position map
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

    /**
     * Remove patient from queue
     * ECB COMPLIANT: Simple operation like MaintainPharmacy
     */
    private boolean removePatientFromQueue(Patient patient) {
        if (!isInQueue(patient)) {
            return false;
        }

        // Simple queue removal
        boolean removed = ((LinkedQueue<Patient>) patientQueue).removeByValue(patient);

        if (removed) {
            queueMap.remove(patient.getId()); // Simple cleanup
        }

        return removed;
    }

    // ==================== PATIENT INFORMATION SERVICES ====================

    /**
     * Check if any patients are registered in the system
     */
    public boolean hasPatients() {
        return patientList.getNumberOfEntries() > 0;
    }

    // ==================== PATIENT SORTING SERVICES ====================

    /**
     * Sort patients by ID in ascending order
     * @return Sorted list of patients by ID (A-Z)
     */
    public ListInterface<Patient> sortPatientsByIdAscending() {
        return sortPatientsByID(true);
    }

    /**
     * Sort patients by ID in descending order
     * @return Sorted list of patients by ID (Z-A)
     */
    public ListInterface<Patient> sortPatientsByIdDescending() {
        return sortPatientsByID(false);
    }

    /**
     * Sort patients by registration date in ascending order (oldest first)
     * @return Sorted list of patients by registration date
     */
    public ListInterface<Patient> sortPatientsByDateAscending() {
        return sortPatientsByRegistrationDate(true);
    }

    /**
     * Sort patients by registration date in descending order (newest first)
     * @return Sorted list of patients by registration date
     */
    public ListInterface<Patient> sortPatientsByDateDescending() {
        return sortPatientsByRegistrationDate(false);
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
     * Queue operation result class - ECB COMPLIANT
     * Uses OperationResult enum for consistency
     */
    public static class QueueOperationResult {
        private final boolean success;
        private final OperationResult resultCode;
        private final Patient patient;
        private final String queueNumber;
        private final int totalInQueue;

        public QueueOperationResult(boolean success, OperationResult resultCode, Patient patient, String queueNumber, int totalInQueue) {
            this.success = success;
            this.resultCode = resultCode;
            this.patient = patient;
            this.queueNumber = queueNumber;
            this.totalInQueue = totalInQueue;
        }

        public boolean isSuccess() {
            return success;
        }
        public OperationResult getResultCode() {
            return resultCode;
        }
        public String getStatusCode() {
            return resultCode.toString(); // Backward compatibility
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





    // ==================== PATIENT SEARCH SERVICES ====================

    /**
     * Find patient by ID using optimized HashMap lookup
     * ECB COMPLIANT: Consistent error handling like MaintainPharmacy
     * @param id Patient ID to search for
     * @return Patient object if found, null otherwise (consistent with DAO pattern)
     */
    public Patient findPatientById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null; // Consistent with DAO pattern - null for not found
        }
        return patientMap.get(id.trim());
    }

    /**
     * Find patients by name using hybrid search strategy
     *
     * This method uses a two-tier approach:
     * 1. First attempts instant HashMap lookup for exact matches
     * 2. Falls back to iterator-based partial matching for broader results
     *
     * @param name Patient name to search for (case-insensitive)
     * @return ListInterface containing all matching patients, empty if none found
     * @throws IllegalArgumentException if name is null or empty
     */
    public ListInterface<Patient> findPatientsByName(String name) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();

        // Input validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be null or empty");
        }

        String searchName = name.trim().toLowerCase();

        // Simple linear search through patient list - appropriate for typical patient counts
        // ECB COMPLIANT: Single responsibility, simple approach like MaintainPharmacy
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getName() != null) {
                String patientName = patient.getName().toLowerCase();
                if (patientName.contains(searchName)) {
                    foundPatients.add(patient);
                }
            }
        }

        return foundPatients;
    }

    /**
     * Find patient by exact name using simple linear search
     * ECB COMPLIANT: Simple approach like MaintainPharmacy
     * @param name Exact patient name to search for
     * @return Patient if found, null otherwise
     */
    public Patient findPatientByExactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchName = name.trim().toLowerCase();
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getName() != null &&
                patient.getName().toLowerCase().equals(searchName)) {
                return patient;
            }
        }
        return null;
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

    // ==================== PATIENT REGISTRATION SERVICES ====================

    /**
     * Register new patient with comprehensive validation
     * @param patient Patient data to register
     * @return Registered patient with generated ID, or null if registration fails
     * @throws IllegalArgumentException if patient data is invalid
     */
    public Patient registerNewPatient(Patient patient) {
        // Comprehensive input validation
        ValidationResult validation = validatePatientForRegistration(patient);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Patient registration failed: " + validation.getErrorMessage());
        }

        // Check IC uniqueness
        if (checkExistingPatient(patient.getIcNumber()) != null) {
            throw new IllegalArgumentException("PATIENT_ALREADY_EXISTS");
        }

        try {
            // Generate unique ID and create patient with today's registration date
            String newId = generateNextPatientId();
            Patient patientWithId = new Patient(newId, patient.getName().trim(), patient.getIcNumber().trim(),
                                              patient.getPhone().trim(), patient.getGender(), java.time.LocalDate.now());

            // Add to all data structures with rollback capability
            addToAllDataStructures(patientWithId);
            return patientWithId;

        } catch (Exception e) {
            throw new RuntimeException("Failed to register patient: " + e.getMessage(), e);
        }
    }

    /**
     * Check if patient is already in queue with thread safety
     * @param patient Patient to check
     * @return true if patient is in queue, false otherwise
     */
    public synchronized boolean isPatientInQueue(Patient patient) {
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
     * Find patient in queue by name using simple linear search
     * ECB COMPLIANT: Simple approach like MaintainPharmacy
     */
    private Patient findPatientInQueueByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchName = name.toLowerCase();
        ListInterface<Patient> queueList = getQueueList();

        // Use efficient iterator instead of O(n²) getEntry() calls
        java.util.Iterator<Patient> iterator = queueList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getName() != null &&
                patient.getName().toLowerCase().equals(searchName)) {
                return patient;
            }
        }
        return null;
    }

    /**
     * Find patient in queue by IC number using simple linear search
     * ECB COMPLIANT: Simple approach like MaintainPharmacy
     */
    private Patient findPatientInQueueByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return null;
        }

        String searchIC = ic.replaceAll("-", "");
        ListInterface<Patient> queueList = getQueueList();

        // Use efficient iterator instead of O(n²) getEntry() calls
        java.util.Iterator<Patient> iterator = queueList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getIcNumber() != null &&
                patient.getIcNumber().replaceAll("-", "").equals(searchIC)) {
                return patient;
            }
        }
        return null;
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

        // Get first and last patients using efficient access
        Patient firstPatient = null;
        Patient lastPatient = null;

        if (sortedList.getNumberOfEntries() > 0) {
            java.util.Iterator<Patient> iterator = sortedList.iterator();
            if (iterator.hasNext()) {
                firstPatient = iterator.next();
                lastPatient = firstPatient; // Initialize with first patient

                // Find last patient efficiently
                while (iterator.hasNext()) {
                    lastPatient = iterator.next();
                }
            }
        }

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
     * Uses the standard ascending sort method for consistency
     * @return sorted list of patients by registration date (oldest first)
     */
    private ListInterface<Patient> sortPatientsByDate() {
        return sortPatientsByDateAscending(); // Use standard method to avoid duplication
    }

    // ==================== APPOINTMENT MANAGEMENT SERVICES ====================

    /**
     * Book new appointment with comprehensive validation
     * ECB COMPLIANT: Returns error codes, UI messages handled by boundary layer
     * @param patientId Patient ID for the appointment
     * @param doctorId Doctor ID for the appointment
     * @param timeString Appointment time in HH:mm format
     * @return AppointmentResult with success status and error code
     */
    public AppointmentResult bookAppointment(String patientId, String doctorId, String timeString) {

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return new AppointmentResult(false, OperationResult.PATIENT_NOT_FOUND, null);
        }

        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        if (doctor == null) {
            return new AppointmentResult(false, OperationResult.DOCTOR_NOT_FOUND, null);
        }

        if (!"Available".equalsIgnoreCase(doctor.getAvailability())) {
            return new AppointmentResult(false, OperationResult.DOCTOR_UNAVAILABLE, null);
        }

        LocalTime appointmentTime;
        try {
            appointmentTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return new AppointmentResult(false, OperationResult.INVALID_TIME_FORMAT, null);
        }

        if (appointmentTime.isBefore(LocalTime.of(9, 0)) || appointmentTime.isAfter(LocalTime.of(17, 0))) {
            return new AppointmentResult(false, OperationResult.INVALID_BUSINESS_HOURS, null);
        }

        if (hasTimeConflict(doctorId, LocalDate.now(), appointmentTime)) {
            return new AppointmentResult(false, OperationResult.DOCTOR_UNAVAILABLE, null);
        }

        String appointmentId = generateAppointmentId(); // Generate unique ID

        // Create appointment
        Appointment appointment = new Appointment(appointmentId, patientId, doctorId, appointmentTime);

        // Add to list and save
        appointmentList.add(appointment);
        appointmentDAO.saveToFile(appointmentList);

        return new AppointmentResult(true, OperationResult.SUCCESS, appointment);
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
            return new AppointmentResult(false, OperationResult.INVALID_INPUT, null);
        }

        // Find and remove the appointment using efficient iterator
        int index = 1;
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment != null && appointment.getAppointmentId().equals(appointmentId.trim())) {
                // Store appointment details before removal
                Appointment removedAppointment = appointment;

                // Remove from list
                appointmentList.remove(index);
                appointmentDAO.saveToFile(appointmentList);

                return new AppointmentResult(true, OperationResult.SUCCESS, removedAppointment);
            }
            index++;
        }

        return new AppointmentResult(false, OperationResult.APPOINTMENT_NOT_FOUND, null);
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
            return new AppointmentResult(false, OperationResult.APPOINTMENT_NOT_FOUND, null);
        }

        // Clear all appointments from the list
        appointmentList.clear();

        // Save empty list to file (clears appointment.dat)
        appointmentDAO.saveToFile(appointmentList);

        return new AppointmentResult(true, OperationResult.SUCCESS, null);
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
     * Appointment booking result class - ECB COMPLIANT
     * Uses error codes instead of UI messages
     */
    public static class AppointmentResult {
        private final boolean success;
        private final OperationResult resultCode;
        private final Appointment appointment;

        public AppointmentResult(boolean success, OperationResult resultCode, Appointment appointment) {
            this.success = success;
            this.resultCode = resultCode;
            this.appointment = appointment;
        }

        public boolean isSuccess() {
            return success;
        }
        public OperationResult getResultCode() {
            return resultCode;
        }
        public Appointment getAppointment() {
            return appointment;
        }
    }

    /**
     * Comprehensive validation for patient registration - like MaintainPharmacy
     * ECB COMPLIANT: Thorough validation with clear error codes
     * @param patient Patient to validate
     * @return ValidationResult with success status and error details
     */
    private ValidationResult validatePatientForRegistration(Patient patient) {
        if (patient == null) {
            return new ValidationResult(false, OperationResult.INVALID_INPUT);
        }

        // Validate name - comprehensive checks like MaintainPharmacy
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return new ValidationResult(false, OperationResult.NAME_REQUIRED);
        }
        if (patient.getName().trim().length() < 2) {
            return new ValidationResult(false, OperationResult.NAME_TOO_SHORT);
        }
        if (patient.getName().trim().length() > 100) {
            return new ValidationResult(false, OperationResult.NAME_TOO_LONG);
        }

        // Validate IC number - comprehensive checks
        if (patient.getIcNumber() == null || patient.getIcNumber().trim().isEmpty()) {
            return new ValidationResult(false, OperationResult.IC_REQUIRED);
        }
        if (!isValidIcFormat(patient.getIcNumber())) {
            return new ValidationResult(false, OperationResult.INVALID_IC_FORMAT);
        }

        // Validate phone number - comprehensive checks
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            return new ValidationResult(false, OperationResult.PHONE_REQUIRED);
        }
        if (!isValidPhoneFormat(patient.getPhone())) {
            return new ValidationResult(false, OperationResult.INVALID_PHONE_FORMAT);
        }

        // Validate gender - comprehensive checks
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            return new ValidationResult(false, OperationResult.GENDER_REQUIRED);
        }
        if (!isValidGender(patient.getGender())) {
            return new ValidationResult(false, OperationResult.INVALID_GENDER);
        }

        return new ValidationResult(true, OperationResult.VALID);
    }

    /**
     * Comprehensive IC format validation - like MaintainPharmacy validation patterns
     * @param ic IC number to validate
     * @return true if format is valid
     */
    private boolean isValidIcFormat(String ic) {
        if (ic == null) return false;
        String cleanIc = ic.replaceAll("-", "").trim();
        return cleanIc.length() == 12 && cleanIc.matches("\\d+");
    }

    /**
     * Phone number format validation
     * @param phone Phone number to validate
     * @return true if format is valid
     */
    private boolean isValidPhoneFormat(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[\\s-()]", "").trim();
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15 && cleanPhone.matches("\\d+");
    }

    /**
     * Gender validation
     * @param gender Gender to validate
     * @return true if gender is valid
     */
    private boolean isValidGender(String gender) {
        if (gender == null) return false;
        String cleanGender = gender.trim().toLowerCase();
        return cleanGender.equals("male") || cleanGender.equals("female") ||
               cleanGender.equals("m") || cleanGender.equals("f");
    }

    /**
     * Inner class for validation results - ECB COMPLIANT
     * Uses error codes instead of UI messages
     */
    private static class ValidationResult {
        private final boolean valid;
        private final OperationResult resultCode;

        public ValidationResult(boolean valid, OperationResult resultCode) {
            this.valid = valid;
            this.resultCode = resultCode;
        }

        public boolean isValid() {
            return valid;
        }

        public OperationResult getResultCode() {
            return resultCode;
        }

        public String getErrorMessage() {
            return resultCode != null ? resultCode.toString() : "UNKNOWN_ERROR";
        }
    }
}
