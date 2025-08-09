package adt;

public interface QueueInterface<T> {

    void enqueue(T data);       // Add to rear (patient joins line)

    T dequeue();                // Remove from front (patient served)

    T peek();                   // View front without removing (who’s next)

    boolean isEmpty();          // Check if queue is empty

    int size();                 // Number of patients in line

    void clear();               // Remove all patients

    boolean contains(T data);   // Check if a patient is in queue

    void enqueueWithNumber(T data); // Add to rear with auto-generated queue number

    String getQueueNumber(T data);  // Get queue number for specific data

    int getPosition(T data);        // Get position of data in queue (1-based)

    String displayWithNumbers();    // Display all patients with queue numbers

    String toString();              // Display all patients
}
