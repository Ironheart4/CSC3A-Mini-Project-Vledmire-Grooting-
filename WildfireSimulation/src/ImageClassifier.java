package src;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * ImageClassifier - Responsible for terrain classification and creating the 
 * coloured masked image shown in the "Classified / Masked Image" panel.
 */
public class ImageClassifier {

    /**
     * Classifies one block of pixels and returns the Terrain type
     */
    public Terrain classifyBlock(BufferedImage image, int row, int col, int size) {
        int startX = col * size;
        int startY = row * size;

        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = startY; y < startY + size && y < image.getHeight(); y++) {
            for (int x = startX; x < startX + size && x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
                count++;
            }
        }

        if (count == 0) return Terrain.BARREN;

        int avgR = (int) (r / count);
        int avgG = (int) (g / count);
        int avgB = (int) (b / count);

        if (avgB > avgG && avgB > avgR) return Terrain.WATER;
        if (avgG > avgR && avgG > avgB) return Terrain.GRASSLAND;
        if (avgR > 150 && avgG < 100) return Terrain.DRY_VEGETATION;
        return Terrain.FOREST;
    }

    /**
     * Creates the coloured masked image for the UI (this is what you see in the right panel)
     */
    public BufferedImage createMaskedImage(Node[][] graph, BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int blockWidth = width / graph[0].length;
        int blockHeight = height / graph.length;

        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                Node node = graph[r][c];
                String hex = node.getTerrain().getBaseColor();
                int rgb = Integer.parseInt(hex.substring(1), 16);

                int startX = c * blockWidth;
                int startY = r * blockHeight;

                for (int y = startY; y < startY + blockHeight && y < height; y++) {
                    for (int x = startX; x < startX + blockWidth && x < width; x++) {
                        masked.setRGB(x, y, 0xAA000000 | rgb);   // semi-transparent
                    }
                }
            }
        }
        return masked;
    }
}