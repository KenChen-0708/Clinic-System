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
    @SuppressWarnings("unchecked")
    public void bubbleSort() {
        if (numberOfEntries <= 1) return;
        
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (((Comparable<T>) current.data).compareTo(current.next.data) > 0) {
                    swapData(current, current.next);
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }
    

    @SuppressWarnings("unchecked")
    public void selectionSort() {
        if (numberOfEntries <= 1) return;
        
        Node current = head;
        while (current != null) {
            Node min = current;
            Node temp = current.next;
            
            while (temp != null) {
                if (((Comparable<T>) temp.data).compareTo(min.data) < 0) {
                    min = temp;
                }
                temp = temp.next;
            }
            
            if (min != current) {
                swapData(current, min);
            }
            current = current.next;
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public void insertionSort() {
        if (numberOfEntries <= 1) return;
        
        Node current = head.next;
        while (current != null) {
            T key = current.data;
            Node prev = current.prev;
            
            // Find the correct position for the key
            while (prev != null && ((Comparable<T>) prev.data).compareTo(key) > 0) {
                prev.next.data = prev.data;
                prev = prev.prev;
            }
            
            // Insert the key at the correct position
            if (prev == null) {
                head.data = key;
            } else {
                prev.next.data = key;
            }
            
            current = current.next;
        }
    }
    

    public void sort(Comparator<T> comparator) {
        if (numberOfEntries <= 1) return;
        
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (comparator.compare(current.data, current.next.data) > 0) {
                    swapData(current, current.next);
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }
    
    private void swapData(Node node1, Node node2) {
        T temp = node1.data;
        node1.data = node2.data;
        node2.data = temp;
    }
    
    // SEARCHING METHODS
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
    

    @SuppressWarnings("unchecked")
    public int binarySearch(T target) {
        if (isEmpty()) return -1;
        
        int left = 1;
        int right = numberOfEntries;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Node midNode = getNodeAt(mid);
            
            if (midNode == null) return -1;
            
            int comparison = ((Comparable<T>) midNode.data).compareTo(target);
            
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
    

    public int[] searchAll(T target) {
        if (!contains(target)) {
            return new int[0]; // Empty array if not found
        }
        
        // First pass: count occurrences
        int count = 0;
        Node current = head;
        while (current != null) {
            if (current.data.equals(target)) {
                count++;
            }
            current = current.next;
        }
        
        // Second pass: collect positions
        int[] positions = new int[count];
        current = head;
        int position = 1;
        int index = 0;
        
        while (current != null) {
            if (current.data.equals(target)) {
                positions[index++] = position;
            }
            current = current.next;
            position++;
        }
        
        return positions;
    }
    

    @SuppressWarnings("unchecked")
    public boolean isSorted() {
        if (numberOfEntries <= 1) return true;
        
        Node current = head;
        while (current != null && current.next != null) {
            if (((Comparable<T>) current.data).compareTo(current.next.data) > 0) {
                return false;
            }
            current = current.next;
        }
        return true;
    }
    

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