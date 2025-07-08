package control;

import adt.DoublyLinkedList;
import dao.TreatmentDAO;
import entity.Treatment;

public class MaintainTreatment {
    private DoublyLinkedList<Treatment> treatmentList;
    private TreatmentDAO treatmentDAO;

    public MaintainTreatment() {
        treatmentDAO = new TreatmentDAO();
        treatmentList = treatmentDAO.loadAll();
    }

    public void addTreatment(Treatment treatment) {
        if (!treatmentList.contains(treatment)) {
            treatmentList.add(treatment);
            treatmentDAO.saveAll(treatmentList);
        } else {
            System.out.println("Treatment ID already exists.");
        }
    }

    public boolean removeTreatment(String treatmentId) {
        for (int i = 0; i < treatmentList.size(); i++) {
            if (treatmentList.get(i).getTreatmentId().equals(treatmentId)) {
                treatmentList.remove(treatmentList.get(i));
                treatmentDAO.saveAll(treatmentList);
                return true;
            }
        }
        return false;
    }

    public void displayAllTreatments() {
        for (int i = 0; i < treatmentList.size(); i++) {
            System.out.println(treatmentList.get(i));
        }
    }

    public void summaryTreatmentsPerPatient() {
        DoublyLinkedList<String> patientIds = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> counts = new DoublyLinkedList<>();

        for (int i = 0; i < treatmentList.size(); i++) {
            String pid = treatmentList.get(i).getPatientId();
            int index = -1;
            for (int j = 0; j < patientIds.size(); j++) {
                if (patientIds.get(j).equals(pid)) {
                    index = j;
                    break;
                }
            }
            if (index == -1) {
                patientIds.add(pid);
                counts.add(1);
            } else {
                counts.add(index, counts.get(index) + 1);
                counts.remove(index + 1);
            }
        }

        System.out.println("\n--- Total Treatments per Patient ---");
        for (int i = 0; i < patientIds.size(); i++) {
            System.out.println("Patient ID: " + patientIds.get(i) + " | Treatments: " + counts.get(i));
        }
    }
}
