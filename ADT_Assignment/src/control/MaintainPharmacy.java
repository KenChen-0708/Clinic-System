/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.DoublyLinkedList;
import entity.Medicine;
import java.io.*;

public class MaintainPharmacy {
    private DoublyLinkedList<Medicine> medicineList = new DoublyLinkedList<>();

    public MaintainPharmacy() {
        loadFromFile();
    }

    public void addMedicine(Medicine med) {
        for (int i = 0; i < medicineList.size(); i++) {
            if (medicineList.get(i).getMedId().equals(med.getMedId())) {
                System.out.println("Medicine ID already exists.");
                return;
            }
        }
        medicineList.add(med);
        saveToFile();
    }

    public void dispenseMedicine(String medId, int qty) {
        for (int i = 0; i < medicineList.size(); i++) {
            Medicine m = medicineList.get(i);
            if (m.getMedId().equals(medId)) {
                if (m.getStock() >= qty) {
                    m.setStock(m.getStock() - qty);
                    saveToFile();
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

    private void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("medicines.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                medicineList.add(Medicine.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No file found. Starting fresh.");
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("medicines.txt"))) {
            for (int i = 0; i < medicineList.size(); i++) {
                bw.write(medicineList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }
}
