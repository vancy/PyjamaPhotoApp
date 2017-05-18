package operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import application.PhotoPanelItem;

public class SearchCompare {

	public static List<PhotoPanelItem> compareHash(JPanel thumbnailsPanel, PhotoPanelItem compare, int accuracy) {
		List<PhotoPanelItem> result = Collections.synchronizedList(new ArrayList<PhotoPanelItem>());
		long imageHash = 0;

		BufferedImage image = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(compare.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

		for (int y=0; y < accuracy; y++) {
			for (int x=0; x < accuracy; x++) {
				imageHash = image.getRGB(x, y) + (imageHash << 6) + (imageHash << 16) - imageHash;
			}
		}

		for (int i=0; i < thumbnailsPanel.getComponentCount(); i++) {
			PhotoPanelItem pi = (PhotoPanelItem)thumbnailsPanel.getComponent(i);
			long tempHash = 0;

			BufferedImage temp = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = temp.createGraphics();
			g3.drawImage(pi.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

			for (int y=0; y < accuracy; y++) {
				for (int x=0; x < accuracy; x++) {
					tempHash = temp.getRGB(x, y) + (tempHash << 6) + (tempHash << 16) - tempHash;
				}
			}

			if (imageHash == tempHash) {
				result.add(pi);
			}
		}
		return result;
	}

	public static List<PhotoPanelItem> compareHash2(JPanel thumbnailsPanel, PhotoPanelItem compare, int accuracy) {
		List<PhotoPanelItem> result = Collections.synchronizedList(new ArrayList<PhotoPanelItem>());
		long imageHash = 0;

		BufferedImage image = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(compare.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

		for (int y=0; y < accuracy; y++) {
			for (int x=0; x < accuracy; x++) {
				imageHash = image.getRGB(x, y) + (imageHash << 6) + (imageHash << 16) - imageHash;
			}
		}

//		TaskIDGroup group = new TaskIDGroup();

		for (int i=0; i < thumbnailsPanel.getComponentCount(); i++) {
//			TaskID id = compareHash2Task(thumbnailsPanel, result, compare, imageHash, accuracy, i);
//			group.add(id);
		}

//		try {
//			group.waitTillFinished();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		return result;
	}
	
	public static void compareHash2Task(JPanel thumbnailsPanel, List<PhotoPanelItem> result, PhotoPanelItem compare, long imageHash, int accuracy, int i) {
		PhotoPanelItem pi = (PhotoPanelItem)thumbnailsPanel.getComponent(i);
		long tempHash = 0;

		BufferedImage temp = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g3 = temp.createGraphics();
		g3.drawImage(pi.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

		for (int y=0; y < accuracy; y++) {
			for (int x=0; x < accuracy; x++) {
				tempHash = temp.getRGB(x, y) + (tempHash << 6) + (tempHash << 16) - tempHash;
			}
		}

		if (imageHash == tempHash) {
			result.add(pi);
		}
	}


	public static List<PhotoPanelItem> compareColor(JPanel thumbnailsPanel, PhotoPanelItem compare, int sensitivity, int accuracy) {
		List<PhotoPanelItem> result = Collections.synchronizedList(new ArrayList<PhotoPanelItem>());

		BufferedImage image = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(compare.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

		for (int i=0; i < thumbnailsPanel.getComponentCount(); i++) {
			boolean match = true;
			PhotoPanelItem pi = (PhotoPanelItem)thumbnailsPanel.getComponent(i);

			BufferedImage temp = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
			Graphics2D g3 = temp.createGraphics();
			g3.drawImage(pi.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

			for (int y=0; y < accuracy; y++) {
				for (int x=0; x < accuracy; x++) {
					Color tc = new Color(image.getRGB(x, y));
					int tr = tc.getRed();
					int tg = tc.getGreen();
					int tb = tc.getBlue();
		
					Color pc = new Color(temp.getRGB(x, y));
					int pr = pc.getRed();
					int pg = pc.getGreen();
					int pb = pc.getBlue();
		
					int distance = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
					if (distance > sensitivity) {
						match = false;
					}
				}
			}
			if (match) {
				result.add(pi);
			}
		}
		return result;
	}
	
	public static List<PhotoPanelItem> compareColor2(JPanel thumbnailsPanel, PhotoPanelItem compare, int sensitivity, int accuracy) {
		List<PhotoPanelItem> result = Collections.synchronizedList(new ArrayList<PhotoPanelItem>());

		BufferedImage image = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(compare.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

//		TaskIDGroup group = new TaskIDGroup();

//		for (int i=0; i < thumbnailsPanel.getComponentCount(); i++) {
//			TaskID id = compareColor2Task(thumbnailsPanel, result, image, i, sensitivity, accuracy);
//			group.add(id);
//		}

//		try {
//			group.waitTillFinished();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		return result;
	}
	
	public static void compareColor2Task(JPanel thumbnailsPanel, List<PhotoPanelItem> result, BufferedImage image, int i, int sensitivity, int accuracy) {
		boolean match = true;
		PhotoPanelItem pi = (PhotoPanelItem)thumbnailsPanel.getComponent(i);

		BufferedImage temp = new BufferedImage(accuracy, accuracy, BufferedImage.TYPE_INT_RGB);
		Graphics2D g3 = temp.createGraphics();
		g3.drawImage(pi.getSquarePhoto().getScaledInstance(accuracy, accuracy, Image.SCALE_DEFAULT), null, null);

		for (int y=0; y < accuracy; y++) {
			for (int x=0; x < accuracy; x++) {
				Color tc = new Color(image.getRGB(x, y));
				int tr = tc.getRed();
				int tg = tc.getGreen();
				int tb = tc.getBlue();
	
				Color pc = new Color(temp.getRGB(x, y));
				int pr = pc.getRed();
				int pg = pc.getGreen();
				int pb = pc.getBlue();
	
				int distance = (int) Math.sqrt(0.3*(tr - pr)*(tr - pr) +  0.59*(tg - pg)*(tg - pg) +  0.11*(tb - pb)*(tb - pb));
				if (distance > sensitivity) {
					match = false;
				}
			}
		}
		if (match) {
			result.add(pi);
		}
	}
}
