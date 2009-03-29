

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

import com.vividsolutions.jts.geom.Coordinate;
import java.util.*;

/**
 * Represents a vertex in the edge of a polygon shell in a coverage.
 * A vertex can be adjusted to a new coordinate value.
 * The vertex implements some heuristics to limit the adjustment.
 * It maintains an ajustment tolerance, and will not adjust the vertex further
 * than this tolerance.  This is set to half the length of the smallest segment
 * incident on the vertex, the idea being to eliminate the possibility of the
 * endpoints of a segment being adjusted to "cross over" one another.
 */
public class Vertex
    extends GeometryComponent
    implements Comparable
{

  private Coordinate pt;
  private Coordinate adjustedPt = null;
  private double adjustTolerance = Double.MAX_VALUE;
  private int shellCount = 0;

  private Set shells = new HashSet();

  public Vertex(Coordinate pt)
  {
    this.pt = pt;
  }

  public Coordinate getOriginalCoordinate() { return pt; }

  public void setMinimumAdjustmentTolerance(double tol)
  {
    if (tol < adjustTolerance) adjustTolerance = tol;
  }

  public void addShell(Shell shell)
  {
    shells.add(shell);
  }

  public int getShellCount()
  {
    if (shellCount <= 0)
      shellCount = shells.size();
    return shellCount;
  }

  public Coordinate getCoordinate()
  {
    if (adjustedPt != null && ! isConflict() )
      return adjustedPt;
    return pt;
  }
/* NOT USED
  public Coordinate getCurrentCoordinate()
  {
    if (adjustedPt != null && ! isConflict() )
      return adjustedPt;
    return pt;
  }
*/
  public Coordinate getAdjustedCoordinate() { return adjustedPt; }

  public void setAdjusted(Coordinate adjustedPt)
  {
    if (! adjustedPt.equals(pt))
      this.adjustedPt = adjustedPt;
  }

  public boolean isAdjusted() { return adjustedPt != null; }

  /**
   * Snaps this vertex and another one together.
   * Conflict resolution rules are used to decide which vertex is adjusted,
   * if any.
   *
   * @param v the Vertex to snap with
   * @return <code>true</code> if the vertex was snapped
   */
  public boolean snap(Vertex v)
  {
    boolean isSnapped = false;
    // if the current coords are equal, we are done
    if (getCoordinate().equals(v.getCoordinate()))
      return true;

    // if the snap is outside tolerance for this vertex, do not snap
    double snapDist = v.getOriginalCoordinate().distance(pt);
    if (snapDist > adjustTolerance)
      return false;

    // if neither have been adjusted, adjust both to the snap point
    // (Note only one will actually get adjusted)
    if (! isAdjusted() && ! v.isAdjusted()) {
      // find the preferred adjustment value
      Coordinate snapPt = getOriginalCoordinate();
      if (v.getShellCount() > getShellCount())
        snapPt = v.getOriginalCoordinate();

      setAdjusted(snapPt);
      v.setAdjusted(snapPt);
      isSnapped = true;
    }

    // if the coordinates do not match after adjustment, we have a conflict
    if (! getCoordinate().equals(v.getCoordinate())) {
      setConflict(true);
      v.setConflict(true);
      isSnapped = false;
    }
    return isSnapped;
  }

  /**
   *  Compares two Vertexes for order.
   *  Uses the ordering on the underlying Coordinate.
   *
   *@param  o1  the first  <code>Vertex</code>
   *@param  o2  the second <code>Vertex</code>
   *
   *@return    a negative integer, zero, or a positive integer
   *        as the first argument is less than, equal to,
   *        or greater than the second.
   */
  public int compareTo(Object o)
  {
    Vertex v = (Vertex) o;
    return pt.compareTo(v.pt);
  }
}
