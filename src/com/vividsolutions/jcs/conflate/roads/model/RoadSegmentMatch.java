package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;


/**
 * (immutable i.e. a value object)
 */
public class RoadSegmentMatch implements Serializable {


    private SourceRoadSegment a;
    private SourceRoadSegment b;
    public RoadSegmentMatch(SourceRoadSegment a, SourceRoadSegment b) {
        this.a = a;
        this.b = b;
    }
    public SourceRoadSegment other(RoadSegment s) {
        return s == a ? b : a;
    }
    public SourceRoadSegment getA() {
        return a;
    }
    public SourceRoadSegment getB() {
        return b;
    }

}