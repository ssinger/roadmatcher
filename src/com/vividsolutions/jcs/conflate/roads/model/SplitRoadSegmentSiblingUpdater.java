package com.vividsolutions.jcs.conflate.roads.model;

public class SplitRoadSegmentSiblingUpdater {

    public SplitRoadSegmentSiblingUpdater(SourceRoadSegment roadSegment) {
        if (roadSegment instanceof SplitRoadSegment) {
            splitRoadSegment = (SplitRoadSegment) roadSegment;
        }
    }

    public void execute() {
        if (splitRoadSegment == null) { return; }
        if (splitRoadSegment.getSiblingAtStart() != null) {
            oldStartSiblingEndSibling = splitRoadSegment
                    .getSiblingAtStart().getSiblingAtEnd();
            oldStartSiblingEndSplitNodeWasExistingVertex = splitRoadSegment
                    .getSiblingAtStart().wasEndSplitNodeExistingVertex();
        }
        if (splitRoadSegment.getSiblingAtEnd() != null) {
            oldEndSiblingStartSibling = splitRoadSegment.getSiblingAtEnd()
                    .getSiblingAtStart();
            oldEndSiblingStartSplitNodeWasExistingVertex = splitRoadSegment
                    .getSiblingAtEnd().wasStartSplitNodeExistingVertex();
        }
        update(splitRoadSegment);
    }

    public void unexecute() {
        if (splitRoadSegment == null) { return; }
        if (splitRoadSegment.getSiblingAtStart() != null) {
            splitRoadSegment.getSiblingAtStart().setSiblingAtEnd(
                    oldStartSiblingEndSibling,
                    oldStartSiblingEndSplitNodeWasExistingVertex);
        }
        if (splitRoadSegment.getSiblingAtEnd() != null) {
            splitRoadSegment.getSiblingAtEnd().setSiblingAtStart(
                    oldEndSiblingStartSibling,
                    oldEndSiblingStartSplitNodeWasExistingVertex);
        }
    }

    private SplitRoadSegment oldEndSiblingStartSibling;

    private boolean oldEndSiblingStartSplitNodeWasExistingVertex;

    private SplitRoadSegment oldStartSiblingEndSibling;

    private boolean oldStartSiblingEndSplitNodeWasExistingVertex;

    private SplitRoadSegment splitRoadSegment;

    public static void update(SplitRoadSegment splitRoadSegment) {
        if (splitRoadSegment.getSiblingAtStart() != null) {
            splitRoadSegment.getSiblingAtStart().setSiblingAtEnd(
                    splitRoadSegment,
                    splitRoadSegment.wasStartSplitNodeExistingVertex());
        }
        if (splitRoadSegment.getSiblingAtEnd() != null) {
            splitRoadSegment.getSiblingAtEnd().setSiblingAtStart(
                    splitRoadSegment,
                    splitRoadSegment.wasEndSplitNodeExistingVertex());
        }
    }
}