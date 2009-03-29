package com.vividsolutions.jcs.conflate.roads.model.resultconsistency;

import java.io.Serializable;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.ErrorMessages;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jts.util.Assert;

public class AdjustedRoadSegmentNodeConsistencyRule extends AbstractNodeConsistencyRule {

	public static final String START_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY = AdjustedRoadSegmentNodeConsistencyRule.class + " - START CANDIDATE ADJUSTMENT LOCATIONS";
    public static final String END_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY = AdjustedRoadSegmentNodeConsistencyRule.class + " - END CANDIDATE ADJUSTMENT LOCATIONS";

    public ResultState.Description checkInconsistent(SourceRoadSegment roadSegment) {
        ResultState.Description description = new ResultState.Description(ResultState.INCONSISTENT);        
		if (!candidateAdjustmentLocations(roadSegment.getStartNode()).contains(roadSegment.getApparentLine().getStartPoint().getCoordinate())) {
            description.addComment(ErrorMessages.adjustedRoadSegmentNodeConsistencyRule_startNodeError);
            description.put(START_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY, candidateAdjustmentLocations(roadSegment.getStartNode()));
        }
        if (!candidateAdjustmentLocations(roadSegment.getEndNode()).contains(roadSegment.getApparentLine().getEndPoint().getCoordinate())) {
            description.addComment(ErrorMessages.adjustedRoadSegmentNodeConsistencyRule_endNodeError);
            description.put(END_CANDIDATE_ADJUSTMENT_LOCATIONS_KEY, candidateAdjustmentLocations(roadSegment.getEndNode()));
        }
        return nullIfCommentNull(description);        
    }
    
    private Set candidateAdjustmentLocations(RoadNode node) {
        return locations(included(nodesMatching(node,
                roadSegmentsAndMatches(node.getIncidentRoadSegments()))));
    }

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		Assert.shouldNeverReachHere();
		return null;
	}

	public Description checkPending(SourceRoadSegment roadSegment) {
		throw new UnsupportedOperationException();
	}
}
