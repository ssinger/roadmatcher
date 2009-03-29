package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;

/**
 * Computes the length along a LineString to the point on the line nearest a given point.
 */
public class LengthOfPoint
{
  public static double lengthAlongSegment(LineSegment seg, Coordinate pt)
  {
    double projFactor = seg.projectionFactor(pt);
    double len = 0.0;
    if (projFactor <= 0.0)
      len = 0.0;
    else if (projFactor <= 1.0)
      len = projFactor * seg.getLength();
    else
      len = seg.getLength();
    return len;
  }

  /**
   * Computes the length along a LineString to the point on the line nearest a given point.
   */
  public static double locate(LineString line, Coordinate inputPt)
  {
    LengthOfPoint lp = new LengthOfPoint(line);
    return lp.locate(inputPt);
  }

  private LineString inputLine;
  private double minDistanceToPoint;
  //private double locationLength;

  public LengthOfPoint(LineString inputLine)
  {
    this.inputLine = inputLine;
  }

  public double locate(Coordinate inputPt)
  {
    double minDistance = Double.MAX_VALUE;
    double ptMeasure = 0.0;
    double segmentStartMeasure = 0.0;
    Coordinate[] pts = inputLine.getCoordinates();
    LineSegment seg = new LineSegment();
    for (int i = 0; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];

      double segDistance = seg.distance(inputPt);
      double segMeasureToPt = segmentNearestMeasure(seg, inputPt, segmentStartMeasure);

      // if point on this segment is closer then record the measure
      if (segDistance < minDistance) {
        ptMeasure = segMeasureToPt;
        minDistance = segDistance;
      }
      segmentStartMeasure += seg.getLength();
    }
    return ptMeasure;
  }

  public double locateAfter(Coordinate inputPt, double minLength)
  {
    // sanity check for minLength at or past end of line
    double maxLen = inputLine.getLength();
    if (minLength >= maxLen)
      return maxLen;

    double minDistance = Double.MAX_VALUE;
    double ptMeasure = minLength;

    double segmentStartMeasure = 0.0;
    Coordinate[] pts = inputLine.getCoordinates();
    LineSegment seg = new LineSegment();
    for (int i = 0; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];

      double segDistance = seg.distance(inputPt);
      double segMeasureToPt = segmentNearestMeasure(seg, inputPt, segmentStartMeasure);

      // if point on this segment is closer then record the measure
      if (segDistance < minDistance
          && segMeasureToPt > minLength) {
        ptMeasure = segMeasureToPt;
        minDistance = segDistance;
      }
      segmentStartMeasure += seg.getLength();
    }
    Assert.isTrue(ptMeasure >= minLength,
                  "computed length is before specified minimum length");
    return ptMeasure;
  }


  private double segmentNearestMeasure(LineSegment seg, Coordinate inputPt,
                            double segmentStartMeasure)
  {
    // found new minimum, so compute location distance of point
    double projFactor = seg.projectionFactor(inputPt);
    if (projFactor <= 0.0)
      return segmentStartMeasure;
    if (projFactor <= 1.0)
      return segmentStartMeasure + projFactor * seg.getLength();
    // projFactor > 1.0
    return segmentStartMeasure + seg.getLength();
  }
}