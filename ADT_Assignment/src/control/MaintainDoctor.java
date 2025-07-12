package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import boundary.DoctorUI;
import dao.DoctorDAO;
import entity.Doctor;
import utility.MessageUI;

public class MaintainDoctor {
    private final ListInterface<Doctor> doctorList;
    private final DoctorDAO doctorDAO;
    private final DoctorUI doctorUI;

    public MaintainDoctor() {
        doctorDAO = new DoctorDAO();
        doctorList = doctorDAO.retrieveFromFile();
        doctorUI = new DoctorUI(this);
    }

    public void addDoctor() {
        Doctor doctor = doctorUI.inputDoctorDetails();
        if (!doctorList.contains(doctor)) {
            doctorList.add(doctor);
            doctorDAO.saveToFile(doctorList);
            doctorUI.displayMessage("Doctor added successfully.");
        } else {
            doctorUI.displayMessage("Doctor already exists.");
        }
    }

    public void removeDoctor() {
        String id = doctorUI.inputDoctorId();
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            if (doctorList.getEntry(i).getId().equalsIgnoreCase(id)) {
                doctorList.remove(i);
                doctorDAO.saveToFile(doctorList);
                doctorUI.displayMessage("Doctor removed.");
                return;
            }
        }
        doctorUI.displayMessage("Doctor not found.");
    }

    public void displayAllDoctors() {
        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor d = doctorList.getEntry(i);
            output.append(i).append(". ")
                  .append(d.getId()).append(" - ")
                  .append(d.getName()).append(" | ")
                  .append("Specialization: ").append(d.getSpecialization()).append("\n");
        }
        doctorUI.displayDoctorList(output.toString());
    }

    public void searchDoctorById() {
        String id = doctorUI.inputDoctorId();
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor doc = doctorList.getEntry(i);
            if (doc.getId().equalsIgnoreCase(id)) {
                String msg = "\nDoctor Found:\n"
                           + "ID            : " + doc.getId() + "\n"
                           + "Name          : " + doc.getName() + "\n"
                           + "Specialization: " + doc.getSpecialization() + "\n";
                doctorUI.displayMessage(msg);
                return;
            }
        }
        doctorUI.displayMessage("Doctor not found.");
    }

    public void setDoctorSchedule() {
        String id = doctorUI.inputDoctorId();
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            if (doctorList.getEntry(i).getId().equalsIgnoreCase(id)) {
                // Just a placeholder for actual schedule logic
                doctorUI.displayMessage("Schedule set successfully for Doctor ID: " + id);
                return;
            }
        }
        doctorUI.displayMessage("Doctor not found.");
    }

    public void runDoctorMenu() {
        doctorUI.run();
    }
}
