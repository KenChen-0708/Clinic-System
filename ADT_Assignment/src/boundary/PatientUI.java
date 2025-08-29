package boundary;

import control.MaintainPatient;
import entity.Patient;
import entity.Doctor;
import entity.Appointment;
import adt.*;
import utility.MessageUI;
import utility.FormatUtil;
import utility.ValidationUtil;

import java.util.Scanner;

/**
 * Patient Management User Interface
 */
public class PatientUI {

    private final Scanner scanner = new Scanner(System.in);
    private final MaintainPatient maintainPatient;

    /**
     * Create new PatientUI with control reference
     */
    public PatientUI(MaintainPatient control) {
        this.maintainPatient = control;
    }

    /**
     * Main application loop - displays menu and processes user choices
     */
    public void run() {
        int choice;
        do {
            displayMenu();
            choice = getMenuChoice();
            handleMainMenuChoice(choice);
        } while (choice != 0);
    }

    /**
     * Process user's main menu selection
     */
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1:
                runPatientManagementMenu();
                break;
            case 2:
                runQueueManagementMenu();
                break;
            case 3:
                runAppointmentMenu();
                break;
            case 4:
                runReportsMenu();
                break;
            case 0:
                displayMessage("\nReturning to Main Menu...");
                break;
            default:
                MessageUI.displayInvalidChoiceMessage();
        }
    }

    // Display patient management menu and handle choices
    public void runPatientManagementMenu() {
        int choice;
        do {
            choice = displayPatientManagementMenu();
            switch (choice) {
                case 1:
                    handlePatientRegistration();
                    break;
                case 2:
                    handleViewAllPatients();
                    break;
                case 3:
                    handleSearchPatientMenu();
                    break;
                case 4:
                    handleUpdatePatient();
                    break;
                case 5:
                    handleRemovePatient();
                    break;
                case 0:
                    displayMessage("\nReturning to Main Patient Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    // Display queue management menu and handle choices
    public void runQueueManagementMenu() {
        int choice;
        do {
            choice = displayQueueManagementMenu();
            switch (choice) {
                case 1:
                    handleAddPatientToQueue();
                    break;
                case 2:
                    handleServeNextPatient();
                    break;
                case 3:
                    handleViewCurrentQueue();
                    break;
                case 4:
                    handleSearchInQueueMenu();
                    break;
                case 5:
                    handleClearQueue();
                    break;
                case 0:
                    displayMessage("Returning to Patient Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    // Display reports menu and handle choices
    public void runReportsMenu() {
        int choice;
        do {
            choice = displayReportsMenu();
            switch (choice) {
                case 1:
                    handlePatientSummaryReport();
                    break;
                case 2:
                    handlePatientTreatmentReport();
                    break;
                case 3:
                    handleRegistrationTimelineReport();
                    break;
                case 0:
                    displayMessage("Returning to Main Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    // Display appointment menu and handle choices
    public void runAppointmentMenu() {
        int choice;
        do {
            choice = displayAppointmentMenu();
            handleAppointmentMenuChoice(choice);
        } while (choice != 0);
    }

    // Display appointment menu options
    public int displayAppointmentMenu() {
        System.out.println("\n===============================");
        System.out.println("  APPOINTMENT MANAGEMENT MENU         ");
        System.out.println("===============================");
        System.out.println("1. Book Appointment");
        System.out.println("2. View Today's Appointments");
        System.out.println("3. Search Appointments by Patient ID");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Clear All Appointments");
        System.out.println("0. Back to Patient Main Menu");
        System.out.println("-------------------------------");
        return getMenuChoice();
    }

    // Handle appointment menu choices
    private void handleAppointmentMenuChoice(int choice) {
        switch (choice) {
            case 1:
                handleBookAppointment();
                break;
            case 2:
                handleViewTodayAppointments();
                break;
            case 3:
                handleSearchAppointmentsByPatientId();
                break;
            case 4:
                handleCancelAppointment();
                break;
            case 5:
                handleClearAllAppointments();
                break;
            case 0:
                displayMessage("Returning to Patient Menu...");
                break;
            default:
                MessageUI.displayInvalidChoiceMessage();
        }
    }

    // Handle appointment booking
    private void handleBookAppointment() {
        displayMessage("\n=== BOOK APPOINTMENT ===");

        // Input patient ID with validation loop
        String patientId = null;
        Patient patient = null;

        while (patient == null) {
            System.out.print("Enter Patient ID (or 0 to return to menu): ");
            patientId = scanner.nextLine().trim();

            if (patientId.equals("0")) {
                return; // Return to appointment menu
            }

            if (patientId.isEmpty()) {
                displayMessage("\nPatient ID cannot be empty. Please try again.\n");
                continue;
            }

            // Find and validate patient
            patient = maintainPatient.findPatientById(patientId);
            if (patient == null) {
                displayMessage("\nPatient not found. Please register the patient first or try again.\n");
                // Continue loop to ask for patient ID again
            }
        }

        // Display patient details for confirmation
        displayMessage("\n=== PATIENT DETAILS ===");
        int age = patient.calculateAge();
        displayPatient(patient, age);

        // Show available doctors
        ListInterface<Doctor> availableDoctors = maintainPatient.getAvailableDoctors();
        if (availableDoctors.isEmpty()) {
            displayMessage("No doctors are currently available for appointments.");
            askToContinueOrReturnToMenu();
            return;
        }

        displayMessage("\n=== AVAILABLE DOCTORS ===");
        displayDoctorList(availableDoctors);

        // Input doctor ID with validation loop
        String doctorId = null;
        boolean validDoctor = false;

        while (!validDoctor) {
            System.out.print("\nEnter Doctor ID (or 0 to return to menu): ");
            doctorId = scanner.nextLine().trim();

            if (doctorId.equals("0")) {
                return; // Return to appointment menu
            }

            if (doctorId.isEmpty()) {
                displayMessage("Doctor ID cannot be empty. Please try again.");
                continue;
            }

            // Validate doctor exists
            if (!maintainPatient.isDoctorValid(doctorId)) {
                displayMessage("\nDoctor not found. Please enter a valid Doctor ID.");
                continue;
            }

            // Check doctor availability
            if (!maintainPatient.isDoctorAvailable(doctorId)) {
                displayMessage("Doctor is not available for appointments. Please choose another doctor.");
                continue;
            }

            validDoctor = true;
        }

        // Input appointment time with validation
        MaintainPatient.AppointmentResult result = null;
        boolean appointmentBooked = false;

        while (!appointmentBooked) {
            System.out.print("\nEnter appointment time in 24-hour format (HH:mm)\n");
            System.out.print("Example: 09:30 for 9:30 AM, 14:30 for 2:30 PM\n");
            System.out.print("\nBusiness hours: 09:00 - 17:00 (or 0 to return to menu): ");
            String timeString = scanner.nextLine().trim();

            if (timeString.equals("0")) {
                return; // Return to appointment menu
            }

            if (timeString.isEmpty()) {
                displayMessage("Appointment time cannot be empty.");
                continue; // Ask for time again
            }

            // Validate time format first
            if (!ValidationUtil.isValidTimeFormat(timeString)) {
                displayMessage("Invalid time format. Please use HH:mm format (e.g., 14:30).");
                continue; // Ask for time again
            }

            // Try to book appointment with validated time
            result = maintainPatient.bookAppointment(patientId, doctorId, timeString);

            if (result.isSuccess()) {
                appointmentBooked = true; // Success - exit loop
                displayMessage("\n" + getAppointmentResultMessage(result.getResultCode()));
            } else {
                // Display error and continue loop to ask for new time
                displayMessage("\n" + getAppointmentResultMessage(result.getResultCode()));
                displayMessage("Please choose a different time.");
                // Loop continues to ask for new time
            }
        }

        if (result.isSuccess() && result.getAppointment() != null) {
            Appointment appointment = result.getAppointment();
            // Get doctor name from control layer
            String doctorName = maintainPatient.getDoctorNameById(appointment.getDoctorId());

            displayMessage("\n=== APPOINTMENT CONFIRMATION ===");
            displayMessage("Appointment ID: " + appointment.getAppointmentId());
            displayMessage("Patient: " + patient.getName() + " (ID: " + appointment.getPatientId() + ")");
            displayMessage("Doctor: " + doctorName + " (ID: " + appointment.getDoctorId() + ")");
            displayMessage("Date: " + appointment.getAppointmentDate());
            displayMessage("Time: " + appointment.getAppointmentTime());
        }

        // Ask about booking another appointment
        askToBookAnotherAppointment();
    }

    // Display list of doctors
    private void displayDoctorList(ListInterface<Doctor> doctors) {
        System.out.println("=".repeat(80));
        System.out.printf("%-10s %-25s %-20s %-15s%n", "Doctor ID", "Name", "Specialization", "Availability");
        System.out.println("=".repeat(80));

        // Display all available doctors
        java.util.Iterator<Doctor> iterator = doctors.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            System.out.printf("%-10s %-25s %-20s %-15s%n",
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getSpecialization(),
                    doctor.getAvailability());
        }
        System.out.println("=".repeat(80));
    }

    // Handle viewing today's appointments with sorting options
    private void handleViewTodayAppointments() {
        displayMessage("\n=== TODAY'S APPOINTMENTS ===");

        ListInterface<Appointment> todayAppointments = maintainPatient.getTodayAppointments();

        if (todayAppointments.isEmpty()) {
            displayMessage("No appointments scheduled for today.");
            askToContinueOrReturnToMenu();
            return;
        }

        // Show sorting submenu with input validation
        displayAppointmentSortingMenu();

        int sortChoice = -1;
        boolean validChoice = false;

        while (!validChoice) {
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return; // Return to appointment menu
            }

            try {
                sortChoice = Integer.parseInt(input);
                if (sortChoice >= 1 && sortChoice <= 2) {
                    validChoice = true;
                } else {
                    MessageUI.displayInvalidChoiceMessage();
                    System.out.print("\nEnter your choice: ");
                }
            } catch (NumberFormatException e) {
                MessageUI.displayInvalidChoiceMessage();
                System.out.print("\nEnter your choice: ");
            }
        }

        ListInterface<Appointment> sortedAppointments = maintainPatient.getSortedAppointments(todayAppointments, sortChoice);
        System.out.println("");
        displaySortingMessage(sortChoice);
        if (sortedAppointments != null) {
            displayAppointmentList(sortedAppointments);
        }

        askToContinueOrReturnToMenu();
    }

    // Display list of appointments with patient and doctor names
    private void displayAppointmentList(ListInterface<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            System.out.println("No appointments to display.");
            return;
        }

        System.out.println("=".repeat(120));
        System.out.printf("%-15s %-12s %-20s %-12s %-20s %-15s %-10s%n",
                "Appointment ID", "Patient ID", "Patient Name", "Doctor ID", "Doctor Name", "Date", "Time");
        System.out.println("=".repeat(120));

        // Format data in boundary layer
        java.util.Iterator<Appointment> iterator = appointments.iterator();
        while (iterator.hasNext()) {
            Appointment appointment = iterator.next();
            
            // Get patient and doctor details from control layer
            Patient patient = maintainPatient.findPatientById(appointment.getPatientId());
            String doctorName = maintainPatient.getDoctorNameById(appointment.getDoctorId());
            
            System.out.printf("%-15s %-12s %-20s %-12s %-20s %-15s %-10s%n",
                    appointment.getAppointmentId(),
                    appointment.getPatientId(),
                    FormatUtil.truncateString(patient != null ? patient.getName() : "Unknown", 20),
                    appointment.getDoctorId(),
                    FormatUtil.truncateString(doctorName != null ? doctorName : "Unknown", 20),
                    appointment.getAppointmentDate().toString(),
                    appointment.getAppointmentTime().toString());
        }
        System.out.println("=".repeat(120));
    }

    // Handle appointment cancellation
    private void handleCancelAppointment() {

        // Show all appointments
        ListInterface<Appointment> allAppointments = maintainPatient.getAllAppointments();

        if (allAppointments.isEmpty()) {
            displayMessage("No appointments found.");
            askToContinueOrReturnToMenu();
            return;
        }

        displayMessage("\n=== ALL APPOINTMENTS ===");
        displayAppointmentList(allAppointments);

        // Input appointment ID to cancel
        System.out.print("\nEnter Appointment ID to cancel (or 0 to return to menu): ");
        String appointmentId = scanner.nextLine().trim();

        if (appointmentId.equals("0")) {
            return; // Return to appointment menu
        }

        if (appointmentId.isEmpty()) {
            displayMessage("\nAppointment ID cannot be empty.");
            askToContinueOrReturnToMenu();
            return;
        }

        // Cancel appointment
        MaintainPatient.AppointmentResult result = maintainPatient.cancelAppointment(appointmentId);

        displayMessage("\n" + getAppointmentResultMessage(result.getResultCode()));

        if (result.isSuccess() && result.getAppointment() != null) {
            Appointment appointment = result.getAppointment();
            displayMessage("\n=== DELETED APPOINTMENT DETAILS ===");
            displayMessage("Appointment ID: " + appointment.getAppointmentId());
            displayMessage("Patient ID: " + appointment.getPatientId());
            displayMessage("Doctor ID: " + appointment.getDoctorId());
            displayMessage("Date: " + appointment.getAppointmentDate());
            displayMessage("Time: " + appointment.getAppointmentTime());
        }

        askToContinueOrReturnToMenu();
    }

    // Handle searching appointments by patient ID
    private void handleSearchAppointmentsByPatientId() {

        String patientId;
        do {
            System.out.print("\nEnter Patient ID to search appointments (or 0 to return to menu): ");
            patientId = scanner.nextLine().trim();

            if (patientId.equals("0")) {
                return; // Return to appointment menu
            }

            if (patientId.isEmpty()) {
                displayMessage("\nPatient ID cannot be empty. Please try again.");
                continue;
            }

            // Validate patient exists
            Patient patient = maintainPatient.findPatientById(patientId);
            if (patient == null) {
                displayMessage("\nPatient not found. Please try again.");
                continue;
            }

            // Search appointments for this patient
            ListInterface<Appointment> patientAppointments = maintainPatient.searchAppointmentsByPatientId(patientId);

            if (patientAppointments.isEmpty()) {
                displayMessage("\nNo appointments found for Patient ID: " + patientId);
                displayMessage("Patient: " + patient.getName() + "\n");
            } else {
                displayMessage("\n=== APPOINTMENTS FOR PATIENT " + patientId + " ===");
                displayMessage("Patient: " + patient.getName());
                displayMessage("Total appointments found: " + patientAppointments.getNumberOfEntries());
                displayMessage("");
                displayAppointmentList(patientAppointments);
            }

        } while (true); // Keep prompting until user enters 0
    }

    // Handle clearing all appointments
    private void handleClearAllAppointments() {

        // Show current appointments before clearing
        ListInterface<Appointment> currentAppointments = maintainPatient.getAllAppointments();
        if (currentAppointments.isEmpty()) {
            displayMessage("\nNo appointments found in the system.");
            askToContinueOrReturnToMenu();
            return;
        }

        displayMessage("\nCurrent appointments in the system:");
        displayAppointmentList(currentAppointments);

        // Confirm before clearing
        System.out.print("\nAre you sure you want to clear ALL appointments from the system? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("y") && !confirmation.equals("yes")) {
            displayMessage("\nOperation cancelled.");
            askToContinueOrReturnToMenu();
            return;
        }

        // Clear all appointments
        MaintainPatient.AppointmentResult result = maintainPatient.clearAllAppointments();

        displayMessage("\n" + getAppointmentResultMessage(result.getResultCode()));

        askToContinueOrReturnToMenu();
    }

    // Generate and display patient treatment report
    private void handlePatientTreatmentReport() {
        MaintainPatient.PatientTreatmentData data = maintainPatient.generatePatientTreatmentReport();

        if (data != null) {
            displayPatientTreatmentReport(data);
        } else {
            displayMessage("\nNo patients registered yet.");
            askToContinueOrReturnToMenu();
        }
    }

    // Handle Patient Summary Report (ECB: boundary consumes report built by control)
    private void handlePatientSummaryReport() {
        if (!maintainPatient.hasPatients()) {
            displayMessage("\nNo patients registered yet.");
            askToContinueOrReturnToMenu();
            return;
        }

        entity.PatientSummaryReport report = maintainPatient.generatePatientSummaryReport();
        displayPatientSummaryReport(report);
        askToContinueOrReturnToMenu();
    }

// Display Patient Summary Report with improved formatting and histograms
    private void displayPatientSummaryReport(entity.PatientSummaryReport report) {
        final int width = 100;
        String lineEq = "=".repeat(width);
        String lineDash = "-".repeat(width);

        // HEADER
        System.out.println();
        System.out.println(lineEq);
        System.out.printf("%" + (width / 2 + 28) + "s%n", "TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.printf("%" + (width / 2 + 12) + "s%n", "CLINIC MANAGEMENT SYSTEM");
        System.out.println(lineEq);
        System.out.printf("%" + (width / 2 + 10) + "s%n", "PATIENT SUMMARY REPORT");
        System.out.println(lineEq);

        // Generated time
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a"));
        System.out.println("Generated at: " + timestamp);
        System.out.println(lineDash);

        // OVERALL PATIENT STATISTICS
        System.out.println("\nOVERALL PATIENT STATISTICS");
        System.out.println(lineDash);
        System.out.printf("%-35s : %d%n", "Total Registered Patients", report.getTotalPatients());
        System.out.printf("%-35s : %d%n", "Average Age", Math.round(report.getAverageAge()));

        entity.Patient y = report.getYoungestPatient();
        entity.Patient o = report.getOldestPatient();
        if (y != null) {
            int yAge = y.calculateAge();
            System.out.printf("%-35s : %-20s (ID: %s, Age %d)%n", "Youngest Patient", y.getName(), y.getId(), yAge);
        }
        if (o != null) {
            int oAge = o.calculateAge();
            System.out.printf("%-35s : %-20s (ID: %s, Age %d)%n", "Oldest Patient", o.getName(), o.getId(), oAge);
        }

        // GENDER DISTRIBUTION
        System.out.println("\nGENDER DISTRIBUTION");
        System.out.println(lineDash);
        System.out.printf("%-20s %-10s%n", "Gender", "Count");
        System.out.println(lineDash);

        var genders = report.getGenderDistribution();
        var genderKeys = genders.getKeys();
        int male = 0, female = 0;
        java.util.Iterator<String> gIt = genderKeys.iterator();
        while (gIt.hasNext()) {
            String g = gIt.next();
            Integer c = genders.get(g);
            int v = (c == null ? 0 : c);
            System.out.printf("%-20s %-10d%n", g, v);
            if (g.equalsIgnoreCase("male")) {
                male = v;
            }
            if (g.equalsIgnoreCase("female")) {
                female = v;
            }
        }
        System.out.println(lineDash);
        System.out.printf("%s : %d Male / %d Female%n", "Gender Ratio", male, female);

        System.out.println("\nHistogram (Gender):");
        gIt = genderKeys.iterator();
        while (gIt.hasNext()) {
            String g = gIt.next();
            int v = genders.get(g);
            System.out.printf("%-20s %s%n", g, "#".repeat(Math.max(1, v)));
        }

        // AGE GROUP DISTRIBUTION
        System.out.println("\nAGE GROUP DISTRIBUTION");
        System.out.println(lineDash);
        System.out.printf("%-25s %-10s%n", "Age Group", "Count");
        System.out.println(lineDash);
        var ageGroups = report.getAgeGroupDistribution();
        var ageGroupKeys = ageGroups.getKeys();
        java.util.Iterator<String> agIt = ageGroupKeys.iterator();
        while (agIt.hasNext()) {
            String k = agIt.next();
            System.out.printf("%-25s %-10d%n", k, ageGroups.get(k));
        }

        System.out.println("\nHistogram (Age Groups):");
        agIt = ageGroupKeys.iterator();
        while (agIt.hasNext()) {
            String k = agIt.next();
            int v = ageGroups.get(k);
            System.out.printf("%-25s %s%n", k, "#".repeat(Math.max(1, v)));
        }

        // AGE BUCKET DISTRIBUTION (placed right after Age Groups)
        System.out.println("\nAGE BUCKET DISTRIBUTION");
        System.out.println(lineDash);
        System.out.printf("%-10s %-10s%n", "Bucket", "Count");
        System.out.println(lineDash);
        var ageBuckets = report.getAgeDistribution();
        var bucketKeys = ageBuckets.getKeys();
        java.util.Iterator<String> bIt = bucketKeys.iterator();
        while (bIt.hasNext()) {
            String bucket = bIt.next();
            System.out.printf("%-10s %-10d%n", bucket, ageBuckets.get(bucket));
        }

        System.out.println("\nHistogram (Age Buckets):");
        bIt = bucketKeys.iterator();
        while (bIt.hasNext()) {
            String bucket = bIt.next();
            int v = ageBuckets.get(bucket);
            System.out.printf("%-10s %s%n", bucket, "#".repeat(Math.max(1, v)));
        }

        // REGISTRATION BY YEAR
        System.out.println("\nREGISTRATION BY YEAR");
        System.out.println(lineDash);
        System.out.printf("%-10s %-10s%n", "Year", "Count");
        System.out.println(lineDash);
        var yearly = report.getYearlyRegistrations();
        var yearKeys = yearly.getKeys();
        java.util.Iterator<Integer> yIt = yearKeys.iterator();
        while (yIt.hasNext()) {
            Integer year = yIt.next();
            System.out.printf("%-10d %-10d%n", year, yearly.get(year));
        }

        System.out.println("\nHistogram (Registrations by Year):");
        yIt = yearKeys.iterator();
        while (yIt.hasNext()) {
            Integer year = yIt.next();
            int v = yearly.get(year);
            System.out.printf("%-10d %s%n", year, "#".repeat(Math.max(1, v)));
        }

        // MONTHLY REGISTRATION COUNT
        System.out.println("\nMONTHLY REGISTRATION COUNT");
        System.out.println(lineDash);
        System.out.printf("%-10s %-10s%n", "Month", "Count");
        System.out.println(lineDash);
        var monthly = report.getMonthlyRegistrations();
        var monthKeys = monthly.getKeys();
        java.util.Iterator<String> mIt = monthKeys.iterator();
        while (mIt.hasNext()) {
            String month = mIt.next();
            System.out.printf("%-10s %-10d%n", month, monthly.get(month));
        }

        System.out.println("\nHistogram (Monthly Registrations):");
        mIt = monthKeys.iterator();
        while (mIt.hasNext()) {
            String month = mIt.next();
            int v = monthly.get(month);
            System.out.printf("%-10s %s%n", month, "#".repeat(Math.max(1, v)));
        }

        // MOST COMMON GENDER PER YEAR
        System.out.println("\nMOST COMMON GENDER PER YEAR");
        System.out.println(lineDash);
        System.out.printf("%-10s %-10s%n", "Year", "Gender");
        System.out.println(lineDash);
        var gByYear = report.getGenderByYear();
        var gYearKeys = gByYear.getKeys();
        java.util.Iterator<String> gyIt = gYearKeys.iterator();
        while (gyIt.hasNext()) {
            String year = gyIt.next();
            String common = report.getMostCommonGenderForYear(year);
            if (common != null) {
                System.out.printf("%-10s %-10s%n", year, common);
            }
        }

        // PATIENT GROWTH
        System.out.println("\nPATIENT GROWTH");
        System.out.println(lineDash);
        int currentYear = java.time.LocalDate.now().getYear();
        int growthAbs = report.getYearlyGrowthAbsolute(currentYear);
        double growthPct = report.getYearlyGrowthPercent(currentYear);
        System.out.printf("%-35s : %+d%n", "Change (" + (currentYear - 1) + " to " + currentYear + ")", growthAbs);
        System.out.printf("%-35s : %d%%%n", "Percent Growth", Math.round(growthPct));

        System.out.println(lineEq);
    }

    // Generate and display registration timeline report
    private void handleRegistrationTimelineReport() {
        MaintainPatient.RegistrationTimelineData data = maintainPatient.generateRegistrationTimelineReport();

        if (data != null) {
            // Format raw patient data for UI display
            String[][] formattedPatientData = formatPatientTableData(data.getPatientList());
            displayRegistrationTimelineReport(formattedPatientData, data.getFirstPatient(),
                    data.getLastPatient(), data.getMaleCount(), data.getFemaleCount(), data.getMonthlyCount());
        } else {
            displayMessage("\nNo patients registered yet.");
            askToContinueOrReturnToMenu();
        }
    }

    // Display reports menu
    private int displayReportsMenu() {
        System.out.println("\n=== PATIENT REPORTS ===");
        System.out.println("1. Patient Summary Report");
        System.out.println("2. Patient Treatment Statistics Report");
        System.out.println("3. Registration Timeline Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("========================");
        return getMenuChoice();
    }

    // Display main menu options
    public void displayMenu() {
        System.out.println("\n===============================");
        System.out.println("        PATIENT MAIN MENU      ");
        System.out.println("===============================");
        System.out.println("1. Patient Management");
        System.out.println("2. Queue Management");
        System.out.println("3. Appointment Booking");
        System.out.println("4. Patient Statistics Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("-------------------------------");
    }

    // Get menu choice from user with validation
    public int getMenuChoice() {
        System.out.print("Enter your choice: ");
        while (!scanner.hasNextInt()) {
            System.out.println("\nPlease enter a valid number.\n");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
    }

    // Collect all patient details from user input
    public Patient inputPatientDetails() {
        String name = inputName();
        String icNumber = inputValidIC();
        String phone = inputPhone();
        String gender = inputGender();
        System.out.println();
        return new entity.Patient(name, icNumber, phone, gender);
    }

    // Get IC number for patient registration with validation
    public String inputICForRegistration() {
        System.out.println("\n=== PATIENT REGISTRATION ===");
        System.out.println("Enter '0' to exit registration");
        System.out.println("-----------------------------");

        String icNumber;
        do {
            System.out.print("Enter IC Number (12 digits): ");
            icNumber = scanner.nextLine().trim();

            if (icNumber.equals("0")) {
                return "0"; // Exit signal
            }

            // Validate IC number
            ValidationUtil.ValidationResult result = ValidationUtil.validateIC(icNumber);
            if (!result.isValid()) {
                System.out.println("\n" + result.getErrorMessage() + " Please try again.\n");
            } else {
                break; // Valid input
            }
        } while (true);

        // Return IC without dashes for storage
        return icNumber.replaceAll("-", "");
    }

    // Get remaining patient details after IC is confirmed
    public Patient inputRemainingPatientDetails(String icNumber) {
        String displayIC = utility.FormatUtil.formatICForDisplay(icNumber);
        System.out.println("IC Number: " + displayIC + " (Confirmed)");
        System.out.println();

        String name = inputName();
        String phone = inputPhone();
        String gender = inputGender();

        return new entity.Patient(name, icNumber, phone, gender);
    }

    // Get patient name with validation
    private String inputName() {
        String name;
        do {
            System.out.print("Enter Name      : ");
            name = scanner.nextLine().trim();

            ValidationUtil.ValidationResult result = ValidationUtil.validateName(name);
            if (!result.isValid()) {
                System.out.println("\n" + result.getErrorMessage() + " Please try again.\n");
            } else {
                break;
            }
        } while (true);
        return name;
    }

    // Get IC number with validation
    private String inputValidIC() {
        String icNumber;
        do {
            System.out.print("Enter IC Number (12 digits): ");
            icNumber = scanner.nextLine().trim();

            ValidationUtil.ValidationResult result = ValidationUtil.validateIC(icNumber);
            if (!result.isValid()) {
                System.out.println("\n" + result.getErrorMessage() + " Please try again.\n");
            } else {
                break;
            }
        } while (true);

        // Return IC without dashes for storage
        return icNumber.replaceAll("-", "");
    }

    private String inputPhone() {
        String phone;
        do {
            System.out.print("Enter Phone     : ");
            phone = scanner.nextLine().trim();

            // Validate phone number using utility validation
            ValidationUtil.ValidationResult result = ValidationUtil.validatePhone(phone);
            if (!result.isValid()) {
                System.out.println("\n" + result.getErrorMessage() + " Please try again.\n");
            } else {
                break; // Valid input
            }
        } while (true);

        // Return raw phone number - control layer handles formatting
        return phone;
    }

    private String inputGender() {
        String gender;
        do {
            System.out.print("Enter Gender (M/F): ");
            gender = scanner.nextLine().trim();

            if (gender.isEmpty()) {
                System.out.println("\nGender cannot be empty. Please try again.\n");
            } else {
                // Normalize gender input using entity layer
                String normalizedGender = Patient.normalizeGender(gender);
                if (normalizedGender != null) {
                    return normalizedGender;
                } else {
                    System.out.println("\nInvalid gender. Please enter M/Male or F/Female.\n");
                }
            }
        } while (true);
    }

    public String inputPatientId() {
        System.out.print("\nEnter Patient ID (or '0' to exit): ");
        return scanner.nextLine().trim();
    }

    public void displayPatientList(String output) {
        System.out.println("\n--- Patient List ---\n" + output);
    }

    public void displayPatient(Patient patient, int age) {
        System.out.println("\n--- Patient Found ---");
        System.out.println("ID        : " + patient.getId());
        System.out.println("Name      : " + patient.getName());
        System.out.println("Gender    : " + patient.getGender());
        System.out.println("Age       : " + age + " years old");
        System.out.println("IC Number : " + FormatUtil.formatICForDisplay(patient.getIcNumber()));
        System.out.println("Phone     : " + patient.getPhone());
        System.out.println("Reg Date  : " + FormatUtil.formatDateForDisplay(patient.getRegistrationDate()));
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public boolean askToJoinQueue() {
        System.out.print("\nWould you like to add this patient to the line-up queue? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    public void askToContinueOrReturnToMenu() {
        System.out.print("\nWould you like to continue with another action? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("n") || response.equals("no")) {
            System.out.println("\nReturning to Patient Menu...\n");
            // The menu loop will continue automatically
        }
        // If yes or any other input, continue with the current flow
    }

    public int displayQueueManagementMenu() {
        System.out.println("\n===============================");
        System.out.println("      QUEUE MANAGEMENT MENU    ");
        System.out.println("===============================");
        System.out.println("1. Add Patient to Queue");
        System.out.println("2. Serve Next Patient");
        System.out.println("3. View Current Queue");
        System.out.println("4. Search Patient in Queue");
        System.out.println("5. Clear Queue");
        System.out.println("0. Back to Patient Menu");
        System.out.println("-------------------------------");
        return getMenuChoice();
    }

    public boolean confirmClearQueue() {
        System.out.print("\nAre you sure you want to clear the entire queue? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    public boolean askToReturnToMainMenu() {
        boolean validChoice = false;

        while (!validChoice) {
            System.out.print("Do you want to return to the main menu? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("y") || choice.equals("yes")) {
                return true; // Return to main menu
            } else if (choice.equals("n") || choice.equals("no")) {
                // Exit system
                System.out.println("\nThank you for using the Clinic System!");
                System.out.println("\nGoodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid choice. Please enter 'y' for yes or 'n' for no.");
            }
        }
        return true; // This line should never be reached
    }

    public boolean askToContinueOperation(String operation) {
        System.out.print("\nWould you like to " + operation + "? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    public int displaySearchInQueueMenu() {
        System.out.println("\n===============================");
        System.out.println("     SEARCH PATIENT IN QUEUE   ");
        System.out.println("===============================");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by IC Number");
        System.out.println("0. Back to Queue Management");
        System.out.println("-------------------------------");
        return getMenuChoice();
    }

    public String inputPatientName() {
        System.out.print("\nEnter Patient Name (or '0' to exit): ");
        return scanner.nextLine().trim();
    }

    public String inputPatientIC() {
        String icNumber;
        do {
            System.out.print("\nEnter Patient IC Number (12 digits, or '0' to exit): ");
            icNumber = scanner.nextLine().trim();

            if (icNumber.equals("0")) {
                return "0"; // Exit signal
            }

            icNumber = icNumber.replaceAll("-", "");

            if (icNumber.isEmpty()) {
                System.out.println("\nIC Number cannot be empty. Please try again.");
            } else if (!icNumber.matches("\\d{12}")) {
                System.out.println("\nIC Number must be exactly 12 digits. Please try again.");
            }
        } while (icNumber.isEmpty() || !icNumber.matches("\\d{12}"));

        // Return IC without dashes for searching
        return icNumber;
    }

    public int displayPatientManagementMenu() {
        System.out.println("\n===============================");
        System.out.println("     PATIENT MANAGEMENT MENU   ");
        System.out.println("===============================");
        System.out.println("1. Register New Patient");
        System.out.println("2. View All Patients");
        System.out.println("3. Search Patient");
        System.out.println("4. Update Patient");
        System.out.println("5. Remove Patient");
        System.out.println("0. Back to Patient Main Menu");
        System.out.println("-------------------------------");
        return getMenuChoice();
    }

    public int displaySearchPatientMenu() {
        System.out.println("\n===============================");
        System.out.println("       SEARCH PATIENT MENU     ");
        System.out.println("===============================");
        System.out.println("1. Search by Patient ID");
        System.out.println("2. Search by Name");
        System.out.println("3. Search by IC Number");
        System.out.println("0. Back to Patient Management");
        System.out.println("-------------------------------");
        return getMenuChoice();
    }

    public Patient inputUpdatedPatientDetails(Patient currentPatient) {
        System.out.println("\nEnter new details (press Enter to keep current value):\n");

        // Name update with validation
        System.out.print("Name [" + currentPatient.getName() + "]: ");
        String nameInput = scanner.nextLine().trim();
        String name;
        if (nameInput.isEmpty()) {
            name = currentPatient.getName();
        } else {
            while (true) {
                ValidationUtil.ValidationResult result = ValidationUtil.validateName(nameInput);
                if (!result.isValid()) {
                    System.out.println("\n" + result.getErrorMessage() + "\n");
                    System.out.print("Name [" + currentPatient.getName() + "]: ");
                    nameInput = scanner.nextLine().trim();
                    if (nameInput.isEmpty()) {
                        nameInput = currentPatient.getName();
                        break;
                    }
                } else {
                    break;
                }
            }
            name = nameInput;
        }

        // Gender update with validation
        System.out.print("Gender [" + currentPatient.getGender() + "]: ");
        String genderInput = scanner.nextLine().trim();
        String gender;
        if (genderInput.isEmpty()) {
            gender = currentPatient.getGender();
        } else {
            String normalizedGender = Patient.normalizeGender(genderInput);
            while (normalizedGender == null) {
                System.out.println("\nInvalid gender. Please enter M/Male or F/Female.\n");
                System.out.print("Gender [" + currentPatient.getGender() + "]: ");
                genderInput = scanner.nextLine().trim();
                if (genderInput.isEmpty()) {
                    normalizedGender = currentPatient.getGender();
                    break;
                } else {
                    normalizedGender = Patient.normalizeGender(genderInput);
                }
            }
            gender = normalizedGender;
        }

        // IC Number update with validation
        System.out.print("IC Number [" + utility.FormatUtil.formatICForDisplay(currentPatient.getIcNumber()) + "] (12 digits): ");
        String icInput = scanner.nextLine().trim();
        String icNumber;
        if (icInput.isEmpty()) {
            icNumber = currentPatient.getIcNumber();
        } else {
            while (true) {
                ValidationUtil.ValidationResult result = ValidationUtil.validateIC(icInput);
                if (!result.isValid()) {
                    System.out.println("\n" + result.getErrorMessage() + "\n");
                    System.out.print("IC Number [" + utility.FormatUtil.formatICForDisplay(currentPatient.getIcNumber()) + "] (12 digits): ");
                    icInput = scanner.nextLine().trim();
                    if (icInput.isEmpty()) {
                        icInput = currentPatient.getIcNumber();
                        break;
                    }
                } else {
                    break;
                }
            }
            icNumber = icInput.replaceAll("-", ""); // Store without dashes
        }

        // Phone update with validation
        System.out.print("Phone [" + currentPatient.getPhone() + "]: ");
        String phoneInput = scanner.nextLine().trim();
        String phone;
        if (phoneInput.isEmpty()) {
            phone = currentPatient.getPhone();
        } else {
            // Validate phone number using utility validation
            while (true) {
                ValidationUtil.ValidationResult result = ValidationUtil.validatePhone(phoneInput);
                if (!result.isValid()) {
                    System.out.println("\n" + result.getErrorMessage() + "\n");
                    System.out.print("Phone [" + currentPatient.getPhone() + "]: ");
                    phoneInput = scanner.nextLine().trim();
                    if (phoneInput.isEmpty()) {
                        phoneInput = currentPatient.getPhone();
                        break;
                    }
                } else {
                    break;
                }
            }
            // Return raw phone number - control layer handles formatting
            phone = phoneInput;
        }

        // Return updated patient entity
        return new entity.Patient(currentPatient.getId(), name, icNumber, phone, gender, currentPatient.getRegistrationDate());
    }

    public int displayViewPatientsMenu() {
        System.out.println("\n=== VIEW PATIENTS MENU ===");
        System.out.println("1. Sort by Patient ID");
        System.out.println("2. Sort by Name");
        System.out.println("3. Sort by Age");
        System.out.println("4. Sort by Registration Date");
        System.out.println("5. Group by Gender");
        System.out.println("0. Return to Main Menu");
        System.out.println("==========================");
        return getMenuChoice();
    }

    /**
     * Display submenu for Patient ID sorting options
     *
     * @return user's choice for sorting order
     */
    public int displaySortByIdMenu() {
        System.out.println("\n=== SORT BY PATIENT ID ===");
        System.out.println("1. Ascending Order (A-Z)");
        System.out.println("2. Descending Order (Z-A)");
        System.out.println("0. Back to View Menu");
        System.out.println("==========================");
        return getMenuChoice();
    }

    /**
     * Display submenu for Name sorting options
     *
     * @return user's choice for sorting order
     */
    public int displaySortByNameMenu() {
        System.out.println("\n=== SORT BY NAME ===");
        System.out.println("1. Ascending Order (A-Z)");
        System.out.println("2. Descending Order (Z-A)");
        System.out.println("0. Back to View Menu");
        System.out.println("==========================");
        return getMenuChoice();
    }

    /**
     * Display submenu for Age sorting options
     *
     * @return user's choice for sorting order
     */
    public int displaySortByAgeMenu() {
        System.out.println("\n=== SORT BY AGE ===");
        System.out.println("1. Ascending Order (Youngest First)");
        System.out.println("2. Descending Order (Oldest First)");
        System.out.println("0. Back to View Menu");
        System.out.println("==================================");
        return getMenuChoice();
    }

    /**
     * Display submenu for Registration Date sorting options
     *
     * @return user's choice for sorting order
     */
    public int displaySortByDateMenu() {
        System.out.println("\n=== SORT BY REGISTRATION DATE ===");
        System.out.println("1. Ascending Order (Oldest First)");
        System.out.println("2. Descending Order (Newest First)");
        System.out.println("0. Back to View Menu");
        System.out.println("==========================");
        return getMenuChoice();
    }

    // Handle view patients choice
    public void handleViewPatientsChoice(int choice) {
        switch (choice) {
            case 1:
                // Patient ID sorting with submenu
                handleSortByIdSubmenu();
                break;
            case 2:
                // Name sorting with submenu
                handleSortByNameSubmenu();
                break;
            case 3:
                // Age sorting with submenu
                handleSortByAgeSubmenu();
                break;
            case 4:
                // Registration Date sorting with submenu
                handleSortByDateSubmenu();
                break;
            case 5:
                // Group by Gender
                displayPatientsByGender();
                break;
            case 0:
                // Return handled by calling method
                break;
            default:
                displayMessage("Invalid choice. Please try again.");
        }
    }

    // Helper method to display sorted patients
    private void displaySortedPatients(String sortType, boolean ascending, String title) {
        ListInterface<Patient> sortedData = null;

        // Get sorted patient data
        switch (sortType) {
            case "name":
                sortedData = maintainPatient.getSortedPatientsByName();
                break;
            case "age":
                sortedData = maintainPatient.getSortedPatientsByAge();
                break;
        }

        if (sortedData != null) {
            String[][] tableData = formatPatientTableData(sortedData);
            displayPatientsTableWithData(tableData, title);
            askToContinueOrReturnToMenu();
        }
    }

    // Handle Patient ID sorting submenu
    private void handleSortByIdSubmenu() {
        boolean continueIdSorting = true;
        while (continueIdSorting) {
            int choice = displaySortByIdMenu();

            switch (choice) {
                case 1:
                    displaySortedPatientsById(true, "Sorted by ID (A-Z)");
                    break;
                case 2:
                    displaySortedPatientsById(false, "Sorted by ID (Z-A)");
                    break;
                case 0:
                    continueIdSorting = false;
                    break;
                default:
                    displayMessage("Invalid choice. Please try again.");
            }
        }
    }

    // Handle Name sorting submenu
    private void handleSortByNameSubmenu() {
        boolean continueNameSorting = true;
        while (continueNameSorting) {
            int choice = displaySortByNameMenu();

            switch (choice) {
                case 1:
                    displaySortedPatientsByName(true, "Sorted by Name (A-Z)");
                    break;
                case 2:
                    displaySortedPatientsByName(false, "Sorted by Name (Z-A)");
                    break;
                case 0:
                    continueNameSorting = false;
                    break;
                default:
                    displayMessage("Invalid choice. Please try again.");
            }
        }
    }

    // Handle Age sorting submenu
    private void handleSortByAgeSubmenu() {
        boolean continueAgeSorting = true;
        while (continueAgeSorting) {
            int choice = displaySortByAgeMenu();

            switch (choice) {
                case 1:
                    displaySortedPatientsByAge(true, "Sorted by Age (Youngest First)");
                    break;
                case 2:
                    displaySortedPatientsByAge(false, "Sorted by Age (Oldest First)");
                    break;
                case 0:
                    continueAgeSorting = false;
                    break;
                default:
                    displayMessage("Invalid choice. Please try again.");
            }
        }
    }

    // Helper method to display sorted patients by ID
    private void displaySortedPatientsById(boolean ascending, String title) {
        ListInterface<Patient> sortedData = ascending
                ? maintainPatient.sortPatientsByIdAscending()
                : maintainPatient.sortPatientsByIdDescending();

        if (sortedData != null && sortedData.getNumberOfEntries() > 0) {
            String[][] tableData = formatPatientTableData(sortedData);
            displayPatientsTableWithData(tableData, title);
            askToContinueOrReturnToMenu();
        } else {
            displayMessage("No patients found to display.");
            askToContinueOrReturnToMenu();
        }
    }

    // Helper method to display sorted patients by Name
    private void displaySortedPatientsByName(boolean ascending, String title) {
        ListInterface<Patient> sortedData = ascending
                ? maintainPatient.getSortedPatientsByName()
                : maintainPatient.getSortedPatientsByNameDescending();

        if (sortedData != null && sortedData.getNumberOfEntries() > 0) {
            String[][] tableData = formatPatientTableData(sortedData);
            displayPatientsTableWithData(tableData, title);
            askToContinueOrReturnToMenu();
        } else {
            displayMessage("No patients found to display.");
            askToContinueOrReturnToMenu();
        }
    }

    // Helper method to display sorted patients by Age
    private void displaySortedPatientsByAge(boolean ascending, String title) {
        ListInterface<Patient> sortedData = ascending
                ? maintainPatient.getSortedPatientsByAge()
                : maintainPatient.getSortedPatientsByAgeDescending();

        if (sortedData != null && sortedData.getNumberOfEntries() > 0) {
            String[][] tableData = formatPatientTableData(sortedData);
            displayPatientsTableWithData(tableData, title);
            askToContinueOrReturnToMenu();
        } else {
            displayMessage("No patients found to display.");
            askToContinueOrReturnToMenu();
        }
    }

    // Handle Registration Date sorting submenu
    private void handleSortByDateSubmenu() {
        boolean continueDateSorting = true;
        while (continueDateSorting) {
            int choice = displaySortByDateMenu();

            switch (choice) {
                case 1:
                    displaySortedPatientsByDate(true, "Sorted by Registration Date (Oldest First)");
                    break;
                case 2:
                    displaySortedPatientsByDate(false, "Sorted by Registration Date (Newest First)");
                    break;
                case 0:
                    continueDateSorting = false;
                    break;
                default:
                    displayMessage("Invalid choice. Please try again.");
            }
        }
    }

    // Helper method to display sorted patients by date
    private void displaySortedPatientsByDate(boolean ascending, String title) {
        ListInterface<Patient> sortedData = ascending
                ? maintainPatient.sortPatientsByDateAscending()
                : maintainPatient.sortPatientsByDateDescending();

        if (sortedData != null && sortedData.getNumberOfEntries() > 0) {
            String[][] tableData = formatPatientTableData(sortedData);
            displayPatientsTableWithData(tableData, title);
            askToContinueOrReturnToMenu();
        } else {
            displayMessage("No patients found to display.");
            askToContinueOrReturnToMenu();
        }
    }

    // Display patients grouped by gender
    private void displayPatientsByGender() {
        MapInterface<String, ListInterface<Patient>> genderGroups = maintainPatient.getPatientsByGender();

        if (genderGroups.isEmpty()) {
            displayMessage("No patients found to display.");
            askToContinueOrReturnToMenu();
            return;
        }

        displayMessage("\n=== PATIENTS GROUPED BY GENDER ===");

        ListInterface<String> genderKeys = genderGroups.getKeys();
        java.util.Iterator<String> genderIterator = genderKeys.iterator();
        
        while (genderIterator.hasNext()) {
            String gender = genderIterator.next();
            ListInterface<Patient> patients = genderGroups.get(gender);
            int count = patients.getNumberOfEntries();

            displayMessage("\n" + "=".repeat(80));
            displayMessage("GENDER: " + gender.toUpperCase() + " (Total: " + count + " patients)");
            displayMessage("=".repeat(80));

            if (count > 0) {
                // Format data in boundary layer
                String[][] patientData = formatPatientTableData(patients);
                displayPatientsTableWithData(patientData, "Patients - " + gender);
            } else {
                displayMessage("No patients in this gender group.");
            }
        }

        askToContinueOrReturnToMenu();
    }

    /**
     * Handle view all patients
     */
    public void handleViewAllPatients() {
        // Check if patients exist
        if (!maintainPatient.hasPatients()) {
            displayMessage("\nNo patients registered yet.");
            askToContinueOrReturnToMenu();
            return;
        }

        // Show view menu and handle sorting
        boolean continueViewing = true;
        while (continueViewing) {
            int choice = displayViewPatientsMenu();

            if (choice == 0) {
                continueViewing = false;
            } else {
                handleViewPatientsChoice(choice);
            }
        }
    }

    /** Format patient data for table display */
    private String[][] formatPatientTableData(ListInterface<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            return new String[0][0];
        }

        String[][] patientData = new String[patients.getNumberOfEntries()][7];
        java.util.Iterator<Patient> iterator = patients.iterator();
        int index = 0;

        while (iterator.hasNext()) {
            Patient p = iterator.next();
            if (p != null) {
                int age = p.calculateAge();
                patientData[index][0] = p.getId();
                patientData[index][1] = p.getName();
                patientData[index][2] = p.getGender();
                patientData[index][3] = String.valueOf(age);
                patientData[index][4] = p.getIcNumber();
                patientData[index][5] = p.getPhone();
                patientData[index][6] = p.getRegistrationDate().toString();
                index++;
            }
        }
        return patientData;
    }

    /**
     * Format queue data for table display
     */
    private String[][] formatQueueTableData() {
        ListInterface<Patient> queueList = maintainPatient.getQueueList();
        String[] queueNumbers = maintainPatient.getQueueNumbers();

        if (queueList.isEmpty()) {
            return new String[0][0];
        }

        String[][] queueData = new String[queueList.getNumberOfEntries()][6];

        // Display queue in FIFO order
        java.util.Iterator<Patient> iterator = queueList.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Patient p = iterator.next();
            int age = p.calculateAge();

            queueData[index][0] = queueNumbers[index] != null ? queueNumbers[index] : String.format("%04d", index + 1);
            queueData[index][1] = p.getId();
            queueData[index][2] = p.getName();
            queueData[index][3] = p.getGender();
            queueData[index][4] = String.valueOf(age);
            queueData[index][5] = p.getPhone();
            index++;
        }

        return queueData;
    }

    // ================================
    // SEARCH HANDLERS
    // ================================
    // Search for patient by ID
    public void handleSearchPatientById() {
        boolean continueSearching = true;

        while (continueSearching) {
            String id = inputPatientId();

            if (id == null || id.equals("0")) {
                displayMessage("\nExiting search...");
                break;
            }

            // Validate input at UI level
            if (id.trim().isEmpty()) {
                displayMessage("\nInvalid input. Please enter a valid Patient ID.");
                continue;
            }

            // Find patient by ID
            Patient patient = maintainPatient.findPatientById(id);

            if (patient != null) {
                int age = patient.calculateAge();
                displayPatient(patient, age);
            } else {
                displayMessage("\nPatient with ID '" + id + "' not found.");
            }

            continueSearching = askToContinueOperation("search another patient by ID");
        }
    }

    /**
     * Handle search patient by name
     */
    public void handleSearchPatientByName() {
        boolean continueSearching = true;

        while (continueSearching) {
            String name = inputPatientName();

            if (name.equals("0")) {
                displayMessage("\nExiting search...");
                break;
            }

            // Search for patients by name
            ListInterface<Patient> foundPatients = maintainPatient.findPatientsByName(name);

            if (foundPatients.getNumberOfEntries() > 0) {
                displayMessage("\nFound " + foundPatients.getNumberOfEntries() + " patient(s) with name '" + name + "':");

                // Display found patients
                int patientNumber = 1;
                java.util.Iterator<Patient> iterator = foundPatients.iterator();
                while (iterator.hasNext()) {
                    displayMessage("\n--- Patient " + patientNumber + " ---");
                    Patient p = iterator.next();
                    int age = p.calculateAge();
                    displayPatient(p, age);
                    patientNumber++;
                }
            } else {
                displayMessage("\nNo patients found with name '" + name + "'.");
            }

            continueSearching = askToContinueOperation("search another patient by name");
        }
    }

    /**
     * Handle search patient by IC
     */
    public void handleSearchPatientByIC() {
        boolean continueSearching = true;

        while (continueSearching) {
            String ic = inputPatientIC();

            if (ic == null || ic.equals("0")) {
                displayMessage("\nExiting search...");
                break;
            }

            // Validate input at UI level
            if (ic.trim().isEmpty()) {
                displayMessage("\nInvalid input. Please enter a valid IC number.");
                continue;
            }

            Patient patient = maintainPatient.findPatientByIC(ic);

            if (patient != null) {
                int age = patient.calculateAge();
                displayPatient(patient, age);
            } else {
                // Format IC for display in error message
                String displayIC = ic.length() == 12
                        ? ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8) : ic;
                displayMessage("\nPatient with IC '" + displayIC + "' not found.");
            }

            continueSearching = askToContinueOperation("search another patient by IC");
        }
    }

    // ================================
    // REGISTRATION HANDLERS
    // ================================
    // Handle patient registration process
    public void handlePatientRegistration() {
        boolean continueRegistering = true;

        while (continueRegistering) {
            // Get IC number and check if user wants to exit
            String icNumber = inputICForRegistration();

            if (icNumber.equals("0")) {
                displayMessage("\nExiting patient registration...\n");
                break;
            }

            Patient existingPatient = maintainPatient.checkExistingPatient(icNumber);

            if (existingPatient != null) {
                // Handle existing patient scenario
                handleExistingPatientRegistration(existingPatient);
            } else {
                // Handle new patient registration
                handleNewPatientRegistration(icNumber);
            }

            continueRegistering = askToContinueOperation("register another patient");
        }
    }

    /**
     * Handle existing patient registration scenario
     *
     * @param existingPatient the existing patient found
     */
    private void handleExistingPatientRegistration(Patient existingPatient) {
        // Show existing patient details
        displayMessage("\n=== PATIENT ALREADY EXISTS ===");
        int age = existingPatient.calculateAge();
        displayPatient(existingPatient, age);

        // Ask if they want to add existing patient to queue
        boolean addToQueue = askToAddExistingPatientToQueue(existingPatient.getName());
        if (addToQueue) {
            if (!maintainPatient.isPatientInQueue(existingPatient)) {
                String queueNum = maintainPatient.addPatientToQueue(existingPatient);
                if (queueNum != null) {
                    displayMessage("\nExisting patient " + existingPatient.getName() + " has been added to the line-up queue.");
                    displayMessage("Queue Number: " + queueNum);
                    displayMessage("Total patients in queue: " + maintainPatient.getQueueSize());
                }
            } else {
                displayMessage("\nPatient is already in the queue.");
            }
        } else {
            displayMessage("\nPatient not added to queue.");
        }
    }

    /**
     * Handle new patient registration
     *
     * @param icNumber IC number for the new patient
     */
    private void handleNewPatientRegistration(String icNumber) {
        // New patient - continue with registration
        displayMessage("\n=== NEW PATIENT REGISTRATION ===");
        Patient patient = inputRemainingPatientDetails(icNumber);

        // Register the patient
        Patient registeredPatient = maintainPatient.registerNewPatient(patient);

        if (registeredPatient != null) {
            displayMessage("\nPatient registered successfully!");

            // Add patient to queue option - use the registered patient with ID
            boolean joinQueue = askToJoinQueue();
            if (joinQueue) {
                String queueNum = maintainPatient.addPatientToQueue(registeredPatient);
                if (queueNum != null) {
                    displayMessage("\nPatient " + registeredPatient.getName() + " has been added to the line-up queue.");
                    displayMessage("Queue Number: " + queueNum);
                    displayMessage("Total patients in queue: " + maintainPatient.getQueueSize());
                } else {
                    displayMessage("\nFailed to add patient to queue. Please try again.");
                }
            } else {
                displayMessage("\nPatient not added to queue.");
            }
        } else {
            displayMessage("\nRegistration failed. IC number may already exist.");
        }
    }

    // ================================
    // UPDATE/REMOVE HANDLERS
    // ================================
    // Handle patient information update
    public void handleUpdatePatient() {
        boolean continueUpdating = true;

        while (continueUpdating) {
            String id = inputPatientId();

            if (id.equals("0")) {
                displayMessage("\nExiting update patient...");
                break;
            }

            Patient patient = maintainPatient.findPatientById(id);

            if (patient != null) {
                displayMessage("Current patient details:");
                int age = patient.calculateAge();
                displayPatient(patient, age);

                Patient updatedPatient = inputUpdatedPatientDetails(patient);

                boolean updateSuccess = maintainPatient.updatePatientInfo(patient, updatedPatient);

                if (updateSuccess) {
                    displayMessage("\nPatient updated successfully.");
                } else {
                    // IC number conflict detected
                    String displayIC = updatedPatient.getIcNumber().length() == 12
                            ? updatedPatient.getIcNumber().substring(0, 6) + "-"
                            + updatedPatient.getIcNumber().substring(6, 8) + "-"
                            + updatedPatient.getIcNumber().substring(8) : updatedPatient.getIcNumber();
                    displayMessage("\nERROR: IC number " + displayIC + " already exists for another patient.");
                    displayMessage("Update cancelled to prevent duplicate IC numbers.");
                }
            } else {
                displayMessage("\nPatient with ID '" + id + "' not found.");
            }

            continueUpdating = askToContinueOperation("update another patient");
        }
    }

    /**
     * Handle patient removal
     */
    public void handleRemovePatient() {
        boolean continueRemoving = true;

        while (continueRemoving) {
            String id = inputPatientId();

            if (id.equals("0")) {
                displayMessage("\nExiting remove patient...");
                break;
            }

            Patient patientToRemove = maintainPatient.findPatientById(id);

            if (patientToRemove != null) {
                // Show patient details and ask for confirmation
                displayMessage("\nPatient to be removed:");
                int age = patientToRemove.calculateAge();
                displayPatient(patientToRemove, age);

                // Show queue information if patient is in queue
                if (maintainPatient.isPatientInQueue(patientToRemove)) {
                    String queueNum = maintainPatient.getPatientQueueNumber(patientToRemove);
                    int queuePosition = maintainPatient.getPatientQueuePosition(patientToRemove);

                    displayFormattedQueueInfo(queueNum, patientToRemove.getName(),
                            patientToRemove.getId(), patientToRemove.getIcNumber(),
                            queuePosition);
                }

                boolean confirmed = confirmRemovePatient(patientToRemove.getName());

                if (confirmed) {
                    boolean removeSuccess = maintainPatient.removePatient(patientToRemove);

                    if (removeSuccess) {
                        displayMessage("\nPatient removed successfully.");
                    }
                } else {
                    displayMessage("\nPatient removal cancelled.");
                }
            } else {
                displayMessage("\nPatient with ID '" + id + "' not found.\n");
            }

            continueRemoving = askToContinueOperation("remove another patient");
        }
    }

    /**
     * Handle search patient menu
     */
    public void handleSearchPatientMenu() {
        int choice;
        do {
            choice = displaySearchPatientMenu();
            switch (choice) {
                case 1:
                    handleSearchPatientById();
                    break;
                case 2:
                    handleSearchPatientByName();
                    break;
                case 3:
                    handleSearchPatientByIC();
                    break;
                case 0:
                    displayMessage("\nReturning to Patient Management Menu...");
                    break;
                default:
                    displayMessage("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    // ================================
    // QUEUE MANAGEMENT HANDLERS
    // ================================
    // Add patient to queue
    private void handleAddPatientToQueue() {
        boolean continueAdding = true;

        while (continueAdding) {
            String id = inputPatientId();

            if (id.equals("0")) {
                displayMessage("Exiting add to queue...");
                break;
            }

            MaintainPatient.QueueOperationResult result = maintainPatient.addPatientToQueueById(id);

            // Handle result based on status code
            if (result.isSuccess()) {
                displayMessage("\nPatient " + result.getPatient().getName() + " added to line-up queue.");
                displayMessage("Queue Number: " + result.getQueueNumber());
                displayMessage("Total patients in queue: " + result.getTotalInQueue());
            } else {
                switch (result.getStatusCode()) {
                    case "INVALID_ID":
                        displayMessage("\nInvalid patient ID.");
                        break;
                    case "PATIENT_NOT_FOUND":
                        displayMessage("\nPatient ID not found.");
                        break;
                    case "ALREADY_IN_QUEUE":
                        displayMessage("\nPatient is already in the queue.");
                        break;
                    default:
                        displayMessage("\nFailed to add patient to queue.");
                }
            }

            continueAdding = askToContinueOperation("add another patient to queue");
        }
    }

    /**
     * Handle serve next patient
     */
    private void handleServeNextPatient() {
        boolean continueServing = true;

        while (continueServing && !maintainPatient.isQueueEmpty()) {
            MaintainPatient.QueueOperationResult result = maintainPatient.serveNextPatient();

            if (result.isSuccess()) {
                displayMessage("\nNow serving: "
                        + (result.getQueueNumber() != null ? "Queue #" + result.getQueueNumber() + " - " : "")
                        + result.getPatient().getName() + " (ID: " + result.getPatient().getId() + ")");
                displayMessage("Remaining patients in queue: " + result.getTotalInQueue());

                if (result.getTotalInQueue() > 0) {
                    continueServing = askToContinueOperation("serve next patient");
                } else {
                    displayMessage("\nAll patients have been served. Queue is now empty.");
                    continueServing = false;
                }
            } else {
                // Provide specific error messages for better user experience
                if ("QUEUE_EMPTY".equals(result.getStatusCode())) {
                    displayMessage("\nNo patients in the queue.");
                } else if ("NO_AVAILABLE_DOCTOR".equals(result.getStatusCode())) {
                    displayMessage("\nNo available doctors to serve patients.");
                    displayMessage("Please ensure doctors are available before serving patients.");
                } else if ("PEEK_FAILED".equals(result.getStatusCode())) {
                    displayMessage("\nError: Unable to access next patient in queue.");
                } else if ("DEQUEUE_FAILED".equals(result.getStatusCode())) {
                    displayMessage("\nError: Unable to remove patient from queue.");
                } else if ("OPERATION_FAILED".equals(result.getStatusCode())) {
                    displayMessage("\nError: Patient serving operation failed.");
                } else {
                    displayMessage("\nFailed to serve next patient. Error: " + result.getStatusCode());
                }
                continueServing = false;
            }
        }

        if (maintainPatient.isQueueEmpty() && continueServing) {
            displayMessage("\nNo patients in the queue.");
        }
    }

    /**
     * Handle view current queue
     */
    private void handleViewCurrentQueue() {
        entity.QueueStatusData queueStatus = maintainPatient.getCurrentQueueStatus();

        if (queueStatus.isEmpty()) {
            displayMessage("\nNo patients in the queue.");
        } else {
            displayMessage("\n=== CURRENT LINE-UP QUEUE ===");
            displayMessage("Total patients waiting: " + queueStatus.getTotalWaiting());

            if (queueStatus.getNextPatient() != null) {
                displayMessage("Next to serve: "
                        + (queueStatus.getNextQueueNumber() != null ? "Queue #" + queueStatus.getNextQueueNumber() + " - " : "")
                        + queueStatus.getNextPatient().getName() + " (ID: " + queueStatus.getNextPatient().getId() + ")");
            }

            // Display queue table if needed
            String[][] queueData = formatQueueTableData();
            if (queueData.length > 0) {
                displayQueueTableData(queueData);
            }
        }

        askToContinueOrReturnToMenu();
    }

    // Display queue table data
    private void displayQueueTableData(String[][] queueData) {
        System.out.println("\n--- QUEUE TABLE ---");
        System.out.println("================================================================================");
        System.out.println(String.format("%-8s %-8s %-20s %-8s %-5s %-15s",
                "Queue#", "ID", "Name", "Gender", "Age", "Phone"));
        System.out.println("================================================================================");

        for (String[] row : queueData) {
            System.out.println(String.format("%-8s %-8s %-20s %-8s %-5s %-15s",
                    row[0], row[1], row[2], row[3], row[4], row[5]));
        }
        System.out.println("================================================================================");
    }

    /**
     * Handle search in queue menu
     */
    private void handleSearchInQueueMenu() {
        int choice;
        do {
            choice = displaySearchInQueueMenu();
            switch (choice) {
                case 1:
                    handleSearchInQueueByName();
                    break;
                case 2:
                    handleSearchInQueueByIC();
                    break;
                case 0:
                    displayMessage("\nReturning to Queue Management Menu...");
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    /**
     * Handle search in queue by name
     */
    private void handleSearchInQueueByName() {
        boolean continueSearching = true;

        while (continueSearching) {
            String name = inputPatientName();

            if (name.equals("0")) {
                displayMessage("\nExiting search...");
                break;
            }

            entity.SearchResult result = maintainPatient.searchPatientInQueueByName(name);

            if (result.isFound()) {
                displayFormattedQueueInfo(result.getQueueNumber(), result.getPatient().getName(),
                        result.getPatient().getId(), result.getPatient().getIcNumber(),
                        result.getQueuePosition());
            } else {
                displayMessage("\nNo patient with name '" + name + "' found in the queue.");
            }

            continueSearching = askToContinueOperation("search another patient by name");
        }
    }

    /**
     * Handle search in queue by IC
     */
    private void handleSearchInQueueByIC() {
        boolean continueSearching = true;

        while (continueSearching) {
            String ic = inputPatientIC();

            if (ic.equals("0")) {
                displayMessage("\nExiting search...");
                break;
            }

            entity.SearchResult result = maintainPatient.searchPatientInQueueByIC(ic);

            if (result.isFound()) {
                displayFormattedQueueInfo(result.getQueueNumber(), result.getPatient().getName(),
                        result.getPatient().getId(), result.getPatient().getIcNumber(),
                        result.getQueuePosition());
            } else {
                String displayIC = ic.length() == 12
                        ? ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8) : ic;
                displayMessage("\nNo patient with IC '" + displayIC + "' found in the queue.");
            }

            continueSearching = askToContinueOperation("search another patient by IC");
        }
    }

    /**
     * Handle clear queue
     */
    private void handleClearQueue() {
        if (maintainPatient.isQueueEmpty()) {
            displayMessage("\nQueue is already empty.");
        } else {
            boolean confirm = confirmClearQueue();
            if (confirm) {
                MaintainPatient.QueueOperationResult result = maintainPatient.clearQueueLogic();

                if (result.isSuccess()) {
                    displayMessage("\nQueue Cleared Successfully.");
                    displayMessage(result.getTotalInQueue() + " patients removed.");
                } else {
                    displayMessage("\nQueue is already empty.");
                }
            } else {
                displayMessage("\nQueue clear cancelled.");
            }
        }
        askToContinueOrReturnToMenu();
    }

    public void displayPatientsTableWithData(String[][] patientData, String sortType) {
        System.out.println("\n=== ALL PATIENTS (" + sortType + ") ===");
        System.out.println("Total Patients: " + patientData.length);
        System.out.println();

        System.out.println("========================================================================================");
        // Table header with correct order: ID, Name, Gender, Age, IC, Phone, Reg Date
        System.out.printf("%-8s %-20s %-8s %-4s %-16s %-14s %-12s%n",
                "ID", "Name", "Gender", "Age", "IC Number", "Phone", "Reg Date");
        System.out.println("========================================================================================");

        // Table rows using prepared data from control layer
        for (int i = 0; i < patientData.length; i++) {
            System.out.printf("%-8s %-20s %-8s %-4s %-16s %-14s %-12s%n",
                    patientData[i][0], // ID
                    FormatUtil.truncateString(patientData[i][1], 20), // Name
                    patientData[i][2], // Gender
                    patientData[i][3], // Age (pre-calculated by control)
                    FormatUtil.formatICForDisplay(patientData[i][4]), // IC
                    patientData[i][5], // Phone
                    FormatUtil.formatDateForDisplay(java.time.LocalDate.parse(patientData[i][6]))); // Reg Date
        }
        System.out.println("========================================================================================");
    }

    public boolean confirmRemovePatient(String patientName) {
        System.out.print("\nAre you sure you want to remove patient '" + patientName + "'? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    public boolean askToAddExistingPatientToQueue(String patientName) {
        System.out.print("\nWould you like to add existing patient '" + patientName + "' to the line-up queue? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    // Display patient treatment report with clear formatting
    public void displayPatientTreatmentReport(MaintainPatient.PatientTreatmentData data) {
        String line = "=".repeat(90);
        String separator = "-".repeat(90);

        // Top header
        System.out.println("\n" + line);
        System.out.printf("%70s%n", "TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.printf("%54s%n", "CLINIC MANAGEMENT SYSTEM");
        System.out.println(line);

// Report title
        System.out.printf("%60s%n", "PATIENT TREATMENT STATISTICS REPORT");
        System.out.println(line);

// Generated time
        String generatedTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d yyyy, hh:mm a"));
        System.out.printf("Generated at: %s%n", generatedTime);
        System.out.println(separator);

        // Overall Statistics Section
        System.out.println("\n OVERALL TREATMENT STATISTICS");
        System.out.println(separator);
        System.out.printf("%-35s : %d%n", "Total Registered Patients", data.getTotalPatients());
        System.out.printf("%-35s : %d%n", "Total Treatments Given", data.getTotalTreatments());
        System.out.printf("%-35s : %d%n", "Patients with Treatments", data.getPatientsWithTreatments());
        System.out.printf("%-35s : %d%n", "Patients without Treatments", data.getPatientsWithoutTreatments());

        System.out.println(separator);

        // Detailed Breakdown Section
        System.out.println("\n DETAILED PATIENT TREATMENT BREAKDOWN (Sorted by ID)");
        System.out.println(separator);
        System.out.printf("%-10s %-25s %-10s %-6s %-12s%n", "ID", "Name", "Gender", "Age", "Treatments");
        System.out.println(separator);

        java.util.Iterator<MaintainPatient.PatientTreatmentInfo> iterator = data.getTreatmentStats().iterator();
        while (iterator.hasNext()) {
            MaintainPatient.PatientTreatmentInfo info = iterator.next();
            System.out.printf("%-10s %-25s %-10s %-6d %-12d%n",
                    info.getPatientId(),
                    info.getPatientName(),
                    info.getGender(),
                    info.getAge(),
                    info.getTreatmentCount()
            );
        }

        System.out.println(separator);
        System.out.println(line + "\n");

        askToContinueOrReturnToMenu();
    }

    public void displayRegistrationTimelineReport(String[][] patientData, Patient firstPatient, Patient lastPatient,
            int maleCount, int femaleCount, MapInterface<String, Integer> monthlyCount) {

        int width = 90;
        String lineEq = "=".repeat(width);
        String lineDash = "-".repeat(width);

        System.out.println();
        System.out.println(lineEq);
        System.out.printf("%70s%n", "TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.printf("%54s%n", "CLINIC MANAGEMENT SYSTEM");
        System.out.println(lineEq);

        System.out.printf("%57s%n", "REGISTRATION TIMELINE REPORT");
        System.out.println(lineEq);

// Generated time
        String generatedTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d yyyy, hh:mm a"));
        System.out.printf("Generated at: %s%n", generatedTime);
        System.out.println(lineDash);

        // REGISTRATION STATISTICS
        System.out.println("\nREGISTRATION STATISTICS");
        System.out.println(lineDash);
        System.out.printf("%-30s : %d%n", "Total Registered Patients", patientData.length);
        System.out.printf("%-30s : %s (%s)%n", "First Registration",
                firstPatient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")),
                firstPatient.getName());
        System.out.printf("%-30s : %s (%s)%n", "Latest Registration",
                lastPatient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")),
                lastPatient.getName());
        System.out.printf("%-30s : %d Male, %d Female%n", "Gender Distribution", maleCount, femaleCount);
        System.out.println(lineDash);

        // DETAILED TIMELINE
        System.out.println("\nDETAILED REGISTRATION TIMELINE");
        System.out.println(lineDash);
        System.out.printf("%-4s %-10s %-20s %-8s %-3s %-16s %-12s%n",
                "#", "Patient ID", "Name", "Gender", "Age", "IC Number", "Reg Date");
        System.out.println(lineDash);

        for (int i = 0; i < patientData.length; i++) {
            System.out.printf("%-4d %-10s %-20s %-8s %-3s %-16s %-12s%n",
                    i + 1,
                    patientData[i][0],
                    FormatUtil.truncateString(patientData[i][1], 20),
                    patientData[i][2],
                    patientData[i][3],
                    FormatUtil.formatICForDisplay(patientData[i][4]),
                    FormatUtil.formatDateForDisplay(java.time.LocalDate.parse(patientData[i][6])));
        }
        System.out.println(lineDash);

        // MONTHLY BREAKDOWN
        if (monthlyCount != null) {
            System.out.println("\nMONTHLY REGISTRATION BREAKDOWN");
            System.out.println(lineDash);
            System.out.printf("%-20s %-20s%n", "Month", "Patient Count");
            System.out.println(lineDash);

            ListInterface<String> months = monthlyCount.getKeys();

            // Display monthly statistics
            java.util.Iterator<String> monthIterator = months.iterator();
            while (monthIterator.hasNext()) {
                String month = monthIterator.next();
                Integer count = monthlyCount.get(month);
                System.out.printf("%-20s %-20s%n", month, count + " patients");
            }
            System.out.println(lineEq);
        }

        askToContinueOrReturnToMenu();
    }

    /**
     * Ask user if they want to book another appointment
     */
    private void askToBookAnotherAppointment() {
        System.out.print("\nWould you like to book another appointment? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("y") || response.equals("yes")) {
            handleBookAppointment(); // Book another appointment
        }
        // If 'n' or anything else, return to appointment menu
    }

    /**
     * Display appointment sorting menu
     */
    private void displayAppointmentSortingMenu() {
        displayMessage("\n=== APPOINTMENT SORTING OPTIONS ===");
        displayMessage("1. Sort by Appointment ID");
        displayMessage("2. Sort by Time");
        displayMessage("0. Return to menu");
        displayMessage("-----------------------------------");
        System.out.print("Enter your choice: ");
    }

    /**
     * Display sorting message based on user choice
     *
     * @param sortChoice User's sorting choice
     */
    private void displaySortingMessage(int sortChoice) {
        switch (sortChoice) {
            case 1:
                displayMessage("Appointments sorted by ID:");
                break;
            case 2:
                displayMessage("Appointments sorted by Time:");
                break;
            case 0:
                displayMessage("Appointments (unsorted):");
                break;
            default:
                displayMessage("Appointments (unsorted):");
                break;
        }
    }

    /**
     * Validate time format (HH:mm)
     *
     * @param timeString Time string to validate
     * @return true if valid format, false otherwise
     */
    /**
     * Display formatted queue information with better layout
     *
     * @param queueNumber Queue number
     * @param name Patient name
     * @param id Patient ID
     * @param ic Patient IC
     * @param queuePosition Queue position
     */
    private void displayFormattedQueueInfo(String queueNumber, String name, String id, String ic, int queuePosition) {
        System.out.println("\n=====================================");
        System.out.println("   Patient FOUND in queue");
        System.out.println("=====================================");
        System.out.printf(" Queue Number   : %s%n", queueNumber != null ? queueNumber : "N/A");
        System.out.printf(" Name           : %s%n", name);
        System.out.printf(" ID             : %s%n", id);

        // Format IC with dashes if it's 12 digits
        String formattedIC = ic;
        if (ic != null && ic.length() == 12 && !ic.contains("-")) {
            formattedIC = ic.substring(0, 6) + "-" + ic.substring(6, 8) + "-" + ic.substring(8);
        }
        System.out.printf(" IC             : %s%n", formattedIC);
        System.out.printf(" Queue Position : %d%n", queuePosition);
        System.out.println("-------------------------------------");
    }

    /**
     * Convert appointment result codes to user-friendly messages
     */
    private String getAppointmentResultMessage(MaintainPatient.OperationResult resultCode) {
        switch (resultCode) {
            case SUCCESS:
                return "Operation completed successfully!";
            case PATIENT_NOT_FOUND:
                return "Patient not found. Please register the patient first.";
            case DOCTOR_NOT_FOUND:
                return "Doctor not found.";
            case DOCTOR_UNAVAILABLE:
                return "Doctor is not available for appointments or has a scheduling conflict.";
            case INVALID_TIME_FORMAT:
                return "Invalid time format. Please use HH:mm format (e.g., 14:30).";
            case INVALID_BUSINESS_HOURS:
                return "Appointment time must be between 09:00 and 17:00.";
            case APPOINTMENT_NOT_FOUND:
                return "No appointments found.";
            case INVALID_INPUT:
                return "Invalid input provided. Please check your data and try again.";
            default:
                return "An error occurred during the operation.";
        }
    }
}
