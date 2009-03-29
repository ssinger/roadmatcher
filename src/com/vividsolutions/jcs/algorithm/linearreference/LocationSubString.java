package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jcs.geom.LineStringUtil;

/**
 * Computes the subline of a {@link LineString} between
 * two {@link LineStringLocation}s on the line.
 */
public class LocationSubString {

  public static LineString getSubstring(LineString line, LineStringLocation start, LineStringLocation end)
  {
    LocationSubString ls = new LocationSubString(line);
    return ls.getSubstring(start, end);
  }

  private LineString line;

  public LocationSubString(LineString line) {
    this.line = line;
  }

  public LineString getSubstring(LineStringLocation start, LineStringLocation end)
  {
    if (end.compareTo(start) < 0) {
      return LineStringUtil.reverse(computeSubstring(end, start));
    }
    return computeSubstring(start, end);
  }

  /**
   * Assumes input is valid (e.g. startDist < endDistance)
   *
   * @param startDistance
   * @param endDistance
   * @return
   */
  private LineString computeSubstring(LineStringLocation start, LineStringLocation end)
  {
    Coordinate[] coordinates = line.getCoordinates();
    CoordinateList newCoordinates = new CoordinateList();

    int startSegmentIndex = start.getSegmentIndex();
    if (start.getSegmentFraction() > 0.0)
      startSegmentIndex += 1;
    int lastSegmentIndex = end.getSegmentIndex();
    if (end.getSegmentFraction() >= 1.0)
      lastSegmentIndex += 1;

    if (! start.isVertex())
      newCoordinates.add(start.getCoordinate());
    for (int i = startSegmentIndex; i <= lastSegmentIndex; i++) {
      newCoordinates.add(coordinates[i]);
    }
    if (! end.isVertex())
      newCoordinates.add(end.getCoordinate());

    Coordinate[] newCoordinateArray = newCoordinates.toCoordinateArray();
    /**
     * Ensure there is enough coordinates to build a valid line.
     * Make a 2-point line with duplicate coordinates, if necessary.
     * There will always be at least one coordinate in the coordList.
     */
    if (newCoordinateArray.length <= 1) {
      newCoordinateArray = new Coordinate[] { newCoordinateArray[0], newCoordinateArray[0]};
    }
    return line.getFactory().createLineString(newCoordinateArray);
  }

}