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
package com.vividsolutions.jcs.qa.offsetcorner;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.geom.LineSegmentUtil;
import com.vividsolutions.jcs.conflate.boundarymatch.SegmentMatcher;
import com.vividsolutions.jump.geom.Angle;

/**
 * Models a polygon shell on the boundary of a dataset.
 * Allows extremal boundary edge points to be computed.
 */
public class BoundaryShell
{
  /**
   * Computes the distance from a point to a ring.
   * @param pt the point to compute the distance to
   * @param ring the ring to compute the distance to
   * @return the minimum distance between the point and the ring
   */
  public static boolean isWithinDistance(Coordinate pt, Coordinate[] ring, double distance)
  {
    LineSegment seg = new LineSegment();
    for (int i = 0; i < ring.length - 1; i++) {
      seg.p0 = ring[i];
      seg.p1 = ring[i + 1];
      double ptDist = seg.distance(pt);
      if (ptDist < distance)
        return true;
    }
    return false;
  }

  /**
   * Computes the interior angles for each vertex of a ring.
   * Interior angles range between 0 and 2*PI.
   * @param ring an array of Coordinates
   * @return an array of the interior angles at each vertex of the ring
   */
  private static double[] interiorAnglesRing(Coordinate[] ring)
  {
    double[] angle = new double[ring.length];

    for (int i = 0; i < ring.length - 1; i++) {
      int previ = i - 1;
      if (previ < 0) previ = ring.length - 1;
      int nexti = i + 1;
      if (nexti >= ring.length) nexti = 0;
      angle[i] = Angle.interiorAngle(ring[previ], ring[i], ring[nexti]);
    }
    // last point is same as first point, so angle is the same
    angle[angle.length - 1] = angle[0];
    return angle;
  }

  //public static final double MAX_CORNER_ANGLE = Angle.toRadians(140.0);

  private static final GeometryFactory geomFactory = new GeometryFactory();

  private Polygon polygon;
  private Coordinate[] pts;
  private boolean[] isOnBoundary;
  private boolean[] isCorner;

  public BoundaryShell(Polygon polygon)
  {
    this.polygon = polygon;
    this.pts = polygon.getExteriorRing().getCoordinates();
    isOnBoundary = new boolean[pts.length];
  }

  public Coordinate[] getCoordinates() { return pts; }

  public Envelope getEnvelope() { return polygon.getEnvelopeInternal(); }

  public void checkBoundary(BoundaryShell shell, double distanceTolerance)
  {
    // if the shells are far apart there is no information about boundary points
    if (getEnvelope().distance(shell.getEnvelope()) > distanceTolerance)
        return;

    // check if any points are on the boundary by virtue of being close to this shell
    for (int i = 0; i < pts.length; i++) {
      if (isWithinDistance(pts[i], shell.pts, distanceTolerance))
        isOnBoundary[i] = true;
    }
  }

  /**
   * Compute corners defined by <code>maxCornerAngle</code>.
   *
   * @param maxCornerAngle maximum angle which is considered to form a corner (in degrees)
   */
  public void computeCorners(double maxCornerAngle)
  {
    double maxCornerAngleRad = Angle.toRadians(maxCornerAngle);
    double[]  interiorAngle = interiorAnglesRing(pts);
    isCorner = new boolean[interiorAngle.length];
    for (int i = 0; i < interiorAngle.length; i++) {
      isCorner[i] = interiorAngle[i] < maxCornerAngleRad;
    }
  }

  /**
   * Implements circular index for pts array
   * @param i any integer
   * @return a valid index for the array
   */
  private int getSegmentIndex(int i)
  {
    if (i < 0) return pts.length - 1;
    if (i > pts.length - 1) return 0;
    return i;
  }

  private boolean isExtremalBoundaryPoint(int i)
  {
    boolean isExtremal = ! isOnBoundary[getSegmentIndex(i - 1)]
                       || ! isOnBoundary[getSegmentIndex(i + 1)];
    boolean isExtremalBoundary = isOnBoundary[i] && isExtremal;
    return isExtremalBoundary;
  }

  public Collection getExtremalBoundaryCoordinates()
  {
    List extremalPts = new ArrayList();
    for (int i = 0; i < pts.length - 1; i++) {
      if (isExtremalBoundaryPoint(i)) {
        extremalPts.add(pts[i]);
      }
    }
    return extremalPts;
  }

  public Collection getBoundaryVertices()
  {
    List boundaryPts = new ArrayList();
    for (int i = 0; i < pts.length - 1; i++) {
      if (isOnBoundary[i]) {
        boundaryPts.add(pts[i]);
      }
    }
    return boundaryPts;
  }
  public Collection getBoundaryCornerVertices()
  {
    List cornerPts = new ArrayList();
    for (int i = 0; i < pts.length - 1; i++) {
      if (isCorner[i] && isOnBoundary[i]) {
        cornerPts.add(pts[i]);
//System.out.println(pts[i]);
      }
    }
    return cornerPts;
  }
  public Collection getCornerVertices()
  {
    List cornerPts = new ArrayList();
    for (int i = 0; i < pts.length - 1; i++) {
      if (isCorner[i]) {
        cornerPts.add(pts[i]);
      }
    }
    return cornerPts;
  }
}
