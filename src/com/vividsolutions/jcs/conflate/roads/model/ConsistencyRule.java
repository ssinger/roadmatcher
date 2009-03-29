package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;

import com.vividsolutions.jcs.conflate.roads.ErrorMessages;


public interface ConsistencyRule extends Serializable {
    public ResultState.Description checkInconsistent(SourceRoadSegment roadSegment);
    public ResultState.Description checkPending(SourceRoadSegment roadSegment);
    public StateTransitionImpactAssessment getStateTransitionImpactAssessment();
    public static final String START_NODE_ERROR = ErrorMessages.consistencyRule_startNodeError;
    public static final String END_NODE_ERROR = ErrorMessages.consistencyRule_endNodeError;
}
