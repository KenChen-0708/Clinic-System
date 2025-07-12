package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Medicine;
import java.io.*;

public class MedicineDAO {
    private final String FILE_NAME = "medicines.dat";

    public void saveToFile(ListInterface<Medicine> medicineList) {
        File file = new File(FILE_NAME);
        try {
            ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(file));
            ooStream.writeObject(medicineList);
            ooStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\nFile not found.");
        } catch (IOException ex) {
            System.out.println("\nCannot save to file.");
        }
    }

    public ListInterface<Medicine> retrieveFromFile() {
        File file = new File(FILE_NAME);
        ListInterface<Medicine> medicineList = new DoublyLinkedList<>();

        try {
            ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file));
            medicineList = (DoublyLinkedList<Medicine>) oiStream.readObject();
            oiStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\nNo existing file found. Starting with an empty list.");
        } catch (IOException ex) {
            System.out.println("\nCannot read from file.");
        } catch (ClassNotFoundException ex) {
            System.out.println("\nClass not found.");
        } finally {
            return medicineList;
        }
    }
}
