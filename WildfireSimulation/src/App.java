package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage stage) {
		MainUI ui = new MainUI(stage);
		Scene scene = ui.createScene();
		java.net.URL css = App.class.getResource("ui.css");
		if (css != null) {
			scene.getStylesheets().add(css.toExternalForm());
		}
		stage.setTitle("Wildfire Spread Simulation");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
