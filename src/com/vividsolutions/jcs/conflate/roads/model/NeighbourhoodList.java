package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;

public class NeighbourhoodList implements Serializable {

	public Collection toCollection() {
		return Collections.unmodifiableCollection(neighbourhoods
				.query(neighbourhoods.getEnvelope()));
	}
	public static NeighbourhoodList postponedInconsistentNeighbourhoods(
			ConflationSession session) {
		final String INSTANCE_KEY = NeighbourhoodList.class + " - INSTANCE";
		if (session.getBlackboard().get(INSTANCE_KEY) == null) {
			session.getBlackboard().put(INSTANCE_KEY, new NeighbourhoodList());
		}
		return (NeighbourhoodList) session.getBlackboard().get(INSTANCE_KEY);
	}

	private LinearScanIndex neighbourhoods = new LinearScanIndex();

	private SortedSet findEqual(SortedSet neighbourhood) {
		for (Iterator i = neighbourhoods.query(envelope(neighbourhood))
				.iterator(); i.hasNext();) {
			SortedSet candidateNeighbourhood = (SortedSet) i.next();
			if (equal(neighbourhood, candidateNeighbourhood)) {
				return candidateNeighbourhood;
			}
		}
		return null;
	}

	public boolean contains(SortedSet neighbourhood) {
		return findEqual(neighbourhood) != null;
	}

	public void add(SortedSet neighbourhood) {
		Assert.isTrue(!contains(neighbourhood));
		neighbourhoods.insert(envelope(neighbourhood), neighbourhood);
	}

	public void remove(SortedSet neighbourhood) {
		SortedSet equalNeighbourhood = findEqual(neighbourhood);
		Assert.isTrue(equalNeighbourhood != null);
		neighbourhoods.remove(envelope(equalNeighbourhood),
				equalNeighbourhood);
	}

	private Envelope envelope(Collection neighbourhood) {
		Envelope envelope = (Envelope) new Envelope();
		for (Iterator i = neighbourhood.iterator(); i.hasNext();) {
			Coordinate coordinate = (Coordinate) i.next();
			envelope.expandToInclude(coordinate);
		}
		return envelope;
	}

	private boolean equal(SortedSet setA, SortedSet setB) {
		if (setA.size() != setB.size()) {
			return false;
		}
		Iterator a = setA.iterator();
		Iterator b = setB.iterator();
		while (a.hasNext()) {
			if (!a.next().equals(b.next())) {
				return false;
			}
		}
		return true;
	}

	public SortedSet largest() {
		SortedSet largest = null;
		for (Iterator i = neighbourhoods.query(neighbourhoods.getEnvelope())
				.iterator(); i.hasNext();) {
			SortedSet neighbourhood = (SortedSet) i.next();
			if (largest == null || neighbourhood.size() > largest.size()) {
				largest = neighbourhood;
			}
		}
		return largest;
	}

	public void removeAll(Collection neighbourhoods) {
		for (Iterator i = neighbourhoods.iterator(); i.hasNext();) {
			SortedSet neighbourhood = (SortedSet) i.next();
			remove(neighbourhood);
		}
	}

	public void addAll(Collection neighbourhoods) {
		for (Iterator i = neighbourhoods.iterator(); i.hasNext();) {
			SortedSet neighbourhood = (SortedSet) i.next();
			add(neighbourhood);
		}
	}
	public void clear() {
		removeAll(toCollection());
	}

}