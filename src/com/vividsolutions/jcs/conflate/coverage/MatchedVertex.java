

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

package com.vividsolutions.jcs.conflate.coverage;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * Represents a vertex of a Segment that has been matched to a Segment.
 * Allows added vertices to be sorted along a segment, and kept only
 * if they project onto the segment
 */
public class MatchedVertex
     implements Comparable
{
  private Vertex vertex;
  private double position;

  public MatchedVertex(Vertex vertex)
  {
    this.vertex = vertex;
  }
  public double getPosition() { return position; }
  public Vertex getVertex() { return vertex; }

  public void computePosition(LineSegment seg)
  {
    position = seg.projectionFactor(vertex.getCoordinate());
  }

  /**
   *  Compares two MatchedVertex for order.
   *  Uses the position along the parent line segment as the basis of
   *  the comparison.
   *
   *@param  o1  the first  <code>MatchedVertex</code>
   *@param  o2  the second <code>MatchedVertex</code>
   *
   *@return    a negative integer, zero, or a positive integer
   *        as the first argument is less than, equal to,
   *        or greater than the second.
   */
  public int compareTo(Object o)
  {
    MatchedVertex v = (MatchedVertex) o;
    if (position < v.position) return -1;
    if (position > v.position) return 1;
    return 0;
  }
}
