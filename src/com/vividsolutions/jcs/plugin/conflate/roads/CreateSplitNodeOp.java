package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
public class CreateSplitNodeOp {
	public static abstract class CreateSplitNodeUndoableCommand
			extends
				UndoableCommand {
		private SourceFeature oldFeature;
		private SourceFeature newFeatureA;
		private SourceFeature newFeatureB;
		public CreateSplitNodeUndoableCommand(SourceFeature oldFeature,
				SourceFeature newFeatureA, SourceFeature newFeatureB) {
			super("Create Split Node");
			this.oldFeature = oldFeature;
			this.newFeatureA = newFeatureA;
			this.newFeatureB = newFeatureB;
		}
		public SourceFeature getNewFeatureA() {
			return newFeatureA;
		}
		public SourceFeature getNewFeatureB() {
			return newFeatureB;
		}
		public SourceFeature getOldFeature() {
			return oldFeature;
		}
	}
	public static CreateSplitNodeUndoableCommand createUndoableCommand(
			SourceFeature oldFeature, Coordinate target,
			boolean moveSplitToTarget,
			boolean attemptToEnforceMinLineSegmentLength,
			boolean warnAboutLineSegmentLength, ToolboxModel model,
			ErrorHandler errorHandler) throws NoninvertibleTransformException,
			ShortSegmentException {
		Assert
				.isTrue(!(moveSplitToTarget && attemptToEnforceMinLineSegmentLength));
		SourceFeature[] newFeatures = split(oldFeature, target,
				moveSplitToTarget, attemptToEnforceMinLineSegmentLength
						? AdjustEndpointOperation.minLineSegmentLength(model
								.getSession())
						: 0);
		if (warnAboutLineSegmentLength) {
			checkLineSegmentLength(new LineString[]{
					newFeatures[0].getRoadSegment().getLine(),
					newFeatures[1].getRoadSegment().getLine()}, model
					.getContext());
		}
		//Fix: we weren't handling Standalones before [Jon Aquino 2004-03-19]
		//Fix: we weren't handling Retired segments [Jon Aquino 2004-06-01]
		int indexOfMoreSimilarFeature = indexOfMoreSimilarFeature(newFeatures,
				oldFeature.getRoadSegment().getState().indicates(
						SourceState.MATCHED)
						? (SourceFeature) oldFeature.getRoadSegment()
								.getMatchingRoadSegment().getFeature()
						: oldFeature);
		newFeatures[indexOfMoreSimilarFeature].getRoadSegment().setState(
				oldFeature.getRoadSegment().getState(),
				oldFeature.getRoadSegment().getState().indicates(
						SourceState.MATCHED)
						? new RoadSegmentMatch(
								newFeatures[indexOfMoreSimilarFeature]
										.getRoadSegment(), oldFeature
										.getRoadSegment()
										.getMatchingRoadSegment())
						: null);
		newFeatures[1 - indexOfMoreSimilarFeature].getRoadSegment().setState(
				SourceState.UNKNOWN, null);
		return create(oldFeature, newFeatures, model, errorHandler);
	}
	public static void checkLineSegmentLength(LineString[] newLines,
			WorkbenchContext context) throws ShortSegmentException {
		//Only check the lengths of the line segment in which the split
		//occurred. Otherwise the user may be confused.
		//[Jon Aquino 2004-03-17]
		//Check both line segments at the same time, so the user won't ever
		//get prompted twice. [Jon Aquino 2004-03-17]
		checkLineSegmentLength(merge(lastLineSegment(newLines[0]),
				firstLineSegment(newLines[1])), context);
	}
	private static LineString merge(LineString a, LineString b) {
		Assert.isTrue(a.getNumPoints() == 2 && b.getNumPoints() == 2
				&& a.getCoordinateN(1).equals(b.getCoordinateN(0)));
		return a.getFactory().createLineString(
				new Coordinate[]{a.getCoordinateN(0), a.getCoordinateN(1),
						b.getCoordinateN(1)});
	}
	private static LineString firstLineSegment(LineString lineString) {
		return lineString.getFactory().createLineString(
				new Coordinate[]{lineString.getCoordinateN(0),
						lineString.getCoordinateN(1)});
	}
	private static LineString lastLineSegment(LineString lineString) {
		return lineString.getFactory()
				.createLineString(
						new Coordinate[]{
								lineString.getCoordinateN(lineString
										.getNumPoints() - 2),
								lineString.getCoordinateN(lineString
										.getNumPoints() - 1)});
	}
	private static void checkLineSegmentLength(LineString lineString,
			WorkbenchContext context) throws ShortSegmentException {
		if (!checkLineSegmentLength(lineString, CreateSplitNodeOp.class
				.getName()
				+ " - DO NOT SHOW AGAIN", context)) {
			throw new ShortSegmentException();
		}
	}
	/**
	 * The user has already been warned (by a dialog or on the status line).
	 * This exception is thrown as a convenient way to abort the current action
	 * (the alternative would have been a complicated return-value scheme).
	 * Simply catch it at the top level and do nothing. [Jon Aquino 2004-03-19]
	 */
	public static class ShortSegmentException extends Exception {
	}
	private static CreateSplitNodeUndoableCommand create(
			SourceFeature oldFeature, SourceFeature[] newFeatures,
			ToolboxModel model, ErrorHandler errorHandler) {
		final Transaction transaction = new Transaction(model, errorHandler);
		transaction.remove(oldFeature.getRoadSegment());
		transaction.add(newFeatures[0].getRoadSegment());
		transaction.add(newFeatures[1].getRoadSegment());
		updateMatchingRoadSegment(newFeatures[0].getRoadSegment(), transaction);
		updateMatchingRoadSegment(newFeatures[1].getRoadSegment(), transaction);
		final SplitRoadSegmentSiblingUpdater siblingUpdater0 = new SplitRoadSegmentSiblingUpdater(
				newFeatures[0].getRoadSegment());
		final SplitRoadSegmentSiblingUpdater siblingUpdater1 = new SplitRoadSegmentSiblingUpdater(
				newFeatures[1].getRoadSegment());
		return new CreateSplitNodeUndoableCommand(oldFeature, newFeatures[0],
				newFeatures[1]) {
			public void execute() {
				transaction.execute();
				siblingUpdater0.execute();
				siblingUpdater1.execute();
			}
			public void unexecute() {
				siblingUpdater1.unexecute();
				siblingUpdater0.unexecute();
				transaction.unexecute();
			}
		};
	}
	private static SourceFeature[] split(SourceFeature feature,
			Coordinate target, boolean moveSplitToTarget,
			double minLineSegmentLength) throws NoninvertibleTransformException {
		LineString[] newLineStrings = new LineStringSplitter().split(
				(LineString) feature.getGeometry(), target, moveSplitToTarget,
				minLineSegmentLength);
		SplitRoadSegment[] splitRoadSegments = SplitRoadSegmentFactory.create(
				newLineStrings, feature.getRoadSegment());
		Assert.isTrue(splitRoadSegments.length == 2);
		return new SourceFeature[]{
				(SourceFeature) splitRoadSegments[0].getFeature(),
				(SourceFeature) splitRoadSegments[1].getFeature()};
	}
	public static void updateMatchingRoadSegment(SourceRoadSegment roadSegment,
			Transaction transaction) {
		if (!roadSegment.getState().indicates(SourceState.MATCHED)) {
			return;
		}
		transaction.setState(roadSegment.getMatchingRoadSegment(), roadSegment
				.getMatchingRoadSegment().getState(), roadSegment.getMatch());
	}
	public static int indexOfMoreSimilarFeature(SourceFeature[] candidates,
			SourceFeature target) {
		return MatchStyle.midPoint((LineString) candidates[0].getGeometry())
				.distance(
						MatchStyle.midPoint((LineString) target.getGeometry())) < MatchStyle
				.midPoint((LineString) candidates[1].getGeometry()).distance(
						MatchStyle.midPoint((LineString) target.getGeometry()))
				? 0
				: 1;
	}
	private static boolean checkLineSegmentLength(LineString newLine,
			String doNotShowAgainID, WorkbenchContext context) {
		return AdjustEndpointOperation.checkLineSegmentLength("Split anyway",
				newLine, doNotShowAgainID, context);
	}
}