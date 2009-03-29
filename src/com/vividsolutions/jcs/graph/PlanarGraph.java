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

package com.vividsolutions.jcs.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A directed graph which can be embedded in a planar surface.
 */
public class PlanarGraph
{
  protected List nodes = new ArrayList();
  protected List edges = new ArrayList();
  protected List dirEdges = new ArrayList();
  protected Map nodeMap = new TreeMap();

  public Node findNode(Coordinate pt)
  {
    return (Node) nodeMap.get(pt);
  }

  public void add(Node node)
  {
    nodes.add(node);
    nodeMap.put(node.getCoordinate(), node);
  }

  /**
   * This assumes that the edge has already been created with it's associated DirectEdges.
   * @param edge
   */
  public void add(Edge edge)
  {
    edges.add(edge);
    add(edge.getDirEdge(0));
    add(edge.getDirEdge(1));
  }

  public void add(DirectedEdge dirEdge)
  {
    dirEdges.add(dirEdge);
  }

  public Iterator nodeIterator()  {    return nodes.iterator();  }
  public List getNodes()  {    return nodes;  }
  public Iterator dirEdgeIterator()  {    return dirEdges.iterator();  }
  public Iterator edgeIterator()  {    return edges.iterator();  }
  public List getEdges()  {    return edges;  }
  public List getDirectedEdges()  {    return dirEdges;  }

  /**
   * Remove an edge and its associated DirectedEdges from the graph.
   * Note: this method does not remove the nodes associated with the edge,
   * even if the removal of the edge reduces the degree of a node to zero.
   *
   * @param edge the edge to remove
   */
  public void remove(Edge edge)
  {
    remove(edge.getDirEdge(0));
    remove(edge.getDirEdge(1));
    edges.remove(edge);
  }
  public void remove(DirectedEdge de)
  {
    DirectedEdge sym = de.getSym();
    if (sym != null) sym.setSym(null);
    de.getFromNode().getOutEdges().remove(de);
    dirEdges.remove(de);
  }
  /**
   * Removes a node from the graph, along with any associated DirectedEdges and edges.
   *
   * @param node the node to remove
   */
  public void remove(Node node)
  {
    // unhook all directed edges
    List outEdges = node.getOutEdges().getEdges();
    for (Iterator i = outEdges.iterator(); i.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) i.next();
      DirectedEdge sym = de.getSym();
      // remove the diredge that points to this node
      if (sym != null) remove(sym);
      // remove this diredge from the graph collection
      dirEdges.remove(de);

      Edge edge = de.getEdge();
      if (edge != null) {
        edges.remove(edge);
      }

    }
    // remove the node from the graph
    nodeMap.remove(node.getCoordinate());
    nodes.remove(node);
  }

  public List findNodesOfDegree(int degree)
  {
    List nodesFound = new ArrayList();
    for (Iterator i = nodes.iterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (node.getDegree() == degree)
        nodesFound.add(node);
    }
    return nodesFound;
  }

}
