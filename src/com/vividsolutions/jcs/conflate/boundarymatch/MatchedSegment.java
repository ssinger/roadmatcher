

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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.geom.*;

import com.vividsolutions.jts.geom.LineSegment;

/**
 * Contains the information about the linesegments (or subsegments)
 * in other {@link Feature}s which
 * match a given {@link LineSegment}.
 * If one of the matching segments is identical, the <code>hasExactMatch</code>
 * flag is set.
 */
public class MatchedSegment
{
  private MatchedShell matchedShell;
  private int index;
  private LineSegment lineseg;  // the LineSegment for this segment
  private List matchedSubsegs = new ArrayList();
  private boolean isMatchesSorted = false;
  private boolean hasExactMatch = false;
  private List adjustedVertices = new ArrayList();

  public MatchedSegment(MatchedShell matchedShell, int index)
  {
    this.matchedShell = matchedShell;
    this.index = index;
    Coordinate p0 = matchedShell.getCoordinate(index);
    Coordinate p1 = matchedShell.getCoordinate(index + 1);
    lineseg = new LineSegment(p0, p1);
  }

  public LineSegment getSegment() { return lineseg; }
  public MatchedShell getEdge() { return matchedShell; }

  /**
   * Adds a segment which matches part or all of this segment.
   *
   * @param matchSeg the segment which matches this segment
   * @param distanceTolerance the tolerance value for the match
   */
  public void addMatchedSegment(MatchedSegment matchSeg, double distanceTolerance)
  {
    MatchedSubsegment ss = new MatchedSubsegment(lineseg, matchSeg, distanceTolerance);
    matchedSubsegs.add(ss);
    /**
     * Attach subsegs to the matched segment too.
     * This info will be used later to adjust the matched seg
     */
    matchSeg.matchedSubsegs.add(ss);

    if (lineseg.equalsTopo(matchSeg.lineseg)) {
        hasExactMatch = true;
        matchSeg.hasExactMatch = true;
    }
    /* DEBUGGING OUTPUT ONLY
    System.out.print(index + " : ");
    if (hasExactMatch)
        Debug.println(lineseg + "   has exact match");
    else
        Debug.println(lineseg + "   matches  " + mls.index + ": " + mls.lineseg);
    */
  }

  /**
   * Determine whethers this segment needs to have adjustments computed
   *
   * @return true if this segment needs to have adjustments computed
   */
  public boolean isAdjustable()
  {
    if (matchedSubsegs.size() <= 0) return false;
    if (hasExactMatch) return false;
    return true;
  }

  public boolean isAdjusted()
  {
    if (hasExactMatch) return false;
    boolean isAdjusted = false;
    for (int i = 0; i < matchedSubsegs.size(); i++) {
      MatchedSubsegment subSeg = (MatchedSubsegment) matchedSubsegs.get(i);
      /**
       * Within each Subsegment
       * coordinates are sorted in order along the target segment.
       * The first and last coordinates are not added
       * if they are adjusted versions of the segment endpoints,
       * since these will be provided by the corresponding MatchedVertex
       */
      if (i >= 0 || ! subSeg.isEndPointAdjusted(0))
        isAdjusted = true;
      if (i < matchedSubsegs.size() - 1  || ! subSeg.isEndPointAdjusted(1))
        isAdjusted = true;
    }
    return isAdjusted;
  }

  public void computeAdjusted()
  {
    /**
     * Sort the matched subsegments in order along this segment.  Note that this
     * code does not attempt to handle robustness problems - if the segments are
     * too short they may be incorrectly sorted.  Choosing a reasonably large
     * tolerance value should prevent this situation from happening.
     */
    if (! isMatchesSorted)  {
      Collections.sort(matchedSubsegs);
      isMatchesSorted = true;
    }
    /**
     * Record the adjusted vertices in the associated matched segment.
     * These vertices will be later inserted into the adjusted segment.
     */
    for (int i = 0; i < matchedSubsegs.size(); i++) {
      MatchedSubsegment subSeg = (MatchedSubsegment) matchedSubsegs.get(i);
      MatchedSegment matchedSeg = subSeg.getMatchedSegment();
      matchedSeg.addAdjustedVertex(subSeg.getAdjustedCoordinate(0));
      matchedSeg.addAdjustedVertex(subSeg.getAdjustedCoordinate(1));
    }
  }

  public void addInsertedSubjectVertices(CoordinateList coordList)
  {
    // matched segments are sorted along the Subject segment
    for (int i = 0; i < matchedSubsegs.size(); i++) {
      MatchedSubsegment refSeg = (MatchedSubsegment) matchedSubsegs.get(i);
      /**
       * Within each SourceSubsegment
       * coordinates are sorted in order along the target segment.
       * The first and last coordinates are not added
       * if they are adjusted versions of the segment endpoints,
       * since these will be provided by the corresponding MatchedVertex
       */
      if (i > 0 || ! refSeg.isEndPointAdjusted(0))
        coordList.add(refSeg.getAdjustedCoordinate(0));
      if (i < matchedSubsegs.size() - 1  || ! refSeg.isEndPointAdjusted(1))
        coordList.add(refSeg.getAdjustedCoordinate(1));
    }
  }

