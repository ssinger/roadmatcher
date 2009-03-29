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

package com.vividsolutions.jcs.plugin.qa;

import java.awt.Color;
import javax.swing.*;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.ColorUtil;
import com.vividsolutions.jump.util.feature.*;
import com.vividsolutions.jcs.qa.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;

public class CloseVertexFinderPlugIn
    extends ThreadedBasePlugIn
{
  private final static String LAYER1 = "Layer 1";
  private final static String LAYER2 = "Layer 2";
  private final static String DIST_TOL = "Distance Tolerance";
  private final static String CREATE_NEW_LAYERS = "Create New Layers";

  private Layer layer1, layer2;
  private double distanceTolerance = 1.0;
  private boolean createNewLayers;

  public CloseVertexFinderPlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
          this, "QA", "Find Close Vertices...", null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Find Close Vertices", true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (!dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context)
       throws Exception
  {
    monitor.allowCancellationRequests();
    compute(monitor, context);
  }

  private void compute(TaskMonitor monitor, PlugInContext context)
  {
    CloseVertexFinder finder = new CloseVertexFinder(
        layer1.getFeatureCollectionWrapper(),
        layer2.getFeatureCollectionWrapper(),
        distanceTolerance
        );
    finder.compute(monitor);
    FeatureCollection indicators = finder.getIndicators();

    if (monitor.isCancelRequested()) return;
    if (indicators.size() > 0) {
      createLayers(context, indicators);
    }
    createOutput(context, indicators);
  }

  private void createOverlapLayer(PlugInContext context,
                                  Layer inputLayer,
                                  FeatureCollection overlaps, Color fillColor)
  {
    String overlapLayerName = "Overlaps " + inputLayer.getName();
    Layer lyr;
    if (createNewLayers) {
      lyr = context.addLayer(StandardCategoryNames.QA,
                             overlapLayerName + " - " + inputLayer.getName(), overlaps);
    }
    else {
      lyr = context.getLayerManager().addOrReplaceLayer(StandardCategoryNames.QA,
          overlapLayerName, overlaps);
    }
    lyr.getBasicStyle().setFillColor(fillColor);
    lyr.getBasicStyle().setLineColor(fillColor.darker());
    lyr.fireAppearanceChanged();
    lyr.setDescription("Overlaps for " + inputLayer.getName());
  }

  private void createLayers(PlugInContext context,
                            FeatureCollection indicators)
  {

      String overlapSizeLayerName = "Close Vertex Indicators";
      Layer lyr3;
      if (createNewLayers) {
        lyr3 = context.addLayer(StandardCategoryNames.QA,
                                overlapSizeLayerName, indicators);
      }
      else {
        lyr3 = context.getLayerManager().addOrReplaceLayer(StandardCategoryNames.QA,
            overlapSizeLayerName, indicators);
      }
      LayerStyleUtil.setLinearStyle(lyr3, Color.blue, 2, 4);
      lyr3.fireAppearanceChanged();
      lyr3.setDescription("Close Vertex Indicators (distance tolerance = " + distanceTolerance + ")" );

  }

  private void createOutput(PlugInContext context,
                            FeatureCollection indicators)
    {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1, "Overlaps");
    context.getOutputFrame().addField("Layer 1: ", layer1.getName() );
    context.getOutputFrame().addField("Layer 2: ", layer2.getName() );
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField(
        "# pairs of Close Vertices: ", "" + indicators.size());

    double[] minMax = FeatureStatistics.minMaxValue(indicators, "LENGTH");
    context.getOutputFrame().addField(
        "Min Overlap Size: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Overlap Size: ", "" + minMax[1]);

  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("CoverageOverlap.png")));
    dialog.setSideBarDescription("Finds all pairs of vertices in two datasets which are closer than a given tolerance."
    );
    String fieldName = LAYER1;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getLayerManager().getLayer(0), null, context.getLayerManager());
    String fieldName1 = LAYER2;
    JComboBox addLayerComboBox1 = dialog.addLayerComboBox(fieldName1, context.getLayerManager().getLayer(1), null, context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, distanceTolerance, 8, "The Distance Tolerance is the maximum size of the gap between unaligned segments");
    dialog.addCheckBox(CREATE_NEW_LAYERS, false, "Create new layers for the output");
  }

  private void getDialogValues(MultiInputDialog dialog)
  {
    layer1 = dialog.getLayer(LAYER1);
    layer2 = dialog.getLayer(LAYER2);
    distanceTolerance = dialog.getDouble(DIST_TOL);
   createNewLayers = dialog.getBoolean(CREATE_NEW_LAYERS);
  }

}
