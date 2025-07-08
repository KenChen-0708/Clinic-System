package dao;

import adt.DoublyLinkedList;
import entity.Patient;
import java.io.*;

public class PatientDAO {
    private final String FILE_NAME = "patients.txt";

    public DoublyLinkedList<Patient> loadAll() {
        DoublyLinkedList<Patient> list = new DoublyLinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Patient.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No patient file found. Starting with an empty list.");
        }
        return list;
    }

    public void saveAll(DoublyLinkedList<Patient> patientList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < patientList.size(); i++) {
                bw.write(patientList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patient data.");
        }
    }
}
