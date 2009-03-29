package com.vividsolutions.jcs.conflate.roads.model.resultconsistency;

import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.StateTransitionImpactAssessment;
import com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency.SourceMatchStateTransitionImpactAssessment;

public class ResultConsistencyRule implements ConsistencyRule {


	private ConsistencyRule adjustedRoadSegmentNodeConsistencyRule = new AdjustedRoadSegmentNodeConsistencyRule();

	private ConsistencyRule unadjustedRoadSegmentNodeConsistencyRule = new UnadjustedRoadSegmentNodeConsistencyRule();

	public ResultState.Description checkInconsistent(SourceRoadSegment roadSegment) {
		return roadSegment.isAdjusted() ? adjustedRoadSegmentNodeConsistencyRule
				.checkInconsistent(roadSegment)
				: unadjustedRoadSegmentNodeConsistencyRule.checkInconsistent(roadSegment);
	}
	
	public ResultState.Description checkPending(SourceRoadSegment roadSegment) {
		return roadSegment.isAdjusted() ? adjustedRoadSegmentNodeConsistencyRule
				.checkPending(roadSegment)
				: unadjustedRoadSegmentNodeConsistencyRule.checkPending(roadSegment);
	}

	private StateTransitionImpactAssessment stateTransitionImpactAssessment = new SourceMatchStateTransitionImpactAssessment();

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		return stateTransitionImpactAssessment;
	}

}