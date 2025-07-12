package adt;

import java.io.Serializable;

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
