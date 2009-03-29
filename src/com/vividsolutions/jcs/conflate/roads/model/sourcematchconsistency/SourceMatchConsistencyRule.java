package com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency;

import java.util.SortedSet;

import com.vividsolutions.jcs.conflate.roads.ErrorMessages;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jcs.jump.FUTURE_Block;
import com.vividsolutions.jump.util.Block;

public class SourceMatchConsistencyRule implements ConsistencyRule {

	public static ResultState.Description checkPending(
			SourceRoadSegment roadSegment, Block unknownStartNeighbourBlock,
			Block unknownEndNeighbourBlock) {
		ResultState.Description resultStateDescription = AbstractNodeConsistencyRule
				.createResultStateDescription(ResultState.PENDING,
						ErrorMessages.resultStateRules_startNeighbourUnknown,
						(SourceRoadSegment) unknownStartNeighbourBlock
								.yield(roadSegment));
		if (resultStateDescription != null) {
			return resultStateDescription;
		}
		resultStateDescription = AbstractNodeConsistencyRule
				.createResultStateDescription(ResultState.PENDING,
						ErrorMessages.resultStateRules_endNeighbourUnknown,
						(SourceRoadSegment) unknownEndNeighbourBlock
								.yield(roadSegment));
		if (resultStateDescription != null) {
			return resultStateDescription;
		}
		if (roadSegment.getState().indicates(SourceState.MATCHED)) {
			resultStateDescription = AbstractNodeConsistencyRule
					.createResultStateDescription(
							ResultState.PENDING,
							ErrorMessages.resultStateRules_match_startNeighbourUnknown,
							(SourceRoadSegment) unknownStartNeighbourBlock
									.yield(roadSegment.getMatchingRoadSegment()));
			if (resultStateDescription != null) {
				return resultStateDescription;
			}
			resultStateDescription = AbstractNodeConsistencyRule
					.createResultStateDescription(
							ResultState.PENDING,
							ErrorMessages.resultStateRules_match_endNeighbourUnknown,
							(SourceRoadSegment) unknownEndNeighbourBlock
									.yield(roadSegment.getMatchingRoadSegment()));
			if (resultStateDescription != null) {
				return resultStateDescription;
			}
		}
		return null;
	}

	public ResultState.Description checkInconsistent(
			SourceRoadSegment roadSegment) {
		ResultState.Description description = new ResultState.Description(
				ResultState.INCONSISTENT);
		SortedSet startNeighbourhood = new NodeNeighbourhood(roadSegment
				.getStartNode()).includedNodeLocations();
		if (startNeighbourhood.size() >= 2) {
			description.addComment(START_NODE_ERROR).put(
					START_NEIGHBOURHOOD_KEY, startNeighbourhood);
		}
		SortedSet endNeighbourhood = new NodeNeighbourhood(roadSegment
				.getEndNode()).includedNodeLocations();
		if (endNeighbourhood.size() >= 2) {
			description.addComment(END_NODE_ERROR).put(END_NEIGHBOURHOOD_KEY,
					endNeighbourhood);
		}
		return AbstractNodeConsistencyRule.nullIfCommentNull(description);
	}

	public static final String END_NEIGHBOURHOOD_KEY = SourceMatchConsistencyRule.class + " - END NODE NEIGHBOURHOOD";

	public static final String START_NEIGHBOURHOOD_KEY = SourceMatchConsistencyRule.class + " - START NODE NEIGHBOURHOOD";

	private StateTransitionImpactAssessment stateTransitionImpactAssessment = new SourceMatchStateTransitionImpactAssessment();

	private Block UNKNOWN_START_NEIGHBOUR_BLOCK = new FUTURE_Block() {
		public Object yield(Object segment) {
			return ((RoadNode) ((SourceRoadSegment) segment).getStartNode())
					.firstRoadSegment(SourceState.UNKNOWN,
							((SourceRoadSegment) segment));
		}
	};
	private Block UNKNOWN_END_NEIGHBOUR_BLOCK = new FUTURE_Block() {
		public Object yield(Object segment) {
			return ((RoadNode) ((SourceRoadSegment) segment).getEndNode())
					.firstRoadSegment(SourceState.UNKNOWN,
							((SourceRoadSegment) segment));
		}
	};

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		return stateTransitionImpactAssessment;
	}

	public Description checkPending(SourceRoadSegment roadSegment) {
		return checkPending(roadSegment, UNKNOWN_START_NEIGHBOUR_BLOCK,
				UNKNOWN_END_NEIGHBOUR_BLOCK);
	}

}