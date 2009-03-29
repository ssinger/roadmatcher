package com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency;

import java.util.Collection;

import com.vividsolutions.jcs.conflate.roads.model.AbstractStateTransitionImpactAssessment;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;

public class SourceMatchStateTransitionImpactAssessment extends
		AbstractStateTransitionImpactAssessment {


	protected Collection includedSegmentsInNeighbourhood(
			SourceRoadSegment roadSegment) {
		return FUTURE_CollectionUtil.concatenate(new NodeNeighbourhood(
				roadSegment.getStartNode()).getIncludedRoadSegments(),
				new NodeNeighbourhood(roadSegment.getEndNode())
						.getIncludedRoadSegments());
	}

	protected Collection neighbours(SourceRoadSegment roadSegment) {
		return roadSegment.getNeighbours();
	}
}