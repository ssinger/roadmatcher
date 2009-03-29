package com.vividsolutions.jcs.plugin.conflate.roads.resultconsistency;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.resultconsistency.AdjustedRoadSegmentNodeConsistencyRule;
import com.vividsolutions.jcs.plugin.conflate.roads.AbstractInconsistentEndpointStyle;
import com.vividsolutions.jcs.plugin.conflate.roads.RoadStyleUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle;

public class AdjustedRoadSegmentInconsistentEndpointStyle
		extends
			AbstractInconsistentEndpointStyle {
	private static final int ARROW_STROKE_WIDTH = 1;

	private static final Color ARROW_COLOUR = Color.black;

	public AdjustedRoadSegmentInconsistentEndpointStyle() {
		super(
				AdjustedRoadSegmentNodeConsistencyRule.START_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY,
				AdjustedRoadSegmentNodeConsistencyRule.END_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY);
	}

	protected void paint(Coordinate endpoint,
			Object candidateAdjustmentLocations, Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		RoadStyleUtil.instance().paintInconsistentEndpoint(endpoint,
				getInconsistentColour(viewport), g,
				viewport);
		g.setColor(ARROW_COLOUR);
		g.setStroke(ARROW_STROKE);
		for (Iterator i = ((Set) candidateAdjustmentLocations).iterator(); i
				.hasNext();) {
			Coordinate candidateAdjustmentLocation = (Coordinate) i.next();
			paintArrow(endpoint, candidateAdjustmentLocation, g, viewport);
		}
	}

	private class ArrowPainter extends ArrowLineStringEndpointStyle {
		private static final String DUMMY_FILE = "Object.gif";

		public ArrowPainter() {
			super(null, false, DUMMY_FILE, 30, 6, false);
			Layer dummyLayer = new Layer("DUMMY", Color.black,
					AddNewLayerPlugIn.createBlankFeatureCollection(),
					new LayerManager());
			dummyLayer.getBasicStyle().setFillColor(ARROW_COLOUR);
			dummyLayer.getBasicStyle().setLineColor(ARROW_COLOUR);
			dummyLayer.getBasicStyle().setLineWidth(ARROW_STROKE_WIDTH);
			initialize(dummyLayer);
		}

		public void paint(Point2D terminal, Point2D next, Viewport viewport,
				Graphics2D graphics) throws NoninvertibleTransformException {
			super.paint(terminal, next, viewport, graphics);
		}
	}

	private ArrowPainter arrowPainter = new ArrowPainter();

	private static final Stroke ARROW_STROKE = new BasicStroke(
			ARROW_STROKE_WIDTH);

	private void paintArrow(Coordinate start, Coordinate end, Graphics2D g,
			Viewport viewport) throws NoninvertibleTransformException {
		Coordinate viewStart = CoordUtil.toCoordinate(viewport
				.toViewPoint(start));
		Coordinate viewEnd = CoordUtil.toCoordinate(viewport.toViewPoint(end));
		Coordinate unitVector = CoordUtil.divide(CoordUtil.subtract(viewEnd,
				viewStart), viewEnd.distance(viewStart));
		Point2D arrowViewStart = CoordUtil.toPoint2D(viewStart
				.distance(viewEnd) < 32 ? viewStart : CoordUtil.add(viewStart,
				CoordUtil.multiply(8, unitVector)));
		Point2D arrowViewEnd = CoordUtil
				.toPoint2D(viewStart.distance(viewEnd) < 32
						? viewEnd
						: CoordUtil.add(viewStart, CoordUtil.multiply(24,
								unitVector)));
		g.drawLine((int) arrowViewStart.getX(), (int) arrowViewStart.getY(),
				(int) arrowViewEnd.getX(), (int) arrowViewEnd.getY());
		arrowPainter.paint(arrowViewEnd, arrowViewStart, viewport, g);
	}

}