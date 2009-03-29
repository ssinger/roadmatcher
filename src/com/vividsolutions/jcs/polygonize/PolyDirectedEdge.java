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
import com.vividsolutions.jcs.graph.*;

public class PolyDirectedEdge
    extends DirectedEdge
{

  private EdgeRing edgeRing = null;
  private PolyDirectedEdge next = null;
  private boolean isDeleted = false;
  private long label = -1;

  public PolyDirectedEdge(Node from, Node to, Coordinate directionPt, boolean edgeDirection)
  {
    super(from, to, directionPt, edgeDirection);
  }

  public boolean isDeleted() { return isDeleted; }
  public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

  public long getLabel() { return label; }
  public void setLabel(long label) { this.label = label; }

  public PolyDirectedEdge getNext()  {    return next;  }
  public void setNext(PolyDirectedEdge next)  {   this.next = next;  }
  public boolean isInRing() { return edgeRing != null; }
  public void setRing(EdgeRing edgeRing)
  {
      this.edgeRing = edgeRing;
  }

}
