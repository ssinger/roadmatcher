package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Map;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_DelegatingStyle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public abstract class SplitNodeStyle implements Style {
	public static class HALO extends SplitNodeStyle {
		protected void paint(Coordinate coordinate, double angle, Graphics2D g,
				Viewport viewport) throws NoninvertibleTransformException {
			g.setColor(Color.yellow);
			Point2D p = viewport.toViewPoint(coordinate);
			double radius = 4;
			g.fill(new Ellipse2D.Double(p.getX() - radius, p.getY() - radius,
					radius * 2, radius * 2));
		}
	}

	public static class PARENTHESES extends SplitNodeStyle {
		protected void paint(Coordinate coordinate, double angle, Graphics2D g,
				Viewport viewport) throws NoninvertibleTransformException {
			g.setColor(Color.black);
			Point2D p = viewport.toViewPoint(coordinate);
			int radius = 6;
			g.draw(new Arc2D.Double(p.getX() - radius, p.getY() - radius,
					radius * 2, radius * 2, 45 + Angle.toDegrees(angle), 90,
					Arc2D.OPEN));
			g.draw(new Arc2D.Double(p.getX() - radius, p.getY() - radius,
					radius * 2, radius * 2, 225 + Angle.toDegrees(angle), 90,
					Arc2D.OPEN));
		}
	}

	public static class X extends SplitNodeStyle {
		private ToolboxModel.StyleChooser styleChooser;

		private BasicStyle style;

		public X(ToolboxModel.StyleChooser styleChooser) {
			this.styleChooser = styleChooser;
		}

		public void paint(Feature f, Graphics2D g, Viewport viewport)
				throws NoninvertibleTransformException {
			style = styleChooser.style(((SourceFeature) f).getRoadSegment());
			super.paint(f, g, viewport);
		}

		private Stroke stroke = new BasicStroke();

		protected void paint(Coordinate coordinate, double angle, Graphics2D g,
				Viewport viewport) throws NoninvertibleTransformException {
			g.setStroke(stroke);
			g.setColor(GUIUtil.alphaColor(style.getLineColor(), style
					.getAlpha()));
			Point2D p = viewport.toViewPoint(coordinate);
			double r1 = 4;
			double r2 = 6;
			g.rotate(-angle, p.getX(), p.getY());
			try {
				g.draw(new Line2D.Double(p.getX() + r1, p.getY() + r1, p.getX()
						+ r2, p.getY() + r2));
				g.draw(new Line2D.Double(p.getX() - r1, p.getY() - r1, p.getX()
						- r2, p.getY() - r2));
				g.draw(new Line2D.Double(p.getX() + r1, p.getY() - r1, p.getX()
						+ r2, p.getY() - r2));
				g.draw(new Line2D.Double(p.getX() - r1, p.getY() + r1, p.getX()
						- r2, p.getY() + r2));
			} finally {
				g.rotate(+angle, p.getX(), p.getY());
			}
		}
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
		return enabled;
	}

	protected abstract void paint(Coordinate coordinate, double angle,
			Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException;

	public void paint(Feature f, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		if (!(((SourceFeature) f).getRoadSegment() instanceof SplitRoadSegment)) {
			return;
		}
		SplitRoadSegment roadSegment = (SplitRoadSegment) ((SourceFeature) f)
				.getRoadSegment();
		if (!roadSegment.isSplitAtStart()) {
			//No need to render it twice [Jon Aquino 12/4/2003]
			return;
		}
		paint(roadSegment.getApparentStartCoordinate(), roadSegment
				.getApparentStartAngle(), g, viewport);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled = true;
}