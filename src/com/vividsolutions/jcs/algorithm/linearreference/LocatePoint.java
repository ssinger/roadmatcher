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

package com.vividsolutions.jcs.algorithm.linearreference;

import com.vividsolutions.jts.geom.*;

/**
 * Provides various ways of computing the actual value
 * of a point a given length along a line.
 */
public class LocatePoint {

  /**
   * Computes the location of a point a given length along a {@link LineSegment}.
   * If the length exceeds the length of the line segment the last
   * point of the segment is returned.
   * If the length is negative the first point
   * of the segment is returned.
   *
   * @param seg the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinate} of the desired point
   */
  public static Coordinate pointAlongSegment(LineSegment seg, double length)
  {
    return pointAlongSegment(seg.p0, seg.p1, length);
  }

  /**
   * Computes the location of a point a given length along a line segment.
   * If the length exceeds the length of the line segment the last
   * point of the segment is returned.
   * If the length is negative the first point
   * of the segment is returned.
   *
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinate} of the desired point
   */
  public static Coordinate pointAlongSegment(Coordinate p0, Coordinate p1, double length)
  {
    double segLen = p1.distance(p0);
    double frac = length / segLen;
    if (frac <= 0.0) return p0;
    if (frac >= 1.0) return p1;

    double x = (p1.x - p0.x) * frac + p0.x;
    double y = (p1.y - p0.y) * frac + p0.y;
    return new Coordinate(x, y);
  }

  /**
   * Computes the location of a point a given length along a line segment.
   * If the length exceeds the length of the line segment the last
   * point of the segment is returned.
   * If the length is negative the first point
   * of the segment is returned.
   *
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinate} of the desired point
   */
  public static Coordinate pointAlongSegmentByFraction(Coordinate p0, Coordinate p1, double frac)
  {
    if (frac <= 0.0) return p0;
    if (frac >= 1.0) return p1;

    double x = (p1.x - p0.x) * frac + p0.x;
    double y = (p1.y - p0.y) * frac + p0.y;
    return new Coordinate(x, y);
  }

  /**
   * Computes the {@link Coordinate} of the point a given length
   * along a {@link LineString}.
   *
   * @param line
   * @param length
   * @return
   */
  public static Coordinate pointAlongLine(LineString line, double length)
  {
    LocatePoint loc = new LocatePoint(line, length);
    return loc.getPoint();
  }

  private Coordinate pt;
  private int index;

  public LocatePoint(LineString line, double length)
  {
    compute(line, length);
  }

  private void compute(LineString line, double length)
  {
    // <TODO> handle negative distances (measure from opposite end of line)
    double totalLength = 0.0;
    Coordinate[] coord = line.getCoordinates();
    for (int i = 0; i < coord.length - 1; i++) {
      Coordinate p0 = coord[i];
      Coordinate p1 = coord[i+1];
      double segLen = p1.distance(p0);
      if (totalLength + segLen > length) {
        pt = pointAlongSegment(p0, p1, length - totalLength);
        index = i;
        return;
      }
      totalLength += segLen;
    }
    // distance is greater than line length
    pt = new Coordinate(coord[coord.length - 1]);
    index = coord.length;
  }

  public Coordinate getPoint()  {    return pt;  }

  /**
   * Returns the index of the segment containing the computed point
   */
  public int getIndex()  {    return index;  }

}
