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

import com.vividsolutions.jcs.graph.*;

public class MatchEdge implements Serializable
{


  double angle; // original angle
  private int index;
  private MatchEdge matchedEdge = null;
  private double matchAngle;
  private DirectedEdge de;

  public MatchEdge(int index, double angle, DirectedEdge de) {
    this.index = index;
    this.angle = angle;
    this.de = de;
  }

  public MatchEdge getMatch() { return matchedEdge; }
  public void setMatch(MatchEdge matchedEdge) { setMatch(matchedEdge, 0.0); }
  public void setMatch(MatchEdge matchedEdge, double matchAngle)
  {
    this.matchedEdge = matchedEdge;
    this.matchAngle = matchAngle;
  }
  public boolean isMatched() { return matchedEdge != null; }
  public double getCurrentAngle() { return angle; }
  public double getMatchAngle() { return matchAngle; }
  public DirectedEdge getDirectedEdge() { return de; }
  public int getIndex() { return index; }

}
