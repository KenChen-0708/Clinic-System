/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.DoublyLinkedList;
import entity.Doctor;
import java.io.*;

public class MaintainDoctor {
    private DoublyLinkedList<Doctor> doctorList = new DoublyLinkedList<>();

    public MaintainDoctor() {
        loadFromFile();
    }

    public void addDoctor(Doctor doctor) {
        if (!doctorList.contains(doctor)) {
            doctorList.add(doctor);
            saveToFile();
        } else {
            System.out.println("Doctor already exists.");
        }
    }

    public boolean removeDoctor(String id) {
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getId().equals(id)) {
                doctorList.remove(doctorList.get(i));
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public void displayAllDoctors() {
        for (int i = 0; i < doctorList.size(); i++) {
            System.out.println(doctorList.get(i));
        }
    }

    public void summaryBySpecialization() {
        System.out.println("\n--- Doctors by Specialization ---");
        DoublyLinkedList<String> specializations = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        for (int i = 0; i < doctorList.size(); i++) {
            String spec = doctorList.get(i).getSpecialization();
            int index = -1;
            for (int j = 0; j < specializations.size(); j++) {
                if (specializations.get(j).equals(spec)) {
                    index = j;
                    break;
                }
            }
            if (index == -1) {
                specializations.add(spec);
                counts.add(1);
            } else {
                counts.remove(index + 1); // overwrite count
                counts.remove(index + 1); // remove old one (shifted due to add)
            }
        }

        for (int i = 0; i < specializations.size(); i++) {
            System.out.println(specializations.get(i) + ": " + counts.get(i) + " doctor(s)");
        }
    }

    private void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("doctors.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                doctorList.add(Doctor.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No file found. Starting fresh.");
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("doctors.txt"))) {
            for (int i = 0; i < doctorList.size(); i++) {
                bw.write(doctorList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }
}
