

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

import com.vividsolutions.jts.geom.Coordinate;
/**
 * A vertex of a shell which is within tolerance
 * of another vertex.
 * It contains an adjusted value,
 * which is usually equal to the
 * equal to the value of the matched vertex.
 * If the vertex matches more than one other vertex, the closest one is chosen.
 */
public class MatchedVertex {

  private MatchedShell matchedShell;
  private int index;  // the index of the vertex in the shell
  private Coordinate adjCoord = null;
  private double adjustDistance = Double.MAX_VALUE;  // set to large value initially
  //private boolean isDeleted = false;  // will be used when we add ability to remove subject vertices

  public MatchedVertex(MatchedShell matchedShell, int index)
  {
    this.matchedShell = matchedShell;
    this.index = index;
  }

  public Coordinate getCoordinate() { return matchedShell.getCoordinate(index); }
  public Coordinate getAdjusted() { return adjCoord; }

  /**
   * Set the adjusted value to the <code>coord</code>,
   * but only if this coordinate is closer than the current adjusted value.
   * @param coord
   */
  public void setAdjusted(Coordinate coord)
  {
    double dist = coord.distance(getCoordinate());
    if (dist < adjustDistance) {
      adjustDistance = dist;
      adjCoord = coord;
    }
  }

  public boolean isAdjusted()
  {
      return adjCoord != null
                       && ! adjCoord.equals(matchedShell.getCoordinate(index));
  }

}
