

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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.boundarymatch.SegmentMatcher;

/**
 * Models the shell of a polygon which can be matched to other shells
 * and adjusted to contain new vertices.
 */
public class Shell
    extends GeometryComponent
{

  Segment[] seg;
  private Coordinate[] initCoord;
  private Coordinate[] adjustedCoord;

  public Shell()
  {

  }

  public void initialize(LinearRing ring, VertexMap vmap)
  {
    Coordinate[] initCoord = ring.getCoordinates();
    // remove any duplicate points, so they don't muck things up
    CoordinateList coordList = new CoordinateList(initCoord, false);

    // create segments
    seg = new Segment[coordList.size() - 1];
    for (int i = 0; i < coordList.size() - 1; i++)
    {
      Vertex v0 = vmap.get(coordList.getCoordinate(i));
      Vertex v1 = vmap.get(coordList.getCoordinate(i + 1));
      seg[i] = new Segment(v0, v1, this);
      v0.addShell(this);
      v1.addShell(this);
    }
  }

  public void match(Shell shell, SegmentMatcher segMatcher)
  {
    // this method might cause the coordinates to change, so make sure they are recomputed
    adjustedCoord = null;
    // this is O(n^2), which can be a problem for large polygons
    for (int i = 0; i < seg.length; i++) {
      for (int j = 0; j < shell.seg.length; j++) {
        /**
         * Inefficient - we already know which segments match
         * Also, could this be done symmetrically?
         * eg the segment added to both segments at the same time?
         */
        LineSegment segi = seg[i].getLineSegment();
        LineSegment segj = shell.seg[j].getLineSegment();
        // heuristic to speed up match checking
        if (segi.distance(segj) > 2.0 * segMatcher.getDistanceTolerance())
          continue;
        boolean isMatch = segMatcher.isMatch(segi, segj);
        boolean isTopoEqual = segi.equalsTopo(segj);
        if (isMatch && ! isTopoEqual)
            seg[i].addMatchedSegment(shell.seg[j], segMatcher.getDistanceTolerance());
      }
    }
  }

  public boolean isAdjusted()
  {
    computeAdjusted();
    boolean isAdjusted = ! CoordinateArrays.equals(initCoord, adjustedCoord);
    return isAdjusted;
  }

  public Coordinate[] getAdjusted()
  {
    computeAdjusted();
    return adjustedCoord;
  }
  private void computeAdjusted()
  {
    if (adjustedCoord != null) return;

    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < seg.length; i++) {
      seg[i].getVertex(0);
      coordList.add(seg[i].getVertex(0).getCoordinate(), false);
      coordList.addAll(seg[i].getInsertedCoordinates(), false);
    }
    coordList.closeRing();

    CoordinateList noRepeatCoordList = removeRepeatedSegments(coordList);

    adjustedCoord = noRepeatCoordList.toCoordinateArray();
  }

  /**
   * Remove any repeated segments
   * (e.g. a pattern of Coordinates of the form "a-b-a" is converted to "a" )
   *
   * @param coordList
   */
  private CoordinateList removeRepeatedSegments(CoordinateList coordList)
  {
    CoordinateList noRepeatCoordList = new CoordinateList();
    for (int i = 0; i < coordList.size() - 1; i++) {
      Coordinate a = coordList.getCoordinate(i);
      noRepeatCoordList.add(a, false);

      // check for a-b-a pattern
      Coordinate b = coordList.getCoordinate(i + 1);
      int nexti = i + 2;
      if (nexti >= coordList.size()) nexti = 1;
      Coordinate a2 = coordList.getCoordinate(nexti);
      // if a = a2 we have found a-b-a pattern, so skip b
      if (a.equals(a2)) {
        i++;
      }
    }
    noRepeatCoordList.closeRing();
    return noRepeatCoordList;
  }

  public boolean isConflict()
  {
    for (int i = 0; i < seg.length; i++) {
      if (seg[i].isConflict()) return true;
      if (seg[i].getVertex(0).isConflict()) return true;
      if (seg[i].getVertex(1).isConflict()) return true;
    }
    return false;
  }
}
