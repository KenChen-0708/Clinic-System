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
    private final DoctorDAO doctorDAO;
    private final DoctorUI doctorUI;

    public MaintainDoctor() {
        doctorDAO = new DoctorDAO();

        // Load data from file into our custom DoublyLinkedList
        ListInterface<Doctor> loadedList = doctorDAO.retrieveFromFile();
        if (loadedList != null) {
            for (int i = 1; i <= loadedList.getNumberOfEntries(); i++) {
                doctorList.add(loadedList.getEntry(i));
            }
        }

        doctorUI = new DoctorUI(this);
    }

    // =========================
    // Core CRUD Operations
    // =========================
    public void addDoctor() {
        Doctor doctor = doctorUI.inputDoctorDetails();
        if (findDoctorById(doctor.getId()) == null) {
            doctorList.add(doctor);
            save();
            doctorUI.displayMessage("Doctor added successfully.");
        } else {
            doctorUI.displayMessage("Doctor with this ID already exists.");
        }
    }

    public void removeDoctor() {
        String id = doctorUI.inputDoctorId();
        int index = getDoctorIndexById(id);
        if (index != -1) {
            doctorList.remove(index + 1); // list is 1-indexed
            save();
            doctorUI.displayMessage("Doctor removed successfully.");
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void updateDoctorDetails() {
        String id = doctorUI.inputDoctorId();
        Doctor doctor = findDoctorById(id);
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
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor d = doctorList.getEntry(i);
            output.append(i).append(". ")
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
        Doctor doc = findDoctorById(id);
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
    // Sorting (using DoublyLinkedList.mergeSort)
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
        Doctor doc = findDoctorById(id);
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
        Doctor doc = findDoctorById(id);
        if (doc != null) {
            if (doc.getSchedule() != null) {
                doctorUI.displayMessage("Doctor Schedule: " + doc.getSchedule());
            } else {
                doctorUI.displayMessage("No schedule set for this doctor.");
            }
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    // =========================
    // Availability Management
    // =========================
    public void setDoctorAvailability() {
        String id = doctorUI.inputDoctorId();
        Doctor doc = findDoctorById(id);
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
        Doctor doc = findDoctorById(id);
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
    // Statistics using custom HashMap
    // =========================
    public void displayStatistics() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors available to display statistics.");
            return;
        }

        int totalDoctors = doctorList.getNumberOfEntries();
        HashMap<String, Integer> specializationCount = new HashMap<>();
        HashMap<String, Integer> availabilityCount = new HashMap<>();

        // Count specializations and availability
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor doc = doctorList.getEntry(i);

            // Specialization
            String spec = doc.getSpecialization();
            Integer count = specializationCount.get(spec);
            specializationCount.put(spec, (count != null ? count : 0) + 1);

            // Availability
            String avail = doc.getAvailability() != null ? doc.getAvailability() : "Not set";
            Integer availCount = availabilityCount.get(avail);
            availabilityCount.put(avail, (availCount != null ? availCount : 0) + 1);
        }

        StringBuilder stats = new StringBuilder();
        stats.append("Total Doctors: ").append(totalDoctors).append("\n\n");

        // Specialization Breakdown
        stats.append("Specialization Breakdown:\n");
        ListInterface<String> specKeys = specializationCount.getKeys();
        for (int i = 1; i <= specKeys.getNumberOfEntries(); i++) {
            String key = specKeys.getEntry(i);
            stats.append(" - ").append(key)
                    .append(": ").append(specializationCount.get(key))
                    .append("\n");
        }

        // Availability Breakdown
        stats.append("\nAvailability Breakdown:\n");
        ListInterface<String> availKeys = availabilityCount.getKeys();
        for (int i = 1; i <= availKeys.getNumberOfEntries(); i++) {
            String key = availKeys.getEntry(i);
            stats.append(" - ").append(key)
                    .append(": ").append(availabilityCount.get(key))
                    .append("\n");
        }

        doctorUI.displayMessage(stats.toString());
    }

    // =========================
    // Helpers
    // =========================
    private Doctor findDoctorById(String id) {
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor doc = doctorList.getEntry(i);
            if (doc.getId().equalsIgnoreCase(id)) {
                return doc;
            }
        }
        return null;
    }

    private int getDoctorIndexById(String id) {
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            if (doctorList.getEntry(i).getId().equalsIgnoreCase(id)) {
                return i - 1; // return 0-based index
            }
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
        doctorDAO.saveToFile(doctorList);
    }

    public void runDoctorMenu() {
        doctorUI.run();
    }

    private void searchDoctors(java.util.function.Predicate<Doctor> filter) {
        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor d = doctorList.getEntry(i);
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
