package application;

import java.awt.image.BufferedImage;

class PaletteItem implements Comparable<PaletteItem> {

	private Integer color;
	private BufferedImage image;

	public PaletteItem(int color, BufferedImage image) {
		this.color = color;
		this.image = image;
	}

	public int compareTo(PaletteItem paint) {
		return color.compareTo(paint.color);
	}

	public int getColor() {
		return color;
	}

	public BufferedImage getImage() {
		return image;
	}
}