package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import com.vividsolutions.jcs.algorithm.linearreference.LengthSubstring;
import com.vividsolutions.jcs.algorithm.linearreference.LengthToPoint;
import com.vividsolutions.jcs.conflate.roads.model.ProperIntersectionFinder;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.jump.FUTURE_Block;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_CompositeUndoableCommand;
import com.vividsolutions.jcs.jump.FUTURE_StyleUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.ShortSegmentException;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

public class ExtendOrClipTool extends DragTool {
	public ExtendOrClipTool() {
		setStroke(SimpleAdjustEndpointTool.STROKE);
		setColor(Color.red);
	}

	private LineString clip(LineString line, double multiplier) {
		return new LengthSubstring(line).getSubstring(0, multiplier
				* line.getLength());
	}

	private Coordinate closestCoordinate(Collection coordinates,
			Coordinate coordinate) {
		return !coordinates.isEmpty() ? CoordUtil.closest(coordinates,
				coordinate) : null;
	}

	private Coordinate closestExternalIntersectionWithinSnapDistance(
			Coordinate coordinate, LineString oldLine, double extension) {
		return ifWithinSnapDistance(closestCoordinate(externalIntersections(
				oldLine, extension), coordinate), coordinate);
	}

	private Coordinate closestInternalIntersectionWithinSnapDistance(
			Coordinate coordinate) {
		return ifWithinSnapDistance(closestCoordinate(internalIntersections,
				coordinate), coordinate);
	}

	private LineString extend(LineString line, double distance) {
		CoordinateList coordinates = new CoordinateList(line.getCoordinates(),
				false);
		coordinates.add(extensionPoint(line, distance));
		return line.getFactory().createLineString(
				coordinates.toCoordinateArray());
	}

	private LineString extension(LineString oldLine, double extension) {
		return new GeometryFactory().createLineString(new Coordinate[] {
				last(oldLine), extensionPoint(oldLine, extension) });
	}

	private Coordinate extensionPoint(LineString line, double distance) {
		return CoordUtil.add(last(line), CoordUtil.multiply(distance, CoordUtil
				.divide(CoordUtil.subtract(last(line), nextToLast(line)), last(
						line).distance(nextToLast(line)))));
	}

