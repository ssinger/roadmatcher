

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

package com.vividsolutions.jcs.conflate.coverage;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Debug;
/**
 * Represents a single line segment from the edge of a shell of a Polygon in a coverage.
 * Maintains a list of added Vertexes added during a conflation/cleaning process.
 */
public class Segment
    extends GeometryComponent
{

  private Shell shell;
  private Vertex vertex[] = new Vertex[2];
  private LineSegment seg;
  //private double distanceTolerance;
  private List matchedVertexList = new ArrayList();
  private List insertedCoordList = null;

  public Segment(Vertex v0, Vertex v1, Shell shell)
  {
    vertex[0] = v0;
    vertex[1] = v1;
    this.shell = shell;
    seg = new LineSegment(vertex[0].getCoordinate(), vertex[1].getCoordinate());
    double segLen = v0.getCoordinate().distance(v1.getCoordinate());
    vertex[0].setMinimumAdjustmentTolerance(segLen / 2);
    vertex[1].setMinimumAdjustmentTolerance(segLen / 2);
  }

  public Vertex getVertex(int i) { return vertex[i]; }
  public LineSegment getLineSegment() { return seg; }
  public List getInsertedCoordinates()
  {
    if (insertedCoordList == null)
      computeInserted();
    return insertedCoordList;
  }

  private double[] vertexDistances(Vertex v)
  {
    double[] dist = new double[2];
    dist[0] = v.getCoordinate().distance(vertex[0].getCoordinate());
    dist[1] = v.getCoordinate().distance(vertex[1].getCoordinate());
    return dist;
  }
  /**
   * Adjusts this Segment to match another Segment seg.
   * @param seg the segment to be adjusted to
   */
  public void addMatchedSegment(Segment matchSeg, double distanceTolerance)
  {
//Debug.println("matching " + matchSeg + "to " + this);
    addMatchedVertex(matchSeg.vertex[0], distanceTolerance);
    addMatchedVertex(matchSeg.vertex[1], distanceTolerance);
  }

  /**
   * Adds a vertex of a segment which matches this segment.
   *
   * @param v the Vertex to be added
   */
  public void addMatchedVertex(Vertex v, double distanceTolerance)
  {
    double[] dist = vertexDistances(v);
    /* TESTING - do we need this check?
    if (   (dist[0] < distanceTolerance && dist[1] < distanceTolerance) ) {
      setConflict(true);
      return;
    }
    */
    boolean isSnapped = false;
    if (dist[0] < distanceTolerance) {
      isSnapped = v.snap(vertex[0]);
    }
    else if (dist[1] < distanceTolerance) {
      isSnapped = v.snap(vertex[1]);
    }
    /**
     * If the vertex wasn't snapped, insert it into this segment.
     * <TODO:> should we check that the vertex isn't too far from the segment?
     * Probably a vertex shouldn't be inserted if it is more than segLen/2 away
     * from the segment, since this would distort the segment too much
     */
    if (! isSnapped) {
      matchedVertexList.add(new MatchedVertex(v));
    }
  }
  /**
   * Adds a snapped vertex to this segment.
   *
   * @param pt the coordinate to be added
   */
  public void addVertex(Coordinate pt)
  {

  }

  private void computeInserted()
  {
    // compute position of matched vertices, taking into acccount any adjustments to the underlying vertex
    for (Iterator j = matchedVertexList.iterator(); j.hasNext(); ) {
      MatchedVertex mv = (MatchedVertex) j.next();
      mv.computePosition(this.getLineSegment());
    }
    // sort added vertices in order along segment
    Collections.sort(matchedVertexList);

    // insert any vertices whose position
    // lies in the interior of this segment
    insertedCoordList = new ArrayList();
    Coordinate prevCoord = null;
    for (Iterator i = matchedVertexList.iterator(); i.hasNext(); ) {
      MatchedVertex mv = (MatchedVertex) i.next();
      Coordinate coord = mv.getVertex().getCoordinate();

      // prevent duplicate coordinates
      if (prevCoord != null && coord.equals(prevCoord))
        continue;
      prevCoord = coord;

      if (mv.getPosition() > 0.0 && mv.getPosition() < 1.0) {
        insertedCoordList.add(coord);
      }
    }
  }

  public String toString()
  {
    return "[" + vertex[0].getOriginalCoordinate() + " - " + vertex[1].getOriginalCoordinate() + "]";
  }
}
