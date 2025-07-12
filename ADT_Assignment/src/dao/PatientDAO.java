package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Patient;
import java.io.*;

public class PatientDAO {
    private final String FILE_NAME = "patients.dat";

    public void saveToFile(ListInterface<Patient> patientList) {
        try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oo.writeObject(patientList);
        } catch (IOException e) {
            System.out.println("Error saving patient data.");
        }
    }

    public ListInterface<Patient> retrieveFromFile() {
        ListInterface<Patient> list = new DoublyLinkedList<>();
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            list = (DoublyLinkedList<Patient>) oi.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No patient file found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading patient data.");
        }
        return list;
    }
}
