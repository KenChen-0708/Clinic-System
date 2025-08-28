package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import adt.HashMap;
import adt.MapInterface;
import boundary.DoctorUI;
import dao.DoctorDAO;
import entity.Doctor;
import entity.Treatment;
import java.util.Arrays;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class MaintainDoctor {

    private final ListInterface<Doctor> doctorList = new DoublyLinkedList<>();
    private final MapInterface<String, Doctor> doctorMap = new HashMap<>(); // Fast lookups
    private final DoctorDAO doctorDAO;
    private final DoctorUI doctorUI;
    private final Scanner scanner = new Scanner(System.in);
    private static final String[] SPECIALIZATIONS = {
        "Cardiology", "Pediatrics", "Dermatology", "General Medicine", "Neurology"
    };

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
        String name = inputDoctorName();
        String specialization = inputSpecialization();
        String availability = inputAvailability();
        String schedule = inputSchedule();

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
        String id = inputDoctorId();
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
        String id = inputDoctorId();
        Doctor doctor = doctorMap.get(id);
        if (doctor != null) {
            String newName = inputDoctorName();
            String newSpecialization = inputSpecialization();
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
        doctorUI.displayAllDoctors(doctorList, doctorDAO);
    }

    public void searchDoctorById() {
        String id = inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            doctorUI.displayDoctorDetails(doc);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void searchDoctorByName() {
        String name = inputDoctorName().toLowerCase();
        searchDoctors(d -> d.getName().toLowerCase().contains(name));
    }

    public void searchDoctorBySpecialization() {
        String specialization = inputSpecialization().toLowerCase();
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
        displayAllDoctors();
    }


    public void sortDoctorsBySpecialization() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors to sort.");
            return;
        }
        doctorList.mergeSort(Comparator.comparing(Doctor::getSpecialization, String.CASE_INSENSITIVE_ORDER));
        save();
        doctorUI.displayMessage("Doctors sorted by specialization.");
        displayAllDoctors();
    }



    public void sortDoctorsById() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors to sort.");
            return;
        }
        doctorList.mergeSort(Comparator.comparing(Doctor::getId, String.CASE_INSENSITIVE_ORDER));
        save();
        doctorUI.displayMessage("Doctors sorted by ID.");
        displayAllDoctors();
    }

    public void sortDoctorsByIdDescending() {
        if (doctorList.isEmpty()) {
            doctorUI.displayMessage("No doctors to sort.");
            return;
        }
        doctorList.mergeSort(Comparator.comparing(Doctor::getId, String.CASE_INSENSITIVE_ORDER).reversed());
        save();
        doctorUI.displayMessage("Doctors sorted by ID (desc).");
        displayAllDoctors();
    }

    // =========================
    // Schedule Management
    // =========================
    public void setDoctorSchedule() {
        String id = inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            String schedule = inputSchedule();
            doc.setSchedule(schedule);
            save();
            doctorUI.displayMessage("Schedule set successfully for Doctor ID: " + id);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void displayDoctorSchedule() {
        String id = inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            doctorUI.displayMessage("Doctor Schedule: "
                    + (doc.getSchedule() != null ? doc.getSchedule() : "Not set"));
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void displayAllSchedules() {
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
                    .append("Schedule: ")
                    .append(d.getSchedule() != null && !d.getSchedule().isEmpty() ? d.getSchedule() : "Not set")
                    .append("\n");
        }
        doctorUI.displayDoctorList(output.toString());
    }

    // =========================
    // Availability Management
    // =========================
    public void setDoctorAvailability() {
        String id = inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            String availability = inputAvailability();
            doc.setAvailability(availability);
            save();
            doctorUI.displayMessage("Availability updated for Doctor ID: " + id);
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    /**
     * Set doctor availability programmatically (for service calls)
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
        String id = inputDoctorId();
        Doctor doc = doctorMap.get(id);
        if (doc != null) {
            doctorUI.displayMessage("Doctor Availability: "
                    + (doc.getAvailability() != null ? doc.getAvailability() : "Not set"));
        } else {
            doctorUI.displayMessage("Doctor not found.");
        }
    }

    public void displayAllAvailability() {
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
                    .append("Availability: ")
                    .append(d.getAvailability() != null ? d.getAvailability() : "Not set")
                    .append("\n");
        }
        doctorUI.displayDoctorList(output.toString());
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

        MapInterface<String, Integer> specializationCount = new HashMap<>();
        MapInterface<String, Integer> availabilityCount = new HashMap<>();

        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            String spec = doc.getSpecialization() != null ? doc.getSpecialization() : "Not specified";
            Integer specCount = specializationCount.get(spec);
            specializationCount.put(spec, (specCount != null ? specCount : 0) + 1);

            String avail = doc.getAvailability() != null ? doc.getAvailability() : "Not set";
            Integer availCount = availabilityCount.get(avail);
            availabilityCount.put(avail, (availCount != null ? availCount : 0) + 1);
        }

        // Doctor workload section
        MapInterface<String, Integer> totalTreatmentsByDoctor = new HashMap<>();
        MapInterface<String, Integer> completedTreatmentsByDoctor = new HashMap<>();
        MapInterface<String, Integer> uniquePatientsByDoctor = new HashMap<>();

        MaintainTreatment treatmentControl = new MaintainTreatment();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            String docId = d.getId();
            ListInterface<Treatment> ts = treatmentControl.findTreatmentsByDoctorId(docId);
            int total = 0;
            int completed = 0;
            ListInterface<String> uniquePatients = new DoublyLinkedList<>();
            for (int i = 1; i <= ts.getNumberOfEntries(); i++) {
                Treatment t = ts.getEntry(i);
                total++;
                if ("Completed".equalsIgnoreCase(t.getStatus())) completed++;
                String pid = t.getPatientId();
                if (!uniquePatients.contains(pid)) uniquePatients.add(pid);
            }
            totalTreatmentsByDoctor.put(docId, total);
            completedTreatmentsByDoctor.put(docId, completed);
            uniquePatientsByDoctor.put(docId, uniquePatients.getNumberOfEntries());
        }

        doctorUI.displayStatistics(
                doctorList,
                doctorDAO,
                specializationCount,
                availabilityCount,
                totalTreatmentsByDoctor,
                completedTreatmentsByDoctor,
                uniquePatientsByDoctor
        );
    }

    // =========================
    // Grouping
    // =========================
    public MapInterface<String, ListInterface<Doctor>> groupDoctorsBySpecialization() {
        MapInterface<String, ListInterface<Doctor>> grouped = new HashMap<>();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            String key = d.getSpecialization() != null ? d.getSpecialization() : "Not specified";
            ListInterface<Doctor> list = grouped.get(key);
            if (list == null) {
                list = new DoublyLinkedList<>();
                grouped.put(key, list);
            }
            list.add(d);
        }
        return grouped;
    }

    public MapInterface<String, ListInterface<Doctor>> groupDoctorsByAvailability() {
        MapInterface<String, ListInterface<Doctor>> grouped = new HashMap<>();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            String key = d.getAvailability() != null ? d.getAvailability() : "Not set";
            ListInterface<Doctor> list = grouped.get(key);
            if (list == null) {
                list = new DoublyLinkedList<>();
                grouped.put(key, list);
            }
            list.add(d);
        }
        return grouped;
    }

    public MapInterface<String, ListInterface<Doctor>> groupDoctorsBySchedule() {
        MapInterface<String, ListInterface<Doctor>> grouped = new HashMap<>();
        for (Doctor d : doctorDAO.getIterable(doctorList)) {
            String key = d.getSchedule() != null && !d.getSchedule().isEmpty() ? d.getSchedule() : "Not set";
            ListInterface<Doctor> list = grouped.get(key);
            if (list == null) {
                list = new DoublyLinkedList<>();
                grouped.put(key, list);
            }
            list.add(d);
        }
        return grouped;
    }

    public void displayGroupBySpecialization() {
        MapInterface<String, ListInterface<Doctor>> grouped = groupDoctorsBySpecialization();
        doctorUI.displayGroupedDoctors("Doctors by Specialization", grouped, doctorDAO);
    }

    public void displayGroupByAvailability() {
        MapInterface<String, ListInterface<Doctor>> grouped = groupDoctorsByAvailability();
        doctorUI.displayGroupedDoctors("Doctors by Availability", grouped, doctorDAO);
    }

    public void displayGroupBySchedule() {
        MapInterface<String, ListInterface<Doctor>> grouped = groupDoctorsBySchedule();
        doctorUI.displayGroupedDoctors("Doctors by Schedule", grouped, doctorDAO);
    }

    // =========================
    // Workload / Productivity
    // =========================
    public void displayDoctorWorkload() {
        String doctorId = inputDoctorId();

        // Use Treatment module to fetch treatments by doctor
        MaintainTreatment treatmentControl = new MaintainTreatment();
        ListInterface<Treatment> doctorTreatments = treatmentControl.findTreatmentsByDoctorId(doctorId);

        int totalTreatments = 0;
        int completedTreatments = 0;
        ListInterface<String> uniquePatients = new DoublyLinkedList<>();

        for (int i = 1; i <= doctorTreatments.getNumberOfEntries(); i++) {
            Treatment t = doctorTreatments.getEntry(i);
            totalTreatments++;
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                completedTreatments++;
            }
            String pid = t.getPatientId();
            if (!uniquePatients.contains(pid)) {
                uniquePatients.add(pid);
            }
        }

        String generatedDate = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        double completedPct = totalTreatments > 0
                ? (completedTreatments * 100.0) / totalTreatments
                : 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n=====================================================================================================================\n");
        sb.append("                               Tunku Abdul Rahman University Of Management And Technology                              \n");
        sb.append("=====================================================================================================================\n");
        sb.append(String.format(" Report Generated On: %s\n", generatedDate));
        sb.append("=====================================================================================================================\n");
        sb.append("                                                   🧾 Doctor Workload Report                                           \n");
        sb.append("=====================================================================================================================\n");

        // Doctor summary table (same columns as statistics for familiarity)
        sb.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                "ID", "Name", "Specialization", "Schedule", "Availability"));
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");
        Doctor doc = doctorMap.get(doctorId);
        String name = (doc != null ? doc.getName() : "-");
        String spec = (doc != null && doc.getSpecialization() != null) ? doc.getSpecialization() : "Not specified";
        String sched = (doc != null && doc.getSchedule() != null) ? doc.getSchedule() : "Not set";
        String avail = (doc != null && doc.getAvailability() != null) ? doc.getAvailability() : "Not set";
        sb.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                doctorId, name, spec, sched, avail));

        sb.append("=====================================================================================================================\n");
        sb.append("\n Workload Summary:\n");
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-22s | %-10s | %-10s\n", "Metric", "Value", "Percent"));
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-22s | %-10d | %-10s\n", "Total Treatments", totalTreatments, "N/A"));
        sb.append(String.format(" %-22s | %-10d | %9.1f%%\n", "Completed", completedTreatments, completedPct));
        sb.append(String.format(" %-22s | %-10d | %-10s\n", "Unique Patients", uniquePatients.getNumberOfEntries(), "N/A"));
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" This report is confidential and intended for TAR UMT Medical Department use only.\n");
        sb.append("=====================================================================================================================\n");

        doctorUI.displayMessage(sb.toString());
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

    // =========================
    // Input Methods (moved from UI)
    // =========================
    private String inputDoctorId() {
        String id;
        do {
            doctorUI.displayMessage("Enter Doctor ID (e.g., D0001): ");
            id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                doctorUI.displayMessage("❌ Doctor ID cannot be empty.");
            } else if (!id.matches("D\\d{4}")) {
                doctorUI.displayMessage("❌ Invalid format. Use 'D' followed by 4 digits.");
            }
        } while (id.isEmpty() || !id.matches("D\\d{4}"));
        return id;
    }

    private String inputDoctorName() {
        String name;
        do {
            doctorUI.displayMessage("Enter Doctor Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                doctorUI.displayMessage("❌ Name cannot be empty.");
            } else if (!name.matches("[a-zA-Z ]+")) {
                doctorUI.displayMessage("❌ Name can only contain letters and spaces.");
            }
        } while (name.isEmpty() || !name.matches("[a-zA-Z ]+"));
        return name;
    }

    private String inputSpecialization() {
        String specialization;
        do {
            doctorUI.displayMessage("Enter Specialization " + Arrays.toString(SPECIALIZATIONS) + ": ");
            specialization = scanner.nextLine().trim();
            boolean valid = false;
            for (String s : SPECIALIZATIONS) {
                if (s.equals(specialization)) { valid = true; break; }
            }
            if (specialization.isEmpty()) {
                doctorUI.displayMessage("❌ Specialization cannot be empty.");
            } else if (!valid) {
                doctorUI.displayMessage("❌ Invalid specialization. Allowed: " + Arrays.toString(SPECIALIZATIONS));
            }
        } while (specialization.isEmpty() || !Arrays.asList(SPECIALIZATIONS).contains(specialization));
        return specialization;
    }

    private String inputSchedule() {
        final String day = "(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
        final String time12 = "(?:1[0-2]|0?[1-9])(?::[0-5][0-9])?\\s?(?:[AaPp][Mm])";
        final String time24 = "(?:[01]?\\d|2[0-3])(?::[0-5][0-9])";
        final String time = "(?:" + time12 + "|" + time24 + ")";
        final String timeRange = time + "\\s?-\\s?" + time;
        final String optionalDayOrRange = "(?:" + day + "(?:\\s?-\\s?" + day + ")?\\s+)?";
        final String pattern = "(?i)^" + optionalDayOrRange + timeRange + "$";

        doctorUI.displayMessage("Enter Schedule (e.g., Mon-Fri 9am-6pm, Tue 10:30am-4pm, 9am-6pm). Leave blank for default: ");
        while (true) {
            String schedule = scanner.nextLine().trim();
            if (schedule.isEmpty()) {
                return "";
            }
            if (schedule.matches(pattern)) {
                return normalizeSchedule(schedule);
            }
            doctorUI.displayMessage("❌ Invalid format.");
            doctorUI.displayMessage("   Examples: Mon-Fri 9am-6pm | Tue 10:30am-4pm | 9am-6pm | 09:00-17:30");
            doctorUI.displayMessage("Enter Schedule: ");
        }
    }

    private String normalizeSchedule(String s) {
        s = s.trim().replaceAll("\\s+", " ");
        s = s.replaceAll("\\s?-\\s?", "-");
        s = s.replaceAll("(?i)am", "AM").replaceAll("(?i)pm", "PM");
        return s;
    }

    private String inputAvailability() {
        String availability;
        String[] options = {"Available", "On Duty", "Off Duty", "On Leave"};
        do {
            doctorUI.displayMessage("Enter Doctor Availability " + Arrays.toString(options) + ": ");
            availability = scanner.nextLine().trim();
            boolean valid = false;
            for (String s : options) {
                if (s.equals(availability)) { valid = true; break; }
            }
            if (availability.isEmpty()) {
                doctorUI.displayMessage("❌ Availability cannot be empty.");
            } else if (!valid) {
                doctorUI.displayMessage("❌ Invalid availability. Allowed: " + Arrays.toString(options));
            }
        } while (availability.isEmpty() || !Arrays.asList(options).contains(availability));
        return availability;
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
        int maxNumeric = 0;
        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            String id = doc.getId();
            if (id != null && id.matches("D\\d{4}")) {
                try {
                    int numeric = Integer.parseInt(id.substring(1));
                    if (numeric > maxNumeric) {
                        maxNumeric = numeric;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("D%04d", maxNumeric + 1);
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
