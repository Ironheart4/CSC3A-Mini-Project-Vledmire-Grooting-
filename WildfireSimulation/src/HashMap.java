package src;

/**
 * HashMap - Self-implemented Hash Table ADT.
 * Implements the Map interface using separate chaining for collision resolution.
 * Average O(1) put/get/containsKey/remove; resizes when load factor exceeds 0.75.
 */
public class HashMap<K, V> implements Map<K, V> {

    private static final int   DEFAULT_CAPACITY   = 16;
    private static final double LOAD_FACTOR_LIMIT = 0.75;

    /** A single entry in the chain. */
    private static class Entry<K, V> {
        K key;
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
    private int capacity  = DEFAULT_CAPACITY;
    private int size      = 0;

    // ------------------------------------------------------------------ //
    //  Map interface                                                       //
    // ------------------------------------------------------------------ //

    @Override
    public void put(K key, V value) {
        if (key == null) return;

        if ((double) size / capacity >= LOAD_FACTOR_LIMIT) {
            resize();
        }

        int idx = indexOf(key);
        Entry<K, V> head = buckets[idx];

        // Update existing key
        for (Entry<K, V> e = head; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }

        // Insert new entry at head of chain
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = head;
        buckets[idx]  = newEntry;
        size++;
    }

    @Override
    public V get(K key) {
        if (key == null) return null;
        Entry<K, V> e = findEntry(key);
        return (e != null) ? e.value : null;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) return false;
        return findEntry(key) != null;
    }

    @Override
    public V remove(K key) {
        if (key == null) return null;

        int idx = indexOf(key);
        Entry<K, V> prev = null;
        Entry<K, V> curr = buckets[idx];

        while (curr != null) {
            if (curr.key.equals(key)) {
                if (prev == null) {
                    buckets[idx] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return size == 0; }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private int indexOf(K key) {
        int hash = key.hashCode();
        // Keep index non-negative and within capacity
        return (hash & 0x7fffffff) % capacity;
    }

    private Entry<K, V> findEntry(K key) {
        for (Entry<K, V> e = buckets[indexOf(key)]; e != null; e = e.next) {
            if (e.key.equals(key)) return e;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity     = capacity * 2;
        Entry<K, V>[] newBuckets = new Entry[newCapacity];

        for (int i = 0; i < capacity; i++) {
            for (Entry<K, V> e = buckets[i]; e != null; ) {
                Entry<K, V> next = e.next;
                int newIdx = (e.key.hashCode() & 0x7fffffff) % newCapacity;
                e.next          = newBuckets[newIdx];
                newBuckets[newIdx] = e;
                e = next;
            }
        }

        buckets  = newBuckets;
        capacity = newCapacity;
    }
}
