package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public abstract class AbstractNodeConsistencyRule implements ConsistencyRule {

	protected Collection included(Collection nodes) {
		return CollectionUtil.select(nodes, new Block() {
			private boolean included(RoadNode node) {
				for (Iterator i = node.getIncidentRoadSegments().iterator(); i
						.hasNext();) {
					SourceRoadSegment roadSegment = (SourceRoadSegment) i
							.next();
					if (roadSegment.getState().indicates(SourceState.INCLUDED)) {
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

	private Set roadSegmentsMatching(Collection roadSegments) {
		return (Set) FUTURE_CollectionUtil.injectInto(roadSegments,
				new HashSet(), new Block() {
					public Object yield(Object matchingRoadSegments,
							Object roadSegment) {
						CollectionUtil.addIfNotNull(
								((SourceRoadSegment) roadSegment)
										.getMatchingRoadSegment(),
								(Collection) matchingRoadSegments);

						return null;
					}
				});
	}

	public static Set roadSegmentsAndMatches(Collection roadSegments) {
		return (Set) FUTURE_CollectionUtil.injectInto(roadSegments,
				new HashSet(), new Block() {
					public Object yield(Object roadSegmentsAndMatches,
							Object roadSegment) {
						((Set) roadSegmentsAndMatches)
								.add((SourceRoadSegment) roadSegment);
						CollectionUtil.addIfNotNull(
								((SourceRoadSegment) roadSegment)
										.getMatchingRoadSegment(),
								(Set) roadSegmentsAndMatches);
						return null;
					}
				});
	}

	/**
	 * @param roadSegments
	 *            RoadSegments, each of which is either incident on the node or
	 *            matched to one that is
	 */
	protected Set nodesMatching(final RoadNode node, Collection roadSegments) {
		return (Set) FUTURE_CollectionUtil.injectInto(roadSegments,
				new HashSet(), new Block() {
					public Object yield(Object matchingNodes, Object roadSegment) {
						CollectionUtil.addIfNotNull(
								matchingNode((SourceRoadSegment) roadSegment),
								(Set) matchingNodes);

						return null;
					}

					private RoadNode matchingNode(SourceRoadSegment roadSegment) {
						return roadSegment.getStartNode() == node
								|| roadSegment.getEndNode() == node ? node
								: (RoadNode) AbstractNodeConsistencyRule
										.moreSimilarDirectedEdge(
												directedEdge(
														roadSegment
																.getMatchingRoadSegment(),
														node), roadSegment)
										.getFromNode();
					}

					private DirectedEdge directedEdge(
							SourceRoadSegment roadSegment, RoadNode startNode) {
						return assertNotNull(roadSegment.getDirEdge(startNode));
					}

					private DirectedEdge assertNotNull(DirectedEdge directedEdge) {
						Assert.isTrue(directedEdge != null);

						return directedEdge;
					}
				});
	}

	public static Set locations(Collection nodes) {
		return new HashSet(CollectionUtil.collect(nodes, new Block() {
			public Object yield(Object node) {
				return ((RoadNode) node).getCoordinate();
			}
		}));
	}

	public static ResultState.Description nullIfCommentNull(
			ResultState.Description description) {
		return description.getComment() != null ? description : null;
	}

	public static DirectedEdge moreSimilarDirectedEdge(
			DirectedEdge directedEdge, SourceRoadSegment roadSegment) {
		return correspondingNodesMatchByAngle(directedEdge.getFromNode()
				.getCoordinate(), directedEdge.getToNode().getCoordinate(),
				roadSegment.getDirEdge(0).getFromNode().getCoordinate(),
				roadSegment.getDirEdge(0).getToNode().getCoordinate()) ? roadSegment
				.getDirEdge(0)
				: roadSegment.getDirEdge(1);
	}

	public static boolean correspondingNodesMatchByProximity(Coordinate a1,
			Coordinate a2, Coordinate b1, Coordinate b2) {
		return a1.distance(b1) + a2.distance(b2) < a1.distance(b2)
				+ a2.distance(b1);
	}

	public static boolean correspondingNodesMatchByAngle(Coordinate a1,
			Coordinate a2, Coordinate b1, Coordinate b2) {
		return Angle.diff(Angle.angle(a1, a2), Angle.angle(b1, b2)) < Angle
				.diff(Angle.angle(a1, a2), Angle.angle(b2, b1));
	}

	public static ResultState.Description createResultStateDescription(
			ResultState resultState, String errorMessage,
			SourceRoadSegment roadSegment) {
		if (roadSegment == null) {
			return null;
		}
		return new ResultState.Description(resultState)
				.addComment(FUTURE_StringUtil.substitute(errorMessage,
						new Object[]{roadSegment.getFeature().getID() + ""}));
	}
}