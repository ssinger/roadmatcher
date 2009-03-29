package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Icon;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;

public class SimpleAdjustEndpointTool extends DragTool {
	public static final Color COLOUR = new Color(194, 179, 205);

	public static final Stroke STROKE = new BasicStroke(5,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

	private SimpleAdjustmentMethod adjustmentMethod;

	public SimpleAdjustEndpointTool(SimpleAdjustmentMethod adjustmentMethod) {
		setColor(COLOUR);
		setStroke(STROKE);
		allowSnapping();
		getSnapManager().addPolicies(Collections.singleton(snapPolicy()));
		this.adjustmentMethod = adjustmentMethod;
	}

	public void activate(final LayerViewPanel layerViewPanel) {
		super.activate(layerViewPanel);
		layerViewPanel.getRenderingManager().putAboveLayerables(
				SPECIFIED_ROAD_SEGMENT_KEY, new Renderer.Factory() {
					public Renderer create() {
						return SimpleAdjustEndpointTool.this
								.createSpecifiedRoadSegmentRenderer(layerViewPanel);
					}
				});
	}

	protected Renderer createSpecifiedRoadSegmentRenderer(
			final LayerViewPanel layerViewPanel) {
		return new SimpleRenderer(SPECIFIED_ROAD_SEGMENT_KEY, layerViewPanel) {
			protected void paint(Graphics2D g) throws Exception {
				if (roadSegment == null) {
					return;
				}
				//StyleUtil#paint takes care of intersecting
				// the geometry with the
				//viewport envelope. [Jon Aquino 12/16/2003]
				StyleUtil.paint(roadSegment.getApparentLine(), g,
						layerViewPanel.getViewport(), false, null, null, true,
						lineStroke, lineColor);
				paintHandle(roadSegment.getApparentStartCoordinate(), g);
				paintHandle(roadSegment.getApparentEndCoordinate(), g);
			}

			private void paintHandle(Coordinate c, Graphics2D g)
					throws NoninvertibleTransformException {
				if (!layerViewPanel.getViewport()
						.getEnvelopeInModelCoordinates().contains(c)) {
					return;
				}
				Point2D p = layerViewPanel.getViewport().toViewPoint(c);
				g.setColor(Color.white);
				g.fillRect((int) (p.getX() - handleWidth / 2),
						(int) (p.getY() - handleWidth / 2), handleWidth,
						handleWidth);
				g.setStroke(handleStroke);
				g.setColor(Color.black);
				g.drawRect((int) (p.getX() - handleWidth / 2),
						(int) (p.getY() - handleWidth / 2), handleWidth,
						handleWidth);
			}

			Stroke handleStroke = new BasicStroke(1);

			int handleWidth = 6;

			Color lineColor = Color.yellow;

			Stroke lineStroke = new BasicStroke(3);
		};
	}

	public void deactivate() {
		super.deactivate();
		roadSegment = null;
		getPanel().getRenderingManager().render(SPECIFIED_ROAD_SEGMENT_KEY);
	}

	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		if (!gestureFinished(getWorkbench().getContext(), getClass().getName()
				+ " - DO NOT SHOW AGAIN", roadSegment,
				closestEndpointWithinTolerance, getModelDestination())) {
			return;
		}
		roadSegment = null;
		getPanel().getRenderingManager().render(SPECIFIED_ROAD_SEGMENT_KEY);
	}

	public Cursor getCursor() {
		throw new UnsupportedOperationException();
	}

	public Icon getIcon() {
		throw new UnsupportedOperationException();
	}

	protected Shape getShape(Point2D source, Point2D destination)
			throws Exception {
		double radius = 20;
		return new Ellipse2D.Double(destination.getX() - (radius / 2),
				destination.getY() - (radius / 2), radius, radius);
	}

