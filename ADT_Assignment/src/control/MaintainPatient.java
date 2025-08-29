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
import entity.PatientSummaryReport;
import entity.QueueStatusData;
import entity.SearchResult;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.IOException;


public class MaintainPatient {

    // Data access objects
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();
    private final MaintainTreatment maintainTreatment = new MaintainTreatment();

    // Appointment management
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ListInterface<Appointment> appointmentList = appointmentDAO.retrieveFromFile();
    private final MaintainDoctor maintainDoctor = new MaintainDoctor();

    // Queue for appointments
    private final QueueInterface<Patient> patientQueue = new LinkedQueue<>();

    // Maps for efficient lookup
    private final MapInterface<String, Patient> patientMap = new HashMap<>(); // Fast lookup by patient ID
    private final MapInterface<String, Patient> icMap = new HashMap<>(); // Fast lookup by cleaned IC number
    private final MapInterface<String, ListInterface<Patient>> nameMap = new HashMap<>(); // Map name key to list of patients

    // Maps for queue lookups
    private final MapInterface<String, Patient> queuePatientMap = new HashMap<>(); // Track who is in queue by patient ID
    private final MapInterface<String, String> queueNameMap = new HashMap<>(); // Map name key to queued patient ID
    private final MapInterface<String, String> queueIcMap = new HashMap<>(); // Map cleaned IC to queued patient ID

    // List index map
    private final MapInterface<String, Integer> patientIndexMap = new HashMap<>(); // Track list index for fast updates

    // Sorted lists kept for reuse (avoid re-sorting every time)
    private ListInterface<Patient> cachedSortedById = null; // Patients sorted by ID
    private ListInterface<Patient> cachedSortedByName = null; // Patients sorted by name
    private ListInterface<Patient> cachedSortedByAge = null; // Patients sorted by age
    private ListInterface<Patient> cachedSortedByDate = null; // Patients sorted by registration date
    private boolean sortCachesDirty = true; 

    /** Operation result codes */
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

    /** Initialize patient management */
    public MaintainPatient() {
        try {
            populateMaps();
        } catch (Exception e) {
            // Use empty maps if loading fails
            initializeEmptyMaps();
        }
    }

    /** Initialize empty maps */
    private void initializeEmptyMaps() {
        patientMap.clear();
        icMap.clear();
        nameMap.clear();
        queuePatientMap.clear();
        queueNameMap.clear();
        queueIcMap.clear();
        patientIndexMap.clear();
    }

    /** Add patient to name map */
    private void addToNameMap(Patient patient) {
        if (patient != null && patient.getName() != null) {
            String nameKey = patient.getName().toLowerCase().trim();
            ListInterface<Patient> patientsWithName = nameMap.get(nameKey);
            if (patientsWithName == null) {
                patientsWithName = new DoublyLinkedList<>();
                nameMap.put(nameKey, patientsWithName);
            }
            patientsWithName.add(patient);
        }
    }

    /** Remove patient from name map */
    private void removeFromNameMap(Patient patient) {
        if (patient != null && patient.getName() != null) {
            String nameKey = patient.getName().toLowerCase().trim();
            ListInterface<Patient> patientsWithName = nameMap.get(nameKey);
            if (patientsWithName != null) {
                // Remove patient from name list
                int index = 1;
                java.util.Iterator<Patient> nameIterator = patientsWithName.iterator();
                while (nameIterator.hasNext()) {
                    Patient p = nameIterator.next();
                    if (p != null && p.getId() != null && p.getId().equals(patient.getId())) {
                        patientsWithName.remove(index);
                        break;
                    }
                    index++;
                }
                // Remove name key if list becomes empty
                if (patientsWithName.isEmpty()) {
                    nameMap.remove(nameKey);
                }
            }
        }
    }

    /** Update maps when patient data changes */
    private void updateMapsForPatientChange(Patient oldPatient, Patient newPatient) {
        // Remove old entries
        patientMap.remove(oldPatient.getId());
        icMap.remove(Patient.cleanICNumber(oldPatient.getIcNumber()));
        removeFromNameMap(oldPatient);

        // Add new entries
        patientMap.put(newPatient.getId(), newPatient);
        icMap.put(Patient.cleanICNumber(newPatient.getIcNumber()), newPatient);
        addToNameMap(newPatient);
    }

    /** Update index map after a removal */
    private void updateIndexMapAfterRemoval(int removedIndex) {
        // Shift indices after the removed position
        ListInterface<String> patientIds = patientIndexMap.getKeys();
        java.util.Iterator<String> iterator = patientIds.iterator();
        while (iterator.hasNext()) {
            String patientId = iterator.next();
            Integer currentIndex = patientIndexMap.get(patientId);
            if (currentIndex != null && currentIndex > removedIndex) {
                patientIndexMap.put(patientId, currentIndex - 1);
            }
        }
    }

