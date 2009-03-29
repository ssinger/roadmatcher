package com.vividsolutions.jcs.plugin.conflate.roads.sourcematchconsistency;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency.SourceMatchConsistencyRule;
import com.vividsolutions.jcs.plugin.conflate.roads.AbstractInconsistentNeighbourhoodStyle;
import com.vividsolutions.jts.geom.Coordinate;

public class SourceMatchInconsistentEndpointStyle
		extends
			AbstractInconsistentNeighbourhoodStyle {
	public SourceMatchInconsistentEndpointStyle() {
		super(SourceMatchConsistencyRule.START_NEIGHBOURHOOD_KEY,
				SourceMatchConsistencyRule.END_NEIGHBOURHOOD_KEY);
	}

	protected Coordinate startCoordinate(SourceRoadSegment roadSegment) {
		return roadSegment.getStartNode().getCoordinate();
	}

	protected Coordinate endCoordinate(SourceRoadSegment roadSegment) {
		return roadSegment.getEndNode().getCoordinate();
	}

}