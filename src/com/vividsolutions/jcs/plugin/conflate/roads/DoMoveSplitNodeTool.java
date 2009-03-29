package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;

import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadSegmentMatch;
import com.vividsolutions.jcs.conflate.roads.model.RoadsListener;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.CreateSplitNodeUndoableCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
public class DoMoveSplitNodeTool extends DragTool {
	public DoMoveSplitNodeTool() {
		setColor(SimpleAdjustEndpointTool.COLOUR);
		setStroke(SimpleAdjustEndpointTool.STROKE);
	}
	private SplitRoadSegment roadSegmentSplitAtStart;
	private LineString mergedLine;
	protected void setRoadSegmentSplitAtStart(
			SplitRoadSegment roadSegmentSplitAtStart) {
		this.roadSegmentSplitAtStart = roadSegmentSplitAtStart;
		SplitRoadSegment roadSegmentSplitAtEnd = roadSegmentSplitAtStart
				.getSiblingAtStart();
		mergedLine = new DeleteSplitNodeOp().merge(roadSegmentSplitAtStart
				.getSiblingAtStart().getLine(), roadSegmentSplitAtStart
				.getLine(), !roadSegmentSplitAtStart
				.wasStartSplitNodeExistingVertex() ? roadSegmentSplitAtStart
				.getLine().getCoordinateN(0) : null);
	}
	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		if (closestPointOnMergedLineIsEndpoint(getModelDestination())) {
			return;
		}
		try {
			CreateSplitNodeOp.checkLineSegmentLength(
					newLines(getModelDestination()), getWorkbench()
							.getContext());
		} catch (CreateSplitNodeOp.ShortSegmentException e) {
			//Eat it. The thrower has already notified the user. [Jon Aquino]
			return;
		}
		execute(createUndoableCommand(getName(), roadSegmentSplitAtStart,
				toolboxModel(), getWorkbench().getContext().getErrorHandler(),
				newSplitLocation(getModelDestination())));
	}
	private ToolboxModel toolboxModel() {
		return ToolboxModel.instance(getWorkbench().getContext());
	}
	//static to ensure we're not depending on any tool state, because the tool
	//state will likely change after we've sent the UndoableCommand into
	//the undo history. [Jon Aquino 2004-05-17]
	private static UndoableCommand createUndoableCommand(String name,
			final SplitRoadSegment roadSegmentSplitAtStart,
			final ToolboxModel toolboxModel, final ErrorHandler errorHandler,
			final Coordinate newSplitLocation) {
		final SourceState roadSegmentState0 = roadSegmentSplitAtStart
				.getSiblingAtStart().getState();
		final SourceState roadSegmentState1 = roadSegmentSplitAtStart
				.getState();
		final SourceRoadSegment matchingRoadSegment0 = roadSegmentSplitAtStart
				.getSiblingAtStart().getMatchingRoadSegment();
		final SourceRoadSegment matchingRoadSegment1 = roadSegmentSplitAtStart
				.getMatchingRoadSegment();
		//Cache the states of the matching road segments, because they will get
		//lost during the Delete Split Node operation [Jon Aquino 2004-06-01]
		final SourceState matchingRoadSegmentState0 = matchingRoadSegment0 != null
				? matchingRoadSegment0.getState()
				: null;
		final SourceState matchingRoadSegmentState1 = matchingRoadSegment1 != null
				? matchingRoadSegment1.getState()
				: null;
		//Why we can't simply adjust the geometries of the two road segments:
		//(1) Moving a split node doesn't count as an adjustment (2) need to
		//update the node indexes [Jon Aquino]
		return new UndoableCommand(name) {
			private UndoableCommand deleteSplitNodeCommand;
			private CreateSplitNodeUndoableCommand createSplitNodeCommand;
			private Transaction assignStatesTransaction;
			private SourceFeature mergedFeature;
			public void execute() {
				// Clear the cached commands; otherwise, get AssertionFailedException
				// on redo. [Jon Aquino 2004-09-07]
				deleteSplitNodeCommand = null;
				createSplitNodeCommand = null;
				assignStatesTransaction = null;
				mergedFeature = trapAddedFeature(new Block() {
					public Object yield() {
						getDeleteSplitNodeCommand().execute();
						return null;
					}
				}, toolboxModel);
				getCreateSplitNodeCommand().execute();
				getAssignStatesTransaction().execute();
			}
			private Transaction getAssignStatesTransaction() {
				if (assignStatesTransaction == null) {
					assignStatesTransaction = new Transaction(toolboxModel,
							errorHandler);
					assignStates(getCreateSplitNodeCommand().getNewFeatureA()
							.getRoadSegment(), roadSegmentState0,
							matchingRoadSegment0, matchingRoadSegmentState0);
					assignStates(getCreateSplitNodeCommand().getNewFeatureB()
							.getRoadSegment(), roadSegmentState1,
							matchingRoadSegment1, matchingRoadSegmentState1);
				}
				return assignStatesTransaction;
			}
			private void assignStates(SourceRoadSegment newRoadSegment,
					SourceState newRoadSegmentState,
					SourceRoadSegment matchingRoadSegment,
					SourceState matchingRoadSegmentState) {
				RoadSegmentMatch match = matchingRoadSegment != null
						? new RoadSegmentMatch(newRoadSegment,
								matchingRoadSegment)
						: null;
				assignStatesTransaction.setState(newRoadSegment,
						newRoadSegmentState, match);
				if (matchingRoadSegment != null) {
					assignStatesTransaction.setState(matchingRoadSegment,
							matchingRoadSegmentState, match);
				}
			}
			public void unexecute() {
				getAssignStatesTransaction().unexecute();
				getCreateSplitNodeCommand().unexecute();
				getDeleteSplitNodeCommand().unexecute();
			}
			private UndoableCommand getDeleteSplitNodeCommand() {
				if (deleteSplitNodeCommand == null) {
					try {
						deleteSplitNodeCommand = new DeleteSplitNodeOp()
								.createUndoableCommand(
										(SourceFeature) roadSegmentSplitAtStart
												.getFeature(),
										toolboxModel
												.getSourceLayer(roadSegmentSplitAtStart
														.getNetworkID()),
										toolboxModel, errorHandler, getName());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				return deleteSplitNodeCommand;
			}
			private CreateSplitNodeUndoableCommand getCreateSplitNodeCommand() {
				if (createSplitNodeCommand == null) {
					try {
						createSplitNodeCommand = CreateSplitNodeOp
								.createUndoableCommand(mergedFeature,
										newSplitLocation, false, true, false,
										toolboxModel, errorHandler);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				return createSplitNodeCommand;
			}
		};
	}
	private static SourceFeature trapAddedFeature(Block block,
			final ToolboxModel toolboxModel) {
		final Collection roadSegmentsAdded = new ArrayList();
		final RoadsListener listener = new RoadsListener() {
			public void roadSegmentAdded(SourceRoadSegment roadSegment) {
				roadSegmentsAdded.add(roadSegment);
			}
			public void roadSegmentRemoved(SourceRoadSegment roadSegment) {
			}
			public void resultStateChanged(ResultState oldResultState,
					SourceRoadSegment roadSegment) {
			}
			public void stateChanged(SourceState oldState,
					SourceRoadSegment roadSegment) {
			}
			public void geometryModifiedExternally(SourceRoadSegment roadSegment) {
			}
			public void roadSegmentsChanged() {
			}
		};
		roadSegmentsAdded.clear();
		toolboxModel.getSession().getRoadsEventFirer().addListener(listener);
		try {
			block.yield();
			Assert.isTrue(roadSegmentsAdded.size() == 1);
			return (SourceFeature) ((SourceRoadSegment) roadSegmentsAdded
					.iterator().next()).getFeature();
		} finally {
			toolboxModel.getSession().getRoadsEventFirer().removeListener(
					listener);
		}
	}
	protected Shape getShape(Point2D source, Point2D destination)
			throws Exception {
		double radius = 20;
		//LineStringSplitter assumes target is not an endpoint
		//[Jon Aquino 2004-05-17]
		if (closestPointOnMergedLineIsEndpoint(getModelDestination())) {
			return null;
		}
		Point2D closestPoint = getPanel().getViewport().toViewPoint(
				newSplitLocation(getModelDestination()));
		return new Ellipse2D.Double(closestPoint.getX() - (radius / 2),
				closestPoint.getY() - (radius / 2), radius, radius);
	}
	private Coordinate newSplitLocation(Coordinate c)
			throws NoninvertibleTransformException {
		return newLines(c)[1].getCoordinateN(0);
	}
	private LineString[] newLines(Coordinate c)
			throws NoninvertibleTransformException {
		return new LineStringSplitter().split(mergedLine,
				closestPointOnMergedLine(c), false, AdjustEndpointOperation
						.minLineSegmentLength(toolboxModel().getSession()));
	}
	private boolean closestPointOnMergedLineIsEndpoint(Coordinate c)
			throws NoninvertibleTransformException {
		return closestPointOnMergedLine(c).equals(
				FUTURE_LineString.first(mergedLine))
				|| closestPointOnMergedLine(c).equals(
						FUTURE_LineString.last(mergedLine));
	}
	private Coordinate closestPointOnMergedLine(Coordinate c)
			throws NoninvertibleTransformException {
		return new DistanceOp(mergedLine, mergedLine.getFactory()
				.createPoint(
						CreateSplitNodeTool.snapToExistingVertex(c, mergedLine,
								EnvelopeUtil.expand(new Envelope(
										getModelDestination()),
										SimpleAdjustEndpointTool.TOLERANCE
												/ getPanel().getViewport()
														.getScale()))))
				.closestPoints()[0];
	}
	public Icon getIcon() {
		throw new UnsupportedOperationException();
	}
}