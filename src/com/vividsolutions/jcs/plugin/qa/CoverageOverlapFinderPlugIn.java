

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

public class CoverageOverlapFinderPlugIn
    extends ThreadedBasePlugIn
{
  private final static String LAYER = "Layer";
  private final static String CREATE_NEW_LAYERS = "Create New Layers";

  private Layer layer;
  private boolean createNewLayers;

  public CoverageOverlapFinderPlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
          this, "QA", "Find Coverage Overlaps...", null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Find Coverage Overlaps", true);
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

    monitor.report("Finding Overlaps...");
    computeOverlaps(monitor, context);
  }

  private void computeOverlaps(TaskMonitor monitor, PlugInContext context)
  {
    InternalOverlapFinder ovf = new InternalOverlapFinder(
        layer.getFeatureCollectionWrapper() );
    FeatureCollection overlaps = ovf.getOverlappingFeatures();
    FeatureCollection overlapInd = ovf.getOverlapIndicators();
    FeatureCollection overlapSizeInd = ovf.getOverlapSizeIndicators();

    // need to sort out what happens if overlap layers exist already before doing this
    //if (overlaps.size() <= 0) return;
    if (monitor.isCancelRequested()) return;

    if (overlaps.size() > 0) {
      createLayers(context, overlaps, overlapInd, overlapSizeInd);
    }
    createOutput(context, overlaps, overlapSizeInd);
  }

  private void createLayers(PlugInContext context,
                            FeatureCollection overlaps,
                            FeatureCollection overlapInd,
                            FeatureCollection overlapSizeInd)
  {
    String overlapLayerName = "Overlaps";
    Layer lyr;
    if (createNewLayers) {
      lyr = context.addLayer(StandardCategoryNames.QA,
                             overlapLayerName + " - " + layer.getName(), overlaps);
    }
    else {
      lyr = context.getLayerManager().addOrReplaceLayer(StandardCategoryNames.QA,
          overlapLayerName, overlaps);
    }
    lyr.getBasicStyle().setFillColor(ColorUtil.GOLD);
    lyr.getBasicStyle().setLineColor(ColorUtil.GOLD.darker());
    lyr.fireAppearanceChanged();
    lyr.setDescription("Overlaps for " + layer.getName());

    String overlapSegLayerName = "Overlap Segments";
    Layer lyr2;
    if (createNewLayers) {
      lyr2 = context.addLayer(StandardCategoryNames.QA,
                              overlapSegLayerName + " - " + layer.getName(), overlapInd);
    }
    else {
      lyr2 = context.getLayerManager().addOrReplaceLayer(StandardCategoryNames.QA,
          overlapSegLayerName, overlapInd);
    }
    LayerStyleUtil.setLinearStyle(lyr2, Color.red, 2, 4);
    lyr2.fireAppearanceChanged();
    lyr2.setDescription("Overlap Segments for " + layer.getName());

    String overlapSizeLayerName = "Overlap Size";
    Layer lyr3;
    if (createNewLayers) {
      lyr3 = context.addLayer(StandardCategoryNames.QA,
                              overlapSizeLayerName + " - " + layer.getName(), overlapSizeInd);
    }
    else {
      lyr3 = context.getLayerManager().addOrReplaceLayer(StandardCategoryNames.QA,
          overlapSizeLayerName, overlapSizeInd);
    }
    LayerStyleUtil.setLinearStyle(lyr3, Color.blue, 2, 4);
    lyr3.fireAppearanceChanged();
    lyr3.setDescription("Overlap Size Indicators for " + layer.getName());
  }
  private void createOutput(PlugInContext context,
      FeatureCollection overlaps,
      FeatureCollection overlapSizeInd)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1, "Coverage Overlaps");
    context.getOutputFrame().addField("Layer: ", layer.getName() );
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField(
        "# Overlapping Features: ", "" + overlaps.size());

    double[] minMax = FeatureStatistics.minMaxValue(overlapSizeInd, "LENGTH");
    context.getOutputFrame().addField(
        "Min Overlap Size: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Overlap Size: ", "" + minMax[1]);

  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("CoverageOverlap.png")));
    dialog.setSideBarDescription("Finds all polygons in a coverage which overlap."
    );
    String fieldName = LAYER;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
    dialog.addCheckBox(CREATE_NEW_LAYERS, false, "Create new layers for the output");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer = dialog.getLayer(LAYER);
    createNewLayers = dialog.getBoolean(CREATE_NEW_LAYERS);
  }

}
