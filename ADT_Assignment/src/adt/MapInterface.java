package adt;

public interface MapInterface<K, V> {

    void put(K key, V value);           // Add or update key-value pair

    V get(K key);                       // Get value by key

    V remove(K key);                    // Remove key-value pair and return value

    boolean containsKey(K key);         // Check if key exists

    boolean containsValue(V value);     // Check if value exists

    boolean isEmpty();                  // Check if map is empty

    int size();                         // Number of key-value pairs

    void clear();                       // Remove all key-value pairs

    ListInterface<K> getKeys();         // Get all keys as a list

    ListInterface<V> getValues();       // Get all values as a list

    String toString();                  // Display all key-value pairs
}
