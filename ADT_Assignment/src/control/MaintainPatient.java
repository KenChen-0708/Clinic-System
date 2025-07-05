// Author: [Student Name]
package control;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import adt.DoublyLinkedList;
import java.io.*;
import entity.Patient;

public class MaintainPatient {
    private DoublyLinkedList<Patient> patientList = new DoublyLinkedList<>();

    public MaintainPatient() {
        loadFromFile();
    }

    public void addPatient(Patient patient) {
        if (!patientList.contains(patient)) {
            patientList.add(patient);
            saveToFile();
        } else {
            System.out.println("Patient already exists.");
        }
    }

    public boolean removePatient(String id) {
        for (int i = 0; i < patientList.size(); i++) {
            if (patientList.get(i).getId().equals(id)) {
                patientList.remove(patientList.get(i));
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public void displayAllPatients() {
        for (int i = 0; i < patientList.size(); i++) {
            System.out.println(patientList.get(i));
        }
    }

    public void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("patients.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                patientList.add(Patient.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No file found. Starting fresh.");
        }
    }

    public void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("patients.txt"))) {
            for (int i = 0; i < patientList.size(); i++) {
                bw.write(patientList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }
}
