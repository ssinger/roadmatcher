package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;

/**
 * Represents a location along a {@link LineString}.
 */
public class LineStringLocation
    implements Comparable
{

  /**
   * Computes the location of a point a given length along a line segment.
   * If the length exceeds the length of the line segment the last
   * point of the segment is returned.
   * If the length is negative the first point
   * of the segment is returned.
   *
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinate} of the desired point
   */
  public static Coordinate pointAlongSegmentByFraction(Coordinate p0, Coordinate p1, double frac)
  {
    if (frac <= 0.0) return p0;
    if (frac >= 1.0) return p1;

    double x = (p1.x - p0.x) * frac + p0.x;
    double y = (p1.y - p0.y) * frac + p0.y;
    return new Coordinate(x, y);
  }

  private LineString line;
  private int segmentIndex;
  private double segmentFraction;

  public LineStringLocation(LineString line, int segmentIndex, double segmentFraction) {
    this.line = line;
    this.segmentIndex = segmentIndex;
    this.segmentFraction = segmentFraction;
    normalize();
  }

  /**
   * Ensures the values in this object are valid
   */
  private void normalize()
  {
    if (segmentFraction < 0.0) segmentFraction = 0.0;
    if (segmentFraction > 1.0) segmentFraction = 1.0;

    if (segmentIndex < 0) {
      segmentIndex = 0;
      segmentFraction = 0.0;
    }
    else if (segmentIndex >= line.getNumPoints()) {
      segmentIndex = line.getNumPoints() - 1;
      segmentFraction = 1.0;
    }
  }
  public LineString getLine() { return line; }
  public int getSegmentIndex() { return segmentIndex; }
  public double getSegmentFraction() { return segmentFraction; }
  public boolean isVertex()
  {
    return segmentFraction <= 0.0 || segmentFraction >= 1.0;
  }
  public boolean isFirst()
  {
    return segmentIndex == 0 && segmentFraction == 0.0;
  }
  public boolean isLast()
  {
    return segmentIndex == line.getNumPoints() - 1 && segmentFraction == 1.0;
  }

  public Coordinate getCoordinate()
  {
    Coordinate p0 = line.getCoordinateN(segmentIndex);
    Coordinate p1 = line.getCoordinateN(segmentIndex + 1);
    return pointAlongSegmentByFraction(p0, p1, segmentFraction);
  }

  /**
   *  Compares this object with the specified object for order.
   *
   *@param  o  the <code>LineStringLocation</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineStringLocation</code>
   *      is less than, equal to, or greater than the specified <code>LineStringLocation</code>
   */
  public int compareTo(Object o) {
    LineStringLocation other = (LineStringLocation) o;
    // compare segments
    if (segmentIndex < other.segmentIndex) return -1;
    if (segmentIndex > other.segmentIndex) return 1;
    // same segment, so compare segment fraction
    if (segmentFraction < other.segmentFraction) return -1;
    if (segmentFraction > other.segmentFraction) return 1;
    // same location
    return 0;
  }

  public static int compareLocationValues(int segmentIndex0, double segmentFraction0,
                           int segmentIndex1, double segmentFraction1)
  {
    // compare segments
    if (segmentIndex0 < segmentIndex1) return -1;
    if (segmentIndex0 > segmentIndex1) return 1;
    // same segment, so compare segment fraction
    if (segmentFraction0 < segmentFraction1) return -1;
    if (segmentFraction0 > segmentFraction1) return 1;
    // same location
    return 0;
  }

  public Object clone()
  {
    return new LineStringLocation(line, segmentIndex, segmentFraction);
  }
}