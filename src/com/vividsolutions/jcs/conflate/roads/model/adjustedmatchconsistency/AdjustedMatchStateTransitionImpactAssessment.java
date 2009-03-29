package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.AbstractStateTransitionImpactAssessment;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class AdjustedMatchStateTransitionImpactAssessment extends
		AbstractStateTransitionImpactAssessment {

	protected Collection includedSegmentsInNeighbourhood(
			SourceRoadSegment roadSegment) {
		return FUTURE_CollectionUtil.concatenate(
				includedSegmentsInNeighbourhood(roadSegment.getApparentLine(),
						roadSegment.getNetwork().getSession()),
				includedSegmentsInNeighbourhood(roadSegment
						.getPreviousApparentLine(), roadSegment.getNetwork()
						.getSession()));
	}

	private Collection includedSegmentsInNeighbourhood(LineString apparentLine,
			ConflationSession session) {
		return FUTURE_CollectionUtil.concatenate(
				includedSegmentsInNeighbourhood(FUTURE_LineString
						.first(apparentLine), session),
				includedSegmentsInNeighbourhood(FUTURE_LineString
						.last(apparentLine), session));
	}

	private Collection includedSegmentsInNeighbourhood(
			Coordinate apparentNodeLocation, ConflationSession session) {
		ArrayList includedSegmentsInNeighbourhood = new ArrayList();
		for (Iterator i = AdjustedMatchConsistencyRule
				.apparentNodesInNeighbourhood(
						new ApparentNode(
								apparentNodeLocation, session),
						session.getSourceNetwork(0)
								.getRoadSegmentApparentEnvelopeIndex(),
						session.getSourceNetwork(1)
								.getRoadSegmentApparentEnvelopeIndex())
				.iterator(); i.hasNext();) {
			ApparentNode apparentNode = (ApparentNode) i.next();
			includedSegmentsInNeighbourhood.addAll(apparentNode
					.getIncidentRoadSegments());
		}
		return includedSegmentsInNeighbourhood;
	}

	protected Collection neighbours(SourceRoadSegment roadSegment) {
		//Unlike SourceMatchStateTransitionImpactAssessment, this works even
		//if roadSegment#isInNetwork is false i.e. roadSegment has been removed
		//because a split node has been created. Anyway, even if it weren't
		//working, neighbouring segments would get their states updated
		//by the impact of the added segment. [Jon Aquino 2004-07-20]
		return FUTURE_CollectionUtil.concatenate(neighbours(roadSegment
				.getApparentLine(), roadSegment.getNetwork().getSession()),
				neighbours(roadSegment.getPreviousApparentLine(), roadSegment
						.getNetwork().getSession()));
	}

	private Collection neighbours(LineString apparentLine,
			ConflationSession session) {
		return FUTURE_CollectionUtil.concatenate(neighbours(FUTURE_LineString
				.first(apparentLine), session), neighbours(FUTURE_LineString
				.last(apparentLine), session));
	}

	private Collection neighbours(Coordinate apparentNodeLocation,
			ConflationSession session) {
		return new ApparentNode(
				apparentNodeLocation, session).getIncidentRoadSegments();
	}

}