package adt;
import java.io.Serializable;
import java.util.Comparator;

public class DoublyLinkedList<T> implements ListInterface<T>, Serializable {
    private class Node implements Serializable {
        T data;
        Node next, prev;
        Node(T data) {
            this.data = data;
        }
    }
    
    private Node head, tail;
    private int numberOfEntries;
    
    public DoublyLinkedList() {
        head = tail = null;
        numberOfEntries = 0;
    }
    
    @Override
    public boolean add(T newEntry) {
        return add(numberOfEntries + 1, newEntry);
    }
    
    @Override
    public boolean add(int newPosition, T newEntry) {
        if (newPosition < 1 || newPosition > numberOfEntries + 1) {
            return false;
        }
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            head = tail = newNode;
        } else if (newPosition == 1) {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        } else if (newPosition == numberOfEntries + 1) {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        } else {
            Node current = getNodeAt(newPosition);
            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
        }
        numberOfEntries++;
        return true;
    }
    
    @Override
    public T remove(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }
        Node toRemove = getNodeAt(givenPosition);
        T result = toRemove.data;
        if (toRemove == head) {
            head = head.next;
            if (head != null) head.prev = null;
        } else {
            toRemove.prev.next = toRemove.next;
        }
        if (toRemove == tail) {
            tail = tail.prev;
            if (tail != null) tail.next = null;
        } else {
            toRemove.next.prev = toRemove.prev;
        }
        numberOfEntries--;
        if (numberOfEntries == 0) head = tail = null;
        return result;
    }
    
    @Override
    public void clear() {
        head = tail = null;
        numberOfEntries = 0;
    }
    
    @Override
    public boolean replace(int givenPosition, T newEntry) {
        Node current = getNodeAt(givenPosition);
        if (current != null) {
            current.data = newEntry;
            return true;
        }
        return false;
    }
    
    @Override
    public T getEntry(int givenPosition) {
        Node current = getNodeAt(givenPosition);
        return (current != null) ? current.data : null;
    }
    
    @Override
    public boolean contains(T anEntry) {
        Node current = head;
        while (current != null) {
            if (current.data.equals(anEntry)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
    
    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }
    
    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }
    
    @Override
    public boolean isFull() {
        return false; // Linked list is dynamic
    }
    
    private Node getNodeAt(int position) {
        if (position < 1 || position > numberOfEntries) return null;
        Node current = head;
        for (int i = 1; i < position; i++) {
            current = current.next;
        }
        return current;
    }
    
    // SORTING METHODS
    @Override
    public void mergeSort(Comparator<T> comparator) {
        if (numberOfEntries <= 1) return;
        
        // Convert to array for efficient merge sort
        Object[] array = new Object[numberOfEntries];
        Node current = head;
        for (int i = 0; i < numberOfEntries; i++) {
            array[i] = current.data;
            current = current.next;
        }
        
        // Perform merge sort on array
        mergeSortArray(array, 0, numberOfEntries - 1, comparator);
        
        // Copy sorted data back to linked list
        current = head;
        for (int i = 0; i < numberOfEntries; i++) {
            current.data = (T) array[i];
            current = current.next;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void mergeSortArray(Object[] array, int left, int right, Comparator<T> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            
            // Sort first and second halves
            mergeSortArray(array, left, mid, comparator);
            mergeSortArray(array, mid + 1, right, comparator);
            
            // Merge the sorted halves
            merge(array, left, mid, right, comparator);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void merge(Object[] array, int left, int mid, int right, Comparator<T> comparator) {
        // Create temporary arrays
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        Object[] leftArray = new Object[n1];
        Object[] rightArray = new Object[n2];
        
        // Copy data to temporary arrays
        for (int i = 0; i < n1; i++) {
            leftArray[i] = array[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = array[mid + 1 + j];
        }
        
        // Merge the temporary arrays back into array[left..right]
        int i = 0, j = 0;
        int k = left;
        
        while (i < n1 && j < n2) {
            if (comparator.compare((T) leftArray[i], (T) rightArray[j]) <= 0) {
                array[k] = leftArray[i];
                i++;
            } else {
                array[k] = rightArray[j];
                j++;
            }
            k++;
        }
        
        // Copy remaining elements of leftArray[], if any
        while (i < n1) {
            array[k] = leftArray[i];
            i++;
            k++;
        }
        
        // Copy remaining elements of rightArray[], if any
        while (j < n2) {
            array[k] = rightArray[j];
            j++;
            k++;
        }
    }
    
    // SEARCHING METHODS
    @Override
    public int linearSearch(T target) {
        if (!contains(target)) {
            return -1; // Not found
        }
        
        Node current = head;
        int position = 1;
        while (current != null) {
            if (current.data.equals(target)) {
                return position;
            }
            current = current.next;
            position++;
        }
        return -1; // Should not reach here if contains() returned true
    }
    

    @Override
    public int binarySearch(T target, Comparator<T> comparator) {
        if (isEmpty()) return -1;
        
        int left = 1;
        int right = numberOfEntries;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Node midNode = getNodeAt(mid);
            
            if (midNode == null) return -1;
            
            int comparison = comparator.compare(midNode.data, target);
            
            if (comparison == 0) {
                return mid; // Found
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1; // Not found
    }

    @Override
    public boolean isSorted(Comparator<T> comparator) {
        if (numberOfEntries <= 1) return true;
        
        Node current = head;
        while (current != null && current.next != null) {
            if (comparator.compare(current.data, current.next.data) > 0) {
                return false;
            }
            current = current.next;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node current = head;
        while (current != null) {
            sb.append(current.data).append("\n");
            current = current.next;
        }
        return sb.toString();
    }
}