  private boolean isAdjustedVertex(Coordinate p)
  {
    Coordinate p0 = matchedShell.getAdjustedVertex(index);
    if (p.equals(p0)) return true;
    Coordinate p1 = matchedShell.getAdjustedVertex(index + 1);
    if (p.equals(p1)) return true;
    return false;
  }

  private void addAdjustedVertexIndicator(Coordinate adjPt, List adjInd)
  {
    // if a vertex has been adjusted to the same pt, don't create an indicator
    if (isAdjustedVertex(adjPt)) return;

    Coordinate origPt = lineseg.project(adjPt);
    Coordinate[] line = MatchedShellSubject.createAdjustedVertexIndicator(origPt, adjPt);
    adjInd.add(line);
  }

  public void addAdjustedVertexIndicators(List adjIndList)
  {
    // matched segments are sorted along the Subject segment
    for (int i = 0; i < matchedSubsegs.size(); i++) {
      MatchedSubsegment refSeg = (MatchedSubsegment) matchedSubsegs.get(i);
      /**
       * Within each subsubsegment
       * coordinates are sorted in order along the Subject segment.
       * The first and last coordinates are not added
       * if they are adjusted versions of the segment endpoints,
       * since these will be provided by the corresponding MatchedVertex
       */
      if (i > 0 || ! refSeg.isEndPointAdjusted(0)) {
        addAdjustedVertexIndicator(refSeg.getAdjustedCoordinate(0), adjIndList);
      }
      if (i < matchedSubsegs.size() - 1  || ! refSeg.isEndPointAdjusted(1)) {
        addAdjustedVertexIndicator(refSeg.getAdjustedCoordinate(1), adjIndList);
      }

// testing only - test range of size of adjustments
/*
System.out.println("adjusted 0: "
+ refSeg.getAdjustedCoordinate(0)
+ " "
+ CGAlgorithms.distancePointLine(refSeg.getAdjustedCoordinate(0),
      lineseg.p0, lineseg.p1) );
System.out.println("adjusted 1: "
+ refSeg.getAdjustedCoordinate(1)
+ "   "
+ CGAlgorithms.distancePointLine(refSeg.getAdjustedCoordinate(1),
      lineseg.p0, lineseg.p1)
);
      */
    }
  }

  public boolean isEndPointAdjusted(int index)
  {
    MatchedSubsegment srcSeg = getExtremalMatchedSegment(index);
    if (srcSeg == null) return false;
    return srcSeg.isEndPointAdjusted(index);
  }

  public Coordinate getAdjustedEndPoint(int index)
  {
    MatchedSubsegment srcSeg = getExtremalMatchedSegment(index);
    if (srcSeg == null) return null;
    return srcSeg.getAdjustedCoordinate(index);
  }

  /**
   * Find one or other of the extremal matched segments (i.e. the end items
   * in the list)
   *
   * @param index 0 or 1, depending on whether the first or last matched segment is desired
   */
  private MatchedSubsegment getExtremalMatchedSegment(int index)
  {
    if (matchedSubsegs.size() < 1) return null;
    MatchedSubsegment srcSeg = null;
    switch (index) {
    case 0:
      srcSeg = (MatchedSubsegment) matchedSubsegs.get(0);
      break;
    case 1:
      srcSeg = (MatchedSubsegment) matchedSubsegs.get(matchedSubsegs.size() - 1);
      break;
    }
    return srcSeg;
  }

  /**
   * Records a vertex that was adjusted in some other Matched shell.
   *
   * @param coord the adjusted value of the vertex
   */
  private void addAdjustedVertex(Coordinate coord)
  {
    adjustedVertices.add(coord);
  }

  /**
   * Creates a list of inserted vertices for a Reference segment.
   * Vertices are inserted into the segment as long
   * as they are not equal to one of the endpoints
   * of the segment.
   * Note that no explicit check is made to ensure that short linesegments are not
   * created as a result of the insertion.
   * However, this should never happen, since an inserted vertex which might create a short
   * line segment should have been snapped to an endpoint already.
   */
  public List computeReferenceInsertedVertices()
  {
    List addedVert = new ArrayList();
    for (int i = 0; i < adjustedVertices.size(); i++) {
        Coordinate coord = (Coordinate) adjustedVertices.get(i);
        // only add vertices which are NOT an endpoint
        if (! ( lineseg.p0.equals(coord) ||
                lineseg.p1.equals(coord)  ) ) {
          Vertex v = new Vertex(coord, lineseg);
          addedVert.add(v);
        }
      // sort the added vertices in order along this segment
      Collections.sort(addedVert, new Vertex.PositionComparator());
    }
    return addedVert;
  }
}
