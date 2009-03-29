package com.vividsolutions.jcs.algorithm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.linearreference.*;

/**
 * Computes the "overlap" of one LineStrings on another.
 *
 * @version 1.0
 */
public class LineStringProjector
{

  /**
   * Computes the projection of a LineString on another LineString.
   * @param line1 the LineString to project
   * @param line2 the LineString to project onto
   * @return a LineString which is the projection of <code>line1</code> on <code>line2</code>
   */
  public static LineString project(LineString line1, LineString line2)
  {
    Coordinate p0 = line1.getCoordinateN(0);
    Coordinate pn = line1.getCoordinateN(line1.getNumPoints() - 1);

    double len0 = LengthToPoint.length(line2, p0);
    double lenn = LengthToPoint.length(line2, pn);

    if (len0 > lenn) {
      double temp = len0;
      len0 = lenn;
      lenn = temp;
    }
    return LengthSubstring.getSubstring(line2, len0, lenn);
  }

  // only contains static methods for now
  private LineStringProjector()
  {

  }

}