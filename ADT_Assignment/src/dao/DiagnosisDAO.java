// Lee Siu Yee

package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Diagnosis;
import entity.Treatment;
import java.io.*;

public class DiagnosisDAO {
    private final String FILE_NAME = "diagnosis.dat";

    public void saveToFile(ListInterface<Diagnosis> diagnosisList) {
        try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oo.writeObject(diagnosisList);
            System.out.println("Diagnosis data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving treatment data: " + e.getMessage());
        }
    }
    
    public ListInterface<Diagnosis> retrieveFromFile() {
        File file = new File(FILE_NAME);
        
        // If file doesn't exist, return empty list without error message
        if (!file.exists()) {
            return new DoublyLinkedList<>();
        }
        
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file))) {
            return (DoublyLinkedList<Diagnosis>) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading Diagnosis data: " + e.getMessage());
            return new DoublyLinkedList<>();
        }
    }
}
