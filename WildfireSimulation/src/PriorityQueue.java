package src;

/**
 * Self-implemented PriorityQueue - Used by WildfireDijkstra.
 * Uses your ArrayList internally.
 */
public class PriorityQueue<T extends Comparable<T>> {

    private final ArrayList<T> entries;

    public PriorityQueue() {
        entries = new ArrayList<>();
    }

    public void add(T item) {
        if (item == null) return;
        entries.add(item);
    }

    public T poll() {
        if (isEmpty()) return null;

        int minIndex = 0;
        for (int i = 1; i < entries.size(); i++) {
            if (entries.get(i).compareTo(entries.get(minIndex)) < 0) {
                minIndex = i;
            }
        }
        return entries.remove(minIndex);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }
}