	private Collection externalIntersections(LineString oldLine,
			double extension) {
		return intersections(extension(oldLine, extension),
				ProperIntersectionFinder.properlyIntersectingRoadSegments(
						extension(oldLine, extension), myRoadSegment
								.getNetwork().getSession(), FUTURE_Block.TRUE));
	}

	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		try {
			LineSpec newLine = newLine(myRoadSegment, getModelSource(),
					getModelDestination());
			if (!new ConstraintChecker(getWorkbench().getFrame())
					.proceedWithAdjusting(myRoadSegment, newLine.getLine(),
							ToolboxModel.instance(getWorkbench().getContext())
									.getSession())) {
				return;
			}
			if (newLine.getLine().equals(myRoadSegment.getApparentLine())) {
				return;
			}
			if (!AdjustEndpointOperation.checkLineSegmentLength(newLine
					.getLine(), getClass().getName() + " - DO NOT SHOW AGAIN",
					getWorkbench().getContext())) {
				return;
			}
			execute(createUndoableCommand(newLine, myRoadSegment
					.getApparentLine(), ToolboxModel.instance(getPanel()
					.getLayerManager(), getWorkbench().getContext()),
					getPanel().getContext()));
		} catch (ShortSegmentException e) {
			//User has already been warned. Do nothing [Jon Aquino 2004-03-19]
		}
	}

	private UndoableCommand createUndoableCommand(final LineSpec newLine,
			LineString oldLine, ToolboxModel model,
			LayerViewPanelContext layerViewPanelContext)
			throws NoninvertibleTransformException, ShortSegmentException {
		FUTURE_CompositeUndoableCommand command = new FUTURE_CompositeUndoableCommand(
				getName()).add(AdjustEndpointOperation.createUndoableCommand(
				getName(), myRoadSegment, myRoadSegment.getApparentLine(),
				newLine.getLine(), getPanel().getLayerManager(), getWorkbench()
						.getContext()));
		if (!newLine.isSnapped()) {
			return command;
		}
		Assert.isTrue(!newLine.getLine().equals(oldLine));
		Collection candidatesForSplitting = candidatesForSplitting(newLine);
		if (candidatesForSplitting.isEmpty()) {
			return command;
		}
		SourceRoadSegment closestRoadSegment = closestRoadSegment(
				candidatesForSplitting, newLine.getSnapPoint());
		if (!strictlyBetween(LengthToPoint.length(closestRoadSegment
				.getApparentLine(), newLine.getSnapPoint()), 0,
				closestRoadSegment.getApparentLine().getLength())) {
			return command;
		}
		if (closestRoadSegment.isAdjusted()) {
			layerViewPanelContext
					.warnUser(ErrorMessages.extendOrClipTool_adjusted);
			return command;
		}
		if (LineStringSplitter.closestPointIsTerminal(closestRoadSegment
				.getApparentLine(), newLine.getSnapPoint())) {
			return command;
		}
		command.add(CreateSplitNodeOp.createUndoableCommand(
				(SourceFeature) closestRoadSegment.getFeature(), newLine
						.getSnapPoint(), true, false, true, model,
				layerViewPanelContext));
		return command;
	}

	private Collection candidatesForSplitting(final LineSpec newLine) {
		Collection candidatesForSplitting = FUTURE_CollectionUtil.concatenate(
				myRoadSegment.getNetwork().roadSegmentsApparentlyIntersecting(
						buffer(newLine.getSnapPoint())), myRoadSegment
						.getNetwork().getOther()
						.roadSegmentsApparentlyIntersecting(
								buffer(newLine.getSnapPoint())));
		candidatesForSplitting.remove(newLine.getLine());
		return candidatesForSplitting;
	}

	private Envelope buffer(Coordinate coordinate) {
		return EnvelopeUtil.expand(new Envelope(coordinate),
				modelSnapDistance());
	}

	private SourceRoadSegment closestRoadSegment(Collection roadSegments,
			Coordinate target) {
		Point p = myRoadSegment.getApparentLine().getFactory().createPoint(
				target);
		SourceRoadSegment closestRoadSegment = null;
		for (Iterator i = roadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			if (closestRoadSegment == null
					|| roadSegment.getApparentLine().distance(p) < closestRoadSegment
							.getApparentLine().distance(p)) {
				closestRoadSegment = roadSegment;
			}
		}
		return closestRoadSegment;
	}

	private boolean strictlyBetween(double x, double min, double max) {
		return min < x && x < max;
	}

	public Icon getIcon() {
		throw new UnsupportedOperationException();
	}

	protected Shape getShape() throws Exception {
		LineSpec newLine = newLine(myRoadSegment, getModelSource(),
				getModelDestination());
		GeneralPath shape = (GeneralPath) FUTURE_StyleUtil._toShape(newLine
				.getLine(), getPanel().getViewport());
		if (newLine.isSnapped()) {
			shape.append(snapIndicator(getPanel().getViewport().toViewPoint(
					newLine.getSnapPoint())), false);
		}
		return shape;
	}

	private Shape snapIndicator(Point2D p) {
		int radius = 15;
		return new Ellipse2D.Double(p.getX() - radius, p.getY() - radius,
				radius * 2, radius * 2);
	}

	private class LineSpec {
		private LineString line;

		private Coordinate snapPoint;

		public LineSpec(LineString line, Coordinate snapPoint) {
			Assert.isTrue(snapPoint == null
					|| LineStringUtil.first(line).equals(snapPoint)
					|| LineStringUtil.last(line).equals(snapPoint));
			this.line = line;
			this.snapPoint = snapPoint;
		}

		public LineString getLine() {
			return line;
		}

		public boolean isSnapped() {
			return snapPoint != null;
		}

		public Coordinate getSnapPoint() {
			return snapPoint;
		}
	}

	private Coordinate ifWithinSnapDistance(Coordinate coordinate,
			Coordinate other) {
		return coordinate != null && withinSnapDistance(coordinate, other) ? coordinate
				: null;
	}

	private Collection intersections(LineString line, Collection roadSegments) {
		Collection intersections = new ArrayList();
		for (Iterator i = roadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment other = (SourceRoadSegment) i.next();
			intersections.addAll(Arrays.asList(line.intersection(
					other.getApparentLine()).getCoordinates()));
		}
		return intersections;
	}

	private Coordinate last(LineString line) {
		return line.getCoordinateN(line.getNumPoints() - 1);
	}

	private double modelSnapDistance() {
		return SNAP_DISTANCE / getPanel().getViewport().getScale();
	}

	private LineSpec newLine(LineString oldLine, Coordinate dragStart,
			Coordinate dragEnd) {
		double multiplier = LengthToPoint.length(oldLine, dragEnd)
				/ oldLine.getLength();
		if (multiplier == 0) {
			return new LineSpec(oldLine, null);
		}
		double extension = multiplier == 1 ? dragEnd.distance(dragStart) : 0;
		LineString newLine = multiplier == 1 ? extend(oldLine, extension)
				: clip(oldLine, multiplier);
		return snap(newLine, oldLine, extension);
	}

	private LineSpec newLine(SourceRoadSegment roadSegment,
			final Coordinate dragStart, final Coordinate dragEnd) {
		return normalizeLineTemporarily(roadSegment, dragStart, new Block() {
			public Object yield(Object oldLine) {
				return newLine((LineString) oldLine, dragStart, dragEnd);
			}
		});
	}

	private Coordinate nextToLast(LineString line) {
		return line.getCoordinateN(line.getNumPoints() - 2);
	}

	private LineSpec normalizeLineTemporarily(SourceRoadSegment roadSegment,
			Coordinate dragStart, Block block) {
		if (roadSegment.apparentEndpointFurthestFrom(dragStart).equals(
				roadSegment.getApparentStartCoordinate())) {
			return (LineSpec) block.yield(roadSegment.getApparentLine());
		}
		return reverse((LineSpec) block.yield(LineStringUtil
				.reverse(roadSegment.getApparentLine())));
	}

	private LineSpec reverse(LineSpec spec) {
		return new LineSpec(LineStringUtil.reverse(spec.getLine()), spec
				.getSnapPoint());
	}

	private Collection properIntersections(SourceRoadSegment roadSegment) {
		LineString line = roadSegment.getApparentLine();
		Collection properlyIntersectingRoadSegments = ProperIntersectionFinder
				.properlyIntersectingRoadSegments(roadSegment,
						FUTURE_Block.TRUE);
		return intersections(line, properlyIntersectingRoadSegments);
	}

	public void setMyRoadSegment(SourceRoadSegment roadSegment) {
		this.myRoadSegment = roadSegment;
		internalIntersections = properIntersections(roadSegment);
	}

	private LineSpec snap(LineString newLine, LineString oldLine,
			double extension) {
		Coordinate originalCoordinate = ifWithinSnapDistance(last(oldLine),
				last(newLine));
		Coordinate closestInternalIntersection = closestInternalIntersectionWithinSnapDistance(last(newLine));
		Coordinate closestExternalIntersection = closestExternalIntersectionWithinSnapDistance(
				last(newLine), oldLine, extension + modelSnapDistance());
		Collection snapPoints = new ArrayList();
		CollectionUtil.addIfNotNull(originalCoordinate, snapPoints);
		CollectionUtil.addIfNotNull(closestInternalIntersection, snapPoints);
		CollectionUtil.addIfNotNull(closestExternalIntersection, snapPoints);
		Coordinate closestSnapPoint = closestCoordinate(snapPoints,
				last(newLine));
		if (closestSnapPoint == null) {
			return new LineSpec(newLine, null);
		}
		if (closestSnapPoint == originalCoordinate) {
			return new LineSpec(oldLine, closestSnapPoint);
		}
		if (closestSnapPoint == closestInternalIntersection) {
			return new LineSpec(moveEndTo(clip(oldLine, LengthToPoint.length(
					oldLine, closestInternalIntersection)
					/ oldLine.getLength()), closestSnapPoint), closestSnapPoint);
		}
		if (closestSnapPoint == closestExternalIntersection) {
			return new LineSpec(moveEndTo(extend(oldLine, last(oldLine)
					.distance(closestExternalIntersection)), closestSnapPoint),
					closestSnapPoint);
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	private LineString moveEndTo(LineString line, Coordinate c) {
		//Ensure that the last point on the line equals the snap point.
		//We use the snap point later to split the other line.
		//[Jon Aquino 2004-03-19]
		last(line).setCoordinate(c);
		return line;
	}

	private boolean withinSnapDistance(Coordinate coordinate, Coordinate other) {
		return coordinate.distance(other) < modelSnapDistance();
	}

	private Collection internalIntersections;

	private SourceRoadSegment myRoadSegment;

	private static final double SNAP_DISTANCE = 10;
}