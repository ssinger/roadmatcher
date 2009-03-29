package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.Optimizable;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.StateTransitionImpactAssessment;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class AdjustedMatchWithStandaloneEliminationConsistencyRule implements
		ConsistencyRule, Optimizable {

	private AdjustedMatchConsistencyRule rule = new AdjustedMatchConsistencyRule() {

		protected int cardinality(Set neighbourhood, ConflationSession session) {
			return AdjustedMatchWithStandaloneEliminationConsistencyRule.this
					.cardinality((Coordinate[]) neighbourhood
							.toArray(new Coordinate[] {}), session);
		}
	};

	private StateTransitionImpactAssessment stateTransitionImpactAssessment = new AdjustedMatchWithStandaloneEliminationStateTransitionImpactAssessment();

	private Set connectedCoordinates(Coordinate coordinate, RoadNetwork network) {
		Set connectedCoordinates = new HashSet();
		for (Iterator i = standaloneSegments(
				new ApparentNode(coordinate, network.getSession()), network)
				.iterator(); i.hasNext();) {
			SourceRoadSegment standaloneSegment = (SourceRoadSegment) i.next();
			ConnectedCoordinateFinder finder = new ConnectedCoordinateFinder();
			visitStandaloneChain(finder, coordinate, standaloneSegment,
					coordinate);
			connectedCoordinates.add(finder.end);
		}
		return connectedCoordinates;
	}

	public static interface StandaloneChainVisitor {
		void visit(SourceRoadSegment standaloneSegment, Coordinate start,
				Coordinate end);
	}

	private class ConnectedCoordinateFinder implements StandaloneChainVisitor {
		public void visit(SourceRoadSegment standaloneSegment,
				Coordinate start, Coordinate end) {
			this.end = end;
		}

		public Coordinate end;
	}

	public static void visitStandaloneChain(StandaloneChainVisitor visitor,
			Coordinate start, SourceRoadSegment standaloneSegment,
			Coordinate veryStart) {
		Assert.isTrue(standaloneSegment.getState() == SourceState.STANDALONE);
		Coordinate end = start.equals(standaloneSegment
				.getApparentStartCoordinate()) ? standaloneSegment
				.getApparentEndCoordinate() : standaloneSegment
				.getApparentStartCoordinate();
		visitor.visit(standaloneSegment, start, end);
		// Loops [Jon Aquino 2004-07-14]
		if (end.equals(veryStart)) {
			return;
		}
		// Small popsicles i.e. hit a 3-way, but 2 of those ways belong to the
		// same segment. Not caught by the loop check above. [Jon Aquino
		// 2004-07-14]
		if (end.equals(start)) {
			return;
		}
		Set segmentsAtEnd = new ApparentNode(end, standaloneSegment
				.getNetwork().getSession()).getIncidentRoadSegments();
		if (segmentsAtEnd.size() != 2) {
			return;
		}
		Assert.isTrue(segmentsAtEnd.remove(standaloneSegment));
		SourceRoadSegment nextSegment = (SourceRoadSegment) segmentsAtEnd
				.iterator().next();
		if (!allowedToBeInStandaloneChain(nextSegment, standaloneSegment
				.getNetwork())) {
			return;
		}
		visitStandaloneChain(visitor, end, nextSegment, veryStart);
	}

	private static boolean allowedToBeInStandaloneChain(
			SourceRoadSegment segment, RoadNetwork network) {
		return segment.getState() == SourceState.STANDALONE
				&& segment.getNetwork() == network;
	}

	private Set standaloneSegments(ApparentNode node, RoadNetwork network) {
		HashSet standaloneSegments = new HashSet();
		for (Iterator i = node.getIncidentRoadSegments().iterator(); i
				.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (!allowedToBeInStandaloneChain(segment, network)) {
				continue;
			}
			standaloneSegments.add(segment);
		}
		return standaloneSegments;
	}

	public Description checkInconsistent(SourceRoadSegment roadSegment) {
		return rule.checkInconsistent(roadSegment);
	}

	public void doOptimizedOp(Block op) {
		rule.doOptimizedOp(op);
	}

	private int cardinality(Coordinate[] neighbourhood,
			ConflationSession session) {
		// Only allow glomming if there are 2 nodes. Glomming more than
		// two nodes is tricky because you need to account for the order
		// in which the nodes are processed -- iteration would work.
		// e.g. if the nodes are connected like so -- A-B-C -- make sure
		// your algorithm works whether they are processed in the order
		// A, B, C or B, A, C. Anyway, anyway, Martin and I discussed the
		// possibility of glomming more than 2 nodes, and we concluded that it
		// is too loose. [Jon Aquino 2004-07-15]
		if (defaultCardinality(neighbourhood) != 2) {
			return defaultCardinality(neighbourhood);
		}
		return cardinality(neighbourhood[0], neighbourhood[1], session);
	}

	private int cardinality(Coordinate a, Coordinate b,
			ConflationSession session) {
		// There must be sufficient complexity at either end of the
		// standalone chain before we collapse it.
		// Example: 648679.6652654019, 5484254.272197577 in Fernie.
		// [Jon Aquino 2004-07-15]
		Collection matchedReferenceSegmentsIncidentOnA = incidentMatchedReferenceSegments(
				a, session);
		Collection matchedReferenceSegmentsIncidentOnB = incidentMatchedReferenceSegments(
				b, session);
		if (matchedReferenceSegmentsIncidentOnA.size() < 2) {
			return 2;
		}
		if (matchedReferenceSegmentsIncidentOnB.size() < 2) {
			return 2;
		}
		Set networksWithMatchedReferenceSegments = networks(FUTURE_CollectionUtil
				.concatenate(matchedReferenceSegmentsIncidentOnA,
						matchedReferenceSegmentsIncidentOnB));
		if (networksWithMatchedReferenceSegments.size() != 1) {
			return 2;
		}
		if (!matchingSegmentsMeetAtPoint(a, b,
				matchedReferenceSegmentsIncidentOnA,
				matchedReferenceSegmentsIncidentOnB, session)) {
			return 2;
		}
		return connectedCoordinates(
				a,
				(RoadNetwork) networksWithMatchedReferenceSegments.iterator()
						.next()).contains(b) ? 1 : 2;
	}

	private Set networks(Collection segments) {
		return new HashSet(CollectionUtil.collect(segments, new Block() {
			public Object yield(Object segment) {
				return ((SourceRoadSegment) segment).getNetwork();
			}
		}));
	}

	private boolean matchingSegmentsMeetAtPoint(Coordinate a, Coordinate b,
			Collection matchedSegmentsIncidentOnA,
			Collection matchedSegmentsIncidentOnB, ConflationSession session) {
		return AdjustedMatchConsistencyRule
				.locations(
						FUTURE_CollectionUtil
								.concatenate(
										MatchingApparentNodesFinder
												.instance()
												.apparentNodesMatching(
														new ApparentNode(a,
																session),
														matchingSegments(matchedSegmentsIncidentOnA)),
										MatchingApparentNodesFinder
												.instance()
												.apparentNodesMatching(
														new ApparentNode(b,
																session),
														matchingSegments(matchedSegmentsIncidentOnB))))
				.size() == 1;
	}

	private Collection matchingSegments(Collection segments) {
		return CollectionUtil.collect(segments, new Block() {
			public Object yield(Object segment) {
				return ((SourceRoadSegment) segment).getMatchingRoadSegment();
			}
		});
	}

	private Collection incidentMatchedReferenceSegments(Coordinate coordinate,
			ConflationSession session) {
		return CollectionUtil.select(new ApparentNode(coordinate, session)
				.getIncidentRoadSegments(), new Block() {
			public Object yield(Object segment) {
				return Boolean
						.valueOf(((SourceRoadSegment) segment).getState() == SourceState.MATCHED_REFERENCE);
			}
		});
	}

	private int defaultCardinality(Coordinate[] neighbourhood) {
		return neighbourhood.length;
	}

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		return stateTransitionImpactAssessment = new AdjustedMatchWithStandaloneEliminationStateTransitionImpactAssessment();
	}

	public Description checkPending(SourceRoadSegment roadSegment) {
		return rule.checkPending(roadSegment);
	}

}