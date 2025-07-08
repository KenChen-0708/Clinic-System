package dao;

import adt.DoublyLinkedList;
import entity.Doctor;
import java.io.*;

public class DoctorDAO {
    private final String FILE_NAME = "doctors.txt";

    public DoublyLinkedList<Doctor> loadAll() {
        DoublyLinkedList<Doctor> list = new DoublyLinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Doctor.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No doctor file found. Starting with an empty list.");
        }
        return list;
    }

    public void saveAll(DoublyLinkedList<Doctor> doctorList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < doctorList.size(); i++) {
                bw.write(doctorList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving doctor data.");
        }
    }
}
