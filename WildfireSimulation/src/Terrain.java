package src;

/**
 * Terrain Enum - Represents land cover types with fire spread cost and colours
 * Used for classification and visualization (legend + masked image)
 */
public enum Terrain {
	GRASSLAND(1.0, "#4CAF50", "#81C784"), DRY_VEGETATION(0.8, "#FF9800", "#FF5722"), FOREST(2.5, "#2E7D32", "#1B5E20"),
	WATER(Double.POSITIVE_INFINITY, "#2196F3", "#1976D2"), BARREN(1.2, "#8D6E63", "#5D4037");

	private final double spreadCost;
	private final String baseColor;
	private final String accentColor;

	Terrain(double spreadCost, String baseColor, String accentColor) {
		this.spreadCost = spreadCost;
		this.baseColor = baseColor;
		this.accentColor = accentColor;
	}

	public double getSpreadCost() {
		return spreadCost;
	}

	public String getBaseColor() {
		return baseColor;
	}

	public String getAccentColor() {
		return accentColor;
	}
}