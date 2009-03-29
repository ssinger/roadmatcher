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
import java.util.*;

public class DirectedEdgeStar implements Serializable
{


  List outEdges = new ArrayList();
  private boolean sorted = false;

  public DirectedEdgeStar() {
  }
  public void add(DirectedEdge de)
  {
    outEdges.add(de);
    sorted = false;
  }
  public void remove(DirectedEdge de)
  {
    outEdges.remove(de);
  }
  public Iterator iterator()
  {
    sortEdges();
    return outEdges.iterator();
  }

  public int getDegree() { return outEdges.size(); }
  public int getNumEdges() { return outEdges.size(); }

  public List getEdges()
  {
    sortEdges();
    return outEdges;
  }

  private void sortEdges()
  {
    if (! sorted) {
      Collections.sort(outEdges);
      sorted = true;
    }
  }
  public int getIndex(Edge edge)
  {
    sortEdges();
    for (int i = 0; i < outEdges.size(); i++) {
      DirectedEdge de = (DirectedEdge) outEdges.get(i);
      if (de.getEdge() == edge)
        return i;
    }
    return -1;
  }
  public int getIndex(DirectedEdge dirEdge)
  {
    sortEdges();
    for (int i = 0; i < outEdges.size(); i++) {
      DirectedEdge de = (DirectedEdge) outEdges.get(i);
      if (de == dirEdge)
        return i;
    }
    return -1;
  }
  public int getIndex(int i)
  {
    int modi = i % outEdges.size();
    if (modi < 0) modi += outEdges.size();
    return modi;
  }

  public DirectedEdge getNextEdge(DirectedEdge dirEdge)
  {
    int i = getIndex(dirEdge);
    if (i < 0)
      throw new IllegalArgumentException("input does not start at this node");
    return (DirectedEdge) outEdges.get(getIndex(i + 1));
  }
}
