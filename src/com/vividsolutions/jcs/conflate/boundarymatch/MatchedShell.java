

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

import java.util.*;
import com.vividsolutions.jts.geom.*;


/**
 * The main data structure to represent the outer shell of
 * a polygon that has been matched with one or more other polygons,
 * and computed the adjusted version.
 * It contains the list of matches for the line segments of
 * the edge for the shell.
 * It can also compute adjusted indicators for segments and vertices
 * <p>
 * MatchedShell and its associated classes model the components of the polygon shells
 * in the Subject and Reference datasets.
 * The main objective of the Boundary Matching algorithm is to adjust the Subject
 * to the Reference, while leaving the Reference changed as little as possible.
 * The following are the main design criteris for the Boundary Matching algorithm:
 * <ul>
 * <li>Subject shells can have vertices adjusted (moved) and new vertices inserted.
 * <li>Reference shells can have new vertices inserted
 * (optionally, under user control), but their existing vertices
 * are never changed.  Insert vertices will lie exactly on the existing
 * edges of the shell (up to numerical precision).
 * <li> <b>NOT YET IMPLEMENTED</b>
 * Subject shells may have vertices that do not appear in the bordering
 * Reference shells deleted (optionally under user control).
 * (This option would be chosen if it desired to match the Subject
 * to the Reference exactly without introducing any new vertices
 * in the Reference.)
 * </ul>
 */
public abstract class MatchedShell
{
  protected Coordinate[] pts;
  protected MatchedSegment[] matchedSeg;
  protected MatchedVertex[] matchedVertex;
  protected List adjSectionList;
  protected Coordinate[] adjCoord = null;

  /**
   * Create a matched shell for a given BoundaryFeature
   *
   * @param shell the points for the shell that has been matched
   */
  public MatchedShell(Coordinate[] shell) {
    pts = shell;
    /**
     * Don't bother with duplicate last point - it will be inserted later.
     * It's simpler to not maintain it explicitly during the processing.
     */
    matchedSeg = new MatchedSegment[pts.length - 1];
    matchedVertex = new MatchedVertex[pts.length - 1];
  }

  public Coordinate getCoordinate(int i)
  {
    return pts[i];
  }
  public Coordinate[] getCoordinates()
  {
    return pts;
  }

  public MatchedSegment getMatchedSegment(int i)
  {
    if (matchedSeg[i] == null)
      matchedSeg[i] = new MatchedSegment(this, i);
    return matchedSeg[i];
  }

  public MatchedVertex getMatchedVertex(int i)
  {
    if (matchedVertex[i] == null)
      matchedVertex[i] = new MatchedVertex(this, i);
    return matchedVertex[i];
  }

  public void computeSegmentMatches(MatchedShell refShell, SegmentMatcher sm) {
    for (int i = 0; i < matchedSeg.length; i++) {
      for (int j = 0; j < refShell.matchedSeg.length; j++) {
        if (! isSegmentMatch(i, refShell, j, sm))
          continue;
        MatchedSegment subMLS = getMatchedSegment(i);
        MatchedSegment refMLS = refShell.getMatchedSegment(j);
        subMLS.addMatchedSegment(refMLS, sm.getDistanceTolerance());
      }
    }
  }

  /**
   * Get the adjusted value of a vertex, if the vertex has been adjusted.
   * Otherwise, return <code>null</code>
   * so that callers can tell that the vertex was not adjusted.
   *
   * @param i the index of the vertex
   * @return <code>null</code> if the vertex was not adjusted
   *      the adjusted coordinate, if there is one
   */
  public Coordinate getAdjustedVertex(int i)
  {
    if (matchedVertex[i] == null)
      return null;
    if (! matchedVertex[i].isAdjusted() )
      return null;
    return matchedVertex[i].getAdjusted();
  }

  /**
   * Default behaviour is to do nothing.
   * Only MatchedShellSubjects may have their vertices adjusted,
   * so only they have an
   * implementation for this method.
   */
  public void updateAdjustedVertices()  {  }

