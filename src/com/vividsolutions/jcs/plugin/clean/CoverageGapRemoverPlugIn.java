

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

package com.vividsolutions.jcs.plugin.clean;

import java.awt.Color;

import javax.swing.JComboBox;

import com.vividsolutions.jcs.conflate.coverage.CoverageGapRemover;
import com.vividsolutions.jump.util.feature.FeatureStatistics;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.ui.*;

public class CoverageGapRemoverPlugIn
    extends ThreadedBasePlugIn
{

  private final static String LAYER = "Layer";
  private final static String DIST_TOL = "Distance Tolerance";
  private final static String ANGLE_TOL = "Angle Tolerance";

  private Layer layer;
  private CoverageGapRemover.Parameters param
      = new CoverageGapRemover.Parameters();

  public CoverageGapRemoverPlugIn() { }

  /**
   * Returns a very brief description of this task.
   * @return the name of this task
   */
  public String getName() { return "Coverage Gap Remover"; }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addLayerNameViewMenuItem(
          this, "Clean", "Remove Coverage Gaps...");
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Remove Coverage Gaps", true);
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

    CoverageGapRemover remover = new CoverageGapRemover(layer.getFeatureCollectionWrapper(), monitor);

    monitor.report("Removing Gaps...");
    remover.process(param);

    if (monitor.isCancelRequested()) return;
    createLayers(context, remover);
  }

  private void createLayers(PlugInContext context, CoverageGapRemover remover)
         throws Exception
  {
    context.addLayer(
        StandardCategoryNames.RESULT_SUBJECT,
        layer.getName(),
        remover.getUpdatedFeatures());

    FeatureCollection adjustedFC = remover.getAdjustedFeatures();
    Layer lyr = context.addLayer(
        StandardCategoryNames.QA,
        "Adjusted-" + layer.getName(),
        adjustedFC);
    lyr.setDescription("Adjusted features for " + layer.getName() + " (Distance Tol = " + param.distanceTolerance + ")");

    FeatureCollection adjustmentIndFC = remover.getAdjustmentIndicators();
    Layer lyr2 = context.addLayer(
        StandardCategoryNames.QA,
        "Adjustment-" + layer.getName(),
        adjustmentIndFC);
    LayerStyleUtil.setLinearStyle(lyr2, Color.blue, 2, 4);
    lyr2.fireAppearanceChanged();
    lyr2.setDescription("Adjustment Size Indicators for " + layer.getName() + " (Distance Tol = " + param.distanceTolerance + ")");

    createOutput(context, adjustedFC, adjustmentIndFC);

  }

  private void createOutput(PlugInContext context,
      FeatureCollection adjustedFC,
      FeatureCollection adjustmentIndFC)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1,
        "Coverage Gap Removal");
    context.getOutputFrame().addField("Layer: ", layer.getName() );
    context.getOutputFrame().addField("Distance Tolerance: ", "" + param.distanceTolerance);

    context.getOutputFrame().addHeader(2, "Adjustments");
    context.getOutputFrame().addField(
        "# Features Adjusted: ", "" + adjustedFC.size());
    context.getOutputFrame().addField(
        "# Vertices Adjusted: ", "" + adjustmentIndFC.size());

    double[] minMax = FeatureStatistics.minMaxValue(adjustmentIndFC, "LENGTH");
    context.getOutputFrame().addField(
        "Min Adjustment Size: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Adjustment Size: ", "" + minMax[1]);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarDescription("Removes all gaps or slivers in a coverage "
        + "which are narrower than the distance tolerance."
    );
    String fieldName = LAYER;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, param.distanceTolerance, 8, "The Distance Tolerance determines how large gaps and overlaps can be");
    dialog.addDoubleField(ANGLE_TOL, param.angleTolerance, 4, "The Angle Tolerance (in degrees) controls how parallel matched segments must be");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer = dialog.getLayer(LAYER);
    param.distanceTolerance = dialog.getDouble(DIST_TOL);
    param.angleTolerance = dialog.getDouble(ANGLE_TOL);
  }
}
