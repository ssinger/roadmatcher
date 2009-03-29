package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
public class Adjustment {
    private List roadSegments;
    private List newApparentLines;
    private List oldApparentLines;
    private Coordinate destination;
    public Adjustment(Coordinate destination, List roadSegments,
            List newApparentLines, int inconsistentNodesBeingAutoAdjusted) {
        this.roadSegments = roadSegments;
        this.newApparentLines = newApparentLines;
        this.destination = destination;
        this.inconsistentNodesBeingAutoAdjusted = inconsistentNodesBeingAutoAdjusted;
        this.oldApparentLines = (List) CollectionUtil.collect(roadSegments,
                new Block() {
                    public Object yield(Object roadSegment) {
                        return ((SourceRoadSegment) roadSegment)
                                .getApparentLine();
                    }
                });
    }
    public List getOldApparentLines() {
        return Collections.unmodifiableList(oldApparentLines);
    }
    public List getNewApparentLines() {
        return Collections.unmodifiableList(newApparentLines);
    }
    public List getRoadSegments() {
        return Collections.unmodifiableList(roadSegments);
    }
    public Collection getAdjustedRoadSegments() {
        Collection adjustedRoadSegments = new ArrayList();
        for (int i = 0; i < roadSegments.size(); i++) {
            if (oldApparentLines.get(i) != newApparentLines.get(i)) {
                adjustedRoadSegments.add(roadSegments.get(i));
            }
        }
        return adjustedRoadSegments;
    }
    private int inconsistentNodesBeingAutoAdjusted;
    private Set endpoints(List lines) {
        HashSet endpoints = new HashSet();
        for (Iterator i = lines.iterator(); i.hasNext();) {
            LineString line = (LineString) i.next();
            endpoints.add(FUTURE_LineString.first(line));
            endpoints.add(FUTURE_LineString.last(line));
        }
        return endpoints;
    }
    public Coordinate getDestination() {
        return destination;
    }
    /**
     * @return 0 if this is a manual adjustment
     */
	public int getInconsistentNodesBeingAutoAdjusted() {
		return inconsistentNodesBeingAutoAdjusted;
	}
}