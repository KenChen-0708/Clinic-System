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
            System.out.println("Treatment data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving treatment data: " + e.getMessage());
        }
    }

    public ListInterface<Treatment> retrieveFromFile() {
        File file = new File(FILE_NAME);
        
        // If file doesn't exist, return empty list without error message
        if (!file.exists()) {
            return new DoublyLinkedList<>();
        }
        
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file))) {
            return (DoublyLinkedList<Treatment>) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading treatment data: " + e.getMessage());
            return new DoublyLinkedList<>();
        }
    }
}