    /** Build lookup maps from the patient list */
    private void populateMaps() {
        try {
            patientMap.clear();
            icMap.clear();
            nameMap.clear();

            if (patientList == null) {
                throw new IllegalStateException("Patient list is null");
            }

            int index = 1;
            java.util.Iterator<Patient> iterator = patientList.iterator();
            while (iterator.hasNext()) {
                Patient patient = iterator.next();
                if (patient.isValidForMapping()) {
                    patientMap.put(patient.getId(), patient);
                    icMap.put(Patient.cleanICNumber(patient.getIcNumber()), patient);
                    patientIndexMap.put(patient.getId(), index); // Track list index

                    // Add to name map
                    String nameKey = patient.getName().toLowerCase().trim();
                    ListInterface<Patient> patientsWithName = nameMap.get(nameKey);
                    if (patientsWithName == null) {
                        patientsWithName = new DoublyLinkedList<>();
                        nameMap.put(nameKey, patientsWithName);
                    }
                    patientsWithName.add(patient);
                }
                index++;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build patient maps: " + e.getMessage(), e);
        }
    }

    
    /** Update patient across all data structures */
    private boolean updateAllDataStructures(Patient oldPatient, Patient newPatient) {
        if (oldPatient == null || newPatient == null ||
            oldPatient.getId() == null || newPatient.getId() == null ||
            oldPatient.getIcNumber() == null || newPatient.getIcNumber() == null) {
            return false;
        }

        // Check if IC changed (using cleaned values)
        String oldCleanIc = Patient.cleanICNumber(oldPatient.getIcNumber());
        String newCleanIc = Patient.cleanICNumber(newPatient.getIcNumber());
        if (!oldCleanIc.equals(newCleanIc)) {
            Patient existingPatient = icMap.get(newCleanIc);
            if (existingPatient != null && !existingPatient.getId().equals(oldPatient.getId())) {
                return false;
            }
        }

        synchronized (this) {
            // Backup for rollback
            Patient backupPatientFromMap = patientMap.get(oldPatient.getId());
            Patient backupPatientFromIcMap = icMap.get(oldCleanIc);

            try {

                // Ensure patient exists
                if (!patientMap.containsKey(oldPatient.getId())) {
                    return false;
                }

                // Replace in list by position
                Integer replaceIndex = patientIndexMap.get(oldPatient.getId());
                if (replaceIndex != null) {
                    // Replace at same index to keep list order
                    boolean replaceSuccess = patientList.replace(replaceIndex, newPatient);
                    if (!replaceSuccess) {
                        return false; // Replace operation failed
                    }

                    // Update index map
                    patientIndexMap.put(newPatient.getId(), replaceIndex);
                    if (!oldPatient.getId().equals(newPatient.getId())) {
                        patientIndexMap.remove(oldPatient.getId());
                    }

                    // Update lookup maps
                    updateMapsForPatientChange(oldPatient, newPatient);
                } else {
                    return false; // Patient not found in index map
                }



                // Invalidate caches
                invalidateSortCaches();

                // Persist changes
                try {
                    patientDAO.saveToFile(patientList);
                } catch (IOException ioException) {
                    throw new RuntimeException("Failed to save patient data: " + ioException.getMessage(), ioException);
                }
                return true;

            } catch (Exception e) {
                // Roll back on failure to preserve data
                try {
                    // Restore list entry if modified
                    Integer restoreIndex = patientIndexMap.get(newPatient.getId());
                    if (restoreIndex != null) {
                        // Replace back original patient
                        patientList.replace(restoreIndex, oldPatient);
                        patientIndexMap.put(oldPatient.getId(), restoreIndex);
                        patientIndexMap.remove(newPatient.getId());
                    }

                    // Restore lookup maps from backups
                    if (backupPatientFromMap != null) {
                        patientMap.put(oldPatient.getId(), backupPatientFromMap);
                    }
                    if (backupPatientFromIcMap != null) {
                        icMap.put(oldCleanIc, backupPatientFromIcMap);
                    }
                } catch (Exception rollbackException) {
                    // Ignore rollback errors to avoid masking original issue
                }
                return false;
            }
        }
    }

    /** Add patient to all data structures */
    private void addToAllDataStructures(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getIcNumber() != null && patient.getName() != null) {
            synchronized (this) {
                try {
                    // Add patient to main list
                    patientList.add(patient);
                    int newIndex = patientList.getNumberOfEntries();

                    // Update lookup maps with new patient
                    patientMap.put(patient.getId(), patient);
                    icMap.put(Patient.cleanICNumber(patient.getIcNumber()), patient);
                    patientIndexMap.put(patient.getId(), newIndex);
                    addToNameMap(patient);

                    // Invalidate cached sorted lists
                    invalidateSortCaches();

                    // Persist patient list to storage
                    try {
                        patientDAO.saveToFile(patientList);
                    } catch (IOException ioException) {
                        throw new RuntimeException("Failed to save patient data: " + ioException.getMessage(), ioException);
                    }
                } catch (Exception e) {
                    // Roll back: remove from list and maps
                    patientList.remove(patientList.getNumberOfEntries());
                    patientMap.remove(patient.getId());
                    icMap.remove(Patient.cleanICNumber(patient.getIcNumber()));
                    patientIndexMap.remove(patient.getId());
                    removeFromNameMap(patient);
                    throw new IllegalStateException("Failed to add patient: " + e.getMessage(), e);
                }
            }
        }
    }

    /** Remove patient from all data structures */
    private void removeFromAllDataStructures(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return;
        }

        synchronized (this) {
            // Remove patient from main list
            Integer removeIndex = patientIndexMap.get(patient.getId());
            if (removeIndex != null) {
                patientList.remove(removeIndex);

                // Update index map after removal
                updateIndexMapAfterRemoval(removeIndex);
            }

            // Remove patient from lookup maps
            patientMap.remove(patient.getId());
            icMap.remove(Patient.cleanICNumber(patient.getIcNumber()));
            patientIndexMap.remove(patient.getId());
            removeFromNameMap(patient);

            // Remove from queue and queue maps if present
            if (isInQueue(patient)) {
                // Remove from actual queue
                patientQueue.remove(patient);

                // Remove from queue maps
                if (patient.getId() != null) {
                    queuePatientMap.remove(patient.getId());
                }
                if (patient.getName() != null && !patient.getName().trim().isEmpty()) {
                    queueNameMap.remove(patient.getName().toLowerCase().trim());
                }
                if (patient.getIcNumber() != null && !patient.getIcNumber().trim().isEmpty()) {
                    queueIcMap.remove(Patient.cleanICNumber(patient.getIcNumber()));
                }
            }
        }

        // Persist updated list to storage
        try {
            patientDAO.saveToFile(patientList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save patient data after removal: " + e.getMessage(), e);
        }
    }

