package com.vividsolutions.jcs.conflate.roads.match;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.*;

/**
 * Computes the match confidence value for two Linestrings representing
 * RoadSegments.
 * A return value of 0.0 indicates a definite non-match.
 */
public class OverlapMatchEvaluator
    implements GeometryMatchEvaluator
{

  private double distanceTolerance = 10.0;
  /**
   * Maximum difference in length of matched segments (as a percentage)
   */
  private double lengthDifferenceTolerance = .05;
  /**
   * Minimum ratio of overlap to source line (as a percentage)
   */
  private double overlapRatioTolerance = 0.3;
  private boolean useOverlapRatio = false;

  /**
   * Minimum difference of overlap length to source line length
   */
  private double overlapLengthDifferenceTolerance = 2 * distanceTolerance;

  public OverlapMatchEvaluator() {
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }
  public void setLengthDifferenceTolerance(double lengthDifferenceTolerance)
  {
    this.lengthDifferenceTolerance = lengthDifferenceTolerance;
  }
  public void setOverlapRatioTolerance(double overlapRatioTolerance)
  {
    this.overlapRatioTolerance = overlapRatioTolerance;
    useOverlapRatio = true;
  }
  public void setOverlapDifferenceTolerance(double overlapLengthDifferenceTolerance)
  {
    this.overlapLengthDifferenceTolerance = overlapLengthDifferenceTolerance;
    useOverlapRatio = false;
  }

  public double match(Geometry geom0, Geometry geom1)
  {
    LineString line = (LineString) geom0;
    LineString testLine =  (LineString) geom1;

    // if lines are roughly straight, they must have similar orientation
    // or else they don't match
    if (LineStringShapeMatcher.isApproximatelyStraight(line)
        && LineStringShapeMatcher.isApproximatelyStraight(testLine)) {
      if (! LineStringShapeMatcher.isOrientationCompatible(line, testLine))
        return 0.0;
    }

    // if the lines are very different in length they don't match
    if (LineStringShapeMatcher.lengthDifferencePercent(line, testLine) < lengthDifferenceTolerance)
      return 0.0;

    // check if too far away
    if (! geom0.isWithinDistance(geom1, distanceTolerance))
        return 0.0;

    //return computeVertexHausdorffDistanceMatchValue(edge0, edge1);
    return computeOverlapDistanceMatchValue(geom0, geom1);
  }

  private double computeVertexHausdorffDistanceMatchValue(Geometry edge0, Geometry edge1)
  {
    //double len0 = edge0.getLength();
    //double len1 = edge1.getLength();
    //double edgeSep = edge0.distance(edge1);

    // match value based on VertexHausdorffDistance
    VertexHausdorffDistance vhDist = new VertexHausdorffDistance(edge0, edge1);
    double hausdorrfDist = vhDist.distance();

    // set an upper bound for distances that are considered a match
    //double distanceUpperBound = Math.max(len0, len1) + edgeSep;
    //double distanceUpperBound = 3 * Math.min(len0, len1);
    double distanceUpperBound = distanceTolerance;

    // the further the distance the worse the match
    double hausdorrfDistMatchValue = MatchValueCombiner.scale(hausdorrfDist, distanceUpperBound, 0.0);
    return hausdorrfDistMatchValue;
  }

  private double computeOverlapDistanceMatchValue(Geometry g0, Geometry g1)
  {
    LineString line0 = (LineString) g0;
    LineString line1 = (LineString) g1;
    double len0 = line0.getLength();
    double len1 = line1.getLength();

    LineString overlap01 = LineStringProjector.project(line0, line1);
    LineString overlap10 = LineStringProjector.project(line1, line0);

    double overlap01Len = overlap01.getLength();
    double overlap10Len = overlap10.getLength();

    if (useOverlapRatio) {
      // check for reasonable ratio of overlap to source length
      if (overlap01Len / len1 < overlapRatioTolerance) return 0.0;
      if (overlap10Len / len0 < overlapRatioTolerance) return 0.0;
    }
    else {
      // check for reasonable absolute length difference between overlap and source
      // this code depends on the fact that len > overlapLen
      if (len1 - overlap01Len > overlapLengthDifferenceTolerance) return 0.0;
      if (len0 - overlap10Len > overlapLengthDifferenceTolerance) return 0.0;
    }
    // match value based on VertexHausdorffDistance
    VertexHausdorffDistance vhDist = new VertexHausdorffDistance(overlap01, overlap10);
    double hausdorrfDist = vhDist.distance();

    // set an upper bound for distances that are considered a match
    //double distanceUpperBound = Math.max(len0, len1) + edgeSep;
    //double distanceUpperBound = 3 * Math.min(len0, len1);
    double distanceUpperBound = 3 * distanceTolerance;

    // the further the distance the worse the match
    double matchValue = MatchValueCombiner.scale(hausdorrfDist, distanceUpperBound, 0.0);
    return matchValue;
  }


}