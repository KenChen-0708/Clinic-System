// Ronaldo Loh Yu Fei

package entity;

/** Simple snapshot of patient queue status */
public class QueueStatusData {
    private final int totalWaiting;
    private final Patient nextPatient;
    private final String nextQueueNumber;

    public QueueStatusData(int totalWaiting, Patient nextPatient, String nextQueueNumber) {
        this.totalWaiting = totalWaiting;
        this.nextPatient = nextPatient;
        this.nextQueueNumber = nextQueueNumber;
    }

    public int getTotalWaiting() { return totalWaiting; }
    public Patient getNextPatient() { return nextPatient; }
    public String getNextQueueNumber() { return nextQueueNumber; }
    public boolean isEmpty() { return totalWaiting == 0; }
}


