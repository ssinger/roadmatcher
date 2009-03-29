package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.geom.InteriorPointFinder;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;


/**
 * Only one of the two source layers needs this style.
 */
public class MatchStyle implements Style {
    private static final double HEIGHT = 8;
    private boolean enabled = true;
    private InteriorPointFinder interiorPointFinder = new InteriorPointFinder();
    private Stroke stroke = new BasicStroke(1);

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();

            return null;
        }
    }

    public void initialize(Layer layer) {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void paint(Feature f, Graphics2D g, Viewport viewport)
        throws NoninvertibleTransformException {
        SourceRoadSegment roadSegmentA = ((SourceFeature) f).getRoadSegment();
        SourceRoadSegment roadSegmentB = roadSegmentA.getMatchingRoadSegment();
        if (roadSegmentB == null) {
            return;
        }
        if (roadSegmentA.getApparentLine().getLength() > roadSegmentB.getApparentLine().getLength()) {
            //Make sure A is shorter, because we locate the ellipse on the
            // midpoint of A. [Jon Aquino 11/18/2003]
            SourceRoadSegment temp = roadSegmentA;
            roadSegmentA = roadSegmentB;
            roadSegmentB = temp;
        }

        Geometry lineA =
            EnvelopeUtil.toGeometry(viewport.getEnvelopeInModelCoordinates())
                        .intersection(roadSegmentA.getApparentLine());
        Geometry lineB =
            EnvelopeUtil.toGeometry(viewport.getEnvelopeInModelCoordinates())
                        .intersection(roadSegmentB.getApparentLine());
        if (!(lineA instanceof LineString) || lineA.isEmpty()) {
            return;
        }
        if (!(lineB instanceof LineString) || lineB.isEmpty()) {
            return;
        }

        Coordinate[] foci =
            foci((LineString) lineA, (LineString) lineB, viewport);
        Point2D a = viewport.toViewPoint(foci[0]);
        Point2D b = viewport.toViewPoint(foci[1]);
        Point2D center =
            new Point2D.Double(
                MathUtil.avg(a.getX(), b.getX()),
                MathUtil.avg(a.getY(), b.getY()));
        g.setStroke(stroke);
        g.setColor(HighlightManager.instance(RoadStyleUtil.instance().context(viewport)).getColourScheme().getRingColour());

        double rotation =
            a.getX() == b.getX() ? Math.PI / 2
                                 : Math.atan(
                (a.getY() - b.getY()) / (a.getX() - b.getX()));
        g.rotate(rotation, center.getX(), center.getY());
        try {
            g.draw(toShape(center, a.distance(b) + (HEIGHT * 2), HEIGHT));
        } finally {
            g.rotate(-rotation, center.getX(), center.getY());
        }
    }

    private Coordinate[] midPointOnFirstAndClosestPointOnSecond(
        LineString first, LineString second) {
        Coordinate midPointOnFirst = midPoint(first);

        return new Coordinate[] {
            midPointOnFirst,
            DistanceOp.closestPoints(
                first.getFactory().createPoint(midPointOnFirst), second)[1]
        };
    }

    /**
     * Not true foci.
     */
    private Coordinate[] foci(
        LineString lineA, LineString lineB, Viewport viewport) {
        Coordinate[] midPointOnAAndClosestPointOnB =
            midPointOnFirstAndClosestPointOnSecond(lineA, lineB);
        if (
            !equal(midPointOnAAndClosestPointOnB[1], lineB.getCoordinateN(0), 4,
                    viewport)
                && !equal(midPointOnAAndClosestPointOnB[1],
                    lineB.getCoordinateN(lineB.getNumPoints() - 1), 4, viewport)) {
            return midPointOnAAndClosestPointOnB;
        }

        Coordinate[] midPointOnBAndClosestPointOnA =
            midPointOnFirstAndClosestPointOnSecond(lineB, lineA);
        if (
            !equal(midPointOnBAndClosestPointOnA[1], lineA.getCoordinateN(0), 4,
                    viewport)
                && !equal(midPointOnBAndClosestPointOnA[1],
                    lineA.getCoordinateN(lineA.getNumPoints() - 1), 4, viewport)) {
            return midPointOnBAndClosestPointOnA;
        }

        return new Coordinate[] {
            midPointOnAAndClosestPointOnB[0], midPointOnBAndClosestPointOnA[0]
        };
    }

    private boolean equal(
        Coordinate a, Coordinate b, int pixelTolerance, Viewport viewport) {
        return a.distance(b) < pixelTolerance / viewport.getScale();
    }

    public static Coordinate midPoint(LineString line) {
        double midPointDistance = line.getLength() / 2;
        double currentDistance = 0;
        for (int i = 1; i < line.getNumPoints(); i++) {
            double roadSegmentLength =
                line.getCoordinateN(i - 1).distance(line.getCoordinateN(i));
            currentDistance += roadSegmentLength;
            if (currentDistance > midPointDistance) {
                double overshoot = currentDistance - midPointDistance;

                return CoordUtil.add(
                    line.getCoordinateN(i),
                    CoordUtil.multiply(
                        overshoot / roadSegmentLength,
                        CoordUtil.subtract(
                            line.getCoordinateN(i - 1), line.getCoordinateN(i))));
            }
        }
        Assert.shouldNeverReachHere();

        return null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private Shape toShape(Point2D center, double width, double height) {
        return new Ellipse2D.Double(
            center.getX() - (width / 2), center.getY() - (height / 2), width,
            height);
    }
}
