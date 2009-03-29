package com.vividsolutions.jcs.conflate.roads.model;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jump.util.Block;
public class RoadsEventFirer {
    private List listeners = new ArrayList();
    public void fireRoadSegmentAdded(SourceRoadSegment roadSegment) {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.roadSegmentAdded(roadSegment);
        }
    }
    public void fireRoadSegmentRemoved(SourceRoadSegment roadSegment) {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.roadSegmentRemoved(roadSegment);
        }
    }
    public void fireGeometryModifiedExternally(SourceRoadSegment roadSegment) {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.geometryModifiedExternally(roadSegment);
        }
    }
    public void fireResultStateChanged(ResultState oldResultState,
            SourceRoadSegment roadSegment) {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.resultStateChanged(oldResultState, roadSegment);
        }
    }
    public void fireStateChanged(SourceState oldState,
            SourceRoadSegment roadSegment) {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.stateChanged(oldState, roadSegment);
        }
    }
    public void fireRoadSegmentsChanged() {
        if (!firingEvents) {
            return;
        }
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            RoadsListener listener = (RoadsListener) i.next();
            listener.roadSegmentsChanged();
        }
    }    
    public void addListener(RoadsListener listener) {
        listeners.add(listener);
    }
    public void removeListener(RoadsListener listener) {
        listeners.remove(listener);
    }
    
    private boolean firingEvents = true;
    public void deferFiringEvents(Block block) {
        boolean originallyFiringEvents = firingEvents;
        try {
            firingEvents = false;
            block.yield();
        } finally {
            firingEvents = originallyFiringEvents;
        }
        fireRoadSegmentsChanged();
    }
}