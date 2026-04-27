package src;

/**
 * WildfireDijkstra - Performs the pathfinding / fire spread simulation.
 * Uses PriorityQueue to always expand the next lowest-cost node.
 * After the run, a HashMap<String, Double> stores each visited node's final
 * distance (keyed by "row,col") and a HashMap<String, String> stores the
 * predecessor tree — both accessible for analysis and UI display.
 */

public class WildfireDijkstra {

    private ArrayList<Node> spreadOrder = new ArrayList<>();

    /** Final fire-spread distances keyed by "row,col". */
    private HashMap<String, Double> distanceMap = new HashMap<>();

    /** Predecessor tree: "row,col" of node -> "row,col" of its predecessor. */
    private HashMap<String, String> predecessorMap = new HashMap<>();

    public void computeSpreadFrom(Node start, Node[][] graph) {
        if (start == null || graph == null) return;
        spreadOrder    = new ArrayList<>();
        distanceMap    = new HashMap<>();
        predecessorMap = new HashMap<>();

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
            spreadOrder.add(current);

            // Record final distance and predecessor in the HashMaps
            String key = nodeKey(current);
            distanceMap.put(key, current.getDistance());
            if (current.getPrevious() != null) {
                predecessorMap.put(key, nodeKey(current.getPrevious()));
            }

            for (Edge edge : current.getEdges()) {
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

    /** Returns nodes in order of fire spread (for animation). */
    public ArrayList<Node> getSpreadOrder() {
        return spreadOrder;
    }

    /**
     * Returns the distance map built during the last simulation run.
     * Keys are "row,col" strings; values are the Dijkstra distances.
     */
    public HashMap<String, Double> getDistanceMap() {
        return distanceMap;
    }

    /**
     * Returns the predecessor (shortest-path tree) map from the last simulation run.
     * Keys are "row,col" of a node; values are "row,col" of its predecessor.
     */
    public HashMap<String, String> getPredecessorMap() {
        return predecessorMap;
    }

    /** Stable key for a node: "row,col". */
    private static String nodeKey(Node node) {
        return node.getRow() + "," + node.getCol();
    }
}
