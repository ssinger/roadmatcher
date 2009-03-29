package com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency;

import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.NeighbourhoodList;
import com.vividsolutions.jcs.conflate.roads.model.ResultState;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetworkFeatureCollection;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.plugin.conflate.roads.AbstractInconsistentNeighbourhoodStyle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;

public class AdjustedMatchInconsistentEndpointStyle
		extends
			AbstractInconsistentNeighbourhoodStyle {
	private NeighbourhoodList neighbourhoodsPainted;
	private RoadNetwork network;
	public AdjustedMatchInconsistentEndpointStyle() {
		super(AdjustedMatchConsistencyRule.START_NEIGHBOURHOOD_KEY,
				AdjustedMatchConsistencyRule.END_NEIGHBOURHOOD_KEY);
	}
	public void initialize(Layer layer) {
		neighbourhoodsPainted = new NeighbourhoodList();
		network = ((RoadNetworkFeatureCollection) layer
				.getFeatureCollectionWrapper().getUltimateWrappee())
				.getNetwork();
		super.initialize(layer);
	}
	protected void paint(Coordinate endpoint, Object neighbourhood,
			Graphics2D g, Viewport viewport)
			throws NoninvertibleTransformException {
		SortedSet largestAssociatedNeighbourhood = largestAssociatedNeighbourhood((Collection) neighbourhood);
		if (neighbourhoodsPainted.contains(largestAssociatedNeighbourhood)) {
			return;
		}
		//HACK: If the neighbourhood involves both datasets, only paint it when
		//we paint the first dataset -- no need to paint it when we paint the
		//second dataset (in fact it's bad because it paints over the nodes of
		//the first dataset) [Jon Aquino 2004-06-07]
		if (network.getID() == 0
				&& hasIncidentIncludedSegments(1,
						largestAssociatedNeighbourhood)) {
			return;
		}
		super.paint(endpoint, largestAssociatedNeighbourhood, g, viewport);
		neighbourhoodsPainted.add(largestAssociatedNeighbourhood);
	}
	private boolean hasIncidentIncludedSegments(int networkID,
			SortedSet neighbourhood) {
		for (Iterator i = neighbourhood.iterator(); i.hasNext();) {
			Coordinate coordinate = (Coordinate) i.next();
			for (Iterator j = new ApparentNode(coordinate, network.getSession())
					.getIncludedIncidentRoadSegments().iterator(); j.hasNext();) {
				SourceRoadSegment roadSegment = (SourceRoadSegment) j.next();
				if (roadSegment.getNetworkID() == networkID) {
					return true;
				}
			}
		}
		return false;
	}
	private SortedSet largestAssociatedNeighbourhood(Collection neighbourhood) {
		return associatedNeighbourhoods(neighbourhood, network.getSession())
				.largest();
	}
	public static NeighbourhoodList associatedNeighbourhoods(
			Collection coordinates, ConflationSession session) {
		NeighbourhoodList associatedNeighbourhoods = new NeighbourhoodList();
		findAssociatedNeighbourhoods(coordinates, associatedNeighbourhoods,
				new HashSet(), session);
		return associatedNeighbourhoods;
	}
	private static void findAssociatedNeighbourhoods(Collection coordinates,
			NeighbourhoodList associatedNeighbourhoods,
			Set coordinatesEncountered, ConflationSession session) {
		for (Iterator i = coordinates.iterator(); i.hasNext();) {
			Coordinate coordinate = (Coordinate) i.next();
			findAssociatedNeighbourhoods(coordinate, associatedNeighbourhoods,
					coordinatesEncountered, session);
		}
	}
	private static void findAssociatedNeighbourhoods(Coordinate coordinate,
			NeighbourhoodList associatedNeighbourhoods,
			Set coordinatesEncountered, ConflationSession session) {
		if (coordinatesEncountered.contains(coordinate)) {
			return;
		}
		coordinatesEncountered.add(coordinate);
		for (Iterator j = new ApparentNode(coordinate, session)
				.getIncludedIncidentRoadSegments().iterator(); j.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) j.next();
			if (roadSegment.getResultState() != ResultState.INCONSISTENT) {
				continue;
			}
			Collection associatedNeighbourhood = associatedNeighbourhood(
					coordinate, roadSegment);
			if (associatedNeighbourhood == null
					|| associatedNeighbourhoods.contains(new TreeSet(
							associatedNeighbourhood))) {
				continue;
			}
			associatedNeighbourhoods.add(new TreeSet(associatedNeighbourhood));
			//Must find neighbourhoods for new coordinates revealed.
			//e.g. for the Fernie cul-de-sac [Jon Aquino 2004-06-07]
			findAssociatedNeighbourhoods(associatedNeighbourhood,
					associatedNeighbourhoods, coordinatesEncountered, session);
		}
	}
	private static Collection associatedNeighbourhood(Coordinate coordinate,
			SourceRoadSegment roadSegment) {
		return (Collection) roadSegment
				.getResultStateDescription()
				.get(
						roadSegment.getApparentStartCoordinate().equals(
								coordinate)
								? AdjustedMatchConsistencyRule.START_NEIGHBOURHOOD_KEY
								: roadSegment.getApparentEndCoordinate()
										.equals(coordinate)
										? AdjustedMatchConsistencyRule.END_NEIGHBOURHOOD_KEY
										: FUTURE_Assert.shouldNeverReachHere2()
												+ "");
	}
}