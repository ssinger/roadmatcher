package com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.plugin.conflate.roads.*;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class AdjustedMatchAutoAdjuster implements AutoAdjuster {
	public Object autoAdjustable(SourceRoadSegment roadSegment,
			Coordinate apparentEndpoint, ConflationSession session) {
		if (roadSegment.getResultState() != ResultState.INCONSISTENT
				|| neighbourhood(roadSegment, apparentEndpoint) == null) {
			return Boolean.FALSE;
		}
		if (unknownIncidentRoadSegmentsPresent(neighbourhood(roadSegment,
				apparentEndpoint), session)) {
			return ErrorMessages.adjustedMatchAutoAdjuster_unknownRoadSegments;
		}
		return Boolean.TRUE;
	}

	private boolean unknownIncidentRoadSegmentsPresent(
			Collection neighbourhood, ConflationSession session) {
		for (Iterator i = neighbourhood.iterator(); i.hasNext();) {
			Coordinate neighbourhoodApparentNodeLocation = (Coordinate) i
					.next();
			if (unknownIncidentRoadSegmentsPresent(
					neighbourhoodApparentNodeLocation, session)) {
				return true;
			}
		}
		return false;
	}

	private boolean unknownIncidentRoadSegmentsPresent(
			Coordinate apparentNodeLocation, ConflationSession session) {
		for (Iterator i = new ApparentNode(
				apparentNodeLocation, session).getIncidentRoadSegments()
				.iterator(); i.hasNext();) {
			SourceRoadSegment incidentRoadSegment = (SourceRoadSegment) i
					.next();
			if (incidentRoadSegment.getState() == SourceState.UNKNOWN) {
				return true;
			}
		}
		return false;
	}

	private Collection neighbourhood(SourceRoadSegment roadSegment,
			Coordinate apparentEndpoint) {
		return (Collection) roadSegment.getResultStateDescription().get(
				neighbourhoodKey(roadSegment, apparentEndpoint));
	}

	private String neighbourhoodKey(SourceRoadSegment roadSegment,
			Coordinate apparentEndpoint) {
		if (roadSegment.getApparentStartCoordinate().equals(apparentEndpoint)) {
			return AdjustedMatchConsistencyRule.START_NEIGHBOURHOOD_KEY;
		}
		if (roadSegment.getApparentEndCoordinate().equals(apparentEndpoint)) {
			return AdjustedMatchConsistencyRule.END_NEIGHBOURHOOD_KEY;
		}
		Assert.shouldNeverReachHere();
		return null;
	}

	public Adjustment adjustment(SourceRoadSegment roadSegment,
			Coordinate apparentEndpoint, ConflationSession session,
			SimpleAdjustmentMethod adjustmentMethod,
			LayerViewPanel layerViewPanel) throws ZeroLengthException {
		ArrayList roadSegments = new ArrayList();
		ArrayList newApparentLines = new ArrayList();
		for (Iterator i = neighbourhood(roadSegment, apparentEndpoint)
				.iterator(); i.hasNext();) {
			Coordinate neighbourhoodApparentNodeLocation = (Coordinate) i
					.next();
			ApparentNode apparentNode = new ApparentNode(
					neighbourhoodApparentNodeLocation, session);
			for (Iterator j = apparentNode.getIncludedIncidentRoadSegments()
					.iterator(); j.hasNext();) {
				SourceRoadSegment incidentRoadSegment = (SourceRoadSegment) j
						.next();
				//If other end has been adjusted, don't lose its adjustment
				//[Jon Aquino 2004-01-16]
				LineString incidentRoadSegmentNewApparentLine = roadSegments
						.contains(incidentRoadSegment) ? (LineString) newApparentLines
						.get(roadSegments.indexOf(incidentRoadSegment))
						: incidentRoadSegment.getApparentLine();
				//May adjust both ends here if they are coincident [Jon Aquino
				// 2004-01-16]
				incidentRoadSegmentNewApparentLine = adjustIfEqual(
						incidentRoadSegmentNewApparentLine,
						incidentRoadSegment, incidentRoadSegment
								.getApparentStartCoordinate(),
						neighbourhoodApparentNodeLocation,
						SimpleAdjustmentMethod.Terminal.START,
						apparentEndpoint, adjustmentMethod, layerViewPanel);
				incidentRoadSegmentNewApparentLine = adjustIfEqual(
						incidentRoadSegmentNewApparentLine,
						incidentRoadSegment, incidentRoadSegment
								.getApparentEndCoordinate(),
						neighbourhoodApparentNodeLocation,
						SimpleAdjustmentMethod.Terminal.END, apparentEndpoint,
						adjustmentMethod, layerViewPanel);
				if (incidentRoadSegmentNewApparentLine.getLength() == 0) {
					throw new ZeroLengthException();
				}
				//Don't just compare endpoints -- compare the whole line,
				//because vertices may have been inserted
				//[Jon Aquino 2004-03-22]
				newApparentLines
						.add(incidentRoadSegment.getLine().equalsExact(
								incidentRoadSegmentNewApparentLine) ? incidentRoadSegment
								.getLine()
								: incidentRoadSegmentNewApparentLine);
				roadSegments.add(incidentRoadSegment);
			}
		}
		return new Adjustment(apparentEndpoint, roadSegments, newApparentLines,
				neighbourhood(roadSegment, apparentEndpoint).size() - 1);
	}

	private LineString adjustIfEqual(LineString line,
			SourceRoadSegment segment, Coordinate a, Coordinate b,
			Terminal terminal, Coordinate target,
			SimpleAdjustmentMethod adjustmentMethod,
			LayerViewPanel layerViewPanel) {
		return a.equals(b) ? adjustmentMethod.adjust(line, segment, terminal,
				target, layerViewPanel) : line;
	}
}