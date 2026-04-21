package src;
/**
 * Self-implemented ArrayList<T> - Auxiliary data structure.
 * Used for Node.edges and other lists. Compliant with Mini Project rules.
 */
public class ArrayList<T> {

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
}