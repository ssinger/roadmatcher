package com.vividsolutions.jcs.conflate.roads.match;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.*;

/**
 * Computes various distance functions for determining
 * how far apart are two matched segments.
 * In general these functions use the Hausdorff distance to compute
 * how far apart geometries are, since the normal Euclidean distance
 * is not a useful measure of "far apartness".
 */
public class MatchDistance {

  /**
   * Computes the maximum distance apart between two linestrings.
   * (Note this is NOT the distance between the two furthest points
   * on the linestrings, which is not a useful measure of "farness").
   *
   * @param a
   * @param b
   * @return
   */
  public static double maxDistance(LineString a, LineString b)
  {
    return VertexHausdorffDistance.distance(a, b);
  }

  /**
   * Computes how far apart are two linestrings
   * after trimming any unmatched length at the ends.
   *
   * @param a
   * @param b
   * @return
   *
   * @see MaximalNearestSubline
   */
  public static double trimmedDistance(LineString a, LineString b)
  {
    LineString trimA = MaximalNearestSubline.getMaximalNearestSubline(a, b);
    LineString trimB = MaximalNearestSubline.getMaximalNearestSubline(b, a);
    return VertexHausdorffDistance.distance(trimA, trimB);
  }

  /**
   * Computes the fraction of length of matched LineStrings which is
   * nearer than a given tolerance value (optionally after trimming).
   * The previously computed maxDistance between the lines
   * can be supplied to allow optimizing the calculation
   * (if maxDistance < tolerance, the nearness fraction = 1.0).
   *
   * @param a a LineString
   * @param b a LineString
   * @param maxDistance the maximum distance between the lines (if previously computed)
   * @param tolerance the distance beyond which to total the length
   * @param trimLines <code>true</code> if the computation should take the trimmed lines into account
   *
   * @return the fraction of matched line length beyond the tolerance
   */
  public static double nearnessFraction(LineString a, LineString b,
                                        double maxDistance, double tolerance,
                                        boolean trimLines)
  {
    // if the orginal lines are closer than the tolerance there is no need for further computation
    if (maxDistance < tolerance)
      return 1.0;
    return nearnessFraction(a, b, tolerance, trimLines);
  }

  /**
   * Computes the fraction of length of LineStrings which is
   * within a given tolerance value, after trimming.
   *
   * @param a a LineString
   * @param b a LineString
   * @param tolerance the distance beyond which to total the length
   * @param trimLines <code>true</code> if the computation should take the trimmed lines into account

   * @return the fraction of matched line length beyond the tolerance
   */
  public static double nearnessFraction(LineString a, LineString b,
                                        double tolerance,
                                        boolean trimLines)
  {
    double nearnessFrac = 0.0;
    nearnessFrac = nearnessFraction(a, b, tolerance);

    if (trimLines) {
      LineString trimmedA = MaximalNearestSubline.getMaximalNearestSubline(a, b);
      LineString trimmedB = MaximalNearestSubline.getMaximalNearestSubline(b, a);
      double trimmedNF = nearnessFraction(trimmedA, trimmedB, tolerance);
      // choose the largest fraction
      // (it can happen that the original nearness is greater, if the lines are not well-aligned)
      if (trimmedNF > nearnessFrac)
        nearnessFrac = trimmedNF;
    }
    return nearnessFrac;
  }

  /**
   * Computes the fraction of length of LineStrings which is
   * within a given tolerance value, after trimming.
   * The previously computed maxDistance between the lines
   * can be supplied to allow optimizing the calculation
   * (if maxDistance < tolerance, the nearness fraction = 1.0).
   *
   * @param a a LineString
   * @param b a LineString
   * @param fullDistance the full distance between the lines (previously computed)
   * @param tolerance the distance beyond which to total the length
   * @return the fraction of length beyond the tolerance
   */
  public static double nearnessFraction(LineString a, LineString b,
                                        double tolerance)
  {
    double lenA = a.getLength();
    double lenB = b.getLength();
    double lenAB = lenA + lenB;
    // this can happen if the segments are badly aligned and get trimmed to points
    if (lenAB <= 0.0) {
      boolean inTolerance = a.distance(b) <= tolerance;
      return inTolerance ? 0.0 : 1.0;
    }

    double farLenA = farLength(a, b, tolerance);
    double farLenB = farLength(b, a, tolerance);
    double nearLenA = lenA - farLenA;
    double nearLenB = lenB - farLenB;

    // avoid division by 0.0
    double nearPctA = lenA > 0 ? nearLenA / lenA : 0.0;
    double nearPctB = lenB > 0 ? nearLenB / lenB : 0.0;

    // choose the worst case scenario as the final value
    double nearnessFrac = Math.min(nearPctA, nearPctB);
    return nearnessFrac;
  }

  private static double farLength(Geometry a, Geometry b, double tolerance)
  {
    Geometry farA = a.difference(b.buffer(tolerance));
    double farALen = farA.getLength();
    return farALen;
  }

  private MatchDistance() {
  }
}