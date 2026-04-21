package src;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainUI {

	private static final double DEFAULT_VIEWER_WIDTH = 520;
	private static final double DEFAULT_VIEWER_HEIGHT = 420;

	private final Stage stage;
	private final GraphBuilder graphBuilder;
	private final ImageClassifier imageClassifier;
	private final WildfireDijkstra wildfireDijkstra;
	private final ImageLoader imageLoader;

	private BufferedImage originalImage;
	private BufferedImage classifiedImage;
	private Node[][] graph;
	private Node ignitionNode;

	private final Canvas originalCanvas;
	private final Canvas classifiedCanvas;
	private final Canvas overlayCanvas;
	private final Label originalPlaceholder;
	private final Label classifiedPlaceholder;

	private final Slider blockSizeSlider;
	private final Slider speedSlider;
	private final Label statusLabel;

	private Timeline fireTimeline;
	private ArrayList<Node> spreadOrder;
	private int spreadIndex;

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

		this.speedSlider = new Slider(1, 60, 20);
		this.speedSlider.setShowTickMarks(true);
		this.speedSlider.setShowTickLabels(true);
		this.speedSlider.setMajorTickUnit(10);
		this.speedSlider.setMinorTickCount(4);

		this.statusLabel = new Label("Load an image to start.");
	}

	public Scene createScene() {
		Label titleLabel = new Label("Wildfire Spread Simulation");
		Button loadButton = new Button("Load Terrain Image");
		Button startButton = new Button("Start Simulation");
		Button resetButton = new Button("Reset");
		Button clearOverlayButton = new Button("Clear Overlay");

		loadButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setMaxWidth(Double.MAX_VALUE);
		resetButton.setMaxWidth(Double.MAX_VALUE);
		clearOverlayButton.setMaxWidth(Double.MAX_VALUE);

		loadButton.setOnAction(e -> loadTerrainImage());
		startButton.setOnAction(e -> startSimulation());
		resetButton.setOnAction(e -> resetSimulation());
		clearOverlayButton.setOnAction(e -> clearOverlay());

		titleLabel.getStyleClass().add("app-title");
		loadButton.getStyleClass().addAll("action-button", "btn-load");
		startButton.getStyleClass().addAll("action-button", "btn-start");
		resetButton.getStyleClass().addAll("action-button", "btn-reset");
		clearOverlayButton.getStyleClass().addAll("action-button", "btn-clear");
		blockSizeSlider.getStyleClass().add("slider-block");
		speedSlider.getStyleClass().add("slider-speed");
		originalPlaceholder.getStyleClass().add("viewer-placeholder");
		classifiedPlaceholder.getStyleClass().add("viewer-placeholder");
		originalPlaceholder.setMouseTransparent(true);
		classifiedPlaceholder.setMouseTransparent(true);
		statusLabel.getStyleClass().add("status-label");

		VBox controls = new VBox(10,
				loadButton,
				new Label("Block Size"),
				blockSizeSlider,
				new Label("Animation Speed (nodes/sec)"),
				speedSlider,
				startButton,
				resetButton,
				clearOverlayButton);
		controls.getStyleClass().add("control-panel");
		controls.setPadding(new Insets(12));
		controls.setPrefWidth(260);

		StackPane originalPanel = new StackPane(originalCanvas, originalPlaceholder);
		StackPane classifiedPanel = new StackPane(classifiedCanvas, overlayCanvas, classifiedPlaceholder);
		originalPanel.getStyleClass().add("viewer-pane");
		classifiedPanel.getStyleClass().add("viewer-pane");
		originalPanel.setPrefSize(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		classifiedPanel.setPrefSize(DEFAULT_VIEWER_WIDTH, DEFAULT_VIEWER_HEIGHT);
		VBox.setVgrow(originalPanel, Priority.ALWAYS);
		VBox.setVgrow(classifiedPanel, Priority.ALWAYS);

		originalCanvas.widthProperty().bind(originalPanel.widthProperty());
		originalCanvas.heightProperty().bind(originalPanel.heightProperty());
		classifiedCanvas.widthProperty().bind(classifiedPanel.widthProperty());
		classifiedCanvas.heightProperty().bind(classifiedPanel.heightProperty());
		overlayCanvas.widthProperty().bind(classifiedPanel.widthProperty());
		overlayCanvas.heightProperty().bind(classifiedPanel.heightProperty());

		originalPanel.widthProperty().addListener((obs, oldVal, newVal) -> drawOriginalImage());
		originalPanel.heightProperty().addListener((obs, oldVal, newVal) -> drawOriginalImage());
		classifiedPanel.widthProperty().addListener((obs, oldVal, newVal) -> {
			drawClassifiedMaskedImage();
			redrawOverlay();
		});
		classifiedPanel.heightProperty().addListener((obs, oldVal, newVal) -> {
			drawClassifiedMaskedImage();
			redrawOverlay();
		});

		overlayCanvas.setMouseTransparent(false);
		overlayCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));

		Label originalTitle = new Label("Original Terrain Image");
		Label classifiedTitle = new Label("Classified / Masked Image");
		originalTitle.getStyleClass().add("viewer-title");
		classifiedTitle.getStyleClass().add("viewer-title");

		VBox originalBox = new VBox(6, originalTitle, originalPanel);
		VBox classifiedBox = new VBox(6, classifiedTitle, classifiedPanel);
		originalBox.getStyleClass().add("viewer-box");
		classifiedBox.getStyleClass().add("viewer-box");
		originalBox.setPadding(new Insets(10));
		classifiedBox.setPadding(new Insets(10));
		originalBox.setAlignment(Pos.TOP_CENTER);
		classifiedBox.setAlignment(Pos.TOP_CENTER);
		VBox.setVgrow(originalBox, Priority.ALWAYS);
		VBox.setVgrow(classifiedBox, Priority.ALWAYS);

		HBox center = new HBox(10, originalBox, classifiedBox);
		center.setPadding(new Insets(10));
		HBox.setHgrow(originalBox, Priority.ALWAYS);
		HBox.setHgrow(classifiedBox, Priority.ALWAYS);

		BorderPane root = new BorderPane();
		root.getStyleClass().add("app-root");
		root.setLeft(controls);
		root.setCenter(center);
		root.setTop(titleLabel);
		BorderPane.setMargin(titleLabel, new Insets(10, 10, 0, 10));
		BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
		BorderPane.setMargin(statusLabel, new Insets(6, 10, 10, 10));
		root.setBottom(statusLabel);

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
			ignitionNode = null;
			spreadOrder = null;
			spreadIndex = 0;

			drawOriginalImage();
			drawClassifiedMaskedImage();
			redrawOverlay();

			updateGridStatus();
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
		spreadIndex = 0;
		redrawOverlay();
		animateFireSpread(spreadOrder);
	}

	public void drawOriginalImage() {
		GraphicsContext gc = originalCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, originalCanvas.getWidth(), originalCanvas.getHeight());
		if (originalImage != null) {
			drawBufferedImageScaled(originalImage, originalCanvas, true);
		}
		updatePlaceholderVisibility();
	}

	public void drawClassifiedMaskedImage() {
		GraphicsContext gc = classifiedCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, classifiedCanvas.getWidth(), classifiedCanvas.getHeight());
		if (classifiedImage != null) {
			drawBufferedImageScaled(classifiedImage, classifiedCanvas, true);
		}
		updatePlaceholderVisibility();
	}

	public void handleMouseClick(double x, double y) {
		if (graph == null || graph.length == 0 || graph[0].length == 0 || classifiedImage == null) {
			return;
		}

		double[] drawRegion = computeDrawRegion(classifiedImage, overlayCanvas, true);
		double drawX = drawRegion[0];
		double drawY = drawRegion[1];
		double drawW = drawRegion[2];
		double drawH = drawRegion[3];
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
	}

	private void animateFireSpread(ArrayList<Node> order) {
		if (order == null || order.isEmpty()) {
			setStatus("No spread path found.");
			return;
		}

		double speed = speedSlider.getValue();
		Duration frame = Duration.millis(1000.0 / speed);
		fireTimeline = new Timeline(new KeyFrame(frame, e -> {
			if (spreadIndex >= order.size()) {
				stopAnimation();
				setStatus("Simulation complete. Burned nodes: " + order.size());
				return;
			}
			spreadIndex++;
			redrawOverlay();
		}));
		fireTimeline.setCycleCount(Timeline.INDEFINITE);
		fireTimeline.play();
	}

	private void redrawOverlay() {
		GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
		if (classifiedImage == null || graph == null || graph.length == 0 || graph[0].length == 0) {
			return;
		}

		double[] drawRegion = computeDrawRegion(classifiedImage, overlayCanvas, true);
		double drawX = drawRegion[0];
		double drawY = drawRegion[1];
		double drawW = drawRegion[2];
		double drawH = drawRegion[3];
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
		redrawOverlay();
		setStatus("Overlay cleared.");
	}

	private void resetSimulation() {
		stopAnimation();
		ignitionNode = null;
		spreadOrder = null;
		spreadIndex = 0;
		redrawOverlay();
		if (graph != null) {
			updateGridStatus();
		} else {
			setStatus("Simulation reset.");
		}
	}

	private void stopAnimation() {
		if (fireTimeline != null) {
			fireTimeline.stop();
			fireTimeline = null;
		}
	}

	private void drawBufferedImageScaled(BufferedImage image, Canvas canvas, boolean keepAspect) {
		if (image == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
			return;
		}
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double[] drawRegion = computeDrawRegion(image, canvas, keepAspect);
		Image fxImage = SwingFXUtils.toFXImage(image, null);
		gc.drawImage(fxImage, drawRegion[0], drawRegion[1], drawRegion[2], drawRegion[3]);
	}

	private double[] computeDrawRegion(BufferedImage image, Canvas canvas, boolean keepAspect) {
		double canvasWidth = canvas.getWidth();
		double canvasHeight = canvas.getHeight();
		if (image == null || canvasWidth <= 0 || canvasHeight <= 0) {
			return new double[] { 0, 0, 0, 0 };
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

		return new double[] { drawX, drawY, drawWidth, drawHeight };
	}

	private void updatePlaceholderVisibility() {
		originalPlaceholder.setVisible(originalImage == null);
		classifiedPlaceholder.setVisible(classifiedImage == null);
	}

	private void updateGridStatus() {
		if (graph == null || graph.length == 0 || graph[0].length == 0) {
			setStatus("No graph loaded.");
			return;
		}
		int rows = graph.length;
		int cols = graph[0].length;
		setStatus("Grid: " + rows + " x " + cols + " | Node count: " + (rows * cols));
	}

	private void setStatus(String message) {
		statusLabel.setText(message);
	}
}
