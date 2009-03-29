package com.vividsolutions.jcs.conflate.roads.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.vividsolutions.jcs.conflate.roads.match.*;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.MatchingApparentNodesFinder;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.Block;

public class SourceRoadSegment extends RoadSegment {
	private double adjustmentSize = 0;

	public static void createMatch(SourceRoadSegment a, SourceRoadSegment b) {
		RoadSegmentMatch match = new RoadSegmentMatch(a, b);
		SourceRoadSegment reference = a.getNetwork().getSession()
				.getPrecedenceRuleEngine().chooseReference(a, b);
		reference.setState(SourceState.MATCHED_REFERENCE, match);
		match.other(reference).setState(SourceState.MATCHED_NON_REFERENCE,
				match);
	}

	private String comment = "";

	private boolean manuallyMatched = false;

	private boolean manuallyAdjusted = false;

	private SourceRoadSegment lastProperlyIntersectingIncludedRoadSegment = null;

	private LineString apparentLine;

	private ResultState.Description resultStateDescription = new ResultState.Description(
			null);

	private double apparentLineLength;

	private Double maxDistance = null;

	private Boolean matchOrientationSame = null;

	private Double trimmedDistance = null;
	
	private Date updateTime = new Date();

	private Double nearnessFraction = null;

	private double adjustedAngleDelta;

	private LineString previousApparentLine;

	public SourceRoadSegment(LineString line, Feature originalFeature,
			RoadNetwork network) {
		super(line, network);
		this.apparentLine = line;
		this.previousApparentLine = line;
		apparentLineLength = apparentLine.getLength();
		setFeature(new SourceFeature(this));
		this.originalFeature = originalFeature;
	}

	public boolean isAdjusted() {
		return apparentLine != getLine();
	}

	public void setApparentLine(final LineString apparentLine) {
		//Don't just compare endpoints -- compare the whole line,
		//because vertices may have been inserted
		//[Jon Aquino 2004-03-22]
		//Moved assert here from AdjustEndpointOperation#setApparentLine,
		//which was called by AutoConnectEndpointTool#setLines and
		//AdjustEndpointOperation#changeLine [Jon Aquino 2004-04-27]
		//Don't assert network is editable -- doesn't make sense for
		//Revert Tool and for undo [Jon Aquino 2004-04-30]
		Assert.isTrue(!getLine().equalsExact(apparentLine)
				|| getLine() == apparentLine);
		previousApparentLine = this.apparentLine;
		assignManuallyAdjusted(new Block() {
			public Object yield() {
				SourceRoadSegment.this.apparentLine = apparentLine;
				return null;
			}
		});
		getNetwork().apparentLineChanged(this, previousApparentLine,
				apparentLine);
		adjustmentSize = !isAdjusted() ? 0 : adjustmentSize(getLine(),
				getApparentLine());
		apparentLineLength = apparentLine.getLength();
		adjustedAngleDelta = Double.NaN;
		clearCachedFields();
		updateTime = new Date();
	}

	private void assignManuallyAdjusted(Block block) {
		LineString originalApparentLine = getApparentLine();
		block.yield();
		if (originalApparentLine.equals(getApparentLine())) {
			return;
		}
		manuallyAdjusted = !getNetwork().getSession()
				.isAutomatedProcessRunning()
				&& isAdjusted();
		manualAdjustmentCount = manuallyAdjusted ? manualAdjustmentCount + 1
				: 0;
		//	Doesn't work perfectly with undo [Jon Aquino 2004-06-23]
	}

	private int manualAdjustmentCount = 0;

	private void assignManuallyMatched(Block block) {
		SourceRoadSegment originalMatch = getMatchingRoadSegment();
		block.yield();
		if (originalMatch == getMatchingRoadSegment()) {
			return;
		}
		manuallyMatched = !getNetwork().getSession()
				.isAutomatedProcessRunning()
				&& isMatched();
		//Does not work perfectly with undo (e.g. manually rematch an
		// AutoMatch, then undo) [Jon Aquino 2004-06-23]
	}

	public static double adjustmentSize(LineString a, LineString b) {
		return Math.max(a.getCoordinateN(0).distance(b.getCoordinateN(0)),
				LineStringUtil.last(a).distance(LineStringUtil.last(b)));
	}

	/**
	 * Returns the adjusted line if this road segment has ben adjusted;
	 * otherwise returns the original line.
	 */
	public LineString getApparentLine() {
		return apparentLine;
	}

	public Coordinate getApparentStartCoordinate() {
		return LineStringUtil.first(getApparentLine());
	}

	public double getApparentStartAngle() {
		return Angle.angle(apparentLine.getCoordinateN(0), apparentLine
				.getCoordinateN(1));
	}

