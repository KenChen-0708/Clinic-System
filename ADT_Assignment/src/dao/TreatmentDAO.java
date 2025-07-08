package dao;

import adt.DoublyLinkedList;
import entity.Treatment;
import java.io.*;

public class TreatmentDAO {
    private final String FILE_NAME = "treatments.txt";

    public DoublyLinkedList<Treatment> loadAll() {
        DoublyLinkedList<Treatment> list = new DoublyLinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Treatment.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No treatment file found. Starting with an empty list.");
        }
        return list;
    }

    public void saveAll(DoublyLinkedList<Treatment> treatmentList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < treatmentList.size(); i++) {
                bw.write(treatmentList.get(i).toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving treatment data.");
        }
    }
}
