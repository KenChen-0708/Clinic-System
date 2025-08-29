package adt;

public interface QueueInterface<T> {

    void enqueue(T data);       // Add to rear (patient joins line)

    T dequeue();                // Remove from front (patient served)

    T peek();                   // View front without removing (who’s next)

    boolean isEmpty();          // Check if queue is empty

    int size();                 // Number of patients in line

    void clear();               // Remove all patients

    boolean contains(T data);   // Check if a patient is in queue

    boolean remove(T data);     // Remove specific patient from queue (regardless of position)

    void enqueueWithNumber(T data); // Add to rear with auto-generated queue number

    void enqueueWithExistingNumber(T data, String existingQueueNumber); // Add with existing queue number (for restoration)

    String getQueueNumber(T data);  // Get queue number for specific data

    int getPosition(T data);        // Get position of data in queue (1-based)

    String displayWithNumbers();    // Display all patients with queue numbers

    String toString();              // Display all patients

    // Safe traversal methods for non-destructive queue access
    T[] toArray();                  // Get queue elements as array in FIFO order

    String[] getQueueNumbers();     // Get queue numbers as array in FIFO order

    ListInterface<T> createSnapshot(); // Create read-only snapshot of queue contents
}
