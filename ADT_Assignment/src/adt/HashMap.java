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
    public V put(K key, V value) { //add a new key value pair
    int index = hash(key);
    Entry<K, V> current = buckets[index]; //get head of chain in that bucket

    // 1) Check if key exists → update value
    while (current != null) {
        if ((key == null && current.key == null) ||
            (key != null && key.equals(current.key))) {
            V oldValue = current.value;
            current.value = value;
            return oldValue; // Return previous value
        }
        current = current.next;
    }

    // 2) Ensure capacity before adding new key (if key doesnt exist)
    if (size + 1 > capacity * LOAD_FACTOR) {
        resize();
        index = hash(key); // Recompute index after resize
    }

    // 3) Insert new entry at head of bucket
    Entry<K, V> newEntry = new Entry<>(key, value);
    newEntry.next = buckets[index];
    buckets[index] = newEntry;
    size++;

    return null; // No previous value
}


    @Override
    public V get(K key) { 
        int index = hash(key); //find bucket index
        Entry<K, V> current = buckets[index]; // get head of bucket

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
        Entry<K, V> current = buckets[index]; // get head of bucket
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
        int index = hash(key); //find index
        Entry<K, V> current = buckets[index]; //get head
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
    public boolean containsValue(V value) { //check if contains value
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i]; //get head for each bucket
            while (current != null) {
                if ((value == null && current.value == null) ||
                    (value != null && value.equals(current.value))) {
                    return true;
                }
                current = current.next; //move foward
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

    // Optional/Advanced methods implementation
    @Override
    public void putAll(MapInterface<K, V> otherMap) {
        if (otherMap == null) {
            return;
        }
        ListInterface<K> keys = otherMap.getKeys();
        for (int i = 1; i <= keys.getNumberOfEntries(); i++) {
            K key = keys.getEntry(i);
            V value = otherMap.get(key);
            put(key, value);
        }
    }

    @Override
    public V replace(K key, V newValue) {
        if (!containsKey(key)) {
            return null; // Key doesn't exist, no replacement
        }
        return put(key, newValue); // Use existing put method which returns old value
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapInterface)) return false;

        @SuppressWarnings("unchecked")
        MapInterface<K, V> otherMap = (MapInterface<K, V>) o;

        if (size() != otherMap.size()) {
            return false;
        }

        // Check if all key-value pairs match
        ListInterface<K> keys = getKeys();
        for (int i = 1; i <= keys.getNumberOfEntries(); i++) {
            K key = keys.getEntry(i);
            V thisValue = get(key);
            V otherValue = otherMap.get(key);

            if ((thisValue == null && otherValue != null) ||
                (thisValue != null && !thisValue.equals(otherValue))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                // Combine key and value hash codes
                int keyHash = (current.key == null) ? 0 : current.key.hashCode();
                int valueHash = (current.value == null) ? 0 : current.value.hashCode();
                hash += keyHash ^ valueHash; // XOR for better distribution
                current = current.next;
            }
        }
        return hash;
    }
}
