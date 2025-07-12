package control;

import adt.ListInterface;
import dao.PatientDAO;
import dao.DoctorDAO;
import dao.TreatmentDAO;
import dao.MedicineDAO;
import entity.Patient;
import entity.Doctor;
import entity.Treatment;
import entity.Medicine;

public class SummaryReport {

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    public void generate() {
        System.out.println("\n====== Clinic Summary Report ======\n");

        totalPatients();
        doctorsBySpecialization();
        treatmentsPerPatient();
        medicineStockReport();

        System.out.println("\n===================================");
    }

    private void totalPatients() {
        ListInterface<Patient> patients = patientDAO.retrieveFromFile();
        System.out.println("Total Registered Patients: " + patients.getNumberOfEntries());
    }

    private void doctorsBySpecialization() {
        ListInterface<Doctor> doctors = doctorDAO.retrieveFromFile();
        ListInterface<String> specs = new adt.DoublyLinkedList<>();
        ListInterface<Integer> counts = new adt.DoublyLinkedList<>();

        for (int i = 1; i <= doctors.getNumberOfEntries(); i++) {
            String spec = doctors.getEntry(i).getSpecialization();
            boolean found = false;
            for (int j = 1; j <= specs.getNumberOfEntries(); j++) {
                if (specs.getEntry(j).equals(spec)) {
                    counts.replace(j, counts.getEntry(j) + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                specs.add(spec);
                counts.add(1);
            }
        }

        System.out.println("\nDoctors by Specialization:");
        for (int i = 1; i <= specs.getNumberOfEntries(); i++) {
            System.out.println("- " + specs.getEntry(i) + ": " + counts.getEntry(i) + " doctor(s)");
        }
    }

    private void treatmentsPerPatient() {
        ListInterface<Treatment> treatments = treatmentDAO.retrieveFromFile();
        ListInterface<String> patientIds = new adt.DoublyLinkedList<>();
        ListInterface<Integer> counts = new adt.DoublyLinkedList<>();

        for (int i = 1; i <= treatments.getNumberOfEntries(); i++) {
            String pid = treatments.getEntry(i).getPatientId();
            boolean found = false;
            for (int j = 1; j <= patientIds.getNumberOfEntries(); j++) {
                if (patientIds.getEntry(j).equals(pid)) {
                    counts.replace(j, counts.getEntry(j) + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                patientIds.add(pid);
                counts.add(1);
            }
        }

        System.out.println("\nTreatments per Patient:");
        for (int i = 1; i <= patientIds.getNumberOfEntries(); i++) {
            System.out.println("- Patient ID: " + patientIds.getEntry(i) +
                    " | Treatments: " + counts.getEntry(i));
        }
    }

    private void medicineStockReport() {
        ListInterface<Medicine> medicines = medicineDAO.retrieveFromFile();
        final int LOW_THRESHOLD = 10;
        int totalStock = 0;
        int lowStockCount = 0;

        System.out.println("\nMedicine Stock Summary:");
        for (int i = 1; i <= medicines.getNumberOfEntries(); i++) {
            Medicine m = medicines.getEntry(i);
            int stock = m.getStock();
            totalStock += stock;
            if (stock < LOW_THRESHOLD) {
                lowStockCount++;
                System.out.println("- LOW STOCK: " + m.getName() + " (" + stock + " units)");
            }
        }

        System.out.println("\nTotal Units of Medicines in Stock: " + totalStock);
        System.out.println("Number of Medicines Below Threshold (" + LOW_THRESHOLD + "): " + lowStockCount);
    }
}
