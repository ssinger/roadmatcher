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

import java.io.Serializable;



/**
 * Represents an undirected edge of a planar graph.
 * An undirected edge
 * in fact simply bundles two opposite {@link DirectedEdge}s.
 */
public class Edge implements Serializable {

  protected DirectedEdge[] dirEdge;

  public Edge()
  {
  }

  public Edge(DirectedEdge de0, DirectedEdge de1)
  {
    setDirectedEdges(de0, de1);
  }

  public void setDirectedEdges(DirectedEdge de0, DirectedEdge de1)
  {
    dirEdge = new DirectedEdge[] { de0, de1 };
    de0.setEdge(this);
    de1.setEdge(this);
    de0.setSym(de1);
    de1.setSym(de0);
    de0.getFromNode().getOutEdges().add(de0);
    de1.getFromNode().getOutEdges().add(de1);
  }

  /**
   * Gets one of the DirectedEdges associated with this edge.
   * @param i 0 or 1
   * @return a DirectedEdge
   */
  public DirectedEdge getDirEdge(int i)
  {
    return dirEdge[i];
  }

  /**
   * Finds the {@link DirectedEdge} that starts from the given node.
   * @param fromNode the {@link Node} the Directed edge starts from
   * @return the {@link DirectedEdge} starting from the node.
   */
  public DirectedEdge getDirEdge(Node fromNode)
  {
    if (dirEdge[0].getFromNode() == fromNode) return dirEdge[0];
    if (dirEdge[1].getFromNode() == fromNode) return dirEdge[1];
    // node not found
    // possibly should throw an exception here?
    return null;
  }

  public Node getOppositeNode(Node node)
  {
    if (dirEdge[0].getFromNode() == node) return dirEdge[0].getToNode();
    if (dirEdge[1].getFromNode() == node) return dirEdge[1].getToNode();
    // node not found
    // possibly should throw an exception here?
    return null;
  }

  public Node getNode(int i)
  {
    return dirEdge[i].getFromNode();
  }

  public int getNodeIndex(Node node)
  {
    if (dirEdge[0].getFromNode() == node) return 0;
    if (dirEdge[1].getFromNode() == node) return 1;
    // node not found
    // possibly should throw an exception here?
    return -999;
  }
}
