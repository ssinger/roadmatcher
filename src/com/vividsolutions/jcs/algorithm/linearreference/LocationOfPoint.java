package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;

/**
 * Computes the {@link LineStringLocation} of the point
 * on a {@link LineString} nearest a given point.
 * The nearest point is not necessarily unique, but this class
 * always computes the nearest point closest to
 * the start of the linestring.
 */
public class LocationOfPoint {

  public static LineStringLocation locate(LineString line, Coordinate inputPt)
  {
    LocationOfPoint locater = new LocationOfPoint(line);
    return locater.locate(inputPt);
  }

  private LineString line;

  public LocationOfPoint(LineString line) {
    this.line = line;
  }

  /**
   * Find the nearest location along a {@link LineString} to a given point.
   *
   * @param inputPt the coordinate to locate
   * @return the location of the nearest point
   */
  public LineStringLocation locate(Coordinate inputPt)
  {
    //return locateAfter(inputPt, null);
    Coordinate[] pts = line.getCoordinates();

    double minDistance = Double.MAX_VALUE;
    int minIndex = 0;
    double minFrac = -1.0;

    LineSegment seg = new LineSegment();
    int startIndex = 0;
    startIndex = 0;

    for (int i = startIndex; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];
      double segDistance = seg.distance(inputPt);
      double segFrac = segmentFraction(seg, inputPt);

      if (segDistance < minDistance) {
        minIndex = i;
        minFrac = segFrac;
        minDistance = segDistance;
      }
    }
    return new LineStringLocation(line, minIndex, minFrac);
  }

  /**
   * Find the nearest location along a {@link LineString} to a given point
   * after the specified minimum {@link LineStringLocation}.
   * If possible the location returned will be strictly greater than the
   * <code>minLocation</code>.
   * If this is not possible, the
   * value returned will equal <code>minLocation</code>.
   * (An example where this is not possible is when
   * minLocation = [end of line] ).
   *
   * @param inputPt the coordinate to locate
   * @param minLocation the minimum location for the point location
   * @return the location of the nearest point
   */
  public LineStringLocation locateAfter(Coordinate inputPt, LineStringLocation minLocation)
  {
    if (minLocation == null) return locate(inputPt);

    Coordinate[] pts = line.getCoordinates();

    // sanity check for minLocation at or past end of line
    if (minLocation.getSegmentIndex() >= line.getNumPoints())
      return new LineStringLocation(line, pts.length - 1, 1.0);

    double minDistance = Double.MAX_VALUE;
    LineStringLocation nextClosestLocation = minLocation;

    LineSegment seg = new LineSegment();
    int startIndex = 0;
    startIndex = minLocation.getSegmentIndex();

    for (int i = startIndex; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];

      double segDistance = seg.distance(inputPt);
      double segFrac = segmentFraction(seg, inputPt);

      if (segDistance < minDistance
          && isGreater(i, segFrac, minLocation)) {
        nextClosestLocation = new LineStringLocation(line, i, segFrac);
        minDistance = segDistance;
      }
    }
    /**
     * Return the minDistanceLocation found.
     * This will not be null, since it was initialized to minLocation
     */
    Assert.isTrue(nextClosestLocation.compareTo(minLocation) >= 0,
                  "computed location is before specified minimum location");
    return nextClosestLocation;
  }


  /**
   * Tests whether a location given by a <index, segmentFraction> pair
   * is located after a {@link LineStringLocation}.
   *
   * @param i the segment index
   * @param segFrac the fraction along the segment
   * @param loc a location
   * @return <code>true</code> if the first location is greater than the second
   */
  private boolean isGreater(int i, double segFrac, LineStringLocation loc)
  {
    return LineStringLocation.compareLocationValues(i, segFrac,
        loc.getSegmentIndex(), loc.getSegmentFraction()) > 0;
  }

  public static double segmentFraction(
      LineSegment seg,
      Coordinate inputPt)
  {
    double segFrac = seg.projectionFactor(inputPt);
    if (segFrac < 0.0)
      segFrac = 0.0;
    else if (segFrac > 1.0)
      segFrac = 1.0;
    return segFrac;
  }
}