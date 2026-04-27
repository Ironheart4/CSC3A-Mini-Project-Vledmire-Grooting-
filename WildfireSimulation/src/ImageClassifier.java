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

    private static final double EXTREME_RISK_THRESHOLD = 0.30;
    private static final double HIGH_RISK_THRESHOLD    = 0.15;
    private static final double MODERATE_RISK_MIN_MED  = 0.50;
    private static final double MODERATE_RISK_MAX_WATER = 0.20;
    private static final double LOW_RISK_WATER_THRESHOLD = 0.40;

    /**
     * Counts nodes per terrain type using a HashMap<Terrain, Integer>.
     * Returns the map so callers can inspect per-type counts directly.
     */
    public Map<Terrain, Integer> countTerrain(Node[][] graph) {
        Map<Terrain, Integer> counts = new HashMap<>();
        if (graph == null) return counts;
        for (int r = 0; r < graph.length; r++) {
            for (int c = 0; c < graph[r].length; c++) {
                Terrain t = graph[r][c].getTerrain();
                Integer prev = counts.get(t);
                counts.put(t, prev == null ? 1 : prev + 1);
            }
        }
        return counts;
    }

    /**
     * Analyses the full graph and returns an overall wildfire risk description.
     * Delegates terrain counting to countTerrain() which uses a HashMap internally.
     */
    public String classify(Node[][] graph) {
        if (graph == null || graph.length == 0) return "Unknown risk";

        Map<Terrain, Integer> counts = countTerrain(graph);

        int total    = 0;
        for (Terrain t : Terrain.values()) {
            Integer c = counts.get(t);
            if (c != null) total += c;
        }
        if (total == 0) return "Unknown risk";

        Integer highRiskVal = counts.get(Terrain.DRY_VEGETATION);
        Integer grassVal    = counts.get(Terrain.GRASSLAND);
        Integer barrenVal   = counts.get(Terrain.BARREN);
        Integer waterVal    = counts.get(Terrain.WATER);

        int highRisk = highRiskVal != null ? highRiskVal : 0;
        int medRisk  = (grassVal  != null ? grassVal  : 0)
                     + (barrenVal != null ? barrenVal : 0);
        int blocked  = waterVal   != null ? waterVal  : 0;

        double highPct  = (double) highRisk / total;
        double medPct   = (double) medRisk  / total;
        double waterPct = (double) blocked  / total;

        if (highPct >= EXTREME_RISK_THRESHOLD) return "EXTREME risk — large dry-vegetation coverage";
        if (highPct >= HIGH_RISK_THRESHOLD) return "HIGH risk — significant dry-vegetation present";
        if (medPct  >= MODERATE_RISK_MIN_MED && waterPct < MODERATE_RISK_MAX_WATER) return "MODERATE risk — open grassland/barren dominant";
        if (waterPct >= LOW_RISK_WATER_THRESHOLD) return "LOW risk — extensive water barriers present";
        return "LOW-MODERATE risk — mixed terrain";
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