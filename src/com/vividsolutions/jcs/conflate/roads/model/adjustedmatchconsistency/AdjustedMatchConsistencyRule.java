package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.model.ResultState.Description;
import com.vividsolutions.jcs.conflate.roads.model.sourcematchconsistency.SourceMatchConsistencyRule;
import com.vividsolutions.jcs.jump.FUTURE_Block;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.FileUtil;

public class AdjustedMatchConsistencyRule implements ConsistencyRule,
		Optimizable {

	public AdjustedMatchConsistencyRule() {
	}

	public Description checkInconsistent(SourceRoadSegment roadSegment) {
		Description description = new Description(ResultState.INCONSISTENT);
		ConflationSession session = roadSegment.getNetwork().getSession();
		SortedSet startNeighbourhood = includedLocationsInNeighbourhood(
				new ApparentNode(roadSegment.getApparentStartCoordinate(),
						session), apparentEnvelopeIndex(session
						.getSourceNetwork(0)), apparentEnvelopeIndex(session
						.getSourceNetwork(1)));
		if (cardinality(startNeighbourhood, session) >= 2) {
			description.addComment(START_NODE_ERROR).put(
					START_NEIGHBOURHOOD_KEY, startNeighbourhood);
		}
		SortedSet endNeighbourhood = includedLocationsInNeighbourhood(
				new ApparentNode(roadSegment.getApparentEndCoordinate(),
						session), apparentEnvelopeIndex(session
						.getSourceNetwork(0)), apparentEnvelopeIndex(session
						.getSourceNetwork(1)));
		if (cardinality(endNeighbourhood, session) >= 2) {
			description.addComment(END_NODE_ERROR).put(END_NEIGHBOURHOOD_KEY,
					endNeighbourhood);
		}
		return description.getComment() != null ? description : null;
	}

	protected int cardinality(Set neighbourhood, ConflationSession session) {
		return neighbourhood.size();
	}

	private static Collection included(Collection apparentNodes,
			final SpatialIndex roadSegmentApparentEnvelopeIndex0,
			final SpatialIndex roadSegmentApparentEnvelopeIndex1) {
		return CollectionUtil.select(apparentNodes, new Block() {
			public Object yield(Object apparentNode) {
				for (Iterator i = ((ApparentNode) apparentNode)
						.getIncidentRoadSegments(
								roadSegmentApparentEnvelopeIndex0,
								roadSegmentApparentEnvelopeIndex1).iterator(); i
						.hasNext();) {
					SourceRoadSegment roadSegment = (SourceRoadSegment) i
							.next();
					if (roadSegment.getState().indicates(SourceState.INCLUDED)) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			}
		});
	}

	private SortedSet includedLocationsInNeighbourhood(ApparentNode n,
			SpatialIndex roadSegmentApparentEnvelopeIndex0,
			SpatialIndex roadSegmentApparentEnvelopeIndex1) {
		return locations(included(apparentNodesInNeighbourhood(n,
				roadSegmentApparentEnvelopeIndex0,
				roadSegmentApparentEnvelopeIndex1),
				roadSegmentApparentEnvelopeIndex0,
				roadSegmentApparentEnvelopeIndex1));
	}

	public static SortedSet locations(Collection apparentNodes) {
		return new TreeSet(CollectionUtil.collect(apparentNodes, new Block() {
			public Object yield(Object node) {
				return ((ApparentNode) node).getCoordinate();
			}
		}));
	}

	public static Collection apparentNodesInNeighbourhood(ApparentNode n,
			SpatialIndex roadSegmentApparentEnvelopeIndex0,
			SpatialIndex roadSegmentApparentEnvelopeIndex1) {
		ArrayList neighbourhood = new ArrayList();
		neighbourhood.addAll(MatchingApparentNodesFinder.instance().apparentNodesMatching(n,
				AbstractNodeConsistencyRule.roadSegmentsAndMatches(n
						.getIncidentRoadSegments(
								roadSegmentApparentEnvelopeIndex0,
								roadSegmentApparentEnvelopeIndex1))));
		//The following loop ensures that we don't forget the match of the
		//neighbour of the match of an incident road segment. [Jon Aquino
		// 12/18/2003]
		for (Iterator i = new ArrayList(neighbourhood).iterator(); i.hasNext();) {
			ApparentNode other = (ApparentNode) i.next();
			neighbourhood.addAll(MatchingApparentNodesFinder.instance().apparentNodesMatching(other,
					AbstractNodeConsistencyRule.roadSegmentsAndMatches(other
							.getIncidentRoadSegments(
									roadSegmentApparentEnvelopeIndex0,
									roadSegmentApparentEnvelopeIndex1))));
		}
		return neighbourhood;
	}

	private SpatialIndex apparentEnvelopeIndex(RoadNetwork network) {
		return (SpatialIndex) apparentEnvelopeIndexGetter.yield(network);
	}

	private static final Block DEFAULT_APPARENT_ENVELOPE_INDEX_GETTER = new Block() {
		public Object yield(Object network) {
			return ((RoadNetwork) network)
					.getRoadSegmentApparentEnvelopeIndex();
		}
	};

	private transient Block apparentEnvelopeIndexGetter = DEFAULT_APPARENT_ENVELOPE_INDEX_GETTER;

	/**
	 * This is not dead code! #readObject is a special serialization method.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		apparentEnvelopeIndexGetter = DEFAULT_APPARENT_ENVELOPE_INDEX_GETTER;
	}

	public static final String END_NEIGHBOURHOOD_KEY = AdjustedMatchConsistencyRule.class + " - END NEIGHBOURHOOD";

	public static final String START_NEIGHBOURHOOD_KEY = AdjustedMatchConsistencyRule.class + " - START NEIGHBOURHOOD";

	/**
	 * Temporarily switch from LinearScanSpatialIndex (always up to date) to
	 * STRtree (which is faster but only a current snapshot of the data).
	 */
	public void doOptimizedOp(Block op) {
		final SpatialIndex[] fastSpatialIndices = new SpatialIndex[2];
		this.apparentEnvelopeIndexGetter = new Block() {
			public Object yield(Object networkObject) {
				RoadNetwork network = (RoadNetwork) networkObject;
				if (fastSpatialIndices[network.getID()] == null) {
					fastSpatialIndices[network.getID()] = createFastSpatialIndex(network);
				}
				return fastSpatialIndices[network.getID()];
			}

			private SpatialIndex createFastSpatialIndex(RoadNetwork network) {
				SpatialIndex fastSpatialIndex = new STRtree();
				for (Iterator i = network.getGraph().getEdges().iterator(); i
						.hasNext();) {
					SourceRoadSegment roadSegment = (SourceRoadSegment) i
							.next();
					fastSpatialIndex.insert(roadSegment.getApparentLine()
							.getEnvelopeInternal(), roadSegment);
				}
				return fastSpatialIndex;
			}
		};
		try {
			op.yield();
		} finally {
			this.apparentEnvelopeIndexGetter = DEFAULT_APPARENT_ENVELOPE_INDEX_GETTER;
		}
	}

	private StateTransitionImpactAssessment stateTransitionImpactAssessment = new AdjustedMatchStateTransitionImpactAssessment();

	public StateTransitionImpactAssessment getStateTransitionImpactAssessment() {
		return stateTransitionImpactAssessment;
	}

	private Block UNKNOWN_START_NEIGHBOUR_BLOCK = new FUTURE_Block() {
		public Object yield(Object segment) {
			for (Iterator i = new ApparentNode(((SourceRoadSegment) segment)
					.getApparentStartCoordinate(),
					((SourceRoadSegment) segment).getNetwork().getSession())
					.getIncidentRoadSegments().iterator(); i.hasNext();) {
				SourceRoadSegment other = (SourceRoadSegment) i.next();
				if (other == segment) {
					continue;
				}
				if (other.getState() == SourceState.UNKNOWN) {
					return other;
				}
			}
			return null;
		}
	};

	private Block UNKNOWN_END_NEIGHBOUR_BLOCK = new FUTURE_Block() {
		public Object yield(Object segment) {
			for (Iterator i = new ApparentNode(((SourceRoadSegment) segment)
					.getApparentEndCoordinate(), ((SourceRoadSegment) segment)
					.getNetwork().getSession()).getIncidentRoadSegments()
					.iterator(); i.hasNext();) {
				SourceRoadSegment other = (SourceRoadSegment) i.next();
				if (other == segment) {
					continue;
				}
				if (other.getState() == SourceState.UNKNOWN) {
					return other;
				}
			}
			return null;
		}
	};

	public Description checkPending(SourceRoadSegment roadSegment) {
		return SourceMatchConsistencyRule.checkPending(roadSegment,
				UNKNOWN_START_NEIGHBOUR_BLOCK, UNKNOWN_END_NEIGHBOUR_BLOCK);
	}
}