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

package com.vividsolutions.jcs.plugin.test;

import java.awt.Color;
import javax.swing.*;
import java.util.*;
import com.vividsolutions.jcs.algorithm.VertexHausdorffDistance;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.feature.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;
import com.vividsolutions.jts.geom.*;

/**
 * Computes the Hausdorff distance between pairs of Geometries in a layer
 */
public class VertexHausdorffDistancePlugIn
    extends AbstractPlugIn
{

  public VertexHausdorffDistancePlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
          this, new String[] {"Conflate", "Test"}, "Vertex Hausdorff Distance", false, null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1))
          );
  }

  public boolean execute(PlugInContext context)
       throws Exception
  {
    computeMatchedSegments(context);
    return true;
  }

  private void computeMatchedSegments(PlugInContext context)
  {
    Layer layer = context.getCandidateLayer(0);
    List fcList = layer.getFeatureCollectionWrapper().getFeatures();

    Geometry g0 = ((Feature) fcList.get(0)).getGeometry();
    Geometry g1 = ((Feature) fcList.get(1)).getGeometry();

    VertexHausdorffDistance hDist = new VertexHausdorffDistance(g0, g1);
    Coordinate[] coord = hDist.getCoordinates();
    GeometryFactory fact = new GeometryFactory();
    LineString dist = fact.createLineString(coord);

    List resultList = new ArrayList();
    resultList.add(dist);
    FeatureCollection resultFC = FeatureDatasetFactory.createFromGeometryWithLength(resultList, "LENGTH");

    context.addLayer(StandardCategoryNames.QA,
          "Vertex Hausdorff Distance",
          resultFC);
  }


}