	public Coordinate getApparentEndCoordinate() {
		return LineStringUtil.last(getApparentLine());
	}

	public void enteredNetwork() {
		super.enteredNetwork();
		getNetwork().getSession().getRoadsEventFirer().fireRoadSegmentAdded(
				this);
	}

	public void exitedNetwork() {
		super.exitedNetwork();
		getNetwork().getSession().getRoadsEventFirer().fireRoadSegmentRemoved(
				this);
	}

	public void exitingNetwork() {
		super.exitingNetwork();
		neighboursBeforeExitingNetwork = getNeighbours();
	}

	public CandidateMatches getCandidateMatches() {
		return candidateMatches;
	}

	public RoadSegmentMatch getMatch() {
		return match;
	}

	public SourceRoadSegment getMatchingRoadSegment() {
		return getMatch() != null ? getMatch().other(this) : null;
	}

	public boolean isMatched() {
		return match != null;
	}

	public Collection getNeighbours() {
		return isInNetwork() ? super.getNeighbours()
				: neighboursBeforeExitingNetwork;
	}

	public Feature getOriginalFeature() {
		return originalFeature;
	}

	public ResultState getResultState() {
		return resultStateDescription.getResultState();
	}

	public ResultState.Description getResultStateDescription() {
		return resultStateDescription;
	}

	public SourceState getState() {
		return state;
	}

	public void setResultState(ResultState.Description resultStateDescription) {
		ResultState.Description oldResultStateDescription = this.resultStateDescription;
		this.resultStateDescription = resultStateDescription;
		Assert
				.isTrue(resultStateDescription
						.get(ResultStateRules.PROPERLY_INTERSECTING_ROAD_SEGMENT_KEY) == null
						|| ((SourceRoadSegment) resultStateDescription
								.get(ResultStateRules.PROPERLY_INTERSECTING_ROAD_SEGMENT_KEY))
								.getState().indicates(SourceState.INCLUDED));
		this.lastProperlyIntersectingIncludedRoadSegment = ((SourceRoadSegment) resultStateDescription
				.get(ResultStateRules.PROPERLY_INTERSECTING_ROAD_SEGMENT_KEY));
		getNetwork().getSession().getRoadsEventFirer().fireResultStateChanged(
				oldResultStateDescription.getResultState(), this);
	}

	/**
	 * @param match
	 *            null if state is not SourceState.MATCHED
	 */
	public SourceRoadSegment setState(final SourceState state,
			final RoadSegmentMatch match) {
		Assert.isTrue(state.indicates(SourceState.MATCHED) == (match != null),
				state + ", " + match);
		//Clear it before changing the match, because we need to clear it
		//in the match too. [Jon Aquino 2004-10-13]
		clearCachedFields();
		SourceState oldState = this.state;
		assignManuallyMatched(new Block() {
			public Object yield() {
				SourceRoadSegment.this.state = state;
				SourceRoadSegment.this.match = match;
				return null;
			}
		});
		getNetwork().getSession().getRoadsEventFirer().fireStateChanged(
				oldState, this);
		updateTime = new Date();
		return this;
	}

	public void clearCachedFields() {
		maxDistance = null;
		matchOrientationSame = null;
		trimmedDistance = null;
		nearnessFraction = null;
		if (match != null) {
			// Clear the other segment's cached value, because we won't
			// get here for the other segment when one segment is adjusted.
			// [Jon Aquino 2004-10-13]
			getMatchingRoadSegment().maxDistance = null;
			getMatchingRoadSegment().matchOrientationSame = null;
			getMatchingRoadSegment().trimmedDistance = null;
			getMatchingRoadSegment().nearnessFraction = null;
		}
	}

	private CandidateMatches candidateMatches = new CandidateMatches(
			new RoadSegmentComparator());

	private RoadSegmentMatch match;

	private Collection neighboursBeforeExitingNetwork = new ArrayList();

	private Feature originalFeature;

	private SourceState state = SourceState.UNKNOWN;

	public SourceRoadSegment getLastProperlyIntersectingIncludedRoadSegment() {
		return lastProperlyIntersectingIncludedRoadSegment;
	}

	/**
	 * Replaces this road segment in the network by SplitRoadSegments with the
	 * given lines.
	 */
	public SplitRoadSegment[] split(LineString[] lines) {
		SplitRoadSegment[] splitRoadSegments = SplitRoadSegmentFactory.create(
				lines, this);
		getNetwork().remove(this);
		for (int i = 0; i < splitRoadSegments.length; i++) {
			getNetwork().add(splitRoadSegments[i]);
			SplitRoadSegmentSiblingUpdater.update(splitRoadSegments[i]);
		}
		return splitRoadSegments;
	}

