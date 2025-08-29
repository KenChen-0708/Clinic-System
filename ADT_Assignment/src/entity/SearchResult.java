// Ronaldo Loh Yu Fei

package entity;

/** Result of searching for a patient in the queue */
public class SearchResult {
    private final boolean found;
    private final Patient patient;
    private final String queueNumber;
    private final int queuePosition;

    public SearchResult(boolean found, Patient patient, String queueNumber, int queuePosition) {
        this.found = found;
        this.patient = patient;
        this.queueNumber = queueNumber;
        this.queuePosition = queuePosition;
    }

    public boolean isFound() { return found; }
    public Patient getPatient() { return patient; }
    public String getQueueNumber() { return queueNumber; }
    public int getQueuePosition() { return queuePosition; }
}


