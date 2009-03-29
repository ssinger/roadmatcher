package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.Collection;
import java.util.Iterator;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.ApparentNode;
import com.vividsolutions.jcs.jump.FUTURE_Assert;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
public class IncidenceAngleChecker {
    public boolean createsIncidenceAngleLessThan(double minimumIncidenceAngle,
            Adjustment adjustment, ConflationSession session) {
        Collection apparentLines = addAll(apparentLines(removeAll(
                includedRoadSegmentsWithApparentEndpoint(adjustment
                        .getDestination(), session), adjustment
                        .getRoadSegments())), adjustment.getNewApparentLines());
        for (Iterator i = adjustment.getNewApparentLines().iterator(); i
                .hasNext();) {
            LineString newApparentLine = (LineString) i.next();
            if (createsIncidenceAngleLessThan(minimumIncidenceAngle,
                    newApparentLine, apparentLines, adjustment.getDestination())) {
                return true;
            }
        }
        return false;
    }
    private Collection addAll(Collection a, Collection b) {
        return FUTURE_CollectionUtil.addAll(a, b);
    }
    private Collection apparentLines(Collection roadSegments) {
        return CollectionUtil.collect(roadSegments, new Block() {
            public Object yield(Object roadSegment) {
                return ((SourceRoadSegment) roadSegment).getApparentLine();
            }
        });
    }
    private Collection removeAll(Collection a, Collection b) {
        return FUTURE_CollectionUtil.removeAll(a, b);
    }
    private Collection includedRoadSegmentsWithApparentEndpoint(
            Coordinate coordinate, ConflationSession session) {
        return new ApparentNode(coordinate,
                session).getIncludedIncidentRoadSegments();
    }
    private boolean createsIncidenceAngleLessThan(double minimumIncidenceAngle,
            LineString newApparentLine, Collection apparentLines,
            Coordinate coordinate) {
        for (Iterator i = apparentLines.iterator(); i.hasNext();) {
            LineString apparentLine = (LineString) i.next();
            if (newApparentLine == apparentLine) {
                continue;
            }
            if (incidenceAngle(newApparentLine, apparentLine, coordinate) < minimumIncidenceAngle) {
                return true;
            }
        }
        return false;
    }
    private double incidenceAngle(LineString a, LineString b,
            Coordinate coordinate) {
        return Angle.toDegrees(Angle.diff(angle(a, coordinate), angle(b,
                coordinate)));
    }
    private double angle(LineString line, Coordinate c) {
        int n = line.getNumPoints();
        return line.getCoordinateN(0).equals(c) ? Angle.angle(line
                .getCoordinateN(0), line.getCoordinateN(1)) : line
                .getCoordinateN(n - 1).equals(c) ? Angle.angle(line
                .getCoordinateN(n - 1), line.getCoordinateN(n - 2)) : Double
                .parseDouble(FUTURE_Assert.throwAssertionFailure().toString());
    }
}