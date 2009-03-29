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

package com.vividsolutions.jcs.polygonize;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jcs.graph.*;

class PolyGraph
    extends PlanarGraph
{

  public static int getDegreeNonDeleted(Node node)
  {
    List edges = node.getOutEdges().getEdges();
    int degree = 0;
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      if (! de.isDeleted()) degree++;
    }
    return degree;
  }

  public static int getDegree(Node node, long label)
  {
    List edges = node.getOutEdges().getEdges();
    int degree = 0;
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      if (de.getLabel() == label) degree++;
    }
    return degree;
  }

  /**
   * Deletes all edges at a node
   * @param node
   */
  public static void deleteAllEdges(Node node)
  {
    List edges = node.getOutEdges().getEdges();
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      de.setDeleted(true);
      PolyDirectedEdge sym = (PolyDirectedEdge) de.getSym();
      if (sym != null)
        sym.setDeleted(true);
    }
  }

  //private List labelledRings;

  public PolyGraph() {
  }

  public void addEdge(LineString line)
  {
    Coordinate[] linePts = CoordinateArrays.removeRepeatedPoints(line.getCoordinates());
    Coordinate startPt = linePts[0];
    Coordinate endPt = linePts[linePts.length - 1];

    Node nStart = getNode(startPt);
    Node nEnd = getNode(endPt);

    DirectedEdge de0 = new PolyDirectedEdge(nStart, nEnd, linePts[1], true);
    DirectedEdge de1 = new PolyDirectedEdge(nEnd, nStart, linePts[linePts.length - 2], false);
    Edge edge = new PolyEdge(line);
    edge.setDirectedEdges(de0, de1);
    add(edge);
  }

  private Node getNode(Coordinate pt)
  {
    Node node = findNode(pt);
    if (node == null) {
      node = new Node(pt);
      // ensure node is only added once to graph
      add(node);
    }
    return node;
  }

  private void computeNextCWEdges()
  {
    // set the next pointers for the edges around each node
    for (Iterator iNode = nodes.iterator(); iNode.hasNext(); ) {
      Node node = (Node) iNode.next();
      computeNextCWEdges(node);
    }
  }

  public void convertMaximalToMinimalEdgeRings(List ringEdges)
  {
    for (Iterator i = ringEdges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      long label = de.getLabel();
      List intNodes = findIntersectionNodes(de, label);

      if (intNodes == null) continue;
      // flip the next pointers on the intersection nodes to create minimal edge rings
      for (Iterator iNode = intNodes.iterator(); iNode.hasNext(); ) {
        Node node = (Node) iNode.next();
        computeNextCCWEdges(node, label);
      }
    }
  }

  /**
   * Finds all nodes in a maximal edgering which are self-intersection nodes
   * @param startDE
   * @param label
   * @return the list of intersection nodes found; <code>null</code> if no intersection nodes were found
   */
  private static List findIntersectionNodes(PolyDirectedEdge startDE, long label)
  {
    PolyDirectedEdge de = startDE;
    List intNodes = null;
    do {
      Node node = de.getFromNode();
      if (getDegree(node, label) > 1) {
        if (intNodes == null)
          intNodes = new ArrayList();
        intNodes.add(node);
      }

      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || ! de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return intNodes;
  }

  public List getEdgeRings()
  {
    // maybe could optimize this, since most of these pointers should be set correctly already
    // by deleteCutEdges()
    computeNextCWEdges();
    // clear labels of all edges in graph
    label(dirEdges, -1);
    List maximalRings = findLabeledEdgeRings(dirEdges);
    convertMaximalToMinimalEdgeRings(maximalRings);

    // find all edgerings
    List edgeRingList = new ArrayList();
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      if (de.isDeleted()) continue;
      if (de.isInRing()) continue;

      EdgeRing er = findEdgeRing(de);
      edgeRingList.add(er);
    }
    return edgeRingList;
  }

  /**
   *
   * @return a List of DirectedEdges, one for each edge ring found
   */
  private static List findLabeledEdgeRings(List dirEdges)
  {
    List edgeRings = new ArrayList();
    // label the edge rings formed
    long currLabel = 1;
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      if (de.isDeleted()) continue;
      if (de.getLabel() >= 0) continue;

      edgeRings.add(de);
      List edges = findEdgeRingList(de);

      label(edges, currLabel);
      currLabel++;
    }
    return edgeRings;
  }

  public List deleteCutEdges()
  {
    computeNextCWEdges();
    // label the current set of edgerings
    findLabeledEdgeRings(dirEdges);

    /**
     * Cut Edges are edges where both dirEdges have the same label.
     * Delete them, and record them
     */
    List cutLines = new ArrayList();
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      if (de.isDeleted()) continue;

      PolyDirectedEdge sym = (PolyDirectedEdge) de.getSym();

      if (de.getLabel() == sym.getLabel()) {
        de.setDeleted(true);
        sym.setDeleted(true);

        // save the line as a cut edge
        PolyEdge e = (PolyEdge) de.getEdge();
        cutLines.add(e.getLine());
      }
    }
    return cutLines;
  }

  private static void label(List dirEdges, long label)
  {
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      PolyDirectedEdge de = (PolyDirectedEdge) i.next();
      de.setLabel(label);
    }
  }
  private static void computeNextCWEdges(Node node)
  {
    DirectedEdgeStar deStar = node.getOutEdges();
    PolyDirectedEdge startDE = null;
    PolyDirectedEdge prevDE = null;

    // the edges are stored in CCW order around the star
    for (Iterator i = deStar.getEdges().iterator(); i.hasNext(); ) {
      PolyDirectedEdge outDE = (PolyDirectedEdge) i.next();
      if (outDE.isDeleted()) continue;

      if (startDE == null)
        startDE = outDE;
      if (prevDE != null) {
        PolyDirectedEdge sym = (PolyDirectedEdge) prevDE.getSym();
        sym.setNext(outDE);
      }
      prevDE = outDE;
    }
    if (prevDE != null) {
      PolyDirectedEdge sym = (PolyDirectedEdge) prevDE.getSym();
      sym.setNext(startDE);
    }
  }
  /**
   * Computes the next edge pointers going CCW around the given node, for the
   * given edgering label.
   * This algorithm has the effect of converting maximal edgerings into minimal edgerings
   */
  private static void computeNextCCWEdges(Node node, long label)
  {
    DirectedEdgeStar deStar = node.getOutEdges();
    //PolyDirectedEdge lastInDE = null;
    PolyDirectedEdge firstOutDE = null;
    PolyDirectedEdge prevInDE = null;

    // the edges are stored in CCW order around the star
    List edges = deStar.getEdges();
    //for (Iterator i = deStar.getEdges().iterator(); i.hasNext(); ) {
    for (int i = edges.size() - 1; i >= 0; i--) {
      PolyDirectedEdge de = (PolyDirectedEdge) edges.get(i);
      PolyDirectedEdge sym = (PolyDirectedEdge) de.getSym();

      PolyDirectedEdge outDE = null;
      if (  de.getLabel() == label) outDE = de;
      PolyDirectedEdge inDE = null;
      if (  sym.getLabel() == label) inDE =  sym;

      if (outDE == null && inDE == null) continue;  // this edge is not in edgering

      if (inDE != null) {
        prevInDE = inDE;
      }

      if (outDE != null) {
        if (prevInDE != null) {
          prevInDE.setNext(outDE);
          prevInDE = null;
        }
        if (firstOutDE == null)
          firstOutDE = outDE;
      }
    }
    if (prevInDE != null) {
      Assert.isTrue(firstOutDE != null);
      prevInDE.setNext(firstOutDE);
    }
  }

  private static List findEdgeRingList(PolyDirectedEdge startDE)
  {
    PolyDirectedEdge de = startDE;
    List edges = new ArrayList();
    do {
      edges.add(de);
      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || ! de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return edges;
  }

  private EdgeRing findEdgeRing(PolyDirectedEdge startDE)
  {
    PolyDirectedEdge de = startDE;
    EdgeRing er = new EdgeRing();
    do {
      er.add(de);
      de.setRing(er);
      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || ! de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return er;
  }

  /**
   * Marks all edges from the graph which are "dangles"
   * (e.g. have are incident on a node with degree 1).
   * This process is recursive, since removing a dangling edge
   * may result in another edge becoming a dangle.
   *
   * @return a List containing the LineStrings that formed dangles
   */
  public Collection deleteDangles()
  {
    List nodesToRemove = findNodesOfDegree(1);
    Set dangleLines = new HashSet();

    Stack nodeStack = new Stack();
    for (Iterator i = nodesToRemove.iterator(); i.hasNext(); ) {
      nodeStack.push(i.next());
    }

    while (! nodeStack.isEmpty()) {
      Node node = (Node) nodeStack.pop();

      deleteAllEdges(node);
      List nodeOutEdges = node.getOutEdges().getEdges();
      for (Iterator i = nodeOutEdges.iterator(); i.hasNext(); ) {
        PolyDirectedEdge de = (PolyDirectedEdge) i.next();
        // delete this edge and its sym
        de.setDeleted(true);
        PolyDirectedEdge sym = (PolyDirectedEdge) de.getSym();
        if (sym != null)
          sym.setDeleted(true);

        // save the line as a dangle
        PolyEdge e = (PolyEdge) de.getEdge();
        dangleLines.add(e.getLine());

        Node toNode = de.getToNode();
        // add the toNode to the list to be processed, if it is now a dangle
        if (getDegreeNonDeleted(toNode) == 1)
          nodeStack.push(toNode);
      }
    }
    return dangleLines;
  }
}
