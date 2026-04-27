package src;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * HashMap<K, V> - Custom hash-table implementation using separate chaining.
 * Extends AbstractMap, which provides isEmpty(), keySet(), and values().
 * Stores entries in an array of ArrayList buckets; resizes when load factor
 * exceeds MAX_LOAD_FACTOR.
 */
public class HashMap<K, V> extends AbstractMap<K, V> {

    private static final int    DEFAULT_CAPACITY  = 17;
    private static final double MAX_LOAD_FACTOR   = 0.75;

    private int                             capacity;
    private int                             size;
    private ArrayList<MapEntry<K, V>>[]     table;

    // -----------------------------------------------------------------------
    //  Constructors
    // -----------------------------------------------------------------------

    public HashMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        this.capacity = Math.max(1, initialCapacity);
        this.size     = 0;
        this.table    = new ArrayList[this.capacity];
    }

    // -----------------------------------------------------------------------
    //  Private helpers
    // -----------------------------------------------------------------------

    /** Maps a key to a bucket index in [0, capacity). */
    private int hashIndex(K key) {
        if (key == null) return 0;
        return Math.abs(key.hashCode()) % capacity;
    }

    // -----------------------------------------------------------------------
    //  Map interface methods
    // -----------------------------------------------------------------------

    @Override
    public int size() { return size; }

    @Override
    public V get(K key) {
        int idx = hashIndex(key);
        ArrayList<MapEntry<K, V>> bucket = table[idx];
        if (bucket == null) return null;
        for (MapEntry<K, V> entry : bucket) {
            if (keysEqual(entry.getKey(), key)) return entry.getValue();
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        int idx = hashIndex(key);
        if (table[idx] == null) table[idx] = new ArrayList<>();
        ArrayList<MapEntry<K, V>> bucket = table[idx];
        for (MapEntry<K, V> entry : bucket) {
            if (keysEqual(entry.getKey(), key)) {
                V old = entry.getValue();
                entry.setValue(value);
                return old;
            }
        }
        bucket.add(new MapEntry<>(key, value));
        size++;
        if ((double) size / capacity > MAX_LOAD_FACTOR) resize();
        return null;
    }

    @Override
    public V remove(K key) {
        int idx = hashIndex(key);
        ArrayList<MapEntry<K, V>> bucket = table[idx];
        if (bucket == null) return null;
        for (int i = 0; i < bucket.size(); i++) {
            MapEntry<K, V> entry = bucket.get(i);
            if (keysEqual(entry.getKey(), key)) {
                bucket.remove(i);
                size--;
                return entry.getValue();
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    //  entrySet() — drives AbstractMap's keySet() and values()
    // -----------------------------------------------------------------------

    @Override
    public Iterable<Map.Entry<K, V>> entrySet() {
        return new EntryIterable();
    }

    private class EntryIterable implements Iterable<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }
    }

    /** Iterates over all entries across all buckets. */
    private class EntryIterator implements Iterator<Map.Entry<K, V>> {
        private int                           bucketIdx = 0;
        private Iterator<MapEntry<K, V>>      bucketIter;

        public EntryIterator() { advance(); }

        /** Advances to the next non-empty bucket. */
        private void advance() {
            while (bucketIdx < capacity) {
                if (table[bucketIdx] != null && table[bucketIdx].size() > 0) {
                    if (bucketIter == null || !bucketIter.hasNext()) {
                        bucketIter = table[bucketIdx].iterator();
                    }
                    if (bucketIter.hasNext()) return;
                }
                bucketIdx++;
                bucketIter = null;
            }
        }

        @Override
        public boolean hasNext() {
            return bucketIdx < capacity && bucketIter != null && bucketIter.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            if (!hasNext()) throw new NoSuchElementException();
            Map.Entry<K, V> result = bucketIter.next();
            if (!bucketIter.hasNext()) {
                bucketIdx++;
                bucketIter = null;
                advance();
            }
            return result;
        }
    }

    // -----------------------------------------------------------------------
    //  Resize
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCap = capacity * 2 + 1;
        ArrayList<MapEntry<K, V>>[] oldTable = table;
        int oldCap = capacity;
        capacity = newCap;
        size     = 0;
        table    = new ArrayList[newCap];
        for (int i = 0; i < oldCap; i++) {
            ArrayList<MapEntry<K, V>> bucket = oldTable[i];
            if (bucket == null) continue;
            for (MapEntry<K, V> entry : bucket) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Utility
    // -----------------------------------------------------------------------

    /** Null-safe key equality check. */
    private boolean keysEqual(K a, K b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<K, V> e : entrySet()) {
            if (!first) sb.append(", ");
            sb.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
