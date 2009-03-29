

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

package com.vividsolutions.jcs.conflate.boundarymatch;

import com.vividsolutions.jts.geom.*;
import java.util.*;

public class Vertex
{
  Coordinate pt;
  double position;
  double distance;
  Vertex match;

  public Vertex(Coordinate pt)
  {
    this.pt = pt;
  }

  public Vertex(Coordinate pt, LineSegment line)
  {
    this.pt = pt;
    position = line.projectionFactor(pt);
  }

  public Coordinate getCoordinate() { return pt; }
  public boolean isAdjusted()  {    return distance > 0.0;  }
  public Coordinate getAdjustedCoordinate()
  {
    return ((Vertex) match).getCoordinate();
  }

  public void setMatchPoint(Coordinate matchPt)
  {
    setMatchComponent(new Vertex(matchPt), pt.distance(matchPt));
  }

  public void setMatchComponent(Vertex match, double distance)
  {
    this.match = match;
    this.distance = distance;
  }

  private static GeometryFactory fact = new GeometryFactory();

  /**
   * Make a linestring showing how this vertex is adjusted
   */
  public Geometry getAdjustedIndicator()
  {
    Coordinate matchPt = ((Vertex) match).getCoordinate();
    Coordinate[] pts = { pt, matchPt };
    LineString line = fact.createLineString(pts);
    return line;
  }

  public static class PositionComparator
      implements Comparator
  {
    /**
     *  Compares two Vertices for order.
     *  Uses the position along a line segment as the basis of
     *  the comparison.
     *
     *@param  o1  the first <code>Vertex</code>
     *@param  o2  the second <code>Vertex</code>
     *
     *@return    a negative integer, zero, or a positive integer
     *        as the first argument is less than, equal to,
     *        or greater than the second.
     */
    public int compare(Object o1, Object o2)
    {
      Vertex v1 = (Vertex) o1;
      Vertex v2 = (Vertex) o2;
      if (v1.position < v2.position) return -1;
      if (v1.position > v2.position) return 1;
      return 0;
    }
    /**
     *  Compares two Vertices for order.
     *  Uses the position along a line segment as the basis of
     *  the comparison.
     *
     *@param  o1  the first <code>Vertex</code>
     *@param  o2  the second <code>Vertex</code>
     *
     *@return    a negative integer, zero, or a positive integer
     *        as the first argument is less than, equal to,
     *        or greater than the second.
     */
    /* NOT USED???
    public boolean equals(Object o1, Object o2)
    {
      Vertex v1 = (Vertex) o1;
      Vertex v2 = (Vertex) o2;
      return v1.position == v2.position;
    }
    */
  }
}
