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

package com.vividsolutions.jcs.graph;

import java.util.*;
import java.io.PrintStream;
import java.io.Serializable;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.Quadrant;

public class DirectedEdge
    implements Comparable, Serializable
{

  public static List toEdges(Collection dirEdges)
  {
    List edges = new ArrayList();
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      edges.add( ((DirectedEdge) i.next()).parentEdge);
    }
    return edges;
  }

  protected static final RobustCGAlgorithms cga = new RobustCGAlgorithms();

  protected Edge parentEdge;
  protected transient Node from;
  protected transient Node to;
  protected Coordinate p0, p1;
  protected DirectedEdge sym = null;  // optional
  protected boolean edgeDirection;
  protected int quadrant;
  protected double angle;

  public DirectedEdge(Node from, Node to, Coordinate directionPt, boolean edgeDirection)
  {
    this.from = from;
    this.to = to;
    this.edgeDirection = edgeDirection;
    p0 = from.getCoordinate();
    p1 = directionPt;
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;
    if (dx == 0 && dy == 0)
     System.out.println( "EdgeEnd with identical endpoints found");
    quadrant = Quadrant.quadrant(dx, dy);
    angle = Math.atan2(dy, dx);
  }

  public Edge getEdge() { return parentEdge; }
  public void setEdge(Edge parentEdge) { this.parentEdge = parentEdge; }

  public boolean getEdgeDirection() { return edgeDirection; }

  public Node getFromNode() { return from; }
  public Node getToNode() { return to; }
  /**
   * Returns the starting angle of this DirectedEdge.
   */
  public double getAngle() { return angle; }

  public DirectedEdge getSym() { return sym; }
  public void setSym(DirectedEdge sym) { this.sym = sym; }

  public int compareTo(Object obj)
  {
      DirectedEdge de = (DirectedEdge) obj;
      return compareDirection(de);
  }
  /**
   * Implements the total order relation:
   * <p>
   *    a has a greater angle with the positive x-axis than b
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is obviously susceptible to roundoff.
   * A robust algorithm is:
   * - first compare the quadrant.  If the quadrants
   * are different, it it trivial to determine which vector is "greater".
   * - if the vectors lie in the same quadrant, the computeOrientation function
   * can be used to decide the relative orientation of the vectors.
   */
  public int compareDirection(DirectedEdge e)
  {
    // if the rays are in different quadrants, determining the ordering is trivial
    if (quadrant > e.quadrant) return 1;
    if (quadrant < e.quadrant) return -1;
    // vectors are in the same quadrant - check relative orientation of direction vectors
    // this is > e if it is CCW of e
    return CGAlgorithms.computeOrientation(e.p0, e.p1, p1);
  }

  public void print(PrintStream out)
  {
    String className = getClass().getName();
    int lastDotPos = className.lastIndexOf('.');
    String name = className.substring(lastDotPos + 1);
    out.print("  " + name + ": " + p0 + " - " + p1 + " " + quadrant + ":" + angle);
  }

}
