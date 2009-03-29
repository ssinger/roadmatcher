package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jts.util.Assert;

public class Statistics implements RoadsListener, Serializable {

	public Statistics(ConflationSession session) {
		this.session = session;
	}

	private void clear() {
		get(0).clear();
		get(1).clear();
	}

	public NetworkStatistics get(int network) {
		if (networkStatistics == null) {
			networkStatistics = new NetworkStatistics[] {
					new NetworkStatistics(session.getSourceNetwork(0)),
					new NetworkStatistics(session.getSourceNetwork(1)) };
		}
		return networkStatistics[network];
	}

	public double getNearness() {
		double nearnessLengthProductSum = 0;
		double lengthSum = 0;
		for (Iterator i = session.getSourceNetwork(0).getFeatureCollection()
				.iterator(); i.hasNext();) {
			SourceFeature feature = (SourceFeature) i.next();
			SourceRoadSegment segment = feature.getRoadSegment();
			if (!segment.isMatched()) {
				continue;
			}
			nearnessLengthProductSum += segment.getNearnessFraction()
					.doubleValue()
					* segment.getApparentLineLength()
					+ segment.getMatchingRoadSegment().getNearnessFraction()
							.doubleValue()
					* segment.getMatchingRoadSegment().getApparentLineLength();
			// Don't use RoadNetwork#getLength, as we are computing length only
			// for matched segments [Jon Aquino 2004-11-02]
			lengthSum += segment.getApparentLineLength()
					+ segment.getMatchingRoadSegment().getApparentLineLength();
		}
		return lengthSum == 0 ? 0 : nearnessLengthProductSum / lengthSum;
	}

	public void refresh() {
		clear();
		for (Iterator i = session.getSourceNetwork(0).getFeatureCollection()
				.iterator(); i.hasNext();) {
			SourceFeature feature = (SourceFeature) i.next();
			roadSegmentAdded(feature.getRoadSegment());
		}
		for (Iterator i = session.getSourceNetwork(1).getFeatureCollection()
				.iterator(); i.hasNext();) {
			SourceFeature feature = (SourceFeature) i.next();
			roadSegmentAdded(feature.getRoadSegment());
		}
	}

	public void roadSegmentAdded(SourceRoadSegment roadSegment) {
		//Instead of #getApparentLine use #getLine which is constant
		//[Jon Aquino 2004-01-15]
		updateCount(roadSegment.getState(), +1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
		updateCount(roadSegment.getResultState(), +1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
	}

	public void roadSegmentRemoved(SourceRoadSegment roadSegment) {
		//Instead of #getApparentLine use #getLine which is constant
		//[Jon Aquino 2004-01-15]
		updateCount(roadSegment.getState(), -1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
		updateCount(roadSegment.getResultState(), -1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
	}

	public void resultStateChanged(ResultState oldResultState,
			SourceRoadSegment roadSegment) {
		if (!roadSegment.isInNetwork()) {
			return;
		}
		//Instead of #getApparentLine use #getLine which is constant
		//[Jon Aquino 2004-01-15]
		updateCount(oldResultState, -1, roadSegment.getLine().getLength(),
				roadSegment.getNetwork().getID());
		updateCount(roadSegment.getResultState(), +1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
	}

	public void stateChanged(SourceState oldState, SourceRoadSegment roadSegment) {
		if (!roadSegment.isInNetwork()) {
			return;
		}
		//Instead of #getApparentLine use #getLine which is constant
		//[Jon Aquino 2004-01-15]
		updateCount(oldState, -1, roadSegment.getLine().getLength(),
				roadSegment.getNetwork().getID());
		updateCount(roadSegment.getState(), +1, roadSegment.getLine()
				.getLength(), roadSegment.getNetwork().getID());
	}

	private void updateCount(SourceState processState, int delta,
			double length, int network) {
		Assert.isTrue(Math.abs(delta) == 1);
		Assert.isTrue(length >= 0);
		get(network).inc(processState, delta, length * delta);
	}

	public int stats;

	private void updateCount(ResultState resultState, int delta, double length,
			int network) {
		Assert.isTrue(Math.abs(delta) == 1);
		Assert.isTrue(length >= 0);
		if (resultState == null) {
			return;
		}
		get(network).inc(resultState, delta, length * delta);
	}

	private ConflationSession session;

	private NetworkStatistics[] networkStatistics;

	public static String normalize(String name) {
		String normalizedName = "";
		for (int i = 0; i < name.length(); i++) {
			if (Character.isLetter(name.charAt(i))) {
				normalizedName += i == 0 ? Character
						.toLowerCase(name.charAt(i)) : name.charAt(i);
			}
		}
		return normalizedName;
	}

	public void geometryModifiedExternally(SourceRoadSegment roadSegment) {
	}

	public void roadSegmentsChanged() {
		refresh();
	}

}