package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import adt.HashMap;
import adt.MapInterface;
import boundary.DoctorUI;
import dao.DoctorDAO;
import entity.Doctor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class MaintainDoctor {

    private final ListInterface<Doctor> doctorList = new DoublyLinkedList<>();
    private final MapInterface<String, Doctor> doctorMap = new HashMap<>(); // Fast lookups
    private final DoctorDAO doctorDAO;
    private final DoctorUI doctorUI;

    public MaintainDoctor() {
        doctorDAO = new DoctorDAO();

        // Load data from file into both structures
        ListInterface<Doctor> loadedList = doctorDAO.retrieveFromFile();
        if (loadedList != null) {
            for (Doctor d : doctorDAO.getIterable(loadedList)) {
                doctorList.add(d);
                doctorMap.put(d.getId(), d);
            }
        }

        doctorUI = new DoctorUI(this);
    }

    // =========================
    // Core CRUD Operations
    // =========================
    public void addDoctor() {
        // Ask UI for name & specialization only
        String name = doctorUI.inputDoctorName();
        String specialization = doctorUI.inputSpecialization();
        String availability = doctorUI.inputAvailability();
        String schedule = doctorUI.inputSchedule();

        // Auto-generate ID
        String newId = generateDoctorId();

        // Create doctor directly with ID
        Doctor doctor = new Doctor(newId, name, specialization, schedule, availability);

        // Save doctor if ID is unique
        if (!doctorMap.containsKey(newId)) {
            doctorList.add(doctor);
            doctorMap.put(newId, doctor);
            save();
            doctorUI.displayMessage("Doctor added successfully with ID: " + newId);
        } else {
            doctorUI.displayMessage("Error: Duplicate ID detected.");
        }
    }

    public void removeDoctor() {
        String id = doctorUI.inputDoctorId();
        Doctor toRemove = doctorMap.get(id);
        if (toRemove != null) {
            int index = getDoctorIndexById(id);
            if (index != -1) {
                doctorList.remove(index + 1); // list is 1-indexed
            }
            doctorMap.remove(id);
            save();
            doctorUI.displayMessage("Doctor removed successfully.");
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void updateDoctorDetails() {
        String id = doctorUI.inputDoctorId();
        Doctor doctor = doctorMap.get(id);
        if (doctor != null) {
            String newName = doctorUI.inputDoctorName();
            String newSpecialization = doctorUI.inputSpecialization();
            doctor.setName(newName);
            doctor.setSpecialization(newSpecialization);
            save();
            doctorUI.displayMessage("Doctor details updated.");
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    // =========================
    // Display & Search
    // =========================
    public void displayAllDoctors() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors found.");
            return;
        }
        StringBuilder output = new StringBuilder();
        int counter = 1;
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            output.append(counter++).append(". ")
                    .append(d.getId()).append(" - ")
                    .append(d.getName()).append(" | ")
                    .append("Specialization: ").append(d.getSpecialization());
            if (d.getSchedule() != null) {
                output.append(" | Schedule: ").append(d.getSchedule());
            }
            if (d.getAvailability() != null) {
                output.append(" | Availability: ").append(d.getAvailability());
            }
            output.append("\n");
        }
        doctorUI.displayDoctorList(output.toString());
    }

    public void searchDoctorById() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            displayDoctorDetails(doc);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void searchDoctorByName() {
        String name = doctorUI.inputDoctorName().toLowerCase();
        searchDoctors(d -> d.getName().toLowerCase().contains(name));
    }

    public void searchDoctorBySpecialization() {
        String specialization = doctorUI.inputSpecialization().toLowerCase();
        searchDoctors(d -> d.getSpecialization().toLowerCase().contains(specialization));
    }

    // =========================
    // Sorting
    // =========================
    public void sortDoctorsByName() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors to sort.");
            return;
        }
        doctorList.mergeSort(Comparator.comparing(Doctor::getName, String.CASE_INSENSITIVE_ORDER));
        save();
        doctorUI.displayMessage("Doctors sorted by name.");
    }

    public void sortDoctorsBySpecialization() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors to sort.");
            return;
        }
        doctorList.mergeSort(Comparator.comparing(Doctor::getSpecialization, String.CASE_INSENSITIVE_ORDER));
        save();
        doctorUI.displayMessage("Doctors sorted by specialization.");
    }

    // =========================
    // Schedule Management
    // =========================
    public void setDoctorSchedule() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            String schedule = doctorUI.inputSchedule();
            doc.setSchedule(schedule);
            save();
            doctorUI.displayMessage("Schedule set successfully for Doctor ID: " + id);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void displayDoctorSchedule() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            doctorUI.displayMessage("Doctor Schedule: "
                    + (doc.getSchedule() != null ? doc.getSchedule() : "Not set"));
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    // =========================
    // Availability Management
    // =========================
    public void setDoctorAvailability() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            String availability = doctorUI.inputAvailability();
            doc.setAvailability(availability);
            save();
            doctorUI.displayMessage("Availability updated for Doctor ID: " + id);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    /**
     * Set doctor availability programmatically (for service calls) used by Patient Module
     */
    public boolean setDoctorAvailability(String doctorId, String availability) {
        if (doctorId == null || availability == null) {
            return false;
        }

        Doctor doc = doctorMap.get(doctorId);
        if (doc != null) {
            doc.setAvailability(availability);
            save();
            return true;
        }
        return false;
    }

    public void displayDoctorAvailability() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            doctorUI.displayMessage("Doctor Availability: "
                    + (doc.getAvailability() != null ? doc.getAvailability() : "Not set"));
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
    public Doctor findAvailableDoctor() {
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor doc = doctorList.getEntry(i);
            if (doc.getAvailability() != null
                    && doc.getAvailability().equalsIgnoreCase("Available")) {
                return doc;
            }
        }
        return null; // No available doctors
    }

    //Siuyee new changes - available doctor only can serve the patient and when serving the patient the doctor is unavailable
    public void updateDoctorInList(Doctor updatedDoctor) {
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor doc = doctorList.getEntry(i);
            if (doc.getId().equals(updatedDoctor.getId())) {
                doctorList.replace(i, updatedDoctor);
                save();
                break;
            }
        }
    }



