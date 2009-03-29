package com.vividsolutions.jcs.conflate.roads.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;

public abstract class AbstractStateTransitionImpactAssessment implements
		StateTransitionImpactAssessment {

	public Set affectedRoadSegments(Collection roadSegments, TaskMonitor monitor) {
		HashSet affectedRoadSegments = new HashSet();
		int j = 0;
		for (Iterator i = roadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			monitor.report(++j, roadSegments.size(), "road segments");
			affectedRoadSegments.addAll(affectedRoadSegments(roadSegment));
		}
		return affectedRoadSegments;
	}

	protected Set affectedRoadSegments(SourceRoadSegment roadSegment) {
		HashSet affectedRoadSegments = new HashSet();
		affectedRoadSegments.add(roadSegment);
		affectedRoadSegments.addAll(neighboursAndTheirMatches(roadSegment));
		if (roadSegment.getState().indicates(SourceState.MATCHED)) {
			affectedRoadSegments.add(roadSegment.getMatchingRoadSegment());
			affectedRoadSegments.addAll(neighboursAndTheirMatches(roadSegment
					.getMatchingRoadSegment()));
		}
		affectedRoadSegments.addAll(ProperIntersectionFinder
				.properlyIntersectingIncludedRoadSegments(roadSegment));
		CollectionUtil.addIfNotNull(roadSegment
				.getLastProperlyIntersectingIncludedRoadSegment(),
				affectedRoadSegments);
		affectedRoadSegments
				.addAll(includedSegmentsInNeighbourhood(roadSegment));
		return affectedRoadSegments;
	}

	protected abstract Collection includedSegmentsInNeighbourhood(
			SourceRoadSegment roadSegment);

	private Collection neighboursAndTheirMatches(SourceRoadSegment roadSegment) {
		HashSet neighboursAndTheirMatches = new HashSet();
		for (Iterator i = neighbours(roadSegment).iterator(); i.hasNext();) {
			SourceRoadSegment neighbour = (SourceRoadSegment) i.next();
			neighboursAndTheirMatches.add(neighbour);
			if (neighbour.getState().indicates(SourceState.MATCHED)) {
				neighboursAndTheirMatches.add(neighbour
						.getMatchingRoadSegment());
			}
		}

		return neighboursAndTheirMatches;
	}

	protected abstract Collection neighbours(SourceRoadSegment roadSegment);

}