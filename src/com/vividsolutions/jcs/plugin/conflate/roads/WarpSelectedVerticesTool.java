package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Iterator;
import com.vividsolutions.jcs.algorithm.linearreference.LengthSubstring;
import com.vividsolutions.jcs.algorithm.linearreference.LengthToPoint;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jcs.plugin.conflate.roads.SpecifyRoadFeaturesTool.GestureMode;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;
public class WarpSelectedVerticesTool extends OneDragSimpleAdjustEndpointTool {
	protected static final String SPECIFIED_LINE_SEGMENT_INDEX_KEY = WarpSelectedVerticesTool.class
			.getName()
			+ " - SPECIFIED LINE SEGMENT INDEX";
	public WarpSelectedVerticesTool() {
		super(new WarpLocallyAdjustmentMethod() {
			public LineString adjust(LineString line, SourceRoadSegment segment, Terminal terminal,
					Coordinate newTerminalLocation, LayerViewPanel panel) {
				return warp(line, toMode(terminal), newTerminalLocation,
						specifiedLineSegment(line, panel).getCoordinate(
								terminal == Terminal.START ? 1 : 0));
			}
		});
	}
	private static int nudge(int i, Terminal terminal, LineString line) {
		return i
				+ (i == 0 && terminal == Terminal.START ? 1 : i == line
						.getNumPoints() - 1
						&& terminal == Terminal.END ? -1 : 0);
	}
	private Terminal closestTerminal;
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		if (specifiedRoadSegment == null) {
			return;
		}
		try {
			if (specifiedRoadSegment.getApparentLine().distance(model(e))
					* panel.getViewport().getScale() > 30) {
				return;
			}
			setClosestTerminal(LengthSubstring.getSubstring(
					specifiedRoadSegment.getApparentLine(),
					0,
					LengthToPoint.length(
							specifiedRoadSegment.getApparentLine(),
							getMidpointOfSpecifiedLineSegment())).distance(
					model(e)) < LengthSubstring.getSubstring(
					specifiedRoadSegment.getApparentLine(),
					LengthToPoint.length(
							specifiedRoadSegment.getApparentLine(),
							getMidpointOfSpecifiedLineSegment()),
					specifiedRoadSegment.getApparentLine().getLength())
					.distance(model(e)) ? Terminal.START : Terminal.END);
		} catch (NoninvertibleTransformException e1) {
			//Not critical -- eat it. [Jon Aquino 2004-04-27]
		}
	}
	private void setClosestTerminal(Terminal closestTerminal) {
		if (this.closestTerminal == closestTerminal) {
			return;
		}
		this.closestTerminal = closestTerminal;
		panel.getRenderingManager().render(WarpSelectedVerticesTool.this);
	}
	private Coordinate getMidpointOfSpecifiedLineSegment() {
		return CoordUtil.average(specifiedLineSegment(specifiedRoadSegment
				.getApparentLine(), panel).p0, specifiedLineSegment(
				specifiedRoadSegment.getApparentLine(), panel).p1);
	}
	private static LineSegment specifiedLineSegment(
			LineString specifiedLineString, LayerViewPanel panel) {
		return LineStringSplitter.lineSegments(specifiedLineString)[((Integer) panel
				.getBlackboard().get(SPECIFIED_LINE_SEGMENT_INDEX_KEY))
				.intValue()];
	}
	private Point model(MouseEvent e) throws NoninvertibleTransformException {
		return factory.createPoint(panel.getViewport().toModelCoordinate(
				e.getPoint()));
	}
	private GeometryFactory factory = new GeometryFactory();
	protected CursorTool getNoEndpointsHereTool() {
		if (specifyRoadToAdjustTool == null) {
			specifyRoadToAdjustTool = new SpecifyClosestRoadFeatureTool(true,
					true, null, null, Color.BLACK, getContext(),
					GestureMode.LINE) {
				protected boolean specifiedRoadSegmentSet;
				protected void gestureFinished() throws Exception {
					specifiedRoadSegmentSet = false;
					super.gestureFinished();
					if (!specifiedRoadSegmentSet) {
						//No road segment here, or adjustment constraint
						// violated
						//[Jon Aquino 2004-04-30]
						clearRenderer();
					}
				}
				protected void gestureFinished(SourceFeature feature,
						Layer layer) throws Exception {
					specifiedRoadSegment = feature.getRoadSegment();
					specifiedRoadSegmentSet = true;
					getPanel()
							.getBlackboard()
							.put(
									SPECIFIED_LINE_SEGMENT_INDEX_KEY,
									new Integer(
											specifiedLineSegmentIndex(specifiedRoadSegment)));
					panel.getRenderingManager().render(
							WarpSelectedVerticesTool.this);
				}
				private int specifiedLineSegmentIndex(
						SourceRoadSegment specifiedRoadSegment) {
					return wasClick()
							? specifiedLineSegmentIndexFromClick(getModelDestination())
							: specifiedLineSegmentIndexFromDrag(new LineSegment(
									getModelSource(), getModelDestination()));
				}
				private int specifiedLineSegmentIndexFromClick(Coordinate click) {
					int indexOfClosestLineSegment = -1;
					double closestDistance = -1;
					LineSegment[] lineSegments = LineStringSplitter
							.lineSegments(specifiedRoadSegment
									.getApparentLine());
					for (int i = 0; i < lineSegments.length; i++) {
						if (indexOfClosestLineSegment == -1
								|| lineSegments[i].distance(click) < closestDistance) {
							indexOfClosestLineSegment = i;
							closestDistance = lineSegments[i].distance(click);
						}
					}
					return indexOfClosestLineSegment;
				}
				private int specifiedLineSegmentIndexFromDrag(LineSegment drag) {
					LineSegment[] lineSegments = LineStringSplitter
							.lineSegments(specifiedRoadSegment
									.getApparentLine());
					for (int i = 0; i < lineSegments.length; i++) {
						Coordinate intersection = drag
								.intersection(lineSegments[i]);
						if (intersection != null) {
							return i;
						}
					}
					Assert.shouldNeverReachHere();
					return -1;
				}
			};
		}
		return specifyRoadToAdjustTool;
	}
	private SourceRoadSegment specifiedRoadSegment;
	private CursorTool specifyRoadToAdjustTool;
	private LayerViewPanel panel;
	protected SourceRoadSegment roadSegmentWithClosestEndpoint(
			Coordinate clickModelCoordinate)
			throws NoninvertibleTransformException {
		if (specifiedRoadSegment == null) {
			return null;
		}
		return closestEndpointWithinTolerance(specifiedRoadSegment,
				clickModelCoordinate) != null ? specifiedRoadSegment : null;
	}
	public void activate(LayerViewPanel layerViewPanel) {
		super.activate(layerViewPanel);
		this.panel = layerViewPanel;
		layerViewPanel.getRenderingManager().putAboveLayerables(this,
				new Renderer.Factory() {
					public Renderer create() {
						return createRenderer();
					}
				});
	}
	public void deactivate() {
		super.deactivate();
		clearRenderer();
	}
	private void clearRenderer() {
		specifiedRoadSegment = null;
		panel.getRenderingManager().render(WarpSelectedVerticesTool.this);
	}
	private SimpleRenderer createRenderer() {
		return new SimpleRenderer(WarpSelectedVerticesTool.this,
				WarpSelectedVerticesTool.this.panel) {
			Stroke lineStroke = new BasicStroke(3);
			Stroke handleStroke = new BasicStroke(1);
			Color lineColor = Color.yellow;
			protected void paint(Graphics2D g) throws Exception {
				if (specifiedRoadSegment == null) {
					return;
				}
				//StyleUtil#paint takes care of intersecting the geometry with
				//the viewport envelope. [Jon Aquino 12/16/2003]
				StyleUtil.paint(specifiedRoadSegment.getApparentLine(), g,
						WarpSelectedVerticesTool.this.panel.getViewport(),
						false, null, null, true, lineStroke, lineColor);
				paintHandles(specifiedRoadSegment.getApparentLine(), g);
			}
			private void paintHandles(LineString line, Graphics2D g)
					throws NoninvertibleTransformException {
				boolean paintedLastWarpableVertex = false;
				for (Iterator i = WarpLocallyAdjustmentMethod.toMode(
						closestTerminal).iterator(line); i.hasNext();) {
					Coordinate coordinate = (Coordinate) i.next();
					paintHandle(coordinate, g, 4, handleStroke,
							paintedLastWarpableVertex ? Color.WHITE : Color.RED);
					paintedLastWarpableVertex = paintedLastWarpableVertex
							|| coordinate == specifiedLineSegment(
									specifiedRoadSegment.getApparentLine(),
									WarpSelectedVerticesTool.this.panel).p0
							|| coordinate == specifiedLineSegment(
									specifiedRoadSegment.getApparentLine(),
									WarpSelectedVerticesTool.this.panel).p1;
				}
				paintHandle(LineStringUtil.first(line), g, 6, handleStroke,
						closestTerminal == Terminal.START
								? Color.RED
								: Color.WHITE);
				paintHandle(LineStringUtil.last(line), g, 6, handleStroke,
						closestTerminal == Terminal.END
								? Color.RED
								: Color.WHITE);
			}
		};
	}
	private void paintHandle(Coordinate c, Graphics2D g, int handleWidth,
			Stroke handleStroke, Color color)
			throws NoninvertibleTransformException {
		if (!panel.getViewport().getEnvelopeInModelCoordinates().contains(c)) {
			return;
		}
		Point2D p = panel.getViewport().toViewPoint(c);
		g.setColor(color);
		g.fillRect((int) (p.getX() - handleWidth / 2),
				(int) (p.getY() - handleWidth / 2), handleWidth, handleWidth);
		g.setStroke(handleStroke);
		g.setColor(Color.black);
		g.drawRect((int) (p.getX() - handleWidth / 2),
				(int) (p.getY() - handleWidth / 2), handleWidth, handleWidth);
	}
}