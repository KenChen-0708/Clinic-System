package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.PharmacyUI;
import dao.MedicineDAO;
import dao.PatientDAO;
import entity.Medicine;
import utility.MessageUI;
import dao.TreatmentDAO;
import entity.Patient;
import entity.Treatment;

public class MaintainPharmacy {

    private ListInterface<Medicine> medicineList = new DoublyLinkedList<>();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private PharmacyUI pharmacyUI = new PharmacyUI();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ListInterface<Patient> patientList = patientDAO.retrieveFromFile();

    public MaintainPharmacy() {
        medicineList = medicineDAO.retrieveFromFile();
    }

    public void runPharmacyMenu() {
        int choice = -1;
        do {
            choice = pharmacyUI.getMenuChoice();
            switch (choice) {
                case 1:
                    MessageUI.clearConsole();
                    viewAllMedicines();
                    MessageUI.pressEnterToContinue();
                    break;
                case 2:
                    addMedicine();
                    break;
                case 3:
                    dispenseMedicine();
                    break;
                case 4:
                    generateInventoryReport();
                    break;
                case 5:
                    generateDispensedReport();
                    break;
                case 0:
                    MessageUI.displayMainMenuMessage();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    public void addMedicine() {
        String newId = generateNextMedicineId();
        Medicine newMed = pharmacyUI.inputMedicineDetails(newId);
        medicineList.add(newMed);
        medicineDAO.saveToFile(medicineList);
        pharmacyUI.displayAddConfirmation(newMed);
    }

    private String generateNextMedicineId() {
        int max = 0;
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            String id = medicineList.getEntry(i).getMedId().substring(1); // skip 'M'
            int num = Integer.parseInt(id);
            if (num > max) {
                max = num;
            }
        }
        return String.format("M%03d", max + 1);
    }

    public String getAllMedicines() {
        String output = "";
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            output += medicineList.getEntry(i).toString() + "\n";
        }
        return output;
    }

    public void viewAllMedicines() {
        String list = getAllMedicines();
        pharmacyUI.displayMedicineInventory(list);
    }

    public void dispenseMedicine() {
        StringBuilder patientOutput = new StringBuilder();
        for (int i = 1; i <= patientList.getNumberOfEntries(); i++) {
            Patient p = patientList.getEntry(i);
            patientOutput.append(i).append(". ")
                .append(p.getId()).append(" - ")
                .append(p.getName()).append("\n");
        }
        pharmacyUI.displayMessage("Select a patient:\n" + patientOutput);

        int selectedIndex = pharmacyUI.inputPatientSelection();
        if (selectedIndex < 1 || selectedIndex > patientList.getNumberOfEntries()) {
            pharmacyUI.displayMessage("Invalid selection.");
            return;
        }

        Patient selectedPatient = patientList.getEntry(selectedIndex);

        ListInterface<Treatment> treatments = treatmentDAO.retrieveFromFile();
        String prescription = "";
        for (int i = treatments.getNumberOfEntries(); i >= 1; i--) {
            Treatment t = treatments.getEntry(i);
            if (t.getPatientId().equals(selectedPatient.getId())) {
                prescription = t.getPrescription();
                break;
            }
        }

        if (prescription.isEmpty()) {
            pharmacyUI.displayMessage("No prescription found for this patient.");
            return;
        }

        String[] meds = prescription.split(",");
        for (String med : meds) {
            String[] parts = med.split(":");
            String medId = parts[0].trim();
            int qty = Integer.parseInt(parts[1].trim());

            boolean found = false;
            for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
                Medicine m = medicineList.getEntry(i);
                if (m.getMedId().equals(medId)) {
                    found = true;
                    if (m.getStock() >= qty) {
                        m.setStock(m.getStock() - qty);
                        pharmacyUI.displayMessage("Dispensed " + qty + " units of " + m.getName());
                    } else {
                        pharmacyUI.displayMessage("Insufficient stock for " + m.getName());
                    }
                    break;
                }
            }

            if (!found) {
                pharmacyUI.displayMessage("Medicine " + medId + " not found in inventory.");
            }
        }

        medicineDAO.saveToFile(medicineList);
    }

    public void generateInventoryReport() {
        pharmacyUI.displayMessage("=== Inventory Report ===\n" + getAllMedicines());
    }

    public void generateDispensedReport() {
        // Placeholder: implement actual tracking/reporting if needed
        pharmacyUI.displayMessage("Dispensed Medicines Report feature not implemented.");
    }

    public static void main(String[] args) {
        MaintainPharmacy pharmacy = new MaintainPharmacy();
        pharmacy.runPharmacyMenu();
    }
}
