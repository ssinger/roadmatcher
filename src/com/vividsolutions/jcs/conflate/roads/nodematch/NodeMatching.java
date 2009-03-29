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

import java.util.List;

import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.DirectedEdgeStar;
import com.vividsolutions.jcs.graph.Edge;
import com.vividsolutions.jcs.graph.Node;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;

/**
 * Represents a possible way of matching the edges around two nodes.
 * An edge of a node may either match an edge in the other node
 * (which is a symmetric relationship) or be unmatched.
 */
public class NodeMatching
{

  /**
   * Computes a match value based on the distance between the matched nodes.
   * Value is normalized to lie in [0,1],
   * based on the maximum allowable distance value.
   *
   * @param maxDistance the maximum distance allowed between matched nodes
   * @param candidateDistance the distance between two matched nodes
   * @return a match value between 0 and 1
   */
  public static double nodeDistanceMatchValue(double maxDistance, double candidateDistance)
  {
    double relDistValue = candidateDistance / maxDistance;
    if (relDistValue > 1.0) return 0.0;
    double normValue = 1 - relDistValue;
    // reduce value of further distances
    double normValue2 = normValue * normValue * normValue;
    return normValue2;
  }

  public static MatchNode createMatchNode(Node node)
  {
    DirectedEdgeStar deStar = node.getOutEdges();
    MatchNode matchNode = new MatchNode(deStar.getNumEdges());
    List edges = deStar.getEdges();
    for (int i = 0; i < deStar.getNumEdges(); i++) {
      DirectedEdge de = (DirectedEdge) edges.get(i);
      matchNode.setEdge(i, de.getAngle(), de);
    }
    return matchNode;
  }

  private static double angleMatchValue(double angleDiff)
  {
    double normAng = angleDiff / Math.PI;
    if (normAng > 1.0)
      normAng = 1.0;
    double angValue = 1.0 - normAng;

    //double value = (2.0 * (1.0 - normAng)) - 1.0;
    return angValue * angValue;
  }

  private Node[] srcNode;
  private MatchNode[] matchNode;
  //private boolean isMatch;

  public NodeMatching(Node n1, Node n2)
  {
    this(createMatchNode(n1), createMatchNode(n2));
    srcNode = new Node[2];
    srcNode[0] = n1;
    srcNode[1] = n2;
  }

  private NodeMatching(MatchNode mn1, MatchNode mn2)
  {
    matchNode = new MatchNode[] { mn1, mn2 };
    matchNode[0].match(matchNode[1], Angle.PI_OVER_4);
  }

  //public boolean isMatch() { return isMatch; }

  public Node getNode(int nodeIndex) { return srcNode[nodeIndex]; }

  public MatchNode[] getMatchNodes() { return matchNode; }

  public Node getMatchedNode(Node queryNode)
  {
    if (srcNode[0] == queryNode) return srcNode[1];
    if (srcNode[1] == queryNode) return srcNode[0];
    Assert.shouldNeverReachHere();
    return null;// unmatched - error
  }
  public int getNodeIndex(Node queryNode)
  {
    if (srcNode[0] == queryNode) return 0;
    if (srcNode[1] == queryNode) return 1;
    Assert.shouldNeverReachHere();
    return -1;// unmatched - error
  }
  public int getMatchedEdgeIndex(Node node, int edgeIndex)
  {
    int nodeIndex = getNodeIndex(node);
    return matchNode[nodeIndex].edges[edgeIndex].getIndex();
  }

  public Edge getMatchedEdge(Node n, Edge edge)
  {
    int matchEdgeIndex = getMatchedEdgeIndex(n, n.getIndex(edge));
    if (matchEdgeIndex == MatchNode.UNMATCHED)
      return null;
    Node otherNode = getMatchedNode(n);
    DirectedEdge dirEdge = (DirectedEdge) otherNode.getOutEdges().getEdges().get(matchEdgeIndex);
    return dirEdge.getEdge();
  }

  /**
   * Get a list of all the {@link MatchEdge}s around the first node in this matching.
   * Note that unmatched edges around either match node will not be
   * present in this list.
   * @return
   */
  public List getEdgeMatches()
  {
    return matchNode[0].getEdgeMatches();
  }

  public double exactTopoMatchValue()
  {
    if (matchNode[0].getNumMatchedEdges() != matchNode[0].getNumEdges())
      return -1.0;
    if (matchNode[1].getNumMatchedEdges() != matchNode[1].getNumEdges())
      return -1.0;
    return 1.0;
  }

  public double minAngleMatchValue()
  {
    double minValue = 1.0;
    for (int i = 0; i < matchNode[0].getNumEdges(); i++) {
      MatchEdge me = matchNode[0].getEdge(i);
      if (me.isMatched()) {
        double value = angleMatchValue(me.getMatchAngle());
        if (value < minValue)
          minValue = value;
      }
    }
    return minValue;
  }

  public double edgeMatchValue()
  {
    int totalEdges = matchNode[0].getNumEdges() + matchNode[1].getNumEdges();
    int totalMatchedEdges = matchNode[0].getNumMatchedEdges() + matchNode[1].getNumMatchedEdges();
    double matchedFrac = totalMatchedEdges / (double) totalEdges;
    double edgeMatchScore = matchedFrac * matchedFrac;
    double matchValue = edgeMatchScore * minAngleMatchValue();
    return matchValue;
  }

  /**
   *  Combines match values for both edge angle matches and node distance
   */
  public double angleDistanceMatchValue(double nodeDistanceTolerance)
  {
    double edgeMatchValue = edgeMatchValue();

    double candidateDist = srcNode[0].getCoordinate().distance(srcNode[1].getCoordinate());
    if (candidateDist > nodeDistanceTolerance) return 0.0;

    double distMatchDistance = nodeDistanceMatchValue(nodeDistanceTolerance, candidateDist);
    // match function is 1 if position is identical, falling to 0 at infinity
    //double distMatchValue = (dist == 0.0 ? 1.0 : 1 / dist);
    //double distMatchValue = 1.0;

    // not bad, but not enough weight on distance
    //double matchValue = matchedEdgeCountValue * distMatchValue;

    // not very good
    //double matchValue = 0.5 * matchedEdgeCountValue + 0.5 * distMatchDistance;

    // empirical testing seems to show the following is a good heuristic
    double matchValue = 0.4 * edgeMatchValue + 0.6 * distMatchDistance;
    //double matchValue = 0.3 * matchedEdgeCountValue + 0.7 * distMatchDistance;

    return matchValue;
  }

  /**
   * Compute a match value based on the node distance and topology,
   * but reduced if one of the nodes is a 2-node.
   * Idea is that nodes which are not very topologically significant
   * should have lower match values.
   *
   * @param nodeDistanceTolerance
   * @return
   */
  public double angleDistTopoSignificanceAdjustedMatchValue(double nodeDistanceTolerance)
  {
    double angleDistanceMatchValue = angleDistanceMatchValue(nodeDistanceTolerance);

    boolean is2Node = matchNode[0].getNumEdges() == 2;
    is2Node |= matchNode[1].getNumEdges() == 2;

    boolean isTopoSignificant = ! is2Node;

    double reducedMatchValue = angleDistanceMatchValue;
    if (! isTopoSignificant)
      reducedMatchValue = .3 * angleDistanceMatchValue;
    return reducedMatchValue;
  }
}
