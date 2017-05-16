package application;

import java.awt.Image;

public class ImageCombo {

	private Image imageLarge = null;
	private Image imageSquare = null;
	private Image imageMedium = null;

	public ImageCombo(Image large, Image square, Image medium) {
		this.imageLarge = large;
		this.imageSquare = square;
		this.imageMedium = medium;
	}

	public Image getImageLarge() {
		return imageLarge;
	}

	public Image getImageSquare() {
		return imageSquare;
	}

	public Image getImageMedium() {
		return imageMedium;
	}
}
