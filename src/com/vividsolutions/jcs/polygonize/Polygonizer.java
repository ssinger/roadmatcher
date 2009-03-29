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
//import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;

import com.vividsolutions.jump.task.TaskMonitor;

/**
 * Polygonizes a set of LineStrings.
 */
public class Polygonizer {

  private class LineStringAdder
      implements GeometryComponentFilter
  {
    public void filter(Geometry g) {
      if (g instanceof LineString)
        add((LineString) g);
    }
  }

  private boolean splitLineStrings = false;
  private GeometryFactory factory = new GeometryFactory();

  private PolyGraph graph = new PolyGraph();
  private Collection dangles = null;
  private List cutEdges = null;
  private List invalidRingLines = null;

  private List holeList = null;
  private List shellList = null;
  private List polyList = null;

  /**
   * To use:
   * <code>
   * polygonizer
   */
  public Polygonizer() {
  }

  public void setSplitLineStrings(boolean splitLineStrings) { this.splitLineStrings = splitLineStrings; }

  public void add(List geomList)
  {
    for (Iterator i = geomList.iterator(); i.hasNext(); ) {
      Geometry geom = (Geometry) i.next();
      add(geom);
    }
  }

  public void add(Geometry g)
  {
    g.apply(new LineStringAdder());
  }

  public void add(LineString line)
  {
    if (! splitLineStrings)
      graph.addEdge(line);
    else
      addSplit(line);
  }

  private void addSplit(LineString line)
  {
    Coordinate[] pts = line.getCoordinates();
    for (int i = 0; i < pts.length - 1; i++) {
      Coordinate[] linePts = new Coordinate[] { pts[i], pts[i + 1] };
      LineString lineSegment = factory.createLineString(linePts);
      graph.addEdge(lineSegment);
    }
  }

  public List getPolygons()  {    return polyList;  }
  public Collection getDangles()  {    return dangles;  }
  public Collection getCutEdges()  {    return cutEdges;  }
  public Collection getInvalidRingLines()  {    return invalidRingLines;  }

  public void polygonize(TaskMonitor monitor)
  {
    monitor.report("Finding dangling edges");
    dangles = graph.deleteDangles();
    monitor.report("Finding cut edges");
    cutEdges = graph.deleteCutEdges();
    monitor.report("Building rings");
    List edgeRingList = graph.getEdgeRings();

    monitor.report("Checking valid rings");
    List validEdgeRingList = new ArrayList();
    invalidRingLines = new ArrayList();
    findValidRings(edgeRingList, validEdgeRingList, invalidRingLines);

    monitor.report("Finding holes");
    findShellsAndHoles(validEdgeRingList);
    assignHolesToShells(holeList, shellList);

    // TODO: create minimal edgerings

    monitor.report("Building polygons");
    polyList = new ArrayList();
    for (Iterator i = shellList.iterator(); i.hasNext(); ) {
      EdgeRing er = (EdgeRing) i.next();
      polyList.add(er.getPolygon());
    }

  }

  private void findValidRings(List edgeRingList, List validEdgeRingList, List invalidRingList)
  {
    for (Iterator i = edgeRingList.iterator(); i.hasNext(); ) {
      EdgeRing er = (EdgeRing) i.next();
      if (er.isValid())
        validEdgeRingList.add(er);
      else
        invalidRingList.add(er.getLineString());
    }
  }

  private void findShellsAndHoles(List edgeRingList)
  {
    holeList = new ArrayList();
    shellList = new ArrayList();
    for (Iterator i = edgeRingList.iterator(); i.hasNext(); ) {
      EdgeRing er = (EdgeRing) i.next();
      if (er.isHole())
        holeList.add(er);
      else
        shellList.add(er);

    }
  }

  private static void assignHolesToShells(List holeList, List shellList)
  {
    for (Iterator i = holeList.iterator(); i.hasNext(); ) {
      EdgeRing holeER = (EdgeRing) i.next();
      assignHoleToShell(holeER, shellList);
    }
  }

  private static void assignHoleToShell(EdgeRing holeER, List shellList)
  {
    EdgeRing shell = EdgeRing.findEdgeRingContaining(holeER, shellList);
    if (shell != null)
      shell.addHole(holeER.getRing());
  }


}
