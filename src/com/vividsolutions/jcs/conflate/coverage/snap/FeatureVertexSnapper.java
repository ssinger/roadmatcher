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
package com.vividsolutions.jcs.conflate.coverage.snap;

import java.util.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jump.task.*;

public class FeatureVertexSnapper {

  private FeatureCollection refFC;
  private FeatureCollection subjectFC;
  private FeatureCollection adjustedFC;
  private double distanceTolerance;
  private FeatureUpdateRecorder updates =  new FeatureUpdateRecorder();
  private int updateCount = 0;
  private GeometryFactory fact = new GeometryFactory();
  private List snappedVertexList = new ArrayList();

  public FeatureVertexSnapper(FeatureCollection refFC,
                              FeatureCollection subjectFC,
                              double distanceTolerance)
  {
    this.refFC = refFC;
    this.subjectFC = subjectFC;
    this.distanceTolerance = distanceTolerance;
  }

  public int getUpdateCount()
  {
    return updateCount;
  }

  public FeatureCollection process(TaskMonitor monitor)
  {
    adjustedFC = new FeatureDataset(subjectFC.getFeatureSchema());
    FeatureCollection indexFC = new IndexedFeatureCollection(refFC);
    int totalSegments = subjectFC.size();
    int count = 0;
    for (Iterator i = subjectFC.iterator(); i.hasNext(); ) {
      monitor.report(++count, totalSegments, "features");

      Feature f = (Feature) i.next();
      Envelope fEnv = f.getGeometry().getEnvelopeInternal();
      Envelope searchEnv = EnvelopeUtil.expand(fEnv, distanceTolerance);
      List closeFeatures = indexFC.query(searchEnv);

      snapFeature(f, closeFeatures);
    }
    updateCount = updates.getCount();

    return updates.applyUpdates(subjectFC);
  }
  public FeatureCollection getAdjustedFeatures()  { return adjustedFC; }

  public FeatureCollection getAdjustmentIndicators()
  {
    GeometryFactory fact = new GeometryFactory();
    List indicatorLineList = new ArrayList();
    for (Iterator i = snappedVertexList.iterator(); i.hasNext(); ) {
      Coordinate[] origCoord = (Coordinate[]) i.next();
      Coordinate[] lineSeg = new Coordinate[] {
        new Coordinate(origCoord[0]),
        new Coordinate(origCoord[1]),
        };
      Geometry line = fact.createLineString(lineSeg);
      indicatorLineList.add(line);
    }
    return FeatureDatasetFactory.createFromGeometryWithLength(indicatorLineList, "LENGTH");
  }

  private void snapFeature(Feature f, List closeFeatures)
  {
    SlowPointIndex ptIndex = new SlowPointIndex();
    ptIndex.add(closeFeatures);
    CoordinateSnapper coordSnapper = new CoordinateSnapper(ptIndex);
    Geometry geom = f.getGeometry();
    GeometryVertexSnapper snapper = new GeometryVertexSnapper(geom, coordSnapper, distanceTolerance);
    Geometry newGeom = snapper.getResult();
    if (snapper.isModified()) {
      snappedVertexList.addAll(snapper.getSnappedVertices());

      // don't update geometry if it's not valid
      if (newGeom.isValid()) {
        Feature newFeat = f.clone(false);
        newFeat.setGeometry(newGeom);
        // record this feature as an update to the original
        updates.update(f, newFeat);
        adjustedFC.add(f);
      }
    }
  }

}
