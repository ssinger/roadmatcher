

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

package com.vividsolutions.jcs.plugin.conflate;

import java.awt.Color;
import com.vividsolutions.jcs.conflate.coverage.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.feature.FeatureStatistics;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;
import com.vividsolutions.jump.util.ColorUtil;
import javax.swing.*;

public class DiscreteFeatureCoverageAlignerPlugIn
    extends ThreadedBasePlugIn
{

  private final static String REF_LAYER = "Reference Layer";
  private final static String SUB_LAYER = "Subject Layer";
  private final static String DIST_TOL = "Distance Tolerance";
  private final static String ANG_TOL = "Angle Tolerance";

  private CoverageAligner.Parameters param = new CoverageAligner.Parameters();
  private Layer refLyr, subLyr;

  public DiscreteFeatureCoverageAlignerPlugIn() { }

  public String getName() { return "Discrete Feature Coverage Aligner"; }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this, new String[] {"Conflate"},
        "Discrete Feature Coverage Alignment...", false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), getName(), true);
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

    DiscreteFeatureCoverageAligner cvgAligner = new DiscreteFeatureCoverageAligner(
        refLyr.getFeatureCollectionWrapper(),
        subLyr.getFeatureCollectionWrapper(), monitor);

    cvgAligner.process(param);
    FeatureCollection updatedFC = cvgAligner.getUpdatedFeatures();
    FeatureCollection adjustedFC = cvgAligner.getAdjustedFeatures();
    FeatureCollection adjustmentIndFC = cvgAligner.getAdjustmentIndicators();
    createLayers(context, updatedFC, adjustedFC, adjustmentIndFC);
  }

  private void createLayers(PlugInContext context,
                            FeatureCollection updatedFC,
                            FeatureCollection adjustedFC,
                            FeatureCollection adjustmentIndFC
                            )
         throws Exception
  {
    context.addLayer(StandardCategoryNames.RESULT_SUBJECT, subLyr.getName(),
        updatedFC);

    Layer lyr = context.addLayer(
        StandardCategoryNames.QA,
        "Adjusted-" + subLyr.getName(),
        adjustedFC);
    lyr.setSynchronizingLineColor(false);
    lyr.getBasicStyle().setFillColor( ColorUtil.GOLD);
    lyr.getBasicStyle().setLineColor(Color.red);
    lyr.getBasicStyle().setRenderingFill(true);
    lyr.getBasicStyle().setAlpha(255);
    lyr.fireAppearanceChanged();
    lyr.setDescription("Adjusted features for " + subLyr.getName() + " (Distance Tol = " + param.distanceTolerance + ")");

    Layer lyr2 = context.addLayer(
        StandardCategoryNames.QA,
        "Adjustment-" + subLyr.getName(),
        adjustmentIndFC);
    LayerStyleUtil.setLinearStyle(lyr2, Color.blue, 2, 4);
    lyr2.fireAppearanceChanged();
    lyr2.setDescription("Adjustment Size Indicators for " + subLyr.getName() + " (Distance Tol = " + param.distanceTolerance + ")");

    createOutput(context, adjustedFC, adjustmentIndFC);

  }

  private void createOutput(PlugInContext context,
                            FeatureCollection adjustedFC,
                            FeatureCollection adjustmentIndFC)
         throws Exception
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1,
        "Coverage Alignment");
    context.getOutputFrame().addField(
        "Reference Layer: ", refLyr.getName() );
    context.getOutputFrame().addField(
        "Subject Layer: ", subLyr.getName() );
    context.getOutputFrame().addField(
        "Distance Tolerance: ", "" + param.distanceTolerance);
    context.getOutputFrame().addField(
        "Angle Tolerance: ", "" + param.angleTolerance);

    context.getOutputFrame().addText(" ");
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
    dialog.setTitle(getName());
    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("BoundaryMatch.gif")));
    dialog.setSideBarDescription("Aligns a feature dataset to a coverage.  "
                                 + "The datsets may be either disjoint or overlapping."
    );
    //Set initial layer values to the first and second layers in the layer list.
    //In #initialize we've already checked that the number of layers >= 2. [Jon Aquino]
    dialog.addLayerComboBox(REF_LAYER, context.getLayerManager().getLayer(0), "The Reference layer is not changed",
                         context.getLayerManager());
    dialog.addLayerComboBox(SUB_LAYER, context.getLayerManager().getLayer(1), "The Subject layer is aligned to the Reference layer",context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, param.distanceTolerance, 4, "The Distance Tolerance determines how large gaps and overlaps can be");
    dialog.addDoubleField(ANG_TOL, param.angleTolerance, 4, "The Angle Tolerance (in degrees) controls how parallel matched segments must be");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    refLyr = dialog.getLayer(REF_LAYER);
    subLyr = dialog.getLayer(SUB_LAYER);
    param.distanceTolerance = dialog.getDouble(DIST_TOL);
    param.angleTolerance = dialog.getDouble(ANG_TOL);
  }
}
