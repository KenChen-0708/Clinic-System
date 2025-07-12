package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Treatment;

import java.io.*;

public class TreatmentDAO {
    private final String FILE_NAME = "treatments.dat";

    public void saveToFile(ListInterface<Treatment> treatmentList) {
        try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oo.writeObject(treatmentList);
        } catch (IOException e) {
            System.out.println("Error saving treatment data.");
        }
    }

    public ListInterface<Treatment> retrieveFromFile() {
        ListInterface<Treatment> list = new DoublyLinkedList<>();
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            list = (DoublyLinkedList<Treatment>) oi.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No treatment file found. Starting with an empty list.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading treatment data.");
        }
        return list;
    }
}
