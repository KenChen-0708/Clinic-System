package adt;

public interface MapInterface<K, V> {

    V put(K key, V value);              // Add or update key-value pair, returns previous value

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

    // Optional/Advanced methods
    void putAll(MapInterface<K, V> otherMap);  // Copy all entries from another map

    V replace(K key, V newValue);       // Replace value only if key exists

    boolean equals(Object o);           // Check if two maps are equal

    int hashCode();                     // Hash code for the map
}
