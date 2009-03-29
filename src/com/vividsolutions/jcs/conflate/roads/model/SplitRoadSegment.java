package com.vividsolutions.jcs.conflate.roads.model;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;


public class SplitRoadSegment extends SourceRoadSegment {

    private SourceRoadSegment parent;
    private SplitRoadSegment siblingAtStart;
    private SplitRoadSegment siblingAtEnd;
    private boolean startSplitNodeExistingVertex = false;
    private boolean endSplitNodeExistingVertex = false;

    public SplitRoadSegment(LineString line, Feature originalFeature,
        RoadNetwork network, SourceRoadSegment parent) {
        //To do: rename "parent" to "originalFeature" once we get rid of the
        //concept of an original feature [Jon Aquino 11/25/2003]
        super(line, originalFeature, network);
        Assert.isTrue(parent != null);
        this.parent = parent;
    }

    public SourceRoadSegment getParent() {
        return parent;
    }

    public boolean isFirst() {
        return siblingAtStart == null;
    }

    public SplitRoadSegment getSiblingAtEnd() {
        return siblingAtEnd;
    }
    public SplitRoadSegment setSiblingAtEnd(SplitRoadSegment siblingAtEnd, boolean endSplitNodeExistingVertex) {
        this.siblingAtEnd = siblingAtEnd;
        this.endSplitNodeExistingVertex = endSplitNodeExistingVertex;
        return this;
    }
    public SplitRoadSegment getSiblingAtStart() {
        return siblingAtStart;
    }
    public SplitRoadSegment setSiblingAtStart(SplitRoadSegment siblingAtStart, boolean startSplitNodeExistingVertex) {
        this.siblingAtStart = siblingAtStart;
        this.startSplitNodeExistingVertex = startSplitNodeExistingVertex;
        return this;
    }

    public boolean isSplitAtStart() {
        return getSiblingAtStart() != null;
    }
    
    public boolean isSplitAtEnd() {
        return getSiblingAtEnd() != null;
    }
    
    public boolean wasStartSplitNodeExistingVertex() {
        return startSplitNodeExistingVertex;
    }
    public boolean wasEndSplitNodeExistingVertex() {
        return endSplitNodeExistingVertex;
    }    
    public boolean isStartNodeConstrained() {
		return !isSplitAtStart() && getParent().isStartNodeConstrained();
	}
    public boolean isEndNodeConstrained() {
    	return !isSplitAtEnd() && getParent().isEndNodeConstrained();
	}
}
