package adt;

import java.io.Serializable;
import java.util.Comparator;

public class DoublyLinkedList<T> implements ListInterface<T>, Serializable {

    // ================================
    // Inner Node class - stores data and links to previous/next nodes
    // ================================
    private class Node implements Serializable {

        T data;        // Element stored in the node
        Node next;     // Pointer to the next node
        Node prev;     // Pointer to the previous node

        Node(T data) {
            this.data = data;
        }
    }

    // ================================
    // Instance variables
    // ================================
    private Node head;             // First node in the list
    private Node tail;             // Last node in the list
    private int numberOfEntries;   // Count of nodes in the list

    // ================================
    // Constructor
    // ================================
    public DoublyLinkedList() {
        head = tail = null;
        numberOfEntries = 0;
    }

    // ================================
    // Add methods
    // ================================
    // Add to the end of the list
    @Override
    public boolean add(T newEntry) {
        return add(numberOfEntries + 1, newEntry);
    }

    // Add at a specific position (1-based index)
    @Override
    public boolean add(int newPosition, T newEntry) {
        // Check for valid position
        if (newPosition < 1 || newPosition > numberOfEntries + 1) {
            return false;
        }

        Node newNode = new Node(newEntry);

        if (isEmpty()) {
            // First element in the list
            head = tail = newNode;
        } else if (newPosition == 1) {
            // Insert at the start
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        } else if (newPosition == numberOfEntries + 1) {
            // Insert at the end
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        } else {
            // Insert in the middle
            Node current = getNodeAt(newPosition);
            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
        }

        numberOfEntries++;
        return true;
    }

    // Helper to unlink/remove a node from the chain
    private T unlink(Node x) {
        T val = x.data;
        Node p = x.prev, n = x.next;

        // Update previous node's next pointer
        if (p == null) {
            head = n;
        } else {
            p.next = n;
        }

        // Update next node's prev pointer
        if (n == null) {
            tail = p;
        } else {
            n.prev = p;
        }

        // Disconnect the removed node
        x.next = x.prev = null;
        numberOfEntries--;
        return val;
    }

    // Remove element at a given position
    @Override
    public T remove(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }
        return unlink(getNodeAt(givenPosition));
    }

    // Clear the entire list
    @Override
    public void clear() {
        head = tail = null;
        numberOfEntries = 0;
    }

    // Replace the data at a specific position
    @Override
    public boolean replace(int givenPosition, T newEntry) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return false;
        }
        getNodeAt(givenPosition).data = newEntry;
        return true;
    }

    // Get the data at a specific position
    @Override
    public T getEntry(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }
        return getNodeAt(givenPosition).data;
    }

    // ================================
    // Searching methods
    // ================================
    // Check if a value exists in the list (linearSearch)
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

    // Get the number of elements in the list
    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    // Check if the list is empty
    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    // Linked list can always grow
    @Override
    public boolean isFull() {
        return false;
    }

    // ================================
    // Node access helper
    // ================================
    private Node getNodeAt(int position) {
        if (position < 1 || position > numberOfEntries) {
            return null;
        }

        // Optimize: start from head or tail depending on which is closer
        if (position <= (numberOfEntries / 2)) {
            Node cur = head;
            for (int i = 1; i < position; i++) {
                cur = cur.next;
            }
            return cur;
        } else {
            Node cur = tail;
            for (int i = numberOfEntries; i > position; i--) {
                cur = cur.prev;
            }
            return cur;
        }
    }

    // ================================
    // Merge Sort (In-place, stable, O(1) extra space)
    // ================================
    @Override
    public void mergeSort(Comparator<T> comparator) {
        if (numberOfEntries <= 1) {
            return;
        }

        head = mergeSortNodes(head, comparator);

        // Rebuild prev pointers and tail after sorting
        Node cur = head, prev = null;
        while (cur != null) {
            cur.prev = prev;
            prev = cur;
            cur = cur.next;
        }
        tail = prev;
    }

    // Recursive merge sort on linked list
    private Node mergeSortNodes(Node h, Comparator<T> comp) {
        if (h == null || h.next == null) {
            return h; // Base case: single node
        }

        Node mid = split(h);         // Split into two halves
        Node right = mid.next;
        mid.next = null;             // End left half
        if (right != null) {
            right.prev = null;
        }

        Node leftSorted = mergeSortNodes(h, comp);
        Node rightSorted = mergeSortNodes(right, comp);
        return mergeLists(leftSorted, rightSorted, comp);
    }

    // Find middle node (end of first half)
    private Node split(Node h) {
        Node slow = h, fast = h;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    // Merge two sorted lists into one
    private Node mergeLists(Node a, Node b, Comparator<T> comp) {
        Node dummy = new Node(null); // Dummy node to build result
        Node tailM = dummy;

        while (a != null && b != null) {
            if (comp.compare(a.data, b.data) <= 0) {
                tailM.next = a;
                a.prev = tailM;
                a = a.next;
            } else {
                tailM.next = b;
                b.prev = tailM;
                b = b.next;
            }
            tailM = tailM.next;
        }

        // Attach remaining nodes
        Node rest = (a != null) ? a : b;
        if (rest != null) {
            tailM.next = rest;
            rest.prev = tailM;
        }

        // Return head of merged list
        Node newHead = dummy.next;
        if (newHead != null) {
            newHead.prev = null;
        }
        return newHead;
    }

    // Check if the list is sorted according to a comparator
    @Override
    public boolean isSorted(Comparator<T> comparator) {
        if (numberOfEntries <= 1) {
            return true;
        }

        Node current = head;
        while (current != null && current.next != null) {
            if (comparator.compare(current.data, current.next.data) > 0) {
                return false;
            }
            current = current.next;
        }
        return true;
    }

    // ================================
    // Reverse operation
    // ================================
    /**
     * Reverses the order of elements in the doubly linked list
     *
     * Algorithm:
     * 1. Store original head and tail references for O(1) swapping
     * 2. Traverse the list once, swapping next/prev pointers for each node
     * 3. Swap head and tail references to complete the reversal
     *
     * Time Complexity: O(n) - single pass through the list
     * Space Complexity: O(1) - only uses a few temporary variables
     *
     * This implementation fixes two key issues:
     * - Efficiency: No unnecessary second traversal to find new tail
     * - Logic clarity: Direct head/tail swapping instead of relying on loop side effects
     */
    @Override
    public void reverse() {
        if (numberOfEntries <= 1) {
            return; // Nothing to reverse for empty list or single element
        }

        // Store original head and tail for efficient O(1) swapping at the end
        Node originalHead = head;
        Node originalTail = tail;

        Node current = head;

        // Single pass: swap next and prev pointers for all nodes
        while (current != null) {
            Node nextNode = current.next; // Store next before we modify pointers

            // Swap the next and prev pointers for current node
            Node temp = current.prev;
            current.prev = current.next;
            current.next = temp;

            current = nextNode; // Move to next node using original direction
        }

        // Complete the reversal: swap head and tail references (O(1) operation)
        head = originalTail;
        tail = originalHead;
    }

    // ================================
    // String representation
    // ================================
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
