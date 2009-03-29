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

import java.util.*;
import com.vividsolutions.jcs.util.BufferedIterator;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.debug.DebugFeature;

public class RoadNodeMatcher {

  private static STRtree buildNodeIndex(Iterator nodeIterator)
  {
    STRtree nodeIndex = new STRtree();
    while (nodeIterator.hasNext ()) {
      RoadNode node = (RoadNode) nodeIterator.next();
      Coordinate pt = node.getCoordinate();
      nodeIndex.insert(new Envelope(pt), node);
    }
    return nodeIndex;
  }

  private static void clearNodeMatches(Iterator nodeIterator)
  {
    while (nodeIterator.hasNext ()) {
      RoadNode node = (RoadNode) nodeIterator.next();
      node.clearMatch();
    }
  }

  private static List nodesWithinDistance(
      STRtree nodeIndex,
      Coordinate queryPt,
      double queryDistance)
  {

    Envelope queryEnv = new Envelope(queryPt.x - queryDistance,
                                     queryPt.x + queryDistance,
                                     queryPt.y - queryDistance,
                                     queryPt.y + queryDistance);
    return nodeIndex.query(queryEnv);
  }

  // parameters controlling matching
  private double nodeDistanceTolerance = 0.0;
  private boolean matchInlineNodes = false;

  private RoadNetwork[] source = new RoadNetwork[2];
  private RoadGraph[] sourceGraph = new RoadGraph[2];

  private STRtree[] nodeIndex = new STRtree[2];

  public RoadNodeMatcher(RoadNetwork source0, RoadNetwork source1)
  {
    source[0] = source0;
    source[1] = source1;

    sourceGraph[0] = source[0].getGraph();
    sourceGraph[1] = source[1].getGraph();

    clearNodeMatches(sourceGraph[0].nodeIterator());
    clearNodeMatches(sourceGraph[1].nodeIterator());

    nodeIndex[0] = buildNodeIndex(sourceGraph[0].nodeIterator());
    nodeIndex[1] = buildNodeIndex(sourceGraph[1].nodeIterator());
  }

  public void setNodeDistanceTolerance(double nodeDistanceTolerance)
  {
    this.nodeDistanceTolerance = nodeDistanceTolerance;
  }

  /**
   * Controls whether nodes of degree 2 are matched.
   *
   * @param matchInlineNodes <code>true</code> if inline nodes are to be matched
   */
  public void setMatchInlineNodes(boolean matchInlineNodes)
  {
    this.matchInlineNodes = matchInlineNodes;
  }

  public void match()
  {
    match(0, 1);
  }

  private void match(int index0, int index1)
  {
    // only allocate return buffers once
    RoadNode[] resultNode = new RoadNode[1];
    NodeMatching[] resultNodeMatching = new NodeMatching[1];

    BufferedIterator i = new BufferedIterator(sourceGraph[index0].nodeIterator());
    while (i.hasNext() ) {
      RoadNode node = (RoadNode) i.next();

      // don't try and match "inline" nodes
      //if (node.getOutEdges().getNumEdges() == 2) continue;

      double queryDist = nodeDistanceTolerance;
      List candidateSet = nodesWithinDistance(nodeIndex[index1], node.getCoordinate(), queryDist);
//Debug.println("Matching node at " + node.getCoordinate() + " to candidate set of size " + candidateSet.size());
      double matchValue = findClosestUnmatchedSimilarTopoNode(node, candidateSet, resultNode, resultNodeMatching);
      RoadNode matchNode = resultNode[0];
      NodeMatching nodeMatching = resultNodeMatching[0];

      if (matchNode != null) {
        // if this match is better than the current one, redo the current match
        RoadNode prevMatchNode = matchNode.getMatch();
        double prevMatchValue = matchNode.getMatchValue();
        if (prevMatchValue < matchValue) {
          // displace the lower valued match
          if (prevMatchNode != null) {
              i.putBack(prevMatchNode);
          }

          matchNode.setMatch(node, matchValue);
          matchNode.setMatching(nodeMatching);
          node.setMatch(matchNode, matchValue);
          node.setMatching(nodeMatching);
//          if (nodeMatching.getEdgeMatches().size() == 0)
//            System.out.println("Found null edge match set");

        }
      }
    }
DebugFeature.saveFeatures(MATCH, "Y:\\jcs\\testUnit\\roads\\nodeAllMatches.jml");
  }

private static final String MATCH = "Match";

  private double findClosestUnmatchedSimilarTopoNode(RoadNode node, List candidateSet,
      RoadNode[] resultNode,
      NodeMatching[] resultNodeMatching)
  {
    resultNode[0] = null;
    resultNodeMatching[0] = null;
    double maxMatchValue = 0.0;

    Coordinate pt = node.getCoordinate();

    for (Iterator i = candidateSet.iterator(); i.hasNext(); ) {
      RoadNode candidateNode = (RoadNode) i.next();

      // check if not matching "inline" nodes
      if (! matchInlineNodes && candidateNode.getOutEdges().getNumEdges() == 2) continue;

      NodeMatching nodeMatching = new NodeMatching(node, candidateNode);
      //double matchValue = nodeMatching.angleDistanceMatchValue(nodeDistanceTolerance);
      double matchValue = nodeMatching.angleDistTopoSignificanceAdjustedMatchValue(nodeDistanceTolerance);
DebugFeature.addLineSegment(MATCH, pt, candidateNode.getCoordinate(), "val=" + (int) (1000.0 * matchValue));

      // don't match if candidate node already has a better match
      if (candidateNode.getMatchValue() > matchValue) {
        continue;
      }

      if (matchValue > maxMatchValue) {
        resultNode[0] = candidateNode;
        resultNodeMatching[0] = nodeMatching;
        maxMatchValue = matchValue;
      }
    }
    return maxMatchValue;
  }

}
