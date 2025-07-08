// Author: [Student Name]
package control;

import adt.DoublyLinkedList;
import dao.PatientDAO;
import entity.Patient;

public class MaintainPatient {
    private DoublyLinkedList<Patient> patientList;
    private PatientDAO patientDAO;

    public MaintainPatient() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.loadAll();
    }

    public void addPatient(Patient patient) {
        if (!patientList.contains(patient)) {
            patientList.add(patient);
            patientDAO.saveAll(patientList);
        } else {
            System.out.println("Patient already exists.");
        }
    }

    public boolean removePatient(String id) {
        for (int i = 0; i < patientList.size(); i++) {
            if (patientList.get(i).getId().equals(id)) {
                patientList.remove(patientList.get(i));
                patientDAO.saveAll(patientList);
                return true;
            }
        }
        return false;
    }

    public void displayAllPatients() {
        for (int i = 0; i < patientList.size(); i++) {
            System.out.println(patientList.get(i));
        }
    }
}
