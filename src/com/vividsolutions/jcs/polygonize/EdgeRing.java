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

package com.vividsolutions.jcs.polygonize;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.graph.*;

public class EdgeRing {

  /**
   * Find the innermost enclosing shell EdgeRing containing the argument EdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B iff envelope(ring A) contains envelope(ring B)
   * <br>
   * This routine is only safe to use if the chosen point of the hole
   * is known to be properly contained in a shell
   * (which is guaranteed to be the case if the hole does not touch its shell)
   *
   * @return containing EdgeRing, if there is one
   * @return null if no containing EdgeRing is found
   */
  public static EdgeRing findEdgeRingContaining(EdgeRing testEr, List shellList)
  {
    LinearRing testRing = testEr.getRing();
    Envelope testEnv = testRing.getEnvelopeInternal();
    Coordinate testPt = testRing.getCoordinateN(0);

    EdgeRing minShell = null;
    Envelope minEnv = null;
    for (Iterator it = shellList.iterator(); it.hasNext(); ) {
      EdgeRing tryShell = (EdgeRing) it.next();
      LinearRing tryRing = tryShell.getRing();
      Envelope tryEnv = tryRing.getEnvelopeInternal();
      if (minShell != null) minEnv = minShell.getRing().getEnvelopeInternal();
      boolean isContained = false;
      // the hole envelope cannot equal the shell envelope
      if (tryEnv.equals(testEnv))
        continue;

      testPt = ptNotInList(testRing.getCoordinates(), tryRing.getCoordinates());
      if (tryEnv.contains(testEnv)
          && cga.isPointInRing(testPt, tryRing.getCoordinates()) )
        isContained = true;
      // check if this new containing ring is smaller than the current minimum ring
      if (isContained) {
        if (minShell == null
            || minEnv.contains(tryEnv)) {
          minShell = tryShell;
        }
      }
    }
    return minShell;
  }

  public static boolean isInside(LinearRing shell, LinearRing hole)
  {
    Coordinate holePt = hole.getCoordinates()[0];
    Coordinate[] shellPts = shell.getCoordinates();
    boolean isInside = cga.isPointInRing(holePt, shellPts);
    return isInside;
  }

  public static Coordinate ptNotInList(Coordinate[] testPts, Coordinate[] pts)
  {
    for (int i = 0; i < testPts.length; i++) {
      Coordinate testPt = testPts[i];
      if (isInList(testPt, pts))
          return testPt;
    }
    return null;
  }
  public static boolean isInList(Coordinate pt, Coordinate[] pts)
  {
    for (int i = 0; i < pts.length; i++) {
        if (pt.equals(pts[i]))
            return false;
    }
    return true;
  }
  private static GeometryFactory fact = new GeometryFactory();
  private static CGAlgorithms cga = new RobustCGAlgorithms();


  private List deList = new ArrayList();

  // cache the following data for efficiency
  private double area = -1;
  private LinearRing ring = null;

  private Coordinate[] ringPts = null;
  // the ring containing this hole
  private EdgeRing shellER = null;
  private List holes;

  public EdgeRing() {
  }

  public void add(DirectedEdge de)
  {
    deList.add(de);
  }

  public double getArea()
  {
    if (area < 0.0) {
      area = getRing().getArea();
    }
    return area;
  }

  public EdgeRing getShell() { return shellER; }
  public void setShell(EdgeRing shellER) { this.shellER = shellER; }

  public boolean isHole()
  {
    LinearRing ring = getRing();
    return cga.isCCW(ring.getCoordinates());
  }

  public void addHole(LinearRing hole) {
    if (holes == null)
      holes = new ArrayList();
    holes.add(hole);
  }

  public Polygon getPolygon()
  {
    LinearRing[] holeLR = null;
    if (holes != null) {
      holeLR = new LinearRing[holes.size()];
      for (int i = 0; i < holes.size(); i++) {
        holeLR[i] = (LinearRing) holes.get(i);
      }
    }
    Polygon poly = fact.createPolygon(ring, holeLR);
    return poly;
  }

  public boolean isValid()
  {
    getCoordinates();
    if (ringPts.length <= 3) return false;
    getRing();
    return ring.isValid();
  }

  public Coordinate[] getCoordinates()
  {
    if (ringPts == null) {
      CoordinateList coordList = new CoordinateList();
      for (Iterator i = deList.iterator(); i.hasNext(); ) {
        DirectedEdge de = (DirectedEdge) i.next();
        PolyEdge edge = (PolyEdge) de.getEdge();
        addEdge(edge.getLine().getCoordinates(), de.getEdgeDirection(), coordList);
      }
      ringPts = coordList.toCoordinateArray();
    }
    return ringPts;
  }


  public LineString getLineString()
  {
    getCoordinates();
    return fact.createLineString(ringPts);
  }

  public LinearRing getRing()
  {
    if (ring != null) return ring;
    getCoordinates();
    if (ringPts.length < 3) System.out.println(ringPts);
    try {
      ring = fact.createLinearRing(ringPts);
    }
    catch (Exception ex) {
      System.out.println(ringPts);
    }
    return ring;
  }

  public void addEdge(Coordinate[] coords, boolean isForward, CoordinateList coordList)
  {
    if (isForward) {
      for (int i = 0; i < coords.length; i++) {
        coordList.add(coords[i], false);
      }
    }
    else {
      for (int i = coords.length - 1; i >= 0; i--) {
        coordList.add(coords[i], false);
      }
    }
  }
}