  /**
   * Compute adjustments to vertices that are within tolerance of a
   * vertex on another shell.
   * Vertices are only adjusted if they are NOT already adjusted
   * (i.e. by a previous segment-segment match)
   */
  public void computeVertexMatches(MatchedShell refShell, double distanceTolerance)
  {
    // try matching to vertices first
    for (int i = 0; i < pts.length - 1; i++) {
      for (int j = 0; j < refShell.pts.length - 1; j++) {
        checkVertexVertexMatch(i, refShell, j, distanceTolerance);
      }
    }
    // now try matching vertices to close segments (if no match was previously found)
    for (int i = 0; i < pts.length - 1; i++) {
      for (int j = 0; j < refShell.pts.length - 1; j++) {
        checkVertexSegmentMatch(i, refShell, j, distanceTolerance);
      }
    }
  }

  private void checkVertexVertexMatch(
      int i,
      MatchedShell refShell, int refi,
      double distanceTolerance)
  {
    // don't adjust if already adjusted
    if (matchedVertex[i] != null && matchedVertex[i].isAdjusted() )
      return;

    // check match with vertex and snap vertex to it if close
    double distPt = pts[i].distance(refShell.pts[refi]);
    if (distPt < distanceTolerance ) {
      MatchedVertex v = getMatchedVertex(i);
      v.setAdjusted(refShell.pts[refi]);
    }
  }

  private void checkVertexSegmentMatch(
        int i,
        MatchedShell refShell, int refi,
        double distanceTolerance)
  {
    // don't adjust if already adjusted
    if (matchedVertex[i] != null && matchedVertex[i].isAdjusted() )
      return;
    // check match with segment and project vertex to it if close
    LineSegment seg = new LineSegment(refShell.pts[refi], refShell.pts[refi + 1]);
    double distSeg = seg.distance(pts[i]);
    if (distSeg < distanceTolerance) {
      MatchedVertex v = getMatchedVertex(i);
      Coordinate adjCoord = seg.project(pts[i]);
      v.setAdjusted(adjCoord);
    }
  }

  private boolean isSegmentMatch(int iSub, MatchedShell refShell,
                                 int iRef, SegmentMatcher sm)
  {
    return sm.isMatch(
          getCoordinate(iSub),
          getCoordinate(iSub + 1),
          refShell.getCoordinate(iRef),
          refShell.getCoordinate(iRef + 1) );
  }

  public Coordinate[] getAdjusted()
  {
    // assumes computeAdjusted() has been called
    return adjCoord;
  }

  public boolean isAdjusted()
  {
    // assumes computeAdjusted() has been called
    return ! CoordinateArrays.equals(adjCoord, pts);
  }

  /**
   *
   * @param i the index of the segment to test
   * @return true if the segment has been adjusted
   */
  public boolean isAdjustedSegment(int i)
  {
    if (i < matchedVertex.length
        && matchedVertex[i] != null && matchedVertex[i].isAdjusted())
      return true;
    if (i + 1 < matchedVertex.length
        && matchedVertex[i + 1] != null && matchedVertex[i + 1].isAdjusted())
      return true;
    if (matchedSeg[i] != null && matchedSeg[i].isAdjusted())
      return true;
    return false;
  }

  /**
   * Computes the adjusted edge indicators.
   *
   * @return a list of 2-point Coordinate arrays representing the adjusted edges
   */
  public abstract List computeAdjustedEdgeIndicators();

  /**
   * Computes the adjusted vertex indicators.
   * Only MatchedShellSubjects implement this method, since only they adjust
   * their vertices
   *
   * @return a list of 2-point Coordinate arrays defining the adjustment vectors
   */
  public List computeAdjustedVertexIndicators()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Computes the new shell reflecting any adjustments made to
   * segments and vertices.
   * Updates the value of adjCoord with the new list of points for the shell.
   */
  public abstract void computeAdjustedShell();
}
