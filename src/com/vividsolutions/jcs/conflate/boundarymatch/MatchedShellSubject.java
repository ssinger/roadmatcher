

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
 * A MatchedShell for features in the Subject dataset.
 * Contains specific algorithms to compute the adjusted coordinates
 * for a shell in a Subject dataset.
 * Subject features may have both vertices added
 * and existing vertices
 * moved (to snap them to Reference vertices or lines).
 */
public class MatchedShellSubject
  extends MatchedShell
{

  /**
   * Create a vector that can be used to visually indicate the adjustment of a vertex.
   * The vector is represented as a Coordinate array containing two elements.
   * These can be converted to Geometry's
   * using the {@link CoordinateArrays#fromCoordinateArrays} method.
   *
   * @param origPt the original location of the vertex
   * @param adjustPt the location of the adjusted vertex
   * @return a 2-point Coordinate array representing the adjustment vector
   */
  public static Coordinate[] createAdjustedVertexIndicator(Coordinate origPt, Coordinate adjustPt)
  {
    Coordinate[] line = new Coordinate[2];
    line[0] = origPt;
    line[1] = adjustPt;
    return line;
  }


  public MatchedShellSubject(Coordinate[] shell)
  {
    super(shell);
  }

  /**
   * For all vertices that were adjusted by the segment matching,
   * record the adjustments in the corresponding MatchedVertex.
   */
  public void updateAdjustedVertices()
  {
    for (int i = 0; i < matchedSeg.length; i++) {
      MatchedSegment seg = matchedSeg[i];
      if (seg != null && seg.isAdjustable()) {
        seg.computeAdjusted();
        if (seg.isEndPointAdjusted(0))
          getMatchedVertex(i).setAdjusted(seg.getAdjustedEndPoint(0));
        // do we need this?
        if (i + 1 < matchedSeg.length && seg.isEndPointAdjusted(1))
          getMatchedVertex(i + 1).setAdjusted(seg.getAdjustedEndPoint(1));
      }
    }
  }

  /**
   * Computes the adjusted edges for a subject feature.
   *
   * @return a list of 2-point Coordinate arrays representing the adjusted edges
   */
  public List computeAdjustedEdgeIndicators()
  {
    List adjEdges = new ArrayList();

    for (int i = 0; i < matchedVertex.length; i++) {
      if (isAdjustedSegment(i)) {
        CoordinateList adjCoordList = new CoordinateList();

        // add first vertex of adjusted segment
        Coordinate segStartPt = null;
        if (matchedVertex[i] != null && matchedVertex[i].isAdjusted())
          segStartPt = matchedVertex[i].getAdjusted();
        else
          segStartPt = pts[i];
        adjCoordList.add(segStartPt);

        // add any inserted vertices
        if (matchedSeg[i] != null) {
          matchedSeg[i].addInsertedSubjectVertices(adjCoordList);
        }

        // add last vertex of adjusted segment
        int lasti = i + 1;
        if (lasti >= matchedVertex.length) lasti = 0;
        Coordinate segEndPt = null;
        if (matchedVertex[lasti] != null && matchedVertex[lasti].isAdjusted())
          segEndPt = matchedVertex[lasti].getAdjusted();
        else
          segEndPt = pts[lasti];
        adjCoordList.add(segEndPt);

        adjEdges.add(adjCoordList.toCoordinateArray());
      }
    }
    return adjEdges;
  }

  /**
   * Computes the adjusted vertex indicators.
   * Only MatchedShellSubjects implement this method, since only they contain
   * adjusted vertices.
   *
   * @return a list of 2-point Coordinate arrays defining the adjustment vectors
   */
  public List computeAdjustedVertexIndicators()
  {
    List adjIndList = new ArrayList();

    for (int i = 0; i < matchedVertex.length; i++) {
      if (matchedVertex[i] != null && matchedVertex[i].isAdjusted()) {
        Coordinate[] line = createAdjustedVertexIndicator(
            pts[i],
            matchedVertex[i].getAdjusted());
        adjIndList.add(line);
      }
      // add indicators for any inserted vertices
      if (matchedSeg[i] != null) {
        matchedSeg[i].addAdjustedVertexIndicators(adjIndList);
      }
    }
    return adjIndList;

  }

  /**
   * Computes a new shell reflecting any adjustments made to
   * segments and vertices.
   * Adjusts (moves) any existing vertices that were adjusted.
   * Also insert any new vertices that were added to segments.
   */
  public void computeAdjustedShell()
  {
    if (adjCoord != null) return;

    CoordinateList adjCoordList = new CoordinateList();
    for (int i = 0; i < matchedVertex.length; i++) {
      // add original or adjusted vertex
      if (matchedVertex[i] != null && matchedVertex[i].isAdjusted())
        adjCoordList.add(matchedVertex[i].getAdjusted());
      else
        adjCoordList.add(pts[i]);

      // add any inserted vertices
      if (matchedSeg[i] != null) {
        matchedSeg[i].addInsertedSubjectVertices(adjCoordList);
      }
    }
    // make sure ring is closed
    adjCoordList.closeRing();
    adjCoord = adjCoordList.toCoordinateArray();
  }

}
