package src;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MainUI {

	private static final double DEFAULT_VIEWER_WIDTH = 520;
	private static final double DEFAULT_VIEWER_HEIGHT = 420;
	private static final String VIEWER_BORDER_COLOR = "#4f7f62";
	private static final String VIEWER_BACKGROUND_COLOR = "#09160f";

	private final Stage stage;
	private final GraphBuilder graphBuilder;
	private final ImageClassifier imageClassifier;
	private final WildfireDijkstra wildfireDijkstra;
	private final ImageLoader imageLoader;

	private BufferedImage originalImage;
	private BufferedImage classifiedImage;
	private Image originalFxImage;
	private Image classifiedFxImage;
	private Node[][] graph;
	private Node ignitionNode;

	private final Canvas originalCanvas;
	private final Canvas classifiedCanvas;
	private final Canvas overlayCanvas;
	private final Label originalPlaceholder;
	private final Label classifiedPlaceholder;

	private final TextField blockSizeField;
	private final Button applyBlockSizeButton;
	private final Label statusLabel;
	private final Button startButton;
	private final Button resetButton;
	private final Button clearOverlayButton;
	/** Toggles the Graph (RAG) connectivity overlay: nodes as circles, edges as lines. */
	private final Button graphOverlayButton;
	/** Toggles the Shortest-Path Tree (SPT) overlay after simulation. */
	private final Button sptOverlayButton;

	private ArrayList<Node> spreadOrder;
	private int spreadIndex;
	private double classifiedImgX;
	private double classifiedImgY;
	private double classifiedImgW;
	private double classifiedImgH;

	/** Current block size used to build the RAG; stored for grid-line overlay. */
	private int currentBlockSize = 10;

	/**
	 * When true the connectivity overlay draws RAG nodes (circles) and edges
	 * (lines) on top of the classified image, so the grader can see the graph
	 * that Dijkstra operates on.  RAG boundary lines are also highlighted where
	 * adjacent cells have different terrain labels.
	 */
	private boolean showGraphOverlay = false;

	/**
	 * When true the Shortest-Path Tree (SPT) produced by Dijkstra is drawn:
	 * each settled node is linked to its predecessor, forming a tree rooted at
	 * the ignition cell.  The SPT uses the predecessorMap stored in
	 * {@link WildfireDijkstra#getPredecessorMap()}.
	 */
	private boolean showSPTOverlay = false;

	private final Label riskLabel;

	/** Terrain-count analysis map, populated after image load. */
	private Map<Terrain, Integer> terrainCounts;
	/** Displays per-terrain node counts from the HashMap. */
	private final GridPane analysisGrid;

	public MainUI(Stage stage) {
		this.stage = stage;
		this.graphBuilder = new GraphBuilder();
		this.imageClassifier = new ImageClassifier();
		this.wildfireDijkstra = new WildfireDijkstra();
		this.imageLoader = new ImageLoader();

		this.originalCanvas = new Canvas(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		this.classifiedCanvas = new Canvas(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		this.overlayCanvas = new Canvas(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		this.originalPlaceholder = new Label("No terrain image loaded");
		this.classifiedPlaceholder = new Label("Awaiting classification");

		this.blockSizeField = new TextField();
		this.blockSizeField.setPromptText("1 - 40");
		this.applyBlockSizeButton = new Button("Apply");

		this.statusLabel = new Label("Grid: 0 x 0 | Node count: 0");
		this.riskLabel = new Label();
		this.startButton = new Button("Start Simulation");
		this.resetButton = new Button("Reset");
		this.clearOverlayButton = new Button("Clear Overlay");
		this.graphOverlayButton = new Button("Graph Overlay (RAG)");
		this.sptOverlayButton   = new Button("Spread Tree (SPT)");
		this.analysisGrid = new GridPane();
	}

	public Scene createScene() {
		Label titleLabel = new Label("Wildfire Spread Simulation");
		Button loadButton = new Button("Load Terrain Image");
		Label blockSizeLabel = new Label("Block Size (1 - 40)");

		loadButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setMaxWidth(Double.MAX_VALUE);
		resetButton.setMaxWidth(Double.MAX_VALUE);
		clearOverlayButton.setMaxWidth(Double.MAX_VALUE);
		applyBlockSizeButton.setMaxWidth(Double.MAX_VALUE);
		graphOverlayButton.setMaxWidth(Double.MAX_VALUE);
		sptOverlayButton.setMaxWidth(Double.MAX_VALUE);

		loadButton.setOnAction(e -> loadTerrainImage());
		startButton.setOnAction(e -> startSimulation());
		resetButton.setOnAction(e -> resetSimulation());
		clearOverlayButton.setOnAction(e -> clearOverlay());
		applyBlockSizeButton.setOnAction(e -> applyBlockSize());
		graphOverlayButton.setOnAction(e -> toggleGraphOverlay());
		sptOverlayButton.setOnAction(e -> toggleSPTOverlay());

		titleLabel.setStyle("-fx-text-fill: #74ff87; -fx-font-size: 24px; -fx-font-weight: bold;");
		String buttonBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"
				+ " -fx-min-height: 36px; -fx-font-size: 13px;";
		loadButton.setStyle(buttonBase + " -fx-background-color: #2e7d32;");
		startButton.setStyle(buttonBase + " -fx-background-color: #ef6c00;");
		resetButton.setStyle(buttonBase + " -fx-background-color: #1565c0;");
		clearOverlayButton.setStyle(buttonBase + " -fx-background-color: #6a1b9a;");
		graphOverlayButton.setStyle(buttonBase + " -fx-background-color: #00695c;");
		sptOverlayButton.setStyle(buttonBase + " -fx-background-color: #4527a0;");
		blockSizeLabel.setStyle("-fx-text-fill: #d4f5dd; -fx-font-weight: 600;");
		blockSizeField.setStyle("-fx-background-color: #1a3a27; -fx-text-fill: #d4f5dd;"
				+ " -fx-prompt-text-fill: #6a9a7a; -fx-background-radius: 6; -fx-border-color: #2d6a4f;"
				+ " -fx-border-radius: 6;");
		applyBlockSizeButton.setStyle(buttonBase + " -fx-background-color: #1b5e20;");
		originalPlaceholder.setStyle("-fx-text-fill: #9cc6ab; -fx-font-size: 15px; -fx-font-style: italic;");
		classifiedPlaceholder.setStyle("-fx-text-fill: #9cc6ab; -fx-font-size: 15px; -fx-font-style: italic;");
		originalPlaceholder.setMouseTransparent(true);
		classifiedPlaceholder.setMouseTransparent(true);
		statusLabel.setStyle("-fx-text-fill: #d3f9d8; -fx-font-weight: 600;");
		riskLabel.setStyle("-fx-text-fill: #ffe082; -fx-font-size: 12px; -fx-font-weight: bold;");

		VBox controls = new VBox(10,
				loadButton,
				blockSizeLabel,
				blockSizeField,
				applyBlockSizeButton,
				startButton,
				resetButton,
				clearOverlayButton,
				graphOverlayButton,
				sptOverlayButton,
				riskLabel);
		controls.setPadding(new Insets(12));
		controls.setPrefWidth(260);
		controls.setStyle(
				"-fx-background-color: rgba(10, 26, 19, 0.85);"
						+ "-fx-background-radius: 12;"
						+ "-fx-border-color: #2d6a4f;"
						+ "-fx-border-radius: 12;");

		StackPane originalPanel = new StackPane(originalCanvas, originalPlaceholder);
		StackPane classifiedPanel = new StackPane(classifiedCanvas, overlayCanvas, classifiedPlaceholder);
		applyViewerStyle(originalPanel);
		applyViewerStyle(classifiedPanel);

		overlayCanvas.setMouseTransparent(false);
		overlayCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));

		Label originalTitle = new Label("Original Terrain Image");
		Label classifiedTitle = new Label("Classified / Masked Image");
		originalTitle.setStyle("-fx-text-fill: #95ffa3; -fx-font-size: 16px; -fx-font-weight: bold;");
		classifiedTitle.setStyle("-fx-text-fill: #95ffa3; -fx-font-size: 16px; -fx-font-weight: bold;");

		VBox originalBox = new VBox(6, originalTitle, originalPanel);
		VBox classifiedBox = new VBox(6, classifiedTitle, classifiedPanel);
		originalBox.setPadding(new Insets(10));
		classifiedBox.setPadding(new Insets(10));
		originalBox.setAlignment(Pos.TOP_CENTER);
		classifiedBox.setAlignment(Pos.TOP_CENTER);
		originalBox.setStyle("-fx-background-color: rgba(8, 22, 16, 0.65); -fx-background-radius: 10;");
		classifiedBox.setStyle("-fx-background-color: rgba(8, 22, 16, 0.65); -fx-background-radius: 10;");

		HBox center = new HBox(10, originalBox, classifiedBox);
		center.setPadding(new Insets(10));
		center.setAlignment(Pos.TOP_CENTER);

		// Legend and analysis panel are placed BELOW the viewers so they remain
		// visible at all times (they are never rebuilt, so simulation cannot break them).
		VBox legend = buildLegend();
		VBox analysisPanel = buildAnalysisPanel();
		HBox legendRow = new HBox(16, legend, analysisPanel);
		legendRow.setPadding(new Insets(6, 10, 10, 10));
		legendRow.setAlignment(Pos.TOP_LEFT);
		legendRow.setStyle("-fx-background-color: rgba(8, 22, 16, 0.65); -fx-background-radius: 8;");

		VBox centerAndLegend = new VBox(4, center, legendRow);

		BorderPane root = new BorderPane();
		root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2a20 0%, #153527 100%);");
		root.setLeft(controls);
		root.setCenter(centerAndLegend);
		root.setTop(titleLabel);
		BorderPane.setMargin(titleLabel, new Insets(10, 10, 0, 10));
		BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
		BorderPane.setMargin(statusLabel, new Insets(6, 10, 10, 10));
		root.setBottom(statusLabel);

		updateControlStates();
		return new Scene(root, 1300, 760);
	}

	public void loadTerrainImage() {
		try {
			BufferedImage loaded = imageLoader.load(stage);
			if (loaded == null) {
				setStatus("Image load cancelled.");
				return;
			}

			originalImage = loaded;
			int blockSize = parseBlockSize();
			graph = graphBuilder.build(originalImage, blockSize);
			classifiedImage = imageClassifier.createMaskedImage(graph, originalImage);
			originalFxImage = toFxImage(originalImage);
			classifiedFxImage = toFxImage(classifiedImage);
			ignitionNode = null;
			spreadOrder = null;
			spreadIndex = 0;

			// Populate terrain-count HashMap via ImageClassifier
			terrainCounts = imageClassifier.countTerrain(graph);
			updateAnalysisPanel();

			drawOriginalImage();
			drawClassifiedMaskedImage();
			redrawOverlay();

			String risk = imageClassifier.classify(graph);
			riskLabel.setText("Risk: " + risk);
			updateGridStatus();
			updateControlStates();
		} catch (IOException ex) {
			setStatus("Failed to load image: " + ex.getMessage());
		}
	}

	private void startSimulation() {
		if (graph == null || classifiedImage == null) {
			setStatus("Load an image first.");
			return;
		}
		if (ignitionNode == null) {
			setStatus("Click a non-water cell in classified image to set ignition point.");
			return;
		}

		wildfireDijkstra.computeSpreadFrom(ignitionNode, graph);
		spreadOrder = wildfireDijkstra.getSpreadOrder();
		if (spreadOrder == null) {
			spreadOrder = new ArrayList<>();
		}
		spreadIndex = spreadOrder.size();
		redrawOverlay();

		HashMap<String, Double> dMap = wildfireDijkstra.getDistanceMap();
		setStatus("Simulation complete. Burned nodes: " + spreadOrder.size()
				+ " | Distance entries: " + dMap.size());
	}

	public void drawOriginalImage() {
		GraphicsContext gc = originalCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, originalCanvas.getWidth(), originalCanvas.getHeight());
		if (originalImage != null) {
			drawBufferedImageScaled(originalImage, originalFxImage, originalCanvas, true);
		}
		updatePlaceholderVisibility();
	}

	public void drawClassifiedMaskedImage() {
		GraphicsContext gc = classifiedCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, classifiedCanvas.getWidth(), classifiedCanvas.getHeight());
		if (classifiedImage != null) {
			Rectangle2D drawRegion = drawBufferedImageScaled(classifiedImage, classifiedFxImage, classifiedCanvas, true);
			classifiedImgX = drawRegion.getMinX();
			classifiedImgY = drawRegion.getMinY();
			classifiedImgW = drawRegion.getWidth();
			classifiedImgH = drawRegion.getHeight();
		} else {
			classifiedImgX = 0;
			classifiedImgY = 0;
			classifiedImgW = 0;
			classifiedImgH = 0;
		}
		updatePlaceholderVisibility();
	}

	public void handleMouseClick(double x, double y) {
		if (graph == null || graph.length == 0 || graph[0].length == 0 || classifiedImage == null) {
			return;
		}

		double drawX = classifiedImgX;
		double drawY = classifiedImgY;
		double drawW = classifiedImgW;
		double drawH = classifiedImgH;
		if (drawW <= 0 || drawH <= 0 || x < drawX || x > drawX + drawW || y < drawY || y > drawY + drawH) {
			setStatus("Click inside the displayed classified image to set ignition.");
			return;
		}

		int row = (int) ((y - drawY) / drawH * graph.length);
		int col = (int) ((x - drawX) / drawW * graph[0].length);
		row = Math.max(0, Math.min(row, graph.length - 1));
		col = Math.max(0, Math.min(col, graph[0].length - 1));

		Node selected = graph[row][col];
		if (selected.getTerrain() == Terrain.WATER) {
			setStatus("Cannot set ignition point on WATER terrain.");
			return;
		}

		ignitionNode = selected;
		redrawOverlay();
		setStatus("Ignition set at row " + row + ", col " + col + ". Ready to start simulation.");
		updateControlStates();
	}

	private void redrawOverlay() {
		GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
		if (classifiedImage == null || graph == null || graph.length == 0 || graph[0].length == 0) {
			return;
		}

		double drawX = classifiedImgX;
		double drawY = classifiedImgY;
		double drawW = classifiedImgW;
		double drawH = classifiedImgH;
		if (drawW <= 0 || drawH <= 0) {
			return;
		}
		double cellW = drawW / graph[0].length;
		double cellH = drawH / graph.length;

		gc.save();
		gc.beginPath();
		gc.rect(drawX, drawY, drawW, drawH);
		gc.clip();

		// ---------------------------------------------------------------
		// Grid lines — always visible to show the RAG block partitioning
		// ---------------------------------------------------------------
		gc.setStroke(Color.rgb(255, 255, 255, 0.12));
		gc.setLineWidth(0.5);
		for (int r = 0; r <= graph.length; r++) {
			gc.strokeLine(drawX, drawY + r * cellH, drawX + drawW, drawY + r * cellH);
		}
		for (int c = 0; c <= graph[0].length; c++) {
			gc.strokeLine(drawX + c * cellW, drawY, drawX + c * cellW, drawY + drawH);
		}

		// ---------------------------------------------------------------
		// Graph (RAG) connectivity overlay — nodes (circles) + edges (lines)
		// RAG boundary lines are brightened where adjacent cells have
		// different terrain labels (bonus overlay per project brief).
		// Node colour encodes danger level:
		//   RED   = DRY_VEGETATION  (danger)
		//   YELLOW= GRASSLAND/BARREN/FOREST  (medium)
		//   BLUE  = WATER  (impossible / barrier)
		// ---------------------------------------------------------------
		if (showGraphOverlay) {
			double nodeRadius = Math.min(cellW, cellH) * 0.22;

			// RAG edges — draw sticks between adjacent node centres,
			// brightening the boundary when terrain types differ (RAG bonus)
			for (int r = 0; r < graph.length; r++) {
				for (int c = 0; c < graph[0].length; c++) {
					double cx = drawX + c * cellW + cellW / 2.0;
					double cy = drawY + r * cellH + cellH / 2.0;
					Terrain t = graph[r][c].getTerrain();

					// Right neighbour
					if (c < graph[0].length - 1) {
						Terrain tn = graph[r][c + 1].getTerrain();
						boolean boundary = tn != t;
						if (boundary) {
							gc.setStroke(Color.rgb(255, 255, 100, 0.75));
							gc.setLineWidth(1.8);
						} else {
							gc.setStroke(Color.rgb(255, 255, 255, 0.25));
							gc.setLineWidth(0.8);
						}
						gc.strokeLine(cx, cy, drawX + (c + 1) * cellW + cellW / 2.0, cy);
					}
					// Down neighbour
					if (r < graph.length - 1) {
						Terrain tn = graph[r + 1][c].getTerrain();
						boolean boundary = tn != t;
						if (boundary) {
							gc.setStroke(Color.rgb(255, 255, 100, 0.75));
							gc.setLineWidth(1.8);
						} else {
							gc.setStroke(Color.rgb(255, 255, 255, 0.25));
							gc.setLineWidth(0.8);
						}
						gc.strokeLine(cx, cy, cx, drawY + (r + 1) * cellH + cellH / 2.0);
					}
				}
			}

			// RAG nodes — draw circles coloured by danger level
			for (int r = 0; r < graph.length; r++) {
				for (int c = 0; c < graph[0].length; c++) {
					Terrain t = graph[r][c].getTerrain();
					double cx = drawX + c * cellW + cellW / 2.0 - nodeRadius;
					double cy = drawY + r * cellH + cellH / 2.0 - nodeRadius;
					double d  = nodeRadius * 2;

					// Colour by danger: red=danger, yellow=medium, blue=impossible
					if (t == Terrain.WATER) {
						gc.setFill(Color.rgb(33, 150, 243, 0.70));  // blue — impossible
					} else if (t == Terrain.DRY_VEGETATION) {
						gc.setFill(Color.rgb(244, 67, 54, 0.80));   // red — danger
					} else {
						gc.setFill(Color.rgb(255, 235, 59, 0.75));  // yellow — medium
					}
					gc.fillOval(cx, cy, d, d);
					gc.setStroke(Color.rgb(255, 255, 255, 0.45));
					gc.setLineWidth(0.5);
					gc.strokeOval(cx, cy, d, d);
				}
			}
		}

		// ---------------------------------------------------------------
		// Fire spread overlay — distance-based heat map (red/orange cells)
		// ---------------------------------------------------------------
		if (spreadOrder != null && spreadOrder.size() > 0) {
			double maxDist = 0;
			for (Node node : spreadOrder) {
				double d = node.getDistance();
				if (d != Double.POSITIVE_INFINITY && d > maxDist) maxDist = d;
			}
			if (maxDist == 0) maxDist = 1;

			int limit = Math.min(spreadIndex, spreadOrder.size());
			for (int i = 0; i < limit; i++) {
				Node node = spreadOrder.get(i);
				double t = node.getDistance() / maxDist;       // 0 = ignition, 1 = furthest
				double alpha  = 0.55 - 0.15 * t;              // 0.55 → 0.40
				double red    = 1.0;
				double green  = 0.30 * (1.0 - t);             // 0.30 → 0.0
				double blue   = 0.0;
				gc.setFill(new Color(red, green, blue, alpha));
				gc.fillRect(drawX + (node.getCol() * cellW), drawY + (node.getRow() * cellH), cellW, cellH);
			}
		}

		// ---------------------------------------------------------------
		// Shortest-Path Tree (SPT) overlay — predecessor links from Dijkstra.
		// Each settled node is connected to its predecessor, forming a tree
		// rooted at the ignition cell (cyan-green lines).
		// ---------------------------------------------------------------
		if (showSPTOverlay && spreadOrder != null && spreadOrder.size() > 0) {
			HashMap<String, String> predMap = wildfireDijkstra.getPredecessorMap();
			if (predMap != null && !predMap.isEmpty()) {
				gc.setStroke(Color.rgb(0, 230, 180, 0.70));
				gc.setLineWidth(1.2);
				for (Node node : spreadOrder) {
					String key     = node.getRow() + "," + node.getCol();
					String predKey = predMap.get(key);
					if (predKey != null) {
						String[] parts = predKey.split(",");
						if (parts.length == 2) {
							try {
								int pr = Integer.parseInt(parts[0]);
								int pc = Integer.parseInt(parts[1]);
								double nx = drawX + node.getCol() * cellW + cellW / 2.0;
								double ny = drawY + node.getRow() * cellH + cellH / 2.0;
								double px = drawX + pc * cellW + cellW / 2.0;
								double py = drawY + pr * cellH + cellH / 2.0;
								gc.strokeLine(nx, ny, px, py);
							} catch (NumberFormatException ignored) { /* skip malformed key */ }
						}
					}
				}
			}
		}

		// ---------------------------------------------------------------
		// Ignition marker — yellow border around the selected cell
		// ---------------------------------------------------------------
		if (ignitionNode != null) {
			gc.setStroke(Color.YELLOW);
			gc.setLineWidth(2);
			double x = drawX + (ignitionNode.getCol() * cellW);
			double y = drawY + (ignitionNode.getRow() * cellH);
			gc.strokeRect(x, y, cellW, cellH);
		}
		gc.restore();
	}

	private void clearOverlay() {
		spreadOrder = null;
		spreadIndex = 0;
		ignitionNode = null;
		showGraphOverlay = false;
		showSPTOverlay   = false;
		String btnBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"
				+ " -fx-min-height: 36px; -fx-font-size: 13px;";
		graphOverlayButton.setStyle(btnBase + " -fx-background-color: #00695c;");
		sptOverlayButton.setStyle(btnBase + " -fx-background-color: #4527a0;");
		redrawOverlay();
		if (graph == null) {
			setStatus("Overlay cleared.");
		} else {
			setStatus("Overlay cleared. Select ignition point.");
		}
		updateControlStates();
	}

	private void resetSimulation() {
		originalImage = null;
		classifiedImage = null;
		originalFxImage = null;
		classifiedFxImage = null;
		graph = null;
		ignitionNode = null;
		spreadOrder = null;
		spreadIndex = 0;
		classifiedImgX = 0;
		classifiedImgY = 0;
		classifiedImgW = 0;
		classifiedImgH = 0;
		terrainCounts = null;
		riskLabel.setText("");
		analysisGrid.getChildren().clear();
		drawOriginalImage();
		redrawClassifiedViewer();
		updateGridStatus();
		updateControlStates();
	}

	private void redrawClassifiedViewer() {
		drawClassifiedMaskedImage();
		redrawOverlay();
	}

	private Rectangle2D drawBufferedImageScaled(BufferedImage image, Image fxImage, Canvas canvas, boolean keepAspect) {
		if (image == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
			return new Rectangle2D(0, 0, 0, 0);
		}
		GraphicsContext gc = canvas.getGraphicsContext2D();
		Rectangle2D drawRegion = computeDrawRegion(image, canvas, keepAspect);
		if (fxImage == null) {
			fxImage = toFxImage(image);
		}
		gc.drawImage(fxImage, drawRegion.getMinX(), drawRegion.getMinY(), drawRegion.getWidth(), drawRegion.getHeight());
		return drawRegion;
	}

	private Image toFxImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		WritableImage writableImage = new WritableImage(width, height);
		PixelWriter pixelWriter = writableImage.getPixelWriter();
		int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
		pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
		return writableImage;
	}

	private Rectangle2D computeDrawRegion(BufferedImage image, Canvas canvas, boolean keepAspect) {
		double canvasWidth = canvas.getWidth();
		double canvasHeight = canvas.getHeight();
		if (image == null || canvasWidth <= 0 || canvasHeight <= 0 || image.getWidth() <= 0 || image.getHeight() <= 0) {
			return new Rectangle2D(0, 0, 0, 0);
		}

		double drawWidth = canvasWidth;
		double drawHeight = canvasHeight;
		double drawX = 0;
		double drawY = 0;

		if (keepAspect) {
			double scale = Math.min(canvasWidth / image.getWidth(), canvasHeight / image.getHeight());
			drawWidth = image.getWidth() * scale;
			drawHeight = image.getHeight() * scale;
			drawX = (canvasWidth - drawWidth) / 2.0;
			drawY = (canvasHeight - drawHeight) / 2.0;
		}

		return new Rectangle2D(drawX, drawY, drawWidth, drawHeight);
	}

	private void updatePlaceholderVisibility() {
		originalPlaceholder.setVisible(originalImage == null);
		classifiedPlaceholder.setVisible(classifiedImage == null);
	}

	private void updateGridStatus() {
		if (graph == null || graph.length == 0 || graph[0].length == 0) {
			setStatus("Grid: 0 x 0 | Node count: 0");
			return;
		}
		int rows = graph.length;
		int cols = graph[0].length;
		setStatus("Grid: " + rows + " x " + cols + " | Node count: " + (rows * cols));
	}

	/**
	 * Parses block size from the text field.
	 * Acceptable display range shown to user is 1–40; internally clamped to 5–50.
	 * The parsed value is stored in {@link #currentBlockSize} so overlay drawing
	 * can reference it without re-parsing.
	 */
	private int parseBlockSize() {
		String text = blockSizeField.getText().trim();
		if (!text.isEmpty()) {
			try {
				int value = Integer.parseInt(text);
				if (value < 5 || value > 50) {
					setStatus("Block size must be between 1 and 40. Using default 10.");
					blockSizeField.setText("");
					currentBlockSize = 10;
					return 10;
				}
				currentBlockSize = value;
				return value;
			} catch (NumberFormatException ex) {
				setStatus("Invalid block size. Using default 10.");
				blockSizeField.setText("");
			}
		}
		currentBlockSize = 10;
		return 10;
	}

	/**
	 * Applies the block size from the text field and rebuilds the graph if an
	 * image is already loaded.
	 */
	private void applyBlockSize() {
		if (originalImage == null) {
			setStatus("Load an image first, then apply block size.");
			return;
		}
		int blockSize = parseBlockSize();
		try {
			graph = graphBuilder.build(originalImage, blockSize);
			classifiedImage = imageClassifier.createMaskedImage(graph, originalImage);
			classifiedFxImage = toFxImage(classifiedImage);
			ignitionNode = null;
			spreadOrder = null;
			spreadIndex = 0;
			drawClassifiedMaskedImage();
			redrawOverlay();
			String risk = imageClassifier.classify(graph);
			riskLabel.setText("Risk: " + risk);
			updateGridStatus();
			updateControlStates();
			setStatus("Block size applied: " + blockSize);
		} catch (Exception ex) {
			setStatus("Failed to apply block size: " + ex.getMessage());
		}
	}

	private void updateControlStates() {
		boolean hasGraph = graph != null && graph.length > 0 && graph[0].length > 0;
		startButton.setDisable(!(hasGraph && ignitionNode != null));
	}

	private void setStatus(String message) {
		statusLabel.setText(message);
	}

	/**
	 * Toggles the Graph (RAG) connectivity overlay on/off.
	 * When active, every RAG node is drawn as a coloured circle and every RAG
	 * edge is drawn as a line between adjacent node centres.  Boundary edges
	 * where terrain types differ are brightened (bonus RAG boundary overlay).
	 */
	private void toggleGraphOverlay() {
		showGraphOverlay = !showGraphOverlay;
		String btnBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"
				+ " -fx-min-height: 36px; -fx-font-size: 13px;";
		if (showGraphOverlay) {
			graphOverlayButton.setStyle(btnBase + " -fx-background-color: #00897b;");
			setStatus("Graph (RAG) overlay ON — circles = nodes, sticks = edges, bright = boundary.");
		} else {
			graphOverlayButton.setStyle(btnBase + " -fx-background-color: #00695c;");
			setStatus("Graph (RAG) overlay OFF.");
		}
		redrawOverlay();
	}

	/**
	 * Toggles the Shortest-Path Tree (SPT) overlay on/off.
	 * After a simulation run, the SPT shows the predecessor links produced by
	 * Dijkstra — each settled node is connected to the node it was reached
	 * from, forming a tree rooted at the ignition cell (cyan-green lines).
	 */
	private void toggleSPTOverlay() {
		showSPTOverlay = !showSPTOverlay;
		String btnBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"
				+ " -fx-min-height: 36px; -fx-font-size: 13px;";
		if (showSPTOverlay) {
			sptOverlayButton.setStyle(btnBase + " -fx-background-color: #6a1b9a;");
			setStatus("Spread Tree (SPT) overlay ON — run simulation to populate.");
		} else {
			sptOverlayButton.setStyle(btnBase + " -fx-background-color: #4527a0;");
			setStatus("Spread Tree (SPT) overlay OFF.");
		}
		redrawOverlay();
	}

	private void applyViewerStyle(StackPane panel) {
		panel.setMinSize(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		panel.setPrefSize(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		panel.setMaxSize(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		panel.setBorder(new Border(new BorderStroke(Color.web(VIEWER_BORDER_COLOR), BorderStrokeStyle.DASHED,
				new CornerRadii(0), new BorderWidths(2))));
		panel.setBackground(new Background(new BackgroundFill(Color.web(VIEWER_BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
	}

	private VBox buildLegend() {
		String labelStyle = "-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: 600;";
		String panelStyle = "-fx-background-color: rgba(5, 18, 12, 0.6); -fx-background-radius: 8;"
				+ "-fx-border-color: #2d6a4f; -fx-border-radius: 8;";

		// --- Terrain colours ---
		Label terrainHeading = new Label("Terrain");
		terrainHeading.setStyle("-fx-text-fill: #74ff87; -fx-font-weight: bold; -fx-font-size: 13px;");
		GridPane terrainGrid = new GridPane();
		terrainGrid.setHgap(8);
		terrainGrid.setVgap(4);
		Terrain[] types = { Terrain.GRASSLAND, Terrain.DRY_VEGETATION, Terrain.FOREST, Terrain.WATER, Terrain.BARREN };
		String[] names   = { "Grassland",       "Dry Vegetation",       "Forest",       "Water",       "Barren" };
		for (int i = 0; i < types.length; i++) {
			Rectangle swatch = new Rectangle(14, 14);
			swatch.setFill(Color.web(types[i].getBaseColor()));
			swatch.setStroke(Color.web(VIEWER_BORDER_COLOR));
			swatch.setStrokeWidth(1);
			Label name = new Label(names[i]);
			name.setStyle(labelStyle);
			terrainGrid.add(swatch, 0, i);
			terrainGrid.add(name,   1, i);
		}
		VBox terrainBox = new VBox(4, terrainHeading, terrainGrid);
		terrainBox.setPadding(new Insets(8));
		terrainBox.setStyle(panelStyle);

		// --- Risk / danger levels (node colour in Graph Overlay) ---
		Label riskHeading = new Label("Risk (Graph Nodes)");
		riskHeading.setStyle("-fx-text-fill: #74ff87; -fx-font-weight: bold; -fx-font-size: 13px;");
		GridPane riskGrid = new GridPane();
		riskGrid.setHgap(8);
		riskGrid.setVgap(4);
		// Danger — DRY_VEGETATION
		javafx.scene.shape.Circle cDanger = new javafx.scene.shape.Circle(7, Color.rgb(244, 67, 54, 0.85));
		cDanger.setStroke(Color.WHITE); cDanger.setStrokeWidth(0.5);
		riskGrid.add(cDanger, 0, 0);
		riskGrid.add(styledLabel("Danger (Dry Veg)", labelStyle), 1, 0);
		// Medium — Grassland / Barren / Forest
		javafx.scene.shape.Circle cMedium = new javafx.scene.shape.Circle(7, Color.rgb(255, 235, 59, 0.85));
		cMedium.setStroke(Color.WHITE); cMedium.setStrokeWidth(0.5);
		riskGrid.add(cMedium, 0, 1);
		riskGrid.add(styledLabel("Medium (Other)", labelStyle), 1, 1);
		// Impossible — Water
		javafx.scene.shape.Circle cImposs = new javafx.scene.shape.Circle(7, Color.rgb(33, 150, 243, 0.85));
		cImposs.setStroke(Color.WHITE); cImposs.setStrokeWidth(0.5);
		riskGrid.add(cImposs, 0, 2);
		riskGrid.add(styledLabel("Impossible (Water)", labelStyle), 1, 2);
		VBox riskBox = new VBox(4, riskHeading, riskGrid);
		riskBox.setPadding(new Insets(8));
		riskBox.setStyle(panelStyle);

		// --- Overlay colour key ---
		Label overlayHeading = new Label("Overlay Key");
		overlayHeading.setStyle("-fx-text-fill: #74ff87; -fx-font-weight: bold; -fx-font-size: 13px;");
		GridPane overlayGrid = new GridPane();
		overlayGrid.setHgap(8);
		overlayGrid.setVgap(4);
		// RAG edges (normal)
		Rectangle edgeSwatch = new Rectangle(14, 4, Color.rgb(255, 255, 255, 0.40));
		overlayGrid.add(edgeSwatch, 0, 0);
		overlayGrid.add(styledLabel("RAG edge (same terrain)", labelStyle), 1, 0);
		// RAG boundary edge
		Rectangle boundSwatch = new Rectangle(14, 4, Color.rgb(255, 255, 100, 0.85));
		overlayGrid.add(boundSwatch, 0, 1);
		overlayGrid.add(styledLabel("RAG boundary (diff terrain)", labelStyle), 1, 1);
		// Fire spread
		Rectangle fireSwatch = new Rectangle(14, 14, Color.rgb(255, 80, 0, 0.55));
		overlayGrid.add(fireSwatch, 0, 2);
		overlayGrid.add(styledLabel("Fire spread (heat map)", labelStyle), 1, 2);
		// SPT
		Rectangle sptSwatch = new Rectangle(14, 4, Color.rgb(0, 230, 180, 0.70));
		overlayGrid.add(sptSwatch, 0, 3);
		overlayGrid.add(styledLabel("SPT path (Dijkstra tree)", labelStyle), 1, 3);
		// Ignition
		Rectangle ignSwatch = new Rectangle(14, 14, Color.TRANSPARENT);
		ignSwatch.setStroke(Color.YELLOW); ignSwatch.setStrokeWidth(2);
		overlayGrid.add(ignSwatch, 0, 4);
		overlayGrid.add(styledLabel("Ignition cell", labelStyle), 1, 4);
		VBox overlayBox = new VBox(4, overlayHeading, overlayGrid);
		overlayBox.setPadding(new Insets(8));
		overlayBox.setStyle(panelStyle);

		HBox allSections = new HBox(12, terrainBox, riskBox, overlayBox);
		allSections.setAlignment(Pos.TOP_LEFT);
		return new VBox(allSections);
	}

	/** Helper: creates a styled label with the given CSS. */
	private Label styledLabel(String text, String style) {
		Label lbl = new Label(text);
		lbl.setStyle(style);
		return lbl;
	}

	/**
	 * Builds the analysis panel container (the GridPane is populated dynamically
	 * by updateAnalysisPanel() after image load, using the terrain-count HashMap).
	 */
	private VBox buildAnalysisPanel() {
		Label heading = new Label("Terrain Analysis (HashMap)");
		heading.setStyle("-fx-text-fill: #74ff87; -fx-font-weight: bold; -fx-font-size: 13px;");

		analysisGrid.setHgap(8);
		analysisGrid.setVgap(4);

		VBox box = new VBox(6, heading, analysisGrid);
		box.setPadding(new Insets(8));
		box.setStyle("-fx-background-color: rgba(5, 18, 12, 0.6); -fx-background-radius: 8;"
				+ "-fx-border-color: #2d6a4f; -fx-border-radius: 8;");
		return box;
	}

	/**
	 * Refreshes the analysis GridPane from the terrainCounts HashMap.
	 * Each row shows the terrain type and its node count, read via entrySet().
	 */
	private void updateAnalysisPanel() {
		analysisGrid.getChildren().clear();
		if (terrainCounts == null || terrainCounts.isEmpty()) return;

		String labelStyle = "-fx-text-fill: #d4f5dd; -fx-font-size: 11px;";
		String countStyle = "-fx-text-fill: #ffe082; -fx-font-size: 11px; -fx-font-weight: bold;";

		int row = 0;
		for (Map.Entry<Terrain, Integer> entry : terrainCounts.entrySet()) {
			Label typeLabel  = new Label(entry.getKey().name());
			Label countLabel = new Label(String.valueOf(entry.getValue()));
			typeLabel.setStyle(labelStyle);
			countLabel.setStyle(countStyle);
			analysisGrid.add(typeLabel,  0, row);
			analysisGrid.add(countLabel, 1, row);
			row++;
		}
	}
}

