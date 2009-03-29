package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CompositeUndoableCommand;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.CreateSplitNodeUndoableCommand;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.ShortSegmentException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
public class CreateSplitNodeAtIntersectionOp {
	private class Intersection {
		private Coordinate coordinate;
		private SourceRoadSegment roadSegmentA;
		private SourceRoadSegment roadSegmentB;
	}
	private Coordinate closestIntersection(Coordinate c, SourceRoadSegment a,
			SourceRoadSegment b) {
		Geometry intersectionGeometry = a.getApparentLine().intersection(
				b.getApparentLine());
		Collection intersectionCoordinates = new ArrayList(Arrays
				.asList(intersectionGeometry.getCoordinates()));
		intersectionCoordinates.remove(a.getApparentStartCoordinate());
		intersectionCoordinates.remove(a.getApparentEndCoordinate());
		intersectionCoordinates.remove(b.getApparentStartCoordinate());
		intersectionCoordinates.remove(b.getApparentEndCoordinate());
		if (intersectionCoordinates.isEmpty()) {
			return null;
		}
		return DistanceOp.closestPoints(intersectionGeometry.getFactory()
				.createMultiPoint(
						(Coordinate[]) intersectionCoordinates
								.toArray(new Coordinate[]{})),
				intersectionGeometry.getFactory().createPoint(c))[0];
	}
	private SourceRoadSegment closestRoadSegment(Envelope envelope,
			boolean network0, boolean network1, ConflationSession session,
			Block distanceBlock) {
		SourceRoadSegment closestRoadSegment = null;
		for (Iterator i = roadSegmentsApparentlyIntersecting(envelope,
				network0, network1, session).iterator(); i.hasNext();) {
			SourceRoadSegment candidate = (SourceRoadSegment) i.next();
			if (distanceBlock.yield(candidate) == null) {
				continue;
			}
			if (closestRoadSegment == null
					|| ((Double) distanceBlock.yield(candidate)).doubleValue() < ((Double) distanceBlock
							.yield(closestRoadSegment)).doubleValue()) {
				closestRoadSegment = candidate;
			}
		}
		return closestRoadSegment;
	}
	private Collection roadSegmentsApparentlyIntersecting(Envelope envelope,
			boolean network0, boolean network1, ConflationSession session) {
		Collection roadSegments = new ArrayList();
		if (network0) {
			roadSegments.addAll(session.getSourceNetwork(0)
					.roadSegmentsApparentlyIntersecting(envelope));
		}
		if (network1) {
			roadSegments.addAll(session.getSourceNetwork(1)
					.roadSegmentsApparentlyIntersecting(envelope));
		}
		return roadSegments;
	}
	private CreateSplitNodeUndoableCommand createUndoableCommand(
			final SourceRoadSegment roadSegment, Coordinate intersection,
			final ToolboxModel toolboxModel, ErrorHandler errorHandler)
			throws NoninvertibleTransformException, ShortSegmentException {
		return CreateSplitNodeOp.createUndoableCommand(
				(SourceFeature) roadSegment.getFeature(), intersection, true, false,
				true, toolboxModel, errorHandler);
	}
	public static class RoadSegmentsAdjustedException extends Exception {
	}
	public boolean handleGesture(Coordinate clickCoordinate, Envelope envelope,
			boolean network0, boolean network1,
			final ToolboxModel toolboxModel, ErrorHandler errorHandler)
			throws RoadSegmentsAdjustedException,
			NoninvertibleTransformException, ShortSegmentException {
		final Intersection intersection = intersection(clickCoordinate,
				envelope, network0, network1, toolboxModel.getSession());
		if (intersection == null) {
			return false;
		}
		if (intersection.roadSegmentA.isAdjusted()
				|| intersection.roadSegmentB.isAdjusted()) {
			throw new RoadSegmentsAdjustedException();
		}
		AbstractPlugIn.execute(new FUTURE_CompositeUndoableCommand(
				"Create Split Node At Intersection").add(
				createUndoableCommand(intersection.roadSegmentA,
						intersection.coordinate, toolboxModel, errorHandler))
				.add(
						createUndoableCommand(intersection.roadSegmentB,
								intersection.coordinate, toolboxModel,
								errorHandler)), layerManagerProxy(
				intersection.roadSegmentA, toolboxModel));
		return true;
	}
	private Intersection intersection(final Coordinate clickCoordinate,
			Envelope envelope, boolean network0, boolean network1,
			ConflationSession session) {
		final Intersection intersection = new Intersection();
		intersection.roadSegmentA = closestRoadSegment(envelope, network0,
				network1, session, new Block() {
					public Object yield(Object roadSegment) {
						return new Double(((SourceRoadSegment) roadSegment)
								.getApparentLine().distance(
										((SourceRoadSegment) roadSegment)
												.getApparentLine().getFactory()
												.createPoint(clickCoordinate)));
					}
				});
		if (intersection.roadSegmentA == null) {
			return null;
		}
		intersection.roadSegmentB = closestRoadSegment(envelope, network0,
				network1, session, new Block() {
					public Object yield(Object roadSegment) {
						if (roadSegment == intersection.roadSegmentA) {
							return null;
						}
						Coordinate closestIntersection = closestIntersection(
								clickCoordinate, intersection.roadSegmentA,
								(SourceRoadSegment) roadSegment);
						if (closestIntersection == null) {
							return null;
						}
						return new Double(closestIntersection
								.distance(clickCoordinate));
					}
				});
		if (intersection.roadSegmentB == null) {
			return null;
		}
		intersection.coordinate = closestIntersection(clickCoordinate,
				intersection.roadSegmentA, intersection.roadSegmentB);
		if (!envelope.contains(intersection.coordinate)) {
			return null;
		}
		return intersection;
	}
	private Layer layer(SourceRoadSegment roadSegment, ToolboxModel toolboxModel) {
		return CreateSplitNodeNearEndpointOp.layer(roadSegment,
				toolboxModel);
	}
	private LayerManagerProxy layerManagerProxy(
			final SourceRoadSegment roadSegment, final ToolboxModel toolboxModel) {
		return new LayerManagerProxy() {
			public LayerManager getLayerManager() {
				return layer(roadSegment, toolboxModel).getLayerManager();
			}
		};
	}
}
