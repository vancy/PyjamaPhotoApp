package application;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class ZoomListener implements MouseListener, MouseMotionListener,
		MouseWheelListener {

	private int zoomLevel = 10;
	private int minZoomLevel = 0;
	private int maxZoomLevel = 10;
	private double zoomFactor = 1.2;

	private Point dragStartScreen;
	private Point dragEndScreen;

	private Component targetComponent;
	private AffineTransform transform = new AffineTransform();

	public ZoomListener(Component targetComponent) {
		this.targetComponent = targetComponent;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		dragStartScreen = e.getPoint();
		dragEndScreen = null;
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		moveCamera(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		zoomCamera(e);
	}

	private void moveCamera(MouseEvent e) {
		try {
			dragEndScreen = e.getPoint();
			Point2D.Float dragStart = transformPoint(dragStartScreen);
			Point2D.Float dragEnd = transformPoint(dragEndScreen);
			double dx = dragEnd.getX() - dragStart.getX();
			double dy = dragEnd.getY() - dragStart.getY();
			transform.translate(dx, dy);
			dragStartScreen = dragEndScreen;
			dragEndScreen = null;
			targetComponent.repaint();
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}

	private void zoomCamera(MouseWheelEvent e) {
		try {
			int wheelRotation = e.getWheelRotation();
			Point p = e.getPoint();
			if (wheelRotation > 0) {
				if (zoomLevel < maxZoomLevel) {
					zoomLevel++;
					Point2D p1 = transformPoint(p);
					transform.scale(1 / zoomFactor, 1 / zoomFactor);
					Point2D p2 = transformPoint(p);
					transform.translate(p2.getX() - p1.getX(), p2.getY()
							- p1.getY());
					targetComponent.repaint();
				}
			} else {
				if (zoomLevel > minZoomLevel) {
					zoomLevel--;
					Point2D p1 = transformPoint(p);
					transform.scale(zoomFactor, zoomFactor);
					Point2D p2 = transformPoint(p);
					transform.translate(p2.getX() - p1.getX(), p2.getY()
							- p1.getY());
					targetComponent.repaint();
				}
			}
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}

	private Point2D.Float transformPoint(Point p1)
			throws NoninvertibleTransformException {
		AffineTransform inverse = transform.createInverse();
		Point2D.Float p2 = new Point2D.Float();
		inverse.transform(p1, p2);
		return p2;
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}

	public void calibrateZoom(int x1, int y1, int x2, int y2) {
		transform.scale((double) x2 / x1, (double) y2 / y1);
	}
}
