package boundary;

import control.MaintainPatient;
import entity.Patient;
import adt.*;
import utility.MessageUI;
import utility.FormatUtil;

import java.util.Scanner;

public class PatientUI {

    private final Scanner scanner = new Scanner(System.in);
    private final MaintainPatient maintainPatient;

    public PatientUI(MaintainPatient control) {
        this.maintainPatient = control;
    }

    public void run() {
        int choice;
        do {
            displayMenu();
            choice = getMenuChoice();
            maintainPatient.handleMenuChoice(choice);
        } while (choice != 0);
    }

    public void displayMenu() {
        System.out.println("\n===============================");
        System.out.println("        PATIENT MAIN MENU      ");
        System.out.println("===============================");
        System.out.println("1. Patient Management");
        System.out.println("2. Queue Management");
        System.out.println("3. Patient Statistics Report");
        System.out.println("4. Patient Contact Directory");
        System.out.println("0. Back to Main Menu");
        System.out.println("-------------------------------");
    }

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

    public Patient inputPatientDetails() {
        String name = inputName();
        String icNumber = inputValidIC();
        String phone = inputPhone();
        String gender = inputGender();
        System.out.println();
        return maintainPatient.createPatient(name, icNumber, phone, gender);
    }

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

            icNumber = icNumber.replaceAll("-", "");

