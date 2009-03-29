package com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

/**
 * Based on the apparent lines from both networks
 */
public class ApparentNode {
	public ApparentNode(Coordinate coordinate, ConflationSession session) {
		this.coordinate = coordinate;
		this.session = session;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public Set getIncidentRoadSegments() {
		return getIncidentRoadSegments(session.getSourceNetwork(0)
				.getRoadSegmentApparentEnvelopeIndex(), session
				.getSourceNetwork(1).getRoadSegmentApparentEnvelopeIndex());
	}

	public Set getIncidentRoadSegments(
			SpatialIndex roadSegmentApparentEnvelopeIndex0,
			SpatialIndex roadSegmentApparentEnvelopeIndex1) {
		if (incidentRoadSegments == null) {
			incidentRoadSegments = new HashSet();
			incidentRoadSegments
					.addAll(incidentRoadSegments(roadSegmentApparentEnvelopeIndex0));
			incidentRoadSegments
					.addAll(incidentRoadSegments(roadSegmentApparentEnvelopeIndex1));
		}
		return incidentRoadSegments;
	}

	public ConflationSession getSession() {
		return session;
	}

	private boolean incidentOn(SourceRoadSegment roadSegment) {
		return coordinate.equals(roadSegment.getApparentStartCoordinate())
				|| coordinate
						.equals(roadSegment.getApparentEndCoordinate());
	}

	private Collection incidentRoadSegments(
			SpatialIndex roadSegmentApparentEnvelopeIndex) {
		return CollectionUtil.select(roadSegmentApparentEnvelopeIndex
				.query(new Envelope(coordinate)), new Block() {
			public Object yield(Object roadSegment) {
				return Boolean
						.valueOf(incidentOn((SourceRoadSegment) roadSegment));
			}
		});
	}

	private Coordinate coordinate;

	private Set incidentRoadSegments = null;

	private ConflationSession session;

	public Set getIncludedIncidentRoadSegments() {
		return new HashSet(CollectionUtil.select(getIncidentRoadSegments(),
				new Block() {
					public Object yield(Object roadSegment) {
						return Boolean
								.valueOf(((SourceRoadSegment) roadSegment)
										.getState().indicates(
												SourceState.INCLUDED));
					}
				}));
	}
}