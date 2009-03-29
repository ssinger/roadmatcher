package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;

/**
 * Determines the location of a subline along a {@link LineString}.
 * The location is reported as a pair of {@link LineStringLocation}s.
 * <p>
 * <b>Note:</b> Currently this algorithm is not guaranteed to
 * return the correct substring in some situations where
 * an endpoint of the test line occurs more than once in the input line.
 * (However, the common case of a ring is always handled correctly).
 */
public class LocationOfSubLine
{

  public static LineStringLocation[] locate(LineString line, LineString subLine)
  {
    LocationOfSubLine locater = new LocationOfSubLine(line);
    return locater.locate(subLine);
  }

  private LineString line;

  public LocationOfSubLine(LineString line) {
    this.line = line;
  }

  public LineStringLocation[] locate(LineString subLine)
  {
    Coordinate startPt = subLine.getCoordinateN(0);
    Coordinate endPt = subLine.getCoordinateN(subLine.getNumPoints() - 1);
    LocationOfPoint locPt = new LocationOfPoint(line);
    LineStringLocation[] subLineLoc = new LineStringLocation[2];
    subLineLoc[0] = locPt.locate(startPt);

    // check for case where subline is zero length
    if (subLine.getLength() == 0.0) {
      subLineLoc[1] = locPt.locate(endPt);
    }
    else  {
      subLineLoc[1] = locPt.locateAfter(endPt, subLineLoc[0]);
    }
    return subLineLoc;
  }
}