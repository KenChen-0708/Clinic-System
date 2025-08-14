package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import java.io.*;
import java.util.Scanner;

public class MedicineNamesDAO {

    private final String FILE_NAME = "medicinesList.txt";

    // Retrieve the list of medicines from the file
    public ListInterface<String> retrieveFromFile() {
        ListInterface<String> medicineNames = new DoublyLinkedList<>();
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                String medicineName = scanner.nextLine().trim();
                if (!medicineName.isEmpty()) {
                    medicineNames.add(medicineName);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Medicines list file not found, starting with an empty list.");
        }
        return medicineNames;
    }

    // Save the list of medicines back to the file
    public void saveToFile(ListInterface<String> medicineNames) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 1; i <= medicineNames.getNumberOfEntries(); i++) {
                writer.println(medicineNames.getEntry(i));
            }
        } catch (IOException e) {
            System.out.println("Error saving medicines list to file.");
        }
    }

    // Add a new medicine to the list and save it to the file
    public void addMedicine(ListInterface<String> medicineNames, String medicineName) {
        medicineNames.add(medicineName);
        saveToFile(medicineNames);
    }

    // Edit an existing medicine name in the list
    public void editMedicine(ListInterface<String> medicineNames, int index, String newMedicineName) {
        medicineNames.replace(index, newMedicineName);
        saveToFile(medicineNames);
    }

    // Remove a medicine from the list
    public void removeMedicine(ListInterface<String> medicineNames, int index) {
        medicineNames.remove(index);
        saveToFile(medicineNames);
    }
}