	public void mousePressed(final MouseEvent e) {
		try {
			closestEndpointWithinTolerance = AdjustEndpointOperation
					.closestEndpointWithinTolerance(roadSegment,
							toModelCoordinate(e.getPoint()), TOLERANCE
									/ getPanel().getViewport().getScale());
			if (closestEndpointWithinTolerance == null) {
				//No warning necessary -- control will now be transferred to
				//SpecifyRoadToAdjustTool [Jon Aquino 12/15/2003]
				roadSegment = null;
				getPanel().getRenderingManager().render(
						SPECIFIED_ROAD_SEGMENT_KEY);
				return;
			}
			super.mousePressed(e);
		} catch (Throwable t) {
			getPanel().getContext().handleThrowable(t);
		}
	}

	public void setRoadSegment(SourceRoadSegment roadSegment) {
		this.roadSegment = roadSegment;
		getPanel().getRenderingManager().render(SPECIFIED_ROAD_SEGMENT_KEY);
	}

	protected SourceRoadSegment getRoadSegment() {
		return roadSegment;
	}

	private Coordinate toModelCoordinate(Point p)
			throws NoninvertibleTransformException {
		return getPanel().getViewport().toModelCoordinate(p);
	}

	private SnapPolicy snapPolicy() {
		return new SnapPolicy() {
			private ConflationSession conflationSession() {
				return ToolboxModel.instance(getPanel().getLayerManager(),
						getWorkbench().getContext()).getSession();
			}

			public Coordinate snap(LayerViewPanel panel,
					Coordinate originalCoordinate) {
				Envelope envelope = EnvelopeUtil.expand(new Envelope(
						originalCoordinate), TOLERANCE_IN_PIXELS
						/ panel.getViewport().getScale());
				Coordinate closest = null;
				for (Iterator i = FUTURE_CollectionUtil.concatenate(
						conflationSession().getSourceNetwork(0)
								.apparentEndpointsIn(envelope),
						conflationSession().getSourceNetwork(1)
								.apparentEndpointsIn(envelope)).iterator(); i
						.hasNext();) {
					Coordinate candidate = (Coordinate) i.next();
					closest = AdjustEndpointOperation.closest(closest,
							candidate, originalCoordinate);
				}
				return closest != null ? closest : originalCoordinate;
			}

			final int TOLERANCE_IN_PIXELS = 20;
		};
	}

	private boolean gestureFinished(WorkbenchContext context,
			String doNotShowAgainID, SourceRoadSegment roadSegment,
			Coordinate closestEndpointWithinTolerance,
			Coordinate modelDestination) {
		LineString newLine;
		newLine = adjustmentMethod.adjust(roadSegment.getApparentLine(),
				roadSegment, closestEndpointWithinTolerance.equals(roadSegment
						.getApparentStartCoordinate()) ? Terminal.START
						: Terminal.END, modelDestination, context
						.getLayerViewPanel());
		if (!new ConstraintChecker(context.getWorkbench().getFrame())
				.proceedWithAdjusting(roadSegment, newLine, ToolboxModel
						.instance(context).getSession())) {
			return false;
		}
		if (!AdjustEndpointOperation.checkLineSegmentLength(newLine,
				doNotShowAgainID, context)) {
			return false;
		}
		//Don't just compare endpoints -- compare the whole line,
		//because vertices may have been inserted
		//[Jon Aquino 2004-03-22]
		AbstractPlugIn.execute(AdjustEndpointOperation.createUndoableCommand(
				"Adjust Endpoint", roadSegment, roadSegment.getApparentLine(),
				roadSegment.getLine().equalsExact(newLine) ? roadSegment
						.getLine() : newLine, context.getLayerManager(),
				context), context.getLayerViewPanel());
		return true;
	}

	private Coordinate closestEndpointWithinTolerance;

	private SourceRoadSegment roadSegment;

	public static final String SPECIFIED_ROAD_SEGMENT_KEY = SimpleAdjustEndpointTool.class
			.getName()
			+ " - SPECIFIED ROAD SEGMENT";

	public final static int TOLERANCE = 12;

	protected Coordinate getClosestEndpointWithinTolerance() {
		return closestEndpointWithinTolerance;
	}
}