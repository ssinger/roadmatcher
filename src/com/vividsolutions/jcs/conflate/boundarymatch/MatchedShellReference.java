

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

import com.vividsolutions.jump.geom.CoordinateList;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jts.geom.*;
import java.util.*;

/**
 *  A {@link MatchedShell} for a feature in a Reference dataset.
 *  Contains specific algorithms to compute the adjusted coordinates
 *  for a shell in a Reference dataset.
 * The primary distinction between adjusting a Reference feature and
 * a Subject feature is that Reference vertices are never changed.
 * The only adjustment performed is to add vertices to existing line segments.
 */
public class MatchedShellReference
  extends MatchedShell
{
  private List adjEdges = new ArrayList();

  public MatchedShellReference(Coordinate[] shell)
  {
    super(shell);
  }

  /**
   * Computes the adjusted edges for a feature.
   *
   * @return a list of 2-point Coordinate arrays representing the adjusted edges
   */
  public List computeAdjustedEdgeIndicators()
  {
    return adjEdges;
  }

  /**
   * Computes the new shell reflecting any adjustments made.
   * For Reference features, new vertices may be added
   * to match vertices in the matching Subject shell(s).
   */
  public void computeAdjustedShell()
  {
    // create a new list of coordinates, and add all the shell segments to it
    CoordinateList adjCoordList = new CoordinateList();
    for (int i = 0; i < matchedSeg.length; i++) {
      // add a segment that was unchanged
      if (matchedSeg[i] == null) {
        adjCoordList.add(getCoordinate(i));
      }
      else {
        List addedVert = matchedSeg[i].computeReferenceInsertedVertices();

        // add segment, with any added vertices
        adjCoordList.add(getCoordinate(i));

        for (Iterator it = addedVert.iterator(); it.hasNext(); ) {
          Vertex v = (Vertex) it.next();
          adjCoordList.add(v.getCoordinate());
        }
        // if this segment has added vertices, add an adjustedEdge for it
        if (addedVert.size() > 0)
          addAdjustedEdge(i, addedVert);
        }
    }
    adjCoordList.closeRing();
    adjCoord = adjCoordList.toCoordinateArray();
  }

  private void addAdjustedEdge(int i, List addedVert)
  {
    Coordinate[] coords = new Coordinate[2 + addedVert.size()];
    int j = 0;
    coords[j++] = getCoordinate(i);
    for (Iterator it = addedVert.iterator(); it.hasNext(); ) {
      Vertex v = (Vertex) it.next();
      coords[j++] = v.getCoordinate();
    }
    coords[j++] = getCoordinate(i + 1);
    adjEdges.add(coords);
  }


}
