package src;

/**
 * WildfireDijkstra - Performs the pathfinding / fire spread simulation.
 * Uses PriorityQueue to always expand the next lowest-cost node.
 */

public class WildfireDijkstra {

    public void computeSpreadFrom(Node start, Node[][] graph) {
        if (start == null || graph == null) return;

        // Reset all nodes
        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                graph[r][c].resetForSimulation();
            }
        }

        start.setDistance(0);

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current == null || current.isVisited()) continue;

            current.setVisited(true);

            for (int i = 0; i < current.getEdges().size(); i++) {
                Edge edge = current.getEdges().get(i);
                Node neighbor = edge.getTarget();
                double newDist = current.getDistance() + edge.getWeight();

                if (newDist < neighbor.getDistance()) {
                    neighbor.setDistance(newDist);
                    neighbor.setPrevious(current);
                    pq.add(neighbor);
                }
            }
        }
    }

    /**
     * Returns nodes in order of fire spread (for animation)
     */
    public ArrayList<Node> getSpreadOrder(Node[][] graph) {
        ArrayList<Node> order = new ArrayList<>();

        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                Node node = graph[r][c];
                if (node.getDistance() != Double.POSITIVE_INFINITY) {
                    order.add(node);
                }
            }
        }
        return order;
    }
}