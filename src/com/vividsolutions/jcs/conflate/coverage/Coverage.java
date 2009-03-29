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
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.debug.Debug;

public class Coverage
{
  private Map featureMap = new TreeMap(new FeatureUtil.IDComparator());
  private VertexMap vertexMap = new VertexMap();
  private FeatureCollection features;
  private FeatureUpdateRecorder updates =  new FeatureUpdateRecorder();
  private FeatureCollection adjustedFC;

  public Coverage(FeatureCollection features)
  {
    this.features = features;
  }

  public FeatureCollection getFeatures()  { return features; }
  public FeatureCollection getAdjustedFeatures()  { return adjustedFC; }
  public FeatureUpdateRecorder getUpdates()   {    return updates;    }

  public FeatureCollection getAdjustmentIndicators()
  {
    GeometryFactory fact = new GeometryFactory();
    List indicatorLineList = new ArrayList();
    Collection vertices = vertexMap.getVertices();
    for (Iterator i = vertices.iterator(); i.hasNext(); ) {
      Vertex v = (Vertex) i.next();
      if (v.isAdjusted()) {
        Coordinate[] lineSeg = new Coordinate[] { v.getOriginalCoordinate(), v.getAdjustedCoordinate() };
        Geometry line = fact.createLineString(lineSeg);
        indicatorLineList.add(line);
      }
    }
    return FeatureDatasetFactory.createFromGeometryWithLength(indicatorLineList, "LENGTH");
  }

  public CoverageFeature getCoverageFeature(Feature f)
  {
    CoverageFeature cgf = (CoverageFeature) featureMap.get(f);
    if (cgf == null) {
      cgf = new CoverageFeature(f, vertexMap);
      featureMap.put(f, cgf);
    }
    return cgf;
  }

  public List getCoverageFeatureList(List featureList)
  {
    List cgfList = new ArrayList();
    for (Iterator i = featureList.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      // currently only polygons are handled
      if (f.getGeometry() instanceof Polygon)
        cgfList.add(getCoverageFeature(f));
    }
    return cgfList;
  }

  public void computeAdjustedFeatureUpdates()
  {
    adjustedFC = new FeatureDataset(features.getFeatureSchema());
    Collection cgfColl = featureMap.values();
    for (Iterator i = cgfColl.iterator(); i.hasNext(); ) {
      CoverageFeature cgf = (CoverageFeature) i.next();
      if (cgf.isAdjusted()) {
        Geometry g = cgf.getAdjustedGeometry();
        // don't update geometry if it's not valid
        if (g.isValid()) {
          Feature originalFeat = cgf.getFeature();
          Feature f = originalFeat.clone(false);
          f.setGeometry(g);
          adjustedFC.add(f);
          // record this feature as an update to the original
          updates.update(cgf.getFeature(), f);
        }
        else {
          // debugging only
          Debug.println("Invalid polygon");
          Debug.println(g);
        }
      }
    }
  }

}
