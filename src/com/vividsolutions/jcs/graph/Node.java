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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;

/**
 */
public class Node
{  
  /**
   * The input nodes are assumed to be different
   */
  public static Collection getEdgesBetween(Node node0, Node node1)
  {
    List edges0 = DirectedEdge.toEdges(node0.getOutEdges().getEdges());
    Set commonEdges = new HashSet(edges0);
    List edges1 = DirectedEdge.toEdges(node1.getOutEdges().getEdges());
    commonEdges.retainAll(edges1);
    return commonEdges;
  }

  protected Coordinate pt;
  protected DirectedEdgeStar deStar;

  public Node(Coordinate pt)
  {
    this(pt, new DirectedEdgeStar());
  }

  public Node(Coordinate pt, DirectedEdgeStar deStar)
  {
    this.pt = pt;
    this.deStar = deStar;
  }

  public Coordinate getCoordinate() { return pt; }

  public DirectedEdgeStar getOutEdges() { return deStar; }
  public int getDegree() { return deStar.getDegree(); }

  public int getIndex(Edge edge)
  {
    return deStar.getIndex(edge);
  }

}
