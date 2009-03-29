

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

/**
 * Computes the projection of a target segment onto a reference
 * segment.  It will snap the computed projection to the endpoints of the reference
 * segment if they are with the tolerance distance.
 */
public class SegmentProjecter
{
  /**
   * "Snap" a coordinate to one or other end of a segment
   * if it is within the <code>tolerance</code>.
   * The coordinate is snapped to the closest endpoint.
   */
  public static Coordinate snap(Coordinate coord,
                            LineSegment seg,
                            double tolerance)
  {
    Coordinate p0 = seg.getCoordinate(0);
    double dist0 = p0.distance(coord);
    Coordinate p1 = seg.getCoordinate(1);
    double dist1 = p1.distance(coord);

    double minDist = dist0;
    Coordinate minCoord = p0;
    if (dist1 < minDist) {
      minDist = dist1;
      minCoord = p1;
    }

    if (minDist < tolerance) {
      return minCoord;
    }
    /**
     * If no snapping occurred, the result coordinate is
     * just the original coord
     */
    return coord;
  }
  private double distanceTolerance;
  // local data for a projected segment
  private boolean[] isEndPointImage = new boolean[2];
  private Coordinate[] projCoord = new Coordinate[2];

  /**
   * Creates a new segment which is the snapped projection of the seg onto the ref segment.
   *
   * @param seg the target seg to be projected
   * @param ref the reference segment to project onto
   * @param distanceTolerance the snapping tolerance
   */
  public SegmentProjecter(LineSegment seg, LineSegment ref, double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
    computeProjection(seg, ref);
  }

  /**
   * The <code>index</code>'th coordinate of the resulting projected segment
   * @param index 0 or 1
   * @return a Coordinate
   */
  public Coordinate getCoordinate(int index) { return projCoord[index]; }

  /**
   * Determines whether the <code>index</code>'th endpoint of the projected segment is
   * the image of an endpoint of the parent segment
   *
   * @param index 0 or 1
   * @return <code>true</code> if the endpoint is the image of a parent endpoint
   */
  public boolean isEndPointImage(int index) { return isEndPointImage[index]; }

  /**
   * Computes the projection of the segment seg onto the Reference segment reg
   */
  private void computeProjection(LineSegment seg, LineSegment ref)
  {
    projCoord[0] = computeProjectedCoord(0, seg, ref);
    projCoord[1] = computeProjectedCoord(1, seg, ref);
  }

  private Coordinate computeProjectedCoord(
                                  int index,
                                  LineSegment seg,
                                  LineSegment ref)
  {
    Coordinate coord = seg.getCoordinate(index);

    // snap the coordinate to Ref seg before projecting
    Coordinate snappedCoord = snap(coord, ref, distanceTolerance);

    double factor = ref.projectionFactor(snappedCoord);

    /**
     * The coordinate argument is an endpoint of the parent segment.
     * If the projected coordinate is inside Reference segment,
     * that means that the this endpoint of the projected segment
     * is the image of an endpoint of the parent segment.
     * (Otherwise, the endpoint of the projected segment is the image
     * of some point internal to the parent segment).
     */
    isEndPointImage[index] = false;
    if (factor >= 0.0 && factor <= 1.0)
      isEndPointImage[index] = true;

    Coordinate projCoord = null;
    /**
     * If the projection of the coord lies outside the ends of the ref segment,
     * use the appropriate endpoint as the actual projected coord.
     * Otherwise, use the projection itself.
     */
    if (factor <= 0.0)
      projCoord = ref.getCoordinate(0);
    else if (factor >= 1.0)
      projCoord = ref.getCoordinate(1);
    else
      projCoord = ref.project(snappedCoord);

//Debug.println(coord.distance(projCoord));
    return projCoord;
  }

}
