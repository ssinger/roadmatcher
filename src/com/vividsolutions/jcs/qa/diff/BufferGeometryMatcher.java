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
package com.vividsolutions.jcs.qa.diff;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.geom.BufferDistancePerturber;
import com.vividsolutions.jcs.precision.*;
/**
 * Matches geometries based on whether each Geometry is contained in the
 * other's buffer.  This is equivalent to each geometry being entirely
 * within the distance tolerance of the other.
 */
public class BufferGeometryMatcher
    implements DiffGeometryMatcher
{
  // the percentage of the buffer distance to use as the error tolerance for perturbation
  public static final double ERROR_TOLERANCE = .1;

  /**
   * if true the buffers of the boundary will be matched as well as the buffers of the
   * geometries themselves.  Matching the boundaries as well is
   * a more accurate matching algorithm.
   */
  private static final boolean checkBoundary = false;

  public static double maxOrthogonalDistance(Envelope env1, Envelope env2)
  {
    double deltaMinX = Math.abs(env1.getMinX() - env2.getMinX());
    double maxDist = deltaMinX;
    double deltaMaxX = Math.abs(env1.getMaxX() - env2.getMaxX());
    if (deltaMaxX > maxDist) maxDist = deltaMaxX;
    double deltaMinY = Math.abs(env1.getMinY() - env2.getMinY());
    if (deltaMinY > maxDist) maxDist = deltaMinY;
    double deltaMaxY = Math.abs(env1.getMaxY() - env2.getMaxY());
    if (deltaMaxY > maxDist) maxDist = deltaMaxY;

    return maxDist;
  }

  private double tolerance;
  private Geometry queryGeom;
  private Geometry queryBuffer;
  private Geometry queryBoundary = null;
  private Geometry queryBoundaryBuffer = null;

  private BufferDistancePerturber bufferDP;

  public BufferGeometryMatcher(double tolerance)
  {
    this.tolerance = tolerance;
    bufferDP = new BufferDistancePerturber(tolerance, tolerance * ERROR_TOLERANCE);
  }
  public void setQueryGeometry(Geometry geom)
  {
    queryGeom = geom;
    queryBuffer = checkedBuffer(geom);

    if (checkBoundary) {
      if (queryGeom.getDimension() == 2) {
        queryBoundary = queryGeom.getBoundary();
        queryBoundaryBuffer = getBoundaryBuffer(queryGeom);
      }
    }
  }

  public Geometry getQueryGeometry()
  {
    return queryBuffer;
  }
  public boolean isMatch(Geometry geom)
  {
    if (geom.getClass() != queryGeom.getClass()) return false;
    if (! isEnvelopeMatch(geom)) return false;

    boolean buffersMatch = isBufferMatch(geom);
    if (! buffersMatch) return false;

    if (! checkBoundary)
      return true;
    else {
      // for non-area geometries this is all we need to check
      if (queryGeom.getDimension() < 2)
        return true;

      // for area geometries, check that the linework matches as well
      return isBoundaryBufferMatch(geom);
    }
  }

  private boolean isBufferMatch(Geometry geom)
  {
    Geometry buf = checkedBuffer(geom);

    boolean queryContains = queryBuffer.contains(geom);
    boolean queryIsContained = buf.contains(queryGeom);
    return queryContains && queryIsContained;
  }

  private boolean isBoundaryBufferMatch(Geometry geom)
  {
    Geometry boundary = geom.getBoundary();
    Geometry bndBuf = getBoundaryBuffer(geom);

    boolean queryContains = queryBoundaryBuffer.contains(boundary);
    boolean queryIsContained = bndBuf.contains(queryBoundary);
    return queryContains && queryIsContained;
  }

  private Geometry checkedBuffer(Geometry geom)
  {
    Geometry buf = null;
    try {
      buf = geom.buffer(tolerance);
      //buf = bufferDP.buffer(geom);
    }
    catch (RuntimeException ex) {
      // hack to get around buffer robustness problems
      System.out.println("Buffer error!");
      System.out.println(geom);
      buf = geom;
    }
    return buf;
  }


  private Geometry getBoundaryBuffer(Geometry geom)
  {
    // clone boundary, so we can precision-reduce it in place
    Geometry boundary = (Geometry) geom.getBoundary().clone();
    double scaleFactor = 1 / (tolerance * ERROR_TOLERANCE);
    GeometryPrecisionReducer pr = new GeometryPrecisionReducer(
        new NumberPrecisionReducer(scaleFactor));
    pr.reduce(boundary);
    Geometry buf = checkedBuffer(boundary);
    if (buf.isEmpty()) {
      System.out.println("Empty boundary buffer found");
      System.out.println(geom);
      System.out.println(boundary);
    }
    return buf;
  }

  private boolean isEnvelopeMatch(Geometry geom)
  {
    // first check envelopes - if they are too far apart the geometries do not match
    double envDist = maxOrthogonalDistance(queryGeom.getEnvelopeInternal(), geom.getEnvelopeInternal());
    return envDist <= tolerance;
  }
}
