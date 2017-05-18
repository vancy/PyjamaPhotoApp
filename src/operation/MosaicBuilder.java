package operation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import application.ImagePanelItem;
import application.flickr.ImageCombo;

public class MosaicBuilder {

	public static ImageCombo buildMosaicTask(ImagePanelItem panel, int density, int size) {
		return buildMosaic(panel, density, size);
	}

	public static ImageCombo buildMosaic(ImagePanelItem panel, int density, int size) {
		Image imageLarge = panel.getImageLarge();
		int width = imageLarge.getWidth(null)/density;
		if (!(width > 0)) {
			width = 1;
		}
		int height = imageLarge.getHeight(null)/density;
		if (!(height > 0)) {
			height = 1;
		}
		BufferedImage template = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = template.createGraphics();
		g2.drawImage(imageLarge.getScaledInstance(width, height, Image.SCALE_DEFAULT), null, null);
		BufferedImage mosaicLarge = new BufferedImage(width*size, height*size, BufferedImage.TYPE_INT_RGB);

		for (int y=0; y < height*size; y++) {
			for (int x=0; x < width*size; x++) {
				mosaicLarge.setRGB(x, y, template.getRGB(x/size, y/size));
			}
		}

		Image imageSquare = panel.getImageSquare();
		BufferedImage mosaicSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g3 = mosaicSquare.createGraphics();
		g3.drawImage(mosaicLarge.getScaledInstance(imageSquare.getWidth(null), imageSquare.getHeight(null), Image.SCALE_DEFAULT), null, null);

		Image imageMedium = panel.getImageMedium();
		BufferedImage mosaicMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g4 = mosaicMedium.createGraphics();
		g4.drawImage(mosaicLarge.getScaledInstance(imageMedium.getWidth(null), imageMedium.getHeight(null), Image.SCALE_DEFAULT), null, null);

		return new ImageCombo(mosaicLarge, mosaicSquare, mosaicMedium);
	}

// Sequential implementation of Image Mosaic 
	public static ImageCombo buildImageMosaic(ImagePanelItem panel, List<PaletteItem> palette, int density) {
		if (!palette.isEmpty()) {
			Image imageLarge = panel.getImageLarge();
			int width = imageLarge.getWidth(null)/density;
			if (!(width > 0)) {
				width = 1;
			}
			int height = imageLarge.getHeight(null)/density;
			if (!(height > 0)) {
				height = 1;
			}
			int size = palette.get(0).getImage().getWidth(null);

			BufferedImage template = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = template.createGraphics();
			g2.drawImage(imageLarge.getScaledInstance(width, height, Image.SCALE_DEFAULT), null, null);
			BufferedImage mosaicLarge = new BufferedImage(width*size, height*size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = mosaicLarge.createGraphics();

			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					Color tc = new Color(template.getRGB(x, y));
					int tr = tc.getRed();
					int tg = tc.getGreen();
					int tb = tc.getBlue();
					int closest = 0;
					int distance = 512;
					for (int i=0; i < palette.size(); i++) {
						Color pc = new Color(palette.get(i).getColor());
						int pr = pc.getRed();
						int pg = pc.getGreen();
						int pb = pc.getBlue();
						int temp = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
						if (temp == 0) {
							closest = i;
							break;
						} else if (temp < distance) {
							closest = i;
							distance = temp;
						}
					}
					g3.drawImage(palette.get(closest).getImage(), x*size, y*size, null);
				}
			}

			Image imageSquare = panel.getImageSquare();
			BufferedImage mosaicSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g4 = mosaicSquare.createGraphics();
			g4.drawImage(mosaicLarge.getScaledInstance(imageSquare.getWidth(null), imageSquare.getHeight(null), Image.SCALE_DEFAULT), null, null);

			Image imageMedium = panel.getImageMedium();
			BufferedImage mosaicMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g5 = mosaicMedium.createGraphics();
			g5.drawImage(mosaicLarge.getScaledInstance(imageMedium.getWidth(null), imageMedium.getHeight(null), Image.SCALE_DEFAULT), null, null);

			return new ImageCombo(mosaicLarge, mosaicSquare, mosaicMedium);
		} else {
			return new ImageCombo(panel.getImageLarge(), panel.getImageSquare(), panel.getImageMedium());
		}
	}

