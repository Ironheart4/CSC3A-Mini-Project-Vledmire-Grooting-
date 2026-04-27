package src;

/**
 * Map ADT - Generic key-value mapping interface.
 * Implemented by HashMap using a hash table.
 */
public interface Map<K, V> {

    /**
     * Associates the specified value with the specified key.
     * If the key already exists, its value is replaced.
     */
    void put(K key, V value);

    /**
     * Returns the value mapped to the given key, or null if absent.
     */
    V get(K key);

    /**
     * Returns true if this map contains a mapping for the given key.
     */
    boolean containsKey(K key);

    /**
     * Removes the mapping for the given key and returns the old value,
     * or null if the key was not present.
     */
    V remove(K key);

    /** Returns the number of key-value mappings in this map. */
    int size();

    /** Returns true if this map contains no mappings. */
    boolean isEmpty();
}
