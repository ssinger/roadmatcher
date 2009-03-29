package com.vividsolutions.jcs.conflate.roads.model;
import java.io.Serializable;
public interface PrecedenceRuleEngine extends Serializable {
    public abstract SourceRoadSegment chooseReference(SourceRoadSegment a,
            SourceRoadSegment b);
}