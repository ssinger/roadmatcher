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

package com.vividsolutions.jcs.algorithm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.linearreference.LocatePoint;

/**
 * Computes the "average" distance between two {@link LineString}s,
 * based on the distance between each vertex and a point the same distance
 * along the other line.
 */
public class AverageLineDistance {

  public static GeometryFactory factory = new GeometryFactory();

  public int[] closestPairIndex(Coordinate[] pts1, Coordinate[] pts2)
  {
    double minDistance = Double.MAX_VALUE;
    int[] result = new int[2];
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        double dist = pts1[i].distance(pts2[j]);
        if (dist < minDistance) {
          result[0] = i;
          result[1] = j;
          minDistance = dist;
        }
      }
    }
    return result;
  }

  public static LineString reverse(LineString line)
  {
    LineString revLine = (LineString) line.clone();
    Coordinate[] pts = revLine.getCoordinates();
    CoordinateArrays.reverse(pts);
    return revLine;
  }

  private double avgLineDistance;

  public AverageLineDistance(LineString line0, LineString line1)
  {
    avgLineDistance = compute(line0, line1);
  }

  public double getDistance()  {    return avgLineDistance;  }

  private double compute(LineString line0, LineString line1)
  {
    LineString[] lines = normalize(line0, line1);
    double distance = averageIntervalDistance(lines, 1.0);
    return distance;
  }

  private double averageIntervalDistance(LineString[] lines, double intervalLen)
  {
    double len0 = lines[0].getLength();
    double len1 = lines[1].getLength();
    double currLen = 0.0;
    double totalDistance = 0.0;
    int intervalCount = 0;
    while (currLen < len0 && currLen < len1) {
      LocatePoint locate0 = new LocatePoint(lines[0], currLen);
      Coordinate p0 = locate0.getPoint();
      LocatePoint locate1 = new LocatePoint(lines[1], currLen);
      Coordinate p1 = locate1.getPoint();
      double intervalDistance = p0.distance(p1);

      totalDistance += intervalDistance;
      currLen += intervalLen;
      intervalCount++;
    }
    return totalDistance / intervalCount;
  }

  /**
   * Normalizes linestrings so that they each begin with the endpoints which are
   * closest.
   * @param line0 an input LineString
   * @param line1 an input LineString
   * @return a pair of normalized LineStrings
   */
  private LineString[] normalize(LineString line0, LineString line1)
  {
    LineString[] lines = new LineString[2];
    Coordinate[] edge0Pts = line0.getCoordinates();
    Coordinate[] edge1Pts = line1.getCoordinates();

    // find closest endpoint pair
    int[] closestPairIndex = closestPairIndex(
        new Coordinate[] { edge0Pts[0], edge0Pts[edge0Pts.length - 1] },
        new Coordinate[] { edge1Pts[0], edge1Pts[edge1Pts.length - 1] });

    lines[0] = line0;
    if (closestPairIndex[0] != 0) lines[0] = reverse(line0);
    lines[1] = line1;
    if (closestPairIndex[1] != 0) lines[1] = reverse(line1);

    return lines;

  }
}
