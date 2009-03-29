package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;

/**
 * Supports linear referencing along a {@link LineString}
 * using {@link LineStringLocation}s as the index.
 */
public class LocationIndexedLine
{
  private LineString inputLine;

  /**
   * Create a {@link LineString} which can be linearly referenced
   * using length as an index.
   *
   * @param line the line to reference
   */
  public LocationIndexedLine(LineString inputLine) {
    this.inputLine = inputLine;
  }

  /**
   * Computes the {@link Coordinate} for the point
   * on the line at the given index.
   * If the index is out of range the first or last point on the
   * line will be returned.
   *
   * @param length the index of the desired point
   * @return the Coordinate at the given index
   */
  public Coordinate locate(LineStringLocation index)
  {
    Coordinate p0 = inputLine.getCoordinateN(index.getSegmentIndex());
    Coordinate p1 = inputLine.getCoordinateN(index.getSegmentIndex() + 1);
    return LineStringLocation.pointAlongSegmentByFraction(p0, p1, index.getSegmentFraction());
  }

  /**
   * Computes the {@link LineString} for the interval
   * on the line between the given indices.
   *
   * @param startIndex the index of the start of the interval
   * @param endIndex the index of the end of the interval
   * @return the linear interval between the indices
   */
  public LineString locate(LineStringLocation startIndex, LineStringLocation endIndex)
  {
    return LocationSubString.getSubstring(inputLine, startIndex, endIndex);
  }

  /**
   * Computes the index for a given point on the line.
   * (The point does not necessarily have to lie precisely
   * on the line, but if it is far from the line the accuracy and
   * performance of this function is not guaranteed).
   *
   * @param pt a point on the line
   * @return the index of the point
   */
  public LineStringLocation indexOf(Coordinate pt)
  {
    return LocationOfPoint.locate(inputLine, pt);
  }

  /**
   * Computes the indices for a subline of the line.
   * (The subline must <b>conform</b> to the line; that is,
   * all vertices in the subline (except possibly the first and last)
   * must be vertices of the line and occcur in the same order).
   *
   * @param subLine a subLine of the line
   * @return a pair of indices for the start and end of the subline.
   */
  public LineStringLocation[] indicesOf(LineString subLine)
  {
    return LocationOfSubLine.locate(inputLine, subLine);
  }

  /**
   * Computes the index for the closest point on the line to the given point.
   * If more than one point has the closest distance the first one along the line
   * is returned.
   * (The point does not necessarily have to lie precisely on the line.)
   *
   * @param pt a point on the line
   * @return the index of the point
   */
  public LineStringLocation project(Coordinate pt)
  {
    return LocationOfPoint.locate(inputLine, pt);
  }

  /**
   * Returns the index of the start of the line
   * @return
   */
  public LineStringLocation getStartIndex()
  {
    return new LineStringLocation(inputLine, 0, 0.0);
  }

  /**
   * Returns the index of the end of the line
   * @return
   */
  public LineStringLocation getEndIndex()
  {
    return new LineStringLocation(inputLine, inputLine.getNumPoints() - 1, 1.0);
  }

  /**
   * Tests whether an index is in the valid index range for the line.
   *
   * @param length the index to test
   * @return <code>true</code> if the index is in the valid range
   */
  public boolean isValidIndex(LineStringLocation index)
  {
    return (index.compareTo(getStartIndex()) >= 0
            && index.compareTo(getEndIndex()) <= 0);
  }

  /**
   * Computes a valid index for this line
   * by clamping the given index to the valid range of index values
   *
   * @return a valid index value
   */
  public LineStringLocation clampIndex(LineStringLocation index)
  {
    LineStringLocation startIndex = getStartIndex();
    if (index.compareTo(startIndex) <= 0) return startIndex;

    LineStringLocation endIndex = getEndIndex();
    if (index.compareTo(endIndex) >= 0) return endIndex;

    return index;
  }
}