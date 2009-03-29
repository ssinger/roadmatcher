package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.StateTransitionImpactAssessment;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchWithStandaloneEliminationConsistencyRule.StandaloneChainVisitor;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.task.TaskMonitor;

public class AdjustedMatchWithStandaloneEliminationStateTransitionImpactAssessment
		implements StateTransitionImpactAssessment {


	private StateTransitionImpactAssessment impactAssessment = new AdjustedMatchStateTransitionImpactAssessment();

	public Set affectedRoadSegments(Collection segments, TaskMonitor monitor) {
		return impactAssessment.affectedRoadSegments(FUTURE_CollectionUtil
				.concatenate(segments, standaloneChainMembers(segments)), monitor);
	}

	private Set standaloneChainMembers(Collection segments) {
		HashSet standaloneChainMembers = new HashSet();
		for (Iterator i = segments.iterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (segment.getState() != SourceState.STANDALONE) {
				continue;
			}
			if (!segment.isInNetwork()) {
				continue;
			}
			standaloneChainMembers.addAll(standaloneChainMembers(segment));
		}
		return standaloneChainMembers;
	}

	private Collection standaloneChainMembers(SourceRoadSegment segment) {
		return FUTURE_CollectionUtil.concatenate(standaloneChainMembers(
				segment, segment.getApparentStartCoordinate()),
				standaloneChainMembers(segment, segment
						.getApparentEndCoordinate()));
	}

	private Collection standaloneChainMembers(SourceRoadSegment segment,
			Coordinate start) {
		final ArrayList standaloneChainMembers = new ArrayList();
		AdjustedMatchWithStandaloneEliminationConsistencyRule
				.visitStandaloneChain(new StandaloneChainVisitor() {
					public void visit(SourceRoadSegment standaloneChainMember,
							Coordinate start, Coordinate end) {
						standaloneChainMembers.add(standaloneChainMember);
					}
				}, start, segment, start);
		return standaloneChainMembers;
	}
}