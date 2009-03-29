package com.vividsolutions.jcs.conflate.roads.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import com.vividsolutions.jcs.conflate.roads.*;
import com.vividsolutions.jcs.graph.Node;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.CollectionUtil;

/**
 * Models a Road Network to be conflated.
 * Contains a graph, as well as a
 * FeatureCollection to wrap it, a spatial index, and (in the future)
 * shape-matching stuff.
 */

//If we implement #remove(RoadNode), replace RoadNode's call to
//PlanarGraph#remove(Node) with a call to the new method [Jon Aquino
// 11/28/2003]
public class RoadNetwork implements Serializable {

    private boolean editable = true;
    private RoadNetworkFeatureCollection featureCollection;
    private String name = "X";
    private ConflationSession conflationSession;
    private transient RoadGraph graph = new RoadGraph(this);
    private LinearScanIndex roadSegmentApparentEnvelopeIndex = new LinearScanIndex();
    private LinearScanIndex roadSegmentEnvelopeIndex = new LinearScanIndex();
    private SourceRoadSegment shortestRoadSegment;

    public RoadNetwork(FeatureSchema featureSchema,
            ConflationSession conflationSession) {
        this.conflationSession = conflationSession;
        featureCollection = new RoadNetworkFeatureCollection(this,
                featureSchema);
    }
    public String getName() {
        return name;
    }
    public RoadSegment add(RoadSegment roadSegment) {
        Assert.isTrue(roadSegment.getNetwork() == this);
        roadSegment.enteringNetwork();
        shortestRoadSegment = shortestRoadSegment == null
                || roadSegment.getLine().getLength() < shortestRoadSegment
                        .getLine().getLength() ? (SourceRoadSegment) roadSegment
                : shortestRoadSegment;
        graph.add(roadSegment);
        roadSegmentApparentEnvelopeIndex.insert(
                ((SourceRoadSegment) roadSegment).getApparentLine()
                        .getEnvelopeInternal(), roadSegment);
        roadSegmentEnvelopeIndex.insert(((SourceRoadSegment) roadSegment)
                .getLine().getEnvelopeInternal(), roadSegment);
        roadSegment.enteredNetwork();
        return roadSegment;
    }
    public RoadNetwork getOther() {
        if (this == conflationSession.getSourceNetwork(0)) {
            return conflationSession.getSourceNetwork(1);
        }
        if (this == conflationSession.getSourceNetwork(1)) {
            return conflationSession.getSourceNetwork(0);
        }
        Assert.shouldNeverReachHere();
        return null;
    }
    public void clear() {
        graph = new RoadGraph(this);
        roadSegmentApparentEnvelopeIndex = new LinearScanIndex();
        roadSegmentEnvelopeIndex = new LinearScanIndex();
    }
    public RoadNetworkFeatureCollection getFeatureCollection() {
        return featureCollection;
    }
    /**
     * Returns road segments whose apparent lines intersect the given envelope.
     */
    public Collection roadSegmentsApparentlyIntersecting(Envelope envelope) {
        return roadSegmentApparentEnvelopeIndex.query(envelope);
    }
    public Collection roadSegmentsIntersecting(Envelope envelope) {
        return roadSegmentEnvelopeIndex.query(envelope);
    }
    public Set apparentEndpointsIn(Envelope envelope) {
        HashSet apparentEndpoints = new HashSet();
        for (Iterator i = roadSegmentsApparentlyIntersecting(envelope)
                .iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            if (envelope.contains(roadSegment.getApparentStartCoordinate())) {
                apparentEndpoints.add(roadSegment.getApparentStartCoordinate());
            }
            if (envelope.contains(roadSegment.getApparentEndCoordinate())) {
                apparentEndpoints.add(roadSegment.getApparentEndCoordinate());
            }
        }
        return apparentEndpoints;
    }
    public RoadGraph getGraph() {
        return graph;
    }
    public void remove(RoadSegment roadSegment) {
        roadSegment.exitingNetwork();
        if (shortestRoadSegment == roadSegment) {
            shortestRoadSegment = null;
        }
        graph.remove(roadSegment);
        roadSegmentApparentEnvelopeIndex.remove(
                ((SourceRoadSegment) roadSegment).getApparentLine()
                        .getEnvelopeInternal(), roadSegment);
        roadSegmentEnvelopeIndex.remove(((SourceRoadSegment) roadSegment)
                .getLine().getEnvelopeInternal(), roadSegment);
        roadSegment.exitedNetwork();
    }
    public List getRoadSegmentsInState(SourceState state) {
        List result = new ArrayList();
        for (Iterator i = graph.getEdges().iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            if (roadSegment.getState() == state) {
                result.add(roadSegment);
            }
        }
        return result;
    }
    public ConflationSession getSession() {
        return conflationSession;
    }
    public Envelope getApparentEnvelope() {
        return roadSegmentApparentEnvelopeIndex.getEnvelope();
    }
    public int getID() {
        return conflationSession.getSourceNetwork(0) == this ? 0
                : conflationSession.getSourceNetwork(1) == this ? 1
                        : FUTURE_Assert.shouldNeverReachHere2();
    }
    public void apparentLineChanged(SourceRoadSegment roadSegment,
            LineString oldApparentLine, LineString newApparentLine) {
        roadSegmentApparentEnvelopeIndex.remove(oldApparentLine
                .getEnvelopeInternal(), roadSegment);
        roadSegmentApparentEnvelopeIndex.insert(newApparentLine
                .getEnvelopeInternal(), roadSegment);
    }
    /**
     * For serialization.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        graph = new RoadGraph(this);
    }
    public void setName(String name) {
        this.name = name;
    }
    public SourceRoadSegment getShortestRoadSegment() {
        if (shortestRoadSegment == null) {
            SourceRoadSegment shortest = (SourceRoadSegment) graph.getEdges()
                    .iterator().next();
            for (Iterator i = graph.getEdges().iterator(); i.hasNext();) {
                SourceRoadSegment candidate = (SourceRoadSegment) i.next();
                shortest = candidate.getLine().getLength() < shortest.getLine()
                        .getLength() ? candidate : shortest;
            }
            shortestRoadSegment = shortest;
        }
        return shortestRoadSegment;
    }

    /**
     * Checks for coincident edges (edges which are incident
     * on the same node with the same angle at that node).
     * These will cause problems during conflation and should not
     * be present in the dataset.
     * If there are no coincident segments the feature collection will be empty.
     *
     * @return a {@link FeatureCollection} containing any coincident edge features
     */
    public FeatureCollection checkCoincidentEdges() {
        Collection edges = graph.checkCoincidentEdges();
        FeatureCollection fc = new FeatureDataset(featureCollection
                .getFeatureSchema());
        for (Iterator i = edges.iterator(); i.hasNext();) {
            SourceRoadSegment edge = (SourceRoadSegment) i.next();
            fc.add(edge.getFeature());
        }
        return fc;
    }

    public boolean isEditable() {
        return editable;
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    public SpatialIndex getRoadSegmentApparentEnvelopeIndex() {
        return roadSegmentApparentEnvelopeIndex;
    }
	public double getLength() {
		double length = 0;
        for (Iterator i = graph.getEdges().iterator(); i.hasNext();) {
            SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
            length += roadSegment.getApparentLineLength();
        }
        return length;
	}
}