package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Treatment;
import java.io.*;

public class TreatmentDAO {
    private final String FILE_NAME = "treatments.dat";
    private final String BACKUP_FILE_NAME = "treatments_backup.dat";

    public void saveToFile(ListInterface<Treatment> treatmentList) {
        try {
            // Create backup of existing file
            File existingFile = new File(FILE_NAME);
            if (existingFile.exists()) {
                File backupFile = new File(BACKUP_FILE_NAME);
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                existingFile.renameTo(backupFile);
            }
            
            // Save new data
            try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
                oo.writeObject(treatmentList);
                System.out.println("Treatment data saved successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error saving treatment data: " + e.getMessage());
        }
    }

    public ListInterface<Treatment> retrieveFromFile() {
        File file = new File(FILE_NAME);
        
        // If file doesn't exist, return empty list
        if (!file.exists()) {
            return new DoublyLinkedList<>();
        }
        
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file))) {
            return (DoublyLinkedList<Treatment>) oi.readObject();
        } catch (InvalidClassException e) {
            System.out.println("Warning: Treatment data format has changed. Creating new data file.");
            System.out.println("Previous data has been backed up as " + BACKUP_FILE_NAME);
            
            // Rename old file as backup
            File backupFile = new File(BACKUP_FILE_NAME);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            file.renameTo(backupFile);
            
            return new DoublyLinkedList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading treatment data: " + e.getMessage());
            return new DoublyLinkedList<>();
        }
    }
    
    public void resetDataFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            File backupFile = new File(BACKUP_FILE_NAME);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            file.renameTo(backupFile);
            System.out.println("Treatment data file reset. Previous data backed up.");
        }
    }
}