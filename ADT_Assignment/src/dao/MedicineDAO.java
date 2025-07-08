package dao;

import adt.DoublyLinkedList;
import entity.Medicine;
import java.io.*;

public class MedicineDAO {
    private final String FILE_NAME = "medicines.txt";

    public DoublyLinkedList<Medicine> loadAll() {
        DoublyLinkedList<Medicine> list = new DoublyLinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Medicine.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No existing medicine file found. Starting with an empty list.");
        }
        return list;
    }

    public void saveAll(DoublyLinkedList<Medicine> medicineList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < medicineList.size(); i++) {
                bw.write(medicineList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving medicine data.");
        }
    }
}
