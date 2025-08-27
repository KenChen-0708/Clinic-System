package adt;

public class LinkedQueue<T> implements QueueInterface<T> {

    // Node class inside the ADT
    private static class Node<T> {
        T data;
        String queueNumber;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.queueNumber = null;
            this.next = null;
        }

        Node(T data, String queueNumber) {
            this.data = data;
            this.queueNumber = queueNumber;
            this.next = null;
        }
    }

    private Node<T> front; // first patient
    private Node<T> rear;  // last patient
    private int size;
    private int nextQueueNumber; // Counter for queue numbers

    public LinkedQueue() {
        front = null;
        rear = null;
        size = 0;
        nextQueueNumber = 1; // Start queue numbers from 0001
    }

    @Override
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            front = newNode;
        } else {
            rear.next = newNode;
        }
        rear = newNode;
        size++;
    }

    @Override
    public void enqueueWithNumber(T data) {
        String queueNum = String.format("%04d", nextQueueNumber++);
        Node<T> newNode = new Node<>(data, queueNum);
        if (isEmpty()) {
            front = newNode;
        } else {
            rear.next = newNode;
        }
        rear = newNode;
        size++;
    }

    /**
     * Enqueue with existing queue number - for restoration purposes
     * Does NOT increment the counter - preserves original queue numbers
     * @param data The data to enqueue
     * @param existingQueueNumber The existing queue number to preserve
     */
    public void enqueueWithExistingNumber(T data, String existingQueueNumber) {
        Node<T> newNode = new Node<>(data, existingQueueNumber);
        if (isEmpty()) {
            front = newNode;
        } else {
            rear.next = newNode;
        }
        rear = newNode;
        size++;
    }

    @Override
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty, no patient to serve.");
        }
        T removedData = front.data;
        front = front.next;
        if (front == null) { // queue became empty
            rear = null;
        }
        size--;
        return removedData;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty, no patient to view.");
        }
        return front.data;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    @Override
    public boolean contains(T anEntry) {
        Node<T> current = front;
        while (current != null) {
            if (current.data.equals(anEntry)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public String getQueueNumber(T data) {
        Node<T> current = front;
        while (current != null) {
            if (current.data.equals(data)) {
                return current.queueNumber;
            }
            current = current.next;
        }
        return null; // Data not found
    }

    @Override
    public int getPosition(T data) {
        Node<T> current = front;
        int position = 1;
        while (current != null) {
            if (current.data.equals(data)) {
                return position;
            }
            current = current.next;
            position++;
        }
        return -1; // Data not found
    }

    @Override
    public String displayWithNumbers() {
        if (isEmpty()) {
            return "Queue is empty";
        }

        StringBuilder sb = new StringBuilder();
        Node<T> current = front;
        int position = 1;

        while (current != null) {
            if (current.queueNumber != null) {
                sb.append("Queue #").append(current.queueNumber)
                  .append(" (Position ").append(position).append("): ")
                  .append(current.data);
            } else {
                sb.append("Position ").append(position).append(": ")
                  .append(current.data);
            }

            if (current.next != null) {
                sb.append("\n");
            }
            current = current.next;
            position++;
        }

        return sb.toString();
    }

    /**
     * Get queue data as array for safe traversal without modifying queue
     * @return array of queue elements in FIFO order
     */
    public T[] toArray() {
        if (isEmpty()) {
            return (T[]) new Object[0];
        }

        T[] array = (T[]) new Object[size];
        Node<T> current = front;
        int index = 0;

        while (current != null) {
            array[index] = current.data;
            current = current.next;
            index++;
        }

        return array;
    }

    /**
     * Get queue numbers as array corresponding to queue elements
     * @return array of queue numbers in FIFO order
     */
    public String[] getQueueNumbers() {
        if (isEmpty()) {
            return new String[0];
        }

        String[] numbers = new String[size];
        Node<T> current = front;
        int index = 0;

        while (current != null) {
            numbers[index] = current.queueNumber;
            current = current.next;
            index++;
        }

        return numbers;
    }

    /**
     * Remove a specific element from the queue by value
     * More efficient than rebuilding the entire queue
     * @param value The element to remove
     * @return true if element was found and removed, false otherwise
     * Time Complexity: O(n) in worst case, but no queue rebuilding
     */
    public boolean removeByValue(T value) {
        if (isEmpty() || value == null) {
            return false;
        }

        // Special case: removing the front element
        if (front.data != null && front.data.equals(value)) {
            front = front.next;
            if (front == null) { // queue became empty
                rear = null;
            }
            size--;
            return true;
        }

        // Search for the element to remove
        Node<T> current = front;
        while (current.next != null) {
            if (current.next.data != null && current.next.data.equals(value)) {
                // Found the element to remove
                Node<T> nodeToRemove = current.next;
                current.next = nodeToRemove.next;

                // Update rear if we're removing the last element
                if (nodeToRemove == rear) {
                    rear = current;
                }

                size--;
                return true;
            }
            current = current.next;
        }

        return false; // Element not found
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