	public Coordinate apparentEndpointClosestTo(Coordinate coordinate) {
		return getApparentStartCoordinate().distance(coordinate) < getApparentEndCoordinate()
				.distance(coordinate) ? getApparentStartCoordinate()
				: getApparentEndCoordinate();
	}

	public Coordinate apparentEndpointFurthestFrom(Coordinate coordinate) {
		return apparentEndpointClosestTo(coordinate).equals(
				getApparentStartCoordinate()) ? getApparentEndCoordinate()
				: getApparentStartCoordinate();
	}

	public double getAdjustmentSize() {
		return adjustmentSize;
	}

	public double getApparentLineLength() {
		return apparentLineLength;
	}

	public Double getMaxDistance() {
		if (maxDistance == null) {
			maxDistance = match != null ? new Double(MatchDistance.maxDistance(
					getApparentLine(), match.other(this).getApparentLine()))
					: null;
		}
		return maxDistance;
	}
	
	private boolean reviewed = false;

	public Boolean isMatchOrientationSame() {
		if (matchOrientationSame == null) {
			// Although we are calling a method directly on
			// AdjustedMatchConsistencyRule, matchOrientationSame is
			// independent of ConsistencyRule. Thus, no need to add a
			// #matchOrientationSame method to the ConsistencyRule interface.
			// [Jon Aquino 2004-11-01]

			// Call #matchingApparentNode, not #apparentNodesMatching, which
			// adds a check if the apparent node equals the start or end
			// apparent nodes. This doesn't work for a pair of segments that
			// point in opposite directions but originate at the same apparent
			// node i.e. #apparentNodesMatching and #matchingApparentNode will
			// return different values because of this extra check. [Jon Aquino
			// 2004-11-01]
			matchOrientationSame = match != null ? Boolean
					.valueOf(MatchingApparentNodesFinder.instance()
							.matchingApparentNode(
									new ApparentNode(
											getApparentStartCoordinate(),
											getNetwork().getSession()),
									getMatchingRoadSegment(), this)
							.getCoordinate().equals(
									getMatchingRoadSegment()
											.getApparentStartCoordinate()))
					: null;
		}
		return matchOrientationSame;
	}

	public Double getTrimmedDistance() {
		if (trimmedDistance == null) {
			trimmedDistance = match != null ? new Double(MatchDistance
					.trimmedDistance(getApparentLine(), match.other(this)
							.getApparentLine())) : null;
		}
		return trimmedDistance;
	}

	private double nearnessTolerance = -1;

	public Double getNearnessFraction() {
		if (nearnessFraction == null
				|| nearnessTolerance != getSessionNearnessTolerance()) {
			if (match != null) {
				double maxDistance = getMaxDistance().doubleValue();
				nearnessFraction = new Double(MatchDistance.nearnessFraction(
						getApparentLine(), match.other(this).getApparentLine(),
						maxDistance, getSessionNearnessTolerance(), true));
				nearnessTolerance = getSessionNearnessTolerance();
			}
		}
		return nearnessFraction;
	}

	private double getSessionNearnessTolerance() {
		return getNetwork().getSession().getMatchOptions()
				.getEdgeMatchOptions().getNearnessTolerance();
	}

	public double getAdjustmentAngleDelta() {
		if (!isAdjusted()) {
			return 0;
		}
		if (Double.isNaN(adjustedAngleDelta)) {
			adjustedAngleDelta = Angle.toDegrees(Angle.diff(angle(getLine()),
					angle(getApparentLine())));
		}
		return adjustedAngleDelta;
	}

	private double angle(LineString line) {
		return Angle.angle(FUTURE_LineString.first(line), FUTURE_LineString
				.last(line));
	}

	private boolean startNodeConstrained = false;

	private boolean endNodeConstrained = false;

	public boolean isEndNodeConstrained() {
		return endNodeConstrained;
	}

	public boolean isStartNodeConstrained() {
		return startNodeConstrained;
	}

	public void setEndNodeConstrained(boolean endNodeConstrained) {
		this.endNodeConstrained = endNodeConstrained;
	}

	public void setStartNodeConstrained(boolean startNodeConstrained) {
		this.startNodeConstrained = startNodeConstrained;
	}

	public boolean isManuallyAdjusted() {
		return manuallyAdjusted;
	}

	public boolean isManuallyMatched() {
		return manuallyMatched;
	}

	public int getManualAdjustmentCount() {
		return manualAdjustmentCount;
	}

	public LineString getPreviousApparentLine() {
		return previousApparentLine;
	}

	/**
	 * @return non-null value
	 */
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		Assert.isTrue(comment != null, "Use empty string instead of null");
		this.comment = comment;
	}
	public boolean isReviewed() {
		return reviewed;
	}
	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
}