// Parallel implementation of Image Mosaic split by cell
	public static ImageCombo buildImageMosaic2(ImagePanelItem panel, List<PaletteItem> palette, int density) {
		if (!palette.isEmpty()) {
			Image imageLarge = panel.getImageLarge();
			int width = imageLarge.getWidth(null)/density;
			if (!(width > 0)) {
				width = 1;
			}
			int height = imageLarge.getHeight(null)/density;
			if (!(height > 0)) {
				height = 1;
			}
			int size = palette.get(0).getImage().getWidth(null);

			BufferedImage template = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = template.createGraphics();
			g2.drawImage(imageLarge.getScaledInstance(width, height, Image.SCALE_DEFAULT), null, null);
			BufferedImage mosaicLarge = new BufferedImage(width*size, height*size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = mosaicLarge.createGraphics();

//			TaskIDGroup group = new TaskIDGroup();

//			for (int y=0; y < height; y++) {
//				for (int x=0; x < width; x++) {
//					TaskID id = buildImageMosaic2Task(palette, template, g3, size, x, y);
//					group.add(id);
//				}
//			}
//
//			try {
//				group.waitTillFinished();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			Image imageSquare = panel.getImageSquare();
			BufferedImage mosaicSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g4 = mosaicSquare.createGraphics();
			g4.drawImage(mosaicLarge.getScaledInstance(imageSquare.getWidth(null), imageSquare.getHeight(null), Image.SCALE_DEFAULT), null, null);

			Image imageMedium = panel.getImageMedium();
			BufferedImage mosaicMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g5 = mosaicMedium.createGraphics();
			g5.drawImage(mosaicLarge.getScaledInstance(imageMedium.getWidth(null), imageMedium.getHeight(null), Image.SCALE_DEFAULT), null, null);

			return new ImageCombo(mosaicLarge, mosaicSquare, mosaicMedium);
		} else {
			return new ImageCombo(panel.getImageLarge(), panel.getImageSquare(), panel.getImageMedium());
		}
	}

// Internal task for parallel implementation of Image Mosaic split by cell
	private static void buildImageMosaic2Task(List<PaletteItem> palette, BufferedImage template, Graphics2D g3, int size, int x, int y) {
		Color tc = new Color(template.getRGB(x, y));
		int tr = tc.getRed();
		int tg = tc.getGreen();
		int tb = tc.getBlue();
		int closest = 0;
		int distance = 512;
		for (int i=0; i < palette.size(); i++) {
			Color pc = new Color(palette.get(i).getColor());
			int pr = pc.getRed();
			int pg = pc.getGreen();
			int pb = pc.getBlue();
			int temp = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
			if (temp == 0) {
				closest = i;
				break;
			} else if (temp < distance) {
				closest = i;
				distance = temp;
			}
		}
		g3.drawImage(palette.get(closest).getImage(), x * size, y * size, null);
	}

// Parallel implementation of Image Mosaic split by row
	public static ImageCombo buildImageMosaic3(ImagePanelItem panel, List<PaletteItem> palette, int density) {
		if (!palette.isEmpty()) {
			Image imageLarge = panel.getImageLarge();
			int width = imageLarge.getWidth(null)/density;
			if (!(width > 0)) {
				width = 1;
			}
			int height = imageLarge.getHeight(null)/density;
			if (!(height > 0)) {
				height = 1;
			}
			int size = palette.get(0).getImage().getWidth(null);

			BufferedImage template = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = template.createGraphics();
			g2.drawImage(imageLarge.getScaledInstance(width, height, Image.SCALE_DEFAULT), null, null);
			BufferedImage mosaicLarge = new BufferedImage(width*size, height*size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = mosaicLarge.createGraphics();

//			TaskIDGroup group = new TaskIDGroup();
//
//			for (int y=0; y < height; y++) {
//				TaskID id = buildImageMosaic3Task(palette, template, g3, size, width, y);
//				group.add(id);
//			}
//
//			try {
//				group.waitTillFinished();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			Image imageSquare = panel.getImageSquare();
			BufferedImage mosaicSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g4 = mosaicSquare.createGraphics();
			g4.drawImage(mosaicLarge.getScaledInstance(imageSquare.getWidth(null), imageSquare.getHeight(null), Image.SCALE_DEFAULT), null, null);

			Image imageMedium = panel.getImageMedium();
			BufferedImage mosaicMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g5 = mosaicMedium.createGraphics();
			g5.drawImage(mosaicLarge.getScaledInstance(imageMedium.getWidth(null), imageMedium.getHeight(null), Image.SCALE_DEFAULT), null, null);

			return new ImageCombo(mosaicLarge, mosaicSquare, mosaicMedium);
		} else {
			return new ImageCombo(panel.getImageLarge(), panel.getImageSquare(), panel.getImageMedium());
		}
	}

// Internal task for parallel implementation of Image Mosaic split by row
	private static void buildImageMosaic3Task(List<PaletteItem> palette, BufferedImage template, Graphics2D g3, int size, int width, int y) {
		for (int x=0; x < width; x++) {
			Color tc = new Color(template.getRGB(x, y));
			int tr = tc.getRed();
			int tg = tc.getGreen();
			int tb = tc.getBlue();
			int closest = 0;
			int distance = 512;
			for (int i=0; i < palette.size(); i++) {
				Color pc = new Color(palette.get(i).getColor());
				int pr = pc.getRed();
				int pg = pc.getGreen();
				int pb = pc.getBlue();
				int temp = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
				if (temp == 0) {
					closest = i;
					break;
				} else if (temp < distance) {
					closest = i;
					distance = temp;
				}
			}
			g3.drawImage(palette.get(closest).getImage(), x * size, y * size, null);
		}
	}

// Parallel implementation of Image Mosaic split by column
	public static ImageCombo buildImageMosaic4(ImagePanelItem panel, List<PaletteItem> palette, int density) {
		if (!palette.isEmpty()) {
			Image imageLarge = panel.getImageLarge();
			int width = imageLarge.getWidth(null)/density;
			if (!(width > 0)) {
				width = 1;
			}
			int height = imageLarge.getHeight(null)/density;
			if (!(height > 0)) {
				height = 1;
			}
			int size = palette.get(0).getImage().getWidth(null);

			BufferedImage template = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = template.createGraphics();
			g2.drawImage(imageLarge.getScaledInstance(width, height, Image.SCALE_DEFAULT), null, null);
			BufferedImage mosaicLarge = new BufferedImage(width*size, height*size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = mosaicLarge.createGraphics();

//			TaskIDGroup group = new TaskIDGroup();
//
//			for (int x=0; x < width; x++) {
//				TaskID id = buildImageMosaic4Task(palette, template, g3, size, height, x);
//				group.add(id);
//			}
//
//			try {
//				group.waitTillFinished();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			Image imageSquare = panel.getImageSquare();
			BufferedImage mosaicSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g4 = mosaicSquare.createGraphics();
			g4.drawImage(mosaicLarge.getScaledInstance(imageSquare.getWidth(null), imageSquare.getHeight(null), Image.SCALE_DEFAULT), null, null);

			Image imageMedium = panel.getImageMedium();
			BufferedImage mosaicMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g5 = mosaicMedium.createGraphics();
			g5.drawImage(mosaicLarge.getScaledInstance(imageMedium.getWidth(null), imageMedium.getHeight(null), Image.SCALE_DEFAULT), null, null);

			return new ImageCombo(mosaicLarge, mosaicSquare, mosaicMedium);
		} else {
			return new ImageCombo(panel.getImageLarge(), panel.getImageSquare(), panel.getImageMedium());
		}
	}

// Internal task for parallel implementation of Image Mosaic split by column
	private static void buildImageMosaic4Task(List<PaletteItem> palette, BufferedImage template, Graphics2D g3, int size, int height, int x) {
		for (int y=0; y < height; y++) {
			Color tc = new Color(template.getRGB(x, y));
			int tr = tc.getRed();
			int tg = tc.getGreen();
			int tb = tc.getBlue();
			int closest = 0;
			int distance = 512;
			for (int i=0; i < palette.size(); i++) {
				Color pc = new Color(palette.get(i).getColor());
				int pr = pc.getRed();
				int pg = pc.getGreen();
				int pb = pc.getBlue();
				int temp = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
				if (temp == 0) {
					closest = i;
					break;
				} else if (temp < distance) {
					closest = i;
					distance = temp;
				}
			}
			g3.drawImage(palette.get(closest).getImage(), x * size, y * size, null);
		}
	}

	public static List<PaletteItem> buildMosaicPaletteItem(ImagePanelItem panel, List<PaletteItem> palette, int size) {
		Image imageLarge = panel.getImageLarge();

		BufferedImage color = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = color.createGraphics();
		g2.drawImage(imageLarge.getScaledInstance(1, 1, Image.SCALE_DEFAULT), null, null);

		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D g3 = image.createGraphics();
		g3.drawImage(imageLarge.getScaledInstance(size, size, Image.SCALE_DEFAULT), null, null);
		
		palette.add(new PaletteItem(color.getRGB(0, 0), image));
		return palette;
	}

}
