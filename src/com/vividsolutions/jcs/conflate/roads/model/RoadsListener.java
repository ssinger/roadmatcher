package com.vividsolutions.jcs.conflate.roads.model;

public interface RoadsListener {
    public void roadSegmentAdded(SourceRoadSegment roadSegment);

    public void roadSegmentRemoved(SourceRoadSegment roadSegment);

    public void resultStateChanged(
            ResultState oldResultState, SourceRoadSegment roadSegment);

    public void stateChanged(SourceState oldState,
            SourceRoadSegment roadSegment);

    public void geometryModifiedExternally(SourceRoadSegment roadSegment);

    public void roadSegmentsChanged();
}