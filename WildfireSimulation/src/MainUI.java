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
import javafx.scene.control.Slider;
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

	private final Slider blockSizeSlider;
	private final Label statusLabel;
	private final Button startButton;
	private final Button resetButton;
	private final Button clearOverlayButton;

	private ArrayList<Node> spreadOrder;
	private int spreadIndex;
	private double classifiedImgX;
	private double classifiedImgY;
	private double classifiedImgW;
	private double classifiedImgH;

	private final Label riskLabel;

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

		this.blockSizeSlider = new Slider(5, 50, 10);
		this.blockSizeSlider.setShowTickMarks(true);
		this.blockSizeSlider.setShowTickLabels(true);
		this.blockSizeSlider.setMajorTickUnit(15);
		this.blockSizeSlider.setMinorTickCount(4);
		this.blockSizeSlider.setSnapToTicks(true);

		this.statusLabel = new Label("Grid: 0 x 0 | Node count: 0");
		this.riskLabel = new Label();
		this.startButton = new Button("Start Simulation");
		this.resetButton = new Button("Reset");
		this.clearOverlayButton = new Button("Clear Overlay");
	}

	public Scene createScene() {
		Label titleLabel = new Label("Wildfire Spread Simulation");
		Button loadButton = new Button("Load Terrain Image");
		Label blockSizeLabel = new Label("Block Size");

		loadButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setMaxWidth(Double.MAX_VALUE);
		resetButton.setMaxWidth(Double.MAX_VALUE);
		clearOverlayButton.setMaxWidth(Double.MAX_VALUE);

		loadButton.setOnAction(e -> loadTerrainImage());
		startButton.setOnAction(e -> startSimulation());
		resetButton.setOnAction(e -> resetSimulation());
		clearOverlayButton.setOnAction(e -> clearOverlay());

		titleLabel.setStyle("-fx-text-fill: #74ff87; -fx-font-size: 24px; -fx-font-weight: bold;");
		String buttonBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;";
		loadButton.setStyle(buttonBase + " -fx-background-color: #2e7d32;");
		startButton.setStyle(buttonBase + " -fx-background-color: #ef6c00;");
		resetButton.setStyle(buttonBase + " -fx-background-color: #1565c0;");
		clearOverlayButton.setStyle(buttonBase + " -fx-background-color: #6a1b9a;");
		blockSizeLabel.setStyle("-fx-text-fill: #d4f5dd; -fx-font-weight: 600;");
		originalPlaceholder.setStyle("-fx-text-fill: #9cc6ab; -fx-font-size: 15px; -fx-font-style: italic;");
		classifiedPlaceholder.setStyle("-fx-text-fill: #9cc6ab; -fx-font-size: 15px; -fx-font-style: italic;");
		originalPlaceholder.setMouseTransparent(true);
		classifiedPlaceholder.setMouseTransparent(true);
		statusLabel.setStyle("-fx-text-fill: #d3f9d8; -fx-font-weight: 600;");
		riskLabel.setStyle("-fx-text-fill: #ffe082; -fx-font-size: 12px; -fx-font-weight: bold;");

		VBox legend = buildLegend();

		VBox controls = new VBox(10,
				loadButton,
				blockSizeLabel,
				blockSizeSlider,
				startButton,
				resetButton,
				clearOverlayButton,
				riskLabel,
				legend);
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

		BorderPane root = new BorderPane();
		root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2a20 0%, #153527 100%);");
		root.setLeft(controls);
		root.setCenter(center);
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

			stopAnimation();

			originalImage = loaded;
			int blockSize = (int) Math.round(blockSizeSlider.getValue());
			graph = graphBuilder.build(originalImage, blockSize);
			classifiedImage = imageClassifier.createMaskedImage(graph, originalImage);
			originalFxImage = toFxImage(originalImage);
			classifiedFxImage = toFxImage(classifiedImage);
			ignitionNode = null;
			spreadOrder = null;
			spreadIndex = 0;

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

		stopAnimation();
		wildfireDijkstra.computeSpreadFrom(ignitionNode, graph);
		spreadOrder = wildfireDijkstra.getSpreadOrder();
		if (spreadOrder == null) {
			spreadOrder = new ArrayList<>();
		}
		spreadIndex = spreadOrder.size();
		redrawOverlay();
		setStatus("Simulation complete. Burned nodes: " + spreadOrder.size());
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

		if (spreadOrder != null) {
			gc.setFill(Color.rgb(255, 69, 0, 0.45));
			int limit = Math.min(spreadIndex, spreadOrder.size());
			for (int i = 0; i < limit; i++) {
				Node node = spreadOrder.get(i);
				gc.fillRect(drawX + (node.getCol() * cellW), drawY + (node.getRow() * cellH), cellW, cellH);
			}
		}

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
		stopAnimation();
		spreadOrder = null;
		spreadIndex = 0;
		ignitionNode = null;
		redrawOverlay();
		if (graph == null) {
			setStatus("Overlay cleared.");
		} else {
			setStatus("Overlay cleared. Select ignition point.");
		}
		updateControlStates();
	}

	private void resetSimulation() {
		stopAnimation();
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
		riskLabel.setText("");
		drawOriginalImage();
		redrawClassifiedViewer();
		updateGridStatus();
		updateControlStates();
	}

	private void stopAnimation() {
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

	private void updateControlStates() {
		boolean hasGraph = graph != null && graph.length > 0 && graph[0].length > 0;
		startButton.setDisable(!(hasGraph && ignitionNode != null));
	}

	private void setStatus(String message) {
		statusLabel.setText(message);
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
		Label heading = new Label("Terrain Legend");
		heading.setStyle("-fx-text-fill: #95ffa3; -fx-font-weight: bold; -fx-font-size: 13px;");

		GridPane grid = new GridPane();
		grid.setHgap(8);
		grid.setVgap(5);

		Terrain[] types = { Terrain.GRASSLAND, Terrain.DRY_VEGETATION, Terrain.FOREST, Terrain.WATER, Terrain.BARREN };
		String[] names = { "Grassland", "Dry Vegetation", "Forest", "Water", "Barren" };

		for (int i = 0; i < types.length; i++) {
			Rectangle swatch = new Rectangle(16, 16);
			String hex = types[i].getBaseColor();
			swatch.setFill(Color.web(hex));
			swatch.setStroke(Color.web(VIEWER_BORDER_COLOR));
			swatch.setStrokeWidth(1);

			Label name = new Label(names[i]);
			name.setStyle("-fx-text-fill: #c8e6c9; -fx-font-size: 11px;");

			grid.add(swatch, 0, i);
			grid.add(name, 1, i);
		}

		VBox box = new VBox(6, heading, grid);
		box.setPadding(new Insets(8));
		box.setStyle("-fx-background-color: rgba(5, 18, 12, 0.6); -fx-background-radius: 8;"
				+ "-fx-border-color: #2d6a4f; -fx-border-radius: 8;");
		return box;
	}
}
