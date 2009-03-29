package com.vividsolutions.jcs.conflate.linearpathmatch.match;

import com.vividsolutions.jts.geom.*;

/**
 * Trims a {@link LineString} to be within a distance tolerance of
 * a given point.
 *
 * @version 1.0
 */
public class LineStringTrimmer
{

  /**
   * Trims the end of a linestring.
   * If only a portion of the first line segment is within the
   * tolerance, an empty linestring is returned.
   *
   * @param inputLine the linestring to trim
   * @param maskPt the point to trim to
   * @param distanceTolerance the distance tolerance to use in trimming
   * @return the trimmed linestring (which may be empty)
   */
  public static LineString trimEnd(LineString inputLine, Coordinate maskPt, double distanceTolerance)
  {
    LineStringTrimmer trimmer = new LineStringTrimmer(inputLine);
    trimmer.setDistanceTolerance(distanceTolerance);
    trimmer.trimEnd(maskPt);
    return trimmer.getGeometry();
  }

  private LineString inputLine;
  private double distanceTolerance = 0.0;
  private Coordinate[] trimmedPts;

  public LineStringTrimmer(LineString inputLine)
  {
    this.inputLine = inputLine;
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Computes the input linestring trimmed at the end.
   * If only a portion of the first line segment is within the
   * tolerance, an empty linestring is returned.
   *
   * @param maskPt the point to trim to
   */
  public void trimEnd(Coordinate maskPt)
  {
    Coordinate closestPt = null;
    boolean isClosestAtVertex = false;
    int lastIndex = -1;
    Coordinate[] pts = inputLine.getCoordinates();
    for (int i = pts.length - 2; i >= 0; i--) {
      LineSegment seg = new LineSegment(pts[i], pts[i + 1]);
      Coordinate candidateClosestPt = seg.closestPoint(maskPt);
      double closestDist = maskPt.distance(candidateClosestPt);
      if (closestDist <= distanceTolerance) {
        closestPt = candidateClosestPt;
        lastIndex = i;
        isClosestAtVertex = closestPt.equals(pts[i]);
        break;
      }
    }

    // no close point found, so trimmed line is empty
    if (closestPt == null) {
      trimmedPts = new Coordinate[0];
      return;
    }

    // found a close point, so copy input pts up to and including it
    int trimmedPtsSize = lastIndex + 1;
    if (! isClosestAtVertex)
      trimmedPtsSize += 1;
    // check for
    if (trimmedPtsSize <= 1) {
      trimmedPts = new Coordinate[0];
      return;
    }

    trimmedPts = new Coordinate[trimmedPtsSize];
    copyCoords(pts, 0, lastIndex + 1, trimmedPts);
    if (! isClosestAtVertex)
      trimmedPts[trimmedPts.length - 1] = closestPt;
  }

  private static void copyCoords(Coordinate[] input, int start, int count, Coordinate[] output)
  {
    int outputIndex = 0;
    for (int i = start; i <= start + count - 1; i++) {
      output[outputIndex++] = input[i];
    }
  }

  public LineString getGeometry()
  {
    return inputLine.getFactory().createLineString(trimmedPts);
  }
}