package operation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.io.*;
import javax.imageio.ImageIO;

import application.ImagePanelItem;
import application.MainFrame;
import application.flickr.ImageCombo;
import util.Complex;

import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;

public class ImageManipulation {
	
	public static void timeWaster(int n) {
	    long start = System.currentTimeMillis(); 
		newton(n);		    
		long end = System.currentTimeMillis();
	    System.out.println("Time waster took "+(end-start)+"ms.");
	}
	
    public static ImageCombo invert(ImagePanelItem panel) {
        Image imageLarge = panel.getImageLarge();
        short[] invert = new short[256];
        for (int i = 0; i < 256; i++) invert[i] = (short) (255 - i);
        BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invert), null);
        BufferedImage mBufferedImageLarge = new BufferedImage(imageLarge.getWidth(null), imageLarge.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mBufferedImageLarge.createGraphics();
        g2.drawImage(imageLarge, null, null);
        mBufferedImageLarge = invertOp.filter(mBufferedImageLarge, null);
        
        Image imageSquare = panel.getImageSquare();
        BufferedImage mBufferedImageSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = mBufferedImageSquare.createGraphics();
        g3.drawImage(imageSquare, null, null);
        mBufferedImageSquare = invertOp.filter(mBufferedImageSquare, null);
        
        Image imageMedium = panel.getImageMedium();
        BufferedImage mBufferedImageMedium = new BufferedImage(imageMedium.getWidth(null), imageMedium.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g4 = mBufferedImageMedium.createGraphics();
        g4.drawImage(imageMedium, null, null);
        mBufferedImageMedium = invertOp.filter(mBufferedImageMedium, null);
        
        ImageCombo combo = new ImageCombo(mBufferedImageLarge, mBufferedImageSquare, mBufferedImageMedium);
        
        timeWaster(MainFrame.timeWasterSize);
        return combo;
    }
	
    public static ImageCombo edgeDetect(ImagePanelItem panel) {
        Image imageLarge = panel.getImageLarge();
        float[] kernel = { 0.0f, -1.0f, 0.0f, -1.0f, 4.0f, -1.0f, 0.0f, -1.0f, 0.0f };
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, kernel));
        BufferedImage mBufferedImageLarge = new BufferedImage(imageLarge.getWidth(null), imageLarge.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mBufferedImageLarge.createGraphics();
        g2.drawImage(imageLarge, null, null);
        mBufferedImageLarge = op.filter(mBufferedImageLarge, null);
        
        Image imageSquare = panel.getImageSquare();
        BufferedImage mBufferedImageSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = mBufferedImageSquare.createGraphics();
        g3.drawImage(imageSquare, null, null);
        mBufferedImageSquare = op.filter(mBufferedImageSquare, null);
        
        Image imageMed = panel.getImageMedium();
        BufferedImage mBufferedImageMed = new BufferedImage(imageMed.getWidth(null), imageMed.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g4 = mBufferedImageMed.createGraphics();
        g4.drawImage(imageMed, null, null);
        mBufferedImageMed = op.filter(mBufferedImageMed, null);
        
        ImageCombo combo = new ImageCombo(mBufferedImageLarge, mBufferedImageSquare,mBufferedImageMed);
        timeWaster(MainFrame.timeWasterSize);
        return combo;
    }
        
    public static Image getMedium(Image image) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        Image im;

    	w = Math.min(ImagePanelItem.dialogWidth,w);
    	h = Math.min(ImagePanelItem.dialogHeight,h);
        
        if (w > h)
        	im = image.getScaledInstance(w, -1, Image.SCALE_SMOOTH);
        else
        	im = image.getScaledInstance(-1, h, Image.SCALE_SMOOTH);

        BufferedImage buf = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        buf.getGraphics().drawImage(im, 0, 0, null);
        return buf;
    }
  
    public static Image getSmallSquare(Image image) {
        Image im = image.getScaledInstance(ImagePanelItem.imageSize, ImagePanelItem.imageSize, Image.SCALE_SMOOTH);
        BufferedImage buf = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        buf.getGraphics().drawImage(im,0,0, null);
        return buf;
    }
    
    public static Image getImageFull(File file) {
    	try {
			return ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
		
    public static ImageCombo sharpen(ImagePanelItem panel) {
        Image imageLarge = panel.getImageLarge();
        float[] sharpKernel = { 0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f };
        BufferedImageOp sharpen = new ConvolveOp(new Kernel(3, 3, sharpKernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage mBufferedImageLarge = new BufferedImage(imageLarge.getWidth(null), imageLarge.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mBufferedImageLarge.createGraphics();
        g2.drawImage(imageLarge, null, null);
        mBufferedImageLarge = sharpen.filter(mBufferedImageLarge, null);
        
        Image imageSquare = panel.getImageSquare();
        BufferedImage mBufferedImageSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = mBufferedImageSquare.createGraphics();
        g3.drawImage(imageSquare, null, null);
        mBufferedImageSquare = sharpen.filter(mBufferedImageSquare, null);
        
        Image imageMed = panel.getImageMedium();
        BufferedImage mBufferedImagemed = new BufferedImage(imageMed.getWidth(null), imageMed.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g4 = mBufferedImagemed.createGraphics();
        g4.drawImage(imageMed, null, null);
        mBufferedImagemed = sharpen.filter(mBufferedImagemed, null);
        ImageCombo combo = new ImageCombo(mBufferedImageLarge, mBufferedImageSquare, mBufferedImagemed);
        timeWaster(MainFrame.timeWasterSize);
        return combo;
    }
	
	public static ImageCombo blur(ImagePanelItem panel) {
        Image imageLarge = panel.getImageLarge();
        float ninth = 1.0f / 9.0f;
        float[] kernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, kernel));
        BufferedImage mBufferedImageLarge = new BufferedImage(imageLarge.getWidth(null), imageLarge.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mBufferedImageLarge.createGraphics();
        g2.drawImage(imageLarge, null, null);
        mBufferedImageLarge = op.filter(mBufferedImageLarge, null);
        
        Image imageSquare = panel.getImageSquare();
        BufferedImage mBufferedImageSquare = new BufferedImage(imageSquare.getWidth(null), imageSquare.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = mBufferedImageSquare.createGraphics();
        g3.drawImage(imageSquare, null, null);
        mBufferedImageSquare = op.filter(mBufferedImageSquare, null);

        Image imageMed = panel.getImageMedium();
        BufferedImage mBufferedImageMed = new BufferedImage(imageMed.getWidth(null), imageMed.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g4 = mBufferedImageMed.createGraphics();
        g4.drawImage(imageMed, null, null);
        mBufferedImageMed = op.filter(mBufferedImageMed, null);
        
        ImageCombo combo = new ImageCombo(mBufferedImageLarge, mBufferedImageSquare, mBufferedImageMed);
        timeWaster(MainFrame.timeWasterSize);
        return combo;
    }

	private static Color newton(Complex z) {
        double EPSILON = 0.00000001;
        Complex four = new Complex(4, 0);
        Complex one = new Complex(1, 0);
        Complex root1 = new Complex(1, 0);
        Complex root2 = new Complex(-1, 0);
        Complex root3 = new Complex(0, 1);
        Complex root4 = new Complex(0, -1);
        for (int i = 0; i < 100; i++) {
            Complex f = z.times(z).times(z).times(z).minus(one);
            Complex fp = four.times(z).times(z).times(z);
            z = z.minus(f.divides(fp));
            if (z.minus(root1).abs() <= EPSILON) return Color.WHITE;
            if (z.minus(root2).abs() <= EPSILON) return Color.RED;
            if (z.minus(root3).abs() <= EPSILON) return Color.GREEN;
            if (z.minus(root4).abs() <= EPSILON) return Color.BLUE;
        }
        return Color.BLACK;
    }

    private static void newton(int N) {
        double xmin = -1.0;
        double ymin = -1.0;
        double width = 2.0;
        double height = 2.0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                double x = xmin + i * width / N;
                double y = ymin + j * height / N;
                Complex z = new Complex(x, y);
                newton(z);
            }
        }
    }

}
