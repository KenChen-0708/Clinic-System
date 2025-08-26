package adt;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

/**
 * DoublyLinkedList<T>
 *
 * Why we selected this ADT:
 * --------------------------
 * - Dynamic: grows/shrinks without fixed size limit.
 * - Efficient insertion & deletion at both ends: O(1).
 * - Doubly linked nodes make reverse() and mergeSort() easier
 *   than in a singly linked list.
 * - Avoids array resizing overhead (like in ArrayList).
 */
public class DoublyLinkedList<T> implements ListInterface<T>, Serializable {

    // ================================
    // Inner Node class
    // Stores element + links to next/prev nodes
    // ================================
    private class Node implements Serializable {
        T data;
        Node next;
        Node prev;
        Node(T data) { this.data = data; }
    }

    // ================================
    // Inner Iterator class
    // Provides safe traversal using Java's Iterator
    // ================================
    private class ListIterator implements Iterator<T> {
        private Node currentNode = head;

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            T data = currentNode.data;
            currentNode = currentNode.next;
            return data;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported.");
        }
    }

    // ================================
    // Instance variables
    // ================================
    private Node head;             // First node
    private Node tail;             // Last node
    private int numberOfEntries;   // Count of nodes

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
    /**
     * add(T newEntry)
     * ----------------
     * - Inserts newEntry at end of list.
     * - Calls add(position n+1).
     *
     * Complexity:
     * - Time: O(1)
     * - Space: O(1)
     */
    @Override
    public boolean add(T newEntry) {
        return add(numberOfEntries + 1, newEntry);
    }

    /**
     * add(int newPosition, T newEntry)
     * --------------------------------
     * - Inserts newEntry at the given position (1-based).
     * - Handles insert at start, end, or middle.
     * - Null entries are rejected.
     *
     * Complexity:
     * - At start/end: O(1)
     * - In middle   : O(min(i, n-i)) to reach node
     */
    @Override
    public boolean add(int newPosition, T newEntry) {
        if (newEntry == null) {
            throw new NullPointerException("newEntry must not be null");
        }
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

    // Helper: unlink node safely (used by remove)
    private T unlink(Node x) {
        T val = x.data;
        Node p = x.prev, n = x.next;

        if (p == null) { head = n; } else { p.next = n; }
        if (n == null) { tail = p; } else { n.prev = p; }

        x.next = x.prev = null; // fully detach
        numberOfEntries--;
        return val;
    }

    @Override
    public T remove(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }
        return unlink(getNodeAt(givenPosition));
    }

    @Override
    public void clear() {
        head = tail = null;
        numberOfEntries = 0;
    }

    @Override
    public boolean replace(int givenPosition, T newEntry) {
        if (newEntry == null) {
            throw new NullPointerException("newEntry must not be null");
        }
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return false;
        }
        getNodeAt(givenPosition).data = newEntry;
        return true;
    }

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
    /**
     * contains(T anEntry)
     * -------------------
     * Why linear search?
     * - Linked list random access is O(n), so binary search is inefficient.
     * - Best approach: walk node-by-node until match.
     *
     * Complexity:
     * - Time: O(n)
     * - Space: O(1)
     */
    @Override
    public boolean contains(T anEntry) {
        Node current = head;
        while (current != null) {
            if (java.util.Objects.equals(current.data, anEntry)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override public int getNumberOfEntries() { return numberOfEntries; }
    @Override public boolean isEmpty() { return numberOfEntries == 0; }
    @Override public boolean isFull() { return false; } // dynamic list never "full"

    // Helper: get node at position (optimized from head/tail)
    private Node getNodeAt(int position) {
        if (position < 1 || position > numberOfEntries) return null;

        if (position <= (numberOfEntries / 2)) {
            Node cur = head;
            for (int i = 1; i < position; i++) cur = cur.next;
            return cur;
        } else {
            Node cur = tail;
            for (int i = numberOfEntries; i > position; i--) cur = cur.prev;
            return cur;
        }
    }

    // ================================
    // Merge Sort (stable, in-place)
    // ================================
    /**
     * mergeSort(Comparator<T> comparator)
     * -----------------------------------
     * Why merge sort?
     * - Splitting and merging are pointer-based (efficient for linked lists).
     * - Stable: preserves order of equal elements.
     * - Better suited than quicksort/heap sort (those need random access).
     *
     * How it works:
     * 1. Recursively split list into halves until size 0/1.
     * 2. Merge sorted halves into one.
     * 3. Rebuild prev pointers & tail.
     *
     * Complexity:
     * - Time: O(n log n)
     * - Space: O(1) auxiliary, O(log n) recursion stack
     */
    @Override
    public void mergeSort(Comparator<T> comparator) {
        if (comparator == null) throw new NullPointerException("comparator must not be null");
        if (numberOfEntries <= 1) return;

        head = mergeSortNodes(head, comparator);

        // Rebuild prev pointers & tail
        Node cur = head, prev = null;
        while (cur != null) {
            cur.prev = prev;
            prev = cur;
            cur = cur.next;
        }
        tail = prev;
    }

    /** Recursive mergeSort on nodes */
    private Node mergeSortNodes(Node h, Comparator<T> comp) {
        if (h == null || h.next == null) return h;
        Node mid = split(h);
        Node right = mid.next;
        mid.next = null;
        if (right != null) right.prev = null;
        Node leftSorted  = mergeSortNodes(h, comp);
        Node rightSorted = mergeSortNodes(right, comp);
        return mergeLists(leftSorted, rightSorted, comp);
    }

    /** Find mid node using slow/fast pointers */
    private Node split(Node h) {
        Node slow = h, fast = h;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    /** Merge two sorted sublists */
    private Node mergeLists(Node a, Node b, Comparator<T> comp) {
        Node dummy = new Node(null);
        Node tailM = dummy;

        while (a != null && b != null) {
            if (comp.compare(a.data, b.data) <= 0) {
                tailM.next = a; a.prev = tailM; a = a.next;
            } else {
                tailM.next = b; b.prev = tailM; b = b.next;
            }
            tailM = tailM.next;
        }

        Node rest = (a != null) ? a : b;
        if (rest != null) { tailM.next = rest; rest.prev = tailM; }

        Node newHead = dummy.next;
        if (newHead != null) newHead.prev = null;
        return newHead;
    }

    /**
     * isSorted()
     * ----------
     * - Checks list order by comparing adjacent pairs.
     *
     * Complexity:
     * - Time: O(n)
     * - Space: O(1)
     */
    @Override
    public boolean isSorted(Comparator<T> comparator) {
        if (comparator == null) throw new NullPointerException("comparator must not be null");
        if (numberOfEntries <= 1) return true;
        Node current = head;
        while (current != null && current.next != null) {
            if (comparator.compare(current.data, current.next.data) > 0) return false;
            current = current.next;
        }
        return true;
    }

    // ================================
    // Reverse (O(n), one pass, O(1) space)
    // ================================
    /**
     * Reverses the order of elements in the doubly linked list
     *
     * Algorithm: 1. Store original head and tail references for O(1) swapping
     * 2. Traverse the list once, swapping next/prev pointers for each node 3.
     * Swap head and tail references to complete the reversal
     *
     * Time Complexity: O(n) - single pass through the list Space Complexity:
     * O(1) - only uses a few temporary variables
     *
     * This implementation fixes two key issues: - Efficiency: No unnecessary
     * second traversal to find new tail - Logic clarity: Direct head/tail
     * swapping instead of relying on loop side effects
     */
    @Override
    public void reverse() {
        if (numberOfEntries <= 1) {
            return;
        }

        Node originalHead = head;
        Node originalTail = tail;
        Node current = head;

        while (current != null) {
            Node nextNode = current.next;
            Node temp = current.prev;
            current.prev = current.next;
            current.next = temp;
            current = nextNode;
        }

        head = originalTail;
        tail = originalHead;
    }

    // ================================
    // Iterator
    // ================================
    /**
     * iterator()
     * ----------
     * - Enables enhanced for-loop: for(T item : list).
     * - Encapsulates traversal without exposing Node.
     *
     * Complexity:
     * - Time per step: O(1)
     * - Space: O(1)
     */
    @Override
    public Iterator<T> iterator() {
        return new ListIterator();
    }

    // ================================
    // toString
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