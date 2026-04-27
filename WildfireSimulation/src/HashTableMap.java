package src;

/**
 * Custom generic Map backed by a hash table (separate chaining).
 * No third-party libraries used — uses the project's own ArrayList<T>.
 *
 * @param <K> Key type (must implement hashCode / equals)
 * @param <V> Value type
 */
public class HashTableMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR   = 0.75;

    /** A single key-value pair stored in a bucket chain. */
    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key   = key;
            this.value = value;
            this.next  = null;
        }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] buckets = new Entry[DEFAULT_CAPACITY];
    private int capacity;
    private int size;

    public HashTableMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.size     = 0;
    }

    public HashTableMap(int initialCapacity) {
        this.capacity = Math.max(1, initialCapacity);
        this.size     = 0;
        @SuppressWarnings("unchecked")
        Entry<K, V>[] b = new Entry[this.capacity];
        this.buckets = b;
    }

    /** Maps the key to its bucket index. */
    private int bucketIndex(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        // ensure non-negative index
        return (h & 0x7FFFFFFF) % capacity;
    }

    /**
     * Associates the specified value with the specified key.
     * If a mapping for the key already exists its value is replaced.
     */
    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) {
            rehash();
        }
        int idx = bucketIndex(key);
        Entry<K, V> entry = buckets[idx];
        while (entry != null) {
            if (keysEqual(entry.key, key)) {
                entry.value = value;
                return;
            }
            entry = entry.next;
        }
        // prepend new entry
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[idx];
        buckets[idx]  = newEntry;
        size++;
    }

    /**
     * Returns the value associated with the key, or {@code null} if absent.
     */
    public V get(K key) {
        int idx = bucketIndex(key);
        Entry<K, V> entry = buckets[idx];
        while (entry != null) {
            if (keysEqual(entry.key, key)) return entry.value;
            entry = entry.next;
        }
        return null;
    }

    /**
     * Returns the value for the key, or {@code defaultValue} if the key is absent.
     */
    public V getOrDefault(K key, V defaultValue) {
        V val = get(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Returns {@code true} if the map contains a mapping for the key.
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Removes the mapping for the key.
     * @return the previous value, or {@code null} if absent.
     */
    public V remove(K key) {
        int idx = bucketIndex(key);
        Entry<K, V> prev = null;
        Entry<K, V> entry = buckets[idx];
        while (entry != null) {
            if (keysEqual(entry.key, key)) {
                if (prev == null) {
                    buckets[idx] = entry.next;
                } else {
                    prev.next = entry.next;
                }
                size--;
                return entry.value;
            }
            prev  = entry;
            entry = entry.next;
        }
        return null;
    }

    /** Number of key-value mappings stored. */
    public int size() { return size; }

    /** Returns {@code true} if the map contains no mappings. */
    public boolean isEmpty() { return size == 0; }

    /** Returns all keys in an ArrayList (insertion order is not guaranteed). */
    public ArrayList<K> keys() {
        ArrayList<K> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> entry = buckets[i];
            while (entry != null) {
                result.add(entry.key);
                entry = entry.next;
            }
        }
        return result;
    }

    /** Returns all values in an ArrayList (insertion order is not guaranteed). */
    public ArrayList<V> values() {
        ArrayList<V> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> entry = buckets[i];
            while (entry != null) {
                result.add(entry.value);
                entry = entry.next;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> entry = buckets[i];
            while (entry != null) {
                if (!first) sb.append(", ");
                sb.append(entry.key).append("=").append(entry.value);
                first = false;
                entry = entry.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // ------------------------------------------------------------------ helpers

    private boolean keysEqual(K a, K b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    /** Doubles capacity and redistributes all entries. */
    @SuppressWarnings("unchecked")
    private void rehash() {
        int newCapacity = capacity * 2;
        Entry<K, V>[] newBuckets = new Entry[newCapacity];
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> entry = buckets[i];
            while (entry != null) {
                Entry<K, V> next = entry.next;
                int newIdx = (entry.key == null ? 0
                        : (entry.key.hashCode() & 0x7FFFFFFF) % newCapacity);
                entry.next      = newBuckets[newIdx];
                newBuckets[newIdx] = entry;
                entry = next;
            }
        }
        buckets  = newBuckets;
        capacity = newCapacity;
    }
}
