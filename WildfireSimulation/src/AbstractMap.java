package src;

import java.util.Iterator;

/**
 * AbstractMap<K, V> - Provides default implementations of isEmpty, keySet, and
 * values on top of the Map interface, following textbook-style guidance.
 * Concrete subclasses must implement: size(), get(), put(), remove(), entrySet().
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    // -----------------------------------------------------------------------
    //  Reusable MapEntry (textbook-style protected nested class)
    // -----------------------------------------------------------------------

    /**
     * Concrete (protected) key-value entry used by subclasses.
     */
    protected static class MapEntry<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;

        public MapEntry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        @Override public K getKey()   { return key; }
        @Override public V getValue() { return value; }

        /** Package-private setter so HashMap can update the value in-place. */
        void setValue(V value) { this.value = value; }

        @Override
        public String toString() { return "(" + key + " -> " + value + ")"; }
    }

    // -----------------------------------------------------------------------
    //  Default keySet() — derived from entrySet()
    // -----------------------------------------------------------------------

    @Override
    public Iterable<K> keySet() {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    private final Iterator<Map.Entry<K, V>> it = entrySet().iterator();
                    @Override public boolean hasNext() { return it.hasNext(); }
                    @Override public K next()          { return it.next().getKey(); }
                };
            }
        };
    }

    // -----------------------------------------------------------------------
    //  Default values() — derived from entrySet()
    // -----------------------------------------------------------------------

    @Override
    public Iterable<V> values() {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    private final Iterator<Map.Entry<K, V>> it = entrySet().iterator();
                    @Override public boolean hasNext() { return it.hasNext(); }
                    @Override public V next()          { return it.next().getValue(); }
                };
            }
        };
    }
}
