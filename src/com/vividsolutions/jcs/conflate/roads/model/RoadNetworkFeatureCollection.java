package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;

public class RoadNetworkFeatureCollection implements FeatureCollection, Serializable {


    private RoadNetwork network;
    private FeatureSchema featureSchema;

    public RoadNetworkFeatureCollection(RoadNetwork network, FeatureSchema featureSchema) {
        this.network = network;
        this.featureSchema = featureSchema;
    }

    public FeatureSchema getFeatureSchema() {
        return featureSchema;
    }

    public Envelope getEnvelope() {
        return network.getApparentEnvelope();
    }

    public int size() {
        return network.getGraph().getEdges().size();
    }

    public boolean isEmpty() {
        return network.getGraph().getEdges().isEmpty();
    }

    public List getFeatures() {
        return toFeatures(network.getGraph().getEdges());
    }

    private static List toFeatures(Collection roadSegments) {
        return (List) CollectionUtil.collect(roadSegments, new Block() {
            public Object yield(Object arg) {
                return ((RoadSegment) arg).getFeature();
            }
        });
    }

    public Iterator iterator() {
        return new Iterator() {
            Iterator iterator = network.getGraph().getEdges().iterator();
            public void remove() {
                iterator.remove();
            }
            public boolean hasNext() {
                return iterator.hasNext();
            }
            public Object next() {
                return ((RoadSegment) iterator.next()).getFeature();
            }
        };
    }

    public List query(Envelope envelope) {
        return toFeatures(network.roadSegmentsApparentlyIntersecting(envelope));
    }

    public void add(Feature feature) {
        throw new UnsupportedOperationException();
    }

    public void addAll(Collection features) {
        for (Iterator i = features.iterator(); i.hasNext(); ) {
            Feature feature = (Feature) i.next();
            add(feature);
        }
    }

    public void removeAll(Collection features) {
        for (Iterator i = features.iterator(); i.hasNext(); ) {
            Feature feature = (Feature) i.next();
            remove(feature);
        }
    }

    public void remove(Feature feature) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        network.clear();
    }

    public Collection remove(Envelope envelope) {
        Collection features = query(envelope);
        removeAll(features);
        return features;
    }

    public RoadNetwork getNetwork() {
        return network;
    }

}