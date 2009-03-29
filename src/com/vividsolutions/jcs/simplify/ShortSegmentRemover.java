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
package com.vividsolutions.jcs.simplify;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LineSegment;

public class ShortSegmentRemover
{
  private double minLength;
  private double maxDisplacement;
  private Coordinate[] pts;
  private boolean isRing = false;
  private Coordinate[] newPts = null;
  private int segmentsRemovedCount = 0;

  public ShortSegmentRemover(Coordinate[] pts, boolean isRing, double minLength, double maxDisplacement)
  {
    this.pts = pts;
    this.isRing = isRing;
    this.minLength = minLength;
    this.maxDisplacement = maxDisplacement;
    removeShortSegments();
  }

  public boolean isModified() { return newPts != null; }

  public int getSegmentsRemovedCount() { return segmentsRemovedCount; }

  public Coordinate[] getUpdatedCoordinates()
  {
    if (newPts == null) return pts;
    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < newPts.length; i++) {
      if (newPts[i] != null)
        coordList.add(newPts[i]);
    }
    if (isRing) coordList.closeRing();
    return coordList.toCoordinateArray();
  }

  private void removeShortSegments()
  {
    Coordinate[] newPts = (Coordinate[]) pts.clone();
    for (int i = 0; i < pts.length - 1; i++) {
      if (pts[i].distance(pts[i + 1]) < minLength) {
        int indexToRemove = computeIndexToRemove(i);
        if (indexToRemove >= 0)
          removeCoordinate(indexToRemove);
      }
    }
  }

  private void removeCoordinate(int i)
  {
    segmentsRemovedCount++;
    if (newPts == null)
      newPts = (Coordinate[]) pts.clone();
    // need to check for ring and endpoint here
    newPts[i] = null;
    if (isRing && (i == 0 || i == pts.length - 1)) {
      newPts[pts.length - 1 - i] = null;
    }
  }

  private Coordinate getPt(int i)
  {
    if (isRing && i < 0) return pts[pts.length - 2];
    if (isRing && i > pts.length - 2) return pts[0];
    // suggested bug fix
    //    if (isRing && i == pts.length) return pts[1];
    //    if (isRing && i == pts.length - 1) return pts[0];
    /**
     *  this will delibarately cause an error
     * if the coordinates are not a ring and the index is out of range.
     * This indicates an algorithm problem
     */
    return pts[i];
  }

  /**
   * Removes the endpoint causing least displacement from a short segment, as long as
   * the resulting segment has a displacement within tolerance.
   * @param i
   * @return index of coordinate to remove, if any (-1 indicates none)
   */
  private int computeIndexToRemove(int i)
  {
    double prevDisplacement = Double.MAX_VALUE;
    double nextDisplacement = Double.MAX_VALUE;

    // check if short segment is at end of list - if it is the vertex to remove is constrained
    if (isRing || i >= 1)
      prevDisplacement = displacement(getPt(i - 1), getPt(i + 1), getPt(i));
    if (isRing || i < pts.length - 2)
      nextDisplacement = displacement(getPt(i), getPt(i + 2), getPt(i + 1));

    int indexToRemove = i;
    double displacement = prevDisplacement;
    if (nextDisplacement < displacement) {
      indexToRemove = i + 1;
      displacement = nextDisplacement;
    }

    // too much displacement - don't remove
    if (displacement > maxDisplacement) return -1;

    return indexToRemove;
  }

  private static LineSegment displacementSegment = new LineSegment();

  private static double displacement(Coordinate p0, Coordinate p1, Coordinate p)
  {
    displacementSegment.p0 = p0;
    displacementSegment.p1 = p1;
    return displacementSegment.distance(p);
  }
}
