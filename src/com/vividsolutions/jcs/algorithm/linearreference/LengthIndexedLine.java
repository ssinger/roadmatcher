package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;

/**
 * Supports linear referencing along a {@link LineString}
 * using the length along the line as the index.
 */
public class LengthIndexedLine
{
  private LineString inputLine;

  /**
   * Create a {@link LineString} which can be linearly referenced
   * using length as an index.
   *
   * @param line the line to reference
   */
  public LengthIndexedLine(LineString inputLine) {
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
  public Coordinate locate(double index)
  {
    return LocatePoint.pointAlongLine(inputLine, index);
  }

  /**
   * Computes the {@link LineString} for the interval
   * on the line between the given indices.
   *
   * @param startIndex the index of the start of the interval
   * @param endIndex the index of the end of the interval
   * @return the linear interval between the indices
   */
  public LineString locate(double startIndex, double endIndex)
  {
    return LengthSubstring.getSubstring(inputLine, startIndex, endIndex);
  }

  /**
   * Computes the index for a point on the line.
   * (The point does not <i>necessarily</i> have to lie precisely
   * on the line, but if it is far from the line the accuracy and
   * performance of this function is not guaranteed.
   * Use {@link #project} to compute a guaranteed result for points
   * known to not be on the line).
   *
   * @param pt a point on the line
   * @return the index of the point
   *
   * @see project
   */
  public double indexOf(Coordinate pt)
  {
    return LengthToPoint.length(inputLine, pt);
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
  public double[] indicesOf(LineString subLine)
  {
    Coordinate startPt = subLine.getCoordinateN(0);
    Coordinate endPt = subLine.getCoordinateN(subLine.getNumPoints() - 1);
    LengthOfPoint locPt = new LengthOfPoint(inputLine);
    double[] index = new double[2];
    index[0] = locPt.locate(startPt);

    // check for case where subline is zero length
    if (subLine.getLength() == 0.0) {
      index[1] = locPt.locate(endPt);
    }
    else  {
      index[1] = locPt.locateAfter(endPt, index[0]);
    }
    return index;
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
  public double project(Coordinate pt)
  {
    return LengthToPoint.length(inputLine, pt);
  }

  /**
   * Returns the index of the start of the line
   * @return
   */
  public double getStartIndex()
  {
    return 0.0;
  }

  /**
   * Returns the index of the end of the line
   * @return
   */
  public double getEndIndex()
  {
    return inputLine.getLength();
  }

  /**
   * Tests whether an index is in the valid index range for the line.
   *
   * @param length the index to test
   * @return <code>true</code> if the index is in the valid range
   */
  public boolean isValidIndex(double index)
  {
    return (index >= getStartIndex()
            && index <= getEndIndex());
  }

  /**
   * Computes a valid index for this line
   * by clamping the given index to the valid range of index values
   *
   * @return a valid index value
   */
  public double clampIndex(double index)
  {
    double startIndex = getStartIndex();
    if (index < startIndex) return startIndex;

    double endIndex = getEndIndex();
    if (index > endIndex) return endIndex;

    return index;
  }
}