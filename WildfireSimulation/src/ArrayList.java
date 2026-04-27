package src;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Self-implemented ArrayList<T> - Auxiliary data structure.
 * Used for Node.edges and other lists. Compliant with Mini Project rules.
 * Implements Iterable<T> to support enhanced for-loops via ArrayIterator.
 */
public class ArrayList<T> implements Iterable<T> {

    private int capacity;
    private T[] array;
    private int size;

    public ArrayList() {
        this.capacity = 1;
        this.size = 0;
        this.array = createArray(capacity);
    }

    public ArrayList(int initialCapacity) {
        this.capacity = Math.max(1, initialCapacity);
        this.size = 0;
        this.array = createArray(capacity);
    }

    @SuppressWarnings("unchecked")
    private T[] createArray(int size) {
        return (T[]) new Object[size];
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        return array[index];
    }

    public void set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        array[index] = element;
    }

    public void add(T element) {
        add(size, element);
    }

    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        if (size == array.length) {
            expandArray();
        }
        for (int i = size - 1; i >= index; i--) {
            array[i + 1] = array[i];
        }
        array[index] = element;
        size++;
    }

    private void expandArray() {
        int newCap = capacity * 2;
        T[] newArray = createArray(newCap);
        for (int i = 0; i < size; i++) {
            newArray[i] = array[i];
        }
        array = newArray;
        capacity = newCap;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        T removed = array[index];
        for (int i = index; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        array[size - 1] = null;
        size--;
        return removed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(array[i]);
            if (i < size - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns an iterator over the elements in this list (textbook ArrayIterator pattern).
     * Enables use of ArrayList in enhanced for-loops.
     */
    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    /**
     * Textbook-style nested iterator for ArrayList.
     */
    private class ArrayIterator implements Iterator<T> {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException("No more elements");
            return array[cursor++];
        }
    }
}