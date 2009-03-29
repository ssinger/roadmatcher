package com.vividsolutions.jcs.conflate.roads.model.resultconsistency;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class UnadjustedRoadSegmentNodeConsistencyRule extends
		AbstractNodeConsistencyRule {

	public static final String START_NEIGHBOURHOOD_KEY = UnadjustedRoadSegmentNodeConsistencyRule.class + " - START NODE NEIGHBOURHOOD";

	public static final String END_NEIGHBOURHOOD_KEY = UnadjustedRoadSegmentNodeConsistencyRule.class + " - END NODE NEIGHBOURHOOD";

	public ResultState.Description checkInconsistent(
			SourceRoadSegment roadSegment) {
		ResultState.Description description = new ResultState.Description(
				ResultState.INCONSISTENT);
		Set locationsOfIncludedNodesMatchingStartNode = locationsOfIncludedNodesMatching(roadSegment
				.getStartNode());
		if (locationsOfIncludedNodesMatchingStartNode.size() >= 2) {
			description.addComment(START_NODE_ERROR).put(
					START_NEIGHBOURHOOD_KEY,
					locationsOfIncludedNodesMatchingStartNode);
		}
		Set locationsOfIncludedNodesMatchingEndNode = locationsOfIncludedNodesMatching(roadSegment
				.getEndNode());
		if (locationsOfIncludedNodesMatchingEndNode.size() >= 2) {
			description.addComment(END_NODE_ERROR).put(END_NEIGHBOURHOOD_KEY,
					locationsOfIncludedNodesMatchingEndNode);
		}
		return nullIfCommentNull(description);
	}

	private Set locationsOfIncludedNodesMatching(RoadNode node) {
		return locations(unadjustablyIncluded(nodesMatching(node,
				roadSegmentsAndMatches(node.getIncidentRoadSegments()))));
	}

	protected Collection unadjustablyIncluded(Collection nodes) {
		return CollectionUtil.select(nodes, new Block() {
			private boolean included(RoadNode node) {
				for (Iterator i = node.getIncidentRoadSegments().iterator(); i
						.hasNext();) {
					SourceRoadSegment roadSegment = (SourceRoadSegment) i
							.next();
					if (!roadSegment.isAdjusted()
							&& roadSegment.getState().indicates(
									SourceState.INCLUDED)) {
						return true;
					}
				}

				return false;
			}

			public Object yield(Object node) {
				return Boolean.valueOf(included((RoadNode) node));
			}
		});
	}

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		Assert.shouldNeverReachHere();
		return null;
	}

	public Description checkPending(SourceRoadSegment roadSegment) {
		throw new UnsupportedOperationException();
	}

}