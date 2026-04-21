package src;

/**
 * Node Class - Represents one terrain block in the wildfire graph.
 * Core of the Graph ADT. Uses Terrain enum for type safety.
 */
public class Node {

    private final int row;
    private final int col;
    private final Terrain terrain;

    private double distance;      // Used only during Dijkstra simulation
    private Node previous;
    private boolean visited;

    private final ArrayList<Edge> edges;   // Self-implemented ArrayList

    /**
     * Constructor
     */
    public Node(int row, int col, Terrain terrain) {
        this.row = row;
        this.col = col;
        this.terrain = terrain;
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
        this.edges = new ArrayList<>();
    }

    /**
     * Adds a proper Edge to this node
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            edges.add(edge);
        }
    }

    /**
     * Resets node for a new simulation run
     */
    public void resetForSimulation() {
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
    }

    // Getters (strong encapsulation)
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
        return "Node(" + row + "," + col + ") [" + terrain + "] dist=" +
               (distance == Double.POSITIVE_INFINITY ? "∞" : String.format("%.2f", distance));
    }
}
