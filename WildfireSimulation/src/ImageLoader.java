package src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ImageLoader {

	public BufferedImage load(Window owner) throws IOException {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Terrain Image");
		chooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
		File file = chooser.showOpenDialog(owner);
		if (file == null) {
			return null;
		}
		return ImageIO.read(file);
	}
}
