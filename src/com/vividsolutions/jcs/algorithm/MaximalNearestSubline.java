package com.vividsolutions.jcs.algorithm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.linearreference.*;

/**
 * Computes the Maximal Nearest Subline of a given linestring relative
 * to another linestring.
 * The Maximal Nearest Subline of A relative to B is the shortest subline
 * of A which contains all the points of A which are the nearest points to
 * the points in B.
 * This effectively "trims" the ends of A which are not near to B.
 * <p>
 * An exact computation of the MNS would require computing a line Voronoi.
 * For this reason, the algorithm used in this class is heuristic-based.
 * It may compute a geometry which is shorter than the actual MNS.
 */
public class MaximalNearestSubline {

  public static LineString getMaximalNearestSubline(LineString a, LineString b)
  {
    MaximalNearestSubline mns = new MaximalNearestSubline(a, b);
    LineStringLocation[] interval = mns.getInterval();
    return getSubline(a, interval[0], interval[1]);
  }

  public static LineString getSubline(LineString line, LineStringLocation start, LineStringLocation end)
  {
    Coordinate[] coordinates = line.getCoordinates();
    CoordinateList newCoordinates = new CoordinateList();

    int includedStartIndex = start.getSegmentIndex();
    if (start.getSegmentFraction() > 0.0) {
      includedStartIndex += 1;
    }
    int includedEndIndex = end.getSegmentIndex();
    if (end.getSegmentFraction() >= 1.0) {
      includedEndIndex += 1;
    }

    if (! start.isVertex()) {
      newCoordinates.add(start.getCoordinate(), false);
    }

    for (int i = includedStartIndex; i <= includedEndIndex; i++) {
      newCoordinates.add(line.getCoordinateN(i), false);
    }
    if (! end.isVertex()) {
      newCoordinates.add(end.getCoordinate(), false);
    }
    Coordinate[] newCoordinateArray = newCoordinates.toCoordinateArray();
    /**
     * Ensure there is enough coordinates to build a valid line. Make a
     * 2-point line with duplicate coordinates, if necessary There will
     * always be at least one coordinate in the coordList.
     */
    if (newCoordinateArray.length <= 1) {
      newCoordinateArray = new Coordinate[] { newCoordinateArray[0], newCoordinateArray[0]};
    }
    return line.getFactory().createLineString(newCoordinateArray);
  }

  private LineString a;
  private LineString b;
  private LocationOfPoint aPtLocator;
  private LineStringLocation[] maxInterval = new LineStringLocation[2];
  /**
   * Create a new Maximal Nearest Subline of
   * {@link LineString} <code>a</code>
   * relative to {@link LineString} <code>b</code>
   *
   * @param a the LineString on which to compute the subline
   * @param b the LineString to compute the subline relative to
   */
  public MaximalNearestSubline(LineString a, LineString b) {
    this.a = a;
    this.b = b;
    aPtLocator = new LocationOfPoint(a);
  }

  /**
   * Computes the interval (range) containing the Maximal Nearest Subline.
   *
   * @return an array containing the minimum and maximum locations of the
   * Maximal Nearest Subline of <code>A</code>
   */
  public LineStringLocation[] getInterval() {

    /**
     * The basic strategy is to pick test points on B
     * and find their nearest point on A.
     * The interval containing these nearest points
     * is approximately the MaximalNeareastSubline of A.
     */

    // Heuristic #1: use every vertex of B as a test point
    CoordinateSequence bCoords = b.getCoordinateSequence();
    for (int ib = 0; ib < bCoords.size(); ib++) {
      findNearestOnA(bCoords.getCoordinate(ib));
    }

    /**
     * Heuristic #2:
     *
     * find the nearest point on B to all vertices of A
     * and use those points of B as test points.
     * For efficiency use only vertices of A outside current max interval.
     */
    LocationOfPoint bPtLocator = new LocationOfPoint(b);
    CoordinateSequence aCoords = a.getCoordinateSequence();
    for (int ia = 0; ia < aCoords.size(); ia++) {
      if (isOutsideInterval(ia)) {
        LineStringLocation bLoc = bPtLocator.locate(aCoords.getCoordinate(ia));
        Coordinate bPt = bLoc.getCoordinate();
        findNearestOnA(bPt);
      }
    }

    return maxInterval;
  }

  private boolean isOutsideInterval(int ia)
  {
    if (ia <= maxInterval[0].getSegmentIndex())
      return true;
    if (ia > maxInterval[1].getSegmentIndex())
      return true;
    return false;
  }

  private void findNearestOnA(Coordinate bPt) {
    LineStringLocation nearestLocationOnA = aPtLocator.locate(bPt);
    expandInterval(nearestLocationOnA);
  }

  private void expandInterval(LineStringLocation loc)
  {
    // expand maximal interval if this point is outside it
    if (maxInterval[0] == null || loc.compareTo(maxInterval[0]) < 0)
      maxInterval[0] = loc;
    if (maxInterval[1] == null || loc.compareTo(maxInterval[1]) > 0)
      maxInterval[1] = loc;
  }
}