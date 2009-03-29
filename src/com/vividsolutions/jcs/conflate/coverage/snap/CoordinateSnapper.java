/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jcs.conflate.coverage.snap;

import com.vividsolutions.jts.geom.*;
import java.util.*;

/**
 * Note: this class is not thread-safe.
 */
public class CoordinateSnapper {

  public Coordinate closestPt(List closePts, Coordinate pt)
  {
    Coordinate closestPt = null;
    double minDistance = 0.0;

    for (Iterator i = closePts.iterator(); i.hasNext(); ) {
      Coordinate closePt = (Coordinate) i.next();
      double distance = closePt.distance(pt);
      if (closestPt == null || distance < minDistance) {
        closestPt = closePt;
        minDistance = distance;
      }
    }
    return closestPt;
  }

  private SlowPointIndex ptIndex;
  private Envelope queryEnv = new Envelope();

  public CoordinateSnapper(SlowPointIndex ptIndex)
  {
    this.ptIndex = ptIndex;
  }

  /**
   * Compute the closest point within the given distance, if any.
   * @param pt
   * @param distance
   * @return the reference point to snap to, if any
   *   the original point if there were no reference points within the given distance
   */
  public Coordinate snap(Coordinate pt, double distance)
  {
    queryEnv.init(pt.x - distance, pt.x + distance, pt.y - distance, pt.y + distance);
    List closePts = ptIndex.query(queryEnv);
    Coordinate closestPt = closestPt(closePts, pt);
    if (closestPt == null) return pt;
    return closestPt;
  }


}
