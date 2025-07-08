package control;

import adt.DoublyLinkedList;
import dao.DoctorDAO;
import entity.Doctor;

public class MaintainDoctor {
    private DoublyLinkedList<Doctor> doctorList;
    private DoctorDAO doctorDAO;

    public MaintainDoctor() {
        doctorDAO = new DoctorDAO();
        doctorList = doctorDAO.loadAll();
    }

    public void addDoctor(Doctor doctor) {
        if (!doctorList.contains(doctor)) {
            doctorList.add(doctor);
            doctorDAO.saveAll(doctorList);
        } else {
            System.out.println("Doctor already exists.");
        }
    }

    public boolean removeDoctor(String id) {
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getId().equals(id)) {
                doctorList.remove(doctorList.get(i));
                doctorDAO.saveAll(doctorList);
                return true;
            }
        }
        return false;
    }

    public void displayAllDoctors() {
        for (int i = 0; i < doctorList.size(); i++) {
            System.out.println(doctorList.get(i));
        }
    }

    public void summaryBySpecialization() {
        System.out.println("\n--- Doctors by Specialization ---");
        DoublyLinkedList<String> specializations = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        for (int i = 0; i < doctorList.size(); i++) {
            String spec = doctorList.get(i).getSpecialization();
            int index = -1;
            for (int j = 0; j < specializations.size(); j++) {
                if (specializations.get(j).equals(spec)) {
                    index = j;
                    break;
                }
            }
            if (index == -1) {
                specializations.add(spec);
                counts.add(1);
            } else {
                counts.remove(index + 1);
            }
        }

        for (int i = 0; i < specializations.size(); i++) {
            System.out.println(specializations.get(i) + ": " + counts.get(i) + " doctor(s)");
        }
    }
}
