package src;

/**
 * Node Class - Represents one terrain block in the wildfire graph.
 * Core of the Graph ADT. Uses Terrain enum for type safety.
 */
public class Node implements Comparable<Node> {

    private final int row;
    private final int col;
    private final Terrain terrain;

    private double distance = Double.POSITIVE_INFINITY;
    private Node previous;
    private boolean visited;

    private final ArrayList<Edge> edges;

    public Node(int row, int col, Terrain terrain) {
        this.row = row;
        this.col = col;
        this.terrain = terrain;
        this.edges = new ArrayList<>();
    }

    public void addEdge(Edge edge) {
        if (edge != null) {
            edges.add(edge);
        }
    }

    public void resetForSimulation() {
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
    }

    // Required for PriorityQueue
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.distance, other.distance);
    }

    // Getters
    public int getRow() { return row; }
    public int getCol() { return col; }
    public Terrain getTerrain() { return terrain; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public Node getPrevious() { return previous; }
    public void setPrevious(Node previous) { this.previous = previous; }
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    public ArrayList<Edge> getEdges() { return edges; }

    @Override
    public String toString() {
        return "Node(" + row + "," + col + ") [" + terrain + "]";
    }
}