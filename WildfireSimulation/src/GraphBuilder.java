package src;

import java.awt.image.BufferedImage;

/**
 * GraphBuilder - Converts a terrain image into a full Node[][] graph.
 * This is the core class that builds the Graph ADT.
 */
public class GraphBuilder {

    private final ImageClassifier classifier;

    public GraphBuilder() {
        this.classifier = new ImageClassifier();
    }

    /**
     * Main method: Builds the complete graph from the loaded image
     */
    public Node[][] build(BufferedImage image, int blockSize) {
        int rows = image.getHeight() / blockSize;
        int cols = image.getWidth() / blockSize;

        Node[][] grid = new Node[rows][cols];

        // Step 1: Create nodes with terrain classification
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Terrain terrain = classifier.classifyBlock(image, r, c, blockSize);
                grid[r][c] = new Node(r, c, terrain);
            }
        }

        // Step 2: Connect adjacent nodes with proper Edge objects
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Node current = grid[r][c];

                if (r > 0) current.addEdge(new Edge(grid[r-1][c], current.getTerrain().getSpreadCost()));
                if (r < rows - 1) current.addEdge(new Edge(grid[r+1][c], current.getTerrain().getSpreadCost()));
                if (c > 0) current.addEdge(new Edge(grid[r][c-1], current.getTerrain().getSpreadCost()));
                if (c < cols - 1) current.addEdge(new Edge(grid[r][c+1], current.getTerrain().getSpreadCost()));
            }
        }

        return grid;
    }
}