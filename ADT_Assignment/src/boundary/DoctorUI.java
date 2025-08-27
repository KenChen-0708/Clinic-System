package boundary;

import control.MaintainDoctor;
import entity.Doctor;
import utility.MessageUI;
import java.util.Arrays;
import java.util.Scanner;

public class DoctorUI {

    private final Scanner scanner = new Scanner(System.in);
    private final MaintainDoctor maintainDoctor;

    public DoctorUI(MaintainDoctor control) {
        this.maintainDoctor = control;
    }

    public void run() {
        int choice;
        do {
            System.out.println("\n===============================");
            System.out.println("     DOCTOR MANAGEMENT MENU    ");
            System.out.println("===============================");
            System.out.println("1. Add New Doctor");
            System.out.println("2. Remove Doctor");
            System.out.println("3. View All Doctors");
            System.out.println("4. Search Doctor by ID");
            System.out.println("5. Search Doctor by Name");
            System.out.println("6. Search Doctor by Specialization");
            System.out.println("7. Update Doctor Details");
            System.out.println("8. Sort Doctors by Name");
            System.out.println("9. Sort Doctors by Specialization");
            System.out.println("10. Set Doctor Schedule");
            System.out.println("11. View Doctor Schedule");
            System.out.println("12. Set Doctor Availability");   // ⭐ NEW
            System.out.println("13. View Doctor Availability");  // ⭐ NEW
            System.out.println("14. View Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------");
            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    maintainDoctor.addDoctor();
                    break;
                case 2:
                    maintainDoctor.removeDoctor();
                    break;
                case 3:
                    maintainDoctor.displayAllDoctors();
                    break;
                case 4:
                    maintainDoctor.searchDoctorById();
                    break;
                case 5:
                    maintainDoctor.searchDoctorByName();
                    break;
                case 6:
                    maintainDoctor.searchDoctorBySpecialization();
                    break;
                case 7:
                    maintainDoctor.updateDoctorDetails();
                    break;
                case 8:
                    maintainDoctor.sortDoctorsByName();
                    break;
                case 9:
                    maintainDoctor.sortDoctorsBySpecialization();
                    break;
                case 10:
                    maintainDoctor.setDoctorSchedule();
                    break;
                case 11:
                    maintainDoctor.displayDoctorSchedule();
                    break;
                case 12:
                    maintainDoctor.setDoctorAvailability();
                    break;
                case 13:
                    maintainDoctor.displayDoctorAvailability();
                    break;
                case 14:
                    maintainDoctor.displayStatistics();
                    break;
                case 0:
                    MessageUI.displayExitMessage();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    // =========================
    // Input Methods
    // =========================
//    public Doctor inputDoctorDetails() {
//        System.out.print("Enter Name          : ");
//        String name = scanner.nextLine();
//        System.out.print("Enter Specialization: ");
//        String specialization = scanner.nextLine();
//        System.out.print("Enter Schedule (e.g., Mon-Fri 9am-5pm): ");
//        String schedule = scanner.nextLine();
//        System.out.print("Enter Availability (Available, On Duty, Off Duty, On Leave): ");
//        String availability = scanner.nextLine();
//
//        // Pass name, specialization, schedule, availability
//        // ID will be assigned in MaintainDoctor
//        return new Doctor(null, name, specialization, schedule, availability);
//    }
    public String inputDoctorId() {
        String id;
        do {
            System.out.print("Enter Doctor ID (e.g., D0001): ");
            id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("❌ Doctor ID cannot be empty.");
            } else if (!id.matches("D\\d{4}")) { // Example format: D0001, 
                System.out.println("❌ Invalid format. Use 'D' followed by 4 digits.");
            }
        } while (id.isEmpty() || !id.matches("D\\d{4}"));
        return id;
    }

    public String inputDoctorName() {
        String name;
        do {
            System.out.print("Enter Doctor Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("❌ Name cannot be empty.");
            } else if (!name.matches("[a-zA-Z ]+")) {
                System.out.println("❌ Name can only contain letters and spaces.");
            }
        } while (name.isEmpty() || !name.matches("[a-zA-Z ]+"));
        return name;
    }

    private static final String[] SPECIALIZATIONS = {
        "Cardiology", "Pediatrics", "Dermatology", "General Medicine", "Neurology"
    };

    public String inputSpecialization() {
        String specialization;
        do {
            System.out.print("Enter Specialization " + Arrays.toString(SPECIALIZATIONS) + ": ");
            specialization = scanner.nextLine().trim();
            if (specialization.isEmpty()) {
                System.out.println("❌ Specialization cannot be empty.");
            } else if (!Arrays.asList(SPECIALIZATIONS).contains(specialization)) {
                System.out.println("❌ Invalid specialization. Allowed: " + Arrays.toString(SPECIALIZATIONS));
            }
        } while (specialization.isEmpty() || !Arrays.asList(SPECIALIZATIONS).contains(specialization));
        return specialization;
    }

    public String inputSchedule() {
        // Build a flexible pattern
        final String day = "(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
        final String time12 = "(?:1[0-2]|0?[1-9])(?::[0-5][0-9])?\\s?(?:[AaPp][Mm])";
        final String time24 = "(?:[01]?\\d|2[0-3])(?::[0-5][0-9])";
        final String time = "(?:" + time12 + "|" + time24 + ")";
        final String timeRange = time + "\\s?-\\s?" + time;
        final String optionalDayOrRange = "(?:" + day + "(?:\\s?-\\s?" + day + ")?\\s+)?";
        final String pattern = "(?i)^" + optionalDayOrRange + timeRange + "$";

        System.out.print("Enter Schedule (e.g., Mon-Fri 9am-6pm, Tue 10:30am-4pm, 9am-6pm). Leave blank for default: ");
        while (true) {
            String schedule = scanner.nextLine().trim();

            // Allow blank -> MaintainDoctor will default it
            if (schedule.isEmpty()) {
                return "";
            }

            if (schedule.matches(pattern)) {
                return normalizeSchedule(schedule);
            }

            System.out.println("❌ Invalid format.");
            System.out.println("   Examples: Mon-Fri 9am-6pm | Tue 10:30am-4pm | 9am-6pm | 09:00-17:30");
            System.out.print("Enter Schedule: ");
        }
    }

    /**
     * Tidy spaces, dash spacing, and AM/PM casing for consistency.
     */
    private String normalizeSchedule(String s) {
        s = s.trim().replaceAll("\\s+", " ");      // collapse multiple spaces
        s = s.replaceAll("\\s?-\\s?", "-");        // normalize dash spacing
        s = s.replaceAll("(?i)am", "AM")
                .replaceAll("(?i)pm", "PM");          // standardize AM/PM
        return s;
    }

    public String inputAvailability() {
        String availability;
        String[] options = {"Available", "On Duty", "Off Duty", "On Leave"};
        do {
            System.out.print("Enter Doctor Availability " + Arrays.toString(options) + ": ");
            availability = scanner.nextLine().trim();
            if (availability.isEmpty()) {
                System.out.println("❌ Availability cannot be empty.");
            } else if (!Arrays.asList(options).contains(availability)) {
                System.out.println("❌ Invalid availability. Allowed: " + Arrays.toString(options));
            }
        } while (availability.isEmpty() || !Arrays.asList(options).contains(availability));
        return availability;
    }

    // =========================
    // Display Methods
    // =========================
    public void displayDoctorList(String output) {
        System.out.println("\n--- Doctor List ---\n" + output);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    
}
