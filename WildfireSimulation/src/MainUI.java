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

		this.originalCanvas = new Canvas(500, 400);
		this.classifiedCanvas = new Canvas(500, 400);
		this.overlayCanvas = new Canvas(500, 400);

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

		VBox controls = new VBox(10,
				loadButton,
				new Label("Block Size"),
				blockSizeSlider,
				new Label("Animation Speed (nodes/sec)"),
				speedSlider,
				startButton,
				resetButton,
				clearOverlayButton);
		controls.setPadding(new Insets(12));
		controls.setPrefWidth(260);

		StackPane classifiedPanel = new StackPane(classifiedCanvas, overlayCanvas);
		overlayCanvas.setMouseTransparent(false);
		overlayCanvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));

		VBox originalBox = new VBox(6, new Label("Original Terrain Image"), originalCanvas);
		VBox classifiedBox = new VBox(6, new Label("Classified / Masked Image"), classifiedPanel);
		originalBox.setPadding(new Insets(10));
		classifiedBox.setPadding(new Insets(10));
		originalBox.setAlignment(Pos.TOP_CENTER);
		classifiedBox.setAlignment(Pos.TOP_CENTER);

		HBox center = new HBox(10, originalBox, classifiedBox);
		center.setPadding(new Insets(10));
		HBox.setHgrow(originalBox, Priority.ALWAYS);
		HBox.setHgrow(classifiedBox, Priority.ALWAYS);

		BorderPane root = new BorderPane();
		root.setLeft(controls);
		root.setCenter(center);
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

			resizeCanvases(originalImage.getWidth(), originalImage.getHeight());
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
		if (originalImage == null) {
			return;
		}
		GraphicsContext gc = originalCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, originalCanvas.getWidth(), originalCanvas.getHeight());
		Image fxImage = SwingFXUtils.toFXImage(originalImage, null);
		gc.drawImage(fxImage, 0, 0, originalCanvas.getWidth(), originalCanvas.getHeight());
	}

	public void drawClassifiedMaskedImage() {
		if (classifiedImage == null) {
			return;
		}
		GraphicsContext gc = classifiedCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, classifiedCanvas.getWidth(), classifiedCanvas.getHeight());
		Image fxImage = SwingFXUtils.toFXImage(classifiedImage, null);
		gc.drawImage(fxImage, 0, 0, classifiedCanvas.getWidth(), classifiedCanvas.getHeight());
	}

	public void handleMouseClick(double x, double y) {
		if (graph == null || graph.length == 0 || graph[0].length == 0) {
			return;
		}

		int row = (int) (y / overlayCanvas.getHeight() * graph.length);
		int col = (int) (x / overlayCanvas.getWidth() * graph[0].length);
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
		if (graph == null || graph.length == 0 || graph[0].length == 0) {
			return;
		}

		double cellW = overlayCanvas.getWidth() / graph[0].length;
		double cellH = overlayCanvas.getHeight() / graph.length;

		if (spreadOrder != null) {
			gc.setFill(Color.rgb(255, 69, 0, 0.45));
			int limit = Math.min(spreadIndex, spreadOrder.size());
			for (int i = 0; i < limit; i++) {
				Node node = spreadOrder.get(i);
				gc.fillRect(node.getCol() * cellW, node.getRow() * cellH, cellW, cellH);
			}
		}

		if (ignitionNode != null) {
			gc.setStroke(Color.YELLOW);
			gc.setLineWidth(2);
			double x = ignitionNode.getCol() * cellW;
			double y = ignitionNode.getRow() * cellH;
			gc.strokeRect(x, y, cellW, cellH);
		}
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

	private void resizeCanvases(double width, double height) {
		originalCanvas.setWidth(width);
		originalCanvas.setHeight(height);
		classifiedCanvas.setWidth(width);
		classifiedCanvas.setHeight(height);
		overlayCanvas.setWidth(width);
		overlayCanvas.setHeight(height);
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
