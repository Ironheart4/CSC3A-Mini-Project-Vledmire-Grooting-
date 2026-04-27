package src;

/**
 * WildfireDijkstra - Performs the pathfinding / fire spread simulation.
 * Uses PriorityQueue to always expand the next lowest-cost node.
 *
 * Algorithm state (distances and visited flags) is tracked externally via
 * two HashMap instances (Hash Table ADT) rather than mutating Node fields
 * directly, keeping simulation state cleanly separated from the graph model.
 */

public class WildfireDijkstra {

    private ArrayList<Node> spreadOrder = new ArrayList<>();

    /**
     * Builds a canonical string key for a node used as the HashMap key.
     */
    private static String key(Node n) {
        return n.getRow() + "," + n.getCol();
    }

    public void computeSpreadFrom(Node start, Node[][] graph) {
        if (start == null || graph == null) return;
        spreadOrder = new ArrayList<>();

        // --- Hash Table ADT: track distances and visited status externally ---
        HashMap<String, Double>  dist    = new HashMap<>();
        HashMap<String, Boolean> visited = new HashMap<>();

        // Initialise all nodes: reset Node fields and seed the dist map
        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                Node n = graph[r][c];
                n.resetForSimulation();                     // resets node-internal fields
                dist.put(key(n), Double.POSITIVE_INFINITY); // dist map starts at ∞
            }
        }

        // Source node has distance 0 in both the map and the node field (used by PQ)
        dist.put(key(start), 0.0);
        start.setDistance(0);

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current == null) continue;

            String currentKey = key(current);
            if (visited.containsKey(currentKey)) continue; // already finalised
            visited.put(currentKey, true);
            spreadOrder.add(current);

            double currentDist = dist.get(currentKey);

            for (int i = 0; i < current.getEdges().size(); i++) {
                Edge   edge      = current.getEdges().get(i);
                Node   neighbor  = edge.getTarget();
                String nKey      = key(neighbor);
                double newDist   = currentDist + edge.getWeight();

                double oldDist = dist.containsKey(nKey)
                        ? dist.get(nKey)
                        : Double.POSITIVE_INFINITY;

                if (newDist < oldDist) {
                    dist.put(nKey, newDist);          // update Hash Table
                    neighbor.setDistance(newDist);     // keep PQ ordering in sync
                    neighbor.setPrevious(current);
                    pq.add(neighbor);
                }
            }
        }
    }

    /**
     * Returns nodes in order of fire spread (for animation).
     */
    public ArrayList<Node> getSpreadOrder() {
        return spreadOrder;
    }
}