    /** Enqueue patient and update maps */
    private void enqueuePatient(Patient patient) {
        if (patient != null && patient.getId() != null && patient.getName() != null && patient.getIcNumber() != null) {
            synchronized (this) {
                patientQueue.enqueueWithNumber(patient);
                // Update queue status via queuePatientMap

                // Add patient to queue maps
                queuePatientMap.put(patient.getId(), patient);
                queueNameMap.put(patient.getName().toLowerCase().trim(), patient.getId());
                queueIcMap.put(Patient.cleanICNumber(patient.getIcNumber()), patient.getId());
            }
        }
    }

    /** Enqueue patient with an existing queue number */
    private void enqueuePatientWithExistingNumber(Patient patient, String queueNumber) {
        if (patient != null && patient.getId() != null && queueNumber != null) {
            synchronized (this) {
                patientQueue.enqueueWithExistingNumber(patient, queueNumber);
                // Update queue status via queuePatientMap

                // Add to queue maps
                queuePatientMap.put(patient.getId(), patient);
                if (patient.getName() != null) {
                    queueNameMap.put(patient.getName().toLowerCase().trim(), patient.getId());
                }
                if (patient.getIcNumber() != null) {
                    queueIcMap.put(Patient.cleanICNumber(patient.getIcNumber()), patient.getId());
                }
            }
        }
    }

    /** Dequeue next patient */
    private Patient dequeuePatient() {
        synchronized (this) {
            if (!patientQueue.isEmpty()) {
                Patient patient = patientQueue.dequeue();
                if (patient != null && patient.getId() != null) {
                    // Remove from queue maps
                    if (patient.getId() != null) {
                        queuePatientMap.remove(patient.getId());
                    }
                    if (patient.getName() != null && !patient.getName().trim().isEmpty()) {
                        queueNameMap.remove(patient.getName().toLowerCase().trim());
                    }
                    if (patient.getIcNumber() != null && !patient.getIcNumber().trim().isEmpty()) {
                        queueIcMap.remove(Patient.cleanICNumber(patient.getIcNumber()));
                    }
                }
                return patient;
            }
            return null;
        }
    }

    /** Check if patient is in queue */
    private boolean isInQueue(Patient patient) {
        return patient != null && patient.getId() != null && queuePatientMap.containsKey(patient.getId());
    }

    /** Clear all queue data */
    private void clearAllQueueMaps() {
        patientQueue.clear();
        queuePatientMap.clear();
        queueNameMap.clear();
        queueIcMap.clear();
    }

    /**
     * Add patient to queue by ID
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
     * Serve next patient in queue
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
            // Find available doctor using existing instance
            Doctor availableDoctor = maintainDoctor.findAvailableDoctor();

            if (availableDoctor != null) {
                // Attempt to set doctor on duty - may fail if doctor became unavailable
                boolean setOnDuty = maintainDoctor.setDoctorAvailability(availableDoctor.getId(), "On Duty");

                if (setOnDuty) {
                    // Set serving patient for treatment module
                    if (maintainTreatment != null) {
                        maintainTreatment.addServingPatientDoctorPair(nextPatient, availableDoctor);
                    }

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

    /** Get current queue status */
    public QueueStatusData getCurrentQueueStatus() {
        if (patientQueue.isEmpty()) {
            return new QueueStatusData(0, null, null);
        }

        Patient nextPatient = patientQueue.peek();
        String queueNum = patientQueue.getQueueNumber(nextPatient);

        return new QueueStatusData(patientQueue.size(), nextPatient, queueNum);
    }

    // QueueStatusData moved to entity

