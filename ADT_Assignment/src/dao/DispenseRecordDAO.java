package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.DispenseRecord;
import java.io.*;

public class DispenseRecordDAO {
    private static final String FILE_NAME = "dispensed.dat";

    public ListInterface<DispenseRecord> retrieveFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return new DoublyLinkedList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            @SuppressWarnings("unchecked")
            ListInterface<DispenseRecord> list = (ListInterface<DispenseRecord>) ois.readObject();
            return list;
        } catch (Exception e) {
            return new DoublyLinkedList<>();
        }
    }

    public void saveToFile(ListInterface<DispenseRecord> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(list);
        } catch (IOException e) {
            // swallow or log
        }
    }
}