            if (icNumber.isEmpty()) {
                System.out.println("\nIC Number cannot be empty. Please try again.\n");
            } else if (!icNumber.matches("\\d{12}")) {
                System.out.println("\nIC Number must be exactly 12 digits (Malaysian format: YYMMDDPBXXXX). Please try again.\n");
            } else if (!maintainPatient.isValidMalaysianIC(icNumber)) {
                System.out.println("\nInvalid Malaysian IC format. Please enter a valid IC number.\n");
            }
        } while (icNumber.isEmpty() || !icNumber.matches("\\d{12}") || !maintainPatient.isValidMalaysianIC(icNumber));

        // Return IC without dashes for storage
        return icNumber;
    }

    public Patient inputRemainingPatientDetails(String icNumber) {
        // Display IC with dashes for confirmation
        String displayIC = utility.FormatUtil.formatICForDisplay(icNumber);
        System.out.println("IC Number: " + displayIC + " (Confirmed)");
        System.out.println();

        String name = inputName();
        String phone = inputPhone();
        String gender = inputGender();

        return maintainPatient.createPatient(name, icNumber, phone, gender);
    }

    private String inputName() {
        String name;
        do {
            System.out.print("Enter Name      : ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("\nName cannot be empty. Please try again.\n");
            } else if (!maintainPatient.isValidName(name)) {
                System.out.println("\nName must contain only letters and spaces and be at least 2 characters long. Please try again.\n");
            }
        } while (name.isEmpty() || !maintainPatient.isValidName(name));
        return name;
    }

    private String inputValidIC() {
        String icNumber;
        do {
            System.out.print("Enter IC Number (12 digits): ");
            icNumber = scanner.nextLine().trim().replaceAll("-", "");

            if (icNumber.isEmpty()) {
                System.out.println("\nIC Number cannot be empty. Please try again.\n");
            } else if (!icNumber.matches("\\d{12}")) {
                System.out.println("\nIC Number must be exactly 12 digits (Malaysian format: YYMMDDPBXXXX). Please try again.\n");
            } else if (!maintainPatient.isValidMalaysianIC(icNumber)) {
                System.out.println("\nInvalid Malaysian IC format. Please enter a valid IC number.\n");
            }
        } while (icNumber.isEmpty() || !icNumber.matches("\\d{12}") || !maintainPatient.isValidMalaysianIC(icNumber));

        // Return IC without dashes for storage
        return icNumber;
    }



    private String inputPhone() {
        String phone;
        do {
            System.out.print("Enter Phone     : ");
            phone = scanner.nextLine().trim();

            if (phone.isEmpty()) {
                System.out.println("\nPhone number cannot be empty. Please try again.\n");
            } else if (!maintainPatient.isValidPhone(phone)) {
                System.out.println("\nInvalid Malaysian phone number format. Must be 10-11 digits. Please try again.\n");
            }
        } while (phone.isEmpty() || !maintainPatient.isValidPhone(phone));

        // Format phone number for storage
        return maintainPatient.formatPhoneForStorage(phone);
    }

    private String inputGender() {
        String gender;
        do {
            System.out.print("Enter Gender (M/F): ");
            gender = scanner.nextLine().trim();

            if (gender.isEmpty()) {
                System.out.println("\nGender cannot be empty. Please try again.\n");
            } else {
                // Normalize gender input using control layer
                String normalizedGender = maintainPatient.normalizeGender(gender);
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
            while (!maintainPatient.isValidName(nameInput)) {
                System.out.println("\nInvalid name. Name must be at least 2 characters and contain only letters and spaces.\n");
                System.out.print("Name [" + currentPatient.getName() + "]: ");
                nameInput = scanner.nextLine().trim();
                if (nameInput.isEmpty()) {
                    nameInput = currentPatient.getName();
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
            String normalizedGender = maintainPatient.normalizeGender(genderInput);
            while (normalizedGender == null) {
                System.out.println("\nInvalid gender. Please enter M/Male or F/Female.\n");
                System.out.print("Gender [" + currentPatient.getGender() + "]: ");
                genderInput = scanner.nextLine().trim();
                if (genderInput.isEmpty()) {
                    normalizedGender = currentPatient.getGender();
                    break;
                } else {
                    normalizedGender = maintainPatient.normalizeGender(genderInput);
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
            icInput = icInput.replaceAll("-", "");
            while (!maintainPatient.isValidMalaysianIC(icInput)) {
                System.out.println("\nInvalid IC number. Must be 12 digits in Malaysian format.\n");
                System.out.print("IC Number [" + utility.FormatUtil.formatICForDisplay(currentPatient.getIcNumber()) + "] (12 digits): ");
                icInput = scanner.nextLine().trim();
                if (icInput.isEmpty()) {
                    icInput = currentPatient.getIcNumber();
                    break;
                } else {
                    icInput = icInput.replaceAll("-", "");
                }
            }
            icNumber = icInput; // Store without dashes
        }

        // Phone update with validation
        System.out.print("Phone [" + currentPatient.getPhone() + "]: ");
        String phoneInput = scanner.nextLine().trim();
        String phone;
        if (phoneInput.isEmpty()) {
            phone = currentPatient.getPhone();
        } else {
            phoneInput = phoneInput.replaceAll("[\\s-]", "");
            while (!maintainPatient.isValidPhone(phoneInput)) {
                System.out.println("\nInvalid phone number. Must be 10-11 digits Malaysian format.\n");
                System.out.print("Phone [" + currentPatient.getPhone() + "]: ");
                phoneInput = scanner.nextLine().trim();
                if (phoneInput.isEmpty()) {
                    phoneInput = currentPatient.getPhone().replaceAll("[\\s-]", "");
                    break;
                } else {
                    phoneInput = phoneInput.replaceAll("[\\s-]", "");
                }
            }
            // Format phone number using control layer
            phone = maintainPatient.formatPhoneForStorage(phoneInput);
        }

        // Return updated data to control layer for entity creation
        return maintainPatient.createPatientWithId(currentPatient.getId(), name, icNumber, phone, gender, currentPatient.getRegistrationDate());
    }

    public int displayViewPatientsMenu() {
        System.out.println("\n=== VIEW PATIENTS MENU ===");
        System.out.println("1. View All Patients (Default Order)");
        System.out.println("2. Sort by Patient ID");
        System.out.println("3. Sort by Name");
        System.out.println("4. Sort by Age");
        System.out.println("5. Sort by Registration Date");
        System.out.println("0. Return to Main Menu");
        System.out.println("==========================");
        return getMenuChoice();
    }

    public void displayPatientsTableWithData(String[][] patientData, String sortType) {
        System.out.println("\n=== ALL PATIENTS (" + sortType + ") ===");
        System.out.println("Total Patients: " + patientData.length);
        System.out.println();

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

    public void displayQueueTable(QueueInterface<Patient> queue) {
        String queueTableData = maintainPatient.getQueueTableData(queue);
        System.out.println(queueTableData);
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



    public void displayAgeDemographicsReport(int totalPatients, int under18, int age18to30, int age31to50, int age51to65, int over65) {
        System.out.println("\n=== AGE DEMOGRAPHICS REPORT ===");
        System.out.println("Total Registered Patients: " + totalPatients);
        System.out.println();
        System.out.println("Under 18: " + under18 + " patients");
        System.out.println("18-30 years: " + age18to30 + " patients");
        System.out.println("31-50 years: " + age31to50 + " patients");
        System.out.println("51-65 years: " + age51to65 + " patients");
        System.out.println("Over 65: " + over65 + " patients");
        System.out.println("===============================\n");

        askToReturnToMainMenu();
    }

    public void displayRegistrationTimelineReport(String[][] patientData, Patient firstPatient, Patient lastPatient,
                                                 int maleCount, int femaleCount, MapInterface<String, Integer> monthlyCount) {
        System.out.println("\n================================================================================");
        System.out.println("                    REGISTRATION TIMELINE REPORT");
        System.out.println("================================================================================");

        // Registration Statistics
        System.out.println("\nREGISTRATION STATISTICS:");
        System.out.println("--------------------------------------------------");
        System.out.println("Total Registered Patients: " + patientData.length);
        System.out.println("First Registration: " + firstPatient.getRegistrationDate().format(
            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")) + " (" + firstPatient.getName() + ")");
        System.out.println("Latest Registration: " + lastPatient.getRegistrationDate().format(
            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")) + " (" + lastPatient.getName() + ")");
        System.out.println("Gender Distribution: " + maleCount + " Male, " + femaleCount + " Female");

        // Detailed Timeline
        System.out.println("\nDETAILED REGISTRATION TIMELINE:");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-6s %-10s %-20s %-8s %-3s %-16s %-12s%n",
                         "#", "Patient ID", "Name", "Gender", "Age", "IC Number", "Reg Date");
        System.out.println("--------------------------------------------------------------------------------");

        for (int i = 0; i < patientData.length; i++) {
            System.out.printf("%-6d %-10s %-20s %-8s %-3s %-16s %-12s%n",
                             i+1,
                             patientData[i][0], // ID
                             FormatUtil.truncateString(patientData[i][1], 20), // Name
                             patientData[i][2], // Gender
                             patientData[i][3], // Age (pre-calculated)
                             FormatUtil.formatICForDisplay(patientData[i][4]), // IC
                             FormatUtil.formatDateForDisplay(java.time.LocalDate.parse(patientData[i][6]))); // Reg Date
        }

        // Monthly Breakdown
        System.out.println("\nMONTHLY REGISTRATION BREAKDOWN:");
        System.out.println("--------------------------------------------------");
        ListInterface<String> months = monthlyCount.getKeys();
        for (int i = 1; i <= months.getNumberOfEntries(); i++) {
            String month = months.getEntry(i);
            Integer count = monthlyCount.get(month);
            System.out.printf("%-15s: %3d patients%n", month, count);
        }

        System.out.println("\n================================================================================\n");

        askToReturnToMainMenu();
    }


}
