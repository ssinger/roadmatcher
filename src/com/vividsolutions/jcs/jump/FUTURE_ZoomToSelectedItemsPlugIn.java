package com.vividsolutions.jcs.jump;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import javax.swing.SwingUtilities;
import com.vividsolutions.jcs.plugin.conflate.roads.BlockingTimer;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
public class FUTURE_ZoomToSelectedItemsPlugIn extends ZoomToSelectedItemsPlugIn {
	private static interface Function {
		public double eval(double x);
	}
	private static class LinearFunction implements Function {
		public LinearFunction(double x1, double x2, double y1, double y2) {
			Assert.isTrue(x1 < x2);
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}
		public double eval(double x) {
			return x > x2 ? y2 : x < x1 ? y1
					: (((x - x1) * (y2 - y1) / (x2 - x1)) + y1);
		}
		private double x1;
		private double x2;
		private double y1;
		private double y2;
	}
	private Envelope _envelope(Collection geometries) {
		return ((Envelope) FUTURE_LangUtil.invokePrivateMethod("envelope",
				new ZoomToSelectedItemsPlugIn(),
				ZoomToSelectedItemsPlugIn.class, new Object[]{geometries},
				new Class[]{Collection.class}));
	}
	public void flash(Collection geometries, final LayerViewPanel panel) {
		final GeometryCollection gc = toGeometryCollection(geometries);
		if (!panel.getViewport().getEnvelopeInModelCoordinates().intersects(
				gc.getEnvelopeInternal())) {
			return;
		}
		if (panel.getRenderingManager().getDefaultRendererThreadQueue()
				.getRunningThreads() == 0) {
			//Renderer might not use a thread [Jon Aquino 12/8/2003]
			flashLater(panel, gc);
		} else {
			//Wait until the zoom is complete before executing the flash. [Jon
			// Aquino]
			ThreadQueue.Listener listener = new ThreadQueue.Listener() {
				public void allRunningThreadsFinished() {
					panel.getRenderingManager().getDefaultRendererThreadQueue()
							.remove(this);
					flashLater(panel, gc);
				}
			};
			panel.getRenderingManager().getDefaultRendererThreadQueue().add(
					listener);
		}
	}
	public void zoom(final Collection geometries, final LayerViewPanel panel)
			throws NoninvertibleTransformException {
		zoomWithoutFlashing(geometries, panel);
		flash(geometries, panel);
	}
	private double _zoomBufferAsExtentFraction(Collection geometries) {
		return ((Double) FUTURE_LangUtil.invokePrivateMethod(
				"zoomBufferAsExtentFraction", new ZoomToSelectedItemsPlugIn(),
				ZoomToSelectedItemsPlugIn.class, new Object[]{geometries},
				new Class[]{Collection.class})).doubleValue();
	}
	public void zoomWithoutFlashing(final Collection geometries,
			final LayerViewPanel panel) throws NoninvertibleTransformException {
		if (_envelope(geometries).isNull()) {
			return;
		}
		if (panSufficientlyGreat(EnvelopeUtil.centre(_envelope(geometries)),
				panel)) {
			animatePan(EnvelopeUtil.centre(panel.getViewport()
					.getEnvelopeInModelCoordinates()), EnvelopeUtil
					.centre(_envelope(geometries)), panel);
		}
		Envelope proposedEnvelope = EnvelopeUtil.bufferByFraction(
				_envelope(geometries), _zoomBufferAsExtentFraction(geometries));
		if ((proposedEnvelope.getWidth() > panel.getLayerManager()
				.getEnvelopeOfAllLayers().getWidth())
				|| (proposedEnvelope.getHeight() > panel.getLayerManager()
						.getEnvelopeOfAllLayers().getHeight())) {
			//We've zoomed out farther than we would if we zoomed to all
			// layers.
			//This is too far. Set scale to that of zooming to all layers,
			//and center on the selected features. [Jon Aquino]
			proposedEnvelope = panel.getViewport().fullExtent();
			EnvelopeUtil.translate(proposedEnvelope, CoordUtil.subtract(
					EnvelopeUtil.centre(_envelope(geometries)), EnvelopeUtil
							.centre(proposedEnvelope)));
		}
		panel.getViewport().zoom(proposedEnvelope);
	}
	public static void animatePan(Coordinate start, Coordinate end,
			final LayerViewPanel panel) {
		//todo: check if new envelope is outside old [Jon Aquino 12/9/2003]
		Coordinate panEnd = end;
		if (!newAndOldEnvelopesIntersect(start, end, panel.getViewport()
				.getEnvelopeInModelCoordinates())) {
			panEnd = CoordUtil.add(start, CoordUtil.multiply(
					FUTURE_EnvelopeUtil.maxExtent(panel.getViewport()
							.getEnvelopeInModelCoordinates())
							/ start.distance(end), CoordUtil.subtract(end,
							start)));
		}
		panel.getRenderingManager().setPaintingEnabled(false);
		try {
			final Image imageWithMargins = createImageWithMargins(panel);
			int totalTime = 500;
			final Function f = new LinearFunction(0, totalTime, 0,
					(start.x - panEnd.x) * panel.getViewport().getScale());
			final Function g = new LinearFunction(0, totalTime, 0,
					-(start.y - panEnd.y) * panel.getViewport().getScale());
			final long startTime = System.currentTimeMillis();
			final long endTime = startTime + totalTime;
			new BlockingTimer(10, new BlockingTimer.Listener() {
				public boolean tick() {
					long currentTime = System.currentTimeMillis();
					if (currentTime > endTime) {
						return false;
					}
					panel.getGraphics().drawImage(
							imageWithMargins,
							(int) f.eval(currentTime - startTime)
									- panel.getWidth(),
							(int) g.eval(currentTime - startTime)
									- panel.getHeight(), panel);
					return true;
				}
			}).start();
		} finally {
			panel.getRenderingManager().setPaintingEnabled(true);
		}
	}
	private static Image createImageWithMargins(LayerViewPanel panel) {
		Image image = panel.createBlankPanelImage();
		panel.paint(image.getGraphics());
		Image imageWithMargins = new BufferedImage(panel.getWidth() * 3, panel
				.getHeight() * 3, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) imageWithMargins.getGraphics();
		graphics.setColor(Color.white);
		graphics.fill(new Rectangle2D.Double(0, 0, panel.getWidth() * 3, panel
				.getHeight() * 3));
		graphics.drawImage(image, panel.getWidth(), panel.getHeight(), panel);
		return imageWithMargins;
	}
	private static void flashLater(final LayerViewPanel panel,
			final GeometryCollection gc) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					for (int i = 0; i < 3; i++) {
						panel.flash(FUTURE_StyleUtil._toShape(gc, panel
								.getViewport()), Color.red, new BasicStroke(5,
								BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
								50);
						Thread.sleep(50);
					}
				} catch (NoninvertibleTransformException e) {
					//Not critical. Eat it. [Jon Aquino 12/9/2003]
				} catch (InterruptedException e) {
					//Not critical. Eat it. [Jon Aquino 12/9/2003]
				}
			}
		});
	}
	private static boolean newAndOldEnvelopesIntersect(Coordinate start,
			Coordinate end, Envelope envelope) {
		return Math.abs(start.x - end.x) < envelope.getWidth()
				&& Math.abs(start.y - end.y) < envelope.getHeight();
	}
	public static boolean panSufficientlyGreat(Coordinate destination,
			LayerViewPanel panel) {
		return !reduceByHalf(
				panel.getViewport().getEnvelopeInModelCoordinates()).contains(
				destination);
	}
	private static Envelope reduceByHalf(Envelope envelope) {
		return new Envelope(envelope.getMinX() + envelope.getWidth() / 4,
				envelope.getMaxX() - envelope.getWidth() / 4, envelope
						.getMinY()
						+ envelope.getHeight() / 4, envelope.getMaxY()
						- envelope.getHeight() / 4);
	}
	private static GeometryCollection toGeometryCollection(Collection geometries) {
		return new GeometryFactory()
				.createGeometryCollection((Geometry[]) geometries
						.toArray(new Geometry[]{}));
	}
}