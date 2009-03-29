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
package com.vividsolutions.jcs.qa.offsetcorner;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jcs.qa.NearFeatureFinder;

/**
 * Finds boundary corners which are offset by a small distance.
 */
public class OffsetBoundaryCornerFinder
{

  public static class Parameters
  {
    /**
     * The proximity required for boundary items
     */
    public double boundaryDistanceTolerance = .01;
    /**
     * The maximum length of an offset segment.
     */
    public double offsetTolerance = 1.0;
    /**
     * The maximum corner angle (in degrees)
     */
    public double maxCornerAngle = 140.0;
  }


  private static final GeometryFactory geomFactory = new GeometryFactory();

  // input data
  private FeatureCollection[] inputFC = new FeatureCollection[2];
  private Parameters param;
  // working data
  private List shellList[] = new List[2];
  // output data
  //private List offsetIndicators = new ArrayList();
  private FeatureCollection offsetIndicatorsFC;

  public OffsetBoundaryCornerFinder(
        FeatureCollection referenceFC,
        FeatureCollection subjectFC,
        Parameters param)
  {
    inputFC[0] = referenceFC;
    inputFC[1] = subjectFC;
    this.param = param;
    //compute();
  }

  public FeatureCollection getOffsetIndicators()
  {
    return offsetIndicatorsFC;
  }

  public void compute(TaskMonitor monitor)
  {
    monitor.report("Finding boundary features");
    NearFeatureFinder nff = new NearFeatureFinder();

    FeatureCollection[] boundaryFC = new FeatureCollection[2];
    boundaryFC[0] = nff.getNearFeatures(inputFC[0], inputFC[1], param.boundaryDistanceTolerance);
    boundaryFC[1] = nff.getNearFeatures(inputFC[1], inputFC[0], param.boundaryDistanceTolerance);

    monitor.report("Finding boundary corners");
    initShellLists(boundaryFC);

    Collection[] bdyCornerCoord = new Collection[2];
    bdyCornerCoord[0] = findBoundaryCorners(shellList[0]);
    bdyCornerCoord[1] = findBoundaryCorners(shellList[1]);
    Collection[] cornerCoord = new Collection[2];
    cornerCoord[0] = findCorners(shellList[0]);
    cornerCoord[1] = findCorners(shellList[1]);

    monitor.report("Finding offsets");
    // find all unique offset segments
    Set offsetSegments = new TreeSet();
    findOffsets(
        cornerCoord[0],
        bdyCornerCoord[1],
        offsetSegments);
    findOffsets(
        cornerCoord[1],
        bdyCornerCoord[0],
        offsetSegments);

    offsetIndicatorsFC = computeIndicatorsFC(offsetSegments);
  }

  private FeatureCollection computeIndicatorsFC(Collection offsetSegments)
  {
    Collection offsetLines = new ArrayList();
    for (Iterator i = offsetSegments.iterator(); i.hasNext(); ) {
      LineSegment seg = (LineSegment) i.next();
      Geometry indicator = geomFactory.createLineString(
          new Coordinate[] { seg.p0, seg.p1 } );
      offsetLines.add(indicator);
    }
    return FeatureDatasetFactory.createFromGeometryWithLength(offsetLines, "LENGTH");
  }
  private void initShellLists(FeatureCollection[] boundaryFC)
  {
    shellList[0] = createBoundaryShellList(boundaryFC[0]);
    shellList[1] = createBoundaryShellList(boundaryFC[1]);

    computeBoundaryShellMatches(0, 1);
    computeBoundaryShellMatches(1, 0);
  }

  /**
   * Computes matches between shells in different datasets
   */
  private void computeBoundaryShellMatches(int index0, int index1)
  {
    for (int i = 0; i < shellList[index0].size(); i++) {
      for (int j = 0; j < shellList[index1].size(); j++) {
        BoundaryShell shell0 = (BoundaryShell) shellList[index0].get(i);
        BoundaryShell shell1 = (BoundaryShell) shellList[index1].get(j);
        shell0.checkBoundary(shell1, param.boundaryDistanceTolerance);
      }
    }
  }
  private List createBoundaryShellList(FeatureCollection polys)
  {
    List shells = new ArrayList();
    for (Iterator i = polys.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      Polygon poly = (Polygon) f.getGeometry();
      BoundaryShell shell = new BoundaryShell(poly);
      shell.computeCorners(param.maxCornerAngle);
      shells.add(shell);
    }
    return shells;
  }

  private Collection findBoundaryCorners(Collection shells)
  {
    Collection cornerPts = new TreeSet();
    for (Iterator i = shells.iterator(); i.hasNext(); ) {
      BoundaryShell shell = (BoundaryShell) i.next();
      cornerPts.addAll(shell.getBoundaryCornerVertices());
    }
    return cornerPts;
  }

  private Collection findCorners(Collection shells)
  {
    Collection cornerPts = new TreeSet();
    for (Iterator i = shells.iterator(); i.hasNext(); ) {
      BoundaryShell shell = (BoundaryShell) i.next();
      cornerPts.addAll(shell.getCornerVertices());
    }
    return cornerPts;
  }

  private void findOffsets(Collection refPts, Collection subPts, Collection offsetSegments)
  {
    for (Iterator i = refPts.iterator(); i.hasNext(); ) {
      Coordinate coord = (Coordinate) i.next();
      Coordinate closestPoint = findClosestPointWithinDistance(coord, subPts, param.offsetTolerance);
      if (closestPoint != null
          && ! coord.equals(closestPoint)
          && coord.distance(closestPoint) <= param.offsetTolerance
          ) {
        /*
        Geometry indicator = geomFactory.createLineString(
            new Coordinate[] { new Coordinate(coord), new Coordinate(closestPoint) } );
        */
        LineSegment seg = new LineSegment(new Coordinate(coord), new Coordinate(closestPoint));
        seg.normalize();
        offsetSegments.add(seg);
      }
    }
  }

  /**
   * Finds closest point within a given distance of a given point.
   * This method uses a brute-force approach, but this query could
   * be more efficiently performed using an index.
   *
   * @param pt
   * @param ptList
   * @param maxDistance
   * @return the closest point within the given distance, if any
   *         <code>null</code> if no such point exists
   */
  private static Coordinate findClosestPointWithinDistance(Coordinate pt, Collection ptList, double maxDistance)
  {
    Coordinate closestPt = null;
    double closestDistance = maxDistance;
    for (Iterator i = ptList.iterator(); i.hasNext(); ) {
      Coordinate testPt = (Coordinate) i.next();
      double dist = pt.distance(testPt);
      if (dist <= maxDistance) {
        closestPt = testPt;
        closestDistance = dist;
      }
    }
    return closestPt;
  }
}
