/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.DoublyLinkedList;
import java.io.*;

public class SummaryReport {

    public void generate() {
        System.out.println("\n====== Clinic Summary Report ======");

        totalPatients();
        doctorsBySpecialization();
        treatmentsPerPatient();
        medicineStockReport();

        System.out.println("===================================");
    }

    private void totalPatients() {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("patients.txt"))) {
            while (br.readLine() != null) count++;
        } catch (IOException e) {
            System.out.println("Error reading patients.txt");
        }
        System.out.println("Total Registered Patients: " + count);
    }

    private void doctorsBySpecialization() {
        DoublyLinkedList<String> specs = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("doctors.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String spec = line.split(",")[2];
                int index = -1;
                for (int i = 0; i < specs.size(); i++) {
                    if (specs.get(i).equals(spec)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    specs.add(spec);
                    counts.add(1);
                } else {
                    counts.add(index, counts.get(index) + 1);
                    counts.remove(index + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading doctors.txt");
        }

        System.out.println("\nDoctors by Specialization:");
        for (int i = 0; i < specs.size(); i++) {
            System.out.println("- " + specs.get(i) + ": " + counts.get(i));
        }
    }

    private void treatmentsPerPatient() {
        DoublyLinkedList<String> patientIds = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("treatments.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String patientId = line.split(",")[1];
                int index = -1;
                for (int i = 0; i < patientIds.size(); i++) {
                    if (patientIds.get(i).equals(patientId)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    patientIds.add(patientId);
                    counts.add(1);
                } else {
                    counts.add(index, counts.get(index) + 1);
                    counts.remove(index + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading treatments.txt");
        }

        System.out.println("\nTreatments per Patient:");
        for (int i = 0; i < patientIds.size(); i++) {
            System.out.println("- Patient ID: " + patientIds.get(i) + " | Treatments: " + counts.get(i));
        }
    }

    private void medicineStockReport() {
        int total = 0;
        int lowStockCount = 0;
        final int LOW_THRESHOLD = 10;

        try (BufferedReader br = new BufferedReader(new FileReader("medicines.txt"))) {
            String line;
            System.out.println("\nMedicine Stock Summary:");
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[1];
                int stock = Integer.parseInt(parts[2]);
                total += stock;
                if (stock < LOW_THRESHOLD) {
                    lowStockCount++;
                    System.out.println("- LOW STOCK: " + name + " (" + stock + " units)");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading medicines.txt");
        }

        System.out.println("\nTotal Units of Medicines in Stock: " + total);
        System.out.println("Number of Medicines Below Threshold (" + LOW_THRESHOLD + "): " + lowStockCount);
    }
}
