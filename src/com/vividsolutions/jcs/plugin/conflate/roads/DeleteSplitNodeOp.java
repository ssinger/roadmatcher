package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegmentSiblingUpdater;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.jump.FUTURE_CoordinateArrays;
import com.vividsolutions.jcs.jump.FUTURE_LineMerger;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;

public class DeleteSplitNodeOp {

	public UndoableCommand createUndoableCommand(SourceFeature feature,
			Layer layer, ToolboxModel toolboxModel, ErrorHandler errorHandler,
			String name) throws Exception {
		return createUndoableCommand(((SplitRoadSegment) feature
				.getRoadSegment()).getStartNode(), ((SplitRoadSegment) feature
				.getRoadSegment()).getParent(), toolboxModel, errorHandler,
				name);
	}

	private UndoableCommand createUndoableCommand(final RoadNode splitNode,
			final SourceRoadSegment parent, final ToolboxModel toolboxModel,
			final ErrorHandler errorHandler, String name) {
		return new UndoableCommand(name) {
			private SplitRoadSegmentSiblingUpdater siblingUpdater;

			private Transaction transaction;

			public void execute() {
				// Figure out the start and end segments at the last minute,
				// because if this command is part of a composite command
				// (like in RevertAllOp), the start and end segments will
				// be changing during the execution of the composite command.
				// [Jon Aquino 2004-09-17]
				
				// Re-use merged segment on subsequent redo's; otherwise, get
				// NullPointerException in the following case:
				//
				// 1. Match two segments
				// 2. Create a split node on one of the segments
				// 3. Delete the split node
				// 4. Revert the match
				// 5. Undo
				// 6. Undo
				// 7. Redo
				// 8. Redo. Get NullPointerException in AdjustedMatchConsistencyRule$6.matchingApparentNode.
				//
				// Reason: the RevertOp undoable command binds the two matching segments.
				// If DeleteSplitNode creates a new merged segment on each redo, RevertOp's redo
				// will not revert the new merged segment. Result: the new merged segment will
				// be matched to an Unknown segment, leading to the NullPointerException.
				// [Jon Aquino 2004-09-17]
				
				if (transaction == null) {
					SplitRoadSegment endRoadSegment = splitSegment(
							(RoadNode) splitNode.getGraph().findNode(
									splitNode.getCoordinate()), parent);
					SplitRoadSegment startRoadSegment = endRoadSegment
							.getSiblingAtStart();
					SplitRoadSegment mergedRoadSegment = merge(
							startRoadSegment,
							endRoadSegment,
							!endRoadSegment.wasStartSplitNodeExistingVertex() ? endRoadSegment
									.getLine().getCoordinateN(0)
									: null);
					Assert
							.isTrue(endRoadSegment.getStartNode() == startRoadSegment
									.getEndNode());
					mergedRoadSegment.setSiblingAtStart(startRoadSegment
							.getSiblingAtStart(), startRoadSegment
							.wasStartSplitNodeExistingVertex());
					mergedRoadSegment.setSiblingAtEnd(endRoadSegment
							.getSiblingAtEnd(), endRoadSegment
							.wasEndSplitNodeExistingVertex());
					transaction = new Transaction(toolboxModel, errorHandler);
					transaction.severMatch(endRoadSegment);
					transaction.severMatch(startRoadSegment);
					transaction.remove(endRoadSegment);
					transaction.remove(startRoadSegment);
					transaction.add(mergedRoadSegment);
					siblingUpdater = new SplitRoadSegmentSiblingUpdater(
							mergedRoadSegment);
					if (mergedRoadSegment.getState().indicates(
							SourceState.MATCHED)) {
						transaction.setState(mergedRoadSegment
								.getMatchingRoadSegment(),
								other(mergedRoadSegment.getState()),
								mergedRoadSegment.getMatch());
					}
				}
				transaction.execute();
				siblingUpdater.execute();
			}

			public void unexecute() {
				siblingUpdater.unexecute();
				transaction.unexecute();
			}
		};
	}

