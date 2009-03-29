package com.vividsolutions.jcs.conflate.roads.model;
import java.util.Collection;

import com.vividsolutions.jcs.conflate.roads.ErrorMessages;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jump.util.Block;
public class ResultStateRules {
	private static ResultStateRules instance = new ResultStateRules();
	public static final String PROPERLY_INTERSECTING_ROAD_SEGMENT_KEY = ResultStateRules.class + " - PROPERLY INTERSECTING ROAD SEGMENT";
	public static final String INTERSECTION_KEY = ResultStateRules.class + " - INTERSECTION";
	private ResultStateRules() {
	}
	private ResultState.Description inconsistentResultStateDescription(
			SourceRoadSegment roadSegment) {
		ResultState.Description resultStateDescription = new ResultState.Description(
				ResultState.INCONSISTENT);
		addIfNotNull(intersectionResultStateDescription(roadSegment),
				resultStateDescription);
		addIfNotNull(inconsistentWithRulesResultStateDescription(roadSegment),
				resultStateDescription);
		return resultStateDescription.getComment() != null ? resultStateDescription
				: null;
	}
	private void addIfNotNull(Description source, Description destination) {
		if (source == null) {
			return;
		}
		destination.addComment(source.getComment());
		destination.getBlackboard().getProperties().putAll(
				source.getBlackboard().getProperties());
	}
	private ResultState.Description inconsistentWithRulesResultStateDescription(
			SourceRoadSegment roadSegment) {
		return roadSegment
				.getNetwork().getSession().getConsistencyRule().checkInconsistent(
						roadSegment);
	}
	private ResultState.Description intersectionResultStateDescription(
			SourceRoadSegment roadSegment) {
		SourceRoadSegment properlyIntersectingIncludedRoadSegment = (SourceRoadSegment) FUTURE_CollectionUtil
				.firstOrNull(ProperIntersectionFinder.properlyIntersectingIncludedRoadSegments(roadSegment));
		ResultState.Description resultStateDescription = AbstractNodeConsistencyRule.createResultStateDescription(
				ResultState.INCONSISTENT,
				ErrorMessages.resultStateRules_intersection,
				properlyIntersectingIncludedRoadSegment);
		return resultStateDescription != null ? resultStateDescription.put(
				PROPERLY_INTERSECTING_ROAD_SEGMENT_KEY,
				properlyIntersectingIncludedRoadSegment).put(
				INTERSECTION_KEY,
				properlyIntersectingIncludedRoadSegment.getApparentLine()
						.intersection(roadSegment.getApparentLine())
						.getCoordinate()) : null;
	}
	public ResultState.Description determineResultState(
			SourceRoadSegment roadSegment) {
		if (!roadSegment.getState().indicates(SourceState.INCLUDED)) {
			return new ResultState.Description(null);
		}
		if (!roadSegment.isInNetwork()) {
			return new ResultState.Description(null);
		}
		ResultState.Description inconsistentResultStateDescription = inconsistentResultStateDescription(roadSegment);
		if (inconsistentResultStateDescription != null) {
			return inconsistentResultStateDescription;
		}
		ResultState.Description pendingResultStateDescription = pendingResultStateDescription(roadSegment);
		if (pendingResultStateDescription != null) {
			return pendingResultStateDescription;
		}
		return new ResultState.Description(ResultState.INTEGRATED);
	}
	private Description pendingResultStateDescription(SourceRoadSegment roadSegment) {
		return roadSegment
		.getNetwork().getSession().getConsistencyRule().checkPending(
				roadSegment);
	}
	public static ResultStateRules instance() {
		return instance;
	}
}
