package boundary;

import control.MaintainPatient;
import entity.Patient;
import utility.MessageUI;

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
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
    }

    public Patient inputPatientDetails() {
        String name = inputValidName();
        String icNumber = inputValidIC();
        String phone = inputValidPhone();
        String gender = inputValidGender();
        System.out.println();
        return new Patient(name, icNumber, phone, gender);
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
            } else if (!isValidMalaysianIC(icNumber)) {
                System.out.println("\nInvalid Malaysian IC format. Please enter a valid IC number.\n");
            }
        } while (icNumber.isEmpty() || !icNumber.matches("\\d{12}") || !isValidMalaysianIC(icNumber));

        // Return IC without dashes for storage
        return icNumber;
    }

    public Patient inputRemainingPatientDetails(String icNumber) {
        // Display IC with dashes for confirmation
        String displayIC = formatICForDisplay(icNumber);
        System.out.println("IC Number: " + displayIC + " (Confirmed)");
        System.out.println();

        String name = inputValidName();
        String phone = inputValidPhone();
        String gender = inputValidGender();

        return new Patient(name, icNumber, phone, gender);
    }

    private String inputValidName() {
        String name;
        do {
            System.out.print("Enter Name      : ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("\nName cannot be empty. Please try again.\n");
            } else if (name.length() < 2) {
                System.out.println("\nName must be at least 2 characters long. Please try again.\n");
            } else if (!name.matches("[a-zA-Z\\s]+")) {
                System.out.println("\nName can only contain letters and spaces. Please try again.\n");
            }
        } while (name.isEmpty() || name.length() < 2 || !name.matches("[a-zA-Z\\s]+"));
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
            } else if (!isValidMalaysianIC(icNumber)) {
                System.out.println("\nInvalid Malaysian IC format. Please enter a valid IC number.\n");
            }
        } while (icNumber.isEmpty() || !icNumber.matches("\\d{12}") || !isValidMalaysianIC(icNumber));

        // Return IC without dashes for storage
        return icNumber;
    }

    private boolean isValidMalaysianIC(String ic) {
        if (ic.length() != 12) return false;

        // Check birth date part (first 6 digits)
        String year = ic.substring(0, 2);
        String month = ic.substring(2, 4);
        String day = ic.substring(4, 6);

        int monthInt = Integer.parseInt(month);
        int dayInt = Integer.parseInt(day);

        // Basic validation for month and day
        if (monthInt < 1 || monthInt > 12) return false;
        if (dayInt < 1 || dayInt > 31) return false;

        // Check place of birth (7th and 8th digits) - should be valid state codes
        String placeCode = ic.substring(6, 8);
        int placeInt = Integer.parseInt(placeCode);
        if (placeInt < 1 || placeInt > 59) return false; // Valid Malaysian state codes

        return true;
    }

    private String inputValidPhone() {
        String phone;
        do {
            System.out.print("Enter Phone     : ");
            phone = scanner.nextLine().trim().replaceAll("[\\s-]", "");

            if (phone.isEmpty()) {
                System.out.println("\nPhone number cannot be empty. Please try again.\n");
            } else if (!phone.matches("\\d{10,11}")) {
                System.out.println("\nPhone number must be 10-11 digits (Malaysian format). Please try again.\n");
            } else if (!phone.startsWith("01") && !phone.startsWith("03") && !phone.startsWith("04") &&
                      !phone.startsWith("05") && !phone.startsWith("06") && !phone.startsWith("07") &&
                      !phone.startsWith("08") && !phone.startsWith("09")) {
                System.out.println("\nInvalid Malaysian phone number format. Please try again.\n");
            }
        } while (phone.isEmpty() || !phone.matches("\\d{10,11}") ||
                (!phone.startsWith("01") && !phone.startsWith("03") && !phone.startsWith("04") &&
                 !phone.startsWith("05") && !phone.startsWith("06") && !phone.startsWith("07") &&
                 !phone.startsWith("08") && !phone.startsWith("09")));

        // Format phone number for display
        if (phone.length() == 10) {
            return phone.substring(0, 3) + "-" + phone.substring(3, 7) + phone.substring(7);
        } else {
            return phone.substring(0, 3) + "-" + phone.substring(3, 7) + "-" + phone.substring(7);
        }
    }

    private String inputValidGender() {
        String gender;
        do {
            System.out.print("Enter Gender (M/F): ");
            gender = scanner.nextLine().trim();

            if (gender.isEmpty()) {
                System.out.println("\nGender cannot be empty. Please try again.\n");
            } else {
                // Normalize gender input
                String normalizedGender = normalizeGender(gender);
                if (normalizedGender != null) {
                    return normalizedGender;
                } else {
                    System.out.println("\nInvalid gender. Please enter M/Male or F/Female.\n");
                }
            }
        } while (true);
    }

    private String normalizeGender(String input) {
        String lower = input.toLowerCase();
        if (lower.equals("m") || lower.equals("male")) {
            return "Male";
        } else if (lower.equals("f") || lower.equals("female")) {
            return "Female";
        }
        return null;
    }

    private String formatICForDisplay(String icNumber) {
        if (icNumber.length() == 12) {
            return icNumber.substring(0, 6) + "-" + icNumber.substring(6, 8) + "-" + icNumber.substring(8);
        }
        return icNumber; // Return as-is if not 12 digits
    }

    public String inputPatientId() {
        System.out.print("\nEnter Patient ID (or '0' to exit): ");
        return scanner.nextLine().trim();
    }

    public void displayPatientList(String output) {
        System.out.println("\n--- Patient List ---\n" + output);
    }

    public void displayPatient(Patient patient) {
        System.out.println("\n--- Patient Found ---");
        System.out.println("ID        : " + patient.getId());
        System.out.println("Name      : " + patient.getName());
        System.out.println("IC Number : " + formatICForDisplay(patient.getIcNumber()));
        System.out.println("Phone     : " + patient.getPhone());
        System.out.println("Gender    : " + patient.getGender());
        System.out.println("Reg Date  : " + patient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
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
        System.out.println("5. Simulate Queue Processing");
        System.out.println("6. Clear Queue");
        System.out.println("0. Back to Patient Menu");
        System.out.println("-------------------------------");
        System.out.print("Enter your choice: ");

        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
    }

    public boolean confirmClearQueue() {
        System.out.print("Are you sure you want to clear the entire queue? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
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
        System.out.print("Enter your choice: ");

        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
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
        System.out.print("Enter your choice: ");

        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
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
        System.out.print("Enter your choice: ");

        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next(); // discard invalid input
            System.out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // flush newline
        return choice;
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
            while (!nameInput.matches("[a-zA-Z\\s]+") || nameInput.length() < 2) {
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

        // IC Number update with validation
        System.out.print("IC Number [" + formatICForDisplay(currentPatient.getIcNumber()) + "] (12 digits): ");
        String icInput = scanner.nextLine().trim();
        String icNumber;
        if (icInput.isEmpty()) {
            icNumber = currentPatient.getIcNumber();
        } else {
            icInput = icInput.replaceAll("-", "");
            while (!icInput.matches("\\d{12}") || !isValidMalaysianIC(icInput)) {
                System.out.println("\nInvalid IC number. Must be 12 digits in Malaysian format.\n");
                System.out.print("IC Number [" + formatICForDisplay(currentPatient.getIcNumber()) + "] (12 digits): ");
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
            while (!phoneInput.matches("\\d{10,11}") ||
                   (!phoneInput.startsWith("01") && !phoneInput.startsWith("03") && !phoneInput.startsWith("04") &&
                    !phoneInput.startsWith("05") && !phoneInput.startsWith("06") && !phoneInput.startsWith("07") &&
                    !phoneInput.startsWith("08") && !phoneInput.startsWith("09"))) {
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
            // Format phone number
            if (phoneInput.length() == 10) {
                phone = phoneInput.substring(0, 3) + "-" + phoneInput.substring(3, 7) + phoneInput.substring(7);
            } else {
                phone = phoneInput.substring(0, 3) + "-" + phoneInput.substring(3, 7) + "-" + phoneInput.substring(7);
            }
        }

        // Gender update with validation
        System.out.print("Gender [" + currentPatient.getGender() + "]: ");
        String genderInput = scanner.nextLine().trim();
        String gender;
        if (genderInput.isEmpty()) {
            gender = currentPatient.getGender();
        } else {
            String normalizedGender = normalizeGender(genderInput);
            while (normalizedGender == null) {
                System.out.println("\nInvalid gender. Please enter M/Male or F/Female.\n");
                System.out.print("Gender [" + currentPatient.getGender() + "]: ");
                genderInput = scanner.nextLine().trim();
                if (genderInput.isEmpty()) {
                    normalizedGender = currentPatient.getGender();
                    break;
                } else {
                    normalizedGender = normalizeGender(genderInput);
                }
            }
            gender = normalizedGender;
        }

        // Create updated patient with same ID and registration date
        return new Patient(currentPatient.getId(), name, icNumber, phone, gender, currentPatient.getRegistrationDate());
    }

    public void displayPatientsTable(adt.ListInterface<entity.Patient> patientList) {
        System.out.println("\n=== ALL PATIENTS (Sorted by Registration Date) ===");
        System.out.println("Total Patients: " + patientList.getNumberOfEntries());
        System.out.println();

        // Table header with correct sequence: ID, Name, Gender, IC, Phone, Date
        System.out.printf("%-8s %-22s %-8s %-16s %-16s %-12s%n",
                         "ID", "Name", "Gender", "IC Number", "Phone Number", "Reg Date");
        System.out.println("======================================================================================");

        // Table rows
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            entity.Patient p = patientList.getEntry(i);
            System.out.printf("%-8s %-22s %-8s %-16s %-16s %-12s%n",
                             p.getId(),
                             truncateString(p.getName(), 22),
                             p.getGender(),
                             formatICForDisplay(p.getIcNumber()),
                             p.getPhone(),
                             p.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }
        System.out.println("======================================================================================");
    }

    public void displayQueueTable(adt.QueueInterface<entity.Patient> queue) {
        System.out.println("\n--- QUEUE TABLE ---");

        // Table header with correct sequence: Queue#, ID, Name, Gender, IC, Phone
        System.out.printf("%-8s %-8s %-20s %-8s %-16s %-16s%n",
                         "Queue#", "ID", "Name", "Gender", "IC Number", "Phone Number");
        System.out.println("================================================================================");

        // Create temporary queue to traverse without modifying original
        adt.QueueInterface<entity.Patient> tempQueue = new adt.LinkedQueue<>();
        int position = 1;

        // Move patients to temp queue while displaying
        while (!queue.isEmpty()) {
            entity.Patient p = queue.dequeue();
            String queueNum = queue.getQueueNumber(p);
            if (queueNum == null) {
                queueNum = String.format("%04d", position);
            }

            System.out.printf("%-8s %-8s %-20s %-8s %-16s %-16s%n",
                             queueNum,
                             p.getId(),
                             truncateString(p.getName(), 20),
                             p.getGender(),
                             formatICForDisplay(p.getIcNumber()),
                             p.getPhone());

            tempQueue.enqueue(p);
            position++;
        }

        // Restore original queue
        while (!tempQueue.isEmpty()) {
            queue.enqueue(tempQueue.dequeue());
        }

        System.out.println("================================================================================");
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
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
}
