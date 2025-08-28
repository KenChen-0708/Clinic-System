package boundary;

import control.MaintainDoctor;
import entity.Doctor;
import utility.MessageUI;
import adt.ListInterface;
import adt.MapInterface;
import dao.DoctorDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            System.out.println("1. Manage Doctors");
            System.out.println("2. Search");
            System.out.println("3. Sort");
            System.out.println("4. Schedule");
            System.out.println("5. Availability");
            System.out.println("6. Group");
            System.out.println("7. Reports");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------");
            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    runManageMenu();
                    break;
                case 2:
                    runSearchMenu();
                    break;
                case 3:
                    runSortMenu();
                    break;
                case 4:
                    runScheduleMenu();
                    break;
                case 5:
                    runAvailabilityMenu();
                    break;
                case 6:
                    runGroupMenu();
                    break;
                case 7:
                    runReportsMenu();
                    break;
                case 0:
                    MessageUI.displayExitMessage();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    private void runManageMenu() {
        int c;
        do {
            System.out.println("\n--- Manage Doctors ---");
            System.out.println("1. Add Doctor");
            System.out.println("2. Update Doctor Details");
            System.out.println("3. Remove Doctor");
            System.out.println("4. View All Doctors");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.addDoctor();
                    break;
                case 2:
                    maintainDoctor.updateDoctorDetails();
                    break;
                case 3:
                    maintainDoctor.removeDoctor();
                    break;
                case 4:
                    maintainDoctor.displayAllDoctors();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runSearchMenu() {
        int c;
        do {
            System.out.println("\n--- Search Doctors ---");
            System.out.println("1. By ID");
            System.out.println("2. By Name");
            System.out.println("3. By Specialization");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.searchDoctorById();
                    break;
                case 2:
                    maintainDoctor.searchDoctorByName();
                    break;
                case 3:
                    maintainDoctor.searchDoctorBySpecialization();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runSortMenu() {
        int c;
        do {
            System.out.println("\n--- Sort Doctors ---");
            System.out.println("1. By ID");
            System.out.println("2. By Name");
            System.out.println("3. By Specialization");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1: {
                    System.out.print("(A=Ascending, D=Descending): ");
                    String opt = scanner.nextLine().trim();
                    if (opt.equalsIgnoreCase("D")) {
                        maintainDoctor.sortDoctorsByIdDescending();
                    } else {
                        maintainDoctor.sortDoctorsById();
                    }
                    break;
                }
                case 2:
                    maintainDoctor.sortDoctorsByName();
                    break;
                case 3:
                    maintainDoctor.sortDoctorsBySpecialization();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runScheduleMenu() {
        int c;
        do {
            System.out.println("\n--- Doctor Schedule ---");
            System.out.println("1. Set Schedule");
            System.out.println("2. View Schedule (by ID)");
            System.out.println("3. View All Schedules");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.setDoctorSchedule();
                    break;
                case 2:
                    maintainDoctor.displayDoctorSchedule();
                    break;
                case 3:
                    maintainDoctor.displayAllSchedules();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runAvailabilityMenu() {
        int c;
        do {
            System.out.println("\n--- Doctor Availability ---");
            System.out.println("1. Set Availability");
            System.out.println("2. View Availability (by ID)");
            System.out.println("3. View All Availability");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.setDoctorAvailability();
                    break;
                case 2:
                    maintainDoctor.displayDoctorAvailability();
                    break;
                case 3:
                    maintainDoctor.displayAllAvailability();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runGroupMenu() {
        int c;
        do {
            System.out.println("\n--- Group Doctors ---");
            System.out.println("1. By Specialization");
            System.out.println("2. By Availability");
            System.out.println("3. By Schedule");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.displayGroupBySpecialization();
                    break;
                case 2:
                    maintainDoctor.displayGroupByAvailability();
                    break;
                case 3:
                    maintainDoctor.displayGroupBySchedule();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    private void runReportsMenu() {
        int c;
        do {
            System.out.println("\n--- Reports ---");
            System.out.println("1. Statistics Report");
            System.out.println("2. Doctor Workload");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Enter a number: ");
                scanner.next();
            }
            c = scanner.nextInt();
            scanner.nextLine();
            switch (c) {
                case 1:
                    maintainDoctor.displayStatistics();
                    break;
                case 2:
                    maintainDoctor.displayDoctorWorkload();
                    break;
                case 0:
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (c != 0);
    }

    // (All input methods moved to MaintainDoctor)
    // =========================
    // Display Methods
    // =========================
    public void displayDoctorList(String output) {
        System.out.println("\n--- Doctor List ---\n" + output);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public void displayDoctorDetails(Doctor doc) {
        String msg = "\nDoctor Found:\n"
                + "ID            : " + doc.getId() + "\n"
                + "Name          : " + doc.getName() + "\n"
                + "Specialization: " + doc.getSpecialization() + "\n"
                + "Schedule      : " + (doc.getSchedule() != null ? doc.getSchedule() : "Not set") + "\n"
                + "Availability  : " + (doc.getAvailability() != null ? doc.getAvailability() : "Not set") + "\n";
        displayMessage(msg);
    }

    public void displayAllDoctors(ListInterface<Doctor> doctorList, DoctorDAO doctorDAO) {
        if (doctorList.isEmpty()) {
            displayMessage("No doctors found.");
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
        displayDoctorList(output.toString());
    }

    public void displayStatistics(
            ListInterface<Doctor> doctorList,
            DoctorDAO doctorDAO,
            MapInterface<String, Integer> specializationCount,
            MapInterface<String, Integer> availabilityCount,
            MapInterface<String, Integer> totalTreatmentsByDoctor,
            MapInterface<String, Integer> completedTreatmentsByDoctor,
            MapInterface<String, Integer> uniquePatientsByDoctor
    ) {
        if (doctorList.isEmpty()) {
            displayMessage("⚠️ No doctors available to display statistics.");
            return;
        }

        String generatedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        int totalDoctors = doctorList.getNumberOfEntries();

        String topSpec = "None";
        int maxSpec = 0;
        for (String key : doctorDAO.getIterable(specializationCount.getKeys())) {
            int count = specializationCount.get(key);
            if (count > maxSpec) {
                maxSpec = count;
                topSpec = key;
            }
        }

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
        stats.append("\n🔹 Summary Statistics:\n");
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(" Total Doctors: ").append(totalDoctors).append("\n\n");

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

        // Doctor workload table
        stats.append("\n Doctor Workload Summary:\n");
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(String.format(" %-8s | %-20s | %-10s | %-10s | %-16s\n",
                "ID", "Name", "Total", "Completed", "Unique Patients"));
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        for (Doctor doc : doctorDAO.getIterable(doctorList)) {
            Integer totalVal = totalTreatmentsByDoctor.get(doc.getId());
            Integer completedVal = completedTreatmentsByDoctor.get(doc.getId());
            Integer uniqueVal = uniquePatientsByDoctor.get(doc.getId());
            int total = totalVal != null ? totalVal : 0;
            int completed = completedVal != null ? completedVal : 0;
            int unique = uniqueVal != null ? uniqueVal : 0;
            stats.append(String.format(" %-8s | %-20s | %-10d | %-10d | %-16d\n",
                    doc.getId(), doc.getName(), total, completed, unique));
        }
        stats.append("---------------------------------------------------------------------------------------------------------------------\n");
        stats.append(" This report is confidential and intended for TAR UMT Medical Department use only.\n");
        stats.append("=====================================================================================================================\n");

        displayMessage(stats.toString());
    }

    public void displaySearchResults(StringBuilder output) {
        if (output.length() > 0) {
            displayDoctorList(output.toString());
        } else {
            displayMessage("No doctors match the criteria given.");
        }
    }

    public void displayGroupedDoctors(String title,
            MapInterface<String, ListInterface<Doctor>> grouped,
            DoctorDAO doctorDAO) {
        StringBuilder sb = new StringBuilder();

        String generatedDate = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
        String header = String.format("%-80s %s", title, "Generated: " + generatedDate);

        sb.append("\n=====================================================================================================================\n");
        sb.append(String.format(" %-100s\n", header));
        sb.append("=====================================================================================================================\n");

        for (String key : doctorDAO.getIterable(grouped.getKeys())) {
            ListInterface<Doctor> list = grouped.get(key);
            int groupCount = (list != null) ? list.getNumberOfEntries() : 0;

            sb.append(String.format("\n[%s]  (%,d)\n", key, groupCount));
            sb.append("---------------------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                    "ID", "Name", "Specialization", "Schedule", "Availability"));
            sb.append("---------------------------------------------------------------------------------------------------------------------\n");

            int i = 1;
            for (Doctor d : doctorDAO.getIterable(list)) {
                sb.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                        d.getId(),
                        d.getName(),
                        (d.getSpecialization() != null ? d.getSpecialization() : "Not specified"),
                        (d.getSchedule() != null ? d.getSchedule() : "Not set"),
                        (d.getAvailability() != null ? d.getAvailability() : "Not set")));
                i++;
            }
        }

        sb.append("\n=====================================================================================================================\n");
        displayMessage(sb.toString());
    }

    public void displayDoctorWorkload(Doctor doc, String doctorId, int totalTreatments, int completedTreatments, int uniquePatients) {
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

        // Doctor summary table
        sb.append(String.format(" %-8s | %-20s | %-20s | %-25s | %-15s\n",
                "ID", "Name", "Specialization", "Schedule", "Availability"));
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");

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
        sb.append(String.format(" %-22s | %-10d | %-10s\n", "Unique Patients", uniquePatients, "N/A"));
        sb.append("---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" This report is confidential and intended for TAR UMT Medical Department use only.\n");
        sb.append("=====================================================================================================================\n");

        displayMessage(sb.toString());
    }

}
