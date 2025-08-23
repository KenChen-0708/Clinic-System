package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import adt.HashMap;
import boundary.DoctorUI;
import dao.DoctorDAO;
import entity.Doctor;

import java.util.Comparator;

public class MaintainDoctor {

    private final ListInterface<Doctor> doctorList = new DoublyLinkedList<>();
    private final HashMap<String, Doctor> doctorMap = new HashMap<>(); // Fast lookups
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
        Doctor doctor = doctorUI.inputDoctorDetails();
        if (!doctorMap.containsKey(doctor.getId())) {
            doctorList.add(doctor);
            doctorMap.put(doctor.getId(), doctor);
            save();
            doctorUI.displayMessage("Doctor added successfully.");
        } else {
            doctorUI.displayMessage("Doctor with this ID already exists.");
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
    // Statistics
    // =========================
    public void displayStatistics() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("⚠️ No doctors available to display statistics.");
            return;
        }

        int totalDoctors = doctorList.getNumberOfEntries();
        HashMap<String, Integer> specializationCount = new HashMap<>();
        HashMap<String, Integer> availabilityCount = new HashMap<>();

        // Count specializations and availability
        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            // Specialization
            String spec = doc.getSpecialization() != null ? doc.getSpecialization() : "Not specified";
            Integer count = specializationCount.get(spec);
            specializationCount.put(spec, (count != null ? count : 0) + 1);

            // Availability
            String avail = doc.getAvailability() != null ? doc.getAvailability() : "Not set";
            Integer availCount = availabilityCount.get(avail);
            availabilityCount.put(avail, (availCount != null ? availCount : 0) + 1);
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=====================================\n");
        stats.append("         📊 Doctor Statistics         \n");
        stats.append("=====================================\n");
        stats.append(" Total Doctors: ").append(totalDoctors).append("\n\n");

        // Specialization breakdown
        stats.append("🔹 Specialization Breakdown:\n");
        stats.append("-------------------------------------\n");
        for (String key : doctorDAO.getIterable(specializationCount.getKeys())) {
            stats.append(String.format(" %-20s : %d\n", key, specializationCount.get(key)));
        }

        // Availability breakdown
        stats.append("\n🔹 Availability Breakdown:\n");
        stats.append("-------------------------------------\n");
        for (String key : doctorDAO.getIterable(availabilityCount.getKeys())) {
            stats.append(String.format(" %-20s : %d\n", key, availabilityCount.get(key)));
        }

        stats.append("=====================================\n");

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
}