    /** Search patient in queue by name */
    public SearchResult searchPatientInQueueByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new SearchResult(false, null, null, -1);
        }

        // Use helper to search by name
        Patient foundPatient = findPatientInQueueByName(name);
        if (foundPatient != null) {
            String queueNum = patientQueue.getQueueNumber(foundPatient);
            int position = patientQueue.getPosition(foundPatient);
            return new SearchResult(true, foundPatient, queueNum, position);
        }

        return new SearchResult(false, null, null, -1);
    }

    /** Search patient in queue by IC */
    public SearchResult searchPatientInQueueByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return new SearchResult(false, null, null, -1);
        }

        // Use helper to search by IC
        Patient foundPatient = findPatientInQueueByIC(ic);
        if (foundPatient != null) {
            String queueNum = patientQueue.getQueueNumber(foundPatient);
            int position = patientQueue.getPosition(foundPatient);
            return new SearchResult(true, foundPatient, queueNum, position);
        }

        return new SearchResult(false, null, null, -1);
    }

    /** Clear queue (thread-safe) */
    public synchronized QueueOperationResult clearQueueLogic() {
        if (patientQueue.isEmpty()) {
            return new QueueOperationResult(false, OperationResult.QUEUE_ALREADY_EMPTY, null, null, 0);
        }

        int clearedCount = patientQueue.size();
        clearAllQueueMaps();
        return new QueueOperationResult(true, OperationResult.QUEUE_CLEARED, null, null, clearedCount);
    }

    /** Get queue snapshot for UI (preserves original queue numbers) */
    /** Get a read-only queue snapshot */
    public ListInterface<Patient> getQueueList() {
        return patientQueue.createSnapshot();
    }

    /** Get queue numbers */
    public String[] getQueueNumbers() {
        return patientQueue.getQueueNumbers();
    }

    /** Check if queue is empty */
    public boolean isQueueEmpty() {
        return patientQueue.isEmpty();
    }

    /** Get a patient's queue number */
    public String getPatientQueueNumber(Patient patient) {
        if (patient == null || !isInQueue(patient)) {
            return null;
        }
        return patientQueue.getQueueNumber(patient);
    }

    /** Get a patient's position in queue (1-based) */
    public int getPatientQueuePosition(Patient patient) {
        if (patient == null) {
            return 0;
        }

        // Use queue's built-in position method
        int position = patientQueue.getPosition(patient);
        return position > 0 ? position : 0; // Return 0 if not found (-1 becomes 0)
    }



    /** Generate next patient ID (highest existing + 1) */
    private String generateNextPatientId() {
        int max = 0;
        // Iterate patient list to find max numeric ID
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next().getId();
            if (id != null && id.startsWith("P")) {
                int num = Patient.extractNumberFromPatientId(id);
                if (num > max) {
                    max = num;
                }
            }
        }
        return Patient.generateNextPatientId(max);
    }



    /** Process patient registration - ECB compliant method for boundary class */
    public void processPatientRegistration(String icNumber, boundary.PatientUI ui) {
        Patient existingPatient = checkExistingPatient(icNumber);

        if (existingPatient != null) {
            // Handle existing patient scenario
            ui.displayMessage("\n=== PATIENT ALREADY EXISTS ===");
            int age = existingPatient.calculateAge();
            ui.displayPatient(existingPatient, age);

            // Ask if they want to add existing patient to queue
            boolean addToQueue = ui.askToAddExistingPatientToQueue(existingPatient.getName());
            if (addToQueue) {
                if (!isPatientInQueue(existingPatient)) {
                    String queueNum = addPatientToQueue(existingPatient);
                    if (queueNum != null) {
                        ui.displayMessage("\nExisting patient " + existingPatient.getName() + " has been added to the line-up queue.");
                        ui.displayMessage("Queue Number: " + queueNum);
                        ui.displayMessage("Total patients in queue: " + getQueueSize());
                    }
                } else {
                    ui.displayMessage("\nPatient is already in the queue.");
                }
            } else {
                ui.displayMessage("\nPatient not added to queue.");
            }
        } else {
            // Handle new patient registration
            ui.displayMessage("\n=== NEW PATIENT REGISTRATION ===");
            Patient patient = ui.inputRemainingPatientDetails(icNumber);

            // Register the patient
            Patient registeredPatient = registerNewPatient(patient);

            if (registeredPatient != null) {
                ui.displayMessage("\nPatient registered successfully!");

                // Add patient to queue option
                boolean joinQueue = ui.askToJoinQueue();
                if (joinQueue) {
                    String queueNum = addPatientToQueue(registeredPatient);
                    if (queueNum != null) {
                        ui.displayMessage("\nPatient " + registeredPatient.getName() + " has been added to the line-up queue.");
                        ui.displayMessage("Queue Number: " + queueNum);
                        ui.displayMessage("Total patients in queue: " + getQueueSize());
                    }
                } else {
                    ui.displayMessage("\nPatient not added to queue.");
                }
            }
        }
    }

    /** Remove patient from queue and update maps */
    private boolean removePatientFromQueue(Patient patient) {
        if (!isInQueue(patient)) {
            return false;
        }

        // Take snapshot to preserve order and numbers
        ListInterface<Patient> snapshot = patientQueue.createSnapshot();
        ListInterface<Patient> keepPatients = new DoublyLinkedList<>();
        ListInterface<String> keepNumbers = new DoublyLinkedList<>();
        boolean removed = false;

        java.util.Iterator<Patient> it = snapshot.iterator();
        while (it.hasNext()) {
            Patient current = it.next();
            if (current != null && current.getId() != null && current.getId().equals(patient.getId())) {
                removed = true; // Skip target patient
            } else {
                keepPatients.add(current);
                keepNumbers.add(patientQueue.getQueueNumber(current));
            }
        }

        if (!removed) {
            return false;
        }

        // Clear queue and related maps
        patientQueue.clear();
        queuePatientMap.clear();
        queueNameMap.clear();
        queueIcMap.clear();

        // Rebuild queue with preserved numbers
        java.util.Iterator<Patient> kpIt = keepPatients.iterator();
        java.util.Iterator<String> knIt = keepNumbers.iterator();
        while (kpIt.hasNext() && knIt.hasNext()) {
            Patient p = kpIt.next();
            String num = knIt.next();
            enqueuePatientWithExistingNumber(p, num);
        }

        return true;
    }

    // ==================== PATIENT INFORMATION SERVICES ====================

    /** Check if any patients are registered */
    public boolean hasPatients() {
        return patientList.getNumberOfEntries() > 0;
    }

    // ==================== PATIENT SORTING SERVICES ====================

    /** Sort by ID ascending (uses cache) */
    public ListInterface<Patient> sortPatientsByIdAscending() {
        buildAllSortCaches();
        return cachedSortedById;
    }

    /** Sort by ID descending */
    public ListInterface<Patient> sortPatientsByIdDescending() {
        buildAllSortCaches();
        // Return reversed view without copying the entire list
        return createReversedView(cachedSortedById);
    }

    /** Sort by registration date ascending (oldest first) */
    public ListInterface<Patient> sortPatientsByDateAscending() {
        buildAllSortCaches();
        return cachedSortedByDate;
    }

    /** Sort by registration date descending (newest first) */
    public ListInterface<Patient> sortPatientsByDateDescending() {
        buildAllSortCaches();
        // Return reversed view without copying the entire list
        return createReversedView(cachedSortedByDate);
    }


    /** Get patients sorted by name (ascending) */
    public ListInterface<Patient> getSortedPatientsByName() {
        buildAllSortCaches();
        return cachedSortedByName;
    }

    /** Get patients sorted by name (descending) */
    public ListInterface<Patient> getSortedPatientsByNameDescending() {
        buildAllSortCaches();
        return createReversedView(cachedSortedByName);
    }

    /** Get patients sorted by age (ascending) */
    public ListInterface<Patient> getSortedPatientsByAge() {
        buildAllSortCaches();
        return cachedSortedByAge;
    }

    /** Get patients sorted by age (descending) */
    public ListInterface<Patient> getSortedPatientsByAgeDescending() {
        buildAllSortCaches();
        return createReversedView(cachedSortedByAge);
    }

    /** Get all registered patients */
    public ListInterface<Patient> getPatientList() {
        return patientList;
    }

    /** Group patients by gender for reporting */
    public MapInterface<String, ListInterface<Patient>> getPatientsByGender() {
        if (patientList == null || patientList.isEmpty()) {
            return new HashMap<>();
        }

        // Group patients by gender using HashMap
        MapInterface<String, ListInterface<Patient>> genderGroups = new HashMap<>();

        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && patient.getGender() != null) {
                String gender = patient.getGender();
                ListInterface<Patient> genderList = genderGroups.get(gender);
                if (genderList == null) {
                    genderList = new DoublyLinkedList<>();
                    genderGroups.put(gender, genderList);
                }
                genderList.add(patient);
            }
        }

        return genderGroups;
    }

    // UI formatting is handled in the boundary layer

    /** Generate a comprehensive patient summary report */
    // Summary caching
    private boolean summaryCacheDirty = true;
    private PatientSummaryReport cachedSummaryReport = null;

    /** Build summary cache if needed */
    private void buildSummaryIfNeeded() {
        if (!summaryCacheDirty && cachedSummaryReport != null) return;
        cachedSummaryReport = generatePatientSummaryReportInternal();
        summaryCacheDirty = false;
    }

    /** Generate or return cached patient summary report */
    public PatientSummaryReport generatePatientSummaryReport() {
        buildSummaryIfNeeded();
        return cachedSummaryReport;
    }

    /** Build summary report data in a single pass */
    private PatientSummaryReport generatePatientSummaryReportInternal() {
        if (patientList == null || patientList.isEmpty()) {
            return new PatientSummaryReport(); // Empty report
        }

        // Initialize complex data structures for multi-dimensional analysis
        MapInterface<String, Integer> genderDistribution = new HashMap<>();
        MapInterface<String, Integer> ageGroupDistribution = new HashMap<>();
        MapInterface<Integer, Integer> yearlyRegistrations = new HashMap<>();
        MapInterface<String, Integer> monthlyRegistrations = new HashMap<>();
        MapInterface<String, MapInterface<String, Integer>> genderByYear = new HashMap<>();
        MapInterface<String, Integer> ageDistribution = new HashMap<>();

        // Statistical tracking variables
        int totalPatients = 0;
        int totalAge = 0;
        int minAge = Integer.MAX_VALUE;
        int maxAge = Integer.MIN_VALUE;
        Patient youngestPatient = null;
        Patient oldestPatient = null;

        // Single pass data processing
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient == null) continue;

            totalPatients++;
            int age = patient.calculateAge();
            totalAge += age;

            // Track youngest and oldest patients
            if (age < minAge) {
                minAge = age;
                youngestPatient = patient;
            }
            if (age > maxAge) {
                maxAge = age;
                oldestPatient = patient;
            }

            // Gender distribution
            String gender = patient.getGender();
            genderDistribution.put(gender, genderDistribution.get(gender) == null ? 1 : genderDistribution.get(gender) + 1);

            // Age group classification
            String ageGroup = PatientSummaryReport.classifyAgeGroup(age);
            ageGroupDistribution.put(ageGroup, ageGroupDistribution.get(ageGroup) == null ? 1 : ageGroupDistribution.get(ageGroup) + 1);

            // Registration patterns
            int year = patient.getRegistrationDate().getYear();
            String month = patient.getRegistrationDate().getYear() + "-" +
                          String.format("%02d", patient.getRegistrationDate().getMonthValue());

            yearlyRegistrations.put(year, yearlyRegistrations.get(year) == null ? 1 : yearlyRegistrations.get(year) + 1);
            monthlyRegistrations.put(month, monthlyRegistrations.get(month) == null ? 1 : monthlyRegistrations.get(month) + 1);

            // Gender distribution by year
            String yearStr = String.valueOf(year);
            MapInterface<String, Integer> yearGenderMap = genderByYear.get(yearStr);
            if (yearGenderMap == null) {
                yearGenderMap = new HashMap<>();
                genderByYear.put(yearStr, yearGenderMap);
            }
            yearGenderMap.put(gender, yearGenderMap.get(gender) == null ? 1 : yearGenderMap.get(gender) + 1);

            // Age buckets for histogram
            String ageBucket = PatientSummaryReport.createAgeBucket(age);
            ageDistribution.put(ageBucket, ageDistribution.get(ageBucket) == null ? 1 : ageDistribution.get(ageBucket) + 1);
        }

        // Statistical calculations
        double averageAge = totalPatients > 0 ? (double) totalAge / totalPatients : 0.0;

        // Build report
        PatientSummaryReport report = new PatientSummaryReport();
        report.setTotalPatients(totalPatients);
        report.setAverageAge(averageAge);
        report.setYoungestPatient(youngestPatient);
        report.setOldestPatient(oldestPatient);
        report.getGenderDistribution().putAll(genderDistribution);
        report.getAgeGroupDistribution().putAll(ageGroupDistribution);
        report.getYearlyRegistrations().putAll(yearlyRegistrations);
        report.getMonthlyRegistrations().putAll(monthlyRegistrations);
        report.getGenderByYear().putAll(genderByYear);
        report.getAgeDistribution().putAll(ageDistribution);

        return report;
    }




    /** Queue operation result */
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
        public String getStatusCode() { return resultCode.toString(); }
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

    // SearchResult moved to entity





    // ==================== PATIENT SEARCH SERVICES ====================

    /** Find patient by ID */
    public synchronized Patient findPatientById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null; // Return null if not found
        }
        return patientMap.get(id.trim());
    }

    /** Find patients by (case-insensitive) name; supports partial match */
    public synchronized ListInterface<Patient> findPatientsByName(String name) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();

        // Return empty list for invalid input
        if (name == null || name.trim().isEmpty()) {
            return foundPatients;
        }

        String searchName = name.trim().toLowerCase();

        // First try exact match
        ListInterface<Patient> exactMatches = nameMap.get(searchName);
        if (exactMatches != null) {
            java.util.Iterator<Patient> iterator = exactMatches.iterator();
            while (iterator.hasNext()) {
                foundPatients.add(iterator.next());
            }
        }

        // If no exact matches, search for partial matches more efficiently
        if (foundPatients.isEmpty()) {
            foundPatients = findPatientsByPartialName(searchName);
        }

        return foundPatients;
    }

    /** Find patient by exact name */
    public synchronized Patient findPatientByExactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchName = name.trim().toLowerCase();

        // Look up patients with this exact name
        ListInterface<Patient> patientsWithName = nameMap.get(searchName);
        if (patientsWithName != null && !patientsWithName.isEmpty()) {
            return patientsWithName.getEntry(1); // Return first patient with this name
        }

        return null;
    }

    /** Find patient by IC number */
    public synchronized Patient findPatientByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return null;
        }

        return icMap.get(Patient.cleanICNumber(ic));
    }



    // Removed entity utility proxies to follow ECB

    /** Check if IC number already exists */
    public Patient checkExistingPatient(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return null;
        }
        return icMap.get(Patient.cleanICNumber(icNumber));
    }

    // ==================== PATIENT REGISTRATION SERVICES ====================

    /** Register new patient - ECB compliant (delegates to entity for validation) */
    public Patient registerNewPatient(Patient patient) {
        // Delegate validation to entity
        Patient.ValidationResult validation = patient.validateForRegistration();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Patient registration failed: " + validation.getErrorMessage());
        }

        // Ensure IC is unique
        if (checkExistingPatient(patient.getIcNumber()) != null) {
            throw new IllegalArgumentException("PATIENT_ALREADY_EXISTS");
        }

        try {
            // Let entity handle its own formatting and normalization
            String formattedPhone = Patient.formatPhoneForStorage(patient.getPhone());
            String normalizedGender = Patient.normalizeGender(patient.getGender());

            // Generate ID and create with today's date
            String newId = generateNextPatientId();
            Patient patientWithId = new Patient(newId, patient.getName().trim(),
                                              Patient.cleanICNumber(patient.getIcNumber()),
                                              formattedPhone, normalizedGender, java.time.LocalDate.now());

            // Add to all data structures
            addToAllDataStructures(patientWithId);
            return patientWithId;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to register patient: " + e.getMessage(), e);
        }
    }

    /** Check if patient is in queue (thread-safe) */
    public synchronized boolean isPatientInQueue(Patient patient) {
        return isInQueue(patient);
    }

    /** Add patient to queue and return queue number */
    public String addPatientToQueue(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return null;
        }

        // Check if patient is already in queue
        if (patient.getName() != null && patient.getIcNumber() != null) {
            // Check if patient is already in queue
            if (isInQueue(patient)) {
                return null; // Already in queue
            }

            enqueuePatient(patient);
            return patientQueue.getQueueNumber(patient);
        }

        return null;
    }

    /** Get queue size */
    public int getQueueSize() {
        return patientQueue.size();
    }

    /** Update patient information */
    public boolean updatePatientInfo(Patient currentPatient, Patient updatedPatient) {
        if (currentPatient == null || updatedPatient == null) {
            return false;
        }

        // Validate updated patient data using entity validation
        Patient.ValidationResult validation = updatedPatient.validateForRegistration();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Patient update failed: " + validation.getErrorMessage());
        }

        // Format phone number after validation
        String formattedPhone = Patient.formatPhoneForStorage(updatedPatient.getPhone());
        
        // Create updated patient with formatted phone
        Patient patientWithFormattedPhone = new Patient(
            updatedPatient.getId(),
            updatedPatient.getName().trim(),
            updatedPatient.getIcNumber().trim(),
            formattedPhone,
            updatedPatient.getGender(),
            updatedPatient.getRegistrationDate()
        );

        return updateAllDataStructures(currentPatient, patientWithFormattedPhone);
    }

    /** Remove patient */
    public boolean removePatient(Patient patient) {
        if (patient == null) {
            return false;
        }

        // Remove from data structures
        removeFromAllDataStructures(patient);

        // Also remove from queue if present
        removePatientFromQueue(patient);

        // Invalidate caches
        invalidateSortCaches();

        return true;
    }

    /** Sort by registration date */
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
     * Sort patients by name in ascending order
     * @return sorted list of patients by name (A-Z)
     */
    private ListInterface<Patient> sortPatientsByName() {
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Sort by name (case-insensitive)
        sortedList.mergeSort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

        return sortedList;
    }

    /**
     * Sort patients by Age (ascending order only) - OPTIMIZED: precompute ages
     * @return sorted list of patients by age (youngest first)
     */
    private ListInterface<Patient> sortPatientsByAge() {
        ListInterface<Patient> sortedList = createPatientListCopy();

        // Precompute ages
        MapInterface<String, Integer> ageCache = new HashMap<>();
        java.util.Iterator<Patient> iter = sortedList.iterator();
        while (iter.hasNext()) {
            Patient p = iter.next();
            if (p != null && p.getId() != null && p.getIcNumber() != null) {
                ageCache.put(p.getId(), p.calculateAge());
            }
        }

        // Sort using cached ages
        sortedList.mergeSort((p1, p2) -> {
            Integer age1 = ageCache.get(p1.getId());
            Integer age2 = ageCache.get(p2.getId());
            if (age1 == null) age1 = 0;
            if (age2 == null) age2 = 0;
            return Integer.compare(age1, age2);
        });

        return sortedList;
    }

    /** Create a copy of the patient list */
    // Business logic for reporting moved to entity

    private ListInterface<Patient> createPatientListCopy() {
        ListInterface<Patient> copyList = new DoublyLinkedList<>();

        // Copy all patients
        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            copyList.add(iterator.next());
        }
        return copyList;
    }

    /** Copy from another list (for reversed view) */
    private ListInterface<Patient> createPatientListCopy(ListInterface<Patient> sourceList) {
        ListInterface<Patient> copyList = new DoublyLinkedList<>();
        java.util.Iterator<Patient> iterator = sourceList.iterator();
        while (iterator.hasNext()) {
            copyList.add(iterator.next());
        }
        return copyList;
    }

    /** Invalidate all sort caches */
    private void invalidateSortCaches() {
        sortCachesDirty = true;
        cachedSortedById = null;
        cachedSortedByName = null;
        cachedSortedByAge = null;
        cachedSortedByDate = null;
        // Also invalidate summary cache
        summaryCacheDirty = true;
        cachedSummaryReport = null;
    }

    /** Build all sort caches (if dirty) */
    private void buildAllSortCaches() {
        if (sortCachesDirty) {
            cachedSortedById = sortPatientsByID(true);
            cachedSortedByName = sortPatientsByName();
            cachedSortedByAge = sortPatientsByAge();
            cachedSortedByDate = sortPatientsByRegistrationDate(true);
            sortCachesDirty = false; // Mark as clean
        }
    }

    /** Create a reversed copy of a list */
    private ListInterface<Patient> createReversedView(ListInterface<Patient> original) {
        // Copy and reverse list
        ListInterface<Patient> reversedList = createPatientListCopy(original);
        reversedList.reverse();
        return reversedList;
    }

    /** Find patients by partial name (dedupe by ID) */
    private ListInterface<Patient> findPatientsByPartialName(String searchName) {
        ListInterface<Patient> foundPatients = new DoublyLinkedList<>();

        // Use a map for O(1) duplicate checks
        MapInterface<String, Boolean> addedPatientIds = new HashMap<>();

        ListInterface<String> nameKeys = nameMap.getKeys();
        java.util.Iterator<String> keyIterator = nameKeys.iterator();

        while (keyIterator.hasNext()) {
            String nameKey = keyIterator.next();
            if (nameKey.contains(searchName)) {
                ListInterface<Patient> patientsWithName = nameMap.get(nameKey);
                if (patientsWithName != null) {
                    java.util.Iterator<Patient> patientIterator = patientsWithName.iterator();
                    while (patientIterator.hasNext()) {
                        Patient patient = patientIterator.next();
                        if (patient != null && patient.getId() != null &&
                            !addedPatientIds.containsKey(patient.getId())) {
                            foundPatients.add(patient);
                            addedPatientIds.put(patient.getId(), true);
                        }
                    }
                }
            }
        }

        return foundPatients;
    }

    /** Check if a list contains a patient by ID */
    private boolean containsPatient(ListInterface<Patient> patientList, Patient targetPatient) {
        if (targetPatient == null || targetPatient.getId() == null) {
            return false;
        }

        java.util.Iterator<Patient> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            if (patient != null && targetPatient.getId().equals(patient.getId())) {
                return true;
            }
        }
        return false;
    }


    /** Find patient in queue by name */
    private Patient findPatientInQueueByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchName = name.toLowerCase().trim();
        String patientId = queueNameMap.get(searchName);
        return patientId != null ? queuePatientMap.get(patientId) : null;
    }

    /** Find patient in queue by IC number */
    private Patient findPatientInQueueByIC(String ic) {
        if (ic == null || ic.trim().isEmpty()) {
            return null;
        }

        String cleanIC = Patient.cleanICNumber(ic);
        String patientId = queueIcMap.get(cleanIC);
        return patientId != null ? queuePatientMap.get(patientId) : null;
    }




    /** Generate patient treatment statistics */
    public PatientTreatmentData generatePatientTreatmentData() {
        if (patientList.isEmpty()) {
            return null;
        }

        // Create list sorted by ID
        ListInterface<Patient> sortedPatients = createPatientListCopy();
        sortedPatients.mergeSort((p1, p2) -> p1.getId().compareToIgnoreCase(p2.getId()));

        // Build stats
        ListInterface<PatientTreatmentInfo> treatmentStats = new DoublyLinkedList<>();
        int totalPatients = 0;
        int totalTreatments = 0;
        int patientsWithTreatments = 0;
        int patientsWithoutTreatments = 0;

        java.util.Iterator<Patient> iterator = sortedPatients.iterator();
        while (iterator.hasNext()) {
            Patient patient = iterator.next();
            totalPatients++;

            // Get treatment count for this patient
            ListInterface<entity.Treatment> patientTreatments = maintainTreatment.findTreatmentsByPatientId(patient.getId());
            int treatmentCount = patientTreatments.getNumberOfEntries();
            totalTreatments += treatmentCount;

            if (treatmentCount > 0) {
                patientsWithTreatments++;
            } else {
                patientsWithoutTreatments++;
            }

            // Add per-patient info
            PatientTreatmentInfo info = new PatientTreatmentInfo(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                patient.calculateAge(),
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

    /** Generate patient treatment report data */
    public PatientTreatmentData generatePatientTreatmentReport() {
        return generatePatientTreatmentData();
    }

    /** Patient treatment data */
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

    /** Individual patient treatment information */
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



    /** Check if there are patients for reports */
    public boolean hasPatientsForReport() {
        return patientList.getNumberOfEntries() > 0;
    }

    /** Generate registration timeline report data */
    public RegistrationTimelineData generateRegistrationTimelineReport() {
        if (!hasPatientsForReport()) {
            return null; // Return data object, not UI message
        }

        // Sort by registration date
        ListInterface<Patient> sortedList = sortPatientsByDate();

        // Get first and last
        Patient firstPatient = null;
        Patient lastPatient = null;

        if (sortedList.getNumberOfEntries() > 0) {
            java.util.Iterator<Patient> iterator = sortedList.iterator();
            if (iterator.hasNext()) {
                firstPatient = iterator.next();
                lastPatient = firstPatient; // Initialize with first patient

                // Find last patient
                while (iterator.hasNext()) {
                    lastPatient = iterator.next();
                }
            }
        }

        // Count gender distribution
        int maleCount = 0, femaleCount = 0;
        java.util.Iterator<Patient> genderIterator = sortedList.iterator();
        while (genderIterator.hasNext()) {
            String gender = genderIterator.next().getGender();
            // Fix gender comparison - check for normalized gender values
            if (gender.equals("M") || gender.equalsIgnoreCase("male")) {
                maleCount++;
            } else if (gender.equals("F") || gender.equalsIgnoreCase("female")) {
                femaleCount++;
            }
        }

        // Monthly breakdown
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

    /** Registration timeline data */
    public static class RegistrationTimelineData {
        private final ListInterface<Patient> patientList;     // Raw data
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

        // Getters
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

    /** Sort by registration date for reports (oldest first) */
    private ListInterface<Patient> sortPatientsByDate() {
        return sortPatientsByDateAscending(); // Use standard method to avoid duplication
    }

    // ==================== APPOINTMENT MANAGEMENT SERVICES ====================

    /** Book a new appointment */
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

        String appointmentId = generateAppointmentId(); // New ID

        // Create appointment
        Appointment appointment = new Appointment(appointmentId, patientId, doctorId, appointmentTime);

        // Add and save
        appointmentList.add(appointment);
        appointmentDAO.saveToFile(appointmentList);

        return new AppointmentResult(true, OperationResult.SUCCESS, appointment);
    }

    /** Get all available doctors */
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

    /** Get doctor name by ID */
    public String getDoctorNameById(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return "Unknown Doctor";
        }
        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        return doctor != null ? doctor.getName() : "Unknown Doctor";
    }

    public boolean isDoctorAvailable(String doctorId) { // Check if doctor is available
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return false;
        }
        Doctor doctor = maintainDoctor.findDoctorById(doctorId);
        return doctor != null && "Available".equalsIgnoreCase(doctor.getAvailability());
    }

    /** Check if doctor has time conflict */
    private boolean hasTimeConflict(String doctorId, LocalDate date, LocalTime time) { // Check for time conflicts with 30-minute gap validation
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment existing = iterator.next();
            if (existing.getDoctorId().equals(doctorId) &&
                existing.getAppointmentDate().equals(date)) {

                LocalTime existingTime = existing.getAppointmentTime();

                // Within 30 minutes?
                if (isWithinThirtyMinutes(time, existingTime)) {
                    return true; // Conflict: appointments too close together
                }
            }
        }
        return false;
    }

    private boolean isWithinThirtyMinutes(LocalTime time1, LocalTime time2) { // Within 30 minutes?
        long minutesBetween = Math.abs(java.time.Duration.between(time1, time2).toMinutes());
        return minutesBetween < 30; // Return true if less than 30 minutes apart
    }

    /** Generate new appointment ID */
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

    /** Get today's appointments sorted by time */
    public ListInterface<Appointment> getTodayAppointments() {
        ListInterface<Appointment> todayAppointments = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();

        // Find today's appointments
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getAppointmentDate().equals(today)) {
                todayAppointments.add(appointment);
            }
        }

        // Sort by time
        if (!todayAppointments.isEmpty()) {
            todayAppointments.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
        }

        return todayAppointments;
    }

    /** Cancel an appointment */
    public AppointmentResult cancelAppointment(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return new AppointmentResult(false, OperationResult.INVALID_INPUT, null);
        }

        // Find and remove
        int index = 1;
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment != null && appointment.getAppointmentId().equals(appointmentId.trim())) {
                // Store before removal
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

    /** Get all appointments sorted by date and time */
    public ListInterface<Appointment> getAllAppointments() {

        ListInterface<Appointment> sortedAppointments = new DoublyLinkedList<>();


        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            sortedAppointments.add(iterator.next());
        }

        // Sort by date then time
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



    /** Sort appointments (1=by ID, 2=by time) */
    public ListInterface<Appointment> getSortedAppointments(ListInterface<Appointment> appointments, int sortType) {

        ListInterface<Appointment> sortedList = new DoublyLinkedList<>();


        java.util.Iterator<Appointment> iterator = appointments.iterator();
        while (iterator.hasNext()) {
            sortedList.add(iterator.next());
        }

        switch (sortType) {
            case 1:
                // By ID
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentId().compareTo(a2.getAppointmentId()));
                break;
            case 2:
                // By time
                sortedList.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
                break;
            default:
                // No sorting
                break;
        }

        return sortedList;
    }

    /** Clear all appointments (clears file) */
    public AppointmentResult clearAllAppointments() {
        int totalAppointments = appointmentList.getNumberOfEntries();

        if (totalAppointments == 0) {
            return new AppointmentResult(false, OperationResult.APPOINTMENT_NOT_FOUND, null);
        }

        // Clear list
        appointmentList.clear();

        // Save empty list to file
        appointmentDAO.saveToFile(appointmentList);

        return new AppointmentResult(true, OperationResult.SUCCESS, null);
    }

    /** 
     * Get appointments with patient and doctor details for display
     * Returns domain objects; UI handles formatting
     * @return List of appointments with populated patient and doctor references
     */
    public ListInterface<Appointment> getAppointmentsWithDetails(ListInterface<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return appointments;
        }

        // Return the appointments list - boundary layer handles formatting
        return appointments;
    }

    /** Search appointments by patient ID */
    public ListInterface<Appointment> searchAppointmentsByPatientId(String patientId) {
        ListInterface<Appointment> patientAppointments = new DoublyLinkedList<>();

        if (patientId == null || patientId.trim().isEmpty()) {
            return patientAppointments; // Return empty list
        }


        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return patientAppointments;
        }

        // Find all for this patient
        java.util.Iterator<Appointment> iterator = appointmentList.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            if (appointment.getPatientId().equals(patientId.trim())) {
                patientAppointments.add(appointment);
            }
        }

        // Sort by time
        if (!patientAppointments.isEmpty()) {
            patientAppointments.mergeSort((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()));
        }

        return patientAppointments;
    }

    /** Appointment booking result */
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

    /** Validation result */
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
