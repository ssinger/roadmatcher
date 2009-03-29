package com.vividsolutions.jcs.conflate.roads.model;

import com.vividsolutions.jts.geom.LineString;

/**
 * @deprecated
 */
public class ResultRoadSegment extends RoadSegment {

    public ResultRoadSegment(LineString line,
            SourceRoadSegment referenceRoadSegment, RoadNetwork network) {
        super(line, network);
        setFeature(new ResultFeature(this));
        this.referenceRoadSegment = referenceRoadSegment;
    }

    public SourceRoadSegment getReferenceRoadSegment() {
        return referenceRoadSegment;
    }

    public SourceRoadSegment getSourceRoadSegment0() {
        return referenceRoadSegment.getNetwork() == getNetwork()
        .getSession().getSourceNetwork(0)
                ? referenceRoadSegment
                : referenceRoadSegment.getMatchingRoadSegment();
    }

    public SourceRoadSegment getSourceRoadSegment1() {
        return referenceRoadSegment.getNetwork() == getNetwork()
        .getSession().getSourceNetwork(1)
                ? referenceRoadSegment
                : referenceRoadSegment.getMatchingRoadSegment();
    }

    private SourceRoadSegment referenceRoadSegment;

}