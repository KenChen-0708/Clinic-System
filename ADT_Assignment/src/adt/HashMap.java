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
        this.capacity = initialCapacity;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    // Hash function to determine bucket index
    private int hash(K key) {
        if (key == null) {
            return 0;
        }
        return Math.abs(key.hashCode()) % capacity;
    }

    @Override
    public void put(K key, V value) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        
        // Check if key already exists
        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                current.value = value; // Update existing value
                return;
            }
            current = current.next;
        }
        
        // Add new entry at the beginning of the bucket
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
        
        // Resize if load factor exceeded
        if (size > capacity * LOAD_FACTOR) {
            resize();
        }
    }

    @Override
    public V get(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        
        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        
        return null; // Key not found
    }

    @Override
    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> previous = null;
        
        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                V removedValue = current.value;
                
                if (previous == null) {
                    // Removing first entry in bucket
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
        
        return null; // Key not found
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                if (current.value != null && current.value.equals(value)) {
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

    // Resize the hash table when load factor is exceeded
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldBuckets = buckets;
        int oldCapacity = capacity;
        
        capacity *= 2;
        buckets = new Entry[capacity];
        size = 0;
        
        // Rehash all entries
        for (int i = 0; i < oldCapacity; i++) {
            Entry<K, V> current = oldBuckets[i];
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
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
