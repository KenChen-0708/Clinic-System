package adt;

public class HashMap<K, V> implements MapInterface<K, V> {

    // Entry class to store key-value pairs
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private Entry<K, V>[] buckets;      // Array of linked lists (buckets)
    private int size;                   // Number of key-value pairs
    private int capacity;               // Size of the bucket array
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    public HashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
        this.capacity = initialCapacity;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    // Hash function to determine bucket index
    private int hash(K key) {
        if (key == null) {
            return 0;
        }
        return (key.hashCode() & 0x7FFFFFFF) % capacity;
    }

    @Override
    public void put(K key, V value) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];

        // 1) Check if key exists → update value
        while (current != null) {
            if ((key == null && current.key == null) ||
                (key != null && key.equals(current.key))) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        // 2) Ensure capacity before adding new key
        if (size + 1 > capacity * LOAD_FACTOR) {
            resize();
            index = hash(key); // Recompute index after resize
        }

        // 3) Insert new entry at head of bucket
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
    }

    @Override
    public V get(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if ((key == null && current.key == null) ||
                (key != null && key.equals(current.key))) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    @Override
    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> previous = null;

        while (current != null) {
            if ((key == null && current.key == null) ||
                (key != null && key.equals(current.key))) {
                V removedValue = current.value;
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return removedValue;
            }
            previous = current;
            current = current.next;
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if ((key == null && current.key == null) ||
                (key != null && key.equals(current.key))) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                if ((value == null && current.value == null) ||
                    (value != null && value.equals(current.value))) {
                    return true;
                }
                current = current.next;
            }
        }
        return false;
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
        for (int i = 0; i < capacity; i++) {
            buckets[i] = null;
        }
        size = 0;
    }

    @Override
    public ListInterface<K> getKeys() {
        ListInterface<K> keys = new DoublyLinkedList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        return keys;
    }

    @Override
    public ListInterface<V> getValues() {
        ListInterface<V> values = new DoublyLinkedList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                values.add(current.value);
                current = current.next;
            }
        }
        return values;
    }

    // Efficient O(n) resize
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldBuckets = buckets;
        capacity *= 2;
        buckets = new Entry[capacity];

        for (int i = 0; i < oldBuckets.length; i++) {
            Entry<K, V> current = oldBuckets[i];
            while (current != null) {
                Entry<K, V> next = current.next;
                int newIndex = hash(current.key); // Reuse hash()
                current.next = buckets[newIndex];
                buckets[newIndex] = current;
                current = next;
            }
        }
        // size remains unchanged
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(current.key).append("=").append(current.value);
                first = false;
                current = current.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
