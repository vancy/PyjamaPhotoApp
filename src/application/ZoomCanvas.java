package application;

import java.awt.*;

public class ZoomCanvas extends Canvas {

	private boolean firstRun = true;
	private Image image;
	private ZoomListener zoomListener;

	public ZoomCanvas(Image image) {
		this.image = image;
		this.zoomListener = new ZoomListener(this);
		this.addMouseListener(zoomListener);
		this.addMouseMotionListener(zoomListener);
		this.addMouseWheelListener(zoomListener);
	}

	@Override
	public Dimension getPreferredSize() {
		if ((image.getWidth(null) < 960) && (image.getHeight(null) < 600)) {
			return new Dimension(image.getWidth(null), image.getHeight(null));
		} else {
			if (image.getWidth(null) > 1.6 * image.getHeight(null)) {
				return new Dimension(960, (image.getHeight(null) * 960)
						/ image.getWidth(null));
			} else {
				return new Dimension((image.getWidth(null) * 600)
						/ image.getHeight(null), 600);
			}
		}
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g2 = (Graphics2D) graphics;
		if (firstRun) {
			firstRun = false;
			zoomListener.setTransform(g2.getTransform());
			zoomListener.calibrateZoom(image.getWidth(null), image
					.getHeight(null), getSize().width, getSize().height);
		}
		g2.setTransform(zoomListener.getTransform());
		g2.drawImage(image, null, null);
	}

}
