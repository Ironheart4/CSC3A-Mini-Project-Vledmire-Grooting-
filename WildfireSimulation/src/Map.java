package src;

/**
 * Map<K, V> interface - Defines the contract for key-value mappings.
 * Follows textbook AbstractMap guidance (Goodrich et al.).
 */
public interface Map<K, V> {

    /** Returns the number of entries in the map. */
    int size();

    /** Returns true if the map contains no entries. */
    boolean isEmpty();

    /**
     * Returns the value associated with key, or null if key is absent.
     */
    V get(K key);

    /**
     * Associates the given value with the given key.
     * If the key was already present, replaces the old value and returns it.
     * Returns null if the key is new.
     */
    V put(K key, V value);

    /**
     * Removes the entry with the given key, returning its value, or null if absent.
     */
    V remove(K key);

    /** Returns an iterable collection of all keys. */
    Iterable<K> keySet();

    /** Returns an iterable collection of all values. */
    Iterable<V> values();

    /** Returns an iterable collection of all key-value entries. */
    Iterable<Map.Entry<K, V>> entrySet();

    /**
     * Nested Entry interface — represents a key-value pair stored in the map.
     */
    interface Entry<K, V> {
        K getKey();
        V getValue();
    }
}
