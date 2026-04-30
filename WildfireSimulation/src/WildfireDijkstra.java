package src;

/**
 * WildfireDijkstra - Performs graph-based fire spread pathfinding.
 *
 * <p>This class implements Dijkstra's shortest-path algorithm on the Region
 * Adjacency Graph (RAG) built by {@link GraphBuilder}. Each {@link Node}
 * is a terrain block; each {@link Edge} carries a spread-cost weight derived
 * from the target cell's terrain type.  Dijkstra always expands the
 * lowest-cost frontier node first (via the custom {@link PriorityQueue}).
 *
 * <p><b>RAG connection:</b> the graph passed in is a practical RAG — nodes
 * are image blocks and edges model spatial adjacency between neighbouring
 * blocks.  Dijkstra therefore finds the minimum-cost fire-spread path
 * through the RAG from the ignition cell to every reachable cell.
 *
 * <p><b>Data structures used:</b>
 * <ul>
 *   <li>Custom {@link PriorityQueue} — min-heap frontier</li>
 *   <li>Custom {@link HashMap} — distance table (Hash Table ADT)</li>
 *   <li>Custom {@link HashMap} — visited set</li>
 *   <li>Custom {@link HashMap} — predecessor / Shortest-Path Tree (SPT)</li>
 * </ul>
 *
 * <p><b>Future extensions:</b> spectral graph theory (e.g., graph Laplacian
 * for segmentation) and GNN-based classification could replace or augment the
 * cost function; k-NN graphs are an alternative RAG construction strategy.
 */
public class WildfireDijkstra {

    private ArrayList<Node> spreadOrder = new ArrayList<>();

    /** Final fire-spread distances keyed by "row,col". */
    private HashMap<String, Double> distanceMap = new HashMap<>();

    /**
     * Predecessor / Shortest-Path Tree (SPT) map built by Dijkstra.
     * Key  : "row,col" of a settled node.
     * Value: "row,col" of that node's predecessor on the cheapest spread path.
     * Drawing these links from the ignition node forms the SPT overlay.
     */
    private HashMap<String, String> predecessorMap = new HashMap<>();

    /**
     * Runs Dijkstra's algorithm on the RAG from {@code start}.
     *
     * <p>Each RAG edge is traversed exactly once per relaxation; the
     * {@link PriorityQueue} guarantees we always settle the cheapest reachable
     * node next.  After the run, {@link #getSpreadOrder()},
     * {@link #getDistanceMap()}, and {@link #getPredecessorMap()} are
     * populated and ready for the UI overlays.
     */
    public void computeSpreadFrom(Node start, Node[][] graph) {
        if (start == null || graph == null) return;
        spreadOrder    = new ArrayList<>();
        distanceMap    = new HashMap<>();
        predecessorMap = new HashMap<>();

        // Hash Table ADT: track distances and visited status externally
        HashMap<String, Double>  dist    = new HashMap<>();
        HashMap<String, Boolean> visited = new HashMap<>();

        // Initialise all nodes: reset Node fields and seed the distance map
        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                Node n = graph[r][c];
                n.resetForSimulation();                          // resets node-internal fields
                dist.put(nodeKey(n), Double.POSITIVE_INFINITY); // dist map starts at ∞
            }
        }

        // Source node has distance 0 in both the map and the node field (used by PQ)
        dist.put(nodeKey(start), 0.0);
        start.setDistance(0);

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current == null) continue;

            String currentKey = nodeKey(current);
            if (visited.get(currentKey) != null) continue; // already finalised
            visited.put(currentKey, true);
            spreadOrder.add(current);

            // Record final distance and predecessor in the HashMaps (builds the SPT)
            distanceMap.put(currentKey, current.getDistance());
            if (current.getPrevious() != null) {
                predecessorMap.put(currentKey, nodeKey(current.getPrevious()));
            }

            // Relax all RAG edges leaving the current node
            for (Edge edge : current.getEdges()) {
                Node   neighbor = edge.getTarget();
                String nKey     = nodeKey(neighbor);
                double oldDist  = dist.get(nKey) == null ? Double.POSITIVE_INFINITY : dist.get(nKey);
                double newDist  = current.getDistance() + edge.getWeight();

                if (newDist < oldDist) {
                    dist.put(nKey, newDist);           // update Hash Table distance
                    neighbor.setDistance(newDist);     // keep PQ ordering in sync
                    neighbor.setPrevious(current);
                    pq.add(neighbor);
                }
            }
        }
    }

    /** Returns nodes in the order they were settled by Dijkstra (fire spread order). */
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
     * Returns the predecessor / Shortest-Path Tree (SPT) map.
     * Keys are "row,col" of a settled node; values are "row,col" of its
     * predecessor.  The UI uses this to draw the SPT overlay after simulation.
     */
    public HashMap<String, String> getPredecessorMap() {
        return predecessorMap;
    }

    /** Produces a stable string key for a node: "row,col". */
    private static String nodeKey(Node node) {
        return node.getRow() + "," + node.getCol();
    }
}
