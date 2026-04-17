/**
 * Self-implemented ArrayList<T> for the Graph-Based Wildfire Spread Simulation System
 * Auxiliary data structure (encouraged by Mini Project spec)
 * Used for Node.edges, spread order, terrain counts, etc.
 */
public class ArrayList<T> {

    private int capacity;
    private T[] array;
    private int size;

    /**
     * Default constructor - starts with capacity 1 and grows by doubling
     */
    public ArrayList() {
        this.capacity = 1;
        this.size = 0;
        this.array = createArray(this.capacity);
    }

    /**
     * Constructor with initial capacity
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = 1;
        }
        this.capacity = initialCapacity;
        this.size = 0;
        this.array = createArray(this.capacity);
    }

    @SuppressWarnings("unchecked")
    private T[] createArray(int size) {
        return (T[]) new Object[size];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index is out of range: " + index);
        }
        return array[index];
    }

    public void set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index is out of range: " + index);
        }
        array[index] = element;
    }

    /**
     * Adds element at the end of the list (most commonly used)
     */
    public void add(T element) {
        add(size, element);
    }

    /**
     * Inserts element at specified index (shifts elements right)
     */
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index is out of range: " + index);
        }

        // Expand if full
        if (size == array.length) {
            expandArray();
        }

        // Shift elements to the right
        for (int i = size - 1; i >= index; i--) {
            array[i + 1] = array[i];
        }

        array[index] = element;
        size++;
    }

    /**
     * Doubles the capacity when the array is full
     */
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
            throw new IndexOutOfBoundsException("Index is out of range: " + index);
        }

        T removed = array[index];

        // Shift elements to the left
        for (int i = index; i < size - 1; i++) {
            array[i] = array[i + 1];
        }

        array[size - 1] = null;  // Help garbage collector
        size--;
        return removed;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            str.append(array[i]);
            if (i < size - 1) {
                str.append(", ");
            }
        }
        str.append("]");
        return str.toString();
    }
}
