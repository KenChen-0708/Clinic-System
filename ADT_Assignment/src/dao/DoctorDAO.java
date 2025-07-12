package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Doctor;
import java.io.*;

public class DoctorDAO {
    private final String FILE_NAME = "doctors.dat";

    public void saveToFile(ListInterface<Doctor> doctorList) {
        try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oo.writeObject(doctorList);
        } catch (IOException e) {
            System.out.println("Error saving doctors.");
        }
    }

    public ListInterface<Doctor> retrieveFromFile() {
        ListInterface<Doctor> list = new DoublyLinkedList<>();
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            list = (DoublyLinkedList<Doctor>) oi.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Doctor file not found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading doctors.");
        }
        return list;
    }
}
