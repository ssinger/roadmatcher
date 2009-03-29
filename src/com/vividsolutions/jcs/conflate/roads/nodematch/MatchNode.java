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

package com.vividsolutions.jcs.conflate.roads.nodematch;

import java.io.Serializable;
import java.util.*;

import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jcs.graph.*;


/**
 * Represents a matching of edges originating at two nodes.
 * The matching is based only on the angles of the edges around each node.
 * <p>
 * <h2>Future</h2>
 * Allow angle to be computed from a length of the input linestring, not just
 * the closest line segment (This will accomodate small ending line segments with
 * angles which do not well represent the overall angle of the incident edge).
 */
public class MatchNode implements Serializable
{

  public static final int UNMATCHED = -1;

  MatchEdge[] edges;

  public MatchNode(int nEdges)
  {
    edges = new MatchEdge[nEdges];
  }

  public MatchEdge getEdge(int index)
  {
    return edges[index];
  }
  public void setEdge(int index, double angle, DirectedEdge de)
  {
    edges[index] = new MatchEdge(index, angle, de);
  }

  public int getNumEdges() { return edges.length; }

  public int getNumMatchedEdges()
  {
    int matchedEdgeCount = 0;
    for (int i = 0; i < edges.length; i++) {
      if (edges[i].isMatched())
        matchedEdgeCount++;
    }
    return matchedEdgeCount;
  }

  /**
   * Get a list of all the {@link MatchEdge}s around the first node in this matching.
   * Note that unmatched edges around either match node will not be
   * present in this list.
   * @return
   */
  public List getEdgeMatches()
  {
    List edgeMatches = new ArrayList();
    int matchedEdgeCount = 0;
    for (int i = 0; i < edges.length; i++) {
      if (edges[i].isMatched())
        edgeMatches.add(edges[i]);
    }
    return edgeMatches;
  }

  public void match(MatchNode other, double maxAngleDiff)
  {
    //matchSlice(0, edges.length - 1, other, 0, other.edges.length - 1, true, maxAngleDiff);
    // initially process all the edges.
    matchSlice(0, 0, other, 0, 0, true, maxAngleDiff);
  }
  public int previousIndex(int index)  {    return (index - 1 + edges.length) % edges.length;  }
  public int nextIndex(int index)  {    return (index + 1) % edges.length;  }

  /**
   * Matches two "slices" of edges in two MatchNodes.
   * The slice contains edges from from edge[start] to edge[end - 1],
   * unless processAllEdges is <code>true</code>, in which case
   * the unmatched block of edges runs from edge[start] to edge[end] inclusive.
   * (The indexing is circular, so if start == end all edges will be processed)
   * @param start
   * @param end
   * @param other
   * @param otherStart
   * @param otherEnd
   * @param processAllEdges
   * @param maxAngleDiff
   */
  private void matchSlice(int start, int end,
                             MatchNode other,
                             int otherStart, int otherEnd,
                             boolean processAllEdges,
                             double maxAngleDiff)
  {
    // one or other of the slices is empty, so no need to continue (but this counts as a valid match)
    if (! processAllEdges && (start == end || otherStart == otherEnd)) return;

    int[] matchIndex = new int[2];
    double matchAngle = findClosestEdge(start, end, other, otherStart, otherEnd, processAllEdges, matchIndex);
//Debug.println("match angle = " + matchAngle);
    if (matchAngle >  maxAngleDiff ) return;

    // set the matched edges
    edges[matchIndex[0]].setMatch(other.edges[matchIndex[1]], matchAngle);
    other.edges[matchIndex[1]].setMatch(edges[matchIndex[0]], matchAngle);

    /**
     * Recurse to match the smaller slices on either side of the newly matched edges
     * If processing all edges, the new slice is the entire circle
     * starting and ending at the same edge, and only one recursion is needed.
     * Otherwise, the matched edges splits its containing slice into up to two pieces
     * so we need two calls to matchSlice.
     */
    if (processAllEdges) {
      matchSlice(nextIndex(matchIndex[0]), matchIndex[0],
                 other,
                 other.nextIndex(matchIndex[1]), matchIndex[1],
                 false, maxAngleDiff);
    }
    else {
      matchSlice(start, matchIndex[0],
                 other,
                 otherStart, matchIndex[1],
                 false, maxAngleDiff);
      int next = nextIndex(matchIndex[0]);
      int otherNext = other.nextIndex(matchIndex[1]);
      matchSlice(next, end,
                 other,
                 otherNext, otherEnd,
                 false, maxAngleDiff);
    }
  }


  private double findClosestEdge(int start, int end,
                                MatchNode other, int otherStart, int otherEnd,
                                boolean processAllEdges,
                                int[] matchIndex)
  {
    boolean isFirst = processAllEdges;
    boolean isFirstOther = processAllEdges;

    matchIndex[0] = UNMATCHED;
    matchIndex[1] = UNMATCHED;
    //MatchEdge[] closestEdge = new MatchEdge[2];
    double minAngleDiff = Double.MAX_VALUE;
    for (int i = start; isFirst || i != end; i = nextIndex(i)) {
      //Assert: ! edges[i].isMatched()
      for (int j = otherStart; isFirstOther || j != otherEnd; j = other.nextIndex(j)) {
        // Assert: ! edges[j].isMatched()
//        try {
//          MatchEdge e = edges[i];
//          e = other.edges[j];
//        }
//        catch (Exception ex) {
//          System.out.println(i);
//        }
        double angleDiff = Angle.diff(edges[i].getCurrentAngle(), other.edges[j].getCurrentAngle());
        if (angleDiff < minAngleDiff) {
          matchIndex[0] = i;
          matchIndex[1] = j;
          minAngleDiff = angleDiff;
        }
        isFirstOther = false;
      }
      isFirst = false;
    }
    //Assert: matchIndex[0] != UNMATCHED && matchIndex[1] != UNMATCHED
    Assert.isTrue(matchIndex[0] != UNMATCHED && matchIndex[1] != UNMATCHED);

    return minAngleDiff;
  }




}
