package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;

public class RoadStyleUtil {

	private RoadStyleUtil() {
	};

	private static RoadStyleUtil instance = new RoadStyleUtil();

	public static RoadStyleUtil instance() {
		return instance;
	}
	
	public WorkbenchContext context(Viewport viewport) {
		return ((WorkbenchFrame) SwingUtilities.getAncestorOfClass(
				WorkbenchFrame.class, viewport.getPanel())).getContext();
	}	

	public void paintInconsistentEndpoint(Coordinate coordinate, Color colour,
			Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		g.setColor(colour);
		Point2D p = viewport.toViewPoint(coordinate);
		double radius = ToolboxModel.INCLUDED_OUTER_LINE_WIDTH;
		g.fill(new Ellipse2D.Double(p.getX() - radius, p.getY() - radius,
				radius * 2, radius * 2));
	}

	/**
	 * @param endpoint
	 *                  the point to swell, or null to swell nothing
	 */
	public void paintNeighbourhood(Coordinate endpoint, Set neighbourhood,
			Color colour, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		Geometry bulb = endpoint != null ? factory.createPoint(endpoint)
				.buffer(12 / viewport.getScale()) : factory
				.createPoint((Coordinate) null);
		StyleUtil.paint(new ConvexHull(factory.createMultiPoint(
				(Coordinate[]) neighbourhood
						.toArray(new Coordinate[neighbourhood.size()])).union(
				bulb)).getConvexHull().buffer(12 / viewport.getScale()), g,
				viewport, false, null, null, true, neighbourhoodStroke, colour);
	}

	private GeometryFactory factory = new GeometryFactory();

	private Stroke neighbourhoodStroke = new BasicStroke(1,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
			new float[]{2, 2}, 0);

}