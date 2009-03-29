package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.AbstractNodeConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class MatchingApparentNodesFinder {
	
	private MatchingApparentNodesFinder() {}
	
	private static MatchingApparentNodesFinder instance = new MatchingApparentNodesFinder();
	
	public static MatchingApparentNodesFinder instance() { return instance; }

	public ApparentNode matchingApparentNode(ApparentNode n,
			SourceRoadSegment roadSegment, SourceRoadSegment matchingRoadSegment) {
		//Angle filters out 99%; distance filters out only 85% [Jon
		// Aquino 2004-07-22]
		if (angleIndicatesNodeMatchingByTopologyRequired(roadSegment
				.getApparentStartCoordinate(), roadSegment
				.getApparentEndCoordinate(), matchingRoadSegment
				.getApparentStartCoordinate(), matchingRoadSegment
				.getApparentEndCoordinate())) {
			ApparentNode apparentNodeMatchingByTopology = apparentNodeMatchingByTopology(
					n, roadSegment, matchingRoadSegment);
			if (apparentNodeMatchingByTopology != null) {
				return apparentNodeMatchingByTopology;
			}
		}

		// Not enough information to compute the matching apparent node
		// using the surrounding topology, so go with Plan B: a
		// reasonable proximity criterion [Jon Aquino 2004-07-16]
		return apparentNodeMatchingByAngle(n, roadSegment, matchingRoadSegment);
	}

	private boolean distanceIndicatesNodeMatchingByTopologyRequired(
			Coordinate a0, Coordinate a1, Coordinate b0, Coordinate b1) {
		return Math.min(a0.distance(b0) + a1.distance(b1), a0.distance(b1)
				+ a1.distance(b0)) > 0.25 * Math.min(a0.distance(a1), b0
				.distance(b1));
	}

	private boolean angleIndicatesNodeMatchingByTopologyRequired(Coordinate a0,
			Coordinate a1, Coordinate b0, Coordinate b1) {
		return Math.min(Angle.angleBetween(a0, a1, CoordUtil.add(b1, CoordUtil
				.subtract(a0, b0))), Angle.angleBetween(a0, a1, CoordUtil.add(
				b0, CoordUtil.subtract(a0, b1)))) > Angle
				.toRadians(MAX_ANGLE_FOR_NODE_MATCHING_BY_ANGLE);
	}

	private ApparentNode apparentNodeMatchingByProximity(ApparentNode n,
			SourceRoadSegment roadSegment, SourceRoadSegment matchingRoadSegment) {
		return new ApparentNode(AbstractNodeConsistencyRule
				.correspondingNodesMatchByProximity(roadSegment
						.getApparentStartCoordinate(), roadSegment
						.getApparentEndCoordinate(), n.getCoordinate(), other(n
						.getCoordinate(), matchingRoadSegment)) ? roadSegment
				.getApparentStartCoordinate() : roadSegment
				.getApparentEndCoordinate(), n.getSession());
	}

	private ApparentNode apparentNodeMatchingByAngle(ApparentNode n,
			SourceRoadSegment roadSegment, SourceRoadSegment matchingRoadSegment) {
		return new ApparentNode(AbstractNodeConsistencyRule
				.correspondingNodesMatchByAngle(roadSegment
						.getApparentStartCoordinate(), roadSegment
						.getApparentEndCoordinate(), n.getCoordinate(), other(n
						.getCoordinate(), matchingRoadSegment)) ? roadSegment
				.getApparentStartCoordinate() : roadSegment
				.getApparentEndCoordinate(), n.getSession());
	}

	private ApparentNode apparentNodeMatchingByTopology(ApparentNode n,
			SourceRoadSegment roadSegment, SourceRoadSegment matchingRoadSegment) {
		ApparentNode a1 = n;
		ApparentNode a2 = new ApparentNode(other(n.getCoordinate(),
				matchingRoadSegment), n.getSession());
		Collection nodesOneHopAwayFromA1 = nodesOneHopAway(a1,
				matchingRoadSegment);
		Collection nodesOneHopAwayFromA2 = nodesOneHopAway(a2,
				matchingRoadSegment);
		Coordinate b1 = roadSegment.getApparentStartCoordinate();
		Coordinate b2 = roadSegment.getApparentEndCoordinate();
		// I'm doing 1 contains check and 2 !contains checks.
		// 2 contains checks would be too strict. [Jon Aquino
		// 2004-07-16]
		if (nodesOneHopAwayFromA1.contains(b1)
				&& !nodesOneHopAwayFromA1.contains(b2)
				&& !nodesOneHopAwayFromA2.contains(b1)) {
			return new ApparentNode(b1, roadSegment.getNetwork().getSession());
		}
		if (nodesOneHopAwayFromA1.contains(b2)
				&& !nodesOneHopAwayFromA1.contains(b1)
				&& !nodesOneHopAwayFromA2.contains(b2)) {
			return new ApparentNode(b2, roadSegment.getNetwork().getSession());
		}
		if (nodesOneHopAwayFromA2.contains(b1)
				&& !nodesOneHopAwayFromA2.contains(b2)
				&& !nodesOneHopAwayFromA1.contains(b1)) {
			return new ApparentNode(b2, roadSegment.getNetwork().getSession());
		}
		if (nodesOneHopAwayFromA2.contains(b2)
				&& !nodesOneHopAwayFromA2.contains(b1)
				&& !nodesOneHopAwayFromA1.contains(b2)) {
			return new ApparentNode(b1, roadSegment.getNetwork().getSession());
		}
		return null;
	}

	private Set nodesOneHopAway(ApparentNode node,
			SourceRoadSegment segmentToIgnore) {
		Set nodesOneHopAway = new HashSet();
		for (Iterator i = node.getIncidentRoadSegments().iterator(); i
				.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (segment == segmentToIgnore) {
				continue;
			}
			if (!segment.getState().indicates(SourceState.MATCHED)) {
				continue;
			}
			nodesOneHopAway.add(segment.getMatchingRoadSegment()
					.getApparentStartCoordinate());
			nodesOneHopAway.add(segment.getMatchingRoadSegment()
					.getApparentEndCoordinate());
		}
		return nodesOneHopAway;
	}

	/**
	 * @param roadSegments
	 *            RoadSegments, each of which is either incident on the apparent
	 *            node or matched to one that is
	 */
	public Collection apparentNodesMatching(final ApparentNode n,
			Collection roadSegments) {
		return new HashSet(CollectionUtil.collect(roadSegments, new Block() {
			private ApparentNode myMatchingApparentNode(
					SourceRoadSegment roadSegment) {
				return roadSegment.getApparentStartCoordinate().equals(
						n.getCoordinate())
						|| roadSegment.getApparentEndCoordinate().equals(
								n.getCoordinate()) ? n : matchingApparentNode(
						n, roadSegment, roadSegment.getMatchingRoadSegment());
			}

			public Object yield(Object roadSegment) {
				return myMatchingApparentNode((SourceRoadSegment) roadSegment);
			}
		}));
	}

	private static final int MAX_ANGLE_FOR_NODE_MATCHING_BY_ANGLE = 45;

	private Coordinate other(Coordinate endpoint, SourceRoadSegment roadSegment) {
		return endpoint.equals(roadSegment.getApparentStartCoordinate()) ? roadSegment
				.getApparentEndCoordinate()
				: endpoint.equals(roadSegment.getApparentEndCoordinate()) ? roadSegment
						.getApparentStartCoordinate()
						: assertionFailure();
	}

	private Coordinate assertionFailure() {
		Assert.shouldNeverReachHere();
		return null;
	}

}