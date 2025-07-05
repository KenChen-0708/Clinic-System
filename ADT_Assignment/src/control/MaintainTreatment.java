/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.DoublyLinkedList;
import entity.Treatment;
import java.io.*;

public class MaintainTreatment {
    private DoublyLinkedList<Treatment> treatmentList = new DoublyLinkedList<>();

    public MaintainTreatment() {
        loadFromFile();
    }

    public void addTreatment(Treatment treatment) {
        if (!treatmentList.contains(treatment)) {
            treatmentList.add(treatment);
            saveToFile();
        } else {
            System.out.println("Treatment ID already exists.");
        }
    }

    public boolean removeTreatment(String treatmentId) {
        for (int i = 0; i < treatmentList.size(); i++) {
            if (treatmentList.get(i).getTreatmentId().equals(treatmentId)) {
                treatmentList.remove(treatmentList.get(i));
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public void displayAllTreatments() {
        for (int i = 0; i < treatmentList.size(); i++) {
            System.out.println(treatmentList.get(i));
        }
    }

    public void summaryTreatmentsPerPatient() {
        DoublyLinkedList<String> patientIds = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        for (int i = 0; i < treatmentList.size(); i++) {
            String pid = treatmentList.get(i).getPatientId();
            int index = -1;
            for (int j = 0; j < patientIds.size(); j++) {
                if (patientIds.get(j).equals(pid)) {
                    index = j;
                    break;
                }
            }
            if (index == -1) {
                patientIds.add(pid);
                counts.add(1);
            } else {
                counts.add(index, counts.get(index) + 1);
                counts.remove(index + 1);
            }
        }

        System.out.println("\n--- Total Treatments per Patient ---");
        for (int i = 0; i < patientIds.size(); i++) {
            System.out.println("Patient ID: " + patientIds.get(i) + " | Treatments: " + counts.get(i));
        }
    }

    private void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("treatments.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                treatmentList.add(Treatment.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No file found. Starting fresh.");
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("treatments.txt"))) {
            for (int i = 0; i < treatmentList.size(); i++) {
                bw.write(treatmentList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }
}
