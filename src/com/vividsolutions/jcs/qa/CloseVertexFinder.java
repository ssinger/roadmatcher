/*
 *  The JCS Conflation Suite (JCS) is a library of Java classes that
 *  can be used to build automated or semi-automated conflation solutions.
 *
 *  Copyright (C) 2002 Vivid Solutions
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  For more information, contact:
 *
 *  Vivid Solutions
 *  Suite #1A
 *  2328 Government Street
 *  Victoria BC  V8T 5G5
 *  Canada
 *
 *  (250)385-6040
 *  jcs.vividsolutions.com
 */

package com.vividsolutions.jcs.qa;

import java.util.*;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;

/**
 * Finds vertices which are close but not equal, in one or two datasets.
 */
public class CloseVertexFinder {

  private GeometryFactory geomFactory = new GeometryFactory();

  private FeatureCollection[] inputFC;
  private double distanceTolerance;
  private FeatureCollection nearIndicatorFC;
  private List nearIndicators = new ArrayList();// a list of Geometry's
  private boolean isComputed = false;

  public CloseVertexFinder(FeatureCollection fc0, FeatureCollection fc1, double distanceTolerance)
  {
    this.inputFC = new FeatureCollection[2];
    this.inputFC[0] = fc0;
    this.inputFC[1] = fc1;
    this.distanceTolerance = distanceTolerance;
  }

  public FeatureCollection getIndicators()
  {
    return nearIndicatorFC;
  }

  public void compute(TaskMonitor monitor)
  {
    if (isComputed) return;
    monitor.allowCancellationRequests();

    FeatureCollection queryFC = inputFC[0];
    monitor.report("Building feature index");
    FeatureCollection indexFC = new IndexedFeatureCollection(inputFC[1]);
    int totalSegments = queryFC.size();
    int count = 0;
    monitor.report("Finding near vertices");
    for (Iterator i = queryFC.iterator(); i.hasNext(); ) {

      monitor.report(++count, totalSegments, "features");

      Feature queryFeat = (Feature) i.next();
      Envelope queryEnv = EnvelopeUtil.expand(queryFeat.getGeometry().getEnvelopeInternal(), distanceTolerance);
      List closeFeat = indexFC.query(queryEnv);
      for (Iterator j = closeFeat.iterator(); j.hasNext(); ) {
        Feature closeF = (Feature) j.next();
        findNearVertices(queryFeat.getGeometry(), closeF.getGeometry());
      }
    }
    nearIndicatorFC = FeatureDatasetFactory.createFromGeometryWithLength(nearIndicators, "LENGTH");

    isComputed = true;
  }

  private void findNearVertices(Geometry g0, Geometry g1)
  {
    Coordinate[] pts0 = g0.getCoordinates();
    Coordinate[] pts1 = g1.getCoordinates();
    for (int i = 0; i < pts0.length; i++) {
      for (int j = 0; j < pts1.length; j++) {
        if (pts0[i].equals(pts1[j]))
            continue;
        if (pts0[i].distance(pts1[j]) < distanceTolerance)
          addNearVertices(pts0[i], pts1[j]);
      }
    }
  }

  private void addNearVertices(Coordinate p0, Coordinate p1)
  {
    nearIndicators.add(geomFactory.createLineString(new Coordinate[] {p0, p1} ));
  }

}