// =========================
// Statistics Report
// =========================
    public void displayStatistics() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("⚠️ No doctors available to display statistics.");
            return;
        }

        // Date & time
        String generatedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        int totalDoctors = doctorList.getNumberOfEntries();
        MapInterface<String, Integer> specializationCount = new HashMap<>();
        MapInterface<String, Integer> availabilityCount = new HashMap<>();

        // Count specializations & availability
        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            // Specialization
            String spec = doc.getSpecialization() != null ? doc.getSpecialization() : "Not specified";
            Integer specCount = specializationCount.get(spec);
            specializationCount.put(spec, (specCount != null ? specCount : 0) + 1);

            // Availability
            String avail = doc.getAvailability() != null ? doc.getAvailability() : "Not set";
            Integer availCount = availabilityCount.get(avail);
            availabilityCount.put(avail, (availCount != null ? availCount : 0) + 1);
        }

        // Find most common specialization
        String topSpec = "None";
        int maxSpec = 0;
        for (String key : doctorDAO.getIterable(specializationCount.getKeys())) {
            int count = specializationCount.get(key);
            if (count > maxSpec) {
                maxSpec = count;
                topSpec = key;
            }
        }

        // Find most common availability
        String topAvail = "None";
        int maxAvail = 0;
        for (String key : doctorDAO.getIterable(availabilityCount.getKeys())) {
            int count = availabilityCount.get(key);
            if (count > maxAvail) {
                maxAvail = count;
                topAvail = key;
            }
        }

        StringBuilder stats = new StringBuilder();
        stats.append("\n=====================================================================================================================\n");
        stats.append("                               Tunku Abdul Rahman University Of Management And Technology                              \n");
        stats.append("=====================================================================================================================\n");
        stats.append(String.format(" Report Generated On: %s\n", generatedDate));
        stats.append("=====================================================================================================================\n");
        stats.append("                                                   📊 Doctor Statistics Report                                         \n");
        stats.append("=====================================================================================================================\n");

        // Doctor list (wide table)
        stats.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                "ID", "Name", "Specialization", "Schedule", "Availability"));
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");

        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            stats.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                    doc.getId(),
                    doc.getName(),
                    (doc.getSpecialization() != null ? doc.getSpecialization() : "Not specified"),
                    (doc.getSchedule() != null ? doc.getSchedule() : "Not set"),
                    (doc.getAvailability() != null ? doc.getAvailability() : "Not set")));
        }

        stats.append("=====================================================================================================================\n");

        // Detailed Statistics Summary
        stats.append("\n🔹 Summary Statistics:\n");
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(" Total Doctors: ").append(totalDoctors).append("\n\n");

        // Specialization breakdown with percentage
        stats.append(" Specialization Breakdown:\n");
        for (String key : doctorDAO.getIterable(specializationCount.getKeys())) {
            int count = specializationCount.get(key);
            double percentage = (count * 100.0) / totalDoctors;
            stats.append(String.format("   %-20s : %d (%.1f%%)\n", key, count, percentage));
        }

        stats.append("\n Availability Breakdown:\n");
        for (String key : doctorDAO.getIterable(availabilityCount.getKeys())) {
            int count = availabilityCount.get(key);
            double percentage = (count * 100.0) / totalDoctors;
            stats.append(String.format("   %-20s : %d (%.1f%%)\n", key, count, percentage));
        }

        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(String.format(" Most Common Specialization : %s (%d doctors)\n", topSpec, maxSpec));
        stats.append(String.format(" Majority Availability      : %s (%d doctors)\n", topAvail, maxAvail));
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(" This report is confidential and intended for TAR UMT Medical Department use only.\n");
        stats.append("=====================================================================================================================\n");

        doctorUI.displayMessage(stats.toString());
    }

    // =========================
    // Helpers
    // =========================
    private int getDoctorIndexById(String id) {
        int index = 0;
        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            if (doc.getId().equalsIgnoreCase(id)) {
                return index; // 0-based index
            }
            index++;
        }
        return -1;
    }

    private void displayDoctorDetails(Doctor doc) {
        String msg = "\nDoctor Found:\n"
                + "ID            : " + doc.getId() + "\n"
                + "Name          : " + doc.getName() + "\n"
                + "Specialization: " + doc.getSpecialization() + "\n"
                + "Schedule      : " + (doc.getSchedule() != null ? doc.getSchedule() : "Not set") + "\n"
                + "Availability  : " + (doc.getAvailability() != null ? doc.getAvailability() : "Not set") + "\n";
        doctorUI.displayMessage(msg);
    }

    private void save() {
        doctorDAO.saveToFile(doctorList); // Save only the list
    }

    public void runDoctorMenu() {
        doctorUI.run();
    }

    private void searchDoctors(java.util.function.Predicate<Doctor> filter) {
        StringBuilder output = new StringBuilder();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            if (filter.test(d)) {
                output.append(d).append("\n");
            }
        }
        if (output.length() > 0) {
            doctorUI.displayDoctorList(output.toString());
        } else {
            doctorUI.displayMessage("No doctors match the criteria given.");
        }
    }

    private String generateDoctorId() {
        int nextId = doctorList.getNumberOfEntries() + 1;
        return String.format("D%04d", nextId); // D0001, D0002, etc.
    }

    //Ronaldo Loh Yu Fei 
    /**
     * Get all available doctors for appointment booking
     *
     * @return List of available doctors
     */
    public ListInterface<Doctor> getAvailableDoctors() {
        ListInterface<Doctor> availableDoctors = new DoublyLinkedList<>();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            if ("Available".equalsIgnoreCase(d.getAvailability())) {
                availableDoctors.add(d);
            }
        }
        return availableDoctors;
    }

    /**
     * Find doctor by ID
     *
     * @param doctorId Doctor ID to search for
     * @return Doctor object if found, null otherwise
     */
    public Doctor findDoctorById(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) {
            return null;
        }
        return doctorMap.get(doctorId.trim());
    }
}
