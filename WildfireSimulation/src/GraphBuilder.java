package src;

import java.awt.image.BufferedImage;

/**
 * GraphBuilder — converts a terrain image into a weighted, undirected graph
 * that forms a <b>Region Adjacency Graph (RAG)</b> of image blocks.
 *
 * <h2>RAG Construction</h2>
 * <p>A Region Adjacency Graph (RAG) is a graph in which each node represents
 * a contiguous image region and edges connect spatially adjacent regions.
 * This implementation partitions the image into a uniform grid of
 * {@code blockSize × blockSize} pixel blocks (the "regions") and connects
 * each block to its 4-connected grid neighbours (up, down, left, right).
 * Every block is classified into a {@link Terrain} type by
 * {@link ImageClassifier#classifyBlock}, and the RAG edge weights encode the
 * fire-spread cost of entering the target terrain.
 *
 * <p>Although this is a regular grid RAG (rather than a superpixel RAG), the
 * structural principle is identical: nodes are image regions and edges model
 * spatial adjacency.  Dijkstra's algorithm then finds minimum-cost paths
 * through the RAG from any ignition cell.
 *
 * <h2>Relationship to other graph techniques</h2>
 * <ul>
 *   <li><b>k-NN graphs</b> – an alternative RAG construction would connect
 *       each block to its k most similar neighbours by colour/texture rather
 *       than by spatial proximity.</li>
 *   <li><b>MST (Minimum Spanning Tree)</b> – could be run on this RAG to find
 *       a cheapest spanning fire corridor; Dijkstra gives single-source
 *       shortest paths, which is more useful here.</li>
 *   <li><b>Spectral graph theory</b> – the graph Laplacian of this RAG could
 *       be used for image segmentation (spectral clustering), a natural
 *       extension of this project.</li>
 *   <li><b>GNNs</b> – a Graph Neural Network could learn terrain-spread
 *       costs from labelled fire data, replacing the hand-coded cost table.</li>
 * </ul>
 */
public class GraphBuilder {

    private final ImageClassifier classifier;

    public GraphBuilder() {
        this.classifier = new ImageClassifier();
    }

    /**
     * Builds the RAG from a terrain image.
     *
     * <p><b>Step 1 – Node creation (image regions):</b> the image is divided
     * into a {@code rows × cols} grid of blocks.  Each block is classified
     * into a {@link Terrain} type using the average pixel colour, and stored
     * as a {@link Node} in the RAG.
     *
     * <p><b>Step 2 – Edge creation (spatial adjacency):</b> each node is
     * connected to its 4-connected grid neighbours.  This is the RAG
     * adjacency relationship: two blocks are "adjacent" if they share an
     * edge in the image grid.  Edge weights reflect the spread cost of the
     * <em>target</em> terrain type (WATER cells receive infinite cost,
     * making them impassable barriers).
     *
     * @param image     source terrain image
     * @param blockSize side length (pixels) of each grid block / RAG node
     * @return the completed {@code Node[][]} RAG, ready for Dijkstra
     */
    public Node[][] build(BufferedImage image, int blockSize) {
        int rows = image.getHeight() / blockSize;
        int cols = image.getWidth() / blockSize;

        Node[][] grid = new Node[rows][cols];

        // Step 1: Create one RAG node per image block, classified by terrain type
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Terrain terrain = classifier.classifyBlock(image, r, c, blockSize);
                grid[r][c] = new Node(r, c, terrain);
            }
        }

        // Step 2: Connect spatially adjacent nodes — this is the RAG adjacency step.
        // Each node is linked to its up/down/left/right neighbours with a directed
        // Edge whose weight = spread cost of the TARGET cell's terrain.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Node current = grid[r][c];

                if (r > 0)          addEdge(current, grid[r - 1][c]); // up
                if (r < rows - 1)   addEdge(current, grid[r + 1][c]); // down
                if (c > 0)          addEdge(current, grid[r][c - 1]); // left
                if (c < cols - 1)   addEdge(current, grid[r][c + 1]); // right
            }
        }

        return grid;
    }

    /**
     * Adds a directed RAG edge from {@code from} to {@code to}.
     *
     * <p>WATER is an impassable barrier (infinite weight); all other terrain
     * types use their {@link Terrain#getSpreadCost()} as the edge weight.
     * Adding edges in both directions (caller loops over all cells) makes the
     * RAG effectively undirected for fire-spread purposes.
     */
    private void addEdge(Node from, Node to) {
        double weight = to.getTerrain() == Terrain.WATER
                ? Double.POSITIVE_INFINITY
                : to.getTerrain().getSpreadCost();
        from.addEdge(new Edge(to, weight));
    }
}
