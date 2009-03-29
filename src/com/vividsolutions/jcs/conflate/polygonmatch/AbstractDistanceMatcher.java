package com.vividsolutions.jcs.conflate.polygonmatch;

import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDistanceMatcher extends IndependentCandidateMatcher {

    public double match(Geometry target, Geometry candidate) {
        return 1
            - (distance(target, candidate)
                / combinedEnvelopeDiagonalDistance(target, candidate));
    }

    protected abstract double distance(Geometry target, Geometry candidate);

    private double combinedEnvelopeDiagonalDistance(
        Geometry target,
        Geometry candidate) {
        Envelope envelope = new Envelope(target.getEnvelopeInternal());
        envelope.expandToInclude(candidate.getEnvelopeInternal());
        return Point2D.distance(
            envelope.getMinX(),
            envelope.getMinY(),
            envelope.getMaxX(),
            envelope.getMaxY());
    }

}
