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
            MapInterface<String, Integer> availabilityCount
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
}
