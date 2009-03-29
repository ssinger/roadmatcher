package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.ResultStateRules;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
public class IntersectionStyle implements Style {
	public static class CircleFactory extends ShapeFactory {
		public Shape create(double x, double y) {
			double extent = 24;
			return new Ellipse2D.Double(x - extent / 2, y - extent / 2, extent,
					extent);
		}
	}
	public static class TriangleFactory extends ShapeFactory {
		public Shape create(double x, double y) {
			double extent = 32;
			double height = Math.sqrt(3 * extent * extent / 4);
			double outerRadius = extent * Math.sin(Angle.toRadians(30))
					/ Math.sin(Angle.toRadians(120));
			GeneralPath path = new GeneralPath();
			path.moveTo((float) x, (float) (y - outerRadius));
			path.lineTo((float) (x + extent / 2),
					(float) (y + height - outerRadius));
			path.lineTo((float) (x - extent / 2),
					(float) (y + height - outerRadius));
			path.lineTo((float) x, (float) (y - outerRadius));
			return path;
		}
	}
	public static class XFactory extends ShapeFactory {
		public Shape create(double x, double y) {
			double extent = 24;
			GeneralPath path = new GeneralPath();
			path.moveTo((float) (x - extent / 2), (float) (y - extent / 2));
			path.lineTo((float) (x + extent / 2), (float) (y + extent / 2));
			path.moveTo((float) (x - extent / 2), (float) (y + extent / 2));
			path.lineTo((float) (x + extent / 2), (float) (y - extent / 2));
			return path;
		}
	}
	private static abstract class ShapeFactory {
		public boolean equals(Object obj) {
			return getClass() == obj.getClass();
		}
		public String toString() {
			//Appears in combobox [Jon Aquino 2004-03-12]
			return StringUtil.toFriendlyName(getClass().getName(), "Factory")
					+ "s";
		}
		public abstract Shape create(double x, double y);
	}
	public static class SquareFactory extends ShapeFactory {
		public Shape create(double x, double y) {
			double extent = 24;
			return new Rectangle2D.Double(x - extent / 2, y - extent / 2,
					extent, extent);
		}
	}
	public IntersectionStyle(WorkbenchContext context) {
		this.context = context;
	}
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();
			return null;
		}
	}
	public void initialize(Layer layer) {
	}
	public boolean isEnabled() {
		return context.getBlackboard().get(SHOWING_INTERSECTIONS_KEY, true);
	}
	public static final String SHOWING_INTERSECTIONS_KEY = IntersectionStyle.class
			.getName()
			+ " - SHOWING INTERSECTIONS";
	private void paint(Coordinate node, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		g.setColor(HighlightManager.instance(context).getColourScheme()
				.getInconsistentColour());
		g.setStroke(stroke);
		Point2D p = viewport.toViewPoint(node);
		try {
			g
					.draw(((ShapeFactory) (((Class) (ApplicationOptionsPlugIn
							.options(context).get(SHAPE_FACTORY_CLASS_KEY,
							DEFAULT_SHAPE_FACTORY_CLASS))).newInstance()))
							.create(p.getX(), p.getY()));
		} catch (InstantiationException e) {
			Assert.shouldNeverReachHere(e.toString());
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere(e.toString());
		}
	}
	public void paint(Feature f, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		SourceRoadSegment roadSegment = ((SourceFeature) f).getRoadSegment();
		if (roadSegment.getResultState() != ResultState.INCONSISTENT) {
			return;
		}
		if (roadSegment.getResultStateDescription().get(
				ResultStateRules.INTERSECTION_KEY) != null) {
			paint((Coordinate) roadSegment.getResultStateDescription().get(
					ResultStateRules.INTERSECTION_KEY), g, viewport);
		}
	}
	public void setEnabled(boolean enabled) {
		throw new UnsupportedOperationException();
	}
	private WorkbenchContext context;
	private Stroke stroke = new BasicStroke(4);
	public static final Class DEFAULT_SHAPE_FACTORY_CLASS = TriangleFactory.class;
	public static final String SHAPE_FACTORY_CLASS_KEY = IntersectionStyle.class
			.getName()
			+ " - SHAPE FACTORY CLASS";
}