	private SplitRoadSegment splitSegment(RoadNode splitNodeAtStart,
			SourceRoadSegment parent) {
		for (Iterator i = splitNodeAtStart.getIncidentRoadSegments().iterator(); i
				.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (segment.getStartNode() != splitNodeAtStart) {
				continue;
			}
			if (!(segment instanceof SplitRoadSegment)) {
				continue;
			}
			if (((SplitRoadSegment) segment).getParent() != parent) {
				continue;
			}
			return (SplitRoadSegment) segment;
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	private LineString deleteVertex(Coordinate vertexToDelete, LineString line) {
		//GeometryEditor#delete compares using == rather than #equals [Jon
		// Aquino 12/5/2003]
		for (int i = 0; i < line.getNumPoints(); i++) {
			if (line.getCoordinateN(i).equals(vertexToDelete)) {
				return (LineString) new GeometryEditor().deleteVertices(line,
						Collections.singleton(line.getCoordinateN(i)));
			}
		}
		return (LineString) FUTURE_Assert.throwAssertionFailure();
	}

	public LineString merge(LineString a, LineString b,
			Coordinate vertexToDelete) {
		Assert.isTrue(FUTURE_LineString.last(a).equals(
				FUTURE_LineString.first(b)));
		Collection mergedLineStrings = new FUTURE_LineMerger().FUTURE_add(a)
				.FUTURE_add(b).getMergedLineStrings();
		Assert.isTrue(1 == mergedLineStrings.size());
		LineString mergedLineString = (LineString) mergedLineStrings.iterator()
				.next();
		if (!FUTURE_LineString.first(a).equals(
				FUTURE_LineString.first(mergedLineString))) {
			mergedLineString = LineStringUtil.reverse(mergedLineString);
		}
		mergedLineString = fixCulDeSacAfterMerge(mergedLineString, a, b);
		Assert.isTrue(FUTURE_LineString.first(a).equals(
				FUTURE_LineString.first(mergedLineString)));
		return vertexToDelete == null ? mergedLineString : deleteVertex(
				vertexToDelete, mergedLineString);
	}

	protected static LineString fixCulDeSacAfterMerge(
			LineString mergedLineString, LineString a, LineString b) {
		if (!FUTURE_LineString.first(mergedLineString).equals(
				FUTURE_LineString.last(mergedLineString))) {
			return mergedLineString;
		}
		LineString fixedMergedLineString = rotate(mergedLineString, a
				.getCoordinateN(0));
		if (!fixedMergedLineString.getCoordinateN(1)
				.equals(a.getCoordinateN(1))) {
			fixedMergedLineString = LineStringUtil
					.reverse(fixedMergedLineString);
		}
		Assert.isTrue(fixedMergedLineString.getCoordinateN(1).equals(
				a.getCoordinateN(1)));
		return fixedMergedLineString;
	}

	private static LineString rotate(LineString lineString, Coordinate newStart) {
		return lineString.getFactory().createLineString(
				rotate(lineString.getCoordinates(), newStart));
	}

	private static Coordinate[] rotate(Coordinate[] coordinates,
			Coordinate newStart) {
		Assert.isTrue(coordinates[0]
				.equals(coordinates[coordinates.length - 1]));
		Coordinate[] coordinatesToScroll = new Coordinate[coordinates.length - 1];
		System.arraycopy(coordinates, 0, coordinatesToScroll, 0,
				coordinates.length - 1);
		FUTURE_CoordinateArrays.scroll(coordinatesToScroll, newStart);
		Coordinate[] newCoordinates = new Coordinate[coordinates.length];
		System.arraycopy(coordinatesToScroll, 0, newCoordinates, 0,
				coordinatesToScroll.length);
		newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
		return newCoordinates;
	}

	private SplitRoadSegment merge(SplitRoadSegment a, SplitRoadSegment b,
			Coordinate vertexToDelete) {
		Assert.isTrue(FUTURE_LineString.last(a.getLine()).equals(
				FUTURE_LineString.first(b.getLine())));
		SplitRoadSegment mergedRoadSegment = new SplitRoadSegment(merge(a
				.getLine(), b.getLine(), vertexToDelete), a
				.getOriginalFeature(), a.getNetwork(), a.getParent());
		SplitRoadSegment moreSimilar = moreSimilar(a, b, mergedRoadSegment);
		mergedRoadSegment.setState(moreSimilar.getState(), moreSimilar
				.getMatch() != null ? new RoadSegmentMatch(mergedRoadSegment,
				moreSimilar.getMatchingRoadSegment()) : null);
		return mergedRoadSegment;
	}

	private SplitRoadSegment moreSimilar(SplitRoadSegment a,
			SplitRoadSegment b, SplitRoadSegment mergedRoadSegment) {
		return CreateSplitNodeOp.indexOfMoreSimilarFeature(
				new SourceFeature[] { (SourceFeature) a.getFeature(),
						(SourceFeature) b.getFeature() },
				(SourceFeature) mergedRoadSegment.getFeature()) == 0 ? a : b;
	}

	private SourceState other(SourceState state) {
		return state == SourceState.MATCHED_REFERENCE ? SourceState.MATCHED_NON_REFERENCE
				: state == SourceState.MATCHED_NON_REFERENCE ? SourceState.MATCHED_REFERENCE
						: (SourceState) FUTURE_Assert.throwAssertionFailure();
	}

	public static boolean deletable(SourceRoadSegment roadSegment,
			boolean ignoreAdjustments, Block actionWhenAdjustmentDetected) {
		if (!(roadSegment instanceof SplitRoadSegment)) {
			return false;
		}
		if (!((SplitRoadSegment) roadSegment).isSplitAtStart()) {
			return false;
		}
		if (!ignoreAdjustments
				&& (roadSegment.isAdjusted() || ((SplitRoadSegment) roadSegment)
						.getSiblingAtStart().isAdjusted())) {
			actionWhenAdjustmentDetected.yield();
			return false;
		}
		return true;
	}
}