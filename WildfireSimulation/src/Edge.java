package src;

/**
 * Edge - Connection between two Nodes with fire spread cost
 */
public class Edge {
    private final Node target;
    private final double weight;

    public Edge(Node target, double weight) {
        this.target = target;
        this.weight = weight;
    }

    public Node getTarget() { return target; }
    public double getWeight() { return weight; }
}
