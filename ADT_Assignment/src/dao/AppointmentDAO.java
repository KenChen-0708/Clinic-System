package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Appointment;
import java.io.*;

/**
 * Data Access Object for Appointment entities
 * Handles file I/O operations for appointment data
 */
public class AppointmentDAO {
    private static final String FILENAME = "appointment.dat";
    
    /**
     * Save appointments to file
     * @param appointmentList List of appointments to save
     */
    public void saveToFile(ListInterface<Appointment> appointmentList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            // Convert to array for serialization
            Appointment[] appointments = new Appointment[appointmentList.getNumberOfEntries()];
            for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
                appointments[i-1] = appointmentList.getEntry(i);
            }
            oos.writeObject(appointments);
        } catch (IOException e) {
            System.err.println("Error saving appointments to file: " + e.getMessage());
        }
    }
    
    /**
     * Retrieve appointments from file
     * @return List of appointments from file, or empty list if file doesn't exist
     */
    public ListInterface<Appointment> retrieveFromFile() {
        ListInterface<Appointment> appointmentList = new DoublyLinkedList<>();
        
        File file = new File(FILENAME);
        if (!file.exists()) {
            return appointmentList; // Return empty list if file doesn't exist
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILENAME))) {
            Appointment[] appointments = (Appointment[]) ois.readObject();
            for (Appointment appointment : appointments) {
                if (appointment != null) {
                    appointmentList.add(appointment);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading appointments from file: " + e.getMessage());
        }
        
        return appointmentList;
    }
    
    /**
     * Check if appointment file exists
     * @return true if file exists, false otherwise
     */
    public boolean fileExists() {
        return new File(FILENAME).exists();
    }
    
    /**
     * Delete appointment file
     * @return true if file was deleted successfully, false otherwise
     */
    public boolean deleteFile() {
        File file = new File(FILENAME);
        return file.exists() && file.delete();
    }
}
