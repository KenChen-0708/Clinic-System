package control;

import adt.DoublyLinkedList;
import dao.MedicineDAO;
import entity.Medicine;

public class MaintainPharmacy {
    private DoublyLinkedList<Medicine> medicineList;
    private MedicineDAO medicineDAO;

    public MaintainPharmacy() {
        medicineDAO = new MedicineDAO();
        medicineList = medicineDAO.loadAll();
    }

    public void addMedicine(Medicine med) {
        for (int i = 0; i < medicineList.size(); i++) {
            if (medicineList.get(i).getMedId().equals(med.getMedId())) {
                System.out.println("Medicine ID already exists.");
                return;
            }
        }
        medicineList.add(med);
        medicineDAO.saveAll(medicineList);
    }

    public void dispenseMedicine(String medId, int qty) {
        for (int i = 0; i < medicineList.size(); i++) {
            Medicine m = medicineList.get(i);
            if (m.getMedId().equals(medId)) {
                if (m.getStock() >= qty) {
                    m.setStock(m.getStock() - qty);
                    medicineDAO.saveAll(medicineList);
                    System.out.println("Dispensed " + qty + " units.");
                    return;
                } else {
                    System.out.println("Insufficient stock.");
                    return;
                }
            }
        }
        System.out.println("Medicine not found.");
    }

    public void viewAllMedicines() {
        for (int i = 0; i < medicineList.size(); i++) {
            System.out.println(medicineList.get(i));
        }
    }

    public void summaryLowStock(int threshold) {
        System.out.println("\n--- Medicines Below Stock Threshold (" + threshold + ") ---");
        boolean found = false;
        for (int i = 0; i < medicineList.size(); i++) {
            Medicine m = medicineList.get(i);
            if (m.getStock() < threshold) {
                System.out.println(m);
                found = true;
            }
        }
        if (!found) System.out.println("All medicines have sufficient stock.");
    }
}
